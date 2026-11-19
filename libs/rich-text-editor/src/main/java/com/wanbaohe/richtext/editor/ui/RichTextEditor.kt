@file:OptIn(ExperimentalFoundationApi::class)
@file:Suppress("FunctionNaming")

/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 * Licensed under the Apache License, Version 2.0
 */

package com.wanbaohe.richtext.editor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import com.wanbaohe.richtext.editor.RichTextEditorState
import com.wanbaohe.richtext.editor.command.rememberRichTextInputTransformation
import com.wanbaohe.richtext.editor.command.richTextEditorKeyboardShortcuts
import com.wanbaohe.richtext.editor.style.RichTextStyleScheme
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun RichTextEditor(
    state: RichTextEditorState,
    scheme: RichTextStyleScheme,
    modifier: Modifier = Modifier,
    behavior: RichTextEditorBehaviorConfig = RichTextEditorBehaviorConfig(),
    visual: RichTextEditorVisualConfig = RichTextEditorVisualConfig(),
) {
    val inputTransformation = rememberRichTextInputTransformation(state, scheme)
    val scrollState = remember(visual.scrollState) { visual.scrollState ?: ScrollState(0) }
    val selectionTracker = visual.selectionAnchorTracker
    var fieldCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    LaunchedEffect(selectionTracker, state.selection, scrollState.value) {
        selectionTracker?.update(state.selection, scrollState.value)
    }
    val textStyle = resolveRichTextEditorTextStyle(visual.textStyle)
    val editorModifier = if (behavior.enabled && !behavior.readOnly) {
        modifier.richTextEditorKeyboardShortcuts(
            state = state,
            scheme = scheme,
            clipboard = behavior.clipboard,
            onUndo = behavior.onUndo ?: state::undo,
            onRedo = behavior.onRedo ?: state::redo,
        )
    } else {
        modifier
    }.onGloballyPositioned { coordinates ->
        fieldCoordinates = coordinates
        selectionTracker?.updateCoordinates(coordinates, state.selection, scrollState.value)
    }
    val longPressModifier = if (behavior.onLongPress != null) {
        Modifier.pointerInput(behavior.onLongPress) {
            awaitEachGesture {
                val down = awaitFirstDown(
                    requireUnconsumed = false,
                    pass = PointerEventPass.Initial,
                )
                val longPressed = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                    var stillPressed = true
                    while (stillPressed) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (event.changes.any {
                                (it.position - down.position).getDistance() > viewConfiguration.touchSlop
                            }
                        ) {
                            return@withTimeoutOrNull false
                        }
                        stillPressed = event.changes.any { it.pressed }
                    }
                    false
                } ?: true
                if (longPressed) {
                    val position = fieldCoordinates?.localToWindow(down.position) ?: down.position
                    behavior.onLongPress?.invoke(Rect(position, Size(1f, 1f)))
                }
            }
        }
    } else {
        Modifier
    }

    Box(modifier = editorModifier.then(longPressModifier)) {
        InlineCodeBackgrounds(
            textLayoutResult = textLayoutResult,
            spanStyles = state.snapshot().spanStyles,
            scheme = scheme,
            // 按字号和基线算背景，而不是整行 32sp 行高，避免圆角块撑到相邻行、文字偏上。
            fontSizePx = with(LocalDensity.current) {
                (textStyle.fontSize.takeIf { it.isSpecified } ?: 16.sp).toPx()
            },
            modifier = Modifier.matchParentSize(),
        )
        BasicTextField(
            state = state.textFieldState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = visual.minHeight)
                // 焦点入口挂在输入框本体上：外部（新建笔记等）可以用它在组合完成后直接聚焦。
                .focusRequester(state.focusRequester),
            enabled = behavior.enabled,
            readOnly = behavior.readOnly,
            inputTransformation = behavior.inputTransformation ?: inputTransformation,
            textStyle = textStyle,
            keyboardOptions = behavior.keyboardOptions,
            onKeyboardAction = behavior.onKeyboardAction,
            lineLimits = behavior.lineLimits,
            onTextLayout = { getResult ->
                getResult()?.let { result ->
                    textLayoutResult = result
                    selectionTracker?.updateLayout(
                        result = result,
                        coordinates = fieldCoordinates,
                        selection = state.selection,
                        scrollOffset = scrollState.value,
                    )
                }
            },
            cursorBrush = visual.cursorBrush ?: SolidColor(MaterialTheme.colorScheme.primary),
            decorator = visual.decorator,
            scrollState = scrollState,
        )
        if (state.text.isEmpty()) visual.placeholder?.invoke()
    }
}

@Composable
private fun InlineCodeBackgrounds(
    textLayoutResult: TextLayoutResult?,
    spanStyles: List<AnnotatedString.Range<SpanStyle>>,
    scheme: RichTextStyleScheme,
    fontSizePx: Float,
    modifier: Modifier = Modifier,
) {
    if (textLayoutResult == null) return
    val density = LocalDensity.current
    val horizontalPadding = with(density) { 2.dp.toPx() }
    val verticalPadding = with(density) { 2.dp.toPx() }
    val cornerRadius = with(density) { 4.dp.toPx() }

    Canvas(modifier) {
        spanStyles.forEach { range ->
            val decoration = scheme.inlineStyles.firstOrNull { style ->
                style.decorationBackground != Color.Unspecified && style.matches(range.item)
            } ?: return@forEach
            drawRoundedSpanBackground(
                layout = textLayoutResult,
                start = range.start,
                end = range.end,
                color = decoration.decorationBackground,
                horizontalPadding = horizontalPadding,
                verticalPadding = verticalPadding,
                fontSizePx = fontSizePx,
                cornerRadius = cornerRadius,
            )
        }
    }
}

private fun DrawScope.drawRoundedSpanBackground(
    layout: TextLayoutResult,
    start: Int,
    end: Int,
    color: Color,
    horizontalPadding: Float,
    verticalPadding: Float,
    fontSizePx: Float,
    cornerRadius: Float,
) {
    val textLength = layout.layoutInput.text.length
    var offset = start.coerceIn(0, textLength)
    val limit = end.coerceIn(offset, textLength)
    while (offset < limit) {
        val line = layout.getLineForOffset(offset)
        val lineStart = layout.getLineStart(line)
        val lineEnd = layout.getLineEnd(line, visibleEnd = true)
        val segmentStart = maxOf(offset, lineStart)
        val segmentEnd = minOf(limit, lineEnd)
        if (segmentEnd > segmentStart) {
            val startBox = layout.getBoundingBox(segmentStart)
            val endBox = layout.getBoundingBox(segmentEnd - 1)
            val left = startBox.left - horizontalPadding
            val right = endBox.right + horizontalPadding
            val baseline = layout.getLineBaseline(line)
            val top = baseline - fontSizePx * 0.75f - verticalPadding
            drawRoundRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(
                    width = (right - left).coerceAtLeast(0f),
                    height = (fontSizePx + verticalPadding * 2f).coerceAtLeast(0f),
                ),
                cornerRadius = CornerRadius(cornerRadius),
            )
        }
        val next = if (segmentEnd > offset) segmentEnd else lineEnd
        if (next <= offset) break
        offset = next
    }
}

@Composable
private fun resolveRichTextEditorTextStyle(
    style: androidx.compose.ui.text.TextStyle,
): androidx.compose.ui.text.TextStyle {
    if (style != androidx.compose.ui.text.TextStyle.Default) return style
    return MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 18.sp,
        lineHeight = 27.sp,
    )
}
