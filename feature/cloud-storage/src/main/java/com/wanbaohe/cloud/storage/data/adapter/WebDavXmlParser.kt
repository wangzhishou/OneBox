package com.wanbaohe.cloud.storage.data.adapter

import com.wanbaohe.cloud.storage.model.CloudBucket
import com.wanbaohe.cloud.storage.model.CloudObjectItem
import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

/**
 * WebDAV `multistatus` 响应解析。
 *
 * 仅提取 `propfind` 列表场景需要的最小字段：href / resourcetype(是否为集合) / getcontentlength /
 * getlastmodified / getcontenttype / getetag。
 */
internal object WebDavXmlParser {

    data class Entry(
        val href: String,
        val isCollection: Boolean,
        val contentLength: Long,
        val lastModified: String?,
        val contentType: String?,
        val etag: String?,
    )

    fun parsePropfind(xml: String): List<Entry> {
        val factory = XmlPullParserFactory.newInstance().apply { isNamespaceAware = true }
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        val entries = mutableListOf<Entry>()
        var inResponse = false
        var inProp = false
        var href: String? = null
        var isCollection = false
        var contentLength: Long = 0L
        var lastModified: String? = null
        var contentType: String? = null
        var etag: String? = null
        var currentTag: String? = null
        var currentNs: String? = null

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    val tag = parser.name
                    val ns = parser.namespace
                    if (tag == "response" && ns.isNullOrEmpty()) {
                        inResponse = true
                        href = null
                        isCollection = false
                        contentLength = 0L
                        lastModified = null
                        contentType = null
                        etag = null
                    } else if (inResponse && tag == "prop") {
                        inProp = true
                    } else if (inProp) {
                        currentTag = tag
                        currentNs = ns
                        if (tag == "collection" && ns.isNullOrEmpty()) {
                            isCollection = true
                        }
                    } else if (inResponse && tag == "href" && ns.isNullOrEmpty()) {
                        href = parser.nextText().trim()
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inProp && currentTag != null) {
                        val text = parser.text?.trim().orEmpty()
                        if (text.isNotEmpty()) {
                            when (currentTag) {
                                "getcontentlength" -> contentLength = text.toLongOrNull() ?: 0L
                                "getlastmodified" -> lastModified = text
                                "getcontenttype" -> contentType = text
                                "getetag" -> etag = text.trim('"')
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    val tag = parser.name
                    val ns = parser.namespace
                    if (tag == "prop" && ns.isNullOrEmpty()) {
                        inProp = false
                        currentTag = null
                    } else if (tag == "response" && ns.isNullOrEmpty()) {
                        inResponse = false
                        href?.let { entries += Entry(it, isCollection, contentLength, lastModified, contentType, etag) }
                    }
                }
            }
            event = parser.next()
        }
        return entries
    }

    fun entriesToItems(
        entries: List<Entry>,
        basePath: String,
    ): List<CloudObjectItem> {
        val normalizedBase = basePath.trim('/').ifBlank { "" }
        return entries.mapNotNull { entry ->
            val rawHref = entry.href.split("?", limit = 2).first().trim()
            val cleanPath = java.net.URLDecoder.decode(rawHref, "UTF-8")
            // 假设：PROPFIND 返回的 href 是相对 host 的"绝对路径"（如 /webdav/docs/file.txt）。
            // 这是主流服务器（Apache mod_dav / Nextcloud / nginx-dav）的实际行为。
            // 但 RFC 4918 § 8.3 允许服务器返回相对路径（如 docs/file.txt）—— 当前实现
            // 不会处理这种情况，会把 "docs/file.txt" 当成绝对路径（leading '/' 被 trim）。
            // 若需严格兼容，需先解析 request-uri 头拿到 base，再 resolve。
            val trimmed = cleanPath.trimStart('/')
            if (trimmed.isBlank()) return@mapNotNull null
            val segments = trimmed.split('/').filter { it.isNotBlank() }
            // 排除父集合自身（如当前目录）
            if (segments.size == 1 && segments[0] == normalizedBase) return@mapNotNull null
            if (!normalizedBase.isBlank()) {
                if (segments.size <= 1) return@mapNotNull null
                if (segments.dropLast(1).joinToString("/") != normalizedBase) return@mapNotNull null
            }
            val key = segments.joinToString("/")
            val name = segments.last()
            if (name.isBlank()) return@mapNotNull null
            if (entry.isCollection || key.endsWith("/") || name.endsWith("/")) {
                CloudObjectItem(
                    key = "$key/",
                    displayName = name.trimEnd('/'),
                    size = 0L,
                    isDirectory = true,
                )
            } else {
                CloudObjectItem(
                    key = key,
                    displayName = name,
                    size = entry.contentLength,
                    lastModified = entry.lastModified,
                    eTag = entry.etag,
                    contentType = entry.contentType,
                    isImage = entry.contentType?.startsWith("image/") == true,
                )
            }
        }
    }

    fun singleRoot(connection: CloudStorageConnection): List<CloudBucket> =
        listOf(CloudBucket(name = connection.displayName.ifBlank { "WebDAV" }))
}
