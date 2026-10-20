@file:OptIn(ExperimentalFoundationApi::class)

package com.wanbaohe.markuplayers.presentation.render

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.FormatClear
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.shifenmiao.common.components.text.MAX_SELECTION_SIZE_SCALE
import com.shifenmiao.common.components.text.MIN_SELECTION_SIZE_SCALE
import com.shifenmiao.common.components.text.RichTextStyleSpan
import com.shifenmiao.common.components.text.SuppressSystemTextSelectionMenu
import com.shifenmiao.common.components.text.TextSelectionAction
import com.shifenmiao.common.components.text.TextSelectionToolbar
import com.shifenmiao.common.components.text.applySelectionStyle
import com.shifenmiao.common.components.text.clearSelectionStyles
import com.shifenmiao.common.components.text.copySelection
import com.shifenmiao.common.components.text.pasteAtSelection
import com.shifenmiao.common.components.text.toRichTextStyleSpan
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.ContentPaste
import com.t8rin.imagetoolbox.core.settings.presentation.model.toUiFont
import com.wanbaohe.core.ui.theme.toColor
import com.wanbaohe.core.ui.widget.color_picker.ColorSelectionRow
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.effectiveColorAt
import com.wanbaohe.markuplayers.domain.model.effectiveSizeScaleAt
import com.wanbaohe.markuplayers.domain.model.isRangeEffectivelyBold
import com.wanbaohe.markuplayers.domain.model.isRangeEffectivelyItalic
import kotlinx.coroutines.flow.drop

/**
 * 文字层就地编辑(对齐图文卡片 InPlaceTextEditor):原位 BasicTextField,
 * 字号/折行/背景与 [TextLayerPreviewRenderer] 同构。输入实时写回编辑会话,
 * 键盘完成键 / [onCommit] 退出(提交由调用方接 commitLayerEditSession)。
 *
 * 选中文字后:
 * - 屏蔽系统 Copy/Paste 菜单(自绘工具条替代);
 * - 输入框上方弹 [TextSelectionToolbar]:B / I / 颜色 / 字号 -+ / 复制 / 粘贴 / 清除;
 * - 所有操作只作用于当前选区,写 Compose 样式层,再经 [onTextChange] 回写到图层;
 * - Popup 非 focusable,点按钮不会丢焦点、选区不会坍缩。
 */
@Composable
fun InPlaceTextLayerEditor(
    type: LayerType.Text,
    canvasWidthPx: Float,
    onTextChange: (String, List<RichTextStyleSpan>) -> Unit,
    onCommit: () -> Unit,
    selectionToolbarVisible: Boolean = true,
    onOpenStylePanel: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboard = LocalClipboardManager.current
    val fontSizePx = type.fontSizeRatio * canvasWidthPx
    val textStyle = textLayerComposeStyle(type, canvasWidthPx)
    // 编辑会话内 TextFieldState 是唯一事实源(文字 + 样式层 + 选区);
    // 模型只做镜像,选区变化由 snapshot 感知,工具条据此自动出现/消失
    val state = rememberTextFieldState(
        initialText = type.text,
        initialSelection = TextRange(type.text.length)
    )
    var anchorBounds by remember { mutableStateOf(Rect.Zero) }
    var showColors by remember { mutableStateOf(false) }
    val selection = state.selection
    val hasSelection = !selection.collapsed

    fun sync() {
        val spans = state.textStyles
            .getSpanStyles(TextRange(0, state.text.length))
            .mapNotNull { it.toRichTextStyleSpan() }
        onTextChange(state.text.toString(), spans)
    }

    LaunchedEffect(state) {
        // 灌回模型已有的局部样式(钳制到当前内容长度,防御脏区间)
        type.styleSpans.forEach { span ->
            span.clamped(type.text.length)?.let { clamped ->
                state.edit { addStyle(clamped.toSpanStyle(), clamped.start, clamped.end) }
            }
        }
        focusRequester.requestFocus()
        keyboardController?.show()
        // 内容变化(键入/删除/粘贴)时回写;样式变化由工具条应用后显式调 sync()
        snapshotFlow { state.text.toString() }.drop(1).collect { sync() }
    }
    LaunchedEffect(hasSelection) {
        if (!hasSelection) showColors = false
    }

    val widthModifier = if (type.widthManuallySet && type.widthRatio != null) {
        Modifier.requiredWidth(with(density) { (type.widthRatio * canvasWidthPx).toDp() })
    } else {
        Modifier.widthIn(max = with(density) { ((type.widthRatio ?: 1f) * canvasWidthPx).toDp() })
    }

    SuppressSystemTextSelectionMenu {
        BasicTextField(
            state = state,
            textStyle = textStyle,
            cursorBrush = SolidColor(type.color.toColor()),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            onKeyboardAction = {
                sync()
                onCommit()
            },
            modifier = modifier
                .then(widthModifier)
                .background(
                    color = type.backgroundColor.toColor(),
                    shape = ShapeDefaults.extraSmall
                )
                // 与预览侧一致:fontSize/6 横向、fontSize/10 纵向
                .padding(
                    horizontal = with(density) { (fontSizePx / 6).toDp() },
                    vertical = with(density) { (fontSizePx / 10).toDp() }
                )
                .focusRequester(focusRequester)
                .onGloballyPositioned { anchorBounds = it.boundsInWindow() }
        )
    }

    if (hasSelection && selectionToolbarVisible) {
        val start = selection.min
        val end = selection.max
        val bold = type.isRangeEffectivelyBold(start, end)
        val italic = type.isRangeEffectivelyItalic(start, end)
        val currentColor = Color(type.effectiveColorAt(start, end))
        val currentSize = type.effectiveSizeScaleAt(start, end)

        val stepSize: (Boolean) -> Unit = { increase ->
            val next = (if (increase) currentSize * 1.15f else currentSize / 1.15f)
                .coerceIn(MIN_SELECTION_SIZE_SCALE, MAX_SELECTION_SIZE_SCALE)
            state.applySelectionStyle(
                style = SpanStyle(fontSize = next.em),
                onChanged = ::sync
            )
        }
        val actions = listOf(
            TextSelectionAction(
                key = "bold",
                label = stringResource(R.string.markup_text_bold),
                glyph = "B",
                selected = bold,
                onClick = {
                    state.applySelectionStyle(
                        style = SpanStyle(
                            fontWeight = if (bold) FontWeight.Normal else FontWeight.Bold
                        ),
                        onChanged = ::sync
                    )
                }
            ),
            TextSelectionAction(
                key = "italic",
                label = stringResource(R.string.markup_text_italic),
                glyph = "I",
                selected = italic,
                onClick = {
                    state.applySelectionStyle(
                        style = SpanStyle(
                            fontStyle = if (italic) FontStyle.Normal else FontStyle.Italic
                        ),
                        onChanged = ::sync
                    )
                }
            ),
            TextSelectionAction(
                key = "color",
                label = stringResource(R.string.markup_text_color),
                swatch = currentColor,
                selected = showColors,
                onClick = { showColors = !showColors }
            ),
            TextSelectionAction(
                key = "size_down",
                label = stringResource(R.string.markup_text_size_decrease),
                glyph = "A-",
                onClick = { stepSize(false) }
            ),
            TextSelectionAction(
                key = "size_up",
                label = stringResource(R.string.markup_text_size_increase),
                glyph = "A+",
                onClick = { stepSize(true) }
            ),
            TextSelectionAction(
                key = "copy",
                label = stringResource(android.R.string.copy),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                onClick = { state.copySelection(clipboard) }
            ),
            TextSelectionAction(
                key = "paste",
                label = stringResource(android.R.string.paste),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentPaste,
                onClick = { state.pasteAtSelection(clipboard, ::sync) }
            ),
            TextSelectionAction(
                key = "clear",
                label = stringResource(R.string.markup_text_clear_format),
                icon = MaterialIcons.Outlined.FormatClear,
                onClick = { state.clearSelectionStyles(::sync) }
            ),
            TextSelectionAction(
                key = "more",
                label = stringResource(R.string.markup_tool_text_style),
                icon = MaterialIcons.Outlined.Tune,
                onClick = onOpenStylePanel
            )
        )

        TextSelectionToolbar(
            anchorBounds = anchorBounds,
            actions = actions,
            footer = if (showColors) {
                {
                    ColorSelectionRow(
                        value = currentColor,
                        onValueChange = { color ->
                            state.applySelectionStyle(
                                style = SpanStyle(color = color),
                                onChanged = ::sync
                            )
                            showColors = false
                        },
                        allowAlpha = false
                    )
                }
            } else null
        )
    }
}

/**
 * 文字层 Compose 样式:预览 OutlinedText 与就地编辑 BasicTextField 共用,
 * 保证编辑态与渲染态观感一致(与导出侧 TextLayerExportRenderer 同构)。
 */
@Composable
fun textLayerComposeStyle(
    type: LayerType.Text,
    canvasWidthPx: Float,
): androidx.compose.ui.text.TextStyle {
    val density = LocalDensity.current
    val fontSizePx = type.fontSizeRatio * canvasWidthPx
    val fontSize = with(density) { fontSizePx.toDp().toSp() }
    val decorations = type.decorations
    return androidx.compose.ui.text.TextStyle(
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
