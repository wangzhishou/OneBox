package com.wanbaohe.textcard.domain.model

/**
 * 渲染一次图文卡片所需的完整状态快照。
 * 预览(Compose)与导出(android Canvas)共用同一份快照,保证两通道一致。
 * 渲染顺序:背景(最底,backgroundVisible 控制)→ 按 layers 列表 z 序逐层画文字/装饰。
 */
data class TextCardRenderState(
    val canvas: CanvasSpec,
    val background: BackgroundSpec,
    val backgroundOpacity: Float = 1f,
    val backgroundVisible: Boolean = true,
    val textBlocks: List<TextBlock>,
    val decorations: List<DecorationSpec> = emptyList(),
    val layers: List<ElementLayer>,
) {
    /** 按 z 序(底层在前)返回可见元素图层 */
    val visibleLayers: List<ElementLayer> get() = layers.filter { it.visible }

    fun blockOf(id: String): TextBlock? = textBlocks.find { it.id == id }
    fun decorationOf(id: String): DecorationSpec? = decorations.find { it.id == id }
}
