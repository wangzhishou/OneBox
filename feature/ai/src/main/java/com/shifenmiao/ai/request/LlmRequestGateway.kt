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

package com.shifenmiao.ai.request

import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.unified.LlmStreamEvent
import com.shifenmiao.model.ai.unified.LlmTurnRequest
import com.t8rin.logger.makeLog
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * 统一 LLM 请求分发器。
 *
 * 注册方式：各 [LlmProviderAdapter] 通过 Hilt @IntoSet 注入本类构造参数。
 * Gateway 按 conversation.engine.requestProtocol 路由到对应 Adapter。
 *
 * 设计约束：
 * - 重复 protocol：保留首条 Adapter，后续同名 Adapter 在 DEBUG 模式记录告警；
 * - 未知 protocol：直接返回 Error 事件，绝不静默回退到云端（用户选 LOCAL 时的隐私要求）。
 */
@Singleton
class LlmRequestGateway @Inject constructor(
    adapters: Set<@JvmSuppressWildcards LlmProviderAdapter>
) {

    private val adapterMap: Map<AiRequestProtocol, LlmProviderAdapter> =
        adapters.groupBy { it.protocol }
            .mapValues { (protocol, list) ->
                if (list.size > 1) {
                    "Multiple adapters registered for $protocol, using first: " +
                        list.joinToString { it::class.simpleName.orEmpty() }
                            .makeLog("LlmRequestGateway")
                }
                list.first()
            }

    fun streamTurn(
        conversation: Conversation,
        request: LlmTurnRequest,
    ): Flow<LlmStreamEvent> {
        val protocol = conversation.engine.requestProtocol
        val adapter = adapterMap[protocol]
            ?: return flowOf(
                LlmStreamEvent.Error(
                    errorMessage = "Unsupported AI protocol: $protocol"
                )
            )
        return adapter.streamTurn(conversation, request)
    }
}
