/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
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

package com.wanbaohe.core.ui.widget.enhanced

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.wanbaohe.base.ui.shapes.BubbleShape
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.wanbaohe.core.ui.theme.outlineVariant
import kotlin.math.roundToInt

/** 箭头三角的高 / 半宽 */
private val BUBBLE_ARROW_SIZE = 10.dp

/** 气泡主体圆角 */
private val BUBBLE_CORNER_RADIUS = 16.dp

/** 菜单与锚点之间的留白 */
private val MENU_ANCHOR_GAP = 8.dp

/** 菜单与屏幕边缘的最小安全边距 */
private val MENU_SCREEN_MARGIN = 8.dp

/** 菜单最大宽度 */
private val MENU_MAX_WIDTH = 320.dp

/** 菜单内容默认上下内边距 */
private val MENU_CONTENT_PADDING = 8.dp

/** 判定「右边放不下」用的菜单最小宽度 */
private val MENU_MIN_WIDTH = 160.dp

/** 锚点容器最小尺寸(动作栏 0dp 约束下的兜底) */
private val MENU_ANCHOR_MIN_SIZE = 48.dp

/** 菜单与屏幕边缘的硬底线边距(为保箭头净空,默认边距最多让步到这里) */
private val MENU_SCREEN_HARD_MARGIN = 2.dp

/**
 * 气泡主体左边缘(窗口坐标),定位与箭头偏移的唯一事实来源。
 *
 * 优先级:① 气泡居中在入口正下方(箭尖自然对准入口中心);
 * ② 屏幕边缘收敛到 sideMargin;
 * ③ 若收敛后箭尖距贴边侧不足 [tipInset](箭尾会骑上圆角弧线,视觉上和气泡断开),
 *    继续让步边距、把气泡往入口方向推,直到净空满足或触底 MENU_SCREEN_HARD_MARGIN。
 */
private fun bodyLeftFor(
    anchorCenterX: Float,
    bodyWidth: Int,
    tipInset: Float,
    alignToEnd: Boolean,
    sideMargin: Float,
    hardMargin: Float,
    windowWidth: Int
): Float {
    val maxByMargin = (windowWidth - bodyWidth - sideMargin).coerceAtLeast(sideMargin)
    val hardMax = (windowWidth - bodyWidth - hardMargin).coerceAtLeast(hardMargin)
    val clamped = (anchorCenterX - bodyWidth / 2f).coerceIn(sideMargin, maxByMargin)
    return if (alignToEnd) {
        // 箭头贴右侧:气泡越靠左,箭尖距右缘越近;left ≥ minForArrow 才有净空
        val minForArrow = anchorCenterX + tipInset - bodyWidth
        if (clamped < minForArrow) minForArrow.coerceIn(hardMargin, hardMax) else clamped
    } else {
        // 箭头贴左侧:气泡越靠右,箭尖距左缘越近;left ≤ maxForArrow 才有净空
        val maxForArrow = anchorCenterX - tipInset
        if (clamped > maxForArrow) maxForArrow.coerceIn(hardMargin, hardMax) else clamped
    }.coerceIn(hardMargin, hardMax)
}

/**
 * 箭尖到气泡主体贴边那一侧的距离。
 *
 * 必须与定位同源:复用 [bodyLeftFor] 算出气泡 **clamp 之后**的最终位置,再按入口中心
 * 反推箭尖;实在放不下(入口贴死屏幕角落)才收紧到 tipInset,箭头依然干干净净落在直边上。
 */
private fun arrowOffsetFor(
    anchorCenterX: Float,
    bodyWidth: Int,
    tipInset: Float,
    sideMargin: Float,
    hardMargin: Float,
    windowWidth: Int,
    alignToEnd: Boolean
): Float {
    val maxPopupWidth = (windowWidth - sideMargin * 2).toInt().coerceAtLeast(0)
    val width = bodyWidth.coerceAtMost(maxPopupWidth)
    val bodyLeft = bodyLeftFor(
        anchorCenterX, width, tipInset, alignToEnd, sideMargin, hardMargin, windowWidth
    )
    // 箭尖距主体左缘 = 入口中心 - 主体左缘,收进 [tipInset, bodyWidth - tipInset]
    val tipFromLeft = (anchorCenterX - bodyLeft)
        .coerceIn(tipInset, (width - tipInset).coerceAtLeast(tipInset))
    return if (alignToEnd) width - tipFromLeft else tipFromLeft
}

/**
 * 全 App 统一的浮层菜单。
 *
 * 视觉与「主题快捷设置」弹窗一致:圆角气泡 + 指向点击位置的小箭头 +
 * `surface` 底色 + 2dp 阴影(开启玻璃主题时自动带磨砂;描边跟随外观设置)。
 * 箭尖落在气泡主体边缘,而气泡主体边缘对齐入口按钮中心,所以箭头始终正对入口图标。
 *
 * 用法与 Material3 的 `DropdownMenu` 基本一致:
 * ```
 * Box {
 *     IconButton(onClick = { expanded = true }) { Icon(Icons.Filled.MoreVert, null) }
 *     EnhancedDropdownMenu(
 *         expanded = expanded,
 *         onDismissRequest = { expanded = false }
 *     ) {
 *         DropdownMenuItem(text = { Text("…") }, onClick = { … })
 *     }
 * }
 * ```
 * 调用方最外层必须是包住入口按钮的那个 `Box` —— 组件靠它的位置算箭头指向。
 * 菜单直接挂在整条 `Row`/`TopAppBar actions` 里、没有专属 `Box` 时,传
 * `alignToAnchor = true` 改用系统给出的锚点矩形定位。
 * 触发器比图标大(如底栏胶囊按钮、被 `weight` 撑宽)时,传
 * `modifier = Modifier.matchParentSize()` 让锚点覆盖整个触发区域,否则只量到左上角 48dp。
 *
 * 箭头贴哪一边、气泡往哪边展开会自动判断:入口右边放得下就靠右(箭头从右上角指出),
 * 放不下就翻到左边。只有需要强制指定时才传 [arrowAlignment]。
 *
 * @param arrowAlignment 强制指定箭头位置([Alignment.End]/[Alignment.Start]);
 *                       默认 [Alignment.CenterHorizontally] 表示自动(它不是真的居中)。
 * @param expandUp 向上展开:气泡落在入口上方、箭头朝下(底部工具栏入口用)。
 * @param alignToAnchor 是否使用系统锚点矩形(默认用外层 `Box`,更准)。
 * @param offset 气泡与锚点之间的间距。
 * @param horizontalMargin 菜单距离屏幕左右边缘的最小安全边距。
 * @param maxWidth 菜单最大宽度。
 * @param maxHeight 菜单最大高度。
 */
@Composable
fun EnhancedDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    arrowAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    offset: Dp = MENU_ANCHOR_GAP,
    scrollState: ScrollState = rememberScrollState(),
    properties: PopupProperties = PopupProperties(focusable = true),
    expandUp: Boolean = false,
    alignToAnchor: Boolean = false,
    horizontalMargin: Dp = MENU_SCREEN_MARGIN,
    maxWidth: Dp = MENU_MAX_WIDTH,
    maxHeight: Dp = MENU_MAX_WIDTH * 2,
    contentPadding: PaddingValues = PaddingValues(vertical = MENU_CONTENT_PADDING),
    content: @Composable ColumnScope.() -> Unit
) {
    if (!expanded) return

    val settings = LocalSettingsState.current
    val density = LocalDensity.current
    val windowSize = LocalWindowInfo.current.containerSize

    val startPadding = contentPadding.calculateStartPadding(LayoutDirection.Ltr)
    val endPadding = contentPadding.calculateEndPadding(LayoutDirection.Ltr)
    // 气泡主体被箭头切掉一侧 [BUBBLE_ARROW_SIZE]：那一侧补回箭头高度，
    // 内容在气泡主体里的上下留白才和另一侧一致，而不是只有 2dp。
    val topPadding = contentPadding.calculateTopPadding() +
        if (expandUp) 0.dp else BUBBLE_ARROW_SIZE
    val bottomPadding = contentPadding.calculateBottomPadding() +
        if (expandUp) BUBBLE_ARROW_SIZE else 0.dp

    // 层叠定位容器的位置(包住入口按钮的那个 Box),用来算弹窗贴哪一边
    var containerBounds by remember { mutableStateOf<Rect?>(null) }

    // 气泡(含箭头外扩带)的实测宽度 —— 箭尖偏移由它推导
    var bubbleWidthPx by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            // 动作栏(actions 槽)测量时给的是 0dp 约束,不加最小尺寸会量出 0x0
            .defaultMinSize(minWidth = MENU_ANCHOR_MIN_SIZE, minHeight = MENU_ANCHOR_MIN_SIZE)
            .onGloballyPositioned { coordinates ->
                containerBounds = Rect(
                    offset = coordinates.positionInWindow(),
                    size = Size(
                        width = coordinates.size.width.toFloat(),
                        height = coordinates.size.height.toFloat()
                    )
                )
            }
    ) {
        // 入口在屏幕哪半边,气泡就往哪边展开(箭头贴那一侧)
        // 注意:不能用"右侧剩余宽度 < 菜单宽度"来判断 —— 右上角的 ⋯ 按钮只有 48dp,
        // 它右侧的剩余空间本来就不够一个菜单宽,那样会被误判成"放不下"而翻到左边。
        val isStartAligned = when (arrowAlignment) {
            Alignment.End -> false
            Alignment.Start -> true
            else -> {
                val bounds = containerBounds
                val minWidthPx = with(density) { MENU_MIN_WIDTH.toPx() }
                val centerX = bounds?.center?.x ?: (windowSize.width / 2f)
                val remainsRight = windowSize.width - centerX
                val remainsLeft = centerX
                // 右边剩余够放最小宽度,或者右边本来就比左边宽 → 靠右
                remainsRight >= minWidthPx || remainsRight >= remainsLeft
            }
        }

        val maxMenuHeight = minOf(
            maxHeight,
            with(density) {
                val reserved = offset + MENU_SCREEN_MARGIN + topPadding + bottomPadding
                (windowSize.height - reserved.roundToPx()).coerceAtLeast(0).toDp()
            }
        )

        // 箭尖对准入口中心:偏移按气泡实测宽度 + clamp 后的最终位置反推,
        // 和定位共用 bodyLeftFor(见 arrowOffsetFor),贴边时也不会漂
        val arrowOffset = if (bubbleWidthPx > 0) {
            with(density) {
                arrowOffsetFor(
                    anchorCenterX = containerBounds?.center?.x ?: (windowSize.width / 2f),
                    bodyWidth = bubbleWidthPx,
                    tipInset = (BUBBLE_CORNER_RADIUS + BUBBLE_ARROW_SIZE).toPx(),
                    sideMargin = horizontalMargin.toPx(),
                    hardMargin = MENU_SCREEN_HARD_MARGIN.toPx(),
                    windowWidth = windowSize.width,
                    alignToEnd = !isStartAligned
                ).toDp()
            }
        } else {
            BUBBLE_CORNER_RADIUS + BUBBLE_ARROW_SIZE
        }

        val shape = remember(isStartAligned, expandUp, arrowOffset) {
            BubbleShape(
                arrowSize = BUBBLE_ARROW_SIZE,
                arrowDirection = if (expandUp) {
                    BubbleShape.ArrowDirection.Bottom
                } else {
                    BubbleShape.ArrowDirection.Top
                },
                arrowAlignment = if (isStartAligned) {
                    BubbleShape.ArrowAlignment.Start
                } else {
                    BubbleShape.ArrowAlignment.End
                },
                arrowOffset = arrowOffset,
                cornerRadius = BUBBLE_CORNER_RADIUS
            )
        }
        Popup(
            popupPositionProvider = remember(
                containerBounds,
                windowSize,
                isStartAligned,
                alignToAnchor,
                expandUp,
                offset,
                horizontalMargin,
                density
            ) {
                BubbleMenuPositionProvider(
                    containerBounds = if (alignToAnchor) null else containerBounds,
                    alignToEnd = !isStartAligned,
                    expandUp = expandUp,
                    arrowSize = with(density) { BUBBLE_ARROW_SIZE.toPx() },
                    tipInset = with(density) { (BUBBLE_CORNER_RADIUS + BUBBLE_ARROW_SIZE).toPx() },
                    gap = with(density) { offset.toPx() },
                    sideMargin = with(density) { horizontalMargin.toPx() },
                    hardMargin = with(density) { MENU_SCREEN_HARD_MARGIN.toPx() }
                )
            },
            onDismissRequest = onDismissRequest,
            properties = properties
        ) {
            // 外层 Box 只限制最大宽度,气泡主体按内容自适应宽度
            Box(
                modifier = Modifier
                    .widthIn(max = maxWidth)
                    .onSizeChanged { bubbleWidthPx = it.width }
            ) {
                Surface(
                    shape = shape,
                    color = containerColor,
                    tonalElevation = 0.dp,
                    // 与主题快捷设置弹窗一致:2dp 阴影把气泡托起来;
                    // 描边跟随外观设置,默认不画(截图那种干净样式)
                    shadowElevation = 2.dp,
                    border = if (settings.borderWidth > 0.dp) {
                        BorderStroke(
                            width = settings.borderWidth,
                            color = MaterialTheme.colorScheme.outlineVariant()
                        )
                    } else null
                ) {
                    Column(
                        modifier = Modifier
                            .heightIn(max = maxMenuHeight)
                            .width(IntrinsicSize.Max)
                            .verticalScroll(scrollState)
                            .padding(
                                start = startPadding,
                                end = endPadding,
                                top = topPadding,
                                bottom = bottomPadding
                            ),
                        content = content
                    )
                }
            }
        }
    }
}

/**
 * 把菜单气泡放在锚点正下方:横向对齐锚点左/右边缘,箭头对准锚点中心。
 *
 * @param containerBounds 外层 `Box`(包住入口按钮那个)在窗口中的位置;为 null 时用系统给的锚点矩形。
 */
private class BubbleMenuPositionProvider(
    private val containerBounds: Rect?,
    private val alignToEnd: Boolean,
    private val expandUp: Boolean,
    private val arrowSize: Float,
    private val tipInset: Float,
    private val gap: Float,
    private val sideMargin: Float,
    private val hardMargin: Float
) : PopupPositionProvider {

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        // 层叠容器没测到尺寸(菜单挂在整条 Row 里之类)时,退回 Popup 自己算出来的锚点矩形
        val container = containerBounds?.takeIf { it.width > 0f && it.height > 0f }
        val anchorLeft = container?.left ?: anchorBounds.left.toFloat()
        val anchorRight = container?.right ?: anchorBounds.right.toFloat()
        val anchorTop = container?.top ?: anchorBounds.top.toFloat()
        val anchorBottom = container?.bottom ?: anchorBounds.bottom.toFloat()

        val maxPopupWidth = (windowSize.width - sideMargin * 2).toInt().coerceAtLeast(0)
        val popupWidth = popupContentSize.width.coerceAtMost(maxPopupWidth)
        // 位置与箭尖偏移共用 bodyLeftFor:贴边时优先保箭头净空(详见该函数注释)
        val bubbleLeft = bodyLeftFor(
            anchorCenterX = (anchorLeft + anchorRight) / 2f,
            bodyWidth = popupWidth,
            tipInset = tipInset,
            alignToEnd = alignToEnd,
            sideMargin = sideMargin,
            hardMargin = hardMargin,
            windowWidth = windowSize.width
        )
        val result = IntOffset(
            x = bubbleLeft.roundToInt(),
            y = if (expandUp) {
                // 气泡底边(含下方箭头)贴着入口上边
                (anchorTop - popupContentSize.height - gap + arrowSize).roundToInt()
            } else {
                (anchorBottom + gap - arrowSize).roundToInt()
            }
        )
        return result
    }
}
