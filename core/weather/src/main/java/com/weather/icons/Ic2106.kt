package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2106: ImageVector
    get() {
        val current = _ic2106
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2106",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M5 .5 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z M2.5 3 a.5 .5 0 1 0 0 -1 .5 .5 0 0 0 0 1Z m-2 2 a.5 .5 0 1 0 0 -1 .5 .5 0 0 0 0 1Z m5 -2 a.5 .5 0 1 0 0 -1 .5 .5 0 0 0 0 1Z m5.5 -.5 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z M8 2 a.5 .5 0 1 0 0 -1 .5 .5 0 0 0 0 1Z m4 -1.5 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z M13.5 3 a.5 .5 0 1 0 0 -1 .5 .5 0 0 0 0 1Z M16 4.5 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z M.5 11 a.5 .5 0 1 1 0 1 .5 .5 0 0 1 0 -1Z M3 13.5 a.5 .5 0 1 0 -1 0 .5 .5 0 0 0 1 0Z M4.5 15 a.5 .5 0 1 1 0 1 .5 .5 0 0 1 0 -1Z M6 13.5 a.5 .5 0 1 0 -1 0 .5 .5 0 0 0 1 0Z m4.5 -.5 a.5 .5 0 1 1 0 1 .5 .5 0 0 1 0 -1Z m-2 1.5 a.5 .5 0 1 0 -1 0 .5 .5 0 0 0 1 0Z m3 .5 a.5 .5 0 1 1 0 1 .5 .5 0 0 1 0 -1Z m2.5 -1.5 a.5 .5 0 1 0 -1 0 .5 .5 0 0 0 1 0Z m1.5 -2.5 a.5 .5 0 1 1 0 1 .5 .5 0 0 1 0 -1Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5 0.5
                moveTo(x = 5.0f, y = 0.5f)
                // a 0.5 0.5 0 1 1 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // a 0.5 0.5 0 0 1 1 0z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                close()
                // M 2.5 3
                moveTo(x = 2.5f, y = 3.0f)
                // a 0.5 0.5 0 1 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // a 0.5 0.5 0 0 0 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
                // m -2 2
                moveToRelative(dx = -2.0f, dy = 2.0f)
                // a 0.5 0.5 0 1 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // a 0.5 0.5 0 0 0 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
                // m 5 -2
                moveToRelative(dx = 5.0f, dy = -2.0f)
                // a 0.5 0.5 0 1 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // a 0.5 0.5 0 0 0 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
                // m 5.5 -0.5
                moveToRelative(dx = 5.5f, dy = -0.5f)
                // a 0.5 0.5 0 1 1 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // a 0.5 0.5 0 0 1 1 0z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                close()
                // M 8 2
                moveTo(x = 8.0f, y = 2.0f)
                // a 0.5 0.5 0 1 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // a 0.5 0.5 0 0 0 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
                // m 4 -1.5
                moveToRelative(dx = 4.0f, dy = -1.5f)
                // a 0.5 0.5 0 1 1 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // a 0.5 0.5 0 0 1 1 0z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                close()
                // M 13.5 3
                moveTo(x = 13.5f, y = 3.0f)
                // a 0.5 0.5 0 1 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // a 0.5 0.5 0 0 0 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
                // M 16 4.5
                moveTo(x = 16.0f, y = 4.5f)
                // a 0.5 0.5 0 1 1 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // a 0.5 0.5 0 0 1 1 0z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                close()
                // M 0.5 11
                moveTo(x = 0.5f, y = 11.0f)
                // a 0.5 0.5 0 1 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // a 0.5 0.5 0 0 1 0 -1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                close()
                // M 3 13.5
                moveTo(x = 3.0f, y = 13.5f)
                // a 0.5 0.5 0 1 0 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // a 0.5 0.5 0 0 0 1 0z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                close()
                // M 4.5 15
                moveTo(x = 4.5f, y = 15.0f)
                // a 0.5 0.5 0 1 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // a 0.5 0.5 0 0 1 0 -1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                close()
                // M 6 13.5
                moveTo(x = 6.0f, y = 13.5f)
                // a 0.5 0.5 0 1 0 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // a 0.5 0.5 0 0 0 1 0z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                close()
                // m 4.5 -0.5
                moveToRelative(dx = 4.5f, dy = -0.5f)
                // a 0.5 0.5 0 1 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // a 0.5 0.5 0 0 1 0 -1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                close()
                // m -2 1.5
                moveToRelative(dx = -2.0f, dy = 1.5f)
                // a 0.5 0.5 0 1 0 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // a 0.5 0.5 0 0 0 1 0z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                close()
                // m 3 0.5
                moveToRelative(dx = 3.0f, dy = 0.5f)
                // a 0.5 0.5 0 1 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // a 0.5 0.5 0 0 1 0 -1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                close()
                // m 2.5 -1.5
                moveToRelative(dx = 2.5f, dy = -1.5f)
                // a 0.5 0.5 0 1 0 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // a 0.5 0.5 0 0 0 1 0z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                close()
                // m 1.5 -2.5
                moveToRelative(dx = 1.5f, dy = -2.5f)
                // a 0.5 0.5 0 1 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // a 0.5 0.5 0 0 1 0 -1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                close()
            }
            // M7.9 13 a4.99 4.99 0 0 0 3.827 -1.783 3 3 0 1 0 .553 -5.63 A4.999 4.999 0 0 0 7.9 3 a4.998 4.998 0 0 0 -4.359 2.549 3 3 0 1 0 .586 5.732 A4.988 4.988 0 0 0 7.9 13Z m.495 -5.467 c-.024 .043 .011 .093 .065 .093 h1.468 c.06 0 .094 .065 .055 .107 l-2.978 3.243 c-.052 .055 -.151 .006 -.124 -.063 L7.62 8.97 c.015 -.043 -.019 -.087 -.069 -.087 h-1.42 c-.096 0 -.16 -.09 -.118 -.17 L7.41 6.078 a.159 .159 0 0 1 .058 -.057 .147 .147 0 0 1 .076 -.02 h1.578 c.054 0 .089 .051 .065 .094 l-.792 1.439Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.9 13
                moveTo(x = 7.9f, y = 13.0f)
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
                // A 4.999 4.999 0 0 0 7.9 3
                arcTo(
                    horizontalEllipseRadius = 4.999f,
                    verticalEllipseRadius = 4.999f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.9f,
                    y1 = 3.0f,
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
                // A 4.988 4.988 0 0 0 7.9 13z
                arcTo(
                    horizontalEllipseRadius = 4.988f,
                    verticalEllipseRadius = 4.988f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.9f,
                    y1 = 13.0f,
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
                // l -2.978 3.243
                lineToRelative(dx = -2.978f, dy = 3.243f)
                // c -0.052 0.055 -0.151 0.006 -0.124 -0.063
                curveToRelative(
                    dx1 = -0.052f,
                    dy1 = 0.055f,
                    dx2 = -0.151f,
                    dy2 = 0.006f,
                    dx3 = -0.124f,
                    dy3 = -0.063f,
                )
                // L 7.62 8.97
                lineTo(x = 7.62f, y = 8.97f)
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
                // L 7.41 6.078
                lineTo(x = 7.41f, y = 6.078f)
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
            }
        }.build().also { _ic2106 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2106: ImageVector? = null
