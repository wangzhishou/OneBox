@file:Suppress("FunctionNaming")

package com.shifenmiao.common.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAudioFile
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLink
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
import com.wanbaohe.core.ui.widget.glass.GlassSegmentedButtonRow
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
import com.wanbaohe.richtext.editor.style.RichTextStyleScheme

private val DockShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
private val DockButtonShape = RoundedCornerShape(13.dp)
private const val DockCollapsedHeight = 34
private const val DockExpandedHeight = 46

/**
 * 编辑器的底部工具坞。
 *
 * 设计目标：
 * - 把模式切换、撤销重做、格式/插入操作全部移出正文上方，正文从标题栏下方直接开始；
 * - 收起态只保留 40dp 图标行，展开态显示文字标签，两个状态之间用高度/透明度动画过渡；
 * - 所有容器走玻璃管线，颜色全部取自 MaterialTheme，跟随浅色/深色/动态取色与全局背景。
 */
@Composable
internal fun NativeMarkdownEditorDock(
    state: NativeMarkdownEditorState,
    extraActions: List<NativeEditorExtraAction>,
    modifier: Modifier = Modifier,
    onInsert: (NativeEditorInsertKind) -> Unit = {},
) {
    var expanded by rememberSaveable { mutableStateOf(true) }

    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.86f,
                    stiffness = 420f,
                ),
            ),
        shape = DockShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        style = GlassStyle.Thick,
        borderWidth = 0.7.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 6.dp, bottom = 6.dp),
        ) {
            NativeMarkdownEditorDockModeRow(
                state = state,
                expanded = expanded,
                onToggleExpanded = { expanded = !expanded },
            )
            Spacer(modifier = Modifier.height(2.dp))
            NativeMarkdownEditorDockActionRow(
                state = state,
                expanded = expanded,
                extraActions = extraActions,
                onInsert = onInsert,
            )
        }
    }
}

@Composable
private fun NativeMarkdownEditorDockModeRow(
    state: NativeMarkdownEditorState,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 0f else 180f,
        animationSpec = tween(durationMillis = 220),
        label = "editor-dock-chevron",
    )
    val focusManager = LocalFocusManager.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        DockHeaderIconButton(
            label = stringResource(com.t8rin.imagetoolbox.core.resources.R.string.undo),
            icon = OneBoxIcons.Outlined.LineUndo,
            enabled = state.canUndo(),
            onClick = state::undo,
        )
        DockHeaderIconButton(
            label = stringResource(com.t8rin.imagetoolbox.core.resources.R.string.redo),
            icon = OneBoxIcons.Outlined.LineRedo,
            enabled = state.canRedo(),
            onClick = state::redo,
        )
        Spacer(modifier = Modifier.width(2.dp))
        GlassSegmentedButtonRow(
            options = NativeMarkdownEditorMode.entries.toList(),
            selectedOption = state.mode,
            onOptionSelected = { mode ->
                if (mode == NativeMarkdownEditorMode.Preview) {
                    // 预览是只读的，切换过去时收起键盘，避免遮住正文。
                    focusManager.clearFocus(force = true)
                }
                state.selectMode(mode)
            },
            modifier = Modifier.weight(1f),
            rowStyle = GlassStyle.Thin,
            buttonHeight = 30.dp,
            selectedColor = MaterialTheme.colorScheme.primaryContainer,
            selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            label = { mode ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Icon(
                        imageVector = mode.icon(),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )
                    AnimatedVisibility(
                        visible = expanded,
                        enter = fadeIn(tween(140)) + expandHorizontally(tween(180)),
                        exit = fadeOut(tween(90)) + shrinkHorizontally(tween(140)),
                    ) {
                        Text(
                            text = stringResource(mode.labelRes()),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                        )
                    }
                }
            },
        )
        Spacer(modifier = Modifier.width(2.dp))
        val toggleLabel = stringResource(
            if (expanded) {
                R.string.native_editor_toolbar_collapse
            } else {
                R.string.native_editor_toolbar_expand
            },
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable(onClick = onToggleExpanded)
                .semantics {
                    contentDescription = toggleLabel
                    role = Role.Button
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = MaterialIcons.Filled.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(chevronRotation),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun NativeMarkdownEditorDockActionRow(
    state: NativeMarkdownEditorState,
    expanded: Boolean,
    extraActions: List<NativeEditorExtraAction>,
    onInsert: (NativeEditorInsertKind) -> Unit,
) {
    val scheme = rememberNativeMarkdownStyleScheme()
    val editor = state.richTextState()
    val activeBlockStyleId = editor?.currentBlockStyle(scheme)?.id
    val codeTemplate = stringResource(R.string.native_editor_template_code)
    val diagramTemplate = stringResource(R.string.native_editor_template_diagram)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
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
private fun DockHeaderIconButton(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val color = if (enabled) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
    }
    GlassSurface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(32.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = label
                role = Role.Button
            },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = color,
        style = GlassStyle.Thin,
        borderWidth = 0.6.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(17.dp),
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
            .widthIn(min = if (expanded) 46.dp else 34.dp)
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
                .padding(horizontal = if (expanded) 6.dp else 0.dp, vertical = 3.dp),
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
