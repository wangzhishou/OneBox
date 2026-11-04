package com.shifenmiao.common.search

object GlobalSearchRelevance {
    fun score(query: String, title: String, description: String): Int {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isEmpty()) return 0
        val normalizedTitle = title.lowercase()
        val normalizedDescription = description.lowercase()
        return when {
            normalizedTitle == normalizedQuery -> 400
            normalizedTitle.startsWith(normalizedQuery) -> 300
            normalizedTitle.contains(normalizedQuery) -> 200
            normalizedDescription.contains(normalizedQuery) -> 100
            else -> 0
        }
    }
}
