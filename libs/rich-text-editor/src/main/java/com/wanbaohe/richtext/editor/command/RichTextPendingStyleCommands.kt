@file:OptIn(ExperimentalFoundationApi::class)

package com.wanbaohe.richtext.editor.command

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import com.wanbaohe.richtext.editor.RichTextEditorState
import com.wanbaohe.richtext.editor.RichTextSnapshot
import com.wanbaohe.richtext.editor.style.RichTextStyleScheme

@Composable
fun rememberRichTextInputTransformation(
    state: RichTextEditorState,
    scheme: RichTextStyleScheme,
): InputTransformation = remember(state, scheme) {
    InputTransformation {
        applyPendingInlineStyles(state, scheme)
        autoContinueRichTextList(scheme.listStyle)
        state.onContentChanged?.invoke(toRichTextSnapshot())
    }
}

fun TextFieldBuffer.toRichTextSnapshot(): RichTextSnapshot = RichTextSnapshot(
    text = toString(),
    spanStyles = getSpanStyles(TextRange(0, length)).mapNotNull { range ->
        if (range.isValid) {
            val textRange = range.textRange
            AnnotatedString.Range(range.spanStyle, textRange.start, textRange.end)
        } else {
            null
        }
    },
    paragraphStyles = getParagraphStyles(TextRange(0, length)).mapNotNull { range ->
        if (range.isValid) {
            val textRange = range.textRange
            AnnotatedString.Range(range.paragraphStyle, textRange.start, textRange.end)
        } else {
            null
        }
    },
)

private fun TextFieldBuffer.applyPendingInlineStyles(
    state: RichTextEditorState,
    scheme: RichTextStyleScheme,
) {
    if (state.pendingInlineStyleIds.isEmpty()) return
    val style = state.pendingInlineStyleIds.mapNotNull(scheme::inlineStyle)
        .fold(SpanStyle()) { current, definition -> definition.apply(current, true) }
    if (style == SpanStyle()) return
    for (index in 0 until changes.changeCount) {
        val inserted = changes.getRange(index)
        val original = changes.getOriginalRange(index)
        if (original.collapsed && !inserted.collapsed) {
            addStyle(style, inserted.min, inserted.max)
        }
    }
}
