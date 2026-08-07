package com.shifenmiao.ai.agent.tool

import com.shifenmiao.model.ai.AttachedMedia

/**
 * Agent 工具执行结果。
 *
 * @param content 工具返回的文本内容，将作为 tool role 消息回传给 LLM
 * @param isError 是否为错误结果；true 时 LLM 会知道工具执行失败
 * @param multiModalAttachments 多模态附件（如截图），会作为 image_url 与 [content] 一同回传给 LLM。
 *            仅在 LLM 支持多模态 tool_result 的协议下生效；纯文本协议会忽略附件仅保留 content。
 *            工具内应保证附件数量可控（建议 ≤ 3），避免单次 tool result 体积过大。
 */
data class AgentToolResult(
    val content: String,
    val isError: Boolean = false,
    val multiModalAttachments: List<AttachedMedia> = emptyList()
)

