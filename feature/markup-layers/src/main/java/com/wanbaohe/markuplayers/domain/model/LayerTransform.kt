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
 * @param blendMode 与下方所有已合成图层的混合模式
 * @param flipHorizontal 内容中心水平翻转,先于 rotation
 * @param flipVertical 内容中心垂直翻转,先于 rotation
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
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
)

enum class LayerBlendMode {
    Normal,
    Multiply,
    Screen,
    Overlay,
    Darken,
    Lighten,
}
