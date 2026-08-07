package com.t8rin.imagetoolbox.core.domain.performance

import android.util.Log
import kotlinx.coroutines.flow.StateFlow
import java.io.File

object StartupTrace {
    private const val TAG = "StartupTrace"

    private val recorder = StartupTraceRecorder(
        initialSinks = listOf { entry ->
            Log.d(TAG, entry.formattedLine)
        },
    )

    val entries: StateFlow<List<StartupTraceEntry>> = recorder.entries

    fun setEnabled(enabled: Boolean) = Unit

    fun begin(stage: String) {
        recorder.begin(stage)
    }

    fun mark(stage: String) {
        recorder.mark(stage)
    }

    fun markOnce(key: String, stage: String) {
        recorder.markOnce(key, stage)
    }

    /**
     * 注册文件 sink：把每次埋点追加写入 [file]，超出 maxBytes 时滚动覆盖。
     * 主要供 release 变体使用，方便现场通过 `adb pull` 读取启动链路。
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
}
