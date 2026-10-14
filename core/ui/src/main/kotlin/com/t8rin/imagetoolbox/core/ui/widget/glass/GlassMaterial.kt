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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import kotlin.math.abs

private fun Color.chroma(): Float {
    if (this == Color.Unspecified || this == Color.Transparent) return 0f
    return maxOf(red, green, blue) - minOf(red, green, blue)
}

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
    val liquidScale = if (isLiquid) 0.82f else 1f
    val plain = !hasWallpaper
    // 表面角色本身几乎是灰的（色差常在 0.03–0.06）。以前色差不到 0.08 就当成没颜色，
    // 再铺一层最多 86% 的白霜，渐变页上每张卡片都变成同一块白，嵌套层次也没了。
    // 色相跟当前主题的 surfaceTint 走，日夜和换主题都会跟着变。
    // 角色离页面 surface 越远，色相和明度差越大，外层浅、内层深。
    // 只是两次纯色填充，不采样背景，也不做离屏模糊。
    val base = if (color == Color.Unspecified) scheme.surfaceContainer else color
    val hueRoom = ((0.10f - base.chroma()) / 0.10f).coerceIn(0f, 1f)
    val lift = abs(base.luminance() - scheme.surface.luminance())
    val step = if (hueRoom < 0.15f) {
        0f
    } else {
        (lift * if (isLight) 2.2f else 10f).coerceIn(0f, 1f)
    }
    val hueBase = when {
        isControl -> if (hasWallpaper) 0.04f else 0.02f
        isLight && hasWallpaper -> 0.12f
        isLight -> 0.05f
        hasWallpaper -> 0.10f
        else -> 0.04f
    }
    val hueMix = (hueRoom * hueBase * (1f + step * 0.35f)).coerceIn(0f, 0.16f)
    val hued = if (hueMix > 0.004f) lerp(base, scheme.surfaceTint, hueMix) else base
    val separated = when {
        step < 0.008f -> hued
        isLight -> lerp(hued, scheme.scrim, (step * 0.12f).coerceAtMost(0.02f))
        else -> lerp(hued, scheme.primaryContainer, (step * 0.22f).coerceAtMost(0.06f))
    }
    // 白/黑霜只留一层薄奶，不再盖住上面的色相。
    val frostStrength = when {
        isControl && isLight && plain -> 0.22f
        isControl && isLight -> 0.10f
        isControl && plain -> 0.16f
        isControl -> 0.10f
        isLight && hasWallpaper -> if (hueRoom > 0.35f) 0.12f else 0.08f
        isLight && plain -> 0.16f
        hasWallpaper -> 0.14f
        else -> 0.12f
    }
    val frostAlpha = (frostStrength * weight * liquidScale).coerceIn(
        0f,
        when {
            isControl && isLight -> 0.40f
            isLight && hasWallpaper -> 0.18f
            isLight -> 0.32f
            hasWallpaper -> 0.26f
            else -> 0.22f
        },
    )
    val tintStrength = when {
        isControl && hueRoom > 0.35f && hasWallpaper -> 0.16f
        isControl && hueRoom > 0.35f -> 0.08f
        isControl && isLight && plain -> 0.42f
        isControl && isLight -> 0.36f
        isControl && plain -> 0.32f
        isControl -> 0.28f
        isLight && hasWallpaper -> 0.40f
        isLight && plain -> if (hueRoom > 0.35f) 0.14f else 0.40f
        hasWallpaper -> 0.40f
        else -> 0.36f
    }
    val tintAlpha = (tintStrength * weight * liquidScale).coerceIn(
        0f,
        when {
            isControl && isLight && plain -> 0.58f
            isControl && isLight -> 0.56f
            isControl -> 0.42f
            isLight && hasWallpaper -> 0.54f
            isLight -> 0.50f
            hasWallpaper -> 0.54f
            else -> 0.46f
        },
    )
    // 四边同一条线，写死白。浅色清楚，暗色只留很淡的一条，避免发亮。
    val rim = Color.White.copy(alpha = if (isLight) 0.42f else 0.10f)
    val frost = (if (isLight) Color.White else Color.Black).copy(alpha = frostAlpha)

    return GlassVeil(
        frost = frost,
        tint = if (tintAlpha > 0f) separated.copy(alpha = tintAlpha) else Color.Transparent,
        // borderWidth <= 0 表示调用方不要描边。Switch 的关闭/禁用态走这条，避免再被抬成 1dp 边框。
        rimWidth = when {
            borderWidth <= 0.dp || rimStrength <= 0f -> 0.dp
            else -> borderWidth.coerceAtLeast(1.dp).coerceAtMost(1.25.dp)
        },
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
