package com.shifenmiao.ai.component

import android.content.Context
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.ai.export.HtmlExporter
import com.shifenmiao.ai.history.withSummaryTitle
import com.shifenmiao.ai.prompt.PromptLoadResult
import com.shifenmiao.ai.prompt.PromptManager
import com.shifenmiao.ai.repository.ConversationRepository
import com.shifenmiao.ai.repository.MessageRepository
import com.shifenmiao.ai.usecase.MessageListUseCase
import com.shifenmiao.ai.utils.AiUtils
import com.shifenmiao.ai.utils.PromptBuildUtils
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.common.manager.AIEngineSyncManager
import com.shifenmiao.core.R
import com.shifenmiao.database.ai.entity.ConversationEntity
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.AIChatObject
import com.shifenmiao.model.Source
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.state.ChatUIState
import com.shifenmiao.model.state.PageState
import com.shifenmiao.storage.AIChatStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.resource.ResourceManager
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.a2ui.catalog.A2uiRenderProvider
import com.t8rin.logger.makeLog
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

data class PromptCardState(
    val title: String = "",
    val isSystemPrompt: Boolean = false,
    val updatedAtMillis: Long? = null,
    val source: Source? = null,
)

open class AIChatBaseComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted conversationParams: Conversation,
    dispatchersHolder: DispatchersHolder,
    resourceManager: ResourceManager,
    val aiEngineManager: AIEngineManager,
    val aiEngineCatalogManager: AIEngineCatalogManager,
    private val aiEngineSyncManager: AIEngineSyncManager,
    val applicationContext: Context,
    open val messageListUseCase: MessageListUseCase,
    private val promptManager: PromptManager,
    private val conversationRepository: ConversationRepository,
    protected val messageRepository: MessageRepository,
    val a2uiRenderProvider: A2uiRenderProvider,
    private val enablePromptInitialization: Boolean = true,
    private val enableModelRefresh: Boolean = true,
) : BaseComponent(dispatchersHolder, componentContext), ResourceManager by resourceManager {

    var _chatUIState = MutableStateFlow(ChatUIState())
    val chatUIState: StateFlow<ChatUIState> = _chatUIState

    var _conversation = MutableStateFlow(conversationParams)
    val conversation: StateFlow<Conversation> = _conversation

    val allEngines: StateFlow<List<AiEngine>> = aiEngineCatalogManager.observeAvailableEngines()
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val modelsByProvider: StateFlow<Map<String, List<AiModel>>> =
        aiEngineCatalogManager.observeModelsByProvider()
            .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val _promptCardState = MutableStateFlow(
        PromptCardState(
            title = conversationParams.appTitle.ifBlank { conversationParams.title }
        )
    )
    val promptCardState: StateFlow<PromptCardState> = _promptCardState

    /**
     * 线程安全的消息列表。
     *
     * 使用 synchronized 块保证 clear+addAll 的原子性语义。
     * 遍历、单条 add/remove 同样在 synchronized(messages) 中操作。
     */
    var messages: MutableList<MessageEntity> = mutableListOf()

    /**
     * Placeholder data for the question message entity.
     */
    var _questionMessageEntity =
        MutableStateFlow(AiUtils.newQuestionMessageEntity(_conversation.value))
    var _answerMessageEntity = MutableStateFlow(AiUtils.newAnswerMessageEntity(_conversation.value))

    /**
     * 当前正在流式输出的 AI 回答纯文本，供 PiP 极简视图订阅。
     * 流式结束后会随着答案消息状态刷新而回到空串。
     */
    val streamingAnswerText: StateFlow<String> =
        _answerMessageEntity
            .map { it.answer }
            .stateIn(componentScope, SharingStarted.WhileSubscribed(), "")


    var fetchJob: Job? = null

    private val _historyMessageEntityListFlow: MutableStateFlow<PagingData<MessageEntity>> =
        MutableStateFlow(value = PagingData.empty())
    val historyMessageEntityListFlow: MutableStateFlow<PagingData<MessageEntity>> get() = _historyMessageEntityListFlow

    private var historyLoadJob: Job? = null

    init {
        initConversation()
        initListenerConversation()
        componentScope.launch {
            if (enablePromptInitialization) {
                initializePrompt()
            } else {
                withContext(Dispatchers.Main) {
                    initReady()
                }
            }
        }
        if (enableModelRefresh) {
            componentScope.launch {
                refreshModels()
            }
        }
    }

    private suspend fun refreshModels() {
        aiEngineSyncManager.refreshEnginesFromRemote()
    }


    /**
     * 子类初始化回调 —— 在 prompt 加载完成后调用。
     *
     * ⚠️ 调用时机保证：此方法通过 [Dispatchers.Main]（非 immediate）派发，
     * 确保在所有子类构造器和字段初始化完成之后才执行。
     * 子类可安全地在此方法中访问自身声明的属性。
     */
    open fun initReady() {

    }

    private suspend fun initializePrompt() {
        val initializationPlan = promptManager.prepareInitializationPlan(_conversation.value)
        initializationPlan?.initialPrompt?.let(::applyPromptLoadResult)
        initializationPlan?.refreshRemotePromptId?.let(::refreshPromptInBackground)
        // 通过 Dispatchers.Main（非 immediate）派发 initReady()，
        // 确保当前同步调用栈（包括所有子类构造器）完成后才执行，
        // 从根本上避免基类 init 调用虚方法时子类字段尚未初始化的问题。
        withContext(Dispatchers.Main) {
            initReady()
        }
    }

    private fun refreshPromptInBackground(remotePromptId: com.shifenmiao.network.api.RemoteId) {
        // conversation.promptId 语义是 item_prompt.id（资源表主键）
        val localPromptId = _conversation.value.promptId?.takeIf { it > 0 } ?: return
        componentScope.launch(ioDispatcher) {
            val refreshedResult = promptManager.refreshPrompt(remotePromptId, localPromptId) ?: return@launch
            withContext(Dispatchers.Main) {
                applyPromptLoadResult(refreshedResult)
            }
        }
    }

    private fun applyPromptLoadResult(result: PromptLoadResult) {
        val currentConversation = _conversation.value
        val localPromptId = result.localPromptId
        val remotePromptId = result.prompt.remoteId
        _conversation.value = currentConversation.copy(
            prompt = result.prompt.prompt.orEmpty(),
            title = result.prompt.title.orEmpty(),
            placeholder = result.prompt.placeholder.orEmpty(),
            template = result.prompt.templates,
            promptId = localPromptId ?: currentConversation.promptId,
            promptRemoteId = remotePromptId ?: currentConversation.promptRemoteId,
            promptDocumentId = result.prompt.documentId ?: currentConversation.promptDocumentId,
            entryRefId = if (currentConversation.entryType == AIConversationEntryType.PROMPT && remotePromptId != null) {
                remotePromptId.toString()
            } else {
                currentConversation.entryRefId
            }
        )
        _promptCardState.value = PromptCardState(
            title = result.prompt.title.orEmpty().ifBlank {
                _conversation.value.appTitle.ifBlank { _conversation.value.title }
            },
            isSystemPrompt = result.isSystemPrompt,
            updatedAtMillis = result.updatedAtMillis,
            source = result.prompt.source,
        )
    }

    private fun initConversation() {
        componentScope.launch {
            if (AiUtils.isAssistant(_conversation.value) && !_conversation.value.showLastMessage) {
                val localConversation = AIChatStorage.loadConfigs(_conversation.value.entryType.name)
                if (localConversation != null) {
                    _conversation.value = localConversation
                }
            }
        }
    }

    private fun initListenerConversation() {
        componentScope.launch {
            aiEngineManager.currentAIModel.collectLatest { currentAIModel ->
                if (_conversation.value.entryType != AIConversationEntryType.DUEL) {
                    _conversation.value = _conversation.value.copy(
                        engine = aiEngineManager.getCurrentAiEngine()
                    )
                }
                onModelChanged(currentAIModel)
            }
        }
    }

    open fun onModelChanged(aiModel: AiModel) {}

    fun hideHistory() {
        _chatUIState.value = _chatUIState.value.copy(
            showHistory = false
        )
    }

    open fun showHistory() {
        _chatUIState.value = _chatUIState.value.copy(
            showHistory = true
        )
        loadHistoryMessageList()
    }

    fun showFailureUI(msg: String? = null) {
        _chatUIState.value = _chatUIState.value.copy(
            pageState = PageState.ERROR,
            chatActive = false,
            errorMessage = msg
                ?: AppContext.getString(R.string.error_message)
        )
    }

    fun hideFailureUI() {
        _chatUIState.value = _chatUIState.value.copy(
            pageState = PageState.IDLE,
            errorMessage = ""
        )
    }

    fun showLoadingUI() {
        _chatUIState.value = _chatUIState.value.copy(
            chatActive = true,
            pageState = PageState.IDLE
        )
    }

    fun markPageReady() {
        _chatUIState.value = _chatUIState.value.copy(pageState = PageState.IDLE)
    }

    open fun showSuccessUI() {
        _chatUIState.value = _chatUIState.value.copy(pageState = PageState.IDLE)
    }

    private fun clearCurrentQuestionAndAnswer() {
        messages.clear()
        _questionMessageEntity.value = AiUtils.newQuestionMessageEntity(_conversation.value)
        _answerMessageEntity.value = AiUtils.newAnswerMessageEntity(_conversation.value)
    }

    fun getTemplateContent(): String {
        _conversation.value.template?.let { body ->
            var startMessage = PromptBuildUtils.buildPromptString(body)
            startMessage = startMessage?.let {
                if (it.endsWith(",") || it.endsWith("，")) {
                    it.dropLast(1) + "。"
                } else {
                    it
                }
            }
            return startMessage ?: ""
        }
        return ""
    }

    private fun loadHistoryMessageList() {
        // 取消之前的任务以防止重复加载
        historyLoadJob?.cancel()
        historyLoadJob = componentScope.launch(ioDispatcher) {
            try {
                messageListUseCase.getHistoryMessageList(Unit)
                    .distinctUntilChanged()
                    .cachedIn(componentScope)
                    .collect {
                        _historyMessageEntityListFlow.value = it
                    }
            } catch (e: Exception) {
                makeLog { "Error loading history: ${e.message}" }
                // 在生产环境中可能需要处理这个错误
            }
        }
    }

    fun deleteHistoryMessageEntity(messageEntity: MessageEntity) {
        componentScope.launch(ioDispatcher) {
            try {
                val deleteId = messageRepository.deleteMessagesByConversationId(messageEntity.conversationId)
                if (deleteId > 0) {
                    messageListUseCase.deleteHistoryByConversationId(messageEntity.conversationId)
                    // 刷新历史记录列表
                    loadHistoryMessageList()
                }
            } catch (e: Exception) {
                makeLog { "Error deleting message: ${e.message}" }
                ActionUtils.showToast(applicationContext.getString(R.string.ai_error_unknown))
            }
        }
    }

    fun loadHistoryById(conversationId: String) {
        componentScope.launch {
            val lastConversation = conversationRepository.getConversationByConversationId(conversationId)
            if (lastConversation != null) {
                _conversation.value = lastConversation
                changeConversationId(lastConversation.id)
            } else {
                changeConversationId(conversationId)
            }
        }
    }

    fun changeConversationId(newId: String) {
        componentScope.launch {
            if (messages.isEmpty()) {
                ActionUtils.showToast(applicationContext.getString(R.string.already_new_chat))
            }
            clearMessages()
            _conversation.value = _conversation.value.copy(
                id = newId,
                title = "",
                titleSource = com.shifenmiao.model.ai.AIConversationTitleSource.SYSTEM
            )
            clearCurrentQuestionAndAnswer()
            loadMessages()
            hideHistory()
            hideFailureUI()
        }
    }

    /**
     * AI 自动摘要标题的统一落点。
     * 后续接入模型生成标题时，直接调用这里即可同步更新会话中心与消息标题。
     */
    fun applyConversationTitleSummary(summaryTitle: String) {
        componentScope.launch(ioDispatcher) {
            val updatedConversation = _conversation.value.withSummaryTitle(summaryTitle)
            if (updatedConversation.title == _conversation.value.title &&
                updatedConversation.titleSource == _conversation.value.titleSource
            ) return@launch

            _conversation.value = updatedConversation
            conversationRepository.updateConversationTitle(
                conversationId = updatedConversation.id,
                title = updatedConversation.title,
                titleSource = updatedConversation.titleSource.name
            )
            messageRepository.updateTitlesByConversationId(
                conversationId = updatedConversation.id,
                title = updatedConversation.title
            )
        }
    }

    open fun loadMessages() {

    }

    open fun clearMessages() {
        synchronized(messages) {
            messages.clear()
        }
    }

    protected open fun submitToAIChat(
        onNavigate: (Screen) -> Unit,
        promptText: String? = null
    ) {
    }

    fun navigateToChatScreen(
        onNavigate: (Screen) -> Unit,
        aiChatObject: AIChatObject? = null
    ) {
        aiChatObject?.let {
            onNavigate(
                Screen.AgentDetail(
                    chatObject = it
                )
            )
        }
    }

    open fun deleteMessage(completionId: String) {
        componentScope.launch {
            val rowsDeleted = messageRepository.deleteMessagesByCompletionId(completionId)
            if (rowsDeleted <= 0) {
                makeLog { "deleteMessage: No rows were deleted" }
            }
        }
    }

    /**
     * AIChatBaseComponent的扩展函数，用于导出聊天记录为HTML
     */
    fun exportChatHistory(
        exportHtml: (String, String) -> Unit,
    ) {
        componentScope.launch {
            val exporter = HtmlExporter()
            exportHtml(
                exporter.exportToHtml(
                    conversation.value,
                    messages,
                    aiEngineCatalogManager,
                ),
                exporter.aIgcInfoString
            )
        }
    }
}
