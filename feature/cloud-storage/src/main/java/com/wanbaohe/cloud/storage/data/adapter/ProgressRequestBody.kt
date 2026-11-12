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

package com.wanbaohe.cloud.storage.data.adapter

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.buffer

/**
 * 通用进度回调 RequestBody —— 给 OkHttp 适配器做上传进度。
 */
internal class ProgressRequestBody(
    private val bytes: ByteArray,
    private val mediaType: MediaType?,
    private val onProgress: (Float) -> Unit,
) : RequestBody() {
    override fun contentType(): MediaType? = mediaType
    override fun contentLength(): Long = bytes.size.toLong()
    override fun writeTo(sink: BufferedSink) {
        val total = bytes.size.toLong()
        val counting = object : ForwardingSink(sink) {
            private var written = 0L
            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                written += byteCount
                if (total > 0) onProgress(written.toFloat() / total)
            }
        }
        val buffered: BufferedSink = counting.buffer()
        buffered.write(bytes)
        buffered.flush()
    }
}
