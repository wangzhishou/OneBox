package com.shifenmiao.network.service

import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.network.downloader.HtmlDownloadConfig
import com.shifenmiao.network.downloader.HtmlDownloadResult
import com.shifenmiao.network.downloader.OkHttpHtmlDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 网络搜索服务
 *
 * 支持多个搜索引擎：百度、Google、Bing、DuckDuckGo、Keenable
 * 返回结构化的搜索结果
 */
@Singleton
class WebSearchService @Inject constructor(
    private val htmlDownloader: OkHttpHtmlDownloader
) {

    companion object {
        private const val DEFAULT_NUM_RESULTS = 5
        private const val MAX_NUM_RESULTS = 20

        /** Keenable 免 Key 公共端点要求的应用标识 */
        private const val KEENABLE_APP_TITLE = "OneBox"

        /** Keenable 的 snippet 是 2-4k 字的页面正文,截断到与其它引擎摘要相近的长度 */
        private const val KEENABLE_SNIPPET_MAX_LENGTH = 500

        /** 与 HTML 引擎的 15 秒连接/读取超时保持一致 */
        private const val KEENABLE_TIMEOUT_SECONDS = 15L
    }

    // Keenable 走 JSON API(POST),不经过 HTML 下载器
    private val keenableClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(KEENABLE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(KEENABLE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    /**
     * 搜索引擎类型
     */
    enum class SearchEngine(
        val displayName: String,
        val searchUrlTemplate: String,
        val charset: String? = null
    ) {
        BAIDU(
            displayName = "百度",
            searchUrlTemplate = "https://www.baidu.com/s?wd=%s&rn=%d",
            charset = "UTF-8"
        ),
        GOOGLE(
            displayName = "Google",
            searchUrlTemplate = "https://www.google.com/search?q=%s&num=%d"
        ),
        BING(
            displayName = "Bing",
            searchUrlTemplate = "https://www.bing.com/search?q=%s&count=%d"
        ),
        DUCKDUCKGO(
            displayName = "DuckDuckGo",
            searchUrlTemplate = "https://duckduckgo.com/html/?q=%s"
        ),
        KEENABLE(
            displayName = "Keenable",
            searchUrlTemplate = "https://api.keenable.ai/v1/search/public"
        );

        fun buildSearchUrl(query: String, numResults: Int = DEFAULT_NUM_RESULTS): String {
            return String.format(searchUrlTemplate, 
                java.net.URLEncoder.encode(query, "UTF-8"),
                numResults
            )
        }
    }

    /**
     * 搜索结果
     */
    data class SearchResult(
        val query: String,
        val engine: SearchEngine,
        val results: List<SearchResultItem>,
        val totalResults: Int
    )

    data class SearchResultItem(
        val title: String,
        val url: String,
        val snippet: String,
        val position: Int
    )

    /**
     * 执行搜索
     *
     * @param query 搜索关键词
     * @param engine 搜索引擎
     * @param numResults 返回结果数量
     * @return 搜索结果
     */
    suspend fun search(
        query: String,
        engine: SearchEngine = SearchEngine.BAIDU,
        numResults: Int = DEFAULT_NUM_RESULTS
    ): Result<SearchResult> = withContext(Dispatchers.IO) {
        try {
            val limitedResults = numResults.coerceIn(1, MAX_NUM_RESULTS)
            if (engine == SearchEngine.KEENABLE) {
                return@withContext searchKeenable(query, limitedResults)
            }
            val searchUrl = engine.buildSearchUrl(query, limitedResults)

            val config = HtmlDownloadConfig.DEFAULT.copy(
                charset = engine.charset,
                connectTimeout = 15,
                readTimeout = 15
            )

            when (val downloadResult = htmlDownloader.downloadHtml(searchUrl, config)) {
                is HtmlDownloadResult.Success -> {
                    val doc = Jsoup.parse(downloadResult.htmlContent, downloadResult.url)
                    val results = parseSearchResults(doc, engine, limitedResults)

                    Result.success(
                        SearchResult(
                            query = query,
                            engine = engine,
                            results = results,
                            totalResults = results.size
                        )
                    )
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
     * 解析搜索结果页面
     */
    private fun parseSearchResults(
        doc: org.jsoup.nodes.Document,
        engine: SearchEngine,
        maxResults: Int
    ): List<SearchResultItem> {
        return when (engine) {
            SearchEngine.BAIDU -> parseBaiduResults(doc, maxResults)
            SearchEngine.GOOGLE -> parseGoogleResults(doc, maxResults)
            SearchEngine.BING -> parseBingResults(doc, maxResults)
            SearchEngine.DUCKDUCKGO -> parseDuckDuckGoResults(doc, maxResults)
            // JSON API,在 searchKeenable 中处理,不会走到 HTML 解析
            SearchEngine.KEENABLE -> emptyList()
        }
    }

    /**
     * Keenable 搜索(JSON API,免 Key)
     *
     * POST /v1/search/public,需带 X-Keenable-Title 请求头;
     * 结果正文在 snippet 字段(description 恒为空)。
     */
    private fun searchKeenable(query: String, maxResults: Int): Result<SearchResult> {
        val body = buildJsonObject {
            put("query", query)
            put("max_results", maxResults)
        }.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(SearchEngine.KEENABLE.searchUrlTemplate)
            .header("X-Keenable-Title", KEENABLE_APP_TITLE)
            .post(body)
            .build()

        return keenableClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return Result.failure(Exception("HTTP ${response.code}"))
            }
            val results = AppJson.parseToJsonElement(response.body.string())
                .jsonObject["results"]?.jsonArray.orEmpty()
                .map { it.jsonObject }
                .filter { !it["url"]?.jsonPrimitive?.contentOrNull.isNullOrBlank() }
                .take(maxResults)
                .mapIndexed { index, item ->
                    SearchResultItem(
                        title = item["title"]?.jsonPrimitive?.contentOrNull.orEmpty().trim(),
                        url = item["url"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                        snippet = item["snippet"]?.jsonPrimitive?.contentOrNull.orEmpty()
                            .replace(Regex("\\s+"), " ")
                            .trim()
                            .take(KEENABLE_SNIPPET_MAX_LENGTH),
                        position = index + 1
                    )
                }

            Result.success(
                SearchResult(
                    query = query,
                    engine = SearchEngine.KEENABLE,
                    results = results,
                    totalResults = results.size
                )
            )
        }
    }

    /**
     * 解析百度搜索结果
     */
    private fun parseBaiduResults(doc: org.jsoup.nodes.Document, maxResults: Int): List<SearchResultItem> {
        val results = mutableListOf<SearchResultItem>()
        val elements = doc.select("div.result, div.c-container")

        for ((index, element) in elements.withIndex()) {
            if (index >= maxResults) break

            val titleElement = element.select("h3 a, .t a").firstOrNull()
            val snippetElement = element.select("div.c-abstract, .c-span-last, div.content-right_8Zs40").firstOrNull()

            if (titleElement != null) {
                results.add(
                    SearchResultItem(
                        title = titleElement.text().trim(),
                        url = titleElement.absUrl("href"),
                        snippet = snippetElement?.text()?.trim() ?: "",
                        position = index + 1
                    )
                )
            }
        }

        return results
    }

    /**
     * 解析 Google 搜索结果
     */
    private fun parseGoogleResults(doc: org.jsoup.nodes.Document, maxResults: Int): List<SearchResultItem> {
        val results = mutableListOf<SearchResultItem>()
        val elements = doc.select("div.g")

        for ((index, element) in elements.withIndex()) {
            if (index >= maxResults) break

            val titleElement = element.select("h3").firstOrNull()?.parent()
            val snippetElement = element.select("div.VwiC3b, span.aCOpRe").firstOrNull()

            if (titleElement != null) {
                results.add(
                    SearchResultItem(
                        title = titleElement.text().trim(),
                        url = titleElement.absUrl("href"),
                        snippet = snippetElement?.text()?.trim() ?: "",
                        position = index + 1
                    )
                )
            }
        }

        return results
    }

    /**
     * 解析 Bing 搜索结果
     */
    private fun parseBingResults(doc: org.jsoup.nodes.Document, maxResults: Int): List<SearchResultItem> {
        val results = mutableListOf<SearchResultItem>()
        val elements = doc.select("li.b_algo")

        for ((index, element) in elements.withIndex()) {
            if (index >= maxResults) break

            val titleElement = element.select("h2 a").firstOrNull()
            val snippetElement = element.select("div.b_caption p").firstOrNull()

            if (titleElement != null) {
                results.add(
                    SearchResultItem(
                        title = titleElement.text().trim(),
                        url = titleElement.absUrl("href"),
                        snippet = snippetElement?.text()?.trim() ?: "",
                        position = index + 1
                    )
                )
            }
        }

        return results
    }

    /**
     * 解析 DuckDuckGo 搜索结果
     */
    private fun parseDuckDuckGoResults(doc: org.jsoup.nodes.Document, maxResults: Int): List<SearchResultItem> {
        val results = mutableListOf<SearchResultItem>()
        val elements = doc.select("div.result")

        for ((index, element) in elements.withIndex()) {
            if (index >= maxResults) break

            val titleElement = element.select("a.result__a").firstOrNull()
            val snippetElement = element.select("a.result__snippet").firstOrNull()

            if (titleElement != null) {
                results.add(
                    SearchResultItem(
                        title = titleElement.text().trim(),
                        url = titleElement.absUrl("href"),
                        snippet = snippetElement?.text()?.trim() ?: "",
                        position = index + 1
                    )
                )
            }
        }

        return results
    }
}
