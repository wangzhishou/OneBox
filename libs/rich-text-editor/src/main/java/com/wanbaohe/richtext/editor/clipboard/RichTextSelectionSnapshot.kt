@file:OptIn(ExperimentalFoundationApi::class)

package com.wanbaohe.richtext.editor.clipboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.ExpandPolicy
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import com.wanbaohe.richtext.editor.RichTextEditorState
import com.wanbaohe.richtext.editor.RichTextSnapshot

fun RichTextEditorState.selectionSnapshot(): RichTextSnapshot {
    val range = selection
    if (range.collapsed) return RichTextSnapshot.Empty
    val start = range.min
    val end = range.max
    return RichTextSnapshot(
        text = text.substring(start, end),
        spanStyles = textFieldState.textStyles.getSpanStyles(range).mapNotNull { style ->
            style.shiftedWithin(start, end)
        },
        paragraphStyles = textFieldState.textStyles.getParagraphStyles(range).mapNotNull { style ->
            style.shiftedWithin(start, end)
        },
    )
}

fun RichTextEditorState.replaceSelectionWithSnapshot(snapshot: RichTextSnapshot) {
    val range = selection
    val start = range.min
    edit {
        replace(range.min, range.max, snapshot.text)
        snapshot.spanStyles.forEach { style ->
            addStyle(
                style.item,
                TextRange(style.start + start, style.end + start),
                ExpandPolicy.AtEnd,
            )
        }
        snapshot.paragraphStyles.forEach { style ->
            addStyle(
                style.item,
                TextRange(style.start + start, style.end + start),
                ExpandPolicy.AtEnd,
            )
        }
        selection = TextRange(start + snapshot.text.length)
    }
}

private fun <T> AnnotatedString.Range<T>.shiftedWithin(
    start: Int,
    end: Int,
): AnnotatedString.Range<T>? {
    val clippedStart = maxOf(this.start, start)
    val clippedEnd = minOf(this.end, end)
    if (clippedStart >= clippedEnd) return null
    return AnnotatedString.Range(item, clippedStart - start, clippedEnd - start)
}
