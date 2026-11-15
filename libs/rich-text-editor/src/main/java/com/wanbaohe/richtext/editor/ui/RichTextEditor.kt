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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.sp
import com.wanbaohe.richtext.editor.RichTextEditorState
import com.wanbaohe.richtext.editor.command.rememberRichTextInputTransformation
import com.wanbaohe.richtext.editor.command.richTextEditorKeyboardShortcuts
import com.wanbaohe.richtext.editor.style.RichTextStyleScheme

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
    Box(modifier = editorModifier) {
        BasicTextField(
            state = state.textFieldState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = visual.minHeight),
            enabled = behavior.enabled,
            readOnly = behavior.readOnly,
            inputTransformation = behavior.inputTransformation ?: inputTransformation,
            textStyle = textStyle,
            keyboardOptions = behavior.keyboardOptions,
            onKeyboardAction = behavior.onKeyboardAction,
            lineLimits = behavior.lineLimits,
            onTextLayout = { getResult ->
                getResult()?.let { result ->
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
