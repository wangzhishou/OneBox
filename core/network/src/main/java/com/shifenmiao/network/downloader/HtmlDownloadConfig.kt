package com.shifenmiao.network.downloader

import java.util.concurrent.TimeUnit

/**
 * Configuration for HTML download operations
 *
 * @param connectTimeout Connection timeout duration
 * @param readTimeout Read timeout duration
 * @param writeTimeout Write timeout duration
 * @param timeUnit Time unit for timeout values
 * @param followRedirects Whether to follow HTTP redirects
 * @param followSslRedirects Whether to follow HTTPS redirects
 * @param maxRedirects Maximum number of redirects to follow
 * @param userAgent User agent string for the request
 * @param headers Additional headers to include in the request
 * @param charset Character encoding for response (null = auto-detect)
 *                Auto-detection uses:
 *                1. Content-Type header from HTTP response
 *                2. HTML meta tags (e.g., <meta charset="GBK">)
 *                3. Mozilla Universal Charset Detector
 *                4. Falls back to UTF-8
 * @param autoDetectCharset Whether to auto-detect charset (true by default)
 * @param maxContentLength Maximum content length in bytes (null = no limit)
 */
data class HtmlDownloadConfig(
    val connectTimeout: Long = 30,
    val readTimeout: Long = 30,
    val writeTimeout: Long = 30,
    val timeUnit: TimeUnit = TimeUnit.SECONDS,
    val followRedirects: Boolean = true,
    val followSslRedirects: Boolean = true,
    val maxRedirects: Int = 5,
    val userAgent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
    val headers: Map<String, String> = emptyMap(),
    val charset: String? = null,
    val autoDetectCharset: Boolean = true,
    val maxContentLength: Long? = 10 * 1024 * 1024 // 10MB default
) {
    companion object {
        /**
         * Default configuration with standard settings and auto charset detection
         */
        val DEFAULT = HtmlDownloadConfig()

        /**
         * Fast configuration with shorter timeouts
         */
        val FAST = HtmlDownloadConfig(
            connectTimeout = 10,
            readTimeout = 10,
            writeTimeout = 10
        )

        /**
         * Slow configuration for unreliable connections
         */
        val SLOW = HtmlDownloadConfig(
            connectTimeout = 60,
            readTimeout = 60,
            writeTimeout = 60
        )

        /**
         * Configuration optimized for Chinese websites (like Baidu)
         * Uses GBK charset as default if auto-detection is disabled
         */
        val CHINESE = HtmlDownloadConfig(
            charset = "GBK",
            autoDetectCharset = true
        )
    }
}

