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

package com.shifenmiao.common.components.text

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.wanbaohe.base.ui.shapes.BubbleShape
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import kotlin.math.roundToInt

private val TOOLBAR_ARROW_SIZE = 10.dp
private val TOOLBAR_CORNER_RADIUS = 20.dp
private val TOOLBAR_ANCHOR_GAP = 12.dp

/**
 * 文字选区浮动操作条(AI 聊天 / 图文卡片 / 图片创作共用)。
 *
 * [anchorBounds] 是选区或就地编辑输入框在窗口坐标系里的真实 bounds
 * (不要传入系统 content rect:它的 bottom 被加过手柄留白)。
 * [preferAbove] 为 true 时优先贴在选区上方,上方放不下才翻到下方;
 * 默认仍优先下方。箭头指向选区水平中心,横向钳制在窗口内。
 *
 * 底色用选区手柄色(与高亮同一套强调色,不透底),图标用能看清的对比色。
 *
 * Popup 非 focusable:点操作按钮不会把焦点从输入框抢走,选区因此不会坍缩;
 * 同时它替代了系统文本菜单(见 [SuppressSystemTextSelectionMenu])。
 */
@Composable
fun TextSelectionToolbar(
    anchorBounds: Rect,
    actions: List<TextSelectionAction>,
    modifier: Modifier = Modifier,
    preferAbove: Boolean = false,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
) {
    if (actions.isEmpty()) return
    val density = LocalDensity.current
    val windowSize = LocalWindowInfo.current.containerSize
    val anchorCenterX = anchorBounds.center.x
    val anchorTop = anchorBounds.top
    val anchorBottom = anchorBounds.bottom
    val gap = with(density) { TOOLBAR_ANCHOR_GAP.toPx() }
    val arrowInset = with(density) { (TOOLBAR_CORNER_RADIUS + TOOLBAR_ARROW_SIZE).toPx() }
    var toolbarSize by remember { mutableStateOf(IntSize.Zero) }
    val placement = placeSelectionToolbar(
        anchorTop = anchorTop,
        anchorBottom = anchorBottom,
        anchorCenterX = anchorCenterX,
        popupWidth = toolbarSize.width,
        popupHeight = toolbarSize.height,
        windowWidth = windowSize.width,
        windowHeight = windowSize.height,
        gapPx = gap,
        arrowInsetPx = arrowInset,
        preferAbove = preferAbove,
    )
    val shape = remember(placement.above, placement.arrowOffsetPx, density) {
        BubbleShape(
            arrowSize = TOOLBAR_ARROW_SIZE,
            arrowDirection = if (placement.above) {
                BubbleShape.ArrowDirection.Bottom
            } else {
                BubbleShape.ArrowDirection.Top
            },
            arrowAlignment = BubbleShape.ArrowAlignment.Start,
            arrowOffset = with(density) { placement.arrowOffsetPx.toDp() },
            cornerRadius = TOOLBAR_CORNER_RADIUS
        )
    }
    val positionProvider = remember(
        anchorCenterX,
        anchorTop,
        anchorBottom,
        gap,
        arrowInset,
        preferAbove,
    ) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
            ): IntOffset = placeSelectionToolbar(
                anchorTop = anchorTop,
                anchorBottom = anchorBottom,
                anchorCenterX = anchorCenterX,
                popupWidth = popupContentSize.width,
                popupHeight = popupContentSize.height,
                windowWidth = windowSize.width,
                windowHeight = windowSize.height,
                gapPx = gap,
                arrowInsetPx = arrowInset,
                preferAbove = preferAbove,
            ).offset
        }
    }
    val handleColor = LocalTextSelectionColors.current.handleColor
    val menuBackground = if (handleColor.alpha >= 1f) {
        handleColor
    } else {
        handleColor.compositeOver(MaterialTheme.colorScheme.surface)
    }
    val menuContent = contentColorOn(menuBackground)

    Popup(
        popupPositionProvider = positionProvider,
        properties = PopupProperties(
            focusable = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            clippingEnabled = false,
            usePlatformDefaultWidth = false
        )
    ) {
        // 实底,避免玻璃透出底下的文字;颜色跟选区手柄走同一套强调色
        GlassCard(
            shape = shape,
            style = GlassStyle.None,
            colors = CardDefaults.cardColors(
                containerColor = menuBackground,
                contentColor = menuContent,
            ),
            modifier = modifier.onSizeChanged { toolbarSize = it }
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 6.dp,
                    end = 6.dp,
                    top = 6.dp + if (placement.above) 0.dp else TOOLBAR_ARROW_SIZE,
                    bottom = 6.dp + if (placement.above) TOOLBAR_ARROW_SIZE else 0.dp
                )
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    actions.forEach { action ->
                        TextSelectionActionButton(
                            action = action,
                            contentColor = menuContent,
                        )
                    }
                }
                if (footer != null) {
                    Column(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .widthIn(max = 320.dp)
                    ) {
                        footer()
                    }
                }
            }
        }
    }
}

/** 工具条上的一个动作:文字字形(B/I)、图标(复制/粘贴)或颜色圆点三者取其一 */
@Immutable
data class TextSelectionAction(
    val key: String,
    val label: String,
    val onClick: () -> Unit,
    val glyph: String? = null,
    val icon: ImageVector? = null,
    val swatch: Color? = null,
    val selected: Boolean = false,
    val tint: Color? = null,
    /** 是否在图标下方显示 [label]。只读内容页(聊天/博客/笔记)开启，就地编辑器保持纯图标。 */
    val showLabel: Boolean = false,
)

@Composable
fun TextSelectionActionButton(
    action: TextSelectionAction,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    val container = if (action.selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    val content = when {
        action.selected -> MaterialTheme.colorScheme.onPrimaryContainer
        action.tint != null -> action.tint
        else -> contentColor
    }
    val showLabel = action.showLabel && action.label.isNotBlank()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .then(
                if (showLabel) {
                    Modifier.widthIn(min = 52.dp, max = 72.dp)
                } else {
                    Modifier.size(36.dp)
                }
            )
            .clip(if (showLabel) RoundedCornerShape(12.dp) else CircleShape)
            .background(if (showLabel) Color.Transparent else container)
            .clickable(onClick = action.onClick)
            .semantics { contentDescription = action.label }
            .padding(if (showLabel) 3.dp else 0.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(if (showLabel) 30.dp else 36.dp)
                .clip(CircleShape)
                .background(container)
        ) {
            when {
                action.swatch != null -> Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(action.swatch)
                )
                action.glyph != null -> Text(
                    text = action.glyph,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = content
                )
                action.icon != null -> Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        if (showLabel) {
            Text(
                text = action.label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = content,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

private class ToolbarPlacement(
    val offset: IntOffset,
    val above: Boolean,
    val arrowOffsetPx: Float,
)

/**
 * 箭头尖端落在选区外 [gapPx] 处,水平对齐选区中心。
 * [preferAbove] 时优先上方;放不下才换边。尺寸还是 0 时按偏好边给箭头方向,避免首帧翻面。
 */
private fun placeSelectionToolbar(
    anchorTop: Float,
    anchorBottom: Float,
    anchorCenterX: Float,
    popupWidth: Int,
    popupHeight: Int,
    windowWidth: Int,
    windowHeight: Int,
    gapPx: Float,
    arrowInsetPx: Float,
    preferAbove: Boolean,
): ToolbarPlacement {
    val edge = gapPx
    val maxX = (windowWidth - popupWidth - edge).coerceAtLeast(edge)
    val maxY = (windowHeight - popupHeight - edge).coerceAtLeast(edge)
    val aboveY = anchorTop - popupHeight - gapPx
    val belowY = anchorBottom + gapPx
    val aboveFits = popupHeight > 0 && aboveY >= edge
    val belowFits = popupHeight > 0 && belowY <= maxY
    val above = when {
        popupHeight <= 0 -> preferAbove
        preferAbove && aboveFits -> true
        !preferAbove && belowFits -> false
        aboveFits -> true
        else -> false
    }
    val x = (anchorCenterX - popupWidth / 2f).coerceIn(edge, maxX)
    val y = (if (above) aboveY else belowY).coerceIn(edge, maxY)
    val arrowOffset = if (popupWidth > 0) {
        (anchorCenterX - x).coerceIn(
            arrowInsetPx,
            (popupWidth - arrowInsetPx).coerceAtLeast(arrowInsetPx),
        )
    } else {
        arrowInsetPx
    }
    return ToolbarPlacement(
        offset = IntOffset(x.roundToInt(), y.roundToInt()),
        above = above,
        arrowOffsetPx = arrowOffset,
    )
}

/** 实底上的可读前景:浅底用深字,深底用浅字,取当前主题里对应的那一对 */
@Composable
private fun contentColorOn(background: Color): Color {
    val scheme = MaterialTheme.colorScheme
    val lightSurface = scheme.surface.luminance() > 0.5f
    val darkText = if (lightSurface) scheme.onSurface else scheme.inverseOnSurface
    val lightText = if (lightSurface) scheme.inverseOnSurface else scheme.onSurface
    return if (background.luminance() > 0.4f) darkText else lightText
}
