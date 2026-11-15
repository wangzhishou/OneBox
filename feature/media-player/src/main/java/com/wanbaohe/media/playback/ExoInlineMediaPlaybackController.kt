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

package com.wanbaohe.media.playback

import android.content.Context
import android.view.TextureView
import androidx.annotation.MainThread
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.media.domain.InlineMediaPlaybackController
import com.wanbaohe.media.domain.InlineMediaPlaybackControllerFactory
import com.wanbaohe.media.domain.InlineMediaPlaybackCoordinator
import com.wanbaohe.media.domain.InlineMediaPlaybackState
import com.wanbaohe.media.domain.MediaKind
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class ExoInlineMediaPlaybackControllerFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchersHolder: DispatchersHolder,
    private val coordinator: InlineMediaPlaybackCoordinator,
) : InlineMediaPlaybackControllerFactory {
    override fun create(): InlineMediaPlaybackController = ExoInlineMediaPlaybackController(
        context = context,
        dispatchersHolder = dispatchersHolder,
        coordinator = coordinator,
    )
}

internal class ExoInlineMediaPlaybackController(
    private val context: Context,
    dispatchersHolder: DispatchersHolder,
    private val coordinator: InlineMediaPlaybackCoordinator,
) : InlineMediaPlaybackController {

    private val _state = MutableStateFlow(InlineMediaPlaybackState())
    override val state: StateFlow<InlineMediaPlaybackState> = _state.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + dispatchersHolder.uiDispatcher)
    private var player: ExoPlayer? = null
    private var activeTextureView: TextureView? = null
    private var loadedUri: String? = null
    private var released = false

    init {
        scope.launch {
            while (isActive) {
                player?.takeIf { it.isPlaying }?.let(::syncProgress)
                delay(POSITION_TICK_MS)
            }
        }
    }

    @MainThread
    override fun load(uri: String, kind: MediaKind) {
        if (released || loadedUri == uri) return
        loadedUri = uri
        val player = ensurePlayer()
        player.stop()
        player.clearMediaItems()
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(
                    if (kind == MediaKind.VIDEO) C.AUDIO_CONTENT_TYPE_MOVIE
                    else C.AUDIO_CONTENT_TYPE_MUSIC
                )
                .build(),
            true,
        )
        _state.value = InlineMediaPlaybackState(isBuffering = true)
        player.setMediaItem(
            MediaItem.Builder()
                .setUri(uri)
                .setMediaId(uri)
                .build()
        )
        player.prepare()
        player.playWhenReady = false
    }

    @MainThread
    override fun togglePlayPause() {
        if (released) return
        val player = ensurePlayer()
        if (player.currentMediaItem == null) return
        when {
            _state.value.hasError -> {
                _state.value = _state.value.copy(hasError = false, isBuffering = true)
                player.prepare()
                coordinator.activate(this)
                player.play()
            }

            player.playbackState == Player.STATE_ENDED -> {
                player.seekTo(0L)
                coordinator.activate(this)
                player.play()
            }

            player.isPlaying -> player.pause()
            else -> {
                coordinator.activate(this)
                player.play()
            }
        }
    }

    @MainThread
    override fun seekToFraction(fraction: Float) {
        val player = player ?: return
        val duration = player.duration.takeIf { it > 0L && it != C.TIME_UNSET } ?: return
        player.seekTo((duration * fraction.coerceIn(0f, 1f)).toLong())
        syncProgress(player)
    }

    @MainThread
    override fun attachVideoTexture(view: TextureView) {
        if (released) return
        activeTextureView = view
        ensurePlayer().setVideoTextureView(view)
    }

    @MainThread
    override fun detachVideoTexture(view: TextureView) {
        if (activeTextureView !== view) return
        activeTextureView = null
        player?.clearVideoSurface()
    }

    @MainThread
    override fun pause() {
        player?.pause()
    }

    @MainThread
    override fun release() {
        if (released) return
        released = true
        coordinator.release(this)
        scope.cancel()
        activeTextureView = null
        player?.release()
        player = null
    }

    @MainThread
    private fun ensurePlayer(): ExoPlayer {
        player?.let { return it }
        val renderersFactory = DefaultRenderersFactory(context)
            .setEnableDecoderFallback(true)
        return ExoPlayer.Builder(context, renderersFactory).build().apply {
            setHandleAudioBecomingNoisy(true)
            repeatMode = Player.REPEAT_MODE_OFF
            addListener(listener)
        }.also { player = it }
    }

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.value = _state.value.copy(
                isPlaying = isPlaying,
                hasError = if (isPlaying) false else _state.value.hasError,
            )
            player?.let(::syncProgress)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val player = player ?: return
            _state.value = _state.value.copy(
                isBuffering = playbackState == Player.STATE_BUFFERING,
                isPlaying = player.isPlaying,
            )
            syncProgress(player)
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            _state.value = _state.value.copy(
                videoWidth = videoSize.width,
                videoHeight = videoSize.height,
            )
        }

        override fun onPlayerError(error: PlaybackException) {
            _state.value = _state.value.copy(
                isPlaying = false,
                isBuffering = false,
                hasError = true,
            )
        }
    }

    private fun syncProgress(player: ExoPlayer) {
        val duration = player.duration.takeIf { it > 0L && it != C.TIME_UNSET } ?: 0L
        _state.value = _state.value.copy(
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = duration,
        )
    }

    private companion object {
        const val POSITION_TICK_MS = 500L
    }
}
