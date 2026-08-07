package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.utils.helper.ProvidesValue

/**
 * 毛玻璃风格 Checkbox —— 参数与 Material3 [Checkbox] 完全对齐。
 *
 * - **Glassmorphism 开启** → 复选框外圈使用玻璃描边 / 背景；选中态由 [checkedGlassColor] 着色
 * - **Glassmorphism 关闭** → 退化为标准 M3 [Checkbox]
 *
 * @param style              毛玻璃浓度等级
 * @param checkedGlassColor  选中时玻璃底色
 * @param uncheckedGlassColor 未选中时玻璃底色
 * @param glassBorderWidth   描边宽度
 */
@Composable
fun GlassCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CheckboxColors = CheckboxDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
    style: GlassStyle = GlassStyle.Regular,
    checkedGlassColor: Color = MaterialTheme.colorScheme.primary,
    uncheckedGlassColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    glassBorderWidth: Dp = 0.9.dp,
) {
    val settingsState = LocalSettingsState.current

    if (!settingsState.isGlassAlphaEnabled) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
            colors = colors,
            interactionSource = interactionSource,
        )
        return
    }

    val glassColor = if (checked) checkedGlassColor else uncheckedGlassColor
    val transparentBoxColors = colors.copy(
        uncheckedBoxColor = Color.Transparent,
        checkedBoxColor = Color.Transparent,
        disabledUncheckedBoxColor = Color.Transparent,
        disabledCheckedBoxColor = Color.Transparent,
    )

    LocalMinimumInteractiveComponentSize.ProvidesValue(Dp.Unspecified) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier.glassControlStyle(
                style = style,
                backgroundAlpha = if (checked) {
                    (style.backgroundAlpha + 0.12f).coerceAtMost(1f)
                } else {
                    (style.backgroundAlpha + 0.05f).coerceAtMost(1f)
                },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                color = glassColor,
                borderWidth = glassBorderWidth,
                enabled = enabled,
            ),
            enabled = enabled,
            colors = transparentBoxColors,
            interactionSource = interactionSource,
        )
    }
}
