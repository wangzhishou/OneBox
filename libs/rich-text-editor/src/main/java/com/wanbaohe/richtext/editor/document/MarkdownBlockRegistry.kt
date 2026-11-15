@file:OptIn(ExperimentalFoundationApi::class)
@file:Suppress("FunctionNaming")

package com.wanbaohe.richtext.editor.document

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import com.wanbaohe.richtext.editor.RichTextSnapshot
import com.wanbaohe.richtext.editor.command.toRichTextSnapshot
import com.wanbaohe.richtext.editor.markdown.MarkdownRawBlockKind

interface MarkdownBlockEditor {
    @Composable
    fun Editor(
        block: MarkdownEditorBlock.Raw,
        onDelete: () -> Unit,
        onContentChanged: (RichTextSnapshot) -> Unit,
    )
}

class MarkdownBlockRegistry(
    private val editors: Map<MarkdownRawBlockKind, MarkdownBlockEditor> = emptyMap(),
    private val fallback: MarkdownBlockEditor = DefaultMarkdownBlockEditor,
) {
    @Composable
    fun Editor(
        block: MarkdownEditorBlock.Raw,
        onDelete: () -> Unit,
        onContentChanged: (RichTextSnapshot) -> Unit,
    ) {
        (editors[block.kind] ?: fallback).Editor(block, onDelete, onContentChanged)
    }

    companion object {
        val Default = MarkdownBlockRegistry()
    }
}

private object DefaultMarkdownBlockEditor : MarkdownBlockEditor {
    @Composable
    override fun Editor(
        block: MarkdownEditorBlock.Raw,
        onDelete: () -> Unit,
        onContentChanged: (RichTextSnapshot) -> Unit,
    ) {
        BasicTextField(
            state = block.state,
            modifier = Modifier.fillMaxWidth(),
            inputTransformation = InputTransformation { onContentChanged(toRichTextSnapshot()) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
            ),
        )
    }
}
