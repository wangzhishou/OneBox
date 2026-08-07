package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.utils.helper.ProvidesValue

/**
 * 毛玻璃风格 OutlinedButton —— 参数与 Material3 [OutlinedButton] 完全对齐。
 *
 * - **Glassmorphism 开启** → 内部容器透明，由 [glassControlStyle] 接管玻璃描边/底色
 * - **Glassmorphism 关闭** → 退化为标准 M3 [OutlinedButton]
 *
 * @param style              毛玻璃浓度等级
 * @param color              玻璃底色（玻璃开启时作为容器色调）
 * @param contentColor       内容（文字/图标）颜色
 * @param borderWidth        描边宽度
 */
@Composable
fun GlassOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp),
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    style: GlassStyle = GlassStyle.Regular,
    borderWidth: Dp = 1.dp,
    @Suppress("UNUSED_PARAMETER")
    blurRadius: Dp = 20.dp,
    colors: ButtonColors? = null,
    border: BorderStroke? = null,
    elevation: ButtonElevation? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val settingsState = LocalSettingsState.current

    if (!settingsState.isGlassAlphaEnabled) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = colors ?: ButtonDefaults.outlinedButtonColors(
                contentColor = contentColor,
            ),
            elevation = elevation,
            border = border,
            contentPadding = contentPadding,
            interactionSource = interactionSource,
            content = content,
        )
        return
    }

    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val resolvedContainerColor = colors.containerColor(enabled = enabled).takeIfSpecifiedOrElse(color)
    val resolvedContentColor = colors.contentColor(enabled = enabled).takeIfSpecifiedOrElse(contentColor)
    val disabledContentColor = colors.contentColor(enabled = false).takeIfSpecifiedOrElse(
        contentColor.copy(alpha = 0.38f),
    )

    LocalMinimumInteractiveComponentSize.ProvidesValue(Dp.Unspecified) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.glassControlStyle(
                style = style,
                backgroundAlpha = (style.backgroundAlpha + 0.02f).coerceAtMost(1f),
                shape = shape,
                color = resolvedContainerColor,
                borderWidth = borderWidth,
                enabled = enabled,
            ),
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = resolvedContentColor,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = disabledContentColor,
            ),
            elevation = null,
            border = null,
            contentPadding = contentPadding,
            interactionSource = interactionSource,
            content = content,
        )
    }
}

@Stable
private fun ButtonColors?.containerColor(enabled: Boolean): Color = when {
    this == null -> Color.Unspecified
    enabled -> containerColor
    else -> disabledContainerColor
}

@Stable
private fun ButtonColors?.contentColor(enabled: Boolean): Color = when {
    this == null -> Color.Unspecified
    enabled -> contentColor
    else -> disabledContentColor
}

private fun Color.takeIfSpecifiedOrElse(fallback: Color): Color {
    return if (this != Color.Unspecified && this != Color.Transparent) this else fallback
}
