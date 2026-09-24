package com.shifenmiao.ai.component

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.google.gson.Gson
import com.halilibo.richtext.markwon.MarkdownAstNodeParser
import com.shifenmiao.ai.BuildConfig
import com.shifenmiao.ai.agent.AgentLoopExecutor
import com.shifenmiao.ai.agent.callback.NavigationRequest
import com.shifenmiao.ai.agent.callback.ToolCallbackRouter
import com.shifenmiao.ai.agent.tool.ConversationToolPolicyRepository
import com.shifenmiao.ai.agent.tool.InteractiveToolRuntime
import com.shifenmiao.ai.agent.tool.AgentToolRegistry
import com.shifenmiao.ai.agent.tool.ToolBindingRepository
import com.shifenmiao.ai.agent.tool.ToolPredicate
import com.shifenmiao.ai.context.ContextCompactor
import com.shifenmiao.ai.logic.ChatInputComponent
import com.shifenmiao.ai.mediator.MessageRemoteMediator
import com.shifenmiao.ai.request.LocalLlmSessionManager
import com.shifenmiao.ai.memory.ConversationMemoryPolicyRepository
import com.shifenmiao.ai.memory.MemoryRepository
import com.shifenmiao.ai.model.MessageUiModel
import com.shifenmiao.ai.prompt.PromptManager
import com.shifenmiao.ai.prompt.SystemPromptRepository
import com.shifenmiao.ai.repository.ConversationRepository
import com.shifenmiao.ai.repository.MessageRepository
import com.shifenmiao.ai.service.ConversationTitleSummaryService
import com.shifenmiao.ai.service.PromptAssemblyService
import com.shifenmiao.ai.service.PromptTemplateToolService
import com.shifenmiao.ai.skill.SkillRepository
import com.shifenmiao.ai.upload.AttachmentContentResolver
import com.shifenmiao.ai.upload.FileUploadRouter
import com.shifenmiao.ai.usecase.MessageListUseCase
import com.shifenmiao.ai.utils.AiUtils
import com.shifenmiao.ai.utils.AttachmentPayloadUtils
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.common.manager.AIEngineSyncManager
import com.shifenmiao.core.R
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.database.image.dao.ImageDao
import com.shifenmiao.database.utils.DataBaseUtils
import com.shifenmiao.model.Source
import com.shifenmiao.model.ai.ChatPrompt
import com.shifenmiao.network.api.ApiService
import com.shifenmiao.network.utils.NetworkUtils
import com.wanbaohe.a2ui.catalog.A2uiRenderProvider
import com.shifenmiao.model.ai.AttachedMedia
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.ContentType
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.MessageUIState
import com.shifenmiao.model.ai.RoleType
import com.shifenmiao.model.ai.ToolDefinition
import com.shifenmiao.model.ai.Usage
import com.shifenmiao.model.ai.event.MainClickEvent
import com.shifenmiao.model.ai.event.MainClickEventFrom
import com.shifenmiao.model.ai.event.MainShowType
import com.shifenmiao.model.ai.tool.ChatWorkingMode
import com.shifenmiao.model.ai.unified.LlmStreamEvent
import com.shifenmiao.model.event.AppEventBus
import com.shifenmiao.model.state.PageState
import com.shifenmiao.model.user.event.VerifyContactEvent
import com.shifenmiao.storage.AIChatStorage
import com.shifenmiao.storage.AppSharedStorage
import com.shifenmiao.storage.TokenStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.resource.ResourceManager
import com.t8rin.logger.makeLog
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Date

open class AIChatComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted conversationParams: Conversation,
    @Assisted private val interactionOwnerId: String,
    @Assisted private val ownsInteractiveRuntimeLifecycle: Boolean,
    dispatchersHolder: DispatchersHolder,
    resourceManager: ResourceManager,
    val appDatabase: AppDatabase,
    private val apiService: ApiService,
    aiEngineManager: AIEngineManager,
    aiEngineCatalogManager: AIEngineCatalogManager,
    aiEngineSyncManager: AIEngineSyncManager,
    promptManager: PromptManager,
    conversationRepository: ConversationRepository,
    messageRepository: MessageRepository,
    private val messageRemoteMediator: MessageRemoteMediator,
    private val agentLoopExecutor: AgentLoopExecutor,
    private val interactiveToolBridge: InteractiveToolRuntime,
    private val globalToolUiHost: GlobalToolUiHost,
    private val toolBindingRepository: ToolBindingRepository,
    private val agentToolRegistry: AgentToolRegistry,
    private val toolPredicates: Set<@JvmSuppressWildcards ToolPredicate>,
    private val agentLoopInterceptors: Set<@JvmSuppressWildcards AgentLoopInterceptor>,
    private val conversationToolPolicyRepository: ConversationToolPolicyRepository,
    private val promptTemplateToolService: PromptTemplateToolService,
    private val conversationMemoryPolicyRepository: ConversationMemoryPolicyRepository,
    private val memoryRepository: MemoryRepository,
    private val skillRepository: SkillRepository,
    private val fileUploadRouter: FileUploadRouter,
    private val attachmentContentResolver: AttachmentContentResolver,
    private val systemPromptRepository: SystemPromptRepository,
    private val conversationTitleSummaryService: ConversationTitleSummaryService,
    private val contextCompactor: ContextCompactor,
    private val localLlmSessionManager: LocalLlmSessionManager,
    @ApplicationContext private val appContext: Context,
    messageListUseCase: MessageListUseCase,
    a2uiRenderProvider: A2uiRenderProvider,
    markdownAstNodeParserFactory: MarkdownAstNodeParser.Factory,
    chatInputComponentFactory: ChatInputComponent.Factory,
    private val gson: Gson,
    private val imageDao: ImageDao
) : AIChatBaseComponent(
    componentContext,
    conversationParams,
    dispatchersHolder,
    resourceManager,
    aiEngineManager,
    aiEngineCatalogManager,
    aiEngineSyncManager,
    appContext,
    messageListUseCase,
    promptManager,
    conversationRepository,
    messageRepository,
    a2uiRenderProvider,
) {
    val chatInputComponent = chatInputComponentFactory(componentContext)
    private val toolCallbackRouter = ToolCallbackRouter(_conversation.value.id, componentContext)

    private val sharedState = ChatSharedState(
        _conversation = _conversation,
        _chatUIState = _chatUIState,
        _questionMessageEntity = _questionMessageEntity,
        _answerMessageEntity = _answerMessageEntity,
        messages = messages,
        componentScope = componentScope,
        ioDispatcher = ioDispatcher,
        applicationContext = appContext
    )

    private val streamContentProcessor = StreamContentProcessor(
        parser = markdownAstNodeParserFactory.create(),
        sharedState = sharedState,
        aiEngineCatalogManager = aiEngineCatalogManager,
        onStreamIdleTimeout = { onStreamIdleTimeout() }
    )

    private val attachmentUploadCoordinator = AttachmentUploadCoordinator(
        fileUploadRouter = fileUploadRouter,
        attachmentContentResolver = attachmentContentResolver,
        imageDao = imageDao,
        gson = gson,
        sharedState = sharedState,
        chatInputComponent = chatInputComponent,
        onProgressStateChanged = { uri, state ->
            componentScope.launch(Dispatchers.Main) {
                chatInputComponent.updateAttachmentState(uri, state)
            }
        }
    )

    private val messagePersistenceWorker = MessagePersistenceWorker(
        messageListUseCase = messageListUseCase,
        conversationTitleSummaryService = conversationTitleSummaryService,
        appDatabase = appDatabase,
        sharedState = sharedState,
        streamContentProcessor = streamContentProcessor,
        attachmentUploadCoordinator = attachmentUploadCoordinator
    )

    private val agentToolCallState = MutableStateFlow<AgentToolCallUIState>(AgentToolCallUIState.Idle)
    private var resolveAgentLoopIteration: () -> Int = { 0 }
    private var resolveAgentLoopMaxIterations: () -> Int = { 0 }

    /** 取消确认弹窗状态：仅在 chatActive 时触发，防止误操作中断已生成内容 */
    private val _showCancelConfirmDialog = MutableStateFlow(false)
    val showCancelConfirmDialog: StateFlow<Boolean> get() = _showCancelConfirmDialog

    private val toolExecutionUiBridge: ToolExecutionUiBridge = AIChatToolUiCoordinator(
        state = agentToolCallState,
        iterationProvider = { resolveAgentLoopIteration() },
        maxIterationsProvider = { resolveAgentLoopMaxIterations() }
    )

    private val toolConfigResolver = ToolConfigResolver(
        resolveBoundToolNames = { resolveBoundToolNames() },
        conversationToolPolicyRepository = conversationToolPolicyRepository,
        promptTemplateToolService = promptTemplateToolService,
        agentToolRegistry = agentToolRegistry,
        toolBindingRepository = toolBindingRepository,
        conversationMemoryPolicyRepository = conversationMemoryPolicyRepository,
        conversationProvider = { _conversation.value },
    )

    private val promptAssemblyService = PromptAssemblyService(
        agentLoopExecutor = agentLoopExecutor,
        toolConfigResolver = toolConfigResolver,
        systemPromptRepository = systemPromptRepository,
        agentToolRegistry = agentToolRegistry,
        memoryRepository = memoryRepository,
        skillRepository = skillRepository,
        // 谓词链收在 PromptAssemblyService 内部（两条工具集产出路径统一过滤）
        toolPredicates = toolPredicates,
    )

    private val modeTransitionManager = ModeTransitionManager(
        conversationToolPolicyRepository = conversationToolPolicyRepository,
    )

    private val agentLoopRunner = AgentLoopRunner(
        agentLoopExecutor = agentLoopExecutor,
        agentToolRegistry = agentToolRegistry,
        promptAssemblyService = promptAssemblyService,
        contextCompactor = contextCompactor,
        gson = gson,
        contentReader = { path -> attachmentContentResolver.readContentFromPath(path) },
        imageDao = imageDao,
        streamCollector = { flow, pendingQuestionMessages, watchdogReason ->
            streamContentProcessor.startStreamWatchdog(reason = watchdogReason)
            try {
                flow.collect { streamEvent ->
                    processingMutex.withLock {
                        handleLlmEvent(streamEvent, pendingQuestionMessages)
                    }
                }
            } finally {
                streamContentProcessor.stopStreamWatchdog()
            }
        },
        remoteRequestProvider = { conversation, request ->
            messageRemoteMediator.fetchWithDirectRequest(conversation, request)
        },
        modeTransitionManager = modeTransitionManager,
        applicationContext = appContext,
        agentLoopInterceptors = agentLoopInterceptors,
    )

    private val agentLoopOrchestrator = AgentLoopOrchestrator(
        agentLoopExecutor = agentLoopExecutor,
        interactiveToolBridge = interactiveToolBridge,
        globalToolUiHost = globalToolUiHost,
        agentToolRegistry = agentToolRegistry,
        conversationToolPolicyRepository = conversationToolPolicyRepository,
        conversationMemoryPolicyRepository = conversationMemoryPolicyRepository,
        toolConfigResolver = toolConfigResolver,
        toolCallbackRouter = toolCallbackRouter,
        sharedState = sharedState,
        streamContentProcessor = streamContentProcessor,
        agentToolCallStatus = agentToolCallState,
        toolUiCoordinator = toolExecutionUiBridge,
        interactionOwnerId = interactionOwnerId,
        ownsInteractiveRuntimeLifecycle = ownsInteractiveRuntimeLifecycle,
        promptAssemblyService = promptAssemblyService,
        modeTransitionManager = modeTransitionManager,
        agentLoopRunner = agentLoopRunner,
        onAgentLoopCompletion = { toolCallsChainJson, questionMessages ->
            messagePersistenceWorker.calculateUsageIfNull(null, questionMessages)
            messagePersistenceWorker.onChatCompletionEnd(
                toolCallsChainJson = toolCallsChainJson,
                startQuestionTime = streamContentProcessor.startQuestionTime
            )
        },
    )

    init {
        resolveAgentLoopIteration = { agentLoopOrchestrator.currentIteration() }
        resolveAgentLoopMaxIterations = { agentLoopOrchestrator.currentMaxIterations() }

        _chatUIState.value = _chatUIState.value.copy(
            showAvatar = true,
            showTokens = true
        )

        componentContext.lifecycle.doOnDestroy {
            onDestroy()
        }

        messagePersistenceWorker.setTitleSummaryCallback { title ->
            applyConversationTitleSummary(title)
        }

        componentScope.launch {
            combine(
                interactiveToolBridge.confirmationRequest,
                interactiveToolBridge.questionRequest
            ) { confirmationRequest, questionRequest ->
                confirmationRequest to questionRequest
            }
                .collectLatest { (confirmationRequest, questionRequest) ->
                    agentLoopOrchestrator.onInteractiveRequestChanged(
                        confirmationRequest = confirmationRequest,
                        questionRequest = questionRequest
                    )
                }
        }

        componentScope.launch {
            AppSharedStorage.isExpandedReasoningChat.collectLatest {
                streamContentProcessor.invalidateMessageCache()
                streamContentProcessor.renderMessage(forceUpdate = true)
            }
        }

        observeNavigationRequests()
    }

    private var observeMessagesJob: kotlinx.coroutines.Job? = null

    val messageUiModels: StateFlow<List<MessageUiModel>> get() = streamContentProcessor.messageUiModels
    val agentToolCallStatus: StateFlow<AgentToolCallUIState> get() = agentToolCallState
    val toolCenterUiState: StateFlow<ToolCenterUiState> get() = agentLoopOrchestrator.toolCenterUiState

    private var questionMessageEntityList: MutableList<MessageEntity> = mutableListOf()
    val messageDao = appDatabase.messageDao()

    companion object {
        @Volatile
        private var hasScheduledTaskCleanup = false

        const val ACTIVE_CHAT_MESSAGE_LIMIT = StreamContentProcessor.ACTIVE_CHAT_MESSAGE_LIMIT
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            conversation: Conversation,
            interactionOwnerId: String,
            ownsInteractiveRuntimeLifecycle: Boolean
        ): AIChatComponent
    }

    private fun observeNavigationRequests() {
        componentScope.launch(ioDispatcher) {
            toolCallbackRouter.navigationRequests.collect { request ->
                when (request) {
                    is NavigationRequest.ToolCall -> {
                        agentLoopOrchestrator.handleToolCallbackRequest(request)
                    }
                    is NavigationRequest.ScreenNavigation -> {
                        agentLoopOrchestrator.handleScreenNavigationRequest(request)
                    }
                    is NavigationRequest.OpenScreen -> {
                        agentLoopOrchestrator.handleOpenScreenRequest(request)
                    }
                }
            }
        }
    }

    override fun initReady() {
        maybeCleanupOldToolTasks()
        loadMessages()
    }

    /**
     * 空态引导进入 AI 助手 Tab：全局单例组件不走导航参数构造，
     * 由调用方把引导会话参数应用进来——开启新会话并把填充词预填到输入框
     * （template 仅作填充词，置空避免发送时重复拼接）。
     * 不传系统 prompt：Agent 工作模式根据输入文案自行发现并使用工具。
     */
    fun startGuidedConversation(guide: Conversation) {
        componentScope.launch {
            val fillIn = guide.template?.trim().orEmpty()
            clearMessages()
            _conversation.value = _conversation.value.copy(
                id = Date().time.toString(),
                title = guide.title,
                titleSource = com.shifenmiao.model.ai.AIConversationTitleSource.SYSTEM,
                template = null,
            )
            clearCurrentQuestionAndAnswer()
            loadMessages()
            hideHistory()
            hideFailureUI()
            if (fillIn.isNotEmpty()) {
                chatInputComponent.onInputTextChange(fillIn)
            }
        }
    }

    private fun maybeCleanupOldToolTasks() {
        if (hasScheduledTaskCleanup) return
        synchronized(AIChatComponent::class.java) {
            if (hasScheduledTaskCleanup) return
            hasScheduledTaskCleanup = true
        }
        componentScope.launch(ioDispatcher) {
            runCatching { agentLoopExecutor.taskManager.cleanupOldTasks() }
                .onFailure {
                    "cleanupOldToolTasks failed: ${it.message}".makeLog("AIChatComponent")
                }
        }
    }


    fun refreshToolCenter() = agentLoopOrchestrator.refreshToolCenter()

    fun setToolEnabled(toolName: String, enabled: Boolean) =
        agentLoopOrchestrator.setToolEnabled(toolName, enabled)

    fun setToolsEnabled(toolNames: List<String>, enabled: Boolean) =
        agentLoopOrchestrator.setToolsEnabled(toolNames, enabled)

    fun setMemoryEnabled(enabled: Boolean) =
        agentLoopOrchestrator.setMemoryEnabled(enabled)

    fun setSkillsEnabled(enabled: Boolean) =
        agentLoopOrchestrator.setSkillsEnabled(enabled)

    fun setWorkingMode(workingMode: ChatWorkingMode) {
        agentLoopOrchestrator.setWorkingMode(workingMode)
    }

    fun pushPromptToRemote(
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {},
    ) {
        val currentConversation = _conversation.value
        val promptId = currentConversation.promptId
            ?: currentConversation.entryRefId?.toIntOrNull()
        if (promptId == null || promptId <= 0) {
            onFailure(getString(R.string.prompt_push_failed))
            return
        }
        componentScope.launch(Dispatchers.IO) {
            val localPromptEntity = appDatabase.chatPromptDao().getPromptById(promptId)
            if (localPromptEntity == null) {
                withContext(Dispatchers.Main) {
                    onFailure(getString(R.string.prompt_push_failed))
                }
                return@launch
            }
            val prompt = DataBaseUtils.promptEntityToPrompt(localPromptEntity)
            // documentId 是 Strapi v5 稳定定位符，优先带上；数字 id 重发后可能已失效
            val requestPrompt = prompt.copy(
                id = localPromptEntity.remoteId ?: promptId,
                documentId = localPromptEntity.documentId ?: prompt.documentId,
            )
            val response = NetworkUtils.safeApiCall {
                apiService.updatePrompt(requestPrompt)
            }
            if (response != null) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val entity = DataBaseUtils.promptToPromptEntity(it.data, source = Source.REMOTE)
                        appDatabase.chatPromptDao().upsertRemotePrompt(entity)
                        _conversation.value = _conversation.value.copy(
                            prompt = it.data.prompt.orEmpty(),
                            title = it.data.title.orEmpty(),
                        )
                    }
                    withContext(Dispatchers.Main) { onSuccess() }
                } else {
                    var errorMsg: String? = null
                    NetworkUtils.handleErrorResponse(
                        response,
                        onFriendlyErrorTip = { message ->
                            errorMsg = message
                        }
                    )
                    withContext(Dispatchers.Main) {
                        onFailure(errorMsg ?: getString(R.string.prompt_push_failed))
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    onFailure(getString(R.string.prompt_push_failed))
                }
            }
        }
    }

    private suspend fun buildRequestTools(): List<ToolDefinition>? =
        agentLoopOrchestrator.buildRequestTools()

    private fun logAgentTrace(message: String) {
        if (BuildConfig.DEBUG) {
            message.makeLog("AgentTrace")
        }
    }

    protected open suspend fun resolveBoundToolNames(): Set<String>? = null

    private fun observeMessages() {
        observeMessagesJob?.cancel()
        observeMessagesJob = componentScope.launch(ioDispatcher) {
            try {
                messageDao.getMessagesFlowPaged(_conversation.value.id, limit = ACTIVE_CHAT_MESSAGE_LIMIT).catch { e ->
                    showFailureUI(e.message)
                }.collectLatest { messageList ->
                    if (messageList.isEmpty()) {
                        if (_chatUIState.value.chatActive) return@collectLatest
                        markPageReady()
                        return@collectLatest
                    }
                    if (_chatUIState.value.chatActive) return@collectLatest

                    val oldFingerprint = synchronized(messages) {
                        if (messages.isEmpty()) "" else "${messages.size}_${messages.last().completionId}"
                    }
                    val newFingerprint = "${messageList.size}_${messageList.last().completionId}"
                    if (oldFingerprint == newFingerprint) return@collectLatest

                    synchronized(messages) {
                        messages.clear()
                        messages.addAll(messageList)
                    }
                    streamContentProcessor.invalidateMessageCache()
                    streamContentProcessor.renderMessage(forceUpdate = true)
                    _chatUIState.value = _chatUIState.value.copy(
                        pageState = PageState.IDLE,
                        showMessageLimitNotice = messageList.size >= ACTIVE_CHAT_MESSAGE_LIMIT
                    )
                }
            } catch (e: Exception) {
                showFailureUI(
                    e.message ?: applicationContext.getString(R.string.ai_chat_failed_to_load_messages)
                )
            }
        }
    }

    override fun loadMessages() {
        if (_conversation.value.id.isNotEmpty()) {
            observeMessages()
        } else {
            "loadMessages skipped: conversation id is empty".makeLog("AIChatComponent")
        }
    }

    override fun clearMessages() {
        synchronized(messages) {
            messages.clear()
        }
        streamContentProcessor.clearAll()
    }

    fun startChatWithStreaming(
        messageContent: String,
        templateContent: String = getTemplateContent(),
        attachments: List<AttachedMedia> = emptyList(),
        enableReasoning: Boolean? = null,
        systemPromptOverride: String? = null,
        onNext: () -> Unit = {}
    ) {
        if (!_conversation.value.engine.hasAvailableChatRoute()) {
            ActionUtils.showToast(R.string.ai_chat_engine_unavailable_toast)
            return
        }
        if (!validateChatContent(messageContent, templateContent, attachments)) {
            return
        }
        streamContentProcessor.startQuestionTime = System.currentTimeMillis()
        // P1 #6: 自增代际号, 让本次 launch 知道自己是不是"当前最新".
        val myGeneration = chatGeneration.incrementAndGet()
        val newJob = componentScope.launch(ioDispatcher) {
            val effectiveReasoningEnabled = enableReasoning
                ?: (_conversation.value.engine.model.canReasoning && AIChatStorage.isEnableReasoning.value)
            val mutableMessageContent = templateContent.trim() + messageContent.trim()
            questionMessageEntityList.clear()
            questionMessageEntityList = prepareQuestionMessages(mutableMessageContent, attachments)
            if (!validateUserCanChat(questionMessageEntityList)) {
                return@launch
            }
            val currentQuestionEntity = questionMessageEntityList
                .lastOrNull { it.role == RoleType.USER.value }
                ?.copy()
                ?: return@launch
            onNext()
            prepareUIForChat(currentQuestionEntity)
            try {
                executeStreamingChat(
                    questionMessageEntityList = questionMessageEntityList,
                    attachments = attachments,
                    enableReasoning = effectiveReasoningEnabled,
                    systemPromptOverride = systemPromptOverride
                )
            } finally {
                // P1 #6: 只有当本协程仍是"最新一代"时, 才清 chatActive.
                // 避免前序被覆盖的旧 job 结束时把后续 job 的 UI 状态误杀.
                if (chatGeneration.get() == myGeneration) {
                    _chatUIState.value = _chatUIState.value.copy(chatActive = false)
                }
            }
        }
        // P1 #6: 主动 cancel 旧 job, 释放网络/DB 资源, 避免新旧 job 并发改写
        // _answerMessageEntity / _conversation / DB. 旧引用也写入 fetchJob 保持向后兼容.
        // 旧 job 取消时, 其父 coroutine (executeStreamingChat) 会立刻进入 catch (CancellationException),
        // 但 [AgentLoopExecutor.executeBatchedToolCalls] 内部的
        //   coroutineScope { async { executeSingleToolCall(...) }.awaitAll() }
        // 是 structured concurrency: 取消信号会传播到子协程, 但子协程要到达下一个 suspension
        // point 才会真正取消. 在这之间的 race window 内, 工具仍可能完成.
        // P1 review M1: 先置 shutdown 标记再 cancel — 窗口内完成的工具结果由执行器侧
        // persistCallbacks 落 DB, onToolCompleted 跳过 live state 修改, 避免与旧 job
        // catch 块 (persistInterruptedChat) 对同一 entity 形成并发读写.
        agentLoopOrchestrator.beginShutdown()
        fetchJob?.cancel()
        fetchJob = newJob
    }

    private fun validateChatContent(messageContent: String, templateContent: String, attachments: List<AttachedMedia> = emptyList()): Boolean {
        return messageContent.trim().isNotEmpty() || templateContent.trim().isNotEmpty() || attachments.isNotEmpty()
    }

    private fun MessageEntity.isValidHistoryContextMessage(): Boolean {
        if (expired) return false
        if (uId != MessageUIState.NORMAL.value) return false

        return when (role) {
            RoleType.USER.value -> {
                question.isNotBlank() || attachmentsJson.isNotBlank() || contentType != ContentType.TEXT.value
            }
            RoleType.ASSISTANT.value -> {
                answer.isNotBlank() || reasoningContent.isNotBlank() || toolCalls.isNotBlank()
            }
            else -> {
                question.isNotBlank() || answer.isNotBlank() || reasoningContent.isNotBlank()
            }
        }
    }

    private fun prepareQuestionMessages(messageContent: String, attachments: List<AttachedMedia> = emptyList()): MutableList<MessageEntity> {
        val questionMessageEntityList = mutableListOf<MessageEntity>()
        questionMessageEntityList.addAll(
            messages.reversed().filter { it.isValidHistoryContextMessage() }
        )
        val entity = MessageEntity(
            completionId = "${Date().time}${System.nanoTime()}${(1000..9999).random()}",
            question = messageContent,
            conversationId = _conversation.value.id,
            contentType = if (attachments.isEmpty()) ContentType.TEXT.value else ContentType.MIXED.value,
            requestProtocol = _conversation.value.engine.requestProtocol.name
        )
        if (attachments.isNotEmpty()) {
            entity.attachmentsJson = attachmentUploadCoordinator.serializeAttachmentsJson(attachments)
        }
        questionMessageEntityList.add(entity)
        return questionMessageEntityList
    }

    private fun validateUserCanChat(questionMessageEntityList: List<MessageEntity>): Boolean {
        // 自有代理引擎要求先完成联系方式验证(邮箱/手机), 未验证弹引导, 由服务端二期兜底
        if (AiUtils.canProxy(_conversation.value) && TokenStorage.isLogin() && !TokenStorage.isVerified()) {
            ActionUtils.showToast(R.string.verify_contact_ai_desc)
            AppEventBus.emit(VerifyContactEvent(source = "AIChatComponent"))
            return false
        }
        if (!AiUtils.canChat(_conversation.value, questionMessageEntityList)) {
            ActionUtils.showToast(R.string.no_points)
            AppEventBus.emit(
                MainClickEvent(
                    from = MainClickEventFrom.AI_START_CHAT,
                    type = MainShowType.BUY_COFFEE
                )
            )
            return false
        }
        return true
    }

    private fun prepareUIForChat(questionEntity: MessageEntity) {
        showLoadingUI()
        updateCurrentAnswerAndQuestion(questionEntity)
        streamContentProcessor.updatePlaceHolderMessage(forceUpdate = true)
    }

    private suspend fun renderErrorUIForChat(
        errorMessage: String,
        questionMessageEntityList: List<MessageEntity>? = null,
        skipPoints: Boolean = false
    ) {
        messagePersistenceWorker.renderErrorUIForChat(
            errorMessage = errorMessage,
            questionMessageEntityList = questionMessageEntityList,
            reasoningTime = streamContentProcessor.reasoningTime,
            skipPoints = skipPoints
        )
    }

    private val processingMutex = kotlinx.coroutines.sync.Mutex()

    /**
     * P0 #11: 标记当前 fetchJob 取消是否由 watchdog 空闲超时触发.
     *
     * 设 true 后, [executeStreamingChat] 的 catch 块会把原本走
     * "interrupted" 路径 (persistInterruptedChat, 拼上"已中断"后缀)
     * 改成走 "error" 路径 (renderErrorUIForChat, 覆盖为"网络超时"提示).
     * 同时 [onStreamIdleTimeout] 不再自行 renderErrorUIForChat, 避免
     * 与 catch 块对同一 _answerMessageEntity 双重写 + 双重 DB insert.
     */
    @Volatile
    private var streamTimeoutByWatchdog = false

    /**
     * P1 #6: 单调递增的 chat 代际号, 用于协调"快速连发场景下前序 job 误杀后续 job UI 状态".
     *
     * 旧实现: [fetchJob] 是单 var, 新的 launch 覆盖旧引用, 旧 job 的 finally
     * 块 (清 chatActive=false) 与新 job 的 chatActive=true 形成竞态 — 前序 job
     * 结束时把后续 job 的 UI 状态也"误杀"成 false.
     *
     * 新实现: 每次 [startChatWithStreaming] 触发时自增 generation, 外层 finally
     * 仅在 generation 仍是最新时清 chatActive. 同时主动 cancel 旧 job, 避免资源泄漏.
     */
    private val chatGeneration = java.util.concurrent.atomic.AtomicInteger(0)

    private suspend fun executeStreamingChat(
        questionMessageEntityList: List<MessageEntity>,
        attachments: List<AttachedMedia> = emptyList(),
        enableReasoning: Boolean = false,
        systemPromptOverride: String? = null
    ) {
        // P0 #11: 进入新一轮请求时清空 watchdog flag, 避免上轮超时的残留.
        streamTimeoutByWatchdog = false
        try {
            agentLoopOrchestrator.reset()
            streamContentProcessor.resetBuffers()
            agentLoopOrchestrator.showToolUiIdle()

            // 快照工具配置：整个请求生命周期内复用，避免重复 DB 查询和竞态条件
            val requestToolConfig = agentLoopOrchestrator.snapshotToolConfig()

            try {
                agentLoopExecutor.taskManager.deleteByConversation(_conversation.value.id)
            } catch (_: Exception) { /* ignore */ }

            val uploadedAttachments = if (attachments.isNotEmpty()) {
                val hasPendingAttachmentProcessing = attachments.any { attachment ->
                    attachment.url == null && attachment.localContent == null && attachment.localPath == null
                }
                if (hasPendingAttachmentProcessing) {
                    _chatUIState.value = _chatUIState.value.copy(pageState = PageState.INITIALIZING)
                    _answerMessageEntity.value = _answerMessageEntity.value.copy(
                        answer = applicationContext.getString(R.string.ai_chat_uploading_attachments)
                    )
                    streamContentProcessor.updatePlaceHolderMessage(forceUpdate = true)
                }

                val enableCompression = chatInputComponent.chatInputState.value.enableImageCompression
                val finalAttachments = attachmentUploadCoordinator.processAttachments(
                    attachments = attachments,
                    enableCompression = enableCompression
                )

                if (hasPendingAttachmentProcessing) {
                    _answerMessageEntity.value = _answerMessageEntity.value.copy(answer = "")
                }

                val currentQuestion = questionMessageEntityList.lastOrNull { it.role == RoleType.USER.value }
                currentQuestion?.let { entity ->
                    entity.contentType = ContentType.MIXED.value
                    entity.attachmentsJson = attachmentUploadCoordinator.serializeAttachmentsJson(
                        finalAttachments, stripLocalContent = false
                    )
                    _questionMessageEntity.value = _questionMessageEntity.value.copy(
                        contentType = entity.contentType,
                        attachmentsJson = entity.attachmentsJson
                    ).also { it.uId = _questionMessageEntity.value.uId }
                }
                finalAttachments
            } else {
                emptyList()
            }

            if (attachments.isNotEmpty()) {
                chatInputComponent.clearAttachments()
            }

            val currentQuestion = questionMessageEntityList.lastOrNull { it.role == RoleType.USER.value }?.question
                ?: _questionMessageEntity.value.question
            val enableWebSearch = AIChatStorage.isEnableWebSearch.value
            val shouldEnableReasoning = enableReasoning && _conversation.value.engine.model.canReasoning
            agentLoopOrchestrator.prepareToolSelection()
            val tools = buildRequestTools()
            val isToolCallSupported = tools != null
            logAgentTrace(
                "initial_request questionLen=${currentQuestion.length} tools=${tools?.size ?: 0} " +
                    "toolNames=${tools?.joinToString { it.function.name }.orEmpty()} " +
                    "webSearch=$enableWebSearch attachments=${uploadedAttachments.size}"
            )

            val baseConversation = if (systemPromptOverride != null) {
                _conversation.value.copy(prompt = systemPromptOverride)
            } else {
                _conversation.value
            }
            val effectiveConversation = agentLoopOrchestrator.buildEffectiveConversation(
                baseConversation = baseConversation,
                preResolvedConfig = requestToolConfig
            )

            // 拦截链首轮钩子: follow-up 回合的 before/after/onLoopError 在 AgentLoopRunner 内
            // 触发, 首轮请求不经过 Runner, 在这里补齐. iteration 固定 0: Agent Loop 迭代从 1
            // 起计, 0 表示首轮(尚未进入循环); completionId 在 ResponseStarted 前可能为空.
            val firstTurnContext = AgentTurnContext(
                conversation = effectiveConversation,
                conversationId = _conversation.value.id,
                completionId = _answerMessageEntity.value.completionId
                    .takeIf { it.isNotBlank() }
                    ?: _questionMessageEntity.value.completionId,
                iteration = 0,
            )
            var firstTurnUsage: Usage? = null
            agentLoopInterceptors.forEachInterceptor { it.beforeLlmTurn(firstTurnContext) }

            val resultFlow: Flow<LlmStreamEvent> = messageRemoteMediator.fetchAndSaveMessages(
                effectiveConversation,
                questionMessageEntityList,
                enableWebSearch,
                shouldEnableReasoning,
                tools
            )

            streamContentProcessor.startStreamWatchdog(reason = "executeStreamingChat")
            var firstTurnAborted = false
            try {
                resultFlow.onEach { streamEvent ->
                    when (streamEvent) {
                        is LlmStreamEvent.UsageUpdated -> firstTurnUsage = streamEvent.usage
                        is LlmStreamEvent.StreamAborted -> firstTurnAborted = true
                        else -> Unit
                    }
                }.collect { streamEvent: LlmStreamEvent ->
                    processingMutex.withLock {
                        handleLlmEvent(streamEvent, questionMessageEntityList)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                "resultFlow.collect EXCEPTION: ${e.javaClass.simpleName}: ${e.message}".makeLog("AIChatComponent")
                // 与 Runner 语义一致: 取消不触发 onLoopError, 首轮流异常在这里上报
                agentLoopInterceptors.forEachInterceptor { it.onLoopError(firstTurnContext, e) }
                throw e
            } finally {
                streamContentProcessor.stopStreamWatchdog()
            }
            // 流正常结束才触发 afterLlmTurn(usage 可能为空: 上游未上报);
            // 中途异常已由上面 catch 走 onLoopError, 不会执行到这里.
            agentLoopInterceptors.forEachInterceptor { it.afterLlmTurn(firstTurnContext, firstTurnUsage) }

            if (firstTurnAborted) {
                // 首轮流异常中断(未收到协议结束标记连接即关闭): 已流出的内容不完整,
                // 直接走错误 UI, 不进入 Agent Loop / 完成持久化路径(避免为失败轮次扣积分)。
                // 首轮不自动重试 —— fetchAndSaveMessages 已落库, 重试可能产生重复消息。
                renderErrorUIForChat(
                    applicationContext.getString(R.string.ai_chat_stream_interrupted),
                    questionMessageEntityList
                )
                return
            }

            if (isToolCallSupported && agentLoopOrchestrator.hasAccumulatedToolCalls()) {
                agentLoopOrchestrator.executeAgentLoop(
                    effectiveConversation = effectiveConversation,
                    originalQuestionMessages = questionMessageEntityList,
                    enableWebSearch = enableWebSearch,
                    tools = tools,
                )
                // Agent Loop 完成后持久化消息（包含工具调用链 JSON）
                messagePersistenceWorker.calculateUsageIfNull(null, questionMessageEntityList)
                messagePersistenceWorker.onChatCompletionEnd(
                    toolCallsChainJson = agentLoopOrchestrator.toolCallsChainJson,
                    startQuestionTime = streamContentProcessor.startQuestionTime
                )
            }
        } catch (e: Exception) {
            streamContentProcessor.stopStreamWatchdog()
            agentLoopOrchestrator.showToolUiIdle()
            // P1 review M1: 兜底置 shutdown 标记 (覆盖 onDestroy 等未经 cancel 入口的取消来源),
            // 本 catch 块接管期间, 窗口内完成的工具结果只入 DB, 不改 live state.
            agentLoopOrchestrator.beginShutdown()
            // 完整栈写到 logcat，UI 上的 errorMessage 只带栈首帧，避免无意义刷屏。
            "executeStreamingChat failed: ${e.javaClass.name}: ${e.message}".makeLog("AIChatComponent")
            e.stackTrace.take(5).forEach { frame ->
                "  at ${frame.className}.${frame.methodName}(${frame.fileName}:${frame.lineNumber})"
                    .makeLog("AIChatComponent")
            }
            try {
                agentLoopExecutor.taskManager.failAllIncompleteTasks(
                    _conversation.value.id,
                    if (e is CancellationException) "cancelled" else (e.message ?: "error")
                )
            } catch (_: Exception) { /* ignore cleanup errors */ }

            // P0 #11: 区分用户主动取消 (走 interrupted 路径) 与 watchdog 超时 (走 error 路径).
            // 旧逻辑: onStreamIdleTimeout 自己在另一个协程里调 renderErrorUIForChat, 与这里的
            // persistInterruptedChat 双路径并发写 _answerMessageEntity + 双重 DB insert + 双重扣分.
            val isWatchdogTimeout = streamTimeoutByWatchdog
            streamTimeoutByWatchdog = false   // 重置 flag, 避免影响下一轮请求
            when {
                e is CancellationException && !isWatchdogTimeout -> {
                    persistInterruptedChat()
                }
                else -> {
                    val errorMessage = when {
                        isWatchdogTimeout -> applicationContext.getString(R.string.agent_stream_timeout)
                        e is LlmStreamAbortedException -> applicationContext.getString(
                            R.string.ai_chat_stream_interrupted
                        )
                        else -> buildChatErrorMessage(e)
                    }
                    renderErrorUIForChat(errorMessage, questionMessageEntityList)
                }
            }
        }
        // P1 #6: chatActive 清理已上移到 [startChatWithStreaming] 的 launch lambda finally,
        // 由 chatGeneration 代际号守卫, 避免旧 job 误杀新 job 的 UI 状态.
    }

    private suspend fun handleLlmEvent(
        streamEvent: LlmStreamEvent,
        questionMessageEntityList: List<MessageEntity>
    ) {
        streamContentProcessor.recordChunkReceived()

        if (chatUIState.value.pageState == PageState.ERROR) return
        if (streamContentProcessor.streamSawEnd) return

        when (streamEvent) {
            is LlmStreamEvent.Error -> {
                // 内容风控拒绝且本轮零产出(无累积 usage、无可见内容)时不扣积分;
                // Agent 跑到一半被拒的场景仍有累积 usage, 按既有错误路径正常计费。
                val nothingProduced = _answerMessageEntity.value.totalTokens == 0 &&
                    _answerMessageEntity.value.answer.isBlank() &&
                    _answerMessageEntity.value.reasoningContent.isBlank()
                val skipPoints = streamEvent.errorCode == AiUtils.ERROR_CODE_CONTENT_FILTER &&
                    nothingProduced
                renderErrorUIForChat(
                    streamEvent.errorMessage,
                    questionMessageEntityList,
                    skipPoints = skipPoints
                )
            }

            is LlmStreamEvent.ResponseStarted -> {
                processResponseStarted(streamEvent)
            }

            is LlmStreamEvent.UsageUpdated -> {
                // Agent Loop 中每轮 LLM 请求独立上报 usage，必须累加而非覆写
                messagePersistenceWorker.accumulateMessageUsage(streamEvent.usage)
            }

            is LlmStreamEvent.SearchResultsEvent -> {
                messagePersistenceWorker.processSearchResults(
                    streamEvent.searchResults,
                    streamEvent.searchInfo
                )
            }

            is LlmStreamEvent.ToolCallDeltaEvent -> {
                agentLoopOrchestrator.accumulateToolCallDeltas(streamEvent.deltas)
                agentLoopOrchestrator.showToolCallPlanning(streamEvent.deltas.size)
                streamContentProcessor.updatePlaceHolderMessage(forceUpdate = true)
            }

            is LlmStreamEvent.TextDelta -> {
                processTextDelta(streamEvent.text)
            }

            is LlmStreamEvent.ReasoningDelta -> {
                processReasoningDelta(streamEvent.text)
            }

            is LlmStreamEvent.Completed -> {
                _answerMessageEntity.value = _answerMessageEntity.value.copy(
                    responseItemsJson = streamEvent.outputItemsJson.orEmpty(),
                    finishReason = streamEvent.finishReason.orEmpty()
                )
                streamContentProcessor.markStreamSawEnd()
                val hasVisibleAssistantOutput = _answerMessageEntity.value.answer.isNotBlank() ||
                    _answerMessageEntity.value.reasoningContent.isNotBlank()
                if (!hasVisibleAssistantOutput && !agentLoopOrchestrator.hasAccumulatedToolCalls()) {
                    renderErrorUIForChat(
                        applicationContext.getString(R.string.ai_chat_empty_response_error),
                        questionMessageEntityList
                    )
                    return
                }
                if (agentLoopOrchestrator.hasAccumulatedToolCalls()) {
                    streamContentProcessor.updatePlaceHolderMessage(forceUpdate = true)
                    return
                }
                messagePersistenceWorker.calculateUsageIfNull(null, questionMessageEntityList)
                messagePersistenceWorker.onChatCompletionEnd(
                    toolCallsChainJson = agentLoopOrchestrator.toolCallsChainJson,
                    startQuestionTime = streamContentProcessor.startQuestionTime
                )
            }

            is LlmStreamEvent.StreamAborted -> {
                // 不渲染错误、不标记 sawEnd: Agent Loop follow-up 回合由 Runner 捕获后重试;
                // 首轮/非 Agent 场景由 executeStreamingChat 在 collect 结束后统一渲染错误。
                "stream aborted before end marker (responseId=${streamEvent.responseId})"
                    .makeLog("AIChatComponent")
            }
        }
    }

    private fun onStreamIdleTimeout() {
        // P0 #11: 仅置 flag + cancel fetchJob, 不再自行 renderErrorUIForChat.
        // 真正的错误 UI + DB 持久化由 executeStreamingChat 的 catch 块统一处理,
        // 避免与 persistInterruptedChat 对 _answerMessageEntity / DB 双重写.
        streamTimeoutByWatchdog = true
        // P1 review M1: 先置 shutdown 标记, 窗口内完成的工具结果只入 DB, 不改 live state.
        agentLoopOrchestrator.beginShutdown()
        runCatching { fetchJob?.cancel() }
    }

    private fun processResponseStarted(event: LlmStreamEvent.ResponseStarted) {
        if (event.responseId.isBlank()) return
        _answerMessageEntity.value = _answerMessageEntity.value.copy(
            completionId = event.responseId,
            providerResponseId = event.responseId,
            requestProtocol = _conversation.value.engine.requestProtocol.name,
            contentUrl = event.responseId
        )
        _questionMessageEntity.value = _questionMessageEntity.value.copy(
            completionId = event.responseId,
            providerResponseId = event.responseId,
            requestProtocol = _conversation.value.engine.requestProtocol.name,
            contentUrl = event.responseId
        )
    }

    private suspend fun processTextDelta(content: String) {
        streamContentProcessor.updateReasoningTimeAndUid()
        if (content.isBlank()) return
        streamContentProcessor.appendDeltaAndRender(
            content = content,
            update = { streamContentProcessor.updateAnswerContentOptimized(it) }
        )
    }

    private suspend fun processReasoningDelta(content: String) {
        if (!_conversation.value.engine.model.canReasoning || !AIChatStorage.isEnableReasoning.value) {
            return
        }
        streamContentProcessor.updateReasoningTimeAndUid()
        if (content.isBlank()) return
        streamContentProcessor.reasoningTime =
            (System.currentTimeMillis() - streamContentProcessor.startQuestionTime) / 1000
        streamContentProcessor.appendDeltaAndRender(
            content = content,
            update = { streamContentProcessor.updateReasoningContentOptimized(it) }
        )
    }

    fun consumePoints(messageEntity: MessageEntity) =
        messagePersistenceWorker.consumePoints(messageEntity)

    private fun updateCurrentAnswerAndQuestion(questionEntity: MessageEntity) {
        _questionMessageEntity.value = questionEntity.copy(
            question = questionEntity.question.trim(),
            conversationId = _conversation.value.id,
            expired = false
        ).also { it.uId = MessageUIState.LOADING.value }
        _answerMessageEntity.value = AiUtils.newAnswerMessageEntity(_conversation.value)
        _answerMessageEntity.value = _answerMessageEntity.value.copy(
            completionId = _questionMessageEntity.value.completionId,
            conversationId = _questionMessageEntity.value.conversationId,
            expired = false
        ).also { it.uId = MessageUIState.LOADING.value }
    }

    /**
     * 请求取消：若当前正在流式输出，先弹确认弹窗；
     * 若已有内容则提示用户"后台继续"或"停止回复"；
     * 若无内容（刚发送还没收到响应）则直接取消。
     */
    fun requestCancelFetch() {
        if (!_chatUIState.value.chatActive) return
        // 已生成内容（answer 或 reasoning）时才弹确认弹窗；刚发请求尚无内容时直接取消
        val hasContent = _answerMessageEntity.value.answer.isNotBlank()
            || _answerMessageEntity.value.reasoningContent.isNotBlank()
        if (hasContent) {
            _showCancelConfirmDialog.value = true
        } else {
            confirmCancelFetch()
        }
    }

    /** 取消确认弹窗中用户选择"停止回复" */
    fun confirmCancelFetch() {
        _showCancelConfirmDialog.value = false
        componentScope.launch {
            attachmentUploadCoordinator.clearPendingImageCaches()

            streamContentProcessor.stopStreamWatchdog()
            // P1 review M3: 同 [cancelCurrentTool], 防御性重置 watchdog flag.
            streamTimeoutByWatchdog = false
            // P1 review M1: 先置 shutdown 标记再 cancel, 窗口内完成的工具结果只入 DB.
            agentLoopOrchestrator.beginShutdown()
            fetchJob?.cancel()
            _chatUIState.value = _chatUIState.value.copy(chatActive = false)
            // fetchJob 被 cancel 后会在 executeStreamingChat 的 catch 块中触发 persistInterruptedChat()，
            // 此处不重复调用，避免双重入库
        }
    }

    /** 取消确认弹窗中用户选择"后台继续"（仅关闭弹窗，不中断流式） */
    fun dismissCancelConfirm() {
        _showCancelConfirmDialog.value = false
    }

    /**
     * 持久化已中断的聊天内容：不再标 expired，正常入库，
     * 用户可在聊天历史中查看已生成的内容。
     */
    private suspend fun persistInterruptedChat() {
        // 保留已累积的服务器 usage，仅在无累积值时才降级到本地估算
        messagePersistenceWorker.calculateUsageIfNull(null, questionMessageEntityList)
        val currentTime = Date()
        val insertCompletionId = _questionMessageEntity.value.completionId
        _questionMessageEntity.value = _questionMessageEntity.value.copy(
            completionId = insertCompletionId,
            createdAt = currentTime,
            entryType = _conversation.value.entryType,
            entryRefId = _conversation.value.entryRefId,
            title = _conversation.value.title,
            reasoningTime = streamContentProcessor.reasoningTime,
        ).also { it.uId = MessageUIState.NORMAL.value }
        // 在已生成内容末尾追加中断标记（而非取消标记）
        val interruptedSuffix = applicationContext.getString(R.string.ai_chat_interrupted)
        _answerMessageEntity.value = _answerMessageEntity.value.copy(
            completionId = insertCompletionId,
            createdAt = Date(currentTime.time + 1),
            entryType = _conversation.value.entryType,
            entryRefId = _conversation.value.entryRefId,
            title = _conversation.value.title,
            reasoningTime = streamContentProcessor.reasoningTime,
            answer = _answerMessageEntity.value.answer + "\n\n$interruptedSuffix"
        ).also { it.uId = MessageUIState.NORMAL.value }
        // 正常入库（expired = false），用户可在聊天列表中看到这条消息
        val questionEntityForDb = _questionMessageEntity.value.let { entity ->
            val strippedJson = AttachmentPayloadUtils.stripLocalContent(entity.attachmentsJson, gson)
            if (strippedJson == entity.attachmentsJson) entity
            else entity.copy(attachmentsJson = strippedJson)
        }
        messageListUseCase.insertQuestionAndAnswer(
            questionEntityForDb,
            _answerMessageEntity.value,
            _conversation.value
        )
        messagePersistenceWorker.syncPersistedMessageIds(insertCompletionId)
        streamContentProcessor.updatePlaceHolderMessage(forceUpdate = true)
        messagePersistenceWorker.persistConversationSnapshot(
            userMessage = _questionMessageEntity.value.question,
            assistantMessage = _answerMessageEntity.value.answer,
            messageIncrement = 2,
            timestamp = currentTime.time
        )
        consumePoints(_answerMessageEntity.value)
    }

    fun retryChat() {
        if (_questionMessageEntity.value.question.isNotEmpty()) {
            componentScope.launch(ioDispatcher) {
                val attachments = attachmentUploadCoordinator.resolveAttachmentsFromJson(
                    _questionMessageEntity.value.attachmentsJson
                )
                startChatWithStreaming(
                    messageContent = _questionMessageEntity.value.question,
                    attachments = attachments
                ) {}
            }
        }
    }

    fun reGenerateMessage(completionId: String) {
        componentScope.launch(Dispatchers.IO) {
            if (completionId.isNotEmpty()) {
                val rowId = messageDao.updateMessageEntityExpiredByCompletionId(
                    completionId,
                    true
                )
                if (rowId > 0) {
                    messages.removeAll {
                        it.completionId == completionId
                    }
                    messageDao.queryQuestionAndAnswerByCompletionId(completionId)
                        .let {
                            _questionMessageEntity.value = it.first()
                            _answerMessageEntity.value = it.last()
                            retryChat()
                        }
                }
            }
        }
    }

    fun cancelCurrentTool() {
        componentScope.launch {
            attachmentUploadCoordinator.clearPendingImageCaches()
            streamContentProcessor.stopStreamWatchdog()
            // P1 review M3: 防御性重置 watchdog flag, 避免 cancel 时机晚于 watchdog 触发
            // 导致 flag 残留. 严格说下一轮 executeStreamingChat 入口会再次重置, 这里只是
            // 缩短 flag 残留窗口, 减少误判可能.
            streamTimeoutByWatchdog = false
            // P1 review M1: 先置 shutdown 标记再 cancel, 窗口内完成的工具结果只入 DB.
            agentLoopOrchestrator.beginShutdown()
            fetchJob?.cancel()
            agentLoopOrchestrator.showToolUiIdle()
            _chatUIState.value = _chatUIState.value.copy(chatActive = false)
            streamContentProcessor.updatePlaceHolderMessage(forceUpdate = true)
        }
    }

    override fun deleteMessage(completionId: String) {
        if (completionId.isBlank()) return
        componentScope.launch(ioDispatcher) {
            val rowsDeleted = messageRepository.deleteMessagesByCompletionId(completionId)
            if (rowsDeleted > 0) {
                synchronized(messages) {
                    messages.removeAll { it.completionId == completionId }
                }
                streamContentProcessor.invalidateMessageCache()
                streamContentProcessor.renderMessage(forceUpdate = true)
            } else {
                makeLog { "deleteMessage: No rows were deleted" }
            }
        }
    }

    fun continueAgentLoop() = agentLoopOrchestrator.continueAgentLoop()

    /**
     * 把流式/Agent Loop 阶段的异常转成最终展示给用户的错误文本。
     *
     * 仅在 [executeStreamingChat] 的兜底 catch 中调用，此时异常 message 经常是
     * Java 内部 NPE（"key == null || value == null" 之类），对用户没有可读性，
     * 但对开发定位又极有价值 —— 因此把异常类型 + 栈首帧拼到 message 后面。
     */
    private fun buildChatErrorMessage(e: Throwable): String {
        val base = e.message?.takeIf { it.isNotBlank() }
            ?: "${e.javaClass.simpleName} (no message)"
        val origin = e.stackTrace.firstOrNull { frame ->
            !frame.className.startsWith("kotlin.coroutines") &&
                !frame.className.startsWith("kotlinx.coroutines")
        }
        val tail = origin?.let { frame ->
            "${e.javaClass.simpleName} @ ${frame.className}.${frame.methodName}" +
                "(${frame.fileName}:${frame.lineNumber})"
        } ?: e.javaClass.simpleName
        return "$base · $tail"
    }

    override fun onModelChanged(aiModel: AiModel) {
        // 进入聊天页 / 切换模型后,若当前是端侧引擎则后台预热(大内存设备才生效),
        // 用户看历史消息与打字的几秒里模型已加载完,首 token 接近秒回
        localLlmSessionManager.maybeWarmUp(_conversation.value.engine)
    }

    fun onDestroy() {
        streamContentProcessor.stopStreamWatchdog()
        agentLoopOrchestrator.onDestroy()
        observeMessagesJob?.cancel()
        clearMessages()
        fetchJob?.cancel()
        chatInputComponent.onDispose()
        // 离开聊天页延迟释放端侧模型权重(60s 内重回会取消),模型只在聊天场景存活
        localLlmSessionManager.scheduleRelease()
    }
}
