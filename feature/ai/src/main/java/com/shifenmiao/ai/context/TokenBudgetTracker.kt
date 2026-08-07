package com.shifenmiao.ai.context

import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.unified.LlmMessage
import com.t8rin.logger.makeLog

/**
 * Agent Loop 全局 Token 预算追踪器。
 *
 * 在 Agent Loop 执行循环中跨迭代累积 token 使用量，
 * 帮助 [com.shifenmiao.ai.component.AgentLoopRunner] 决定何时需要调用
 * [ContextWindowManager] 进行上下文裁剪。
 *
 * 设计原则：
 * 1. 有状态但无副作用 —— 只记录数值，不做裁剪决策
 * 2. 每次迭代后更新，追踪累积增量
 * 3. 提供预算健康度判断（是否接近上限）
 *
 * 线程安全：仅在 Agent Loop 协程内单线程使用，无需同步。
 */
class TokenBudgetTracker(
    private val contextWindowTokens: Int,
    private val outputReserveTokens: Int,
) {
    /** System prompt 估算 token 数 */
    var systemTokens: Int = 0
        private set

    /** 初始对话历史 token 数（首次 buildContextMessages 后） */
    var initialHistoryTokens: Int = 0
        private set

    /** 累积的工具结果 token 数（跨迭代） */
    var accumulatedToolResultTokens: Int = 0
        private set

    /** 最近一次 follow-up 请求前的上下文总 token 估算 */
    var lastSnapshotTokens: Int = 0
        private set

    /** 已执行的迭代次数 */
    var iterationCount: Int = 0
        private set

    /** 可用预算 = 上下文窗口 - 输出预留 */
    val budget: Int
        get() = (contextWindowTokens - outputReserveTokens).coerceAtLeast(0)

    /** 当前估算总使用量 */
    val currentUsage: Int
        get() = systemTokens + initialHistoryTokens + accumulatedToolResultTokens

    /** 剩余可用 token */
    val remaining: Int
        get() = (budget - currentUsage).coerceAtLeast(0)

    /**
     * 使用率（0.0 ~ 1.0+），超过 1.0 表示已超出预算。
     */
    val usageRatio: Float
        get() = if (budget > 0) currentUsage.toFloat() / budget else 1f

    /**
     * 是否需要触发上下文裁剪。
     * 当使用率超过 [TRUNCATION_THRESHOLD] 时返回 true。
     */
    val needsTruncation: Boolean
        get() = usageRatio >= TRUNCATION_THRESHOLD

    /**
     * 初始化系统 prompt token 数。
     */
    fun recordSystemTokens(messages: List<LlmMessage>) {
        systemTokens = TokenEstimator.estimateMessages(messages)
    }

    /**
     * 记录初始对话历史 token 数。
     */
    fun recordInitialHistory(messages: List<LlmMessage>) {
        initialHistoryTokens = TokenEstimator.estimateMessages(messages)
    }

    /**
     * 记录一次迭代的工具结果 token 增量。
     */
    fun addToolResultTokens(messages: List<LlmMessage>) {
        val tokens = TokenEstimator.estimateMessages(messages)
        accumulatedToolResultTokens += tokens
        iterationCount++
        logSnapshot("addToolResults")
    }

    /**
     * 快照当前总使用量（在发送 follow-up 前调用）。
     */
    fun snapshot(): Int {
        lastSnapshotTokens = currentUsage
        return lastSnapshotTokens
    }

    /**
     * 重置追踪器（新请求开始时）。
     */
    fun reset() {
        systemTokens = 0
        initialHistoryTokens = 0
        accumulatedToolResultTokens = 0
        lastSnapshotTokens = 0
        iterationCount = 0
    }

    /**
     * 便捷方法：基于 AiModel 创建追踪器。
     */
    companion object {
        /** 触发裁剪的使用率阈值 */
        const val TRUNCATION_THRESHOLD = 0.75f

        /** 输出预留比例 */
        private const val OUTPUT_RESERVE_RATIO = 0.20
        private const val OUTPUT_RESERVE_CAP = 8192

        fun forModel(model: AiModel): TokenBudgetTracker {
            val contextWindow = model.effectiveContextWindow()
            val outputReserve = minOf(
                (contextWindow * OUTPUT_RESERVE_RATIO).toInt(),
                OUTPUT_RESERVE_CAP
            )
            return TokenBudgetTracker(
                contextWindowTokens = contextWindow,
                outputReserveTokens = outputReserve,
            )
        }
    }

    private fun logSnapshot(tag: String) {
        "TokenBudget[$tag]: iter=$iterationCount " +
            "sys=$systemTokens hist=$initialHistoryTokens " +
            "toolAccum=$accumulatedToolResultTokens " +
            "total=$currentUsage/$budget (${(usageRatio * 100).toInt()}%)"
            .makeLog("TokenBudget")
    }
}
