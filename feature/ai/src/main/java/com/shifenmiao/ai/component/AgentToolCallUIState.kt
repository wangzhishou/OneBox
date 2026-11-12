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

import com.shifenmiao.ai.execution.model.ExecutionStepUiModel
import com.shifenmiao.model.ai.ToolCall

/**
 * Agent 工具调用的 UI 状态，驱动聊天界面中工具执行状态的可视化。
 *
 * 状态流转：
 * Idle → Planning(工具规划中) → Executing(工具执行中) → WaitingLLM(等待 LLM 回复) → ... → Idle
 *       → MaxIterationsReached(达到上限，等待用户决定)
 */
sealed class AgentToolCallUIState {
    /** 空闲：无工具调用 */
    data object Idle : AgentToolCallUIState()

    /**
     * LLM 已在流式输出 tool_call delta，但工具参数尚未完整，暂不能执行。
     */
    data class Planning(
        val toolCallCount: Int = 0,
        val iteration: Int = 0,
        val maxIterations: Int = 0,
        val steps: List<ExecutionStepUiModel> = emptyList()
    ) : AgentToolCallUIState()

    /**
     * 工具执行中
     * @param toolCalls 当前批次的所有工具调用
     * @param currentToolName 当前正在执行的工具名称（null 表示尚未开始）
     * @param currentToolCallId 当前正在执行的工具调用 ID（用于取消）
     * @param iteration 当前 Agent 循环轮次
     */
    data class Executing(
        val toolCalls: List<ToolCall>,
        val currentToolName: String? = null,
        val currentToolCallId: String? = null,
        val iteration: Int = 1,
        val maxIterations: Int = 0,
        val steps: List<ExecutionStepUiModel> = emptyList()
    ) : AgentToolCallUIState()

    /**
     * 等待 LLM 回复（工具执行完毕，已将结果发回 LLM）
     * @param iteration 当前 Agent 循环轮次
     */
    data class WaitingLLM(
        val iteration: Int = 1,
        val maxIterations: Int = 0,
        val steps: List<ExecutionStepUiModel> = emptyList()
    ) : AgentToolCallUIState()

    /**
     * 等待用户在全局交互宿主中填写表单或确认操作。
     */
    data class WaitingUserInput(
        val toolName: String,
        val toolCallId: String,
        val requestType: String,
        val iteration: Int = 1,
        val maxIterations: Int = 0,
        val steps: List<ExecutionStepUiModel> = emptyList()
    ) : AgentToolCallUIState()

    /**
     * 达到最大迭代次数，等待用户决定是否继续
     * @param iteration 当前轮次（应等于 MAX_AGENT_ITERATIONS）
     */
    data class MaxIterationsReached(
        val iteration: Int,
        val maxIterations: Int,
        val steps: List<ExecutionStepUiModel> = emptyList()
    ) : AgentToolCallUIState()
}
