package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.utils.helper.ProvidesValue

/**
 * 毛玻璃风格 [RadioButton] —— 参数与 M3 [RadioButton] 对齐。
 *
 * - **Glassmorphism 开启** → 外圈用玻璃描边/底色 ([selectedGlassColor] / [unselectedGlassColor])
 * - **Glassmorphism 关闭** → 退化为标准 M3 [RadioButton]
 */
@Composable
fun GlassRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: RadioButtonColors = RadioButtonDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
    style: GlassStyle = GlassStyle.Regular,
    selectedGlassColor: Color = MaterialTheme.colorScheme.primary,
    unselectedGlassColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    glassBorderWidth: Dp = 0.9.dp,
) {
    val settingsState = LocalSettingsState.current

    if (!settingsState.isGlassAlphaEnabled) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = colors,
            interactionSource = interactionSource,
        )
        return
    }

    val glassColor = if (selected) selectedGlassColor else unselectedGlassColor
    val transparentColors = colors.copy(
        selectedColor = Color.Transparent,
        unselectedColor = Color.Transparent,
        disabledSelectedColor = Color.Transparent,
        disabledUnselectedColor = Color.Transparent,
    )

    LocalMinimumInteractiveComponentSize.ProvidesValue(Dp.Unspecified) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            modifier = modifier.glassControlStyle(
                style = style,
                backgroundAlpha = if (selected) {
                    (style.backgroundAlpha + 0.10f).coerceAtMost(1f)
                } else {
                    (style.backgroundAlpha + 0.04f).coerceAtMost(1f)
                },
                shape = androidx.compose.foundation.shape.CircleShape,
                color = glassColor,
                borderWidth = glassBorderWidth,
                enabled = enabled,
            ),
            enabled = enabled,
            colors = transparentColors,
            interactionSource = interactionSource,
        )
    }
}
