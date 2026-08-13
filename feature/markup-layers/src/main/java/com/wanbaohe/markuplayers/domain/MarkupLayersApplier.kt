package com.wanbaohe.markuplayers.domain

import com.wanbaohe.markuplayers.domain.model.MarkupLayer

/**
 * 把图层列表重绘到底图上,输出与原图同分辨率的位图。
 * 取代旧的 Compose 截图保存方案。
 */
interface MarkupLayersApplier<I> {

    suspend fun applyToImage(
        image: I,
        layers: List<MarkupLayer>,
    ): I
}
