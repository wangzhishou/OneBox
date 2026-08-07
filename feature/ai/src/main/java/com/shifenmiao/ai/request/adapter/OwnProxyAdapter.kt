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
 * 内部自研代理协议 Adapter（走 OwnProxyAIService）。
 */
class OwnProxyAdapter @Inject constructor(
    private val delegate: ProtocolRoutingAIRequestHandler
) : LlmProviderAdapter {

    override val protocol: AiRequestProtocol = AiRequestProtocol.OWN_PROXY

    override fun streamTurn(
        conversation: Conversation,
        request: LlmTurnRequest
    ): Flow<LlmStreamEvent> = delegate.startChatWithDirectRequest(conversation, request)
}
