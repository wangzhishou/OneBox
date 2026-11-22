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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipElevation
import androidx.compose.material3.SelectableChipColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.wanbaohe.core.ui.utils.helper.ProvidesValue
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem

/**
 * 毛玻璃风格 FilterChip。
 *
 * 玻璃开启时：
 * - 使用 glass modifier 承担背景与描边
 * - 将 FilterChip 自身容器设为透明，保留文字/图标/交互语义
 *   （文字、图标颜色仍来自调用方传入的 [colors]）
 *
 * 玻璃关闭时：
 * - 直接回退为标准 Material3 [FilterChip]
 *
 * 触达（验收口径，勿按视觉大小直接放大）：
 * - 本组件主动关掉 M3 的 48dp 交互下限（`LocalMinimumInteractiveComponentSize`），
 *   布局尺寸 = 视觉尺寸（M3 FilterChip 默认高 32dp），Compose 的 clickable 不做触摸外扩，
 *   所以**实际命中区就是这颗 chip 本身**。密集分类 chip 行是有意保持这个密度。
 * - 单页确实需要 48dp 触达时，由调用方传 `Modifier.heightIn(min = 48.dp)`（视觉一起变高），
 *   或改用 `EnhancedButton` / 列表行；不要在共享层统一放大或统一关/开下限。
 */
@Composable
fun GlassFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = OneBoxDesignSystem.chipShape,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(),
    elevation: SelectableChipElevation? = FilterChipDefaults.filterChipElevation(),
    @Suppress("UNUSED_PARAMETER")
    border: BorderStroke? = FilterChipDefaults.filterChipBorder(
        enabled = enabled,
        selected = selected,
    ),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(FilterChipDefaults.HorizontalSpacing),
    contentPadding: PaddingValues = FilterChipDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    style: GlassStyle = GlassStyle.Regular,
    glassBorderWidth: Dp = 0.9.dp,
    @Suppress("UNUSED_PARAMETER")
    selectedColor: Color = Color.Unspecified,
    glassContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    glassSelectedContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedSurfaceStyle: GlassStyle? = null,
) {
    val settingsState = LocalSettingsState.current
    // 指定选中表面层级时，使用和 GlassCard 相同的玻璃与平涂底色。
    val usesSurfaceStyle = enabled && selectedSurfaceStyle != null
    val materialColors = FilterChipDefaults.filterChipColors()
    val usesDefaultContainerColors = (
        colors.containerColor == materialColors.containerColor &&
            colors.selectedContainerColor == materialColors.selectedContainerColor
        ) || (
        colors.containerColor == MaterialTheme.colorScheme.surfaceContainerLow &&
            colors.selectedContainerColor == MaterialTheme.colorScheme.primaryContainer
        )

    LocalMinimumInteractiveComponentSize.ProvidesValue(Dp.Unspecified) {
        if (!settingsState.isGlassAlphaEnabled && !usesSurfaceStyle) {
            FilterChip(
                selected = selected,
                onClick = onClick,
                label = label,
                modifier = modifier,
                enabled = enabled,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                shape = shape,
                colors = colors.copy(
                    containerColor = if (usesDefaultContainerColors) {
                        flatGlassContainerColor(
                            color = glassContainerColor,
                            style = style,
                            backgroundAlpha = glassSelectionBackgroundAlpha(style, selected = false),
                        )
                    } else {
                        flatGlassContainerColor(
                            color = colors.containerColor,
                            style = style,
                        )
                    },
                    selectedContainerColor = if (usesDefaultContainerColors) {
                        flatGlassContainerColor(
                            color = glassSelectedContainerColor,
                            style = style,
                            backgroundAlpha = glassSelectionBackgroundAlpha(style, selected = true),
                        )
                    } else if (style == GlassStyle.None) {
                        flatGlassContainerColor(
                            color = colors.selectedContainerColor,
                            style = style,
                        )
                    } else {
                        colors.selectedContainerColor
                    },
                    disabledContainerColor = if (style == GlassStyle.None) {
                        flatGlassContainerColor(
                            color = colors.disabledContainerColor,
                            style = style,
                        )
                    } else {
                        colors.disabledContainerColor
                    },
                    disabledSelectedContainerColor = if (style == GlassStyle.None) {
                        flatGlassContainerColor(
                            color = colors.disabledSelectedContainerColor,
                            style = style,
                        )
                    } else {
                        colors.disabledSelectedContainerColor
                    },
                ),
                elevation = elevation,
                border = null,
                horizontalArrangement = horizontalArrangement,
                contentPadding = contentPadding,
                interactionSource = interactionSource,
            )
        } else {
            val containerColor = if (selected) glassSelectedContainerColor else glassContainerColor
            val surfaceModifier = if (usesSurfaceStyle) {
                val surfaceStyle = if (selected) selectedSurfaceStyle ?: style else style
                modifier.glassSimpleStyle(
                    style = surfaceStyle,
                    shape = shape,
                    color = containerColor,
                    borderWidth = glassBorderWidth,
                )
            } else {
                modifier.glassSelectionBackground(
                    selected = selected,
                    style = style,
                    shape = shape,
                    color = containerColor,
                    borderWidth = glassBorderWidth,
                    enabled = enabled,
                )
            }
            val resolvedColors = colors.copy(
                containerColor = Color.Transparent,
                selectedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                disabledSelectedContainerColor = Color.Transparent,
            )

            FilterChip(
                selected = selected,
                onClick = onClick,
                label = label,
                modifier = surfaceModifier,
                enabled = enabled,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                shape = shape,
                colors = resolvedColors,
                elevation = elevation,
                border = null,
                horizontalArrangement = horizontalArrangement,
                contentPadding = contentPadding,
                interactionSource = interactionSource,
            )
        }
    }
}


