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
 * OpenAI Responses API Adapter。
 *
 * Phase 1：委派给既有 Handler，保留原 event-first SSE 解析逻辑。
 * Phase 2：把 Responses 专属 SSE 解析、output item → event 转换下沉到本类。
 */
class ResponsesAdapter @Inject constructor(
    private val delegate: ProtocolRoutingAIRequestHandler
) : LlmProviderAdapter {

    override val protocol: AiRequestProtocol = AiRequestProtocol.RESPONSES_COMPATIBLE

    override fun streamTurn(
        conversation: Conversation,
        request: LlmTurnRequest
    ): Flow<LlmStreamEvent> = delegate.startChatWithDirectRequest(conversation, request)
}
