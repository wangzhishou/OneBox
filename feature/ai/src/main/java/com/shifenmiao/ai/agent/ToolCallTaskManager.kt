package com.shifenmiao.ai.agent

import com.shifenmiao.database.ai.dao.ToolCallTaskDao
import com.shifenmiao.database.ai.entity.ToolCallTaskEntity
import com.shifenmiao.model.ai.FunctionCall
import com.shifenmiao.model.ai.ToolCall
import com.t8rin.logger.makeLog
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 工具调用任务管理器 —— 封装 [ToolCallTaskDao]，提供面向业务的持久化操作。
 *
 * 职责：
 * 1. 在 Agent Loop 的每个生命周期节点写入 DB（PENDING → EXECUTING → COMPLETED/FAILED）。
 * 2. App 重启后构建恢复状态（[buildResumeState]），告知 AIChatComponent 从哪里续跑。
 * 3. 将已完成的任务转换为 [ToolCallRecord] 列表，兼容 MessageEntity.toolCalls 序列化。
 * 4. 定期清理过期任务。
 *
 * 线程安全：Room DAO 本身是线程安全的，所有操作都是 suspend 函数。
 */
@Singleton
class ToolCallTaskManager @Inject constructor(
    private val dao: ToolCallTaskDao
) {

    /**
     * 批量持久化新的工具调用任务（从 LLM 刚返回的 tool_calls 构建）。
     * 在 [AgentLoopExecutor.buildCompletedToolCalls] 之后立即调用。
     */
    suspend fun persistNewTasks(
        conversationId: String,
        completionId: String,
        iteration: Int,
        toolCalls: List<ToolCall>
    ) {
        val tasks = toolCalls.mapIndexed { index, toolCall ->
            ToolCallTaskEntity(
                id = toolCall.id,
                conversationId = conversationId,
                completionId = completionId,
                iteration = iteration,
                sequenceIndex = index,
                toolName = toolCall.function.name,
                arguments = toolCall.function.arguments,
                status = ToolCallTaskEntity.Status.PENDING
            )
        }
        dao.upsertAll(tasks)
        "Persisted ${tasks.size} new tool call tasks for conversation=$conversationId, iteration=$iteration"
            .makeLog("ToolCallTaskManager")
    }

    /** 标记任务为执行中 */
    suspend fun markExecuting(taskId: String) {
        dao.markExecuting(taskId)
    }

    /** 标记任务为等待用户输入，并保存表单请求 JSON */
    suspend fun markWaitingInput(taskId: String, formRequestJson: String) {
        dao.markWaitingInput(taskId, formRequestJson)
    }

    /** 标记任务为已完成 */
    suspend fun markCompleted(taskId: String, result: String, isError: Boolean = false) {
        dao.markCompleted(taskId, result, isError)
    }

    /** 标记任务为失败 */
    suspend fun markFailed(taskId: String, error: String) {
        dao.markFailed(taskId, error)
    }

    /** 将某会话所有未完成任务标记为失败（取消/异常时调用） */
    suspend fun failAllIncompleteTasks(conversationId: String, reason: String = "cancelled") {
        dao.failAllIncompleteTasks(conversationId, reason)
    }

    /**
     * 构建恢复状态 —— App 重启后调用，判断是否有需要续跑的任务。
     *
     * 三种恢复场景：
     * 1. 有 WAITING_INPUT 任务 → 恢复交互式表单 UI
     * 2. 有 PENDING/EXECUTING 任务 → 重新执行未完成的工具调用
     * 3. 没有未完成任务，但有已完成任务记录 → 工具全部执行完毕但 LLM 后续请求未发送
     *
     * @return null 表示无需恢复；非 null 表示需要从指定位置恢复执行
     */
    suspend fun buildResumeState(conversationId: String): ResumeState? {
        val incompleteTasks = dao.getIncompleteTasksOnce(conversationId)

        // 场景 1 & 2：有未完成的任务
        if (incompleteTasks.isNotEmpty()) {
            // 查找是否有正在等待用户输入的任务（优先恢复）
            val waitingInputTask = incompleteTasks.firstOrNull {
                it.status == ToolCallTaskEntity.Status.WAITING_INPUT
            }
            if (waitingInputTask != null) {
                "Resume state: WAITING_INPUT for task ${waitingInputTask.id}".makeLog("ToolCallTaskManager")
                return ResumeState.WaitingInput(
                    task = waitingInputTask,
                    remainingTasks = incompleteTasks.filter { it.id != waitingInputTask.id }
                )
            }

            // 否则从第一个未完成任务的迭代轮次开始恢复
            val firstPending = incompleteTasks.first()
            "Resume state: FromTool at iteration=${firstPending.iteration}, ${incompleteTasks.size} tasks pending"
                .makeLog("ToolCallTaskManager")
            return ResumeState.FromTool(
                iteration = firstPending.iteration,
                pendingTasks = incompleteTasks
            )
        }

        // 场景 3：所有任务已完成，但记录仍存在（说明 Agent Loop 未正常结束）
        val completedTasks = dao.getCompletedTasks(conversationId)
        if (completedTasks.isNotEmpty()) {
            "Resume state: CompletedNeedsLLM, ${completedTasks.size} completed tasks found"
                .makeLog("ToolCallTaskManager")
            return ResumeState.CompletedNeedsLLM(
                completedTasks = completedTasks
            )
        }

        return null
    }

    /**
     * 获取某会话已完成的任务，并按 iteration + sequence_index 排序。
     */
    suspend fun getCompletedTasks(conversationId: String): List<ToolCallTaskEntity> {
        return dao.getCompletedTasks(conversationId)
    }

    /**
     * 将已完成的任务转换为 [ToolCallRecord] 列表，
     * 兼容已有的 MessageEntity.toolCalls JSON 序列化格式。
     */
    suspend fun toToolCallRecords(conversationId: String): List<ToolCallRecord> {
        return dao.getCompletedTasks(conversationId).map { task ->
            ToolCallRecord(
                id = task.id,
                name = task.toolName,
                arguments = task.arguments,
                result = task.result ?: "",
                isError = task.isError
            )
        }
    }

    /**
     * 将 [ToolCallTaskEntity] 列表转换为 [ToolCall] 列表，
     * 用于恢复执行时重建 ToolCall 对象。
     */
    fun toToolCalls(tasks: List<ToolCallTaskEntity>): List<ToolCall> {
        return tasks.map { task ->
            ToolCall(
                id = task.id,
                type = "function",
                function = FunctionCall(
                    name = task.toolName,
                    arguments = task.arguments
                )
            )
        }
    }

    /** 删除某会话的所有工具调用任务 */
    suspend fun deleteByConversation(conversationId: String) {
        dao.deleteByConversation(conversationId)
    }

    /** 清理 24 小时前已完成的任务 */
    suspend fun cleanupOldTasks() {
        val cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        dao.deleteOldCompletedTasks(cutoff)
    }
}

/**
 * 恢复状态 —— 描述 App 重启后需要从哪里继续执行。
 */
sealed class ResumeState {
    /**
     * 需要从指定迭代轮次的未完成工具开始恢复执行。
     */
    data class FromTool(
        val iteration: Int,
        val pendingTasks: List<ToolCallTaskEntity>
    ) : ResumeState()

    /**
     * 有一个交互式工具正在等待用户输入，需要恢复表单 UI。
     */
    data class WaitingInput(
        val task: ToolCallTaskEntity,
        val remainingTasks: List<ToolCallTaskEntity>
    ) : ResumeState()

    /**
     * 所有工具已执行完毕，但 LLM 后续请求尚未发送。
     * App 重启后需要重新构建上下文并发起 LLM 请求。
     */
    data class CompletedNeedsLLM(
        val completedTasks: List<ToolCallTaskEntity>
    ) : ResumeState()
}
