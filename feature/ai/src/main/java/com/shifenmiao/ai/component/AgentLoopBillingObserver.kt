package com.shifenmiao.ai.component

import com.shifenmiao.model.ai.Usage
import com.t8rin.logger.makeLog
import javax.inject.Inject

/**
 * 计费观察钩子 —— 只在 LLM 回合结束时记录 usage 日志,不做任何扣费。
 *
 * 注意:Agent 聊天的实际扣费仍在 MessagePersistenceWorker.consumePoints
 * (消息入库时按 token 自报触发,见 AIChatComponent 的入库回调)。
 * 本类仅作为未来把计费迁移到拦截链时的观察点,迁移完成前保持纯日志行为。
 */
class AgentLoopBillingObserver @Inject constructor() : AgentLoopInterceptor {

    override suspend fun afterLlmTurn(context: AgentTurnContext, usage: Usage?) {
        usage ?: return
        "turn_usage conversation=${context.conversationId} iteration=${context.iteration} " +
            "completionId=${context.completionId} total=${usage.totalTokens} " +
            "prompt=${usage.promptTokens} completion=${usage.completionTokens}"
            .makeLog(TAG)
    }

    private companion object {
        const val TAG = "AgentLoopBilling"
    }
}
