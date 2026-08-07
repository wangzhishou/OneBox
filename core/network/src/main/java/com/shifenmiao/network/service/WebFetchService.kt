package com.shifenmiao.network.service

import com.shifenmiao.network.downloader.HtmlDownloadConfig
import com.shifenmiao.network.downloader.HtmlDownloadResult
import com.shifenmiao.network.downloader.OkHttpHtmlDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 网页抓取服务
 *
 * 提供网页内容抓取和解析功能，支持多种提取模式：
 * - text: 提取纯文本
 * - html: 返回原始HTML
 * - links: 提取所有链接
 * - images: 提取所有图片URL
 * - all: 返回完整信息
 */
@Singleton
class WebFetchService @Inject constructor(
    private val htmlDownloader: OkHttpHtmlDownloader
) {

    companion object {
        private const val DEFAULT_MAX_LENGTH = 5000
        private const val MAX_REDIRECTS = 5
    }

    /**
     * 抓取模式
     */
    enum class ExtractMode {
        TEXT,    // 纯文本
        HTML,    // 原始HTML
        LINKS,   // 所有链接
        IMAGES,  // 所有图片
        ALL      // 完整信息
    }

    /**
     * 网页抓取结果
     */
    data class FetchResult(
        val url: String,
        val title: String?,
        val content: String,
        val links: List<LinkInfo> = emptyList(),
        val images: List<ImageInfo> = emptyList(),
        val contentType: String?,
        val contentLength: Long,
        val extractMode: ExtractMode
    )

    data class LinkInfo(
        val url: String,
        val text: String,
        val rel: String?
    )

    data class ImageInfo(
        val url: String,
        val alt: String?,
        val width: Int?,
        val height: Int?
    )

    /**
     * 抓取网页内容
     *
     * @param url 要抓取的URL
     * @param extractMode 提取模式
     * @param maxLength 返回内容最大长度
     * @param config 下载配置
     * @return 抓取结果
     */
    suspend fun fetch(
        url: String,
        extractMode: ExtractMode = ExtractMode.TEXT,
        maxLength: Int = DEFAULT_MAX_LENGTH,
        config: HtmlDownloadConfig = HtmlDownloadConfig.DEFAULT
    ): Result<FetchResult> = withContext(Dispatchers.IO) {
        try {
            // 下载网页
            when (val downloadResult = htmlDownloader.downloadHtml(url, config)) {
                is HtmlDownloadResult.Success -> {
                    val doc = Jsoup.parse(downloadResult.htmlContent, downloadResult.url)

                    val result = when (extractMode) {
                        ExtractMode.TEXT -> {
                            val text = doc.body().text()
                            FetchResult(
                                url = downloadResult.url,
                                title = doc.title(),
                                content = truncateText(text, maxLength),
                                contentType = downloadResult.contentType,
                                contentLength = downloadResult.contentLength,
                                extractMode = extractMode
                            )
                        }

                        ExtractMode.HTML -> {
                            FetchResult(
                                url = downloadResult.url,
                                title = doc.title(),
                                content = truncateText(downloadResult.htmlContent, maxLength),
                                contentType = downloadResult.contentType,
                                contentLength = downloadResult.contentLength,
                                extractMode = extractMode
                            )
                        }

                        ExtractMode.LINKS -> {
                            val links = extractLinks(doc)
                            val linksText = links.joinToString("\n") { "${it.text}: ${it.url}" }
                            FetchResult(
                                url = downloadResult.url,
                                title = doc.title(),
                                content = truncateText(linksText, maxLength),
                                links = links,
                                contentType = downloadResult.contentType,
                                contentLength = downloadResult.contentLength,
                                extractMode = extractMode
                            )
                        }

                        ExtractMode.IMAGES -> {
                            val images = extractImages(doc)
                            val imagesText = images.joinToString("\n") { 
                                "${it.alt ?: "图片"}: ${it.url}" 
                            }
                            FetchResult(
                                url = downloadResult.url,
                                title = doc.title(),
                                content = truncateText(imagesText, maxLength),
                                images = images,
                                contentType = downloadResult.contentType,
                                contentLength = downloadResult.contentLength,
                                extractMode = extractMode
                            )
                        }

                        ExtractMode.ALL -> {
                            val links = extractLinks(doc)
                            val images = extractImages(doc)
                            val text = doc.body().text()
                            val fullContent = buildString {
                                appendLine("=== 页面标题 ===")
                                appendLine(doc.title())
                                appendLine()
                                appendLine("=== 页面内容 ===")
                                appendLine(truncateText(text, maxLength / 2))
                                appendLine()
                                appendLine("=== 链接 (${links.size}个) ===")
                                links.take(20).forEach { appendLine("  ${it.text}: ${it.url}") }
                                appendLine()
                                appendLine("=== 图片 (${images.size}个) ===")
                                images.take(20).forEach { appendLine("  ${it.alt ?: "图片"}: ${it.url}") }
                            }
                            FetchResult(
                                url = downloadResult.url,
                                title = doc.title(),
                                content = truncateText(fullContent, maxLength),
                                links = links,
                                images = images,
                                contentType = downloadResult.contentType,
                                contentLength = downloadResult.contentLength,
                                extractMode = extractMode
                            )
                        }
                    }

                    Result.success(result)
                }

                is HtmlDownloadResult.Failure -> {
                    Result.failure(Exception(downloadResult.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 提取页面链接
     */
    private fun extractLinks(doc: Document): List<LinkInfo> {
        return doc.select("a[href]").mapNotNull { element ->
            val href = element.absUrl("href")
            if (href.isNotBlank()) {
                LinkInfo(
                    url = href,
                    text = element.text().trim(),
                    rel = element.attr("rel").ifBlank { null }
                )
            } else null
        }.distinctBy { it.url }
    }

    /**
     * 提取页面图片
     */
    private fun extractImages(doc: Document): List<ImageInfo> {
        return doc.select("img[src]").mapNotNull { element ->
            val src = element.absUrl("src")
            if (src.isNotBlank()) {
                ImageInfo(
                    url = src,
                    alt = element.attr("alt").ifBlank { null },
                    width = element.attr("width").toIntOrNull(),
                    height = element.attr("height").toIntOrNull()
                )
            } else null
        }.distinctBy { it.url }
    }

    /**
     * 截断文本
     */
    private fun truncateText(text: String, maxLength: Int): String {
        return if (text.length > maxLength) {
            text.take(maxLength) + "\n...(内容已截断，共${text.length}字符)"
        } else {
            text
        }
    }
}
