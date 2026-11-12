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
