package com.shifenmiao.marquee.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import kotlin.math.roundToInt

/**
 * A Text that is optically centered using ink bounds calculation.
 * This ensures the actual visible glyphs are centered, not the font metrics box.
 */
@Suppress("UNUSED_PARAMETER")
@Composable
internal fun OpticalCenteredText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color,
    fontSize: TextUnit,
    fontWeight: FontWeight,
    textAlign: TextAlign,
    softWrap: Boolean,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Clip,
    clipContent: Boolean = false,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    style: TextStyle = TextStyle(
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    ),
) {
    val measurer = rememberTextMeasurer()

    val optimizedStyle = remember(style, fontSize, fontWeight, textAlign, color, letterSpacing) {
        style.copy(
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = textAlign,
            lineHeight = fontSize,
            letterSpacing = letterSpacing,
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.Both
            ),
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        )
    }

    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
        softWrap = softWrap,
        maxLines = maxLines,
        overflow = overflow,
        letterSpacing = letterSpacing,
        style = optimizedStyle,
        modifier = modifier.layout { measurable, constraints ->
            // 无限制测量，获取文字实际大小
            val looseConstraints = Constraints(
                maxWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else Int.MAX_VALUE,
                maxHeight = Int.MAX_VALUE
            )
            val placeable = measurable.measure(looseConstraints)

            // 测量墨水区域
            val layoutResult = measurer.measure(
                text = text,
                style = optimizedStyle,
                softWrap = softWrap,
                maxLines = maxLines,
                overflow = overflow,
                constraints = looseConstraints
            )

            // 计算墨水边界 - 使用原生 Paint 获取更精确的文字边界
            var minInkTop = Float.MAX_VALUE
            var maxInkBottom = Float.MIN_VALUE

            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = fontSize.toPx()
                val typefaceStyle = if (fontWeight.weight >= 600) {
                    android.graphics.Typeface.BOLD
                } else {
                    android.graphics.Typeface.NORMAL
                }
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, typefaceStyle)
            }
            val bounds = android.graphics.Rect()

            for (i in 0 until layoutResult.lineCount) {
                val start = layoutResult.getLineStart(i)
                val end = layoutResult.getLineEnd(i)
                if (start < end) {
                    paint.getTextBounds(text, start, end, bounds)
                    val baseline = layoutResult.getLineBaseline(i)
                    val lineTop = baseline + bounds.top
                    val lineBottom = baseline + bounds.bottom

                    if (lineTop < minInkTop) minInkTop = lineTop
                    if (lineBottom > maxInkBottom) maxInkBottom = lineBottom
                }
            }

            val hasValidInkBounds = minInkTop < maxInkBottom && minInkTop != Float.MAX_VALUE

            // 布局尺寸：使用父容器给定的约束
            val layoutWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else placeable.width
            val layoutHeight = if (constraints.hasBoundedHeight) constraints.maxHeight else placeable.height

            // 计算墨水区域的中心
            val inkCenterInPlaceable = if (hasValidInkBounds) (minInkTop + maxInkBottom) / 2f else placeable.height / 2f

            // 水平居中
            val deltaX = (layoutWidth - placeable.width) / 2

            // 垂直居中：将墨水区域的中心对齐到布局中心
            val layoutCenterY = layoutHeight / 2f
            val deltaY = (layoutCenterY - inkCenterInPlaceable).roundToInt()

            layout(layoutWidth, layoutHeight) {
                placeable.placeRelative(deltaX, deltaY)
            }
        }
    )
}