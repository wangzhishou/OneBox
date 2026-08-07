package com.wanbaohe.file.browser.ui

import android.net.Uri
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassRegular
import com.wanbaohe.file.browser.model.FileBrowserState
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolder

/**
 * 最近访问网格项 — Grid 视图。
 *
 * 对齐 [FileGridItem] 的视觉风格：glassRegular 圆角卡片 + 居中大图标/缩略图 + 名称。
 * 图片文件显示 Coil 缩略图，文件夹/非图片文件显示图标。
 */
@Composable
fun RecentAccessGridItem(
    item: FileBrowserState.RecentItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFolder = item.accessType == "folder"
    val extension = item.uri.path?.substringAfterLast('.', "")?.lowercase().orEmpty()
    val isImage = !isFolder && extension in IMAGE_EXTENSIONS
    val isVideo = !isFolder && extension in VIDEO_EXTENSIONS

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassRegular(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RecentAccessGridThumbnail(
            uri = item.uri,
            isFolder = isFolder,
            isImage = isImage,
            isVideo = isVideo,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 最近访问缩略图/图标 — Grid 视图尺寸。
 */
@Composable
private fun RecentAccessGridThumbnail(
    uri: Uri,
    isFolder: Boolean,
    isImage: Boolean,
    isVideo: Boolean,
    modifier: Modifier = Modifier
) {
    if (isImage || isVideo) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = uri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
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
