package com.wanbaohe.cloud.storage.data.protocol

object ObjectStoragePathResolver {

    fun normalizePrefix(prefix: String): String {
        val trimmed = prefix.trim().trim('/')
        return if (trimmed.isBlank()) "" else "$trimmed/"
    }

    fun splitBreadcrumbs(prefix: String): List<String> {
        val normalized = normalizePrefix(prefix)
        if (normalized.isBlank()) return listOf("")
        val rawParts = normalized.removeSuffix("/").split("/").filter { it.isNotBlank() }
        val crumbs = mutableListOf("")
        var acc = ""
        rawParts.forEach { part ->
            acc = if (acc.isBlank()) "$part/" else "$acc$part/"
            crumbs += acc
        }
        return crumbs
    }

    fun parentPrefix(prefix: String): String {
        val parts = normalizePrefix(prefix).removeSuffix("/").split("/").filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> ""
            parts.size == 1 -> ""
            else -> parts.dropLast(1).joinToString(separator = "/", postfix = "/")
        }
    }

    fun childPrefix(currentPrefix: String, childName: String): String =
        normalizePrefix("${normalizePrefix(currentPrefix)}${childName.trim().trim('/')}")

    fun displayName(key: String, isDirectory: Boolean): String {
        val normalized = if (isDirectory) key.removeSuffix("/") else key
        return normalized.substringAfterLast('/', normalized)
    }
}
