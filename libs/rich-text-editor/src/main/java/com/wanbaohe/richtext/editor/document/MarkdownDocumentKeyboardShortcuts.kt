package com.wanbaohe.richtext.editor.document

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

fun Modifier.markdownDocumentKeyboardShortcuts(
    state: MarkdownDocumentEditorState,
): Modifier = onKeyEvent { event ->
    if (event.type != KeyEventType.KeyDown || !event.isCtrlPressed) return@onKeyEvent false
    when (event.key) {
        Key.Z -> {
            if (event.isShiftPressed) state.redo() else state.undo()
            true
        }

        Key.Y -> {
            state.redo()
            true
        }

        else -> false
    }
}
