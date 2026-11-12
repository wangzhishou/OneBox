/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    // 不区分命名空间：标准服务器（Nextcloud / Apache mod_dav / 坚果云）返回
                    // <D:multistatus xmlns:D="DAV:">，按 namespace 过滤会一个条目都解析不到。
                    val tag = parser.name
                    if (tag == "response") {
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
                        if (tag == "collection") {
                            isCollection = true
                        }
                    } else if (inResponse && tag == "href") {
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
                    if (tag == "prop") {
                        inProp = false
                        currentTag = null
                    } else if (tag == "response") {
                        inResponse = false
                        href?.let { entries += Entry(it, isCollection, contentLength, lastModified, contentType, etag) }
                    }
                }
            }
            event = parser.next()
        }
        return entries
    }

    /**
     * 把 PROPFIND 条目转成 [CloudObjectItem]。
     *
     * @param basePath 当前目录相对 WebDAV 根的路径（如 `照片/2026旅行`），用于过滤父集合与兄弟项。
     * @param hrefPrefix WebDAV 根在服务器上的绝对路径前缀（如 Nextcloud 的
     *   `remote.php/dav/files/user`）。多数服务器返回的 href 含这段前缀，必须先剥掉，
     *   否则所有条目都会被当成"不在当前目录"而过滤光。
     */
    fun entriesToItems(
        entries: List<Entry>,
        basePath: String,
        hrefPrefix: String = "",
    ): List<CloudObjectItem> {
        val normalizedBase = basePath.trim('/').ifBlank { "" }
        val normalizedPrefix = hrefPrefix.trim('/')
        return entries.mapNotNull { entry ->
            val rawHref = entry.href.split("?", limit = 2).first().trim()
            // RFC 4918 允许 href 是绝对 URL（http://host/path），只取 path 部分。
            val pathHref = if (rawHref.startsWith("http://") || rawHref.startsWith("https://")) {
                runCatching { java.net.URI(rawHref).path }.getOrNull() ?: rawHref
            } else {
                rawHref
            }
            var cleanPath = java.net.URLDecoder.decode(pathHref, "UTF-8").trimStart('/')
            if (normalizedPrefix.isNotBlank()) {
                cleanPath = when {
                    cleanPath == normalizedPrefix -> ""
                    cleanPath.startsWith("$normalizedPrefix/") -> cleanPath.removePrefix("$normalizedPrefix/")
                    else -> cleanPath
                }
            }
            if (cleanPath.isBlank()) return@mapNotNull null
            val segments = cleanPath.split('/').filter { it.isNotBlank() }
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

    /**
     * WebDAV 只有一个根，"桶名"用服务器主机名，避免和页面标题里的连接名重复。
     */
    fun singleRoot(connection: CloudStorageConnection): List<CloudBucket> {
        val webDav = connection as? CloudStorageConnection.WebDav
            ?: return listOf(CloudBucket(name = "WebDAV"))
        val host = webDav.baseUrl
            .removePrefix("https://")
            .removePrefix("http://")
            .substringBefore("/")
            .ifBlank { "WebDAV" }
        return listOf(CloudBucket(name = host))
    }
}
