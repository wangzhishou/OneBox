package com.shifenmiao.ai.component

import com.shifenmiao.ai.BuildConfig
import com.shifenmiao.ai.agent.AgentLoopExecutor
import com.shifenmiao.ai.agent.AgentLoopSessionState
import com.shifenmiao.ai.agent.ToolCallRecord
import com.shifenmiao.ai.agent.callback.CallbackResult
import com.shifenmiao.ai.agent.callback.NavigationRequest
import com.shifenmiao.ai.agent.callback.ToolCallbackRouter
import com.shifenmiao.ai.agent.tool.AgentUserQuestionRequest
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.ConversationToolPolicyRepository
import com.shifenmiao.ai.agent.tool.InteractiveToolRuntime
import com.shifenmiao.ai.agent.tool.ToolConfirmationRequest
import com.shifenmiao.ai.agent.tool.AgentToolRegistry
import com.shifenmiao.ai.execution.model.ExecutionStepUiModel
import com.shifenmiao.ai.execution.presenter.ToolExecutionTextResolver
import com.shifenmiao.ai.service.PromptAssemblyService
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.ToolCall
import com.shifenmiao.model.ai.ToolDefinition
import com.shifenmiao.model.ai.tool.ConversationToolPolicy
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolSelectionResult
import com.shifenmiao.model.ai.unified.LlmStreamEvent
import com.shifenmiao.model.ai.unified.LlmTurnRequest
import com.shifenmiao.storage.AIChatStorage
import com.t8rin.logger.makeLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

fun interface StreamCollector {
    suspend fun collectStream(
        flow: Flow<LlmStreamEvent>,
        questionMessageEntityList: List<MessageEntity>,
        watchdogReason: String
    )
}

fun interface RemoteRequestProvider {
    suspend fun fetchWithDirectRequest(
        conversation: Conversation,
        request: LlmTurnRequest
    ): Flow<LlmStreamEvent>
}

/**
 * Agent Loop 编排器 —— 负责工具筛选、Agent Loop 执行和工具中心状态。
 *
 * 从 [AIChatComponent] 中抽离，职责边界：
 * 1. 工具策略筛选与配置（EffectiveToolConfig, ToolSelectionResult）
 * 2. Agent Loop 循环执行（execute/continue）
 * 3. 工具调用 UI 状态（AgentToolCallUIState, ToolCenterUiState）
 * 4. 工具回调路由观察
 *
 * 设计说明：
 * - 编排器本身不持有 LLM 流式收集逻辑，通过外部注入的流收集器回调，
 *   让 AIChatComponent 继续负责 Flow.collect + handleLlmEvent。
 * - [AgentLoopSessionState] 由编排器持有，因为它是 Agent Loop 的专属状态。
 */
class AgentLoopOrchestrator(
    private val agentLoopExecutor: AgentLoopExecutor,
    private val interactiveToolBridge: InteractiveToolRuntime,
    private val globalToolUiHost: GlobalToolUiHost,
    private val agentToolRegistry: AgentToolRegistry,
    private val conversationToolPolicyRepository: ConversationToolPolicyRepository,
    private val toolConfigResolver: ToolConfigResolver,
    private val toolCallbackRouter: ToolCallbackRouter,
    private val sharedState: ChatSharedState,
    private val streamContentProcessor: StreamContentProcessor,
    val agentToolCallStatus: MutableStateFlow<AgentToolCallUIState>,
    private val toolUiCoordinator: ToolExecutionUiBridge,
    private val interactionOwnerId: String,
    private val ownsInteractiveRuntimeLifecycle: Boolean,
    private val promptAssemblyService: PromptAssemblyService,
    private val modeTransitionManager: ModeTransitionManager,
    private val agentLoopRunner: AgentLoopRunner,
    /** Agent Loop 完成后的持久化回调（工具调用链落库），由 AIChatComponent 注入 */
    private val onAgentLoopCompletion: suspend (toolCallsChainJson: String, questionMessages: List<MessageEntity>) -> Unit,
) {
    private val agentLoopSession = AgentLoopSessionState()

    /** Agent Loop 累积的工具调用链 JSON，用于持久化 */
    var toolCallsChainJson: String = ""
        private set

    /** 工具中心状态流 */
    private val _toolCenterUiState = MutableStateFlow(ToolCenterUiState())
    val toolCenterUiState: MutableStateFlow<ToolCenterUiState> = _toolCenterUiState

    /** Agent Loop 暂停上下文 */
    private var pausedAgentLoopContext: PausedAgentLoopContext? = null

    fun prepareToolSelection(): ToolSelectionResult {
        return promptAssemblyService.prepareToolSelection(
            engine = sharedState.conversation.value.engine
        )
    }

    suspend fun buildRequestTools(): List<ToolDefinition>? {
        val tools = promptAssemblyService.buildRequestTools()
        logToolTrace("request_tools names=${tools?.map { it.function.name }} count=${tools?.size}")
        return tools
    }

    suspend fun buildEffectiveConversation(
        baseConversation: Conversation,
        preResolvedConfig: ToolConfigResolver.EffectiveToolConfig? = null
    ): Conversation {
        val planInjection = modeTransitionManager.extractPlanForInjection()
        return promptAssemblyService.buildEffectiveConversation(
            baseConversation = baseConversation,
            preResolvedConfig = preResolvedConfig,
            planInjection = planInjection
        )
    }

    suspend fun buildToolCenterUiState(): ToolCenterUiState {
        val effectiveToolConfig = toolConfigResolver.resolveFresh()
        val policy = effectiveToolConfig.policy
        val allTools = agentToolRegistry.getVisibleTools()
        // 规则 2: 绑定的工具视为开启, 同时并入用户显式开启列表.
        // UI 始终展示全量可见工具, 不再被 boundToolNames 当白名单过滤,
        // 避免 PROMPT/AGENT 在已有 binding 时把 binding 外的工具隐藏掉.
        val enabledSet = (effectiveToolConfig.boundToolNames.orEmpty() + policy.selectedToolNames)
            .filter { name -> allTools.any { it.name == name } }
            .distinct()
        val bootstrapToolNames = allTools
            .filter { policy.workingMode in it.bootstrapModes }
            .map { it.name }
        val systemTools = allTools.filter { it.category == ToolCategory.SYSTEM }
        val disabledSystemToolTitles = systemTools
            .filterNot { it.name in bootstrapToolNames || it.name in enabledSet }
            .map { it.title }
        return ToolCenterUiState(
            workingMode = policy.workingMode,
            allTools = allTools,
            bootstrapToolNames = bootstrapToolNames,
            enabledToolNames = enabledSet,
            systemToolNames = systemTools.map { it.name },
            disabledSystemToolTitles = disabledSystemToolTitles
        )
    }

    fun refreshToolCenter() {
        sharedState.componentScope.launch(sharedState.ioDispatcher) {
            _toolCenterUiState.value = _toolCenterUiState.value.copy(isLoading = true)
            _toolCenterUiState.value = buildToolCenterUiState().copy(isLoading = false)
        }
    }

    fun setToolEnabled(toolName: String, enabled: Boolean) {
        setToolsEnabled(listOf(toolName), enabled)
    }

    fun setWorkingMode(workingMode: com.shifenmiao.model.ai.tool.ChatWorkingMode) {
        sharedState.componentScope.launch(sharedState.ioDispatcher) {
            modeTransitionManager.switchMode(
                conversation = sharedState.conversation.value,
                targetMode = workingMode,
                currentAnswerText = sharedState.answerMessageEntity.value.answer
            ) ?: return@launch
            toolConfigResolver.clearCache()
            _toolCenterUiState.value = buildToolCenterUiState()
        }
    }

    fun setToolsEnabled(toolNames: List<String>, enabled: Boolean) {
        sharedState.componentScope.launch(sharedState.ioDispatcher) {
            if (toolNames.isEmpty()) return@launch
            val currentPolicy = conversationToolPolicyRepository.getPolicy(sharedState.conversation.value)
                ?: ConversationToolPolicy()
            val resolved = toolConfigResolver.resolveFresh()
            // 有效开启集 = 已有 selected ∪ 持久化 binding.
            // [PromptCreationService] / [AgentLoopOrchestrator.syncDefaultBindings] 等
            // 路径会绕过 [setToolsEnabled] 直接写 binding, 此时 binding 中可能有
            // selected 没有的项; 不并入的话, 用户在工具中心取消勾选 binding-only
            // 的工具时不会从 binding 里移除, 会出现 "UI 显示关闭但首轮仍发送".
            val effectiveEnabled = (currentPolicy.selectedToolNames + resolved.boundToolNames.orEmpty())
                .toMutableSet()
            toolNames.distinct().forEach { toolName ->
                if (enabled) effectiveEnabled.add(toolName) else effectiveEnabled.remove(toolName)
            }
            val newList = effectiveEnabled.toList().sorted()
            conversationToolPolicyRepository.savePolicy(
                conversation = sharedState.conversation.value,
                policy = currentPolicy.copy(selectedToolNames = newList)
            )
            toolConfigResolver.syncDefaultBindings(sharedState.conversation.value, newList)
            toolConfigResolver.clearCache()
            _toolCenterUiState.value = buildToolCenterUiState()
        }
    }


    suspend fun executeAgentLoop(
        effectiveConversation: Conversation,
        originalQuestionMessages: List<MessageEntity>,
        enableWebSearch: Boolean,
        tools: List<ToolDefinition>?,
    ) {
        val conversation = sharedState.conversation.value
        val enableReasoning = AIChatStorage.isEnableReasoning.value
        val conversationId = conversation.id
        // P1 #4: 用 provider 替代一次性快照, 保证 follow-up 回合关联到当回合 ResponseStarted 的
        // 新 responseId (在 processResponseStarted 中更新), 而不是初始回合的旧 ID.
        // ToolCallTaskEntity 入库时按此 ID 关联, 跨会话恢复时也能正确归位.
        val completionIdProvider: () -> String = {
            sharedState.answerMessageEntity.value.completionId.takeIf { it.isNotBlank() }
                ?: sharedState.questionMessageEntity.value.completionId
        }

        try {
            val result = agentLoopRunner.execute(
                session = agentLoopSession,
                interactionOwnerId = interactionOwnerId,
                callbackRouter = toolCallbackRouter,
                conversation = conversation,
                effectiveConversation = effectiveConversation,
                originalQuestionMessages = originalQuestionMessages,
                enableWebSearch = enableWebSearch,
                enableReasoning = enableReasoning,
                tools = tools,
                conversationId = conversationId,
                completionIdProvider = completionIdProvider,
                answerProvider = { sharedState.answerMessageEntity.value.answer },
                reasoningContentProvider = { sharedState.answerMessageEntity.value.reasoningContent },
                previousResponseIdProvider = { sharedState.answerMessageEntity.value.providerResponseId },
                callback = buildAgentLoopCallback(),
            )

            toolCallsChainJson = result.toolCallsChainJson
            if (result.maxIterationsReached && result.pausedContext != null) {
                val ctx = result.pausedContext
                pausedAgentLoopContext = PausedAgentLoopContext(
                    effectiveConversation = ctx.effectiveConversation,
                    originalQuestionMessages = ctx.originalQuestionMessages,
                    enableWebSearch = ctx.enableWebSearch,
                    tools = ctx.tools,
                )
                toolUiCoordinator.showMaxIterationsReached(
                    iteration = agentLoopSession.currentIteration,
                    maxIterations = agentLoopExecutor.currentMaxIterations(agentLoopSession),
                    steps = currentExecutionSteps(),
                )
            } else {
                pausedAgentLoopContext = null
                toolUiCoordinator.showIdle()
            }
        } catch (e: Exception) {
            pausedAgentLoopContext = null
            toolCallsChainJson = ""
            toolUiCoordinator.showIdle()
            throw e
        }
    }

    suspend fun handleToolCallbackRequest(request: NavigationRequest.ToolCall) {
        val result = agentLoopExecutor.executeSingleTool(
            toolName = request.toolName,
            arguments = request.arguments,
            interactionOwnerId = interactionOwnerId,
            callbackRouter = toolCallbackRouter
        )
        toolCallbackRouter.completeCallback(
            request.callbackId,
            if (result.isError) CallbackResult.error(result.content)
            else CallbackResult.success(result.content)
        )
    }

    fun handleScreenNavigationRequest(request: NavigationRequest.ScreenNavigation) {
        globalToolUiHost.enqueueScreenNavigation(
            requestId = request.callbackId,
            screen = request.screen
        )
    }

    fun onInteractiveRequestChanged(
        confirmationRequest: ToolConfirmationRequest?,
        questionRequest: AgentUserQuestionRequest?
    ) {
        toolUiCoordinator.onInteractiveRequestChanged(
            confirmationRequest = confirmationRequest,
            questionRequest = questionRequest,
            interactionOwnerId = interactionOwnerId,
            steps = currentExecutionSteps()
        )
    }

    fun handleOpenScreenRequest(request: NavigationRequest.OpenScreen) {
        globalToolUiHost.enqueueScreenNavigation(
            requestId = request.requestId,
            screen = request.screen
        )
    }

    /**
     * 在请求开始时快照工具配置，整个请求生命周期内复用。
     *
     * 解决两个问题：
     * 1. 避免同一请求内多次 resolveEffectiveToolConfig() 的重复 DB 查询
     * 2. 防止用户在请求过程中切换工作模式导致的竞态条件
     */
    suspend fun snapshotToolConfig(): ToolConfigResolver.EffectiveToolConfig {
        return toolConfigResolver.snapshot()
    }

    fun continueAgentLoop() {
        val ctx = pausedAgentLoopContext ?: return
        val pausedSteps = currentExecutionSteps()

        sharedState.updateChatUiState { it.copy(chatActive = true) }
        if (agentLoopSession.currentIteration >= agentLoopExecutor.currentMaxIterations(agentLoopSession)) {
            agentLoopExecutor.extendIterationLimit(agentLoopSession)
        }

        sharedState.componentScope.launch(sharedState.ioDispatcher) {
            try {
                executeAgentLoop(
                    effectiveConversation = ctx.effectiveConversation,
                    originalQuestionMessages = ctx.originalQuestionMessages,
                    enableWebSearch = ctx.enableWebSearch,
                    tools = ctx.tools,
                )
                if (pausedAgentLoopContext == null) {
                    // Agent Loop 完成后触发持久化（工具调用链落库）
                    onAgentLoopCompletion(toolCallsChainJson, ctx.originalQuestionMessages)
                }
            } catch (e: Exception) {
                "Failed to continue Agent Loop: ${e.message}".makeLog("AgentLoopOrchestrator")
                pausedAgentLoopContext = ctx
                runCatching {
                    agentLoopExecutor.taskManager.failAllIncompleteTasks(
                        sharedState.conversation.value.id,
                        e.message ?: "error"
                    )
                }
                toolUiCoordinator.showMaxIterationsReached(
                    iteration = agentLoopSession.currentIteration,
                    maxIterations = agentLoopExecutor.currentMaxIterations(agentLoopSession),
                    steps = pausedSteps,
                )
            } finally {
                sharedState.updateChatUiState { it.copy(chatActive = false) }
            }
        }
    }

    fun reset() {
        agentLoopExecutor.reset(agentLoopSession)
        interactiveToolBridge.clearPendingRequestOwnedBy(interactionOwnerId)
        modeTransitionManager.reset()
        pausedAgentLoopContext = null
        toolCallsChainJson = ""
        toolConfigResolver.clearCache()
        toolUiCoordinator.showIdle()
    }

    fun onDestroy() {
        if (ownsInteractiveRuntimeLifecycle) {
            interactiveToolBridge.clearPendingRequestOwnedBy(interactionOwnerId)
        }
    }

    fun hasAccumulatedToolCalls(): Boolean =
        agentLoopExecutor.hasAccumulatedToolCalls(agentLoopSession)

    fun currentIteration(): Int = agentLoopSession.currentIteration

    fun currentMaxIterations(): Int = agentLoopExecutor.currentMaxIterations(agentLoopSession)


    fun accumulateToolCallDeltas(deltas: List<com.shifenmiao.model.ai.ToolCallDelta>) {
        agentLoopExecutor.accumulateToolCallDeltas(agentLoopSession, deltas)
    }

    fun showToolCallPlanning(deltaCount: Int) {
        if (agentToolCallStatus.value is AgentToolCallUIState.Planning) return
        if (
            agentToolCallStatus.value is AgentToolCallUIState.Executing ||
            agentToolCallStatus.value is AgentToolCallUIState.WaitingUserInput ||
            agentToolCallStatus.value is AgentToolCallUIState.WaitingLLM
        ) {
            return
        }
        toolUiCoordinator.showPlanning(
            toolCallCount = deltaCount,
            iteration = agentLoopSession.currentIteration,
            maxIterations = agentLoopExecutor.currentMaxIterations(agentLoopSession)
        )
        logToolTrace("stream_tool_call_delta count=$deltaCount")
    }

    fun showToolUiIdle() {
        toolUiCoordinator.showIdle()
    }

    /**
     * 构建 AgentLoopCallback 实现，将 Runner 执行事件桥接到 UI 层。
     */
    /**
     * P1 #2: 保护 `toolCalls` JSON 的 read-modify-write 原子性.
     *
     * [onToolCompleted] 在并行工具执行场景下会被多个 coroutine 并发触发
     * (见 [AgentLoopExecutor.executeBatchedToolCalls] 的 `async { ... }.awaitAll()`),
     * 而 [ToolCallRecord.appendToJson] 是 parse → merge → serialize 的非原子过程.
     * 若不加锁, 多个 callback 并发读旧值、各自 merge 后写回, 后写覆盖导致记录丢失.
     *
     * 用普通对象锁即可, 不需要协程 Mutex (callback 上下文可能不在协程里).
     */
    private val toolCallAppendLock = Any()

    private fun buildAgentLoopCallback(): AgentLoopCallback = object : AgentLoopCallback {
        override fun onEnsureStartingHint() = Unit

        override fun onToolStarted(toolCall: ToolCall, stepTitle: String) = Unit

        override fun onToolCompleted(toolCall: ToolCall, result: AgentToolResult, stepTitle: String) {
            // P1 #2: read → 解析 → 合并 → 序列化 → 写回 整段必须在同一把锁内完成.
            // 旧实现只把 read-modify 放锁内, write 暴露在锁外, 导致:
            //   协程A 释放锁 → 计算 "[recordA]" → 协程B 立刻拿锁 → 读 "[]" (A 还没写) → 计算 "[recordB]"
            //   → A 写 "[recordA]" → B 写 "[recordB]" 覆盖 → recordA 丢失.
            // 修复: 把 [sharedState.updateAnswerMessage] 也收进 synchronized 块, 保证整个
            // append 流程对其它并发的 onToolCompleted 不可分割.
            synchronized(toolCallAppendLock) {
                val currentJson = sharedState.answerMessageEntity.value.toolCalls
                val newToolCallsJson = ToolCallRecord.appendToJson(
                    existingJson = currentJson,
                    record = ToolCallRecord(
                        id = toolCall.id,
                        name = toolCall.function.name,
                        arguments = toolCall.function.arguments,
                        result = result.content,
                        isError = result.isError,
                        displayTitle = stepTitle,
                    )
                )
                sharedState.updateAnswerMessage { current ->
                    current.copy(toolCalls = newToolCallsJson)
                }
            }
            // updatePlaceHolderMessage 只读 StateFlow 刷新 UI, 放锁外即可 —
            // 即使读到稍旧的值也只会延迟一帧渲染, 不会丢数据.
            streamContentProcessor.updatePlaceHolderMessage(forceUpdate = true)
        }

        override fun onToolWaitingInput(toolCall: ToolCall, stepTitle: String) {
            toolUiCoordinator.showWaitingUserInput(
                toolName = stepTitle, toolCallId = toolCall.id,
                requestType = "INPUT",
                iteration = agentLoopSession.currentIteration,
                maxIterations = agentLoopExecutor.currentMaxIterations(agentLoopSession),
                steps = currentExecutionSteps(),
            )
        }

        override fun onToolNeedConfirmation(toolCall: ToolCall) {
            val stepTitle = ToolExecutionTextResolver.resolveTitle(toolCall.function.name)
            toolUiCoordinator.showWaitingUserInput(
                toolName = stepTitle, toolCallId = toolCall.id,
                requestType = "CONFIRMATION",
                iteration = agentLoopSession.currentIteration,
                maxIterations = agentLoopExecutor.currentMaxIterations(agentLoopSession),
                steps = currentExecutionSteps(),
            )
        }

        override fun onShowExecuting(
            toolCalls: List<ToolCall>,
            currentToolName: String?,
            currentToolCallId: String?,
            iteration: Int,
            steps: List<ExecutionStepUiModel>,
        ) {
            toolUiCoordinator.showExecuting(
                toolCalls = toolCalls,
                currentToolName = currentToolName,
                currentToolCallId = currentToolCallId,
                iteration = iteration,
                maxIterations = agentLoopExecutor.currentMaxIterations(agentLoopSession),
                steps = steps,
            )
        }

        override fun onShowWaitingLLM(iteration: Int, steps: List<ExecutionStepUiModel>) {
            toolUiCoordinator.showWaitingLLM(
                iteration = iteration,
                maxIterations = agentLoopExecutor.currentMaxIterations(agentLoopSession),
                steps = steps
            )
        }

        override fun onResetStreamWatchdog() = streamContentProcessor.resetStreamWatchdog()
        override fun onUpdatePlaceHolder(forceUpdate: Boolean) = streamContentProcessor.updatePlaceHolderMessage(forceUpdate)
        override fun onUpdateAnswerMessage(previousResponseId: String?) {
            if (previousResponseId != null) {
                sharedState.updateAnswerMessage { it.copy(previousResponseId = previousResponseId) }
            }
        }
    }


    private fun currentExecutionSteps(): List<ExecutionStepUiModel> {
        return when (val status = agentToolCallStatus.value) {
            AgentToolCallUIState.Idle -> emptyList()
            is AgentToolCallUIState.Planning -> status.steps
            is AgentToolCallUIState.Executing -> status.steps
            is AgentToolCallUIState.WaitingLLM -> status.steps
            is AgentToolCallUIState.WaitingUserInput -> status.steps
            is AgentToolCallUIState.MaxIterationsReached -> status.steps
        }
    }


    private fun logToolTrace(message: String) {
        if (BuildConfig.DEBUG) {
            message.makeLog("AgentToolTrace")
        }
    }

    private data class PausedAgentLoopContext(
        val effectiveConversation: Conversation,
        val originalQuestionMessages: List<MessageEntity>,
        val enableWebSearch: Boolean,
        val tools: List<ToolDefinition>?,
    )

    private companion object
}
