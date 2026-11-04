package com.shifenmiao.common.search

interface GlobalSearchProvider {
    val sourceId: String

    suspend fun search(query: String, limit: Int): GlobalSearchResultPage
}
