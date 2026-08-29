package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.MeshGradientPainter
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
            val isDark = settingsState.isNightMode
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

            // light 模式整体再淡一点:降饱和、提高向 surface 混合、alpha 打 8 折,
            // 避免背景补色压过品牌主色
            val classicLightFade = if (isDark) 1f else 0.8f
            val classicColors = remember(
                surfaceColor, isDark, meshGradientAlphaMultiplier, classicLightFade,
                primaryAlpha, tertiaryAlpha, secondaryAlpha, accentAlpha,
                themePrimary, themeSecondary, themeTertiary, themeSurfaceHighest,
            ) {
                ClassicColors(
                    primary = themePrimary.toMutedContrastingBackdropColor(
                        surfaceColor = surfaceColor,
                        hueShift = 180f,
                        saturationMultiplier = if (isDark) 0.40f else 0.28f,
                        lightnessDelta = if (isDark) 0.16f else 0.10f,
                        surfaceBlend = if (isDark) 0.30f else 0.58f,
                        alpha = primaryAlpha * 0.82f * meshGradientAlphaMultiplier * classicLightFade,
                    ),
                    tertiary = themeTertiary.toMutedContrastingBackdropColor(
                        surfaceColor = surfaceColor,
                        hueShift = 180f,
                        saturationMultiplier = if (isDark) 0.38f else 0.26f,
                        lightnessDelta = if (isDark) 0.14f else 0.08f,
                        surfaceBlend = if (isDark) 0.32f else 0.60f,
                        alpha = tertiaryAlpha * 0.80f * meshGradientAlphaMultiplier * classicLightFade,
                    ),
                    secondary = themeSecondary.toMutedContrastingBackdropColor(
                        surfaceColor = surfaceColor,
                        hueShift = 180f,
                        saturationMultiplier = if (isDark) 0.36f else 0.24f,
                        lightnessDelta = if (isDark) 0.12f else 0.07f,
                        surfaceBlend = if (isDark) 0.34f else 0.62f,
                        alpha = secondaryAlpha * 0.76f * meshGradientAlphaMultiplier * classicLightFade,
                    ),
                    accent = themeSurfaceHighest.toMutedContrastingBackdropColor(
                        surfaceColor = surfaceColor,
                        hueShift = 160f,
                        saturationMultiplier = if (isDark) 0.24f else 0.16f,
                        lightnessDelta = if (isDark) 0.08f else 0.05f,
                        surfaceBlend = if (isDark) 0.42f else 0.66f,
                        alpha = accentAlpha * 0.78f * meshGradientAlphaMultiplier * classicLightFade,
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

            // 色彩洗类风格用 Compose 1.12 官方 MeshGradientPainter(硬件加速 drawVertices,
            // 双三次插值),色彩流体融合;Classic/Ethereal/StarryNight 保留光斑叠加
            if (style in meshGradientStyles) {
                val meshPainter = remember(style, surfaceColor, isDark) {
                    createMeshStylePainter(style, surfaceColor, isDark)
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .paint(meshPainter, contentScale = ContentScale.FillBounds)
                )
            } else {
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
            }

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

/** 高性能渐变图元：径向光斑。 */
private data class RadialSpec(
    val brush: Brush,
    val radius: Float,
    val center: Offset,
)

private fun ClassicColors.toSpecs(w: Float, h: Float): List<RadialSpec> = listOf(
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

private fun EtherealColors.toSpecs(w: Float, h: Float): List<RadialSpec> = listOf(
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

private fun DrawScope.drawSpecs(specs: List<RadialSpec>) {
    for (spec in specs) {
        drawCircle(
            brush = spec.brush,
            radius = spec.radius,
            center = spec.center,
        )
    }
}

/**
 * 用官方 MeshGradientPainter 网格渲染的风格:都是多色"色彩洗",适合网格融合。
 * Classic/Ethereal 跟随主题色做极淡渐变、StarryNight 依赖局部小星点光斑,保留光斑路径。
 */
private val meshGradientStyles = setOf(
    GradientBackgroundStyle.Aurora,
    GradientBackgroundStyle.Ocean,
    GradientBackgroundStyle.Sunset,
    GradientBackgroundStyle.SakuraMist,
    GradientBackgroundStyle.MintBreeze,
    GradientBackgroundStyle.Lavender,
    GradientBackgroundStyle.WarmGlow,
    GradientBackgroundStyle.NeonCyber,
    GradientBackgroundStyle.PrismFlow,
)

/** 网格四角顶点:颜色 + 各自的 alpha 系数;边/中心顶点由四角 lerp 得到。 */
private data class MeshVertex(val argb: Long, val alphaScale: Float = 1f)

private data class MeshStylePalette(
    val base: Float,
    val topLeft: MeshVertex,
    val topRight: MeshVertex,
    val bottomLeft: MeshVertex,
    val bottomRight: MeshVertex,
)

/** 各风格四角顶点色,沿用原光斑方案的色值与 alpha 系数,方位对应原光斑主位置。 */
private fun meshStylePalette(
    style: GradientBackgroundStyle,
    isDark: Boolean,
): MeshStylePalette? = when (style) {
    GradientBackgroundStyle.Aurora -> MeshStylePalette(
        base = if (isDark) 0.22f else 0.26f,
        topLeft = MeshVertex(0xFF80CBC4),              // 薄荷
        topRight = MeshVertex(0xFF7C4DFF, 0.95f),      // 紫罗兰
        bottomLeft = MeshVertex(0xFF00BFA5, 0.85f),    // 青绿
        bottomRight = MeshVertex(0xFFF48FB1, 0.75f),   // 柔粉
    )

    GradientBackgroundStyle.Ocean -> MeshStylePalette(
        base = if (isDark) 0.24f else 0.28f,
        topLeft = MeshVertex(0xFF0D1B2A, 1.1f),        // 午夜蓝
        topRight = MeshVertex(0xFF00E5FF),             // 荧光青
        bottomLeft = MeshVertex(0xFF1A237E, 0.75f),    // 靛蓝
        bottomRight = MeshVertex(0xFF00838F, 0.65f),   // 深青
    )

    GradientBackgroundStyle.Sunset -> MeshStylePalette(
        base = if (isDark) 0.24f else 0.30f,
        topLeft = MeshVertex(0xFFD81B60, 0.85f),       // 玫瑰粉
        topRight = MeshVertex(0xFFFF6F00),             // 琥珀橙
        bottomLeft = MeshVertex(0xFF4A148C, 0.55f),    // 暗紫
        bottomRight = MeshVertex(0xFFFFD54F, 0.70f),   // 金色
    )

    GradientBackgroundStyle.SakuraMist -> MeshStylePalette(
        base = if (isDark) 0.20f else 0.26f,
        topLeft = MeshVertex(0xFFF8BBD0),              // 柔粉
        topRight = MeshVertex(0xFFFFAB91, 0.85f),      // 蜜桃
        bottomLeft = MeshVertex(0xFFE1BEE7, 0.70f),    // 淡紫
        bottomRight = MeshVertex(0xFFFFF3E0, 0.55f),   // 奶油
    )

    GradientBackgroundStyle.MintBreeze -> MeshStylePalette(
        base = if (isDark) 0.20f else 0.26f,
        topLeft = MeshVertex(0xFF40C4FF, 0.90f),       // 晴空蓝
        topRight = MeshVertex(0xFF81C784, 0.70f),      // 嫩芽绿
        bottomLeft = MeshVertex(0xFF69F0AE),           // 薄荷绿
        bottomRight = MeshVertex(0xFFFFF9C4, 0.55f),   // 奶黄
    )

    GradientBackgroundStyle.Lavender -> MeshStylePalette(
        base = if (isDark) 0.22f else 0.28f,
        topLeft = MeshVertex(0xFF7E57C2),              // 紫罗兰
        topRight = MeshVertex(0xFFF8BBD0, 0.65f),      // 柔粉
        bottomLeft = MeshVertex(0xFF82B1FF, 0.50f),    // 淡蓝
        bottomRight = MeshVertex(0xFFB39DDB, 0.85f),   // 丁香
    )

    GradientBackgroundStyle.WarmGlow -> MeshStylePalette(
        base = if (isDark) 0.24f else 0.30f,
        topLeft = MeshVertex(0xFFFFE0B2, 0.65f),       // 奶油
        topRight = MeshVertex(0xFFFF7043, 0.85f),      // 珊瑚
        bottomLeft = MeshVertex(0xFFFFD54F, 0.55f),    // 暖金
        bottomRight = MeshVertex(0xFFFFB74D),          // 琥珀
    )

    GradientBackgroundStyle.NeonCyber -> MeshStylePalette(
        base = if (isDark) 0.28f else 0.32f,
        topLeft = MeshVertex(0xFF00E5FF),              // 电青
        topRight = MeshVertex(0xFF7C4DFF, 0.65f),      // 紫罗兰
        bottomLeft = MeshVertex(0xFFFF00E5, 0.90f),    // 品红
        bottomRight = MeshVertex(0xFF76FF03, 0.75f),   // 荧光绿
    )

    else -> null
}

/**
 * 把四角"色彩洗"风格渲染成 3×3 网格渐变:四角取风格主色,
 * 边中点与中心由相邻角 lerp 得到,双三次插值融合成整屏连续渐变。
 */
private fun createMeshStylePainter(
    style: GradientBackgroundStyle,
    surfaceColor: Color,
    isDark: Boolean,
): MeshGradientPainter {
    val palette = meshStylePalette(style, isDark)
        ?: return createPrismFlowPainter(surfaceColor, isDark)

    fun vertexColor(vertex: MeshVertex): Color =
        Color(vertex.argb)
            .softenIfLight(surfaceColor, isDark)
            .copy(alpha = (palette.base * vertex.alphaScale).coerceAtMost(1f))

    val c1 = vertexColor(palette.topLeft)
    val c2 = vertexColor(palette.topRight)
    val c3 = vertexColor(palette.bottomLeft)
    val c4 = vertexColor(palette.bottomRight)

    return MeshGradientPainter(rows = 3, columns = 3, hasBicubicColor = true) {
        setVertex(0, 0, Offset(0f, 0f), c1)
        setVertex(0, 1, Offset(0.5f, 0f), lerp(c1, c2, 0.5f))
        setVertex(0, 2, Offset(1f, 0f), c2)
        setVertex(1, 0, Offset(0f, 0.5f), lerp(c1, c3, 0.5f))
        setVertex(1, 1, Offset(0.5f, 0.5f), lerp(lerp(c1, c2, 0.5f), lerp(c3, c4, 0.5f), 0.5f))
        setVertex(1, 2, Offset(1f, 0.5f), lerp(c2, c4, 0.5f))
        setVertex(2, 0, Offset(0f, 1f), c3)
        setVertex(2, 1, Offset(0.5f, 1f), lerp(c3, c4, 0.5f))
        setVertex(2, 2, Offset(1f, 1f), c4)
    }
}

/**
 * PrismFlow 风格:3×3 网格 + 双三次颜色插值,顶点取青/紫/品红/暖金等高饱和色,
 * 融合成整屏连续的"流光"渐变,区别于其余风格的光斑叠加。
 */
private fun createPrismFlowPainter(
    surfaceColor: Color,
    isDark: Boolean,
): MeshGradientPainter {
    val base = if (isDark) 0.30f else 0.34f

    fun vertexColor(argb: Long, alphaScale: Float = 1f): Color =
        Color(argb)
            .softenIfLight(surfaceColor, isDark)
            .copy(alpha = (base * alphaScale).coerceAtMost(1f))

    return MeshGradientPainter(rows = 3, columns = 3, hasBicubicColor = true) {
        setVertex(0, 0, Offset(0f, 0f), vertexColor(0xFF00BFA5))            // 青碧
        setVertex(0, 1, Offset(0.48f, 0f), vertexColor(0xFF40C4FF))         // 晴空蓝
        setVertex(0, 2, Offset(1f, 0f), vertexColor(0xFF7C4DFF))            // 紫罗兰
        setVertex(1, 0, Offset(0f, 0.52f), vertexColor(0xFF00E5FF, 0.9f))   // 荧光青
        setVertex(1, 1, Offset(0.55f, 0.48f), vertexColor(0xFFF06292, 0.8f))// 蔷薇粉
        setVertex(1, 2, Offset(1f, 0.55f), vertexColor(0xFFFF7043, 0.9f))   // 珊瑚橙
        setVertex(2, 0, Offset(0f, 1f), vertexColor(0xFF81C784, 0.85f))     // 嫩芽绿
        setVertex(2, 1, Offset(0.52f, 1f), vertexColor(0xFFFFD54F))         // 暖金
        setVertex(2, 2, Offset(1f, 1f), vertexColor(0xFF5E35B1))            // 深紫
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
): List<RadialSpec> = when (style) {
    // 网格渲染的风格不会进入光斑路径
    GradientBackgroundStyle.Classic,
    GradientBackgroundStyle.Ethereal,
    GradientBackgroundStyle.Aurora,
    GradientBackgroundStyle.Ocean,
    GradientBackgroundStyle.Sunset,
    GradientBackgroundStyle.SakuraMist,
    GradientBackgroundStyle.MintBreeze,
    GradientBackgroundStyle.Lavender,
    GradientBackgroundStyle.WarmGlow,
    GradientBackgroundStyle.NeonCyber,
    GradientBackgroundStyle.PrismFlow -> emptyList()

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


