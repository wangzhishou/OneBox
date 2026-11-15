/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 * Licensed under the Apache License, Version 2.0
 */

package com.wanbaohe.richtext.editor.ui

import androidx.compose.ui.graphics.Color

data class RichTextSelectionToolbarConfig(
    val preferAbove: Boolean = true,
    val containerColor: Color = Color.Unspecified,
    val contentColor: Color = Color.Unspecified,
)
