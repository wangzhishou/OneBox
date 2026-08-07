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

package com.t8rin.imagetoolbox.core.ui.widget.enhanced

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SliderColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.utils.animation.animateFloatingRangeAsState
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.modifier.container
import com.t8rin.imagetoolbox.core.ui.widget.sliders.custom_slider.CustomSliderColors
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCustomRangeSlider
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCustomSlider
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCustomSliderDefaults

@Composable
fun EnhancedSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    enabled: Boolean = true,
    colors: SliderColors? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    drawContainer: Boolean = true
) {
    val realColors = colors?.toCustomSliderColors()
        ?: defaultEnhancedSliderColors()
    val trackHeight = if (drawContainer) 16.dp else 20.dp
    val trackStyle = if (drawContainer) GlassStyle.Thin else GlassStyle.Regular
    val activeTrackStyle = if (drawContainer) GlassStyle.Regular else GlassStyle.Medium
    val thumbStyle = if (drawContainer) GlassStyle.Regular else GlassStyle.Medium
    val glassBorderWidth = if (drawContainer) 0.dp else 0.5.dp

    if (steps != 0) {
        var compositions by remember {
            mutableIntStateOf(0)
        }
        val haptics = LocalHapticFeedback.current
        val updatedValue by rememberUpdatedState(newValue = value)

        LaunchedEffect(updatedValue) {
            if (compositions > 0) haptics.press()

            compositions++
        }
    }

    GlassCustomSlider(
        value = animateFloatAsState(value).value,
        onValueChange = onValueChange,
        modifier = modifier.enhancedSliderContainer(drawContainer = drawContainer),
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        colors = realColors,
        interactionSource = interactionSource,
        trackStyle = trackStyle,
        activeTrackStyle = activeTrackStyle,
        thumbStyle = thumbStyle,
        glassBorderWidth = glassBorderWidth,
        trackHeight = trackHeight,
    )
}

@Composable
fun EnhancedRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    enabled: Boolean = true,
    colors: SliderColors? = null,
    startInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    endInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    drawContainer: Boolean = true
) {
    val realColors = colors?.toCustomSliderColors()
        ?: defaultEnhancedRangeSliderColors()
    val trackHeight = if (drawContainer) 16.dp else 20.dp
    val trackStyle = if (drawContainer) GlassStyle.Thin else GlassStyle.Regular
    val activeTrackStyle = if (drawContainer) GlassStyle.Regular else GlassStyle.Medium
    val thumbStyle = if (drawContainer) GlassStyle.Regular else GlassStyle.Medium
    val glassBorderWidth = if (drawContainer) 0.dp else 0.5.dp

    if (steps != 0) {
        var compositions by remember {
            mutableIntStateOf(0)
        }
        val haptics = LocalHapticFeedback.current
        val updatedValue by rememberUpdatedState(newValue = value)

        LaunchedEffect(updatedValue) {
            if (compositions > 0) haptics.press()

            compositions++
        }
    }

    GlassCustomRangeSlider(
        value = animateFloatingRangeAsState(value).value,
        onValueChange = onValueChange,
        modifier = modifier.enhancedSliderContainer(drawContainer = drawContainer),
        enabled = enabled,
        valueRange = valueRange,
        onValueChangeFinished = onValueChangeFinished,
        colors = realColors,
        startInteractionSource = startInteractionSource,
        endInteractionSource = endInteractionSource,
        startThumb = {
            GlassCustomSliderDefaults.Thumb(
                interactionSource = startInteractionSource,
                colors = realColors,
                enabled = enabled,
                style = thumbStyle,
                glassBorderWidth = glassBorderWidth,
            )
        },
        endThumb = {
            GlassCustomSliderDefaults.Thumb(
                interactionSource = endInteractionSource,
                colors = realColors,
                enabled = enabled,
                style = thumbStyle,
                glassBorderWidth = glassBorderWidth,
            )
        },
        track = { rangeSliderState ->
            GlassCustomSliderDefaults.Track(
                rangeSliderState = rangeSliderState,
                colors = realColors,
                enabled = enabled,
                trackStyle = trackStyle,
                activeTrackStyle = activeTrackStyle,
                glassBorderWidth = glassBorderWidth,
                trackHeight = trackHeight,
            )
        },
        steps = steps,
    )
}

private fun SliderColors.toCustomSliderColors(): CustomSliderColors = CustomSliderColors(
    thumbColor = thumbColor,
    activeTrackColor = activeTrackColor,
    activeTickColor = activeTickColor,
    inactiveTrackColor = inactiveTrackColor,
    inactiveTickColor = inactiveTickColor,
    disabledThumbColor = disabledThumbColor,
    disabledActiveTrackColor = disabledActiveTrackColor,
    disabledActiveTickColor = disabledActiveTickColor,
    disabledInactiveTrackColor = disabledInactiveTrackColor,
    disabledInactiveTickColor = disabledInactiveTickColor,
)

private fun Modifier.enhancedSliderContainer(
    drawContainer: Boolean,
): Modifier = then(
    if (drawContainer) {
        Modifier
            .container(
                shape = CircleShape,
                resultPadding = 0.dp,
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    } else Modifier
)

@Composable
private fun defaultEnhancedSliderColors(): CustomSliderColors = CustomSliderColors(
    thumbColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer.copy(0.68f),
    activeTrackColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
    activeTickColor = androidx.compose.material3.MaterialTheme.colorScheme.inverseSurface,
    inactiveTrackColor = androidx.compose.material3.SwitchDefaults.colors().disabledCheckedTrackColor,
    inactiveTickColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
    disabledThumbColor = androidx.compose.material3.SliderDefaults.colors().disabledThumbColor,
    disabledActiveTrackColor = androidx.compose.material3.SliderDefaults.colors().disabledActiveTrackColor,
    disabledActiveTickColor = androidx.compose.material3.SliderDefaults.colors().disabledActiveTickColor,
    disabledInactiveTrackColor = androidx.compose.material3.SliderDefaults.colors().disabledInactiveTrackColor,
    disabledInactiveTickColor = androidx.compose.material3.SliderDefaults.colors().disabledInactiveTickColor,
)

@Composable
private fun defaultEnhancedRangeSliderColors(): CustomSliderColors = CustomSliderColors(
    thumbColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer,
    activeTrackColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
    activeTickColor = androidx.compose.material3.MaterialTheme.colorScheme.inverseSurface,
    inactiveTrackColor = androidx.compose.material3.SwitchDefaults.colors().disabledCheckedTrackColor,
    inactiveTickColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
    disabledThumbColor = androidx.compose.material3.SliderDefaults.colors().disabledThumbColor,
    disabledActiveTrackColor = androidx.compose.material3.SliderDefaults.colors().disabledActiveTrackColor,
    disabledActiveTickColor = androidx.compose.material3.SliderDefaults.colors().disabledActiveTickColor,
    disabledInactiveTrackColor = androidx.compose.material3.SliderDefaults.colors().disabledInactiveTrackColor,
    disabledInactiveTickColor = androidx.compose.material3.SliderDefaults.colors().disabledInactiveTickColor,
)
