package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2203: ImageVector
    get() {
        val current = _ic2203
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2203",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M11.34 3.333 a1.667 1.667 0 1 0 3.333 0 c0 -.833 -.925 -2.325 -1.666 -3.333 -.741 1.008 -1.667 2.5 -1.667 3.333Z M2.219 9.121 A3 3 0 0 1 1.34 7 c0 -1.5 1.666 -4.185 3 -6 .7 .953 1.493 2.147 2.087 3.283 -1.226 1.887 -2.37 3.96 -2.85 5.618 a3 3 0 0 1 -1.358 -.78Z m5.88 -.519 c-.057 -.362 .17 -.8 .496 -.997 .256 -.153 .551 -.133 .806 .023 l.07 .042 a.846 .846 0 0 1 .41 .853 l-.36 2.277 H8.444 L8.1 8.602Z M9.59 11.9 a.6 .6 0 1 1 -1.2 0 .6 .6 0 0 1 1.2 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.34 3.333
                moveTo(x = 11.34f, y = 3.333f)
                // a 1.667 1.667 0 1 0 3.333 0
                arcToRelative(
                    a = 1.667f,
                    b = 1.667f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 3.333f,
                    dy1 = 0.0f,
                )
                // c 0 -0.833 -0.925 -2.325 -1.666 -3.333
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.833f,
                    dx2 = -0.925f,
                    dy2 = -2.325f,
                    dx3 = -1.666f,
                    dy3 = -3.333f,
                )
                // c -0.741 1.008 -1.667 2.5 -1.667 3.333z
                curveToRelative(
                    dx1 = -0.741f,
                    dy1 = 1.008f,
                    dx2 = -1.667f,
                    dy2 = 2.5f,
                    dx3 = -1.667f,
                    dy3 = 3.333f,
                )
                close()
                // M 2.219 9.121
                moveTo(x = 2.219f, y = 9.121f)
                // A 3 3 0 0 1 1.34 7
                arcTo(
                    horizontalEllipseRadius = 3.0f,
                    verticalEllipseRadius = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 1.34f,
                    y1 = 7.0f,
                )
                // c 0 -1.5 1.666 -4.185 3 -6
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.5f,
                    dx2 = 1.666f,
                    dy2 = -4.185f,
                    dx3 = 3.0f,
                    dy3 = -6.0f,
                )
                // c 0.7 0.953 1.493 2.147 2.087 3.283
                curveToRelative(
                    dx1 = 0.7f,
                    dy1 = 0.953f,
                    dx2 = 1.493f,
                    dy2 = 2.147f,
                    dx3 = 2.087f,
                    dy3 = 3.283f,
                )
                // c -1.226 1.887 -2.37 3.96 -2.85 5.618
                curveToRelative(
                    dx1 = -1.226f,
                    dy1 = 1.887f,
                    dx2 = -2.37f,
                    dy2 = 3.96f,
                    dx3 = -2.85f,
                    dy3 = 5.618f,
                )
                // a 3 3 0 0 1 -1.358 -0.78z
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.358f,
                    dy1 = -0.78f,
                )
                close()
                // m 5.88 -0.519
                moveToRelative(dx = 5.88f, dy = -0.519f)
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
                // a 0.846 0.846 0 0 1 0.41 0.853
                arcToRelative(
                    a = 0.846f,
                    b = 0.846f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.41f,
                    dy1 = 0.853f,
                )
                // l -0.36 2.277
                lineToRelative(dx = -0.36f, dy = 2.277f)
                // H 8.444
                horizontalLineTo(x = 8.444f)
                // L 8.1 8.602z
                lineTo(x = 8.1f, y = 8.602f)
                close()
                // M 9.59 11.9
                moveTo(x = 9.59f, y = 11.9f)
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
            // M8.244 3.067 C6.348 5.797 4.34 9.28 4.34 11.333 a4.667 4.667 0 1 0 9.333 0 c0 -2.054 -2.008 -5.536 -3.904 -8.266 A49.186 49.186 0 0 0 9.007 2 a49.22 49.22 0 0 0 -.763 1.067Z m-2.66 8.266 c0 -.377 .11 -.92 .364 -1.623 .248 -.689 .608 -1.46 1.044 -2.27 a37.761 37.761 0 0 1 2.015 -3.285 37.754 37.754 0 0 1 2.015 3.284 c.435 .811 .795 1.582 1.043 2.27 .254 .704 .364 1.247 .364 1.624 a3.422 3.422 0 1 1 -6.845 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.244 3.067
                moveTo(x = 8.244f, y = 3.067f)
                // C 6.348 5.797 4.34 9.28 4.34 11.333
                curveTo(
                    x1 = 6.348f,
                    y1 = 5.797f,
                    x2 = 4.34f,
                    y2 = 9.28f,
                    x3 = 4.34f,
                    y3 = 11.333f,
                )
                // a 4.667 4.667 0 1 0 9.333 0
                arcToRelative(
                    a = 4.667f,
                    b = 4.667f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 9.333f,
                    dy1 = 0.0f,
                )
                // c 0 -2.054 -2.008 -5.536 -3.904 -8.266
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -2.054f,
                    dx2 = -2.008f,
                    dy2 = -5.536f,
                    dx3 = -3.904f,
                    dy3 = -8.266f,
                )
                // A 49.186 49.186 0 0 0 9.007 2
                arcTo(
                    horizontalEllipseRadius = 49.186f,
                    verticalEllipseRadius = 49.186f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 9.007f,
                    y1 = 2.0f,
                )
                // a 49.22 49.22 0 0 0 -0.763 1.067z
                arcToRelative(
                    a = 49.22f,
                    b = 49.22f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.763f,
                    dy1 = 1.067f,
                )
                close()
                // m -2.66 8.266
                moveToRelative(dx = -2.66f, dy = 8.266f)
                // c 0 -0.377 0.11 -0.92 0.364 -1.623
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.377f,
                    dx2 = 0.11f,
                    dy2 = -0.92f,
                    dx3 = 0.364f,
                    dy3 = -1.623f,
                )
                // c 0.248 -0.689 0.608 -1.46 1.044 -2.27
                curveToRelative(
                    dx1 = 0.248f,
                    dy1 = -0.689f,
                    dx2 = 0.608f,
                    dy2 = -1.46f,
                    dx3 = 1.044f,
                    dy3 = -2.27f,
                )
                // a 37.761 37.761 0 0 1 2.015 -3.285
                arcToRelative(
                    a = 37.761f,
                    b = 37.761f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.015f,
                    dy1 = -3.285f,
                )
                // a 37.754 37.754 0 0 1 2.015 3.284
                arcToRelative(
                    a = 37.754f,
                    b = 37.754f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.015f,
                    dy1 = 3.284f,
                )
                // c 0.435 0.811 0.795 1.582 1.043 2.27
                curveToRelative(
                    dx1 = 0.435f,
                    dy1 = 0.811f,
                    dx2 = 0.795f,
                    dy2 = 1.582f,
                    dx3 = 1.043f,
                    dy3 = 2.27f,
                )
                // c 0.254 0.704 0.364 1.247 0.364 1.624
                curveToRelative(
                    dx1 = 0.254f,
                    dy1 = 0.704f,
                    dx2 = 0.364f,
                    dy2 = 1.247f,
                    dx3 = 0.364f,
                    dy3 = 1.624f,
                )
                // a 3.422 3.422 0 1 1 -6.845 0z
                arcToRelative(
                    a = 3.422f,
                    b = 3.422f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -6.845f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic2203 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2203: ImageVector? = null
