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

package com.shifenmiao.common.voice

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlin.math.sqrt

/**
 * PCM 录音封装:AudioRecord 采集 16kHz 单声道 16bit PCM,
 * 按 1280B(约 40ms 音频)一块回调给讯飞 WebSocket,同时回调 RMS 音量(0..1)供 UI 做脉冲动画。
 * 权限(RECORD_AUDIO)已在 UI 层申请,故此处抑制 MissingPermission。
 */
class PcmAudioRecorder {

    private var audioRecord: AudioRecord? = null
    private var readThread: Thread? = null

    @Volatile
    private var recording = false

    @SuppressLint("MissingPermission")
    fun start(onChunk: (ByteArray) -> Unit, onRms: (Double) -> Unit) {
        stop()
        val minBuffer = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val record = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBuffer * 2
        )
        check(record.state == AudioRecord.STATE_INITIALIZED) { "AudioRecord init failed" }
        audioRecord = record
        record.startRecording()
        recording = true
        readThread = Thread({
            val buffer = ByteArray(CHUNK_SIZE)
            while (recording) {
                val read = record.read(buffer, 0, buffer.size)
                if (read > 0) {
                    val chunk = buffer.copyOf(read)
                    onChunk(chunk)
                    onRms(computeRms(chunk))
                }
            }
        }, "pcm-audio-recorder").also { it.start() }
    }

    fun stop() {
        recording = false
        readThread?.join(300)
        readThread = null
        audioRecord?.let {
            runCatching { it.stop() }
            it.release()
        }
        audioRecord = null
    }

    /** 归一化 RMS:little-endian 解析 short,sqrt(mean(pcm^2)) / Short.MAX_VALUE,约束到 0..1 */
    private fun computeRms(chunk: ByteArray): Double {
        var sum = 0.0
        var count = 0
        var i = 0
        while (i + 1 < chunk.size) {
            val sample = (chunk[i + 1].toInt() shl 8) or (chunk[i].toInt() and 0xFF)
            sum += sample.toDouble() * sample
            count++
            i += 2
        }
        if (count == 0) return 0.0
        return (sqrt(sum / count) / Short.MAX_VALUE).coerceIn(0.0, 1.0)
    }

    companion object {
        const val SAMPLE_RATE = 16000
        /** 16kHz * 2B * 40ms = 1280B,与讯飞建议的 40ms/帧对齐 */
        const val CHUNK_SIZE = 1280
    }
}
