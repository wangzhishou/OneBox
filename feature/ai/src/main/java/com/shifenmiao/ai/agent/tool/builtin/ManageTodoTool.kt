package com.shifenmiao.ai.agent.tool.builtin

import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.R
import com.shifenmiao.model.jsonStringOf
import com.shifenmiao.common.handle.navigation.AppNavigationTargetType
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.model.todo.CategoryInput
import com.shifenmiao.model.todo.CategoryLookup
import com.shifenmiao.model.todo.MarkTodoServiceInterface
import com.shifenmiao.model.todo.TaskInput
import com.shifenmiao.model.todo.TodoCategoryDto
import com.shifenmiao.model.todo.TodoTaskDto
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

class ManageTodoTool @Inject constructor(
    private val todoService: MarkTodoServiceInterface,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "manage_todo"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_manage_todo)

    override val title: String =
        textProvider.string(R.string.agent_tool_manage_todo_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_manage_todo_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_todo_param_action),
                enum = listOf(
                    "create_list", "create_task", "create_category",
                    "list", "toggle_complete", "toggle_star", "delete_task"
                )
            ),
            "category_name" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_todo_param_category_name)
            ),
            "tasks" to ToolParameterProperty(
                type = "array",
                description = textProvider.string(R.string.agent_tool_manage_todo_param_tasks),
                items = ToolParameterProperty(type = "string")
            ),
            "title" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_todo_param_title)
            ),
            "note" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_todo_param_note)
            ),
            "due_date_millis" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_manage_todo_param_due_date_millis)
            ),
            "has_due_time" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_manage_todo_param_has_due_time)
            ),
            "tags" to ToolParameterProperty(
                type = "array",
                description = textProvider.string(R.string.agent_tool_manage_todo_param_tags),
                items = ToolParameterProperty(type = "string")
            ),
            "icon_key" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_todo_param_icon_key)
            ),
            "task_id" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_todo_param_task_id)
            ),
        ),
        required = listOf("action")
    )

    override fun shouldRequireConfirmation(arguments: String): Boolean {
        val action = runCatching {
            AppJson.decodeFromString<ManageTodoParams>(arguments).action
        }.getOrNull() ?: return false
        return normalizeAction(action) == "delete_task"
    }

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = AppJson.decodeFromString<ManageTodoParams>(arguments)
            when (normalizeAction(params.action)) {
                "list" -> handleList(params)
                "create_list" -> handleCreateList(params)
                "create_task" -> handleCreateTask(params)
                "create_category" -> handleCreateCategory(params)
                "toggle_complete" -> handleToggleComplete(params)
                "toggle_star" -> handleToggleStar(params)
                "delete_task" -> handleDeleteTask(params)
                else -> AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_manage_todo_unknown_action,
                        params.action
                    ),
                    isError = true
                )
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_manage_todo_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    /**
     * 模型常按"用户怎么说"编动作名(create_list / add_task / new_category…),这里收敛成真实动作。
     * 实测:mimo 收到"帮我建一个清单"后直接调 `create_list`,而工具当时只认 create_category,
     * 于是整轮以「未知操作」告终 —— 提示词与别名都要兜住这一类叫法。
     */
    private fun normalizeAction(raw: String): String {
        val key = raw.trim().lowercase().replace('-', '_').replace(' ', '_')
        return when (key) {
            "create_list", "add_list", "new_list", "make_list", "create_checklist",
            "add_checklist", "create_todo_list", "create_list_with_tasks" -> "create_list"
            "create_task", "add_task", "new_task", "add_todo", "create_todo", "new_todo" -> "create_task"
            "create_category", "add_category", "new_category", "create_topic", "add_topic" -> "create_category"
            "list", "list_all", "get_list", "query", "get", "read" -> "list"
            "toggle_complete", "complete", "complete_task", "finish", "finish_task",
            "mark_complete", "mark_done", "reopen", "uncomplete", "toggle_done" -> "toggle_complete"
            "toggle_star", "star", "unstar", "favorite", "toggle_favorite", "pin" -> "toggle_star"
            "delete_task", "remove_task", "delete_todo", "remove" -> "delete_task"
            else -> key
        }
    }

    /**
     * 一次建好分类 + 其中全部条目 —— "帮我建一个 XX 清单"的唯一入口。
     * 分类名重名时复用已有分类(不新建),条目逐条写;任一条失败不影响其余条。
     */
    private suspend fun handleCreateList(params: ManageTodoParams): AgentToolResult {
        val listTitle = params.title?.takeIf { it.isNotBlank() }
            ?: return errorResult(R.string.agent_tool_manage_todo_missing_title)
        val taskTitles = (params.tasks ?: emptyList())
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (taskTitles.isEmpty()) {
            return errorResult(R.string.agent_tool_manage_todo_missing_tasks)
        }

        val existing = todoService.findCategoryByTitle(listTitle)
        val categoryId = existing?.id ?: todoService.createCategory(
            CategoryInput(
                title = listTitle,
                iconKey = params.icon_key ?: "Checklist",
                sortOrder = 0
            ),
            source = SOURCE_AGENT
        ).getOrElse { return errorResult(R.string.agent_tool_manage_todo_failed, it.message ?: "unknown") }

        val created = mutableListOf<String>()
        val failed = mutableListOf<String>()
        taskTitles.forEach { title ->
            val tags = params.tags ?: emptyList()
            todoService.createTask(
                TaskInput(
                    categoryId = categoryId,
                    title = title,
                    note = null,
                    dueDateMillis = params.due_date_millis,
                    tags = tags,
                    hasDueTime = params.has_due_time == true,
                ),
                source = SOURCE_AGENT
            ).onSuccess { created += title }.onFailure { failed += title }
        }

        val result = mapOf(
            "action" to "create_list",
            "success" to failed.isEmpty(),
            "category" to listTitle,
            "category_id" to categoryId,
            "created" to created,
            "failed" to failed,
            "message" to textProvider.string(
                R.string.agent_tool_manage_todo_list_created,
                listTitle,
                created.size
            ),
            "deeplink" to markTodoDeeplink("category_id" to categoryId)
        )
        return AgentToolResult(content = jsonStringOf(result), isError = failed.isNotEmpty())
    }

    // ── action handlers ──────────────────────────────

    private suspend fun handleList(params: ManageTodoParams): AgentToolResult {
        val dashboard = todoService.getDashboardDto()
        val filtered = if (!params.category_name.isNullOrBlank()) {
            dashboard.filter { it.title.equals(params.category_name, ignoreCase = true) }
        } else {
            dashboard
        }
        val inboxTasks = if (params.category_name.isNullOrBlank()) {
            todoService.getInboxTasksDto()
        } else {
            emptyList()
        }
        val result = mapOf(
            "action" to "list",
            "success" to true,
            "categories" to filtered.map { it.toMap() },
            "inbox_tasks" to inboxTasks.map { it.toMap() },
            "deeplink" to markTodoDeeplink()
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun handleCreateTask(params: ManageTodoParams): AgentToolResult {
        val catName = params.category_name?.trim()?.takeIf { it.isNotEmpty() }
        val taskTitle = params.title
        if (taskTitle.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_todo_missing_title)
        }
        val lookup = catName?.let { categoryName ->
            todoService.findCategoryByTitle(categoryName) ?: run {
                val newId = todoService.createCategory(
                    CategoryInput(title = categoryName, iconKey = "Checklist", sortOrder = 0),
                    source = SOURCE_AGENT
                ).getOrElse { return errorResult(R.string.agent_tool_manage_todo_failed, it.message ?: "unknown") }
                CategoryLookup(id = newId, title = categoryName)
            }
        }

        val input = TaskInput(
            categoryId = lookup?.id,
            title = taskTitle,
            note = params.note,
            dueDateMillis = params.due_date_millis,
            tags = params.tags ?: emptyList(),
            hasDueTime = params.has_due_time == true,
        )
        todoService.createTask(input, source = SOURCE_AGENT)
            .getOrElse { return errorResult(R.string.agent_tool_manage_todo_failed, it.message ?: "unknown") }

        val result = mapOf(
            "action" to "create_task",
            "success" to true,
            "category" to lookup?.title,
            "is_inbox" to (lookup == null),
            "task_title" to taskTitle,
            "message" to textProvider.string(R.string.agent_tool_manage_todo_task_created, taskTitle),
            "deeplink" to (lookup?.let { markTodoDeeplink("category_id" to it.id) } ?: markTodoDeeplink())
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun handleCreateCategory(params: ManageTodoParams): AgentToolResult {
        val catTitle = params.title
        if (catTitle.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_todo_missing_title)
        }
        val input = CategoryInput(
            title = catTitle,
            iconKey = params.icon_key ?: "inbox",
            sortOrder = 0
        )
        todoService.createCategory(input, source = SOURCE_AGENT)
            .getOrElse { return errorResult(R.string.agent_tool_manage_todo_failed, it.message ?: "unknown") }

        val result = mapOf(
            "action" to "create_category",
            "success" to true,
            "category_title" to catTitle,
            "message" to textProvider.string(R.string.agent_tool_manage_todo_category_created, catTitle),
            "deeplink" to markTodoDeeplink()
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun handleToggleComplete(params: ManageTodoParams): AgentToolResult {
        val taskId = params.task_id
        if (taskId.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_todo_missing_task_id)
        }
        val task = todoService.getTaskDto(taskId)
            ?: return errorResult(R.string.agent_tool_manage_todo_task_not_found, taskId)

        todoService.toggleTaskComplete(
            taskId = taskId,
            isCompleted = !task.isCompleted,
            taskTitle = task.title,
            source = SOURCE_AGENT
        ).getOrElse { return errorResult(R.string.agent_tool_manage_todo_failed, it.message ?: "unknown") }

        val newStatus = if (task.isCompleted) "reopened" else "completed"
        val result = mapOf(
            "action" to "toggle_complete",
            "success" to true,
            "task_title" to task.title,
            "status" to newStatus,
            "message" to textProvider.string(R.string.agent_tool_manage_todo_toggled, task.title, newStatus),
            "deeplink" to markTodoDeeplink()
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun handleToggleStar(params: ManageTodoParams): AgentToolResult {
        val taskId = params.task_id
        if (taskId.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_todo_missing_task_id)
        }
        val task = todoService.getTaskDto(taskId)
            ?: return errorResult(R.string.agent_tool_manage_todo_task_not_found, taskId)

        todoService.toggleTaskStar(
            taskId = taskId,
            isStarred = !task.isStarred,
            taskTitle = task.title,
            source = SOURCE_AGENT
        ).getOrElse { return errorResult(R.string.agent_tool_manage_todo_failed, it.message ?: "unknown") }

        val newStatus = if (task.isStarred) "unstarred" else "starred"
        val result = mapOf(
            "action" to "toggle_star",
            "success" to true,
            "task_title" to task.title,
            "status" to newStatus,
            "message" to textProvider.string(R.string.agent_tool_manage_todo_toggled, task.title, newStatus),
            "deeplink" to markTodoDeeplink()
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun handleDeleteTask(params: ManageTodoParams): AgentToolResult {
        val taskId = params.task_id
        if (taskId.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_todo_missing_task_id)
        }
        val task = todoService.getTaskDto(taskId)
            ?: return errorResult(R.string.agent_tool_manage_todo_task_not_found, taskId)

        todoService.deleteTask(
            taskId = taskId,
            taskTitle = task.title,
            source = SOURCE_AGENT
        ).getOrElse { return errorResult(R.string.agent_tool_manage_todo_failed, it.message ?: "unknown") }

        val result = mapOf(
            "action" to "delete_task",
            "success" to true,
            "task_title" to task.title,
            "message" to textProvider.string(R.string.agent_tool_manage_todo_task_deleted, task.title),
            "deeplink" to markTodoDeeplink()
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    // ── helpers ───────────────────────────────────

    private fun errorResult(resId: Int, vararg args: Any): AgentToolResult =
        AgentToolResult(content = textProvider.string(resId, *args), isError = true)

    private fun markTodoDeeplink(vararg extraParams: Pair<String, String>): String =
        AppNavigationRegistry.buildStructuredDeeplink(
            targetType = AppNavigationTargetType.SCREEN,
            routeKey = "mark_todo_router",
            params = extraParams.toMap()
        )

    private fun TodoCategoryDto.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "title" to title,
        "icon_key" to iconKey,
        "task_count" to tasks.size,
        "completed_count" to tasks.count { it.isCompleted },
        "tasks" to tasks.map { it.toMap() }
    )

    private fun TodoTaskDto.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "category_id" to categoryId,
        "title" to title,
        "note" to note,
        "due_date" to dueDate,
        "has_due_time" to hasDueTime,
        "created_at" to createdAt,
        "tags" to tags,
        "is_completed" to isCompleted,
        "is_starred" to isStarred
    )

    @Serializable
    private data class ManageTodoParams(
        val action: String = "",
        val category_name: String? = null,
        val title: String? = null,
        val note: String? = null,
        val due_date_millis: Long? = null,
        val has_due_time: Boolean? = null,
        val tags: List<String>? = null,
        val tasks: List<String>? = null,
        val icon_key: String? = null,
        val task_id: String? = null,
    )

    companion object {
        private const val SOURCE_AGENT = "ai_agent"
    }
}
