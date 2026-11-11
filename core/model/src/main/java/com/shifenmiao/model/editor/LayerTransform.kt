package com.shifenmiao.model.editor

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
