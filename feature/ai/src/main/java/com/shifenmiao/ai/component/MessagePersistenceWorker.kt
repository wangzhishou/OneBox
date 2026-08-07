package com.shifenmiao.ai.component

import com.shifenmiao.ai.history.withHistorySnapshot
import com.shifenmiao.ai.service.ConversationTitleSummaryService
import com.shifenmiao.ai.utils.AiUtils
import com.shifenmiao.ai.utils.AttachmentPayloadUtils
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.core.R
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.ai.entity.ConversationEntity
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.model.ai.MessageUIState
import com.shifenmiao.model.ai.RoleType
import com.shifenmiao.model.ai.SearchCitation
import com.shifenmiao.model.ai.SearchResult
import com.shifenmiao.model.ai.Usage
import com.shifenmiao.model.state.PageState
import com.shifenmiao.storage.AIChatStorage
import com.shifenmiao.ai.usecase.MessageListUseCase
import com.t8rin.logger.makeLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

/**
 * 消息持久化工作者 —— 负责 DB 写入、积分消耗和标题摘要。
 *
 * 从 [AIChatComponent] 中抽离，职责边界：
 * 1. 消息对（Q&A）写入 DB + 会话元数据更新
 * 2. Usage（token 计算）+ 积分消耗
 * 3. Token 预警
 * 4. 会话标题自动摘要
 * 5. 错误消息持久化
 * 6. 搜索结果处理
 *
 * 线程安全：所有 suspend 方法由调用方在 IO 协程中执行。
 */
class MessagePersistenceWorker(
    private val messageListUseCase: MessageListUseCase,
    private val conversationTitleSummaryService: ConversationTitleSummaryService,
    private val appDatabase: AppDatabase,
    private val sharedState: ChatSharedState,
    private val streamContentProcessor: StreamContentProcessor,
    private val attachmentUploadCoordinator: AttachmentUploadCoordinator,
) {
    companion object {
        /** Token 预警阈值 */
        const val TOKEN_WARNING_THRESHOLD = 10_000
    }

    /**
     * 流式聊天正常结束时调用：
     * 1. 构建最终的 Q&A 消息实体
     * 2. 写入 DB
     * 3. 消费积分
     * 4. 触发标题摘要
     */
    suspend fun onChatCompletionEnd(
        toolCallsChainJson: String,
        startQuestionTime: Long,
    ) {
        val currentTime = Date()
        val insertCompletionId = sharedState.questionMessageEntity.value.completionId

        // 如果流返回了 id，优先使用
        // (completionId 由 processCompletionData 更新，此处已可见)

        val persistedAnswerText = sharedState.answerMessageEntity.value.answer
        val updatedConversation = sharedState.conversation.value.withHistorySnapshot(
            defaultTitle = sharedState.applicationContext.getString(R.string.ai_chat_title),
            userMessage = sharedState.questionMessageEntity.value.question,
            assistantMessage = persistedAnswerText,
            messageIncrement = 2,
            timestamp = currentTime.time
        )
        sharedState.setConversation(updatedConversation)

        val reasoningTime = (System.currentTimeMillis() - startQuestionTime) / 1000

        sharedState.setQuestionMessage(sharedState.questionMessageEntity.value.copy(
            completionId = insertCompletionId,
            createdAt = currentTime,
            entryType = sharedState.conversation.value.entryType,
            entryRefId = sharedState.conversation.value.entryRefId,
            title = updatedConversation.title,
            reasoningTime = reasoningTime,
            requestProtocol = sharedState.conversation.value.engine.requestProtocol.name,
            providerResponseId = sharedState.questionMessageEntity.value.providerResponseId.ifBlank { insertCompletionId },
        ))
        sharedState.setAnswerMessage(sharedState.answerMessageEntity.value.copy(
            completionId = insertCompletionId,
            createdAt = Date(currentTime.time + 1),
            entryType = sharedState.conversation.value.entryType,
            entryRefId = sharedState.conversation.value.entryRefId,
            title = updatedConversation.title,
            reasoningTime = reasoningTime,
            requestProtocol = sharedState.conversation.value.engine.requestProtocol.name,
            providerResponseId = sharedState.answerMessageEntity.value.providerResponseId.ifBlank { insertCompletionId },
            toolCalls = toolCallsChainJson.ifBlank { sharedState.answerMessageEntity.value.toolCalls }
        ))

        // 切换到 NORMAL 状态
        sharedState.setQuestionMessage(
            sharedState.questionMessageEntity.value.copy().also { it.uId = MessageUIState.NORMAL.value }
        )
        sharedState.setAnswerMessage(
            sharedState.answerMessageEntity.value.copy().also { it.uId = MessageUIState.NORMAL.value }
        )
        // toolCalls 在 Agent Loop 完成后才写入，流式阶段的缓存不含 RobotToolCallHistory，
        // 必须失效缓存才能触发 UI 模型重建，否则历史卡片不会显示。
        streamContentProcessor.invalidateMessageCache()
        streamContentProcessor.updatePlaceHolderMessage(forceUpdate = true)

        val persistedAnswerEntity = sharedState.answerMessageEntity.value

        // DB 持久化前剥离 localContent，避免 DB 膨胀（base64 字符串约 100-700KB/张图片）
        // localContent 保留在 live state 用于 API 请求，DB 仅存储 localPath 引用
        val persistedQuestionEntity = sharedState.questionMessageEntity.value.stripLocalContentFromAttachments()

        messageListUseCase.insertQuestionAndAnswer(
            persistedQuestionEntity,
            persistedAnswerEntity,
            updatedConversation
        )

        syncPersistedMessageIds(insertCompletionId)
        streamContentProcessor.updatePlaceHolderMessage(forceUpdate = true)

        // 图片持久化
        attachmentUploadCoordinator.saveImagesToEntity()

        // 保存设置
        saveSetting()

        appDatabase.conversationDao()
            .insertReplace(ConversationEntity.fromConversation(updatedConversation))

        consumePoints(sharedState.answerMessageEntity.value)
        checkAndWarnTokenUsage()

        requestConversationTitleSummary(
            conversation = updatedConversation,
            userMessage = sharedState.questionMessageEntity.value.question,
            assistantMessage = persistedAnswerEntity.answer
        )
    }

    /**
     * 渲染错误 UI 并持久化错误消息。
     */
    suspend fun renderErrorUIForChat(
        errorMessage: String,
        questionMessageEntityList: List<MessageEntity>? = null,
        reasoningTime: Long = 0L,
    ) {
        sharedState.updateChatUiState {
            it.copy(
            pageState = PageState.ERROR
            )
        }
        sharedState.setAnswerMessage(sharedState.answerMessageEntity.value.copy(
            answer = errorMessage,
            reasoningContent = ""
        ).also { it.uId = MessageUIState.ERROR.value })
        streamContentProcessor.updatePlaceHolderMessage()

        questionMessageEntityList?.let { list ->
            // 仅在无累积服务器 usage 时才使用本地估算，避免覆写已累积的真实数据
            calculateUsageIfNull(null, list)
            val currentTime = Date()
            val insertCompletionId = sharedState.questionMessageEntity.value.completionId
            sharedState.setQuestionMessage(sharedState.questionMessageEntity.value.copy(
                completionId = insertCompletionId,
                createdAt = currentTime,
                entryType = sharedState.conversation.value.entryType,
                entryRefId = sharedState.conversation.value.entryRefId,
                title = sharedState.conversation.value.title,
                reasoningTime = reasoningTime,
                requestProtocol = sharedState.conversation.value.engine.requestProtocol.name,
                expired = true,
            ))
            sharedState.setAnswerMessage(sharedState.answerMessageEntity.value.copy(
                completionId = insertCompletionId,
                createdAt = Date(currentTime.time + 1),
                entryType = sharedState.conversation.value.entryType,
                entryRefId = sharedState.conversation.value.entryRefId,
                title = sharedState.conversation.value.title,
                reasoningTime = reasoningTime,
                requestProtocol = sharedState.conversation.value.engine.requestProtocol.name,
                expired = true,
            ))
            messageListUseCase.insertQuestionAndAnswer(
                sharedState.questionMessageEntity.value.stripLocalContentFromAttachments(),
                sharedState.answerMessageEntity.value,
                sharedState.conversation.value
            )
            syncPersistedMessageIds(insertCompletionId)
            streamContentProcessor.updatePlaceHolderMessage(forceUpdate = true)
            persistConversationSnapshot(
                userMessage = sharedState.questionMessageEntity.value.question,
                assistantMessage = sharedState.answerMessageEntity.value.answer,
                messageIncrement = 2,
                timestamp = currentTime.time
            )
            consumePoints(sharedState.answerMessageEntity.value)
        }
    }

    /**
     * 根据当前 Q&A 快照更新 conversations 表。
     * 用于正常结束、中断、报错等所有需要把会话暴露到历史中心的场景。
     */
    suspend fun persistConversationSnapshot(
        userMessage: String,
        assistantMessage: String,
        messageIncrement: Int = 0,
        timestamp: Long = System.currentTimeMillis(),
    ) {
        val updatedConversation = sharedState.conversation.value.withHistorySnapshot(
            defaultTitle = sharedState.applicationContext.getString(R.string.ai_chat_title),
            userMessage = userMessage,
            assistantMessage = assistantMessage,
            messageIncrement = messageIncrement,
            timestamp = timestamp
        )
        sharedState.setConversation(updatedConversation)
        appDatabase.conversationDao()
            .insertReplace(ConversationEntity.fromConversation(updatedConversation))
    }

    // ── Usage 与积分 ──────────────────────────────────────────

    /** 更新消息 usage 信息（覆写） */
    fun updateMessageUsage(usage: Usage) {
        sharedState.setQuestionMessage(sharedState.questionMessageEntity.value.copy(
            promptTokens = usage.promptTokens,
            completionTokens = usage.completionTokens,
            totalTokens = usage.totalTokens
        ))
        sharedState.setAnswerMessage(sharedState.answerMessageEntity.value.copy(
            promptTokens = usage.promptTokens,
            completionTokens = usage.completionTokens,
            totalTokens = usage.totalTokens
        ))
    }

    /**
     * 累积消息 usage 信息（累加而非覆写）。
     *
     * Agent Loop 中每次 LLM 请求（初始 + 各轮 follow-up）都会独立上报 usage，
     * 必须累加才能得到整个请求链的真实 token 消耗。
     */
    fun accumulateMessageUsage(usage: Usage) {
        val currentQ = sharedState.questionMessageEntity.value
        sharedState.setQuestionMessage(currentQ.copy(
            promptTokens = currentQ.promptTokens + usage.promptTokens,
            completionTokens = currentQ.completionTokens + usage.completionTokens,
            totalTokens = currentQ.totalTokens + usage.totalTokens
        ))
        val currentA = sharedState.answerMessageEntity.value
        sharedState.setAnswerMessage(currentA.copy(
            promptTokens = currentA.promptTokens + usage.promptTokens,
            completionTokens = currentA.completionTokens + usage.completionTokens,
            totalTokens = currentA.totalTokens + usage.totalTokens
        ))
    }

    /**
     * 估算 usage（当 API 未返回时降级）。
     *
     * 包含工具调用链的 token 开销（toolCalls JSON 字段）。
     */
    fun calculateUsage(questionMessageEntityList: List<MessageEntity>): Usage {
        val promptText = AiUtils.concatenateQuestionsAndAnswers(questionMessageEntityList)
        // 工具调用链开销：toolCalls JSON 中包含工具名、参数、结果
        val toolCallsOverhead = questionMessageEntityList.sumOf { entity ->
            if (entity.toolCalls.isNotBlank()) StringUtils.calculateTokens(entity.toolCalls) else 0
        }
        val promptTokens = StringUtils.calculateTokens(promptText) + toolCallsOverhead
        val completionTokens = StringUtils.calculateTokens(sharedState.answerMessageEntity.value.answer)
        return Usage(
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            totalTokens = promptTokens + completionTokens
        )
    }

    /**
     * 兜底 usage：仅在 API 未返回且当前无累积值时执行本地估算。
     *
     * 优先保留已有的累积服务器 usage（来自 accumulateMessageUsage），
     * 只有当 totalTokens 仍为 0 时才降级到本地估算。
     */
    fun calculateUsageIfNull(
        usage: Usage?,
        questionMessageEntityList: List<MessageEntity>
    ) {
        if (usage != null && usage.totalTokens > 0) {
            updateMessageUsage(usage)
            return
        }
        if (sharedState.answerMessageEntity.value.totalTokens == 0) {
            val fallbackUsage = calculateUsage(questionMessageEntityList)
            updateMessageUsage(fallbackUsage)
        }
    }

    /** 消费积分 */
    fun consumePoints(messageEntity: MessageEntity) {
        sharedState.componentScope.launch {
            if (messageEntity.totalTokens > 0 && AiUtils.isNotFree(sharedState.conversation.value)) {
                BaseUtils.consumePointsByToken(
                    messageEntity.totalTokens,
                    sharedState.conversation.value,
                    desc = messageEntity.completionId
                )
            }
        }
    }

    /** Token 预警 */
    fun checkAndWarnTokenUsage() {
        val totalTokens = sharedState.answerMessageEntity.value.totalTokens
        if (totalTokens >= TOKEN_WARNING_THRESHOLD) {
            sharedState.componentScope.launch(Dispatchers.Main) {
                ActionUtils.showToast(
                    sharedState.applicationContext.getString(R.string.ai_chat_token_warning, totalTokens)
                )
            }
        }
    }

    // ── 搜索结果处理 ──────────────────────────────────────────

    /**
     * 处理搜索结果，支持多种 API 格式。
     * 仅处理一次（通常在流的开始或结束返回）。
     */
    fun processSearchResults(
        searchResults: List<SearchCitation>?,
        searchInfo: com.shifenmiao.model.ai.SearchInfo?
    ) {
        val citations = mutableListOf<SearchCitation>()

        SearchResult.fromJson(sharedState.answerMessageEntity.value.searchResults)?.citations
            ?.let { citations.addAll(it) }

        searchResults?.let { citations.addAll(it) }
        searchInfo?.searchResults?.let { baiduResults ->
            citations.addAll(baiduResults.map { it.toSearchCitation() })
        }

        if (citations.isNotEmpty()) {
            val deduplicatedCitations = citations
                .distinctBy { citation ->
                    listOf(
                        citation.url.trim(),
                        citation.title.trim(),
                        citation.snippet.trim()
                    ).joinToString("|")
                }
            val searchResult = SearchResult(
                query = sharedState.questionMessageEntity.value.question,
                citations = deduplicatedCitations.mapIndexed { index, citation ->
                    if (citation.index == 0) citation.copy(index = index + 1) else citation
                }
            )
            sharedState.setAnswerMessage(sharedState.answerMessageEntity.value.copy(
                searchResults = searchResult.toJson()
            ))
        }
    }

    // ── 私有方法 ──────────────────────────────────────────────

    private fun saveSetting() {
        sharedState.componentScope.launch(Dispatchers.IO) {
            if (AiUtils.isAssistant(sharedState.conversation.value)) {
                AIChatStorage.saveConfigs(
                    sharedState.conversation.value.copy(
                        title = "",
                        titleSource = com.shifenmiao.model.ai.AIConversationTitleSource.SYSTEM
                    )
                )
            }
        }
    }

    private fun requestConversationTitleSummary(
        conversation: com.shifenmiao.model.ai.Conversation,
        userMessage: String,
        assistantMessage: String,
    ) {
        sharedState.componentScope.launch(sharedState.ioDispatcher) {
            val title = conversationTitleSummaryService.generateTitle(
                conversation = conversation,
                userMessage = userMessage,
                assistantMessage = assistantMessage
            ) ?: return@launch

            if (sharedState.conversation.value.id != conversation.id) return@launch

            // 使用 AIChatBaseComponent 的 applyConversationTitleSummary
            // 通过回调通知 AIChatComponent
            _titleSummaryResult?.invoke(title)
        }
    }

    /** 标题摘要回调，由 AIChatComponent 注入 */
    private var _titleSummaryResult: ((String) -> Unit)? = null

    fun setTitleSummaryCallback(callback: (String) -> Unit) {
        _titleSummaryResult = callback
    }

    /**
     * 将 Room 自增主键同步回当前 live state，避免 placeholder 长时间保留 id=0。
     *
     * 这会导致：
     * 1. 下一轮 updatePlaceHolderMessage() 误删上一轮消息（因为旧消息仍满足 id <= 0）
     * 2. saveImagesToEntity() 取不到 questionMessageEntity.id，图片无法落库
     */
    fun syncPersistedMessageIds(completionId: String) {
        if (completionId.isBlank()) return

        val persistedMessages = runCatching {
            appDatabase.messageDao().queryQuestionAndAnswerByCompletionId(completionId)
        }.getOrElse {
            "syncPersistedMessageIds failed: ${it.message}".makeLog("MessagePersistenceWorker")
            return
        }

        val persistedQuestion = persistedMessages.firstOrNull { it.role == RoleType.USER.value }
        val persistedAnswer = persistedMessages.firstOrNull { it.role == RoleType.ASSISTANT.value }

        persistedQuestion?.let { persisted ->
            val current = sharedState.questionMessageEntity.value
            if (current.id != persisted.id) {
                sharedState.setQuestionMessage(current.copy(
                    id = persisted.id,
                    createdAt = persisted.createdAt,
                    title = persisted.title
                ).also { it.uId = current.uId })
            }
        }

        persistedAnswer?.let { persisted ->
            val current = sharedState.answerMessageEntity.value
            if (current.id != persisted.id) {
                sharedState.setAnswerMessage(current.copy(
                    id = persisted.id,
                    createdAt = persisted.createdAt,
                    title = persisted.title
                ).also { it.uId = current.uId })
            }
        }
    }
}

/**
 * 将 MessageEntity.attachmentsJson 中的 localContent 剥离，仅保留 localPath 引用。
 *
 * 用途：DB 持久化前调用，避免 base64 字符串（约 100-700KB/张）导致 DB 膨胀。
 * live state 中保留 localContent 用于 API 请求构建，DB 仅存储 localPath。
 */
private fun MessageEntity.stripLocalContentFromAttachments(): MessageEntity {
    val json = this.attachmentsJson
    if (json.isBlank()) return this
    val stripped = AttachmentPayloadUtils.stripLocalContent(json, com.google.gson.Gson())
    return if (stripped == json) this else this.copy(attachmentsJson = stripped)
}
