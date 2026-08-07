package com.shifenmiao.network.downloader

/**
 * Result wrapper for HTML download operations
 */
sealed class HtmlDownloadResult {
    /**
     * Successful download
     * @param htmlContent The downloaded HTML content
     * @param url The actual URL (may differ from requested if redirected)
     * @param contentType The content type from response headers
     * @param contentLength The content length in bytes
     */
    data class Success(
        val htmlContent: String,
        val url: String,
        val contentType: String?,
        val contentLength: Long
    ) : HtmlDownloadResult()

    /**
     * Download failed
     * @param error The exception that caused the failure
     * @param message User-friendly error message
     * @param url The URL that was attempted
     */
    data class Failure(
        val error: Throwable,
        val message: String,
        val url: String
    ) : HtmlDownloadResult()
}

