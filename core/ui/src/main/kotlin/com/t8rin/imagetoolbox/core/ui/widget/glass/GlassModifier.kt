package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.model.theme.ThemeDefaults
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.theme.blend


/**
 * 玻璃描边的**实际**可见度(装饰性描边用, 输入框走 [textFieldStrokeVisibility])。
 *
 * 描边存在的意义是"在花哨背景上把卡片边缘拉回来": 页面铺了渐变背景 / 自定义背景图时,
 * 卡片填充会和背景里的色块糊在一起, 需要一圈线定边界; 而页面只是纯色底时, 卡片靠填充色
 * 的明度差就已经分得清(实测中性卡片比页面暗 10.5/255, 彩色卡片 9.5), 再描一圈线只是
 * 把卡片"框起来", 显脏。
 *
 * 所以描边跟随背景层开关: 没有背景层就不画描边(此时滑杆值被忽略)。
 */
@Composable
internal fun effectiveGlassBorderAlpha(): Float {
    val settingsState = LocalSettingsState.current
    val hasBackdrop = settingsState.isMeshGradientBackgroundEnabled ||
        settingsState.customBackgroundImageUri != null
    if (!hasBackdrop) return 0f
    return settingsState.glassBorderAlpha.takeIf { it.isFinite() }
        ?.coerceIn(0f, 1f) ?: ThemeDefaults.DEFAULT_GLASS_BORDER_ALPHA
}


internal fun Color.withGlassBaseAlpha(glassBaseAlpha: Float): Color {
    if (this == Color.Unspecified || this == Color.Transparent) return this
    return copy(alpha = (alpha * glassBaseAlpha).coerceIn(0f, 1f))
}

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
    if (blurRadius < 0.dp) return this
    val isLiquidGlass = liquidOverride ?: LocalSettingsState.current.isLiquidGlassEnabled
    return glassMaterial(
        style = style,
        shape = shape,
        color = color,
        backgroundAlpha = backgroundAlpha,
        borderWidth = borderWidth,
        isLiquid = isLiquidGlass,
        showTopEdge = showTopEdgeEffects,
        showBottomEdge = showBottomEdgeEffects,
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
    return glassMaterial(
        style = style,
        shape = shape,
        color = color,
        backgroundAlpha = backgroundAlpha,
        borderWidth = if (showInnerHighlight) borderWidth.coerceAtLeast(1.1.dp) else borderWidth,
        isLiquid = LocalSettingsState.current.isLiquidGlassEnabled,
        isControl = true,
        enabled = enabled,
        showTopEdge = showTopEdge,
        showBottomEdge = true,
    )
}

/**
 * 玻璃关闭时的平涂底面。只用原来的容器色覆盖率，不跟开着时的白霜走。
 */
@Composable
internal fun Modifier.glassFlatSurface(
    shape: Shape,
    color: Color = Color.Unspecified,
    style: GlassStyle = GlassStyle.Regular,
    backgroundAlpha: Float = style.backgroundAlpha,
): Modifier {
    if (style == GlassStyle.Transparent) {
        return clip(shape).background(Color.Transparent, shape)
    }
    return clip(shape).background(
        color = flatGlassContainerColor(
            color = color,
            style = style,
            backgroundAlpha = backgroundAlpha,
        ),
        shape = shape,
    )
}

/**
 * 平涂底色的**颜色**版本 —— 给那些把颜色交给 M3 组件(Button/Chip/IconButton 的
 * `containerColor`)的降级分支复用,语义与 [glassFlatSurface] 完全一致:
 * 容器色按 GlassStyle 覆盖率压成一个颜色,不含模糊、高光与描边。
 * 不复用开玻璃时的白霜,否则关掉玻璃卡片也会被洗白。
 */
@Composable
internal fun flatGlassContainerColor(
    color: Color = Color.Unspecified,
    style: GlassStyle = GlassStyle.Regular,
    backgroundAlpha: Float = style.backgroundAlpha,
): Color {
    // 透明 = 不画底(调用方用 Color.Transparent 表示"这个控件不要背景")。
    // 必须原样放行:blendARGB 会把透明当成黑色参与混色,洗出一块灰底。
    if (color == Color.Transparent) return Color.Transparent
    if (style == GlassStyle.Transparent) return Color.Transparent
    val settingsState = LocalSettingsState.current
    val colorScheme = MaterialTheme.colorScheme
    val glassBaseAlpha = settingsState.glassBaseAlpha.coerceIn(0f, 1f)
    // None = 明确不用玻璃、要一层实底(如分段按钮行的底板),不参与玻璃的覆盖率折算;
    // 未传色时落到默认中性底,避免把 Color.Unspecified 直接交给 background
    if (style == GlassStyle.None) {
        val solidBase = if (color != Color.Unspecified) color else colorScheme.surfaceContainerLow
        return solidBase.withGlassBaseAlpha(glassBaseAlpha)
    }
    val baseColor = if (color != Color.Unspecified) color else colorScheme.surfaceContainerLow
    val scaledBackgroundAlpha = (backgroundAlpha * glassBaseAlpha).coerceIn(0f, 1f)
    val isLight = colorScheme.surface.luminance() > 0.5f
    val isTintedSurface = color != Color.Unspecified
    val surface = colorScheme.surface
    val surfaceTint = colorScheme.surfaceTint
    val primaryContainer = colorScheme.primaryContainer
    val scrim = colorScheme.scrim
    return remember(
        baseColor,
        style,
        scaledBackgroundAlpha,
        glassBaseAlpha,
        isLight,
        isTintedSurface,
        surface,
        surfaceTint,
        primaryContainer,
        scrim,
    ) {
        flatContainerCoverage(
            style = style,
            baseColor = baseColor,
            surface = surface,
            surfaceTint = surfaceTint,
            primaryContainer = primaryContainer,
            scrim = scrim,
            backgroundAlpha = scaledBackgroundAlpha,
            glassBaseAlpha = glassBaseAlpha,
            isLight = isLight,
            isTintedSurface = isTintedSurface,
        )
    }
}

private const val NEUTRAL_GLASS_DEEPEN = 0.045f
private const val NEUTRAL_GLASS_TINT_BOOST = 1.30f
private const val NEUTRAL_GLASS_MAX_CHANNEL_SPREAD = 12f

private fun Color.isNearNeutralGlass(): Boolean {
    val spread = (maxOf(red, green, blue) - minOf(red, green, blue)) * 255f
    return spread <= NEUTRAL_GLASS_MAX_CHANNEL_SPREAD
}

/** 关玻璃时的底色。覆盖率沿用改白霜之前的公式。 */
private fun flatContainerCoverage(
    style: GlassStyle,
    baseColor: Color,
    surface: Color,
    surfaceTint: Color,
    primaryContainer: Color,
    scrim: Color,
    backgroundAlpha: Float,
    glassBaseAlpha: Float,
    isLight: Boolean,
    isTintedSurface: Boolean,
): Color {
    val neutralGlassFill = if (isLight && baseColor.isNearNeutralGlass()) {
        baseColor.blend(scrim, NEUTRAL_GLASS_DEEPEN)
    } else {
        null
    }
    val flattenedBase = if (isTintedSurface) {
        neutralGlassFill ?: baseColor.blend(surface, if (isLight) 0.08f else 0.06f)
    } else {
        neutralGlassFill ?: baseColor.blend(surface, if (isLight) 0.26f else 0.20f)
    }
    val accent = if (isTintedSurface) {
        neutralGlassFill?.blend(surfaceTint, 0.04f)
            ?: baseColor.blend(surfaceTint, if (isLight) 0.08f else 0.06f)
    } else {
        neutralGlassFill?.blend(surfaceTint, 0.06f)
            ?: baseColor
                .blend(surfaceTint, if (isLight) 0.14f else 0.12f)
                .blend(primaryContainer, if (isLight) 0.06f else 0.08f)
    }
    val fillColor = flattenedBase.copy(
        alpha = if (isTintedSurface) {
            (backgroundAlpha * 0.30f).coerceAtMost(1f)
        } else {
            (backgroundAlpha * 0.38f).coerceAtMost(1f)
        }
    )
    val tintAlpha = (when {
        isTintedSurface && isLight -> (style.tintAlpha * 1.42f).coerceAtMost(0.42f)
        isTintedSurface -> (style.tintAlpha * 1.54f).coerceAtMost(0.46f)
        isLight -> (style.tintAlpha * 0.82f).coerceAtMost(0.16f)
        else -> (style.tintAlpha * 0.96f).coerceAtMost(0.20f)
    } * glassBaseAlpha * if (neutralGlassFill != null) NEUTRAL_GLASS_TINT_BOOST else 1f).coerceIn(0f, 1f)
    return accent.copy(alpha = tintAlpha).compositeOver(fillColor)
}

@Composable
private fun Modifier.fallbackGlassBackground(
    shape: Shape,
    color: Color,
    style: GlassStyle,
): Modifier {
    return glassFlatSurface(
        shape = shape,
        color = color,
        style = style,
    )
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

/**
 * Uniform translucent fill for lazy-list card segments, with highlights only on the outer contour.
 * Uses the glass palette and opacity settings without per-item reflections or depth gradients.
 */
@Composable
fun Modifier.flatGlassCardSegment(
    segment: GlassCardSegment,
    shape: Shape = segment.toShape(),
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    style: GlassStyle = GlassStyle.Thick,
    borderWidth: Dp = 0.7.dp,
): Modifier {
    val hasTop = segment == GlassCardSegment.Top || segment == GlassCardSegment.Solo
    val hasBottom = segment == GlassCardSegment.Bottom || segment == GlassCardSegment.Solo
    if (!LocalSettingsState.current.isGlassAlphaEnabled || style == GlassStyle.None ||
        style == GlassStyle.Transparent || color == Color.Transparent
    ) {
        return fallbackGlassBackground(shape = shape, color = color, style = style)
    }
    return glassMaterial(
        style = style,
        shape = shape,
        color = color,
        backgroundAlpha = style.backgroundAlpha,
        borderWidth = if (hasTop && hasBottom) borderWidth else 0.dp,
        isLiquid = LocalSettingsState.current.isLiquidGlassEnabled,
        showTopEdge = hasTop,
        showBottomEdge = hasBottom,
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
