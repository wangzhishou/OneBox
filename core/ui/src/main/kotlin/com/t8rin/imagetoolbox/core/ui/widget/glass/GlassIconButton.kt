package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.utils.helper.ProvidesValue

/**
 * 毛玻璃风格 [IconButton]（标准变体，无填充）—— 参数与 M3 对齐。
 */
@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource? = null,
    style: GlassStyle = GlassStyle.Thin,
    glassBorderWidth: Dp = 0.9.dp,
    content: @Composable () -> Unit,
) {
    val settingsState = LocalSettingsState.current

    if (!settingsState.isGlassAlphaEnabled) {
        IconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = colors,
            interactionSource = interactionSource,
            content = content,
        )
        return
    }

    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val resolvedContentColor = colors.contentColor(enabled = enabled)
    val glassColor = colors.containerColor(enabled = enabled).takeIfSpecifiedOrElse(
        MaterialTheme.colorScheme.surface,
    )

    LocalMinimumInteractiveComponentSize.ProvidesValue(Dp.Unspecified) {
        IconButton(
            onClick = onClick,
            modifier = modifier.glassControlStyle(
                style = style,
                backgroundAlpha = (style.backgroundAlpha + 0.04f).coerceAtMost(1f),
                shape = RoundedCornerShape(50),
                color = glassColor,
                borderWidth = glassBorderWidth,
                enabled = enabled,
            ),
            enabled = enabled,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Transparent,
                contentColor = resolvedContentColor,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = colors.contentColor(enabled = false),
            ),
            interactionSource = interactionSource,
            content = content,
        )
    }
}

/**
 * 毛玻璃风格 [FilledIconButton]（实心填充）—— 参数与 M3 对齐。
 */
@Composable
fun GlassFilledIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = IconButtonDefaults.filledShape,
    colors: IconButtonColors = IconButtonDefaults.filledIconButtonColors(),
    interactionSource: MutableInteractionSource? = null,
    style: GlassStyle = GlassStyle.Medium,
    glassBorderWidth: Dp = 0.9.dp,
    content: @Composable () -> Unit,
) {
    val settingsState = LocalSettingsState.current

    if (!settingsState.isGlassAlphaEnabled) {
        FilledIconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = colors,
            interactionSource = interactionSource,
            content = content,
        )
        return
    }

    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val resolvedContentColor = colors.contentColor(enabled = enabled)
    val glassColor = colors.containerColor(enabled = enabled).takeIfSpecifiedOrElse(
        MaterialTheme.colorScheme.primary,
    )

    LocalMinimumInteractiveComponentSize.ProvidesValue(Dp.Unspecified) {
        FilledIconButton(
            onClick = onClick,
            modifier = modifier.glassControlStyle(
                style = style,
                backgroundAlpha = (style.backgroundAlpha + 0.10f).coerceAtMost(1f),
                shape = shape,
                color = glassColor,
                borderWidth = glassBorderWidth,
                enabled = enabled,
            ),
            enabled = enabled,
            shape = shape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.Transparent,
                contentColor = resolvedContentColor,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = colors.contentColor(enabled = false),
            ),
            interactionSource = interactionSource,
            content = content,
        )
    }
}

/**
 * 毛玻璃风格 [OutlinedIconButton]（描边变体）—— 参数与 M3 对齐。
 */
@Composable
fun GlassOutlinedIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = IconButtonDefaults.outlinedShape,
    colors: IconButtonColors = IconButtonDefaults.outlinedIconButtonColors(),
    border: BorderStroke? = IconButtonDefaults.outlinedIconButtonBorder(enabled),
    interactionSource: MutableInteractionSource? = null,
    style: GlassStyle = GlassStyle.Regular,
    glassBorderWidth: Dp = 1.dp,
    content: @Composable () -> Unit,
) {
    val settingsState = LocalSettingsState.current

    if (!settingsState.isGlassAlphaEnabled) {
        OutlinedIconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = colors,
            border = border,
            interactionSource = interactionSource,
            content = content,
        )
        return
    }

    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val resolvedContentColor = colors.contentColor(enabled = enabled)
    val glassColor = colors.containerColor(enabled = enabled).takeIfSpecifiedOrElse(
        Color.Transparent,
    )

    LocalMinimumInteractiveComponentSize.ProvidesValue(Dp.Unspecified) {
        OutlinedIconButton(
            onClick = onClick,
            modifier = modifier.glassControlStyle(
                style = style,
                backgroundAlpha = (style.backgroundAlpha + 0.03f).coerceAtMost(1f),
                shape = shape,
                color = glassColor,
                borderWidth = glassBorderWidth,
                enabled = enabled,
            ),
            enabled = enabled,
            shape = shape,
            colors = IconButtonDefaults.outlinedIconButtonColors(
                containerColor = Color.Transparent,
                contentColor = resolvedContentColor,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = colors.contentColor(enabled = false),
            ),
            border = null,
            interactionSource = interactionSource,
            content = content,
        )
    }
}

private fun IconButtonColors.containerColor(enabled: Boolean): Color = if (enabled) {
    containerColor
} else {
    disabledContainerColor
}

private fun IconButtonColors.contentColor(enabled: Boolean): Color = if (enabled) {
    contentColor
} else {
    disabledContentColor
}

private fun Color.takeIfSpecifiedOrElse(fallback: Color): Color {
    return if (this != Color.Unspecified && this != Color.Transparent) this else fallback
}
