package com.wanbaohe.poem.component

import com.wanbaohe.poem.model.Poem

data class PoemSearchUiState(
    val query: String = "",
    val dynasties: List<String> = emptyList(),
    val types: List<String> = emptyList(),
    val selectedDynasty: String? = null,
    val selectedType: String? = null,
    val selectedAuthor: String? = null,
    val results: List<Poem> = emptyList(),
    val hasSearched: Boolean = false,
    val isSearching: Boolean = false,
    val error: String? = null,
    /** 非空时页内展示该诗词详情 */
    val selectedPoem: Poem? = null,
    val isGeneratingInsight: Boolean = false,
    val insightError: String? = null,
    val isGeneratingTranslation: Boolean = false,
    val translationError: String? = null,
) {
    /** 朝代/体裁/诗人筛选在结果集上本地过滤(搜索接口只支持关键词) */
    val displayedResults: List<Poem>
        get() = results.filter { poem ->
            (selectedDynasty == null || poem.dynasty == selectedDynasty) &&
                (selectedType == null || poem.type == selectedType) &&
                (selectedAuthor == null || poem.author == selectedAuthor)
        }
}
