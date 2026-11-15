/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wanbaohe.media.ui

import android.view.TextureView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.AsyncImage
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.media.R
import com.wanbaohe.media.domain.InlineMediaPlaybackController
import com.wanbaohe.media.domain.InlineMediaPlaybackControllerFactory
import com.wanbaohe.media.domain.InlineMediaPlaybackState
import com.wanbaohe.media.domain.MediaKind
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@Composable
fun MarkdownAudioPlayer(
    url: String,
    title: String,
    modifier: Modifier = Modifier,
) {
    val controller = rememberInlineMediaController(url, MediaKind.AUDIO)
    val state by controller.state.collectAsState()
    val displayTitle = title.ifBlank { stringResource(R.string.media_inline_audio) }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        style = GlassStyle.Regular,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlayPauseCircle(
                isPlaying = state.isPlaying,
                contentDescription = stringResource(
                    if (state.isPlaying) R.string.media_pause else R.string.media_play
                ),
                onClick = controller::togglePlayPause,
                size = 52.dp,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayTitle,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (state.isBuffering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                MediaSeekBar(
                    positionMs = state.positionMs,
                    durationMs = state.durationMs,
                    onSeek = controller::seekToFraction,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (state.hasError) {
                    Text(
                        text = stringResource(R.string.media_inline_play_error),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
fun MarkdownVideoPlayer(
    url: String,
    title: String,
    posterUrl: String? = null,
    modifier: Modifier = Modifier,
    onOpenFullScreen: (() -> Unit)? = null,
) {
    val controller = rememberInlineMediaController(url, MediaKind.VIDEO)
    val state by controller.state.collectAsState()
    val aspectRatio = state.aspectRatio()

    Column(modifier = modifier.fillMaxWidth()) {
        if (title.isNotBlank()) {
            Text(
                text = title,
                modifier = Modifier.padding(bottom = 6.dp),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black),
        ) {
            AndroidView(
                factory = { context ->
                    TextureView(context).also(controller::attachVideoTexture)
                },
                onRelease = controller::detachVideoTexture,
                modifier = Modifier.fillMaxSize(),
                update = {},
            )
            if (!posterUrl.isNullOrBlank() && state.positionMs == 0L && !state.isPlaying) {
                AsyncImage(
                    model = posterUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            if (state.isBuffering) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White,
                )
            } else if (!state.hasError) {
                PlayPauseCircle(
                    isPlaying = state.isPlaying,
                    contentDescription = stringResource(
                        if (state.isPlaying) R.string.media_pause else R.string.media_play
                    ),
                    onClick = controller::togglePlayPause,
                    modifier = Modifier.align(Alignment.Center),
                    size = 64.dp,
                )
            }
            onOpenFullScreen?.let { openFullScreen ->
                MediaActionButton(
                    icon = MediaFullscreenIcon,
                    contentDescription = stringResource(R.string.media_fullscreen),
                    onClick = {
                        controller.pause()
                        openFullScreen()
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    size = 40.dp,
                )
            }
            MediaSeekBar(
                positionMs = state.positionMs,
                durationMs = state.durationMs,
                onSeek = controller::seekToFraction,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 8.dp),
                showTimes = false,
                activeColor = Color.White,
                inactiveColor = Color.White.copy(alpha = 0.35f),
                thumbColor = Color.White,
            )
            if (state.hasError) {
                InlineVideoError(
                    onRetry = controller::togglePlayPause,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

@Composable
private fun InlineVideoError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.72f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.media_play_error),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text(stringResource(R.string.media_retry))
        }
    }
}

private fun InlineMediaPlaybackState.aspectRatio(): Float {
    if (videoWidth <= 0 || videoHeight <= 0) return 16f / 9f
    return (videoWidth.toFloat() / videoHeight.toFloat()).coerceIn(0.5f, 2.4f)
}

@Composable
fun InlineMediaPlayerScope(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val factory = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            InlineMediaEntryPoint::class.java,
        ).factory()
    }
    val registry = remember(factory) { InlineMediaControllerRegistry(factory) }
    DisposableEffect(registry) {
        onDispose { registry.releaseAll() }
    }
    CompositionLocalProvider(
        LocalInlineMediaControllerRegistry provides registry,
        content = content,
    )
}

private class InlineMediaControllerRegistry(
    private val factory: InlineMediaPlaybackControllerFactory,
) {
    private val controllers = mutableMapOf<String, InlineMediaPlaybackController>()

    fun obtain(url: String, kind: MediaKind): InlineMediaPlaybackController {
        val key = "${kind.name}|$url"
        return controllers.getOrPut(key) {
            factory.create().also { it.load(url, kind) }
        }
    }

    fun releaseAll() {
        controllers.values.forEach { it.release() }
        controllers.clear()
    }
}

private val LocalInlineMediaControllerRegistry = compositionLocalOf<InlineMediaControllerRegistry?> {
    null
}

@Composable
private fun rememberInlineMediaController(
    url: String,
    kind: MediaKind,
): InlineMediaPlaybackController {
    val registry = requireNotNull(LocalInlineMediaControllerRegistry.current) {
        "Markdown media must be wrapped in InlineMediaPlayerScope"
    }
    val controller = remember(registry, url, kind) { registry.obtain(url, kind) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(controller, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) controller.pause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            controller.pause()
        }
    }
    return controller
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface InlineMediaEntryPoint {
    fun factory(): InlineMediaPlaybackControllerFactory
}
