package com.shifenmiao.common.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.shifenmiao.common.R
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Close

/**
 * 图片选择列表
 * 展示已选择的图片，支持添加/删除
 * 可在多个模块中复用
 */
@Composable
fun ImagePickerList(
    selectedUris: List<Uri>,
    currentIndex: Int,
    onImageSelected: (Int) -> Unit,
    onAddClick: () -> Unit,
    onAddLongClick: () -> Unit = {},
    onRemoveClick: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    LazyRow(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        // 首尾留白,避免内容贴到容器边缘
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        stickyHeader {
            AddImageButton(onClick = onAddClick, onLongClick = onAddLongClick)
        }
        // 已选图片列表 - key 必须唯一:同一张图可能被重复添加(同 uri 出现多次),
        // 仅用 uri 作 key 会触发 "was already used" 崩溃,故拼上位置序号
        selectedUris.forEachIndexed { index, uri ->
            item(key = "image_${index}_$uri") {
                ImageThumbnail(
                    uri = uri,
                    isSelected = index == currentIndex,
                    onClick = { onImageSelected(index) },
                    onRemove = { onRemoveClick(uri) }
                )
                if (index != selectedUris.size - 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}

/**
 * 添加图片按钮
 * 点击：使用默认选择器添加图片
 * 长按：显示更多选择器选项（相机、文件管理器、系统照片选择器等）
 */
@Composable
private fun AddImageButton(
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val shape = MaterialTheme.shapes.medium
    val background = MaterialTheme.colorScheme.surfaceContainerHighest
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(68.dp)
            .background(
                MaterialTheme.colorScheme.surfaceContainer,
                shape = shape
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    background,
                    shape = shape
                )
                .clip(shape)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // 主按钮
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.common_add_image),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    modifier = Modifier.basicMarquee(),
                    text = stringResource(R.string.common_more_options),
                    color = MaterialTheme.colorScheme.onSurface.copy(0.68f),
                    fontSize = 9.sp
                )
            }
        }
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
        modifier = Modifier.size(60.dp)
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
        ) {}

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
                contentDescription = stringResource(R.string.common_delete),
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
