package com.shifenmiao.base.ui.painter

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class TextPainterNoBg(
    private val size: Size,
    textMeasurer: TextMeasurer,
    val text: String,
    textColor: Color = Color.White
) : Painter() {

    private val textLayoutResult: TextLayoutResult = textMeasurer.measure(
        text = AnnotatedString(text),
        style = TextStyle(
            color = textColor,
            fontSize = (size.width / 2.5).dp.value.sp,
            fontWeight = FontWeight.Bold
        )
    )

    override val intrinsicSize: Size get() = size

    override fun DrawScope.onDraw() {
        val textSize = textLayoutResult.size
        // The text
        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                (this.size.width - textSize.width) / 2f,
                (this.size.height - textSize.height) / 2f
            )
        )
    }
}