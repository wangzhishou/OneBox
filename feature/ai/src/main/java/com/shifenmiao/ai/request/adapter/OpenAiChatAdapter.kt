/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
