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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.SliderColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.wanbaohe.core.ui.widget.glass.GlassCustomRangeSlider
import com.wanbaohe.core.ui.widget.glass.GlassCustomSlider
import com.wanbaohe.core.ui.widget.sliders.custom_slider.CustomSliderColors
import com.wanbaohe.core.ui.widget.sliders.custom_slider.CustomSliderDefaults

/**
 * 滑杆条目里用的滑杆。
 *
 * **它只是 [GlassCustomSlider] 的薄封装，不再自带任何视觉参数**
 * （之前这里写死了 `trackHeight = 16.dp`、`GlassStyle.Thin`，还额外套了一层
 * 胶囊容器，导致「水印透明度」这类滑杆和设置里「文字大小」那种裸
 * [GlassCustomSlider] 长得不一样）。
 *
 * 现在两者是同一个组件、同一套默认值：细扁轨道 + 滑块在轨道内滑动。
 * 想调整所有滑杆的外观，只改 [com.wanbaohe.core.ui.widget.glass.GlassCustomSliderDefaults]。
 */
@Composable
fun EnhancedSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    enabled: Boolean = true,
    sliderColors: SliderColors? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    GlassCustomSlider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        colors = sliderColors?.toCustomSliderColors() ?: CustomSliderDefaults.colors(),
        interactionSource = interactionSource,
    )
}

/**
 * [EnhancedRangeSlider] 同 [EnhancedSlider]：纯 [GlassCustomRangeSlider] 封装，
 * 不覆盖任何视觉参数。
 */
@Composable
fun EnhancedRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    enabled: Boolean = true,
    sliderColors: SliderColors? = null,
    @Suppress("UNUSED_PARAMETER")
    startInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    @Suppress("UNUSED_PARAMETER")
    endInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    GlassCustomRangeSlider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        colors = sliderColors?.toCustomSliderColors() ?: CustomSliderDefaults.colors(),
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
