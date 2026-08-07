package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ChipColors
import androidx.compose.material3.ChipElevation
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.utils.helper.ProvidesValue

/**
 * 毛玻璃风格 [AssistChip] —— 参数与 Material3 [AssistChip] 对齐。
 *
 * - **Glassmorphism 开启** → 容器背景由玻璃接管 ([glassContainerColor] 作为底色)
 * - **Glassmorphism 关闭** → 退化为标准 M3 [AssistChip]
 *
 * @param style              毛玻璃浓度等级
 * @param glassContainerColor 玻璃底色
 * @param glassBorderWidth   描边宽度
 */
@Composable
fun GlassAssistChip(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = AssistChipDefaults.shape,
    colors: ChipColors = AssistChipDefaults.assistChipColors(),
    elevation: ChipElevation? = AssistChipDefaults.assistChipElevation(),
    border: BorderStroke? = AssistChipDefaults.assistChipBorder(enabled),
    horizontalArrangement: Arrangement.Horizontal = AssistChipDefaults.horizontalArrangement(),
    contentPadding: PaddingValues = AssistChipDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    style: GlassStyle = GlassStyle.Regular,
    glassContainerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    glassBorderWidth: Dp = 0.9.dp,
) {
    val settingsState = LocalSettingsState.current

    LocalMinimumInteractiveComponentSize.ProvidesValue(Dp.Unspecified) {
        if (!settingsState.isGlassAlphaEnabled) {
            AssistChip(
                onClick = onClick,
                label = label,
                modifier = modifier,
                enabled = enabled,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                shape = shape,
                colors = colors,
                elevation = elevation,
                border = border,
                horizontalArrangement = horizontalArrangement,
                contentPadding = contentPadding,
                interactionSource = interactionSource,
            )
        } else {
            val transparentColors = colors.copy(
                containerColor = Color.Transparent,
                labelColor = Color.Unspecified,
                leadingIconContentColor = Color.Unspecified,
                trailingIconContentColor = Color.Unspecified,
                disabledContainerColor = Color.Transparent,
                disabledLabelColor = Color.Unspecified,
                disabledLeadingIconContentColor = Color.Unspecified,
                disabledTrailingIconContentColor = Color.Unspecified,
            )
            AssistChip(
                onClick = onClick,
                label = label,
                modifier = modifier.glassControlStyle(
                    style = style,
                    backgroundAlpha = (style.backgroundAlpha + 0.04f).coerceAtMost(1f),
                    shape = shape,
                    color = glassContainerColor,
                    borderWidth = glassBorderWidth,
                    enabled = enabled,
                ),
                enabled = enabled,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                shape = shape,
                colors = transparentColors,
                elevation = elevation,
                border = null,
                horizontalArrangement = horizontalArrangement,
                contentPadding = contentPadding,
                interactionSource = interactionSource,
            )
        }
    }
}

/**
 * 毛玻璃风格 [SuggestionChip] —— 参数与 Material3 [SuggestionChip] 对齐。
 *
 * 与 [GlassAssistChip] 区别仅在于 `icon` 而非 `leadingIcon`。
 */
@Composable
fun GlassSuggestionChip(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    shape: Shape = SuggestionChipDefaults.shape,
    colors: ChipColors = SuggestionChipDefaults.suggestionChipColors(),
    elevation: ChipElevation? = SuggestionChipDefaults.suggestionChipElevation(),
    border: BorderStroke? = SuggestionChipDefaults.suggestionChipBorder(enabled),
    horizontalArrangement: Arrangement.Horizontal = SuggestionChipDefaults.horizontalArrangement(),
    contentPadding: PaddingValues = SuggestionChipDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    style: GlassStyle = GlassStyle.Regular,
    glassContainerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    glassBorderWidth: Dp = 0.9.dp,
) {
    val settingsState = LocalSettingsState.current

    LocalMinimumInteractiveComponentSize.ProvidesValue(Dp.Unspecified) {
        if (!settingsState.isGlassAlphaEnabled) {
            SuggestionChip(
                onClick = onClick,
                label = label,
                modifier = modifier,
                enabled = enabled,
                icon = icon,
                shape = shape,
                colors = colors,
                elevation = elevation,
                border = border,
                horizontalArrangement = horizontalArrangement,
                contentPadding = contentPadding,
                interactionSource = interactionSource,
            )
        } else {
            val transparentColors = colors.copy(
                containerColor = Color.Transparent,
                labelColor = Color.Unspecified,
                leadingIconContentColor = Color.Unspecified,
                trailingIconContentColor = Color.Unspecified,
                disabledContainerColor = Color.Transparent,
                disabledLabelColor = Color.Unspecified,
                disabledLeadingIconContentColor = Color.Unspecified,
                disabledTrailingIconContentColor = Color.Unspecified,
            )
            SuggestionChip(
                onClick = onClick,
                label = label,
                modifier = modifier.glassControlStyle(
                    style = style,
                    backgroundAlpha = (style.backgroundAlpha + 0.04f).coerceAtMost(1f),
                    shape = shape,
                    color = glassContainerColor,
                    borderWidth = glassBorderWidth,
                    enabled = enabled,
                ),
                enabled = enabled,
                icon = icon,
                shape = shape,
                colors = transparentColors,
                elevation = elevation,
                border = null,
                horizontalArrangement = horizontalArrangement,
                contentPadding = contentPadding,
                interactionSource = interactionSource,
            )
        }
    }
}
