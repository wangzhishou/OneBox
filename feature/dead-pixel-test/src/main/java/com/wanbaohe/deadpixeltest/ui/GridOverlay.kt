package com.wanbaohe.deadpixeltest.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 像素网格叠加层
 *
 * 在当前测试色面板上叠加细网格线，帮助用户精确定位坏点坐标。
 * 线条颜色自动取与背景色的互补色（通过亮度反转），保证在任何背景上可见。
 *
 * @param baseColor    当前测试背景色，用于计算对比网格线颜色
 * @param cellSizeDp   网格单元格大小（默认 40 dp，约 40~60 像素，兼顾辨识度与性能）
 */
@Composable
fun GridOverlay(
    baseColor: Color,
    cellSizeDp: Float = 40f,
    modifier: Modifier = Modifier,
) {
    // 互补色：亮背景用深线，暗背景用亮线
    val gridColor = complementColor(baseColor).copy(alpha = 0.35f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val cellPx = cellSizeDp.dp.toPx()
        val w = size.width
        val h = size.height

        // 竖线
        var x = 0f
        while (x <= w) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 1f
            )
            x += cellPx
        }
        // 横线
        var y = 0f
        while (y <= h) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
            y += cellPx
        }
    }
}

/** 根据亮度计算互补色（亮→黑，暗→白，中间线性插值） */
private fun complementColor(color: Color): Color {
    val luminance = 0.299f * color.red + 0.587f * color.green + 0.114f * color.blue
    return if (luminance > 0.5f) Color.Black else Color.White
}

