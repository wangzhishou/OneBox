package com.wanbaohe.cloud.storage.model

data class CloudObjectItem(
    val key: String,
    val displayName: String,
    val size: Long,
    val lastModified: String? = null,
    val eTag: String? = null,
    val contentType: String? = null,
    val isDirectory: Boolean = false,
    val isImage: Boolean = false,
) {
    val prefix: String
        get() = if (isDirectory) key else key.substringBeforeLast('/', missingDelimiterValue = "")
}
