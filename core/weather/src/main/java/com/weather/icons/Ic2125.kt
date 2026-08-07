package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2125: ImageVector
    get() {
        val current = _ic2125
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2125",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M2.932 11.708 2.354 10 1 11.19 l.319 2.674 1.613 -2.156Z m12 0 L14.354 10 13 11.19 l.319 2.674 1.613 -2.156Z M5.354 11 l.578 1.708 -1.613 2.156 L4 12.19 5.354 11Z m6.578 1.708 L11.354 11 10 12.19 l.319 2.674 1.613 -2.156Z M8.354 12 l.578 1.708 -1.613 2.156 L7 13.19 8.354 12Z M7.9 10 a4.99 4.99 0 0 0 3.827 -1.783 3 3 0 1 0 .553 -5.63 A4.999 4.999 0 0 0 7.9 0 a4.998 4.998 0 0 0 -4.359 2.549 3 3 0 1 0 .586 5.732 A4.988 4.988 0 0 0 7.9 10Z m-.791 -6.398 c-.057 -.362 .17 -.8 .496 -.997 .256 -.153 .551 -.133 .806 .023 l.07 .042 a.846 .846 0 0 1 .409 .853 L8.532 5.8 H7.454 L7.11 3.602Z M8.599 6.9 a.6 .6 0 1 1 -1.2 0 .6 .6 0 0 1 1.2 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 2.932 11.708
                moveTo(x = 2.932f, y = 11.708f)
                // L 2.354 10
                lineTo(x = 2.354f, y = 10.0f)
                // L 1 11.19
                lineTo(x = 1.0f, y = 11.19f)
                // l 0.319 2.674
                lineToRelative(dx = 0.319f, dy = 2.674f)
                // l 1.613 -2.156z
                lineToRelative(dx = 1.613f, dy = -2.156f)
                close()
                // m 12 0
                moveToRelative(dx = 12.0f, dy = 0.0f)
                // L 14.354 10
                lineTo(x = 14.354f, y = 10.0f)
                // L 13 11.19
                lineTo(x = 13.0f, y = 11.19f)
                // l 0.319 2.674
                lineToRelative(dx = 0.319f, dy = 2.674f)
                // l 1.613 -2.156z
                lineToRelative(dx = 1.613f, dy = -2.156f)
                close()
                // M 5.354 11
                moveTo(x = 5.354f, y = 11.0f)
                // l 0.578 1.708
                lineToRelative(dx = 0.578f, dy = 1.708f)
                // l -1.613 2.156
                lineToRelative(dx = -1.613f, dy = 2.156f)
                // L 4 12.19
                lineTo(x = 4.0f, y = 12.19f)
                // L 5.354 11z
                lineTo(x = 5.354f, y = 11.0f)
                close()
                // m 6.578 1.708
                moveToRelative(dx = 6.578f, dy = 1.708f)
                // L 11.354 11
                lineTo(x = 11.354f, y = 11.0f)
                // L 10 12.19
                lineTo(x = 10.0f, y = 12.19f)
                // l 0.319 2.674
                lineToRelative(dx = 0.319f, dy = 2.674f)
                // l 1.613 -2.156z
                lineToRelative(dx = 1.613f, dy = -2.156f)
                close()
                // M 8.354 12
                moveTo(x = 8.354f, y = 12.0f)
                // l 0.578 1.708
                lineToRelative(dx = 0.578f, dy = 1.708f)
                // l -1.613 2.156
                lineToRelative(dx = -1.613f, dy = 2.156f)
                // L 7 13.19
                lineTo(x = 7.0f, y = 13.19f)
                // L 8.354 12z
                lineTo(x = 8.354f, y = 12.0f)
                close()
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
                // m -0.791 -6.398
                moveToRelative(dx = -0.791f, dy = -6.398f)
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
                // L 8.532 5.8
                lineTo(x = 8.532f, y = 5.8f)
                // H 7.454
                horizontalLineTo(x = 7.454f)
                // L 7.11 3.602z
                lineTo(x = 7.11f, y = 3.602f)
                close()
                // M 8.599 6.9
                moveTo(x = 8.599f, y = 6.9f)
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
        }.build().also { _ic2125 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2125: ImageVector? = null
