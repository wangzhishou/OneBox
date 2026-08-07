package com.wanbaohe.file.browser.ui

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassRegular
import com.wanbaohe.file.browser.model.FileBrowserState
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolder

/**
 * 最近访问列表项 — 列表视图。
 *
 * 对齐 [FileListItem] 的视觉风格：glassRegular 圆角卡片 + 图标/缩略图 + 标题/副标题。
 * 图片文件显示 Coil 缩略图，文件夹/非图片文件显示图标。
 */
@Composable
fun RecentAccessListItem(
    item: FileBrowserState.RecentItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFolder = item.accessType == "folder"
    val extension = item.uri.path?.substringAfterLast('.', "")?.lowercase().orEmpty()
    val isImage = !isFolder && extension in IMAGE_EXTENSIONS
    val isVideo = !isFolder && extension in VIDEO_EXTENSIONS

    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassRegular(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RecentAccessThumbnail(
            uri = item.uri,
            isFolder = isFolder,
            isImage = isImage,
            isVideo = isVideo,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 最近访问缩略图/图标 — 列表视图尺寸。
 * 图片/视频显示 Coil 缩略图，文件夹显示 Folder 图标，其他文件显示 InsertDriveFile 图标。
 */
@Composable
private fun RecentAccessThumbnail(
    uri: Uri,
    isFolder: Boolean,
    isImage: Boolean,
    isVideo: Boolean,
    modifier: Modifier = Modifier
) {
    if (isImage || isVideo) {
        val context = LocalContext.current
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(uri)
                    .crossfade(true)
                    .size(120)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth()
            )
        }
    } else {
        val icon: ImageVector = if (isFolder) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFolder else Icons.AutoMirrored.Filled.InsertDriveFile
        val tint = if (isFolder) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        }
        Icon(
            imageVector = icon,
            contentDescription = if (isFolder) "文件夹" else "文件",
            tint = tint,
            modifier = modifier
        )
    }
}

private val IMAGE_EXTENSIONS = setOf(
    "jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "heif", "avif"
)

private val VIDEO_EXTENSIONS = setOf(
    "mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "m4v", "3gp", "mpeg", "mpg"
)
