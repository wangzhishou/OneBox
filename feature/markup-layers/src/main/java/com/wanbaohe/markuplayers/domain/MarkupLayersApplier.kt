package com.wanbaohe.markuplayers.domain

import com.wanbaohe.markuplayers.domain.model.MarkupLayer

/**
 * 把图层列表按 z 序逐层重绘到原图的可变副本上,输出与原图同分辨率的位图。
 * [filteredLayers] 可提供已完成顺序效果与局部 alpha 蒙版的导出内容(layerId → 位图)。
 * 不应传低分辨率预览缓存。缺失内容由实现按图层串行预算,解码/滤镜失败显式失败,
 * 不会以未处理内容替代。定位、调色、不透明度与混合模式在合成阶段应用。
 */
interface MarkupLayersApplier<I> {

    suspend fun applyToImage(
        image: I,
        layers: List<MarkupLayer>,
        filteredLayers: Map<String, I> = emptyMap(),
    ): I
}
