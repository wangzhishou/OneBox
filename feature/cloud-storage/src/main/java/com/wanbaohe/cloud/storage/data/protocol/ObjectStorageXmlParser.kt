package com.wanbaohe.cloud.storage.data.protocol

import com.wanbaohe.cloud.storage.model.CloudBucket
import com.wanbaohe.cloud.storage.model.CloudObjectItem
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

object ObjectStorageXmlParser {

    data class ListObjectsResult(
        val items: List<CloudObjectItem>,
        val nextContinuationToken: String? = null,
    )

    fun parseBuckets(xml: String): List<CloudBucket> {
        val doc = parseDocument(xml) ?: return emptyList()
        val buckets = doc.getElementsByTagName("Bucket")
        return buildList {
            for (index in 0 until buckets.length) {
                val element = buckets.item(index) as? Element ?: continue
                add(
                    CloudBucket(
                        name = element.childText("Name") ?: continue,
                        creationDate = element.childText("CreationDate"),
                    )
                )
            }
        }
    }

    fun parseListObjects(xml: String): ListObjectsResult {
        val doc = parseDocument(xml) ?: return ListObjectsResult(emptyList())
        val root = doc.documentElement ?: return ListObjectsResult(emptyList())
        val items = mutableListOf<CloudObjectItem>()

        val prefixes = root.getElementsByTagName("CommonPrefixes")
        for (index in 0 until prefixes.length) {
            val element = prefixes.item(index) as? Element ?: continue
            val prefix = element.childText("Prefix") ?: continue
            items += CloudObjectItem(
                key = prefix,
                displayName = ObjectStoragePathResolver.displayName(prefix, isDirectory = true),
                size = 0L,
                isDirectory = true,
            )
        }

        val contents = root.getElementsByTagName("Contents")
        for (index in 0 until contents.length) {
            val element = contents.item(index) as? Element ?: continue
            val key = element.childText("Key") ?: continue
            if (key.endsWith("/")) {
                items += CloudObjectItem(
                    key = key,
                    displayName = ObjectStoragePathResolver.displayName(key, isDirectory = true),
                    size = 0L,
                    lastModified = element.childText("LastModified"),
                    eTag = element.childText("ETag")?.trim('"'),
                    isDirectory = true,
                )
            } else {
                items += CloudObjectItem(
                    key = key,
                    displayName = ObjectStoragePathResolver.displayName(key, isDirectory = false),
                    size = element.childText("Size")?.toLongOrNull() ?: 0L,
                    lastModified = element.childText("LastModified"),
                    eTag = element.childText("ETag")?.trim('"'),
                    isImage = isImageKey(key),
                )
            }
        }

        return ListObjectsResult(
            items = items.distinctBy { it.key },
            nextContinuationToken = root.childText("NextContinuationToken")
                ?: root.childText("NextMarker"),
        )
    }

    private fun parseDocument(xml: String) = runCatching {
        DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(ByteArrayInputStream(xml.toByteArray()))
    }.getOrNull()

    private fun Element.childText(tagName: String): String? {
        val nodes = getElementsByTagName(tagName)
        if (nodes.length == 0) return null
        return nodes.item(0)?.textContent?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun isImageKey(key: String): Boolean {
        val lower = key.lowercase()
        return lower.endsWith(".png") ||
            lower.endsWith(".jpg") ||
            lower.endsWith(".jpeg") ||
            lower.endsWith(".webp") ||
            lower.endsWith(".gif") ||
            lower.endsWith(".bmp") ||
            lower.endsWith(".avif")
    }
}
