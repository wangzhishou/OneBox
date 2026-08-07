package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1082: ImageVector
    get() {
        val current = _ic1082
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1082",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M5.109 4.132 c-.057 -.362 .17 -.8 .496 -.997 .256 -.153 .551 -.133 .806 .023 l.07 .042 a.846 .846 0 0 1 .409 .853 L6.532 6.33 H5.454 L5.11 4.132Z M6.6 7.43 a.6 .6 0 1 1 -1.2 0 .6 .6 0 0 1 1.2 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5.109 4.132
                moveTo(x = 5.109f, y = 4.132f)
                // c -0.057 -0.362 0.17 -0.8 0.496 -0.997
                curveToRelative(
                    dx1 = -0.057f,
                    dy1 = -0.362f,
                    dx2 = 0.17f,
                    dy2 = -0.8f,
                    dx3 = 0.496f,
                    dy3 = -0.997f,
                )
                // c 0.256 -0.153 0.551 -0.133 0.806 0.023
                curveToRelative(
                    dx1 = 0.256f,
                    dy1 = -0.153f,
                    dx2 = 0.551f,
                    dy2 = -0.133f,
                    dx3 = 0.806f,
                    dy3 = 0.023f,
                )
                // l 0.07 0.042
                lineToRelative(dx = 0.07f, dy = 0.042f)
                // a 0.846 0.846 0 0 1 0.409 0.853
                arcToRelative(
                    a = 0.846f,
                    b = 0.846f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.409f,
                    dy1 = 0.853f,
                )
                // L 6.532 6.33
                lineTo(x = 6.532f, y = 6.33f)
                // H 5.454
                horizontalLineTo(x = 5.454f)
                // L 5.11 4.132z
                lineTo(x = 5.11f, y = 4.132f)
                close()
                // M 6.6 7.43
                moveTo(x = 6.6f, y = 7.43f)
                // a 0.6 0.6 0 1 1 -1.2 0
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.2f,
                    dy1 = 0.0f,
                )
                // a 0.6 0.6 0 0 1 1.2 0z
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.2f,
                    dy1 = 0.0f,
                )
                close()
            }
            // M7.178 0 c1.097 0 2.136 .21 3.063 .584 2.452 .97 4.15 3.083 4.15 5.536 v1.014 l1.462 2.378 c.37 .6 .019 1.092 -.774 1.092 h-.689 v2.26 c0 .705 -.677 1.286 -1.508 1.286 l-2.071 -.263 -.004 1.765 v.013 c-.011 .226 -.521 .342 -.512 .335 h-7.97 v-5.38 C.897 9.502 0 7.899 0 6.114 .004 2.735 3.217 0 7.178 0Z M10 5.53 a4 4 0 1 0 -8 0 4 4 0 0 0 8 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.178 0
                moveTo(x = 7.178f, y = 0.0f)
                // c 1.097 0 2.136 0.21 3.063 0.584
                curveToRelative(
                    dx1 = 1.097f,
                    dy1 = 0.0f,
                    dx2 = 2.136f,
                    dy2 = 0.21f,
                    dx3 = 3.063f,
                    dy3 = 0.584f,
                )
                // c 2.452 0.97 4.15 3.083 4.15 5.536
                curveToRelative(
                    dx1 = 2.452f,
                    dy1 = 0.97f,
                    dx2 = 4.15f,
                    dy2 = 3.083f,
                    dx3 = 4.15f,
                    dy3 = 5.536f,
                )
                // v 1.014
                verticalLineToRelative(dy = 1.014f)
                // l 1.462 2.378
                lineToRelative(dx = 1.462f, dy = 2.378f)
                // c 0.37 0.6 0.019 1.092 -0.774 1.092
                curveToRelative(
                    dx1 = 0.37f,
                    dy1 = 0.6f,
                    dx2 = 0.019f,
                    dy2 = 1.092f,
                    dx3 = -0.774f,
                    dy3 = 1.092f,
                )
                // h -0.689
                horizontalLineToRelative(dx = -0.689f)
                // v 2.26
                verticalLineToRelative(dy = 2.26f)
                // c 0 0.705 -0.677 1.286 -1.508 1.286
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.705f,
                    dx2 = -0.677f,
                    dy2 = 1.286f,
                    dx3 = -1.508f,
                    dy3 = 1.286f,
                )
                // l -2.071 -0.263
                lineToRelative(dx = -2.071f, dy = -0.263f)
                // l -0.004 1.765
                lineToRelative(dx = -0.004f, dy = 1.765f)
                // v 0.013
                verticalLineToRelative(dy = 0.013f)
                // c -0.011 0.226 -0.521 0.342 -0.512 0.335
                curveToRelative(
                    dx1 = -0.011f,
                    dy1 = 0.226f,
                    dx2 = -0.521f,
                    dy2 = 0.342f,
                    dx3 = -0.512f,
                    dy3 = 0.335f,
                )
                // h -7.97
                horizontalLineToRelative(dx = -7.97f)
                // v -5.38
                verticalLineToRelative(dy = -5.38f)
                // C 0.897 9.502 0 7.899 0 6.114
                curveTo(
                    x1 = 0.897f,
                    y1 = 9.502f,
                    x2 = 0.0f,
                    y2 = 7.899f,
                    x3 = 0.0f,
                    y3 = 6.114f,
                )
                // C 0.004 2.735 3.217 0 7.178 0z
                curveTo(
                    x1 = 0.004f,
                    y1 = 2.735f,
                    x2 = 3.217f,
                    y2 = 0.0f,
                    x3 = 7.178f,
                    y3 = 0.0f,
                )
                close()
                // M 10 5.53
                moveTo(x = 10.0f, y = 5.53f)
                // a 4 4 0 1 0 -8 0
                arcToRelative(
                    a = 4.0f,
                    b = 4.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -8.0f,
                    dy1 = 0.0f,
                )
                // a 4 4 0 0 0 8 0z
                arcToRelative(
                    a = 4.0f,
                    b = 4.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 8.0f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic1082 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1082: ImageVector? = null
