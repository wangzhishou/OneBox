package com.wanbaohe.media.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.min
import com.wanbaohe.media.R
import com.wanbaohe.media.component.MediaPlayerComponent
import com.wanbaohe.media.component.MediaPlayerUiState
import com.wanbaohe.media.domain.PlaybackMode

/**
 * 音乐播放页正文（原型"正在播放"）。
 *
 * 布局按原型的纵向节奏：大封面 → 标题/作者 → 操作圆钮 → 进度条 → 上一首/播放/下一首
 * → 播放模式/播放队列两行。整页用滚动容器，小屏或大字体下也不会被截断。
 */
@Composable
fun AudioPlayerContent(
    component: MediaPlayerComponent,
    state: MediaPlayerUiState,
    onAddFile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val duration = state.playback.durationMs.takeIf { it > 0L } ?: state.metadata.durationMs
    val artist = state.metadata.displayArtist(
        state.current?.subtitleHint ?: stringResource(R.string.media_unknown_artist)
    )

    if (state.current == null) {
        EmptyAudioPlayer(onAddFile = onAddFile, modifier = modifier)
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AudioCover(component = component, state = state)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = state.displayTitle().ifBlank { stringResource(R.string.media_now_playing_hint) },
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = artist,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )


        if (state.playback.hasError) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.media_play_error),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MediaActionButton(
                icon = if (state.isFavorite) {
                    MediaFavoriteIcon
                } else {
                    MediaFavoriteBorderIcon
                },
                contentDescription = stringResource(R.string.media_favorite),
                onClick = component::toggleFavorite,
                selected = state.isFavorite,
            )
            MediaActionButton(
                icon = MediaShuffleIcon,
                contentDescription = stringResource(R.string.media_mode_shuffle),
                onClick = {
                    component.setMode(
                        if (state.mode == PlaybackMode.SHUFFLE) {
                            PlaybackMode.SEQUENTIAL
                        } else {
                            PlaybackMode.SHUFFLE
                        }
                    )
                },
                selected = state.mode == PlaybackMode.SHUFFLE,
            )
            MediaActionButton(
                icon = MediaQueueIcon,
                contentDescription = stringResource(R.string.media_playback_queue),
                onClick = { component.showQueue(true) },
            )
            MediaActionButton(
                icon = MediaOpenInNewIcon,
                contentDescription = stringResource(R.string.media_open_with),
                onClick = component::openWithOtherApp,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        MediaSeekBar(
            positionMs = state.playback.positionMs,
            durationMs = duration,
            onSeek = component::seekToFraction,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MediaActionButton(
                icon = MediaSkipPreviousIcon,
                contentDescription = stringResource(R.string.media_previous),
                onClick = component::previous,
                size = 56.dp,
            )
            if (state.isBuffering) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(44.dp),
                )
            } else {
                PlayPauseCircle(
                    isPlaying = state.isPlaying,
                    contentDescription = stringResource(
                        if (state.isPlaying) R.string.media_pause else R.string.media_play
                    ),
                    onClick = component::togglePlayPause,
                    size = 76.dp,
                )
            }
            MediaActionButton(
                icon = MediaSkipNextIcon,
                contentDescription = stringResource(R.string.media_next),
                onClick = component::next,
                size = 56.dp,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        MediaInfoRow(
            icon = playbackModeIcon(state.mode),
            title = stringResource(R.string.media_playback_mode),
            subtitle = playbackModeLabel(state.mode),
            onClick = { component.showModeSheet(true) },
        )

        Spacer(modifier = Modifier.height(12.dp))

        MediaInfoRow(
            icon = MediaQueueIcon,
            title = stringResource(R.string.media_playback_queue),
            subtitle = pluralStringResource(
                R.plurals.media_queue_count,
                state.queue.size,
                state.queue.size,
            ),
            onClick = { component.showQueue(true) },
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/** 还没选文件时的空播放器：渐变封面 + 一句提示 + "去添加"。 */
@Composable
private fun EmptyAudioPlayer(
    onAddFile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer,
                        )
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.Icon(
                imageVector = MediaMusicIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f),
                modifier = Modifier.size(56.dp),
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.media_now_playing_hint),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        androidx.compose.material3.Button(onClick = onAddFile) {
            Text(text = stringResource(R.string.media_add))
        }
    }
}

/**
 * 方形封面。
 *
 * 尺寸跟着可用宽度走（小屏不撑破页面），最大 260dp；没有内嵌封面时画渐变占位。
 */
@Composable
private fun AudioCover(
    component: MediaPlayerComponent,
    state: MediaPlayerUiState,
) {
    val entry = state.current
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val side = min(maxWidth, 260.dp)
        if (entry == null) {
            Box(modifier = Modifier.size(side))
        } else {
            MediaArtwork(
                entry = entry,
                kind = state.kind,
                maxSize = 512,
                loadArtwork = component::artwork,
                modifier = Modifier
                    .size(side)
                    .aspectRatio(1f),
                shape = RoundedCornerShape(24.dp),
                iconSize = 64.dp,
            )
        }
    }
}
