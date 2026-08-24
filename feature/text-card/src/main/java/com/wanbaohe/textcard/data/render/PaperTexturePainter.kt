package com.wanbaohe.textcard.data.render

import android.graphics.Canvas
import android.graphics.Paint
import com.wanbaohe.textcard.domain.model.PaperKind
import com.wanbaohe.textcard.domain.render.CardLayout

/**
 * 纸张纹理导出侧绘制(android Canvas)。间距/颜色常量与预览侧共用 [CardLayout],
 * 预览侧的 Compose 实现见 presentation/editor/PaperTexturePreview.kt。
 */
object PaperTexturePainter {

    fun draw(
        canvas: Canvas,
        kind: PaperKind,
        width: Float,
        height: Float,
    ) {
        when (kind) {
            PaperKind.Lined -> drawLined(canvas, width, height)
            PaperKind.Grid -> drawGrid(canvas, width, height)
            PaperKind.Kraft -> drawSolidPaper(
                canvas, width, height,
                baseColor = CardLayout.PAPER_KRAFT_COLOR,
                edgeColor = CardLayout.PAPER_KRAFT_EDGE_COLOR
            )
            PaperKind.Letter -> drawLetter(canvas, width, height)
            PaperKind.Colorful -> drawColorful(canvas, width, height)
        }
    }

    private fun drawLined(canvas: Canvas, width: Float, height: Float) {
        val spacing = width * CardLayout.PAPER_LINE_SPACING_RATIO
        val paint = strokePaint(CardLayout.PAPER_LINE_COLOR, width)
        var y = spacing
        while (y < height) {
            canvas.drawLine(0f, y, width, y, paint)
            y += spacing
        }
    }

    private fun drawGrid(canvas: Canvas, width: Float, height: Float) {
        val spacing = width * CardLayout.PAPER_GRID_SPACING_RATIO
        val paint = strokePaint(CardLayout.PAPER_LINE_COLOR, width)
        var y = spacing
        while (y < height) {
            canvas.drawLine(0f, y, width, y, paint)
            y += spacing
        }
        var x = spacing
        while (x < width) {
            canvas.drawLine(x, 0f, x, height, paint)
            x += spacing
        }
    }

    /** 纯色纸近似:底色 + 内描边(牛皮纸用) */
    private fun drawSolidPaper(
        canvas: Canvas,
        width: Float,
        height: Float,
        baseColor: Long,
        edgeColor: Long,
    ) {
        canvas.drawColor(baseColor.toInt())
        val edgeWidth = width * CardLayout.PAPER_EDGE_WIDTH_RATIO
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = edgeColor.toInt()
            style = Paint.Style.STROKE
            strokeWidth = edgeWidth
        }
        val half = edgeWidth / 2f
        canvas.drawRect(half, half, width - half, height - half, paint)
    }

    /** 信纸近似:米色底 + 顶部装饰条 */
    private fun drawLetter(canvas: Canvas, width: Float, height: Float) {
        canvas.drawColor(CardLayout.PAPER_LETTER_COLOR.toInt())
        val barHeight = width * CardLayout.PAPER_EDGE_WIDTH_RATIO
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = CardLayout.PAPER_LETTER_ACCENT_COLOR.toInt()
        }
        canvas.drawRect(0f, 0f, width, barHeight, paint)
        canvas.drawRect(0f, height - barHeight, width, height, paint)
    }

    /** 彩色纸近似:暖色底 + 规则波点 */
    private fun drawColorful(canvas: Canvas, width: Float, height: Float) {
        canvas.drawColor(CardLayout.PAPER_COLORFUL_COLOR.toInt())
        val spacing = width * CardLayout.PAPER_GRID_SPACING_RATIO * 1.5f
        val radius = width * 0.006f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = CardLayout.PAPER_COLORFUL_DOT_COLOR.toInt()
        }
        var y = spacing / 2f
        var row = 0
        while (y < height) {
            val offset = if (row % 2 == 0) 0f else spacing / 2f
            var x = spacing / 2f + offset
            while (x < width) {
                canvas.drawCircle(x, y, radius, paint)
                x += spacing
            }
            y += spacing
            row++
        }
    }

    private fun strokePaint(color: Long, width: Float) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color.toInt()
            style = Paint.Style.STROKE
            strokeWidth = (width * 0.0012f).coerceAtLeast(1f)
        }
}
