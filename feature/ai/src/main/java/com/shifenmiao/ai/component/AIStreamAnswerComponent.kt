package com.shifenmiao.ai.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.ai.history.withHistorySnapshot
import com.shifenmiao.ai.history.withSummaryTitle
import com.shifenmiao.ai.mediator.MessageRemoteMediator
import com.shifenmiao.ai.service.ConversationTitleSummaryService
import com.shifenmiao.ai.usecase.MessageListUseCase
import com.shifenmiao.common.ai.AIPromptExecutor
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.ai.entity.ConversationEntity
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.RoleType
import com.shifenmiao.model.ai.unified.LlmStreamEvent
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import java.util.UUID

/**
 * 轻量 AI 问答流式输出组件
 *
 * 职责：
 * 1. 接收 systemPrompt + question，自动发起 AI 请求
 * 2. 支持流式（SSE）和同步两种模式（useStreaming 参数控制）
 * 3. 流/同步结束后通过 MessageListUseCase 持久化对话记录（写入 ActivityLog）
 * 4. 每次进入 Screen 生成新 UUID 作为 conversationId，互不干扰
 * 5. 暴露 retry() 方便 UI 重试
 */
class AIStreamAnswerComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val screen: Screen.AIStreamAnswer,
    dispatchersHolder: DispatchersHolder,
    private val aiEngineManager: AIEngineManager,
    private val messageRemoteMediator: MessageRemoteMediator,
    private val aiPromptExecutor: AIPromptExecutor,
    private val conversationTitleSummaryService: ConversationTitleSummaryService,
    private val messageListUseCase: MessageListUseCase,
    private val appDatabase: AppDatabase,
) : BaseComponent(dispatchersHolder, componentContext) {

    // ── 每次进入页面生成新的 conversationId ───────────────────────────
    private val conversationId: String = UUID.randomUUID().toString()

    // ── 对外暴露的状态 ────────────────────────────────────────────────
    private val _accumulatedText = MutableStateFlow("")
    val accumulatedText: StateFlow<String> = _accumulatedText.asStateFlow()

    /** 深度思考（reasoning）累积内容 */
    private val _reasoningText = MutableStateFlow("")
    val reasoningText: StateFlow<String> = _reasoningText.asStateFlow()

    private val _status = MutableStateFlow(AIStreamAnswerStatus.LOADING)
    val status: StateFlow<AIStreamAnswerStatus> = _status.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    /** "引擎名 · 模型名" 用于底部说明 */
    private val _engineInfo = MutableStateFlow("")
    val engineInfo: StateFlow<String> = _engineInfo.asStateFlow()

    /** TopBar 展示标题 */
    val displayTitle: String
        get() = screen.label.ifEmpty { screen.question.take(24) }

    /** UI 判断是否需要打字机动画（同步模式需要动画，流式模式已实时累积无需动画） */
    val useStreaming: Boolean get() = screen.useStreaming

    private var answerJob: Job? = null

    init {
        startAnswer()
    }

    // ── 公开操作 ──────────────────────────────────────────────────────

    fun retry() {
        _accumulatedText.value = ""
        _reasoningText.value = ""
        _status.value = AIStreamAnswerStatus.LOADING
        _errorMessage.value = ""
        startAnswer()
    }

    fun cancelAnswer() {
        answerJob?.cancel()
        answerJob = null
    }

    // ── 内部逻辑 ─────────────────────────────────────────────────────

    private fun startAnswer() {
        answerJob?.cancel()
        answerJob = componentScope.launch(Dispatchers.IO) {
            val engine = aiEngineManager.getCurrentAiEngine()
            _engineInfo.value = buildEngineInfo(engine)
            if (screen.useStreaming) {
                runStreamingAnswer(engine)
            } else {
                runSyncAnswer(engine)
            }
        }
    }

    // ── 流式路径 ──────────────────────────────────────────────────────

    private suspend fun runStreamingAnswer(engine: AiEngine) {
        val conversation = buildConversation(engine)
        val questionMessage = buildQuestionMessage(engine, conversation)

        // 注意：不要在一开始就切到 STREAMING。
        // 深度思考模式下模型会先长时间只输出 ReasoningDelta，若提前切换状态，
        // UI 会停留在空白内容区（看起来没有 loading）。
        // 等首个正文/思考增量到达后再切换。
        var lastUsagePromptTokens = 0
        var lastUsageCompletionTokens = 0
        var lastUsageTotalTokens = 0

        val textBuffer = StringBuilder()
        val reasoningBuffer = StringBuilder()

        try {
            messageRemoteMediator.fetchAndSaveMessages(
                conversation = conversation,
                questionMessageEntityList = listOf(questionMessage),
                enableWebSearch = false,
            ).collect { event ->
                when (event) {
                    is LlmStreamEvent.Error -> {
                        _status.value = AIStreamAnswerStatus.ERROR
                        _errorMessage.value = event.errorMessage.ifBlank { "Unknown error" }
                    }

                    is LlmStreamEvent.TextDelta -> {
                        _status.value = AIStreamAnswerStatus.STREAMING
                        textBuffer.append(event.text)
                        _accumulatedText.value = textBuffer.toString()
                    }

                    is LlmStreamEvent.ReasoningDelta -> {
                        _status.value = AIStreamAnswerStatus.STREAMING
                        reasoningBuffer.append(event.text)
                        _reasoningText.value = reasoningBuffer.toString()
                    }

                    is LlmStreamEvent.UsageUpdated -> {
                        lastUsagePromptTokens = event.usage.promptTokens
                        lastUsageCompletionTokens = event.usage.completionTokens
                        lastUsageTotalTokens = event.usage.totalTokens
                    }

                    is LlmStreamEvent.ResponseStarted,
                    is LlmStreamEvent.SearchResultsEvent,
                    is LlmStreamEvent.ToolCallDeltaEvent,
                    is LlmStreamEvent.Completed -> Unit
                }
            }

            // Flow 正常结束，若未出错则视为完成
            if (_status.value == AIStreamAnswerStatus.STREAMING ||
                _status.value == AIStreamAnswerStatus.LOADING
            ) {
                val answer = textBuffer.toString()
                if (answer.isNotEmpty()) {
                    persistMessages(
                        conversation = conversation,
                        questionMessage = questionMessage,
                        answer = answer,
                        reasoningContent = reasoningBuffer.toString(),
                        promptTokens = lastUsagePromptTokens,
                        completionTokens = lastUsageCompletionTokens,
                        totalTokens = lastUsageTotalTokens,
                    )
                    _status.value = AIStreamAnswerStatus.SUCCESS
                } else {
                    _status.value = AIStreamAnswerStatus.ERROR
                    _errorMessage.value = "Empty response"
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            _status.value = AIStreamAnswerStatus.ERROR
            _errorMessage.value = e.message ?: "Network error"
        }
    }

    // ── 同步路径 ──────────────────────────────────────────────────────

    private suspend fun runSyncAnswer(engine: AiEngine) {
        try {
            val result = aiPromptExecutor.execute(
                input = screen.question,
                systemPrompt = screen.systemPrompt,
            )
            if (result.isSuccess) {
                _accumulatedText.value = result.content
                val conversation = buildConversation(engine)
                val questionMessage = buildQuestionMessage(engine, conversation)
                persistMessages(
                    conversation = conversation,
                    questionMessage = questionMessage,
                    answer = result.content,
                    promptTokens = 0,
                    completionTokens = 0,
                    totalTokens = 0,
                )
                _status.value = AIStreamAnswerStatus.SUCCESS
            } else {
                _status.value = AIStreamAnswerStatus.ERROR
                _errorMessage.value = result.errorMessage ?: "Request failed"
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            _status.value = AIStreamAnswerStatus.ERROR
            _errorMessage.value = e.message ?: "Network error"
        }
    }

    // ── 数据构建 ──────────────────────────────────────────────────────

    private fun buildConversation(engine: AiEngine): Conversation = Conversation(
        id = conversationId,
        entryType = AIConversationEntryType.STREAM_QA,
        engine = engine,
        title = "",
        appTitle = displayTitle,
        prompt = screen.systemPrompt,
    )

    private fun buildQuestionMessage(engine: AiEngine, conversation: Conversation): MessageEntity =
        MessageEntity(
            completionId = "${Date().time}${System.nanoTime()}",
            conversationId = conversationId,
            role = RoleType.USER.value,
            question = screen.question,
            answer = "",
            reasoningContent = "",
            engine = engine.name,
            model = engine.model.name,
            entryType = AIConversationEntryType.STREAM_QA,
            title = conversation.title,
            createdAt = Date(),
        )

    private suspend fun persistMessages(
        conversation: Conversation,
        questionMessage: MessageEntity,
        answer: String,
        reasoningContent: String = "",
        promptTokens: Int,
        completionTokens: Int,
        totalTokens: Int,
    ) {
        val snapshot = conversation.withHistorySnapshot(
            defaultTitle = applicationTitle(),
            userMessage = questionMessage.question,
            assistantMessage = answer,
            messageIncrement = 2,
            timestamp = System.currentTimeMillis()
        )
        val answerMessage = MessageEntity(
            completionId = questionMessage.completionId,
            conversationId = conversationId,
            role = RoleType.ASSISTANT.value,
            question = "",
            answer = answer,
            reasoningContent = reasoningContent,
            engine = questionMessage.engine,
            model = questionMessage.model,
            entryType = AIConversationEntryType.STREAM_QA,
            title = snapshot.title,
            createdAt = Date(questionMessage.createdAt.time + 1),
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            totalTokens = totalTokens,
        )
        // insertQuestionAndAnswer 会同时写入 ActivityLog
        messageListUseCase.insertQuestionAndAnswer(
            questionMessage.copy(title = snapshot.title),
            answerMessage,
            snapshot
        )
        appDatabase.conversationDao().insertReplace(ConversationEntity.fromConversation(snapshot))
        requestConversationTitleSummary(
            conversation = snapshot,
            userMessage = questionMessage.question,
            assistantMessage = answer
        )
    }

    private fun applicationTitle(): String = screen.label.ifEmpty {
        screen.question.take(24).ifBlank { "AI" }
    }

    private fun requestConversationTitleSummary(
        conversation: Conversation,
        userMessage: String,
        assistantMessage: String,
    ) {
        componentScope.launch(ioDispatcher) {
            val title = conversationTitleSummaryService.generateTitle(
                conversation = conversation,
                userMessage = userMessage,
                assistantMessage = assistantMessage
            ) ?: return@launch

            val updatedConversation = conversation.withSummaryTitle(title)
            appDatabase.conversationDao().updateTitle(
                conversationId = updatedConversation.id,
                title = updatedConversation.title,
                titleSource = updatedConversation.titleSource.name
            )
            appDatabase.messageDao().updateTitlesByConversationId(
                conversationId = updatedConversation.id,
                title = updatedConversation.title
            )
        }
    }

    private fun buildEngineInfo(engine: AiEngine): String =
        "${engine.title.ifBlank { engine.name }} · ${engine.model.name}"

    // ── Factory ───────────────────────────────────────────────────────

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            screen: Screen.AIStreamAnswer,
        ): AIStreamAnswerComponent
    }
}

/** 页面状态 */
enum class AIStreamAnswerStatus {
    /** 等待首个 chunk / 同步请求进行中 */
    LOADING,

    /** 已在接收流式 chunk */
    STREAMING,

    /** 已完成（流结束 / 同步返回） */
    SUCCESS,

    /** 请求失败 */
    ERROR,
}
