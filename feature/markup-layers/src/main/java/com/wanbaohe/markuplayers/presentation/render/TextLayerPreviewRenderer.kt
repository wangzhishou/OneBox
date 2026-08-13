package com.wanbaohe.markuplayers.presentation.render

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.em
import com.t8rin.imagetoolbox.core.settings.presentation.model.toUiFont
import com.t8rin.imagetoolbox.core.ui.theme.toColor
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.text.OutlineParams
import com.t8rin.imagetoolbox.core.ui.widget.text.OutlinedText
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import kotlin.reflect.KClass

/** 文字图层预览:字号 = fontSizeRatio × 画布宽,与导出侧(× 原图宽)同比例 */
object TextLayerPreviewRenderer : LayerPreviewRenderer {

    override val supportedType: KClass<out LayerType> = LayerType.Text::class

    @Composable
    override fun Content(
        layer: MarkupLayer,
        canvasWidthPx: Float,
        canvasHeightPx: Float,
    ) {
        val type = layer.type as? LayerType.Text ?: return
        if (type.text.isEmpty()) return

        val density = LocalDensity.current
        val fontSizePx = type.fontSizeRatio * canvasWidthPx
        val fontSize = with(density) { fontSizePx.toDp().toSp() }

        val decorations = type.decorations
        val outlineParams = type.outline?.let {
            OutlineParams(
                color = it.color.toColor(),
                stroke = Stroke(
                    width = it.width * canvasWidthPx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        OutlinedText(
            text = type.text,
            outlineParams = outlineParams,
            color = type.color.toColor(),
            fontSize = fontSize,
            lineHeight = fontSize * type.lineHeight,
            fontFamily = type.font.toUiFont().fontFamily,
            letterSpacing = type.letterSpacingEm.em,
            textDecoration = TextDecoration.combine(
                buildList {
                    if (LayerType.Text.Decoration.Underline in decorations) {
                        add(TextDecoration.Underline)
                    }
                    if (LayerType.Text.Decoration.LineThrough in decorations) {
                        add(TextDecoration.LineThrough)
                    }
                }
            ),
            fontWeight = if (LayerType.Text.Decoration.Bold in decorations) {
                FontWeight.Bold
            } else null,
            fontStyle = if (LayerType.Text.Decoration.Italic in decorations) {
                FontStyle.Italic
            } else null,
            textAlign = when (type.alignment) {
                LayerType.Text.TextAlignment.Left -> TextAlign.Left
                LayerType.Text.TextAlignment.Center -> TextAlign.Center
                LayerType.Text.TextAlignment.Right -> TextAlign.Right
                LayerType.Text.TextAlignment.Justify -> TextAlign.Justify
            },
            style = LocalTextStyle.current,
            modifier = Modifier
                .background(
                    color = type.backgroundColor.toColor(),
                    shape = ShapeDefaults.extraSmall
                )
                .padding(
                    horizontal = with(density) { (fontSizePx / 2).toDp() },
                    vertical = with(density) { (fontSizePx / 3).toDp() }
                )
        )
    }
}
