@file:OptIn(ExperimentalFoundationApi::class)

/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 * Licensed under the Apache License, Version 2.0
 */

package com.shifenmiao.demo.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.FormatClear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.components.text.SuppressSystemTextSelectionMenu
import com.shifenmiao.common.components.text.copySelection
import com.shifenmiao.common.components.text.pasteAtSelection
import com.shifenmiao.demo.R
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.ContentPaste
import com.wanbaohe.core.ui.widget.glass.GlassSegmentedButtonRow
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.richtext.editor.RichTextEditorState
import com.wanbaohe.richtext.editor.RichTextSnapshot
import com.wanbaohe.richtext.editor.command.applyBlockStyle
import com.wanbaohe.richtext.editor.command.clearInlineFormatting
import com.wanbaohe.richtext.editor.command.currentBlockStyle
import com.wanbaohe.richtext.editor.command.isListActive
import com.wanbaohe.richtext.editor.command.toggleList
import com.wanbaohe.richtext.editor.markdown.rememberRichTextMarkdownEncoder
import com.wanbaohe.richtext.editor.rememberRichTextEditorState
import com.wanbaohe.richtext.editor.richTextSnapshot
import com.wanbaohe.richtext.editor.style.RichTextBlockStyle
import com.wanbaohe.richtext.editor.style.RichTextInlineStyle
import com.wanbaohe.richtext.editor.style.RichTextStyleScheme
import com.wanbaohe.richtext.editor.style.rememberRichTextStyleScheme
import com.wanbaohe.richtext.editor.ui.RichTextEditor
import com.wanbaohe.richtext.editor.ui.RichTextSelectionAction
import com.wanbaohe.richtext.editor.ui.RichTextSelectionToolbar
import com.wanbaohe.richtext.editor.ui.richTextInlineSelectionActions
import kotlinx.coroutines.delay

@Composable
fun NativeRichTextEditorDemoScreen(
    onDismiss: () -> Unit,
) {
    val scheme = rememberDemoRichTextStyleScheme()
    val sample = rememberNativeRichTextSample(scheme)
    val editorState = rememberRichTextEditorState(sample)
    val encoder = rememberRichTextMarkdownEncoder(scheme)
    var mode by rememberSaveable { mutableStateOf(NativeRichTextEditorMode.EDIT) }
    var editorBounds by remember { mutableStateOf(Rect.Zero) }
    val focusManager = LocalFocusManager.current
    val markdown = encoder.encode(editorState.snapshot())
    val selection = editorState.selection

    LaunchedEffect(mode) {
        if (mode != NativeRichTextEditorMode.EDIT) focusManager.clearFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 560.dp, max = 780.dp)
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.demo_rich_editor_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.demo_rich_editor_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        GlassSegmentedButtonRow(
            options = NativeRichTextEditorMode.entries.toList(),
            selectedOption = mode,
            onOptionSelected = { mode = it },
            modifier = Modifier.fillMaxWidth(),
            rowStyle = GlassStyle.None,
            buttonHeight = 38.dp,
            label = { option ->
                Text(
                    text = stringResource(option.labelRes),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                )
            },
        )

        if (mode == NativeRichTextEditorMode.EDIT) {
            NativeRichTextBlockToolbar(editorState, scheme)
        }

        when (mode) {
            NativeRichTextEditorMode.EDIT -> {
                SuppressSystemTextSelectionMenu {
                    NativeRichTextEditorSurface(
                        state = editorState,
                        scheme = scheme,
                        onBoundsChanged = { editorBounds = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                }
                if (!selection.collapsed) {
                    NativeRichTextSelectionMenu(
                        state = editorState,
                        scheme = scheme,
                        anchorBounds = editorBounds,
                    )
                }
            }

            NativeRichTextEditorMode.PREVIEW -> KmpMarkdownPreview(
                markdown = markdown,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )

            NativeRichTextEditorMode.SOURCE -> NativeRichTextMarkdownSource(
                markdown = markdown,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }

        Text(
            text = if (mode == NativeRichTextEditorMode.EDIT && selection.collapsed) {
                stringResource(
                    R.string.demo_rich_editor_stats_with_hint,
                    editorState.text.length,
                    editorState.styleRangeCount,
                )
            } else {
                stringResource(
                    R.string.demo_rich_editor_stats,
                    editorState.text.length,
                    editorState.styleRangeCount,
                )
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.demo_rich_editor_close))
        }
    }
}

@Composable
private fun NativeRichTextBlockToolbar(
    state: RichTextEditorState,
    scheme: RichTextStyleScheme,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        scheme.blockStyles.forEach { definition ->
            NativeRichTextToolbarButton(
                label = definition.label,
                selected = state.currentBlockStyle(scheme).id == definition.id,
                onClick = { state.applyBlockStyle(definition.id, scheme) },
            )
        }
        NativeRichTextToolbarButton(
            label = stringResource(com.wanbaohe.richtext.editor.R.string.rich_text_bullet_list),
            selected = state.isListActive(scheme),
            onClick = { state.toggleList(scheme) },
        )
    }
}

@Composable
private fun NativeRichTextToolbarButton(
    label: String,
    onClick: () -> Unit,
    selected: Boolean = false,
) {
    GlassSurface(
        modifier = Modifier
            .pointerInput(onClick) {
                detectTapGestures(onTap = { onClick() })
            }
            .semantics(mergeDescendants = true) {
                onClick {
                    onClick()
                    true
                }
            },
        shape = RoundedCornerShape(10.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        style = if (selected) GlassStyle.Medium else GlassStyle.Thin,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun NativeRichTextEditorSurface(
    state: RichTextEditorState,
    scheme: RichTextStyleScheme,
    onBoundsChanged: (Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassSurface(
        modifier = modifier.onGloballyPositioned { onBoundsChanged(it.boundsInWindow()) },
        style = GlassStyle.Thin,
        shape = MaterialTheme.shapes.medium,
    ) {
        RichTextEditor(
            state = state,
            scheme = scheme,
            visual = com.wanbaohe.richtext.editor.ui.RichTextEditorVisualConfig(
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
            ),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        )
    }
}

@Composable
private fun NativeRichTextSelectionMenu(
    state: RichTextEditorState,
    scheme: RichTextStyleScheme,
    anchorBounds: Rect,
) {
    val clipboard = LocalClipboardManager.current
    val formattingActions = richTextInlineSelectionActions(state, scheme)
    val clipboardActions = listOf(
        RichTextSelectionAction(
            id = "copy",
            label = stringResource(android.R.string.copy),
            onClick = { state.textFieldState.copySelection(clipboard) },
            content = {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            },
        ),
        RichTextSelectionAction(
            id = "paste",
            label = stringResource(android.R.string.paste),
            onClick = { state.textFieldState.pasteAtSelection(clipboard, onChanged = {}) },
            content = {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentPaste,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            },
        ),
        RichTextSelectionAction(
            id = "clear",
            label = stringResource(com.wanbaohe.richtext.editor.R.string.rich_text_clear_formatting),
            onClick = { state.clearInlineFormatting(scheme) },
            content = {
                Icon(
                    imageVector = MaterialIcons.Outlined.FormatClear,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            },
        ),
    )
    RichTextSelectionToolbar(
        anchorBounds = anchorBounds,
        actions = formattingActions + clipboardActions,
    )
}

@Composable
private fun NativeRichTextMarkdownSource(
    markdown: String,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1_200)
            copied = false
        }
    }
    GlassSurface(
        modifier = modifier,
        style = GlassStyle.Thin,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TextButton(
                onClick = {
                    clipboard.setText(AnnotatedString(markdown))
                    copied = true
                },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    stringResource(
                        if (copied) {
                            R.string.demo_rich_editor_copied
                        } else {
                            R.string.demo_rich_editor_copy_markdown
                        },
                    ),
                )
            }
            SelectionContainer {
                Text(
                    text = markdown,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                    ),
                )
            }
        }
    }
}

private enum class NativeRichTextEditorMode(
    @StringRes val labelRes: Int,
) {
    EDIT(R.string.demo_rich_editor_mode_edit),
    PREVIEW(R.string.demo_rich_editor_mode_preview),
    SOURCE(R.string.demo_rich_editor_mode_source),
}

@Composable
private fun rememberNativeRichTextSample(
    scheme: RichTextStyleScheme,
): RichTextSnapshot {
    val title = stringResource(R.string.demo_rich_editor_sample_title)
    val body = stringResource(R.string.demo_rich_editor_sample_body)
    val bulletStyles = stringResource(R.string.demo_rich_editor_sample_bullet_styles)
    val bulletExport = stringResource(R.string.demo_rich_editor_sample_bullet_export)
    val stateWord = stringResource(R.string.demo_rich_editor_sample_state_word)
    val webViewWord = stringResource(R.string.demo_rich_editor_sample_webview_word)
    val footer = stringResource(
        R.string.demo_rich_editor_sample_footer,
        stateWord,
        webViewWord,
    )
    return remember(scheme, title, body, bulletStyles, bulletExport, stateWord, webViewWord, footer) {
        val marker = scheme.listStyle.marker
        val text = buildString {
            append(title)
            append("\n\n")
            append(body)
            append("\n\n")
            append(marker)
            append(bulletStyles)
            append('\n')
            append(marker)
            append(bulletExport)
            append("\n\n")
            append(footer)
        }
        val heading = scheme.blockStyle(RichTextBlockStyle.Heading1Id) ?: scheme.defaultBlockStyle
        val inlineCode = scheme.inlineStyle(RichTextInlineStyle.InlineCodeId)
        val bold = scheme.inlineStyle(RichTextInlineStyle.BoldId)
        richTextSnapshot(text) {
            addSpanStyle(heading.spanStyle, 0, title.length)
            addParagraphStyle(heading.paragraphStyle, 0, title.length + 1)
            listOf(bulletStyles, bulletExport).forEach { item ->
                val itemStart = text.indexOf(item)
                val start = itemStart - marker.length
                addParagraphStyle(
                    scheme.listStyle.paragraphStyle,
                    start,
                    itemStart + item.length + 1,
                )
            }
            inlineCode?.let { definition ->
                val start = text.indexOf(stateWord)
                addSpanStyle(
                    definition.apply(SpanStyle(), true),
                    start,
                    start + stateWord.length,
                )
            }
            bold?.let { definition ->
                val start = text.indexOf(webViewWord)
                addSpanStyle(
                    definition.apply(SpanStyle(), true),
                    start,
                    start + webViewWord.length,
                )
            }
        }
    }
}


@Composable
private fun rememberDemoRichTextStyleScheme(): RichTextStyleScheme {
    val baseScheme = rememberRichTextStyleScheme()
    val highlightColor = MaterialTheme.colorScheme.secondaryContainer
    val label = stringResource(R.string.demo_rich_editor_highlight)
    return remember(baseScheme, highlightColor, label) {
        val highlight = RichTextInlineStyle(
            id = "highlight",
            label = label,
            glyph = "H",
            matches = { it.background == highlightColor },
            apply = { style, enabled ->
                style.copy(
                    background = if (enabled) {
                        highlightColor
                    } else {
                        androidx.compose.ui.graphics.Color.Unspecified
                    },
                )
            },
            markdown = com.wanbaohe.richtext.editor.style.RichTextMarkdownInlineRule.delimited(
                "==",
                order = 15,
            ),
        )
        baseScheme.copy(inlineStyles = baseScheme.inlineStyles + highlight)
    }
}
