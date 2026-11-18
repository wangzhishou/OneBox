@file:OptIn(ExperimentalFoundationApi::class)

/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.markuplayers.presentation.render

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.shifenmiao.common.components.text.ColorSheet
import com.shifenmiao.common.components.text.MAX_SELECTION_SIZE_SCALE
import com.shifenmiao.common.components.text.MIN_SELECTION_SIZE_SCALE
import com.shifenmiao.common.components.text.QuickColorRow
import com.shifenmiao.common.components.text.RichTextStyleSpan
import com.shifenmiao.common.components.text.SuppressSystemTextSelectionMenu
import com.shifenmiao.common.components.text.TextSelectionAction
import com.shifenmiao.common.components.text.TextSelectionToolbar
import com.shifenmiao.common.components.text.applySelectionStyle
import com.shifenmiao.common.components.text.clearSelectionStyles
import com.shifenmiao.common.components.text.copySelection
import com.shifenmiao.common.components.text.pasteAtSelection
import com.shifenmiao.common.components.text.rememberSelectionColorPickerState
import com.shifenmiao.common.components.text.toRichTextStyleSpan
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.ContentPaste
import com.t8rin.imagetoolbox.core.settings.presentation.model.toUiFont
import com.wanbaohe.core.ui.theme.toColor
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.effectiveColorAt
import com.wanbaohe.markuplayers.domain.model.effectiveSizeScaleAt
import com.wanbaohe.markuplayers.domain.model.isRangeEffectivelyBold
import com.wanbaohe.markuplayers.domain.model.isRangeEffectivelyItalic
import kotlinx.coroutines.flow.drop
import kotlin.math.abs

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
    /** 编辑框宽度变化量(px):宿主据此按对齐方式钉住锚点边,避免输入时文字左右飘 */
    onContentWidthChanged: (Float) -> Unit = {},
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
    val selection = state.selection
    val hasSelection = !selection.collapsed
    // 快选色行 + 全屏取色面板:面板是 ModalSheet(decorView 图层),工具条是独立窗口,
    // 两者同时存在时窗口会盖住面板,所以取色时工具条收起、面板由工具条外面承接
    val colorPicker = rememberSelectionColorPickerState(state)

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

    // 编辑框宽度按内容自适应:BasicTextField 的测量结果恒等于 maxWidth 约束,
    // 所以原来的 widthIn(上限) 实际等于整行铺满 —— 光标后面拖出一大片空白,
    // 而且和提交后的预览(Text 自己贴合内容)宽度不一致,进出编辑会跳一下。
    // 这里量出内容宽度给出精确 width,让编辑框和预览同宽。
    val horizontalPaddingPx = fontSizePx / 6f
    val fieldLimitPx = ((type.widthRatio ?: 1f) * canvasWidthPx).coerceAtLeast(fontSizePx * 2f)
    val contentWidthPx = measureTextFieldContentWidth(
        measurer = rememberTextMeasurer(),
        state = state,
        style = textStyle,
        maxWidthPx = (fieldLimitPx - horizontalPaddingPx * 2f).coerceAtLeast(1f),
    )
    // 空文本也要留出能点到光标的宽度
    val minFieldWidthPx = fontSizePx * 0.9f
    val fieldWidthPx = if (type.widthManuallySet && type.widthRatio != null) {
        type.widthRatio * canvasWidthPx
    } else {
        (contentWidthPx + horizontalPaddingPx * 2f).coerceIn(minFieldWidthPx, fieldLimitPx)
    }
    val widthModifier = Modifier.width(with(density) { fieldWidthPx.toDp() })

    // 宽度变了就通知宿主(首帧不通知:那是从预览切进来的正常尺寸)
    var lastFieldWidthPx by remember(state) { mutableStateOf<Float?>(null) }
    LaunchedEffect(fieldWidthPx) {
        val previous = lastFieldWidthPx
        lastFieldWidthPx = fieldWidthPx
        if (previous != null && abs(fieldWidthPx - previous) > 0.5f) {
            onContentWidthChanged(fieldWidthPx - previous)
        }
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

    // 取色面板开着时工具条必须让位:它是独立窗口,会压在面板上面
    if (hasSelection && selectionToolbarVisible && !colorPicker.isSheetVisible) {
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
                selected = colorPicker.quickColorRowVisible,
                onClick = { colorPicker.toggleQuickColorRow() }
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
            footer = if (colorPicker.quickColorRowVisible) {
                {
                    colorPicker.QuickColorRow(
                        value = currentColor,
                        onColorChange = { color ->
                            state.applySelectionStyle(
                                style = SpanStyle(color = color),
                                onChanged = ::sync
                            )
                        },
                        allowAlpha = false
                    )
                }
            } else null
        )
    }

    // 全屏取色面板挂在工具条外面:面板显示期间工具条收起,面板才不会被独立窗口压住
    colorPicker.ColorSheet(
        onColorPicked = { color ->
            state.applySelectionStyle(
                style = SpanStyle(color = color),
                onChanged = ::sync
            )
        },
        allowAlpha = false
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

/**
 * 量出编辑框内容需要的宽度(取最宽一行的行宽)。
 *
 * 不能用 measure(...).size.width:它等于约束上限(段落按约束铺满),
 * 拿它当宽度就成了"永远整行"。行宽才是内容的真实宽度。
 */
private fun measureTextFieldContentWidth(
    measurer: TextMeasurer,
    state: TextFieldState,
    style: androidx.compose.ui.text.TextStyle,
    maxWidthPx: Float,
): Float {
    val text = state.text.toString()
    if (text.isEmpty()) return 0f
    val spans = state.textStyles.getSpanStyles(TextRange(0, text.length))
    val result = measurer.measure(
        text = AnnotatedString(text, spanStyles = spans),
        style = style,
        constraints = Constraints(maxWidth = maxWidthPx.toInt().coerceAtLeast(1)),
    )
    if (result.lineCount <= 0) return 0f
    return (0 until result.lineCount).maxOf { result.multiParagraph.getLineWidth(it) }
}
