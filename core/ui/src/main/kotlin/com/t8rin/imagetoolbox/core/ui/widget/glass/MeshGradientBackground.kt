package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.core.graphics.ColorUtils
import coil3.compose.rememberAsyncImagePainter
import com.t8rin.imagetoolbox.core.settings.domain.model.GradientBackgroundStyle
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState

@Composable
fun MeshGradientBackground(
    modifier: Modifier = Modifier,
    primaryAlpha: Float = 0.16f,
    tertiaryAlpha: Float = 0.20f,
    secondaryAlpha: Float = 0.14f,
    accentAlpha: Float = 0.08f,
    content: @Composable BoxScope.() -> Unit = { }
) {
    val settingsState = LocalSettingsState.current
    val surfaceColor = MaterialTheme.colorScheme.surface
    val shouldRenderMeshGradient = settingsState.isMeshGradientBackgroundEnabled

    val backgroundImageUri = settingsState.customBackgroundImageUri

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(surfaceColor)
    ) {
        if (backgroundImageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(model = backgroundImageUri),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            val overlayAlpha = settingsState.customBackgroundOverlayAlpha
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(surfaceColor.copy(alpha = overlayAlpha))
            )
        }

        if (shouldRenderMeshGradient) {
            val style = settingsState.gradientBackgroundStyle
            val isDark = isSystemInDarkTheme()
            val meshGradientAlphaMultiplier = if (isDark) 1f else 0.78f
            val meshGradientVeilMultiplier = if (isDark) 1f else 0.88f

            // 颜色计算全部进 remember — HSL 转换是 JNI 调用，避免每重组执行
            // MaterialTheme.colorScheme 必须在 @Composable 上下文中读取，再传入 remember
            val themePrimary = MaterialTheme.colorScheme.primary
            val themeSecondary = MaterialTheme.colorScheme.secondary
            val themeTertiary = MaterialTheme.colorScheme.tertiary
            val themeSurfaceHighest = MaterialTheme.colorScheme.surfaceContainerHighest
            val themePrimaryContainer = MaterialTheme.colorScheme.primaryContainer
            val themeTertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer

            val classicColors = remember(
                surfaceColor, isDark, meshGradientAlphaMultiplier,
                primaryAlpha, tertiaryAlpha, secondaryAlpha, accentAlpha,
                themePrimary, themeSecondary, themeTertiary, themeSurfaceHighest,
            ) {
                ClassicColors(
                    primary = themePrimary.toMutedContrastingBackdropColor(
                        surfaceColor = surfaceColor,
                        hueShift = 180f,
                        saturationMultiplier = 0.40f,
                        lightnessDelta = if (isDark) 0.16f else 0.08f,
                        surfaceBlend = if (isDark) 0.30f else 0.44f,
                        alpha = primaryAlpha * 0.82f * meshGradientAlphaMultiplier,
                    ),
                    tertiary = themeTertiary.toMutedContrastingBackdropColor(
                        surfaceColor = surfaceColor,
                        hueShift = 180f,
                        saturationMultiplier = 0.38f,
                        lightnessDelta = if (isDark) 0.14f else 0.06f,
                        surfaceBlend = if (isDark) 0.32f else 0.46f,
                        alpha = tertiaryAlpha * 0.80f * meshGradientAlphaMultiplier,
                    ),
                    secondary = themeSecondary.toMutedContrastingBackdropColor(
                        surfaceColor = surfaceColor,
                        hueShift = 180f,
                        saturationMultiplier = 0.36f,
                        lightnessDelta = if (isDark) 0.12f else 0.05f,
                        surfaceBlend = if (isDark) 0.34f else 0.48f,
                        alpha = secondaryAlpha * 0.76f * meshGradientAlphaMultiplier,
                    ),
                    accent = themeSurfaceHighest.toMutedContrastingBackdropColor(
                        surfaceColor = surfaceColor,
                        hueShift = 160f,
                        saturationMultiplier = 0.24f,
                        lightnessDelta = if (isDark) 0.08f else 0.04f,
                        surfaceBlend = if (isDark) 0.42f else 0.54f,
                        alpha = accentAlpha * 0.78f * meshGradientAlphaMultiplier,
                    ),
                )
            }

            val etherealFade = 0.22f
            val etherealColors = remember(
                surfaceColor, isDark, meshGradientAlphaMultiplier,
                primaryAlpha, tertiaryAlpha, secondaryAlpha, accentAlpha,
                themePrimary, themeSecondary, themeTertiary,
                themePrimaryContainer, themeTertiaryContainer,
            ) {
                EtherealColors(
                    primary = themePrimary.toMutedContrastingBackdropColor(
                        surfaceColor = surfaceColor,
                        hueShift = 180f,
                        saturationMultiplier = 0.24f,
                        lightnessDelta = if (isDark) 0.12f else 0.02f,
                        surfaceBlend = if (isDark) 0.48f else 0.62f,
                        alpha = primaryAlpha * etherealFade * meshGradientAlphaMultiplier,
                    ),
                    tertiary = themeTertiary.toMutedContrastingBackdropColor(
                        surfaceColor = surfaceColor,
                        hueShift = 180f,
                        saturationMultiplier = 0.22f,
                        lightnessDelta = if (isDark) 0.10f else 0.02f,
                        surfaceBlend = if (isDark) 0.50f else 0.64f,
                        alpha = tertiaryAlpha * etherealFade * meshGradientAlphaMultiplier,
                    ),
                    secondary = themeSecondary.toMutedContrastingBackdropColor(
                        surfaceColor = surfaceColor,
                        hueShift = 180f,
                        saturationMultiplier = 0.20f,
                        lightnessDelta = if (isDark) 0.08f else 0f,
                        surfaceBlend = if (isDark) 0.52f else 0.66f,
                        alpha = secondaryAlpha * etherealFade * meshGradientAlphaMultiplier,
                    ),
                    container = themePrimaryContainer.toMutedContrastingBackdropColor(
                        surfaceColor = surfaceColor,
                        hueShift = 180f,
                        saturationMultiplier = 0.18f,
                        lightnessDelta = if (isDark) 0.08f else 0.04f,
                        surfaceBlend = if (isDark) 0.56f else 0.70f,
                        alpha = accentAlpha * etherealFade * meshGradientAlphaMultiplier,
                    ),
                    tertiaryContainer = themeTertiaryContainer.toMutedContrastingBackdropColor(
                        surfaceColor = surfaceColor,
                        hueShift = 180f,
                        saturationMultiplier = 0.16f,
                        lightnessDelta = if (isDark) 0.06f else 0.04f,
                        surfaceBlend = if (isDark) 0.58f else 0.72f,
                        alpha = tertiaryAlpha * etherealFade * 0.58f * meshGradientAlphaMultiplier,
                    ),
                )
            }

            // drawWithCache：只在 size 变化时创建 Brush，避免每帧 new Shader
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        val w = size.width
                        val h = size.height

                        val specs = when (style) {
                            GradientBackgroundStyle.Classic -> classicColors.toSpecs(w, h)
                            GradientBackgroundStyle.Ethereal -> etherealColors.toSpecs(w, h)
                            else -> precomputeStyleSpecs(style, isDark, surfaceColor, w, h)
                        }
                        onDrawBehind { drawSpecs(specs) }
                    }
            )

            // 遮罩层也缓存 Brush
            val veilBrush = remember(surfaceColor, isDark, meshGradientVeilMultiplier) {
                Brush.verticalGradient(
                    0.0f to surfaceColor.copy(
                        alpha = (if (isDark) 0.08f else 0.03f) * meshGradientVeilMultiplier
                    ),
                    0.42f to Color.Transparent,
                    1.0f to surfaceColor.copy(
                        alpha = (if (isDark) 0.16f else 0.07f) * meshGradientVeilMultiplier
                    ),
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(veilBrush)
            )
        }

        content()
    }
}

private data class ClassicColors(
    val primary: Color,
    val tertiary: Color,
    val secondary: Color,
    val accent: Color,
)

private data class EtherealColors(
    val primary: Color,
    val tertiary: Color,
    val secondary: Color,
    val container: Color,
    val tertiaryContainer: Color,
)

/** 高性能渐变图元：径向光斑或矩形渐变层。 */
private sealed class GradientSpec {
    abstract val brush: Brush
}

private data class RadialSpec(
    override val brush: Brush,
    val radius: Float,
    val center: Offset,
) : GradientSpec()

private data class RectSpec(
    override val brush: Brush,
    val topLeft: Offset,
    val size: Size,
) : GradientSpec()

private fun ClassicColors.toSpecs(w: Float, h: Float): List<GradientSpec> = listOf(
    RadialSpec(
        brush = Brush.radialGradient(
            colors = listOf(tertiary, Color.Transparent),
            center = Offset(w * 0.1f, h * 0.15f),
            radius = w * 0.7f
        ),
        radius = w * 0.7f,
        center = Offset(w * 0.1f, h * 0.15f),
    ),
    RadialSpec(
        brush = Brush.radialGradient(
            colors = listOf(primary, Color.Transparent),
            center = Offset(w * 0.85f, h * 0.05f),
            radius = w * 0.65f
        ),
        radius = w * 0.65f,
        center = Offset(w * 0.85f, h * 0.05f),
    ),
    RadialSpec(
        brush = Brush.radialGradient(
            colors = listOf(secondary, Color.Transparent),
            center = Offset(w * 0.9f, h * 0.75f),
            radius = w * 0.6f
        ),
        radius = w * 0.6f,
        center = Offset(w * 0.9f, h * 0.75f),
    ),
    RadialSpec(
        brush = Brush.radialGradient(
            colors = listOf(accent, Color.Transparent),
            center = Offset(w * 0.05f, h * 0.9f),
            radius = w * 0.55f
        ),
        radius = w * 0.55f,
        center = Offset(w * 0.05f, h * 0.9f),
    ),
)

private fun EtherealColors.toSpecs(w: Float, h: Float): List<GradientSpec> = listOf(
    RadialSpec(
        brush = Brush.radialGradient(
            colors = listOf(tertiary, Color.Transparent),
            center = Offset(w * 0.15f, h * 0.10f),
            radius = w * 0.85f
        ),
        radius = w * 0.85f,
        center = Offset(w * 0.15f, h * 0.10f),
    ),
    RadialSpec(
        brush = Brush.radialGradient(
            colors = listOf(primary, Color.Transparent),
            center = Offset(w * 0.80f, h * 0.08f),
            radius = w * 0.80f
        ),
        radius = w * 0.80f,
        center = Offset(w * 0.80f, h * 0.08f),
    ),
    RadialSpec(
        brush = Brush.radialGradient(
            colors = listOf(tertiaryContainer, Color.Transparent),
            center = Offset(w * 0.50f, h * 0.45f),
            radius = w * 0.70f
        ),
        radius = w * 0.70f,
        center = Offset(w * 0.50f, h * 0.45f),
    ),
    RadialSpec(
        brush = Brush.radialGradient(
            colors = listOf(secondary, Color.Transparent),
            center = Offset(w * 0.85f, h * 0.70f),
            radius = w * 0.75f
        ),
        radius = w * 0.75f,
        center = Offset(w * 0.85f, h * 0.70f),
    ),
    RadialSpec(
        brush = Brush.radialGradient(
            colors = listOf(container, Color.Transparent),
            center = Offset(w * 0.10f, h * 0.88f),
            radius = w * 0.70f
        ),
        radius = w * 0.70f,
        center = Offset(w * 0.10f, h * 0.88f),
    ),
)

private fun DrawScope.drawSpecs(specs: List<GradientSpec>) {
    for (spec in specs) {
        when (spec) {
            is RadialSpec -> drawCircle(
                brush = spec.brush,
                radius = spec.radius,
                center = spec.center,
            )
            is RectSpec -> drawRect(
                brush = spec.brush,
                topLeft = spec.topLeft,
                size = spec.size,
            )
        }
    }
}

private fun Color.softenIfLight(
    surfaceColor: Color,
    isDark: Boolean,
    amount: Float = 0.12f,
): Color {
    if (isDark || amount <= 0f) return this
    return lerp(this, surfaceColor, amount)
}

private fun precomputeStyleSpecs(
    style: GradientBackgroundStyle,
    isDark: Boolean,
    surfaceColor: Color,
    w: Float,
    h: Float,
): List<GradientSpec> = when (style) {
    GradientBackgroundStyle.Classic,
    GradientBackgroundStyle.Ethereal -> emptyList()

    GradientBackgroundStyle.Aurora -> {
        val base = if (isDark) 0.22f else 0.26f
        val mint = Color(0xFF80CBC4).softenIfLight(surfaceColor, isDark).copy(alpha = base)
        val violet = Color(0xFF7C4DFF).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.95f)
        val teal = Color(0xFF00BFA5).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.85f)
        val pink = Color(0xFFF48FB1).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.75f)
        listOf(
            RectSpec(
                brush = Brush.linearGradient(
                    0.0f to mint.copy(alpha = 0f),
                    0.45f to mint,
                    1.0f to violet.copy(alpha = 0f),
                    start = Offset(0f, 0f),
                    end = Offset(w * 1.3f, h * 0.9f),
                    tileMode = TileMode.Clamp,
                ),
                topLeft = Offset.Zero,
                size = Size(w, h),
            ),
            RectSpec(
                brush = Brush.linearGradient(
                    0.0f to teal.copy(alpha = 0f),
                    0.50f to teal,
                    1.0f to pink.copy(alpha = 0f),
                    start = Offset(w * 0.1f, h * 1.1f),
                    end = Offset(w, 0f),
                    tileMode = TileMode.Clamp,
                ),
                topLeft = Offset.Zero,
                size = Size(w, h),
            ),
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(pink.copy(alpha = base * 0.6f), Color.Transparent),
                    center = Offset(w * 0.85f, h * 0.88f),
                    radius = h * 0.55f,
                ),
                radius = h * 0.55f,
                center = Offset(w * 0.85f, h * 0.88f),
            ),
        )
    }

    GradientBackgroundStyle.Ocean -> {
        val base = if (isDark) 0.24f else 0.28f
        val deep = Color(0xFF0D1B2A).softenIfLight(surfaceColor, isDark).copy(alpha = base * 1.1f)
        val cyan = Color(0xFF00E5FF).softenIfLight(surfaceColor, isDark).copy(alpha = base)
        val indigo = Color(0xFF1A237E).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.75f)
        val teal = Color(0xFF00838F).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.65f)
        listOf(
            RectSpec(
                brush = Brush.verticalGradient(
                    0.0f to deep.copy(alpha = base * 0.9f),
                    0.55f to teal.copy(alpha = base * 0.35f),
                    1.0f to Color.Transparent,
                ),
                topLeft = Offset.Zero,
                size = Size(w, h),
            ),
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(cyan, Color.Transparent),
                    center = Offset(w * 0.85f, h * 0.35f),
                    radius = w * 0.55f,
                ),
                radius = w * 0.55f,
                center = Offset(w * 0.85f, h * 0.35f),
            ),
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(indigo, Color.Transparent),
                    center = Offset(w * 0.25f, h * 0.85f),
                    radius = h * 0.65f,
                ),
                radius = h * 0.65f,
                center = Offset(w * 0.25f, h * 0.85f),
            ),
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(cyan.copy(alpha = base * 1.3f), Color.Transparent),
                    center = Offset(w * 0.55f, h * 0.92f),
                    radius = w * 0.25f,
                ),
                radius = w * 0.25f,
                center = Offset(w * 0.55f, h * 0.92f),
            ),
        )
    }

    GradientBackgroundStyle.Sunset -> {
        val base = if (isDark) 0.24f else 0.30f
        val amber = Color(0xFFFF6F00).softenIfLight(surfaceColor, isDark).copy(alpha = base)
        val rose = Color(0xFFD81B60).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.85f)
        val gold = Color(0xFFFFD54F).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.70f)
        val purple = Color(0xFF4A148C).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.55f)
        listOf(
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(amber, Color.Transparent),
                    center = Offset(w * 0.85f, h * 0.05f),
                    radius = w * 0.85f,
                ),
                radius = w * 0.85f,
                center = Offset(w * 0.85f, h * 0.05f),
            ),
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(rose, Color.Transparent),
                    center = Offset(w * 0.05f, h * 0.35f),
                    radius = w * 0.75f,
                ),
                radius = w * 0.75f,
                center = Offset(w * 0.05f, h * 0.35f),
            ),
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(gold, Color.Transparent),
                    center = Offset(w * 0.60f, h * 0.95f),
                    radius = w * 0.60f,
                ),
                radius = w * 0.60f,
                center = Offset(w * 0.60f, h * 0.95f),
            ),
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(purple, Color.Transparent),
                    center = Offset(w * 0.20f, h * 0.88f),
                    radius = w * 0.55f,
                ),
                radius = w * 0.55f,
                center = Offset(w * 0.20f, h * 0.88f),
            ),
        )
    }

    GradientBackgroundStyle.SakuraMist -> {
        val base = if (isDark) 0.20f else 0.26f
        val pink = Color(0xFFF8BBD0).softenIfLight(surfaceColor, isDark).copy(alpha = base)
        val peach = Color(0xFFFFAB91).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.85f)
        val lavender = Color(0xFFE1BEE7).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.70f)
        val cream = Color(0xFFFFF3E0).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.55f)
        listOf(
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(pink, Color.Transparent),
                    center = Offset(w * 0.15f, h * 0.15f),
                    radius = w * 0.75f,
                ),
                radius = w * 0.75f,
                center = Offset(w * 0.15f, h * 0.15f),
            ),
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(peach, Color.Transparent),
                    center = Offset(w * 0.90f, h * 0.25f),
                    radius = w * 0.65f,
                ),
                radius = w * 0.65f,
                center = Offset(w * 0.90f, h * 0.25f),
            ),
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(lavender, Color.Transparent),
                    center = Offset(w * 0.55f, h * 0.85f),
                    radius = w * 0.70f,
                ),
                radius = w * 0.70f,
                center = Offset(w * 0.55f, h * 0.85f),
            ),
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(cream, Color.Transparent),
                    center = Offset(w * 0.45f, h * 0.45f),
                    radius = w * 0.45f,
                ),
                radius = w * 0.45f,
                center = Offset(w * 0.45f, h * 0.45f),
            ),
        )
    }

    GradientBackgroundStyle.MintBreeze -> {
        val base = if (isDark) 0.20f else 0.26f
        val mint = Color(0xFF69F0AE).softenIfLight(surfaceColor, isDark).copy(alpha = base)
        val sky = Color(0xFF40C4FF).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.90f)
        val leaf = Color(0xFF81C784).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.70f)
        val cream = Color(0xFFFFF9C4).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.55f)
        listOf(
            RectSpec(
                brush = Brush.linearGradient(
                    0.0f to sky.copy(alpha = 0f),
                    0.50f to sky,
                    1.0f to mint.copy(alpha = 0f),
                    start = Offset(w, 0f),
                    end = Offset(0f, h),
                    tileMode = TileMode.Clamp,
                ),
                topLeft = Offset.Zero,
                size = Size(w, h),
            ),
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(mint.copy(alpha = base * 1.1f), Color.Transparent),
                    center = Offset(w * 0.15f, h * 0.80f),
                    radius = h * 0.55f,
                ),
                radius = h * 0.55f,
                center = Offset(w * 0.15f, h * 0.80f),
            ),
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(leaf, Color.Transparent),
                    center = Offset(w * 0.85f, h * 0.20f),
                    radius = w * 0.40f,
                ),
                radius = w * 0.40f,
                center = Offset(w * 0.85f, h * 0.20f),
            ),
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(cream, Color.Transparent),
                    center = Offset(w * 0.75f, h * 0.75f),
                    radius = w * 0.30f,
                ),
                radius = w * 0.30f,
                center = Offset(w * 0.75f, h * 0.75f),
            ),
        )
    }

    GradientBackgroundStyle.StarryNight -> {
        val base = if (isDark) 0.30f else 0.22f
        val midnight = Color(0xFF0D1B2A).softenIfLight(surfaceColor, isDark).copy(alpha = base)
        val indigo = Color(0xFF1B263B).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.85f)
        val silver = Color(0xFFB0BEC5).softenIfLight(surfaceColor, isDark).copy(alpha = base * 1.1f)
        val gold = Color(0xFFFFD54F).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.65f)
        listOf(
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(midnight, Color.Transparent),
                    center = Offset(w * 0.30f, h * 0.30f),
                    radius = w * 0.95f,
                ),
                radius = w * 0.95f,
                center = Offset(w * 0.30f, h * 0.30f),
            ),
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(indigo, Color.Transparent),
                    center = Offset(w * 0.85f, h * 0.20f),
                    radius = w * 0.55f,
                ),
                radius = w * 0.55f,
                center = Offset(w * 0.85f, h * 0.20f),
            ),
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(silver.copy(alpha = base * 1.4f), Color.Transparent),
                    center = Offset(w * 0.55f, h * 0.45f),
                    radius = w * 0.18f,
                ),
                radius = w * 0.18f,
                center = Offset(w * 0.55f, h * 0.45f),
            ),
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(gold.copy(alpha = base * 0.8f), Color.Transparent),
                    center = Offset(w * 0.75f, h * 0.72f),
                    radius = w * 0.10f,
                ),
                radius = w * 0.10f,
                center = Offset(w * 0.75f, h * 0.72f),
            ),
        )
    }

    GradientBackgroundStyle.Lavender -> {
        val base = if (isDark) 0.22f else 0.28f
        val violet = Color(0xFF7E57C2).softenIfLight(surfaceColor, isDark).copy(alpha = base)
        val lilac = Color(0xFFB39DDB).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.85f)
        val blush = Color(0xFFF8BBD0).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.65f)
        val blue = Color(0xFF82B1FF).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.50f)
        listOf(
            RectSpec(
                brush = Brush.sweepGradient(
                    colorStops = arrayOf(
                        0.0f to violet.copy(alpha = base * 0.45f),
                        0.25f to lilac.copy(alpha = base * 0.30f),
                        0.50f to blush.copy(alpha = base * 0.20f),
                        0.75f to blue.copy(alpha = base * 0.25f),
                        1.0f to violet.copy(alpha = base * 0.45f),
                    ),
                    center = Offset(w * 0.5f, h * 0.5f),
                ),
                topLeft = Offset.Zero,
                size = Size(w, h),
            ),
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(violet, Color.Transparent),
                    center = Offset(w * 0.20f, h * 0.15f),
                    radius = w * 0.65f,
                ),
                radius = w * 0.65f,
                center = Offset(w * 0.20f, h * 0.15f),
            ),
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(lilac, Color.Transparent),
                    center = Offset(w * 0.85f, h * 0.80f),
                    radius = w * 0.60f,
                ),
                radius = w * 0.60f,
                center = Offset(w * 0.85f, h * 0.80f),
            ),
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(blush, Color.Transparent),
                    center = Offset(w * 0.70f, h * 0.25f),
                    radius = w * 0.35f,
                ),
                radius = w * 0.35f,
                center = Offset(w * 0.70f, h * 0.25f),
            ),
        )
    }

    GradientBackgroundStyle.WarmGlow -> {
        val base = if (isDark) 0.24f else 0.30f
        val amber = Color(0xFFFFB74D).softenIfLight(surfaceColor, isDark).copy(alpha = base)
        val coral = Color(0xFFFF7043).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.85f)
        val cream = Color(0xFFFFE0B2).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.65f)
        val gold = Color(0xFFFFD54F).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.55f)
        listOf(
            RectSpec(
                brush = Brush.verticalGradient(
                    0.0f to Color.Transparent,
                    0.55f to coral.copy(alpha = base * 0.45f),
                    1.0f to amber.copy(alpha = base * 0.85f),
                ),
                topLeft = Offset.Zero,
                size = Size(w, h),
            ),
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(amber, Color.Transparent),
                    center = Offset(w * 0.85f, h * 0.85f),
                    radius = w * 0.85f,
                ),
                radius = w * 0.85f,
                center = Offset(w * 0.85f, h * 0.85f),
            ),
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(cream, Color.Transparent),
                    center = Offset(w * 0.15f, h * 0.20f),
                    radius = w * 0.45f,
                ),
                radius = w * 0.45f,
                center = Offset(w * 0.15f, h * 0.20f),
            ),
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(gold.copy(alpha = base * 0.9f), Color.Transparent),
                    center = Offset(w * 0.40f, h * 0.65f),
                    radius = w * 0.25f,
                ),
                radius = w * 0.25f,
                center = Offset(w * 0.40f, h * 0.65f),
            ),
        )
    }

    GradientBackgroundStyle.NeonCyber -> {
        val base = if (isDark) 0.28f else 0.32f
        val cyan = Color(0xFF00E5FF).softenIfLight(surfaceColor, isDark).copy(alpha = base)
        val magenta = Color(0xFFFF00E5).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.90f)
        val lime = Color(0xFF76FF03).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.75f)
        val violet = Color(0xFF7C4DFF).softenIfLight(surfaceColor, isDark).copy(alpha = base * 0.65f)
        listOf(
            RectSpec(
                brush = Brush.linearGradient(
                    0.0f to cyan.copy(alpha = 0f),
                    0.35f to cyan,
                    0.65f to magenta,
                    1.0f to magenta.copy(alpha = 0f),
                    start = Offset(w * 0.2f, 0f),
                    end = Offset(w * 0.9f, h),
                    tileMode = TileMode.Clamp,
                ),
                topLeft = Offset.Zero,
                size = Size(w, h),
            ),
            RectSpec(
                brush = Brush.linearGradient(
                    0.0f to violet.copy(alpha = 0f),
                    0.40f to violet,
                    0.70f to lime,
                    1.0f to lime.copy(alpha = 0f),
                    start = Offset(w, h * 0.15f),
                    end = Offset(0f, h * 0.95f),
                    tileMode = TileMode.Clamp,
                ),
                topLeft = Offset.Zero,
                size = Size(w, h),
            ),
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(cyan.copy(alpha = base * 1.2f), Color.Transparent),
                    center = Offset(w * 0.85f, h * 0.15f),
                    radius = w * 0.25f,
                ),
                radius = w * 0.25f,
                center = Offset(w * 0.85f, h * 0.15f),
            ),
            RadialSpec(
                brush = Brush.radialGradient(
                    colors = listOf(magenta.copy(alpha = base * 1.1f), Color.Transparent),
                    center = Offset(w * 0.15f, h * 0.85f),
                    radius = w * 0.25f,
                ),
                radius = w * 0.25f,
                center = Offset(w * 0.15f, h * 0.85f),
            ),
        )
    }
}

private fun Color.toMutedContrastingBackdropColor(
    surfaceColor: Color,
    hueShift: Float,
    saturationMultiplier: Float,
    lightnessDelta: Float,
    surfaceBlend: Float,
    alpha: Float,
): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(toArgb(), hsl)
    hsl[0] = (hsl[0] + hueShift + 360f) % 360f
    hsl[1] = (hsl[1] * saturationMultiplier).coerceIn(0f, 1f)
    hsl[2] = (hsl[2] + lightnessDelta).coerceIn(0f, 1f)

    return Color(ColorUtils.HSLToColor(hsl))
        .blendTowardBackground(surfaceColor, surfaceBlend)
        .copy(alpha = alpha.coerceAtLeast(0f))
}

private fun Color.blendTowardBackground(
    backgroundColor: Color,
    amount: Float,
): Color {
    if (amount <= 0f) return this
    return lerp(this, backgroundColor, amount.coerceIn(0f, 1f))
}


