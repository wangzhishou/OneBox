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

package com.wanbaohe.media.domain

import android.view.TextureView
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class InlineMediaPlaybackState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val hasError: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val videoWidth: Int = 0,
    val videoHeight: Int = 0,
)

interface InlineMediaPlaybackController {
    val state: StateFlow<InlineMediaPlaybackState>

    fun load(uri: String, kind: MediaKind)
    fun togglePlayPause()
    fun seekToFraction(fraction: Float)
    fun attachVideoTexture(view: TextureView)
    fun detachVideoTexture(view: TextureView)
    fun pause()
    fun release()
}

interface InlineMediaPlaybackControllerFactory {
    fun create(): InlineMediaPlaybackController
}

@Singleton
internal class InlineMediaPlaybackCoordinator @Inject constructor() {
    private var activeController: InlineMediaPlaybackController? = null

    fun activate(controller: InlineMediaPlaybackController) {
        if (activeController === controller) return
        activeController?.pause()
        activeController = controller
    }

    fun release(controller: InlineMediaPlaybackController) {
        if (activeController === controller) activeController = null
    }
}
