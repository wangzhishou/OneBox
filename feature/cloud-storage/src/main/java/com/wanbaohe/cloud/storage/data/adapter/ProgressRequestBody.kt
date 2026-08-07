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
