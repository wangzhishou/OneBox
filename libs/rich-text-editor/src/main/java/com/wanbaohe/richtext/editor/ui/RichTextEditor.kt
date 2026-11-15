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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.sp
import com.wanbaohe.richtext.editor.RichTextEditorState
import com.wanbaohe.richtext.editor.command.rememberRichTextListInputTransformation
import com.wanbaohe.richtext.editor.style.RichTextStyleScheme

@Composable
fun RichTextEditor(
    state: RichTextEditorState,
    scheme: RichTextStyleScheme,
    modifier: Modifier = Modifier,
    behavior: RichTextEditorBehaviorConfig = RichTextEditorBehaviorConfig(),
    visual: RichTextEditorVisualConfig = RichTextEditorVisualConfig(),
) {
    val listInputTransformation = rememberRichTextListInputTransformation(scheme)
    val scrollState = androidx.compose.runtime.remember(visual.scrollState) {
        visual.scrollState ?: ScrollState(0)
    }
    val textStyle = if (visual.textStyle == androidx.compose.ui.text.TextStyle.Default) {
        MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp,
            lineHeight = 27.sp,
        )
    } else {
        visual.textStyle
    }
    BasicTextField(
        state = state.textFieldState,
        modifier = modifier,
        enabled = behavior.enabled,
        readOnly = behavior.readOnly,
        inputTransformation = behavior.inputTransformation ?: listInputTransformation,
        textStyle = textStyle,
        keyboardOptions = behavior.keyboardOptions,
        onKeyboardAction = behavior.onKeyboardAction,
        lineLimits = behavior.lineLimits,
        cursorBrush = visual.cursorBrush ?: SolidColor(MaterialTheme.colorScheme.primary),
        decorator = visual.decorator,
        scrollState = scrollState,
    )
}
