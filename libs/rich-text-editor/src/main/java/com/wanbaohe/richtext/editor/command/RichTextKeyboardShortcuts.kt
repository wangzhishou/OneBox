package com.wanbaohe.richtext.editor.command

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import com.wanbaohe.richtext.editor.RichTextEditorState
import com.wanbaohe.richtext.editor.clipboard.RichTextClipboard
import com.wanbaohe.richtext.editor.style.RichTextInlineStyle
import com.wanbaohe.richtext.editor.style.RichTextStyleScheme

fun Modifier.richTextEditorKeyboardShortcuts(
    state: RichTextEditorState,
    scheme: RichTextStyleScheme,
    clipboard: RichTextClipboard? = null,
    onUndo: () -> Unit = state::undo,
    onRedo: () -> Unit = state::redo,
): Modifier = onKeyEvent { event ->
    if (event.type != KeyEventType.KeyDown || !event.isCtrlPressed) return@onKeyEvent false
    when (event.key) {
        Key.Z -> {
            if (event.isShiftPressed) onRedo() else onUndo()
            true
        }

        Key.Y -> {
            onRedo()
            true
        }

        Key.C -> clipboard?.copySelection(state) == true
        Key.V -> clipboard?.paste(state) == true
        Key.X -> if (event.isShiftPressed) {
            state.toggleInlineStyle(RichTextInlineStyle.StrikethroughId, scheme)
            true
        } else {
            clipboard?.cutSelection(state) == true
        }

        Key.B -> {
            state.toggleInlineStyle(RichTextInlineStyle.BoldId, scheme)
            true
        }

        Key.I -> {
            state.toggleInlineStyle(RichTextInlineStyle.ItalicId, scheme)
            true
        }

        Key.U -> {
            state.toggleInlineStyle(RichTextInlineStyle.UnderlineId, scheme)
            true
        }

        else -> false
    }
}
