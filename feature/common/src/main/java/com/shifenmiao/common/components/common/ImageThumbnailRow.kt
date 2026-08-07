package com.shifenmiao.common.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.shifenmiao.base.utils.ImageUtils
import com.shifenmiao.model.StrapiImage
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassRegular
import com.t8rin.imagetoolbox.core.ui.widget.image.ImageGalleryViewer
import com.t8rin.imagetoolbox.core.resources.icons.line.LineError

/**
 * 缩略图横向列表 (图片型) — 与图片相关的列表都可以复用, 例如 blog / comment.
 *
 * 原来位于 feature/common/.../components/blog/ImageThumbnailRow.kt, 提升到 common
 * 包下供所有需要展示 StrapiImage 列表的场景使用, 包名也不再带 blog 关键字.
 *
 * ## 优化点
 * - 使用 [LazyRow] 提升长列表性能
 * - 缩略图独立加载/错误状态
 * - 点击查看复用 [ImageGalleryViewer], 支持缩放、滑动、页码指示
 *
 * @param images 图片列表
 */
@Composable
fun ImageThumbnailRow(images: List<StrapiImage>) {
    var selectedImageIndex by remember { mutableIntStateOf(-1) }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = images,
            key = { index, image -> "${image.url}_$index" }
        ) { index, image ->
            ThumbnailItem(
                image = image,
                onClick = { selectedImageIndex = index }
            )
        }
    }

    if (selectedImageIndex >= 0) {
        ImageGalleryViewer(
            images = images.map { it.url },
            initialIndex = selectedImageIndex,
            onDismiss = { selectedImageIndex = -1 }
        )
    }
}

@Composable
private fun ThumbnailItem(
    image: StrapiImage,
    onClick: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .glassRegular(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium
            )
            .size(80.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageUtils.getImageThumbnailPath(image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
            onState = { state ->
                isLoading = state is AsyncImagePainter.State.Loading
                isError = state is AsyncImagePainter.State.Error
            }
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )
        }

        if (isError) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineError,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
