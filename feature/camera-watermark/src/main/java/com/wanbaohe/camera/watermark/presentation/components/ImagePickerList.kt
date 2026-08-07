package com.wanbaohe.camera.watermark.presentation.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.camera.watermark.R
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Close

/**
 * 图片选择列表
 * 展示已选择的图片，支持添加/删除
 */
@Composable
fun ImagePickerList(
    selectedUris: List<Uri>,
    currentIndex: Int,
    onImageSelected: (Int) -> Unit,
    onAddClick: () -> Unit,
    onAddLongClick: () -> Unit = {},
    onRemoveClick: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        stickyHeader {
            AddImageButton(onClick = onAddClick, onLongClick = onAddLongClick)
        }
        // 已选图片列表
        selectedUris.forEachIndexed { index, uri ->
            item(key = uri.hashCode()) {
                ImageThumbnail(
                    uri = uri,
                    isSelected = index == currentIndex,
                    onClick = { onImageSelected(index) },
                    onRemove = { onRemoveClick(uri) }
                )
            }
        }
    }
}

/**
 * 添加图片按钮
 */
@Composable
private fun AddImageButton(
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val shape = MaterialTheme.shapes.medium
    Box(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = shape
            )
            .clip(shape)
            .size(60.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
            contentDescription = stringResource(R.string.camera_watermark_add_image),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp)
        )
    }
}

/**
 * 图片缩略图
 */
@Composable
private fun ImageThumbnail(
    uri: Uri,
    isSelected: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(60.dp)
    ) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Surface(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .clickable(onClick = onClick)
                .fillMaxSize(),
            color = if (!isSelected) {
                MaterialTheme.colorScheme.surface.copy(0.6f)
            } else {
                Color.Transparent
            }
        ) {

        }
        // 删除按钮
        FilledIconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(18.dp),
            shape = MaterialTheme.shapes.medium,
            colors = AppTheme.colors.filledIconButtonColors().copy(
                containerColor = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                contentColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                contentDescription = stringResource(R.string.camera_watermark_delete),
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

