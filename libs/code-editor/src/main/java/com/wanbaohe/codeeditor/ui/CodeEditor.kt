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

@file:Suppress("FunctionNaming")

package com.wanbaohe.codeeditor.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrm.codehigh.renderer.CodeColors
import com.wanbaohe.codeeditor.CodeEditorSearchStyle
import com.wanbaohe.codeeditor.CodeFold
import com.wanbaohe.codeeditor.CodeEditorState
import com.wanbaohe.codeeditor.CodeEditorVisualTransformation
import com.wanbaohe.codeeditor.CodeHighlighter
import com.wanbaohe.codeeditor.CodeText
import com.wanbaohe.codeeditor.FoldProjection
import com.wanbaohe.codeeditor.findCodeFolds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/** 编辑区外观：排版、语法配色、光标与查找/当前行的底色。 */
@Immutable
data class CodeEditorTheme(
    val textStyle: TextStyle,
    val colors: CodeColors,
    val cursorColor: Color = Color.Unspecified,
    val activeLineHighlight: Color = Color.Unspecified,
    val matchHighlight: Color = Color.Unspecified,
    val activeMatchHighlight: Color = Color.Unspecified,
    val activeMatchContent: Color = Color.Unspecified,
)

/** 编辑区回调：选区浮条锚点与长按。 */
data class CodeEditorCallbacks(
    val onSelectionAnchorChange: (Rect) -> Unit = {},
    val onLongPress: ((Rect) -> Unit)? = null,
    val foldActionLabel: ((line: Int, folded: Boolean) -> String)? = null,
)

/**
 * 纯 Kotlin 代码编辑控件。
 *
 * 结构（从左到右）：行号槽 + 可滚动文本区；纵向一个 ScrollState，关闭软换行时再套一层横向滚动。
 * 用值版 [BasicTextField] + [androidx.compose.ui.text.input.VisualTransformation] 做语法着色——
 * 状态版 API 只支持持久化 span 和装饰器，没法把「派生出来的」token 颜色贴上去。
 */
@Suppress("LongParameterList", "LongMethod") // 公开控件 API：参数都带默认值；布局代码本身就是一段完整结构
@Composable
fun CodeEditor(
    state: CodeEditorState,
    theme: CodeEditorTheme,
    modifier: Modifier = Modifier,
    placeholder: (@Composable () -> Unit)? = null,
    readOnly: Boolean = false,
    callbacks: CodeEditorCallbacks = CodeEditorCallbacks(),
) {
    val highlighted = rememberHighlightedCode(state = state, theme = theme)
    val projection = remember(state.foldedRanges) { FoldProjection(state.foldedRanges) }
    val transformation = remember(highlighted, projection) {
        val highlighter = CodeEditorVisualTransformation { highlighted }
        VisualTransformation { text ->
            val styled = highlighter.filter(text)
            if (projection.isEmpty) styled
            else TransformedText(projection.project(styled.text), projection)
        }
    }
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var fieldCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val lineStarts = remember(state.contentVersion) { CodeText.lineStarts(state.text) }
    val folds = rememberCodeFolds(state, lineStarts)
    val gutterLines = remember(lineStarts, projection, folds) {
        lineStarts.mapIndexedNotNull { index, offset ->
            if (projection.hidesLineStart(offset)) null
            else CodeEditorGutterLine(index + 1, projection.originalToTransformed(offset), folds[index])
        }
    }
    val gutterStyle = rememberCodeEditorGutterStyle(theme)
    val foldColumn = if (folds.isEmpty()) 0.dp else FOLD_COLUMN_WIDTH
    val gutterWidth = rememberCodeEditorGutterWidth(lineStarts.size, gutterStyle.activeNumberStyle) + foldColumn
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()
    val shortcutsModifier = Modifier.codeEditorKeyboardShortcuts(state)
    val longPressModifier = rememberLongPressModifier(fieldCoordinates, callbacks.onLongPress)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val viewport = CodeEditorViewport(
            verticalScroll = verticalScroll,
            horizontalScroll = horizontalScroll,
            viewportHeight = maxHeight,
            contentWidth = (maxWidth - gutterWidth - CONTENT_HORIZONTAL_PADDING * 2)
                .coerceAtLeast(1.dp),
        )
        Row(modifier = Modifier.fillMaxSize().padding(vertical = CONTENT_VERTICAL_PADDING)) {
            CodeEditorLineNumbers(
                lines = gutterLines,
                layoutResult = layoutResult,
                activeOffset = projection.originalToTransformed(state.selection.max),
                scrollState = verticalScroll,
                style = gutterStyle,
                foldColumn = foldColumn,
                foldedRanges = state.foldedRanges,
                onToggleFold = state::toggleFold,
                foldActionLabel = callbacks.foldActionLabel,
                modifier = Modifier
                    .width(gutterWidth)
                    .fillMaxHeight(),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = CONTENT_HORIZONTAL_PADDING)
                    .clipToBounds(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScroll),
                ) {
                    val scrollModifier = if (state.wrapLines) {
                        Modifier.fillMaxWidth()
                    } else {
                        Modifier.horizontalScroll(horizontalScroll)
                    }
                    Box(
                        modifier = scrollModifier.drawBehind {
                            drawActiveLine(
                                layoutResult,
                                projection.originalToTransformed(state.selection.max),
                                theme.activeLineHighlight,
                            )
                        },
                    ) {
                        BasicTextField(
                            value = state.value,
                            onValueChange = state::onValueChange,
                            modifier = Modifier
                                .then(
                                    if (state.wrapLines) {
                                        Modifier.fillMaxWidth()
                                    } else {
                                        Modifier.widthIn(min = viewport.contentWidth)
                                    }
                                )
                                .heightIn(min = viewport.viewportHeight)
                                .onGloballyPositioned { fieldCoordinates = it }
                                .then(shortcutsModifier)
                                .then(longPressModifier),
                            enabled = true,
                            readOnly = readOnly,
                            textStyle = theme.textStyle,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.None,
                                autoCorrectEnabled = false,
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Default,
                            ),
                            keyboardActions = KeyboardActions(),
                            singleLine = false,
                            maxLines = Int.MAX_VALUE,
                            visualTransformation = transformation,
                            onTextLayout = { result -> layoutResult = result },
                            cursorBrush = SolidColor(
                                theme.cursorColor.takeIf { it != Color.Unspecified } ?: theme.colors.text
                            ),
                        )
                        if (state.text.isEmpty() && placeholder != null) {
                            placeholder()
                        }
                    }
                }
            }
        }
        CodeEditorScrollEffects(
            state = state,
            theme = theme,
            viewport = viewport,
            projection = projection,
            layoutResult = layoutResult,
            fieldCoordinates = fieldCoordinates,
            onSelectionAnchorChange = callbacks.onSelectionAnchorChange,
        )
    }
}

@Composable
private fun rememberCodeFolds(state: CodeEditorState, lineStarts: List<Int>): Map<Int, CodeFold> {
    val text = state.text
    val language = state.language
    var folds by remember(text, language) { mutableStateOf(emptyMap<Int, CodeFold>()) }
    LaunchedEffect(text, language) {
        if (text.length <= MAX_FOLD_CHARS && lineStarts.size > 1) {
            folds = withContext(Dispatchers.Default) { findCodeFolds(text, language, lineStarts) }
        }
    }
    return folds
}

/** 视口的滚动状态与尺寸，供光标跟随/查找定位复用。 */
private class CodeEditorViewport(
    val verticalScroll: ScrollState,
    val horizontalScroll: ScrollState,
    val viewportHeight: Dp,
    val contentWidth: Dp,
)

/**
 * 三类滚动副作用：光标跟随、查找命中定位、选区浮条锚点。
 * 从主布局里抽出来，避免正文布局被十几个状态读取淹没。
 */
@Suppress("LongParameterList", "LongMethod", "CyclomaticComplexMethod") // 三类滚动副作用集中接线
@Composable
private fun CodeEditorScrollEffects(
    state: CodeEditorState,
    theme: CodeEditorTheme,
    viewport: CodeEditorViewport,
    projection: FoldProjection,
    layoutResult: TextLayoutResult?,
    fieldCoordinates: LayoutCoordinates?,
    onSelectionAnchorChange: (Rect) -> Unit,
) {
    val density = LocalDensity.current
    val lineHeightSp = theme.textStyle.lineHeight.takeIf { it.isSp }
        ?: (theme.textStyle.fontSize.value * FALLBACK_LINE_HEIGHT_RATIO).sp
    val verticalScroll = viewport.verticalScroll
    val horizontalScroll = viewport.horizontalScroll
    var handledReveal by remember { mutableLongStateOf(0L) }

    // 光标跟随：选区变化时把光标滚进视口（用户主动滚动不触发）。
    LaunchedEffect(state.selection, layoutResult, state.wrapLines) {
        val layout = layoutResult ?: return@LaunchedEffect
        val textLength = layout.layoutInput.text.length
        val caret = layout.getCursorRect(projection.originalToTransformed(state.selection.max).coerceIn(0, textLength))
        val viewportHeightPx = with(density) { viewport.viewportHeight.toPx() }
        val lineHeightPx = with(density) { lineHeightSp.toPx() }
        when {
            caret.bottom > verticalScroll.value + viewportHeightPx ->
                verticalScroll.scrollTo((caret.bottom - viewportHeightPx + lineHeightPx).toInt())

            caret.top < verticalScroll.value ->
                verticalScroll.scrollTo((caret.top - lineHeightPx).toInt().coerceAtLeast(0))
        }
        if (!state.wrapLines) {
            val viewportWidthPx = with(density) { viewport.contentWidth.toPx() }
            when {
                caret.right > horizontalScroll.value + viewportWidthPx ->
                    horizontalScroll.scrollTo(
                        (caret.right - viewportWidthPx + lineHeightPx).toInt().coerceAtLeast(0)
                    )

                caret.left < horizontalScroll.value ->
                    horizontalScroll.scrollTo((caret.left - lineHeightPx).toInt().coerceAtLeast(0))
            }
        }
    }

    // 查找命中定位：把目标行滚到视口上三分之一处。
    LaunchedEffect(state.reveal?.requestId, layoutResult) {
        val request = state.reveal ?: return@LaunchedEffect
        if (request.requestId <= handledReveal) return@LaunchedEffect
        val layout = layoutResult ?: return@LaunchedEffect
        val offset = projection.originalToTransformed(request.offset)
        if (offset > layout.layoutInput.text.length) return@LaunchedEffect
        handledReveal = request.requestId
        val line = layout.getLineForOffset(offset)
        val viewportHeightPx = with(density) { viewport.viewportHeight.toPx() }
        val target = layout.getLineTop(line) - viewportHeightPx / REVEAL_TOP_DIVISOR
        verticalScroll.animateScrollTo(target.toInt().coerceAtLeast(0))
    }

    // 自绘选区工具条的锚点（窗口坐标）。
    LaunchedEffect(
        state.selection,
        state.contentVersion,
        projection,
        layoutResult,
        verticalScroll.value,
        horizontalScroll.value,
    ) {
        val layout = layoutResult ?: return@LaunchedEffect
        val coordinates = fieldCoordinates?.takeIf { it.isAttached } ?: return@LaunchedEffect
        val selection = TextRange(
            projection.originalToTransformed(state.selection.min),
            projection.originalToTransformed(state.selection.max),
        )
        val textLength = layout.layoutInput.text.length
        if (selection.collapsed || selection.max > textLength) {
            onSelectionAnchorChange(Rect.Zero)
            return@LaunchedEffect
        }
        val bounds = layout.getPathForRange(selection.min, selection.max).getBounds()
        val origin = coordinates.localToWindow(Offset.Zero)
        onSelectionAnchorChange(bounds.translate(origin.x, origin.y))
    }
}

/**
 * 语法高亮在正文变化后异步重算；高亮结果落后于正文时由
 * [CodeEditorVisualTransformation] 回退成纯色，不会把旧样式贴到新文本上。
 */
@Composable
private fun rememberHighlightedCode(
    state: CodeEditorState,
    theme: CodeEditorTheme,
): AnnotatedString? {
    val highlighter = remember(theme.colors) { CodeHighlighter(theme.colors) }
    var highlighted by remember { mutableStateOf<AnnotatedString?>(null) }
    val searchStyle = remember(theme) {
        CodeEditorSearchStyle(
            matchHighlight = theme.matchHighlight.takeIf { it != Color.Unspecified },
            activeMatchHighlight = theme.activeMatchHighlight.takeIf { it != Color.Unspecified },
            activeMatchContent = theme.activeMatchContent.takeIf { it != Color.Unspecified },
        )
    }
    LaunchedEffect(
        state.contentVersion,
        state.language,
        state.findMatches,
        state.activeMatchIndex,
        searchStyle,
    ) {
        val text = state.text
        val language = state.language
        val matches = state.findMatches
        val activeMatch = state.activeMatch
        highlighted = when {
            text.length > MAX_HIGHLIGHT_CHARS -> AnnotatedString(text)
            text.length > ASYNC_HIGHLIGHT_CHARS -> withContext(Dispatchers.Default) {
                highlighter.highlight(text, language, matches, activeMatch, searchStyle)
            }

            else -> highlighter.highlight(text, language, matches, activeMatch, searchStyle)
        }
    }
    return highlighted
}

@Composable
private fun rememberCodeEditorGutterStyle(theme: CodeEditorTheme): CodeEditorGutterStyle {
    return remember(theme) {
        val numberStyle = theme.textStyle.copy(
            color = theme.colors.lineNumber.let { it.copy(alpha = it.alpha * GUTTER_NUMBER_OPACITY) },
            fontSize = (theme.textStyle.fontSize.value * GUTTER_FONT_SCALE).sp,
            fontWeight = FontWeight.Normal,
            lineHeight = theme.textStyle.lineHeight,
        )
        CodeEditorGutterStyle(
            numberStyle = numberStyle,
            activeNumberStyle = numberStyle.copy(
                color = theme.colors.keyword,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun rememberLongPressModifier(
    fieldCoordinates: LayoutCoordinates?,
    onLongPress: ((Rect) -> Unit)?,
): Modifier {
    if (onLongPress == null) return Modifier
    return Modifier.pointerInput(onLongPress, fieldCoordinates) {
        awaitEachGesture {
            val down = awaitFirstDown(
                requireUnconsumed = false,
                pass = PointerEventPass.Initial,
            )
            val longPressed = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                var stillPressed = true
                while (stillPressed) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val moved = event.changes.any {
                        (it.position - down.position).getDistance() > viewConfiguration.touchSlop
                    }
                    if (moved) return@withTimeoutOrNull false
                    stillPressed = event.changes.any { it.pressed }
                }
                false
            } ?: true
            if (longPressed) {
                val position = fieldCoordinates?.localToWindow(down.position) ?: down.position
                onLongPress(Rect(position, Size(1f, 1f)))
            }
        }
    }
}

private fun DrawScope.drawActiveLine(
    layoutResult: TextLayoutResult?,
    activeOffset: Int,
    color: Color,
) {
    if (color == Color.Unspecified || layoutResult == null) return
    val textLength = layoutResult.layoutInput.text.length
    val line = layoutResult.getLineForOffset(activeOffset.coerceIn(0, textLength))
    val top = layoutResult.getLineTop(line)
    val bottom = layoutResult.getLineBottom(line)
    if (bottom > top) {
        drawRect(
            color = color,
            topLeft = Offset(0f, top),
            size = Size(width = size.width, height = bottom - top),
        )
    }
}

private const val FALLBACK_LINE_HEIGHT_RATIO = 1.4f
private const val REVEAL_TOP_DIVISOR = 3f
private const val GUTTER_FONT_SCALE = 0.75f
private const val GUTTER_NUMBER_OPACITY = 0.78f
private const val ASYNC_HIGHLIGHT_CHARS = 8_000
private const val MAX_HIGHLIGHT_CHARS = 200_000
private const val MAX_FOLD_CHARS = 1_000_000
private val FOLD_COLUMN_WIDTH = 20.dp
private val CONTENT_HORIZONTAL_PADDING = 8.dp
private val CONTENT_VERTICAL_PADDING = 8.dp
