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

package com.shifenmiao.ai.agent

/**
 * 单个 AI 会话独有的 Agent 调用链状态。
 *
 * 之前这些状态直接挂在单例 [AgentLoopExecutor] 上，多个会话并行时会互相污染。
 * 现在把"累积中的 tool_call 碎片"与"当前迭代计数"提到会话侧保存，
 * 执行器本身只保留纯执行职责。
 */
class AgentLoopSessionState {

    internal data class AccumulatedToolCall(
        var id: String = "",
        var type: String = "function",
        var functionName: StringBuilder = StringBuilder(),
        var functionArguments: StringBuilder = StringBuilder()
    )

    internal val accumulator = mutableMapOf<Int, AccumulatedToolCall>()

    var currentIteration: Int = 0
        private set

    private var iterationLimit: Int = 0

    fun currentIterationLimit(): Int = iterationLimit

    fun resetAccumulator() {
        accumulator.clear()
    }

    fun reset(maxIterations: Int) {
        accumulator.clear()
        currentIteration = 0
        iterationLimit = maxIterations.coerceAtLeast(1)
    }

    fun incrementIteration() {
        currentIteration++
    }

    fun ensureIterationLimitAtLeast(maxIterations: Int) {
        if (iterationLimit < maxIterations) {
            iterationLimit = maxIterations.coerceAtLeast(1)
        }
    }

    fun extendIterationLimit(additionalIterations: Int) {
        iterationLimit = (iterationLimit + additionalIterations.coerceAtLeast(1))
            .coerceAtLeast(currentIteration + 1)
    }
}
