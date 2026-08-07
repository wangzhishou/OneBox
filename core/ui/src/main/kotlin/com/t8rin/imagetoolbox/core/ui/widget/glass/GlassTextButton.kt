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
import androidx.compose.material3.TextButton
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
 * 毛玻璃风格 TextButton —— 参数与 Material3 [TextButton] 完全对齐。
 *
 * - **Glassmorphism 开启** → 容器背景透明 + 自带 press 时玻璃高亮（通过 [color] 控制）
 * - **Glassmorphism 关闭** → 退化为标准 M3 [TextButton]
 *
 * @param style          毛玻璃浓度等级（影响 press 态背景）
 * @param color          press 时玻璃底色
 * @param contentColor   内容（文字）颜色
 */
@Composable
fun GlassTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(8.dp),
    color: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
    contentColor: Color = MaterialTheme.colorScheme.primary,
    style: GlassStyle = GlassStyle.Thin,
    borderWidth: Dp = 0.dp,
    @Suppress("UNUSED_PARAMETER")
    blurRadius: Dp = 20.dp,
    colors: ButtonColors? = null,
    border: BorderStroke? = null,
    elevation: ButtonElevation? = null,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val settingsState = LocalSettingsState.current

    if (!settingsState.isGlassAlphaEnabled) {
        TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = colors ?: ButtonDefaults.textButtonColors(
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

    LocalMinimumInteractiveComponentSize.ProvidesValue(Dp.Unspecified) {
        TextButton(
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
            colors = ButtonDefaults.textButtonColors(
                containerColor = Color.Transparent,
                contentColor = resolvedContentColor,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = resolvedContentColor.copy(alpha = 0.38f),
            ),
            elevation = null,
            border = border,
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
