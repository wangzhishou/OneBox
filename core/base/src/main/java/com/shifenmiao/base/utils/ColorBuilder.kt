package com.shifenmiao.base.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.shifenmiao.theme.AppTheme
import com.t8rin.dynamic.theme.ColorTuple
import com.t8rin.dynamic.theme.calculateSecondaryColor
import com.t8rin.dynamic.theme.calculateSurfaceColor
import com.t8rin.dynamic.theme.calculateTertiaryColor
import com.t8rin.imagetoolbox.core.ui.theme.toColor

object ColorBuilder {

    @Composable
    fun getDefaultColorTuples(): List<ColorTuple> {
        val colorScheme = MaterialTheme.colorScheme
        return remember(colorScheme) {
            listOf(
                colorScheme.primary,
                colorScheme.secondary,
                colorScheme.tertiary
            ).map {
                ColorTuple(
                    primary = it,
                    secondary = it.calculateSecondaryColor().toColor(),
                    tertiary = it.calculateTertiaryColor().toColor(),
                    surface = it.calculateSurfaceColor().toColor()
                )
            }
        }
    }

    @Composable
    fun getDefaultContainerColorTuples(): List<ColorTuple> {
        val colorScheme = MaterialTheme.colorScheme
        return remember(colorScheme) {
            listOf(
                colorScheme.primaryContainer,
                colorScheme.secondaryContainer,
                colorScheme.tertiaryContainer
            ).map {
                ColorTuple(
                    primary = it,
                    secondary = it.calculateSecondaryColor().toColor(),
                    tertiary = it.calculateTertiaryColor().toColor(),
                    surface = it.calculateSurfaceColor().toColor()
                )
            }
        }
    }

    @Composable
    fun getColorById(id: Int): ColorTuple {
        val colorTuples = getDefaultColorTuples()
        val index = remember(id, colorTuples.size) { id % colorTuples.size }
        return colorTuples[index]
    }

    @Composable
    fun getColorContainerById(id: Int, recommend: Boolean?): ColorTuple {
        if (recommend == true) {
            if (id < 0) {
                return ColorTuple(
                    primary = MaterialTheme.colorScheme.surfaceContainerHighest,
                    secondary = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tertiary = MaterialTheme.colorScheme.surfaceContainerLow,
                    surface = MaterialTheme.colorScheme.surface
                )
            }
            val colorTuples = getDefaultContainerColorTuples()
            val index = remember(id, colorTuples.size) { id % colorTuples.size }
            return colorTuples[index]
        }
        return ColorTuple(
            primary = MaterialTheme.colorScheme.surfaceContainerHighest,
            secondary = MaterialTheme.colorScheme.surfaceContainerHigh,
            tertiary = MaterialTheme.colorScheme.surfaceContainerLow,
            surface = MaterialTheme.colorScheme.surface
        )

    }

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

    @Composable
    fun getProfileCircleColors(): List<Color> {
        return listOf(
            MaterialTheme.colorScheme.surfaceContainerLowest,
            MaterialTheme.colorScheme.surfaceContainerLow,
            MaterialTheme.colorScheme.surfaceBright,
        )
    }

    @Composable
    fun getProfileLinearGradientColors(): List<Color> {
        return listOf(
            MaterialTheme.colorScheme.inversePrimary,
            MaterialTheme.colorScheme.primaryContainer
        )
    }

    @Composable
    fun getAppLinearGradientColors(): List<Color> {
        return listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.surfaceContainerLowest
        )
    }

    @Composable
    fun getAppMaskColor(): Color {
        return AppTheme.colors.getContainerSurfaceColor()
    }
}