package com.wanbaohe.textcard.presentation.editor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.wanbaohe.textcard.domain.model.PaperKind
import com.wanbaohe.textcard.domain.render.CardLayout

/**
 * 纸张纹理预览侧绘制(Compose DrawScope)。间距/颜色常量与导出侧共用 [CardLayout],
 * 导出侧的 android Canvas 实现见 data/render/PaperTexturePainter.kt。
 */
fun DrawScope.drawPaperTexture(kind: PaperKind) {
    val width = size.width
    val height = size.height
    when (kind) {
        PaperKind.Lined -> drawLinedPaper(width, height)
        PaperKind.Grid -> drawGridPaper(width, height)
        PaperKind.Kraft -> drawSolidPaper(
            width, height,
            baseColor = Color(CardLayout.PAPER_KRAFT_COLOR),
            edgeColor = Color(CardLayout.PAPER_KRAFT_EDGE_COLOR)
        )

        PaperKind.Letter -> drawLetterPaper(width, height)
        PaperKind.Colorful -> drawColorfulPaper(width, height)
    }
}

private fun DrawScope.drawLinedPaper(width: Float, height: Float) {
    val spacing = width * CardLayout.PAPER_LINE_SPACING_RATIO
    val lineColor = Color(CardLayout.PAPER_LINE_COLOR)
    val stroke = (width * 0.0012f).coerceAtLeast(1f)
    var y = spacing
    while (y < height) {
        drawLine(lineColor, Offset(0f, y), Offset(width, y), strokeWidth = stroke)
        y += spacing
    }
}

private fun DrawScope.drawGridPaper(width: Float, height: Float) {
    val spacing = width * CardLayout.PAPER_GRID_SPACING_RATIO
    val lineColor = Color(CardLayout.PAPER_LINE_COLOR)
    val stroke = (width * 0.0012f).coerceAtLeast(1f)
    var y = spacing
    while (y < height) {
        drawLine(lineColor, Offset(0f, y), Offset(width, y), strokeWidth = stroke)
        y += spacing
    }
    var x = spacing
    while (x < width) {
        drawLine(lineColor, Offset(x, 0f), Offset(x, height), strokeWidth = stroke)
        x += spacing
    }
}

/** 纯色纸近似:底色 + 内描边(牛皮纸用) */
private fun DrawScope.drawSolidPaper(
    width: Float,
    height: Float,
    baseColor: Color,
    edgeColor: Color,
) {
    drawRect(baseColor)
    val edgeWidth = width * CardLayout.PAPER_EDGE_WIDTH_RATIO
    drawRect(
        color = edgeColor,
        topLeft = Offset(edgeWidth / 2f, edgeWidth / 2f),
        size = size.copy(width = width - edgeWidth, height = height - edgeWidth),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = edgeWidth)
    )
}

/** 信纸近似:米色底 + 顶/底部装饰条 */
private fun DrawScope.drawLetterPaper(width: Float, height: Float) {
    drawRect(Color(CardLayout.PAPER_LETTER_COLOR))
    val barHeight = width * CardLayout.PAPER_EDGE_WIDTH_RATIO
    val accent = Color(CardLayout.PAPER_LETTER_ACCENT_COLOR)
    drawRect(accent, size = size.copy(height = barHeight))
    drawRect(accent, topLeft = Offset(0f, height - barHeight), size = size.copy(height = barHeight))
}

/** 彩色纸近似:暖色底 + 规则波点 */
private fun DrawScope.drawColorfulPaper(width: Float, height: Float) {
    drawRect(Color(CardLayout.PAPER_COLORFUL_COLOR))
    val spacing = width * CardLayout.PAPER_GRID_SPACING_RATIO * 1.5f
    val radius = width * 0.006f
    val dotColor = Color(CardLayout.PAPER_COLORFUL_DOT_COLOR)
    var y = spacing / 2f
    var row = 0
    while (y < height) {
        val offset = if (row % 2 == 0) 0f else spacing / 2f
        var x = spacing / 2f + offset
        while (x < width) {
            drawCircle(dotColor, radius = radius, center = Offset(x, y))
            x += spacing
        }
        y += spacing
        row++
    }
}
