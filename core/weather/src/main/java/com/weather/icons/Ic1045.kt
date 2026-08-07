package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1045: ImageVector
    get() {
        val current = _ic1045
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1045",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M3.25 5.121 c.055 -.203 .306 -.25 .455 -.102 l1.411 1.412 a.8 .8 0 0 0 1.132 0 l.282 -.283 a.8 .8 0 0 0 0 -1.132 L5.12 3.605 c-.149 -.149 -.101 -.4 .102 -.454 a2.799 2.799 0 0 1 3.531 2.79 .284 .284 0 0 1 -.085 .191 l-.51 .51 3.974 3.975 a.8 .8 0 0 1 0 1.131 l-.283 .283 a.8 .8 0 0 1 -1.132 0 L6.744 8.057 l-.51 .51 a.284 .284 0 0 1 -.192 .085 2.799 2.799 0 0 1 -2.79 -3.531Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 3.25 5.121
                moveTo(x = 3.25f, y = 5.121f)
                // c 0.055 -0.203 0.306 -0.25 0.455 -0.102
                curveToRelative(
                    dx1 = 0.055f,
                    dy1 = -0.203f,
                    dx2 = 0.306f,
                    dy2 = -0.25f,
                    dx3 = 0.455f,
                    dy3 = -0.102f,
                )
                // l 1.411 1.412
                lineToRelative(dx = 1.411f, dy = 1.412f)
                // a 0.8 0.8 0 0 0 1.132 0
                arcToRelative(
                    a = 0.8f,
                    b = 0.8f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.132f,
                    dy1 = 0.0f,
                )
                // l 0.282 -0.283
                lineToRelative(dx = 0.282f, dy = -0.283f)
                // a 0.8 0.8 0 0 0 0 -1.132
                arcToRelative(
                    a = 0.8f,
                    b = 0.8f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.132f,
                )
                // L 5.12 3.605
                lineTo(x = 5.12f, y = 3.605f)
                // c -0.149 -0.149 -0.101 -0.4 0.102 -0.454
                curveToRelative(
                    dx1 = -0.149f,
                    dy1 = -0.149f,
                    dx2 = -0.101f,
                    dy2 = -0.4f,
                    dx3 = 0.102f,
                    dy3 = -0.454f,
                )
                // a 2.799 2.799 0 0 1 3.531 2.79
                arcToRelative(
                    a = 2.799f,
                    b = 2.799f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 3.531f,
                    dy1 = 2.79f,
                )
                // a 0.284 0.284 0 0 1 -0.085 0.191
                arcToRelative(
                    a = 0.284f,
                    b = 0.284f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.085f,
                    dy1 = 0.191f,
                )
                // l -0.51 0.51
                lineToRelative(dx = -0.51f, dy = 0.51f)
                // l 3.974 3.975
                lineToRelative(dx = 3.974f, dy = 3.975f)
                // a 0.8 0.8 0 0 1 0 1.131
                arcToRelative(
                    a = 0.8f,
                    b = 0.8f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.131f,
                )
                // l -0.283 0.283
                lineToRelative(dx = -0.283f, dy = 0.283f)
                // a 0.8 0.8 0 0 1 -1.132 0
                arcToRelative(
                    a = 0.8f,
                    b = 0.8f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.132f,
                    dy1 = 0.0f,
                )
                // L 6.744 8.057
                lineTo(x = 6.744f, y = 8.057f)
                // l -0.51 0.51
                lineToRelative(dx = -0.51f, dy = 0.51f)
                // a 0.284 0.284 0 0 1 -0.192 0.085
                arcToRelative(
                    a = 0.284f,
                    b = 0.284f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.192f,
                    dy1 = 0.085f,
                )
                // a 2.799 2.799 0 0 1 -2.79 -3.531z
                arcToRelative(
                    a = 2.799f,
                    b = 2.799f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.79f,
                    dy1 = -3.531f,
                )
                close()
            }
            // M16 8 A8 8 0 1 1 0 8 a8 8 0 0 1 16 0Z m-1.3 0 a6.67 6.67 0 0 0 -1.352 -4.037 l-9.385 9.385 A6.7 6.7 0 0 0 14.7 8Z m-2.385 -5.126 a6.7 6.7 0 0 0 -9.44 9.44 l9.44 -9.44Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 16 8
                moveTo(x = 16.0f, y = 8.0f)
                // A 8 8 0 1 1 0 8
                arcTo(
                    horizontalEllipseRadius = 8.0f,
                    verticalEllipseRadius = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 0.0f,
                    y1 = 8.0f,
                )
                // a 8 8 0 0 1 16 0z
                arcToRelative(
                    a = 8.0f,
                    b = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 16.0f,
                    dy1 = 0.0f,
                )
                close()
                // m -1.3 0
                moveToRelative(dx = -1.3f, dy = 0.0f)
                // a 6.67 6.67 0 0 0 -1.352 -4.037
                arcToRelative(
                    a = 6.67f,
                    b = 6.67f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.352f,
                    dy1 = -4.037f,
                )
                // l -9.385 9.385
                lineToRelative(dx = -9.385f, dy = 9.385f)
                // A 6.7 6.7 0 0 0 14.7 8z
                arcTo(
                    horizontalEllipseRadius = 6.7f,
                    verticalEllipseRadius = 6.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 14.7f,
                    y1 = 8.0f,
                )
                close()
                // m -2.385 -5.126
                moveToRelative(dx = -2.385f, dy = -5.126f)
                // a 6.7 6.7 0 0 0 -9.44 9.44
                arcToRelative(
                    a = 6.7f,
                    b = 6.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -9.44f,
                    dy1 = 9.44f,
                )
                // l 9.44 -9.44z
                lineToRelative(dx = 9.44f, dy = -9.44f)
                close()
            }
        }.build().also { _ic1045 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1045: ImageVector? = null
