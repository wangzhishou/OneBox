package com.hrm.diagram.render.compose

import androidx.compose.ui.text.TextMeasurer as ComposeTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.hrm.diagram.core.draw.FontSpec
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.text.TextMetrics

/**
 * Bridges Compose's [ComposeTextMeasurer] (Skia-backed, pixel-perfect) to the
 * backend-neutral [TextMeasurer] used by the layout pipeline.
 *
 * `Diagram.session(textMeasurer = ComposeTextMeasurerAdapter(rememberTextMeasurer()))`.
 *
 * Note: the adapter treats `FontSpec.sizeSp` as sp directly; Compose handles the px conversion
 * through the active `Density`. Returned metrics are in *the same unit Compose draws in*
 * (pixels at the current density), which matches what `DiagramCanvas` consumes when it draws.
 */
internal class ComposeTextMeasurerAdapter(
    private val measurer: ComposeTextMeasurer,
    private val defaultStyle: TextStyle = TextStyle(),
) : TextMeasurer {
    override fun measure(text: String, font: FontSpec, maxWidth: Float?): TextMetrics {
        val style = defaultStyle.copy(fontSize = font.sizeSp.sp)
        val constraints = if (maxWidth != null && maxWidth > 0f) {
            Constraints(maxWidth = maxWidth.toInt().coerceAtLeast(1))
        } else Constraints()
        val r = measurer.measure(text = text, style = style, constraints = constraints)
        return TextMetrics(
            width = r.size.width.toFloat(),
            height = r.size.height.toFloat(),
            ascent = r.firstBaseline,
            lineCount = r.lineCount,
        )
    }
}
