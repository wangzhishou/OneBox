package com.shifenmiao.ai.agent

import android.content.Context
import com.google.gson.Gson
import com.shifenmiao.ai.agent.auth.AuthorizationResult
import com.shifenmiao.ai.agent.auth.ToolAuthorizationGuard
import com.shifenmiao.ai.agent.callback.ToolCallbackRouter
import com.shifenmiao.ai.agent.tool.AgentToolExecutionPolicy
import com.shifenmiao.ai.agent.tool.AgentToolLoginChecker
import com.shifenmiao.ai.agent.tool.AgentToolPermissionRequester
import com.shifenmiao.ai.agent.tool.AgentToolRegistry
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.InteractiveToolRuntime
import com.shifenmiao.core.R
import com.shifenmiao.database.ai.entity.ToolCallTaskEntity
import com.shifenmiao.model.ai.FunctionCall
import com.shifenmiao.model.ai.ToolCall
import com.shifenmiao.model.ai.ToolCallDelta
import com.shifenmiao.model.ai.tool.ToolCatalogItem
import com.shifenmiao.storage.AIChatStorage
import com.shifenmiao.storage.RemoteConfigStorage
import com.t8rin.logger.makeLog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Agent 调用链执行器 —— 管理工具执行、确认与恢复。
 *
 * 核心职责：
 * 1. 执行已构建完成的 ToolCall 列表，并处理确认、失败与恢复。
 * 2. 通过 [ToolCallTaskManager] 在每个执行节点持久化状态到 DB，支持断点续跑。
 * 3. 复用交互运行时承接"工具确认/等待输入"等异步回调场景。
 * 4. 支持无状态纯查询类工具并行执行，提升多工具调用吞吐。
 *
 * 设计原则：
 * - 执行器本身不再持有会话级累积态，避免多个 AI 会话并行时互相污染。
 * - tool_call 累积器与迭代次数统一下沉到 [AgentLoopSessionState]。
 * - LLM  facing 的工具拒绝/失败结果统一使用英文 reason_code，避免多语言模型理解偏差。
 */
@Singleton
class AgentLoopExecutor @Inject constructor(
    val toolRegistry: AgentToolRegistry,
    val taskManager: ToolCallTaskManager,
    private val authorizationGuard: ToolAuthorizationGuard,
    private val permissionRequester: AgentToolPermissionRequester,
    private val loginChecker: AgentToolLoginChecker,
    private val interactiveToolBridge: InteractiveToolRuntime,
    private val gson: Gson,
    @ApplicationContext private val context: Context
) {
    companion object {
        /** 普通工具默认执行超时（毫秒），服务端未下发 agentToolTimeoutSeconds 时的本地兜底 */
        private const val DEFAULT_TOOL_TIMEOUT_MS = 60_000L
    }

    /** 每轮 Agent Loop 开始前调用，重置累积器（不重置迭代计数） */
    fun resetAccumulator(session: AgentLoopSessionState) {
        session.resetAccumulator()
    }

    /** 完全重置（新的聊天轮次开始） */
    fun reset(session: AgentLoopSessionState) {
        session.reset(resolveConfiguredMaxIterations())
    }

    /** 继续执行时扩展可用迭代预算，保留当前已执行轮次 */
    fun extendIterationLimit(session: AgentLoopSessionState) {
        session.ensureIterationLimitAtLeast(resolveConfiguredMaxIterations())
        session.extendIterationLimit(resolveConfiguredMaxIterations())
    }

    /** 递增迭代计数 */
    fun incrementIteration(session: AgentLoopSessionState) {
        session.incrementIteration()
    }

    /** 是否已达最大迭代次数 */
    fun isMaxIterationsReached(session: AgentLoopSessionState): Boolean {
        return session.currentIteration >= currentMaxIterations(session)
    }

    /** 当前会话允许执行到的最大轮次（支持 continue 后动态扩展） */
    fun currentMaxIterations(session: AgentLoopSessionState): Int {
        session.ensureIterationLimitAtLeast(resolveConfiguredMaxIterations())
        return session.currentIterationLimit()
    }

    /**
     * 累积流式返回的 tool_call delta 碎片。
     *
     * OpenAI 流式协议中 tool_calls 是增量返回的：
     * - 第一个 chunk 通常包含 id 和 function.name
     * - 后续 chunk 逐步追加 function.arguments 碎片
     *
     * @param deltas 当前 chunk 中的 ToolCallDelta 列表
     */
    fun accumulateToolCallDeltas(
        session: AgentLoopSessionState,
        deltas: List<ToolCallDelta>
    ) {
        for (delta in deltas) {
            val entry = session.accumulator.getOrPut(delta.index) { AgentLoopSessionState.AccumulatedToolCall() }

            // 首次出现时设置 id 和 type
            delta.id?.let { entry.id = it }
            delta.type?.let { entry.type = it }

            // 增量拼接函数名和参数
            delta.function?.let { funcDelta ->
                funcDelta.name?.let { entry.functionName.append(it) }
                funcDelta.arguments?.let { entry.functionArguments.append(it) }
            }
        }
    }

    /** 检查是否有正在累积的 tool_calls */
    fun hasAccumulatedToolCalls(session: AgentLoopSessionState): Boolean = session.accumulator.isNotEmpty()

    /**
     * 将累积的碎片构建为完整的 ToolCall 列表。
     * 在 finish_reason == "tool_calls" 时调用。
     */
    fun buildCompletedToolCalls(session: AgentLoopSessionState): List<ToolCall> {
        return session.accumulator.entries
            .sortedBy { it.key }
            .map { (_, acc) ->
                ToolCall(
                    id = acc.id,
                    type = acc.type,
                    function = FunctionCall(
                        name = acc.functionName.toString(),
                        arguments = acc.functionArguments.toString()
                    )
                )
            }
    }

    /**
     * 执行一组已完成的 ToolCall，并在每个节点持久化状态到 DB。
     *
     * 执行策略：
     * - 连续的可并行工具批量并发执行（coroutineScope + async）。
     * - 需要确认/登录/权限/交互式工具串行执行，避免弹窗/权限申请冲突。
     *
     * @param toolCalls 完整的工具调用列表
     * @param conversationId 当前会话 ID（用于 DB 持久化）
     * @param completionId 当前消息 completionId（用于 DB 关联）
     * @param onToolStarted 工具开始执行时的回调（用于 UI 更新）
     * @param onToolCompleted 工具执行完成时的回调（用于 UI 更新）
     * @param onToolWaitingInput 交互式工具等待用户输入时的回调
     * @return 每个 toolCall 对应的执行结果
     */
    suspend fun executeToolCalls(
        session: AgentLoopSessionState,
        toolCalls: List<ToolCall>,
        conversationId: String = "",
        completionId: String = "",
        interactionOwnerId: String? = null,
        callbackRouter: ToolCallbackRouter? = null,
        onToolStarted: (ToolCall) -> Unit = {},
        onToolCompleted: (ToolCall, AgentToolResult) -> Unit = { _, _ -> },
        onToolWaitingInput: (ToolCall) -> Unit = {},
        onToolNeedConfirmation: (ToolCall, String) -> Unit = { _, _ -> }
    ): List<Pair<ToolCall, AgentToolResult>> {
        // 批量持久化为 PENDING 状态
        if (conversationId.isNotEmpty()) {
            taskManager.persistNewTasks(
                conversationId = conversationId,
                completionId = completionId,
                iteration = session.currentIteration,
                toolCalls = toolCalls
            )
        }

        return executeBatchedToolCalls(
            toolCalls = toolCalls,
            interactionOwnerId = interactionOwnerId,
            callbackRouter = callbackRouter,
            onToolStarted = onToolStarted,
            onToolCompleted = onToolCompleted,
            onToolWaitingInput = onToolWaitingInput,
            onToolNeedConfirmation = onToolNeedConfirmation,
            persistCallbacks = createPersistCallbacks(conversationId)
        )
    }

    /**
     * 从 DB 恢复执行未完成的工具调用（App 重启后调用）。
     *
     * 只执行状态为 PENDING/EXECUTING 的任务，跳过已 COMPLETED 的。
     * WAITING_INPUT 状态的任务由 InteractiveToolRuntime 单独恢复。
     * 恢复路径同样按并行/串行策略分组执行。
     *
     * @param tasks DB 中查出的未完成任务列表
     * @param onToolStarted 工具开始执行时的回调
     * @param onToolCompleted 工具执行完成时的回调
     * @param onToolWaitingInput 交互式工具等待用户输入时的回调
     * @return 每个 toolCall 对应的执行结果
     */
    suspend fun resumeToolCalls(
        session: AgentLoopSessionState,
        tasks: List<ToolCallTaskEntity>,
        skipConfirmationForTaskIds: Set<String> = emptySet(),
        interactionOwnerId: String? = null,
        callbackRouter: ToolCallbackRouter? = null,
        onToolStarted: (ToolCall) -> Unit = {},
        onToolCompleted: (ToolCall, AgentToolResult) -> Unit = { _, _ -> },
        onToolWaitingInput: (ToolCall) -> Unit = {},
        onToolNeedConfirmation: (ToolCall, String) -> Unit = { _, _ -> }
    ): List<Pair<ToolCall, AgentToolResult>> {
        val results = mutableListOf<Pair<ToolCall, AgentToolResult>>()

        // 先过滤出需要恢复的任务，构建 ToolCall 列表
        val pendingTasks = mutableListOf<ToolCallTaskEntity>()
        for (task in tasks) {
            if (task.status == ToolCallTaskEntity.Status.COMPLETED ||
                task.status == ToolCallTaskEntity.Status.FAILED
            ) {
                if (task.status == ToolCallTaskEntity.Status.COMPLETED && task.result != null) {
                    val toolCall = ToolCall(
                        id = task.id,
                        type = "function",
                        function = FunctionCall(name = task.toolName, arguments = task.arguments)
                    )
                    results.add(toolCall to AgentToolResult(task.result!!, task.isError))
                }
                continue
            }
            pendingTasks.add(task)
        }

        val toolCalls = pendingTasks.map { task ->
            ToolCall(
                id = task.id,
                type = "function",
                function = FunctionCall(name = task.toolName, arguments = task.arguments)
            )
        }

        val persistCallbacks = PersistCallbacks(
            onExecuting = { id -> taskManager.markExecuting(id) },
            onCompleted = { id, content, isError -> taskManager.markCompleted(id, content, isError) },
            onFailed = { id, message -> taskManager.markFailed(id, message) }
        )

        results.addAll(
            executeBatchedToolCalls(
                toolCalls = toolCalls,
                interactionOwnerId = interactionOwnerId,
                callbackRouter = callbackRouter,
                skipConfirmationForTaskIds = skipConfirmationForTaskIds,
                onToolStarted = onToolStarted,
                onToolCompleted = onToolCompleted,
                onToolWaitingInput = onToolWaitingInput,
                onToolNeedConfirmation = onToolNeedConfirmation,
                persistCallbacks = persistCallbacks
            )
        )

        return results
    }

    /**
     * 按并行/串行策略分批执行工具调用。
     * executeToolCalls 和 resumeToolCalls 共享此方法，避免重复的批次逻辑。
     */
    private suspend fun executeBatchedToolCalls(
        toolCalls: List<ToolCall>,
        interactionOwnerId: String?,
        callbackRouter: ToolCallbackRouter?,
        skipConfirmationForTaskIds: Set<String> = emptySet(),
        onToolStarted: (ToolCall) -> Unit,
        onToolCompleted: (ToolCall, AgentToolResult) -> Unit,
        onToolWaitingInput: (ToolCall) -> Unit,
        onToolNeedConfirmation: (ToolCall, String) -> Unit,
        persistCallbacks: PersistCallbacks
    ): List<Pair<ToolCall, AgentToolResult>> {
        val results = mutableListOf<Pair<ToolCall, AgentToolResult>>()

        var i = 0
        while (i < toolCalls.size) {
            val parallelBatch = mutableListOf<ToolCall>()
            while (i < toolCalls.size && isEffectivelyParallelizable(toolCalls[i])) {
                parallelBatch.add(toolCalls[i])
                i++
            }

            if (parallelBatch.isNotEmpty()) {
                val batchResults = coroutineScope {
                    parallelBatch.map { toolCall ->
                        async {
                            executeSingleToolCall(
                                toolCall = toolCall,
                                interactionOwnerId = interactionOwnerId,
                                callbackRouter = callbackRouter,
                                skipConfirmation = toolCall.id in skipConfirmationForTaskIds,
                                onToolStarted = onToolStarted,
                                onToolCompleted = onToolCompleted,
                                onToolWaitingInput = onToolWaitingInput,
                                onToolNeedConfirmation = onToolNeedConfirmation,
                                onPersistExecuting = persistCallbacks.onExecuting,
                                onPersistCompleted = persistCallbacks.onCompleted,
                                onPersistFailed = persistCallbacks.onFailed
                            )
                        }
                    }.awaitAll()
                }
                results.addAll(batchResults)
            }

            if (i < toolCalls.size) {
                results.add(
                    executeSingleToolCall(
                        toolCall = toolCalls[i],
                        interactionOwnerId = interactionOwnerId,
                        callbackRouter = callbackRouter,
                        skipConfirmation = toolCalls[i].id in skipConfirmationForTaskIds,
                        onToolStarted = onToolStarted,
                        onToolCompleted = onToolCompleted,
                        onToolWaitingInput = onToolWaitingInput,
                        onToolNeedConfirmation = onToolNeedConfirmation,
                        onPersistExecuting = persistCallbacks.onExecuting,
                        onPersistCompleted = persistCallbacks.onCompleted,
                        onPersistFailed = persistCallbacks.onFailed
                    )
                )
                i++
            }
        }

        return results
    }

    /** 为指定 conversationId 创建持久化回调（空 conversationId 则不持久化） */
    private fun createPersistCallbacks(conversationId: String): PersistCallbacks {
        if (conversationId.isEmpty()) {
            return PersistCallbacks()
        }
        return PersistCallbacks(
            onExecuting = { id -> taskManager.markExecuting(id) },
            onCompleted = { id, content, isError -> taskManager.markCompleted(id, content, isError) },
            onFailed = { id, message -> taskManager.markFailed(id, message) }
        )
    }

    /** 持久化回调容器，消除 executeToolCalls / resumeToolCalls 中 6 处重复的 lambda 定义 */
    private data class PersistCallbacks(
        val onExecuting: suspend (String) -> Unit = {},
        val onCompleted: suspend (String, String, Boolean) -> Unit = { _, _, _ -> },
        val onFailed: suspend (String, String) -> Unit = { _, _ -> }
    )

    /**
     * 执行单个工具调用（用于 callback 场景和恢复场景）。
     *
     * 与 executeToolCalls() 批量执行的区别：
     * 1. 只执行一个指定工具，不进入完整的 Agent Loop
     * 2. 支持传入 callbackRouter，让工具内部可以继续发起 callback
     * 3. 不写入 ToolCallTaskManager（因为 callback 链的工具调用不需要持久化到 DB）
     * 4. 返回原始 AgentToolResult，由调用方包装为 CallbackResult
     *
     * @param toolName 要执行的工具名
     * @param arguments 工具参数（JSON 字符串）
     * @param callbackRouter 回调路由器，支持嵌套调用
     * @return 工具执行结果
     */
    suspend fun executeSingleTool(
        toolName: String,
        arguments: String,
        interactionOwnerId: String? = null,
        callbackRouter: ToolCallbackRouter? = null
    ): AgentToolResult {
        val singleToolCallId = "single_${toolName}_${System.currentTimeMillis()}"
        val singleToolCall = ToolCall(
            id = singleToolCallId,
            type = "function",
            function = FunctionCall(name = toolName, arguments = arguments)
        )
        evaluateToolExecutionGuards(
            toolCall = singleToolCall,
            interactionOwnerId = interactionOwnerId
        )?.let { return it }

        return try {
            if (callbackRouter != null) {
                // callback 子工具也统一通过 registry 注入 executionContext，
                // 避免主链路和子链路在 toolCallId / interactionOwnerId 上再次分叉。
                toolRegistry.executeToolWithCallback(
                    toolName = toolName,
                    arguments = arguments,
                    callback = callbackRouter,
                    toolCallId = singleToolCallId,
                    interactionOwnerId = interactionOwnerId
                )
            } else {
                toolRegistry.executeTool(
                    toolName = toolName,
                    arguments = arguments,
                    toolCallId = singleToolCallId,
                    interactionOwnerId = interactionOwnerId
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AgentToolResult("Tool execution failed: ${e.message}", isError = true)
        }
    }

    private suspend fun executeToolCallWithOptionalCallback(
        toolName: String,
        arguments: String,
        toolCallId: String,
        interactionOwnerId: String?,
        callbackRouter: ToolCallbackRouter?
    ): AgentToolResult {
        return if (callbackRouter != null) {
            toolRegistry.executeToolWithCallback(
                toolName = toolName,
                arguments = arguments,
                callback = callbackRouter,
                toolCallId = toolCallId,
                interactionOwnerId = interactionOwnerId
            )
        } else {
            toolRegistry.executeTool(
                toolName = toolName,
                arguments = arguments,
                toolCallId = toolCallId,
                interactionOwnerId = interactionOwnerId
            )
        }
    }

    /**
     * 将工具调用链序列化为 JSON 字符串，用于持久化到 MessageEntity.toolCalls。
     */
    fun serializeToolCallsChain(
        toolCalls: List<ToolCall>,
        results: List<Pair<ToolCall, AgentToolResult>>
    ): String {
        val chain = results.map { (call, result) ->
            ToolCallRecord(
                id = call.id,
                name = call.function.name,
                arguments = call.function.arguments,
                result = result.content,
                isError = result.isError
            )
        }
        return try {
            gson.toJson(chain)
        } catch (e: Exception) {
            "[]"
        }
    }

    /**
     * 从 JSON 反序列化工具调用链记录。
     */
    fun deserializeToolCallsChain(json: String): List<ToolCallRecord> {
        if (json.isBlank()) return emptyList()
        return try {
            val type = object : com.google.gson.reflect.TypeToken<List<ToolCallRecord>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 判断工具在当前上下文中是否可以并行执行。
     *
     * 并行条件（需全部满足）：
     * 1. 工具声明 parallelizable = true
     * 2. 非交互式工具
     * 3. 不需要登录
     * 4. 不需要确认
     * 5. 不需要权限申请
     */
    /**
     * 超时优先级:工具自身声明(ToolExecutionConfig.executionTimeoutMs)
     * > 远程配置(RemoteConfig.agentToolTimeoutSeconds) > 本地默认 60s。
     */
    private fun resolveExecutionTimeout(toolName: String): Long? {
        if (toolRegistry.isInteractiveTool(toolName)) return null
        val toolTimeout = toolRegistry.getExecutionPolicy(toolName)?.executionTimeoutMs ?: 0L
        if (toolTimeout > 0) return toolTimeout
        return RemoteConfigStorage.getRemoteConfig().agentToolTimeoutSeconds
            ?.takeIf { it > 0 }
            ?.times(1000L)
            ?: DEFAULT_TOOL_TIMEOUT_MS
    }

    private fun isEffectivelyParallelizable(toolCall: ToolCall): Boolean {
        val policy = toolRegistry.getExecutionPolicy(
            toolName = toolCall.function.name,
            arguments = toolCall.function.arguments
        ) ?: return false
        return policy.parallelizable
                && !policy.isInteractive
                && !policy.requiresConfirmation
                && !policy.requiresLogin
                && policy.requiredPermissions.isEmpty()
    }

    private fun resolveConfiguredMaxIterations(): Int {
        return AIChatStorage.maxAgentIterations.value
            .coerceIn(
                AIChatStorage.MIN_MAX_AGENT_ITERATIONS,
                AIChatStorage.MAX_MAX_AGENT_ITERATIONS
            )
    }

    /**
     * 执行单个 ToolCall 的完整生命周期（guard → 执行 → 持久化 → 回调）。
     * 供 executeToolCalls / resumeToolCalls 复用，避免并行/串行两套重复代码。
     */
    private suspend fun executeSingleToolCall(
        toolCall: ToolCall,
        interactionOwnerId: String?,
        callbackRouter: ToolCallbackRouter?,
        skipConfirmation: Boolean = false,
        onToolStarted: (ToolCall) -> Unit,
        onToolCompleted: (ToolCall, AgentToolResult) -> Unit,
        onToolWaitingInput: (ToolCall) -> Unit,
        onToolNeedConfirmation: (ToolCall, String) -> Unit,
        onPersistExecuting: (suspend (String) -> Unit)? = null,
        onPersistCompleted: (suspend (String, String, Boolean) -> Unit)? = null,
        onPersistFailed: (suspend (String, String) -> Unit)? = null
    ): Pair<ToolCall, AgentToolResult> {
        onToolStarted(toolCall)

        val guardResult = evaluateToolExecutionGuards(
            toolCall = toolCall,
            interactionOwnerId = interactionOwnerId,
            skipConfirmation = skipConfirmation,
            onToolNeedConfirmation = onToolNeedConfirmation
        )
        if (guardResult != null) {
            onPersistCompleted?.invoke(toolCall.id, guardResult.content, guardResult.isError)
            onToolCompleted(toolCall, guardResult)
            return toolCall to guardResult
        }

        onPersistExecuting?.invoke(toolCall.id)

        if (toolRegistry.isInteractiveTool(toolCall.function.name)) {
            onToolWaitingInput(toolCall)
        }

        val timeoutMs = resolveExecutionTimeout(toolCall.function.name)
        val result = try {
            val rawResult = if (timeoutMs != null) {
                withTimeoutOrNull(timeoutMs) {
                    executeToolCallWithOptionalCallback(
                        toolName = toolCall.function.name,
                        arguments = toolCall.function.arguments,
                        toolCallId = toolCall.id,
                        interactionOwnerId = interactionOwnerId,
                        callbackRouter = callbackRouter
                    )
                }
            } else {
                executeToolCallWithOptionalCallback(
                    toolName = toolCall.function.name,
                    arguments = toolCall.function.arguments,
                    toolCallId = toolCall.id,
                    interactionOwnerId = interactionOwnerId,
                    callbackRouter = callbackRouter
                )
            }
            if (rawResult != null) {
                onPersistCompleted?.invoke(toolCall.id, rawResult.content, rawResult.isError)
                onToolCompleted(toolCall, rawResult)
                "Tool '${toolCall.function.name}' executed: isError=${rawResult.isError}, contentLen=${rawResult.content.length}"
                    .makeLog("AgentLoopExecutor")
                return toolCall to rawResult
            }
            // 超时（仅普通工具会走到这里）
            val timeoutSeconds = (timeoutMs ?: 0L) / 1000
            val timeoutMsg = context.getString(R.string.agent_tool_execution_timeout, timeoutSeconds)
            "Tool '${toolCall.function.name}' timed out after ${timeoutMs}ms"
                .makeLog("AgentLoopExecutor")
            AgentToolResult(content = timeoutMsg, isError = true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            "Tool '${toolCall.function.name}' failed: ${e.message}"
                .makeLog("AgentLoopExecutor")
            AgentToolResult(
                content = "Tool execution failed: ${e.message ?: "Unknown error"}",
                isError = true
            )
        }

        // 仅在异常/超时路径到达此处（正常路径已提前 return）
        onPersistFailed?.invoke(toolCall.id, result.content)
        onToolCompleted(toolCall, result)
        return toolCall to result
    }

    private suspend fun evaluateToolExecutionGuards(
        toolCall: ToolCall,
        interactionOwnerId: String?,
        skipConfirmation: Boolean = false,
        onToolNeedConfirmation: (ToolCall, String) -> Unit = { _, _ -> }
    ): AgentToolResult? {
        val toolName = toolCall.function.name
        val arguments = toolCall.function.arguments
        val metadata = toolRegistry.getToolCatalogItem(toolName)
        when (val authResult = authorizationGuard.evaluate(
            toolName = toolName,
            metadata = metadata
        )) {
            is AuthorizationResult.Denied -> return AgentToolResult(authResult.reason, isError = true)
            is AuthorizationResult.NeedConfirmation -> Unit // Legacy branch; confirmation is handled by execution policy below.
            AuthorizationResult.Allowed -> Unit
        }

        toolRegistry.validatePreExecutionExpression(toolName, arguments)?.let { return it }

        val policy = toolRegistry.getExecutionPolicy(
            toolName = toolName,
            arguments = arguments
        )

        if (policy?.requiresLogin == true && !loginChecker.isLoggedIn()) {
            return buildLoginRequiredResult(toolName)
        }

        if (policy != null && policy.requiredPermissions.isNotEmpty()) {
            val granted = permissionRequester.requestPermissions(
                permissions = policy.requiredPermissions,
                permissionRequest = policy.permissionRequest
            )
            if (!granted) {
                return buildPermissionDeniedResult(toolName, policy)
            }
        }

        if (policy?.requiresConfirmation == true && !skipConfirmation) {
            val reason = policy.confirmationToolPresentation
                .takeIf { it.isNotBlank() }
                ?: metadata?.summary?.takeIf { it.isNotBlank() }
                ?: metadata?.description?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.agent_tool_confirmation_required)
            onToolNeedConfirmation(toolCall, reason)
            val confirmation = requestToolConfirmation(
                toolCallId = toolCall.id,
                toolName = toolName,
                metadata = metadata,
                reason = reason,
                interactionOwnerId = interactionOwnerId,
                arguments = arguments,
                policy = policy
            )
            if (!confirmation.approved) {
                return buildRejectedConfirmationResult(toolName, reason)
            }
        }

        return null
    }

    private suspend fun requestToolConfirmation(
        toolCallId: String,
        toolName: String,
        metadata: ToolCatalogItem?,
        reason: String,
        interactionOwnerId: String? = null,
        arguments: String = "",
        policy: AgentToolExecutionPolicy? = null
    ): ToolConfirmationDecision {
        val title = metadata?.title?.takeIf { it.isNotBlank() } ?: toolName
        val summary = metadata?.summary?.takeIf { it.isNotBlank() }
            ?: metadata?.description?.takeIf { it.isNotBlank() }
            ?: reason
        val dialogTitle = policy?.confirmationTitle?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.agent_tool_confirm_title, title)
        val dialogMessage = policy?.confirmationToolPresentation?.takeIf { it.isNotBlank() }
            ?: summary
        val result = interactiveToolBridge.requestConfirmation(
            toolCallId = toolCallId,
            toolName = toolName,
            interactionOwnerId = interactionOwnerId,
            dialogTitle = dialogTitle,
            dialogMessage = buildString {
                append(dialogMessage)
                if (metadata?.category != null) {
                    append("\n\n")
                    append(context.getString(R.string.agent_tool_confirm_category, metadata.category.name))
                }
                if (metadata?.riskLevel != null) {
                    append("\n")
                    append(context.getString(R.string.agent_tool_confirm_risk, metadata.riskLevel.name))
                }
                if (arguments.isNotBlank() && arguments != "{}") {
                    append("\n")
                    append(
                        context.getString(
                            R.string.agent_tool_confirm_arguments,
                            arguments.take(200) + if (arguments.length > 200) "…" else ""
                        )
                    )
                }
            },
            confirmPayload = """{"decision":"approved"}""",
            dismissPayload = """{"decision":"rejected"}""",
            submitButtonText = context.getString(R.string.agent_tool_confirm_approve),
            cancelButtonText = context.getString(R.string.agent_tool_confirm_reject)
        )
        return if (result?.contains("\"approved\"") == true) {
            ToolConfirmationDecision(approved = true)
        } else {
            ToolConfirmationDecision(approved = false)
        }
    }

    private fun buildPermissionDeniedResult(
        toolName: String,
        policy: AgentToolExecutionPolicy
    ): AgentToolResult {
        return AgentToolResult(
            content = gson.toJson(
                mapOf(
                    "toolName" to toolName,
                    "executed" to false,
                    "reason_code" to "permission_denied",
                    "message" to "User did not grant the required permissions for this tool",
                    "permissionRequest" to policy.permissionRequest.name,
                    "permissions" to policy.requiredPermissions
                )
            ),
            isError = true
        )
    }

    private fun buildLoginRequiredResult(toolName: String): AgentToolResult {
        return AgentToolResult(
            content = gson.toJson(
                mapOf(
                    "toolName" to toolName,
                    "executed" to false,
                    "reason_code" to "login_required",
                    "message" to "This tool requires the user to be logged in",
                    "requiresLogin" to true
                )
            ),
            isError = true
        )
    }

    private fun buildRejectedConfirmationResult(
        toolName: String,
        reason: String
    ): AgentToolResult {
        return AgentToolResult(
            content = gson.toJson(
                mapOf(
                    "toolName" to toolName,
                    "decision" to "rejected",
                    "executed" to false,
                    "reason_code" to "user_rejected",
                    "message" to "User rejected the tool execution",
                    "reason" to reason
                )
            ),
            isError = false
        )
    }
}

private data class ToolConfirmationDecision(
    val approved: Boolean
)

/**
 * 工具调用记录，用于持久化存储和历史回放。
 */
data class ToolCallRecord(
    val id: String,
    val name: String,
    val arguments: String,
    val result: String,
    val isError: Boolean = false,
    val displayTitle: String? = null,
    val displaySummary: String? = null
) {
    companion object {
        private val gson = com.google.gson.Gson()

        /** 从 JSON 字符串解析 ToolCallRecord 列表，解析失败返回空列表 */
        fun parseFromJson(json: String): List<ToolCallRecord> {
            if (json.isBlank()) return emptyList()
            return runCatching {
                val type = object : com.google.gson.reflect.TypeToken<List<ToolCallRecord>>() {}.type
                gson.fromJson<List<ToolCallRecord>>(json, type).orEmpty()
            }.getOrElse { emptyList() }
        }

        /** 将单条记录追加到已有 JSON 中（按 id 去重），返回合并后的 JSON */
        fun appendToJson(existingJson: String, record: ToolCallRecord): String {
            val merged = LinkedHashMap<String, ToolCallRecord>()
            parseFromJson(existingJson).forEach { merged[it.id] = it }
            merged[record.id] = record
            return gson.toJson(merged.values.toList())
        }

        /** 合并两组记录（按 id 去重，incoming 覆盖 existing） */
        fun merge(
            existing: List<ToolCallRecord>,
            incoming: List<ToolCallRecord>,
        ): List<ToolCallRecord> {
            if (existing.isEmpty()) return incoming
            if (incoming.isEmpty()) return existing
            val merged = LinkedHashMap<String, ToolCallRecord>()
            existing.forEach { merged[it.id] = it }
            incoming.forEach { merged[it.id] = it }
            return merged.values.toList()
        }
    }
}
