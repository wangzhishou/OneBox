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

import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.model.ai.ToolCall
import com.shifenmiao.model.ai.Usage
import com.t8rin.logger.makeLog
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * 统一日志拦截器 —— 把循环关键事件(轮次、工具名、耗时、usage、错误)
 * 集中到一个 Log 通道输出。
 *
 * Runner/Executor 内既有的散落日志保持不变(避免行为变化),
 * 本类只提供新的统一通道,便于后续按 tag 抓取或接入埋点。
 */
class AgentLoopLoggingInterceptor @Inject constructor() : AgentLoopInterceptor {

    /** 工具开始时间(按 toolCallId 关联 before/after,并行工具共用故用并发容器) */
    private val toolStartTimes = ConcurrentHashMap<String, Long>()

    override suspend fun beforeLlmTurn(context: AgentTurnContext) {
        "llm_turn_start conversation=${context.conversationId} iteration=${context.iteration} " +
            "completionId=${context.completionId} model=${context.conversation.engine.model.name}"
            .makeLog(TAG)
    }

    override suspend fun afterLlmTurn(context: AgentTurnContext, usage: Usage?) {
        "llm_turn_end conversation=${context.conversationId} iteration=${context.iteration} " +
            "usageTotal=${usage?.totalTokens ?: -1} prompt=${usage?.promptTokens ?: -1} " +
            "completion=${usage?.completionTokens ?: -1}"
            .makeLog(TAG)
    }

    override suspend fun beforeToolExecute(toolCall: ToolCall, tool: AgentTool?) {
        toolStartTimes[toolCall.id] = System.currentTimeMillis()
        "tool_start name=${toolCall.function.name} id=${toolCall.id}".makeLog(TAG)
    }

    override suspend fun afterToolExecute(toolCall: ToolCall, result: AgentToolResult) {
        val elapsedMs = toolStartTimes.remove(toolCall.id)
            ?.let { System.currentTimeMillis() - it }
        "tool_end name=${toolCall.function.name} id=${toolCall.id} isError=${result.isError} " +
            "elapsedMs=${elapsedMs ?: -1} contentLen=${result.content.length}"
            .makeLog(TAG)
    }

    override suspend fun onLoopError(context: AgentTurnContext, error: Throwable) {
        "loop_error conversation=${context.conversationId} iteration=${context.iteration} " +
            "error=${error.javaClass.simpleName}: ${error.message}"
            .makeLog(TAG)
    }

    private companion object {
        const val TAG = "AgentLoop"
    }
}
