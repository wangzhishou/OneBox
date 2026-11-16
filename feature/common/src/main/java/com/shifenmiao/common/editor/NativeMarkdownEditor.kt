@file:OptIn(ExperimentalFoundationApi::class)
@file:Suppress("FunctionNaming")

package com.shifenmiao.common.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.R
import com.shifenmiao.common.components.text.SuppressSystemTextSelectionMenu
import com.shifenmiao.common.markdown.OneBoxMarkdownContent
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.markdown.OneBoxMarkdown
import com.wanbaohe.richtext.editor.clipboard.rememberRichTextClipboard
import com.wanbaohe.richtext.editor.command.clearInlineFormatting
import com.wanbaohe.richtext.editor.command.toRichTextSnapshot
import com.wanbaohe.richtext.editor.document.MarkdownBlockEditor
import com.wanbaohe.richtext.editor.document.MarkdownBlockRegistry
import com.wanbaohe.richtext.editor.document.MarkdownDocumentEditor
import com.wanbaohe.richtext.editor.document.MarkdownDocumentEditorConfig
import com.wanbaohe.richtext.editor.document.MarkdownEditorBlock
import com.wanbaohe.richtext.editor.markdown.MarkdownRawBlockKind
import com.wanbaohe.richtext.editor.style.RichTextStyleScheme
import com.wanbaohe.richtext.editor.ui.RichTextEditorBehaviorConfig
import com.wanbaohe.richtext.editor.ui.RichTextEditorVisualConfig
import com.wanbaohe.richtext.editor.ui.RichTextSelectionAction
import com.wanbaohe.richtext.editor.ui.RichTextSelectionToolbar
import com.wanbaohe.richtext.editor.ui.rememberRichTextSelectionAnchorTracker
import com.wanbaohe.richtext.editor.ui.richTextInlineSelectionActions
import kotlinx.coroutines.flow.drop

@Composable
fun NativeMarkdownEditor(
    state: NativeMarkdownEditorState,
    modifier: Modifier = Modifier,
    extraActions: List<NativeEditorExtraAction> = emptyList(),
    onContentChanged: () -> Unit = {},
) {
    val scheme = rememberNativeMarkdownStyleScheme()
    val registry = rememberNativeMarkdownBlockRegistry()
    val selectionTracker = rememberRichTextSelectionAnchorTracker()
    val clipboard = rememberRichTextClipboard(scheme)
    LaunchedEffect(state) {
        snapshotFlow { state.userContentVersion }.drop(1).collect { onContentChanged() }
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            SuppressSystemTextSelectionMenu {
                MarkdownDocumentEditor(
                    state = state,
                    scheme = scheme,
                    config = MarkdownDocumentEditorConfig(
                        registry = registry,
                        richTextBehavior = RichTextEditorBehaviorConfig(clipboard = clipboard),
                        richTextVisual = RichTextEditorVisualConfig(
                            // 与预览使用同一套正文排版（字号/行高/字体），切模式时正文不跳。
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
                            selectionAnchorTracker = selectionTracker,
                            // 只给空段落一个可点高度；非空块至少一行 32dp，不会被额外撑高。
                            minHeight = 32.dp,
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.native_editor_empty_hint),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            },
                        ),
                    ),
                    previewContent = { markdown ->
                        OneBoxMarkdownContent(
                            message = markdown,
                            modifier = Modifier.fillMaxSize(),
                            // 外层统一 16dp 横向留白；预览自身不再额外缩进，避免切换时左右跳。
                            paddingValues = PaddingValues(0.dp),
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }

        if (state.mode == NativeMarkdownEditorMode.Source && state.sourceState.text.isEmpty()) {
            Text(
                text = stringResource(R.string.native_editor_source_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        NativeMarkdownEditorDock(
            state = state,
            extraActions = extraActions,
        )
    }

    NativeMarkdownEditorSelectionMenu(state, scheme, selectionTracker)
}

@Composable
private fun NativeMarkdownEditorSelectionMenu(
    state: NativeMarkdownEditorState,
    scheme: RichTextStyleScheme,
    selectionTracker: com.wanbaohe.richtext.editor.ui.RichTextSelectionAnchorTracker,
) {
    val editor = state.richTextState() ?: return
    if (editor.selection.collapsed) return
    val clipboard = rememberRichTextClipboard(scheme)
    val actions = richTextInlineSelectionActions(editor, scheme) + listOf(
        RichTextSelectionAction(
            id = "copy",
            label = stringResource(android.R.string.copy),
            glyph = "⧉",
            onClick = { clipboard.copySelection(editor) },
        ),
        RichTextSelectionAction(
            id = "cut",
            label = stringResource(android.R.string.cut),
            glyph = "✂",
            onClick = { clipboard.cutSelection(editor) },
        ),
        RichTextSelectionAction(
            id = "paste",
            label = stringResource(android.R.string.paste),
            glyph = "▣",
            onClick = { clipboard.paste(editor) },
        ),
        RichTextSelectionAction(
            id = "clear",
            label = stringResource(com.wanbaohe.richtext.editor.R.string.rich_text_clear_formatting),
            glyph = "⌫",
            onClick = { editor.clearInlineFormatting(scheme) },
        ),
    )
    RichTextSelectionToolbar(
        anchorBounds = selectionTracker.anchorBounds,
        actions = actions,
    )
}

@Composable
private fun rememberNativeMarkdownBlockRegistry(): MarkdownBlockRegistry {
    val editors = remember {
        MarkdownRawBlockKind.entries.associateWith { NativeRawBlockEditor() }
    }
    return remember(editors) { MarkdownBlockRegistry(editors) }
}

private class NativeRawBlockEditor : MarkdownBlockEditor {
    @Composable
    override fun Editor(
        block: MarkdownEditorBlock.Raw,
        onDelete: () -> Unit,
        onContentChanged: (com.wanbaohe.richtext.editor.RichTextSnapshot) -> Unit,
    ) {
        var showPreview by remember(block.id) { mutableStateOf(false) }
        GlassSurface(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            style = GlassStyle.Thin,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                NativeRawBlockHeader(
                    block = block,
                    showPreview = showPreview,
                    onTogglePreview = { showPreview = !showPreview },
                    onDelete = onDelete,
                )
                if (showPreview) {
                    OneBoxMarkdown(
                        markdown = block.state.text.toString(),
                        modifier = Modifier.fillMaxWidth(),
                        enableScroll = false,
                    )
                } else {
                    BasicTextField(
                        state = block.state,
                        inputTransformation = androidx.compose.foundation.text.input.InputTransformation {
                            onContentChanged(
                                toRichTextSnapshot(),
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp)
                            .padding(12.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily.Monospace,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
                    )
                }
            }
        }
    }
}

private val MarkdownRawBlockKind.labelRes: Int
    get() = when (this) {
        MarkdownRawBlockKind.Code -> R.string.native_editor_kind_code
        MarkdownRawBlockKind.Diagram -> R.string.native_editor_kind_diagram
        MarkdownRawBlockKind.Image -> R.string.native_editor_kind_image
        MarkdownRawBlockKind.Video -> R.string.native_editor_kind_video
        MarkdownRawBlockKind.Audio -> R.string.native_editor_kind_audio
        MarkdownRawBlockKind.Math -> R.string.native_editor_kind_math
        MarkdownRawBlockKind.Html -> R.string.native_editor_kind_html
        else -> R.string.native_editor_kind_other
    }

@Composable
private fun NativeRawBlockHeader(
    block: MarkdownEditorBlock.Raw,
    showPreview: Boolean,
    onTogglePreview: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(block.kind.labelRes),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        IconButton(onClick = onTogglePreview) {
            Icon(
                imageVector = if (showPreview) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                contentDescription = stringResource(R.string.native_editor_show_preview),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp),
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.native_editor_delete_block),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
