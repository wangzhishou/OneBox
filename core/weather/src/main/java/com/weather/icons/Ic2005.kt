package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2005: ImageVector
    get() {
        val current = _ic2005
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2005",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M10.476 16 a.785 .785 0 0 1 -.667 -.372 .749 .749 0 0 1 -.029 -.741 c.58 -1.146 1.028 -2.673 .473 -3.344 -1.782 .238 -2.987 -.086 -3.588 -.957 a2.272 2.272 0 0 1 -.385 -1.261 c-1.243 1.752 -1.006 4.007 -.518 5.697 a.752 .752 0 0 1 -.124 .667 .802 .802 0 0 1 -1.06 .18 c-.125 -.082 -3.078 -2.046 -3.078 -5.14 0 -2.505 1.5 -3.892 2.825 -5.12 C5.742 4.291 6.968 3.154 6.968 .768 a.766 .766 0 0 1 .445 -.696 .783 .783 0 0 1 .827 .106 c3.056 2.53 6.25 6.876 6.051 10.428 -.116 2.091 -1.242 3.864 -3.384 5.263 a.773 .773 0 0 1 -.43 .131Z M7.792 .858 c-.037 2.672 -1.495 4.023 -2.9 5.32 -1.325 1.248 -2.568 2.415 -2.568 4.55 0 2.366 2.071 4.003 2.597 4.384 -.596 -2.153 -.799 -5.095 1.48 -7.15 l.231 -.188 a.418 .418 0 0 1 .493 -.049 .406 .406 0 0 1 .17 .458 l-.083 .299 a1.956 1.956 0 0 0 .116 1.662 c.414 .614 1.417 .818 2.945 .585 h.046 c.12 -.017 .24 .02 .331 .098 l.034 .024 c1.217 1.065 .413 3.217 -.1 4.257 1.802 -1.228 2.767 -2.755 2.866 -4.502 .195 -3.286 -2.783 -7.334 -5.658 -9.748Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.476 16
                moveTo(x = 10.476f, y = 16.0f)
                // a 0.785 0.785 0 0 1 -0.667 -0.372
                arcToRelative(
                    a = 0.785f,
                    b = 0.785f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.667f,
                    dy1 = -0.372f,
                )
                // a 0.749 0.749 0 0 1 -0.029 -0.741
                arcToRelative(
                    a = 0.749f,
                    b = 0.749f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.029f,
                    dy1 = -0.741f,
                )
                // c 0.58 -1.146 1.028 -2.673 0.473 -3.344
                curveToRelative(
                    dx1 = 0.58f,
                    dy1 = -1.146f,
                    dx2 = 1.028f,
                    dy2 = -2.673f,
                    dx3 = 0.473f,
                    dy3 = -3.344f,
                )
                // c -1.782 0.238 -2.987 -0.086 -3.588 -0.957
                curveToRelative(
                    dx1 = -1.782f,
                    dy1 = 0.238f,
                    dx2 = -2.987f,
                    dy2 = -0.086f,
                    dx3 = -3.588f,
                    dy3 = -0.957f,
                )
                // a 2.272 2.272 0 0 1 -0.385 -1.261
                arcToRelative(
                    a = 2.272f,
                    b = 2.272f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.385f,
                    dy1 = -1.261f,
                )
                // c -1.243 1.752 -1.006 4.007 -0.518 5.697
                curveToRelative(
                    dx1 = -1.243f,
                    dy1 = 1.752f,
                    dx2 = -1.006f,
                    dy2 = 4.007f,
                    dx3 = -0.518f,
                    dy3 = 5.697f,
                )
                // a 0.752 0.752 0 0 1 -0.124 0.667
                arcToRelative(
                    a = 0.752f,
                    b = 0.752f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.124f,
                    dy1 = 0.667f,
                )
                // a 0.802 0.802 0 0 1 -1.06 0.18
                arcToRelative(
                    a = 0.802f,
                    b = 0.802f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.06f,
                    dy1 = 0.18f,
                )
                // c -0.125 -0.082 -3.078 -2.046 -3.078 -5.14
                curveToRelative(
                    dx1 = -0.125f,
                    dy1 = -0.082f,
                    dx2 = -3.078f,
                    dy2 = -2.046f,
                    dx3 = -3.078f,
                    dy3 = -5.14f,
                )
                // c 0 -2.505 1.5 -3.892 2.825 -5.12
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -2.505f,
                    dx2 = 1.5f,
                    dy2 = -3.892f,
                    dx3 = 2.825f,
                    dy3 = -5.12f,
                )
                // C 5.742 4.291 6.968 3.154 6.968 0.768
                curveTo(
                    x1 = 5.742f,
                    y1 = 4.291f,
                    x2 = 6.968f,
                    y2 = 3.154f,
                    x3 = 6.968f,
                    y3 = 0.768f,
                )
                // a 0.766 0.766 0 0 1 0.445 -0.696
                arcToRelative(
                    a = 0.766f,
                    b = 0.766f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.445f,
                    dy1 = -0.696f,
                )
                // a 0.783 0.783 0 0 1 0.827 0.106
                arcToRelative(
                    a = 0.783f,
                    b = 0.783f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.827f,
                    dy1 = 0.106f,
                )
                // c 3.056 2.53 6.25 6.876 6.051 10.428
                curveToRelative(
                    dx1 = 3.056f,
                    dy1 = 2.53f,
                    dx2 = 6.25f,
                    dy2 = 6.876f,
                    dx3 = 6.051f,
                    dy3 = 10.428f,
                )
                // c -0.116 2.091 -1.242 3.864 -3.384 5.263
                curveToRelative(
                    dx1 = -0.116f,
                    dy1 = 2.091f,
                    dx2 = -1.242f,
                    dy2 = 3.864f,
                    dx3 = -3.384f,
                    dy3 = 5.263f,
                )
                // a 0.773 0.773 0 0 1 -0.43 0.131z
                arcToRelative(
                    a = 0.773f,
                    b = 0.773f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.43f,
                    dy1 = 0.131f,
                )
                close()
                // M 7.792 0.858
                moveTo(x = 7.792f, y = 0.858f)
                // c -0.037 2.672 -1.495 4.023 -2.9 5.32
                curveToRelative(
                    dx1 = -0.037f,
                    dy1 = 2.672f,
                    dx2 = -1.495f,
                    dy2 = 4.023f,
                    dx3 = -2.9f,
                    dy3 = 5.32f,
                )
                // c -1.325 1.248 -2.568 2.415 -2.568 4.55
                curveToRelative(
                    dx1 = -1.325f,
                    dy1 = 1.248f,
                    dx2 = -2.568f,
                    dy2 = 2.415f,
                    dx3 = -2.568f,
                    dy3 = 4.55f,
                )
                // c 0 2.366 2.071 4.003 2.597 4.384
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 2.366f,
                    dx2 = 2.071f,
                    dy2 = 4.003f,
                    dx3 = 2.597f,
                    dy3 = 4.384f,
                )
                // c -0.596 -2.153 -0.799 -5.095 1.48 -7.15
                curveToRelative(
                    dx1 = -0.596f,
                    dy1 = -2.153f,
                    dx2 = -0.799f,
                    dy2 = -5.095f,
                    dx3 = 1.48f,
                    dy3 = -7.15f,
                )
                // l 0.231 -0.188
                lineToRelative(dx = 0.231f, dy = -0.188f)
                // a 0.418 0.418 0 0 1 0.493 -0.049
                arcToRelative(
                    a = 0.418f,
                    b = 0.418f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.493f,
                    dy1 = -0.049f,
                )
                // a 0.406 0.406 0 0 1 0.17 0.458
                arcToRelative(
                    a = 0.406f,
                    b = 0.406f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.17f,
                    dy1 = 0.458f,
                )
                // l -0.083 0.299
                lineToRelative(dx = -0.083f, dy = 0.299f)
                // a 1.956 1.956 0 0 0 0.116 1.662
                arcToRelative(
                    a = 1.956f,
                    b = 1.956f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.116f,
                    dy1 = 1.662f,
                )
                // c 0.414 0.614 1.417 0.818 2.945 0.585
                curveToRelative(
                    dx1 = 0.414f,
                    dy1 = 0.614f,
                    dx2 = 1.417f,
                    dy2 = 0.818f,
                    dx3 = 2.945f,
                    dy3 = 0.585f,
                )
                // h 0.046
                horizontalLineToRelative(dx = 0.046f)
                // c 0.12 -0.017 0.24 0.02 0.331 0.098
                curveToRelative(
                    dx1 = 0.12f,
                    dy1 = -0.017f,
                    dx2 = 0.24f,
                    dy2 = 0.02f,
                    dx3 = 0.331f,
                    dy3 = 0.098f,
                )
                // l 0.034 0.024
                lineToRelative(dx = 0.034f, dy = 0.024f)
                // c 1.217 1.065 0.413 3.217 -0.1 4.257
                curveToRelative(
                    dx1 = 1.217f,
                    dy1 = 1.065f,
                    dx2 = 0.413f,
                    dy2 = 3.217f,
                    dx3 = -0.1f,
                    dy3 = 4.257f,
                )
                // c 1.802 -1.228 2.767 -2.755 2.866 -4.502
                curveToRelative(
                    dx1 = 1.802f,
                    dy1 = -1.228f,
                    dx2 = 2.767f,
                    dy2 = -2.755f,
                    dx3 = 2.866f,
                    dy3 = -4.502f,
                )
                // c 0.195 -3.286 -2.783 -7.334 -5.658 -9.748z
                curveToRelative(
                    dx1 = 0.195f,
                    dy1 = -3.286f,
                    dx2 = -2.783f,
                    dy2 = -7.334f,
                    dx3 = -5.658f,
                    dy3 = -9.748f,
                )
                close()
            }
        }.build().also { _ic2005 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2005: ImageVector? = null
