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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 单次启动链路中的一个埋点。
 */
data class StartupTraceEntry(
    val sequence: Int,
    val stage: String,
    val totalMs: Long,
    val deltaMs: Long,
) {
    val formattedLine: String
        get() = "#${sequence.toString().padStart(2, '0')} +${totalMs}ms Δ${deltaMs}ms $stage"
}

internal class StartupTraceRecorder(
    initialSinks: List<(StartupTraceEntry) -> Unit> = emptyList(),
) {
    private val lock = Any()
    private val emittedKeys = linkedSetOf<String>()
    private var startElapsedMs = 0L
    private var lastElapsedMs = 0L
    private var sequence = 0

    @Volatile
    private var sinks: List<(StartupTraceEntry) -> Unit> = initialSinks

    private val _entries = MutableStateFlow<List<StartupTraceEntry>>(emptyList())
    val entries: StateFlow<List<StartupTraceEntry>> = _entries.asStateFlow()

    fun begin(
        stage: String,
        startedAtElapsedMs: Long = SystemClock.elapsedRealtime(),
    ) {
        synchronized(lock) {
            val entry = StartupTraceEntry(
                sequence = 0,
                stage = stage,
                totalMs = 0L,
                deltaMs = 0L,
            )
            startElapsedMs = startedAtElapsedMs
            lastElapsedMs = startedAtElapsedMs
            sequence = 0
            emittedKeys.clear()
            _entries.value = listOf(entry)
            emitSafely(entry)
        }
    }

    fun mark(stage: String) {
        synchronized(lock) {
            ensureSessionStarted()
            val now = SystemClock.elapsedRealtime()
            sequence += 1
            val entry = StartupTraceEntry(
                sequence = sequence,
                stage = stage,
                totalMs = now - startElapsedMs,
                deltaMs = now - lastElapsedMs,
            )
            _entries.value += entry
            lastElapsedMs = now
            emitSafely(entry)
        }
    }

    fun markOnce(key: String, stage: String) {
        synchronized(lock) {
            if (!emittedKeys.add(key)) return
        }
        mark(stage)
    }

    fun clear() {
        synchronized(lock) {
            startElapsedMs = 0L
            lastElapsedMs = 0L
            sequence = 0
            emittedKeys.clear()
            _entries.value = emptyList()
        }
    }

    /**
     * 追加一个 sink。sink 异常被吞掉，永不抛出——避免遥测代码拖垮启动链路。
     */
    fun addSink(sink: (StartupTraceEntry) -> Unit) {
        synchronized(lock) {
            sinks = sinks + sink
        }
    }

    private fun emitSafely(entry: StartupTraceEntry) {
        val current = sinks
        current.forEach { sink ->
            try {
                sink(entry)
            } catch (_: Throwable) {
                // 永不因遥测失败而 crash
            }
        }
    }

    private fun ensureSessionStarted() {
        if (startElapsedMs == 0L) {
            begin("implicit_begin")
        }
    }
}
