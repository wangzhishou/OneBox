package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState

/** 浅色表面容器常带一点主题色。色差不到这个值按中性玻璃，再高一点就保留色相。 */
private const val NEUTRAL_CHROMA = 0.08f

private fun Color.chroma(): Float {
    if (this == Color.Unspecified || this == Color.Transparent) return 0f
    return maxOf(red, green, blue) - minOf(red, green, blue)
}

private fun Color.isNearNeutral(): Boolean = chroma() < NEUTRAL_CHROMA

private fun styleWeight(style: GlassStyle): Float = when (style) {
    GlassStyle.Thin -> 0.78f
    GlassStyle.Regular -> 1f
    GlassStyle.Medium -> 1.16f
    GlassStyle.Thick -> 1.34f
    GlassStyle.Dense -> 1.52f
    else -> 1f
}

@Immutable
internal data class GlassVeil(
    val frost: Color,
    val tint: Color,
    val rimWidth: Dp,
    val rim: Color,
)

@Composable
internal fun glassVeil(
    color: Color,
    style: GlassStyle,
    backgroundAlpha: Float,
    isLiquid: Boolean,
    isControl: Boolean,
    enabled: Boolean,
    contour: Boolean,
    borderWidth: Dp,
): GlassVeil {
    val settings = LocalSettingsState.current
    val scheme = MaterialTheme.colorScheme
    val isLight = scheme.surface.luminance() > 0.5f
    val neutral = color.isNearNeutral()
    val glassBaseAlpha = settings.glassBaseAlpha.coerceIn(0f, 1f)
    val hasWallpaper = settings.isMeshGradientBackgroundEnabled ||
        settings.customBackgroundImageUri != null
    val borderSetting = settings.glassBorderAlpha.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 0.2f
    val rimStrength = when {
        !enabled -> 0.4f
        !contour -> 0f
        borderSetting <= 0f && hasWallpaper -> 0f
        hasWallpaper -> borderSetting
        else -> 0.45f
    }.coerceIn(0f, 1f)
    val coverage = (backgroundAlpha / style.backgroundAlpha.coerceAtLeast(0.04f)).coerceIn(0.65f, 1.35f)
    val weight = styleWeight(style) * glassBaseAlpha * coverage * (if (isControl) 1.05f else 1f) *
        (if (enabled) 1f else 0.65f)
    // 霜写死白，暗色写死黑。换主题时色相靠下面的 tint，不靠这层底。
    // 透明度按玻璃厚度、液态、有没有渐变来收放：
    // 中性卡片没有色调可染，渐变页上要更实，不然会发透。
    // 彩色卡片的色相靠 tint，霜只垫薄薄一层。液态开关再透一点。
    // 玻璃开关关闭时走不到这里。
    val liquidScale = if (isLiquid) 0.82f else 1f
    val plain = !hasWallpaper
    val frostStrength = when {
        neutral && hasWallpaper && isLight -> 0.70f
        neutral && hasWallpaper -> 0.64f
        neutral && isLight -> 0.18f
        neutral -> 0.16f
        isLight && isControl && plain -> 0.22f
        isLight && isControl -> 0.10f
        isLight && plain -> 0.28f
        isLight -> 0.14f
        isControl && plain -> 0.16f
        isControl -> 0.10f
        plain -> 0.20f
        else -> 0.16f
    }
    val frostAlpha = (frostStrength * weight * liquidScale).coerceIn(
        0f,
        when {
            neutral && hasWallpaper && isLight -> 0.86f
            neutral && hasWallpaper -> 0.80f
            isLight -> 0.46f
            else -> 0.36f
        },
    )
    val tintAlpha = (
        if (neutral || color == Color.Unspecified || color == Color.Transparent) {
            0f
        } else if (isLight && plain) {
            (if (isControl) 0.42f else 0.50f) * weight * liquidScale
        } else if (isLight) {
            (if (isControl) 0.36f else 0.46f) * weight * liquidScale
        } else if (plain) {
            (if (isControl) 0.32f else 0.36f) * weight * liquidScale
        } else {
            (if (isControl) 0.28f else 0.36f) * weight * liquidScale
        }
        ).coerceIn(
            0f,
            when {
                isLight && plain -> 0.58f
                isLight -> 0.56f
                plain -> 0.42f
                else -> 0.42f
            },
        )
    // 四边同一条线，写死白。浅色清楚，暗色只留很淡的一条，避免发亮。
    val rim = Color.White.copy(alpha = if (isLight) 0.42f else 0.10f)
    val frost = (if (isLight) Color.White else Color.Black).copy(alpha = frostAlpha)

    return GlassVeil(
        frost = frost,
        tint = if (tintAlpha > 0f) color.copy(alpha = tintAlpha) else Color.Transparent,
        rimWidth = if (rimStrength <= 0f) 0.dp else borderWidth.coerceAtLeast(1.dp).coerceAtMost(1.25.dp),
        rim = rim,
    )
}

@Composable
internal fun Modifier.glassMaterial(
    style: GlassStyle,
    shape: Shape,
    color: Color,
    backgroundAlpha: Float,
    borderWidth: Dp,
    isLiquid: Boolean,
    isControl: Boolean = false,
    enabled: Boolean = true,
    showTopEdge: Boolean = true,
    showBottomEdge: Boolean = true,
): Modifier {
    if (style == GlassStyle.Transparent || color == Color.Transparent) {
        return clip(shape)
    }
    if (style == GlassStyle.None) {
        val solid = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.surfaceContainer
        return clip(shape).background(solid, shape)
    }
    // 关玻璃：只铺平涂底，不画高光边。分段选中条、滑杆、按钮都从这里走。
    if (!LocalSettingsState.current.isGlassAlphaEnabled) {
        return glassFlatSurface(
            shape = shape,
            color = color,
            style = style,
            backgroundAlpha = backgroundAlpha,
        )
    }
    val contour = showTopEdge && showBottomEdge
    val veil = glassVeil(
        color = color,
        style = style,
        backgroundAlpha = backgroundAlpha,
        isLiquid = isLiquid,
        isControl = isControl,
        enabled = enabled,
        contour = contour,
        borderWidth = borderWidth,
    )
    // 不按卡片各自采样、模糊背景。那条路径每张卡片一块离屏缓冲，列表一滑就卡。
    // 半透明霜直接叠在已经画好的内容上，后面的壁纸和文字都能透出来。
    return veilGlass(shape, veil)
}

private fun Modifier.veilGlass(
    shape: Shape,
    veil: GlassVeil,
): Modifier = clip(shape).drawWithCache {
    val outline = shape.createOutline(size, layoutDirection, this)
    onDrawWithContent {
        if (veil.frost.alpha > 0f) drawOutline(outline, veil.frost)
        if (veil.tint.alpha > 0f) drawOutline(outline, veil.tint)
        drawContent()
        drawGlassRim(shape, veil)
    }
}

private fun DrawScope.drawGlassRim(shape: Shape, veil: GlassVeil) {
    if (veil.rimWidth <= 0.dp || veil.rim.alpha <= 0f) return
    // 描边往里收半个线宽，整条线都落在裁切里面，避免被切掉一半后发虚。
    val stroke = veil.rimWidth.toPx().coerceAtLeast(1f)
    val inset = stroke / 2f
    val width = (size.width - stroke).coerceAtLeast(1f)
    val height = (size.height - stroke).coerceAtLeast(1f)
    val outline = shape.createOutline(Size(width, height), layoutDirection, this)
    translate(inset, inset) {
        drawOutline(
            outline = outline,
            color = veil.rim,
            style = Stroke(width = stroke),
        )
    }
}

/**
 * 背景和内容叠在同一层。玻璃是半透明霜，不再把整屏录进离屏缓冲。
 */
@Composable
internal fun GlassBackdropFrame(
    modifier: Modifier = Modifier,
    backdrop: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier) {
        Box(Modifier.matchParentSize(), content = backdrop)
        Box(Modifier.matchParentSize(), content = content)
    }
}
