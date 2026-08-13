package com.wanbaohe.markuplayers.domain.model

/**
 * 图层变换,坐标全部相对底图归一化(0..1),
 * UI 预览按预览尺寸换算,导出按原图尺寸换算,保证所见即所得。
 *
 * @param centerX 图层中心点 X,相对底图宽度
 * @param centerY 图层中心点 Y,相对底图高度
 * @param scale 缩放倍数(相对图层自身基础尺寸)
 * @param rotation 旋转角度(度)
 * @param alpha 不透明度 0..1
 * @param visible 是否可见
 * @param locked 是否锁定(锁定后不可选中/变换)
 * @param blendMode 混合模式,本期仅 [LayerBlendMode.Normal]
 */
data class LayerTransform(
    val centerX: Float = 0.5f,
    val centerY: Float = 0.5f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val alpha: Float = 1f,
    val visible: Boolean = true,
    val locked: Boolean = false,
    val blendMode: LayerBlendMode = LayerBlendMode.Normal,
)

enum class LayerBlendMode {
    Normal,
    // 预留扩展:Multiply, Screen, Overlay ...
}
