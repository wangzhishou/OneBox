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
