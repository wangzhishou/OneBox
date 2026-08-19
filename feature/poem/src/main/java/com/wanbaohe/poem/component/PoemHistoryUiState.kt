package com.wanbaohe.poem.component

import com.wanbaohe.poem.model.Poem

data class PoemHistoryUiState(
    val poems: List<Poem> = emptyList(),
    val favoritesOnly: Boolean = false,
    /** 非空时页内展示该诗词详情 */
    val selectedPoem: Poem? = null,
    val isGeneratingInsight: Boolean = false,
    val insightError: String? = null,
)
