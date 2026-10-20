package com.shifenmiao.marktodo.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.components.ThemeGroupCard
import com.shifenmiao.marktodo.components.ThemeGroupGridCard
import com.shifenmiao.marktodo.model.DialogState
import com.shifenmiao.marktodo.model.MarkTodoUiEvent
import com.shifenmiao.marktodo.model.TodoCategory
import com.shifenmiao.marktodo.model.TodoStatusTab
import com.shifenmiao.marktodo.model.TodoTag
import com.shifenmiao.marktodo.model.TodoTask
import com.shifenmiao.marktodo.screenLogic.MarkTodoComponent
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAccessTime
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCheckCircleOutline
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFeatures
import com.t8rin.imagetoolbox.core.resources.icons.line.LineQuickTiles
import com.t8rin.imagetoolbox.core.resources.icons.line.LineViewList
import com.wanbaohe.core.ui.widget.glass.GlassFilterChip
import com.wanbaohe.core.ui.widget.navigation.BottomNavItem
import com.wanbaohe.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.core.ui.widget.system.OneBoxDangerButton
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState

/**
 * 待办事项首页 —— 原型设计稿重做：
 * - 顶部：标签筛选 chips（默认隐藏，点任务上的标签呼出；点「全部」或已选中标签收起）
 * - 内容：按主题分组的卡片（列表 / 网格两种视图，顶栏单图标切换，偏好持久化）
 * - 底部：状态页签（全部 / 进行中 / 已完成，复用 BottomNavigationBar，与万年历一致）
 * - 主题编辑/删除走长按卡片菜单，无独立编辑模式
 */
@Composable
fun MarkTodoScreen(
    markTodoComponent: MarkTodoComponent,
    onGoBack: () -> Unit
) {
    val uiState by markTodoComponent.uiState.collectAsState()
    val dialogState by markTodoComponent.dialogState.collectAsState()
    val categories by markTodoComponent.categoriesState.collectAsState()
    val tags by markTodoComponent.tagsState.collectAsState()

    // 标签名 → 标签 的查找表（任务行标签配色）
    val tagsByName = remember(tags) { tags.associateBy { it.name } }

    // 页签 + 标签过滤后的可见分类
    val visibleCategories = remember(categories, uiState.statusTab, uiState.selectedTagName) {
        MarkTodoComponent.filterCategories(categories, uiState.statusTab, uiState.selectedTagName)
    }

    BaseScreen(
        title = { Text(text = stringResource(R.string.marktodo)) },
        actions = {
            MarkTodoScreenActions(
                isGridView = uiState.isGridView,
                isReorderMode = uiState.isReorderMode,
                onViewModeChange = { isGrid ->
                    markTodoComponent.handleEvent(MarkTodoUiEvent.SetGridView(isGrid))
                },
                onExitReorder = {
                    markTodoComponent.handleEvent(MarkTodoUiEvent.SetReorderMode(false))
                },
                onAddTask = {
                    markTodoComponent.handleEvent(MarkTodoUiEvent.AddTaskClicked())
                }
            )
        },
        onGoBack = onGoBack,
        isShowDefaultActions = false
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 标签筛选 chips（默认隐藏，点击任务上的标签后呼出；排序模式下隐藏）
            AnimatedVisibility(visible = uiState.tagFilterVisible && !uiState.isReorderMode) {
                TagFilterRow(
                    tags = tags,
                    selectedTagName = uiState.selectedTagName,
                    onSelectTag = {
                        markTodoComponent.handleEvent(MarkTodoUiEvent.SelectTag(it))
                    }
                )
            }

            // 主题分组内容（列表 / 网格）
            CategoriesContent(
                categories = visibleCategories,
                allCategories = categories,
                isFiltered = uiState.statusTab != TodoStatusTab.ALL || uiState.selectedTagName != null,
                tagsByName = tagsByName,
                statusTab = uiState.statusTab,
                isGridView = uiState.isGridView,
                isReorderMode = uiState.isReorderMode,
                onCategoryClick = {
                    markTodoComponent.handleEvent(MarkTodoUiEvent.CategoryClicked(it))
                },
                onTaskClick = {
                    markTodoComponent.handleEvent(MarkTodoUiEvent.TaskClicked(it))
                },
                onTaskToggleComplete = {
                    markTodoComponent.handleEvent(MarkTodoUiEvent.ToggleTaskComplete(it))
                },
                onEditCategory = {
                    markTodoComponent.handleEvent(MarkTodoUiEvent.EditCategoryClicked(it))
                },
                onDeleteCategory = {
                    markTodoComponent.handleEvent(MarkTodoUiEvent.DeleteCategory(it))
                },
                onEnterReorder = {
                    markTodoComponent.handleEvent(MarkTodoUiEvent.SetReorderMode(true))
                },
                onTagClick = { tagName ->
                    markTodoComponent.handleEvent(MarkTodoUiEvent.TagOnTaskClicked(tagName))
                },
                onReorder = { from, to ->
                    markTodoComponent.handleEvent(MarkTodoUiEvent.ReorderCategories(from, to))
                },
                modifier = Modifier.weight(1f)
            )

            // 底部状态页签（与万年历共用 BottomNavigationBar；排序模式下隐藏防误触）
            AnimatedVisibility(visible = !uiState.isReorderMode) {
                StatusBottomBar(
                    selectedTab = uiState.statusTab,
                    onSelectTab = {
                        markTodoComponent.handleEvent(MarkTodoUiEvent.SelectStatusTab(it))
                    }
                )
            }
        }
    }

    // 删除主题确认对话框
    when (val state = dialogState) {
        is DialogState.DeleteCategoryConfirm -> {
            AlertDialog(
                containerColor = AppTheme.colors.getContainerSurfaceColor(),
                onDismissRequest = {
                    markTodoComponent.handleEvent(MarkTodoUiEvent.DismissDialog)
                },
                title = { Text(stringResource(R.string.dialog_delete_category_title)) },
                text = {
                    Text(stringResource(R.string.dialog_delete_category_message, state.category.title))
                },
                confirmButton = {
                    OneBoxDangerButton(
                        text = stringResource(R.string.action_delete),
                        onClick = {
                            markTodoComponent.handleEvent(
                                MarkTodoUiEvent.ConfirmDeleteCategory(state.category)
                            )
                        }
                    )
                },
                dismissButton = {
                    OneSecondaryButton(
                        text = stringResource(R.string.action_cancel),
                        onClick = {
                            markTodoComponent.handleEvent(MarkTodoUiEvent.DismissDialog)
                        }
                    )
                }
            )
        }
        else -> { /* No dialog to show */ }
    }

    BackHandler(enabled = dialogState !is DialogState.Dismissed) {
        markTodoComponent.handleEvent(MarkTodoUiEvent.DismissDialog)
    }

    BackHandler(enabled = dialogState is DialogState.Dismissed && uiState.isReorderMode) {
        markTodoComponent.handleEvent(MarkTodoUiEvent.SetReorderMode(false))
    }

    BackHandler(enabled = dialogState is DialogState.Dismissed && !uiState.isReorderMode) {
        onGoBack()
    }
}

/**
 * 顶栏操作：视图切换（单图标，用双列/单列图标）+ 新增待办；排序模式下显示「完成」退出排序。
 * 编辑/删除/排序主题走长按卡片菜单。
 */
@Composable
private fun MarkTodoScreenActions(
    isGridView: Boolean,
    isReorderMode: Boolean,
    onViewModeChange: (Boolean) -> Unit,
    onExitReorder: () -> Unit,
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isReorderMode) {
            // 退出排序模式
            IconButton(onClick = onExitReorder) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                    contentDescription = stringResource(R.string.cd_edit_mode_done),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            return@Row
        }
        // 视图切换：当前双列时显示单列图标（点击切单列），反之亦然
        IconButton(onClick = { onViewModeChange(!isGridView) }) {
            Icon(
                imageVector = if (isGridView) {
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineViewList
                } else {
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQuickTiles
                },
                contentDescription = if (isGridView) {
                    stringResource(R.string.cd_switch_to_list_view)
                } else {
                    stringResource(R.string.cd_switch_to_grid_view)
                },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // 新增待办
        IconButton(onClick = onAddTask) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                contentDescription = stringResource(R.string.action_add_task),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 标签筛选行：「全部」+ 标签表驱动的 chips。
 * 点「全部」或已选中的标签 = 清除筛选并收起筛选行。
 */
@Composable
private fun TagFilterRow(
    tags: List<TodoTag>,
    selectedTagName: String?,
    onSelectTag: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = OneBoxDesignSystem.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item(key = "tag_all") {
            GlassFilterChip(
                selected = selectedTagName == null,
                onClick = { onSelectTag(null) },
                label = { Text(stringResource(R.string.filter_all)) }
            )
        }
        items(items = tags, key = { it.id }, contentType = { "tag_chip" }) { tag ->
            val tagColor = tag.colorArgb?.let { Color(it) }
            val selected = selectedTagName == tag.name
            GlassFilterChip(
                selected = selected,
                onClick = { onSelectTag(if (selected) null else tag.name) },
                label = {
                    Text(
                        text = tag.name,
                        color = if (selected) {
                            tagColor ?: MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            Color.Unspecified
                        }
                    )
                }
            )
        }
    }
}

/**
 * 主题分组内容区：列表 / 网格两种视图；排序模式下卡片可拖拽（置顶项不参与拖拽）
 */
@Composable
private fun CategoriesContent(
    categories: List<TodoCategory>,
    allCategories: List<TodoCategory>,
    isFiltered: Boolean,
    tagsByName: Map<String, TodoTag>,
    statusTab: TodoStatusTab,
    isGridView: Boolean,
    isReorderMode: Boolean,
    onCategoryClick: (TodoCategory) -> Unit,
    onTaskClick: (TodoTask) -> Unit,
    onTaskToggleComplete: (TodoTask) -> Unit,
    onEditCategory: (TodoCategory) -> Unit,
    onDeleteCategory: (TodoCategory) -> Unit,
    onEnterReorder: () -> Unit,
    onTagClick: (String) -> Unit,
    onReorder: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty()) {
        // 区分「还没有待办」与「当前筛选无结果」
        val emptyText = if (allCategories.isNotEmpty() || isFiltered) {
            stringResource(R.string.empty_filtered_tasks)
        } else {
            stringResource(R.string.empty_tasks_title)
        }
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = emptyText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val lazyGridState = rememberLazyGridState()
    val reorderableState = rememberReorderableLazyGridState(
        lazyGridState = lazyGridState,
        onMove = { from, to ->
            if (isReorderMode) {
                onReorder(from.index, to.index)
            }
        }
    )

    val columns = if (isGridView) {
        GridCells.Adaptive(minSize = 180.dp)
    } else {
        GridCells.Fixed(1)
    }

    LazyVerticalGrid(
        columns = columns,
        state = lazyGridState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = OneBoxDesignSystem.screenPadding,
            vertical = OneBoxDesignSystem.blockSpacing
        ),
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing),
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing)
    ) {
        items(
            items = categories,
            key = { it.id },
            contentType = { "theme_group_card" }
        ) { category ->
            // 头部进度按全量任务统计（category.tasks 已被页签/标签过滤）
            val fullCategory = allCategories.firstOrNull { it.id == category.id } ?: category

            // 置顶项始终固定在顶部（DAO 按 is_pinned DESC 排序），拖拽会弹回，直接禁用
            ReorderableItem(
                state = reorderableState,
                key = category.id,
                enabled = isReorderMode && !category.isPinned
            ) { _ ->
                val cardModifier = if (isReorderMode && !category.isPinned) {
                    Modifier.draggableHandle()
                } else {
                    Modifier
                }

                if (isGridView) {
                    ThemeGroupGridCard(
                        category = fullCategory,
                        visibleTasks = category.tasks,
                        tagsByName = tagsByName,
                        isReorderMode = isReorderMode,
                        onEnterReorder = onEnterReorder,
                        onCategoryClick = onCategoryClick,
                        onTaskClick = onTaskClick,
                        onToggleComplete = onTaskToggleComplete,
                        onEditCategory = onEditCategory,
                        onDeleteCategory = onDeleteCategory,
                        onTagClick = onTagClick,
                        modifier = cardModifier
                    )
                } else {
                    ThemeGroupCard(
                        category = fullCategory,
                        visibleTasks = category.tasks,
                        tagsByName = tagsByName,
                        showMenu = statusTab == TodoStatusTab.COMPLETED && !isReorderMode,
                        isReorderMode = isReorderMode,
                        onEnterReorder = onEnterReorder,
                        onCategoryClick = onCategoryClick,
                        onTaskClick = onTaskClick,
                        onToggleComplete = onTaskToggleComplete,
                        onEditCategory = onEditCategory,
                        onDeleteCategory = onDeleteCategory,
                        onTagClick = onTagClick,
                        modifier = cardModifier
                    )
                }
            }
        }
    }
}

/**
 * 底部状态页签栏：全部 / 进行中 / 已完成（复用 BottomNavigationBar，与万年历一致）
 */
@Composable
private fun StatusBottomBar(
    selectedTab: TodoStatusTab,
    onSelectTab: (TodoStatusTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(
            id = TodoStatusTab.ALL.name,
            label = stringResource(R.string.filter_all),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFeatures
        ),
        BottomNavItem(
            id = TodoStatusTab.ACTIVE.name,
            label = stringResource(R.string.label_active),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAccessTime
        ),
        BottomNavItem(
            id = TodoStatusTab.COMPLETED.name,
            label = stringResource(R.string.filter_completed),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCheckCircleOutline
        ),
    )

    BottomNavigationBar(
        items = items,
        selectedItemId = selectedTab.name,
        onItemClick = { item -> onSelectTab(TodoStatusTab.valueOf(item.id)) },
        modifier = modifier.fillMaxWidth(),
    )
}
