package com.shifenmiao.ai.context

import com.shifenmiao.model.ai.unified.LlmContentPart
import com.shifenmiao.model.ai.unified.LlmMessage

/**
 * 近似 Token 计数器。
 *
 * 不依赖外部 tokenizer 库，通过字符类型加权估算，精度约 ±15%。
 * 足以支撑上下文窗口裁剪决策（不需要精确到个位数）。
 *
 * 估算规则：
 * - CJK 字符（中日韩）：约 1.5 token/字
 * - 英文单词：约 1.3 token/word（~4 字符/token）
 * - JSON/代码：约 0.3 token/字符
 * - 图片：固定 token 开销（按 base64 估算）
 * - 工具调用结构：额外 token 开销（function name, arguments 框架）
 */
object TokenEstimator {

    /** 单条消息的 token 估算 */
    fun estimateMessage(message: LlmMessage): Int {
        var tokens = 0

        // 角色标记固定开销（<role>...</role>）
        tokens += 4

        // 文本内容
        for (part in message.parts) {
            tokens += when (part) {
                is LlmContentPart.Text -> estimateText(part.text)
                is LlmContentPart.ImageUrlPart -> IMAGE_TOKEN_COST
            }
        }

        // reasoning content
        message.reasoningContent?.let {
            tokens += estimateText(it)
            tokens += 4 // 结构开销
        }

        // tool calls 结构开销
        if (message.toolCalls.isNotEmpty()) {
            for (toolCall in message.toolCalls) {
                tokens += 7 // function call 框架 token
                tokens += estimateText(toolCall.function.name)
                tokens += estimateText(toolCall.function.arguments)
            }
        }

        // tool result 结构开销
        if (message.toolCallId != null) {
            tokens += 3 // tool_call_id 引用
        }

        return tokens
    }

    /** 消息列表的总 token 估算 */
    fun estimateMessages(messages: List<LlmMessage>): Int {
        // 每条消息有 2 token 的消息分隔符开销（<message>...</message>）
        return messages.sumOf { estimateMessage(it) } + messages.size * 2
    }

    /**
     * 估算文本的 token 数。
     *
     * 按字符类型分区计算：
     * - CJK 区间：每个字符 ≈ 1.5 token
     * - ASCII 字母/数字：每 4 个字符 ≈ 1 token
     * - 其他 Unicode：每个字符 ≈ 1 token
     */
    fun estimateText(text: String): Int {
        if (text.isEmpty()) return 0

        var cjkCount = 0
        var asciiWordChars = 0
        var otherChars = 0

        for (char in text) {
            when {
                char.isCjk() -> cjkCount++
                char.isAsciiWordChar() -> asciiWordChars++
                else -> otherChars++
            }
        }

        return (cjkCount * 1.5).toInt() +
            (asciiWordChars / 3.5).toInt() +
            otherChars
    }

    private fun Char.isCjk(): Boolean {
        val code = this.code
        return code in 0x4E00..0x9FFF ||      // CJK 统一汉字
            code in 0x3400..0x4DBF ||          // CJK 扩展 A
            code in 0x20000..0x2A6DF ||        // CJK 扩展 B
            code in 0x3040..0x309F ||          // 平假名
            code in 0x30A0..0x30FF ||          // 片假名
            code in 0xAC00..0xD7AF ||          // 韩文音节
            code in 0xF900..0xFAFF             // CJK 兼容汉字
    }

    private fun Char.isAsciiWordChar(): Boolean {
        return this in 'a'..'z' || this in 'A'..'Z' || this in '0'..'9' || this == '_'
    }

    /** 图片的固定 token 开销（低分辨率约 85 token，高分辨率约 170 token） */
    private const val IMAGE_TOKEN_COST = 130
}
