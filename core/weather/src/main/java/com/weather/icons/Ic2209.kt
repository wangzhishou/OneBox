package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2209: ImageVector
    get() {
        val current = _ic2209
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2209",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // m1 9 1 1.5 L1 12 l-1 -1.5 L1 9Z m15 1.5 L15 9 l-1 1.5 1 1.5 1 -1.5Z m-7 4 L8 13 l-1 1.5 L8 16 l1 -1.5Z M4.5 11 l1 1.5 -1 1.5 -1 -1.5 1 -1.5Z m8 1.5 -1 -1.5 -1 1.5 1 1.5 1 -1.5Z M7.9 10 a4.99 4.99 0 0 0 3.827 -1.783 3 3 0 1 0 .553 -5.63 A4.999 4.999 0 0 0 7.9 0 a4.998 4.998 0 0 0 -4.359 2.549 3 3 0 1 0 .586 5.732 A4.988 4.988 0 0 0 7.9 10Z m-.791 -6.398 c-.057 -.362 .17 -.8 .496 -.997 .256 -.153 .551 -.133 .806 .023 l.07 .042 a.846 .846 0 0 1 .409 .853 L8.532 5.8 H7.454 L7.11 3.602Z M8.599 6.9 a.6 .6 0 1 1 -1.2 0 .6 .6 0 0 1 1.2 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 1 9
                moveTo(x = 1.0f, y = 9.0f)
                // l 1 1.5
                lineToRelative(dx = 1.0f, dy = 1.5f)
                // L 1 12
                lineTo(x = 1.0f, y = 12.0f)
                // l -1 -1.5
                lineToRelative(dx = -1.0f, dy = -1.5f)
                // L 1 9z
                lineTo(x = 1.0f, y = 9.0f)
                close()
                // m 15 1.5
                moveToRelative(dx = 15.0f, dy = 1.5f)
                // L 15 9
                lineTo(x = 15.0f, y = 9.0f)
                // l -1 1.5
                lineToRelative(dx = -1.0f, dy = 1.5f)
                // l 1 1.5
                lineToRelative(dx = 1.0f, dy = 1.5f)
                // l 1 -1.5z
                lineToRelative(dx = 1.0f, dy = -1.5f)
                close()
                // m -7 4
                moveToRelative(dx = -7.0f, dy = 4.0f)
                // L 8 13
                lineTo(x = 8.0f, y = 13.0f)
                // l -1 1.5
                lineToRelative(dx = -1.0f, dy = 1.5f)
                // L 8 16
                lineTo(x = 8.0f, y = 16.0f)
                // l 1 -1.5z
                lineToRelative(dx = 1.0f, dy = -1.5f)
                close()
                // M 4.5 11
                moveTo(x = 4.5f, y = 11.0f)
                // l 1 1.5
                lineToRelative(dx = 1.0f, dy = 1.5f)
                // l -1 1.5
                lineToRelative(dx = -1.0f, dy = 1.5f)
                // l -1 -1.5
                lineToRelative(dx = -1.0f, dy = -1.5f)
                // l 1 -1.5z
                lineToRelative(dx = 1.0f, dy = -1.5f)
                close()
                // m 8 1.5
                moveToRelative(dx = 8.0f, dy = 1.5f)
                // l -1 -1.5
                lineToRelative(dx = -1.0f, dy = -1.5f)
                // l -1 1.5
                lineToRelative(dx = -1.0f, dy = 1.5f)
                // l 1 1.5
                lineToRelative(dx = 1.0f, dy = 1.5f)
                // l 1 -1.5z
                lineToRelative(dx = 1.0f, dy = -1.5f)
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
        }.build().also { _ic2209 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2209: ImageVector? = null
