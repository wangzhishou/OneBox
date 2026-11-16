/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 * Licensed under the Apache License, Version 2.0
 */

package com.wanbaohe.richtext.editor.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class RichTextSelectionToolbarConfig(
    val preferAbove: Boolean = true,
    val containerColor: Color = Color.Unspecified,
    val contentColor: Color = Color.Unspecified,
    /** 点击浮层外部是否关闭（长按菜单需要，选区长按工具栏保持旧行为）。 */
    val dismissOnClickOutside: Boolean = false,
    val onDismiss: (() -> Unit)? = null,
    /** 选中操作较多时限制浮层宽度并横向滚动。 */
    val maxWidth: Dp = 320.dp,
)
