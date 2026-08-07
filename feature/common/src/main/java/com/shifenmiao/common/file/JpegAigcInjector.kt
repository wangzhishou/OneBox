package com.shifenmiao.common.file

import androidx.annotation.Keep

/**
 * JPEG AIGC XMP APP1 字节级注入/提取器。
 *
 * 按 GB 45438-2025 6.2 节:
 *   JPEG / JPG 文件,将元数据写入 APP1 中标签名为 XMP 的字段。
 *
 * JPEG segment 结构:
 * - SOI(0xFFD8)
 * - 若干 segment(每个:marker=0xFF+type,length=2 字节大端含自身,data)
 * - EOI(0xFFD9)
 *
 * XMP APP1 segment 布局:
 * - marker: 0xFFE1
 * - length: 2 字节大端(包含 length 自身 2 字节,不含 marker)
 * - data: `"XMP\0XMP\0"` (10 字节固定 header) + XMP packet 内容
 */
@Keep
internal object JpegAigcInjector {

    private val JPEG_SOI = byteArrayOf(0xFF.toByte(), 0xD8.toByte())
    // "XMP\0XMP\0" —— XMP APP1 segment 固定 10 字节标识头
    private val XMP_HEADER_BYTES = byteArrayOf(
        0x58, 0x4D, 0x50, 0x00,
        0x58, 0x4D, 0x50, 0x00,
    )
    private const val APP1_MARKER = 0xE1
    private const val APP1_MARKER_LEN = 2

    internal fun isJpeg(bytes: ByteArray): Boolean {
        if (bytes.size < 2) return false
        return bytes[0] == JPEG_SOI[0] && bytes[1] == JPEG_SOI[1]
    }

    /**
     * 在 JPEG 字节流中注入 AIGC XMP APP1 segment。
     * 若已存在 XMP APP1(以 "XMP\0XMP\0" 标识),则替换;否则在 SOI 之后插入。
     * 非 JPEG 字节流原样返回。
     *
     * 注意:这里的 XMP packet 直接由 [XmpAigcPacket.build] 生成,内容**包含** `<?xpacket begin ?>` / `<?xpacket end ?>` 头尾。
     * 标准 XMP APP1 字段要求 **不含** xpacket 头尾(直接是 xmpmeta)。这里为了工具兼容性选择保留,
     * 与 ExifTool / Photoshop 等写入保持一致。
     */
    fun injectOrReplace(jpegBytes: ByteArray, aigcJson: String): ByteArray {
        if (!isJpeg(jpegBytes)) return jpegBytes
        val xmpPacket = XmpAigcPacket.build(aigcJson)
        val newSegment = buildApp1XmpSegment(xmpPacket)

        val existingRange = findXmpApp1Range(jpegBytes)
        return if (existingRange != null) {
            val start = existingRange.first
            val end = existingRange.last + 1
            val out = ByteArray(jpegBytes.size - (end - start) + newSegment.size)
            System.arraycopy(jpegBytes, 0, out, 0, start)
            System.arraycopy(newSegment, 0, out, start, newSegment.size)
            System.arraycopy(jpegBytes, end, out, start + newSegment.size, jpegBytes.size - end)
            out
        } else {
            // 在 SOI 之后插入
            val out = ByteArray(jpegBytes.size + newSegment.size)
            System.arraycopy(jpegBytes, 0, out, 0, 2)
            System.arraycopy(newSegment, 0, out, 2, newSegment.size)
            System.arraycopy(jpegBytes, 2, out, 2 + newSegment.size, jpegBytes.size - 2)
            out
        }
    }

    fun extract(jpegBytes: ByteArray): String? {
        if (!isJpeg(jpegBytes)) return null
        val range = findXmpApp1Range(jpegBytes) ?: return null
        val start = range.first
        val end = range.last + 1
        val data = jpegBytes.copyOfRange(start, end)
        // 跳过 "XMP\0XMP\0" 8 字节 header
        if (data.size <= XMP_HEADER_BYTES.size) return null
        for (i in XMP_HEADER_BYTES.indices) {
            if (data[i] != XMP_HEADER_BYTES[i]) return null
        }
        val xmpText = data.copyOfRange(XMP_HEADER_BYTES.size, data.size).toString(Charsets.UTF_8)
        return XmpAigcPacket.extractAigcJson(xmpText)
    }

    /**
     * 返回现有 XMP APP1 的 [start, end) 区间(start 指向 APP1 marker,end 指向 segment 末尾+1)。
     * 找不到时返回 null。
     */
    private fun findXmpApp1Range(bytes: ByteArray): IntRange? {
        var i = 2
        while (i + 4 <= bytes.size) {
            if (bytes[i] != 0xFF.toByte()) return null
            val marker = bytes[i + 1].toInt() and 0xFF
            if (marker == 0xD9 /* EOI */) return null
            if (marker == 0xDA /* SOS */) return null // 扫描数据开始,后面不再解析 segment
            if (marker == 0x00 || marker == 0xFF) {
                i += 2
                continue
            }
            val segLength = ((bytes[i + 2].toInt() and 0xFF) shl 8) or
                (bytes[i + 3].toInt() and 0xFF)
            if (segLength < 2) return null
            val segEnd = i + 2 + segLength

            if (marker == APP1_MARKER && segEnd <= bytes.size) {
                val data = bytes.copyOfRange(i + 4, segEnd)
                if (data.size > XMP_HEADER_BYTES.size &&
                    startsWithXmpHeader(data)
                ) {
                    return i until segEnd
                }
            }
            i = segEnd
        }
        return null
    }

    private fun startsWithXmpHeader(data: ByteArray): Boolean {
        for (i in XMP_HEADER_BYTES.indices) {
            if (data[i] != XMP_HEADER_BYTES[i]) return false
        }
        return true
    }

    private fun buildApp1XmpSegment(xmpPacket: String): ByteArray {
        val xmpBytes = xmpPacket.toByteArray(Charsets.UTF_8)
        val dataLength = XMP_HEADER_BYTES.size + xmpBytes.size
        val segLength = 2 + dataLength // length 字段含自身 2 字节
        val out = ByteArray(APP1_MARKER_LEN + 2 + dataLength)
        // marker
        out[0] = 0xFF.toByte()
        out[1] = APP1_MARKER.toByte()
        // length
        out[2] = ((segLength ushr 8) and 0xFF).toByte()
        out[3] = (segLength and 0xFF).toByte()
        // XMP header
        System.arraycopy(XMP_HEADER_BYTES, 0, out, 4, XMP_HEADER_BYTES.size)
        // XMP packet
        System.arraycopy(xmpBytes, 0, out, 4 + XMP_HEADER_BYTES.size, xmpBytes.size)
        return out
    }

    private data class IntPair(val start: Int, val end: Int)
}
