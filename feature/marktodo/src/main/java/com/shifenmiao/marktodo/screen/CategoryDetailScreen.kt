package com.shifenmiao.marktodo.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.components.QuickAddTodoSheet
import com.shifenmiao.marktodo.components.TaskFilterSortSheet
import com.shifenmiao.marktodo.components.TaskListContent
import com.shifenmiao.marktodo.components.TaskListEmptyState
import com.shifenmiao.marktodo.domain.TodoTaskQuery
import com.shifenmiao.marktodo.model.CategoryDetailUiEvent
import com.shifenmiao.marktodo.model.TaskCompletionFilter
import com.shifenmiao.marktodo.model.TaskFilter
import com.shifenmiao.marktodo.model.TaskFilterMode
import com.shifenmiao.marktodo.model.TodoCategory
import com.shifenmiao.marktodo.screenLogic.CategoryDetailComponent
import com.shifenmiao.marktodo.theme.categoryAccentColor
import com.shifenmiao.marktodo.theme.categoryAccentContainerColor
import com.shifenmiao.marktodo.theme.categoryCardTintColor
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineQuickTiles
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSort
import com.t8rin.imagetoolbox.core.resources.icons.line.LineViewList
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassFilterChip
import com.wanbaohe.core.ui.widget.glass.GlassLinearProgressIndicator
import com.wanbaohe.core.ui.widget.system.OneBoxDangerButton
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton

@Composable
fun CategoryDetailScreen(
    component: CategoryDetailComponent,
    onGoBack: () -> Unit,
) {
    val uiState by component.uiState.collectAsState()
    val showDeleteCategoryConfirm by component.showDeleteCategoryConfirm.collectAsState()
    var isShowGrid by remember { mutableStateOf(false) }

    val category = uiState.category
    val visibleTasks = remember(
        category,
        uiState.filterMode,
        uiState.sort,
        uiState.pendingCompletionIds,
    ) {
        val source = category?.let { TodoTaskQuery.flatten(it.tasks, listOf(it)) }.orEmpty()
        val filter = when (uiState.filterMode) {
            TaskFilterMode.ALL -> TaskFilter(completion = TaskCompletionFilter.ALL)
            TaskFilterMode.ACTIVE -> TaskFilter(completion = TaskCompletionFilter.ACTIVE)
            TaskFilterMode.COMPLETED -> TaskFilter(completion = TaskCompletionFilter.COMPLETED)
            TaskFilterMode.STARRED -> TaskFilter(
                completion = TaskCompletionFilter.ALL,
                starredOnly = true,
            )
        }
        TodoTaskQuery.filterAndSort(
            tasks = source,
            filter = filter,
            sort = uiState.sort,
            retainedCompletedTaskIds = uiState.pendingCompletionIds,
        )
    }

    BaseScreen(
        title = {
            Text(
                modifier = Modifier.basicMarquee(),
                text = category?.title.orEmpty(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        actions = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { component.handleEvent(CategoryDetailUiEvent.ShowSortSheet) }) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSort,
                        contentDescription = stringResource(R.string.action_sort),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                IconButton(onClick = { isShowGrid = !isShowGrid }) {
                    Icon(
                        imageVector = if (isShowGrid) {
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineViewList
                        } else {
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQuickTiles
                        },
                        contentDescription = stringResource(
                            if (isShowGrid) R.string.cd_switch_to_list_view else R.string.cd_switch_to_grid_view
                        ),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { component.handleEvent(CategoryDetailUiEvent.AddTaskClicked) }) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.action_add_task),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        isShowDefaultActions = false,
        onGoBack = onGoBack,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
                uiState.error != null -> Text(
                    text = uiState.error!!,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.align(Alignment.Center),
                )
                category != null -> Column(modifier = Modifier.fillMaxSize()) {
                    CategoryHeaderCard(
                        category = category,
                        accentColor = categoryAccentColor(category),
                        onEditCategory = {
                            component.handleEvent(CategoryDetailUiEvent.EditCategoryClicked)
                        },
                        onDeleteCategory = {
                            component.handleEvent(CategoryDetailUiEvent.DeleteCategoryClicked)
                        },
                        modifier = Modifier.padding(horizontal = OneBoxDesignSystem.screenPadding),
                    )
                    StatFilterRow(
                        category = category,
                        filterMode = uiState.filterMode,
                        onFilterChange = {
                            component.handleEvent(CategoryDetailUiEvent.ChangeFilter(it))
                        },
                        modifier = Modifier.padding(vertical = OneBoxDesignSystem.compactSpacing),
                    )
                    if (visibleTasks.isEmpty()) {
                        TaskListEmptyState(
                            message = stringResource(
                                if (category.tasks.isEmpty()) {
                                    R.string.empty_tasks
                                } else {
                                    R.string.empty_filtered_tasks
                                }
                            ),
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        TaskListContent(
                            tasks = visibleTasks,
                            tagsByName = remember(uiState.tags) {
                                uiState.tags.associateBy { it.name }
                            },
                            onTaskClick = {
                                component.handleEvent(CategoryDetailUiEvent.TaskClicked(it))
                            },
                            onToggleComplete = {
                                component.handleEvent(CategoryDetailUiEvent.ToggleTaskComplete(it))
                            },
                            onToggleStar = {
                                component.handleEvent(CategoryDetailUiEvent.ToggleTaskStar(it))
                            },
                            onDelete = {
                                component.handleEvent(CategoryDetailUiEvent.DeleteTask(it))
                            },
                            showCategory = false,
                            isGrid = isShowGrid,
                            enableSwipeDelete = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }

    QuickAddTodoSheet(component.quickAddTodoComponent)

    if (uiState.isSortSheetVisible) {
        TaskFilterSortSheet(
            visible = true,
            categories = emptyList(),
            tags = emptyList(),
            initialFilter = TaskFilter(),
            initialSort = uiState.sort,
            onDismiss = { component.handleEvent(CategoryDetailUiEvent.DismissSortSheet) },
            onApply = { _, sort ->
                component.handleEvent(CategoryDetailUiEvent.ChangeSort(sort))
            },
            showFilters = false,
        )
    }

    if (showDeleteCategoryConfirm && category != null) {
        AlertDialog(
            containerColor = AppTheme.colors.getContainerSurfaceColor(),
            onDismissRequest = {
                component.handleEvent(CategoryDetailUiEvent.DismissDeleteCategory)
            },
            title = { Text(stringResource(R.string.dialog_delete_category_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.p1_delete_category_message_count,
                        category.title,
                        category.totalCount,
                    )
                )
            },
            confirmButton = {
                OneBoxDangerButton(
                    text = stringResource(R.string.action_delete),
                    enabled = !uiState.isDeletingCategory,
                    onClick = { component.handleEvent(CategoryDetailUiEvent.ConfirmDeleteCategory) },
                )
            },
            dismissButton = {
                OneSecondaryButton(
                    text = stringResource(R.string.action_cancel),
                    enabled = !uiState.isDeletingCategory,
                    onClick = { component.handleEvent(CategoryDetailUiEvent.DismissDeleteCategory) },
                )
            },
        )
    }

    BackHandler(enabled = !uiState.isSortSheetVisible) {
        onGoBack()
    }
}

@Composable
private fun CategoryHeaderCard(
    category: TodoCategory,
    accentColor: Color,
    onEditCategory: () -> Unit,
    onDeleteCategory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = categoryCardTintColor(category)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(OneBoxDesignSystem.itemSpacing),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(OneBoxDesignSystem.smallRadius))
                        .background(categoryAccentContainerColor(category)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(R.string.category_task_count, category.totalCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onEditCategory, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.action_edit),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp),
                    )
                }
                IconButton(onClick = onDeleteCategory, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.action_delete),
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = OneBoxDesignSystem.compactSpacing),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
            ) {
                GlassLinearProgressIndicator(
                    progress = { category.progressPercentage / 100f },
                    color = accentColor,
                    trackColor = accentColor.copy(alpha = 0.15f),
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp),
                )
                Text(
                    text = stringResource(R.string.task_progress_percent, category.progressPercentage),
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                )
            }
        }
    }
}

@Composable
private fun StatFilterRow(
    category: TodoCategory,
    filterMode: TaskFilterMode,
    onFilterChange: (TaskFilterMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(
        TaskFilterMode.ALL to Pair(R.string.filter_all, category.totalCount),
        TaskFilterMode.ACTIVE to Pair(
            R.string.filter_active,
            category.totalCount - category.completedCount,
        ),
        TaskFilterMode.COMPLETED to Pair(R.string.filter_completed, category.completedCount),
        TaskFilterMode.STARRED to Pair(
            R.string.p1_focus,
            category.tasks.count { it.isStarred },
        ),
    )
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = OneBoxDesignSystem.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
    ) {
        options.forEach { (mode, labelAndCount) ->
            item(key = mode.name) {
                GlassFilterChip(
                    selected = filterMode == mode,
                    onClick = { onFilterChange(mode) },
                    label = {
                        Text(
                            text = stringResource(
                                R.string.p1_filter_count,
                                stringResource(labelAndCount.first),
                                labelAndCount.second,
                            )
                        )
                    },
                )
            }
        }
    }
}
