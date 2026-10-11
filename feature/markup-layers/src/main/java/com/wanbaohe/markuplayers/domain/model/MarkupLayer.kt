package com.wanbaohe.markuplayers.domain.model

import com.t8rin.imagetoolbox.core.filters.presentation.model.UiFilter
import com.wanbaohe.markuplayers.presentation.tools.adjust.BaseAdjustments
import java.util.UUID

/**
 * 一个图层 = 内容(type) + 变换(transform)。
 * 列表顺序即 z 序:索引越大越靠上。
 *
 * @param id 稳定唯一 id,用于选中态/历史快照定位
 * @param name 图层面板显示名,空串时按类型给默认名
 * @param adjustments 图层自身的矩阵调色,中立值 = 无调节;
 * 随图层列表进 undo 历史,预览经 colorFilter 实时生效,导出时按图层烘焙
 * @param filter 旧项目的单滤镜兼容字段。新编辑写入 [effects] 并清空本字段。
 * @param effects 顺序执行的位图效果;文字/形状需先光栅化。
 * @param mask 位图内容局部坐标的 alpha 蒙版,不随图层变换改写。
 * @param imageHistory 本地图片内容版本引用,不保存 Bitmap。
 * @param groupId 编辑器分组标识,不改变列表的 z 序。
 */
data class MarkupLayer(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val type: LayerType,
    val transform: LayerTransform = LayerTransform(),
    val adjustments: BaseAdjustments = BaseAdjustments(),
    val filter: UiFilter<*>? = null,
    val effects: List<LayerEffect> = emptyList(),
    val mask: LayerMask? = null,
    val imageHistory: List<String> = emptyList(),
    val groupId: String? = null,
) {
    val effectiveEffects: List<LayerEffect>
        get() = effects.ifEmpty { filter?.let { listOf(LayerEffect(it)) }.orEmpty() }

    val requiresBitmapProcessing: Boolean
        get() = mask != null || effectiveEffects.isNotEmpty()
}
