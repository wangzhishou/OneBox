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
 * OpenAI Chat Completions 协议 Adapter。
 *
 * Phase 1：直接委派给既有 [ProtocolRoutingAIRequestHandler]，行为零变化。
 * Phase 2：把 Chat Completions 的请求构建、SSE 解析、chunk → event 转换下沉到本类。
 */
class OpenAiChatAdapter @Inject constructor(
    private val delegate: ProtocolRoutingAIRequestHandler
) : LlmProviderAdapter {

    override val protocol: AiRequestProtocol = AiRequestProtocol.OPENAI_COMPATIBLE

    override fun streamTurn(
        conversation: Conversation,
        request: LlmTurnRequest
    ): Flow<LlmStreamEvent> = delegate.startChatWithDirectRequest(conversation, request)
}
