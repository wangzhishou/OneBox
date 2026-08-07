package com.shifenmiao.marktodo.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.components.CategoryCard
import com.shifenmiao.marktodo.model.DialogState
import com.shifenmiao.marktodo.model.MarkTodoUiEvent
import com.shifenmiao.marktodo.model.TodoCategory
import com.shifenmiao.marktodo.model.TodoTask
import com.shifenmiao.marktodo.screenLogic.MarkTodoComponent
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDangerButton
import com.t8rin.imagetoolbox.core.ui.widget.system.OneSecondaryButton
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFeatures

/**
 * Main screen for the MarkTodo module displaying a staggered grid of category cards.
 * Refactored for high performance with fine-grained state management and component separation.
 *
 * Performance improvements:
 * - Separated dialogs into independent, reusable components
 * - Fine-grained state management to reduce unnecessary recompositions
 * - Proper use of keys in lazy lists for efficient updates
 * - Extracted sub-composables to control recomposition scope
 *
 * @param markTodoComponent The MarkTodo component managing state and logic.
 * @param onGoBack Callback invoked when the back button is pressed.
 */
@Composable
fun MarkTodoScreen(
    markTodoComponent: MarkTodoComponent,
    onGoBack: () -> Unit
) {

    // 网格布局状态
    var isShowGrid by remember { mutableStateOf(true) }

    // Observe states from component using StateFlow for fine-grained updates
    val uiState by markTodoComponent.uiState.collectAsState()
    val dialogState by markTodoComponent.dialogState.collectAsState()
    val categories by markTodoComponent.categoriesState.collectAsState()

    BaseScreen(
        title = { Text(
            text = stringResource(R.string.marktodo)
        ) },
        navigationIcon = {
            Row {
                // 返回按钮
                IconButton(onClick = onGoBack) {
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
            MarkTodoScreenActions(
                isEditMode = uiState.isEditMode,
                onToggleEditMode = {
                    markTodoComponent.handleEvent(MarkTodoUiEvent.ToggleEditMode)
                },
                onAddCategory = {
                    markTodoComponent.handleEvent(MarkTodoUiEvent.AddCategoryClicked)
                }
            )
        },
        onGoBack = onGoBack,
        isShowDefaultActions = false
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content - Category grid
            // This will only recompose when categories list changes
            CategoriesGrid(
                categories = categories,
                isEditMode = uiState.isEditMode,
                isShowGrid = isShowGrid,
                onCategoryClick = { category ->
                    markTodoComponent.handleEvent(MarkTodoUiEvent.CategoryClicked(category))
                },
                onAddTaskClick = { category ->
                    markTodoComponent.handleEvent(MarkTodoUiEvent.AddTaskClicked(category))
                },
                onTaskToggleComplete = { task ->
                    markTodoComponent.handleEvent(MarkTodoUiEvent.ToggleTaskComplete(task))
                },
                onDeleteCategory = { category ->
                    markTodoComponent.handleEvent(MarkTodoUiEvent.DeleteCategory(category))
                },
                onEditCategory = { category ->
                    markTodoComponent.handleEvent(MarkTodoUiEvent.EditCategoryClicked(category))
                },
                onReorderCategories = { fromIndex, toIndex ->
                    markTodoComponent.handleEvent(MarkTodoUiEvent.ReorderCategories(fromIndex, toIndex))
                }
            )
        }
    }

    // Handle delete confirmation dialog
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

    // Back handler - handles dialog dismissal and screen exit
    BackHandler(enabled = dialogState !is DialogState.Dismissed) {
        markTodoComponent.handleEvent(MarkTodoUiEvent.DismissDialog)
    }

    BackHandler(enabled = uiState.isEditMode) {
        markTodoComponent.handleEvent(MarkTodoUiEvent.ToggleEditMode)
    }

    BackHandler(enabled = dialogState is DialogState.Dismissed && !uiState.isEditMode) {
        onGoBack()
    }
}

/**
 * Actions section of the screen - separated to prevent unnecessary recompositions.
 */
@Composable
private fun MarkTodoScreenActions(
    isEditMode: Boolean,
    onToggleEditMode: () -> Unit,
    onAddCategory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {

        // 编辑按钮
        IconButton(onClick = onToggleEditMode) {
            Icon(
                imageVector = if (isEditMode) Icons.Outlined.Cancel else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                contentDescription = if (isEditMode) stringResource(R.string.cd_edit_mode_done) else stringResource(R.string.action_edit),
                tint = if (isEditMode) MaterialTheme.colorScheme.primary else LocalContentColor.current
            )
        }

        // 添加分类按钮
        IconButton(
            onClick = onAddCategory,
            enabled = !isEditMode
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                contentDescription = stringResource(R.string.action_add_category)
            )
        }
    }
}

/**
 * Categories grid component - separated for fine-grained recomposition.
 * Only recomposes when categories list changes.
 *
 * 支持拖动排序，使用 sh.calvin.reorderable 库
 * 支持任务勾选完成功能
 */
@Composable
private fun CategoriesGrid(
    categories: List<TodoCategory>,
    isEditMode: Boolean,
    isShowGrid: Boolean,
    onCategoryClick: (TodoCategory) -> Unit,
    onAddTaskClick: (TodoCategory) -> Unit,
    onTaskToggleComplete: (TodoTask) -> Unit,
    onDeleteCategory: (TodoCategory) -> Unit,
    onEditCategory: (TodoCategory) -> Unit,
    onReorderCategories: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyGridState = rememberLazyGridState()

    val reorderableState = sh.calvin.reorderable.rememberReorderableLazyGridState(
        lazyGridState = lazyGridState,
        onMove = { from, to ->
            // 只在编辑模式下允许拖动
            if (isEditMode) {
                onReorderCategories(from.index, to.index)
            }
        }
    )

    // 根据 isShowGrid 切换网格列数
    val columns = if (isShowGrid) {
        GridCells.Adaptive(minSize = 160.dp)
    } else {
        GridCells.Fixed(1)
    }

    LazyVerticalGrid(
        columns = columns,
        state = lazyGridState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = OneBoxDesignSystem.screenPadding, vertical = OneBoxDesignSystem.itemSpacing),
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)
    ) {
        itemsIndexed(
            items = categories,
            key = { _, item -> item.id },
            contentType = { _, _ -> "category_card" }
        ) { index, category ->
            ReorderableItem(
                state = reorderableState,
                key = category.id,
                enabled = isEditMode
            ) { _ ->
                CategoryCard(
                    category = category,
                    themeIndex = index,
                    onCategoryClick = onCategoryClick,
                    onAddTaskClick = onAddTaskClick,
                    onTaskToggleComplete = onTaskToggleComplete,
                    isEditMode = isEditMode,
                    onDeleteClick = if (isEditMode) onDeleteCategory else null,
                    onEditClick = if (isEditMode) onEditCategory else null,
                    modifier = if (isEditMode) {
                        Modifier.draggableHandle(
                            onDragStopped = {
                                // 拖动结束
                            }
                        )
                    } else {
                        Modifier
                    }
                )
            }
        }
    }
}
