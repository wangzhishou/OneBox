package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.annotation.IntRange
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.sliders.custom_slider.CustomRangeSlider
import com.t8rin.imagetoolbox.core.ui.widget.sliders.custom_slider.CustomRangeSliderState
import com.t8rin.imagetoolbox.core.ui.widget.sliders.custom_slider.CustomSliderColors
import com.t8rin.imagetoolbox.core.ui.widget.sliders.custom_slider.CustomSliderDefaults

/**
 * 毛玻璃风格的 [com.t8rin.imagetoolbox.core.ui.widget.sliders.custom_slider.CustomRangeSlider]。
 *
 * 交互、语义与状态逻辑全部复用 [com.t8rin.imagetoolbox.core.ui.widget.sliders.custom_slider.CustomRangeSlider]，
 * 仅将默认的两个 thumb 和 track 替换为玻璃视觉实现。
 */
@Composable
fun GlassCustomRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    @IntRange(from = 0)
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: CustomSliderColors = CustomSliderDefaults.colors(),
    trackStyle: GlassStyle = GlassStyle.Regular,
    activeTrackStyle: GlassStyle = GlassStyle.Medium,
    thumbStyle: GlassStyle = GlassStyle.Medium,
    trackGlassColor: Color = Color.Unspecified,
    activeTrackGlassColor: Color = Color.Unspecified,
    startThumbGlassColor: Color = Color.Unspecified,
    endThumbGlassColor: Color = Color.Unspecified,
    glassBorderWidth: Dp = 0.5.dp,
    trackHeight: Dp = GlassCustomSliderDefaults.TrackHeight,
    thumbSize: DpSize = GlassCustomSliderDefaults.ThumbSize,
) {
    val startInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val endInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() }

    GlassCustomRangeSlider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        startInteractionSource = startInteractionSource,
        endInteractionSource = endInteractionSource,
        startThumb = {
            GlassCustomSliderDefaults.Thumb(
                interactionSource = startInteractionSource,
                colors = colors,
                enabled = enabled,
                style = thumbStyle,
                glassColor = startThumbGlassColor,
                glassBorderWidth = glassBorderWidth,
                thumbSize = thumbSize,
            )
        },
        endThumb = {
            GlassCustomSliderDefaults.Thumb(
                interactionSource = endInteractionSource,
                colors = colors,
                enabled = enabled,
                style = thumbStyle,
                glassColor = endThumbGlassColor,
                glassBorderWidth = glassBorderWidth,
                thumbSize = thumbSize,
            )
        },
        track = { rangeSliderState ->
            GlassCustomSliderDefaults.Track(
                rangeSliderState = rangeSliderState,
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
    )
}

@Composable
fun GlassCustomRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: CustomSliderColors = CustomSliderDefaults.colors(),
    startInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    endInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    startThumb: @Composable (CustomRangeSliderState) -> Unit,
    endThumb: @Composable (CustomRangeSliderState) -> Unit,
    track: @Composable (CustomRangeSliderState) -> Unit,
    @IntRange(from = 0)
    steps: Int = 0,
) {
    val state = remember(
        steps,
        valueRange,
        onValueChangeFinished,
    ) {
        CustomRangeSliderState(
            activeRangeStart = value.start,
            activeRangeEnd = value.endInclusive,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
        )
    }

    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    state.onValueChange = { newRange ->
        if (steps > 0) {
            val stepFraction = 1f / (steps + 1)
            val rangeSpan = valueRange.endInclusive - valueRange.start
            val newStartIdx = ((newRange.start - valueRange.start) / rangeSpan / stepFraction).toInt()
            val newEndIdx = ((newRange.endInclusive - valueRange.start) / rangeSpan / stepFraction).toInt()
            val oldStartIdx = ((state.activeRangeStart - valueRange.start) / rangeSpan / stepFraction).toInt()
            val oldEndIdx = ((state.activeRangeEnd - valueRange.start) / rangeSpan / stepFraction).toInt()
            if (newStartIdx != oldStartIdx || newEndIdx != oldEndIdx) {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.SegmentTick)
            }
        }
        onValueChange(newRange.start..newRange.endInclusive)
    }
    state.activeRangeStart = value.start
    state.activeRangeEnd = value.endInclusive

    GlassCustomRangeSlider(
        state = state,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        startInteractionSource = startInteractionSource,
        endInteractionSource = endInteractionSource,
        startThumb = startThumb,
        endThumb = endThumb,
        track = track,
    )
}

@Composable
fun GlassCustomRangeSlider(
    state: CustomRangeSliderState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CustomSliderColors = CustomSliderDefaults.colors(),
    startInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    endInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    startThumb: @Composable (CustomRangeSliderState) -> Unit = {
        GlassCustomSliderDefaults.Thumb(
            interactionSource = startInteractionSource,
            colors = colors,
            enabled = enabled,
        )
    },
    endThumb: @Composable (CustomRangeSliderState) -> Unit = {
        GlassCustomSliderDefaults.Thumb(
            interactionSource = endInteractionSource,
            colors = colors,
            enabled = enabled,
        )
    },
    track: @Composable (CustomRangeSliderState) -> Unit = { rangeSliderState ->
        GlassCustomSliderDefaults.Track(
            rangeSliderState = rangeSliderState,
            colors = colors,
            enabled = enabled,
        )
    },
) {
    CustomRangeSlider(
        state = state,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        startInteractionSource = startInteractionSource,
        endInteractionSource = endInteractionSource,
        startThumb = startThumb,
        endThumb = endThumb,
        track = track,
    )
}

