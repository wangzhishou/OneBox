package com.shifenmiao.marktodo.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.components.TodoTagChip
import com.shifenmiao.marktodo.components.formatTodoDate
import com.shifenmiao.marktodo.model.CategoryDetailUiEvent
import com.shifenmiao.marktodo.model.TaskFilterMode
import com.shifenmiao.marktodo.model.TodoCategory
import com.shifenmiao.marktodo.model.TodoTag
import com.shifenmiao.marktodo.model.TodoTask
import com.shifenmiao.marktodo.screenLogic.CategoryDetailComponent
import com.shifenmiao.marktodo.theme.categoryAccentColor
import com.shifenmiao.marktodo.theme.categoryCardTintColor
import com.shifenmiao.marktodo.theme.categoryAccentContainerColor
import com.shifenmiao.marktodo.theme.priorityColor
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.CheckCircle
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAccessTime
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCheckBoxBlank
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFlag
import com.t8rin.imagetoolbox.core.resources.icons.line.LineQuickTiles
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineViewList
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassLinearProgressIndicator
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.ui.widget.other.RevealDirection
import com.t8rin.imagetoolbox.core.ui.widget.other.RevealValue
import com.t8rin.imagetoolbox.core.ui.widget.other.SwipeToReveal
import com.t8rin.imagetoolbox.core.ui.widget.other.rememberRevealState
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDangerButton
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneSecondaryButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 主题详情页 —— 原型设计稿：
 * - 头部卡片：图标徽章 + 标题 + 任务数 + 圆头玻璃进度条 + 编辑/删除主题图标
 * - 统计筛选：全部 / 未完成 / 已完成 / 已标星
 * - 任务卡片：勾选 + 标题 + 星标 / 起止日期 / 标签（网格模式含备注）
 * - 任务左滑删除（SwipeToReveal），点击进编辑页
 * - 顶栏：返回 + 视图切换（单图标）+ 新增待办，无编辑模式
 */
@Composable
fun CategoryDetailScreen(
    component: CategoryDetailComponent,
    onGoBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val uiState by component.uiState.collectAsState()
    val showDeleteCategoryConfirm by component.showDeleteCategoryConfirm.collectAsState()

    var isShowGrid by remember { mutableStateOf(false) }

    val snackbarTaskDeleted = stringResource(R.string.snackbar_task_deleted)

    BaseScreen(
        title = {
            Text(
                modifier = Modifier.basicMarquee(),
                text = uiState.category?.title ?: "",
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        actions = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 视图切换（单图标，双列/单列图标，与首页一致）
                IconButton(onClick = { isShowGrid = !isShowGrid }) {
                    Icon(
                        imageVector = if (isShowGrid) {
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineViewList
                        } else {
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQuickTiles
                        },
                        contentDescription = if (isShowGrid) {
                            stringResource(R.string.cd_switch_to_list_view)
                        } else {
                            stringResource(R.string.cd_switch_to_grid_view)
                        },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // 新增任务
                IconButton(onClick = { component.handleEvent(CategoryDetailUiEvent.AddTaskClicked) }) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.action_add_task),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        isShowDefaultActions = false,
        onGoBack = onGoBack
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.category != null -> {
                    val accentColor = categoryAccentColor(uiState.category!!)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = OneBoxDesignSystem.screenPadding)
                    ) {
                        // 主题头部卡片
                        CategoryHeaderCard(
                            category = uiState.category!!,
                            accentColor = accentColor,
                            onEditCategory = {
                                component.handleEvent(CategoryDetailUiEvent.EditCategoryClicked)
                            },
                            onDeleteCategory = {
                                component.handleEvent(CategoryDetailUiEvent.DeleteCategoryClicked)
                            }
                        )

                        // 统计筛选
                        StatFilterRow(
                            category = uiState.category!!,
                            filterMode = uiState.filterMode,
                            accentColor = accentColor,
                            onFilterChange = {
                                component.handleEvent(CategoryDetailUiEvent.ChangeFilter(it))
                            },
                            modifier = Modifier.padding(vertical = OneBoxDesignSystem.blockSpacing)
                        )

                        // 任务列表（滑动删除）
                        TasksContent(
                            tasks = uiState.filteredTasks,
                            hasAnyTask = uiState.category!!.tasks.isNotEmpty(),
                            accentColor = accentColor,
                            tagsByName = remember(uiState.tags) { uiState.tags.associateBy { it.name } },
                            isShowGrid = isShowGrid,
                            onTaskClick = {
                                component.handleEvent(CategoryDetailUiEvent.TaskClicked(it))
                            },
                            onToggleComplete = {
                                component.handleEvent(CategoryDetailUiEvent.ToggleTaskComplete(it))
                            },
                            onToggleStar = {
                                component.handleEvent(CategoryDetailUiEvent.ToggleTaskStar(it))
                            },
                            onDeleteTask = { task ->
                                component.handleEvent(CategoryDetailUiEvent.DeleteTask(task))
                                scope.launch { AppToastHost.showToast(snackbarTaskDeleted) }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    // 删除主题确认
    if (showDeleteCategoryConfirm && uiState.category != null) {
        AlertDialog(
            containerColor = AppTheme.colors.getContainerSurfaceColor(),
            onDismissRequest = { component.handleEvent(CategoryDetailUiEvent.DismissDeleteCategory) },
            title = { Text(stringResource(R.string.dialog_delete_category_title)) },
            text = {
                Text(stringResource(R.string.dialog_delete_category_message, uiState.category!!.title))
            },
            confirmButton = {
                OneBoxDangerButton(
                    text = stringResource(R.string.action_delete),
                    onClick = { component.handleEvent(CategoryDetailUiEvent.ConfirmDeleteCategory) }
                )
            },
            dismissButton = {
                OneSecondaryButton(
                    text = stringResource(R.string.action_cancel),
                    onClick = { component.handleEvent(CategoryDetailUiEvent.DismissDeleteCategory) }
                )
            }
        )
    }

    BackHandler {
        onGoBack()
    }
}

/**
 * 主题头部卡片：图标徽章 + 标题 + 任务数 + 进度条 + 编辑/删除主题（卡片按主题色系染色）
 */
@Composable
private fun CategoryHeaderCard(
    category: TodoCategory,
    accentColor: Color,
    onEditCategory: () -> Unit,
    onDeleteCategory: () -> Unit
) {
    val accentContainerColor = categoryAccentContainerColor(category)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = categoryCardTintColor(category)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(OneBoxDesignSystem.itemSpacing)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)
            ) {
                // 图标徽章
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(OneBoxDesignSystem.smallRadius))
                        .background(accentContainerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.category_task_count, category.totalCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 编辑主题
                IconButton(onClick = onEditCategory, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.action_edit),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                // 删除主题
                IconButton(onClick = onDeleteCategory, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.action_delete),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // 进度条 + 百分比
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = OneBoxDesignSystem.itemSpacing),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)
            ) {
                GlassLinearProgressIndicator(
                    progress = { category.progressPercentage / 100f },
                    color = accentColor,
                    trackColor = accentColor.copy(alpha = 0.15f),
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                )
                Text(
                    text = stringResource(R.string.task_progress_percent, category.progressPercentage),
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor.copy(alpha = 0.85f)
                )
            }
        }
    }
}

/**
 * 统计筛选行：全部 / 未完成 / 已完成 / 已标星（选中态用主题色系）
 */
@Composable
private fun StatFilterRow(
    category: TodoCategory,
    filterMode: TaskFilterMode,
    accentColor: Color,
    onFilterChange: (TaskFilterMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val starredCount = remember(category.tasks) { category.tasks.count { it.isStarred } }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
    ) {
        StatFilterItem(
            label = stringResource(R.string.filter_all),
            count = category.totalCount,
            isSelected = filterMode == TaskFilterMode.ALL,
            accentColor = accentColor,
            onClick = { onFilterChange(TaskFilterMode.ALL) },
            modifier = Modifier.weight(1f)
        )
        StatFilterItem(
            label = stringResource(R.string.filter_active),
            count = category.totalCount - category.completedCount,
            isSelected = filterMode == TaskFilterMode.ACTIVE,
            accentColor = accentColor,
            onClick = { onFilterChange(TaskFilterMode.ACTIVE) },
            modifier = Modifier.weight(1f)
        )
        StatFilterItem(
            label = stringResource(R.string.filter_completed),
            count = category.completedCount,
            isSelected = filterMode == TaskFilterMode.COMPLETED,
            accentColor = accentColor,
            onClick = { onFilterChange(TaskFilterMode.COMPLETED) },
            modifier = Modifier.weight(1f)
        )
        StatFilterItem(
            label = stringResource(R.string.filter_starred),
            count = starredCount,
            isSelected = filterMode == TaskFilterMode.STARRED,
            accentColor = accentColor,
            onClick = { onFilterChange(TaskFilterMode.STARRED) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatFilterItem(
    label: String,
    count: Int,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .glassBackground(
                style = if (isSelected) GlassStyle.Dense else GlassStyle.Regular,
                color = if (isSelected) {
                    accentColor.copy(alpha = 0.22f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                },
                shape = RoundedCornerShape(OneBoxDesignSystem.mediumRadius)
            )
            .clickable(onClick = onClick)
            .padding(vertical = OneBoxDesignSystem.compactSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) {
                accentColor
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) {
                accentColor.copy(alpha = 0.8f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

/**
 * 任务内容区：列表 / 网格，任务卡片支持左滑删除
 */
@Composable
private fun TasksContent(
    tasks: List<TodoTask>,
    hasAnyTask: Boolean,
    accentColor: Color,
    tagsByName: Map<String, TodoTag>,
    isShowGrid: Boolean,
    onTaskClick: (TodoTask) -> Unit,
    onToggleComplete: (TodoTask) -> Unit,
    onToggleStar: (TodoTask) -> Unit,
    onDeleteTask: (TodoTask) -> Unit,
    modifier: Modifier = Modifier
) {
    if (tasks.isEmpty()) {
        // 区分「还没有待办」与「当前筛选无结果」
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
            ) {
                Text(
                    text = stringResource(
                        if (hasAnyTask) R.string.empty_filtered_tasks else R.string.empty_tasks
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!hasAnyTask) {
                    Text(
                        text = stringResource(R.string.empty_tasks_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        return
    }

    val lazyGridState = rememberLazyGridState()
    val columns = if (isShowGrid) GridCells.Fixed(2) else GridCells.Fixed(1)

    LazyVerticalGrid(
        modifier = modifier.fillMaxSize(),
        state = lazyGridState,
        columns = columns,
        contentPadding = PaddingValues(bottom = OneBoxDesignSystem.blockSpacing),
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing),
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing)
    ) {
        items(items = tasks, key = { it.id }, contentType = { "task_card" }) { task ->
            SwipeableTaskCard(
                task = task,
                accentColor = accentColor,
                tagsByName = tagsByName,
                onClick = { onTaskClick(task) },
                onToggleComplete = { onToggleComplete(task) },
                onToggleStar = { onToggleStar(task) },
                onDelete = { onDeleteTask(task) }
            )
        }
    }
}

/**
 * 任务卡片 + 左滑删除（SwipeToReveal 滑出红色删除区，点删除图标删除该待办）
 */
@Composable
private fun SwipeableTaskCard(
    task: TodoTask,
    accentColor: Color,
    tagsByName: Map<String, TodoTag>,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    onToggleStar: () -> Unit,
    onDelete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val revealState = rememberRevealState()

    SwipeToReveal(
        directions = setOf(RevealDirection.EndToStart),
        maxRevealDp = 72.dp,
        state = revealState,
        // 玻璃卡片半透明，删除区需要随手势渐入，否则会透出来
        alphaTransformEnabled = true,
        revealedContentEnd = {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(start = OneBoxDesignSystem.compactSpacing)
                    .width(72.dp)
                    .clip(RoundedCornerShape(OneBoxDesignSystem.mediumRadius))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .clickable {
                        scope.launch { revealState.animateTo(RevealValue.Default) }
                        onDelete()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        swipeableContent = {
            TaskCardContent(
                task = task,
                accentColor = accentColor,
                tagsByName = tagsByName,
                onClick = onClick,
                onToggleComplete = onToggleComplete,
                onToggleStar = onToggleStar
            )
        }
    )
}

/**
 * 任务卡片内容：勾选 + 标题 + 星标 / 起止日期 / 标签（网格模式含备注）
 */
@Composable
private fun TaskCardContent(
    task: TodoTask,
    accentColor: Color,
    tagsByName: Map<String, TodoTag>,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    onToggleStar: () -> Unit
) {
    val contentAlpha = if (task.isCompleted) 0.5f else 1f

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(OneBoxDesignSystem.itemSpacing),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing)
        ) {
            // 第一行：勾选 + 标题 + 星标
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
            ) {
                Icon(
                    imageVector = if (task.isCompleted) {
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.CheckCircle
                    } else {
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCheckBoxBlank
                    },
                    contentDescription = if (task.isCompleted) {
                        stringResource(R.string.cd_task_completed)
                    } else {
                        stringResource(R.string.cd_task_pending)
                    },
                    tint = accentColor.copy(alpha = 0.68f),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(
                            interactionSource = null,
                            indication = null
                        ) { onToggleComplete() }
                )
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStar,
                    contentDescription = if (task.isStarred) {
                        stringResource(R.string.cd_remove_star)
                    } else {
                        stringResource(R.string.cd_add_star)
                    },
                    tint = if (task.isStarred) {
                        accentColor
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    },
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(
                            interactionSource = null,
                            indication = null
                        ) { onToggleStar() }
                )
            }

            // 第二行：开始日期 · 截止日期
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = formatMonthDay(task.startDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                task.dueDate?.let { due ->
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        text = formatTodoDate(due),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (task.isOverdue) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        }
                    )
                }
            }

            // 备注（有则显示，最多 2 行）
            if (!task.note.isNullOrBlank()) {
                Text(
                    text = task.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 标签 + 优先级旗帜（中优先级不显示旗帜，保持安静）
            val showPriority = task.priority != TodoTask.PRIORITY_MEDIUM
            if (task.tags.isNotEmpty() || showPriority) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing)
                ) {
                    if (showPriority) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFlag,
                            contentDescription = null,
                            tint = priorityColor(task.priority),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    task.tags.take(2).forEach { tagName ->
                        TodoTagChip(tagName = tagName, tag = tagsByName[tagName])
                    }
                }
            }
        }
    }
}

private fun formatMonthDay(timestamp: Long): String {
    return SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(timestamp))
}
