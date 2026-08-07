package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2501: ImageVector
    get() {
        val current = _ic2501
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2501",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.9 10 a4.99 4.99 0 0 0 3.827 -1.783 3 3 0 1 0 .553 -5.63 A4.999 4.999 0 0 0 7.9 0 a4.998 4.998 0 0 0 -4.359 2.549 3 3 0 1 0 .586 5.732 A4.988 4.988 0 0 0 7.9 10Z m.495 -5.467 c-.024 .043 .011 .093 .065 .093 h1.468 c.06 0 .094 .065 .055 .107 L7.005 7.976 c-.052 .055 -.151 .006 -.124 -.063 L7.62 5.97 c.015 -.043 -.019 -.087 -.069 -.087 h-1.42 c-.096 0 -.16 -.09 -.118 -.17 L7.41 3.078 a.159 .159 0 0 1 .058 -.057 .147 .147 0 0 1 .076 -.02 h1.578 c.054 0 .089 .051 .065 .094 l-.792 1.439Z m-5.609 6.53 c.205 .13 .275 .416 .157 .64 L.8 15.766 c-.119 .225 -.38 .302 -.586 .172 a.494 .494 0 0 1 -.157 -.64 L2.2 11.234 c.119 -.225 .38 -.302 .586 -.172Z m12 0 c.205 .13 .275 .416 .157 .64 L12.8 15.766 c-.119 .225 -.38 .302 -.586 .172 a.494 .494 0 0 1 -.157 -.64 l2.143 -4.062 c.119 -.225 .38 -.302 .586 -.172Z m-5.843 .641 a.494 .494 0 0 0 -.157 -.641 c-.205 -.13 -.467 -.053 -.586 .172 l-2.143 4.061 a.494 .494 0 0 0 .157 .641 c.205 .13 .467 .053 .586 -.172 l2.143 -4.061Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.9 10
                moveTo(x = 7.9f, y = 10.0f)
                // a 4.99 4.99 0 0 0 3.827 -1.783
                arcToRelative(
                    a = 4.99f,
                    b = 4.99f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 3.827f,
                    dy1 = -1.783f,
                )
                // a 3 3 0 1 0 0.553 -5.63
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.553f,
                    dy1 = -5.63f,
                )
                // A 4.999 4.999 0 0 0 7.9 0
                arcTo(
                    horizontalEllipseRadius = 4.999f,
                    verticalEllipseRadius = 4.999f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.9f,
                    y1 = 0.0f,
                )
                // a 4.998 4.998 0 0 0 -4.359 2.549
                arcToRelative(
                    a = 4.998f,
                    b = 4.998f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -4.359f,
                    dy1 = 2.549f,
                )
                // a 3 3 0 1 0 0.586 5.732
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.586f,
                    dy1 = 5.732f,
                )
                // A 4.988 4.988 0 0 0 7.9 10z
                arcTo(
                    horizontalEllipseRadius = 4.988f,
                    verticalEllipseRadius = 4.988f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.9f,
                    y1 = 10.0f,
                )
                close()
                // m 0.495 -5.467
                moveToRelative(dx = 0.495f, dy = -5.467f)
                // c -0.024 0.043 0.011 0.093 0.065 0.093
                curveToRelative(
                    dx1 = -0.024f,
                    dy1 = 0.043f,
                    dx2 = 0.011f,
                    dy2 = 0.093f,
                    dx3 = 0.065f,
                    dy3 = 0.093f,
                )
                // h 1.468
                horizontalLineToRelative(dx = 1.468f)
                // c 0.06 0 0.094 0.065 0.055 0.107
                curveToRelative(
                    dx1 = 0.06f,
                    dy1 = 0.0f,
                    dx2 = 0.094f,
                    dy2 = 0.065f,
                    dx3 = 0.055f,
                    dy3 = 0.107f,
                )
                // L 7.005 7.976
                lineTo(x = 7.005f, y = 7.976f)
                // c -0.052 0.055 -0.151 0.006 -0.124 -0.063
                curveToRelative(
                    dx1 = -0.052f,
                    dy1 = 0.055f,
                    dx2 = -0.151f,
                    dy2 = 0.006f,
                    dx3 = -0.124f,
                    dy3 = -0.063f,
                )
                // L 7.62 5.97
                lineTo(x = 7.62f, y = 5.97f)
                // c 0.015 -0.043 -0.019 -0.087 -0.069 -0.087
                curveToRelative(
                    dx1 = 0.015f,
                    dy1 = -0.043f,
                    dx2 = -0.019f,
                    dy2 = -0.087f,
                    dx3 = -0.069f,
                    dy3 = -0.087f,
                )
                // h -1.42
                horizontalLineToRelative(dx = -1.42f)
                // c -0.096 0 -0.16 -0.09 -0.118 -0.17
                curveToRelative(
                    dx1 = -0.096f,
                    dy1 = 0.0f,
                    dx2 = -0.16f,
                    dy2 = -0.09f,
                    dx3 = -0.118f,
                    dy3 = -0.17f,
                )
                // L 7.41 3.078
                lineTo(x = 7.41f, y = 3.078f)
                // a 0.159 0.159 0 0 1 0.058 -0.057
                arcToRelative(
                    a = 0.159f,
                    b = 0.159f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.058f,
                    dy1 = -0.057f,
                )
                // a 0.147 0.147 0 0 1 0.076 -0.02
                arcToRelative(
                    a = 0.147f,
                    b = 0.147f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.076f,
                    dy1 = -0.02f,
                )
                // h 1.578
                horizontalLineToRelative(dx = 1.578f)
                // c 0.054 0 0.089 0.051 0.065 0.094
                curveToRelative(
                    dx1 = 0.054f,
                    dy1 = 0.0f,
                    dx2 = 0.089f,
                    dy2 = 0.051f,
                    dx3 = 0.065f,
                    dy3 = 0.094f,
                )
                // l -0.792 1.439z
                lineToRelative(dx = -0.792f, dy = 1.439f)
                close()
                // m -5.609 6.53
                moveToRelative(dx = -5.609f, dy = 6.53f)
                // c 0.205 0.13 0.275 0.416 0.157 0.64
                curveToRelative(
                    dx1 = 0.205f,
                    dy1 = 0.13f,
                    dx2 = 0.275f,
                    dy2 = 0.416f,
                    dx3 = 0.157f,
                    dy3 = 0.64f,
                )
                // L 0.8 15.766
                lineTo(x = 0.8f, y = 15.766f)
                // c -0.119 0.225 -0.38 0.302 -0.586 0.172
                curveToRelative(
                    dx1 = -0.119f,
                    dy1 = 0.225f,
                    dx2 = -0.38f,
                    dy2 = 0.302f,
                    dx3 = -0.586f,
                    dy3 = 0.172f,
                )
                // a 0.494 0.494 0 0 1 -0.157 -0.64
                arcToRelative(
                    a = 0.494f,
                    b = 0.494f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.157f,
                    dy1 = -0.64f,
                )
                // L 2.2 11.234
                lineTo(x = 2.2f, y = 11.234f)
                // c 0.119 -0.225 0.38 -0.302 0.586 -0.172z
                curveToRelative(
                    dx1 = 0.119f,
                    dy1 = -0.225f,
                    dx2 = 0.38f,
                    dy2 = -0.302f,
                    dx3 = 0.586f,
                    dy3 = -0.172f,
                )
                close()
                // m 12 0
                moveToRelative(dx = 12.0f, dy = 0.0f)
                // c 0.205 0.13 0.275 0.416 0.157 0.64
                curveToRelative(
                    dx1 = 0.205f,
                    dy1 = 0.13f,
                    dx2 = 0.275f,
                    dy2 = 0.416f,
                    dx3 = 0.157f,
                    dy3 = 0.64f,
                )
                // L 12.8 15.766
                lineTo(x = 12.8f, y = 15.766f)
                // c -0.119 0.225 -0.38 0.302 -0.586 0.172
                curveToRelative(
                    dx1 = -0.119f,
                    dy1 = 0.225f,
                    dx2 = -0.38f,
                    dy2 = 0.302f,
                    dx3 = -0.586f,
                    dy3 = 0.172f,
                )
                // a 0.494 0.494 0 0 1 -0.157 -0.64
                arcToRelative(
                    a = 0.494f,
                    b = 0.494f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.157f,
                    dy1 = -0.64f,
                )
                // l 2.143 -4.062
                lineToRelative(dx = 2.143f, dy = -4.062f)
                // c 0.119 -0.225 0.38 -0.302 0.586 -0.172z
                curveToRelative(
                    dx1 = 0.119f,
                    dy1 = -0.225f,
                    dx2 = 0.38f,
                    dy2 = -0.302f,
                    dx3 = 0.586f,
                    dy3 = -0.172f,
                )
                close()
                // m -5.843 0.641
                moveToRelative(dx = -5.843f, dy = 0.641f)
                // a 0.494 0.494 0 0 0 -0.157 -0.641
                arcToRelative(
                    a = 0.494f,
                    b = 0.494f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.157f,
                    dy1 = -0.641f,
                )
                // c -0.205 -0.13 -0.467 -0.053 -0.586 0.172
                curveToRelative(
                    dx1 = -0.205f,
                    dy1 = -0.13f,
                    dx2 = -0.467f,
                    dy2 = -0.053f,
                    dx3 = -0.586f,
                    dy3 = 0.172f,
                )
                // l -2.143 4.061
                lineToRelative(dx = -2.143f, dy = 4.061f)
                // a 0.494 0.494 0 0 0 0.157 0.641
                arcToRelative(
                    a = 0.494f,
                    b = 0.494f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.157f,
                    dy1 = 0.641f,
                )
                // c 0.205 0.13 0.467 0.053 0.586 -0.172
                curveToRelative(
                    dx1 = 0.205f,
                    dy1 = 0.13f,
                    dx2 = 0.467f,
                    dy2 = 0.053f,
                    dx3 = 0.586f,
                    dy3 = -0.172f,
                )
                // l 2.143 -4.061z
                lineToRelative(dx = 2.143f, dy = -4.061f)
                close()
            }
        }.build().also { _ic2501 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2501: ImageVector? = null
