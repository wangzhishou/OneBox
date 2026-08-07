package com.wanbaohe.blog.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.shifenmiao.base.ui.ActionButton
import com.shifenmiao.common.upload.UploadingImage
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Close

@Composable
fun ImageUploadGallery(
    size: Dp = 80.dp,
    shape: Shape = MaterialTheme.shapes.large,
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit,
    images: List<UploadingImage>,
    onImageRemove: (UploadingImage) -> Unit
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Add button
        stickyHeader {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(shape)
                    .glassBackground(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = shape
                    )
                    .clickable { onAddClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                    contentDescription = "Add Image",
                    tint = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // 统一展示所有图片
        items(images.size) { index ->
            val image = images[index]
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(shape)
                    .border(
                        width = 1.dp,
                        color = when {
                            image.isError -> MaterialTheme.colorScheme.error
                            image.isUploaded -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceContainerHighest
                        },
                        shape = shape
                    )
                    .glassBackground(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = shape
                    )
            ) {
                // 图片预览
                AsyncImage(
                    model = if (image.isUploaded && image.strapiImage != null) image.strapiImage!!.url else image.localUri,
                    contentDescription = "Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // 根据状态显示不同的覆盖层
                when {
                    // 上传中
                    !image.isUploaded && !image.isError -> {
                        val animatedProgress by animateFloatAsState(
                            targetValue = image.progress,
                            label = "upload-progress"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                    // 上传失败
                    image.isError -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Failed",
                                color = MaterialTheme.colorScheme.onError,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    // 已上传成功，无需额外覆盖层
                }

                // 删除按钮
                ActionButton(
                    modifier = Modifier.align(Alignment.TopEnd),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                    contentDescription = "Remove",
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    onImageRemove(image)
                }
            }
        }
    }
}