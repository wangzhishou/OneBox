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

package com.wanbaohe.core.ui.widget.glass

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState

/**
 * 毛玻璃风格 CircularProgressIndicator —— 参数与 M3 [CircularProgressIndicator] 对齐。
 *
 * - **Glassmorphism 开启** → 进度色由 [color] 控制，背景轨道由 [trackColor] 控制；
 *   二者均加玻璃色调(高 6% 透明度)以与玻璃面板协调
 * - **Glassmorphism 关闭** → 退化为标准 M3 [CircularProgressIndicator]
 */
@Composable
fun GlassCircularProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    trackColor: Color = ProgressIndicatorDefaults.circularTrackColor,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
    gapSize: Dp = ProgressIndicatorDefaults.CircularIndicatorTrackGapSize,
    style: GlassStyle = GlassStyle.Regular,
) {
    val settingsState = LocalSettingsState.current

    if (!settingsState.isGlassAlphaEnabled) {
        CircularProgressIndicator(
            progress = progress,
            modifier = modifier,
            color = color,
            trackColor = trackColor,
            strokeWidth = strokeWidth,
            gapSize = gapSize,
        )
        return
    }

    val glassColor = color.copy(alpha = color.alpha.coerceAtLeast(0.86f))
    val glassTrack = trackColor.copy(alpha = trackColor.alpha.coerceAtLeast(0.32f))
    CircularProgressIndicator(
        progress = progress,
        modifier = modifier,
        color = glassColor,
        trackColor = glassTrack,
        strokeWidth = strokeWidth,
        gapSize = gapSize,
    )
}

/**
 * Indeterminate 重载 —— 与 M3 [CircularProgressIndicator] 无参版本对齐。
 */
@Composable
fun GlassCircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    trackColor: Color = ProgressIndicatorDefaults.circularTrackColor,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
    gapSize: Dp = ProgressIndicatorDefaults.CircularIndicatorTrackGapSize,
    style: GlassStyle = GlassStyle.Regular,
) {
    val settingsState = LocalSettingsState.current

    if (!settingsState.isGlassAlphaEnabled) {
        CircularProgressIndicator(
            modifier = modifier,
            color = color,
            trackColor = trackColor,
            strokeWidth = strokeWidth,
            gapSize = gapSize,
        )
        return
    }

    val glassColor = color.copy(alpha = color.alpha.coerceAtLeast(0.86f))
    val glassTrack = trackColor.copy(alpha = trackColor.alpha.coerceAtLeast(0.32f))
    CircularProgressIndicator(
        modifier = modifier,
        color = glassColor,
        trackColor = glassTrack,
        strokeWidth = strokeWidth,
        gapSize = gapSize,
    )
}

/**
 * 毛玻璃风格 LinearProgressIndicator —— 参数与 M3 [LinearProgressIndicator] 对齐。
 *
 * - **Glassmorphism 开启** → 进度/轨道色按 [color]/[trackColor] 调整透明度与玻璃面板协调
 * - **Glassmorphism 关闭** → 退化为标准 M3 [LinearProgressIndicator]
 */
@Composable
fun GlassLinearProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    trackColor: Color = ProgressIndicatorDefaults.linearTrackColor,
    strokeCap: androidx.compose.ui.graphics.StrokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
    gapSize: Dp = ProgressIndicatorDefaults.LinearIndicatorTrackGapSize,
    drawStopIndicator: DrawScope.() -> Unit = {},
) {
    val settingsState = LocalSettingsState.current

    if (!settingsState.isGlassAlphaEnabled) {
        LinearProgressIndicator(
            progress = progress,
            modifier = modifier,
            color = color,
            trackColor = trackColor,
            strokeCap = strokeCap,
            gapSize = gapSize,
            drawStopIndicator = drawStopIndicator,
        )
        return
    }

    val glassColor = color.copy(alpha = color.alpha.coerceAtLeast(0.86f))
    val glassTrack = trackColor.copy(alpha = trackColor.alpha.coerceAtLeast(0.32f))
    LinearProgressIndicator(
        progress = progress,
        modifier = modifier,
        color = glassColor,
        trackColor = glassTrack,
        strokeCap = strokeCap,
        gapSize = gapSize,
        drawStopIndicator = drawStopIndicator,
    )
}

/**
 * Indeterminate 重载 —— 与 M3 [LinearProgressIndicator] 无参版本对齐。
 */
@Composable
fun GlassLinearProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    trackColor: Color = ProgressIndicatorDefaults.linearTrackColor,
    strokeCap: androidx.compose.ui.graphics.StrokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
    gapSize: Dp = ProgressIndicatorDefaults.LinearIndicatorTrackGapSize,
) {
    val settingsState = LocalSettingsState.current

    if (!settingsState.isGlassAlphaEnabled) {
        LinearProgressIndicator(
            modifier = modifier,
            color = color,
            trackColor = trackColor,
            strokeCap = strokeCap,
            gapSize = gapSize,
        )
        return
    }

    val glassColor = color.copy(alpha = color.alpha.coerceAtLeast(0.86f))
    val glassTrack = trackColor.copy(alpha = trackColor.alpha.coerceAtLeast(0.32f))
    LinearProgressIndicator(
        modifier = modifier,
        color = glassColor,
        trackColor = glassTrack,
        strokeCap = strokeCap,
        gapSize = gapSize,
    )
}
