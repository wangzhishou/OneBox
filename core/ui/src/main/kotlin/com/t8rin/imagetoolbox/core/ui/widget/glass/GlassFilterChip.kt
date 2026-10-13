package com.t8rin.imagetoolbox.core.ui.widget.glass

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
import com.t8rin.imagetoolbox.core.ui.utils.helper.ProvidesValue

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
    shape: Shape = FilterChipDefaults.shape,
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
) {
    val settingsState = LocalSettingsState.current

    LocalMinimumInteractiveComponentSize.ProvidesValue(Dp.Unspecified) {
        if (!settingsState.isGlassAlphaEnabled) {
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
                    containerColor = flatGlassContainerColor(
                        color = colors.containerColor,
                        style = style,
                    ),
                    // 实底（GlassStyle.None）时选中态也铺容器色，不再留 M3 默认的描边空底。
                    // 其它浓度保持原选中色，避免把全站 chip 的选中底一起洗淡。
                    selectedContainerColor = if (style == GlassStyle.None) {
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
                modifier = modifier.glassControlStyle(
                    style = style,
                    backgroundAlpha = if (selected) {
                        (style.backgroundAlpha + 0.12f).coerceAtMost(1f)
                    } else {
                        (style.backgroundAlpha + 0.04f).coerceAtMost(1f)
                    },
                    shape = shape,
                    color = if (selected) glassSelectedContainerColor else glassContainerColor,
                    borderWidth = glassBorderWidth,
                    enabled = enabled,
                ),
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



