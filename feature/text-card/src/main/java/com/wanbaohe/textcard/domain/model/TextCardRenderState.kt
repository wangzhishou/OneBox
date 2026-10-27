package com.wanbaohe.textcard.domain.model

data class TextCardSegment(
    val block: TextBlock,
    val role: TextCardRole,
    val sourceStart: Int,
    val sourceEnd: Int,
)

data class TextCardSourceSelection(
    val role: TextCardRole,
    val start: Int,
    val end: Int,
)

data class TextCardRenderState(
    val canvas: CanvasSpec,
    val background: BackgroundSpec,
    val backgroundOpacity: Float = 1f,
    val backgroundVisible: Boolean = true,
    val textBlocks: List<TextBlock> = emptyList(),
    val segments: List<TextCardSegment> = emptyList(),
)
