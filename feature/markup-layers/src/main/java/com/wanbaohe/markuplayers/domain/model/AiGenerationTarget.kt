package com.wanbaohe.markuplayers.domain.model

enum class AiGenerationTarget {
    NewLayer, SelectedLayer, Background
}

data class AiImageResult(
    val imagePath: String,
    val title: String,
    val targetLayerId: String?,
    val replaceBackground: Boolean,
    val width: Int,
    val height: Int,
    /** 采用结果时扣除的积分。0 表示本次不扣(缓存命中或未计价)。 */
    val pointsCost: Int = 0,
)
