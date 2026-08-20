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
    /** 诗朗诵语音合成中 */
    val isSynthesizingSpeech: Boolean = false,
    /** 诗朗诵播放中 */
    val isReciting: Boolean = false,
    /** 全部历史(时间倒序),含当前展示的诗词;主页滑动翻页与历史弹层共用 */
    val history: List<Poem> = emptyList(),
)
