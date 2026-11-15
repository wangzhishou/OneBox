@file:OptIn(ExperimentalFoundationApi::class)

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wanbaohe.richtext.editor.clipboard.RichTextClipboard

data class RichTextEditorBehaviorConfig(
    val enabled: Boolean = true,
    val readOnly: Boolean = false,
    val keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    val onKeyboardAction: KeyboardActionHandler? = null,
    val lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    val inputTransformation: InputTransformation? = null,
    val clipboard: RichTextClipboard? = null,
    val onUndo: (() -> Unit)? = null,
    val onRedo: (() -> Unit)? = null,
)

data class RichTextEditorVisualConfig(
    val textStyle: TextStyle = TextStyle.Default,
    val cursorBrush: Brush? = null,
    val decorator: TextFieldDecorator? = null,
    val scrollState: ScrollState? = null,
    val selectionAnchorTracker: RichTextSelectionAnchorTracker? = null,
    val minHeight: Dp = 0.dp,
    val placeholder: (@Composable () -> Unit)? = null,
)
