package com.shifenmiao.ai.component

import android.content.Context
import com.google.gson.Gson
import com.shifenmiao.ai.BuildConfig
import com.shifenmiao.ai.agent.AgentLoopExecutor
import com.shifenmiao.ai.agent.AgentLoopSessionState
import com.shifenmiao.ai.agent.ToolCallRecord
import com.shifenmiao.ai.agent.tool.AgentToolRegistry
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.context.ContextWindowManager
import com.shifenmiao.ai.context.TokenBudgetTracker
import com.shifenmiao.ai.context.ToolResultTruncator
import com.shifenmiao.ai.service.PromptAssemblyService
import com.shifenmiao.ai.execution.model.ExecutionStepStatus
import com.shifenmiao.ai.execution.presenter.ToolExecutionTextResolver
import com.shifenmiao.ai.utils.AiUtils
import com.shifenmiao.model.ai.AttachedMedia
import com.shifenmiao.core.R
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.database.image.dao.ImageDao
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.ToolCall
import com.shifenmiao.model.ai.ToolDefinition
import com.shifenmiao.model.ai.unified.LlmBuiltinTool
import com.shifenmiao.model.ai.unified.LlmMessage
import com.shifenmiao.model.ai.unified.LlmTurnRequest
import com.t8rin.logger.makeLog

/**
 * Agent Loop 执行器 —— 负责 Agent Loop 的核心执行循环。
 *
 * 从 AgentLoopOrchestrator 中抽离，职责边界：
 * 1. Agent Loop 主循环（while 迭代控制）
 * 2. 单次工具迭代执行
 * 3. LLM follow-up 请求
 * 4. 上下文消息构建与工具结果追加
 * 5. 工具调用链持久化
 *
 * 设计原则：
 * - 核心逻辑与 UI 分离：执行器不直接操作 UI 状态
 * - 通过回调向调用方报告执行事件（工具开始/完成/等待输入等）
 * - [AgentLoopSessionState] 由外部传入，因为生命周期由 Orchestrator 管理
 */
class AgentLoopRunner(
    private val agentLoopExecutor: AgentLoopExecutor,
    private val agentToolRegistry: AgentToolRegistry,
    private val promptAssemblyService: PromptAssemblyService,
    private val gson: Gson,
    private val contentReader: suspend (String) -> String?,
    private val imageDao: ImageDao,
    private val streamCollector: StreamCollector,
    private val remoteRequestProvider: RemoteRequestProvider,
    private val modeTransitionManager: ModeTransitionManager,
    private val applicationContext: Context,
) {

    /**
     * Agent Loop 执行结果。
     *
     * @param toolCallsChainJson 工具调用链 JSON，用于持久化
     * @param maxIterationsReached 是否达到最大迭代次数
     * @param pausedContext 达到最大迭代时的暂停上下文，用于后续 continueAgentLoop
     */
    data class RunResult(
        val toolCallsChainJson: String,
        val maxIterationsReached: Boolean,
        val pausedContext: PausedContext? = null,
    )

    /** 暂停上下文，用于在达到最大迭代后恢复执行 */
    data class PausedContext(
        val effectiveConversation: Conversation,
        val originalQuestionMessages: List<MessageEntity>,
        val enableWebSearch: Boolean,
        val tools: List<ToolDefinition>?,
    )

    /**
     * 执行 Agent Loop 主循环。
     *
     * 循环执行工具调用 → LLM follow-up，直到：
     * - LLM 不再发起工具调用
     * - 达到最大迭代次数
     * - 执行过程中出现异常
     *
     * @param session Agent Loop 会话状态
     * @param interactionOwnerId 交互所有者 ID
     * @param callbackRouter 工具回调路由
     * @param effectiveConversation 有效会话（含系统 prompt）
     * @param originalQuestionMessages 原始问题消息
     * @param enableWebSearch 是否启用网络搜索
     * @param enableReasoning 是否启用推理模式
     * @param tools 工具定义列表
     * @param conversation 当前会话（用于构建 LLM 请求参数）
     * @param conversationId 会话 ID
     * @param completionId 补全 ID
     * @param answerProvider 当前回答文本提供者
     * @param reasoningContentProvider 当前推理文本提供者
     * @param previousResponseIdProvider 上一次响应 ID 提供者
     * @param callback Agent Loop 执行回调，统一收口所有 UI/状态事件
     */
    suspend fun execute(
        session: AgentLoopSessionState,
        interactionOwnerId: String,
        callbackRouter: com.shifenmiao.ai.agent.callback.ToolCallbackRouter,
        conversation: Conversation,
        effectiveConversation: Conversation,
        originalQuestionMessages: List<MessageEntity>,
        enableWebSearch: Boolean,
        enableReasoning: Boolean,
        tools: List<ToolDefinition>?,
        conversationId: String,
        completionIdProvider: () -> String,
        answerProvider: () -> String,
        reasoningContentProvider: () -> String,
        previousResponseIdProvider: () -> String,
        callback: AgentLoopCallback,
    ): RunResult {
        callback.onEnsureStartingHint()
        val contextMessages = buildContextMessages(effectiveConversation, originalQuestionMessages)

        // Phase 3.1: 初始化全局 token 预算追踪
        val budgetTracker = TokenBudgetTracker.forModel(conversation.engine.model)
        val (systemMsgs, historyMsgs) = contextMessages.partition { it.role == "system" }
        budgetTracker.recordSystemTokens(systemMsgs)
        budgetTracker.recordInitialHistory(historyMsgs)

        val executionContext = ExecutionContext(
            effectiveConversation = effectiveConversation,
            originalQuestionMessages = originalQuestionMessages,
            enableWebSearch = enableWebSearch,
            initialTools = tools,
            activeTools = tools,
            contextMessages = contextMessages,
        )

        val allToolCallResults = mutableListOf<Pair<ToolCall, AgentToolResult>>()
        val allStepDescriptors = mutableListOf<ToolStepDescriptor>()
        val allStepStatuses = linkedMapOf<String, ExecutionStepStatus>()
        val allStepResults = linkedMapOf<String, String>()
        var activeTools = tools

        // P0 #1+#13: 跨回合跟踪"已被 LLM context 消费过的" answer / reasoning 文本快照,
        // 避免每回合 assistant 消息 content 重复塞入前序所有回合的累积内容.
        // 初始为空串, 表示"此前没有任何 assistant 消息携带 LLM 文本".
        var consumedAnswerSnapshot = ""
        var consumedReasoningSnapshot = ""

        while (!agentLoopExecutor.isMaxIterationsReached(session)) {
            val iterationResult = executeToolIteration(
                session = session,
                executionContext = executionContext,
                activeTools = activeTools,
                allToolCallResults = allToolCallResults,
                allStepDescriptors = allStepDescriptors,
                allStepStatuses = allStepStatuses,
                allStepResults = allStepResults,
                interactionOwnerId = interactionOwnerId,
                callbackRouter = callbackRouter,
                conversationId = conversationId,
                completionIdProvider = completionIdProvider,
                answerProvider = answerProvider,
                reasoningContentProvider = reasoningContentProvider,
                budgetTracker = budgetTracker,
                callback = callback,
                previousAnswerSnapshot = consumedAnswerSnapshot,
                previousReasoningSnapshot = consumedReasoningSnapshot,
            ) ?: break

            // 回合结束: 当前 answer / reasoning 已被 appendToolResultsToContext 消费过,
            // 下一回合用此刻的值作为快照基线.
            consumedAnswerSnapshot = answerProvider()
            consumedReasoningSnapshot = reasoningContentProvider()

            activeTools = iterationResult.activeTools
            executionContext.activeTools = activeTools
            // Phase 3.2: 发送 follow-up 前检查上下文预算，必要时裁剪
            if (budgetTracker.needsTruncation) {
                val before = executionContext.contextMessages.size
                val fitted = ContextWindowManager.fitToContextWindow(
                    messages = executionContext.contextMessages,
                    contextWindowTokens = conversation.engine.model.effectiveContextWindow(),
                )
                executionContext.contextMessages.clear()
                executionContext.contextMessages.addAll(fitted)
                "AgentLoopRunner: context truncated $before → ${fitted.size} msgs " +
                    "(usage=${budgetTracker.currentUsage}/${budgetTracker.budget})"
                    .makeLog("AgentLoopRunner")
            }
            budgetTracker.snapshot()

            val hasMoreToolCalls = requestLlmFollowUp(
                session = session,
                conversation = conversation,
                questionMessages = executionContext.originalQuestionMessages,
                contextMessages = executionContext.contextMessages,
                toolResultMessages = iterationResult.toolResultMessages,
                activeTools = activeTools,
                enableWebSearch = executionContext.enableWebSearch,
                enableReasoning = enableReasoning,
                iteration = iterationResult.iteration,
                stepDescriptors = iterationResult.stepDescriptors,
                stepStatuses = iterationResult.stepStatuses,
                stepResults = allStepResults,
                watchdogReason = "executeAgentLoop:iter=${iterationResult.iteration}",
                previousResponseIdProvider = previousResponseIdProvider,
                callback = callback,
            )
            if (!hasMoreToolCalls) break
        }

        return finalizeExecutionLoop(session, executionContext, allToolCallResults, conversationId)
    }

    /**
     * 构建上下文消息列表。
     *
     * 将数据库消息转换为 LLM 消息格式，并注入模式切换标记。
     */
    suspend fun buildContextMessages(
        conversation: Conversation,
        sourceMessages: List<MessageEntity>,
    ): MutableList<LlmMessage> {
        val messages = AiUtils.buildLlmMessages(
            conversation,
            sourceMessages,
            contentReader = contentReader,
            imageDao = imageDao,
        ).toMutableList()
        modeTransitionManager.injectTransitionMarker(messages)
        return messages
    }

    /**
     * 将已完成的任务结果追加到上下文消息中（用于恢复场景）。
     */
    fun appendCompletedTasksToContext(
        contextMessages: MutableList<LlmMessage>,
        toolCalls: List<ToolCall>,
        completedTasks: List<com.shifenmiao.database.ai.entity.ToolCallTaskEntity>,
    ): List<LlmMessage> {
        contextMessages.add(LlmMessage.createAssistantToolCallMessage(toolCalls))
        return completedTasks.map { task ->
            LlmMessage.createToolResultMessage(
                toolCallId = task.id,
                toolName = task.toolName,
                content = task.result ?: "",
            ).also(contextMessages::add)
        }
    }

    /**
     * 解析工具步骤描述符。
     */
    suspend fun resolveToolStepDescriptors(toolCalls: List<ToolCall>): List<ToolStepDescriptor> {
        return toolCalls.map { toolCall ->
            val metadata = agentToolRegistry.getToolByName(toolCall.function.name)
            ToolStepDescriptor(
                id = toolCall.id,
                toolName = toolCall.function.name,
                title = ToolExecutionTextResolver.resolveTitle(
                    toolName = toolCall.function.name,
                    preferredTitle = metadata?.title,
                ),
                summary = metadata?.summary?.takeIf { it.isNotBlank() },
                arguments = toolCall.function.arguments
                    .takeIf { it.isNotBlank() && it != "{}" },
                debugInfo = buildString {
                    append("tool=")
                    append(toolCall.function.name)
                    append(" · callId=")
                    append(toolCall.id)
                },
            )
        }
    }

    // ---- private helpers ----

    private suspend fun executeToolIteration(
        session: AgentLoopSessionState,
        executionContext: ExecutionContext,
        activeTools: List<ToolDefinition>?,
        allToolCallResults: MutableList<Pair<ToolCall, AgentToolResult>>,
        allStepDescriptors: MutableList<ToolStepDescriptor>,
        allStepStatuses: LinkedHashMap<String, ExecutionStepStatus>,
        allStepResults: LinkedHashMap<String, String>,
        interactionOwnerId: String,
        callbackRouter: com.shifenmiao.ai.agent.callback.ToolCallbackRouter,
        conversationId: String,
        completionIdProvider: () -> String,
        answerProvider: () -> String,
        reasoningContentProvider: () -> String,
        budgetTracker: TokenBudgetTracker,
        callback: AgentLoopCallback,
        previousAnswerSnapshot: String,
        previousReasoningSnapshot: String,
    ): IterationResult? {
        agentLoopExecutor.incrementIteration(session)
        val iteration = session.currentIteration

        val completedToolCalls = agentLoopExecutor.buildCompletedToolCalls(session)
        if (completedToolCalls.isEmpty()) return null
        logToolTrace(
            "actual_tool_calls iteration=$iteration names=${completedToolCalls.map { it.function.name }} " +
                "ids=${completedToolCalls.map { it.id }}"
        )

        val stepDescriptors = resolveToolStepDescriptors(completedToolCalls)
        allStepDescriptors.addAll(stepDescriptors)
        stepDescriptors.forEach { descriptor ->
            allStepStatuses[descriptor.id] = ExecutionStepStatus.PENDING
        }
        val stepStatuses = allStepStatuses
        val stepResults = allStepResults
        val visibleStepDescriptors = allStepDescriptors.toList()

        fun buildVisibleExecutionSteps(finalStepStatus: ExecutionStepStatus) =
            buildExecutionSteps(visibleStepDescriptors, stepStatuses, stepResults, finalStepStatus)

        callback.onShowExecuting(
            completedToolCalls, null, null, iteration,
            buildVisibleExecutionSteps(ExecutionStepStatus.PENDING),
        )

        val results = agentLoopExecutor.executeToolCalls(
            session = session,
            toolCalls = completedToolCalls,
            conversationId = conversationId,
            completionId = completionIdProvider(),
            interactionOwnerId = interactionOwnerId,
            callbackRouter = callbackRouter,
            onToolStarted = { toolCall ->
                callback.onResetStreamWatchdog()
                stepStatuses[toolCall.id] = ExecutionStepStatus.RUNNING
                val stepTitle = stepDescriptors.firstOrNull { it.id == toolCall.id }?.title
                    ?: ToolExecutionTextResolver.resolveTitle(toolCall.function.name)
                callback.onShowExecuting(
                    completedToolCalls, stepTitle, toolCall.id, iteration,
                    buildVisibleExecutionSteps(ExecutionStepStatus.PENDING),
                )
                callback.onToolStarted(toolCall, stepTitle)
            },
            onToolCompleted = { toolCall, result ->
                callback.onResetStreamWatchdog()
                stepStatuses[toolCall.id] = if (result.isError) {
                    ExecutionStepStatus.FAILED
                } else {
                    ExecutionStepStatus.DONE
                }
                if (result.content.isNotBlank()) {
                    stepResults[toolCall.id] = result.content
                }
                val stepTitle = stepDescriptors.firstOrNull { it.id == toolCall.id }?.title
                    ?: ToolExecutionTextResolver.resolveTitle(toolCall.function.name)
                callback.onShowExecuting(
                    completedToolCalls, null, null, iteration,
                    buildVisibleExecutionSteps(ExecutionStepStatus.PENDING),
                )
                callback.onToolCompleted(toolCall, result, stepTitle)
            },
            onToolWaitingInput = { toolCall ->
                callback.onResetStreamWatchdog()
                stepStatuses[toolCall.id] = ExecutionStepStatus.WAITING_USER
                val stepTitle = stepDescriptors.firstOrNull { it.id == toolCall.id }?.title
                    ?: ToolExecutionTextResolver.resolveTitle(toolCall.function.name)
                callback.onToolWaitingInput(toolCall, stepTitle)
            },
            onToolNeedConfirmation = { toolCall, _ ->
                callback.onResetStreamWatchdog()
                stepStatuses[toolCall.id] = ExecutionStepStatus.WAITING_USER
                callback.onToolNeedConfirmation(toolCall)
            },
        )
        allToolCallResults.addAll(results)

        val nextActiveTools = promptAssemblyService.buildFollowUpToolsAfterDiscovery(
            results = results,
            currentTools = activeTools,
        )
        val appendedMessages = appendToolResultsToContext(
            contextMessages = executionContext.contextMessages,
            completedToolCalls = completedToolCalls,
            results = results,
            answerProvider = answerProvider,
            reasoningContentProvider = reasoningContentProvider,
            previousAnswer = previousAnswerSnapshot,
            previousReasoning = previousReasoningSnapshot,
        )
        // Phase 3.1: 追踪累积 token
        budgetTracker.addToolResultTokens(appendedMessages)
        agentLoopExecutor.resetAccumulator(session)

        return IterationResult(
            iteration = iteration,
            activeTools = nextActiveTools,
            toolResultMessages = results.map { (toolCall, result) ->
                LlmMessage.createToolResultMessage(
                    toolCallId = toolCall.id,
                    toolName = toolCall.function.name,
                    content = result.content,
                )
            },
            stepDescriptors = visibleStepDescriptors,
            stepStatuses = stepStatuses,
        )
    }

    private suspend fun requestLlmFollowUp(
        session: AgentLoopSessionState,
        conversation: Conversation,
        questionMessages: List<MessageEntity>,
        contextMessages: List<LlmMessage>,
        toolResultMessages: List<LlmMessage>,
        activeTools: List<ToolDefinition>?,
        enableWebSearch: Boolean,
        enableReasoning: Boolean,
        iteration: Int,
        stepDescriptors: List<ToolStepDescriptor>,
        stepStatuses: Map<String, ExecutionStepStatus>,
        stepResults: Map<String, String>,
        watchdogReason: String,
        previousResponseIdProvider: () -> String,
        callback: AgentLoopCallback,
    ): Boolean {
        val previousResponseId = previousResponseIdProvider().takeIf {
            conversation.engine.requestProtocol == AiRequestProtocol.RESPONSES_COMPATIBLE &&
                it.isNotBlank()
        }
        if (previousResponseId != null) {
            callback.onUpdateAnswerMessage(previousResponseId)
        }

        callback.onShowWaitingLLM(
            iteration,
            buildExecutionSteps(
                stepDescriptors = stepDescriptors,
                statuses = stepStatuses.mapValues { (_, status) ->
                    if (status == ExecutionStepStatus.RUNNING || status == ExecutionStepStatus.WAITING_USER) {
                        ExecutionStepStatus.DONE
                    } else {
                        status
                    }
                },
                stepResults = stepResults,
                finalStepStatus = ExecutionStepStatus.RUNNING,
            ),
        )

        val followUpRequest = LlmTurnRequest(
            stream = conversation.engine.stream,
            model = conversation.engine.model.name,
            messages = if (previousResponseId != null) toolResultMessages else contextMessages,
            tools = activeTools,
            builtinTools = buildSet {
                if (enableWebSearch) add(LlmBuiltinTool.WEB_SEARCH)
            },
            reasoningEnabled = enableReasoning && conversation.engine.model.canReasoning,
            previousResponseId = previousResponseId,
        )
        val followUpFlow = remoteRequestProvider.fetchWithDirectRequest(
            conversation,
            followUpRequest,
        )
        streamCollector.collectStream(followUpFlow, questionMessages, watchdogReason)

        return agentLoopExecutor.hasAccumulatedToolCalls(session)
    }

    private fun appendToolResultsToContext(
        contextMessages: MutableList<LlmMessage>,
        completedToolCalls: List<ToolCall>,
        results: List<Pair<ToolCall, AgentToolResult>>,
        answerProvider: () -> String,
        reasoningContentProvider: () -> String,
        previousAnswer: String,
        previousReasoning: String,
    ): List<LlmMessage> {
        val appended = mutableListOf<LlmMessage>()
        // P0 #1+#13: 仅取本回合 LLM 新增的 answer / reasoning delta 作为 assistant 消息 content,
        // 避免回合 N≥2 的 assistant 消息把前序所有回合文本当作"自己一次说出的内容"重复塞回 context.
        val llmTextContent = answerProvider().deltaFrom(previousAnswer)
        val llmReasoningContent = reasoningContentProvider().deltaFrom(previousReasoning)
        val assistantMsg = LlmMessage.createAssistantToolCallMessage(
            toolCalls = completedToolCalls,
            content = llmTextContent,
            reasoningContent = llmReasoningContent,
        )
        contextMessages.add(assistantMsg)
        appended.add(assistantMsg)
        results.forEach { (toolCall, result) ->
            val truncatedResult = ToolResultTruncator.truncate(result)
            val imageUrls = extractImageUrls(truncatedResult.multiModalAttachments)
            val toolResultMsg = LlmMessage.createToolResultMessage(
                toolCallId = toolCall.id,
                toolName = toolCall.function.name,
                content = truncatedResult.content,
                imageUrls = imageUrls,
            )
            contextMessages.add(toolResultMsg)
            appended.add(toolResultMsg)
        }
        return appended
    }

    /**
     * 计算 current 相对 previous 的增量, 仅当 previous 是 current 的前缀时返回增量,
     * 否则返回 null (保守策略: 不发送可能错误的整段文本).
     *
     * - previous 为空 → 返回 current (去掉首尾空白后)
     * - current.length ≤ previous.length → 返回 null (内容被截短, 不当作增量)
     * - current 不以 previous 起头 → 返回 null (不变量被破坏, 可能是 _answerMessageEntity 被外部重置)
     */
    private fun String.deltaFrom(previous: String): String? {
        if (previous.isEmpty()) return takeIf { it.isNotBlank() }
        if (length <= previous.length) return null
        if (!startsWith(previous)) return null
        return substring(previous.length).takeIf { it.isNotBlank() }
    }

    /**
     * 从工具返回的多模态附件中提取可发送给 LLM 的 URL 列表。
     *
     * 优先级:
     * 1. 远程 url(已上传) —— 优先使用
     * 2. localContent (Base64 data URI) —— 降级使用,适用于截图类工具
     * 3. 都为空 → 跳过该附件
     *
     * 数量上限: 最多 3 张,避免单次 tool result 体积过大撑爆上下文窗口。
     */
    private fun extractImageUrls(attachments: List<AttachedMedia>): List<String> {
        if (attachments.isEmpty()) return emptyList()
        return attachments
            .asSequence()
            .mapNotNull { media ->
                when {
                    !media.url.isNullOrBlank() -> media.url
                    !media.localContent.isNullOrBlank() -> {
                        val mime = media.mimeType.takeIf { it.isNotBlank() } ?: "image/jpeg"
                        "data:$mime;base64,${media.localContent}"
                    }
                    else -> null
                }
            }
            .take(3)
            .toList()
    }

    private suspend fun finalizeExecutionLoop(
        session: AgentLoopSessionState,
        executionContext: ExecutionContext,
        allToolCallResults: List<Pair<ToolCall, AgentToolResult>>,
        conversationId: String,
    ): RunResult {
        val chainJson = persistToolCallChain(allToolCallResults, conversationId)

        if (agentLoopExecutor.isMaxIterationsReached(session)) {
            return RunResult(
                toolCallsChainJson = chainJson,
                maxIterationsReached = true,
                pausedContext = PausedContext(
                    effectiveConversation = executionContext.effectiveConversation,
                    originalQuestionMessages = executionContext.originalQuestionMessages,
                    enableWebSearch = executionContext.enableWebSearch,
                    tools = executionContext.activeTools,
                ),
            )
        }

        try {
            agentLoopExecutor.taskManager.deleteByConversation(conversationId)
        } catch (_: Exception) {}

        return RunResult(
            toolCallsChainJson = chainJson,
            maxIterationsReached = false,
        )
    }

    private suspend fun persistToolCallChain(
        allToolCallResults: List<Pair<ToolCall, AgentToolResult>>,
        conversationId: String,
    ): String {
        val currentRecords = buildToolCallRecords(allToolCallResults)
        val dbRecords = runCatching {
            agentLoopExecutor.taskManager.toToolCallRecords(conversationId)
        }.getOrElse { emptyList() }
        val mergedRecords = ToolCallRecord.merge(
            existing = dbRecords,
            incoming = currentRecords,
        )
        if (mergedRecords.isEmpty()) return ""
        return gson.toJson(enrichToolCallRecords(mergedRecords))
    }

    private fun buildToolCallRecords(results: List<Pair<ToolCall, AgentToolResult>>): List<ToolCallRecord> {
        return results.map { (call, result) ->
            ToolCallRecord(
                id = call.id,
                name = call.function.name,
                arguments = call.function.arguments,
                result = result.content,
                isError = result.isError,
                displayTitle = ToolExecutionTextResolver.resolveTitle(call.function.name),
            )
        }
    }

    private fun enrichToolCallRecords(records: List<ToolCallRecord>): List<ToolCallRecord> {
        return records.map { record ->
            if (!record.displayTitle.isNullOrBlank() || !record.displaySummary.isNullOrBlank()) {
                record
            } else {
                record.copy(
                    displayTitle = ToolExecutionTextResolver.resolveTitle(record.name),
                )
            }
        }
    }

    private fun buildExecutionSteps(
        stepDescriptors: List<ToolStepDescriptor>,
        statuses: Map<String, ExecutionStepStatus>,
        stepResults: Map<String, String>,
        finalStepStatus: ExecutionStepStatus,
    ): List<com.shifenmiao.ai.execution.model.ExecutionStepUiModel> {
        return buildList {
            add(
                com.shifenmiao.ai.execution.model.ExecutionStepUiModel(
                    id = "prepare_execution",
                    title = applicationContext.getString(R.string.ai_execution_step_prepare),
                    status = ExecutionStepStatus.DONE,
                    isSystemStep = true,
                )
            )
            stepDescriptors.forEach { descriptor ->
                val result = stepResults[descriptor.id]
                add(
                    com.shifenmiao.ai.execution.model.ExecutionStepUiModel(
                        id = descriptor.id,
                        title = descriptor.title,
                        subtitle = descriptor.summary,
                        status = statuses[descriptor.id] ?: ExecutionStepStatus.PENDING,
                        debugInfo = descriptor.debugInfo,
                        arguments = descriptor.arguments,
                        result = result?.takeIf { it.isNotBlank() },
                    )
                )
            }
            add(
                com.shifenmiao.ai.execution.model.ExecutionStepUiModel(
                    id = "final_response",
                    title = applicationContext.getString(R.string.ai_execution_step_generate_response),
                    subtitle = applicationContext.getString(R.string.ai_execution_waiting_response_summary),
                    status = finalStepStatus,
                    isSystemStep = true,
                )
            )
        }
    }

    private fun logToolTrace(message: String) {
        if (BuildConfig.DEBUG) {
            message.makeLog("AgentToolTrace")
        }
    }

    // ---- internal data classes ----

    private data class ExecutionContext(
        val effectiveConversation: Conversation,
        val originalQuestionMessages: List<MessageEntity>,
        val enableWebSearch: Boolean,
        val initialTools: List<ToolDefinition>?,
        var activeTools: List<ToolDefinition>?,
        val contextMessages: MutableList<LlmMessage>,
    )

    private data class IterationResult(
        val iteration: Int,
        val activeTools: List<ToolDefinition>?,
        val toolResultMessages: List<LlmMessage>,
        val stepDescriptors: List<ToolStepDescriptor>,
        val stepStatuses: LinkedHashMap<String, ExecutionStepStatus>,
    )

    data class ToolStepDescriptor(
        val id: String,
        val toolName: String,
        val title: String,
        val summary: String?,
        val arguments: String?,
        val debugInfo: String?,
    )
}
