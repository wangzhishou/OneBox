package com.wanbaohe.textcard.domain.model

/**
 * 渲染一次文字卡片所需的完整状态快照。
 * 预览(Compose)与导出(android Canvas)共用同一份快照,保证两通道一致。
 */
data class TextCardRenderState(
    val canvas: CanvasSpec,
    val background: BackgroundSpec,
    val backgroundOpacity: Float = 1f,
    val title: TextBlock,
    val body: TextBlock,
    val decoration: DecorationSpec = DecorationSpec(),
    val layers: List<TextCardLayer> = TextCardLayer.defaultOrder(),
) {
    /** 按 z 序(底层在前)返回可见图层 */
    val visibleLayers: List<TextCardLayer> get() = layers.filter { it.visible }
}
