@file:OptIn(ExperimentalFoundationApi::class, FlowPreview::class)
@file:Suppress("FunctionNaming")

package com.wanbaohe.richtext.editor.document

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import com.wanbaohe.richtext.editor.command.toRichTextSnapshot
import com.wanbaohe.richtext.editor.style.RichTextStyleScheme
import com.wanbaohe.richtext.editor.ui.RichTextEditor
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

@Composable
fun MarkdownDocumentEditor(
    state: MarkdownDocumentEditorState,
    scheme: RichTextStyleScheme,
    modifier: Modifier = Modifier,
    config: MarkdownDocumentEditorConfig = MarkdownDocumentEditorConfig(),
    previewContent: @Composable (String) -> Unit = { markdown ->
        SelectionContainer {
            Text(text = markdown, fontFamily = FontFamily.Monospace)
        }
    },
) {
    Column(modifier = modifier) {
        if (state.isDecoding) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        when (state.mode) {
            MarkdownEditorMode.Edit -> LazyColumn {
                items(
                    items = state.blocks,
                    key = { block -> block.id },
                    contentType = { block -> block::class },
                ) { block ->
                    when (block) {
                        is MarkdownEditorBlock.RichText -> RichTextDocumentBlock(
                            block = block,
                            state = state,
                            scheme = scheme,
                            config = config,
                        )
                        is MarkdownEditorBlock.Raw -> RawDocumentBlock(block, state, config.registry)
                    }
                }
            }

            MarkdownEditorMode.Source -> {
                BasicTextField(
                    state = state.sourceState,
                    enabled = !state.isDecoding,
                    inputTransformation = InputTransformation {
                        state.notifyContentChanged(toRichTextSnapshot())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .markdownDocumentKeyboardShortcuts(state),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
                )
            }

            MarkdownEditorMode.Preview -> {
                val markdown by produceState<String?>(null, state) {
                    value = state.exportMarkdown()
                    snapshotFlow { state.contentVersion }
                        .debounce(PREVIEW_ENCODE_DEBOUNCE_MS)
                        .collect { value = state.exportMarkdown() }
                }
                val content = markdown
                if (content == null) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    previewContent(content)
                }
            }
        }
    }
}

@Composable
private fun RichTextDocumentBlock(
    block: MarkdownEditorBlock.RichText,
    state: MarkdownDocumentEditorState,
    scheme: RichTextStyleScheme,
    config: MarkdownDocumentEditorConfig,
) {
    RichTextEditor(
        state = block.state,
        scheme = scheme,
        behavior = config.richTextBehavior.copy(
            onUndo = state::undo,
            onRedo = state::redo,
        ),
        visual = if (state.activeBlockId == block.id) {
            config.richTextVisual
        } else {
            config.richTextVisual.copy(selectionAnchorTracker = null)
        },
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(block.id) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    state.activate(block.id)
                }
            },
    )
}

@Composable
private fun RawDocumentBlock(
    block: MarkdownEditorBlock.Raw,
    state: MarkdownDocumentEditorState,
    registry: MarkdownBlockRegistry,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(block.id) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    state.activate(block.id)
                }
            },
    ) {
        registry.Editor(
            block = block,
            onDelete = { state.removeBlock(block.id) },
            onContentChanged = { snapshot -> state.notifyContentChanged(snapshot) },
        )
    }
}

private const val PREVIEW_ENCODE_DEBOUNCE_MS = 300L
