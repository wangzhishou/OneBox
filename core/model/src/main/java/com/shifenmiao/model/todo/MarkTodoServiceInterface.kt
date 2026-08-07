package com.shifenmiao.model.todo

/**
 * 待办清单业务 Service 接口。
 *
 * 供 UI Component 层与 AI AgentTool 层共同依赖的契约。
 * 实现类位于 feature/marktodo/service/。
 *
 * 职责:
 *  - 封装所有写操作（创建/更新/删除/重排/完成/收藏）
 *  - 提供只读查询（供 Agent 使用）
 */
interface MarkTodoServiceInterface {

    // ── 读操作（供 AgentTool 使用） ──────────────────────

    suspend fun getDashboardDto(): List<TodoCategoryDto>

    suspend fun getTaskDto(taskId: String): TodoTaskDto?

    suspend fun findCategoryByTitle(title: String): CategoryLookup?

    // ── 分类操作 ────────────────────────────────────

    suspend fun createCategory(
        input: CategoryInput,
        source: String,
    ): Result<String>

    suspend fun updateCategory(
        categoryId: String,
        input: CategoryInput,
        source: String,
    ): Result<Unit>

    suspend fun deleteCategory(
        categoryId: String,
        categoryTitle: String,
        source: String,
    ): Result<Unit>

    suspend fun reorderCategories(
        orderedIds: List<String>,
        source: String,
    ): Result<Unit>

    // ── 任务操作 ────────────────────────────────────

    suspend fun createTask(
        input: TaskInput,
        source: String,
    ): Result<String>

    suspend fun updateTask(
        taskId: String,
        input: TaskInput,
        source: String,
    ): Result<Unit>

    suspend fun deleteTask(
        taskId: String,
        taskTitle: String,
        source: String,
    ): Result<Unit>

    suspend fun toggleTaskComplete(
        taskId: String,
        isCompleted: Boolean,
        taskTitle: String,
        source: String,
    ): Result<Unit>

    suspend fun toggleTaskStar(
        taskId: String,
        isStarred: Boolean,
        taskTitle: String,
        source: String,
    ): Result<Unit>

    suspend fun reorderTasks(
        categoryId: String,
        orderedIds: List<String>,
        source: String,
    ): Result<Unit>
}
