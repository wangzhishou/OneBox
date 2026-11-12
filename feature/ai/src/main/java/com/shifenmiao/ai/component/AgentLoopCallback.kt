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

    /**
     * 流中断重试前调用：把本回合已流式渲染到 UI 的内容回滚到快照，
     * 避免重试后新旧内容重复叠加。
     */
    fun onRestoreStreamContent(answerSnapshot: String, reasoningSnapshot: String)
}
