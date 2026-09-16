package com.shifenmiao.ai.context

import com.shifenmiao.ai.request.LlmRequestGateway
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.unified.LlmMessage
import com.shifenmiao.model.ai.unified.LlmStreamEvent
import com.shifenmiao.model.ai.unified.LlmTurnRequest
import com.t8rin.logger.makeLog
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * 上下文压缩器 —— 把 Agent Loop 中被裁剪掉的早期对话经一次 LLM 调用总结为摘要。
 *
 * 借鉴 Google ADK 的 context compaction 思想：上下文超预算时不直接丢弃早期消息，
 * 而是先压缩成摘要插回上下文，保留任务连续性。
 *
 * 调用约定：
 * - 使用当前会话自己的引擎（[Conversation.engine]），非流式请求，不经计费链；
 * - 任何异常 / 超时 / 空结果都返回 null，由调用方回退到硬裁剪。
 */
@Singleton
class ContextCompactor @Inject constructor(
    private val llmRequestGateway: LlmRequestGateway,
) {

    /**
     * 压缩被裁掉的消息段，返回摘要文本；失败（异常/超时/空结果）返回 null。
     */
    suspend fun compact(
        evicted: List<LlmMessage>,
        conversation: Conversation,
    ): String? {
        if (evicted.isEmpty()) return null
        val transcript = buildTranscript(evicted)
        if (transcript.isBlank()) return null

        val request = LlmTurnRequest(
            model = conversation.engine.model.name,
            stream = false,
            messages = listOf(
                LlmMessage.createTextMessage(role = "system", text = SUMMARY_SYSTEM_PROMPT),
                LlmMessage.createTextMessage(role = "user", text = "请压缩以下对话片段：\n\n$transcript"),
            ),
        )

        return runCatching {
            withTimeoutOrNull(TIMEOUT_MS) {
                val content = StringBuilder()
                var failed = false
                llmRequestGateway.streamTurn(conversation, request)
                    .catch { failed = true }
                    .collect { event ->
                        when (event) {
                            is LlmStreamEvent.TextDelta -> content.append(event.text)
                            is LlmStreamEvent.Error -> failed = true
                            else -> Unit
                        }
                    }
                if (failed) {
                    null
                } else {
                    content.toString().trim()
                        .takeIf { it.isNotBlank() }
                        ?.take(MAX_SUMMARY_CHARS)
                }
            }
        }.getOrElse {
            "ContextCompactor: compact failed: ${it.message}".makeLog("ContextCompactor")
            null
        }
    }

    /**
     * 把被裁消息序列化为可读的对话记录。
     * tool 结果可能很大，逐条截断；总体积封顶，超出部分只保留较新的内容。
     */
    private fun buildTranscript(evicted: List<LlmMessage>): String {
        val builder = StringBuilder()
        evicted.forEach { msg ->
            val roleLabel = when (msg.role) {
                "user" -> "用户"
                "assistant" -> "助手"
                "tool" -> "工具结果(${msg.name ?: "unknown"})"
                "system" -> "系统"
                else -> msg.role
            }
            val text = msg.textContent().take(TOOL_CONTENT_MAX_CHARS)
            if (text.isBlank() && msg.toolCalls.isNotEmpty()) {
                builder.appendLine("$roleLabel：[发起工具调用] ${msg.toolCalls.joinToString { it.function.name }}")
            } else if (text.isNotBlank()) {
                builder.appendLine("$roleLabel：$text")
            }
        }
        if (builder.length <= TRANSCRIPT_MAX_CHARS) return builder.toString()
        // 超出上限时丢弃最旧部分，摘要更关注近期的任务状态
        return "……(更早内容略)\n" + builder.substring(builder.length - TRANSCRIPT_MAX_CHARS)
    }

    private companion object {
        const val TIMEOUT_MS = 30_000L

        /** 单条消息进入总结请求的最大字符数 */
        const val TOOL_CONTENT_MAX_CHARS = 500

        /** 总结请求整体最大字符数 */
        const val TRANSCRIPT_MAX_CHARS = 12_000

        /** 摘要结果防御性上限 */
        const val MAX_SUMMARY_CHARS = 1_500

        val SUMMARY_SYSTEM_PROMPT = """
            你是上下文压缩助手。以下对话片段即将因上下文窗口限制被移除，请将其压缩为一份简明摘要，供后续对话延续使用。
            要求：
            1. 保留用户的核心目标与需求。
            2. 保留已完成的工具操作及其关键结果（做成了什么、什么失败了）。
            3. 保留未完成的待办事项与下一步计划。
            4. 保留关键实体：文件路径、变量名、URL、重要数据等。
            5. 只输出摘要正文，控制在 300 字以内，不要输出任何额外解释。
        """.trimIndent()
    }
}
