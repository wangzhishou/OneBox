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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.TextRange

@Stable
class RichTextEditorState internal constructor(
    val textFieldState: TextFieldState,
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

    fun snapshot(): RichTextSnapshot = RichTextSnapshot(
        text = text.toString(),
        spanStyles = textFieldState.textStyles.getSpanStyles(TextRange(0, text.length)),
        paragraphStyles = textFieldState.textStyles.getParagraphStyles(TextRange(0, text.length)),
    )

    fun select(range: TextRange) {
        edit { selection = range }
    }

    fun edit(block: TextFieldBuffer.() -> Unit) {
        textFieldState.edit(block)
    }
}

object RichTextEditorStateSaver : Saver<RichTextEditorState, Any> {
    override fun SaverScope.save(value: RichTextEditorState): Any? =
        with(TextFieldState.Saver) { save(value.textFieldState) }

    override fun restore(value: Any): RichTextEditorState? =
        TextFieldState.Saver.restore(value)?.let(::RichTextEditorState)
}

@Composable
fun rememberRichTextEditorState(
    snapshot: RichTextSnapshot = RichTextSnapshot.Empty,
): RichTextEditorState = rememberSaveable(saver = RichTextEditorStateSaver) {
    RichTextEditorState(snapshot)
}
