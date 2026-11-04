package com.shifenmiao.common.search

import androidx.annotation.StringRes

data class GlobalSearchResultPage(
    val results: List<GlobalSearchResult>,
    val isTruncated: Boolean = false,
)

data class GlobalSearchResult(
    val sourceId: String,
    val id: String,
    val title: String,
    val description: String,
    @StringRes val labelRes: Int,
    val destination: GlobalSearchDestination,
    val iconName: String? = null,
    val relevance: Int = 0,
)
