package com.shifenmiao.common.file

import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.Keep
import com.shifenmiao.interfaces.singleton.AppContext
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.t8rin.imagetoolbox.core.domain.image.model.MetadataTag
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import java.nio.charset.StandardCharsets

@Keep
object AigcFileMetadataReader {

    suspend fun readFromPngImageDescription(
        imageUri: String,
        imageGetter: ImageGetter<Bitmap>
    ): AigcMetadata? {
        // 1) 优先按 GB 45438-2025 6.2 节读取 XMP 元数据:
        //    - PNG: iTXt(XML:com.adobe.xmp) chunk;
        //    - JPEG: APP1 segment,以 "XMP\0XMP\0" 标识。
        runCatching {
            val context = AppContext.getContext()
            val bytes = context.contentResolver.openInputStream(Uri.parse(imageUri))?.use {
                it.readBytes()
            }
            if (bytes != null) {
                val extracted = PngAigcInjector.extract(bytes)
                    ?: JpegAigcInjector.extract(bytes)
                if (!extracted.isNullOrBlank()) return AigcMetadata(extracted)
            }
        }
        // 2) 兼容旧的 EXIF ImageDescription 字段(历史导出文件)
        return imageGetter.getImage(imageUri)?.metadata?.getAttribute(MetadataTag.ImageDescription)?.let {
            AigcMetadata(it)
        }
    }

    /**
     * 从 HTML 中读取 AIGC 隐式标识。
     *
     * 按 GB 45438-2025 标准,优先读取 `<head>` 中:
     * ```
     * <meta name="AIGC" content="{...7要素 JSON...}">
     * ```
     * 同时向下兼容:
     * - `<!-- AIGC: ... -->` 注释(由 `DuelHtmlExporter` 注入,内容为 HTML 转义后的 JSON,需要反转义);
     * - `<!-- AIGC_INFO: ... -->` 旧版注释(早期 `WebViewComponent` 注入格式)。
     */
    suspend fun readFromHtml(
        htmlUri: String,
        fileController: FileController
    ): AigcMetadata? {
        return try {
            val text = runCatching { fileController.readBytes(htmlUri).toString(Charsets.UTF_8) }
                .getOrNull() ?: return null

            val metaRaw = META_AIGC.find(text)?.groupValues?.getOrNull(1)?.let(::unescapeHtml)
            if (!metaRaw.isNullOrBlank()) return AigcMetadata(metaRaw)

            val commentRaw = AIGC_COMMENT.find(text)?.groupValues?.getOrNull(1)?.let(::unescapeHtml)
            if (!commentRaw.isNullOrBlank()) return AigcMetadata(commentRaw)

            val legacyRaw = AIGC_INFO_COMMENT.find(text)?.groupValues?.getOrNull(1)?.let(::unescapeHtml)
            if (!legacyRaw.isNullOrBlank()) return AigcMetadata(legacyRaw)

            null
        } catch (_: Exception) {
            null
        }
    }

    private val META_AIGC = Regex(
        "(?is)<meta\\s+[^>]*name\\s*=\\s*[\"']AIGC[\"'][^>]*content\\s*=\\s*[\"']([^\"']*)[\"'][^>]*>",
    )

    private val AIGC_COMMENT = Regex(
        "<!--\\s*AIGC\\s*:\\s*(.*?)-->",
        RegexOption.DOT_MATCHES_ALL
    )

    private val AIGC_INFO_COMMENT = Regex(
        "<!--\\s*AIGC_INFO\\s*:\\s*(.*?)-->",
        RegexOption.DOT_MATCHES_ALL
    )

    private fun unescapeHtml(input: String): String {
        return input
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
    }

    suspend fun readFromPdf(
        pdfUri: String,
        fileController: FileController
    ): AigcMetadata? {
        return try {
            val bytes = fileController.readBytes(pdfUri)
            // 1) 优先按 GB 45438-2025 6.3 节读取:
            //      /AIGC({"Label":"value1",...})
            //    值是 PDF 字符串字面量 (...),含 PDF 转义。
            val raw = readPdfLiteralString(bytes, key = "/AIGC ")
                // 2) 回退到尾部 hex 注释(向下兼容历史导出文件,以及读不到增量更新对象的情况)
                ?: readTrailingUtf8Hex(bytes, marker = "%AIGCUTF8HEX ")
                ?: readTrailingUtf8Hex(bytes, marker = "%AIGCInfoUTF8HEX ")
            if (raw.isNullOrBlank()) null else AigcMetadata(raw)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 在 PDF 字节流中查找 `key` 后面的字符串字面量 `(...)`,并按 PDF 1.7 7.3.4.2
     * 转义规则解码。返回 null 表示未找到。
     */
    private fun readPdfLiteralString(bytes: ByteArray, key: String): String? {
        val keyIndex = bytes.lastIndexOfAscii(key)
        if (keyIndex < 0) return null
        var i = keyIndex + key.length
        // 跳过 key 与值之间的空白
        while (i < bytes.size && isPdfWhitespace(bytes[i])) i++
        if (i >= bytes.size || bytes[i] != '('.code.toByte()) return null
        i++ // 越过 (
        val sb = StringBuilder()
        var depth = 1
        while (i < bytes.size && depth > 0) {
            val b = bytes[i]
            when {
                b == '\\'.code.toByte() && i + 1 < bytes.size -> {
                    when (bytes[i + 1]) {
                        '('.code.toByte() -> { sb.append('('); i += 2 }
                        ')'.code.toByte() -> { sb.append(')'); i += 2 }
                        '\\'.code.toByte() -> { sb.append('\\'); i += 2 }
                        'n'.code.toByte() -> { sb.append('\n'); i += 2 }
                        'r'.code.toByte() -> { sb.append('\r'); i += 2 }
                        't'.code.toByte() -> { sb.append('\t'); i += 2 }
                        'b'.code.toByte() -> { sb.append('\b'); i += 2 }
                        'f'.code.toByte() -> { sb.append('\u000C'); i += 2 }
                        else -> { sb.append((bytes[i + 1].toInt() and 0xFF).toChar()); i += 2 }
                    }
                }
                b == '('.code.toByte() -> { depth++; sb.append('('); i++ }
                b == ')'.code.toByte() -> { depth--; if (depth > 0) sb.append(')'); i++ }
                else -> { sb.append((b.toInt() and 0xFF).toChar()); i++ }
            }
        }
        return if (depth == 0) sb.toString() else null
    }

    private fun isPdfWhitespace(b: Byte): Boolean {
        return b == ' '.code.toByte() ||
            b == '\t'.code.toByte() ||
            b == '\r'.code.toByte() ||
            b == '\n'.code.toByte() ||
            b == 0x0C.toByte() // \f
    }

    private fun readTrailingUtf8Hex(bytes: ByteArray, marker: String): String? {
        val markerIndex = bytes.lastIndexOfAscii(marker)
        if (markerIndex < 0) return null
        val hexStart = markerIndex + marker.length
        var hexEnd = hexStart
        while (hexEnd < bytes.size &&
            bytes[hexEnd] != '\n'.code.toByte() &&
            bytes[hexEnd] != '\r'.code.toByte()
        ) {
            hexEnd++
        }
        val hexString = bytes.copyOfRange(hexStart, hexEnd).toString(StandardCharsets.US_ASCII)
        return String(hexStringToByteArray(hexString), StandardCharsets.UTF_8)
    }

    private fun ByteArray.lastIndexOfAscii(
        ascii: String,
        endExclusive: Int = size
    ): Int {
        val needle = ascii.toByteArray(StandardCharsets.US_ASCII)
        if (needle.isEmpty()) return endExclusive.coerceAtMost(size)
        val lastStart = (endExclusive - needle.size).coerceAtMost(size - needle.size)
        for (i in lastStart downTo 0) {
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

    private fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}