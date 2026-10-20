package com.wanbaohe.media.ui

import android.text.format.Formatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassRegular
import com.wanbaohe.media.R
import com.wanbaohe.media.component.MediaPlayerUiState
import com.wanbaohe.media.domain.MediaEntry
import com.wanbaohe.media.domain.PlaybackMode

/**
 * 播放模式选择面板。
 *
 * 三个模式互斥，选中项右侧打勾；点一下即生效并收起，不额外要"确定"。
 */
@Composable
fun PlaybackModeSheet(
    visible: Boolean,
    current: PlaybackMode,
    onSelect: (PlaybackMode) -> Unit,
    onDismiss: () -> Unit,
) {
    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { onDismiss() },
        title = stringResource(R.string.media_playback_mode),
        showCloseButton = false,
    ) {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            PlaybackMode.entries.forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(mode) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = playbackModeIcon(mode),
                        contentDescription = null,
                        tint = if (mode == current) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = playbackModeLabel(mode),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (mode == current) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.weight(1f),
                    )
                    if (mode == current) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * 播放列表面板。
 *
 * 直接列队列：当前项打播放标记，点其它项立即切歌并收起面板。
 */
@Composable
fun PlayQueueSheet(
    visible: Boolean,
    queue: List<MediaEntry>,
    currentUri: String?,
    onSelect: (MediaEntry) -> Unit,
    onDismiss: () -> Unit,
) {
    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { onDismiss() },
        title = stringResource(R.string.media_playback_queue),
        showCloseButton = false,
    ) {
        if (queue.isEmpty()) {
            Text(
                text = stringResource(R.string.media_queue_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            )
            return@EnhancedModalBottomSheet
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp),
        ) {
            items(queue, key = { it.uri }) { entry ->
                val isCurrent = entry.uri == currentUri
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(entry) }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = MediaPlayIcon,
                        contentDescription = null,
                        tint = if (isCurrent) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        },
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = entry.bestTitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isCurrent) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        val secondary = entry.subtitleHint
                            ?: entry.location
                            ?: entry.displayName
                        if (secondary != entry.bestTitle) {
                            Text(
                                text = secondary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 路径太长时省略中间。
 *
 * 直接折行会把"位置"撑成两行、把弹窗顶高，而且断点落在目录名中间更难读；
 * 保留头尾（存储位置 + 当前目录）比保留中间有用。
 */
private fun middleEllipsize(value: String, maxLength: Int = 44): String {
    if (value.length <= maxLength) return value
    val head = maxLength / 2 - 2
    val tail = maxLength - head - 1
    return value.take(head) + "…" + value.takeLast(tail)
}

/**
 * 文件信息弹窗。
 *
 * 只展示已有数据：路径、大小、时长、分辨率、作者 —— 拿不到的字段直接不出现，
 * 不用"未知"把每一行都填满。
 */
@Composable
fun MediaInfoDialog(
    state: MediaPlayerUiState,
    onDismiss: () -> Unit,
) {
    val entry = state.current ?: return
    val context = LocalContext.current
    val rows = buildList {
        add(stringResource(R.string.media_info_name) to entry.displayName)
        // 位置优先用 MediaStore 的目录，拿不到才退回 URI（解码后至少能看懂）
        // 长路径省略中间：开头是存储/一级目录，结尾是文件所在目录，两头都比中段有用
        add(
            stringResource(R.string.media_info_path) to middleEllipsize(
                entry.location
                    ?: runCatching { android.net.Uri.decode(entry.uri) }.getOrDefault(entry.uri)
            )
        )
        state.metadata.sizeBytes.takeIf { it > 0L }?.let {
            add(stringResource(R.string.media_info_size) to Formatter.formatFileSize(context, it))
        }
        val duration = state.playback.durationMs.takeIf { it > 0L } ?: state.metadata.durationMs
        if (duration > 0L) {
            add(stringResource(R.string.media_info_duration) to formatMediaDuration(duration))
        }
        val width = state.metadata.width.takeIf { it > 0 } ?: state.playback.videoWidth
        val height = state.metadata.height.takeIf { it > 0 } ?: state.playback.videoHeight
        if (width > 0 && height > 0) {
            add(stringResource(R.string.media_info_resolution) to "$width × $height")
        }
        state.metadata.artist?.takeIf { it.isNotBlank() }?.let {
            add(stringResource(R.string.media_info_artist) to it)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        // 文件信息是独立浮层，底色用最浅的中性容器色，和页面玻璃卡片区分开
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 0.dp,
        title = { Text(text = stringResource(R.string.media_file_info)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rows.forEach { (label, value) ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassRegular(RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.media_close))
            }
        },
    )
}
