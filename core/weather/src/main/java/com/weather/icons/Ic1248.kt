package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1248: ImageVector
    get() {
        val current = _ic1248
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1248",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.531 6.829 16 13.922 5.5 16 0 13.832 l6.318 -6.461 -.999 2.606 a.396 .396 0 0 0 .002 .251 .23 .23 0 0 0 .055 .092 .109 .109 0 0 0 .075 .033 H6.47 c.008 0 .015 .003 .022 .009 a.068 .068 0 0 1 .017 .024 .12 .12 0 0 1 .01 .035 .138 .138 0 0 1 -.002 .038 l-.7 4.041 L8.76 9.254 c.09 -.162 .022 -.427 -.11 -.427 h-.996 a.036 .036 0 0 1 -.025 -.012 .079 .079 0 0 1 -.019 -.031 .13 .13 0 0 1 .003 -.085 l.919 -1.87Z m-.905 -4.58 c-.019 -.201 .154 -.374 .374 -.374 s.393 .173 .374 .374 l-.17 1.764 h-.409 l-.169 -1.764Z m.655 2.345 a.281 .281 0 1 1 -.562 0 .281 .281 0 0 1 .562 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.531 6.829
                moveTo(x = 8.531f, y = 6.829f)
                // L 16 13.922
                lineTo(x = 16.0f, y = 13.922f)
                // L 5.5 16
                lineTo(x = 5.5f, y = 16.0f)
                // L 0 13.832
                lineTo(x = 0.0f, y = 13.832f)
                // l 6.318 -6.461
                lineToRelative(dx = 6.318f, dy = -6.461f)
                // l -0.999 2.606
                lineToRelative(dx = -0.999f, dy = 2.606f)
                // a 0.396 0.396 0 0 0 0.002 0.251
                arcToRelative(
                    a = 0.396f,
                    b = 0.396f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.002f,
                    dy1 = 0.251f,
                )
                // a 0.23 0.23 0 0 0 0.055 0.092
                arcToRelative(
                    a = 0.23f,
                    b = 0.23f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.055f,
                    dy1 = 0.092f,
                )
                // a 0.109 0.109 0 0 0 0.075 0.033
                arcToRelative(
                    a = 0.109f,
                    b = 0.109f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.075f,
                    dy1 = 0.033f,
                )
                // H 6.47
                horizontalLineTo(x = 6.47f)
                // c 0.008 0 0.015 0.003 0.022 0.009
                curveToRelative(
                    dx1 = 0.008f,
                    dy1 = 0.0f,
                    dx2 = 0.015f,
                    dy2 = 0.003f,
                    dx3 = 0.022f,
                    dy3 = 0.009f,
                )
                // a 0.068 0.068 0 0 1 0.017 0.024
                arcToRelative(
                    a = 0.068f,
                    b = 0.068f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.017f,
                    dy1 = 0.024f,
                )
                // a 0.12 0.12 0 0 1 0.01 0.035
                arcToRelative(
                    a = 0.12f,
                    b = 0.12f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.01f,
                    dy1 = 0.035f,
                )
                // a 0.138 0.138 0 0 1 -0.002 0.038
                arcToRelative(
                    a = 0.138f,
                    b = 0.138f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.002f,
                    dy1 = 0.038f,
                )
                // l -0.7 4.041
                lineToRelative(dx = -0.7f, dy = 4.041f)
                // L 8.76 9.254
                lineTo(x = 8.76f, y = 9.254f)
                // c 0.09 -0.162 0.022 -0.427 -0.11 -0.427
                curveToRelative(
                    dx1 = 0.09f,
                    dy1 = -0.162f,
                    dx2 = 0.022f,
                    dy2 = -0.427f,
                    dx3 = -0.11f,
                    dy3 = -0.427f,
                )
                // h -0.996
                horizontalLineToRelative(dx = -0.996f)
                // a 0.036 0.036 0 0 1 -0.025 -0.012
                arcToRelative(
                    a = 0.036f,
                    b = 0.036f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.025f,
                    dy1 = -0.012f,
                )
                // a 0.079 0.079 0 0 1 -0.019 -0.031
                arcToRelative(
                    a = 0.079f,
                    b = 0.079f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.019f,
                    dy1 = -0.031f,
                )
                // a 0.13 0.13 0 0 1 0.003 -0.085
                arcToRelative(
                    a = 0.13f,
                    b = 0.13f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.003f,
                    dy1 = -0.085f,
                )
                // l 0.919 -1.87z
                lineToRelative(dx = 0.919f, dy = -1.87f)
                close()
                // m -0.905 -4.58
                moveToRelative(dx = -0.905f, dy = -4.58f)
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
            // M7.83 .473 a.197 .197 0 0 1 .34 0 l2.804 4.86 a.195 .195 0 0 1 -.17 .292 H5.196 a.195 .195 0 0 1 -.17 -.291 L7.83 .473Z m2.533 4.702 L8 1.078 5.637 5.175 h4.726Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.83 0.473
                moveTo(x = 7.83f, y = 0.473f)
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
                // H 5.196
                horizontalLineTo(x = 5.196f)
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
                // L 7.83 0.473z
                lineTo(x = 7.83f, y = 0.473f)
                close()
                // m 2.533 4.702
                moveToRelative(dx = 2.533f, dy = 4.702f)
                // L 8 1.078
                lineTo(x = 8.0f, y = 1.078f)
                // L 5.637 5.175
                lineTo(x = 5.637f, y = 5.175f)
                // h 4.726z
                horizontalLineToRelative(dx = 4.726f)
                close()
            }
        }.build().also { _ic1248 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1248: ImageVector? = null
