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

package com.wanbaohe.period.model

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * 经期模块配色 — 全部取自系统主题(Material You 三套配色中的 tertiary 系),
 * 随主题切换自动变化,不写死任何色值。
 */
@Immutable
data class PeriodColors(
    val accent: Color,
    val accentSoft: Color,
    val accentContainer: Color,
    val onAccent: Color,
    val onAccentContainer: Color,
    val background: Color,
    val surface: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val chartLine: Color,
    val chartBubble: Color,
)

@Composable
fun periodColors(): PeriodColors {
    val scheme = MaterialTheme.colorScheme
    return PeriodColors(
        accent = scheme.tertiary,
        accentSoft = scheme.tertiary.copy(alpha = 0.12f),
        accentContainer = scheme.tertiaryContainer,
        onAccent = scheme.onTertiary,
        onAccentContainer = scheme.onTertiaryContainer,
        background = scheme.background,
        surface = scheme.surface,
        onSurface = scheme.onSurface,
        onSurfaceVariant = scheme.onSurfaceVariant,
        chartLine = scheme.tertiary,
        chartBubble = scheme.tertiary,
    )
}
