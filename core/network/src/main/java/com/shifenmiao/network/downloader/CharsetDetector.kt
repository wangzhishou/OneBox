package com.shifenmiao.network.downloader

import org.mozilla.universalchardet.UniversalDetector
import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Utility class for detecting character encoding of HTML content
 */
object CharsetDetector {

    /**
     * Detect charset from byte array using multiple methods:
     * 1. Parse HTML meta tags
     * 2. Use Mozilla Universal Charset Detector
     * 3. Fall back to UTF-8
     *
     * @param bytes The byte array to detect charset from
     * @param contentType The Content-Type header from HTTP response (optional)
     * @return Detected Charset
     */
    fun detectCharset(bytes: ByteArray, contentType: String? = null): Charset {
        // Method 1: Try to get charset from Content-Type header
        contentType?.let { type ->
            extractCharsetFromContentType(type)?.let { charset ->
                return charset
            }
        }

        // Method 2: Try to detect from HTML meta tags
        detectFromHtmlMeta(bytes)?.let { charset ->
            return charset
        }

        // Method 3: Use Universal Charset Detector
        detectWithUniversalDetector(bytes)?.let { charset ->
            return charset
        }

        // Method 4: Fall back to UTF-8
        return StandardCharsets.UTF_8
    }

    /**
     * Extract charset from Content-Type header
     * Example: "text/html; charset=UTF-8"
     */
    private fun extractCharsetFromContentType(contentType: String): Charset? {
        return try {
            val charsetMatch = Regex("charset=([^;\\s]+)", RegexOption.IGNORE_CASE)
                .find(contentType)
            charsetMatch?.groupValues?.get(1)?.let { charsetName ->
                Charset.forName(charsetName)
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Detect charset from HTML meta tags
     * Looks for:
     * - <meta charset="UTF-8">
     * - <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
     */
    private fun detectFromHtmlMeta(bytes: ByteArray): Charset? {
        return try {
            // Read first 4KB which should contain the <head> section
            val previewSize = minOf(bytes.size, 4096)
            val preview = String(bytes, 0, previewSize, StandardCharsets.ISO_8859_1)

            // Pattern 1: <meta charset="xxx">
            val charsetPattern1 = Regex(
                """<meta\s+charset=["']?([^"'\s>]+)["']?""",
                RegexOption.IGNORE_CASE
            )
            charsetPattern1.find(preview)?.groupValues?.get(1)?.let { charsetName ->
                return tryCreateCharset(charsetName)
            }

            // Pattern 2: <meta http-equiv="Content-Type" content="text/html; charset=xxx">
            val charsetPattern2 = Regex(
                """<meta\s+http-equiv=["']?content-type["']?\s+content=["']?[^"']*charset=([^"'\s;>]+)""",
                RegexOption.IGNORE_CASE
            )
            charsetPattern2.find(preview)?.groupValues?.get(1)?.let { charsetName ->
                return tryCreateCharset(charsetName)
            }

            // Pattern 3: Alternative order
            val charsetPattern3 = Regex(
                """<meta\s+content=["']?[^"']*charset=([^"'\s;>]+)[^"']*["']?\s+http-equiv=["']?content-type["']?""",
                RegexOption.IGNORE_CASE
            )
            charsetPattern3.find(preview)?.groupValues?.get(1)?.let { charsetName ->
                return tryCreateCharset(charsetName)
            }

            null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Use Mozilla Universal Charset Detector
     */
    private fun detectWithUniversalDetector(bytes: ByteArray): Charset? {
        return try {
            val detector = UniversalDetector(null)

            // Feed data to detector
            val chunkSize = 8192
            ByteArrayInputStream(bytes).use { input ->
                val buffer = ByteArray(chunkSize)
                var bytesRead: Int

                while (input.read(buffer).also { bytesRead = it } != -1 && !detector.isDone) {
                    detector.handleData(buffer, 0, bytesRead)
                }
            }

            detector.dataEnd()
            val detectedCharset = detector.detectedCharset
            detector.reset()

            detectedCharset?.let { charsetName ->
                tryCreateCharset(charsetName)
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Try to create Charset object from charset name
     * Handles common charset name variations
     */
    private fun tryCreateCharset(charsetName: String): Charset? {
        return try {
            // Normalize charset name
            val normalized = when (charsetName.uppercase().replace("-", "").replace("_", "")) {
                "GB2312", "GBK", "GB18030" -> "GBK"
                "UTF8" -> "UTF-8"
                "ISO88591" -> "ISO-8859-1"
                else -> charsetName
            }

            Charset.forName(normalized)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Detect charset from bytes and return the charset name
     */
    fun detectCharsetName(bytes: ByteArray, contentType: String? = null): String {
        return detectCharset(bytes, contentType).name()
    }
}

