package com.shifenmiao.ai.component

import com.shifenmiao.ai.agent.tool.AgentUserQuestionRequest
import com.shifenmiao.ai.agent.tool.ToolConfirmationRequest
import com.shifenmiao.ai.execution.model.ExecutionStepUiModel
import com.shifenmiao.model.ai.ToolCall
import kotlinx.coroutines.flow.MutableStateFlow

interface ToolExecutionUiBridge {
    fun onInteractiveRequestChanged(
        confirmationRequest: ToolConfirmationRequest?,
        questionRequest: AgentUserQuestionRequest?,
        interactionOwnerId: String,
        steps: List<ExecutionStepUiModel> = emptyList()
    )

    fun showPlanning(
        toolCallCount: Int = 0,
        iteration: Int,
        maxIterations: Int,
        steps: List<ExecutionStepUiModel> = emptyList()
    )

    fun showExecuting(
        toolCalls: List<ToolCall>,
        currentToolName: String? = null,
        currentToolCallId: String? = null,
        iteration: Int,
        maxIterations: Int,
        steps: List<ExecutionStepUiModel> = emptyList()
    )

    fun showWaitingLLM(
        iteration: Int,
        maxIterations: Int,
        steps: List<ExecutionStepUiModel> = emptyList()
    )

    fun showWaitingUserInput(
        toolName: String,
        toolCallId: String,
        requestType: String,
        iteration: Int,
        maxIterations: Int,
        steps: List<ExecutionStepUiModel> = emptyList()
    )

    fun showMaxIterationsReached(
        iteration: Int,
        maxIterations: Int,
        steps: List<ExecutionStepUiModel> = emptyList()
    )

    fun showIdle()
}

/**
 * AIChat 工具 UI 状态协调器。
 *
 * 目的不是再造一层复杂架构，而是先把 AIChatComponent 里最容易膨胀的
 * "工具状态适配逻辑"收口出来：
 * - 把运行时交互请求映射成 UI 状态
 * - 统一执行中 / 等待 LLM / 空闲态切换
 * - 按 owner 过滤全局交互请求，避免多个聊天组件互相污染状态
 */
class AIChatToolUiCoordinator(
    private val state: MutableStateFlow<AgentToolCallUIState>,
    private val iterationProvider: () -> Int,
    private val maxIterationsProvider: () -> Int
) : ToolExecutionUiBridge {

    override fun onInteractiveRequestChanged(
        confirmationRequest: ToolConfirmationRequest?,
        questionRequest: AgentUserQuestionRequest?,
        interactionOwnerId: String,
        steps: List<ExecutionStepUiModel>
    ) {
        val activeRequest = when {
            confirmationRequest?.interactionOwnerId == interactionOwnerId -> confirmationRequest
            questionRequest?.interactionOwnerId == interactionOwnerId -> questionRequest
            else -> null
        }

        if (activeRequest != null) {
            val toolName = when (activeRequest) {
                is ToolConfirmationRequest -> activeRequest.toolName
                is AgentUserQuestionRequest -> activeRequest.toolName
                else -> return
            }
            val toolCallId = when (activeRequest) {
                is ToolConfirmationRequest -> activeRequest.toolCallId
                is AgentUserQuestionRequest -> activeRequest.toolCallId
                else -> return
            }
            val requestType = when (activeRequest) {
                is ToolConfirmationRequest -> "CONFIRMATION"
                is AgentUserQuestionRequest -> "QUESTION"
                else -> return
            }
            // 全局宿主负责真正的 UI 呈现，本地状态只展示“等待用户处理中”。
            state.value = AgentToolCallUIState.WaitingUserInput(
                toolName = toolName,
                toolCallId = toolCallId,
                requestType = requestType,
                iteration = iterationProvider(),
                maxIterations = maxIterationsProvider(),
                steps = steps
            )
        }
    }

    override fun showPlanning(
        toolCallCount: Int,
        iteration: Int,
        maxIterations: Int,
        steps: List<ExecutionStepUiModel>
    ) {
        state.value = AgentToolCallUIState.Planning(
            toolCallCount = toolCallCount,
            iteration = iteration,
            maxIterations = maxIterations,
            steps = steps
        )
    }

    override fun showExecuting(
        toolCalls: List<ToolCall>,
        currentToolName: String?,
        currentToolCallId: String?,
        iteration: Int,
        maxIterations: Int,
        steps: List<ExecutionStepUiModel>
    ) {
        state.value = AgentToolCallUIState.Executing(
            toolCalls = toolCalls,
            currentToolName = currentToolName,
            currentToolCallId = currentToolCallId,
            iteration = iteration,
            maxIterations = maxIterations,
            steps = steps
        )
    }

    override fun showWaitingLLM(
        iteration: Int,
        maxIterations: Int,
        steps: List<ExecutionStepUiModel>
    ) {
        state.value = AgentToolCallUIState.WaitingLLM(
            iteration = iteration,
            maxIterations = maxIterations,
            steps = steps
        )
    }

    override fun showWaitingUserInput(
        toolName: String,
        toolCallId: String,
        requestType: String,
        iteration: Int,
        maxIterations: Int,
        steps: List<ExecutionStepUiModel>
    ) {
        state.value = AgentToolCallUIState.WaitingUserInput(
            toolName = toolName,
            toolCallId = toolCallId,
            requestType = requestType,
            iteration = iteration,
            maxIterations = maxIterations,
            steps = steps
        )
    }

    override fun showMaxIterationsReached(
        iteration: Int,
        maxIterations: Int,
        steps: List<ExecutionStepUiModel>
    ) {
        state.value = AgentToolCallUIState.MaxIterationsReached(
            iteration = iteration,
            maxIterations = maxIterations,
            steps = steps
        )
    }

    override fun showIdle() {
        state.value = AgentToolCallUIState.Idle
    }
}
