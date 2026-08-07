package com.shifenmiao.ai.agent.tool.builtin

import com.google.gson.Gson
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.R
import com.shifenmiao.common.handle.navigation.AppNavigationTargetType
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.model.todo.CategoryInput
import com.shifenmiao.model.todo.MarkTodoServiceInterface
import com.shifenmiao.model.todo.TaskInput
import com.shifenmiao.model.todo.TodoCategoryDto
import com.shifenmiao.model.todo.TodoTaskDto
import javax.inject.Inject

class ManageTodoTool @Inject constructor(
    private val todoService: MarkTodoServiceInterface,
    private val gson: Gson,
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
                    "list", "create_task", "create_category",
                    "toggle_complete", "toggle_star", "delete_task"
                )
            ),
            "category_name" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_todo_param_category_name)
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
            "tags" to ToolParameterProperty(
                type = "array",
                description = textProvider.string(R.string.agent_tool_manage_todo_param_tags)
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

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = gson.fromJson(arguments, ManageTodoParams::class.java)
            when (params.action) {
                "list" -> handleList(params)
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

    // ── action handlers ──────────────────────────────

    private suspend fun handleList(params: ManageTodoParams): AgentToolResult {
        val dashboard = todoService.getDashboardDto()
        val filtered = if (!params.category_name.isNullOrBlank()) {
            dashboard.filter { it.title.equals(params.category_name, ignoreCase = true) }
        } else {
            dashboard
        }
        val data = filtered.map { it.toMap() }
        val result = mapOf(
            "action" to "list",
            "success" to true,
            "categories" to data,
            "deeplink" to markTodoDeeplink()
        )
        return AgentToolResult(content = gson.toJson(result))
    }

    private suspend fun handleCreateTask(params: ManageTodoParams): AgentToolResult {
        val catName = params.category_name
        if (catName.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_todo_missing_category_name)
        }
        val taskTitle = params.title
        if (taskTitle.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_todo_missing_title)
        }
        val lookup = todoService.findCategoryByTitle(catName)
            ?: return errorResult(
                R.string.agent_tool_manage_todo_category_not_found,
                catName
            )

        @Suppress("UNCHECKED_CAST")
        val tags = params.tags ?: emptyList()
        val input = TaskInput(
            categoryId = lookup.id,
            title = taskTitle,
            note = params.note,
            dueDateMillis = params.due_date_millis,
            tags = tags
        )
        todoService.createTask(input, source = SOURCE_AGENT)
            .getOrElse { return errorResult(R.string.agent_tool_manage_todo_failed, it.message ?: "unknown") }

        val result = mapOf(
            "action" to "create_task",
            "success" to true,
            "category" to lookup.title,
            "task_title" to taskTitle,
            "message" to textProvider.string(R.string.agent_tool_manage_todo_task_created, taskTitle),
            "deeplink" to markTodoDeeplink("category_id" to lookup.id)
        )
        return AgentToolResult(content = gson.toJson(result))
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
        return AgentToolResult(content = gson.toJson(result))
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
        return AgentToolResult(content = gson.toJson(result))
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
        return AgentToolResult(content = gson.toJson(result))
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
        return AgentToolResult(content = gson.toJson(result))
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
        "title" to title,
        "note" to note,
        "due_date" to dueDate,
        "tags" to tags,
        "is_completed" to isCompleted,
        "is_starred" to isStarred
    )

    private data class ManageTodoParams(
        val action: String = "",
        val category_name: String? = null,
        val title: String? = null,
        val note: String? = null,
        val due_date_millis: Long? = null,
        val tags: List<String>? = null,
        val icon_key: String? = null,
        val task_id: String? = null,
    )

    companion object {
        private const val SOURCE_AGENT = "ai_agent"
    }
}
