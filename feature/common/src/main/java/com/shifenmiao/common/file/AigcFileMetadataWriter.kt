package com.shifenmiao.common.file

import android.graphics.Bitmap
import androidx.annotation.Keep
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.t8rin.imagetoolbox.core.domain.image.model.MetadataTag
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Keep
data class AigcMetadata(
    val raw: String
)

@Keep
object AigcFileMetadataWriter {

    suspend fun writeImageDescriptionIfPresent(
        imageUri: String,
        aigc: AigcMetadata?,
        imageGetter: ImageGetter<Bitmap>,
        fileController: FileController
    ) {
        val raw = aigc?.raw?.takeIf { it.isNotBlank() } ?: return

        // 1) PNG / JPEG: 按 GB 45438-2025 6.2 节写入 XMP 元数据。
        //    - PNG: iTXt chunk,keyword = "XML:com.adobe.xmp";
        //    - JPEG: APP1 segment,以 "XMP\0XMP\0" 标识。
        val xmpInjected = runCatching {
            val original = fileController.readBytes(imageUri)
            when {
                PngAigcInjector.isPng(original) -> {
                    val injected = PngAigcInjector.injectOrReplace(original, raw)
                    if (injected === original) false
                    else {
                        fileController.writeBytes(uri = imageUri) { it.writeBytes(injected) }
                        true
                    }
                }
                JpegAigcInjector.isJpeg(original) -> {
                    val injected = JpegAigcInjector.injectOrReplace(original, raw)
                    if (injected === original) false
                    else {
                        fileController.writeBytes(uri = imageUri) { it.writeBytes(injected) }
                        true
                    }
                }
                else -> false
            }
        }.getOrDefault(false)

        if (xmpInjected) return

        // 2) 其它格式(WebP / 不识别 / 解析失败): 写 EXIF ImageDescription 作为冗余,
        //    旧读取器可继续工作。
        runCatching {
            val image = imageGetter.getImage(imageUri) ?: return
            val metadata = image.metadata ?: return
            metadata.setAttribute(MetadataTag.ImageDescription, raw)
            fileController.writeMetadata(
                imageUri = imageUri,
                metadata = metadata
            )
        }
    }

    suspend fun transferPdfWithAigcInfo(
        fromPdfUri: String,
        toPdfUri: String,
        aigc: AigcMetadata?,
        fileController: FileController
    ): SaveResult {
        val raw = aigc?.raw?.takeIf { it.isNotBlank() }
            ?: return fileController.transferBytes(fromUri = fromPdfUri, toUri = toPdfUri)

        // 这里不再做静默降级，而是直接抛出异常，由外层调用方（WebViewComponent）决定是否捕获并降级。
        // 这样如果发生错误，外层可以通过 runCatching 捕获到异常信息并在 UI 上反馈。
        val originalBytes = fileController.readBytes(fromPdfUri)
        val updatedBytes = PdfAigcInfoInjector.injectOrAppendComment(
            pdfBytes = originalBytes,
            aigcInfo = raw
        )
        return fileController.writeBytes(uri = toPdfUri) { it.writeBytes(updatedBytes) }
    }
}

@Keep
internal object PdfAigcInfoInjector {

    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneOffset.UTC)

    fun injectOrAppendComment(
        pdfBytes: ByteArray,
        aigcInfo: String
    ): ByteArray {
        val injected = runCatching { injectAsIncrementalInfoUpdate(pdfBytes, aigcInfo) }.getOrNull()
        // 无论增量更新是否成功，都追加尾部注释，以保证自定义读取器能总是读到数据
        return appendTrailingComment(injected ?: pdfBytes, aigcInfo)
    }

    private fun injectAsIncrementalInfoUpdate(
        pdfBytes: ByteArray,
        aigcInfo: String
    ): ByteArray {
        val startXrefIndex = pdfBytes.lastIndexOfAscii("startxref")
        require(startXrefIndex >= 0) { "startxref not found" }

        val prevStartXref = pdfBytes.parseLongAfterAsciiKeyword(
            keywordIndex = startXrefIndex,
            keyword = "startxref"
        )

        val trailerIndex = pdfBytes.lastIndexOfAscii("trailer", endExclusive = startXrefIndex)
        require(trailerIndex >= 0) { "trailer not found" }

        val trailerDict = pdfBytes.extractTrailerDictionary(
            trailerIndex = trailerIndex,
            endExclusive = startXrefIndex
        )

        val size = trailerDict.findFirstLong(Regex("/Size\\s+(\\d+)"))
        val rootRef = trailerDict.findFirstString(Regex("/Root\\s+(\\d+\\s+\\d+\\s+R)"))
        val idValue = trailerDict.findFirstString(Regex("/ID\\s*\\[(.+?)]"))
        val encryptRef = trailerDict.findFirstString(Regex("/Encrypt\\s+(\\d+\\s+\\d+\\s+R)"))

        val newObjectNumber = size.toInt()
        val newSize = size + 1

        val objectOffset = pdfBytes.size.toLong()
        val modDate = "D:${dateFormatter.format(Instant.now())}+00'00'"
        // 按 GB 45438-2025 6.3 节:PDF 写入 Document Information Dictionary,
        // 值为 PDF 字符串字面量 (...),例如 /AIGC({"Label":"value1",...})
        val pdfLiteralString = toPdfLiteralString(aigcInfo)

        val infoObjectBytes = buildString {
            append('\n')
            append(newObjectNumber)
            append(" 0 obj\n")
            append("<< /AIGC ")
            append(pdfLiteralString)
            append(" /ModDate (")
            append(modDate)
            append(") >>\n")
            append("endobj\n")
        }.toByteArray(StandardCharsets.US_ASCII)

        val xrefStart = objectOffset + infoObjectBytes.size
        val offsetLine = objectOffset.toString().padStart(10, '0')

        val xrefBytes = buildString {
            append("xref\n")
            append(newObjectNumber)
            append(" 1\n")
            append(offsetLine)
            append(" 00000 n \n")
        }.toByteArray(StandardCharsets.US_ASCII)

        val trailerBytes = buildString {
            append("trailer\n<< /Size ")
            append(newSize)
            append(" /Root ")
            append(rootRef)
            append(" /Info ")
            append(newObjectNumber)
            append(" 0 R")
            if (!encryptRef.isNullOrBlank()) {
                append(" /Encrypt ")
                append(encryptRef)
            }
            if (!idValue.isNullOrBlank()) {
                append(" /ID [")
                append(idValue)
                append(']')
            }
            append(" /Prev ")
            append(prevStartXref)
            append(" >>\nstartxref\n")
            append(xrefStart)
            append("\n%%EOF\n")
        }.toByteArray(StandardCharsets.US_ASCII)

        return pdfBytes + infoObjectBytes + xrefBytes + trailerBytes
    }

    private fun appendTrailingComment(
        pdfBytes: ByteArray,
        aigcInfo: String
    ): ByteArray {
        val infoHexUtf8 = aigcInfo.toByteArray(StandardCharsets.UTF_8).toHexString()
        val suffix = "\n%AIGCUTF8HEX $infoHexUtf8\n".toByteArray(StandardCharsets.US_ASCII)
        return pdfBytes + suffix
    }

    private fun toPdfLiteralString(value: String): String {
        // PDF 字符串字面量规则 (PDF 1.7 7.3.4.2):
        // - 用 ( 和 ) 包裹;
        // - 字面量内的反斜杠 \ 必须写成 \\;
        // - 字面量内的左括号 ( 和右括号 ) 必须写成 \( 和 \);
        // - 换行 / 回车 / Tab / 退格 / 换页 / 左 / 右括号 对应转义为 \n \r \t \b \f \( \);
        // - JSON 字符串里的 " 不需要转义(JSON 引号不影响 PDF 字面量)。
        val escaped = buildString(value.length) {
            for (c in value) {
                when (c) {
                    '\\' -> append("\\\\")
                    '(' -> append("\\(")
                    ')' -> append("\\)")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    '\b' -> append("\\b")
                    '\u000C' -> append("\\f")
                    else -> append(c)
                }
            }
        }
        return "($escaped)"
    }

    private fun ByteArray.extractTrailerDictionary(
        trailerIndex: Int,
        endExclusive: Int
    ): String {
        val dictStart = indexOfAscii("<<", startIndex = trailerIndex, endExclusive = endExclusive)
        require(dictStart >= 0) { "Trailer dictionary start not found" }
        val dictEnd = lastIndexOfAscii(">>", endExclusive = endExclusive)
        require(dictEnd >= 0 && dictEnd > dictStart) { "Trailer dictionary end not found" }
        val bytes = copyOfRange(dictStart, dictEnd + 2)
        return bytes.toString(StandardCharsets.US_ASCII)
    }

    private fun String.findFirstLong(regex: Regex): Long {
        val value = regex.find(this)?.groupValues?.getOrNull(1)
        require(!value.isNullOrBlank()) { "Value not found for ${regex.pattern}" }
        return value.toLong()
    }

    private fun String.findFirstString(regex: Regex): String? =
        regex.find(this)?.groupValues?.getOrNull(1)

    private fun ByteArray.parseLongAfterAsciiKeyword(
        keywordIndex: Int,
        keyword: String
    ): Long {
        var i = keywordIndex + keyword.length
        while (i < size && (this[i].toInt().toChar().isWhitespace())) i++
        while (i < size && (this[i] == '\r'.code.toByte() || this[i] == '\n'.code.toByte())) i++
        while (i < size && (this[i].toInt().toChar().isWhitespace())) i++
        val start = i
        while (i < size && this[i] in '0'.code.toByte()..'9'.code.toByte()) i++
        require(i > start) { "No number after $keyword" }
        return copyOfRange(start, i).toString(StandardCharsets.US_ASCII).toLong()
    }

    private fun ByteArray.indexOfAscii(
        ascii: String,
        startIndex: Int = 0,
        endExclusive: Int = size
    ): Int {
        val needle = ascii.toByteArray(StandardCharsets.US_ASCII)
        if (needle.isEmpty()) return startIndex.coerceAtMost(endExclusive)
        val lastStart = (endExclusive - needle.size).coerceAtLeast(startIndex)
        for (i in startIndex..lastStart) {
            var matched = true
            for (j in needle.indices) {
                if (this[i + j] != needle[j]) {
                    matched = false
                    break
                }
            }
            if (matched) return i
        }
        return -1
    }

    private fun ByteArray.lastIndexOfAscii(
        ascii: String,
        endExclusive: Int = size,
        startInclusive: Int = 0
    ): Int {
        val needle = ascii.toByteArray(StandardCharsets.US_ASCII)
        if (needle.isEmpty()) return endExclusive.coerceAtMost(size)
        val lastStart = (endExclusive - needle.size).coerceAtMost(size - needle.size)
        for (i in lastStart downTo startInclusive) {
            var matched = true
            for (j in needle.indices) {
                if (this[i + j] != needle[j]) {
                    matched = false
                    break
                }
            }
            if (matched) return i
        }
        return -1
    }

    private fun ByteArray.toHexString(): String {
        val out = CharArray(size * 2)
        var k = 0
        for (b in this) {
            val i = b.toInt() and 0xFF
            val hi = i ushr 4
            val lo = i and 0x0F
            out[k++] = "0123456789ABCDEF"[hi]
            out[k++] = "0123456789ABCDEF"[lo]
        }
        return String(out)
    }
}
