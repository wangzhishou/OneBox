@file:Suppress("FunctionNaming")

package com.shifenmiao.common.editor

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.R
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.richtext.editor.command.applyBlockStyle
import com.wanbaohe.richtext.editor.command.currentBlockStyle
import com.wanbaohe.richtext.editor.command.isInlineStyleActive
import com.wanbaohe.richtext.editor.command.isListActive
import com.wanbaohe.richtext.editor.command.toggleInlineStyle
import com.wanbaohe.richtext.editor.command.toggleList
import com.wanbaohe.richtext.editor.markdown.MarkdownRawBlockKind
import com.wanbaohe.richtext.editor.style.RichTextBlockStyle
import com.wanbaohe.richtext.editor.style.RichTextStyleScheme

@Composable
internal fun NativeMarkdownEditorActionBar(
    state: NativeMarkdownEditorState,
    extraActions: List<NativeEditorExtraAction>,
    modifier: Modifier = Modifier,
) {
    val scheme = rememberNativeMarkdownStyleScheme()
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NativeEditorFormattingRow(state, scheme)
        NativeEditorInsertionRow(state, extraActions)
    }
}

@Composable
private fun NativeEditorFormattingRow(
    state: NativeMarkdownEditorState,
    scheme: RichTextStyleScheme,
) {
    val editor = state.richTextState()
    NativeEditorToolbarSection(title = stringResource(R.string.native_editor_title_formatting)) {
        scheme.inlineStyles.forEach { definition ->
            val active = editor?.isInlineStyleActive(definition.id, scheme) == true
            NativeEditorToolbarButton(
                label = definition.label,
                displayLabel = definition.glyph,
                selected = active,
                enabled = editor != null,
                onClick = { editor?.toggleInlineStyle(definition.id, scheme) },
            )
        }
        scheme.blockStyles.forEach { definition ->
            NativeEditorToolbarButton(
                label = definition.label,
                displayLabel = if (definition.id == RichTextBlockStyle.BodyId) "T" else definition.label,
                selected = editor?.currentBlockStyle(scheme)?.id == definition.id,
                enabled = editor != null,
                onClick = { editor?.applyBlockStyle(definition.id, scheme) },
            )
        }
        NativeEditorToolbarButton(
            label = stringResource(com.wanbaohe.richtext.editor.R.string.rich_text_bullet_list),
            displayLabel = "•",
            selected = editor?.isListActive(scheme) == true,
            enabled = editor != null,
            onClick = { editor?.toggleList(scheme) },
        )
    }
}

@Composable
private fun NativeEditorInsertionRow(
    state: NativeMarkdownEditorState,
    extraActions: List<NativeEditorExtraAction>,
) {
    val codeTemplate = stringResource(R.string.native_editor_template_code)
    val diagramTemplate = stringResource(R.string.native_editor_template_diagram)
    val imageTemplate = stringResource(R.string.native_editor_template_image)
    val videoTemplate = stringResource(R.string.native_editor_template_video)
    NativeEditorToolbarSection(title = stringResource(R.string.native_editor_title_insert)) {
        NativeEditorToolbarButton(
            label = stringResource(R.string.native_editor_insert_text),
            icon = Icons.Outlined.TextFields,
            onClick = state::insertTextBlock,
        )
        NativeEditorToolbarButton(
            label = stringResource(R.string.native_editor_insert_code),
            icon = Icons.Outlined.Code,
            onClick = {
                state.insertRawBlock(
                    kind = MarkdownRawBlockKind.Code,
                    markdown = codeTemplate,
                )
            },
        )
        NativeEditorToolbarButton(
            label = stringResource(R.string.native_editor_insert_diagram),
            icon = Icons.Outlined.AccountTree,
            onClick = {
                state.insertRawBlock(
                    kind = MarkdownRawBlockKind.Diagram,
                    markdown = diagramTemplate,
                )
            },
        )
        NativeEditorToolbarButton(
            label = stringResource(R.string.native_editor_insert_image),
            icon = Icons.Outlined.Image,
            onClick = {
                state.insertRawBlock(
                    kind = MarkdownRawBlockKind.Image,
                    markdown = imageTemplate,
                )
            },
        )
        NativeEditorToolbarButton(
            label = stringResource(R.string.native_editor_insert_video),
            icon = Icons.Outlined.PlayCircle,
            onClick = {
                state.insertRawBlock(
                    kind = MarkdownRawBlockKind.Video,
                    markdown = videoTemplate,
                )
            },
        )
        NativeEditorExtraActions(extraActions)
    }
}

@Composable
private fun NativeEditorExtraActions(actions: List<NativeEditorExtraAction>) {
    actions.forEach { action ->
        NativeEditorToolbarButton(
            label = action.label,
            icon = action.icon,
            onClick = action.onClick,
        )
    }
}

@Composable
private fun NativeEditorToolbarSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            modifier = Modifier.width(36.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            content = { content() },
        )
    }
}

@Suppress("LongParameterList")
@Composable
internal fun NativeEditorToolbarButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    displayLabel: String = label,
    icon: ImageVector? = null,
    selected: Boolean = false,
    enabled: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
) {
    val colorScheme = MaterialTheme.colorScheme
    val contentColor = if (selected) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant
    GlassSurface(
        modifier = modifier
            .height(36.dp)
            .pointerInput(onClick, enabled) {
                detectTapGestures { if (enabled) onClick() }
            }
            .semantics(mergeDescendants = true) {
                onClick {
                    if (enabled) onClick()
                    true
                }
            },
        color = if (selected) colorScheme.primaryContainer else colorScheme.surfaceContainer,
        style = if (selected) GlassStyle.Medium else GlassStyle.Thin,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let { imageVector ->
                Icon(
                    imageVector = imageVector,
                    contentDescription = label,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor,
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = displayLabel,
                style = textStyle,
                color = contentColor,
                maxLines = 1,
            )
        }
    }
}

data class NativeEditorExtraAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)
