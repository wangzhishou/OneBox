package com.wanbaohe.file.browser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassRegular
import com.wanbaohe.file.browser.R
import com.wanbaohe.file.browser.model.FileItem
import com.wanbaohe.file.browser.utils.FileIconProvider
import java.text.SimpleDateFormat
import java.util.Locale
import com.t8rin.imagetoolbox.core.resources.icons.CheckCircle

/**
 * Individual file item in the list
 *
 * @param item The file item to display
 * @param onClick Callback when the item is clicked
 * @param modifier Modifier for the composable
 */
@Composable
fun FileListItem(
    item: FileItem,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        item.isSelected -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
        item.isHighlighted -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surfaceContainerLowest
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassRegular(RoundedCornerShape(8.dp), backgroundColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FileIcon(
            item = item,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            FileMetadata(item = item)
        }

        if (item.isSelected) {
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Check if the file is an image that can show thumbnail
 */
private fun isImageFile(mimeType: String?, extension: String): Boolean {
    if (mimeType?.startsWith("image/") == true) return true
    val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "heif", "avif")
    return extension.lowercase() in imageExtensions
}

/**
 * Check if the file is a video that can show thumbnail
 */
private fun isVideoFile(mimeType: String?, extension: String): Boolean {
    if (mimeType?.startsWith("video/") == true) return true
    val videoExtensions = setOf("mp4", "mkv", "avi", "mov", "webm", "flv", "wmv", "3gp")
    return extension.lowercase() in videoExtensions
}

/**
 * File icon based on type, with thumbnail support for images/videos
 */
@Composable
private fun FileIcon(
    item: FileItem,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isImage = !item.isDirectory && isImageFile(item.mimeType, item.extension)
    val isVideo = !item.isDirectory && isVideoFile(item.mimeType, item.extension)

    if (isImage || isVideo) {
        // Show thumbnail for images and videos
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(item.uri)
                    .crossfade(true)
                    .size(120) // Thumbnail size
                    .build(),
                contentDescription = stringResource(R.string.cd_file_icon),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        // Show icon for other files
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

        Icon(
            imageVector = icon,
            contentDescription = if (item.isDirectory) {
                stringResource(R.string.cd_folder_icon)
            } else {
                stringResource(R.string.cd_file_icon)
            },
            tint = tint,
            modifier = modifier
        )
    }
}

/**
 * File metadata (size, date)
 */
@Composable
private fun FileMetadata(item: FileItem) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val modifiedString = dateFormat.format(item.lastModified)

    val metadataText = buildString {
        if (!item.isDirectory) {
            append(item.getFormattedSize())
            append(" · ")
        }
        append(modifiedString)
    }

    Text(
        text = metadataText,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
