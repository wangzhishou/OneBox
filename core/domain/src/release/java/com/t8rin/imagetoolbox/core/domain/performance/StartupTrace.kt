/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
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

package com.t8rin.imagetoolbox.core.domain.performance

import android.os.SystemClock
import kotlinx.coroutines.flow.StateFlow
import java.io.File

object StartupTrace {
    private val recorder = StartupTraceRecorder()
    private val lock = Any()

    @Volatile
    private var enabled = false

    private var sessionStarted = false
    private var pendingStartStage: String? = null
    private var pendingStartElapsedMs = 0L

    val entries: StateFlow<List<StartupTraceEntry>> = recorder.entries

    fun setEnabled(enabled: Boolean) {
        if (this.enabled == enabled) return

        synchronized(lock) {
            if (this.enabled == enabled) return
            this.enabled = enabled

            if (!enabled) {
                sessionStarted = false
                recorder.clear()
                return
            }

            startSessionIfNeededLocked()
        }
    }

    fun begin(stage: String) {
        val now = SystemClock.elapsedRealtime()

        synchronized(lock) {
            pendingStartStage = stage
            pendingStartElapsedMs = now
            sessionStarted = false

            if (!enabled) return

            recorder.begin(stage = stage, startedAtElapsedMs = now)
            sessionStarted = true
        }
    }

    fun mark(stage: String) {
        if (!enabled) return

        synchronized(lock) {
            if (!enabled) return
            startSessionIfNeededLocked()
        }

        recorder.mark(stage)
    }

    fun markOnce(key: String, stage: String) {
        if (!enabled) return

        synchronized(lock) {
            if (!enabled) return
            startSessionIfNeededLocked()
        }

        recorder.markOnce(key, stage)
    }

    /**
     * 注册文件 sink：把每次埋点追加写入 [file]，超出 maxBytes 时滚动覆盖。
     * 这是 release 变体下让生产数据可见的核心通道。
     */
    fun addFileSink(file: File) {
        recorder.addSink(FileStartupTraceSink(file)::write)
    }

    /**
     * 注册一个自定义 sink。sink 异常会被吞掉，永不抛出。
     */
    fun addSink(sink: (StartupTraceEntry) -> Unit) {
        recorder.addSink(sink)
    }

    private fun startSessionIfNeededLocked() {
        if (sessionStarted) return

        val startStage = pendingStartStage ?: "implicit_begin"
        val startedAtElapsedMs = pendingStartElapsedMs.takeIf { it > 0L }
            ?: SystemClock.elapsedRealtime()

        recorder.begin(
            stage = startStage,
            startedAtElapsedMs = startedAtElapsedMs,
        )
        sessionStarted = true
    }
}
