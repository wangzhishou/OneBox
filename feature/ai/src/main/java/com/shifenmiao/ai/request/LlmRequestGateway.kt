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
