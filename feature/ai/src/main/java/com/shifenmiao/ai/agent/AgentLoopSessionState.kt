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
