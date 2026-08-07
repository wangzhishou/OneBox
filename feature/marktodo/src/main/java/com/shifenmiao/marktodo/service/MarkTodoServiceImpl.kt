package com.shifenmiao.marktodo.service

import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.marktodo.entity.MarkTodoCategoryEntity
import com.shifenmiao.database.marktodo.entity.MarkTodoTaskEntity
import com.shifenmiao.database.marktodo.repo.MarkTodoRepository
import com.shifenmiao.marktodo.data.iconFromKey
import com.shifenmiao.marktodo.data.toModel
import com.shifenmiao.marktodo.model.TodoCategory
import com.shifenmiao.marktodo.model.TodoTask
import com.shifenmiao.model.todo.CategoryInput
import com.shifenmiao.model.todo.CategoryLookup
import com.shifenmiao.model.todo.MarkTodoServiceInterface
import com.shifenmiao.model.todo.TaskInput
import com.shifenmiao.model.todo.TodoCategoryDto
import com.shifenmiao.model.todo.TodoTaskDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 待办清单业务门面 — UI 层与 Agent 层共用的唯一写入入口。
 *
 * 职责:
 *  - 入参校验
 *  - 调用 [MarkTodoRepository] 写库
 *  - 调用 [ActivityLogRecorder] 写审计日志
 *
 * 不做的事:
 *  - 不持有 UI 状态
 *  - 不暴露 Flow（订阅仍走 Repository，Component 直接 inject Repository 用作只读）
 */
@Singleton
class MarkTodoServiceImpl @Inject constructor(
    private val repository: MarkTodoRepository,
    private val activityLogRecorder: ActivityLogRecorder,
) : MarkTodoServiceInterface {

    // ── 分类操作 ────────────────────────────────────

    override suspend fun createCategory(
        input: CategoryInput,
        source: String,
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val categoryId = UUID.randomUUID().toString()
            val entity = MarkTodoCategoryEntity(
                id = categoryId,
                title = input.title.trim(),
                iconKey = input.iconKey,
                sortOrder = input.sortOrder,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
            repository.upsertCategory(entity)

            activityLogRecorder.recordMarkTodo(
                entityId = categoryId,
                entityType = "CATEGORY",
                actionType = "CREATE",
                source = source,
                title = "新增分类: ${input.title}",
                description = "创建了待办分类「${input.title}」"
            )
            categoryId
        }
    }

    override suspend fun updateCategory(
        categoryId: String,
        input: CategoryInput,
        source: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val entity = MarkTodoCategoryEntity(
                id = categoryId,
                title = input.title.trim(),
                iconKey = input.iconKey,
                sortOrder = input.sortOrder,
                updatedAt = System.currentTimeMillis(),
            )
            repository.upsertCategory(entity)

            activityLogRecorder.recordMarkTodo(
                entityId = categoryId,
                entityType = "CATEGORY",
                actionType = "UPDATE",
                source = source,
                title = "编辑分类: ${input.title}",
                description = "更新了待办分类「${input.title}」"
            )
        }
    }

    override suspend fun deleteCategory(
        categoryId: String,
        categoryTitle: String,
        source: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            repository.deleteCategory(categoryId)

            activityLogRecorder.recordMarkTodo(
                entityId = categoryId,
                entityType = "CATEGORY",
                actionType = "DELETE",
                source = source,
                title = "删除分类: $categoryTitle",
                description = "删除了待办分类「$categoryTitle」及其所有任务"
            )
        }
    }

    override suspend fun reorderCategories(
        orderedIds: List<String>,
        source: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            orderedIds.forEachIndexed { index, categoryId ->
                repository.updateCategoryOrder(categoryId, index)
            }

            activityLogRecorder.recordMarkTodo(
                entityId = "batch",
                entityType = "CATEGORY",
                actionType = "REORDER",
                source = source,
                title = "重排分类",
                description = "调整了 ${orderedIds.size} 个分类的排序"
            )
        }
    }

    // ── 任务操作 ────────────────────────────────────

    override suspend fun createTask(
        input: TaskInput,
        source: String,
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val taskId = UUID.randomUUID().toString()
            val entity = MarkTodoTaskEntity(
                id = taskId,
                categoryId = input.categoryId,
                title = input.title.trim(),
                note = input.note?.trim()?.takeIf { it.isNotBlank() },
                startDate = System.currentTimeMillis(),
                dueDate = input.dueDateMillis,
                tags = input.tags,
                isCompleted = false,
                isStarred = false,
                sortOrder = (repository.getDashboard()
                    .flatMap { it.tasks }
                    .maxOfOrNull { it.sortOrder } ?: -1) + 1,
            )
            repository.addTask(entity)

            activityLogRecorder.recordMarkTodo(
                entityId = taskId,
                entityType = "TASK",
                actionType = "CREATE",
                source = source,
                title = "新增待办: ${input.title}",
                description = "在分类中创建了待办「${input.title}」"
            )
            taskId
        }
    }

    override suspend fun updateTask(
        taskId: String,
        input: TaskInput,
        source: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val entity = MarkTodoTaskEntity(
                id = taskId,
                categoryId = input.categoryId,
                title = input.title.trim(),
                note = input.note?.trim()?.takeIf { it.isNotBlank() },
                startDate = System.currentTimeMillis(),
                dueDate = input.dueDateMillis,
                tags = input.tags,
                updatedAt = System.currentTimeMillis(),
            )
            repository.updateTask(entity)

            activityLogRecorder.recordMarkTodo(
                entityId = taskId,
                entityType = "TASK",
                actionType = "UPDATE",
                source = source,
                title = "编辑待办: ${input.title}",
                description = "更新了待办「${input.title}」"
            )
        }
    }

    override suspend fun deleteTask(
        taskId: String,
        taskTitle: String,
        source: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            repository.deleteTask(taskId)

            activityLogRecorder.recordMarkTodo(
                entityId = taskId,
                entityType = "TASK",
                actionType = "DELETE",
                source = source,
                title = "删除待办: $taskTitle",
                description = "删除了待办「$taskTitle」"
            )
        }
    }

    override suspend fun toggleTaskComplete(
        taskId: String,
        isCompleted: Boolean,
        taskTitle: String,
        source: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            repository.setTaskCompleted(taskId, isCompleted)

            activityLogRecorder.recordMarkTodo(
                entityId = taskId,
                entityType = "TASK",
                actionType = "TOGGLE_COMPLETE",
                source = source,
                title = if (isCompleted) "完成待办: $taskTitle" else "重新打开: $taskTitle",
                description = if (isCompleted) "标记待办「$taskTitle」为已完成"
                else "重新打开待办「$taskTitle》"
            )
        }
    }

    override suspend fun toggleTaskStar(
        taskId: String,
        isStarred: Boolean,
        taskTitle: String,
        source: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            repository.setTaskStarred(taskId, isStarred)

            activityLogRecorder.recordMarkTodo(
                entityId = taskId,
                entityType = "TASK",
                actionType = "TOGGLE_STAR",
                source = source,
                title = if (isStarred) "收藏待办: $taskTitle" else "取消收藏: $taskTitle",
                description = if (isStarred) "收藏了待办「$taskTitle》"
                else "取消收藏待办「$taskTitle》"
            )
        }
    }

    override suspend fun reorderTasks(
        categoryId: String,
        orderedIds: List<String>,
        source: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            orderedIds.forEachIndexed { index, taskId ->
                repository.updateTaskOrder(taskId, index)
            }

            activityLogRecorder.recordMarkTodo(
                entityId = categoryId,
                entityType = "TASK",
                actionType = "REORDER",
                source = source,
                title = "重排任务",
                description = "调整了 ${orderedIds.size} 个任务的排序"
            )
        }
    }

    // ── 只读查询（供 Component 与 Agent 使用）────────

    suspend fun getDashboard(): List<TodoCategory> = withContext(Dispatchers.IO) {
        repository.getDashboard().map { rel ->
            val safeIconKey = rel.category.iconKey.ifBlank { "inbox" }
            rel.category.toModel(
                icon = iconFromKey(safeIconKey),
                tasks = rel.tasks.map { it.toModel() }
            )
        }
    }

    suspend fun getCategoryWithTasks(categoryId: String): TodoCategory? = withContext(Dispatchers.IO) {
        repository.getCategoryWithTasks(categoryId)?.let { rel ->
            val safeIconKey = rel.category.iconKey.ifBlank { "inbox" }
            rel.category.toModel(
                icon = iconFromKey(safeIconKey),
                tasks = rel.tasks.map { it.toModel() }
            )
        }
    }

    suspend fun getTask(taskId: String): TodoTask? = withContext(Dispatchers.IO) {
        repository.getDashboard()
            .flatMap { it.tasks }
            .find { it.id == taskId }
            ?.toModel()
    }

    // ── DTO 读操作（实现接口，供 AgentTool 使用）────────

    override suspend fun getDashboardDto(): List<TodoCategoryDto> = withContext(Dispatchers.IO) {
        repository.getDashboard().map { rel ->
            TodoCategoryDto(
                id = rel.category.id,
                title = rel.category.title,
                iconKey = rel.category.iconKey.ifBlank { "inbox" },
                tasks = rel.tasks.map { task ->
                    TodoTaskDto(
                        id = task.id,
                        title = task.title,
                        note = task.note,
                        startDate = task.startDate,
                        dueDate = task.dueDate,
                        tags = task.tags,
                        isCompleted = task.isCompleted,
                        isStarred = task.isStarred
                    )
                }
            )
        }
    }

    override suspend fun getTaskDto(taskId: String): TodoTaskDto? = withContext(Dispatchers.IO) {
        repository.getDashboard()
            .flatMap { it.tasks }
            .find { it.id == taskId }
            ?.let { task ->
                TodoTaskDto(
                    id = task.id,
                    title = task.title,
                    note = task.note,
                    startDate = task.startDate,
                    dueDate = task.dueDate,
                    tags = task.tags,
                    isCompleted = task.isCompleted,
                    isStarred = task.isStarred
                )
            }
    }

    override suspend fun findCategoryByTitle(title: String): CategoryLookup? = withContext(Dispatchers.IO) {
        repository.getDashboard()
            .find { it.category.title.equals(title, ignoreCase = true) }
            ?.let { CategoryLookup(id = it.category.id, title = it.category.title) }
    }
}
