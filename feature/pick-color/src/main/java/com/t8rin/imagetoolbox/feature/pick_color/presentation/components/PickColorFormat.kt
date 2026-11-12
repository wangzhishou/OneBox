/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
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

package com.t8rin.imagetoolbox.feature.pick_color.presentation.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.roundToInt

internal data class RgbTriplet(
    val red: Int,
    val green: Int,
    val blue: Int
)

internal data class HslTriplet(
    val hue: Int,
    val saturation: Int,
    val lightness: Int
)

internal fun Color.toRgbTriplet(): RgbTriplet = RgbTriplet(
    red = (red * 255).roundToInt(),
    green = (green * 255).roundToInt(),
    blue = (blue * 255).roundToInt()
)

internal fun Color.toHslTriplet(): HslTriplet {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(toArgb(), hsl)
    return HslTriplet(
        hue = hsl[0].roundToInt(),
        saturation = (hsl[1] * 100).roundToInt(),
        lightness = (hsl[2] * 100).roundToInt()
    )
}

internal fun localDateOf(
    epochMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault()
): LocalDate = Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalDate()

internal fun localTimeOf(
    epochMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault()
): LocalTime = Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalTime()

internal fun historyTimeFormatter(): DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

internal fun historyDateFormatter(): DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
