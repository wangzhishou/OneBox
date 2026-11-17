/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.ai.component

import com.shifenmiao.ai.model.MessageUiModel
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.model.ai.MessageUIState
import com.shifenmiao.storage.AppSharedStorage
import com.t8rin.logger.makeLog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 流式内容处理器 —— 负责流式内容累积、渲染节流、看门狗和消息 UI 模型管理。
 *
 * 从 [AIChatComponent] 中抽离，职责边界：
 * 1. 流式文本缓冲（answer / reasoning）+ O(n) 追加
 * 2. Markdown 源码 → MessageUiModel 列表 + 节流渲染
 * 3. SSE 看门狗（空闲超时检测 + 兜底取消）
 * 4. Placeholder 消息更新
 * 5. 打字机效果
 *
 * 线程安全：
 * - [answerBuffer] / [reasoningBuffer] 仅在调用方指定的单协程中读写
 *   （executeStreamingChat 的 IO 协程），无需加锁。
 * - 看门狗协程与流式协程通过 [lastChunkAt] / [streamSawEnd] 的 @Volatile 保证可见性。
 * - [cachedMessageUiModels] 与 [doRenderMessage] 同协程，无需加锁。
 */
class StreamContentProcessor(
    private val sharedState: ChatSharedState,
    private val aiEngineCatalogManager: AIEngineCatalogManager,
    private val onStreamIdleTimeout: () -> Unit,
) {
    companion object {
        /** 流式渲染节流间隔 */
        const val RENDER_THROTTLE_MS = 48L

        /**
         * 自适应流式刷新阈值：内容越长，越不应该在每次 chunk 后都整段重建 Markdown。
         * 下面这些分段配置只影响 streaming 过程，不影响最终完成态的完整渲染。
         */
        private const val STREAM_RENDER_MEDIUM_LENGTH = 1_000
        private const val STREAM_RENDER_LARGE_LENGTH = 3_000
        private const val STREAM_RENDER_HUGE_LENGTH = 8_000
        private const val STREAM_RENDER_MEDIUM_INTERVAL_MS = 84L
        private const val STREAM_RENDER_LARGE_INTERVAL_MS = 120L
        private const val STREAM_RENDER_HUGE_INTERVAL_MS = 180L
        private const val STREAM_RENDER_SMALL_CHAR_THRESHOLD = 12
        private const val STREAM_RENDER_MEDIUM_CHAR_THRESHOLD = 48
        private const val STREAM_RENDER_LARGE_CHAR_THRESHOLD = 96
        private const val STREAM_RENDER_HUGE_CHAR_THRESHOLD = 160

        /** SSE 空闲超时阈值 */
        const val STREAM_IDLE_TIMEOUT_MS = 60_000L

        /** 看门狗轮询间隔 */
        const val WATCHDOG_INTERVAL_MS = 5_000L

        /** 活跃聊天消息上限 */
        const val ACTIVE_CHAT_MESSAGE_LIMIT = 50
    }

    // ── 流式内容缓冲 ──────────────────────────────────────────
    private val answerBuffer = StringBuilder()
    private val reasoningBuffer = StringBuilder()
    private var hasPendingAnswerUpdate = false
    private var hasPendingReasoningUpdate = false

    /**
     * reasoning 最后一段在 [reasoningBuffer] 中的起始索引。
     *
     * 设计：增量维护，不额外分配 buffer。
     * - 每次 [updateReasoningContentOptimized] 时，检查增量内容中是否包含段落分隔 \n\n
     * - 如有，将起始索引移至最后一个 \n\n 之后
     * - 读取时 O(1) substring，无需正则/split/数组分配
     */
    private var reasoningTailStartIndex: Int = 0

    // ── 渲染状态 ──────────────────────────────────────────────
    private var lastRenderTime = 0L
    private var lastPlaceholderUpdateTime = 0L
    /** 上一次真正触发整段 UI 重建的时刻，用于自适应 streaming 降频。 */
    private var lastStreamFrameTime = 0L

    /** 上一次真正渲染到 UI 的 answer/reasoning 长度，用于计算“本轮增长值是否值得重绘”。 */
    private var lastRenderedAnswerLength = 0
    private var lastRenderedReasoningLength = 0

    /** UI 模型列表状态流 */
    private val _messageUiModels = MutableStateFlow<List<MessageUiModel>>(emptyList())
    val messageUiModels: MutableStateFlow<List<MessageUiModel>> = _messageUiModels

    /** 缓存已构建的消息 UI 模型，key 为 completionId_role */
    private val cachedMessageUiModels = mutableMapOf<String, List<MessageUiModel>>()

    // ── 看门狗状态 ────────────────────────────────────────────
    @Volatile
    private var lastChunkAt: Long = 0L

    @Volatile
    private var streamChunkCount: Int = 0

    @Volatile
    var streamSawEnd: Boolean = false
        private set

    private var watchdogJob: Job? = null

    // ── 时间追踪 ──────────────────────────────────────────────
    var startQuestionTime: Long = 0L
    var reasoningTime: Long = 0L

    // ── 内容追加方法 ──────────────────────────────────────────

    /** 追加 LLM 生成的回答内容（干净内容，会被持久化） */
    fun updateAnswerContentOptimized(incrementContent: String) {
        answerBuffer.append(incrementContent)
        hasPendingAnswerUpdate = true
    }

    /** 追加推理内容，同时增量维护 [reasoningTailStartIndex] */
    fun updateReasoningContentOptimized(incrementContent: String) {
        val cleaned = normalizeReasoningIncrement(incrementContent)
        if (cleaned.isEmpty()) return

        val insertPos = reasoningBuffer.length
        reasoningBuffer.append(cleaned)
        hasPendingReasoningUpdate = true

        // 增量更新 tail 起始索引：检查新增内容中的换行
        val lastLineBreak = cleaned.lastIndexOf('\n')
        if (lastLineBreak >= 0) {
            reasoningTailStartIndex = insertPos + lastLineBreak + 1
        }
    }

    /** 完成事件读取消息前，以及占位消息实际刷新时，统一发布最新缓冲区。 */
    fun flushBufferedContent() {
        if (!hasPendingAnswerUpdate && !hasPendingReasoningUpdate) return
        val current = sharedState.answerMessageEntity.value
        val answer = if (hasPendingAnswerUpdate) answerBuffer.toString() else current.answer
        val reasoning = if (hasPendingReasoningUpdate) {
            reasoningBuffer.toString()
        } else {
            current.reasoningContent
        }
        sharedState.setAnswerMessage(
            current.copy(answer = answer, reasoningContent = reasoning).also { it.uId = current.uId }
        )
        hasPendingAnswerUpdate = false
        hasPendingReasoningUpdate = false
    }

    /**
     * 流中断重试前，把流式内容整体回滚到本回合开始时的快照。
     *
     * 为什么必须连缓冲区一起回滚：增量只写 [answerBuffer]/[reasoningBuffer]，
     * UI 实体是 [flushBufferedContent] 的产物。只改实体不回滚缓冲区时，
     * 重试流的第一个增量就会把已作废的文本整段冲回 UI（画面表现为"内容先回退、
     * 随后又跳回旧长度"），且最终回答里会混入失败尝试的重复段落。
     * 缓冲区回滚后，重试输出恰好接在快照之后，实体长度与缓冲区恒等。
     */
    fun restoreStreamSnapshot(answerSnapshot: String, reasoningSnapshot: String) {
        answerBuffer.setLength(0)
        answerBuffer.append(answerSnapshot)
        reasoningBuffer.setLength(0)
        reasoningBuffer.append(reasoningSnapshot)
        // 与 updateReasoningContentOptimized 的增量维护口径一致：最后一段的起始索引
        reasoningTailStartIndex = reasoningBuffer.lastIndexOf('\n') + 1
        // 标记为待发布，复用统一的发布/驱逐路径（flushBufferedContent + 占位消息重建）
        hasPendingAnswerUpdate = true
        hasPendingReasoningUpdate = true
        // 渲染基线归零，让下一帧按"回滚后的内容"重新计算增量阈值
        lastStreamFrameTime = 0L
        lastRenderedAnswerLength = 0
        lastRenderedReasoningLength = 0
        updatePlaceHolderMessage(forceUpdate = true)
    }

    fun currentStreamContentLength(): Int = answerBuffer.length + reasoningBuffer.length

    fun currentStreamContentSnapshot(): Pair<String, String> =
        answerBuffer.toString() to reasoningBuffer.toString()

    /**
     * 归一化 reasoning 增量，避免多轮 Agent Loop 拼接后产生连续空行。
     *
     * - 将 \r\n 统一为 \n
     * - 首段去掉前导换行
     * - 连续 3 个及以上 \n 折叠为 2 个（\n\n），既保留段落分隔的 Markdown 语义，
     *   又避免深度思考卡片出现大段空白
     */
    private fun normalizeReasoningIncrement(increment: String): String {
        if (increment.isEmpty()) return ""
        val lf = increment.replace("\r\n", "\n")
        if (reasoningBuffer.isEmpty()) {
            return lf.trimStart { it == '\n' || it == '\r' }
        }
        // 只看 buffer 末尾 2 个字符 + 新内容，把跨边界的 3+ 换行折叠为 2 个，
        // 避免在流式过程中反复扫描整个 buffer。
        val boundaryPrefix = reasoningBuffer.takeLast(2).toString()
        val combined = boundaryPrefix + lf
        val collapsed = combined.replace(Regex("""\n{3,}"""), "\n\n")
        return collapsed.removePrefix(boundaryPrefix)
    }

    /**
     * 获取当前 reasoning 的最后一段内容（用于折叠态 preview）。
     *
     * 基于 [reasoningTailStartIndex] 做 O(1) substring，
     * 无正则、无 split、无数组分配。
     */
    fun currentReasoningTail(maxLength: Int = 120): String {
        if (reasoningTailStartIndex >= reasoningBuffer.length) return ""
        return reasoningBuffer
            .substring(reasoningTailStartIndex, reasoningBuffer.length)
            .trim()
            .take(maxLength)
    }

    /** 更新推理时间和消息 uid 状态 */
    fun updateReasoningTimeAndUid() {
        val current = sharedState.answerMessageEntity.value
        if (current.reasoningTime != reasoningTime || current.uId != MessageUIState.STREAMING.value) {
            sharedState.setAnswerMessage(current.copy(
                reasoningTime = reasoningTime
            ).also { it.uId = MessageUIState.STREAMING.value })
        }
        if (sharedState.questionMessageEntity.value.uId != MessageUIState.STREAMING.value) {
            sharedState.setQuestionMessage(sharedState.questionMessageEntity.value.copy().also {
                it.uId = MessageUIState.STREAMING.value
            })
        }
    }

    // ── 增量追加 ──────────────────────────────────────────────

    /**
     * 追加一段 SSE 增量内容到缓冲区，并触发节流 UI 渲染。
     *
     * 设计说明（不再做打字机式逐字切分）：
     * - 上层 [com.shifenmiao.ai.component.AIChatComponent] 以串行 collect +
     *   processingMutex 消费事件。若在此处对增量做逐字 delay，会把整条 SSE 流
     *   串行卡住：显示远落后于模型真实进度，并在 Completed 时产生跳变；
     * - 更严重的是逐字切分会把不完整的 Markdown 语法（未闭合的 ```` ``` ````,
     *   悬空的 `**`、半截表格行等）反复喂给解析器，产生肉眼可见的错位/闪烁。
     *
     * 现在：整段增量直接追加，渲染节奏完全交给 [updatePlaceHolderMessage] →
     * [shouldRenderStreamingFrame] 的节流策略；视觉上的“打字感”来自 token
     * 实际到达节奏。
     */
    fun appendDeltaAndRender(
        content: String,
        update: (String) -> Unit
    ) {
        if (content.isEmpty()) return
        update(content)
        updatePlaceHolderMessage()
    }

    // ── 渲染方法 ──────────────────────────────────────────────

    /**
     * 定位当前流式消息中应显示打字光标的 answer block 索引。
     *
     * UI 模型列表按视觉顺序排列（reverseLayout=true）：footer 在前，answer blocks 居中，
     * header 在后。footer 之后遇到的第一个 answer block 即为文档末尾 block，光标应加在此处。
     */
    private fun findStreamingCursorIndex(uiModels: List<MessageUiModel>): Int? {
        val footerIndex = uiModels.indexOfFirst { it is MessageUiModel.RobotContainerFooter }
        val headerIndex = uiModels.indexOfFirst { it is MessageUiModel.RobotContainerHeader }
        val searchEnd = if (headerIndex >= 0) headerIndex else uiModels.size
        if (footerIndex < 0 || footerIndex >= searchEnd) return null

        val firstAnswerBlockIndex = uiModels.subList(footerIndex, searchEnd)
            .indexOfFirst { it is MessageUiModel.RobotContent }
        return if (firstAnswerBlockIndex >= 0) footerIndex + firstAnswerBlockIndex else null
    }

    /** 带节流的消息渲染 */
    fun renderMessage(forceUpdate: Boolean = false) {
        val currentTime = System.currentTimeMillis()
        if (!forceUpdate && currentTime - lastRenderTime < RENDER_THROTTLE_MS) {
            return
        }
        doRenderMessage()
    }

    private fun doRenderMessage() {
        val now = System.currentTimeMillis()
        lastRenderTime = now
        val showExpandedReasoning = AppSharedStorage.isExpandedReasoningChat.value

        val liveQuestion = sharedState.questionMessageEntity.value
        val liveAnswer = sharedState.answerMessageEntity.value
        val messagesCopy = sharedState.getMessagesSnapshot()
            // 同一 DB 行在内存里可能滞留多份快照（cid 随 Agent 轮次漂移后按 cid 去重失效）,
            // 已落库的行优先按行主键归组; 未落库(id<=0)的占位仍按 cid+role。
            .groupBy {
                if (it.id > 0) RenderRowKey(it.id, it.role) else RenderCidKey(it.completionId, it.role)
            }
            .mapValues { (_, msgs) -> msgs.first() }
            .values
            // 本回合的 Q/A 一律用 live 实体覆盖同组快照: 列表里可能残留这行的旧拷贝
            // （例如刚 beginStreamDraft 时插入的空占位、或上一轮 uId 翻转前后的快照）。
            // 不覆盖时会出现气泡短暂变空/回退到上一轮正文，随后又被下一帧"追平"的闪烁。
            .map { message ->
                when {
                    message === liveAnswer || message === liveQuestion -> message
                    isSameRenderRow(message, liveAnswer) -> liveAnswer
                    isSameRenderRow(message, liveQuestion) -> liveQuestion
                    else -> message
                }
            }
            .sortedByDescending { it.createdAt }
            .toList()

        val newUiModels = messagesCopy.flatMap { message ->
            val isStreaming = message.uId == MessageUIState.STREAMING.value
            // uId 决定消息的渲染形态(NORMAL / ERROR / ...); 错误态必须与流式/正常态分开缓存,
            // 否则本轮流式期间生成的 RobotContent 会被错误消息复用, 错误卡片退化成一坨纯文本.
            //
            // 键里必须带上内容本身: cid 由每轮流式响应写入, 同一行会在 stream→normal 间来回切,
            // 且不同轮次可能落到同一个 cid 上; 只用 cid+uId 做键时, 一轮结束后会命中"这行还是
            // 短文本时"缓存的模型, 气泡正文/深度思考/token 数会整段回退到上一轮的样子。
            val cacheKey = "${message.completionId}_${message.role}_${message.uId}_" +
                "${if (showExpandedReasoning) 1 else 0}_${message.hashCode()}"

            if (!isStreaming && message.uId > 0 && message.id > 0 &&
                cachedMessageUiModels.containsKey(cacheKey)
            ) {
                cachedMessageUiModels[cacheKey]!!
            } else {
                val uiModels = MessageUiModel.fromMessage(
                    message = message,
                    conversation = sharedState.conversation.value,
                    getAIModel = { modelName ->
                        aiEngineCatalogManager.getAiModelTitleByModel(modelName)
                    },
                    showExpandedReasoning = showExpandedReasoning
                )
                val finalUiModels = if (isStreaming) {
                    // streaming 态：在机器人回答的最后一个 block 末尾显示打字光标
                    val cursorIndex = findStreamingCursorIndex(uiModels)
                    val withCursor = if (cursorIndex != null) {
                        uiModels.mapIndexed { index, model ->
                            when {
                                index == cursorIndex && model is MessageUiModel.RobotContent ->
                                    model.copy(showCursor = true)

                                else -> model
                            }
                        }
                    } else uiModels

                    if (reasoningBuffer.isNotEmpty()) {
                        // 用 O(1) 的 currentReasoningTail 直接覆盖 preview，
                        // 跳过 buildReasoningPreview 的正则/split/数组分配
                        val tail = currentReasoningTail()
                        withCursor.map { model ->
                            when (model) {
                                is MessageUiModel.RobotReasoningHeader -> model.copy(preview = tail)
                                is MessageUiModel.UserReasoningHeader -> model.copy(preview = tail)
                                else -> model
                            }
                        }
                    } else {
                        withCursor
                    }
                } else {
                    uiModels
                }
                if (!isStreaming && message.uId > 0 && message.id > 0) {
                    cachedMessageUiModels[cacheKey] = finalUiModels
                }
                finalUiModels
            }
        }

        _messageUiModels.value = newUiModels
        recordRenderedStreamingSnapshot(now)
    }

    /**
     * 更新 placeholder 消息。
     *
     * 关键点：streaming 期间会先把最新 Q/A 实体写回 messages 头部，但是否立刻整段重建 UI
     * 由 [shouldRenderStreamingFrame] 决定。这样可以保留最新数据，又避免长文本时几乎每个 chunk
     * 都重新 parse 整段 Markdown。
     */
    fun updatePlaceHolderMessage(forceUpdate: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!forceUpdate && now - lastPlaceholderUpdateTime < RENDER_THROTTLE_MS) {
            return
        }
        lastPlaceholderUpdateTime = now
        flushBufferedContent()

        val liveQuestion = sharedState.questionMessageEntity.value
        val liveAnswer = sharedState.answerMessageEntity.value
        val currentCompletionId = liveQuestion.completionId

        sharedState.mutateMessages { msgs ->
            msgs.removeIf {
                it.uId <= MessageUIState.STREAMING.value ||
                        it.id <= 0 ||
                        it.completionId == currentCompletionId ||
                        // 同一对 DB 行的旧快照按行主键驱逐：cid 会随 Agent 轮次
                        // (processResponseStarted) 漂移、uId 会随终态翻转，上面三个条件
                        // 都可能漏掉本回合的旧占位，导致同一问题/回答在列表里渲染两次。
                        (liveQuestion.id > 0 && it.id == liveQuestion.id) ||
                        (liveAnswer.id > 0 && it.id == liveAnswer.id)
            }
            msgs.addAll(
                0,
                listOf(
                    liveAnswer,
                    liveQuestion
                )
            )
        }

        if (!forceUpdate && !shouldRenderStreamingFrame(now)) {
            return
        }
        renderMessage(forceUpdate = true)
    }

    /**
     * streaming 帧调度策略：
     * 1. 命中结构边界（换行/句末/代码块闭合）优先刷新；
     * 2. 文本越长，时间阈值与字符阈值越高；
     * 3. 没有新字符时不做无效重绘。
     */
    private fun shouldRenderStreamingFrame(now: Long): Boolean {
        val answer = sharedState.answerMessageEntity.value.answer
        val reasoning = sharedState.answerMessageEntity.value.reasoningContent
        val answerLength = answer.length
        val reasoningLength = reasoning.length
        val answerDelta = answerLength - lastRenderedAnswerLength
        val reasoningDelta = reasoningLength - lastRenderedReasoningLength
        val maxLength = maxOf(answerLength, reasoningLength)
        val elapsed = now - lastStreamFrameTime

        if (lastStreamFrameTime == 0L) return true
        if (answerDelta <= 0 && reasoningDelta <= 0) return false
        if (hasStreamingBoundary(answer, answerDelta) || hasStreamingBoundary(reasoning, reasoningDelta)) {
            return true
        }

        val intervalMs = when {
            maxLength >= STREAM_RENDER_HUGE_LENGTH -> STREAM_RENDER_HUGE_INTERVAL_MS
            maxLength >= STREAM_RENDER_LARGE_LENGTH -> STREAM_RENDER_LARGE_INTERVAL_MS
            maxLength >= STREAM_RENDER_MEDIUM_LENGTH -> STREAM_RENDER_MEDIUM_INTERVAL_MS
            else -> RENDER_THROTTLE_MS
        }
        val charThreshold = when {
            maxLength >= STREAM_RENDER_HUGE_LENGTH -> STREAM_RENDER_HUGE_CHAR_THRESHOLD
            maxLength >= STREAM_RENDER_LARGE_LENGTH -> STREAM_RENDER_LARGE_CHAR_THRESHOLD
            maxLength >= STREAM_RENDER_MEDIUM_LENGTH -> STREAM_RENDER_MEDIUM_CHAR_THRESHOLD
            else -> STREAM_RENDER_SMALL_CHAR_THRESHOLD
        }

        return elapsed >= intervalMs || maxOf(answerDelta, reasoningDelta) >= charThreshold
    }

    /**
     * 轻量边界探测：当新增文本落在“自然停顿”处时提前刷新，提升主观流畅度。
     * 不做昂贵语法分析，只看新增尾部的少量字符。
     */
    private fun hasStreamingBoundary(content: String, delta: Int): Boolean {
        if (content.isEmpty() || delta <= 0) return false
        val tailLength = maxOf(delta + 8, 16).coerceAtMost(content.length)
        val tail = content.takeLast(tailLength)
        // 注意：不把 `endsWith("|")` 当作边界——表格行没写完时刷新会触发对残缺
        // Markdown 的解析（竖线字面化），等行尾 \n 自然落点再刷新即可。
        // `endsWith("\n\n")` 已被 `endsWith("\n")` 覆盖，故不单列。
        return tail.endsWith("\n") ||
            tail.endsWith("```") ||
            tail.endsWith("。") ||
            tail.endsWith("！") ||
            tail.endsWith("？") ||
            tail.endsWith(".") ||
            tail.endsWith("!") ||
            tail.endsWith("?")
    }

    /** 记录一次真正渲染到 UI 的 streaming 快照，供下一轮帧调度计算增量。 */
    private fun recordRenderedStreamingSnapshot(now: Long) {
        lastStreamFrameTime = now
        lastRenderedAnswerLength = sharedState.answerMessageEntity.value.answer.length
        lastRenderedReasoningLength = sharedState.answerMessageEntity.value.reasoningContent.length
    }

    // ── 看门狗 ────────────────────────────────────────────────

    /** 记录收到 chunk 的时间戳（在 handleChatCompletion 开头调用） */
    fun recordChunkReceived() {
        lastChunkAt = System.currentTimeMillis()
        streamChunkCount++
    }

    /** 标记流结束 */
    fun markStreamSawEnd() {
        streamSawEnd = true
    }

    /** 启动 SSE 看门狗 */
    fun startStreamWatchdog(reason: String = "stream") {
        stopStreamWatchdog()
        lastChunkAt = System.currentTimeMillis()
        streamChunkCount = 0
        streamSawEnd = false
        watchdogJob = sharedState.componentScope.launch(sharedState.ioDispatcher) {
            while (isActive && sharedState.chatUIState.value.chatActive) {
                delay(WATCHDOG_INTERVAL_MS)
                val idle = System.currentTimeMillis() - lastChunkAt
                if (!sharedState.chatUIState.value.chatActive) break
                if (streamSawEnd) break
                if (idle > STREAM_IDLE_TIMEOUT_MS) {
                    "Stream idle timeout: idle=${idle}ms chunkCount=$streamChunkCount reason=$reason"
                        .makeLog("StreamContentProcessor")
                    sharedState.componentScope.launch {
                        onStreamIdleTimeout()
                    }
                    break
                }
            }
        }
    }

    /** 停止看门狗 */
    fun stopStreamWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = null
    }

    fun resetStreamWatchdog() {
        lastChunkAt = System.currentTimeMillis()
    }

    // ── 重置/清理 ─────────────────────────────────────────────

    /** 重置所有缓冲区和缓存（新聊天轮次开始时调用） */
    fun resetBuffers() {
        answerBuffer.clear()
        reasoningBuffer.clear()
        hasPendingAnswerUpdate = false
        hasPendingReasoningUpdate = false
        reasoningTailStartIndex = 0
        lastStreamFrameTime = 0L
        lastRenderedAnswerLength = 0
        lastRenderedReasoningLength = 0
    }

    /** 清理所有缓存和 UI 模型 */
    fun clearAll() {
        resetBuffers()
        cachedMessageUiModels.clear()
        _messageUiModels.value = emptyList()
        lastRenderTime = 0L
        lastPlaceholderUpdateTime = 0L
        lastStreamFrameTime = 0L
        lastRenderedAnswerLength = 0
        lastRenderedReasoningLength = 0
    }

    /** 清理消息缓存（消息列表变化时调用） */
    fun invalidateMessageCache() {
        cachedMessageUiModels.clear()
    }
}

/**
 * 判断 [candidate] 是否就是 [live] 这条消息的某个快照。
 *
 * 已落库的行按行主键比对；尚未落库(id<=0)的占位按 completionId+role 比对——
 * 与 [doRenderMessage] 的归组键保持一致，避免把别的消息误判成本回合的行。
 */
private fun isSameRenderRow(candidate: MessageEntity, live: MessageEntity): Boolean {
    if (candidate.role != live.role) return false
    return if (live.id > 0) {
        candidate.id == live.id
    } else {
        candidate.id <= 0 && live.completionId.isNotBlank() &&
            candidate.completionId == live.completionId
    }
}

/** 渲染归组键：已落库的行按行主键，未落库的占位按 cid+role；类型化避免与 cid 字符串撞 key。 */
private data class RenderRowKey(val id: Int, val role: String)

private data class RenderCidKey(val cid: String, val role: String)
