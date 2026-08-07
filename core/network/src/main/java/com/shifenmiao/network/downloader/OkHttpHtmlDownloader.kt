package com.shifenmiao.network.downloader

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp-based implementation of HtmlDownloader
 */
@Singleton
class OkHttpHtmlDownloader @Inject constructor() : HtmlDownloader {

    private var currentClient: OkHttpClient? = null

    override suspend fun downloadHtml(
        url: String,
        config: HtmlDownloadConfig
    ): HtmlDownloadResult = withContext(Dispatchers.IO) {
        try {
            // Validate URL
            val validatedUrl = validateAndNormalizeUrl(url)

            // Create OkHttpClient with configuration
            val client = createClient(config)
            currentClient = client

            // Build request
            val request = buildRequest(validatedUrl, config)

            // Execute request
            val response = client.newCall(request).execute()

            // Process response
            processResponse(response, validatedUrl, config)

        } catch (e: MalformedURLException) {
            HtmlDownloadResult.Failure(
                error = e,
                message = "Invalid URL format: ${e.message}",
                url = url
            )
        } catch (e: IOException) {
            HtmlDownloadResult.Failure(
                error = e,
                message = "Network error: ${e.message}",
                url = url
            )
        } catch (e: Exception) {
            HtmlDownloadResult.Failure(
                error = e,
                message = "Download failed: ${e.message}",
                url = url
            )
        } finally {
            currentClient = null
        }
    }

    override fun cancelDownload() {
        currentClient?.dispatcher?.cancelAll()
        currentClient = null
    }

    /**
     * Validate and normalize the URL
     */
    private fun validateAndNormalizeUrl(url: String): String {
        if (url.isBlank()) {
            throw MalformedURLException("URL cannot be empty")
        }

        // Add scheme if missing
        val normalizedUrl = when {
            url.startsWith("http://") || url.startsWith("https://") -> url
            else -> "https://$url"
        }

        // Validate URL structure
        URL(normalizedUrl)

        return normalizedUrl
    }

    /**
     * Create OkHttpClient with specified configuration
     */
    private fun createClient(config: HtmlDownloadConfig): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(config.connectTimeout, config.timeUnit)
            .readTimeout(config.readTimeout, config.timeUnit)
            .writeTimeout(config.writeTimeout, config.timeUnit)
            .followRedirects(config.followRedirects)
            .followSslRedirects(config.followSslRedirects)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Build HTTP request with headers
     */
    private fun buildRequest(url: String, config: HtmlDownloadConfig): Request {
        val requestBuilder = Request.Builder()
            .url(url)
            .header("User-Agent", config.userAgent)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Accept-Language", "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7")
            // REMOVED: .header("Accept-Encoding", "gzip, deflate")
            // Removing this allows OkHttp to handle GZIP decompression transparently
            .header("Connection", "keep-alive")

        // Add custom headers
        config.headers.forEach { (key, value) ->
            requestBuilder.header(key, value)
        }

        return requestBuilder.build()
    }

    /**
     * Process the HTTP response
     */
    private fun processResponse(
        response: Response,
        originalUrl: String,
        config: HtmlDownloadConfig
    ): HtmlDownloadResult {
        if (!response.isSuccessful) {
            return HtmlDownloadResult.Failure(
                error = IOException("HTTP ${response.code}: ${response.message}"),
                message = "Server returned error: ${response.code} ${response.message}",
                url = originalUrl
            )
        }

        val responseBody = response.body

        // Check content length limit
        val contentLength = responseBody.contentLength()
        if (config.maxContentLength != null && contentLength > config.maxContentLength) {
            return HtmlDownloadResult.Failure(
                error = IOException("Content too large: $contentLength bytes"),
                message = "Content exceeds maximum size (${config.maxContentLength} bytes)",
                url = originalUrl
            )
        }

        // Read bytes first for charset detection
        val bytes = responseBody.bytes()

        // Determine charset
        val charset = if (config.charset != null) {
            // Use configured charset if specified
            Charset.forName(config.charset)
        } else if (config.autoDetectCharset) {
            // Auto-detect charset using CharsetDetector
            val contentType = responseBody.contentType()?.toString()
            CharsetDetector.detectCharset(bytes, contentType)
        } else {
            // Use UTF-8 as fallback if auto-detection is disabled
            Charsets.UTF_8
        }

        // Decode content with detected charset
        val htmlContent = String(bytes, charset)

        return HtmlDownloadResult.Success(
            htmlContent = htmlContent,
            url = response.request.url.toString(), // May differ if redirected
            contentType = responseBody.contentType()?.toString(),
            contentLength = contentLength.takeIf { it >= 0 } ?: bytes.size.toLong()
        )
    }
}
