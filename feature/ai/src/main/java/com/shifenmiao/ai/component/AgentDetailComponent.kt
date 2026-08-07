package com.shifenmiao.ai.component

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.google.gson.Gson
import com.halilibo.richtext.markwon.MarkdownAstNodeParser
import com.shifenmiao.ai.agent.AgentLoopExecutor
import com.shifenmiao.ai.agent.tool.ConversationToolPolicyRepository
import com.shifenmiao.ai.agent.tool.InteractiveToolRuntime
import com.shifenmiao.ai.agent.tool.AgentToolRegistry
import com.shifenmiao.ai.agent.tool.ToolBindingRepository
import com.shifenmiao.ai.logic.ChatInputComponent
import com.shifenmiao.ai.mediator.MessageRemoteMediator
import com.shifenmiao.ai.prompt.PromptManager
import com.shifenmiao.ai.prompt.SystemPromptRepository
import com.wanbaohe.a2ui.catalog.A2uiRenderProvider
import com.shifenmiao.ai.repository.ConversationRepository
import com.shifenmiao.ai.repository.MessageRepository
import com.shifenmiao.ai.service.ConversationTitleSummaryService
import com.shifenmiao.ai.service.PromptTemplateToolService
import com.shifenmiao.ai.upload.AttachmentContentResolver
import com.shifenmiao.ai.upload.FileUploadRouter
import com.shifenmiao.ai.usecase.MessageListUseCase
import com.shifenmiao.ai.utils.AiUtils
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.common.manager.AIEngineSyncManager
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.ai.entity.ConversationEntity
import com.shifenmiao.database.image.dao.ImageDao
import com.shifenmiao.database.utils.DataBaseUtils
import com.shifenmiao.model.AIChatObject
import com.shifenmiao.model.ai.Agent
import com.shifenmiao.network.api.ApiService
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.state.PageState
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.resource.ResourceManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class AgentDetailComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val aiChatObject: AIChatObject,
    dispatchersHolder: DispatchersHolder,
    resourceManager: ResourceManager,
    appDatabase: AppDatabase,
    apiService: ApiService,
    aiEngineManager: AIEngineManager,
    aiEngineCatalogManager: AIEngineCatalogManager,
    aiEngineSyncManager: AIEngineSyncManager,
    promptManager: PromptManager,
    conversationRepository: ConversationRepository,
    messageRepository: MessageRepository,
    messageRemoteMediator: MessageRemoteMediator,
    agentLoopExecutor: AgentLoopExecutor,
    interactiveToolBridge: InteractiveToolRuntime,
    globalToolUiHost: GlobalToolUiHost,
    private val localAgentToolRegistry: AgentToolRegistry,
    private val toolBindingRepository: ToolBindingRepository,
    conversationToolPolicyRepository: ConversationToolPolicyRepository,
    promptTemplateToolService: PromptTemplateToolService,
    fileUploadRouter: FileUploadRouter,
    attachmentContentResolver: AttachmentContentResolver,
    systemPromptRepository: SystemPromptRepository,
    conversationTitleSummaryService: ConversationTitleSummaryService,
    @ApplicationContext applicationContext: Context,
    messageListUseCase: MessageListUseCase,
    markdownAstNodeParserFactory: MarkdownAstNodeParser.Factory,
    chatInputComponentFactory: ChatInputComponent.Factory,
    gson: Gson,
    imageDao: ImageDao,
    a2uiRenderProvider: A2uiRenderProvider
) : AIChatComponent(
    componentContext = componentContext,
    conversationParams = aiChatObject.conversation,
    interactionOwnerId = "agent_detail_${aiChatObject.agentId}_${componentContext.hashCode()}",
    ownsInteractiveRuntimeLifecycle = true,
    dispatchersHolder = dispatchersHolder,
    resourceManager = resourceManager,
    appDatabase = appDatabase,
    apiService = apiService,
    aiEngineManager = aiEngineManager,
    aiEngineCatalogManager = aiEngineCatalogManager,
    aiEngineSyncManager = aiEngineSyncManager,
    promptManager = promptManager,
    conversationRepository = conversationRepository,
    messageRepository = messageRepository,
    appContext = applicationContext,
    messageListUseCase = messageListUseCase,
    messageRemoteMediator = messageRemoteMediator,
    agentLoopExecutor = agentLoopExecutor,
    markdownAstNodeParserFactory = markdownAstNodeParserFactory,
    chatInputComponentFactory = chatInputComponentFactory,
    interactiveToolBridge = interactiveToolBridge,
    globalToolUiHost = globalToolUiHost,
    toolBindingRepository = toolBindingRepository,
    agentToolRegistry = localAgentToolRegistry,
    conversationToolPolicyRepository = conversationToolPolicyRepository,
    promptTemplateToolService = promptTemplateToolService,
    fileUploadRouter = fileUploadRouter,
    attachmentContentResolver = attachmentContentResolver,
    systemPromptRepository = systemPromptRepository,
    conversationTitleSummaryService = conversationTitleSummaryService,
    gson = gson,
    imageDao = imageDao,
    a2uiRenderProvider = a2uiRenderProvider
) {
    private var _isRecommendState = MutableStateFlow(false)
    val isRecommendState: StateFlow<Boolean> = _isRecommendState

    init {
        initDataAndStart()
    }

    private fun initDataAndStart() {
        _conversation.value = _conversation.value.copy(
            entryType = AIConversationEntryType.AGENT,
            entryRefId = aiChatObject.agentId,
            appTitle = _conversation.value.appTitle
        )
        if (aiChatObject.message.isNullOrEmpty()) {
            fetchLocalData()
            if (aiChatObject.showHistory) {
                showHistory()
                loadLastConversation()
            } else {
                markPageReady()
            }
            return
        }
        _isRecommendState.value = false
        startChatWithStreaming(aiChatObject.message!!)
    }

    fun loadLastConversation() {
        componentScope.launch(Dispatchers.IO) {
            val lastConversation =
                appDatabase.conversationDao().getLastConversationByEntry(
                    entryType = AIConversationEntryType.AGENT.name,
                    entryRefId = aiChatObject.agentId
                )
            lastConversation?.let { conversationEntity ->
                _conversation.value = _conversation.value.copy(
                    id = conversationEntity.conversationId,
                    title = conversationEntity.title,
                    appTitle = conversationEntity.appTitle,
                    prompt = conversationEntity.prompt,
                    entryType = AIConversationEntryType.AGENT,
                    entryRefId = aiChatObject.agentId,
                    titleSource = ConversationEntity.toConversation(conversationEntity).titleSource,
                    lastMessagePreview = conversationEntity.lastMessagePreview,
                    lastUserMessagePreview = conversationEntity.lastUserMessagePreview,
                    lastActiveAt = conversationEntity.lastActiveAt,
                    messageCount = conversationEntity.messageCount,
                )
            }
        }
    }

    override fun showSuccessUI() {
        _isRecommendState.value = true
        _chatUIState.value = _chatUIState.value.copy(
            pageState = PageState.IDLE
        )
    }

    override fun onModelChanged(aiModel: AiModel) {
        if (messages.firstOrNull() != null) {
            reGenerateMessage(completionId = messages.first().completionId)
        }
    }

    private fun fetchLocalData() {
        componentScope.launch(Dispatchers.IO) {
            val agentEntity = appDatabase.agentDao().getAgentById(aiChatObject.agentId.toInt())
            agentEntity?.let {
                val agent = DataBaseUtils.agentEntityToAgent(it)
                if (!agent.dynamicBody.isNullOrBlank()) {
                    updateAgentAndConversation(agent)
                }
            }
        }
    }

    private fun updateAgentAndConversation(agent: Agent) {
        _conversation.value = _conversation.value.copy(
            appTitle = agent.title.orEmpty(),
            prompt = agent.prompt.orEmpty(),
            entryType = AIConversationEntryType.AGENT,
            entryRefId = agent.id.toString()
        )
    }

    override suspend fun resolveBoundToolNames(): Set<String>? {
        // Agent 详情页必须严格受绑定表约束。
        // 若当前 Agent 还没有绑定记录，也不要回退到全量工具，避免越权把所有工具发给模型。
        return toolBindingRepository.getAgentBoundToolNames(aiChatObject.agentId.toInt()).orEmpty()
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            aiChatObject: AIChatObject
        ): AgentDetailComponent
    }
}
