/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 * Licensed under the Apache License, Version 2.0
 */

package com.wanbaohe.richtext.editor.ui

import androidx.compose.runtime.Composable
import com.wanbaohe.richtext.editor.RichTextEditorState
import com.wanbaohe.richtext.editor.command.isInlineStyleActive
import com.wanbaohe.richtext.editor.command.toggleInlineStyle
import com.wanbaohe.richtext.editor.style.RichTextStyleScheme

data class RichTextSelectionAction(
    val id: String,
    val label: String,
    val onClick: () -> Unit,
    val selected: Boolean = false,
    val glyph: String? = null,
    val content: (@Composable () -> Unit)? = null,
)

fun richTextInlineSelectionActions(
    state: RichTextEditorState,
    scheme: RichTextStyleScheme,
): List<RichTextSelectionAction> = scheme.inlineStyles.map { definition ->
    RichTextSelectionAction(
        id = definition.id,
        label = definition.label,
        glyph = definition.glyph,
        selected = state.isInlineStyleActive(definition.id, scheme),
        onClick = { state.toggleInlineStyle(definition.id, scheme) },
    )
}
