package com.t8rin.imagetoolbox.core.ui.widget.glass

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.MeshGradientPainter
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
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

    GlassBackdropFrame(
        modifier = modifier.fillMaxSize(),
        backdrop = {
        Box(
            Modifier
                .matchParentSize()
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
            val themePrimary = MaterialTheme.colorScheme.primary
            val themeSecondary = MaterialTheme.colorScheme.secondary
            val themeTertiary = MaterialTheme.colorScheme.tertiary
            val scheme = MaterialTheme.colorScheme
            val primaryContainer = scheme.primaryContainer
            val secondaryContainer = scheme.secondaryContainer
            val tertiaryContainer = scheme.tertiaryContainer
            val surfaceContainer = scheme.surfaceContainer
            val schemePrimaryFixed = scheme.primaryFixed
            val schemePrimaryFixedDim = scheme.primaryFixedDim
            val schemeSecondaryFixed = scheme.secondaryFixed
            val schemeSecondaryFixedDim = scheme.secondaryFixedDim
            val schemeTertiaryFixed = scheme.tertiaryFixed
            val schemeTertiaryFixedDim = scheme.tertiaryFixedDim
            val schemeInversePrimary = scheme.inversePrimary
            val surfaceContainerLow = scheme.surfaceContainerLow
            val surfaceContainerHigh = scheme.surfaceContainerHigh
            val surfaceContainerLowest = scheme.surfaceContainerLowest
            // 默认参数之和是 0.58，调用方没改透明度时强度为 1，只影响丝雾。
            val themeIntensity = (
                (primaryAlpha + secondaryAlpha + tertiaryAlpha + accentAlpha) / 0.58f
                ).coerceIn(0.8f, 1.25f)

            // 经典直接铺 ColorScheme 的色调角色。其余样式按名字配色相和饱和度。
            val meshSpec = remember(
                style,
                isDark,
                themePrimary,
                themeSecondary,
                themeTertiary,
                themeIntensity,
                primaryContainer,
                secondaryContainer,
                tertiaryContainer,
                surfaceContainer,
                schemePrimaryFixed,
                schemePrimaryFixedDim,
                schemeSecondaryFixed,
                schemeSecondaryFixedDim,
                schemeTertiaryFixed,
                schemeTertiaryFixedDim,
                schemeInversePrimary,
                surfaceContainerLow,
                surfaceContainerHigh,
                surfaceContainerLowest,
            ) {
                if (style == GradientBackgroundStyle.Classic) {
                    schemeMeshSpec(
                        listOf(
                            listOf(
                                themePrimary,
                                themeSecondary,
                                themeTertiary,
                                schemePrimaryFixedDim,
                                schemeInversePrimary,
                            ),
                            listOf(
                                primaryContainer,
                                secondaryContainer,
                                tertiaryContainer,
                                surfaceContainerLow,
                                schemePrimaryFixed,
                            ),
                            listOf(
                                schemeSecondaryFixed,
                                schemeTertiaryFixed,
                                themePrimary,
                                schemeTertiaryFixedDim,
                                schemeSecondaryFixedDim,
                            ),
                            listOf(
                                surfaceContainerLowest,
                                primaryContainer,
                                secondaryContainer,
                                surfaceContainerHigh,
                                tertiaryContainer,
                            ),
                        ),
                    )
                } else {
                    organicMeshSpec(
                        style = style,
                        isDark = isDark,
                        primary = themePrimary,
                        secondary = themeSecondary,
                        tertiary = themeTertiary,
                        themeIntensity = themeIntensity,
                    )
                }
            }
            val meshPainter = remember(meshSpec) { meshSpec.toPainter() }
            MeshGradientLayer(meshSpec, meshPainter)

            val meshGradientVeilMultiplier = if (isDark) 1f else 0.88f
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
        }
        },
        content = content,
    )
}

/**
 * 铺网格渐变。
 *
 * API 34 及以上交给平台的 Mesh API([MeshGradientPainter]);低版本自己铺三角网
 * (见 [MeshGradientSpec]):Compose 的回退实现会把顶点色乘成黑色,这里换白色 paint 画,
 * 颜色才是对的。
 */
@Composable
private fun MeshGradientLayer(spec: MeshGradientSpec, painter: MeshGradientPainter) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .paint(painter, contentScale = ContentScale.FillBounds)
        )
        return
    }
    val mesh = remember(spec) { spec.buildMesh() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // 顶点是归一化坐标,铺满整块画布。
                scale(size.width, size.height, pivot = Offset.Zero) {
                    with(mesh) { draw() }
                }
            }
    )
}

/**
 * 经典:顶点就是当前 ColorScheme 的色调角色。
 * 一行用主色、辅色、三级色这类比较实的颜色,下一行用对应的 Container 和 Fixed,
 * 对比度跟主题走,不再把四个容器色铺匀。
 */
private fun schemeMeshSpec(bands: List<List<Color>>): MeshGradientSpec = meshGradientSpec(
    rows = bands.size - 1,
    columns = bands.first().size - 1,
    positions = vertexPositions(bands.size, bands.first().size, salt = 0),
    colors = bands.flatten(),
    flow = CLASSIC_FLOW,
    salt = 0,
)

private fun organicMeshSpec(
    style: GradientBackgroundStyle,
    isDark: Boolean,
    primary: Color,
    secondary: Color,
    tertiary: Color,
    themeIntensity: Float,
): MeshGradientSpec {
    val recipe = meshRecipe(style, isDark, primary, secondary, tertiary, themeIntensity)
    val vertexRows = recipe.rows + 1
    val vertexColumns = recipe.columns + 1
    return meshGradientSpec(
        rows = recipe.rows,
        columns = recipe.columns,
        positions = vertexPositions(vertexRows, vertexColumns, style.ordinal2),
        colors = vertexColors(recipe, vertexRows, vertexColumns, style.ordinal2, isDark),
        flow = recipe.flow,
        salt = style.ordinal2,
    )
}

/**
 * 内部顶点给一对错开的控制点,让网格边界有点流动感;四边不给,保持平整。
 * 控制点只影响 patch 内部的弯曲,位置本身照旧走 [vertexPositions] 的抖动。
 */
private fun meshGradientSpec(
    rows: Int,
    columns: Int,
    positions: List<Offset>,
    colors: List<Color>,
    flow: Float,
    salt: Int,
): MeshGradientSpec {
    val vertexRows = rows + 1
    val vertexColumns = columns + 1
    val rightTangents = MutableList(positions.size) { Offset.Unspecified }
    val bottomTangents = MutableList(positions.size) { Offset.Unspecified }
    val dx = flow / columns
    val dy = flow / rows
    for (row in 1 until vertexRows - 1) {
        for (column in 1 until vertexColumns - 1) {
            val index = row * vertexColumns + column
            val sign = if ((salt + row + column) % 2 == 0) 1f else -1f
            rightTangents[index] = Offset(dx * sign, -dy * 0.35f * sign)
            bottomTangents[index] = Offset(-dx * 0.3f * sign, dy * sign)
        }
    }
    return MeshGradientSpec(
        rows = rows,
        columns = columns,
        positions = positions,
        colors = colors,
        rightTangents = rightTangents,
        bottomTangents = bottomTangents,
    )
}

/** 经典样式的流动幅度,与旧实现里写死的 0.16 一致。 */
private const val CLASSIC_FLOW = 0.16f

private data class MeshRecipe(
    val rows: Int,
    val columns: Int,
    val hues: List<Float>,
    val saturation: Float,
    val lightness: Float,
    val jitter: Float,
    val flow: Float,
    /** 相邻顶点的饱和度差。0 表示只跟 [jitter] 轻轻错开。 */
    val satSpread: Float = 0f,
    /** 相邻顶点的明度差。深的更饱和，浅的更淡。 */
    val lightSpread: Float = 0f,
    /** 深色模式下允许顶点亮到这里，否则淡顶点会被压成同一块暗色。 */
    val darkLuminanceCeiling: Float = 0.22f,
    /** 顶点颜色的不透明度。1 为铺满，低于 1 时底色透出来。 */
    val alpha: Float = 1f,
    /** 稀疏点缀。每隔 [accentEvery] 个顶点换这套色相，用来和卡片拉开层次。0 表示不点缀。 */
    val accentHue: Float? = null,
    val accentEvery: Int = 0,
    val accentSaturation: Float = 0f,
    val accentLightness: Float = 0f,
    val accentAlpha: Float = 1f,
)

private fun meshRecipe(
    style: GradientBackgroundStyle,
    isDark: Boolean,
    primary: Color,
    secondary: Color,
    tertiary: Color,
    themeIntensity: Float,
): MeshRecipe {
    if (style == GradientBackgroundStyle.Ethereal) {
        val base = primary.stableHue()
        val spread = analogousThemeHues(
            base = base,
            secondary = secondary.stableHue(),
            tertiary = tertiary.stableHue(),
            span = 18f,
        )
        return MeshRecipe(
            rows = 2,
            columns = 3,
            hues = spread,
            saturation = 0.26f * themeIntensity,
            lightness = if (isDark) 0.30f else 0.84f,
            jitter = 0.02f,
            flow = 0.10f,
        )
    }
    val light = !isDark
    return when (style) {
        // 竖向薄幕：青绿和紫罗兰还在，饱和度压低，颜色本身半透明，底色透出来。
        GradientBackgroundStyle.Aurora -> recipe(
            rows = 6, columns = 3,
            hues = listOf(158f, 292f, 174f, 318f, 188f, 274f),
            light = light,
            saturation = if (light) 0.22f else 0.28f,
            lightLightness = 0.86f,
            darkLightness = 0.20f,
            jitter = 0.03f,
            flow = 0.20f,
            satSpread = 0.10f,
            lightSpread = 0.08f,
            darkLuminanceCeiling = 0.30f,
            alpha = if (light) 0.46f else 0.58f,
        )
        // 只有蓝和青绿。整体偏淡，顶点一深一浅。
        GradientBackgroundStyle.Ocean -> recipe(
            rows = 5, columns = 3,
            hues = listOf(204f, 196f, 216f, 186f, 224f),
            light = light, saturation = 0.40f, lightLightness = 0.66f, darkLightness = 0.24f,
            jitter = 0.035f, flow = 0.14f,
            satSpread = 0.26f, lightSpread = 0.16f, darkLuminanceCeiling = 0.46f,
        )
        // 樱粉铺得很淡，底色透出来，卡片才有一层可站。
        // 少数顶点点浅叶绿，粉和叶交错，避免整屏一块粉、和卡片糊在一起。
        GradientBackgroundStyle.SakuraMist -> recipe(
            rows = 3, columns = 4,
            hues = listOf(346f, 352f, 338f, 356f, 344f),
            light = light, saturation = if (light) 0.36f else 0.42f,
            lightLightness = 0.92f, darkLightness = 0.30f,
            jitter = 0.02f, flow = 0.10f,
            satSpread = if (light) 0.04f else 0.16f,
            lightSpread = if (light) 0.01f else 0.10f,
            darkLuminanceCeiling = 0.40f,
            alpha = if (light) 0.42f else 1f,
            accentHue = if (light) 132f else null,
            accentEvery = if (light) 5 else 0,
            accentSaturation = 0.16f,
            accentLightness = 0.92f,
            accentAlpha = 0.22f,
        )
        // 薄荷、汽水蓝、青柠、一点柠檬。
        GradientBackgroundStyle.MintBreeze -> recipe(
            rows = 3, columns = 5,
            hues = listOf(164f, 188f, 146f, 200f, 86f, 54f),
            light = light, saturation = 0.50f, lightLightness = 0.74f, darkLightness = 0.28f,
            jitter = 0.045f, flow = 0.18f,
        )
        // 浅色是银靛薄暮，深色压到海军蓝。夜色里也要有亮斑和暗块。
        GradientBackgroundStyle.StarryNight -> recipe(
            rows = 5, columns = 4,
            hues = listOf(232f, 250f, 222f, 268f, 238f),
            light = light, saturation = if (light) 0.34f else 0.48f, lightLightness = 0.56f, darkLightness = 0.22f,
            jitter = 0.08f, flow = 0.12f,
            satSpread = 0.22f, lightSpread = 0.12f, darkLuminanceCeiling = 0.38f,
        )
        // 一整片紫罗兰到丁香。淡底上让饱和的紫和近白的紫交错。
        GradientBackgroundStyle.Lavender -> recipe(
            rows = 3, columns = 5,
            hues = listOf(276f, 288f, 264f, 302f, 252f),
            light = light, saturation = 0.34f, lightLightness = 0.76f, darkLightness = 0.34f,
            jitter = 0.025f, flow = 0.12f,
            satSpread = 0.28f, lightSpread = 0.16f, darkLuminanceCeiling = 0.50f,
        )
        // 玫瑰、琥珀、金色。比焦糖更淡，顶点从近白到较饱和交错。
        GradientBackgroundStyle.Sunset -> recipe(
            rows = 3, columns = 4,
            hues = listOf(348f, 24f, 42f, 8f, 32f),
            light = light, saturation = 0.32f, lightLightness = 0.78f, darkLightness = 0.36f,
            jitter = 0.025f, flow = 0.14f,
            satSpread = 0.28f, lightSpread = 0.16f, darkLuminanceCeiling = 0.50f,
        )
        // 蜜桃、琥珀、焦糖，全在暖色里。淡的蜂蜜和深的焦糖相邻。
        GradientBackgroundStyle.WarmGlow -> recipe(
            rows = 3, columns = 4,
            hues = listOf(22f, 34f, 12f, 42f, 4f),
            light = light, saturation = 0.44f, lightLightness = 0.72f, darkLightness = 0.34f,
            jitter = 0.035f, flow = 0.15f,
            satSpread = 0.26f, lightSpread = 0.15f, darkLuminanceCeiling = 0.48f,
        )
        // 电青、品红、紫罗兰、荧光绿，饱和度最高。
        GradientBackgroundStyle.NeonCyber -> recipe(
            rows = 5, columns = 5,
            hues = listOf(178f, 318f, 268f, 142f, 198f, 332f),
            light = light, saturation = 0.84f, lightLightness = 0.62f, darkLightness = 0.30f,
            jitter = 0.07f, flow = 0.24f,
        )
        // 色环铺满，网格最密，就是现在这种溢彩。
        GradientBackgroundStyle.PrismFlow -> recipe(
            rows = 4, columns = 6,
            hues = listOf(12f, 200f, 48f, 276f, 160f, 328f, 92f, 232f),
            light = light, saturation = 0.80f, lightLightness = 0.70f, darkLightness = 0.28f,
            jitter = 0.08f, flow = 0.30f,
        )
        GradientBackgroundStyle.Classic,
        GradientBackgroundStyle.Ethereal -> error("Classic and silk mist are built elsewhere")
    }
}

private fun recipe(
    rows: Int,
    columns: Int,
    hues: List<Float>,
    light: Boolean,
    saturation: Float,
    lightLightness: Float,
    darkLightness: Float,
    jitter: Float,
    flow: Float,
    satSpread: Float = 0f,
    lightSpread: Float = 0f,
    darkLuminanceCeiling: Float = 0.22f,
    alpha: Float = 1f,
    accentHue: Float? = null,
    accentEvery: Int = 0,
    accentSaturation: Float = 0f,
    accentLightness: Float = 0f,
    accentAlpha: Float = 1f,
): MeshRecipe = MeshRecipe(
    rows = rows,
    columns = columns,
    hues = hues,
    saturation = saturation,
    lightness = if (light) lightLightness else darkLightness,
    jitter = jitter,
    flow = flow,
    satSpread = satSpread,
    lightSpread = lightSpread,
    darkLuminanceCeiling = darkLuminanceCeiling,
    alpha = alpha,
    accentHue = accentHue,
    accentEvery = accentEvery,
    accentSaturation = accentSaturation,
    accentLightness = accentLightness,
    accentAlpha = accentAlpha,
)

/** 主题色锚住，辅色离得太远就拉回邻近范围，避免经典/丝雾变成满色环。 */
private fun analogousThemeHues(
    base: Float,
    secondary: Float,
    tertiary: Float,
    span: Float,
): List<Float> {
    fun pull(hue: Float): Float {
        val delta = signedHueDelta(base, hue).coerceIn(-span, span)
        return (base + delta + 360f) % 360f
    }
    return listOf(
        base,
        pull(secondary),
        (base + span * 0.65f) % 360f,
        pull(tertiary),
        (base - span * 0.45f + 360f) % 360f,
    )
}

private fun signedHueDelta(from: Float, to: Float): Float {
    var delta = (to - from) % 360f
    if (delta > 180f) delta -= 360f
    if (delta < -180f) delta += 360f
    return delta
}

private fun vertexColors(
    recipe: MeshRecipe,
    vertexRows: Int,
    vertexColumns: Int,
    salt: Int,
    isDark: Boolean,
): List<Color> = List(vertexRows * vertexColumns) { index ->
    val row = index / vertexColumns
    val column = index % vertexColumns
    val hue = hueAt(row, column, recipe.hues, salt)
    val accentHue = recipe.accentHue
    if (accentHue != null && recipe.accentEvery > 0 && (row + column) % recipe.accentEvery == 0) {
        return@List airyColor(
            hue = accentHue,
            saturation = recipe.accentSaturation,
            lightness = recipe.accentLightness,
            isDark = isDark,
            darkLuminanceCeiling = recipe.darkLuminanceCeiling,
            alpha = recipe.accentAlpha,
        )
    }
    // 窄色相按整行交替：一行更饱和更深，下一行更淡更浅。
    // 逐格棋盘会被双三次插值抹成中间色，渐变又变顺。
    val deep = if (recipe.satSpread > 0f) {
        row % 2 == 0
    } else {
        (row + column) % 2 == 0
    }
    val flip = if (deep) recipe.jitter else -recipe.jitter
    val columnNudge = if (recipe.satSpread > 0f && column % 2 == 0) 0.06f else 0f
    val satDelta = if (recipe.satSpread > 0f) {
        (if (deep) recipe.satSpread else -recipe.satSpread) + columnNudge
    } else {
        flip * 0.25f
    }
    val lightDelta = if (recipe.lightSpread > 0f) {
        if (deep) -recipe.lightSpread else recipe.lightSpread
    } else {
        flip
    }
    airyColor(
        hue = hue,
        saturation = recipe.saturation + satDelta,
        lightness = recipe.lightness + lightDelta,
        isDark = isDark,
        darkLuminanceCeiling = recipe.darkLuminanceCeiling,
        alpha = recipe.alpha,
    )
}

/**
 * 横着相邻差 1 个色相，竖着相邻差 2 个色相。调色板按交错顺序排，
 * 这两步都不会落到相近的颜色上。
 */
private fun hueAt(row: Int, col: Int, hues: List<Float>, salt: Int): Float {
    val count = hues.size
    val index = (row * 2 + col + salt) % count
    return hues[index]
}

private fun vertexPositions(vertexRows: Int, vertexColumns: Int, salt: Int): List<Offset> {
    return List(vertexRows * vertexColumns) { index ->
        val row = index / vertexColumns
        val column = index % vertexColumns
        Offset(
            x = edgeOrJitter(column, vertexColumns, salt, row),
            y = edgeOrJitter(row, vertexRows, salt * 3, column),
        )
    }
}

private fun edgeOrJitter(index: Int, count: Int, salt: Int, other: Int): Float {
    if (count <= 1) return 0f
    if (index == 0) return 0f
    if (index == count - 1) return 1f
    val base = index / (count - 1).toFloat()
    val jitter = (((salt * 13 + other * 17 + index * 11) % 9) - 4) * 0.014f
    return (base + jitter).coerceIn(0.06f, 0.94f)
}

private fun airyColor(
    hue: Float,
    saturation: Float,
    lightness: Float,
    isDark: Boolean,
    darkLuminanceCeiling: Float = 0.22f,
    alpha: Float = 1f,
): Color {
    val hsl = floatArrayOf(
        (hue % 360f + 360f) % 360f,
        saturation.coerceIn(0.08f, 0.88f),
        lightness.coerceIn(0.08f, 0.92f),
    )
    var color = Color(ColorUtils.HSLToColor(hsl))
    var guard = 0
    if (isDark) {
        while (color.luminance() > darkLuminanceCeiling && guard++ < 5) {
            color = lerp(color, Color.Black, 0.12f)
        }
        guard = 0
        while (color.luminance() < 0.02f && guard++ < 4) {
            color = lerp(color, Color.White, 0.1f)
        }
    } else if (color.luminance() < 0.22f) {
        while (color.luminance() < 0.22f && guard++ < 4) {
            color = lerp(color, Color.White, 0.1f)
        }
    }
    return color.copy(alpha = alpha.coerceIn(0f, 1f))
}

private fun Color.stableHue(): Float {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(toArgb(), hsl)
    return if (hsl[1] < 0.06f) 204f else hsl[0]
}
