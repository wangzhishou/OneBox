package com.wanbaohe.idphoto.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalIconButton
import com.wanbaohe.idphoto.R
import com.wanbaohe.idphoto.domain.IdPhotoSize
import com.wanbaohe.idphoto.util.localizedSizeName
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Edit

/**
 * 证件照尺寸选择器
 */
@Composable
fun SizeSelector(
    sizes: List<IdPhotoSize>,
    selectedSize: IdPhotoSize,
    onSizeSelected: (IdPhotoSize) -> Unit,
    onSizeCustomize: () -> Unit,
    onEditSize: (IdPhotoSize) -> Unit = {},
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    LazyRow(
        state = listState,
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 固定的添加按钮
        stickyHeader {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(68.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                AddSizeItem(onClick = onSizeCustomize)
            }
        }
        // 尺寸列表
        items(sizes, key = { it.id }) { size ->
            SizePresetItem(
                size = size,
                isSelected = size.id == selectedSize.id,
                onClick = { onSizeSelected(size) },
                onEdit = { onEditSize(size) }
            )
            if(size != sizes.last()) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

/**
 * 添加尺寸按钮
 */
@Composable
private fun AddSizeItem(
    onClick: () -> Unit
) {
    val shape = MaterialTheme.shapes.medium

    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = shape
            )
            .clip(shape)
            .clickable(onClick = onClick)
            .size(60.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
            contentDescription = stringResource(R.string.id_photo_manage_size),
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 单个尺寸预设项
 */
@Composable
private fun SizePresetItem(
    size: IdPhotoSize,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit
) {
    val shape = MaterialTheme.shapes.medium

    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(shape)
            .background(
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                },
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        // 主卡片
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 尺寸比例图示
                SizePreviewIcon(
                    widthPx = size.widthPx,
                    heightPx = size.heightPx,
                    isSelected = isSelected
                )

                Spacer(modifier = Modifier.height(2.dp))

                // 尺寸名称
                Text(
                    text = localizedSizeName(size.name),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }

        // 右侧中间编辑按钮（仅选中时显示）
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = shape
                    )
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                GlassTonalIconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(20.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors().copy(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.id_photo_edit_size),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * 尺寸比例图示
 */
@Composable
private fun SizePreviewIcon(
    widthPx: Int,
    heightPx: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val aspectRatio = widthPx.toFloat() / heightPx.toFloat()
    val maxSize = 28.dp

    // 根据宽高比计算显示尺寸
    val (displayWidth, displayHeight) = if (aspectRatio >= 1f) {
        // 横向或正方形
        maxSize to (maxSize / aspectRatio)
    } else {
        // 纵向
        (maxSize * aspectRatio) to maxSize
    }

    Box(
        modifier = modifier
            .size(width = displayWidth, height = displayHeight)
            .background(
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                },
                shape = MaterialTheme.shapes.extraSmall
            ),
        contentAlignment = Alignment.Center
    ) {
        // 显示尺寸文字
        Text(
            text = "${widthPx}×${heightPx}",
            fontSize = 6.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1
        )
    }
}

