package com.wanbaohe.idphoto.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.idphoto.R
import com.wanbaohe.idphoto.domain.IdPhotoSize
import com.wanbaohe.idphoto.util.localizedSizeDescription
import com.wanbaohe.idphoto.util.localizedSizeName
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.Refresh

/**
 * 尺寸管理器
 * 用于管理、编辑、新增证件照尺寸
 */
@Composable
fun SizeCustomizer(
    sizes: List<IdPhotoSize>,
    onSaveSize: (IdPhotoSize) -> Unit,
    onDeleteSize: (Long) -> Unit,
    onBatchDeleteSizes: (List<Long>) -> Unit,
    onResetPresets: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var showAddSheet by remember { mutableStateOf(false) }
    var editingSize by remember { mutableStateOf<IdPhotoSize?>(null) }
    var showEditSheet by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // 顶部操作栏
        Row(
            modifier = Modifier.navigationBarsPadding()
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEditMode) {
                // 编辑模式：恢复默认、删除、取消
                FilledTonalButton(
                    onClick = {
                        onResetPresets()
                        isEditMode = false
                        selectedIds = emptySet()
                    },
                    shape = MaterialTheme.shapes.medium,
                    colors = AppTheme.colors.getSurfaceContainerButtonColors()
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.id_photo_reset_default))
                }

                Spacer(modifier = Modifier.width(8.dp))

                FilledTonalButton(
                    onClick = {
                        onBatchDeleteSizes(selectedIds.toList())
                        isEditMode = false
                        selectedIds = emptySet()
                    },
                    enabled = selectedIds.isNotEmpty(),
                    shape = MaterialTheme.shapes.medium,
                    colors = AppTheme.colors.getSurfaceContainerButtonColors()
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.id_photo_delete_count, selectedIds.size))
                }

                Spacer(modifier = Modifier.width(8.dp))

                FilledTonalButton(
                    onClick = {
                        isEditMode = false
                        selectedIds = emptySet()
                    },
                    shape = MaterialTheme.shapes.medium,
                    colors = AppTheme.colors.getSurfaceContainerButtonColors()
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.id_photo_cancel))
                }
            } else {
                // 正常模式：编辑、新增
                FilledTonalButton(
                    onClick = { isEditMode = true },
                    shape = MaterialTheme.shapes.medium,
                    colors = AppTheme.colors.getSurfaceContainerButtonColors()
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.id_photo_edit))
                }

                Spacer(modifier = Modifier.width(8.dp))

                FilledTonalButton(
                    onClick = { showAddSheet = true },
                    shape = MaterialTheme.shapes.medium,
                    colors = AppTheme.colors.filledTonalButtonColors()
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.id_photo_add))
                }
            }
        }

        // 尺寸列表
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sizes, key = { it.id }) { size ->
                SizeListItem(
                    size = size,
                    isEditMode = isEditMode,
                    isSelected = selectedIds.contains(size.id),
                    onSelect = { selected ->
                        selectedIds = if (selected) {
                            selectedIds + size.id
                        } else {
                            selectedIds - size.id
                        }
                    },
                    onEdit = {
                        editingSize = size
                        showEditSheet = true
                    },
                    onDelete = { onDeleteSize(size.id) }
                )
            }
        }
    }

    // 新增尺寸弹窗
    if (showAddSheet) {
        EnhancedModalBottomSheet(
            visible = showAddSheet,
            onDismiss = { showAddSheet = false },
            title = {
                Text(
                    text = stringResource(R.string.id_photo_add_size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            enableBackHandler = true,
            enableBottomContentWeight = false
        ) {
            SizeEditor(
                size = IdPhotoSize(
                    name = "",
                    widthMm = 25f,
                    heightMm = 35f,
                    widthPx = 295,
                    heightPx = 413,
                    description = ""
                ),
                isNew = true,
                onSave = { newSize ->
                    onSaveSize(newSize)
                    showAddSheet = false
                },
                onCancel = { showAddSheet = false }
            )
        }
    }

    // 编辑尺寸弹窗
    editingSize?.let { size ->
        if (showEditSheet) {
            EnhancedModalBottomSheet(
                visible = showEditSheet,
                onDismiss = {
                    showEditSheet = false
                    editingSize = null
                },
                title = {
                    Text(
                        text = stringResource(R.string.id_photo_edit_size),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                enableBackHandler = true,
                enableBottomContentWeight = false
            ) {
                SizeEditor(
                    size = size,
                    isNew = false,
                    onSave = { updatedSize ->
                        onSaveSize(updatedSize)
                        showEditSheet = false
                        editingSize = null
                    },
                    onCancel = {
                        showEditSheet = false
                        editingSize = null
                    }
                )
            }
        }
    }
}

/**
 * 尺寸列表项
 */
@Composable
private fun SizeListItem(
    size: IdPhotoSize,
    isEditMode: Boolean,
    isSelected: Boolean,
    onSelect: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val shape = MaterialTheme.shapes.medium

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                },
                shape = shape
            )
            .clickable {
                if (isEditMode) {
                    onSelect(!isSelected)
                } else {
                    onEdit() // 非编辑模式点击直接编辑
                }
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 尺寸预览图（固定容器）
        SizePreviewBox(
            widthPx = size.widthPx,
            heightPx = size.heightPx,
            isSelected = isSelected
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 尺寸信息
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = localizedSizeName(size.name),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${size.formatSize()} / ${size.formatPixels()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (size.description.isNotEmpty()) {
                Text(
                    text = localizedSizeDescription(size.name, size.description),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 操作按钮
        if (isEditMode) {
            // 编辑模式：显示选中状态和删除按钮
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 删除按钮
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.id_photo_delete),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 选中图标
                if (isSelected) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                        contentDescription = stringResource(R.string.id_photo_selected),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        } else {
            // 非编辑模式：显示编辑按钮
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.id_photo_edit_size),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 尺寸预览框（固定容器大小，内部预览居中）
 */
@Composable
private fun SizePreviewBox(
    widthPx: Int,
    heightPx: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val containerSize = 48.dp  // 固定容器大小
    val maxPreviewSize = 40.dp // 预览框最大尺寸
    val aspectRatio = widthPx.toFloat() / heightPx.toFloat()

    // 计算预览框尺寸（保持宽高比，适应容器）
    val (previewWidth, previewHeight) = if (aspectRatio >= 1f) {
        maxPreviewSize to (maxPreviewSize / aspectRatio)
    } else {
        (maxPreviewSize * aspectRatio) to maxPreviewSize
    }

    // 固定大小的外层容器
    Box(
        modifier = modifier.size(containerSize),
        contentAlignment = Alignment.Center
    ) {
        // 预览框
        Box(
            modifier = Modifier
                .size(width = previewWidth, height = previewHeight)
                .background(
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    },
                    shape = MaterialTheme.shapes.small
                ),
            contentAlignment = Alignment.Center
        ) {
            // 文字自适应（根据预览框大小调整字号）
            val fontSize = (minOf(previewWidth.value, previewHeight.value) * 0.18f).sp
            Text(
                text = "${widthPx}×${heightPx}",
                fontSize = fontSize,
                maxLines = 1,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

/**
 * 尺寸编辑器
 */
@Composable
fun SizeEditor(
    size: IdPhotoSize,
    isNew: Boolean = false,
    onSave: (IdPhotoSize) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember(size.id) { mutableStateOf(size.name) }
    var widthMm by remember(size.id) { mutableStateOf(size.widthMm.toString()) }
    var heightMm by remember(size.id) { mutableStateOf(size.heightMm.toString()) }
    var widthPx by remember(size.id) { mutableStateOf(size.widthPx.toString()) }
    var heightPx by remember(size.id) { mutableStateOf(size.heightPx.toString()) }
    var description by remember(size.id) { mutableStateOf(size.description) }
    val shape = MaterialTheme.shapes.medium
    val colors = AppTheme.colors.getOutlinedTextFieldColors()

    Column(
        modifier = Modifier
            .fillMaxWidth().navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = AppTheme.dimens.spaceNormal)
    ) {
        // 名称
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.id_photo_field_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = shape,
            colors = colors
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 尺寸（毫米）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = widthMm,
                onValueChange = { widthMm = it },
                label = { Text(stringResource(R.string.id_photo_field_width_mm)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = shape,
                colors = colors
            )
            OutlinedTextField(
                value = heightMm,
                onValueChange = { heightMm = it },
                label = { Text(stringResource(R.string.id_photo_field_height_mm)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = shape,
                colors = colors
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 尺寸（像素）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = widthPx,
                onValueChange = { widthPx = it },
                label = { Text(stringResource(R.string.id_photo_field_width_px)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = shape,
                colors = colors
            )
            OutlinedTextField(
                value = heightPx,
                onValueChange = { heightPx = it },
                label = { Text(stringResource(R.string.id_photo_field_height_px)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = shape,
                colors = colors
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 描述
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(stringResource(R.string.id_photo_field_description_optional)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = shape,
            colors = colors
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            FilledTonalButton(
                onClick = onCancel,
                shape = MaterialTheme.shapes.medium,
                colors = AppTheme.colors.getSurfaceContainerButtonColors()
            ) {
                Text(stringResource(R.string.id_photo_cancel))
            }

            Spacer(modifier = Modifier.width(8.dp))

            FilledTonalButton(
                onClick = {
                    val updatedSize = size.copy(
                        name = name,
                        widthMm = widthMm.toFloatOrNull() ?: size.widthMm,
                        heightMm = heightMm.toFloatOrNull() ?: size.heightMm,
                        widthPx = widthPx.toIntOrNull() ?: size.widthPx,
                        heightPx = heightPx.toIntOrNull() ?: size.heightPx,
                        description = description
                    )
                    onSave(updatedSize)
                },
                enabled = name.isNotBlank(),
                shape = MaterialTheme.shapes.medium,
                colors = AppTheme.colors.filledTonalButtonColors()
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(if (isNew) R.string.id_photo_create else R.string.id_photo_save))
            }
        }
    }
}

