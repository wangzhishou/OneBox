package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.utils.helper.ProvidesValue

/**
 * Glass-styled tonal icon button that stays as close as possible to Material3
 * [FilledTonalIconButton] and only replaces the container rendering.
 */
@Composable
fun GlassTonalIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = IconButtonDefaults.filledShape,
    colors: IconButtonColors = IconButtonDefaults.filledTonalIconButtonColors(),
    interactionSource: MutableInteractionSource? = null,
    style: GlassStyle = GlassStyle.Medium,
    glassBorderWidth: Dp = 0.9.dp,
    content: @Composable () -> Unit,
) {
    val settingsState = LocalSettingsState.current

    if (!settingsState.isGlassAlphaEnabled) {
        FilledTonalIconButton(
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

    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by resolvedInteractionSource.collectIsPressedAsState()
    val containerColor = colors.containerColor(enabled = enabled)
    val contentColor = colors.contentColor(enabled = enabled)
    val disabledContentColor = colors.contentColor(enabled = false)
    val animatedBackgroundAlpha by animateFloatAsState(
        targetValue = when {
            !enabled -> (style.backgroundAlpha + 0.03f).coerceAtMost(1f)
            isPressed -> (style.backgroundAlpha + 0.15f).coerceAtMost(1f)
            else -> (style.backgroundAlpha + 0.10f).coerceAtMost(1f)
        },
        animationSpec = tween(durationMillis = if (isPressed) 140 else 220),
        label = "GlassTonalIconButtonBackgroundAlpha",
    )

    LocalMinimumInteractiveComponentSize.ProvidesValue(Dp.Unspecified) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = modifier.glassControlStyle(
                style = style,
                backgroundAlpha = animatedBackgroundAlpha,
                shape = shape,
                color = containerColor.takeIfSpecifiedOrElse(colors.contentColor(enabled = true)),
                borderWidth = glassBorderWidth,
                enabled = enabled,
                showInnerHighlight = true,
            ),
            enabled = enabled,
            shape = shape,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = Color.Transparent,
                contentColor = contentColor,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = disabledContentColor,
            ),
            interactionSource = resolvedInteractionSource,
            content = content,
        )
    }
}

@Stable
private fun IconButtonColors.containerColor(enabled: Boolean): Color = if (enabled) {
    containerColor
} else {
    disabledContainerColor
}

@Stable
private fun IconButtonColors.contentColor(enabled: Boolean): Color = if (enabled) {
    contentColor
} else {
    disabledContentColor
}

private fun Color.takeIfSpecifiedOrElse(fallback: Color): Color {
    return if (this != Color.Unspecified && this != Color.Transparent) this else fallback
}
