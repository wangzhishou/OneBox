package com.shifenmiao.ai.component

import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.execution.model.ExecutionStepUiModel
import com.shifenmiao.model.ai.ToolCall

/**
 * Agent Loop 执行回调接口 —— 统一收口 Runner 执行过程中向外部报告的事件。
 *
 * 替代原先 15+ 个散落的 lambda 参数，职责分组：
 * - 工具生命周期：[onToolStarted] / [onToolCompleted] / [onToolWaitingInput] / [onToolNeedConfirmation]
 * - 执行状态展示：[onShowExecuting] / [onShowWaitingLLM]
 * - 流控制：[onEnsureStartingHint] / [onResetStreamWatchdog]
 * - 内容更新：[onUpdatePlaceHolder] / [onUpdateAnswerMessage]
 */
interface AgentLoopCallback {
    /** 确保起始提示已就绪 */
    fun onEnsureStartingHint()

    /** 工具开始执行 */
    fun onToolStarted(toolCall: ToolCall, stepTitle: String)

    /** 工具执行完成 */
    fun onToolCompleted(toolCall: ToolCall, result: AgentToolResult, stepTitle: String)

    /** 工具等待用户输入 */
    fun onToolWaitingInput(toolCall: ToolCall, stepTitle: String)

    /** 工具需要用户确认 */
    fun onToolNeedConfirmation(toolCall: ToolCall)

    /** 显示工具执行中状态 */
    fun onShowExecuting(
        toolCalls: List<ToolCall>,
        currentToolName: String?,
        currentToolCallId: String?,
        iteration: Int,
        steps: List<ExecutionStepUiModel>,
    )

    /** 显示等待 LLM 响应状态 */
    fun onShowWaitingLLM(iteration: Int, steps: List<ExecutionStepUiModel>)

    /** 重置流看门狗计时器 */
    fun onResetStreamWatchdog()


    /** 更新占位消息 */
    fun onUpdatePlaceHolder(forceUpdate: Boolean)

    /** 更新回答消息（如 previousResponseId） */
    fun onUpdateAnswerMessage(previousResponseId: String?)
}
