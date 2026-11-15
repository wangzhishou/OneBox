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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle

data class RichTextEditorBehaviorConfig(
    val enabled: Boolean = true,
    val readOnly: Boolean = false,
    val keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    val onKeyboardAction: KeyboardActionHandler? = null,
    val lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    val inputTransformation: InputTransformation? = null,
)

data class RichTextEditorVisualConfig(
    val textStyle: TextStyle = TextStyle.Default,
    val cursorBrush: Brush? = null,
    val decorator: TextFieldDecorator? = null,
    val scrollState: ScrollState? = null,
)
