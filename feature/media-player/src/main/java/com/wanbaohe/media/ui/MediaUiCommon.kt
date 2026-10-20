package com.wanbaohe.media.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassIconButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.wanbaohe.media.R
import com.wanbaohe.media.component.MediaPlayerUiState
import com.wanbaohe.media.domain.MediaEntry
import com.wanbaohe.media.domain.MediaKind
import com.wanbaohe.media.domain.MediaMetadata
import com.wanbaohe.media.domain.PlaybackMode
import java.util.concurrent.TimeUnit

/** 进度/时长的统一格式：不到一小时 mm:ss，超过一小时 h:mm:ss。 */
fun formatMediaDuration(durationMs: Long): String {
    val totalSeconds = durationMs.coerceAtLeast(0L) / 1000L
    val hours = TimeUnit.SECONDS.toHours(totalSeconds)
    val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

/** 媒体类型对应的占位图标（没有封面时用）。 */
fun MediaKind.placeholderIcon(): ImageVector = when (this) {
    MediaKind.VIDEO -> MediaMovieIcon
    MediaKind.AUDIO -> MediaMusicIcon
}

/**
 * 媒体封面/首帧。
 *
 * 先按 [loadArtwork] 异步解码，解码完成前画渐变占位；列表项复用同一个 composable，
 * 尺寸由调用方给，避免大图被压进小格子白白占内存。
 */
@Composable
fun MediaArtwork(
    entry: MediaEntry,
    kind: MediaKind,
    maxSize: Int,
    loadArtwork: suspend (MediaEntry, Int) -> android.graphics.Bitmap?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    iconSize: Dp = 28.dp,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    val bitmap by produceState<android.graphics.Bitmap?>(
        initialValue = null,
        entry.uri,
        maxSize,
    ) {
        value = loadArtwork(entry, maxSize)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer),
        contentAlignment = Alignment.Center,
    ) {
        val image = bitmap
        if (image != null) {
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.tertiaryContainer,
                            )
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = kind.placeholderIcon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f),
                    modifier = Modifier.size(iconSize),
                )
            }
        }
        overlay()
    }
}

/** 缩略图右下角的时长小药丸。 */
@Composable
fun BoxScope.DurationBadge(durationMs: Long) {
    if (durationMs <= 0L) return
    Text(
        text = formatMediaDuration(durationMs),
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(6.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(horizontal = 5.dp, vertical = 1.dp),
    )
}

/**
 * 圆形操作按钮（原型播放页那一排）。
 *
 * 选中态走 primaryContainer（收藏、当前模式），未选中走中性底，
 * 颜色只由 [selected] 决定，调用方不需要各自挑色。
 */
@Composable
fun MediaActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    size: Dp = 48.dp,
) {
    val colors: IconButtonColors = if (selected) {
        IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    } else {
        IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    GlassIconButton(
        onClick = onClick,
        modifier = modifier.size(size),
        enabled = enabled,
        colors = colors,
        style = GlassStyle.Thin,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(size * 0.45f),
        )
    }
}

/** 播放模式 → 文案（用户可见字符串只出现在 UI 层，组件里不含）。 */
@Composable
fun playbackModeLabel(mode: PlaybackMode): String = when (mode) {
    PlaybackMode.SEQUENTIAL -> stringResource(R.string.media_mode_sequential)
    PlaybackMode.REPEAT_ONE -> stringResource(R.string.media_mode_repeat_one)
    PlaybackMode.SHUFFLE -> stringResource(R.string.media_mode_shuffle)
}

/** 播放模式 → 图标。 */
fun playbackModeIcon(mode: PlaybackMode): ImageVector = when (mode) {
    PlaybackMode.SEQUENTIAL -> MediaRepeatIcon
    PlaybackMode.REPEAT_ONE -> MediaRepeatOneIcon
    PlaybackMode.SHUFFLE -> MediaShuffleIcon
}

/** 列表项的元信息：读一次缓存住，时长/作者/缩略图共用。 */
@Composable
fun rememberMediaMetadata(
    entry: MediaEntry,
    enabled: Boolean = true,
    load: suspend (MediaEntry) -> MediaMetadata,
): MediaMetadata {
    // MediaStore 已经给了标题/时长时不要再跑 MediaMetadataRetriever：
    // 1000 条的库里"每行 2~3 次 IO"就是滚动卡顿的来源
    val state = produceState(MediaMetadata(), entry.uri, enabled) {
        if (enabled) value = load(entry)
    }
    return state.value
}

/** 当前媒体标题：元数据标题优先，其次媒体库标题，最后才是文件名。 */
fun MediaPlayerUiState.displayTitle(): String =
    metadata.title?.takeIf { it.isNotBlank() } ?: current?.bestTitle.orEmpty()

/** 当前媒体作者：元数据里没有作者时用调用方给的兜底文案。 */
fun MediaMetadata.displayArtist(fallback: String): String =
    artist?.takeIf { it.isNotBlank() } ?: fallback

/**
 * 圆形播放按钮（视频中央 / 音频主控都用它）。
 *
 * primaryContainer 填充 + onPrimaryContainer 图标，和全站强调色用法保持一致。
 */
@Composable
fun PlayPauseCircle(
    isPlaying: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isPlaying) MediaPauseIcon else MediaPlayIcon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(size * 0.44f),
        )
    }
}
