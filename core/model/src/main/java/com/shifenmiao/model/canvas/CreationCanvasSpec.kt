package com.shifenmiao.model.canvas

enum class CreationCanvasSpec(
    val width: Int,
    val height: Int,
    val ratioLabel: String,
) {
    SQUARE(width = 1080, height = 1080, ratioLabel = "1:1"),
    PORTRAIT(width = 1080, height = 1440, ratioLabel = "3:4"),
    LANDSCAPE(width = 1440, height = 1080, ratioLabel = "4:3"),
    WIDE(width = 1920, height = 1080, ratioLabel = "16:9"),
    STORY(width = 1080, height = 1920, ratioLabel = "9:16");

    val aspectRatio: Float get() = width.toFloat() / height.toFloat()
}
