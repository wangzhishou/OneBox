@file:Suppress("FunctionNaming")

package com.shifenmiao.common.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.R
import com.t8rin.imagetoolbox.core.resources.Icons as OneBoxIcons
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.Mic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAudioFile
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCheckBoxBlank
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLink
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTableChart
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
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
import com.wanbaohe.richtext.editor.style.RichTextInlineStyle

private val DockButtonShape = RoundedCornerShape(13.dp)
private const val DockCollapsedHeight = 34
private const val DockExpandedHeight = 46

/**
 * 编辑器的底部工具坞。
 *
 * 设计目标：
 * - 把模式切换、撤销重做、格式/插入操作全部移出正文上方，正文从标题栏下方直接开始；
 * - 收起态只保留 40dp 图标行，展开态显示文字标签，两个状态之间用高度/透明度动画过渡；
 * - 外层不铺整体背景，按钮/模式分段各自走玻璃管线，直接浮在页面主题背景上。
 */
@Composable
internal fun NativeMarkdownEditorDock(
    state: NativeMarkdownEditorState,
    extraActions: List<NativeEditorExtraAction>,
    modifier: Modifier = Modifier,
    onInsert: (NativeEditorInsertKind) -> Unit = {},
    onVoiceInput: (() -> Unit)? = null,
) {
    var expanded by rememberSaveable { mutableStateOf(true) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.86f,
                    stiffness = 420f,
                ),
            )
            .navigationBarsPadding()
            .padding(horizontal = 4.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        NativeMarkdownEditorModeToggle(state, expanded)
        Spacer(modifier = Modifier.width(1.dp))
        NativeMarkdownEditorDockActionRow(
            state = state,
            expanded = expanded,
            extraActions = extraActions,
            onInsert = onInsert,
            onVoiceInput = onVoiceInput,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(1.dp))
        NativeMarkdownEditorDockCollapseButton(
            expanded = expanded,
            onToggleExpanded = { expanded = !expanded },
        )
    }
}

@Composable
private fun NativeMarkdownEditorModeToggle(
    state: NativeMarkdownEditorState,
    expanded: Boolean,
) {
    val current = if (state.mode == NativeMarkdownEditorMode.Source) {
        NativeMarkdownEditorMode.Source
    } else {
        NativeMarkdownEditorMode.Edit
    }
    DockIconButton(
        label = stringResource(current.labelRes()),
        icon = current.icon(),
        expanded = expanded,
        onClick = {
            state.selectMode(
                if (current == NativeMarkdownEditorMode.Edit) {
                    NativeMarkdownEditorMode.Source
                } else {
                    NativeMarkdownEditorMode.Edit
                }
            )
        },
    )
}

@Composable
private fun NativeMarkdownEditorDockCollapseButton(
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 0f else 180f,
        animationSpec = tween(durationMillis = 220),
        label = "editor-dock-chevron",
    )
    DockIconChip(
        label = stringResource(
            if (expanded) {
                R.string.native_editor_toolbar_collapse
            } else {
                R.string.native_editor_toolbar_expand
            },
        ),
        icon = MaterialIcons.Filled.KeyboardArrowDown,
        expanded = expanded,
        onClick = onToggleExpanded,
        iconModifier = Modifier.rotate(chevronRotation),
    )
}

@Composable
private fun NativeMarkdownEditorDockActionRow(
    state: NativeMarkdownEditorState,
    expanded: Boolean,
    extraActions: List<NativeEditorExtraAction>,
    onInsert: (NativeEditorInsertKind) -> Unit,
    onVoiceInput: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val scheme = rememberNativeMarkdownStyleScheme()
    val editor = state.richTextState()
    val activeBlockStyleId = editor?.currentBlockStyle(scheme)?.id
    val codeTemplate = stringResource(R.string.native_editor_template_code)
    val diagramTemplate = stringResource(R.string.native_editor_template_diagram)
    val tableTemplate = stringResource(R.string.native_editor_template_table)
    val taskListTemplate = stringResource(R.string.native_editor_template_task_list)
    val actionScrollState = rememberScrollState()
    LaunchedEffect(Unit) {
        // 进入新编辑页时把工具条复位到最左侧，优先露出语音/撤销/重做。
        actionScrollState.scrollTo(0)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(actionScrollState),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            if (onVoiceInput != null) {
                DockIconButton(
                    label = stringResource(com.shifenmiao.core.R.string.ai_input_voice),
                    icon = OneBoxIcons.Outlined.Mic,
                    expanded = expanded,
                    onClick = onVoiceInput,
                )
            }
            DockIconButton(
                label = stringResource(com.t8rin.imagetoolbox.core.resources.R.string.undo),
                icon = OneBoxIcons.Outlined.LineUndo,
                expanded = expanded,
                enabled = state.canUndo(),
                onClick = state::undo,
            )
            DockIconButton(
                label = stringResource(com.t8rin.imagetoolbox.core.resources.R.string.redo),
                icon = OneBoxIcons.Outlined.LineRedo,
                expanded = expanded,
                enabled = state.canRedo(),
                onClick = state::redo,
            )
            DockDivider()

            scheme.inlineStyles.forEach { definition ->
                DockTextButton(
                    label = definition.label,
                    displayLabel = definition.glyph,
                    expanded = expanded,
                    selected = editor?.isInlineStyleActive(definition.id, scheme) == true,
                    enabled = editor != null,
                    glyphStyle = inlineGlyphStyle(definition.id),
                    onClick = { editor?.toggleInlineStyle(definition.id, scheme) },
                )
            }

            DockDivider()

            val bodyStyle = scheme.defaultBlockStyle
            DockTextButton(
                label = bodyStyle.label,
                displayLabel = "T",
                expanded = expanded,
                selected = activeBlockStyleId == bodyStyle.id,
                enabled = editor != null,
                onClick = { editor?.applyBlockStyle(bodyStyle.id, scheme) },
            )
            scheme.blockStyle(RichTextBlockStyle.Heading1Id)?.let { style ->
                DockTextButton(
                    label = stringResource(R.string.native_editor_style_heading_1),
                    displayLabel = "H1",
                    expanded = expanded,
                    selected = activeBlockStyleId == style.id,
                    enabled = editor != null,
                    onClick = { editor?.applyBlockStyle(style.id, scheme) },
                )
            }
            scheme.blockStyle(RichTextBlockStyle.Heading2Id)?.let { style ->
                DockTextButton(
                    label = stringResource(R.string.native_editor_style_heading_2),
                    displayLabel = "H2",
                    expanded = expanded,
                    selected = activeBlockStyleId == style.id,
                    enabled = editor != null,
                    onClick = { editor?.applyBlockStyle(style.id, scheme) },
                )
            }
            scheme.blockStyle(NATIVE_EDITOR_HEADING_3_ID)?.let { style ->
                DockTextButton(
                    label = stringResource(R.string.native_editor_style_heading_3),
                    displayLabel = "H3",
                    expanded = expanded,
                    selected = activeBlockStyleId == style.id,
                    enabled = editor != null,
                    onClick = { editor?.applyBlockStyle(style.id, scheme) },
                )
            }
            scheme.blockStyle(NATIVE_EDITOR_HEADING_4_ID)?.let { style ->
                DockTextButton(
                    label = stringResource(R.string.native_editor_style_heading_4),
                    displayLabel = "H4",
                    expanded = expanded,
                    selected = activeBlockStyleId == style.id,
                    enabled = editor != null,
                    onClick = { editor?.applyBlockStyle(style.id, scheme) },
                )
            }
            scheme.blockStyle(NATIVE_EDITOR_QUOTE_ID)?.let { style ->
                DockTextButton(
                    label = style.label,
                    displayLabel = "❝",
                    expanded = expanded,
                    selected = activeBlockStyleId == style.id,
                    enabled = editor != null,
                    onClick = { editor?.applyBlockStyle(style.id, scheme) },
                )
            }

            DockDivider()

            DockTextButton(
                label = stringResource(com.wanbaohe.richtext.editor.R.string.rich_text_bullet_list),
                displayLabel = "•",
                expanded = expanded,
                selected = editor?.isListActive(scheme) == true,
                enabled = editor != null,
                onClick = { editor?.toggleList(scheme) },
            )
            DockIconButton(
                label = stringResource(R.string.native_editor_insert_text),
                icon = MaterialIcons.Outlined.TextFields,
                expanded = expanded,
                onClick = state::insertTextBlock,
            )
            DockIconButton(
                label = stringResource(R.string.native_editor_insert_code),
                icon = MaterialIcons.Outlined.Code,
                expanded = expanded,
                onClick = {
                    state.insertRawBlock(
                        kind = MarkdownRawBlockKind.Code,
                        markdown = codeTemplate,
                    )
                },
            )
            DockIconButton(
                label = stringResource(R.string.native_editor_insert_diagram),
                icon = MaterialIcons.Outlined.AccountTree,
                expanded = expanded,
                onClick = {
                    state.insertRawBlock(
                        kind = MarkdownRawBlockKind.Diagram,
                        markdown = diagramTemplate,
                    )
                },
            )
            DockIconButton(
                label = stringResource(R.string.native_editor_insert_table),
                icon = OneBoxIcons.Outlined.LineTableChart,
                expanded = expanded,
                onClick = {
                    state.insertRawBlock(
                        kind = MarkdownRawBlockKind.Table,
                        markdown = tableTemplate,
                    )
                },
            )
            DockIconButton(
                label = stringResource(R.string.native_editor_insert_task_list),
                icon = OneBoxIcons.Outlined.LineCheckBoxBlank,
                expanded = expanded,
                onClick = {
                    state.insertRawBlock(
                        kind = MarkdownRawBlockKind.TaskList,
                        markdown = taskListTemplate,
                    )
                },
            )
            DockIconButton(
                label = stringResource(R.string.native_editor_insert_link),
                icon = OneBoxIcons.Outlined.LineLink,
                expanded = expanded,
                onClick = { onInsert(NativeEditorInsertKind.Link) },
            )
            DockIconButton(
                label = stringResource(R.string.native_editor_insert_image),
                icon = MaterialIcons.Outlined.Image,
                expanded = expanded,
                onClick = { onInsert(NativeEditorInsertKind.Image) },
            )
            DockIconButton(
                label = stringResource(R.string.native_editor_insert_audio),
                icon = OneBoxIcons.Outlined.LineAudioFile,
                expanded = expanded,
                onClick = { onInsert(NativeEditorInsertKind.Audio) },
            )
            DockIconButton(
                label = stringResource(R.string.native_editor_insert_video),
                icon = MaterialIcons.Outlined.PlayCircle,
                expanded = expanded,
                onClick = { onInsert(NativeEditorInsertKind.Video) },
            )

            if (extraActions.isNotEmpty()) {
                DockDivider()
                extraActions.forEach { action ->
                    DockIconButton(
                        label = action.label,
                        icon = action.icon,
                        expanded = expanded,
                        onClick = action.onClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun DockIconChip(
    label: String,
    icon: ImageVector,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    iconModifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val contentColor = when {
        !enabled -> colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        selected -> colorScheme.onPrimaryContainer
        else -> colorScheme.onSurfaceVariant
    }
    GlassSurface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(if (expanded) DockExpandedHeight.dp else DockCollapsedHeight.dp)
            .widthIn(min = if (expanded) 44.dp else 32.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = label
                role = Role.Button
                this.selected = selected
            },
        shape = DockButtonShape,
        color = if (selected) colorScheme.primaryContainer else colorScheme.surfaceContainer,
        contentColor = contentColor,
        style = if (selected) GlassStyle.Medium else GlassStyle.Thin,
        borderWidth = 0.7.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)
                    .then(iconModifier),
            )
        }
    }
}

@Composable
private fun DockDivider() {
    VerticalDivider(
        modifier = Modifier.height(24.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
private fun DockIconButton(
    label: String,
    icon: ImageVector,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    DockButtonSurface(
        label = label,
        expanded = expanded,
        selected = false,
        enabled = enabled,
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(19.dp),
        )
    }
}

@Composable
private fun DockTextButton(
    label: String,
    displayLabel: String,
    expanded: Boolean,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glyphStyle: TextStyle = MaterialTheme.typography.labelMedium,
) {
    DockButtonSurface(
        label = label,
        expanded = expanded,
        selected = selected,
        enabled = enabled,
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(
            text = displayLabel,
            style = glyphStyle,
            maxLines = 1,
        )
    }
}

@Composable
private fun DockButtonSurface(
    label: String,
    expanded: Boolean,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val contentColor = when {
        !enabled -> colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        selected -> colorScheme.onPrimaryContainer
        else -> colorScheme.onSurfaceVariant
    }

    GlassSurface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(
                if (expanded) DockExpandedHeight.dp else DockCollapsedHeight.dp,
            )
            .widthIn(min = if (expanded) 44.dp else 32.dp)
            .animateContentSize(tween(180))
            .semantics(mergeDescendants = true) {
                contentDescription = label
                role = Role.Button
            },
        shape = DockButtonShape,
        color = when {
            selected -> colorScheme.primaryContainer
            else -> colorScheme.surfaceContainer
        },
        contentColor = contentColor,
        style = if (selected) GlassStyle.Medium else GlassStyle.Thin,
        borderWidth = 0.7.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = if (expanded) 4.dp else 0.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            content()
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(130)) + expandVertically(tween(170)),
                exit = fadeOut(tween(80)) + shrinkVertically(tween(130)),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp),
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 78.dp),
                )
            }
        }
    }
}

@Composable
private fun inlineGlyphStyle(id: String): TextStyle {
    val base = MaterialTheme.typography.labelMedium
    return when (id) {
        RichTextInlineStyle.BoldId -> base.copy(fontWeight = FontWeight.Bold)
        RichTextInlineStyle.ItalicId -> base.copy(fontStyle = FontStyle.Italic)
        RichTextInlineStyle.UnderlineId -> base.copy(textDecoration = TextDecoration.Underline)
        RichTextInlineStyle.StrikethroughId -> base.copy(textDecoration = TextDecoration.LineThrough)
        else -> base
    }
}

private fun NativeMarkdownEditorMode.labelRes(): Int = when (this) {
    NativeMarkdownEditorMode.Edit -> R.string.native_editor_mode_edit
    NativeMarkdownEditorMode.Preview -> R.string.native_editor_mode_preview
    NativeMarkdownEditorMode.Source -> R.string.native_editor_mode_source
}

private fun NativeMarkdownEditorMode.icon(): ImageVector = when (this) {
    NativeMarkdownEditorMode.Edit -> OneBoxIcons.Outlined.Edit
    NativeMarkdownEditorMode.Preview -> MaterialIcons.Outlined.Visibility
    NativeMarkdownEditorMode.Source -> MaterialIcons.Outlined.Code
}

data class NativeEditorExtraAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)
