package com.shifenmiao.common.components.category

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.PaddingValues
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.shifenmiao.base.ui.DeleteConfirmDialog
import com.shifenmiao.base.ui.button.CancelButton
import com.shifenmiao.base.ui.button.ConfirmButton
import com.shifenmiao.core.R
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.shifenmiao.model.Source
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDragHandle

/**
 * Generic interface for items that can be managed
 */
interface ManageableItem {
    val id: Int
    val name: String
    val order: Int
    val canEdit: Boolean?
    val source: Source
}

/**
 * Generic category management dialog.
 *
 * 设计说明：
 * - 列表使用 3 列 LazyVerticalGrid + sh.calvin.reorderable 的 grid 拖拽
 * - 每个 cell 是一块色块（surfaceContainer），无 border、无 divider
 * - 顶部一行：drag handle + 操作按钮（编辑 / 删除 / 确认 / 取消）
 * - 中部：分类名（maxLines = 1, ellipsis）
 * - "添加新分类"按钮放在 grid 上方，让操作入口在视觉上前置
 */
@Composable
fun <T : ManageableItem> CategoryManagementDialog(
    items: List<T>,
    title: String = stringResource(R.string.reorderable_category),
    onDismiss: () -> Unit,
    onAdd: (name: String) -> Unit,
    onDelete: (T) -> Unit,
    onRename: (T, String) -> Unit,
    onReorder: (List<T>) -> Unit
) {
    var itemList by remember(items) { mutableStateOf(items) }
    var editingItem by remember { mutableStateOf<T?>(null) }
    var editText by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<T?>(null) }
    val showDeleteDialog = remember { mutableStateOf(false) }

    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        confirmButton = {},
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Title and Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))

                // 添加按钮 — 放在 grid 上方
                FilledTonalButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.add_new_category))
                }

                Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))

                // 3 列 grid + reorderable
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
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(
                        items = itemList,
                        key = { _, item -> item.id }
                    ) { _, item ->
                        ReorderableItem(
                            state = reorderableState,
                            key = item.id
                        ) { isDragging ->
                            CategoryGridCell(
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
            }
        }
    )

    // Add Category Dialog
    if (showAddDialog) {
        AddCategoryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                onAdd(name)
                showAddDialog = false
            }
        )
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

/**
 * Grid 单元格：
 *
 * ┌─────────────────────────┐
 * │ ⋮⋮          ✏  🗑       │ ← drag handle + 操作按钮
 * │                         │
 * │       分类名称           │ ← 文字（含编辑态）
 * │                         │
 * └─────────────────────────┘
 *
 * 不画 border，仅用 surfaceContainer 色块 + 圆角；拖拽时切到 surfaceContainerHighest 提示。
 */
@Composable
private fun <T : ManageableItem> CategoryGridCell(
    item: T,
    isDragging: Boolean,
    isEditing: Boolean,
    editText: String,
    onEditTextChange: (String) -> Unit,
    onStartEdit: () -> Unit,
    onConfirmEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onDelete: () -> Unit,
    dragHandleModifier: Modifier,
) {
    val canModify = item.canEdit != false && item.source == Source.LOCAL

    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp),
        style = if (isDragging) GlassStyle.Medium else GlassStyle.Regular,
        shape = RoundedCornerShape(16.dp),
        borderWidth = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // 顶部：drag handle + 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDragHandle,
                    contentDescription = stringResource(R.string.drag_to_reorder),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = dragHandleModifier.size(20.dp),
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isEditing) {
                        IconButton(
                            onClick = onConfirmEdit,
                            modifier = Modifier.size(28.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Transparent,
                            ),
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                                contentDescription = stringResource(R.string.confirm),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        IconButton(
                            onClick = onCancelEdit,
                            modifier = Modifier.size(28.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Transparent,
                            ),
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.cancel),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    } else if (canModify) {
                        IconButton(
                            onClick = onStartEdit,
                            modifier = Modifier.size(28.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Transparent,
                            ),
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                                contentDescription = stringResource(R.string.edit),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Transparent,
                            ),
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }
            }

            // 中部：分类名 / 编辑框
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (isEditing) {
                    GlassOutlinedTextField(
                        value = editText,
                        onValueChange = onEditTextChange,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    )
                } else {
                    Text(
                        text = item.name,
                        minLines = 1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.basicMarquee(),
                    )
                }
            }
        }
    }
}

/**
 * Legacy 列表项布局（保留向后兼容）。
 *
 * 仍被 [ReorderableContent] 使用，签名不变。
 */
@Composable
fun <T : ManageableItem> CategoryItemRow(
    item: T,
    isDragging: Boolean,
    isEditing: Boolean,
    editText: String,
    onEditTextChange: (String) -> Unit,
    onStartEdit: () -> Unit,
    onConfirmEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onDelete: () -> Unit,
    dragHandleModifier: Modifier
) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth(),
        style = if (isDragging) GlassStyle.Medium else GlassStyle.Regular,
        shape = RoundedCornerShape(16.dp),
        borderWidth = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Drag Handle
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDragHandle,
                    contentDescription = stringResource(R.string.drag_to_reorder),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = dragHandleModifier.size(24.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isEditing) {
                    // Confirm Edit
                    IconButton(
                        onClick = onConfirmEdit,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                            contentDescription = stringResource(R.string.confirm),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    // Cancel Edit
                    IconButton(
                        onClick = onCancelEdit,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.cancel),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else {
                    // Edit Button
                    if (item.canEdit != false && item.source == Source.LOCAL) {
                        IconButton(
                            onClick = onStartEdit,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Transparent
                            ),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                                contentDescription = stringResource(R.string.edit),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    // Delete Button
                    if (item.canEdit != false && item.source == Source.LOCAL) {
                        IconButton(
                            onClick = onDelete,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Transparent
                            ),
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
            Column(
                modifier = Modifier.heightIn(60.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Content
                if (isEditing) {
                    GlassOutlinedTextField(
                        value = editText,
                        onValueChange = onEditTextChange,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    )
                } else {
                    Text(
                        modifier = Modifier.basicMarquee(),
                        text = item.name,
                        minLines = 1,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.add_new_category),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            GlassOutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.category_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            )
        },
        confirmButton = {
            ConfirmButton {
                if (name.isNotBlank()) {
                    onConfirm(name)
                }
            }
        },
        dismissButton = {
            CancelButton {
                onDismiss()
            }
        }
    )
}
