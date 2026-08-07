package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.resources.icons.Check

// ──────────────────────────────────────────────────────────────
//  毛玻璃分段选择按钮行 —— 替代 Material3 SingleChoiceSegmentedButtonRow
// ──────────────────────────────────────────────────────────────

/**
 * 毛玻璃分段选择按钮行 —— Material3 `SingleChoiceSegmentedButtonRow` 的对齐命名。
 *
 * 与 [GlassSegmentedButtonRow] 是同一个组件。提供此别名以便在导入和调用处
 * 直接沿用 Material3 官方命名（`SingleChoiceGlassSegmentedButtonRow` ↔
 * `SingleChoiceSegmentedButtonRow`），降低学习与迁移成本。
 *
 * 与标准 SegmentedButtonRow 的区别：
 * - **外层容器**使用毛玻璃背景（[GlassSurface]），带半透明底色 + 描边
 * - **每个按钮独立圆角** —— 中间按钮也拥有完整圆角，不再是传统的平边拼接
 * - **选中 / 未选中态**分别使用不同浓度的玻璃样式，选中态有色调叠加 + 描边
 * - 支持选中时显示动画图标（缩放 + 淡入淡出）
 *
 * 遵循全局设置自动切换：
 * - `isGlassmorphismEnabled == false`：降级为纯色背景
 * - `isLiquidGlassEnabled == true`（需毛玻璃开启）：自动增强 Liquid Glass 效果
 *
 * ```
 * ╭─────────────────────────────────────────╮  ← rowShape 外层容器
 * │  ╭──────────╮  ╭──────────╮  ╭────────╮ │
 * │  │ ✓ 选项A  │  │  选项B   │  │ 选项C  │ │  ← buttonShape 每个按钮独立圆角
 * │  ╰──────────╯  ╰──────────╯  ╰────────╯ │
 * ╰─────────────────────────────────────────╯
 * ```
 *
 * ### 快速使用
 *
 * ```kotlin
 * var selected by remember { mutableStateOf(options.first()) }
 * GlassSegmentedButtonRow(
 *     options = options,
 *     selectedOption = selected,
 *     onOptionSelected = { selected = it },
 *     modifier = Modifier.fillMaxWidth(),
 *     label = { Text(it.title) },
 *     selectedIcon = {
 *         Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check, null, Modifier.size(16.dp))
 *     },
 * )
 * ```
 *
 * @param T                        选项类型
 * @param options                  选项列表
 * @param selectedOption           当前选中项
 * @param onOptionSelected         选项点击回调
 * @param modifier                 外层容器修饰符
 * @param label                    按钮文字内容
 * @param selectedIcon             选中态动画图标（为 null 时不显示）
 * @param rowShape                 外层容器形状，默认 12 dp 圆角
 * @param buttonShape              单个按钮形状，默认 10 dp 圆角（嵌套在 row 内，略小于 row 圆角）
 * @param spacing                  按钮间距，默认 4 dp
 * @param contentPadding           内容内边距（外层容器到按钮的间距），默认 4 dp
 * @param buttonHeight             按钮高度，为 null 时由内容决定
 * @param hugContent               为 true 时整行包裹内容宽度：按钮不再均分可用宽度，
 *                                 而是各自按内容测量，指示器按各按钮实测宽度定位。
 *                                 适合选项文字长短不一、不想被省略号截断的场景
 * @param rowStyle                 外层容器玻璃浓度
 * @param rowColor                 外层容器色调
 * @param selectedStyle            选中态按钮玻璃浓度
 * @param selectedColor            选中态按钮色调
 * @param selectedColorForOption   根据当前选中项动态计算滑块色调；传入后优先级高于 selectedColor
 * @param selectedContentColor     选中态内容（文字/图标）颜色
 * @param unselectedContentColor   未选中态内容颜色
 * @param unselectedColor          未选中态按钮背景色
 * @param rowBorderWidth           外层容器描边宽度
 * @param buttonBorderWidth        选中态按钮描边宽度
 */
@Composable
fun <T> GlassSegmentedButtonRow(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (T) -> Unit,
    selectedIcon: (@Composable () -> Unit)? = null,
    rowShape: Shape = MaterialTheme.shapes.large,
    buttonShape: Shape = MaterialTheme.shapes.large,
    spacing: Dp = 4.dp,
    contentPadding: PaddingValues = PaddingValues(2.5.dp),
    buttonHeight: Dp? = null,
    hugContent: Boolean = false,
    rowStyle: GlassStyle = GlassStyle.Regular,
    rowColor: Color = Color.Unspecified,
    selectedStyle: GlassStyle = GlassStyle.Dense,
    selectedColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedColorForOption: ((T) -> Color)? = null,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedColor: Color = Color.Transparent,
    rowBorderWidth: Dp = 0.9.dp,
    buttonBorderWidth: Dp = 0.9.dp,
) {
    val bgColor = if (rowColor != Color.Unspecified) rowColor
    else MaterialTheme.colorScheme.surfaceContainerHigh
    if (options.isEmpty()) return

    val rowWidthModifier = if (hugContent) Modifier.wrapContentWidth() else Modifier.fillMaxWidth()

    Box(
        modifier = modifier
            .clip(rowShape)
            .glassBackground(
                color = bgColor,
                shape = rowShape,
                borderWidth = rowBorderWidth,
                style = rowStyle
            )
            .then(rowWidthModifier),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .padding(contentPadding)
                .then(rowWidthModifier)
        ) {
            val selectedIndex = options.indexOf(selectedOption).coerceAtLeast(0)
            val currentSelectedOption = options[selectedIndex]
            val buttonCount = options.size
            val density = LocalDensity.current

            // hugContent 模式下按钮宽度不等,需按各按钮实测宽度定位指示器
            val buttonWidthsPx = remember(options) { mutableStateMapOf<Int, Int>() }
            val indicatorWidth: Dp
            val indicatorTargetOffset: Dp
            if (hugContent) {
                val spacingPx = with(density) { spacing.roundToPx() }
                var offsetPx = 0
                for (index in 0 until selectedIndex) {
                    offsetPx += (buttonWidthsPx[index] ?: 0) + spacingPx
                }
                indicatorWidth = with(density) { (buttonWidthsPx[selectedIndex] ?: 0).toDp() }
                indicatorTargetOffset = with(density) { offsetPx.toDp() }
            } else {
                indicatorWidth = (maxWidth - spacing * (buttonCount - 1)) / buttonCount
                indicatorTargetOffset = (indicatorWidth + spacing) * selectedIndex
            }
            val indicatorOffset by animateDpAsState(
                targetValue = indicatorTargetOffset,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                label = "glassSegmentedButtonRowIndicatorOffset",
            )
            val indicatorColor by animateColorAsState(
                targetValue = selectedColorForOption?.invoke(currentSelectedOption) ?: selectedColor,
                animationSpec = tween(durationMillis = 260),
                label = "glassSegmentedButtonRowIndicatorColor",
            )
            val indicatorScale = remember { Animatable(1f) }

            LaunchedEffect(selectedIndex) {
                indicatorScale.snapTo(0.965f)
                indicatorScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = 0.62f,
                        stiffness = Spring.StiffnessMedium,
                    ),
                )
            }

            Box(
                modifier = Modifier
                    .then(rowWidthModifier)
                    .height(IntrinsicSize.Min)
            ) {
                if (indicatorWidth > 0.dp) {
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = with(density) { indicatorOffset.roundToPx() },
                                    y = 0,
                                )
                            }
                            .width(indicatorWidth)
                            .fillMaxHeight()
                            .graphicsLayer {
                                scaleX = indicatorScale.value
                                scaleY = 0.985f + (indicatorScale.value - 0.965f) / 0.035f * 0.015f
                            }
                            .clip(buttonShape)
                            .glassControlStyle(
                                style = selectedStyle,
                                backgroundAlpha = (selectedStyle.backgroundAlpha + 0.06f).coerceAtMost(
                                    1f
                                ),
                                shape = buttonShape,
                                color = indicatorColor,
                                borderWidth = buttonBorderWidth,
                                showTopEdge = false,
                                showInnerHighlight = false,
                            )
                    )
                }

                Row(
                    modifier = if (hugContent) Modifier else Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                ) {
                    options.forEachIndexed { index, option ->
                        val isSelected = option == selectedOption
                        GlassSegmentedButton(
                            selected = isSelected,
                            onClick = { onOptionSelected(option) },
                            modifier = Modifier
                                .then(if (hugContent) Modifier else Modifier.weight(1f))
                                .onGloballyPositioned { coordinates ->
                                    if (hugContent) {
                                        buttonWidthsPx[index] = coordinates.size.width
                                    }
                                }
                                .then(
                                    if (buttonHeight != null) Modifier.height(buttonHeight)
                                    else Modifier
                                ),
                            shape = buttonShape,
                            contentHorizontalPadding = if (hugContent) 12.dp else 0.dp,
                            selectedContentColor = selectedContentColor,
                            unselectedContentColor = unselectedContentColor,
                            unselectedColor = unselectedColor,
                            selectedIcon = selectedIcon,
                            label = { label(option) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * [GlassSegmentedButtonRow] 的 Material3 对齐命名别名。
 *
 * ```kotlin
 * SingleChoiceGlassSegmentedButtonRow(
 *     options = listOf("A", "B", "C"),
 *     selectedOption = selected,
 *     onOptionSelected = { selected = it },
 *     label = { Text(it) },
 * )
 * ```
 *
 * 参数与 [GlassSegmentedButtonRow] 完全一致 —— 仅命名上对齐
 * Material3 `SingleChoiceSegmentedButtonRow`，便于在 Material3 ↔ Glass
 * 之间切换时零成本迁移。
 */
@Composable
fun <T> SingleChoiceGlassSegmentedButtonRow(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (T) -> Unit,
    selectedIcon: (@Composable () -> Unit)? = null,
    rowShape: Shape = MaterialTheme.shapes.large,
    buttonShape: Shape = MaterialTheme.shapes.large,
    spacing: Dp = 4.dp,
    contentPadding: PaddingValues = PaddingValues(2.5.dp),
    buttonHeight: Dp? = null,
    hugContent: Boolean = false,
    rowStyle: GlassStyle = GlassStyle.Regular,
    rowColor: Color = Color.Unspecified,
    selectedStyle: GlassStyle = GlassStyle.Dense,
    selectedColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedColorForOption: ((T) -> Color)? = null,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedColor: Color = Color.Transparent,
    rowBorderWidth: Dp = 0.9.dp,
    buttonBorderWidth: Dp = 0.9.dp,
) = GlassSegmentedButtonRow(
    options = options,
    selectedOption = selectedOption,
    onOptionSelected = onOptionSelected,
    modifier = modifier,
    label = label,
    selectedIcon = selectedIcon,
    rowShape = rowShape,
    buttonShape = buttonShape,
    spacing = spacing,
    contentPadding = contentPadding,
    buttonHeight = buttonHeight,
    hugContent = hugContent,
    rowStyle = rowStyle,
    rowColor = rowColor,
    selectedStyle = selectedStyle,
    selectedColor = selectedColor,
    selectedColorForOption = selectedColorForOption,
    selectedContentColor = selectedContentColor,
    unselectedContentColor = unselectedContentColor,
    unselectedColor = unselectedColor,
    rowBorderWidth = rowBorderWidth,
    buttonBorderWidth = buttonBorderWidth,
)

// ──────────────────────────────────────────────────────────────
//  单个毛玻璃分段按钮
// ──────────────────────────────────────────────────────────────

/**
 * 单个毛玻璃分段按钮 —— 用于 [GlassSegmentedButtonRow] 内部。
 *
 * - 选中态：玻璃底色绘制在 `Surface` 内容内部，保证点击态/ripple 与形状完全一致
 * - 未选中态：透明，透出外层容器的玻璃效果
 * - 切换时内容颜色平滑过渡（300 ms `animateColorAsState`）
 * - 选中图标带缩放 + 淡入淡出动画
 */
@Composable
private fun GlassSegmentedButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    contentHorizontalPadding: Dp = 0.dp,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedColor: Color = Color.Transparent,
    selectedIcon: (@Composable () -> Unit)? = null,
    label: @Composable () -> Unit,
) {
    val glassBaseAlpha = LocalSettingsState.current.glassBaseAlpha
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "glassSegmentedButtonPressedScale",
    )
    // 内容颜色平滑过渡
    val contentColor by animateColorAsState(
        targetValue = if (selected) selectedContentColor else unselectedContentColor,
        animationSpec = tween(durationMillis = 300),
        label = "glassSegmentedButtonContentColor",
    )
    Row(
        modifier =
            modifier
                .clip(shape)
                .defaultMinSize(minHeight = 40.dp)
                .graphicsLayer {
                    scaleX = pressedScale
                    scaleY = pressedScale
                }
                .background(unselectedColor.withGlassBaseAlpha(glassBaseAlpha), shape)
                .clickable(
                    onClick = onClick,
                    indication = null, // 取消默认点击效果，使用玻璃样式自带的高亮
                    interactionSource = interactionSource,
                )
                .padding(horizontal = contentHorizontalPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            // 选中态动画图标
            if (selectedIcon != null) {
                AnimatedVisibility(
                    visible = selected,
                    enter = fadeIn(tween(200)) + scaleIn(
                        initialScale = 0.6f,
                        animationSpec = tween(200),
                    ),
                    exit = fadeOut(tween(150)) + scaleOut(
                        targetScale = 0.6f,
                        animationSpec = tween(150),
                    ),
                ) {
                    selectedIcon()
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
            label()
        }
    }
}

