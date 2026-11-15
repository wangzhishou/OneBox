@file:Suppress("FunctionNaming")

/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 * Licensed under the Apache License, Version 2.0
 */

package com.wanbaohe.richtext.editor.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt

@Composable
fun RichTextSelectionToolbar(
    anchorBounds: Rect,
    actions: List<RichTextSelectionAction>,
    modifier: Modifier = Modifier,
    config: RichTextSelectionToolbarConfig = RichTextSelectionToolbarConfig(),
) {
    if (actions.isEmpty() || anchorBounds == Rect.Zero) return
    val positionProvider = remember(anchorBounds, config.preferAbove) {
        RichTextSelectionPopupPositionProvider(anchorBounds, config.preferAbove)
    }
    val containerColor = config.containerColor.takeIf { it != Color.Unspecified }
        ?: MaterialTheme.colorScheme.primaryContainer
    val contentColor = config.contentColor.takeIf { it != Color.Unspecified }
        ?: MaterialTheme.colorScheme.onPrimaryContainer
    Popup(
        popupPositionProvider = positionProvider,
        properties = PopupProperties(
            focusable = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            clippingEnabled = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(18.dp),
            color = containerColor,
            contentColor = contentColor,
            shadowElevation = 6.dp,
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                actions.forEach { action ->
                    RichTextSelectionActionButton(action, contentColor)
                }
            }
        }
    }
}

@Composable
private fun RichTextSelectionActionButton(
    action: RichTextSelectionAction,
    contentColor: Color,
) {
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .semantics { contentDescription = action.label }
                .clickable(onClick = action.onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (action.content != null) {
                action.content()
            } else {
                Text(
                    text = action.glyph.orEmpty(),
                    color = contentColor,
                    fontWeight = if (action.selected) FontWeight.Bold else FontWeight.Normal,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

private class RichTextSelectionPopupPositionProvider(
    private val anchorBounds: Rect,
    private val preferAbove: Boolean,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x = (this.anchorBounds.center.x - popupContentSize.width / 2f)
            .roundToInt()
            .coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))
        val above = this.anchorBounds.top - popupContentSize.height
        val below = this.anchorBounds.bottom
        val y = if (preferAbove && above >= 0) {
            above
        } else if (below + popupContentSize.height <= windowSize.height) {
            below
        } else {
            above.coerceAtLeast(0f)
        }
        return IntOffset(x, y.roundToInt())
    }
}
