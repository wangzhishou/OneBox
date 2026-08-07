package com.shifenmiao.network.downloader

/**
 * Interface for downloading HTML content from URLs
 */
interface HtmlDownloader {
    /**
     * Download HTML content from the specified URL
     *
     * @param url The URL to download from
     * @param config Optional configuration for the download operation
     * @return Result containing either the HTML content or an error
     */
    suspend fun downloadHtml(
        url: String,
        config: HtmlDownloadConfig = HtmlDownloadConfig.DEFAULT
    ): HtmlDownloadResult

    /**
     * Cancel any ongoing downloads
     */
    fun cancelDownload()
}

