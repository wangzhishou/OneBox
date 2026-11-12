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

import java.io.File

/**
 * 把 [StartupTraceEntry] 追加写入指定文件，超过 [maxBytes] 时滚动覆盖。
 *
 * 同步 I/O，调用方负责保证时机（启动期内单次写入 ~80B，整体 ≤ 2KB，
 * 总耗时 < 5ms，可接受）。如未来需要更高吞吐，改为通道 + 后台 writer 即可。
 */
internal class FileStartupTraceSink(
    private val file: File,
    private val maxBytes: Long = DEFAULT_MAX_BYTES,
) {
    private val lock = Any()

    fun write(entry: StartupTraceEntry) {
        synchronized(lock) {
            runCatching {
                val parent = file.parentFile
                if (parent != null && !parent.exists()) parent.mkdirs()
                if (!file.exists()) {
                    file.createNewFile()
                } else if (file.length() > maxBytes) {
                    file.delete()
                    file.createNewFile()
                }
                file.appendText(entry.formattedLine + "\n", Charsets.UTF_8)
            }
        }
    }

    fun reset() {
        synchronized(lock) {
            runCatching { file.delete() }
        }
    }

    companion object {
        const val DEFAULT_MAX_BYTES: Long = 64L * 1024L
    }
}
