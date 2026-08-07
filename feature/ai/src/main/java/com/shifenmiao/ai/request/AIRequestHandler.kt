package com.shifenmiao.ai.request

import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.ToolDefinition
import com.shifenmiao.model.ai.unified.LlmStreamEvent
import com.shifenmiao.model.ai.unified.LlmTurnRequest
import kotlinx.coroutines.flow.Flow

/**
 * 已废弃：保留仅为过渡期不破坏既有调用方，新代码请使用 [LlmRequestGateway] + [LlmProviderAdapter]。
 *
 * 迁移路径：
 * - 调用方改为注入 [LlmRequestGateway]；
 * - 协议分支逻辑由各 [LlmProviderAdapter] 实现按 protocol 自行路由。
 *
 * 计划在 Phase 2 把 OPENAI / Responses / Anthropic / OwnProxy 拆分到独立 Adapter 后
 * 删除该接口。
 */
@Deprecated(
    message = "Use LlmRequestGateway + LlmProviderAdapter instead",
    replaceWith = ReplaceWith("LlmRequestGateway", "com.shifenmiao.ai.request.LlmRequestGateway")
)
interface AIRequestHandler {
    fun startChatWithStreaming(
        conversation: Conversation,
        messageEntityList: List<MessageEntity>,
        enableWebSearch: Boolean = false,
        enableReasoning: Boolean = false,
        tools: List<ToolDefinition>? = null
    ): Flow<LlmStreamEvent>

    /**
     * 使用预构建的 ChatCompletionRequest 直接发起流式请求。
     * 用于 Agent Loop 中的工具调用后续轮次，调用方完全控制请求内容。
     *
     * 默认实现抛出异常；需要支持 tool_calls 的 Handler 应覆盖此方法。
     */
    fun startChatWithDirectRequest(
        conversation: Conversation,
        chatCompletionRequest: LlmTurnRequest
    ): Flow<LlmStreamEvent> {
        throw UnsupportedOperationException(
            "startChatWithDirectRequest not supported by ${this::class.simpleName}"
        )
    }
}