@file:OptIn(ExperimentalFoundationApi::class)

/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 * Licensed under the Apache License, Version 2.0
 */

package com.wanbaohe.richtext.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.ExpandPolicy
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange

@Stable
class RichTextEditorState internal constructor(
    val textFieldState: TextFieldState,
    pendingInlineStyleIds: Set<String> = emptySet(),
) {
    constructor(snapshot: RichTextSnapshot = RichTextSnapshot.Empty) : this(
        TextFieldState(snapshot.text).also { state ->
            state.edit {
                snapshot.spanStyles.forEach { style ->
                    addStyle(style.item, TextRange(style.start, style.end), ExpandPolicy.AtEnd)
                }
                snapshot.paragraphStyles.forEach { style ->
                    addStyle(style.item, TextRange(style.start, style.end), ExpandPolicy.AtEnd)
                }
            }
        },
    )

    val text: CharSequence
        get() = textFieldState.text

    val selection: TextRange
        get() = textFieldState.selection

    val selectedText: String
        get() {
            val range = selection
            return if (range.collapsed) "" else text.substring(range.min, range.max)
        }

    val styleRangeCount: Int
        get() = textFieldState.textStyles.getSpanStyles(TextRange(0, text.length)).size +
            textFieldState.textStyles.getParagraphStyles(TextRange(0, text.length)).size

    var pendingInlineStyleIds by mutableStateOf(pendingInlineStyleIds)
        private set

    val canUndo: Boolean
        get() = textFieldState.undoState.canUndo

    val canRedo: Boolean
        get() = textFieldState.undoState.canRedo

    fun snapshot(): RichTextSnapshot = RichTextSnapshot(
        text = text.toString(),
        spanStyles = textFieldState.textStyles.getSpanStyles(TextRange(0, text.length)),
        paragraphStyles = textFieldState.textStyles.getParagraphStyles(TextRange(0, text.length)),
    )

    var onContentChanged: ((RichTextSnapshot) -> Unit)? = null

    fun select(range: TextRange) {
        textFieldState.edit { selection = range }
    }

    fun edit(block: TextFieldBuffer.() -> Unit) {
        textFieldState.edit(block)
        onContentChanged?.invoke(snapshot())
    }

    fun togglePendingInlineStyle(styleId: String) {
        pendingInlineStyleIds = if (styleId in pendingInlineStyleIds) {
            pendingInlineStyleIds - styleId
        } else {
            pendingInlineStyleIds + styleId
        }
    }

    fun clearPendingInlineStyles() {
        pendingInlineStyleIds = emptySet()
    }

    fun undo() {
        textFieldState.undoState.undo()
        onContentChanged?.invoke(snapshot())
    }

    fun redo() {
        textFieldState.undoState.redo()
        onContentChanged?.invoke(snapshot())
    }
}

object RichTextEditorStateSaver : Saver<RichTextEditorState, Any> {
    override fun SaverScope.save(value: RichTextEditorState): Any? = listOf(
        with(TextFieldState.Saver) { save(value.textFieldState) },
        value.pendingInlineStyleIds.toList(),
    )

    override fun restore(value: Any): RichTextEditorState? {
        val parts = value as? List<*> ?: return null
        val hasPendingStyles = parts.firstOrNull() is List<*>
        val savedState = if (hasPendingStyles) parts[0] else value
        val pending = if (hasPendingStyles) {
            (parts.getOrNull(1) as? List<*>)?.filterIsInstance<String>()?.toSet().orEmpty()
        } else {
            emptySet()
        }
        return TextFieldState.Saver.restore(savedState as Any)?.let { state ->
            RichTextEditorState(state, pending)
        }
    }
}

@Composable
fun rememberRichTextEditorState(
    snapshot: RichTextSnapshot = RichTextSnapshot.Empty,
): RichTextEditorState = rememberSaveable(saver = RichTextEditorStateSaver) {
    RichTextEditorState(snapshot)
}
