package com.shifenmiao.model.todo

/**
 * 待办清单 Service 的写入参数与结果模型。
 *
 * 这些类型由 Service 接口引用，供 UI 层和 AgentTool 层共用。
 */

// ── 写入参数 ─────────────────────────────────────────

data class CategoryInput(
    val title: String,
    val iconKey: String,
    val sortOrder: Int = 0,
)

data class TaskInput(
    val categoryId: String,
    val title: String,
    val note: String?,
    val dueDateMillis: Long?,
    val tags: List<String>,
)

// ── 读取 DTO（供 AgentTool 使用，不依赖 Compose） ──────────

data class CategoryLookup(
    val id: String,
    val title: String,
)

data class TodoCategoryDto(
    val id: String,
    val title: String,
    val iconKey: String,
    val tasks: List<TodoTaskDto>,
)

data class TodoTaskDto(
    val id: String,
    val title: String,
    val note: String?,
    val startDate: Long,
    val dueDate: Long?,
    val tags: List<String>,
    val isCompleted: Boolean,
    val isStarred: Boolean,
)
