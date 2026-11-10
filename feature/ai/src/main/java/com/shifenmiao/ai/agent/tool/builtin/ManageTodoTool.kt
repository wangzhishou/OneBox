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
import java.util.Locale
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

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_manage_todo_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_manage_todo_examples)

    /**
     * list 已改成紧凑分页输出，正常一页 50 条约 5~6KB；这里留够余量，
     * 避免结果在 JSON 中间被默认 4096 字符上限截断（截断后的 JSON 不可解析，
     * 模型会看不到后面的条目）。
     */
    override val maxResultLength: Int = 16_384

    /** 所有 action 都会写本地库或审计日志，不能和同轮其他工具并发。 */
    override val parallelizable: Boolean = false

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_todo_param_action),
                enum = listOf(
                    "create_list", "create_task", "create_category",
                    "list",
                    "complete_task", "reopen_task", "toggle_complete",
                    "star_task", "unstar_task", "toggle_star",
                    "delete_task"
                )
            ),
            "category_name" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_todo_param_category_name)
            ),
            "category_id" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_todo_param_category_id)
            ),
            "query" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_todo_param_query)
            ),
            "include_completed" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_manage_todo_param_include_completed)
            ),
            "limit" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_manage_todo_param_limit)
            ),
            "offset" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_manage_todo_param_offset)
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
                "complete_task" -> setTaskCompleted(params, target = true, action = "complete_task")
                "reopen_task" -> setTaskCompleted(params, target = false, action = "reopen_task")
                "toggle_complete" -> setTaskCompleted(params, target = null, action = "toggle_complete")
                "star_task" -> setTaskStarred(params, target = true, action = "star_task")
                "unstar_task" -> setTaskStarred(params, target = false, action = "unstar_task")
                "toggle_star" -> setTaskStarred(params, target = null, action = "toggle_star")
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
     *
     * 显式动作和 toggle 必须分开：
     * - complete_task / reopen_task / star_task / unstar_task 是幂等设置状态；
     * - toggle_complete / toggle_star 才做状态取反。
     * 旧实现把 "complete"/"reopen"/"star" 都映射到 toggle，模型对已完成任务再说"完成"
     * 会把任务反开，属于会改错数据的语义 bug。
     */
    private fun normalizeAction(raw: String): String {
        val key = raw.trim().lowercase(Locale.ROOT).replace('-', '_').replace(' ', '_')
        return when (key) {
            "create_list", "add_list", "new_list", "make_list", "create_checklist",
            "add_checklist", "create_todo_list", "create_list_with_tasks" -> "create_list"
            "create_task", "add_task", "new_task", "add_todo", "create_todo", "new_todo" -> "create_task"
            "create_category", "add_category", "new_category", "create_topic", "add_topic" -> "create_category"
            "list", "list_all", "get_list", "query", "get", "read" -> "list"
            "complete_task", "complete", "complete_todo", "finish", "finish_task",
            "mark_complete", "mark_done", "done" -> "complete_task"
            "reopen_task", "reopen", "uncomplete", "uncomplete_task", "undo_complete" -> "reopen_task"
            "toggle_complete", "toggle_done" -> "toggle_complete"
            "star_task", "star", "star_todo", "favorite", "favourite", "pin" -> "star_task"
            "unstar_task", "unstar", "unfavorite", "unfavourite", "unpin" -> "unstar_task"
            "toggle_star", "toggle_favorite", "toggle_favourite" -> "toggle_star"
            "delete_task", "remove_task", "delete_todo", "remove" -> "delete_task"
            else -> key
        }
    }

    /**
     * 一次建好分类 + 其中全部条目 —— "帮我建一个 XX 清单"的唯一入口。
     * 分类名重名时复用已有分类(不新建),条目逐条写;同名条目跳过,任一条失败不影响其余条。
     */
    private suspend fun handleCreateList(params: ManageTodoParams): AgentToolResult {
        val listTitle = params.title?.takeIf { it.isNotBlank() }
            ?: return errorResult(R.string.agent_tool_manage_todo_missing_title)
        val rawTitles = (params.tasks ?: emptyList())
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (rawTitles.isEmpty()) {
            return errorResult(R.string.agent_tool_manage_todo_missing_tasks)
        }

        val dashboard = todoService.getDashboardDto()
        val existing = dashboard.firstOrNull { it.title.equals(listTitle, ignoreCase = true) }
        var categoryCreated = false
        val categoryId = if (existing != null) {
            existing.id
        } else {
            todoService.createCategory(
                CategoryInput(
                    title = listTitle,
                    iconKey = params.icon_key ?: "Checklist",
                ),
                source = SOURCE_AGENT
            ).getOrElse {
                return errorResult(R.string.agent_tool_manage_todo_failed, it.message ?: "unknown")
            }.also { categoryCreated = true }
        }

        val tags = params.tags ?: emptyList()
        ensureTags(tags)
        val seenTitles = existing?.tasks
            ?.mapTo(mutableSetOf()) { it.title.trim().lowercase(Locale.ROOT) }
            ?: mutableSetOf()
        val created = mutableListOf<Map<String, Any?>>()
        val skipped = mutableListOf<String>()
        val failed = mutableListOf<Map<String, Any?>>()

        rawTitles.forEach { title ->
            val normalized = title.lowercase(Locale.ROOT)
            if (!seenTitles.add(normalized)) {
                skipped += title
                return@forEach
            }
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
            ).onSuccess { taskId ->
                created += mapOf("id" to taskId, "title" to title)
            }.onFailure { error ->
                failed += mapOf(
                    "title" to title,
                    "error" to (error.message ?: "unknown")
                )
            }
        }

        val message = if (categoryCreated) {
            textProvider.string(
                R.string.agent_tool_manage_todo_list_created,
                listTitle,
                created.size
            )
        } else {
            textProvider.string(
                R.string.agent_tool_manage_todo_list_reused,
                listTitle,
                created.size,
                skipped.size
            )
        }
        val result = mapOf(
            "action" to "create_list",
            "success" to failed.isEmpty(),
            "category" to listTitle,
            "category_id" to categoryId,
            "category_created" to categoryCreated,
            "created" to created,
            "skipped" to skipped,
            "failed" to failed,
            "message" to message,
            "deepLinks" to listOf(categoryDeepLink(categoryId, listTitle))
        )
        return AgentToolResult(content = jsonStringOf(result), isError = failed.isNotEmpty())
    }

    // ── action handlers ──────────────────────────────

    private suspend fun handleList(params: ManageTodoParams): AgentToolResult {
        val dashboard = todoService.getDashboardDto()
        val inboxTasks = todoService.getInboxTasksDto()
        val inboxTitle = textProvider.string(R.string.agent_tool_manage_todo_inbox_title)
        val selections = buildList {
            add(
                CategorySelection(
                    id = INBOX_ID,
                    title = inboxTitle,
                    iconKey = "inbox",
                    isPinned = false,
                    isInbox = true,
                    order = -1,
                    tasks = inboxTasks,
                )
            )
            dashboard.forEachIndexed { index, category ->
                add(
                    CategorySelection(
                        id = category.id,
                        title = category.title,
                        iconKey = category.iconKey.ifBlank { "inbox" },
                        isPinned = category.isPinned,
                        isInbox = false,
                        order = index,
                        tasks = category.tasks,
                    )
                )
            }
        }

        val requestedId = params.category_id?.trim()?.takeIf { it.isNotEmpty() }
        val requestedName = params.category_name?.trim()?.takeIf { it.isNotEmpty() }
        val selected = when {
            requestedId != null -> selections.firstOrNull { it.id == requestedId }
            requestedName != null -> resolveSelectionByName(selections, requestedName)
            else -> null
        }
        if ((requestedId != null || requestedName != null) && selected == null) {
            return errorResult(
                R.string.agent_tool_manage_todo_category_not_found,
                requestedId ?: requestedName.orEmpty()
            )
        }

        val query = params.query?.trim().orEmpty()
        val includeCompleted = params.include_completed ?: true
        val limit = (params.limit ?: DEFAULT_LIST_LIMIT).coerceIn(1, MAX_LIST_LIMIT)
        val offset = (params.offset ?: 0).coerceAtLeast(0)

        val scopeSelections = selected?.let(::listOf) ?: selections
        val scopeRows = scopeSelections
            .flatMap { selection ->
                selection.tasks.map { task -> TaskRow(task = task, selection = selection) }
            }
            .filter { row -> query.isEmpty() || row.task.matchesQuery(query) }
        val completedCount = scopeRows.count { it.task.isCompleted }
        val pendingCount = scopeRows.size - completedCount
        val visibleRows = if (includeCompleted) {
            scopeRows
        } else {
            scopeRows.filter { !it.task.isCompleted }
        }
        val page = buildTaskPage(
            rows = visibleRows.sortedWith(agentTaskComparator),
            offset = offset,
            limit = limit,
            includeCategory = selected == null,
        )

        val allCategories = when {
            // 有关键词时分组计数会和当前结果对不上，不返回分组摘要，避免模型把全量计数当成命中数。
            query.isNotEmpty() -> emptyList()
            selected == null -> selections.filter { !it.isInbox }.map { it.toSummary() }
            selected.isInbox -> emptyList()
            else -> listOf(selected.toSummary())
        }
        val categories = allCategories.take(MAX_LIST_CATEGORIES)
        val inbox = selections.first { it.isInbox }
        val inboxSummary = when {
            query.isNotEmpty() -> null
            selected?.isInbox == true -> inbox.toSummary()
            selected != null -> null
            inbox.tasks.isNotEmpty() -> inbox.toSummary()
            else -> null
        }

        val summary = buildMap<String, Any?> {
            put("task_count", scopeRows.size)
            put("completed_count", completedCount)
            put("pending_count", pendingCount)
            put("category_count", dashboard.size)
            put("returned_count", page.tasks.size)
            put("offset", offset)
            put("limit", limit)
            put("has_more", page.hasMore)
            page.nextOffset?.let { put("next_offset", it) }
            put("include_completed", includeCompleted)
        }
        val scope = when {
            query.isNotEmpty() -> "search"
            selected != null -> "category"
            else -> "all"
        }
        val result = buildMap<String, Any?> {
            put("action", "list")
            put("success", true)
            put("scope", scope)
            selected?.let { put("category", mapOf("id" to it.id, "title" to it.title)) }
            if (query.isNotEmpty()) put("query", query)
            put("summary", summary)
            put("categories", categories)
            if (allCategories.size > categories.size) {
                put("categories_truncated", true)
            }
            inboxSummary?.let { put("inbox", it) }
            put("tasks", page.tasks)
            put(
                "message",
                textProvider.string(
                    R.string.agent_tool_manage_todo_list_summary,
                    scopeRows.size,
                    completedCount,
                    pendingCount,
                    page.tasks.size
                )
            )
            put(
                "deepLinks",
                listOf(
                    if (selected != null) {
                        categoryDeepLink(selected.id, selected.title)
                    } else {
                        dashboardDeepLink()
                    }
                )
            )
        }
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun handleCreateTask(params: ManageTodoParams): AgentToolResult {
        val categoryIdParam = params.category_id?.trim()?.takeIf { it.isNotEmpty() }
        val catName = params.category_name?.trim()?.takeIf { it.isNotEmpty() }
        val taskTitle = params.title
        if (taskTitle.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_todo_missing_title)
        }
        val inboxTitle = textProvider.string(R.string.agent_tool_manage_todo_inbox_title)
        val isInboxTarget = categoryIdParam == INBOX_ID ||
            catName == INBOX_ID ||
            catName.equals(inboxTitle, ignoreCase = true) ||
            catName.equals("inbox", ignoreCase = true)
        val lookup: CategoryLookup? = when {
            isInboxTarget -> null
            categoryIdParam != null -> {
                val category = todoService.getDashboardDto()
                    .firstOrNull { it.id == categoryIdParam }
                    ?: return errorResult(
                        R.string.agent_tool_manage_todo_category_not_found,
                        categoryIdParam
                    )
                CategoryLookup(id = category.id, title = category.title)
            }
            catName != null -> todoService.findCategoryByTitle(catName) ?: run {
                val newId = todoService.createCategory(
                    CategoryInput(title = catName, iconKey = "Checklist"),
                    source = SOURCE_AGENT
                ).getOrElse {
                    return errorResult(
                        R.string.agent_tool_manage_todo_failed,
                        it.message ?: "unknown"
                    )
                }
                CategoryLookup(id = newId, title = catName)
            }
            else -> null
        }

        val input = TaskInput(
            categoryId = lookup?.id,
            title = taskTitle,
            note = params.note,
            dueDateMillis = params.due_date_millis,
            tags = params.tags ?: emptyList(),
            hasDueTime = params.has_due_time == true,
        )
        val taskId = todoService.createTask(input, source = SOURCE_AGENT)
            .getOrElse {
                return errorResult(R.string.agent_tool_manage_todo_failed, it.message ?: "unknown")
            }
        ensureTags(input.tags)

        val result = mapOf(
            "action" to "create_task",
            "success" to true,
            "category" to lookup?.title,
            "category_id" to lookup?.id,
            "is_inbox" to (lookup == null),
            "task_id" to taskId,
            "task_title" to taskTitle,
            "message" to textProvider.string(R.string.agent_tool_manage_todo_task_created, taskTitle),
            "deepLinks" to listOf(
                taskDeepLink(
                    taskId = taskId,
                    categoryId = lookup?.id,
                    label = taskTitle,
                )
            )
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun handleCreateCategory(params: ManageTodoParams): AgentToolResult {
        val catTitle = params.title
        if (catTitle.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_todo_missing_title)
        }
        val existing = todoService.findCategoryByTitle(catTitle)
        if (existing != null) {
            val result = mapOf(
                "action" to "create_category",
                "success" to true,
                "created" to false,
                "category_id" to existing.id,
                "category_title" to existing.title,
                "message" to textProvider.string(
                    R.string.agent_tool_manage_todo_category_exists,
                    existing.title
                ),
                "deepLinks" to listOf(categoryDeepLink(existing.id, existing.title))
            )
            return AgentToolResult(content = jsonStringOf(result))
        }

        val categoryId = todoService.createCategory(
            CategoryInput(
                title = catTitle,
                iconKey = params.icon_key ?: "inbox",
            ),
            source = SOURCE_AGENT
        ).getOrElse {
            return errorResult(R.string.agent_tool_manage_todo_failed, it.message ?: "unknown")
        }

        val result = mapOf(
            "action" to "create_category",
            "success" to true,
            "created" to true,
            "category_id" to categoryId,
            "category_title" to catTitle,
            "message" to textProvider.string(
                R.string.agent_tool_manage_todo_category_created,
                catTitle
            ),
            "deepLinks" to listOf(categoryDeepLink(categoryId, catTitle))
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun setTaskCompleted(
        params: ManageTodoParams,
        target: Boolean?,
        action: String,
    ): AgentToolResult {
        val task = resolveTask(params.task_id)
            ?: return errorResult(
                R.string.agent_tool_manage_todo_task_not_found,
                params.task_id.orEmpty()
            )
        val newValue = target ?: !task.isCompleted
        val changed = newValue != task.isCompleted
        if (changed) {
            todoService.toggleTaskComplete(
                taskId = task.id,
                isCompleted = newValue,
                taskTitle = task.title,
                source = SOURCE_AGENT
            ).getOrElse {
                return errorResult(R.string.agent_tool_manage_todo_failed, it.message ?: "unknown")
            }
        }

        val status = if (newValue) "completed" else "reopened"
        val result = mapOf(
            "action" to action,
            "success" to true,
            "changed" to changed,
            "task_id" to task.id,
            "task_title" to task.title,
            "status" to status,
            "message" to textProvider.string(
                R.string.agent_tool_manage_todo_toggled,
                task.title,
                status
            ),
            "deepLinks" to listOf(
                taskDeepLink(
                    taskId = task.id,
                    categoryId = task.categoryId,
                    label = task.title,
                )
            )
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun setTaskStarred(
        params: ManageTodoParams,
        target: Boolean?,
        action: String,
    ): AgentToolResult {
        val task = resolveTask(params.task_id)
            ?: return errorResult(
                R.string.agent_tool_manage_todo_task_not_found,
                params.task_id.orEmpty()
            )
        val newValue = target ?: !task.isStarred
        val changed = newValue != task.isStarred
        if (changed) {
            todoService.toggleTaskStar(
                taskId = task.id,
                isStarred = newValue,
                taskTitle = task.title,
                source = SOURCE_AGENT
            ).getOrElse {
                return errorResult(R.string.agent_tool_manage_todo_failed, it.message ?: "unknown")
            }
        }

        val status = if (newValue) "starred" else "unstarred"
        val result = mapOf(
            "action" to action,
            "success" to true,
            "changed" to changed,
            "task_id" to task.id,
            "task_title" to task.title,
            "status" to status,
            "message" to textProvider.string(
                R.string.agent_tool_manage_todo_toggled,
                task.title,
                status
            ),
            "deepLinks" to listOf(
                taskDeepLink(
                    taskId = task.id,
                    categoryId = task.categoryId,
                    label = task.title,
                )
            )
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun handleDeleteTask(params: ManageTodoParams): AgentToolResult {
        val task = resolveTask(params.task_id)
            ?: return errorResult(
                R.string.agent_tool_manage_todo_task_not_found,
                params.task_id.orEmpty()
            )

        todoService.deleteTask(
            taskId = task.id,
            taskTitle = task.title,
            source = SOURCE_AGENT
        ).getOrElse {
            return errorResult(R.string.agent_tool_manage_todo_failed, it.message ?: "unknown")
        }

        val result = mapOf(
            "action" to "delete_task",
            "success" to true,
            "task_id" to task.id,
            "task_title" to task.title,
            "message" to textProvider.string(R.string.agent_tool_manage_todo_task_deleted, task.title),
            "deepLinks" to listOf(dashboardDeepLink())
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    // ── helpers ───────────────────────────────────

    /**
     * 优先按 id 找；模型偶尔会把标题当 task_id 传，这里在「全库唯一同名」时兜底，
     * 有重名则返回 null，绝不猜。
     */
    /**
     * Agent 写入的标签也要进标签池，否则任务行能看到 #标签，
     * 但筛选面板的标签来源是标签表，里面没有这一项。
     * 单个标签创建失败不影响任务本身，吞掉失败即可。
     */
    private suspend fun ensureTags(tags: List<String>) {
        tags.map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase(Locale.ROOT) }
            .forEach { tag ->
                runCatching { todoService.createTag(tag, null, SOURCE_AGENT) }
            }
    }

    private suspend fun resolveTask(taskId: String?): TodoTaskDto? {
        val raw = taskId?.trim().orEmpty()
        if (raw.isEmpty()) return null
        todoService.getTaskDto(raw)?.let { return it }
        val candidates = todoService.getDashboardDto().flatMap { it.tasks } +
            todoService.getInboxTasksDto()
        return candidates
            .filter { it.title.equals(raw, ignoreCase = true) }
            .singleOrNull()
    }

    private fun resolveSelectionByName(
        selections: List<CategorySelection>,
        name: String,
    ): CategorySelection? {
        if (name == INBOX_ID || name.equals("inbox", ignoreCase = true)) {
            return selections.firstOrNull { it.isInbox }
        }
        selections.firstOrNull { it.title.equals(name, ignoreCase = true) }?.let { return it }
        return selections
            .filter { it.title.contains(name, ignoreCase = true) }
            .singleOrNull()
    }

    private fun TaskRow.toCompactMap(includeCategory: Boolean): Map<String, Any?> = buildMap {
        put("id", task.id)
        put("title", task.title)
        if (includeCategory) put("category", selection.title)
        put("done", task.isCompleted)
        if (task.isStarred) put("starred", true)
        if (task.priority != TODO_PRIORITY_MEDIUM) put("priority", task.priority)
        task.dueDate?.let { due ->
            put("due_date", due)
            if (task.hasDueTime) put("due_has_time", true)
        }
        if (task.tags.isNotEmpty()) put("tags", task.tags)
        task.note?.trim()?.takeIf { it.isNotEmpty() }?.let { note ->
            put(
                "note",
                if (note.length > NOTE_PREVIEW_CHARS) {
                    note.take(NOTE_PREVIEW_CHARS) + "…"
                } else {
                    note
                }
            )
        }
    }

    private fun CategorySelection.toSummary(): Map<String, Any?> = mapOf(
        "id" to id,
        "title" to title,
        "icon_key" to iconKey,
        "task_count" to tasks.size,
        "completed_count" to tasks.count { it.isCompleted },
        "pending_count" to tasks.count { !it.isCompleted },
        "is_pinned" to isPinned,
    )

    private fun TodoTaskDto.matchesQuery(query: String): Boolean =
        title.contains(query, ignoreCase = true) ||
            note?.contains(query, ignoreCase = true) == true ||
            tags.any { it.contains(query, ignoreCase = true) }

    private fun buildTaskPage(
        rows: List<TaskRow>,
        offset: Int,
        limit: Int,
        includeCategory: Boolean,
    ): TaskPage {
        if (offset >= rows.size) {
            return TaskPage(tasks = emptyList(), hasMore = false, nextOffset = null)
        }
        val tasks = mutableListOf<Map<String, Any?>>()
        var usedChars = 0
        var index = offset
        var hasMore = false
        while (index < rows.size && tasks.size < limit) {
            val map = rows[index].toCompactMap(includeCategory)
            val cost = jsonStringOf(map).length + 1
            // 先保证本页至少有一条，避免单条超长时永远返回空页。
            if (tasks.isNotEmpty() && usedChars + cost > LIST_TASK_BUDGET_CHARS) {
                hasMore = true
                break
            }
            tasks += map
            usedChars += cost
            index++
        }
        if (index < rows.size) hasMore = true
        return TaskPage(
            tasks = tasks,
            hasMore = hasMore,
            nextOffset = if (hasMore) index else null,
        )
    }

    private fun errorResult(resId: Int, vararg args: Any): AgentToolResult =
        AgentToolResult(content = textProvider.string(resId, *args), isError = true)

    private fun markTodoDeeplink(vararg extraParams: Pair<String, String>): String =
        AppNavigationRegistry.buildStructuredDeeplink(
            targetType = AppNavigationTargetType.SCREEN,
            routeKey = "mark_todo_router",
            params = extraParams.toMap(),
        )

    private fun dashboardDeepLink(): Map<String, Any?> = mapOf(
        "uri" to markTodoDeeplink(),
        "label" to textProvider.string(R.string.agent_tool_manage_todo_title),
        "primary" to true,
    )

    private fun categoryDeepLink(
        categoryId: String,
        categoryTitle: String,
    ): Map<String, Any?> = mapOf(
        // 只带 id：标题里可能有 &、% 等字符，标题由页面加载后自己显示。
        "uri" to markTodoDeeplink("category_id" to categoryId),
        "label" to categoryTitle,
        "primary" to true,
    )

    private fun taskDeepLink(
        taskId: String,
        categoryId: String?,
        label: String,
    ): Map<String, Any?> {
        val params = buildList {
            add("task_id" to taskId)
            if (!categoryId.isNullOrBlank()) {
                add("category_id" to categoryId)
            }
        }.toTypedArray()
        return mapOf(
            "uri" to markTodoDeeplink(*params),
            "label" to label,
            "primary" to true,
        )
    }

    private data class CategorySelection(
        val id: String,
        val title: String,
        val iconKey: String,
        val isPinned: Boolean,
        val isInbox: Boolean,
        val order: Int,
        val tasks: List<TodoTaskDto>,
    )

    private data class TaskRow(
        val task: TodoTaskDto,
        val selection: CategorySelection,
    )

    private data class TaskPage(
        val tasks: List<Map<String, Any?>>,
        val hasMore: Boolean,
        val nextOffset: Int?,
    )

    private val agentTaskComparator: Comparator<TaskRow> =
        compareBy<TaskRow> { it.selection.order }
            .thenByDescending { it.task.isStarred }
            .thenByDescending { it.task.createdAt }
            .thenBy { it.task.id }

    @Serializable
    private data class ManageTodoParams(
        val action: String = "",
        val category_name: String? = null,
        val category_id: String? = null,
        val query: String? = null,
        val include_completed: Boolean? = null,
        val limit: Int? = null,
        val offset: Int? = null,
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

        /** 与 feature/marktodo 的 TodoCategory.INBOX_ID 保持一致 */
        private const val INBOX_ID = "__inbox__"

        private const val DEFAULT_LIST_LIMIT = 20
        private const val MAX_LIST_LIMIT = 100
        private const val MAX_LIST_CATEGORIES = 50

        /**
         * 单页任务 JSON 的软上限。超过后提前结束本页并返回 next_offset，
         * 保证结果不会撞上 maxResultLength 在 JSON 中间被截断。
         */
        private const val LIST_TASK_BUDGET_CHARS = 6_000

        private const val NOTE_PREVIEW_CHARS = 120
    }
}

private const val TODO_PRIORITY_MEDIUM = 1
