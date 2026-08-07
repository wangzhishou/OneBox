package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.annotation.IntRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.utils.helper.rememberRipple
import com.t8rin.imagetoolbox.core.ui.widget.sliders.custom_slider.CustomRangeSliderState
import com.t8rin.imagetoolbox.core.ui.widget.sliders.custom_slider.CustomSlider
import com.t8rin.imagetoolbox.core.ui.widget.sliders.custom_slider.CustomSliderColors
import com.t8rin.imagetoolbox.core.ui.widget.sliders.custom_slider.CustomSliderDefaults
import com.t8rin.imagetoolbox.core.ui.widget.sliders.custom_slider.CustomSliderState

/**
 * 毛玻璃风格的 [com.t8rin.imagetoolbox.core.ui.widget.sliders.custom_slider.CustomSlider]。
 *
 * 交互、语义与状态同步全部复用 [com.t8rin.imagetoolbox.core.ui.widget.sliders.custom_slider.CustomSlider]，
 * 仅将默认的 thumb / track 替换为玻璃视觉实现。
 */
@Composable
fun GlassCustomSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    @IntRange(from = 0)
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: CustomSliderColors = CustomSliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    trackStyle: GlassStyle = GlassStyle.Regular,
    activeTrackStyle: GlassStyle = GlassStyle.Medium,
    thumbStyle: GlassStyle = GlassStyle.Medium,
    trackGlassColor: Color = Color.Unspecified,
    activeTrackGlassColor: Color = Color.Unspecified,
    thumbGlassColor: Color = Color.Unspecified,
    glassBorderWidth: Dp = 0.5.dp,
    trackHeight: Dp = GlassCustomSliderDefaults.TrackHeight,
    thumbSize: DpSize = GlassCustomSliderDefaults.ThumbSize,
) {
    GlassCustomSlider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        interactionSource = interactionSource,
        steps = steps,
        thumb = {
            GlassCustomSliderDefaults.Thumb(
                interactionSource = interactionSource,
                colors = colors,
                enabled = enabled,
                style = thumbStyle,
                glassColor = thumbGlassColor,
                glassBorderWidth = glassBorderWidth,
                thumbSize = thumbSize,
            )
        },
        track = { sliderState ->
            GlassCustomSliderDefaults.Track(
                sliderState = sliderState,
                colors = colors,
                enabled = enabled,
                trackStyle = trackStyle,
                activeTrackStyle = activeTrackStyle,
                trackGlassColor = trackGlassColor,
                activeTrackGlassColor = activeTrackGlassColor,
                glassBorderWidth = glassBorderWidth,
                trackHeight = trackHeight,
                edgeInset = thumbSize.width / 2 + GlassCustomSliderDefaults.TrackEdgeSpacing,
            )
        },
        valueRange = valueRange,
    )
}

@Composable
fun GlassCustomSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: CustomSliderColors = CustomSliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    @IntRange(from = 0)
    steps: Int = 0,
    thumb: @Composable (CustomSliderState) -> Unit,
    track: @Composable (CustomSliderState) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
) {
    val state = remember(steps, valueRange) {
        CustomSliderState(
            value = value,
            steps = steps,
            valueRange = valueRange,
        )
    }

    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val lastStepIndexState = remember(steps, valueRange) {
        androidx.compose.runtime.mutableStateOf(Int.MIN_VALUE)
    }

    state.onValueChangeFinished = onValueChangeFinished
    state.onValueChange = { newVal ->
        if (steps > 0) {
            val stepFraction = 1f / (steps + 1)
            val newStepIndex = ((newVal - valueRange.start) / (valueRange.endInclusive - valueRange.start) / stepFraction).toInt()
            if (newStepIndex != lastStepIndexState.value) {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.SegmentTick)
                lastStepIndexState.value = newStepIndex
            }
        }
        onValueChange(newVal)
    }
    state.value = value

    GlassCustomSlider(
        state = state,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        thumb = thumb,
        track = track,
    )
}

@Composable
fun GlassCustomSlider(
    state: CustomSliderState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CustomSliderColors = CustomSliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    thumb: @Composable (CustomSliderState) -> Unit = {
        GlassCustomSliderDefaults.Thumb(
            interactionSource = interactionSource,
            colors = colors,
            enabled = enabled,
        )
    },
    track: @Composable (CustomSliderState) -> Unit = { sliderState ->
        GlassCustomSliderDefaults.Track(
            sliderState = sliderState,
            colors = colors,
            enabled = enabled,
        )
    },
) {
    CustomSlider(
        state = state,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        thumb = thumb,
        track = track,
    )
}

@Stable
object GlassCustomSliderDefaults {
    val ThumbSize: DpSize = DpSize(30.dp, 30.dp)
    val TrackHeight: Dp = 40.dp
    val TrackShape: Shape = RoundedCornerShape(percent = 50)

    /**
     * 轨道两端在 "thumb 半径补偿" 之外额外向外扩展的量。
     *
     * 外层 [CustomSlider] / [com.t8rin.imagetoolbox.core.ui.widget.sliders.custom_slider.CustomRangeSlider] 的 track 测量宽度 = sliderWidth - thumbWidth，
     * 并被放置在 `thumbWidth / 2` 处。为了让玻璃轨道在视觉上能完整覆盖 thumb 的
     * 运动范围（包括 thumb 处于两端时的左/右端点），默认 `edgeInset` 保持
     * `thumbWidth / 2`。外扩后的轨道左右端点恰好对齐整个 slider 的左右边界，
     * 不会超出 slider（也就是不会超出父级测量宽度）。
     *
     * 这里的 [TrackEdgeSpacing] 是在 `thumbWidth / 2` 基础上**额外**的装饰性外扩，
     * 默认 0.dp，需要玻璃风格的"溢出感"时再调大即可。
     */
    val TrackEdgeSpacing: Dp = 0.dp

    @Composable
    fun Thumb(
        interactionSource: MutableInteractionSource,
        modifier: Modifier = Modifier,
        colors: CustomSliderColors = CustomSliderDefaults.colors(),
        enabled: Boolean = true,
        style: GlassStyle = GlassStyle.Medium,
        glassColor: Color = Color.Unspecified,
        glassBorderWidth: Dp = 0.5.dp,
        thumbSize: DpSize = ThumbSize,
    ) {
        val interactions = remember { mutableStateListOf<Interaction>() }
        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> interactions.add(interaction)
                    is PressInteraction.Release -> interactions.remove(interaction.press)
                    is PressInteraction.Cancel -> interactions.remove(interaction.press)
                    is DragInteraction.Start -> interactions.add(interaction)
                    is DragInteraction.Stop -> interactions.remove(interaction.start)
                    is DragInteraction.Cancel -> interactions.remove(interaction.start)
                }
            }
        }

        val elevation = if (interactions.isNotEmpty()) 6.dp else 1.dp
        val resolvedGlassColor = glassColor.takeUnless { it == Color.Unspecified }
            ?: colors.thumbColor(enabled)

        Spacer(
            modifier = modifier
                .size(thumbSize)
                .indication(
                    interactionSource = interactionSource,
                    indication = rememberRipple(
                        bounded = false,
                        radius = 20.dp,
                    ),
                )
                .hoverable(interactionSource = interactionSource)
                .shadow(if (enabled) elevation else 0.dp, CircleShape, clip = false)
                .glassBackground(
                    style = style,
                    shape = CircleShape,
                    color = resolvedGlassColor,
                    borderWidth = glassBorderWidth,
                )
        )
    }

    @Composable
    fun Track(
        sliderState: CustomSliderState,
        modifier: Modifier = Modifier,
        colors: CustomSliderColors = CustomSliderDefaults.colors(),
        enabled: Boolean = true,
        trackStyle: GlassStyle = GlassStyle.Regular,
        activeTrackStyle: GlassStyle = GlassStyle.Medium,
        trackGlassColor: Color = Color.Unspecified,
        activeTrackGlassColor: Color = Color.Unspecified,
        glassBorderWidth: Dp = 0.5.dp,
        trackHeight: Dp = TrackHeight,
        shape: Shape = TrackShape,
        edgeInset: Dp = ThumbSize.width / 2 + TrackEdgeSpacing,
    ) {
        val inactiveTickColor = colors.tickColor(enabled = enabled, active = false)
        val activeTickColor = colors.tickColor(enabled = enabled, active = true)
        val inactiveGlass = trackGlassColor.takeUnless { it == Color.Unspecified }
            ?: colors.trackColor(enabled = enabled, active = false)
        val activeGlass = activeTrackGlassColor.takeUnless { it == Color.Unspecified }
            ?: colors.trackColor(enabled = enabled, active = true)
        val fraction = sliderState.coercedValueAsFraction.coerceIn(0f, 1f)
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        TrackLayout(
            modifier = modifier,
            height = trackHeight,
            shape = shape,
            inactiveGlass = inactiveGlass,
            trackStyle = trackStyle,
            glassBorderWidth = glassBorderWidth,
            edgeInset = edgeInset,
            activeStartFraction = 0f,
            activeEndFraction = fraction,
            activeGlass = activeGlass,
            activeTrackStyle = activeTrackStyle,
        ) { fractionToX ->
            val activeEdgeX = fractionToX(fraction)

            sliderState.tickFractions.forEach { tickFraction ->
                val x = fractionToX(tickFraction)
                val isActiveTick = if (isRtl) x >= activeEdgeX else x <= activeEdgeX
                drawCircle(
                    color = if (isActiveTick) activeTickColor else inactiveTickColor,
                    center = Offset(x, center.y),
                    radius = 1.dp.toPx(),
                )
            }
        }
    }

    @Composable
    fun Track(
        rangeSliderState: CustomRangeSliderState,
        modifier: Modifier = Modifier,
        colors: CustomSliderColors = CustomSliderDefaults.colors(),
        enabled: Boolean = true,
        trackStyle: GlassStyle = GlassStyle.Regular,
        activeTrackStyle: GlassStyle = GlassStyle.Medium,
        trackGlassColor: Color = Color.Unspecified,
        activeTrackGlassColor: Color = Color.Unspecified,
        glassBorderWidth: Dp = 0.5.dp,
        trackHeight: Dp = TrackHeight,
        shape: Shape = TrackShape,
        edgeInset: Dp = ThumbSize.width / 2 + TrackEdgeSpacing,
    ) {
        val inactiveTickColor = colors.tickColor(enabled = enabled, active = false)
        val activeTickColor = colors.tickColor(enabled = enabled, active = true)
        val inactiveGlass = trackGlassColor.takeUnless { it == Color.Unspecified }
            ?: colors.trackColor(enabled = enabled, active = false)
        val activeGlass = activeTrackGlassColor.takeUnless { it == Color.Unspecified }
            ?: colors.trackColor(enabled = enabled, active = true)
        val startFraction = rangeSliderState.coercedActiveRangeStartAsFraction.coerceIn(0f, 1f)
        val endFraction = rangeSliderState.coercedActiveRangeEndAsFraction.coerceIn(0f, 1f)

        TrackLayout(
            modifier = modifier,
            height = trackHeight,
            shape = shape,
            inactiveGlass = inactiveGlass,
            trackStyle = trackStyle,
            glassBorderWidth = glassBorderWidth,
            edgeInset = edgeInset,
            activeStartFraction = startFraction,
            activeEndFraction = endFraction,
            activeGlass = activeGlass,
            activeTrackStyle = activeTrackStyle,
        ) { fractionToX ->
            val activeStartX = fractionToX(startFraction)
            val activeEndX = fractionToX(endFraction)
            val activeMinX = minOf(activeStartX, activeEndX)
            val activeMaxX = maxOf(activeStartX, activeEndX)

            rangeSliderState.tickFractions.forEach { tickFraction ->
                val x = fractionToX(tickFraction)
                val isActiveTick = x in activeMinX..activeMaxX
                drawCircle(
                    color = if (isActiveTick) activeTickColor else inactiveTickColor,
                    center = Offset(x, center.y),
                    radius = 1.dp.toPx(),
                )
            }
        }
    }

    @Composable
    private fun TrackLayout(
        modifier: Modifier,
        height: Dp,
        shape: Shape,
        inactiveGlass: Color,
        trackStyle: GlassStyle,
        glassBorderWidth: Dp,
        edgeInset: Dp,
        activeStartFraction: Float,
        activeEndFraction: Float,
        activeGlass: Color,
        activeTrackStyle: GlassStyle,
        ticks: DrawScope.((Float) -> Float) -> Unit,
    ) {
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        val clampedStart = activeStartFraction.coerceIn(0f, 1f)
        val clampedEnd = activeEndFraction.coerceIn(clampedStart, 1f)
        val activeWidthFraction = (clampedEnd - clampedStart).coerceIn(0f, 1f)
        val visualStartFraction = if (isRtl) 1f - clampedEnd else clampedStart

        // BoxWithConstraints 对外报告的尺寸 = 外层 CustomSlider 分配给 track 的
        // 宽度 (= sliderWidth - thumbWidth)；但通过 Modifier.layout 把其内部的
        // 测量宽度扩大为 (+ 2 * edgeInset)，并在放置时向左平移 edgeInset，
        // 使得内部子项的坐标系 [0, extendedWidth] 恰好对应整个 slider 的
        // [0, sliderWidth]。这样就不再依赖 align(CenterStart) + offset +
        // requiredWidth 这种组合，避免某些场景下位置漂移。
        BoxWithConstraints(
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .layout { measurable, constraints ->
                    val insetPx = edgeInset.roundToPx()
                    val outerWidth = constraints.maxWidth
                    val extendedWidth = (outerWidth + insetPx * 2).coerceAtLeast(0)
                    val childConstraints = constraints.copy(
                        minWidth = extendedWidth,
                        maxWidth = extendedWidth,
                    )
                    val placeable = measurable.measure(childConstraints)
                    layout(outerWidth, placeable.height) {
                        placeable.place(-insetPx, 0)
                    }
                }
        ) {
            // 此处 maxWidth = 扩展后的宽度 (= sliderWidth，也就是覆盖 thumb
            // 两端端点后的完整玻璃轨道宽度)
            val visualMaxWidth = maxWidth

            // 1) 完整的非激活玻璃底 —— 直接铺满 BoxWithConstraints
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .glassBackground(
                        style = trackStyle,
                        shape = shape,
                        color = inactiveGlass,
                        borderWidth = glassBorderWidth,
                    )
            )

            // 2) 激活段（可选）。fraction 对应的是 thumb 的运动区间
            //    (slider.x = edgeInset .. sliderWidth - edgeInset)，所以需要
            //    把 fraction 映射到 [edgeInset, visualMaxWidth - edgeInset]，
            //    再在两端贴边时额外扩展 edgeInset 以覆盖 thumb 端点。
            if (activeWidthFraction > 0f) {
                val isAtStart = visualStartFraction == 0f
                val isAtEnd = (visualStartFraction + activeWidthFraction) >= 0.99f
                val movableWidth = (visualMaxWidth - edgeInset * 2).coerceAtLeast(0.dp)

                val baseStart = edgeInset + movableWidth * visualStartFraction
                val baseEnd = edgeInset + movableWidth * (visualStartFraction + activeWidthFraction)
                val startOffset = baseStart - if (isAtStart) edgeInset else 0.dp
                val endOffset = baseEnd + if (isAtEnd) edgeInset else 0.dp
                val activeWidth = (endOffset - startOffset).coerceAtLeast(0.dp)

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = startOffset)
                        .requiredWidth(activeWidth)
                        .fillMaxHeight()
                        .clip(shape)
                        .glassBackground(
                            style = activeTrackStyle,
                            shape = shape,
                            color = activeGlass
                        )
                )
            }

            // 3) 刻度点。Canvas 铺满 BWC，内部 x = edgeInset + movableWidth * fraction
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val edgeInsetPx = edgeInset.toPx()
                val movableWidthPx = (size.width - edgeInsetPx * 2).coerceAtLeast(0f)
                val fractionToX: (Float) -> Float = { fraction ->
                    val visualFraction = if (isRtl) 1f - fraction.coerceIn(0f, 1f)
                    else fraction.coerceIn(0f, 1f)
                    edgeInsetPx + movableWidthPx * visualFraction
                }
                ticks(fractionToX)
            }
        }
    }
}
