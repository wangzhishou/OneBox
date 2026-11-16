@file:OptIn(ExperimentalFoundationApi::class, FlowPreview::class)
@file:Suppress("FunctionNaming")

package com.wanbaohe.richtext.editor.document

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import com.wanbaohe.richtext.editor.command.toRichTextSnapshot
import com.wanbaohe.richtext.editor.internal.richTextBlockSnapshot
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
        AnimatedContent(
            targetState = state.mode,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                val enter = fadeIn(tween(EDITOR_MODE_FADE_MS)) +
                    slideInHorizontally(tween(EDITOR_MODE_SLIDE_MS)) { width ->
                        if (forward) width / 12 else -width / 12
                    }
                val exit = fadeOut(tween(EDITOR_MODE_FADE_MS)) +
                    slideOutHorizontally(tween(EDITOR_MODE_SLIDE_MS)) { width ->
                        if (forward) -width / 12 else width / 12
                    }
                enter togetherWith exit
            },
            label = "markdown-editor-mode",
        ) { mode ->
            when (mode) {
                MarkdownEditorMode.Edit -> {
                    val listState = rememberLazyListState()
                    LaunchedEffect(state.revealBlockId, state.blocks.size) {
                        val revealId = state.revealBlockId ?: return@LaunchedEffect
                        val index = state.blocks.indexOfFirst { block -> block.id == revealId }
                        if (index >= 0) {
                            listState.animateScrollToItem(index)
                            state.consumeRevealBlockId()
                        }
                    }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        // 与预览 MarkdownLayout.blockSpacing(12dp) 完全一致，切换模式时正文不整体位移。
                        verticalArrangement = Arrangement.spacedBy(MARKDOWN_BLOCK_SPACING),
                    ) {
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
                }

                MarkdownEditorMode.Source -> {
                    BasicTextField(
                        state = state.sourceState,
                        enabled = !state.isDecoding,
                        inputTransformation = InputTransformation {
                            state.notifyContentChanged(toRichTextSnapshot())
                        },
                        modifier = Modifier
                            .fillMaxSize()
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
                        Box(modifier = Modifier.fillMaxSize()) {
                            previewContent(content)
                        }
                    }
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
    val snapshot = block.state.snapshot()
    val blockStyle = remember(snapshot, scheme) {
        val blockSnapshot = richTextBlockSnapshot(
            source = snapshot.text,
            paragraph = TextRange(0, snapshot.text.length),
            snapshot = snapshot,
            scheme = scheme,
        )
        scheme.blockStyles.firstOrNull { style -> !style.isDefault && style.matches(blockSnapshot) }
            ?: scheme.defaultBlockStyle
    }
    // 标题不再画下划线：编辑区与预览保持同一规则，标题上下只留 blockSpacing。
    val baseVisual = config.richTextVisual
    val isListBlock = snapshot.text.startsWith(scheme.listStyle.marker)
    val blockVisual = run {
        var textStyle = baseVisual.textStyle
        if (blockStyle.paragraphStyle.lineHeight.isSpecified && textStyle != TextStyle.Default) {
            textStyle = textStyle.copy(lineHeight = blockStyle.paragraphStyle.lineHeight)
        }
        if (isListBlock && textStyle != TextStyle.Default) {
            textStyle = textStyle.copy(textIndent = scheme.listStyle.paragraphStyle.textIndent)
        }
        if (textStyle != baseVisual.textStyle) baseVisual.copy(textStyle = textStyle) else baseVisual
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        RichTextEditor(
            state = block.state,
            scheme = scheme,
            behavior = config.richTextBehavior.copy(
                onUndo = state::undo,
                onRedo = state::redo,
            ),
            visual = if (state.activeBlockId == block.id) {
                blockVisual
            } else {
                blockVisual.copy(selectionAnchorTracker = null)
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
private const val EDITOR_MODE_FADE_MS = 180
private const val EDITOR_MODE_SLIDE_MS = 260

// 与 libs/markdown 的 MarkdownLayout.blockSpacing 默认值一致。
private val MARKDOWN_BLOCK_SPACING = 12.dp
