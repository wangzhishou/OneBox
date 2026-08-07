package com.wanbaohe.com.color

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import kotlin.math.max

/**
 * Utilities to generate the decision wheel segment colors in a consistent and flat style.
 *
 * Color generation rules (aligned with the Color Tools approach):
 * 1) Pick a base color (usually the app theme primary color).
 * 2) Generate its complementary color.
 * 3) Generate as many mixed colors as there are items by interpolating between
 *    the base and complementary colors.
 *
 * Text color rule:
 * - Prefer the inverse of the segment color.
 * - If the inverse doesn't provide sufficient contrast, fall back to black/white
 *   based on WCAG contrast ratio.
 *
 * Edge cases:
 * - If [count] <= 0 returns an empty list.
 * - If [count] == 1 returns a single-item list with the base color.
 */
object ColorGenerator {

    /** Minimum acceptable contrast ratio for text on a segment color. */
    private const val MinContrastRatio: Double = 4.5

    /**
     * Represents a color pair for a wheel segment.
     *
     * @property background Background color of the wheel segment.
     * @property content Foreground/text color of the wheel segment.
     */
    @Immutable
    data class SegmentColors(
        val background: Color,
        val content: Color
    )

    /**
     * Generates [count] distinct segment background colors using the base→complement gradient.
     *
     * @param baseColor The selected base color.
     * @param count Number of required colors (usually equals items count).
     */
    fun generateSegmentBackgrounds(
        baseColor: Color,
        count: Int
    ): List<Color> {
        if (count <= 0) return emptyList()
        if (count == 1) return listOf(baseColor)

        val baseArgb = baseColor.toOpaqueArgb()
        val compArgb = complementaryColor(baseArgb)

        // Evenly sample along the blend line [0..1], inclusive.
        return List(count) { index ->
            val t = index.toFloat() / max(1, count - 1).toFloat()
            Color(ColorUtils.blendARGB(baseArgb, compArgb, t))
        }
    }

    /**
     * Generates [SegmentColors] for each segment.
     */
    fun generateSegmentColors(
        baseColor: Color,
        count: Int
    ): List<SegmentColors> {
        return generateSegmentBackgrounds(baseColor, count).map { bg ->
            SegmentColors(
                background = bg,
                content = contentColorFor(bg)
            )
        }
    }

    /**
     * Returns a readable text color for the given [background].
     *
     * Primary strategy: inverse color.
     * Fallback: black/white based on contrast.
     */
    fun contentColorFor(background: Color): Color {
        val bgArgb = background.toOpaqueArgb()

        val inverse = inverseColor(bgArgb)
        if (hasSufficientContrast(inverse, bgArgb)) {
            return Color(inverse)
        }

        val white = android.graphics.Color.WHITE
        val black = android.graphics.Color.BLACK

        // Choose the better one.
        val cWhite = ColorUtils.calculateContrast(white, bgArgb)
        val cBlack = ColorUtils.calculateContrast(black, bgArgb)
        return Color(if (cWhite >= cBlack) white else black)
    }

    /** Computes complementary color using HSL hue rotation (Hue + 180°). */
    private fun complementaryColor(argb: Int): Int {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(argb, hsl)
        hsl[0] = (hsl[0] + 180f) % 360f
        return ColorUtils.HSLToColor(hsl)
    }

    /** Simple inverse in ARGB space, keeps alpha as 255. */
    private fun inverseColor(argb: Int): Int {
        val r = 255 - android.graphics.Color.red(argb)
        val g = 255 - android.graphics.Color.green(argb)
        val b = 255 - android.graphics.Color.blue(argb)
        return android.graphics.Color.argb(255, r, g, b)
    }

    private fun hasSufficientContrast(foregroundArgb: Int, backgroundArgb: Int): Boolean {
        return runCatching {
            ColorUtils.calculateContrast(foregroundArgb, backgroundArgb) >= MinContrastRatio
        }.getOrDefault(false)
    }

    /**
     * Converts a Compose [Color] to an opaque ARGB int.
     * If the input color is translucent, we treat it as already composited on an opaque surface
     * by forcing alpha to 255. This keeps contrast computations stable.
     */
    private fun Color.toOpaqueArgb(): Int {
        val argb = toArgb()
        return android.graphics.Color.argb(
            255,
            android.graphics.Color.red(argb),
            android.graphics.Color.green(argb),
            android.graphics.Color.blue(argb)
        )
    }

    /**
     * Returns two split-complementary colors for the given [baseColor].
     *
     * This matches the Color Tools definition:
     * - Base hue: H
     * - Split complementary hues: H ± 150°
     */
    fun changeChroma(color: Color, chroma: Float): Int {
        val hct = FloatArray(3)
        ColorUtils.colorToM3HCT(color.toArgb(), hct)
        return ColorUtils.M3HCTToColor(hct[0], (hct[1] + chroma), hct[2])
    }

    fun changeM3HCT(color: Color, hue: Float = 0f, chroma: Float = 0f, tone: Float = 0f): Color {
        val hct = FloatArray(3)
        ColorUtils.colorToM3HCT(color.toArgb(), hct)
        return Color(
            ColorUtils.M3HCTToColor(
                (hct[0] + hue).coerceAtLeast(0f).coerceAtMost(360f),
                (hct[1] + chroma),
                (hct[2] + tone).coerceAtLeast(0f).coerceAtMost(100f)
            )
        )
    }

    fun splitComplementaryPair(baseColor: Color): Pair<Color, Color> {
        val baseArgb = baseColor.toOpaqueArgb()
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(baseArgb, hsl)
        val h = hsl[0]
        val s = hsl[1]
        val l = hsl[2]

        val left = floatArrayOf((h - 150f + 360f) % 360f, s, l)
        val right = floatArrayOf((h + 150f) % 360f, s, l)

        return Color(ColorUtils.HSLToColor(left)) to Color(ColorUtils.HSLToColor(right))
    }
}