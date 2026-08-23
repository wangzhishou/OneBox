package com.t8rin.imagetoolbox.core.ui.widget.glass

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LayerOutsets
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.shifenmiao.interfaces.singleton.AppContext
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.theme.blend
import kotlin.math.max
import kotlin.math.roundToInt

private const val MIN_VISIBLE_GLASS_DECORATION_ALPHA = 0.04f

internal fun Color.withGlassBaseAlpha(glassBaseAlpha: Float): Color {
    if (this == Color.Unspecified || this == Color.Transparent) return this
    return copy(alpha = (alpha * glassBaseAlpha).coerceIn(0f, 1f))
}

@Immutable
private data class GlassDecorationColors(
    val fillColor: Color,
    val tintColor: Color,
    val sheenColor: Color,
    val depthColor: Color,
    val borderColor: Color,
    val topBorderColor: Color,
    val innerBorderColor: Color,
    val edgeTransitionColor: Color,
    val rimLightColor: Color,
    val chromaticEdgeColor: Color,
    val causticColor: Color,
)

@Immutable
private data class GlassControlDecorationColors(
    val fillColor: Color,
    val tintColor: Color,
    val borderColor: Color,
    val innerBorderColor: Color,
    val topEdgeColor: Color,
    val bottomShadeColor: Color,
    val innerHighlightColor: Color,
)

@Composable
fun Modifier.glassBackground(
    style: GlassStyle = GlassStyle.Regular,
    shape: Shape = RoundedCornerShape(12.dp),
    color: Color = Color.Unspecified,
    borderWidth: Dp = 1.dp,
    blurRadius: Dp = 24.dp,
): Modifier {
    val settingsState = LocalSettingsState.current
    if (!settingsState.isGlassAlphaEnabled || style == GlassStyle.None) {
        return fallbackGlassBackground(
            shape = shape,
            color = color,
            style = style,
        )
    }

    return glassSimpleStyle(
        style = style,
        shape = shape,
        color = color,
        borderWidth = borderWidth,
        blurRadius = blurRadius,
    )
}

@Composable
internal fun Modifier.glassSimpleStyle(
    style: GlassStyle = GlassStyle.Regular,
    backgroundAlpha: Float = style.backgroundAlpha,
    shape: Shape,
    color: Color = Color.Unspecified,
    borderWidth: Dp = 1.dp,
    blurRadius: Dp = 24.dp,
    showTopEdgeEffects: Boolean = true,
    showBottomEdgeEffects: Boolean = true,
    liquidOverride: Boolean? = null,
): Modifier {
    val settingsState = LocalSettingsState.current
    val colorScheme = MaterialTheme.colorScheme
    val isLiquidGlass = liquidOverride ?: settingsState.isLiquidGlassEnabled
    val isLight = colorScheme.surface.luminance() > 0.5f
    val baseColor = if (color != Color.Unspecified) {
        color
    } else {
        colorScheme.surfaceContainerHighest
    }
    val glassBaseAlpha = settingsState.glassBaseAlpha.coerceIn(0f, 1f)
    val isTintedSurface = color != Color.Unspecified
    val scaledBackgroundAlpha = (backgroundAlpha * glassBaseAlpha).coerceIn(0f, 1f)
    val colors = remember(baseColor, colorScheme, style, scaledBackgroundAlpha, glassBaseAlpha, isLight, isTintedSurface, isLiquidGlass) {
        createGlassDecorationColors(
            style = style,
            colorSchemeSurface = colorScheme.surface,
            colorSchemeOutline = colorScheme.outlineVariant,
            colorSchemePrimary = colorScheme.primary,
            colorSchemeSurfaceTint = colorScheme.surfaceTint,
            colorSchemeScrim = colorScheme.scrim,
            baseColor = baseColor,
            backgroundAlpha = scaledBackgroundAlpha,
            glassBaseAlpha = glassBaseAlpha,
            isLight = isLight,
            isTintedSurface = isTintedSurface,
            isLiquidGlass = isLiquidGlass,
        )
    }
    val useLiquidBlur = isLiquidGlass &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        blurRadius > 0.dp &&
        (style == GlassStyle.Thick || style == GlassStyle.Dense)

    return ultraFlatGlassDecoration(
        style = style,
        shape = shape,
        borderWidth = borderWidth,
        blurRadius = blurRadius,
        colors = colors,
        isLight = isLight,
        isLiquidGlass = isLiquidGlass,
        useLiquidBlur = useLiquidBlur,
        showTopEdgeEffects = showTopEdgeEffects,
        showBottomEdgeEffects = showBottomEdgeEffects,
    )
}

@Composable
internal fun Modifier.glassControlStyle(
    style: GlassStyle = GlassStyle.Regular,
    backgroundAlpha: Float = (style.backgroundAlpha + 0.08f).coerceAtMost(1f),
    shape: Shape,
    color: Color = Color.Unspecified,
    borderWidth: Dp = 0.9.dp,
    enabled: Boolean = true,
    showTopEdge: Boolean = true,
    showInnerHighlight: Boolean = false,
): Modifier {
    val settingsState = LocalSettingsState.current
    val colorScheme = MaterialTheme.colorScheme
    val isLiquidGlass = settingsState.isLiquidGlassEnabled
    val isLight = colorScheme.surface.luminance() > 0.5f
    val baseColor = if (color != Color.Unspecified) color else colorScheme.surfaceContainerHigh
    val isTintedSurface = color != Color.Unspecified
    val glassBaseAlpha = settingsState.glassBaseAlpha.coerceIn(0f, 1f)
    val scaledBackgroundAlpha = (backgroundAlpha * glassBaseAlpha).coerceIn(0f, 1f)
    val colors = remember(baseColor, colorScheme, style, scaledBackgroundAlpha, glassBaseAlpha, isLight, isTintedSurface, enabled, isLiquidGlass) {
        createGlassControlDecorationColors(
            style = style,
            colorSchemeSurface = colorScheme.surface,
            colorSchemeOutline = colorScheme.outlineVariant,
            colorSchemePrimary = colorScheme.primary,
            colorSchemeSurfaceTint = colorScheme.surfaceTint,
            colorSchemeScrim = colorScheme.scrim,
            baseColor = baseColor,
            backgroundAlpha = scaledBackgroundAlpha,
            glassBaseAlpha = glassBaseAlpha,
            isLight = isLight,
            isTintedSurface = isTintedSurface,
            enabled = enabled,
            isLiquidGlass = isLiquidGlass,
        )
    }

    return controlSurfaceDecoration(
        shape = shape,
        borderWidth = borderWidth,
        colors = colors,
        isLiquidGlass = isLiquidGlass,
        showTopEdge = showTopEdge,
        showInnerHighlight = showInnerHighlight,
    )
}

private fun createGlassDecorationColors(
    style: GlassStyle,
    colorSchemeSurface: Color,
    colorSchemeOutline: Color,
    colorSchemePrimary: Color,
    colorSchemeSurfaceTint: Color,
    colorSchemeScrim: Color,
    baseColor: Color,
    backgroundAlpha: Float,
    glassBaseAlpha: Float,
    isLight: Boolean,
    isTintedSurface: Boolean,
    isLiquidGlass: Boolean,
): GlassDecorationColors {
    val flattenedBase = if (isTintedSurface) {
        if (isLight) baseColor.blend(colorSchemeSurface, 0.08f)
        else baseColor.blend(colorSchemeSurface, 0.06f)
    } else {
        if (isLight) baseColor.blend(colorSchemeSurface, 0.26f)
        else baseColor.blend(colorSchemeSurface, 0.20f)
    }
    val accent = if (isTintedSurface) {
        baseColor.blend(colorSchemeSurfaceTint, if (isLight) 0.08f else 0.06f)
    } else {
        baseColor
            .blend(colorSchemeSurfaceTint, if (isLight) 0.14f else 0.12f)
            .blend(colorSchemePrimary, if (isLight) 0.06f else 0.08f)
    }
    val fillColor = flattenedBase.copy(
        alpha = if (isTintedSurface) {
            (backgroundAlpha * if (isLiquidGlass) 0.40f else 0.30f).coerceAtMost(1f)
        } else {
            (backgroundAlpha * if (isLiquidGlass) 0.50f else 0.38f).coerceAtMost(1f)
        }
    )
    val tintColor = accent.copy(
        alpha = (when {
            isTintedSurface && isLight -> (style.tintAlpha * if (isLiquidGlass) 1.94f else 1.42f).coerceAtMost(if (isLiquidGlass) 0.56f else 0.42f)
            isTintedSurface -> (style.tintAlpha * if (isLiquidGlass) 2.08f else 1.54f).coerceAtMost(if (isLiquidGlass) 0.60f else 0.46f)
            isLight -> (style.tintAlpha * if (isLiquidGlass) 1.14f else 0.82f).coerceAtMost(if (isLiquidGlass) 0.24f else 0.16f)
            else -> (style.tintAlpha * if (isLiquidGlass) 1.30f else 0.96f).coerceAtMost(if (isLiquidGlass) 0.28f else 0.20f)
        } * glassBaseAlpha).coerceIn(0f, 1f)
    )
    val sheenColor = Color.White.copy(
        alpha = ((style.surfaceOverlayAlpha * if (isLiquidGlass) {
            if (isLight) 1.56f else 1.32f
        } else {
            if (isLight) 0.46f else 0.40f
        }).coerceAtMost(if (isLiquidGlass) 0.09f else 0.022f) * glassBaseAlpha).coerceIn(0f, 1f)
    )
    val depthColor = colorSchemeScrim.copy(
        alpha = ((style.innerShadowAlpha * if (isLiquidGlass) {
            if (isLight) 0.72f else 0.82f
        } else {
            if (isLight) 0.16f else 0.20f
        }).coerceAtMost(if (isLiquidGlass) 0.038f else 0.010f) * glassBaseAlpha).coerceIn(0f, 1f)
    )
    val borderBase = if (isTintedSurface) {
        baseColor.blend(colorSchemeOutline, if (isLight) 0.12f else 0.10f)
    } else {
        baseColor.blend(colorSchemeOutline, if (isLight) 0.18f else 0.14f)
    }
    val borderColor = borderBase.copy(
        alpha = (when {
            isTintedSurface && isLight -> (style.borderAlpha * 1.72f).coerceAtMost(0.96f)
            isTintedSurface -> (style.borderAlpha * 1.58f).coerceAtMost(0.90f)
            isLight -> (style.borderAlpha * 1.66f).coerceAtMost(0.82f)
            else -> (style.borderAlpha * 1.52f).coerceAtMost(0.84f)
        } * glassBaseAlpha).coerceIn(0f, 1f)
    )
    val topBorderColor = (if (isTintedSurface) {
        borderBase.blend(Color.White, if (isLight) 0.22f else 0.16f)
    } else {
        borderBase.blend(Color.White, if (isLight) 0.28f else 0.20f)
    }).copy(
        alpha = ((style.highlightAlpha * if (isLiquidGlass) {
            if (isLight) 1.72f else 1.46f
        } else {
            if (isLight) 1.34f else 1.18f
        }).coerceAtMost(if (isLiquidGlass) 0.32f else 0.20f) * glassBaseAlpha).coerceIn(0f, 1f)
    )
    val innerBorderColor = borderBase.blend(Color.White, if (isLight) 0.10f else 0.08f).copy(
        alpha = (if (isLiquidGlass) {
            (style.highlightAlpha * if (isLight) 1.04f else 0.90f).coerceAtMost(0.17f)
        } else {
            (style.highlightAlpha * if (isLight) 0.58f else 0.46f).coerceAtMost(0.10f)
        } * glassBaseAlpha).coerceIn(0f, 1f)
    )
    val expressiveColor = if (isTintedSurface) {
        baseColor
            .blend(colorSchemeSurfaceTint, if (isLight) 0.14f else 0.10f)
            .blend(colorSchemePrimary, if (isLight) 0.08f else 0.12f)
    } else {
        accent
    }
    val edgeTransitionColor = (if (isTintedSurface) {
        expressiveColor.blend(Color.White, if (isLight) 0.28f else 0.18f)
    } else {
        borderBase.blend(AppContext.getColorScheme().surfaceContainerLowest, if (isLight) 0.18f else 0.10f)
    }).copy(
        alpha = (when {
            isLiquidGlass && isTintedSurface -> (style.highlightAlpha * 1.00f).coerceAtMost(0.15f)
            isLiquidGlass -> (style.highlightAlpha * 0.74f).coerceAtMost(0.11f)
            isTintedSurface -> (style.highlightAlpha * 0.72f).coerceAtMost(0.10f)
            else -> (style.highlightAlpha * 0.50f).coerceAtMost(0.065f)
        } * glassBaseAlpha).coerceIn(0f, 1f)
    )
    val rimLightColor = expressiveColor.blend(Color.White, if (isLight) 0.42f else 0.28f).copy(
        alpha = (when {
            isLiquidGlass && isTintedSurface -> {
                (style.highlightAlpha * if (isLight) 1.74f else 1.12f).coerceAtMost(if (isLight) 0.28f else 0.18f)
            }
            isLiquidGlass -> {
                (style.highlightAlpha * if (isLight) 1.12f else 0.72f).coerceAtMost(if (isLight) 0.16f else 0.09f)
            }
            isTintedSurface -> (style.highlightAlpha * 0.46f).coerceAtMost(0.08f)
            else -> (style.highlightAlpha * 0.22f).coerceAtMost(0.035f)
        } * glassBaseAlpha).coerceIn(0f, 1f)
    )
    val chromaticEdgeColor = expressiveColor.copy(
        alpha = (when {
            isLiquidGlass && isTintedSurface -> (style.tintAlpha * 0.92f).coerceAtMost(0.24f)
            isLiquidGlass -> (style.tintAlpha * 0.42f).coerceAtMost(0.08f)
            isTintedSurface -> (style.tintAlpha * 0.34f).coerceAtMost(0.07f)
            else -> (style.tintAlpha * 0.12f).coerceAtMost(0.025f)
        } * glassBaseAlpha).coerceIn(0f, 1f)
    )
    val causticColor = expressiveColor.blend(Color.White, if (isLight) 0.52f else 0.36f).copy(
        alpha = (when {
            isLiquidGlass && isTintedSurface -> {
                (style.surfaceOverlayAlpha * if (isLight) 10.0f else 6.0f).coerceAtMost(if (isLight) 0.18f else 0.10f)
            }
            isLiquidGlass -> {
                (style.surfaceOverlayAlpha * if (isLight) 6.0f else 3.6f).coerceAtMost(if (isLight) 0.11f else 0.055f)
            }
            isTintedSurface -> (style.surfaceOverlayAlpha * 2.8f).coerceAtMost(0.045f)
            else -> (style.surfaceOverlayAlpha * 1.1f).coerceAtMost(0.018f)
        } * glassBaseAlpha).coerceIn(0f, 1f)
    )

    return GlassDecorationColors(
        fillColor = fillColor,
        tintColor = tintColor,
        sheenColor = sheenColor,
        depthColor = depthColor,
        borderColor = borderColor,
        topBorderColor = topBorderColor,
        innerBorderColor = innerBorderColor,
        edgeTransitionColor = edgeTransitionColor,
        rimLightColor = rimLightColor,
        chromaticEdgeColor = chromaticEdgeColor,
        causticColor = causticColor,
    )
}

private fun createGlassControlDecorationColors(
    style: GlassStyle,
    colorSchemeSurface: Color,
    colorSchemeOutline: Color,
    colorSchemePrimary: Color,
    colorSchemeSurfaceTint: Color,
    colorSchemeScrim: Color,
    baseColor: Color,
    backgroundAlpha: Float,
    glassBaseAlpha: Float,
    isLight: Boolean,
    isTintedSurface: Boolean,
    enabled: Boolean,
    isLiquidGlass: Boolean,
): GlassControlDecorationColors {
    val brandAccent = if (isTintedSurface) {
        baseColor.blend(colorSchemeSurfaceTint, if (isLight) 0.10f else 0.08f)
    } else {
        baseColor
            .blend(colorSchemeSurfaceTint, if (isLight) 0.18f else 0.14f)
            .blend(colorSchemePrimary, if (isLight) 0.12f else 0.16f)
    }
    val surfaceBlend = if (isTintedSurface) {
        if (isLight) 0.06f else 0.03f
    } else {
        if (isLight) 0.14f else 0.10f
    }
    val fillBase = baseColor
        .blend(colorSchemeSurface, surfaceBlend)
        .blend(brandAccent, if (enabled) 0.08f else 0.04f)
    val resolvedFillAlpha = when {
        !enabled -> backgroundAlpha * 0.40f
        isTintedSurface -> backgroundAlpha * if (isLiquidGlass) 0.44f else 0.34f
        else -> backgroundAlpha * if (isLiquidGlass) 0.52f else 0.40f
    }
    val tintAlpha = (when {
        !enabled -> 0f
        isTintedSurface && isLight -> (style.tintAlpha * 1.94f).coerceAtMost(0.56f)
        isTintedSurface -> (style.tintAlpha * 2.08f).coerceAtMost(0.60f)
        else -> (style.tintAlpha * 1.34f).coerceAtMost(0.28f)
    } * glassBaseAlpha).coerceIn(0f, 1f)
    val borderColor = if (isTintedSurface) {
        baseColor.blend(colorSchemeOutline, if (isLight) 0.16f else 0.12f)
    } else {
        brandAccent.blend(colorSchemeOutline, if (isLight) 0.20f else 0.14f)
    }.copy(
        alpha = (when {
            !enabled -> 0.18f
            isTintedSurface && isLight -> (style.borderAlpha * 1.60f).coerceAtMost(0.94f)
            isTintedSurface -> (style.borderAlpha * 1.48f).coerceAtMost(0.90f)
            isLight -> (style.borderAlpha * 1.52f).coerceAtMost(0.78f)
            else -> (style.borderAlpha * 1.40f).coerceAtMost(0.80f)
        } * glassBaseAlpha).coerceIn(0f, 1f)
    )
    val innerBorderColor = borderColor.blend(AppContext.getColorScheme().surfaceContainerLowest, if (isLight) 0.10f else 0.08f).copy(
        alpha = (when {
            !enabled -> 0.03f
            isLiquidGlass -> {
                (style.highlightAlpha * if (isLight) 0.92f else 0.78f).coerceAtMost(0.18f)
            }

            else -> {
                (style.highlightAlpha * if (isLight) 0.38f else 0.30f).coerceAtMost(0.06f)
            }
        } * glassBaseAlpha).coerceIn(0f, 1f)
    )
    val topEdgeColor = (if (isTintedSurface) {
        borderColor.blend(Color.White, if (isLight) 0.18f else 0.12f)
    } else {
        borderColor.blend(Color.White, if (isLight) 0.22f else 0.16f)
    }).copy(
        alpha = (if (enabled) {
            (style.highlightAlpha * if (isLiquidGlass) {
                if (isLight) 1.56f else 1.34f
            } else {
                if (isLight) 1.02f else 0.90f
            }).coerceAtMost(if (isLiquidGlass) 0.28f else 0.16f)
        } else {
            0.05f
        } * glassBaseAlpha).coerceIn(0f, 1f)
    )
    val bottomShadeColor = colorSchemeScrim.copy(
        alpha = (if (enabled) {
            (style.innerShadowAlpha * if (isLiquidGlass) {
                if (isLight) 0.66f else 0.72f
            } else {
                if (isLight) 0.14f else 0.18f
            }).coerceAtMost(if (isLiquidGlass) 0.034f else 0.010f)
        } else {
            0.03f
        } * glassBaseAlpha).coerceIn(0f, 1f)
    )
    val innerHighlightColor = (if (isTintedSurface) {
        borderColor.blend(AppContext.getColorScheme().surfaceContainerLowest, if (isLight) 0.14f else 0.10f)
    } else {
        borderColor.blend(AppContext.getColorScheme().surfaceContainerLowest, if (isLight) 0.18f else 0.12f)
    }).copy(
        alpha = (if (enabled) {
            (style.highlightAlpha * if (isLight) 1.08f else 0.92f).coerceAtMost(0.16f)
        } else {
            0.03f
        } * glassBaseAlpha).coerceIn(0f, 1f)
    )

    return GlassControlDecorationColors(
        fillColor = fillBase.copy(alpha = resolvedFillAlpha),
        tintColor = brandAccent.copy(alpha = tintAlpha),
        borderColor = borderColor,
        innerBorderColor = innerBorderColor,
        topEdgeColor = topEdgeColor,
        bottomShadeColor = bottomShadeColor,
        innerHighlightColor = innerHighlightColor,
    )
}

@Composable
private fun Modifier.ultraFlatGlassDecoration(
    style: GlassStyle,
    shape: Shape,
    borderWidth: Dp,
    blurRadius: Dp,
    colors: GlassDecorationColors,
    isLight: Boolean,
    isLiquidGlass: Boolean,
    useLiquidBlur: Boolean,
    showTopEdgeEffects: Boolean,
    showBottomEdgeEffects: Boolean,
): Modifier {
    val graphicsLayer = if (useLiquidBlur) rememberGraphicsLayer() else null
    val blurPx = with(LocalDensity.current) {
        minOf(
            blurRadius.toPx(),
            if (style == GlassStyle.Dense) 8.dp.toPx() else 6.dp.toPx(),
        )
    }
    val blurEffect = remember(blurPx, useLiquidBlur) {
        if (useLiquidBlur) {
            BlurEffect(blurPx, blurPx, TileMode.Clamp)
        } else {
            null
        }
    }

    // Liquid Glass:顶部高光经 graphicsLayer + LayerOutsets 溢出组件上边界,
    // 模拟真实玻璃边缘折光的外溢;非 liquid 模式零开销(不加离屏层)
    val showOverflowGlow = isLiquidGlass &&
        colors.rimLightColor.alpha >= MIN_VISIBLE_GLASS_DECORATION_ALPHA

    return then(
        if (showOverflowGlow) {
            Modifier.liquidTopGlowOverflow(
                glowColor = colors.rimLightColor,
                isLight = isLight,
            )
        } else {
            Modifier
        }
    ).clip(shape).drawWithCache {
        val outline: Outline = shape.createOutline(
            size = size,
            layoutDirection = layoutDirection,
            density = this,
        )
        val strokeWidthPx = if (borderWidth > 0.dp) {
            borderWidth.toPx().coerceAtLeast(if (isLiquidGlass) 1.90f else 1.74f)
        } else {
            0f
        }
        val mainStroke = Stroke(width = strokeWidthPx)
        val topStroke = Stroke(width = (strokeWidthPx * if (isLiquidGlass) 0.96f else 0.90f).coerceAtLeast(if (isLiquidGlass) 1.24f else 1.04f))
        val innerStroke = Stroke(width = (strokeWidthPx * if (isLiquidGlass) 0.74f else 0.64f).coerceAtLeast(if (isLiquidGlass) 1.02f else 0.84f))
        val shouldDrawCaustic = colors.causticColor.alpha >= MIN_VISIBLE_GLASS_DECORATION_ALPHA
        val shouldDrawChromatic = colors.chromaticEdgeColor.alpha >= MIN_VISIBLE_GLASS_DECORATION_ALPHA
        val shouldDrawEdgeTransition = colors.edgeTransitionColor.alpha >= MIN_VISIBLE_GLASS_DECORATION_ALPHA
        val shouldDrawLiquidLens = isLiquidGlass && colors.rimLightColor.alpha >= MIN_VISIBLE_GLASS_DECORATION_ALPHA
        val chromaticStroke = if (strokeWidthPx > 0f && shouldDrawChromatic) {
            Stroke(width = (strokeWidthPx * if (isLiquidGlass) 2.8f else 1.9f).coerceAtLeast(1f))
        } else {
            null
        }
        val sheenBrush = Brush.verticalGradient(
            0.0f to colors.sheenColor,
            (if (isLiquidGlass) 0.11f else 0.06f) to colors.sheenColor.copy(alpha = colors.sheenColor.alpha * if (isLiquidGlass) 0.82f else 0.68f),
            (if (isLiquidGlass) 0.20f else 0.10f) to colors.sheenColor.copy(alpha = colors.sheenColor.alpha * if (isLiquidGlass) 0.26f else 0.16f),
            (if (isLiquidGlass) 0.30f else 0.14f) to Color.Transparent,
        )
        val depthBrush = Brush.verticalGradient(
            0.0f to Color.Transparent,
            (if (isLiquidGlass) 0.84f else 0.92f) to Color.Transparent,
            1.0f to colors.depthColor,
        )
        val causticRadius = max(size.width, size.height).coerceAtLeast(1f) * if (isLiquidGlass) 0.72f else 0.54f
        val primaryCausticBrush = if (shouldDrawCaustic) {
            Brush.radialGradient(
                0.0f to colors.causticColor,
                0.34f to colors.causticColor.copy(alpha = colors.causticColor.alpha * if (isLiquidGlass) {
                    if (isLight) 0.56f else 0.30f
                } else {
                    0.32f
                }),
                1.0f to Color.Transparent,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.16f, size.height * 0.06f),
                radius = causticRadius,
            )
        } else {
            null
        }
        val secondaryCausticBrush = if (shouldDrawChromatic) {
            Brush.radialGradient(
                0.0f to colors.chromaticEdgeColor.copy(alpha = colors.chromaticEdgeColor.alpha * if (isLiquidGlass) 0.62f else 0.30f),
                1.0f to Color.Transparent,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.88f, size.height * 0.92f),
                radius = causticRadius * 0.78f,
            )
        } else {
            null
        }
        val chromaticRimBrush = if (shouldDrawChromatic) {
            Brush.linearGradient(
                0.0f to colors.chromaticEdgeColor.copy(alpha = colors.chromaticEdgeColor.alpha * 0.26f),
                0.22f to colors.rimLightColor,
                0.62f to colors.chromaticEdgeColor.copy(alpha = colors.chromaticEdgeColor.alpha * if (isLiquidGlass) 0.88f else 0.46f),
                1.0f to Color.Transparent,
                start = androidx.compose.ui.geometry.Offset.Zero,
                end = androidx.compose.ui.geometry.Offset(size.width, size.height),
            )
        } else {
            null
        }
        val liquidLensBrush = if (shouldDrawLiquidLens) {
            Brush.radialGradient(
                0.0f to Color.White.copy(alpha = colors.rimLightColor.alpha * if (isLight) 0.82f else 0.40f),
                0.42f to colors.rimLightColor.copy(alpha = colors.rimLightColor.alpha * if (isLight) 0.44f else 0.18f),
                1.0f to Color.Transparent,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.34f, size.height * 0.24f),
                radius = max(size.minDimension, 1f) * 0.72f,
            )
        } else {
            null
        }
        val edgeTransitionBrush = if (shouldDrawEdgeTransition) {
            Brush.radialGradient(
                0.0f to Color.Transparent,
                (if (isLiquidGlass) 0.56f else 0.60f) to Color.Transparent,
                (if (isLiquidGlass) 0.84f else 0.86f) to colors.edgeTransitionColor.copy(
                    alpha = colors.edgeTransitionColor.alpha * if (isLiquidGlass) 0.54f else 0.50f,
                ),
                1.0f to colors.edgeTransitionColor,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.50f, size.height * 0.50f),
                radius = max(size.width, size.height).coerceAtLeast(1f) * if (isLiquidGlass) 0.78f else 0.72f,
            )
        } else {
            null
        }
        val topBorderBrush = Brush.verticalGradient(
            0.0f to colors.topBorderColor.copy(
                alpha = if (showTopEdgeEffects) colors.topBorderColor.alpha else colors.topBorderColor.alpha * 0.20f,
            ),
            (if (isLiquidGlass) 0.08f else 0.045f) to colors.topBorderColor.copy(alpha = colors.topBorderColor.alpha * if (isLiquidGlass) 0.94f else 0.78f),
            (if (isLiquidGlass) 0.20f else 0.095f) to colors.topBorderColor.copy(alpha = colors.topBorderColor.alpha * if (isLiquidGlass) 0.32f else 0.18f),
            (if (isLiquidGlass) 0.30f else 0.13f) to Color.Transparent,
            1.0f to Color.Transparent,
        )

        val drawBackdrop: DrawScope.() -> Unit = {
            drawOutline(outline = outline, color = colors.fillColor)
            if (colors.tintColor.alpha > 0f) {
                drawOutline(outline = outline, color = colors.tintColor)
            }
            edgeTransitionBrush?.let { brush ->
                drawOutline(outline = outline, brush = brush)
            }
            primaryCausticBrush?.let { brush ->
                drawOutline(outline = outline, brush = brush)
            }
            secondaryCausticBrush?.let { brush ->
                drawOutline(outline = outline, brush = brush)
            }
            if (showTopEdgeEffects && colors.sheenColor.alpha > 0f) {
                drawOutline(outline = outline, brush = sheenBrush)
            }
            liquidLensBrush?.let { brush ->
                drawOutline(outline = outline, brush = brush)
            }
        }

        onDrawWithContent {
            if (blurEffect != null && graphicsLayer != null) {
                graphicsLayer.record(
                    size = IntSize(
                        width = size.width.roundToInt().coerceAtLeast(1),
                        height = size.height.roundToInt().coerceAtLeast(1),
                    ),
                    block = drawBackdrop,
                )
                graphicsLayer.renderEffect = blurEffect
                drawLayer(graphicsLayer)
            } else {
                drawBackdrop()
            }

            if (showBottomEdgeEffects && colors.depthColor.alpha > 0f) {
                drawOutline(outline = outline, brush = depthBrush)
            }

            drawContent()

            if (strokeWidthPx > 0f) {
                if (chromaticRimBrush != null && chromaticStroke != null) {
                    drawOutline(
                        outline = outline,
                        brush = chromaticRimBrush,
                        style = chromaticStroke,
                    )
                }
                drawOutline(outline = outline, color = colors.borderColor, style = mainStroke)
                if (colors.innerBorderColor.alpha > 0f) {
                    drawOutline(outline = outline, color = colors.innerBorderColor, style = innerStroke)
                }
                drawOutline(outline = outline, brush = topBorderBrush, style = topStroke)
            }
        }
    }
}

/**
 * Liquid Glass 顶部溢出高光:`graphicsLayer` + [LayerOutsets] 把离屏层绘制边界
 * 向上/左右各扩出一截,辉光从组件上缘溢出,模拟玻璃边缘折光。
 * 注意会给元素引入一个离屏层,仅 liquid 模式启用。
 */
private fun Modifier.liquidTopGlowOverflow(
    glowColor: Color,
    isLight: Boolean,
): Modifier = this
    .graphicsLayer {
        outsets = LayerOutsets(8.dp, 16.dp)
    }
    .drawWithCache {
        val horizontalInset = 8.dp.toPx()
        val verticalOutset = 16.dp.toPx()
        val glowAlphaScale = if (isLight) 1.2f else 0.9f
        val glowBrush = Brush.radialGradient(
            0.0f to glowColor.copy(alpha = (glowColor.alpha * glowAlphaScale).coerceAtMost(1f)),
            0.55f to glowColor.copy(alpha = glowColor.alpha * 0.5f),
            1.0f to Color.Transparent,
            center = androidx.compose.ui.geometry.Offset(size.width / 2f, -verticalOutset * 0.35f),
            radius = max(size.width * 0.72f, 1f),
        )
        onDrawBehind {
            drawRect(
                brush = glowBrush,
                topLeft = androidx.compose.ui.geometry.Offset(-horizontalInset, -verticalOutset),
                size = androidx.compose.ui.geometry.Size(
                    size.width + horizontalInset * 2,
                    size.height * 0.45f + verticalOutset,
                ),
            )
        }
    }

@Composable
private fun Modifier.controlSurfaceDecoration(
    shape: Shape,
    borderWidth: Dp,
    colors: GlassControlDecorationColors,
    isLiquidGlass: Boolean,
    showTopEdge: Boolean,
    showInnerHighlight: Boolean,
): Modifier = clip(shape).drawWithCache {
    val outline: Outline = shape.createOutline(
        size = size,
        layoutDirection = layoutDirection,
        density = this,
    )
    val strokeWidthPx = if (borderWidth > 0.dp) {
        borderWidth.toPx().coerceAtLeast(if (isLiquidGlass) 1.78f else 1.60f)
    } else {
        0f
    }
    val mainStroke = Stroke(width = strokeWidthPx)
    val topStroke = Stroke(width = (strokeWidthPx * if (isLiquidGlass) 0.96f else 0.88f).coerceAtLeast(if (isLiquidGlass) 1.14f else 0.96f))
    val middleStroke = Stroke(width = (strokeWidthPx * if (isLiquidGlass) 0.74f else 0.62f).coerceAtLeast(if (isLiquidGlass) 1.0f else 0.80f))
    val innerStroke = Stroke(width = (strokeWidthPx * if (isLiquidGlass) 0.72f else 0.60f).coerceAtLeast(if (isLiquidGlass) 0.96f else 0.76f))
    val topEdgeBrush = Brush.verticalGradient(
        0.0f to colors.topEdgeColor,
        (if (isLiquidGlass) 0.10f else 0.045f) to colors.topEdgeColor.copy(alpha = colors.topEdgeColor.alpha * if (isLiquidGlass) 0.88f else 0.74f),
        (if (isLiquidGlass) 0.20f else 0.095f) to colors.topEdgeColor.copy(alpha = colors.topEdgeColor.alpha * if (isLiquidGlass) 0.28f else 0.16f),
        (if (isLiquidGlass) 0.28f else 0.13f) to Color.Transparent,
    )
    val bottomShadeBrush = Brush.verticalGradient(
        0.0f to Color.Transparent,
        (if (isLiquidGlass) 0.86f else 0.93f) to Color.Transparent,
        1.0f to colors.bottomShadeColor,
    )
    val innerHighlightBrush = Brush.verticalGradient(
        0.0f to colors.innerHighlightColor,
        (if (isLiquidGlass) 0.08f else 0.05f) to colors.innerHighlightColor.copy(alpha = colors.innerHighlightColor.alpha * if (isLiquidGlass) 0.82f else 0.68f),
        (if (isLiquidGlass) 0.14f else 0.09f) to colors.innerHighlightColor.copy(alpha = colors.innerHighlightColor.alpha * if (isLiquidGlass) 0.24f else 0.14f),
        (if (isLiquidGlass) 0.20f else 0.12f) to Color.Transparent,
        1.0f to Color.Transparent,
    )

    onDrawWithContent {
        drawOutline(outline = outline, color = colors.fillColor)
        if (colors.tintColor.alpha > 0f) {
            drawOutline(outline = outline, color = colors.tintColor)
        }
        if (colors.bottomShadeColor.alpha > 0f) {
            drawOutline(outline = outline, brush = bottomShadeBrush)
        }
        drawContent()
        if (strokeWidthPx > 0f) {
            drawOutline(outline = outline, color = colors.borderColor, style = mainStroke)
            if (colors.innerBorderColor.alpha > 0f) {
                drawOutline(outline = outline, color = colors.innerBorderColor, style = middleStroke)
            }
            if (showTopEdge) {
                drawOutline(outline = outline, brush = topEdgeBrush, style = topStroke)
            }
            if (showInnerHighlight) {
                drawOutline(outline = outline, brush = innerHighlightBrush, style = innerStroke)
            }
        }
    }
}

@Composable
private fun Modifier.fallbackGlassBackground(
    shape: Shape,
    color: Color,
    style: GlassStyle,
): Modifier {
    val glassBaseAlpha = LocalSettingsState.current.glassBaseAlpha
    val fallbackColor = when {
        style == GlassStyle.Transparent -> Color.Transparent
        color != Color.Unspecified -> color
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }.withGlassBaseAlpha(glassBaseAlpha)
    return clip(shape).background(fallbackColor, shape)
}

@Composable
fun Modifier.glassThin(
    shape: Shape = CircleShape,
    color: Color = Color.Unspecified,
    borderWidth: Dp = 0.9.dp,
    blurRadius: Dp = 16.dp,
): Modifier = glassBackground(
    style = GlassStyle.Thin,
    shape = shape,
    color = color,
    borderWidth = borderWidth,
    blurRadius = blurRadius,
)

@Composable
fun Modifier.glassRegular(
    shape: Shape = RoundedCornerShape(14.dp),
    color: Color = Color.Unspecified,
    borderWidth: Dp = 0.9.dp,
    blurRadius: Dp = 24.dp,
): Modifier = glassBackground(
    style = GlassStyle.Regular,
    shape = shape,
    color = color,
    borderWidth = borderWidth,
    blurRadius = blurRadius,
)

@Composable
fun Modifier.glassMedium(
    shape: Shape = RoundedCornerShape(16.dp),
    color: Color = Color.Unspecified,
    borderWidth: Dp = 0.95.dp,
    blurRadius: Dp = 28.dp,
): Modifier = glassBackground(
    style = GlassStyle.Medium,
    shape = shape,
    color = color,
    borderWidth = borderWidth,
    blurRadius = blurRadius,
)

@Composable
fun Modifier.glassThick(
    shape: Shape = RoundedCornerShape(16.dp),
    color: Color = Color.Unspecified,
    borderWidth: Dp = 0.9.dp,
    blurRadius: Dp = 32.dp,
): Modifier = glassBackground(
    style = GlassStyle.Thick,
    shape = shape,
    color = color,
    borderWidth = borderWidth,
    blurRadius = blurRadius,
)

@Composable
fun Modifier.glassDense(
    shape: Shape = RoundedCornerShape(16.dp),
    color: Color = Color.Unspecified,
    borderWidth: Dp = 1.dp,
    blurRadius: Dp = 32.dp,
): Modifier = glassBackground(
    style = GlassStyle.Dense,
    shape = shape,
    color = color,
    borderWidth = borderWidth,
    blurRadius = blurRadius,
)

@Composable
fun Modifier.glassTextField(
    style: GlassStyle = GlassStyle.Thin,
    shape: Shape = RoundedCornerShape(50),
    color: Color = Color.Unspecified,
    borderWidth: Dp = 0.9.dp,
): Modifier {
    val settingsState = LocalSettingsState.current
    if (!settingsState.isGlassAlphaEnabled) {
        return fallbackGlassBackground(
            shape = shape,
            color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.surfaceContainerHighest,
            style = style,
        )
    }

    return glassSimpleStyle(
        style = style,
        shape = shape,
        color = color,
        borderWidth = borderWidth,
    )
}

@Composable
fun Modifier.glassCardSegment(
    segment: GlassCardSegment,
    style: GlassStyle = GlassStyle.Thick,
    shape: Shape? = null,
    color: Color = Color.Unspecified,
    borderWidth: Dp = 0.9.dp,
    blurRadius: Dp = 24.dp,
): Modifier {
    val settingsState = LocalSettingsState.current
    val resolvedBorderWidth = when (segment) {
        GlassCardSegment.Solo -> borderWidth
        else -> 0.dp
    }
    val resolvedShape = shape ?: segment.toShape()
    val showTopEdgeEffects = segment == GlassCardSegment.Top || segment == GlassCardSegment.Solo
    val showBottomEdgeEffects =
        segment == GlassCardSegment.Bottom || segment == GlassCardSegment.Solo

    if (!settingsState.isGlassAlphaEnabled) {
        return fallbackGlassBackground(
            shape = resolvedShape,
            color = color,
            style = style,
        )
    }

    return glassSimpleStyle(
        style = style,
        shape = resolvedShape,
        color = color,
        borderWidth = resolvedBorderWidth,
        blurRadius = blurRadius,
        showTopEdgeEffects = showTopEdgeEffects,
        showBottomEdgeEffects = showBottomEdgeEffects,
    )
}
