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
