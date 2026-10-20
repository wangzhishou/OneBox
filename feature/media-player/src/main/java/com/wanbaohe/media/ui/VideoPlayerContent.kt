package com.wanbaohe.media.ui

import android.view.TextureView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAspectRatio
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFavorite
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFavoriteFilled
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInfo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMovie
import com.t8rin.imagetoolbox.core.resources.icons.line.LineOpenWith
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSkipNext
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSkipPrevious
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.wanbaohe.media.R
import com.wanbaohe.media.component.MediaPlayerComponent
import com.wanbaohe.media.component.MediaPlayerUiState
import com.wanbaohe.media.domain.MediaEntry
import com.wanbaohe.media.domain.MediaKind
import com.wanbaohe.media.domain.MediaMetadata
import kotlinx.coroutines.delay

/**
 * 视频播放页正文（原型"视频播放"）。
 *
 * 顺序和原型一致：播放器 → 标题/作者 → 一排操作 → 最近观看。
 * 播放器区域单独抽成 [VideoStage]，因为画中画/全屏模式下整页只剩它一个。
 */
@Composable
fun VideoPlayerContent(
    component: MediaPlayerComponent,
    state: MediaPlayerUiState,
    onAddFile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val duration = state.playback.durationMs.takeIf { it > 0L }
        ?: state.current?.durationMs
        ?: state.metadata.durationMs
    val current = state.current
    // 同一个文件可能以 MediaStore URI 和文档 URI 两种形式各记一条，按文件名再去一次重
    val recents = state.recentEntries
        .filter { it.uri != current?.uri && it.displayName != current?.displayName }
        .take(8)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            VideoStage(
                component = component,
                state = state,
                durationMs = duration,
                onAddFile = onAddFile,
                onToggleFullscreen = { component.setFullscreen(true) },
            )
        }

        item {
            PlayerTitleBlock(
                title = state.displayTitle()
                    .ifBlank { stringResource(R.string.media_now_playing_hint) },
                subtitle = state.metadata.displayArtist(
                    current?.subtitleHint ?: stringResource(R.string.media_video_type)
                ),
            )
        }

        if (state.playback.hasError) {
            item {
                Text(
                    text = stringResource(R.string.media_play_error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        item {
            ActionRow(
                isFavorite = state.isFavorite,
                enabled = state.current != null,
                onFavorite = component::toggleFavorite,
                onShare = component::share,
                onInfo = { component.showInfo(true) },
                onOpenWith = component::openWithOtherApp,
            )
        }

        if (recents.isNotEmpty()) {
            item {
                RecentMediaCard(
                    title = stringResource(R.string.media_recent_videos),
                    entries = recents,
                    kind = state.kind,
                    loadArtwork = component::artwork,
                    loadMetadata = component::metadata,
                    onSelect = component::play,
                )
            }
        }
    }
}

/**
 * 视频画面 + 播放控件。
 *
 * 控件在播放时 3 秒后自动收起，点画面可以再叫出来；画中画模式下整页只渲染这一块
 * （[showControls] = false），全屏模式则由页面把这一个组件铺满屏幕。
 */
@Composable
fun VideoStage(
    component: MediaPlayerComponent,
    state: MediaPlayerUiState,
    durationMs: Long,
    modifier: Modifier = Modifier,
    showControls: Boolean = true,
    fullscreen: Boolean = false,
    onAddFile: (() -> Unit)? = null,
    onToggleFullscreen: (() -> Unit)? = null,
) {
    val entry = state.current
    var controlsVisible by remember { mutableStateOf(true) }

    LaunchedEffect(controlsVisible, state.isPlaying, entry?.uri) {
        if (controlsVisible && state.isPlaying) {
            delay(CONTROLS_AUTO_HIDE_MS)
            controlsVisible = false
        }
    }
    // 暂停时控件必须能看见，否则用户找不到播放键
    val controlsShown = showControls && (controlsVisible || !state.isPlaying)
    val interactionSource = remember { MutableInteractionSource() }

    val stageModifier = if (fullscreen) {
        modifier
            .fillMaxSize()
            .background(Color.Black)
    } else {
        modifier
            .fillMaxWidth()
            .aspectRatio(videoAspectRatio(state))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
    }

    Box(
        modifier = stageModifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                if (entry == null) return@clickable
                // 暂停时点画面直接继续播放（比"只切控件"有反馈），播放中点画面收起/唤出控件
                if (!state.isPlaying) component.togglePlayPause() else controlsVisible = !controlsVisible
            },
        contentAlignment = Alignment.Center,
    ) {
        if (entry == null) {
            EmptyStage(onAddFile = onAddFile)
            return@Box
        }

        val textureView = remember { mutableStateOf<TextureView?>(null) }
        AndroidView(
            factory = { context -> TextureView(context).also { textureView.value = it } },
            update = { view ->
                // 播放中每次重组都会走 update；同一个 View 不需要重复 setVideoTextureView
                if (textureView.value !== view) {
                    textureView.value = view
                    component.attachVideoTexture(view)
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
        DisposableEffect(Unit) {
            onDispose { textureView.value?.let(component::detachVideoTexture) }
        }

        if (state.isBuffering) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(40.dp),
            )
        }

        AnimatedVisibility(
            visible = controlsShown,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MediaActionButton(
                        icon = Icons.Outlined.LineSkipPrevious,
                        contentDescription = stringResource(R.string.media_previous),
                        onClick = component::previous,
                        size = 52.dp,
                    )
                    PlayPauseCircle(
                        isPlaying = state.isPlaying,
                        contentDescription = stringResource(
                            if (state.isPlaying) R.string.media_pause else R.string.media_play
                        ),
                        onClick = component::togglePlayPause,
                        size = 66.dp,
                    )
                    MediaActionButton(
                        icon = Icons.Outlined.LineSkipNext,
                        contentDescription = stringResource(R.string.media_next),
                        onClick = component::next,
                        size = 52.dp,
                    )
                }

                if (onToggleFullscreen != null) {
                    MediaActionButton(
                        icon = Icons.Outlined.LineAspectRatio,
                        contentDescription = stringResource(R.string.media_fullscreen),
                        onClick = onToggleFullscreen,
                        size = 40.dp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(78.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = formatMediaDuration(state.playback.positionMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                    )
                    MediaSeekBar(
                        positionMs = state.playback.positionMs,
                        durationMs = durationMs,
                        onSeek = component::seekToFraction,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        showTimes = false,
                        activeColor = Color.White,
                        inactiveColor = Color.White.copy(alpha = 0.35f),
                    )
                    Text(
                        text = formatMediaDuration(durationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

private const val CONTROLS_AUTO_HIDE_MS = 3_000L

/**
 * 视频宽高比：直接用视频本身的尺寸。
 *
 * 以前把竖屏限制在 3:4，9:16 的手机会被 TextureView 横向拉宽；现在只在极端比例上兜底，
 * 竖屏视频按原比例留黑边。
 */
private fun videoAspectRatio(state: MediaPlayerUiState): Float {
    val width = state.playback.videoWidth.takeIf { it > 0 } ?: state.metadata.width
    val height = state.playback.videoHeight.takeIf { it > 0 } ?: state.metadata.height
    if (width <= 0 || height <= 0) return 16f / 9f
    return (width.toFloat() / height).coerceIn(0.42f, 2.6f)
}

@Composable
private fun EmptyStage(onAddFile: (() -> Unit)? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.LineMovie,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.media_now_playing_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f),
        )
        if (onAddFile != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Button(onClick = onAddFile) {
                Text(text = stringResource(R.string.media_add))
            }
        }
    }
}

/** 播放页的标题/作者两行。 */
@Composable
fun PlayerTitleBlock(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** 原型里那排圆形操作按钮。 */
@Composable
fun ActionRow(
    isFavorite: Boolean,
    enabled: Boolean = true,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    onInfo: () -> Unit,
    onOpenWith: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MediaActionButton(
            icon = if (isFavorite) {
                Icons.Outlined.LineFavoriteFilled
            } else {
                Icons.Outlined.LineFavorite
            },
            contentDescription = stringResource(R.string.media_favorite),
            onClick = onFavorite,
            selected = isFavorite,
            enabled = enabled,
        )
        MediaActionButton(
            icon = Icons.Outlined.LineShare,
            contentDescription = stringResource(R.string.media_share),
            onClick = onShare,
            enabled = enabled,
        )
        MediaActionButton(
            icon = Icons.Outlined.LineInfo,
            contentDescription = stringResource(R.string.media_file_info),
            onClick = onInfo,
            enabled = enabled,
        )
        MediaActionButton(
            icon = Icons.Outlined.LineOpenWith,
            contentDescription = stringResource(R.string.media_open_with),
            onClick = onOpenWith,
            enabled = enabled,
        )
    }
}

/** "最近观看/最近播放"横向卡片。 */
@Composable
fun RecentMediaCard(
    title: String,
    entries: List<MediaEntry>,
    kind: MediaKind,
    loadArtwork: suspend (MediaEntry, Int) -> android.graphics.Bitmap?,
    loadMetadata: suspend (MediaEntry) -> MediaMetadata,
    onSelect: (MediaEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        style = GlassStyle.Regular,
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            MediaSectionHeader(title = title)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
            ) {
                items(entries, key = { it.uri }) { entry ->
                    RecentMediaTile(
                        entry = entry,
                        kind = kind,
                        loadArtwork = loadArtwork,
                        loadMetadata = loadMetadata,
                        onClick = { onSelect(entry) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentMediaTile(
    entry: MediaEntry,
    kind: MediaKind,
    loadArtwork: suspend (MediaEntry, Int) -> android.graphics.Bitmap?,
    loadMetadata: suspend (MediaEntry) -> MediaMetadata,
    onClick: () -> Unit,
) {
    val needsMetadata = entry.durationMs <= 0L || entry.mediaTitle == null
    val metadata = rememberMediaMetadata(entry, enabled = needsMetadata, load = loadMetadata)
    val typeLabel = stringResource(
        if (kind == MediaKind.VIDEO) R.string.media_video_type else R.string.media_audio_type
    )
    val duration = metadata.durationMs.takeIf { it > 0L } ?: entry.durationMs

    Column(
        modifier = Modifier
            .width(148.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
    ) {
        Box {
            MediaArtwork(
                entry = entry,
                kind = kind,
                maxSize = 320,
                loadArtwork = loadArtwork,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.45f),
                iconSize = 30.dp,
            )
            DurationBadge(duration)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = metadata.title?.takeIf { it.isNotBlank() } ?: entry.bestTitle,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = metadata.displayArtist(entry.subtitleHint ?: typeLabel),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
