package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2382: ImageVector
    get() {
        val current = _ic2382
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2382",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M1 13 a1 1 0 1 1 0 -2 1 1 0 0 1 0 2Z m5.7 0 a.9 .9 0 1 1 0 -1.8 .9 .9 0 0 1 0 1.8Z m1.9 -.7 a.7 .7 0 1 0 1.4 0 .7 .7 0 0 0 -1.4 0Z m3 .7 a.6 .6 0 1 1 0 -1.2 .6 .6 0 0 1 0 1.2Z m1.6 -.5 a.5 .5 0 1 0 1 0 .5 .5 0 0 0 -1 0Z m2 3.5 a1 1 0 1 0 0 -2 1 1 0 0 0 0 2Z m-5.7 0 a.9 .9 0 1 0 0 -1.8 .9 .9 0 0 0 0 1.8Z m-1.9 -.7 a.7 .7 0 1 1 -1.4 0 .7 .7 0 0 1 1.4 0Z m-3 .7 a.6 .6 0 1 0 0 -1.2 .6 .6 0 0 0 0 1.2Z m-2.1 0 a.5 .5 0 1 0 0 -1 .5 .5 0 0 0 0 1Z m10.7 -.9 a.9 .9 0 1 1 -1.8 0 .9 .9 0 0 1 1.8 0Z M3 12.1 a.9 .9 0 1 0 1.8 0 .9 .9 0 0 0 -1.8 0Z m11.466 -7.345 a4.243 4.243 0 0 0 -.836 -3.122 A4.106 4.106 0 0 0 10.866 .028 c-1.888 -.243 -3.62 1.12 -3.86 3.039 a2.945 2.945 0 0 0 .58 2.164 A2.852 2.852 0 0 0 9.5 6.342 a.617 .617 0 0 0 .69 -.54 .624 .624 0 0 0 -.534 -.7 1.639 1.639 0 0 1 -1.098 -.638 1.695 1.695 0 0 1 -.333 -1.241 c.156 -1.234 1.27 -2.111 2.484 -1.955 .775 .1 1.467 .502 1.945 1.13 .48 .627 .69 1.409 .589 2.197 a4.544 4.544 0 0 1 -2.22 3.282 c-1.998 1.159 -4.994 .916 -8.662 -.702 a.613 .613 0 0 0 -.81 .326 .629 .629 0 0 0 .319 .823 c1.051 .463 2.014 .804 2.895 1.044 3.393 .922 5.567 .353 6.868 -.4 1.83 -1.059 2.665 -2.872 2.832 -4.213Z M2.626 2.249 c-.019 -.201 .154 -.374 .374 -.374 s.393 .173 .374 .374 l-.17 1.764 h-.409 l-.169 -1.764Z m.655 2.345 a.281 .281 0 1 1 -.562 0 .281 .281 0 0 1 .562 0Z
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
                // M 2.626 2.249
                moveTo(x = 2.626f, y = 2.249f)
                // c -0.019 -0.201 0.154 -0.374 0.374 -0.374
                curveToRelative(
                    dx1 = -0.019f,
                    dy1 = -0.201f,
                    dx2 = 0.154f,
                    dy2 = -0.374f,
                    dx3 = 0.374f,
                    dy3 = -0.374f,
                )
                // s 0.393 0.173 0.374 0.374
                reflectiveCurveToRelative(
                    dx1 = 0.393f,
                    dy1 = 0.173f,
                    dx2 = 0.374f,
                    dy2 = 0.374f,
                )
                // l -0.17 1.764
                lineToRelative(dx = -0.17f, dy = 1.764f)
                // h -0.409
                horizontalLineToRelative(dx = -0.409f)
                // l -0.169 -1.764z
                lineToRelative(dx = -0.169f, dy = -1.764f)
                close()
                // m 0.655 2.345
                moveToRelative(dx = 0.655f, dy = 2.345f)
                // a 0.281 0.281 0 1 1 -0.562 0
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.562f,
                    dy1 = 0.0f,
                )
                // a 0.281 0.281 0 0 1 0.562 0z
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.562f,
                    dy1 = 0.0f,
                )
                close()
            }
            // M2.83 .473 a.197 .197 0 0 1 .34 0 l2.804 4.86 a.195 .195 0 0 1 -.17 .292 H.196 a.195 .195 0 0 1 -.17 -.291 L2.83 .473Z m2.533 4.702 L3 1.078 .637 5.175 h4.726Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 2.83 0.473
                moveTo(x = 2.83f, y = 0.473f)
                // a 0.197 0.197 0 0 1 0.34 0
                arcToRelative(
                    a = 0.197f,
                    b = 0.197f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.34f,
                    dy1 = 0.0f,
                )
                // l 2.804 4.86
                lineToRelative(dx = 2.804f, dy = 4.86f)
                // a 0.195 0.195 0 0 1 -0.17 0.292
                arcToRelative(
                    a = 0.195f,
                    b = 0.195f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.17f,
                    dy1 = 0.292f,
                )
                // H 0.196
                horizontalLineTo(x = 0.196f)
                // a 0.195 0.195 0 0 1 -0.17 -0.291
                arcToRelative(
                    a = 0.195f,
                    b = 0.195f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.17f,
                    dy1 = -0.291f,
                )
                // L 2.83 0.473z
                lineTo(x = 2.83f, y = 0.473f)
                close()
                // m 2.533 4.702
                moveToRelative(dx = 2.533f, dy = 4.702f)
                // L 3 1.078
                lineTo(x = 3.0f, y = 1.078f)
                // L 0.637 5.175
                lineTo(x = 0.637f, y = 5.175f)
                // h 4.726z
                horizontalLineToRelative(dx = 4.726f)
                close()
            }
        }.build().also { _ic2382 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2382: ImageVector? = null
