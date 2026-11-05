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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.wanbaohe.base.ui.shapes.BubbleShape
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.tintedGlassCardColors
import kotlin.math.roundToInt

private val TOOLBAR_ARROW_SIZE = 10.dp
private val TOOLBAR_CORNER_RADIUS = 20.dp
private val TOOLBAR_ANCHOR_GAP = 8.dp

/**
 * 文字选区浮动操作条(AI 聊天 / 图文卡片 / 图片创作共用)。
 *
 * [anchorBounds] 是选区或就地编辑输入框在窗口坐标系里的 bounds。
 * 工具条默认弹在下方,空间不足时翻到上方,箭头始终指向锚点中心;
 * 横向钳制在窗口内,每次选区/布局变化都会重算。
 *
 * Popup 非 focusable:点操作按钮不会把焦点从输入框抢走,选区因此不会坍缩;
 * 同时它替代了系统文本菜单(见 [SuppressSystemTextSelectionMenu])。
 */
@Composable
fun TextSelectionToolbar(
    anchorBounds: Rect,
    actions: List<TextSelectionAction>,
    modifier: Modifier = Modifier,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
) {
    if (actions.isEmpty()) return
    val density = LocalDensity.current
    val windowSize = LocalWindowInfo.current.containerSize
    val anchorCenterX = anchorBounds.center.x
    val anchorTop = anchorBounds.top
    val anchorBottom = anchorBounds.bottom
    val gap = with(density) { TOOLBAR_ANCHOR_GAP.toPx() }
    val arrowSize = with(density) { TOOLBAR_ARROW_SIZE.toPx() }
    val arrowInset = with(density) { (TOOLBAR_CORNER_RADIUS + TOOLBAR_ARROW_SIZE).toPx() }
    var toolbarSize by remember { mutableStateOf(IntSize.Zero) }
    val expandUp = toolbarSize.height > 0 &&
        anchorBottom + gap - arrowSize + toolbarSize.height > windowSize.height - gap
    val maxX = (windowSize.width - toolbarSize.width - gap).coerceAtLeast(gap)
    val toolbarLeft = (anchorCenterX - toolbarSize.width / 2f).coerceIn(gap, maxX)
    val arrowOffset = if (toolbarSize.width > 0) {
        (anchorCenterX - toolbarLeft).coerceIn(
            arrowInset,
            (toolbarSize.width - arrowInset).coerceAtLeast(arrowInset)
        )
    } else {
        arrowInset
    }
    val shape = remember(expandUp, arrowOffset, density) {
        BubbleShape(
            arrowSize = TOOLBAR_ARROW_SIZE,
            arrowDirection = if (expandUp) {
                BubbleShape.ArrowDirection.Bottom
            } else {
                BubbleShape.ArrowDirection.Top
            },
            arrowAlignment = BubbleShape.ArrowAlignment.Start,
            arrowOffset = with(density) { arrowOffset.toDp() },
            cornerRadius = TOOLBAR_CORNER_RADIUS
        )
    }
    val positionProvider = remember(anchorCenterX, anchorTop, anchorBottom, density) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
            ): IntOffset {
                val gap = with(density) { TOOLBAR_ANCHOR_GAP.toPx() }
                val arrowSize = with(density) { TOOLBAR_ARROW_SIZE.toPx() }
                val maxX = (windowSize.width - popupContentSize.width - gap).coerceAtLeast(gap)
                val maxY = (windowSize.height - popupContentSize.height - gap).coerceAtLeast(gap)
                val x = (anchorCenterX - popupContentSize.width / 2f)
                    .roundToInt()
                    .coerceIn(gap.roundToInt(), maxX.roundToInt())
                val above = anchorTop - popupContentSize.height - gap + arrowSize
                val below = anchorBottom + gap - arrowSize
                val y = (if (below <= maxY) below else above)
                    .roundToInt()
                    .coerceIn(gap.roundToInt(), maxY.roundToInt())
                return IntOffset(x, y)
            }
        }
    }

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
        // 浮在任意画布/文字之上,玻璃太透会看不清操作件:这里用玻璃管线的实底档
        GlassCard(
            shape = shape,
            style = GlassStyle.None,
            colors = tintedGlassCardColors(MaterialTheme.colorScheme.surface),
            modifier = modifier.onSizeChanged { toolbarSize = it }
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 6.dp,
                    end = 6.dp,
                    top = 6.dp + if (expandUp) 0.dp else TOOLBAR_ARROW_SIZE,
                    bottom = 6.dp + if (expandUp) TOOLBAR_ARROW_SIZE else 0.dp
                )
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    actions.forEach { action ->
                        TextSelectionActionButton(action = action)
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
)

@Composable
fun TextSelectionActionButton(
    action: TextSelectionAction,
    modifier: Modifier = Modifier,
) {
    val container = if (action.selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    val content = when {
        action.selected -> MaterialTheme.colorScheme.onPrimaryContainer
        action.tint != null -> action.tint
        else -> MaterialTheme.colorScheme.onSurface
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(container)
            .clickable(onClick = action.onClick)
            .semantics { contentDescription = action.label }
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
}
