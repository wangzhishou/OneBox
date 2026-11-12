/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.base.player

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import com.shifenmiao.interfaces.player.AudioPlayStatusListener
import java.io.File

class AudioPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var audioPlayStatusListener: AudioPlayStatusListener? = null
    private val handler = Handler(Looper.getMainLooper())

    private val updateProgressTask = object : Runnable {
        override fun run() {
            if (audioPlayStatusListener != null) {
                val progress = mediaPlayer?.currentPosition ?: 0
                audioPlayStatusListener?.onPlayProgress(progress)
                handler.postDelayed(this, 1000) // update every second
            }
        }
    }

    init {
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setOnCompletionListener {
            audioPlayStatusListener?.onPlayStop()
        }
    }

    fun setPlayStatusListener(listener: AudioPlayStatusListener) {
        this.audioPlayStatusListener = listener
    }

    fun startPlaying(file: File) {
        mediaPlayer?.reset()
        mediaPlayer?.setDataSource(file.absolutePath)
        mediaPlayer?.prepare()
        mediaPlayer?.start()
        handler.post(updateProgressTask) // start updating progress
        audioPlayStatusListener?.onPlayStart()
    }

    fun stopPlaying() {
        mediaPlayer?.stop()
        handler.removeCallbacks(updateProgressTask) // stop updating progress
        audioPlayStatusListener?.onPlayStop()
    }

    fun pausePlaying() {
        mediaPlayer?.pause()
        handler.removeCallbacks(updateProgressTask) // stop updating progress
        audioPlayStatusListener?.onPlayPause()
    }

    fun resumePlaying() {
        mediaPlayer?.start()
        handler.post(updateProgressTask) // start updating progress
        audioPlayStatusListener?.onPlayResume()
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacks(updateProgressTask) // stop updating progress
    }
}