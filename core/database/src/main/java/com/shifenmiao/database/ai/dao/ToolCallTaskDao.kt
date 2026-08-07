package com.shifenmiao.database.ai.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.ai.entity.ToolCallTaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * 工具调用任务 DAO —— 提供工具调用链的增删改查操作。
 *
 * 核心查询：
 * - [getIncompleteTasks]: 返回 Flow，用于 App 重启后轮询未完成任务并恢复执行。
 * - [getTasksByConversation]: 获取某会话的完整工具调用链，用于序列化到 MessageEntity。
 */
@Dao
interface ToolCallTaskDao {

    /** 插入或替换单个任务（幂等写入） */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: ToolCallTaskEntity)

    /** 批量插入任务（一次 LLM 可能返回多个 tool_calls） */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tasks: List<ToolCallTaskEntity>)

    /** 按 ID 查询单个任务 */
    @Query("SELECT * FROM tool_call_task WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): ToolCallTaskEntity?

    /**
     * 查询某会话下所有未完成的任务（PENDING / EXECUTING / WAITING_INPUT），
     * 按 iteration + sequence_index 排序。
     * 返回 Flow 以支持响应式观察。
     */
    @Query("""
        SELECT * FROM tool_call_task 
        WHERE conversation_id = :conversationId 
          AND status IN ('PENDING', 'EXECUTING', 'WAITING_INPUT')
        ORDER BY iteration ASC, sequence_index ASC
    """)
    fun getIncompleteTasks(conversationId: String): Flow<List<ToolCallTaskEntity>>

    /**
     * 一次性查询某会话下所有未完成的任务（非 Flow 版本，用于恢复逻辑）。
     */
    @Query("""
        SELECT * FROM tool_call_task 
        WHERE conversation_id = :conversationId 
          AND status IN ('PENDING', 'EXECUTING', 'WAITING_INPUT')
        ORDER BY iteration ASC, sequence_index ASC
    """)
    suspend fun getIncompleteTasksOnce(conversationId: String): List<ToolCallTaskEntity>

    /**
     * 查询某会话下所有任务（含已完成），按执行顺序排列。
     * 用于序列化完整工具调用链到 MessageEntity.toolCalls。
     */
    @Query("""
        SELECT * FROM tool_call_task 
        WHERE conversation_id = :conversationId 
        ORDER BY iteration ASC, sequence_index ASC
    """)
    suspend fun getTasksByConversation(conversationId: String): List<ToolCallTaskEntity>

    /**
     * 查询某会话下所有已完成的任务。
     */
    @Query("""
        SELECT * FROM tool_call_task 
        WHERE conversation_id = :conversationId 
          AND status = 'COMPLETED'
        ORDER BY iteration ASC, sequence_index ASC
    """)
    suspend fun getCompletedTasks(conversationId: String): List<ToolCallTaskEntity>

    /**
     * 原子更新任务状态为 EXECUTING。
     */
    @Query("""
        UPDATE tool_call_task 
        SET status = 'EXECUTING', updated_at = :now 
        WHERE id = :taskId
    """)
    suspend fun markExecuting(taskId: String, now: Long = System.currentTimeMillis())

    /**
     * 原子更新任务状态为 WAITING_INPUT，并保存表单请求 JSON。
     */
    @Query("""
        UPDATE tool_call_task 
        SET status = 'WAITING_INPUT', form_request_json = :formJson, updated_at = :now 
        WHERE id = :taskId
    """)
    suspend fun markWaitingInput(
        taskId: String,
        formJson: String,
        now: Long = System.currentTimeMillis()
    )

    /**
     * 原子更新任务状态为 COMPLETED，写入执行结果。
     */
    @Query("""
        UPDATE tool_call_task 
        SET status = 'COMPLETED', result = :result, is_error = :isError, updated_at = :now 
        WHERE id = :taskId
    """)
    suspend fun markCompleted(
        taskId: String,
        result: String,
        isError: Boolean = false,
        now: Long = System.currentTimeMillis()
    )

    /**
     * 原子更新任务状态为 FAILED。
     */
    @Query("""
        UPDATE tool_call_task 
        SET status = 'FAILED', result = :error, is_error = 1, updated_at = :now 
        WHERE id = :taskId
    """)
    suspend fun markFailed(
        taskId: String,
        error: String,
        now: Long = System.currentTimeMillis()
    )

    /**
     * 批量将某会话所有未完成任务标记为 FAILED（用于取消或异常清理）。
     */
    @Query("""
        UPDATE tool_call_task 
        SET status = 'FAILED', result = :reason, is_error = 1, updated_at = :now 
        WHERE conversation_id = :conversationId 
          AND status IN ('PENDING', 'EXECUTING', 'WAITING_INPUT')
    """)
    suspend fun failAllIncompleteTasks(
        conversationId: String,
        reason: String = "cancelled",
        now: Long = System.currentTimeMillis()
    )

    /** 删除某会话的所有工具调用任务（清理） */
    @Query("DELETE FROM tool_call_task WHERE conversation_id = :conversationId")
    suspend fun deleteByConversation(conversationId: String)

    /** 删除所有已完成且超过指定时间的任务（定期清理） */
    @Query("""
        DELETE FROM tool_call_task 
        WHERE status IN ('COMPLETED', 'FAILED') 
          AND updated_at < :beforeTimestamp
    """)
    suspend fun deleteOldCompletedTasks(beforeTimestamp: Long)
}

