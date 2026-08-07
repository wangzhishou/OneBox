package com.shifenmiao.ai.agent.tool

/**
 * 交互式 Agent 工具标记接口 —— 继承自 [AgentTool]。
 *
 * 与普通工具的区别：
 * 1. 需要显示交互式问题或确认界面给用户，等待用户处理后才返回结果。
 * 2. 没有执行超时限制 —— 生命周期跟随用户交互或上层取消，工具作者无需关心。
 *
 * 实现要求：
 * - 在 [execute] 中通过 [InteractiveToolRuntime.requestUserQuestion] 或
 *   [InteractiveToolRuntime.requestConfirmation] 挂起等待用户处理。
 * - 确保处理 null 返回值（用户取消）的情况。
 *
 * 使用示例：
 * ```
 * class AskUserTool @Inject constructor(
 *     private val bridge: InteractiveToolRuntime
 * ) : InteractiveAgentTool {
 *     override suspend fun execute(arguments: String): AgentToolResult {
 *         val request = parseQuestionRequest(arguments)
 *         val result = bridge.requestUserQuestion(request)
 *         return AgentToolResult(content = result ?: "用户取消了操作")
 *     }
 * }
 * ```
 */
interface InteractiveAgentTool : AgentTool
