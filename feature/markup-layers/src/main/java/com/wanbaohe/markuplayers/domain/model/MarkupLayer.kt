package com.wanbaohe.markuplayers.domain.model

import java.util.UUID

/**
 * 一个图层 = 内容(type) + 变换(transform)。
 * 列表顺序即 z 序:索引越大越靠上。
 *
 * @param id 稳定唯一 id,用于选中态/历史快照定位
 * @param name 图层面板显示名,空串时按类型给默认名
 */
data class MarkupLayer(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val type: LayerType,
    val transform: LayerTransform = LayerTransform(),
)
