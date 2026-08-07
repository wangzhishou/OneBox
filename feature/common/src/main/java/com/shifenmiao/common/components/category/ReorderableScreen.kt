package com.shifenmiao.common.components.category

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.DeleteConfirmDialog
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.theme.AppTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import com.t8rin.imagetoolbox.core.resources.icons.Add

@Composable
fun ReorderableScreen(
    reorderableComponent: ReorderableComponent,
    onGoBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    BaseScreen(
        title = {
            Text(text = stringResource(R.string.reorderable_category))
        },
        actions = {
            IconButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.new_add)
                )
            }
        },
        onGoBack = onGoBack
    ) {
        ReorderableContent(
            reorderableComponent = reorderableComponent
        )
    }

    if (showAddDialog) {
        AddCategoryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                reorderableComponent.addItem(name)
                showAddDialog = false
            }
        )
    }

    BackHandler {
        onGoBack()
    }
}

@Composable
fun ReorderableContent(
    reorderableComponent: ReorderableComponent
) {
    val items = reorderableComponent.items

    ReorderableContent(
        items = items,
        onDelete = reorderableComponent::deleteItem,
        onRename = reorderableComponent::renameItem,
        onReorder = reorderableComponent::reorderItems
    )
}

@Composable
fun <T : ManageableItem> ReorderableContent(
    items: List<T>,
    title: String = stringResource(R.string.reorderable_category),
    onDelete: (T) -> Unit,
    onRename: (T, String) -> Unit,
    onReorder: (List<T>) -> Unit
) {
    var itemList by remember { mutableStateOf(items) }
    var editingItem by remember { mutableStateOf<T?>(null) }
    var editText by remember { mutableStateOf("") }
    var itemToDelete by remember { mutableStateOf<T?>(null) }
    val showDeleteDialog = remember { mutableStateOf(false) }

    // Sync itemList with incoming items from database
    LaunchedEffect(items) {
        itemList = items
    }

    // Category Grid with Reordering
    val lazyGridState = rememberLazyGridState()
    val reorderableState = rememberReorderableLazyGridState(
        lazyGridState = lazyGridState
    ) { from, to ->
        itemList = itemList.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    LazyVerticalGrid(
        state = lazyGridState,
        columns = GridCells.Adaptive(150.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(AppTheme.dimens.paddingNormal),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = itemList,
            key = { _, item -> item.id }
        ) { _, item ->
            ReorderableItem(
                state = reorderableState,
                key = item.id
            ) { isDragging ->
                CategoryItemRow(
                    item = item,
                    isDragging = isDragging,
                    isEditing = editingItem?.id == item.id,
                    editText = editText,
                    onEditTextChange = { editText = it },
                    onStartEdit = {
                        editingItem = item
                        editText = item.name
                    },
                    onConfirmEdit = {
                        if (editText.isNotBlank()) {
                            onRename(item, editText)
                            editingItem = null
                            editText = ""
                        }
                    },
                    onCancelEdit = {
                        editingItem = null
                        editText = ""
                    },
                    onDelete = {
                        itemToDelete = item
                        showDeleteDialog.value = true
                    },
                    dragHandleModifier = Modifier.draggableHandle(
                        onDragStopped = {
                            onReorder(itemList)
                        }
                    )
                )
            }
        }
    }

    // Delete confirmation dialog
    itemToDelete?.let { item ->
        DeleteConfirmDialog(
            onDelete = {
                onDelete(item)
                showDeleteDialog.value = false
                itemToDelete = null
            },
            showDeleteDialogState = showDeleteDialog,
            message = stringResource(R.string.delete_category_message, item.name)
        )
    }
}
