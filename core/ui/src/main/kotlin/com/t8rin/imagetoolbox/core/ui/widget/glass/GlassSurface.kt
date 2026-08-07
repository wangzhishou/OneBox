package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState

@Immutable
enum class GlassStyle(
    val backgroundAlpha: Float,
    val borderAlpha: Float,
    val tintAlpha: Float = 0.25f,
    val highlightAlpha: Float = 0.35f,
    val innerShadowAlpha: Float = 0.05f,
    val surfaceOverlayAlpha: Float = 0.03f,
) {
    Transparent(
        backgroundAlpha = 0f,
        borderAlpha = 0f,
        tintAlpha = 0f,
        highlightAlpha = 0f,
        innerShadowAlpha = 0f,
        surfaceOverlayAlpha = 0f,
    ),

    Thin(
        backgroundAlpha = 0.08f,
        borderAlpha = 0.38f,
        tintAlpha = 0.14f,
        highlightAlpha = 0.12f,
        innerShadowAlpha = 0.010f,
        surfaceOverlayAlpha = 0.010f,
    ),

    Regular(
        backgroundAlpha = 0.12f,
        borderAlpha = 0.44f,
        tintAlpha = 0.18f,
        highlightAlpha = 0.14f,
        innerShadowAlpha = 0.015f,
        surfaceOverlayAlpha = 0.012f,
    ),

    Medium(
        backgroundAlpha = 0.15f,
        borderAlpha = 0.50f,
        tintAlpha = 0.22f,
        highlightAlpha = 0.16f,
        innerShadowAlpha = 0.018f,
        surfaceOverlayAlpha = 0.014f,
    ),

    Thick(
        backgroundAlpha = 0.19f,
        borderAlpha = 0.58f,
        tintAlpha = 0.26f,
        highlightAlpha = 0.19f,
        innerShadowAlpha = 0.022f,
        surfaceOverlayAlpha = 0.016f,
    ),

    Dense(
        backgroundAlpha = 0.24f,
        borderAlpha = 0.64f,
        tintAlpha = 0.30f,
        highlightAlpha = 0.22f,
        innerShadowAlpha = 0.026f,
        surfaceOverlayAlpha = 0.018f,
    ),

    Darker(
        backgroundAlpha = 1f,
        borderAlpha = 1f,
        tintAlpha = 0f,
        highlightAlpha = 0f,
        innerShadowAlpha = 0f,
        surfaceOverlayAlpha = 0f,
    ),

    None(
        backgroundAlpha = 1f,
        borderAlpha = 1f,
        tintAlpha = 0f,
        highlightAlpha = 0f,
        innerShadowAlpha = 0f,
        surfaceOverlayAlpha = 0f,
    ),
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    style: GlassStyle = GlassStyle.Regular,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = Color.Unspecified,
    borderWidth: Dp = 0.9.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val settingsState = LocalSettingsState.current

    if (!settingsState.isGlassAlphaEnabled || style == GlassStyle.None) {
        val fallbackColor = when {
            style == GlassStyle.Transparent -> {
                Color.Transparent
            }

            color != Color.Unspecified -> {
                color
            }

            else -> {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        }.withGlassBaseAlpha(settingsState.glassBaseAlpha)
        Box(
            modifier = modifier
                .clip(shape)
                .background(fallbackColor, shape),
            propagateMinConstraints = true,
            content = content,
        )
        return
    }

    Box(
        modifier = modifier.glassSimpleStyle(
            style = style,
            shape = shape,
            color = color,
            borderWidth = borderWidth,
        ),
        propagateMinConstraints = true,
        content = content,
    )
}

@Composable
fun GlassSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp),
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(color),
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource? = null,
    style: GlassStyle = GlassStyle.Regular,
    borderWidth: Dp = 0.9.dp,
    content: @Composable () -> Unit,
) {
    val settingsState = LocalSettingsState.current
    val glassTint = if (color == Color.Transparent) Color.Unspecified else color
    val resolvedShadowElevation =
        if (settingsState.drawContainerShadows) shadowElevation else 0.dp

    if (!settingsState.isGlassAlphaEnabled || style == GlassStyle.None) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            color = color.withGlassBaseAlpha(settingsState.glassBaseAlpha),
            contentColor = contentColor,
            tonalElevation = tonalElevation,
            shadowElevation = resolvedShadowElevation,
            border = border,
            interactionSource = interactionSource,
            content = content,
        )
        return
    }

    Surface(
        onClick = onClick,
        modifier = modifier.glassBackground(
            style = style,
            shape = shape,
            color = glassTint,
            borderWidth = borderWidth,
        ),
        enabled = enabled,
        shape = shape,
        color = Color.Transparent,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = 0.dp,
        border = border,
        interactionSource = interactionSource,
        content = content,
    )
}

