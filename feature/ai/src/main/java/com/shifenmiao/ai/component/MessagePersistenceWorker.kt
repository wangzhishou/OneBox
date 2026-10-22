package com.shifenmiao.ai.component

import com.shifenmiao.ai.error.AiChatError
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
import com.wanbaohe.core.ui.review.ReviewPromptHost
import com.wanbaohe.core.ui.review.ReviewPromptTrigger
import com.t8rin.logger.makeLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        const val TOKEN_WARNING_THRESHOLD = 20_000

        /**
         * 流式草稿写库的最小间隔：写太频会跟渲染抢 IO，写太稀又失去"进程被杀少丢字"的意义。
         */
        private const val STREAM_DRAFT_INTERVAL_MS = 2_000L

        /**
         * 草稿行的 finish_reason 哨兵值。
         *
         * 正常结束时完成路径会用 provider 的结束原因覆盖它；进程被杀则保留，
         * 下次打开会话时据此识别"这条回答没写完"，补中断标记，避免留下没有下文的空气泡。
         */
        const val STREAM_DRAFT_FINISH_REASON = "streaming"

        /** 中断回合的 finish_reason，与 [STREAM_DRAFT_FINISH_REASON] 区分开，避免重复补标记。 */
        const val INTERRUPTED_FINISH_REASON = "interrupted"

        /**
         * 同一会话只提示一次。超过阈值后每轮结束都弹，会盖住输入区。
         */
        private val warnedConversationIds = mutableSetOf<String>()
    }

    // ── 流式草稿（进程被杀兜底） ────────────────────────────────
    //
    // 直播期间每 [STREAM_DRAFT_INTERVAL_MS] 把已生成内容写回草稿行。
    // 正常结束时 onChatCompletionEnd / renderErrorUIForChat / persistInterruptedChat
    // 复用同一组 id 走 REPLACE，行被原地更新，不会产生重复消息。
    // 进程被杀时草稿行留在库里 = 用户重新打开会话还能看到已生成的部分。
    //
    // 每个回合持有自己的 [StreamDraft] 句柄：草稿行一旦写入，就必须由该回合自己的
    // 退出路径（完成/错误/取消/被新消息顶掉）收尾。不能依赖 worker 上的共享字段，
    // 否则新一轮开始后旧回合的迟到写入或清理会落到新回合的行上。

    /**
     * 一次流式回合的草稿句柄。
     *
     * [active] 在回合进入终态（完成/错误/中断/被顶掉）时置 false，之后迟到的
     * 流式写入直接丢弃，避免覆盖已经 REPLACE 的最终消息。
     */
    class StreamDraft internal constructor(
        val questionId: Int,
        val answerId: Int,
        val completionId: String,
    ) {
        @Volatile
        internal var active: Boolean = true

        // 事件线程与 ioDispatcher 会并发读写这两个节流字段；最坏只是多/少写一次库，
        // 但保持 volatile 让实测行为稳定，避免不同设备上节流窗口漂移。
        @Volatile
        internal var lastWriteAt: Long = 0L

        @Volatile
        internal var lastLength: Int = -1
    }

    /**
     * 新一轮开始前插入草稿行，并把 id 写回 sharedState，
     * 让后续完成路径的 copy() 自动带上 id（REPLACE 即原地更新）。
     *
     * 全程 [NonCancellable]：调用方可能在插入途中被取消，草稿行一旦写入
     * 就必须把句柄返回给清理路径，否则会留下没有归属的孤儿行。
     *
     * @param expectedCompletionId 本回合发送时生成的 completionId。若 live state 已被
     *                             更新的回合接管（快速连发），直接放弃，避免把新回合的
     *                             问题插成旧回合的草稿。
     * @return 本回合的草稿句柄；问题为空、回合已被接管或写库失败时返回 null。
     */
    suspend fun beginStreamDraft(expectedCompletionId: String): StreamDraft? = withContext(NonCancellable) {
        val question = sharedState.questionMessageEntity.value
        if (expectedCompletionId.isBlank() || question.completionId != expectedCompletionId) {
            "beginStreamDraft skipped: live completionId=${question.completionId} " +
                "expected=$expectedCompletionId".makeLog("MessagePersistenceWorker")
            return@withContext null
        }
        if (question.question.isBlank()) return@withContext null

        val answer = sharedState.answerMessageEntity.value
        val now = Date()
        runCatching {
            // 幂等：同一 completionId 已经有行（重复执行/上次进程内已开局）就复用，
            // 不再插一对新的，否则完成时 REPLACE 只会更新最新那对，旧的会变成孤儿消息。
            val existing = withContext(Dispatchers.IO) {
                appDatabase.messageDao()
                    .queryQuestionAndAnswerByCompletionId(question.completionId)
            }
            val existingQuestion = existing.firstOrNull { it.role == RoleType.USER.value }
            val existingAnswer = existing.firstOrNull { it.role == RoleType.ASSISTANT.value }

            val questionId: Int
            val answerId: Int
            if (existingQuestion != null && existingAnswer != null) {
                questionId = existingQuestion.id
                answerId = existingAnswer.id
            } else {
                val (insertedQuestionId, insertedAnswerId) = appDatabase.messageDao()
                    .insertQuestionAndAnswer(
                        question.copy(
                            id = 0,
                            // 本地 base64 附件不入库，与完成路径 stripLocalContentFromAttachments 保持一致
                            attachmentsJson = AttachmentPayloadUtils.stripLocalContent(question.attachmentsJson),
                            createdAt = now,
                            expired = false,
                        ),
                        answer.copy(
                            id = 0,
                            answer = "",
                            reasoningContent = "",
                            finishReason = STREAM_DRAFT_FINISH_REASON,
                            createdAt = Date(now.time + 1),
                            expired = false,
                        ),
                    )
                questionId = insertedQuestionId.toInt()
                answerId = insertedAnswerId.toInt()
            }
            sharedState.setQuestionMessage(
                sharedState.questionMessageEntity.value.copy(id = questionId)
            )
            sharedState.setAnswerMessage(
                sharedState.answerMessageEntity.value.copy(id = answerId)
            )
            StreamDraft(
                questionId = questionId,
                answerId = answerId,
                completionId = question.completionId,
            )
        }.getOrElse { throwable ->
            throwable.makeLog("MessagePersistenceWorker")
            "beginStreamDraft failed, fallback to final-only persist".makeLog("MessagePersistenceWorker")
            null
        }
    }

    /**
     * 节流判断：距离上次写库够久、且内容确实变长了才值得写。
     *
     * 返回 true 时立刻占住时间片（[StreamDraft.lastWriteAt]），避免同一窗口内的
     * 连续 chunk 各自通过判断、排队写库把 IO 打满。
     */
    fun shouldPersistStreamDraft(draft: StreamDraft?): Boolean {
        if (draft == null || !draft.active || draft.answerId <= 0) return false
        val now = System.currentTimeMillis()
        if (now - draft.lastWriteAt < STREAM_DRAFT_INTERVAL_MS) return false
        val answer = sharedState.answerMessageEntity.value
        if (answer.answer.length + answer.reasoningContent.length == draft.lastLength) return false
        draft.lastWriteAt = now
        return true
    }

    /** 把当前已生成内容写回草稿行；失败只记日志，绝不影响正在进行的流。 */
    suspend fun persistStreamDraft(draft: StreamDraft?) {
        if (draft == null || !draft.active || draft.answerId <= 0) return
        val answer = sharedState.answerMessageEntity.value
        draft.lastLength = answer.answer.length + answer.reasoningContent.length
        runCatching {
            appDatabase.messageDao().updateStreamDraft(
                id = draft.answerId,
                answer = answer.answer,
                reasoning = answer.reasoningContent,
                // 只更新仍是草稿的行：完成/中断路径把哨兵改写之后，迟到的写入自动失效。
                draftFinishReason = STREAM_DRAFT_FINISH_REASON,
            )
        }.onFailure { throwable ->
            throwable.makeLog("MessagePersistenceWorker")
            "persistStreamDraft failed".makeLog("MessagePersistenceWorker")
        }
    }

    /**
     * 作废草稿句柄：回合已有终态结果，后续迟到的流式写入不能再覆盖已落库的消息。
     */
    fun finishStreamDraft(draft: StreamDraft?) {
        if (draft == null) return
        draft.active = false
    }

    /**
     * 丢弃未完成回合的草稿行。
     *
     * 用于回合被新消息顶掉（stale generation）的路径：这一轮不会再走完成/错误/取消
     * 中的任何一个收尾路径，草稿行若保留，稍后就会以一条与后续回答重复的半截消息出现。
     * 删除条件带 finish_reason 哨兵，避免与完成路径的原地 REPLACE 竞争时误删最终消息。
     */
    suspend fun discardStreamDraft(draft: StreamDraft?) {
        if (draft == null) return
        finishStreamDraft(draft)
        withContext(NonCancellable) {
            runCatching {
                appDatabase.messageDao().deleteDraftPair(
                    questionId = draft.questionId,
                    answerId = draft.answerId,
                    finishReason = STREAM_DRAFT_FINISH_REASON,
                )
            }.onFailure { throwable ->
                throwable.makeLog("MessagePersistenceWorker")
                "discardStreamDraft failed for completionId=${draft.completionId} " +
                    "ids=${draft.questionId}/${draft.answerId}".makeLog("MessagePersistenceWorker")
            }
        }
        streamContentProcessor.invalidateMessageCache()
    }

    /**
     * 打开会话时修复上次进程被杀留下的草稿行。
     *
     * 仍带 [STREAM_DRAFT_FINISH_REASON] 的 assistant 行 = 那一轮没走完：
     * - 有已生成内容 → 末尾补一条「已中断」标记；
     * - 一个字都没生成 → 整条替换成标记文案，避免留下没有下文的空气泡。
     * 然后把 finish_reason 改成 [INTERRUPTED_FINISH_REASON]，重复打开不会重复补。
     */
    suspend fun recoverInterruptedStreamDrafts(conversationId: String) {
        if (conversationId.isBlank()) return
        // 正在生成的那一轮不要动，草稿行此刻就是 live 消息的落库副本
        if (sharedState.chatUIState.value.chatActive) return

        val drafts = runCatching {
            appDatabase.messageDao()
                .getMessagesByFinishReason(conversationId, STREAM_DRAFT_FINISH_REASON)
        }.getOrElse { throwable ->
            throwable.makeLog("MessagePersistenceWorker")
            return
        }
        if (drafts.isEmpty()) return

        val suffix = sharedState.applicationContext.getString(R.string.ai_chat_interrupted)
        drafts.forEach { draft ->
            val answer = when {
                draft.answer.isBlank() -> suffix
                draft.answer.endsWith(suffix) -> draft.answer
                else -> draft.answer + "\n\n" + suffix
            }
            runCatching {
                appDatabase.messageDao().updateMessageAnswerAndFinishReason(
                    id = draft.id,
                    answer = answer,
                    finishReason = INTERRUPTED_FINISH_REASON,
                )
            }.onFailure { throwable ->
                throwable.makeLog("MessagePersistenceWorker")
                "recoverInterruptedStreamDrafts failed for id=${draft.id}".makeLog("MessagePersistenceWorker")
            }
        }
        streamContentProcessor.invalidateMessageCache()
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
        fallbackQuestion: String = "",
        draft: StreamDraft? = null,
    ) {
        // 终态已到：封住本回合还在排队的草稿写入，随后用同一组 id REPLACE 成最终消息。
        finishStreamDraft(draft)
        val currentTime = Date()
        val insertCompletionId = sharedState.questionMessageEntity.value.completionId
        // 实体在流式期间被竞态重置为空时, 用发送时固化的原文兜底, 避免写空 question 行
        val questionText = resolvePersistQuestion(
            current = sharedState.questionMessageEntity.value.question,
            fallback = fallbackQuestion,
            completionId = insertCompletionId
        )

        // 如果流返回了 id，优先使用
        // (completionId 由 processCompletionData 更新，此处已可见)

        val persistedAnswerText = sharedState.answerMessageEntity.value.answer
        val updatedConversation = sharedState.conversation.value.withHistorySnapshot(
            defaultTitle = sharedState.applicationContext.getString(R.string.ai_chat_title),
            userMessage = questionText,
            assistantMessage = persistedAnswerText,
            messageIncrement = 2,
            timestamp = currentTime.time
        )
        sharedState.setConversation(updatedConversation)

        val reasoningTime = (System.currentTimeMillis() - startQuestionTime) / 1000

        sharedState.setQuestionMessage(sharedState.questionMessageEntity.value.copy(
            completionId = insertCompletionId,
            question = questionText,
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

        // 一轮问答完整落库 = 用户刚拿到价值,报给应用内评分弹层做门控判断
        // (次数门槛/冷却/每安装上限都在 InAppReviewPrompt 里,失败静默,不影响落库主流程)
        ReviewPromptHost.notifySuccess(ReviewPromptTrigger.AI_ANSWER_SUCCESS)
    }

    /**
     * 渲染错误 UI 并持久化错误消息。
     *
     * 落库的 `answer` 是面向用户的文案（后台 message 或本地化提示），
     * 语义分类 / HTTP 状态码写进 `finish_reason`（`error:<status>:<code>`），
     * 供重启后重新渲染错误卡片；原始 URL / 响应体只进 logcat。
     *
     * @param skipPoints  true 时跳过积分扣减(用于内容风控拒绝且本轮零产出等
     *                    未产生任何有效内容的场景); 默认 false 保持既有行为。
     */
    suspend fun renderErrorUIForChat(
        error: AiChatError,
        questionMessageEntityList: List<MessageEntity>? = null,
        reasoningTime: Long = 0L,
        skipPoints: Boolean = false,
        draft: StreamDraft? = null,
    ) {
        // 终态已到：封住本回合还在排队的草稿写入，错误消息随后复用同一组 id 落库。
        finishStreamDraft(draft)
        sharedState.updateChatUiState {
            it.copy(
            pageState = PageState.ERROR
            )
        }
        sharedState.setAnswerMessage(sharedState.answerMessageEntity.value.copy(
            answer = error.message,
            reasoningContent = "",
            finishReason = error.meta,
        ).also { it.uId = MessageUIState.ERROR.value })
        streamContentProcessor.updatePlaceHolderMessage()

        questionMessageEntityList?.let { list ->
            // 仅在无累积服务器 usage 时才使用本地估算，避免覆写已累积的真实数据
            calculateUsageIfNull(null, list)
            val currentTime = Date()
            val insertCompletionId = sharedState.questionMessageEntity.value.completionId
            sharedState.setQuestionMessage(sharedState.questionMessageEntity.value.copy(
                completionId = insertCompletionId,
                question = resolvePersistQuestion(
                    current = sharedState.questionMessageEntity.value.question,
                    fallback = list.lastOrNull { it.role == RoleType.USER.value }?.question.orEmpty(),
                    completionId = insertCompletionId
                ),
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
            if (!skipPoints) {
                consumePoints(sharedState.answerMessageEntity.value)
            }
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

    /**
     * Token 预警。同一会话越过阈值后只提示一次，避免每轮结束都挡住聊天。
     */
    fun checkAndWarnTokenUsage() {
        val totalTokens = sharedState.answerMessageEntity.value.totalTokens
        if (totalTokens < TOKEN_WARNING_THRESHOLD) return
        val conversationId = sharedState.conversation.value.id
        synchronized(warnedConversationIds) {
            if (!warnedConversationIds.add(conversationId)) return
        }
        sharedState.componentScope.launch(Dispatchers.Main) {
            ActionUtils.showToast(
                sharedState.applicationContext.getString(R.string.ai_chat_token_warning, totalTokens)
            )
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
    val stripped = AttachmentPayloadUtils.stripLocalContent(json)
    return if (stripped == json) this else this.copy(attachmentsJson = stripped)
}

/**
 * 落库 question 文本兜底: 实体在流式期间被竞态重置为空时,
 * 用发送时固化的原文恢复; 仍为空则记日志, 便于定位实体被清空的源头.
 */
internal fun resolvePersistQuestion(current: String, fallback: String, completionId: String): String {
    if (current.isNotBlank()) return current
    if (fallback.isNotBlank()) {
        "question blank at persist, recovered from send-time list (completionId=$completionId)"
            .makeLog("MessagePersistenceWorker")
        return fallback
    }
    "persisting BLANK question (completionId=$completionId)".makeLog("MessagePersistenceWorker")
    return current
}
