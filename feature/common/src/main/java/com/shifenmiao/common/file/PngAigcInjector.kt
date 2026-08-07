package com.shifenmiao.common.file

import androidx.annotation.Keep
import java.io.ByteArrayOutputStream
import java.util.zip.CRC32

/**
 * PNG AIGC 隐式标识字节级注入/提取器。
 *
 * 按 GB 45438-2025 标准,字段名是 AIGC,值是 7 要素 JSON。
 * 在 PNG 中通过自定义 iTXt chunk 实现:
 * ```
 * iTXt chunk: keyword = "AIGC", text = 7要素 JSON
 * ```
 *
 * 该实现不依赖任何 XMP 库,直接在 PNG 字节层面操作:
 * - 找到 IEND chunk 位置(末尾);
 * - 在 IEND 之前插入/替换已有的 iTXt(AIGC) chunk;
 * - CRC-32 校验值与 iTXt 规范一致。
 */
@Keep
internal object PngAigcInjector {

    private val PNG_SIGNATURE = byteArrayOf(
        0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte(),
        0x0D.toByte(), 0x0A.toByte(), 0x1A.toByte(), 0x0A.toByte()
    )
    /**
     * 按 GB 45438-2025 6.2 节:
     *   PNG 文件,将元数据写入类型为 iTXt 的 XMP 字段
     * 即 iTXt chunk 的 keyword = `XML:com.adobe.xmp`,text = 完整 XMP packet。
     */
    private const val XMP_KEYWORD = "XML:com.adobe.xmp"
    private const val ITXT_TYPE = 0x69545874 // "iTXt"
    private const val IEND_TYPE = 0x49454E44 // "IEND"

    /**
     * 向 PNG 字节流注入 AIGC XMP iTXt chunk(若已存在则替换)。
     * 非 PNG 字节流直接原样返回。
     *
     * @param aigcJson 7 要素 JSON 字符串(不含外层 AIGC 包裹)
     */
    fun injectOrReplace(pngBytes: ByteArray, aigcJson: String): ByteArray {
        if (!isPng(pngBytes)) return pngBytes
        val xmpPacket = XmpAigcPacket.build(aigcJson)
        val chunks = collectChunks(pngBytes)
        val xmpIndex = chunks.indexOfFirst { it.isAigcXmpITxt(pngBytes) }
        val newChunk = buildITxtChunk(XMP_KEYWORD, xmpPacket)
        return if (xmpIndex >= 0) {
            replaceChunk(pngBytes, chunks[xmpIndex], newChunk)
        } else {
            insertBeforeIend(pngBytes, chunks, newChunk)
        }
    }

    /**
     * 从 PNG 字节流中提取 AIGC iTXt(XMP) chunk 并解析出 7 要素 JSON。
     * 非 PNG 字节流或找不到 AIGC iTXt 时返回 null。
     */
    fun extract(pngBytes: ByteArray): String? {
        if (!isPng(pngBytes)) return null
        val chunks = collectChunks(pngBytes)
        return chunks.firstOrNull { it.isAigcXmpITxt(pngBytes) }?.let { chunk ->
            val xmpText = decodeITxtText(pngBytes, chunk) ?: return null
            XmpAigcPacket.extractAigcJson(xmpText)
        }
    }

    internal fun isPng(bytes: ByteArray): Boolean {
        if (bytes.size < PNG_SIGNATURE.size) return false
        for (i in PNG_SIGNATURE.indices) {
            if (bytes[i] != PNG_SIGNATURE[i]) return false
        }
        return true
    }

    /**
     * 单个 chunk 的位置信息:[chunkType] 之后的字节,绝对偏移
     */
    private data class ChunkRange(
        val type: Int,
        val dataOffset: Int,
        val dataLength: Int,
    ) {
        fun isAigcXmpITxt(bytes: ByteArray): Boolean {
            if (type != ITXT_TYPE) return false
            if (dataLength < XMP_KEYWORD.length + 1) return false
            for (i in XMP_KEYWORD.indices) {
                if (bytes[dataOffset + i] != XMP_KEYWORD[i].code.toByte()) return false
            }
            return bytes[dataOffset + XMP_KEYWORD.length] == 0.toByte()
        }

        val totalLength: Int get() = 4 + 4 + dataLength + 4 // length + type + data + crc
    }

    private fun collectChunks(bytes: ByteArray): List<ChunkRange> {
        val result = mutableListOf<ChunkRange>()
        var offset = PNG_SIGNATURE.size
        while (offset + 8 <= bytes.size) {
            val length = readUInt32BE(bytes, offset)
            val type = readUInt32BE(bytes, offset + 4)
            val dataOffset = offset + 8
            val dataLength = length
            result.add(ChunkRange(type = type, dataOffset = dataOffset, dataLength = dataLength))
            val next = offset + 4 + 4 + dataLength + 4
            if (next <= offset) break
            offset = next
            if (type == IEND_TYPE) break
        }
        return result
    }

    private fun replaceChunk(bytes: ByteArray, old: ChunkRange, newChunk: ByteArray): ByteArray {
        val start = old.dataOffset - 8
        val end = start + old.totalLength
        val out = ByteArray(bytes.size - old.totalLength + newChunk.size)
        System.arraycopy(bytes, 0, out, 0, start)
        System.arraycopy(newChunk, 0, out, start, newChunk.size)
        System.arraycopy(bytes, end, out, start + newChunk.size, bytes.size - end)
        return out
    }

    private fun insertBeforeIend(
        bytes: ByteArray,
        chunks: List<ChunkRange>,
        newChunk: ByteArray
    ): ByteArray {
        val last = chunks.lastOrNull() ?: return bytes + newChunk
        val iendStart = last.dataOffset - 8
        val out = ByteArray(bytes.size + newChunk.size)
        System.arraycopy(bytes, 0, out, 0, iendStart)
        System.arraycopy(newChunk, 0, out, iendStart, newChunk.size)
        System.arraycopy(bytes, iendStart, out, iendStart + newChunk.size, bytes.size - iendStart)
        return out
    }

    /**
     * 构建一个 iTXt chunk 完整字节(4 字节 length + 4 字节 type + data + 4 字节 CRC)。
     * 压缩标志 = 0(不压缩),压缩方法 = 0,语言标签空,翻译后关键字空,文本按 UTF-8 写入。
     */
    private fun buildITxtChunk(keyword: String, text: String): ByteArray {
        require(keyword.isNotEmpty() && keyword.length <= 79) {
            "iTXt keyword must be 1..79 bytes"
        }
        require('\u0000' !in keyword) { "iTXt keyword must not contain NUL" }

        val out = ByteArrayOutputStream()
        out.write(keyword.toByteArray(Charsets.UTF_8))
        out.write(0) // NUL
        out.write(0) // compression flag
        out.write(0) // compression method
        out.write(0) // language tag (empty, NUL)
        out.write(0) // translated keyword (empty, NUL)
        out.write(text.toByteArray(Charsets.UTF_8))
        val dataBytes = out.toByteArray()

        val typeBytes = "iTXt".toByteArray(Charsets.US_ASCII)
        val length = dataBytes.size
        val crc = crc32(typeBytes + dataBytes)

        val result = ByteArray(4 + 4 + dataBytes.size + 4)
        result[0] = ((length ushr 24) and 0xFF).toByte()
        result[1] = ((length ushr 16) and 0xFF).toByte()
        result[2] = ((length ushr 8) and 0xFF).toByte()
        result[3] = (length and 0xFF).toByte()
        System.arraycopy(typeBytes, 0, result, 4, 4)
        System.arraycopy(dataBytes, 0, result, 8, dataBytes.size)
        val crcInt = crc.toInt()
        val crcStart = 8 + dataBytes.size
        result[crcStart] = ((crcInt ushr 24) and 0xFF).toByte()
        result[crcStart + 1] = ((crcInt ushr 16) and 0xFF).toByte()
        result[crcStart + 2] = ((crcInt ushr 8) and 0xFF).toByte()
        result[crcStart + 3] = (crcInt and 0xFF).toByte()
        return result
    }

    private fun decodeITxtText(bytes: ByteArray, chunk: ChunkRange): String? {
        var i = chunk.dataOffset
        val end = chunk.dataOffset + chunk.dataLength
        // 跳过 keyword\0
        while (i < end && bytes[i] != 0.toByte()) i++
        if (i >= end) return null
        i++ // 越过 keyword 结束 NUL
        if (i + 2 > end) return null
        val compressionFlag = bytes[i].toInt() and 0xFF
        val compressionMethod = bytes[i + 1].toInt() and 0xFF
        i += 2
        // 跳过 language tag\0
        while (i < end && bytes[i] != 0.toByte()) i++
        if (i >= end) return null
        i++
        // 跳过 translated keyword\0
        while (i < end && bytes[i] != 0.toByte()) i++
        if (i >= end) return null
        i++
        if (i >= end) return ""
        val textBytes = ByteArray(end - i)
        System.arraycopy(bytes, i, textBytes, 0, textBytes.size)
        if (compressionFlag == 0 && compressionMethod == 0) {
            return textBytes.toString(Charsets.UTF_8)
        }
        return null
    }

    private fun readUInt32BE(bytes: ByteArray, offset: Int): Int {
        return ((bytes[offset].toInt() and 0xFF) shl 24) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
            (bytes[offset + 3].toInt() and 0xFF)
    }

    private fun crc32(data: ByteArray): Long {
        val crc = CRC32()
        crc.update(data)
        return crc.value
    }
}
