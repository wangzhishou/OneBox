package com.shifenmiao.ai.component

import android.content.Context
import com.google.gson.Gson
import com.shifenmiao.ai.agent.AgentLoopExecutor
import com.shifenmiao.ai.agent.AgentLoopSessionState
import com.shifenmiao.ai.agent.ResumeState
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.InteractivePendingRequestSnapshot
import com.shifenmiao.ai.agent.tool.InteractiveToolResultFactory
import com.shifenmiao.ai.agent.tool.InteractiveToolRuntime
import com.shifenmiao.ai.agent.callback.ToolCallbackRouter
import com.shifenmiao.ai.execution.model.ExecutionStepStatus
import com.shifenmiao.ai.execution.presenter.ToolExecutionTextResolver
import com.shifenmiao.core.R
import com.shifenmiao.database.ai.entity.ToolCallTaskEntity
import com.shifenmiao.model.ai.ToolCall
import com.t8rin.logger.makeLog

/**
 * 工具恢复协调器 —— 负责 Agent Loop 断点恢复逻辑。
 *
 * 从 AgentLoopOrchestrator 中抽离，职责边界：
 * 1. 分析 ResumeState 确定恢复策略
 * 2. 恢复交互式表单（确认/表单/空值）
 * 3. 恢复挂起的工具任务
 * 4. 构建恢复上下文供 LLM follow-up 使用
 *
 * 设计原则：
 * - 恢复决策逻辑内聚于此，不分散在 Orchestrator 中
 * - 实际执行（LLM follow-up、AgentLoop 重入）通过回调委托给调用方
 */
class ToolResumeCoordinator(
    private val agentLoopExecutor: AgentLoopExecutor,
    private val interactiveToolBridge: InteractiveToolRuntime,
    private val gson: Gson,
    private val appContext: Context,
) {

    /**
     * 恢复操作结果。
     *
     * @param shouldResumeLLMFollowUp 是否应继续 LLM follow-up
     * @param conversationId 会话 ID
     */
    data class ResumeDecision(
        val shouldResumeLLMFollowUp: Boolean,
        val conversationId: String,
    )

    /**
     * 检查并恢复 Agent Loop。
     *
     * 分析数据库中的未完成任务，根据状态决定恢复策略：
     * - WaitingInput: 恢复交互式输入并继续执行
     * - FromTool: 从工具调用点恢复
     * - CompletedNeedsLLM: 直接发起 LLM follow-up
     *
     * @param conversationId 会话 ID
     * @param session Agent Loop 会话状态
     * @param interactionOwnerId 交互所有者 ID
     * @param callbackRouter 工具回调路由
     * @param currentQuestionProvider 当前问题提供者
     * @param onResumeToolTasks 恢复工具任务执行
     * @param onResumeLLMFollowUp 恢复 LLM follow-up
     * @param onShowIdle 显示空闲 UI
     */
    suspend fun checkAndResume(
        conversationId: String,
        session: AgentLoopSessionState,
        interactionOwnerId: String,
        callbackRouter: ToolCallbackRouter,
        currentQuestionProvider: () -> String,
        onResumeToolTasks: suspend (
            tasks: List<ToolCallTaskEntity>,
            iteration: Int,
            skipConfirmationForTaskIds: Set<String>,
            onToolCompleted: (ToolCall, AgentToolResult) -> Unit,
        ) -> Unit,
        onResumeLLMFollowUp: suspend (conversationId: String) -> Unit,
        onShowIdle: () -> Unit,
    ) {
        if (conversationId.isEmpty()) return

        try {
            val resumeState = agentLoopExecutor.taskManager.buildResumeState(conversationId) ?: return

            when (resumeState) {
                is ResumeState.WaitingInput -> {
                    handleWaitingInputResume(
                        resumeState = resumeState,
                        session = session,
                        interactionOwnerId = interactionOwnerId,
                        callbackRouter = callbackRouter,
                        currentQuestionProvider = currentQuestionProvider,
                        onResumeToolTasks = onResumeToolTasks,
                        onResumeLLMFollowUp = onResumeLLMFollowUp,
                    )
                }

                is ResumeState.FromTool -> {
                    handleFromToolResume(
                        resumeState = resumeState,
                        onResumeToolTasks = onResumeToolTasks,
                        onResumeLLMFollowUp = onResumeLLMFollowUp,
                    )
                }

                is ResumeState.CompletedNeedsLLM -> {
                    onResumeLLMFollowUp(conversationId)
                }
            }
        } catch (e: Exception) {
            "Failed to resume Agent Loop: $e".makeLog("ToolResumeCoordinator")
            runCatching { onShowIdle() }
        }
    }

    private suspend fun handleWaitingInputResume(
        resumeState: ResumeState.WaitingInput,
        session: AgentLoopSessionState,
        interactionOwnerId: String,
        callbackRouter: ToolCallbackRouter,
        currentQuestionProvider: () -> String,
        onResumeToolTasks: suspend (
            tasks: List<ToolCallTaskEntity>,
            iteration: Int,
            skipConfirmationForTaskIds: Set<String>,
            onToolCompleted: (ToolCall, AgentToolResult) -> Unit,
        ) -> Unit,
        onResumeLLMFollowUp: suspend (conversationId: String) -> Unit,
    ) {
        val task = resumeState.task
        val restored = interactiveToolBridge.restoreWaitingInput(task)
        val requestKind = restored?.kind ?: parseInteractiveRequestKind(task)

        val tasksToResume = when (requestKind) {
            InteractivePendingRequestSnapshot.KIND_CONFIRMATION -> {
                if (InteractiveToolResultFactory.isConfirmationApproved(restored?.payload)) {
                    resumeState.remainingTasks.toMutableList().apply { add(0, task) }
                } else {
                    val rejection = InteractiveToolResultFactory.buildConfirmationRejectedResult(
                        toolName = task.toolName,
                        reason = restored?.confirmationRequest?.dialogMessage
                            ?.takeIf { it.isNotBlank() }
                            ?: appContext.getString(R.string.agent_tool_confirmation_required),
                        gson = gson,
                    )
                    agentLoopExecutor.taskManager.markCompleted(task.id, rejection.content, rejection.isError)
                    resumeState.remainingTasks
                }
            }
            InteractivePendingRequestSnapshot.KIND_QUESTION -> {
                val result = if (restored?.payload.isNullOrBlank()) {
                    InteractiveToolResultFactory.buildQuestionCancelledResult(gson)
                } else {
                    InteractiveToolResultFactory.buildQuestionSubmittedResult(
                        answersJson = restored.payload.orEmpty(),
                        gson = gson,
                    )
                }
                agentLoopExecutor.taskManager.markCompleted(task.id, result.content, result.isError)
                resumeState.remainingTasks
            }
            InteractiveToolRuntime.KIND_LEGACY_FORM -> {
                agentLoopExecutor.taskManager.markFailed(
                    task.id,
                    appContext.getString(R.string.agent_interaction_legacy_form_removed),
                )
                resumeState.remainingTasks
            }
            null -> {
                agentLoopExecutor.taskManager.markFailed(
                    task.id,
                    appContext.getString(R.string.agent_interaction_restore_failed),
                )
                resumeState.remainingTasks
            }
            else -> {
                agentLoopExecutor.taskManager.markFailed(
                    task.id,
                    appContext.getString(R.string.agent_interaction_restore_failed),
                )
                resumeState.remainingTasks
            }
        }

        if (tasksToResume.isNotEmpty()) {
            val approvedTaskIds = buildSet {
                if (
                    requestKind == InteractivePendingRequestSnapshot.KIND_CONFIRMATION &&
                    InteractiveToolResultFactory.isConfirmationApproved(restored?.payload)
                ) {
                    add(task.id)
                }
            }
            onResumeToolTasks(
                tasksToResume,
                task.iteration,
                approvedTaskIds,
                { _, _ -> },
            )
        }

        onResumeLLMFollowUp(task.conversationId)
    }

    private suspend fun handleFromToolResume(
        resumeState: ResumeState.FromTool,
        onResumeToolTasks: suspend (
            tasks: List<ToolCallTaskEntity>,
            iteration: Int,
            skipConfirmationForTaskIds: Set<String>,
            onToolCompleted: (ToolCall, AgentToolResult) -> Unit,
        ) -> Unit,
        onResumeLLMFollowUp: suspend (conversationId: String) -> Unit,
    ) {
        onResumeToolTasks(
            resumeState.pendingTasks,
            resumeState.iteration,
            emptySet(),
            { toolCall, result ->
                val statusText = if (result.isError) {
                    appContext.getString(R.string.agent_tool_error)
                } else {
                    appContext.getString(R.string.agent_tool_success)
                }
                "Resumed ${toolCall.function.name}: $statusText".makeLog("ToolResumeCoordinator")
            },
        )
        val resumeConversationId = resumeState.pendingTasks.firstOrNull()?.conversationId.orEmpty()
        if (resumeConversationId.isNotEmpty()) {
            onResumeLLMFollowUp(resumeConversationId)
        }
    }

    private fun parseInteractiveRequestKind(task: ToolCallTaskEntity): String? {
        if (task.formRequestJson.isNullOrBlank()) return null
        return runCatching {
            gson.fromJson(task.formRequestJson, InteractivePendingRequestSnapshot::class.java)?.kind
        }.getOrNull()
    }
}
