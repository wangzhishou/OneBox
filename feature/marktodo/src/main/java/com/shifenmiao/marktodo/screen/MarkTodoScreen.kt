package com.shifenmiao.marktodo.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.components.QuickAddTodoSheet
import com.shifenmiao.marktodo.components.TaskFilterSortSheet
import com.shifenmiao.marktodo.components.TaskListContent
import com.shifenmiao.marktodo.components.TaskListEmptyState
import com.shifenmiao.marktodo.components.TaskListItem
import com.shifenmiao.marktodo.components.ThemeGroupCard
import com.shifenmiao.marktodo.components.ThemeGroupGridCard
import com.shifenmiao.marktodo.components.taskSortDirectionLabel
import com.shifenmiao.marktodo.components.taskSortModeLabel
import com.shifenmiao.marktodo.domain.TodoTaskQuery
import com.shifenmiao.marktodo.model.DialogState
import com.shifenmiao.marktodo.model.MarkTodoUiEvent
import com.shifenmiao.marktodo.model.TaskCompletionFilter
import com.shifenmiao.marktodo.model.TaskDateScope
import com.shifenmiao.marktodo.model.TaskFilter
import com.shifenmiao.marktodo.model.TaskSortConfig
import com.shifenmiao.marktodo.model.TaskWithCategory
import com.shifenmiao.marktodo.model.TodayGroup
import com.shifenmiao.marktodo.model.TodayTaskGroup
import com.shifenmiao.marktodo.model.TodoCategory
import com.shifenmiao.marktodo.model.TodoTag
import com.shifenmiao.marktodo.model.TodoTask
import com.shifenmiao.marktodo.screenLogic.MarkTodoComponent
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAccessTime
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCheckCircleOutline
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFeatures
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFilterAlt
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInbox
import com.t8rin.imagetoolbox.core.resources.icons.line.LineQuickTiles
import com.t8rin.imagetoolbox.core.resources.icons.line.LineViewList
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassFilterChip
import com.wanbaohe.core.ui.widget.glass.GlassSearchTextField
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.navigation.BottomNavItem
import com.wanbaohe.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.core.ui.widget.system.OneBoxDangerButton
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState

private const val QueryToolsMinTaskCount = 8
private const val SearchFieldMinTaskCount = 100

@Composable
fun MarkTodoScreen(
    markTodoComponent: MarkTodoComponent,
    onGoBack: () -> Unit,
) {
    val uiState by markTodoComponent.uiState.collectAsState()
    val dialogState by markTodoComponent.dialogState.collectAsState()
    val categories by markTodoComponent.categoriesState.collectAsState()
    val tasks by markTodoComponent.tasksState.collectAsState()
    val tags by markTodoComponent.tagsState.collectAsState()

    val tagsByName = remember(tags) { tags.associateBy { it.name } }
    val fullCategories = remember(categories, tasks) {
        categories.map { category ->
            category.copy(
                tasks = tasks.filter { it.categoryId == category.id }.sortedBy { it.sortOrder }
            )
        }
    }
    val flattened = remember(tasks, fullCategories) {
        TodoTaskQuery.flatten(tasks = tasks, categories = fullCategories)
    }
    val baseTaskCount = remember(flattened, uiState.pendingCompletionIds) {
        TodoTaskQuery.filterAndSort(
            tasks = flattened,
            retainedCompletedTaskIds = uiState.pendingCompletionIds,
        ).size
    }
    val showSearchField = uiState.searchQuery.isNotBlank() ||
        baseTaskCount > SearchFieldMinTaskCount
    val showFilterBar = uiState.filter != TaskFilter() ||
        uiState.sort != TaskSortConfig() ||
        baseTaskCount >= QueryToolsMinTaskCount
    // 底部状态页签是完成状态的唯一开关：筛选面板里不再单独管完成状态
    val effectiveFilter = remember(uiState.filter, uiState.statusTab) {
        uiState.filter.copy(completion = uiState.statusTab)
    }
    val visibleTasks = remember(
        flattened,
        uiState.searchQuery,
        effectiveFilter,
        uiState.sort,
        uiState.pendingCompletionIds,
    ) {
        TodoTaskQuery.filterAndSort(
            tasks = flattened,
            searchQuery = uiState.searchQuery,
            filter = effectiveFilter,
            sort = uiState.sort,
            retainedCompletedTaskIds = uiState.pendingCompletionIds,
        )
    }
    val todayGroups = remember(visibleTasks) { TodoTaskQuery.groupForToday(visibleTasks) }
    val tasksByCategory = remember(visibleTasks) {
        visibleTasks.filter { it.task.categoryId != null }.groupBy { it.task.categoryId!! }
    }
    // 收集箱 = 未归组任务（库里没有分组行），页面上拼成一张虚拟分组卡片
    val inboxTitle = stringResource(R.string.p1_inbox)
    val inboxCategory = remember(tasks, inboxTitle) {
        TodoCategory(
            id = TodoCategory.INBOX_ID,
            title = inboxTitle,
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInbox,
            tasks = tasks.filter { it.categoryId == null }.sortedBy { it.sortOrder },
        )
    }
    val inboxVisibleTasks = remember(visibleTasks) {
        visibleTasks.filter { it.task.categoryId == null }.map { it.task }
    }
    val hasNarrowingQuery = uiState.searchQuery.isNotBlank() ||
        uiState.filter.hasCategoryFilter ||
        uiState.filter.tagNames.isNotEmpty() ||
        uiState.filter.priorities.isNotEmpty() ||
        uiState.filter.hasDateFilter ||
        uiState.filter.starredOnly
    val visibleCategories = remember(fullCategories, tasksByCategory, hasNarrowingQuery) {
        fullCategories.filter { category ->
            !hasNarrowingQuery || tasksByCategory[category.id]?.isNotEmpty() == true
        }
    }

    BaseScreen(
        title = { Text(text = stringResource(R.string.marktodo)) },
        actions = {
            MarkTodoScreenActions(
                isReorderMode = uiState.isReorderMode,
                isGridView = uiState.isGridView,
                showViewMode = uiState.searchQuery.isBlank() &&
                    !uiState.filter.hasSmartDateFilter,
                onToggleViewMode = {
                    markTodoComponent.handleEvent(MarkTodoUiEvent.SetGridView(!uiState.isGridView))
                },
                onExitReorder = {
                    markTodoComponent.handleEvent(MarkTodoUiEvent.SetReorderMode(false))
                },
                onAddTask = {
                    markTodoComponent.handleEvent(MarkTodoUiEvent.AddTaskClicked())
                },
            )
        },
        onGoBack = onGoBack,
        isShowDefaultActions = false,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (showSearchField) {
                GlassSearchTextField(
                    value = uiState.searchQuery,
                    onValueChange = {
                        markTodoComponent.handleEvent(MarkTodoUiEvent.SearchQueryChanged(it))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = OneBoxDesignSystem.screenPadding,
                            vertical = OneBoxDesignSystem.compactSpacing,
                        ),
                    placeholder = stringResource(R.string.p1_search_tasks),
                )
            } else {
                Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
            }

            if (showFilterBar) {
                QueryShortcutBar(
                    filter = uiState.filter,
                    sort = uiState.sort,
                    onFilterChange = {
                        markTodoComponent.handleEvent(MarkTodoUiEvent.SetFilter(it))
                    },
                    onSortChange = {
                        markTodoComponent.handleEvent(MarkTodoUiEvent.SetSort(it))
                    },
                    onShowFilterSort = {
                        markTodoComponent.handleEvent(MarkTodoUiEvent.ShowFilterSortSheet)
                    },
                )
            }

            val contentModifier = Modifier.weight(1f)
            when {
                uiState.searchQuery.isNotBlank() -> {
                    if (visibleTasks.isEmpty()) {
                        TaskListEmptyState(
                            message = stringResource(R.string.p1_empty_search),
                            actionLabel = stringResource(R.string.p1_clear_search),
                            onAction = {
                                markTodoComponent.handleEvent(MarkTodoUiEvent.SearchQueryChanged(""))
                            },
                            modifier = contentModifier,
                        )
                    } else {
                        SearchTaskList(
                            tasks = visibleTasks,
                            tagsByName = tagsByName,
                            component = markTodoComponent,
                            modifier = contentModifier,
                        )
                    }
                }
                uiState.filter.hasSmartDateFilter -> TodayTaskList(
                    groups = todayGroups,
                    tagsByName = tagsByName,
                    component = markTodoComponent,
                    modifier = contentModifier,
                )
                visibleTasks.isEmpty() && hasNarrowingQuery -> TaskListEmptyState(
                    message = stringResource(R.string.empty_filtered_tasks),
                    actionLabel = stringResource(R.string.p1_clear_filters),
                    onAction = {
                        markTodoComponent.handleEvent(MarkTodoUiEvent.ClearFilters)
                    },
                    modifier = contentModifier,
                )
                else -> CategoriesContent(
                    categories = visibleCategories,
                    // 按分组筛选时，没勾「收集箱」就不摆这张卡
                    inboxCategory = inboxCategory.takeIf {
                        !uiState.filter.hasCategoryFilter || uiState.filter.includeInbox
                    },
                    inboxVisibleTasks = inboxVisibleTasks,
                    themeIndexById = fullCategories.mapIndexed { index, category ->
                        category.id to index + 1
                    }.toMap(),
                    tasksByCategory = tasksByCategory,
                    tagsByName = tagsByName,
                    // 一个分组都没有时，补一张「新建分组」卡，别把建组入口藏进快速添加
                    showAddGroupCard = fullCategories.isEmpty() && !uiState.isReorderMode,
                    isGridView = uiState.isGridView,
                    isReorderMode = uiState.isReorderMode,
                    component = markTodoComponent,
                    modifier = contentModifier,
                )
            }

            AnimatedVisibility(visible = !uiState.isReorderMode) {
                StatusBottomBar(
                    selectedTab = uiState.statusTab,
                    onSelectTab = {
                        markTodoComponent.handleEvent(MarkTodoUiEvent.SelectStatusTab(it))
                    },
                )
            }
        }
    }

    QuickAddTodoSheet(
        component = markTodoComponent.quickAddTodoComponent,
        onAddCategory = {
            markTodoComponent.handleEvent(MarkTodoUiEvent.AddCategoryClicked)
        },
    )

    if (uiState.isFilterSortSheetVisible) {
        TaskFilterSortSheet(
            visible = true,
            categories = fullCategories,
            tags = tags,
            initialFilter = uiState.filter,
            initialSort = uiState.sort,
            onDismiss = {
                markTodoComponent.handleEvent(MarkTodoUiEvent.DismissFilterSortSheet)
            },
            onApply = { filter, sort ->
                markTodoComponent.handleEvent(MarkTodoUiEvent.ApplyFilterSort(filter, sort))
            },
        )
    }

    when (val state = dialogState) {
        is DialogState.DeleteCategoryConfirm -> AlertDialog(
            containerColor = AppTheme.colors.getContainerSurfaceColor(),
            onDismissRequest = {
                markTodoComponent.handleEvent(MarkTodoUiEvent.DismissDialog)
            },
            title = { Text(stringResource(R.string.dialog_delete_category_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.p1_delete_category_message_count,
                        state.category.title,
                        state.category.totalCount,
                    )
                )
            },
            confirmButton = {
                OneBoxDangerButton(
                    text = stringResource(R.string.action_delete),
                    enabled = !uiState.isDeletingCategory,
                    onClick = {
                        markTodoComponent.handleEvent(
                            MarkTodoUiEvent.ConfirmDeleteCategory(state.category)
                        )
                    },
                )
            },
            dismissButton = {
                OneSecondaryButton(
                    text = stringResource(R.string.action_cancel),
                    enabled = !uiState.isDeletingCategory,
                    onClick = {
                        markTodoComponent.handleEvent(MarkTodoUiEvent.DismissDialog)
                    },
                )
            },
        )
        else -> Unit
    }

    BackHandler(enabled = !uiState.isFilterSortSheetVisible) {
        when {
            dialogState !is DialogState.Dismissed -> {
                markTodoComponent.handleEvent(MarkTodoUiEvent.DismissDialog)
            }
            uiState.isReorderMode -> {
                markTodoComponent.handleEvent(MarkTodoUiEvent.SetReorderMode(false))
            }
            else -> onGoBack()
        }
    }
}

@Composable
private fun MarkTodoScreenActions(
    isReorderMode: Boolean,
    isGridView: Boolean,
    showViewMode: Boolean,
    onToggleViewMode: () -> Unit,
    onExitReorder: () -> Unit,
    onAddTask: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isReorderMode) {
            IconButton(onClick = onExitReorder) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                    contentDescription = stringResource(R.string.cd_edit_mode_done),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            return@Row
        }
        // 网格 / 列表切换常驻顶栏：以前它藏在筛选条里，列表短（<8 条）时整条筛选条不显示，按钮就找不到了
        if (showViewMode) {
            IconButton(onClick = onToggleViewMode) {
                Icon(
                    imageVector = if (isGridView) {
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineViewList
                    } else {
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQuickTiles
                    },
                    contentDescription = stringResource(
                        if (isGridView) {
                            R.string.cd_switch_to_list_view
                        } else {
                            R.string.cd_switch_to_grid_view
                        }
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        IconButton(onClick = onAddTask) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                contentDescription = stringResource(R.string.action_add_task),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SearchTaskList(
    tasks: List<TaskWithCategory>,
    tagsByName: Map<String, TodoTag>,
    component: MarkTodoComponent,
    modifier: Modifier = Modifier,
) {
    TaskListContent(
        tasks = tasks,
        tagsByName = tagsByName,
        onTaskClick = { component.handleEvent(MarkTodoUiEvent.TaskClicked(it)) },
        onToggleComplete = { component.handleEvent(MarkTodoUiEvent.ToggleTaskComplete(it)) },
        onToggleStar = { component.handleEvent(MarkTodoUiEvent.ToggleTaskStar(it)) },
        onDuplicate = { component.handleEvent(MarkTodoUiEvent.DuplicateTask(it)) },
        onDelete = { component.handleEvent(MarkTodoUiEvent.DeleteTask(it)) },
        onTagClick = { component.handleEvent(MarkTodoUiEvent.TagOnTaskClicked(it)) },
        modifier = modifier,
    )
}

@Composable
private fun TodayTaskList(
    groups: List<TodayTaskGroup>,
    tagsByName: Map<String, TodoTag>,
    component: MarkTodoComponent,
    modifier: Modifier = Modifier,
) {
    if (groups.isEmpty()) {
        TaskListEmptyState(
            message = stringResource(R.string.p1_empty_today),
            modifier = modifier,
        )
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = OneBoxDesignSystem.screenPadding,
            vertical = OneBoxDesignSystem.compactSpacing,
        ),
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
    ) {
        groups.forEach { group ->
            item(key = "today_group_${group.group}", contentType = "today_group_header") {
                Text(
                    text = stringResource(
                        when (group.group) {
                            TodayGroup.OVERDUE -> R.string.p1_group_overdue
                            TodayGroup.TODAY -> R.string.p1_group_today
                            TodayGroup.UPCOMING -> R.string.p1_group_upcoming
                            TodayGroup.NO_DATE -> R.string.p1_group_no_date
                        }
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(
                        top = OneBoxDesignSystem.compactSpacing,
                        bottom = OneBoxDesignSystem.microSpacing,
                    ),
                )
            }
            items(
                items = group.tasks,
                key = { it.task.id },
                contentType = { "today_task" },
            ) { item ->
                TaskListItem(
                    item = item,
                    tagsByName = tagsByName,
                    onClick = { component.handleEvent(MarkTodoUiEvent.TaskClicked(it)) },
                    onToggleComplete = {
                        component.handleEvent(MarkTodoUiEvent.ToggleTaskComplete(it))
                    },
                    onToggleStar = { component.handleEvent(MarkTodoUiEvent.ToggleTaskStar(it)) },
                    onDuplicate = { component.handleEvent(MarkTodoUiEvent.DuplicateTask(it)) },
                    onDelete = { component.handleEvent(MarkTodoUiEvent.DeleteTask(it)) },
                    onTagClick = { component.handleEvent(MarkTodoUiEvent.TagOnTaskClicked(it)) },
                )
            }
        }
    }
}

@Composable
private fun QueryShortcutBar(
    filter: TaskFilter,
    sort: TaskSortConfig,
    onFilterChange: (TaskFilter) -> Unit,
    onSortChange: (TaskSortConfig) -> Unit,
    onShowFilterSort: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = OneBoxDesignSystem.screenPadding,
                end = OneBoxDesignSystem.microSpacing,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (filter.hasAdvancedFilters) {
                ActiveConditionChips(filter = filter, onFilterChange = onFilterChange)
            } else {
                QuickDateFilterChip(
                    label = stringResource(R.string.p1_group_overdue),
                    scope = TaskDateScope.OVERDUE,
                    filter = filter,
                    onFilterChange = onFilterChange,
                )
                QuickDateFilterChip(
                    label = stringResource(R.string.p1_today),
                    scope = TaskDateScope.TODAY,
                    filter = filter,
                    onFilterChange = onFilterChange,
                )
                QuickDateFilterChip(
                    label = stringResource(R.string.p1_next_7_days),
                    scope = TaskDateScope.NEXT_7_DAYS,
                    filter = filter,
                    onFilterChange = onFilterChange,
                )
            }
            if (sort != TaskSortConfig()) {
                ActiveConditionChip(
                    label = stringResource(
                        R.string.p1_sort_chip,
                        taskSortModeLabel(sort.mode),
                        taskSortDirectionLabel(sort.ascending),
                    ),
                    onClear = { onSortChange(TaskSortConfig()) },
                )
            }
        }
        IconButton(onClick = onShowFilterSort, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFilterAlt,
                contentDescription = stringResource(R.string.p1_filter_sort),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun ActiveConditionChips(
    filter: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit,
) {
    if (filter.hasCategoryFilter) {
        val count = filter.categoryIds.size + if (filter.includeInbox) 1 else 0
        ActiveConditionChip(
            label = if (count == 1 && filter.includeInbox) {
                stringResource(R.string.p1_inbox)
            } else {
                stringResource(R.string.p1_categories_selected, count)
            },
            onClear = {
                onFilterChange(filter.copy(categoryIds = emptySet(), includeInbox = false))
            },
        )
    }
    if (filter.tagNames.isNotEmpty()) {
        ActiveConditionChip(
            label = stringResource(R.string.p1_tags_selected, filter.tagNames.size),
            onClear = { onFilterChange(filter.copy(tagNames = emptySet())) },
        )
    }
    if (filter.priorities.isNotEmpty()) {
        ActiveConditionChip(
            label = stringResource(R.string.p1_priorities_selected, filter.priorities.size),
            onClear = { onFilterChange(filter.copy(priorities = emptySet())) },
        )
    }
    if (filter.hasDueRange ||
        filter.dateScope == TaskDateScope.UPCOMING ||
        filter.dateScope == TaskDateScope.NO_DATE
    ) {
        ActiveConditionChip(
            label = stringResource(
                when (filter.dateScope) {
                    TaskDateScope.UPCOMING -> R.string.p1_group_upcoming
                    TaskDateScope.NO_DATE -> R.string.p1_group_no_date
                    else -> R.string.p1_filter_due_range
                }
            ),
            onClear = {
                onFilterChange(
                    filter.copy(
                        dateScope = TaskDateScope.ALL,
                        dueStartMillis = null,
                        dueEndMillis = null,
                    )
                )
            },
        )
    }
    if (filter.starredOnly) {
        ActiveConditionChip(
            label = stringResource(R.string.p1_focus),
            onClear = { onFilterChange(filter.copy(starredOnly = false)) },
        )
    }
}

@Composable
private fun QuickDateFilterChip(
    label: String,
    scope: TaskDateScope,
    filter: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit,
) {
    GlassFilterChip(
        selected = filter.dateScope == scope,
        onClick = {
            onFilterChange(
                TaskFilter(
                    dateScope = if (filter.dateScope == scope) TaskDateScope.ALL else scope
                )
            )
        },
        style = GlassStyle.Dense,
        glassContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
        modifier = Modifier.height(28.dp),
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
        },
        trailingIcon = if (filter.dateScope == scope) {
            {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.p1_clear_condition),
                    modifier = Modifier.size(14.dp),
                )
            }
        } else {
            null
        },
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
    )
}

@Composable
private fun ActiveConditionChip(
    label: String,
    onClear: () -> Unit,
) {
    GlassFilterChip(
        selected = true,
        onClick = onClear,
        modifier = Modifier.height(28.dp),
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
        },
        trailingIcon = {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                contentDescription = stringResource(R.string.p1_clear_condition),
                modifier = Modifier.size(14.dp),
            )
        },
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
    )
}

@Composable
private fun CategoriesContent(
    categories: List<TodoCategory>,
    inboxCategory: TodoCategory?,
    inboxVisibleTasks: List<TodoTask>,
    themeIndexById: Map<String, Int>,
    tasksByCategory: Map<String, List<TaskWithCategory>>,
    tagsByName: Map<String, TodoTag>,
    showAddGroupCard: Boolean,
    isGridView: Boolean,
    isReorderMode: Boolean,
    component: MarkTodoComponent,
    modifier: Modifier = Modifier,
) {
    val lazyGridState = rememberLazyGridState()
    // 收集箱卡片出现在网格首位但不参与拖拽，把它占掉的 index 偏置补回来
    val inboxOffset = if (inboxCategory != null) 1 else 0
    val reorderableState = rememberReorderableLazyGridState(
        lazyGridState = lazyGridState,
        onMove = { from, to ->
            if (isReorderMode) {
                val fromIndex = from.index - inboxOffset
                val toIndex = to.index - inboxOffset
                if (fromIndex >= 0 && toIndex >= 0) {
                    component.handleEvent(
                        MarkTodoUiEvent.ReorderCategories(
                            fromIndex = fromIndex,
                            toIndex = toIndex,
                            visibleCategoryIds = categories.map { it.id },
                        )
                    )
                }
            }
        },
    )
    LazyVerticalGrid(
        columns = if (isGridView && categories.size + inboxOffset > 1) {
            GridCells.Adaptive(minSize = 180.dp)
        } else {
            GridCells.Fixed(1)
        },
        state = lazyGridState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = OneBoxDesignSystem.screenPadding,
            vertical = OneBoxDesignSystem.compactSpacing,
        ),
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing),
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing),
    ) {
        // 收集箱固定排第一张：未归组任务是随手记的默认落点，不参与拖拽排序
        if (inboxCategory != null) item(key = TodoCategory.INBOX_ID, contentType = "theme_group_card") {
            if (isGridView) {
                ThemeGroupGridCard(
                    category = inboxCategory,
                    themeIndex = 0,
                    visibleTasks = inboxVisibleTasks,
                    tagsByName = tagsByName,
                    isReorderMode = false,
                    showMenu = false,
                    onCategoryClick = {
                        component.handleEvent(MarkTodoUiEvent.CategoryClicked(it))
                    },
                    onTaskClick = { component.handleEvent(MarkTodoUiEvent.TaskClicked(it)) },
                    onToggleComplete = {
                        component.handleEvent(MarkTodoUiEvent.ToggleTaskComplete(it))
                    },
                    onEditCategory = {},
                    onDeleteCategory = {},
                    onTagClick = { component.handleEvent(MarkTodoUiEvent.TagOnTaskClicked(it)) },
                    onToggleStar = {
                        component.handleEvent(MarkTodoUiEvent.ToggleTaskStar(it))
                    },
                    onDuplicateTask = { component.handleEvent(MarkTodoUiEvent.DuplicateTask(it)) },
                    onDeleteTask = { component.handleEvent(MarkTodoUiEvent.DeleteTask(it)) },
                )
            } else {
                ThemeGroupCard(
                    category = inboxCategory,
                    themeIndex = 0,
                    visibleTasks = inboxVisibleTasks,
                    tagsByName = tagsByName,
                    showMenu = false,
                    isReorderMode = false,
                    onCategoryClick = {
                        component.handleEvent(MarkTodoUiEvent.CategoryClicked(it))
                    },
                    onTaskClick = { component.handleEvent(MarkTodoUiEvent.TaskClicked(it)) },
                    onToggleComplete = {
                        component.handleEvent(MarkTodoUiEvent.ToggleTaskComplete(it))
                    },
                    onEditCategory = {},
                    onDeleteCategory = {},
                    onTagClick = { component.handleEvent(MarkTodoUiEvent.TagOnTaskClicked(it)) },
                    onToggleStar = {
                        component.handleEvent(MarkTodoUiEvent.ToggleTaskStar(it))
                    },
                    onDuplicateTask = { component.handleEvent(MarkTodoUiEvent.DuplicateTask(it)) },
                    onDeleteTask = { component.handleEvent(MarkTodoUiEvent.DeleteTask(it)) },
                )
            }
        }
        items(items = categories, key = { it.id }, contentType = { "theme_group_card" }) { category ->
            ReorderableItem(
                state = reorderableState,
                key = category.id,
                enabled = isReorderMode && !category.isPinned,
            ) {
                val cardModifier = if (isReorderMode && !category.isPinned) {
                    Modifier.draggableHandle()
                } else {
                    Modifier
                }
                val visibleTasks = tasksByCategory[category.id].orEmpty().map { it.task }
                if (isGridView) {
                    ThemeGroupGridCard(
                        category = category,
                        themeIndex = themeIndexById[category.id] ?: 0,
                        visibleTasks = visibleTasks,
                        tagsByName = tagsByName,
                        isReorderMode = isReorderMode,
                        onEnterReorder = {
                            component.handleEvent(MarkTodoUiEvent.SetReorderMode(true))
                        },
                        onCategoryClick = {
                            component.handleEvent(MarkTodoUiEvent.CategoryClicked(it))
                        },
                        onTaskClick = { component.handleEvent(MarkTodoUiEvent.TaskClicked(it)) },
                        onToggleComplete = {
                            component.handleEvent(MarkTodoUiEvent.ToggleTaskComplete(it))
                        },
                        onToggleStar = { component.handleEvent(MarkTodoUiEvent.ToggleTaskStar(it)) },
                        onDuplicateTask = { component.handleEvent(MarkTodoUiEvent.DuplicateTask(it)) },
                    onDeleteTask = { component.handleEvent(MarkTodoUiEvent.DeleteTask(it)) },
                        onEditCategory = {
                            component.handleEvent(MarkTodoUiEvent.EditCategoryClicked(it))
                        },
                        onDeleteCategory = {
                            component.handleEvent(MarkTodoUiEvent.DeleteCategory(it))
                        },
                        onTagClick = { component.handleEvent(MarkTodoUiEvent.TagOnTaskClicked(it)) },
                        modifier = cardModifier,
                    )
                } else {
                    ThemeGroupCard(
                        category = category,
                        themeIndex = themeIndexById[category.id] ?: 0,
                        visibleTasks = visibleTasks,
                        tagsByName = tagsByName,
                        showMenu = !isReorderMode,
                        isReorderMode = isReorderMode,
                        onEnterReorder = {
                            component.handleEvent(MarkTodoUiEvent.SetReorderMode(true))
                        },
                        onCategoryClick = {
                            component.handleEvent(MarkTodoUiEvent.CategoryClicked(it))
                        },
                        onTaskClick = { component.handleEvent(MarkTodoUiEvent.TaskClicked(it)) },
                        onToggleComplete = {
                            component.handleEvent(MarkTodoUiEvent.ToggleTaskComplete(it))
                        },
                        onToggleStar = { component.handleEvent(MarkTodoUiEvent.ToggleTaskStar(it)) },
                        onDuplicateTask = { component.handleEvent(MarkTodoUiEvent.DuplicateTask(it)) },
                    onDeleteTask = { component.handleEvent(MarkTodoUiEvent.DeleteTask(it)) },
                        onEditCategory = {
                            component.handleEvent(MarkTodoUiEvent.EditCategoryClicked(it))
                        },
                        onDeleteCategory = {
                            component.handleEvent(MarkTodoUiEvent.DeleteCategory(it))
                        },
                        onTagClick = { component.handleEvent(MarkTodoUiEvent.TagOnTaskClicked(it)) },
                        modifier = cardModifier,
                    )
                }
            }
        }
        if (showAddGroupCard) {
            item(key = "add_group_card", contentType = "add_group_card") {
                GlassCard(
                    onClick = { component.handleEvent(MarkTodoUiEvent.AddCategoryClicked) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = com.wanbaohe.core.ui.widget.glass.tintedGlassCardColors(
                        MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(OneBoxDesignSystem.itemSpacing),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(
                            OneBoxDesignSystem.compactSpacing
                        ),
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = stringResource(R.string.action_add_category),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 底部状态页签：全部 / 进行中 / 已完成。
 *
 * 页签只过滤每张分组卡片里的任务，不改变分组本身；进行中 = 未完成的任务。
 */
@Composable
private fun StatusBottomBar(
    selectedTab: TaskCompletionFilter,
    onSelectTab: (TaskCompletionFilter) -> Unit,
) {
    val items = listOf(
        BottomNavItem(
            id = TaskCompletionFilter.ALL.name,
            label = stringResource(R.string.filter_all),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFeatures,
        ),
        BottomNavItem(
            id = TaskCompletionFilter.ACTIVE.name,
            label = stringResource(R.string.label_active),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAccessTime,
        ),
        BottomNavItem(
            id = TaskCompletionFilter.COMPLETED.name,
            label = stringResource(R.string.filter_completed),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCheckCircleOutline,
        ),
    )
    BottomNavigationBar(
        items = items,
        selectedItemId = selectedTab.name,
        onItemClick = { item -> onSelectTab(TaskCompletionFilter.valueOf(item.id)) },
        modifier = Modifier.fillMaxWidth(),
    )
}
