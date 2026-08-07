package com.shifenmiao.marktodo.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Add
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.components.TaskItemDetailed
import com.shifenmiao.marktodo.model.CategoryDetailUiEvent
import com.shifenmiao.marktodo.model.TaskFilterMode
import com.shifenmiao.marktodo.model.TaskSortMode
import com.shifenmiao.marktodo.model.TodoCategory
import com.shifenmiao.marktodo.model.TodoTask
import com.shifenmiao.marktodo.screenLogic.CategoryDetailComponent
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFeatures

/**
 * 分类详情页面，展示单个分类下的所有任务
 *
 * 特性：
 * - 细粒度状态管理，最小化重组
 * - 支持任务筛选和排序
 * - 高性能任务列表
 * - 极致扁平化设计，与首页风格一致
 * - 使用系统主题色
 * - 支持编辑模式（批量删除）
 */
@Composable
fun CategoryDetailScreen(
    component: CategoryDetailComponent,
    onGoBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // 状态收集
    val uiState by component.uiState.collectAsState()

    // 编辑模式状态
    var isEditMode by remember { mutableStateOf(false) }
    var selectedTasks by remember { mutableStateOf(setOf<String>()) }

    // 网格布局状态
    var isShowGrid by remember { mutableStateOf(false) }

    // 字符串资源
    val snackbarTaskDeleted = stringResource(R.string.snackbar_task_deleted)

    BaseScreen(
        title = {
            Text(
                modifier = Modifier.basicMarquee(),
                text = uiState.category?.title ?: "",
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        navigationIcon = {
            Row {
                // 返回按钮
                IconButton(onClick = {
                    if (isEditMode) {
                        isEditMode = false
                        selectedTasks = emptySet()
                    } else {
                        onGoBack()
                    }
                }) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                // 布局切换按钮
                IconButton(onClick = { isShowGrid = !isShowGrid }) {
                    Icon(
                        imageVector = if (isShowGrid) Icons.AutoMirrored.Filled.ViewList else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFeatures,
                        contentDescription = if (isShowGrid) stringResource(R.string.cd_switch_to_list_view) else stringResource(R.string.cd_switch_to_grid_view)
                    )
                }
            }
        },
        actions = {
            CategoryDetailActions(
                isEditMode = isEditMode,
                hasSelection = selectedTasks.isNotEmpty(),
                onToggleEditMode = {
                    isEditMode = !isEditMode
                    if (!isEditMode) {
                        selectedTasks = emptySet()
                    }
                },
                onAddTask = {
                    component.handleEvent(CategoryDetailUiEvent.AddTaskClicked)
                },
                onDeleteSelected = {
                    // 批量删除选中的任务
                    val deletedCount = selectedTasks.size
                    selectedTasks.forEach { taskId ->
                        uiState.filteredTasks.find { it.id == taskId }?.let { task ->
                            component.handleEvent(CategoryDetailUiEvent.DeleteTask(task))
                        }
                    }
                    selectedTasks = emptySet()
                    isEditMode = false
                    scope.launch {
                        AppToastHost.showToast(context.getString(R.string.snackbar_batch_tasks_deleted, deletedCount))
                    }
                }
            )
        },
        isShowDefaultActions = false,
        onGoBack = {
            if (isEditMode) {
                isEditMode = false
                selectedTasks = emptySet()
            } else {
                onGoBack()
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    LoadingState()
                }
                uiState.error != null -> {
                    ErrorState(error = uiState.error!!)
                }
                uiState.category != null -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // 统计信息和筛选栏
                        CategoryHeader(
                            category = uiState.category!!,
                            filterMode = uiState.filterMode,
                            onFilterChange = { filter ->
                                component.handleEvent(CategoryDetailUiEvent.ChangeFilter(filter))
                            }
                        )

                        // 任务列表
                        TasksList(
                            tasks = uiState.filteredTasks,
                            isEditMode = isEditMode,
                            isShowGrid = isShowGrid,
                            sortMode = uiState.sortMode,
                            selectedTasks = selectedTasks,
                            onTaskClick = { task ->
                                if (isEditMode) {
                                    selectedTasks = if (task.id in selectedTasks) {
                                        selectedTasks - task.id
                                    } else {
                                        selectedTasks + task.id
                                    }
                                } else {
                                    component.handleEvent(CategoryDetailUiEvent.TaskClicked(task))
                                }
                            },
                            onToggleComplete = { task ->
                                component.handleEvent(CategoryDetailUiEvent.ToggleTaskComplete(task))
                            },
                            onToggleStar = { task ->
                                component.handleEvent(CategoryDetailUiEvent.ToggleTaskStar(task))
                            },
                            onDeleteTask = { task ->
                                component.handleEvent(CategoryDetailUiEvent.DeleteTask(task))
                                scope.launch {
                                    AppToastHost.showToast(snackbarTaskDeleted)
                                }
                            },
                            onReorder = { from, to ->
                                component.handleEvent(CategoryDetailUiEvent.ReorderTasks(from, to))
                            }
                        )
                    }
                }
            }
        }
    }

    // 返回处理
    BackHandler(enabled = isEditMode) {
        isEditMode = false
        selectedTasks = emptySet()
    }
}

/**
 * 工具栏操作按钮 - 极致扁平化设计
 */
@Composable
private fun CategoryDetailActions(
    isEditMode: Boolean,
    hasSelection: Boolean,
    onToggleEditMode: () -> Unit,
    onAddTask: () -> Unit,
    onDeleteSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // 编辑模式：显示删除按钮
        if (isEditMode) {
            // 删除选中项按钮
            IconButton(
                onClick = onDeleteSelected,
                enabled = hasSelection
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.action_delete_selected_cd),
                    tint = if (hasSelection) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            // 取消按钮
            IconButton(onClick = onToggleEditMode) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.action_cancel),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // 正常模式：添加任务和编辑按钮
            IconButton(onClick = onToggleEditMode) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.action_edit),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onAddTask) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.action_add_task),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 分类头部信息，包含统计和快速筛选
 */
@Composable
private fun CategoryHeader(
    category: TodoCategory,
    filterMode: TaskFilterMode,
    onFilterChange: (TaskFilterMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val starredCount = remember(category.tasks) {
        category.tasks.count { it.isStarred }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatFilterItem(
            label = stringResource(R.string.filter_all),
            count = category.totalCount,
            isSelected = filterMode == TaskFilterMode.ALL,
            onClick = { onFilterChange(TaskFilterMode.ALL) },
            modifier = Modifier.weight(1f)
        )
        StatFilterItem(
            label = stringResource(R.string.filter_active),
            count = category.totalCount - category.completedCount,
            isSelected = filterMode == TaskFilterMode.ACTIVE,
            onClick = { onFilterChange(TaskFilterMode.ACTIVE) },
            modifier = Modifier.weight(1f)
        )
        StatFilterItem(
            label = stringResource(R.string.filter_completed),
            count = category.completedCount,
            isSelected = filterMode == TaskFilterMode.COMPLETED,
            onClick = { onFilterChange(TaskFilterMode.COMPLETED) },
            modifier = Modifier.weight(1f)
        )
        StatFilterItem(
            label = stringResource(R.string.filter_starred),
            count = starredCount,
            isSelected = filterMode == TaskFilterMode.STARRED,
            onClick = { onFilterChange(TaskFilterMode.STARRED) },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 统计项组件 - 兼具筛选功能
 */
@Composable
private fun StatFilterItem(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    val contentColorFinal = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier
            .glassBackground(
                style = if(isSelected) GlassStyle.Dense else GlassStyle.Regular,
                color = backgroundColor
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = contentColorFinal
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) contentColorFinal.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 任务列表 - 支持编辑模式
 */
@Composable
private fun TasksList(
    tasks: List<TodoTask>,
    isEditMode: Boolean,
    isShowGrid: Boolean,
    sortMode: TaskSortMode,
    selectedTasks: Set<String>,
    onTaskClick: (TodoTask) -> Unit,
    onToggleComplete: (TodoTask) -> Unit,
    onToggleStar: (TodoTask) -> Unit,
    onDeleteTask: (TodoTask) -> Unit,
    onReorder: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyGridState = rememberLazyGridState()
    if (tasks.isEmpty()) {
        EmptyTasksState(modifier = modifier)
    } else {
        val isReorderEnabled = sortMode == TaskSortMode.CUSTOM && isEditMode
        val reorderableState = rememberReorderableLazyGridState(
            lazyGridState
        ) { from, to ->
            // 只在编辑模式且自定义排序模式下才允许拖动
            if (isReorderEnabled) {
                onReorder(from.index, to.index)
            }
        }

        // 根据 isShowGrid 切换网格列数
        val columns = if (isShowGrid) {
            GridCells.Adaptive(minSize = 160.dp)
        } else {
            GridCells.Fixed(1)
        }

        LazyVerticalGrid(
            modifier = modifier.fillMaxSize(),
            state = lazyGridState,
            columns = columns,
            contentPadding = PaddingValues(horizontal = OneBoxDesignSystem.screenPadding, vertical = OneBoxDesignSystem.itemSpacing),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
            horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
        ) {
            items(
                items = tasks,
                key = { it.id },
                contentType = { "task_item" }
            ) { task ->
                ReorderableItem(
                    state = reorderableState,
                    key = task.id,
                    enabled = isReorderEnabled
                ) { _ ->
                    TaskItemDetailed(
                        task = task,
                        themeIndex = 0, // 分类详情页使用单一主题色
                        isEditMode = isEditMode,
                        isSelected = task.id in selectedTasks,
                        onClick = onTaskClick,
                        onToggleComplete = onToggleComplete,
                        onToggleStar = onToggleStar,
                        onDelete = if (isEditMode) onDeleteTask else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isReorderEnabled) {
                                    Modifier.draggableHandle(
                                        onDragStopped = {
                                            // 拖动结束
                                        }
                                    )
                                } else {
                                    Modifier
                                }
                            )
                    )
                }
            }
        }
    }
}

/**
 * 空状态 - 根据筛选模式显示不同提示
 */
@Composable
private fun EmptyTasksState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.empty_tasks),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.empty_tasks_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 加载状态
 */
@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * 错误状态
 */
@Composable
private fun ErrorState(
    error: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}
