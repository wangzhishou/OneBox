package com.wanbaohe.poem.component

import com.wanbaohe.poem.model.Poem

data class PoemUiState(
    val poem: Poem? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isGeneratingInsight: Boolean = false,
    val insightError: String? = null,
    val isGeneratingTranslation: Boolean = false,
    val translationError: String? = null,
    /** 拼音生成中(静默后台任务,用于底部灰色状态提示) */
    val isGeneratingPinyin: Boolean = false,
    /** 最近历史(前 5 条),含当前展示的诗词 */
    val recentHistory: List<Poem> = emptyList(),
)
