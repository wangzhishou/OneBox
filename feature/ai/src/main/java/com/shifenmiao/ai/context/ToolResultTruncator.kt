package com.shifenmiao.ai.context

import com.shifenmiao.ai.agent.tool.AgentToolResult

/**
 * 工具结果智能截断器。
 *
 * 替代原有的固定 4096 字符截断策略，根据内容类型采用不同截断方式：
 * - JSON：保留结构完整的头尾，中间用省略标记
 * - 代码/日志：按行截断，保留头部和尾部
 * - 纯文本：按句子/段落边界截断
 * - 错误结果：不截断（通常很短）
 *
 * 设计原则：
 * 1. 截断后的内容仍能让 LLM 理解核心信息
 * 2. 保留足够的上下文供后续推理
 * 3. 明确告知 LLM 内容被截断
 */
object ToolResultTruncator {

    /** 默认最大字符数 */
    const val DEFAULT_MAX_CHARS = 4096

    /** 最小保留字符数 */
    private const val MIN_CHARS = 200

    /**
     * 截断工具结果内容。
     *
     * @param result 原始工具执行结果
     * @param maxChars 最大字符数限制，0 表示使用默认值
     * @return 截断后的结果（如果不需要截断则返回原始结果）
     */
    fun truncate(
        result: AgentToolResult,
        maxChars: Int = DEFAULT_MAX_CHARS
    ): AgentToolResult {
        val limit = if (maxChars > 0) maxChars else DEFAULT_MAX_CHARS
        val content = result.content

        if (content.length <= limit) return result
        if (result.isError && content.length <= limit * 2) return result // 错误信息宽松处理

        val truncated = smartTruncate(content, limit)
        return AgentToolResult(content = truncated, isError = result.isError)
    }

    /**
     * 批量截断工具结果。
     */
    fun truncateAll(
        results: List<Pair<Any, AgentToolResult>>,
        maxChars: Int = DEFAULT_MAX_CHARS
    ): List<Pair<Any, AgentToolResult>> {
        return results.map { (call, result) ->
            call to truncate(result, maxChars)
        }
    }

    /**
     * 根据内容特征选择截断策略。
     */
    private fun smartTruncate(text: String, maxChars: Int): String {
        if (text.length <= maxChars) return text
        if (maxChars < MIN_CHARS) return text.take(maxChars) + "\n…[truncated]"

        return when {
            isJsonContent(text) -> truncateJson(text, maxChars)
            isCodeOrLogContent(text) -> truncateByLines(text, maxChars)
            else -> truncateText(text, maxChars)
        }
    }

    /**
     * JSON 内容截断：保留头部结构和尾部闭合，中间用省略标记。
     */
    private fun truncateJson(text: String, maxChars: Int): String {
        val headSize = (maxChars * 0.5).toInt()
        val tailSize = (maxChars * 0.3).toInt()

        val head = text.take(headSize)
        val tail = text.takeLast(tailSize)

        return buildString {
            append(head)
            append("\n…[")
            append(text.length - head.length - tail.length)
            append(" chars truncated — JSON content abbreviated]…\n")
            append(tail)
        }
    }

    /**
     * 代码/日志内容截断：按行截断，保留头部和尾部的完整行。
     */
    private fun truncateByLines(text: String, maxChars: Int): String {
        val lines = text.lines()
        if (lines.size <= 2) return truncateText(text, maxChars)

        val headLineCount = (lines.size * 0.4).toInt().coerceAtLeast(1)
        val tailLineCount = (lines.size * 0.2).toInt().coerceAtLeast(1)

        val headLines = lines.take(headLineCount)
        val tailLines = lines.takeLast(tailLineCount)

        val head = headLines.joinToString("\n")
        val tail = tailLines.joinToString("\n")

        // 如果头尾加起来还是超标，进一步缩减
        if (head.length + tail.length > maxChars) {
            return truncateText(text, maxChars)
        }

        val droppedLines = lines.size - headLineCount - tailLineCount
        return buildString {
            append(head)
            append("\n…[")
            append(droppedLines)
            append(" lines truncated]…\n")
            append(tail)
        }
    }

    /**
     * 纯文本截断：尽量在句子或段落边界断开。
     */
    private fun truncateText(text: String, maxChars: Int): String {
        val headSize = (maxChars * 0.6).toInt()
        val tailSize = (maxChars * 0.3).toInt()

        val head = text.take(headSize).let { h ->
            // 尝试在换行处断开
            val lastNewline = h.lastIndexOf('\n')
            if (lastNewline > headSize * 0.7) h.take(lastNewline)
            else {
                // 尝试在句号处断开
                val lastSentence = h.lastIndexOf('。').coerceAtLeast(h.lastIndexOf('.'))
                if (lastSentence > headSize * 0.7) h.take(lastSentence + 1) else h
            }
        }

        val tail = text.takeLast(tailSize).let { t ->
            val firstNewline = t.indexOf('\n')
            if (firstNewline in 1..(tailSize / 2)) t.drop(firstNewline + 1) else t
        }

        return "$head\n\n…[${text.length - head.length - tail.length} chars truncated]…\n\n$tail"
    }

    /** 简单判断是否为 JSON 内容 */
    private fun isJsonContent(text: String): Boolean {
        val trimmed = text.trim()
        return trimmed.startsWith("{") || trimmed.startsWith("[")
    }

    /** 简单判断是否为代码或日志内容 */
    private fun isCodeOrLogContent(text: String): Boolean {
        // 包含大量换行 + 缩进 → 可能是代码/日志
        val lineCount = text.count { it == '\n' }
        return lineCount > 10
    }
}
