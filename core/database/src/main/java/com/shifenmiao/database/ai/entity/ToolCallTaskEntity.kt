package com.shifenmiao.database.ai.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 工具调用任务持久化实体 —— 每一次 LLM 发起的 tool_call 对应一行。
 *
 * 设计目标：
 * - 在 LLM 返回 tool_calls 后立即写入 DB（状态 PENDING），确保不丢失。
 * - 执行过程中实时更新状态（EXECUTING → WAITING_INPUT → COMPLETED/FAILED）。
 * - App 重启后通过查询未完成任务实现断点续跑。
 * - 交互式工具的表单请求数据（formRequestJson）也持久化，
 *   即使进程被杀也能恢复表单 UI。
 */
@Entity(
    tableName = "tool_call_task",
    indices = [
        Index(value = ["conversation_id"]),
        Index(value = ["conversation_id", "status"])
    ]
)
data class ToolCallTaskEntity(
    /** ToolCall.id，由 LLM 生成的唯一标识 */
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    /** 所属会话 ID */
    @ColumnInfo(name = "conversation_id")
    val conversationId: String,

    /** 关联的 completionId（对应 MessageEntity.completionId），用于关联到消息 */
    @ColumnInfo(name = "completion_id")
    val completionId: String = "",

    /** Agent Loop 迭代轮次（从 1 开始） */
    @ColumnInfo(name = "iteration")
    val iteration: Int = 1,

    /** 本轮批次内的序号（一次 LLM 可能返回多个 tool_calls） */
    @ColumnInfo(name = "sequence_index")
    val sequenceIndex: Int = 0,

    /** 工具名称（对应 ToolCall.function.name） */
    @ColumnInfo(name = "tool_name")
    val toolName: String,

    /** LLM 传入的 JSON 参数（对应 ToolCall.function.arguments） */
    @ColumnInfo(name = "arguments")
    val arguments: String = "{}",

    /** 当前执行状态 */
    @ColumnInfo(name = "status")
    val status: String = Status.PENDING,

    /** 工具执行结果文本 */
    @ColumnInfo(name = "result")
    val result: String? = null,

    /** 是否为错误结果 */
    @ColumnInfo(name = "is_error")
    val isError: Boolean = false,

    /**
     * 交互式工具的等待输入快照 JSON。
     * 仅 status == WAITING_INPUT 时有值，用于进程恢复后重建确认或提问 UI。
     */
    @ColumnInfo(name = "form_request_json")
    val formRequestJson: String? = null,

    /** 创建时间戳（毫秒） */
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    /** 最后更新时间戳（毫秒） */
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 工具调用任务状态枚举。
     */
    object Status {
        /** 已入库，等待执行 */
        const val PENDING = "PENDING"

        /** 正在执行中 */
        const val EXECUTING = "EXECUTING"

        /** 交互式工具，等待用户输入 */
        const val WAITING_INPUT = "WAITING_INPUT"

        /** 执行完成（成功或有结果） */
        const val COMPLETED = "COMPLETED"

        /** 执行失败或已取消 */
        const val FAILED = "FAILED"

        /** 所有"未完成"状态的集合 */
        val INCOMPLETE_STATUSES = listOf(PENDING, EXECUTING, WAITING_INPUT)
    }
}
