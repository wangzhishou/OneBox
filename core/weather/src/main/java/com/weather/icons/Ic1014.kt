package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1014: ImageVector
    get() {
        val current = _ic1014
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1014",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.058 0 h5.403 c.264 0 .422 .311 .279 .548 l-2.975 3.85 a.173 .173 0 0 0 .05 .232 .147 .147 0 0 0 .08 .024 h3.13 c.416 0 .63 .53 .345 .855 L4.12 16 l2.203 -8.082 a.177 .177 0 0 0 -.025 -.146 .159 .159 0 0 0 -.055 -.048 .148 .148 0 0 0 -.07 -.018 H2.976 a.451 .451 0 0 1 -.236 -.067 .49 .49 0 0 1 -.173 -.183 .532 .532 0 0 1 -.006 -.503 L6.56 .311 C6.66 .119 6.85 0 7.057 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.058 0
                moveTo(x = 7.058f, y = 0.0f)
                // h 5.403
                horizontalLineToRelative(dx = 5.403f)
                // c 0.264 0 0.422 0.311 0.279 0.548
                curveToRelative(
                    dx1 = 0.264f,
                    dy1 = 0.0f,
                    dx2 = 0.422f,
                    dy2 = 0.311f,
                    dx3 = 0.279f,
                    dy3 = 0.548f,
                )
                // l -2.975 3.85
                lineToRelative(dx = -2.975f, dy = 3.85f)
                // a 0.173 0.173 0 0 0 0.05 0.232
                arcToRelative(
                    a = 0.173f,
                    b = 0.173f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.05f,
                    dy1 = 0.232f,
                )
                // a 0.147 0.147 0 0 0 0.08 0.024
                arcToRelative(
                    a = 0.147f,
                    b = 0.147f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.08f,
                    dy1 = 0.024f,
                )
                // h 3.13
                horizontalLineToRelative(dx = 3.13f)
                // c 0.416 0 0.63 0.53 0.345 0.855
                curveToRelative(
                    dx1 = 0.416f,
                    dy1 = 0.0f,
                    dx2 = 0.63f,
                    dy2 = 0.53f,
                    dx3 = 0.345f,
                    dy3 = 0.855f,
                )
                // L 4.12 16
                lineTo(x = 4.12f, y = 16.0f)
                // l 2.203 -8.082
                lineToRelative(dx = 2.203f, dy = -8.082f)
                // a 0.177 0.177 0 0 0 -0.025 -0.146
                arcToRelative(
                    a = 0.177f,
                    b = 0.177f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.025f,
                    dy1 = -0.146f,
                )
                // a 0.159 0.159 0 0 0 -0.055 -0.048
                arcToRelative(
                    a = 0.159f,
                    b = 0.159f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.055f,
                    dy1 = -0.048f,
                )
                // a 0.148 0.148 0 0 0 -0.07 -0.018
                arcToRelative(
                    a = 0.148f,
                    b = 0.148f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.07f,
                    dy1 = -0.018f,
                )
                // H 2.976
                horizontalLineTo(x = 2.976f)
                // a 0.451 0.451 0 0 1 -0.236 -0.067
                arcToRelative(
                    a = 0.451f,
                    b = 0.451f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.236f,
                    dy1 = -0.067f,
                )
                // a 0.49 0.49 0 0 1 -0.173 -0.183
                arcToRelative(
                    a = 0.49f,
                    b = 0.49f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.173f,
                    dy1 = -0.183f,
                )
                // a 0.532 0.532 0 0 1 -0.006 -0.503
                arcToRelative(
                    a = 0.532f,
                    b = 0.532f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.006f,
                    dy1 = -0.503f,
                )
                // L 6.56 0.311
                lineTo(x = 6.56f, y = 0.311f)
                // C 6.66 0.119 6.85 0 7.057 0z
                curveTo(
                    x1 = 6.66f,
                    y1 = 0.119f,
                    x2 = 6.85f,
                    y2 = 0.0f,
                    x3 = 7.057f,
                    y3 = 0.0f,
                )
                close()
            }
        }.build().also { _ic1014 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1014: ImageVector? = null
