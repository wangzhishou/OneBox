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
)
