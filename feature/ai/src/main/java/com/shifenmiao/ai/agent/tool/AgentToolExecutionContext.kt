package com.shifenmiao.ai.agent.tool

import com.shifenmiao.ai.agent.callback.ToolCallback

/**
 * 工具执行上下文。
 *
 * 通过执行链路向需要“感知当前会话边界”的工具传递只读信息，
 * 例如 toolCallId 和 interactionOwnerId。
 */
data class AgentToolExecutionContext(
    val toolCallId: String? = null,
    val interactionOwnerId: String? = null
)

/**
 * 上下文感知型工具接口。
 *
 * 普通工具无需实现，只有需要读取执行上下文的工具才实现。
 * 典型场景：工具目录查询、策略解释、会话级安全边界回显。
 */
interface ContextAwareAgentTool {
    suspend fun execute(arguments: String, context: AgentToolExecutionContext): AgentToolResult
}

/**
 * 同时需要“执行上下文”与“callback 能力”的工具接口。
 *
 * 这样 callback 子工具链路也能和主链路共用同一套上下文信息：
 * - toolCallId
 * - interactionOwnerId
 */
interface ContextAwareCallbackAgentTool {
    suspend fun execute(
        arguments: String,
        context: AgentToolExecutionContext,
        callback: ToolCallback
    ): AgentToolResult
}
