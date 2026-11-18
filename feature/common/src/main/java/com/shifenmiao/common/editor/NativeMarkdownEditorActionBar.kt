@file:Suppress("FunctionNaming")

package com.shifenmiao.common.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.shifenmiao.common.R
import com.t8rin.imagetoolbox.core.resources.icons.Mic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowDropDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
import com.wanbaohe.core.ui.widget.enhanced.EnhancedDropdownMenu
import com.wanbaohe.richtext.editor.command.applyBlockStyle
import com.wanbaohe.richtext.editor.command.currentBlockStyle
import com.wanbaohe.richtext.editor.command.isInlineStyleActive
import com.wanbaohe.richtext.editor.command.toggleInlineStyle
import com.wanbaohe.richtext.editor.style.RichTextBlockStyle
import com.t8rin.imagetoolbox.core.resources.Icons as OneBoxIcons

/**
 * Markdown 编辑器的底部工具坞。
 *
 * 交互外壳（玻璃按钮、展开/收起、横向滚动）在 [NativeEditorDock] 里，
 * 这里只负责把「模式切换 + 格式/插入动作」翻译成工具坞动作表。
 */
@Composable
internal fun NativeMarkdownEditorDock(
    state: NativeMarkdownEditorState,
    extraActions: List<NativeEditorExtraAction>,
    modifier: Modifier = Modifier,
    onInsert: (NativeEditorInsertKind) -> Unit = {},
    onVoiceInput: (() -> Unit)? = null,
) {
    var showInsertMenu by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        NativeEditorDock(
            actions = markdownDockActions(
                state = state,
                extraActions = extraActions,
                onShowInsert = { showInsertMenu = true },
                onVoiceInput = onVoiceInput,
            ),
            leading = { expanded -> NativeMarkdownEditorModeToggle(state = state, expanded = expanded) },
        )
        NativeEditorInsertMenu(
            expanded = showInsertMenu,
            onDismiss = { showInsertMenu = false },
            state = state,
            onInsert = onInsert,
        )
    }
}

@Composable
private fun NativeMarkdownEditorModeToggle(
    state: NativeMarkdownEditorState,
    expanded: Boolean,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Box {
        NativeEditorDockChip(
            label = stringResource(state.mode.labelRes()),
            icon = OneBoxIcons.Outlined.LineArrowDropDown,
            expanded = expanded,
            onClick = { menuExpanded = true },
        )
        EnhancedDropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            NativeMarkdownEditorMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(stringResource(mode.labelRes())) },
                    leadingIcon = { Icon(mode.icon(), contentDescription = null) },
                    trailingIcon = {
                        if (mode == state.mode) {
                            Icon(Icons.Filled.Check, contentDescription = null)
                        }
                    },
                    onClick = {
                        menuExpanded = false
                        state.selectMode(mode)
                    },
                )
            }
        }
    }
}

@Composable
private fun markdownDockActions(
    state: NativeMarkdownEditorState,
    extraActions: List<NativeEditorExtraAction>,
    onShowInsert: () -> Unit,
    onVoiceInput: (() -> Unit)?,
): List<NativeEditorDockAction> =
    historyActions(state = state, onVoiceInput = onVoiceInput) +
        inlineStyleActions(state = state) +
        blockStyleActions(state = state) +
        insertActions(state = state, onShowInsert = onShowInsert) +
        extraActions.mapIndexed { index, action ->
            NativeEditorDockAction(
                id = "extra_$index",
                label = action.label,
                icon = action.icon,
                dividerBefore = index == 0,
                onClick = action.onClick,
            )
        }

@Composable
private fun historyActions(
    state: NativeMarkdownEditorState,
    onVoiceInput: (() -> Unit)?,
): List<NativeEditorDockAction> = buildList {
    if (onVoiceInput != null) {
        add(
            NativeEditorDockAction(
                id = "voice",
                label = stringResource(com.shifenmiao.core.R.string.ai_input_voice),
                icon = OneBoxIcons.Outlined.Mic,
                onClick = onVoiceInput,
            )
        )
    }
    add(
        NativeEditorDockAction(
            id = "undo",
            label = stringResource(com.t8rin.imagetoolbox.core.resources.R.string.undo),
            icon = OneBoxIcons.Outlined.LineUndo,
            enabled = state.canUndo(),
            onClick = state::undo,
        )
    )
    add(
        NativeEditorDockAction(
            id = "redo",
            label = stringResource(com.t8rin.imagetoolbox.core.resources.R.string.redo),
            icon = OneBoxIcons.Outlined.LineRedo,
            enabled = state.canRedo(),
            onClick = state::redo,
        )
    )
}

@Composable
private fun inlineStyleActions(state: NativeMarkdownEditorState): List<NativeEditorDockAction> {
    val scheme = rememberNativeMarkdownStyleScheme()
    val editor = state.richTextState()
    return scheme.inlineStyles.mapIndexed { index, definition ->
        NativeEditorDockAction(
            id = "inline_${definition.id}",
            label = definition.label,
            glyph = definition.glyph,
            glyphStyle = inlineGlyphStyle(definition.id),
            selected = editor?.isInlineStyleActive(definition.id, scheme) == true,
            enabled = editor != null && state.mode == NativeMarkdownEditorMode.Edit,
            dividerBefore = index == 0,
            onClick = { editor?.toggleInlineStyle(definition.id, scheme) },
        )
    }
}

@Composable
private fun blockStyleActions(state: NativeMarkdownEditorState): List<NativeEditorDockAction> {
    val scheme = rememberNativeMarkdownStyleScheme()
    val editor = state.richTextState()
    val activeBlockStyleId = editor?.currentBlockStyle(scheme)?.id
    val onApply: (String) -> Unit = { id -> editor?.applyBlockStyle(id, scheme) }
    return buildList {
        val bodyStyle = scheme.defaultBlockStyle
        add(
            NativeEditorDockAction(
                id = "block_body",
                label = bodyStyle.label,
                glyph = "T",
                selected = activeBlockStyleId == bodyStyle.id,
                enabled = editor != null && state.mode == NativeMarkdownEditorMode.Edit,
                dividerBefore = true,
                onClick = { onApply(bodyStyle.id) },
            )
        )
        headingSpecs().forEach { spec ->
            scheme.blockStyle(spec.styleId)?.let { style ->
                add(
                    blockStyleAction(
                        spec = BlockStyleSpec(
                            style = style,
                            glyph = spec.glyph,
                            label = stringResource(spec.labelRes),
                        ),
                        activeBlockStyleId = activeBlockStyleId,
                        enabled = editor != null && state.mode == NativeMarkdownEditorMode.Edit,
                        onApply = onApply,
                    )
                )
            }
        }
        scheme.blockStyle(NATIVE_EDITOR_QUOTE_ID)?.let { style ->
            add(
                blockStyleAction(
                    spec = BlockStyleSpec(style = style, glyph = "❝", label = style.label),
                    activeBlockStyleId = activeBlockStyleId,
                    enabled = editor != null && state.mode == NativeMarkdownEditorMode.Edit,
                    onApply = onApply,
                )
            )
        }
    }
}

private data class BlockStyleSpec(
    val style: RichTextBlockStyle,
    val glyph: String,
    val label: String,
)

private fun blockStyleAction(
    spec: BlockStyleSpec,
    activeBlockStyleId: String?,
    enabled: Boolean,
    onApply: (String) -> Unit,
): NativeEditorDockAction = NativeEditorDockAction(
    id = "block_${spec.style.id}",
    label = spec.label,
    glyph = spec.glyph,
    selected = activeBlockStyleId == spec.style.id,
    enabled = enabled,
    onClick = { onApply(spec.style.id) },
)

private data class HeadingSpec(
    val styleId: String,
    val glyph: String,
    val labelRes: Int,
    val style: RichTextBlockStyle? = null,
)

private fun headingSpecs(): List<HeadingSpec> = listOf(
    HeadingSpec(
        styleId = RichTextBlockStyle.Heading1Id,
        glyph = "H1",
        labelRes = R.string.native_editor_style_heading_1,
    ),
    HeadingSpec(
        styleId = RichTextBlockStyle.Heading2Id,
        glyph = "H2",
        labelRes = R.string.native_editor_style_heading_2,
    ),
    HeadingSpec(
        styleId = NATIVE_EDITOR_HEADING_3_ID,
        glyph = "H3",
        labelRes = R.string.native_editor_style_heading_3,
    ),
    HeadingSpec(
        styleId = NATIVE_EDITOR_HEADING_4_ID,
        glyph = "H4",
        labelRes = R.string.native_editor_style_heading_4,
    ),
)

data class NativeEditorExtraAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)
