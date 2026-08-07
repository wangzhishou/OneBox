package com.shifenmiao.ai.request.adapter

import com.shifenmiao.ai.request.LlmProviderAdapter
import com.shifenmiao.ai.request.ProtocolRoutingAIRequestHandler
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.unified.LlmStreamEvent
import com.shifenmiao.model.ai.unified.LlmTurnRequest
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Anthropic Messages API 兼容协议 Adapter。
 *
 * Phase 1：委派给既有 Handler，转换与 SSE 解析保留在 Handler 内。
 * Phase 2：把 AnthropicMessagesRequest 转换、content_block 增量解析下沉到本类。
 */
class AnthropicAdapter @Inject constructor(
    private val delegate: ProtocolRoutingAIRequestHandler
) : LlmProviderAdapter {

    override val protocol: AiRequestProtocol = AiRequestProtocol.ANTHROPIC_COMPATIBLE

    override fun streamTurn(
        conversation: Conversation,
        request: LlmTurnRequest
    ): Flow<LlmStreamEvent> = delegate.startChatWithDirectRequest(conversation, request)
}
