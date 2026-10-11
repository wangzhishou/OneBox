package com.wanbaohe.markuplayers.domain.project

data class MarkupProjectSummary(
    val id: String,
    val name: String,
    val updatedAt: Long,
    val thumbnailPath: String?,
    val layerCount: Int,
)
