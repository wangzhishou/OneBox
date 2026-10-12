package com.wanbaohe.markuplayers.presentation.render

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.em
import com.t8rin.imagetoolbox.core.settings.presentation.model.toUiFont
import com.t8rin.imagetoolbox.core.ui.theme.toColor
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.markuplayers.domain.model.LayerType

/**
 * 文字层就地编辑(对齐图文卡片 InPlaceTextEditor):原位 BasicTextField,
 * 字号/折行/背景与 [TextLayerPreviewRenderer] 同构。输入实时写回编辑会话,
 * 键盘完成键 / [onCommit] 退出(提交由调用方接 commitLayerEditSession)。
 */
@Composable
fun InPlaceTextLayerEditor(
    type: LayerType.Text,
    canvasWidthPx: Float,
    onTextChange: (String) -> Unit,
    onCommit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val fontSizePx = type.fontSizeRatio * canvasWidthPx
    val textStyle = textLayerComposeStyle(type, canvasWidthPx)

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    BasicTextField(
        value = type.text,
        onValueChange = onTextChange,
        textStyle = textStyle,
        cursorBrush = SolidColor(type.color.toColor()),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onCommit() }),
        modifier = modifier
            .then(
                type.widthRatio?.let {
                    Modifier.requiredWidth(with(density) { (it * canvasWidthPx).toDp() })
                } ?: Modifier
            )
            .background(
                color = type.backgroundColor.toColor(),
                shape = ShapeDefaults.extraSmall
            )
            .padding(
                horizontal = with(density) { (fontSizePx / 2).toDp() },
                vertical = with(density) { (fontSizePx / 3).toDp() }
            )
            .focusRequester(focusRequester)
    )
}

/**
 * 文字层 Compose 样式:预览 OutlinedText 与就地编辑 BasicTextField 共用,
 * 保证编辑态与渲染态观感一致(与导出侧 TextLayerExportRenderer 同构)。
 */
@Composable
fun textLayerComposeStyle(
    type: LayerType.Text,
    canvasWidthPx: Float,
): TextStyle {
    val density = LocalDensity.current
    val fontSizePx = type.fontSizeRatio * canvasWidthPx
    val fontSize = with(density) { fontSizePx.toDp().toSp() }
    val decorations = type.decorations
    return TextStyle(
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
        } else FontWeight.Normal,
        fontStyle = if (LayerType.Text.Decoration.Italic in decorations) {
            FontStyle.Italic
        } else FontStyle.Normal,
        textAlign = when (type.alignment) {
            LayerType.Text.TextAlignment.Left -> TextAlign.Left
            LayerType.Text.TextAlignment.Center -> TextAlign.Center
            LayerType.Text.TextAlignment.Right -> TextAlign.Right
            LayerType.Text.TextAlignment.Justify -> TextAlign.Justify
        }
    )
}

/** 描边参数:预览 OutlinedText 与导出侧共用换算 */
@Composable
fun textLayerOutlineStroke(
    type: LayerType.Text,
    canvasWidthPx: Float,
): Stroke? {
    val outline = type.outline ?: return null
    return Stroke(
        width = outline.width * canvasWidthPx,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    )
}
