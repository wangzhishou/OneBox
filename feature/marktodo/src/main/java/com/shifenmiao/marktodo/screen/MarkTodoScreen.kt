package com.shifenmiao.marktodo.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.shifenmiao.marktodo.model.HomeView
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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFeatures
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFilterAlt
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInbox
import com.t8rin.imagetoolbox.core.resources.icons.line.LineQuickTiles
import com.t8rin.imagetoolbox.core.resources.icons.line.LineViewList
import com.wanbaohe.core.ui.widget.glass.GlassFilterChip
import com.wanbaohe.core.ui.widget.glass.GlassSearchTextField
import com.wanbaohe.core.ui.widget.navigation.BottomNavItem
import com.wanbaohe.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.core.ui.widget.system.OneBoxDangerButton
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState

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
    val scopedTasks = remember(flattened, uiState.homeView, uiState.searchQuery) {
        if (uiState.searchQuery.isBlank() && uiState.homeView == HomeView.INBOX) {
            flattened.filter { it.task.categoryId == null }
        } else {
            flattened
        }
    }
    val visibleTasks = remember(
        scopedTasks,
        uiState.searchQuery,
        uiState.filter,
        uiState.sort,
        uiState.pendingCompletionIds,
    ) {
        TodoTaskQuery.filterAndSort(
            tasks = scopedTasks,
            searchQuery = uiState.searchQuery,
            filter = uiState.filter,
            sort = uiState.sort,
            retainedCompletedTaskIds = uiState.pendingCompletionIds,
        )
    }
    val todayGroups = remember(visibleTasks) { TodoTaskQuery.groupForToday(visibleTasks) }
    val tasksByCategory = remember(visibleTasks) {
        visibleTasks.filter { it.task.categoryId != null }.groupBy { it.task.categoryId!! }
    }
    val hasNarrowingQuery = uiState.searchQuery.isNotBlank() ||
        uiState.filter.completion != TaskCompletionFilter.ALL ||
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
                isGridView = uiState.isGridView,
                isReorderMode = uiState.isReorderMode,
                showViewMode = uiState.homeView == HomeView.THEMES &&
                    uiState.searchQuery.isBlank() &&
                    !uiState.filter.hasSmartDateFilter,
                onViewModeChange = {
                    markTodoComponent.handleEvent(MarkTodoUiEvent.SetGridView(it))
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = OneBoxDesignSystem.screenPadding,
                        end = OneBoxDesignSystem.microSpacing,
                        top = OneBoxDesignSystem.compactSpacing,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlassSearchTextField(
                    value = uiState.searchQuery,
                    onValueChange = {
                        markTodoComponent.handleEvent(MarkTodoUiEvent.SearchQueryChanged(it))
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = stringResource(R.string.p1_search_tasks),
                )
                IconButton(
                    onClick = {
                        markTodoComponent.handleEvent(MarkTodoUiEvent.ShowFilterSortSheet)
                    },
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFilterAlt,
                        contentDescription = stringResource(R.string.p1_filter_sort),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            ActiveQueryChips(
                filter = uiState.filter,
                sort = uiState.sort,
                onFilterChange = {
                    markTodoComponent.handleEvent(MarkTodoUiEvent.SetFilter(it))
                },
                onSortChange = {
                    markTodoComponent.handleEvent(MarkTodoUiEvent.SetSort(it))
                },
                onReset = {
                    markTodoComponent.handleEvent(MarkTodoUiEvent.ResetQuery)
                },
            )

            val contentModifier = Modifier.weight(1f)
            when {
                uiState.searchQuery.isNotBlank() -> {
                    if (visibleTasks.isEmpty()) {
                        TaskListEmptyState(
                            message = stringResource(R.string.p1_empty_search),
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
                uiState.homeView == HomeView.INBOX -> {
                    if (visibleTasks.isEmpty()) {
                        TaskListEmptyState(
                            message = stringResource(R.string.p1_empty_inbox),
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
                else -> CategoriesContent(
                    categories = visibleCategories,
                    hasAnyCategory = fullCategories.isNotEmpty(),
                    tasksByCategory = tasksByCategory,
                    tagsByName = tagsByName,
                    isGridView = uiState.isGridView,
                    isReorderMode = uiState.isReorderMode,
                    component = markTodoComponent,
                    modifier = contentModifier,
                )
            }

            AnimatedVisibility(visible = !uiState.isReorderMode) {
                HomeBottomBar(
                    selectedView = uiState.homeView,
                    onSelectView = {
                        markTodoComponent.handleEvent(MarkTodoUiEvent.SelectHomeView(it))
                    },
                )
            }
        }
    }

    QuickAddTodoSheet(markTodoComponent.quickAddTodoComponent)

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
    isGridView: Boolean,
    isReorderMode: Boolean,
    showViewMode: Boolean,
    onViewModeChange: (Boolean) -> Unit,
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
        if (showViewMode) {
            IconButton(onClick = { onViewModeChange(!isGridView) }) {
                Icon(
                    imageVector = if (isGridView) {
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineViewList
                    } else {
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQuickTiles
                    },
                    contentDescription = stringResource(
                        if (isGridView) R.string.cd_switch_to_list_view else R.string.cd_switch_to_grid_view
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
                    onDelete = { component.handleEvent(MarkTodoUiEvent.DeleteTask(it)) },
                    onTagClick = { component.handleEvent(MarkTodoUiEvent.TagOnTaskClicked(it)) },
                )
            }
        }
    }
}

@Composable
private fun ActiveQueryChips(
    filter: TaskFilter,
    sort: TaskSortConfig,
    onFilterChange: (TaskFilter) -> Unit,
    onSortChange: (TaskSortConfig) -> Unit,
    onReset: () -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            horizontal = OneBoxDesignSystem.screenPadding,
            vertical = OneBoxDesignSystem.microSpacing,
        ),
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (filter.completion != TaskCompletionFilter.ALL) {
            item(key = "completion") {
                ActiveConditionChip(
                    label = stringResource(
                        if (filter.completion == TaskCompletionFilter.ACTIVE) {
                            R.string.filter_active
                        } else {
                            R.string.filter_completed
                        }
                    ),
                    onClear = { onFilterChange(filter.copy(completion = TaskCompletionFilter.ALL)) },
                )
            }
        }
        if (filter.hasCategoryFilter) {
            item(key = "categories") {
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
        }
        if (filter.tagNames.isNotEmpty()) {
            item(key = "tags") {
                ActiveConditionChip(
                    label = stringResource(R.string.p1_tags_selected, filter.tagNames.size),
                    onClear = { onFilterChange(filter.copy(tagNames = emptySet())) },
                )
            }
        }
        if (filter.priorities.isNotEmpty()) {
            item(key = "priorities") {
                ActiveConditionChip(
                    label = stringResource(R.string.p1_priorities_selected, filter.priorities.size),
                    onClear = { onFilterChange(filter.copy(priorities = emptySet())) },
                )
            }
        }
        if (filter.hasDateFilter) {
            item(key = "date_scope") {
                ActiveConditionChip(
                    label = stringResource(
                        when (filter.dateScope) {
                            TaskDateScope.TODAY -> R.string.p1_today
                            TaskDateScope.OVERDUE -> R.string.p1_group_overdue
                            TaskDateScope.UPCOMING -> R.string.p1_group_upcoming
                            TaskDateScope.NO_DATE -> R.string.p1_group_no_date
                            TaskDateScope.ALL, TaskDateScope.RANGE -> R.string.p1_filter_due_range
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
        }
        if (filter.starredOnly) {
            item(key = "focus") {
                ActiveConditionChip(
                    label = stringResource(R.string.p1_focus),
                    onClear = { onFilterChange(filter.copy(starredOnly = false)) },
                )
            }
        }
        if (sort != TaskSortConfig()) {
            item(key = "sort") {
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
        if (filter != TaskFilter() || sort != TaskSortConfig()) {
            item(key = "reset") {
                GlassFilterChip(
                    selected = false,
                    onClick = onReset,
                    label = { Text(stringResource(R.string.p1_reset)) },
                )
            }
        }
    }
}

@Composable
private fun ActiveConditionChip(
    label: String,
    onClear: () -> Unit,
) {
    GlassFilterChip(
        selected = true,
        onClick = onClear,
        label = { Text(label) },
        trailingIcon = {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                contentDescription = stringResource(R.string.p1_clear_condition),
                modifier = Modifier.size(14.dp),
            )
        },
    )
}

@Composable
private fun CategoriesContent(
    categories: List<TodoCategory>,
    hasAnyCategory: Boolean,
    tasksByCategory: Map<String, List<TaskWithCategory>>,
    tagsByName: Map<String, TodoTag>,
    isGridView: Boolean,
    isReorderMode: Boolean,
    component: MarkTodoComponent,
    modifier: Modifier = Modifier,
) {
    if (categories.isEmpty()) {
        TaskListEmptyState(
            message = stringResource(
                if (hasAnyCategory) R.string.empty_filtered_tasks else R.string.p1_empty_themes
            ),
            modifier = modifier,
        )
        return
    }

    val lazyGridState = rememberLazyGridState()
    val reorderableState = rememberReorderableLazyGridState(
        lazyGridState = lazyGridState,
        onMove = { from, to ->
            if (isReorderMode) {
                component.handleEvent(
                    MarkTodoUiEvent.ReorderCategories(
                        fromIndex = from.index,
                        toIndex = to.index,
                        visibleCategoryIds = categories.map { it.id },
                    )
                )
            }
        },
    )
    LazyVerticalGrid(
        columns = if (isGridView) GridCells.Adaptive(minSize = 180.dp) else GridCells.Fixed(1),
        state = lazyGridState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = OneBoxDesignSystem.screenPadding,
            vertical = OneBoxDesignSystem.compactSpacing,
        ),
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing),
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing),
    ) {
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
    }
}

@Composable
private fun HomeBottomBar(
    selectedView: HomeView,
    onSelectView: (HomeView) -> Unit,
) {
    val items = listOf(
        BottomNavItem(
            id = HomeView.THEMES.name,
            label = stringResource(R.string.p1_themes),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFeatures,
        ),
        BottomNavItem(
            id = HomeView.INBOX.name,
            label = stringResource(R.string.p1_inbox),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInbox,
        ),
    )
    BottomNavigationBar(
        items = items,
        selectedItemId = selectedView.name,
        onItemClick = { item -> onSelectView(HomeView.valueOf(item.id)) },
        modifier = Modifier.fillMaxWidth(),
    )
}
