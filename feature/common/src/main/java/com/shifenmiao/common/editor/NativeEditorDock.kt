/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.R
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import androidx.compose.material.icons.Icons as MaterialIcons

private val DockButtonShape = RoundedCornerShape(13.dp)
private const val DOCK_COLLAPSED_HEIGHT = 34
private const val DOCK_EXPANDED_HEIGHT = 46
private const val DISABLED_ALPHA = 0.38f
private const val DOCK_LABEL_FONT_SIZE = 10
private const val DOCK_LABEL_LINE_HEIGHT = 12
private const val DOCK_LABEL_MAX_WIDTH = 78
private const val DOCK_SIZE_ANIMATION_MS = 180
private const val DOCK_LABEL_FADE_IN_MS = 130
private const val DOCK_LABEL_EXPAND_MS = 170
private const val DOCK_LABEL_FADE_OUT_MS = 80
private const val DOCK_LABEL_SHRINK_MS = 130
private val DOCK_MIN_WIDTH_EXPANDED = 44.dp
private val DOCK_MIN_WIDTH_COLLAPSED = 32.dp
private val DOCK_BORDER_WIDTH = 0.7.dp
private val DOCK_CONTENT_HORIZONTAL_PADDING = 4.dp
private val DOCK_CONTENT_VERTICAL_PADDING = 2.dp

/**
 * 底部工具坞里的一个动作。
 *
 * 图标动作给 [icon]；纯文字动作（如 Markdown 的 B/I/H1、代码编辑器的 "//" 注释）给 [glyph]。
 * [dividerBefore] 用来分组，视觉上等价于手工插入的竖分隔线。
 */
data class NativeEditorDockAction(
    val id: String,
    val label: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    val glyph: String? = null,
    val glyphStyle: TextStyle? = null,
    val selected: Boolean = false,
    val enabled: Boolean = true,
    val dividerBefore: Boolean = false,
)

/**
 * Markdown 编辑器与代码编辑器共用的底部工具坞。
 *
 * 设计目标（沿用 NativeMarkdownEditorDock）：
 * - 把撤销/重做、格式化/插入操作全部移出正文上方，正文从标题栏下方直接开始；
 * - 收起态只保留图标行，展开态显示文字标签，两个状态之间用高度/透明度动画过渡；
 * - 外层不铺整体背景，按钮各自走玻璃管线，直接浮在页面主题背景上。
 */
@Composable
fun NativeEditorDock(
    actions: List<NativeEditorDockAction>,
    modifier: Modifier = Modifier,
    leading: (@Composable (expanded: Boolean) -> Unit)? = null,
    initiallyExpanded: Boolean = true,
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }

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
        leading?.invoke(expanded)
        if (leading != null) {
            Spacer(modifier = Modifier.width(1.dp))
        }
        NativeEditorDockActionRow(
            actions = actions,
            expanded = expanded,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(1.dp))
        NativeEditorDockCollapseButton(
            expanded = expanded,
            onToggleExpanded = { expanded = !expanded },
        )
    }
}

@Composable
private fun NativeEditorDockActionRow(
    actions: List<NativeEditorDockAction>,
    expanded: Boolean,
    modifier: Modifier = Modifier,
) {
    val actionScrollState = rememberScrollState()
    LaunchedEffect(Unit) {
        // 进入新的编辑页时把工具条复位到最左侧，优先露出撤销/重做。
        actionScrollState.scrollTo(0)
    }
    Row(
        modifier = modifier
            .horizontalScroll(actionScrollState),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        actions.forEach { action ->
            if (action.dividerBefore) {
                NativeEditorDockDivider()
            }
            NativeEditorDockActionButton(action = action, expanded = expanded)
        }
    }
}

@Composable
private fun NativeEditorDockActionButton(
    action: NativeEditorDockAction,
    expanded: Boolean,
) {
    if (action.icon != null) {
        NativeEditorDockIconButton(
            label = action.label,
            icon = action.icon,
            expanded = expanded,
            enabled = action.enabled,
            selected = action.selected,
            onClick = action.onClick,
        )
    } else {
        NativeEditorDockTextButton(
            label = action.label,
            displayLabel = action.glyph.orEmpty(),
            expanded = expanded,
            selected = action.selected,
            enabled = action.enabled,
            glyphStyle = action.glyphStyle ?: MaterialTheme.typography.labelMedium,
            onClick = action.onClick,
        )
    }
}

@Composable
private fun NativeEditorDockCollapseButton(
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 0f else 180f,
        animationSpec = tween(durationMillis = 220),
        label = "editor-dock-chevron",
    )
    NativeEditorDockChip(
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
        showLabel = false,
    )
}

/**
 * 工具坞里带文字的独立控件（Markdown 的模式切换、代码编辑器的语言选择）。
 */
@Suppress("LongParameterList") // Compose 控件：尺寸/状态/图标都可选，拆对象反而更难用
@Composable
fun NativeEditorDockChip(
    label: String,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    icon: ImageVector? = null,
    glyph: String? = null,
    iconModifier: Modifier = Modifier,
    showLabel: Boolean = true,
) {
    NativeEditorDockButtonSurface(
        label = label,
        expanded = expanded,
        selected = selected,
        enabled = enabled,
        onClick = onClick,
        modifier = modifier,
        showLabel = showLabel,
    ) {
        when {
            icon != null -> Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(19.dp)
                    .then(iconModifier),
            )

            glyph != null -> Text(
                text = glyph,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun NativeEditorDockDivider() {
    VerticalDivider(
        modifier = Modifier.height(24.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Suppress("LongParameterList")
@Composable
private fun NativeEditorDockIconButton(
    label: String,
    icon: ImageVector,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
) {
    NativeEditorDockButtonSurface(
        label = label,
        expanded = expanded,
        selected = selected,
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

@Suppress("LongParameterList")
@Composable
private fun NativeEditorDockTextButton(
    label: String,
    displayLabel: String,
    expanded: Boolean,
    selected: Boolean,
    enabled: Boolean,
    glyphStyle: TextStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NativeEditorDockButtonSurface(
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
private fun DockButtonLabel(
    label: String,
    visible: Boolean,
    contentColor: Color,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(DOCK_LABEL_FADE_IN_MS)) + expandVertically(tween(DOCK_LABEL_EXPAND_MS)),
        exit = fadeOut(tween(DOCK_LABEL_FADE_OUT_MS)) + shrinkVertically(tween(DOCK_LABEL_SHRINK_MS)),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = DOCK_LABEL_FONT_SIZE.sp,
                lineHeight = DOCK_LABEL_LINE_HEIGHT.sp,
            ),
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = DOCK_LABEL_MAX_WIDTH.dp),
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun NativeEditorDockButtonSurface(
    label: String,
    expanded: Boolean,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val contentColor = when {
        !enabled -> colorScheme.onSurfaceVariant.copy(alpha = DISABLED_ALPHA)
        selected -> colorScheme.onPrimaryContainer
        else -> colorScheme.onSurfaceVariant
    }

    GlassSurface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(
                if (expanded) DOCK_EXPANDED_HEIGHT.dp else DOCK_COLLAPSED_HEIGHT.dp,
            )
            .widthIn(
                min = if (expanded) DOCK_MIN_WIDTH_EXPANDED else DOCK_MIN_WIDTH_COLLAPSED,
            )
            .animateContentSize(tween(DOCK_SIZE_ANIMATION_MS))
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
        borderWidth = DOCK_BORDER_WIDTH,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(
                    horizontal = if (expanded) DOCK_CONTENT_HORIZONTAL_PADDING else 0.dp,
                    vertical = DOCK_CONTENT_VERTICAL_PADDING,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            content()
            DockButtonLabel(
                label = label,
                visible = expanded && showLabel,
                contentColor = contentColor,
            )
        }
    }
}
