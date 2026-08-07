package com.wanbaohe.file.browser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassRegular
import com.wanbaohe.file.browser.R
import com.wanbaohe.file.browser.model.FileItem
import com.wanbaohe.file.browser.utils.FileIconProvider
import com.t8rin.imagetoolbox.core.resources.icons.CheckCircle

/**
 * Compact grid item for file/folder.
 */
@Composable
fun FileGridItem(
    item: FileItem,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        item.isSelected -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
        item.isHighlighted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surfaceContainerLowest
    }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassRegular(RoundedCornerShape(12.dp), color = backgroundColor)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val icon: ImageVector = FileIconProvider.getIcon(
                isDirectory = item.isDirectory,
                extension = item.extension,
                mimeType = item.mimeType
            )

            val tint = if (item.isDirectory) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            }

            val isImage = !item.isDirectory && (
                    item.mimeType?.startsWith("image/") == true || item.extension.lowercase() in setOf(
                        "jpg", "jpeg", "png", "webp", "gif", "bmp", "heic", "heif", "avif"
                    )
                    )

            val isVideo = !item.isDirectory && (
                    item.mimeType?.startsWith("video/") == true || item.extension.lowercase() in setOf(
                        "mp4", "mkv", "avi", "mov", "webm", "flv", "wmv", "3gp"
                    )
                    )

            if (isImage || isVideo) {
                // Thumbnail: keep it compact to match other grid cards visually.
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = item.uri,
                        contentDescription = stringResource(R.string.cd_file_icon),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize(),
                        loading = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(32.dp)
                            )
                        },
                        error = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    )
                }
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = if (item.isDirectory) {
                        stringResource(R.string.cd_folder_icon)
                    } else {
                        stringResource(R.string.cd_file_icon)
                    },
                    tint = tint,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (item.isSelected) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
            )
        }
    }
}
