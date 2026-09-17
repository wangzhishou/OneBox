package com.shifenmiao.ai.component

import com.shifenmiao.model.ai.Usage
import com.t8rin.logger.makeLog
import javax.inject.Inject

/**
 * usage 观察钩子 —— 纯 usage 观测日志,只在 LLM 回合结束时记录 usage,不做任何扣费。
 *
 * 注意:Agent 聊天的实际扣费仍在 MessagePersistenceWorker.consumePoints
 * (消息入库时按 token 自报触发,见 AIChatComponent 的入库回调)。
 * 本类是拦截链的占位验证:验证首轮与 follow-up 的 usage 都能被观测到,
 * 未来若把计费迁移到拦截链,这里即是挂接点;迁移完成前保持纯日志行为。
 */
class AgentLoopUsageObserver @Inject constructor() : AgentLoopInterceptor {

    override suspend fun afterLlmTurn(context: AgentTurnContext, usage: Usage?) {
        usage ?: return
        "turn_usage conversation=${context.conversationId} iteration=${context.iteration} " +
            "completionId=${context.completionId} total=${usage.totalTokens} " +
            "prompt=${usage.promptTokens} completion=${usage.completionTokens}"
            .makeLog(TAG)
    }

    private companion object {
        const val TAG = "AgentLoopUsage"
    }
}
