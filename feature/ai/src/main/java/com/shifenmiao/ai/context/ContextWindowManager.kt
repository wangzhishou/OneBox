package com.shifenmiao.ai.context

import com.shifenmiao.model.ai.unified.LlmMessage
import com.t8rin.logger.makeLog

/**
 * 上下文窗口管理器 —— 在发送前裁剪消息列表，确保不超过模型上下文窗口。
 *
 * 设计目标（对标 Claude / OpenCode）：
 * 1. System prompt 永远保留（不可裁剪）
 * 2. 当前用户问题永远保留（不可裁剪）
 * 3. 历史消息按从新到旧填充，直到预算耗尽
 * 4. 工具调用链（assistant tool_calls + tool results）作为原子单元保留或丢弃
 * 5. 当预算紧张时，对远端工具结果做内容截断
 * 6. 裁剪后在消息列表开头插入裁剪提示，避免 LLM 因"丢失记忆"而困惑
 *
 * 线程安全：无状态，所有方法均为纯函数。
 */
object ContextWindowManager {

    /**
     * 输出预留比例：上下文窗口的 20%，但不超过此上限。
     * 用于给模型生成留出空间。
     */
    private const val OUTPUT_RESERVE_RATIO = 0.20
    private const val OUTPUT_RESERVE_CAP = 8192

    /** 工具结果截断时的最小保留字符数 */
    private const val MIN_TOOL_RESULT_CHARS = 200

    /** 工具结果截断时的默认最大字符数 */
    private const val DEFAULT_TOOL_RESULT_MAX_CHARS = 4096

    /**
     * 裁剪结果。
     *
     * @param kept 保留的消息列表（system + 选中的消息段，不含裁剪提示）
     * @param evicted 被整体裁掉的早期消息（被截断的 tool result 不算，仍属 kept）
     * @param droppedSegmentCount 被裁掉的消息段数量（用于裁剪提示文案）
     */
    data class FitResult(
        val kept: List<LlmMessage>,
        val evicted: List<LlmMessage>,
        val droppedSegmentCount: Int,
    )

    /**
     * 裁剪消息列表使其适配模型上下文窗口。
     *
     * @param messages 完整的消息列表（index 0 通常是 system prompt）
     * @param contextWindowTokens 模型上下文窗口大小（token 数）
     * @param maxOutputTokens 模型最大输出 token 数，用于预留输出空间
     * @return 裁剪后的消息列表
     */
    fun fitToContextWindow(
        messages: List<LlmMessage>,
        contextWindowTokens: Int,
        maxOutputTokens: Int = 0,
    ): List<LlmMessage> {
        val result = fitToContextWindowWithEvicted(messages, contextWindowTokens, maxOutputTokens)
        if (result.droppedSegmentCount == 0) return result.kept

        // 5.1: 在 system 之后插入裁剪提示，避免 LLM 因“丢失记忆”而困惑
        val marker = LlmMessage.createTextMessage(
            role = "system",
            text = "...[${result.droppedSegmentCount} earlier message segments omitted due to context window limits]..."
        )
        val insertIndex = result.kept.indexOfFirst { it.role != "system" }
            .let { if (it < 0) result.kept.size else it }
        return result.kept.toMutableList().apply { add(insertIndex, marker) }
    }

    /**
     * 裁剪消息列表并同时返回被裁掉的早期消息，供 [ContextCompactor] 压缩为摘要。
     *
     * 裁剪规则与 [fitToContextWindow] 一致，但返回的 kept 不含裁剪提示占位，
     * 由调用方决定插入摘要还是走 [fitToContextWindow] 的占位逻辑。
     */
    fun fitToContextWindowWithEvicted(
        messages: List<LlmMessage>,
        contextWindowTokens: Int,
        maxOutputTokens: Int = 0,
    ): FitResult {
        if (messages.isEmpty()) return FitResult(messages, emptyList(), 0)

        val outputReserve = if (maxOutputTokens > 0) {
            maxOutputTokens
        } else {
            minOf((contextWindowTokens * OUTPUT_RESERVE_RATIO).toInt(), OUTPUT_RESERVE_CAP)
        }
        val budget = contextWindowTokens - outputReserve
        if (budget <= 0) {
            "ContextWindowManager: budget <= 0 (window=$contextWindowTokens, reserve=$outputReserve)"
                .makeLog("ContextWindow")
            // 至少保留最后一条；此处无 segment 概念，droppedSegmentCount 置 0 不插占位
            return FitResult(
                kept = messages.takeLast(1),
                evicted = messages.dropLast(1),
                droppedSegmentCount = 0,
            )
        }

        // 分离 system prompt 和对话消息
        val (systemMessages, conversationMessages) = messages.partition { it.role == "system" }
        val systemTokens = TokenEstimator.estimateMessages(systemMessages)

        if (systemTokens >= budget) {
            "ContextWindowManager: system prompt alone ($systemTokens) exceeds budget ($budget)"
                .makeLog("ContextWindow")
            // system prompt 本身就超标，只保留 system + 最后一条消息
            val lastMsg = conversationMessages.lastOrNull()
                ?: return FitResult(systemMessages, emptyList(), 0)
            return FitResult(
                kept = systemMessages + lastMsg,
                evicted = conversationMessages.dropLast(1),
                droppedSegmentCount = 0,
            )
        }

        val remainingBudget = budget - systemTokens

        // 将对话消息按工具调用链分组为原子单元
        val segments = segmentMessages(conversationMessages)

        // 从最新到最旧填充
        val selectedSegments = mutableListOf<MessageSegment>()
        var usedTokens = 0

        // 最后一个 segment（当前用户问题）必须保留
        if (segments.isNotEmpty()) {
            val lastSegment = segments.last()
            val lastTokens = TokenEstimator.estimateMessages(lastSegment.messages)
            selectedSegments.add(0, lastSegment)
            usedTokens += lastTokens
        }

        // 从倒数第二个 segment 开始向前填充
        for (i in (segments.size - 2) downTo 0) {
            val segment = segments[i]
            val segmentTokens = TokenEstimator.estimateMessages(segment.messages)
            if (usedTokens + segmentTokens <= remainingBudget) {
                selectedSegments.add(0, segment)
                usedTokens += segmentTokens
            } else {
                // 预算不够放完整 segment，尝试截断工具结果后再试
                val truncated = truncateToolResults(segment, remainingBudget - usedTokens)
                if (truncated != null) {
                    selectedSegments.add(0, truncated)
                    usedTokens += TokenEstimator.estimateMessages(truncated.messages)
                }
                // 无论是否截断成功，后续更旧的 segment 都放不下
                break
            }
        }

        val droppedCount = segments.size - selectedSegments.size
        // 选中段从尾部连续，被裁段即前 droppedCount 个；被截断的 tool result 段仍在选中列，不算 evicted
        val evicted = segments.take(droppedCount).flatMap { it.messages }
        val kept = systemMessages + selectedSegments.flatMap { it.messages }

        "ContextWindowManager: ${messages.size} msgs → ${kept.size} msgs, " +
            "dropped $droppedCount segments, " +
            "est. tokens: system=$systemTokens + history=$usedTokens = ${systemTokens + usedTokens} / $budget"
            .makeLog("ContextWindow")

        return FitResult(
            kept = kept,
            evicted = evicted,
            droppedSegmentCount = droppedCount,
        )
    }

    /**
     * 将消息列表按工具调用链分组为原子 segment。
     *
     * 分组规则：
     * - assistant(tool_calls) + 后续连续的 tool(result) → 一个 ToolCallChain segment
     * - 其他消息各自独立为 SingleMessage segment
     */
    private fun segmentMessages(messages: List<LlmMessage>): List<MessageSegment> {
        val segments = mutableListOf<MessageSegment>()
        var i = 0

        while (i < messages.size) {
            val msg = messages[i]

            // 检测是否为 assistant tool_calls 消息
            if (msg.role == "assistant" && msg.toolCalls.isNotEmpty()) {
                val chainMessages = mutableListOf(msg)
                val expectedIds = msg.toolCalls.map { it.id }.toSet()
                var j = i + 1

                // 收集对应的 tool result 消息，校验 toolCallId 匹配
                while (j < messages.size && messages[j].role == "tool") {
                    val toolResult = messages[j]
                    // 5.2: 校验 toolCallId 是否匹配，不匹配则断链
                    val resultId = toolResult.toolCallId
                    if (!resultId.isNullOrEmpty() && resultId !in expectedIds) {
                        "ContextWindowManager: tool result toolCallId='${toolResult.toolCallId}' " +
                            "not in expectedIds=$expectedIds, breaking chain"
                            .makeLog("ContextWindow")
                        break
                    }
                    chainMessages.add(toolResult)
                    j++
                }

                segments.add(MessageSegment.ToolCallChain(chainMessages))
                i = j
            } else {
                segments.add(MessageSegment.SingleMessage(msg))
                i++
            }
        }

        return segments
    }

    /**
     * 对工具调用链中的 tool result 内容进行截断，尝试适配剩余预算。
     *
     * @return 截断后的 segment，如果截断后仍超出预算则返回 null
     */
    private fun truncateToolResults(
        segment: MessageSegment,
        tokenBudget: Int
    ): MessageSegment? {
        if (segment !is MessageSegment.ToolCallChain) return null

        val originalTokens = TokenEstimator.estimateMessages(segment.messages)
        if (originalTokens <= tokenBudget) return segment

        // 尝试逐个截断每个 tool result，复用 ToolResultTruncator 的内容感知策略
        val truncatedMessages = segment.messages.map { msg ->
            if (msg.role == "tool" && msg.parts.isNotEmpty()) {
                val text = msg.textContent()
                if (text.length > MIN_TOOL_RESULT_CHARS) {
                    // 按字符比例截断，但委托给 ToolResultTruncator 的内容感知截断
                    val ratio = tokenBudget.toFloat() / originalTokens
                    val targetChars = maxOf(
                        MIN_TOOL_RESULT_CHARS,
                        (text.length * ratio).toInt()
                    )
                    // 4.2: 复用 ToolResultTruncator 的智能截断逻辑
                    val agentResult = com.shifenmiao.ai.agent.tool.AgentToolResult(content = text)
                    val truncatedResult = ToolResultTruncator.truncate(agentResult, targetChars)
                    val truncatedText = truncatedResult.content
                    msg.copy(
                        parts = listOf(
                            com.shifenmiao.model.ai.unified.LlmContentPart.Text(truncatedText)
                        )
                    )
                } else {
                    msg
                }
            } else {
                msg
            }
        }

        val truncatedTokens = TokenEstimator.estimateMessages(truncatedMessages)
        return if (truncatedTokens <= tokenBudget) {
            MessageSegment.ToolCallChain(truncatedMessages)
        } else {
            null // 即使截断也放不下
        }
    }

    /**
     * 消息分段 —— 将消息按语义边界分组，确保工具调用链不被拆分。
     */
    sealed class MessageSegment {
        abstract val messages: List<LlmMessage>

        /** 单条独立消息 */
        data class SingleMessage(
            val message: LlmMessage
        ) : MessageSegment() {
            override val messages: List<LlmMessage> = listOf(message)
        }

        /** 工具调用链：assistant(tool_calls) + tool(result) × N */
        data class ToolCallChain(
            override val messages: List<LlmMessage>
        ) : MessageSegment()
    }
}
