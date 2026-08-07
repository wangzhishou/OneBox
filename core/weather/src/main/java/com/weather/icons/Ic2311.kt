package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2311: ImageVector
    get() {
        val current = _ic2311
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2311",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M1 13 a1 1 0 1 1 0 -2 1 1 0 0 1 0 2Z m5.7 0 a.9 .9 0 1 1 0 -1.8 .9 .9 0 0 1 0 1.8Z m1.9 -.7 a.7 .7 0 1 0 1.4 0 .7 .7 0 0 0 -1.4 0Z m3 .7 a.6 .6 0 1 1 0 -1.2 .6 .6 0 0 1 0 1.2Z m1.6 -.5 a.5 .5 0 1 0 1 0 .5 .5 0 0 0 -1 0Z m2 3.5 a1 1 0 1 0 0 -2 1 1 0 0 0 0 2Z m-5.7 0 a.9 .9 0 1 0 0 -1.8 .9 .9 0 0 0 0 1.8Z m-1.9 -.7 a.7 .7 0 1 1 -1.4 0 .7 .7 0 0 1 1.4 0Z m-3 .7 a.6 .6 0 1 0 0 -1.2 .6 .6 0 0 0 0 1.2Z m-2.1 0 a.5 .5 0 1 0 0 -1 .5 .5 0 0 0 0 1Z m10.7 -.9 a.9 .9 0 1 1 -1.8 0 .9 .9 0 0 1 1.8 0Z M3 12.1 a.9 .9 0 1 0 1.8 0 .9 .9 0 0 0 -1.8 0Z m11.466 -7.345 a4.243 4.243 0 0 0 -.836 -3.122 A4.106 4.106 0 0 0 10.866 .028 c-1.888 -.243 -3.62 1.12 -3.86 3.039 a2.945 2.945 0 0 0 .58 2.164 A2.852 2.852 0 0 0 9.5 6.342 a.617 .617 0 0 0 .69 -.54 .624 .624 0 0 0 -.534 -.7 1.639 1.639 0 0 1 -1.098 -.638 1.695 1.695 0 0 1 -.333 -1.241 c.156 -1.234 1.27 -2.111 2.484 -1.955 .775 .1 1.467 .502 1.945 1.13 .48 .627 .69 1.409 .589 2.197 a4.544 4.544 0 0 1 -2.22 3.282 c-1.998 1.159 -4.994 .916 -8.662 -.702 a.613 .613 0 0 0 -.81 .326 .629 .629 0 0 0 .319 .823 c1.051 .463 2.014 .804 2.895 1.044 3.393 .922 5.567 .353 6.868 -.4 1.83 -1.059 2.665 -2.872 2.832 -4.213Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 1 13
                moveTo(x = 1.0f, y = 13.0f)
                // a 1 1 0 1 1 0 -2
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -2.0f,
                )
                // a 1 1 0 0 1 0 2z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 2.0f,
                )
                close()
                // m 5.7 0
                moveToRelative(dx = 5.7f, dy = 0.0f)
                // a 0.9 0.9 0 1 1 0 -1.8
                arcToRelative(
                    a = 0.9f,
                    b = 0.9f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.8f,
                )
                // a 0.9 0.9 0 0 1 0 1.8z
                arcToRelative(
                    a = 0.9f,
                    b = 0.9f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.8f,
                )
                close()
                // m 1.9 -0.7
                moveToRelative(dx = 1.9f, dy = -0.7f)
                // a 0.7 0.7 0 1 0 1.4 0
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 1.4f,
                    dy1 = 0.0f,
                )
                // a 0.7 0.7 0 0 0 -1.4 0z
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.4f,
                    dy1 = 0.0f,
                )
                close()
                // m 3 0.7
                moveToRelative(dx = 3.0f, dy = 0.7f)
                // a 0.6 0.6 0 1 1 0 -1.2
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.2f,
                )
                // a 0.6 0.6 0 0 1 0 1.2z
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.2f,
                )
                close()
                // m 1.6 -0.5
                moveToRelative(dx = 1.6f, dy = -0.5f)
                // a 0.5 0.5 0 1 0 1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // a 0.5 0.5 0 0 0 -1 0z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                close()
                // m 2 3.5
                moveToRelative(dx = 2.0f, dy = 3.5f)
                // a 1 1 0 1 0 0 -2
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -2.0f,
                )
                // a 1 1 0 0 0 0 2z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 2.0f,
                )
                close()
                // m -5.7 0
                moveToRelative(dx = -5.7f, dy = 0.0f)
                // a 0.9 0.9 0 1 0 0 -1.8
                arcToRelative(
                    a = 0.9f,
                    b = 0.9f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.8f,
                )
                // a 0.9 0.9 0 0 0 0 1.8z
                arcToRelative(
                    a = 0.9f,
                    b = 0.9f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.8f,
                )
                close()
                // m -1.9 -0.7
                moveToRelative(dx = -1.9f, dy = -0.7f)
                // a 0.7 0.7 0 1 1 -1.4 0
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.4f,
                    dy1 = 0.0f,
                )
                // a 0.7 0.7 0 0 1 1.4 0z
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.4f,
                    dy1 = 0.0f,
                )
                close()
                // m -3 0.7
                moveToRelative(dx = -3.0f, dy = 0.7f)
                // a 0.6 0.6 0 1 0 0 -1.2
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.2f,
                )
                // a 0.6 0.6 0 0 0 0 1.2z
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.2f,
                )
                close()
                // m -2.1 0
                moveToRelative(dx = -2.1f, dy = 0.0f)
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
                // m 10.7 -0.9
                moveToRelative(dx = 10.7f, dy = -0.9f)
                // a 0.9 0.9 0 1 1 -1.8 0
                arcToRelative(
                    a = 0.9f,
                    b = 0.9f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.8f,
                    dy1 = 0.0f,
                )
                // a 0.9 0.9 0 0 1 1.8 0z
                arcToRelative(
                    a = 0.9f,
                    b = 0.9f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.8f,
                    dy1 = 0.0f,
                )
                close()
                // M 3 12.1
                moveTo(x = 3.0f, y = 12.1f)
                // a 0.9 0.9 0 1 0 1.8 0
                arcToRelative(
                    a = 0.9f,
                    b = 0.9f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 1.8f,
                    dy1 = 0.0f,
                )
                // a 0.9 0.9 0 0 0 -1.8 0z
                arcToRelative(
                    a = 0.9f,
                    b = 0.9f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.8f,
                    dy1 = 0.0f,
                )
                close()
                // m 11.466 -7.345
                moveToRelative(dx = 11.466f, dy = -7.345f)
                // a 4.243 4.243 0 0 0 -0.836 -3.122
                arcToRelative(
                    a = 4.243f,
                    b = 4.243f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.836f,
                    dy1 = -3.122f,
                )
                // A 4.106 4.106 0 0 0 10.866 0.028
                arcTo(
                    horizontalEllipseRadius = 4.106f,
                    verticalEllipseRadius = 4.106f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 10.866f,
                    y1 = 0.028f,
                )
                // c -1.888 -0.243 -3.62 1.12 -3.86 3.039
                curveToRelative(
                    dx1 = -1.888f,
                    dy1 = -0.243f,
                    dx2 = -3.62f,
                    dy2 = 1.12f,
                    dx3 = -3.86f,
                    dy3 = 3.039f,
                )
                // a 2.945 2.945 0 0 0 0.58 2.164
                arcToRelative(
                    a = 2.945f,
                    b = 2.945f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.58f,
                    dy1 = 2.164f,
                )
                // A 2.852 2.852 0 0 0 9.5 6.342
                arcTo(
                    horizontalEllipseRadius = 2.852f,
                    verticalEllipseRadius = 2.852f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 9.5f,
                    y1 = 6.342f,
                )
                // a 0.617 0.617 0 0 0 0.69 -0.54
                arcToRelative(
                    a = 0.617f,
                    b = 0.617f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.69f,
                    dy1 = -0.54f,
                )
                // a 0.624 0.624 0 0 0 -0.534 -0.7
                arcToRelative(
                    a = 0.624f,
                    b = 0.624f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.534f,
                    dy1 = -0.7f,
                )
                // a 1.639 1.639 0 0 1 -1.098 -0.638
                arcToRelative(
                    a = 1.639f,
                    b = 1.639f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.098f,
                    dy1 = -0.638f,
                )
                // a 1.695 1.695 0 0 1 -0.333 -1.241
                arcToRelative(
                    a = 1.695f,
                    b = 1.695f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.333f,
                    dy1 = -1.241f,
                )
                // c 0.156 -1.234 1.27 -2.111 2.484 -1.955
                curveToRelative(
                    dx1 = 0.156f,
                    dy1 = -1.234f,
                    dx2 = 1.27f,
                    dy2 = -2.111f,
                    dx3 = 2.484f,
                    dy3 = -1.955f,
                )
                // c 0.775 0.1 1.467 0.502 1.945 1.13
                curveToRelative(
                    dx1 = 0.775f,
                    dy1 = 0.1f,
                    dx2 = 1.467f,
                    dy2 = 0.502f,
                    dx3 = 1.945f,
                    dy3 = 1.13f,
                )
                // c 0.48 0.627 0.69 1.409 0.589 2.197
                curveToRelative(
                    dx1 = 0.48f,
                    dy1 = 0.627f,
                    dx2 = 0.69f,
                    dy2 = 1.409f,
                    dx3 = 0.589f,
                    dy3 = 2.197f,
                )
                // a 4.544 4.544 0 0 1 -2.22 3.282
                arcToRelative(
                    a = 4.544f,
                    b = 4.544f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.22f,
                    dy1 = 3.282f,
                )
                // c -1.998 1.159 -4.994 0.916 -8.662 -0.702
                curveToRelative(
                    dx1 = -1.998f,
                    dy1 = 1.159f,
                    dx2 = -4.994f,
                    dy2 = 0.916f,
                    dx3 = -8.662f,
                    dy3 = -0.702f,
                )
                // a 0.613 0.613 0 0 0 -0.81 0.326
                arcToRelative(
                    a = 0.613f,
                    b = 0.613f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.81f,
                    dy1 = 0.326f,
                )
                // a 0.629 0.629 0 0 0 0.319 0.823
                arcToRelative(
                    a = 0.629f,
                    b = 0.629f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.319f,
                    dy1 = 0.823f,
                )
                // c 1.051 0.463 2.014 0.804 2.895 1.044
                curveToRelative(
                    dx1 = 1.051f,
                    dy1 = 0.463f,
                    dx2 = 2.014f,
                    dy2 = 0.804f,
                    dx3 = 2.895f,
                    dy3 = 1.044f,
                )
                // c 3.393 0.922 5.567 0.353 6.868 -0.4
                curveToRelative(
                    dx1 = 3.393f,
                    dy1 = 0.922f,
                    dx2 = 5.567f,
                    dy2 = 0.353f,
                    dx3 = 6.868f,
                    dy3 = -0.4f,
                )
                // c 1.83 -1.059 2.665 -2.872 2.832 -4.213z
                curveToRelative(
                    dx1 = 1.83f,
                    dy1 = -1.059f,
                    dx2 = 2.665f,
                    dy2 = -2.872f,
                    dx3 = 2.832f,
                    dy3 = -4.213f,
                )
                close()
            }
            // M3.17 .473 a.197 .197 0 0 0 -.34 0 L.026 5.333 c-.075 .13 .02 .292 .17 .292 h5.607 a.194 .194 0 0 0 .17 -.291 L3.17 .473Z m-.637 1.608 c-.024 -.212 .192 -.393 .467 -.393 s.491 .181 .467 .393 l-.211 1.857 h-.512 l-.21 -1.857Z m.845 2.607 a.375 .375 0 1 1 -.75 0 .375 .375 0 0 1 .75 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 3.17 0.473
                moveTo(x = 3.17f, y = 0.473f)
                // a 0.197 0.197 0 0 0 -0.34 0
                arcToRelative(
                    a = 0.197f,
                    b = 0.197f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.34f,
                    dy1 = 0.0f,
                )
                // L 0.026 5.333
                lineTo(x = 0.026f, y = 5.333f)
                // c -0.075 0.13 0.02 0.292 0.17 0.292
                curveToRelative(
                    dx1 = -0.075f,
                    dy1 = 0.13f,
                    dx2 = 0.02f,
                    dy2 = 0.292f,
                    dx3 = 0.17f,
                    dy3 = 0.292f,
                )
                // h 5.607
                horizontalLineToRelative(dx = 5.607f)
                // a 0.194 0.194 0 0 0 0.17 -0.291
                arcToRelative(
                    a = 0.194f,
                    b = 0.194f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.17f,
                    dy1 = -0.291f,
                )
                // L 3.17 0.473z
                lineTo(x = 3.17f, y = 0.473f)
                close()
                // m -0.637 1.608
                moveToRelative(dx = -0.637f, dy = 1.608f)
                // c -0.024 -0.212 0.192 -0.393 0.467 -0.393
                curveToRelative(
                    dx1 = -0.024f,
                    dy1 = -0.212f,
                    dx2 = 0.192f,
                    dy2 = -0.393f,
                    dx3 = 0.467f,
                    dy3 = -0.393f,
                )
                // s 0.491 0.181 0.467 0.393
                reflectiveCurveToRelative(
                    dx1 = 0.491f,
                    dy1 = 0.181f,
                    dx2 = 0.467f,
                    dy2 = 0.393f,
                )
                // l -0.211 1.857
                lineToRelative(dx = -0.211f, dy = 1.857f)
                // h -0.512
                horizontalLineToRelative(dx = -0.512f)
                // l -0.21 -1.857z
                lineToRelative(dx = -0.21f, dy = -1.857f)
                close()
                // m 0.845 2.607
                moveToRelative(dx = 0.845f, dy = 2.607f)
                // a 0.375 0.375 0 1 1 -0.75 0
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.75f,
                    dy1 = 0.0f,
                )
                // a 0.375 0.375 0 0 1 0.75 0z
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.75f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic2311 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2311: ImageVector? = null
