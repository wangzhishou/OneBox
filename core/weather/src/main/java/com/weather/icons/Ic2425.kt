package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2425: ImageVector
    get() {
        val current = _ic2425
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2425",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.46 10.626 c-.054 0 -.089 -.05 -.065 -.093 l.792 -1.439 C9.21 9.051 9.176 9 9.122 9 H7.544 a.147 .147 0 0 0 -.076 .02 .159 .159 0 0 0 -.058 .057 l-1.397 2.637 c-.042 .079 .022 .17 .118 .17 h1.42 c.05 0 .084 .043 .069 .086 l-.739 1.943 c-.027 .07 .072 .118 .124 .063 l2.978 -3.243 c.04 -.042 .006 -.107 -.055 -.107 H8.46Z m1.87 -2.99 A3.118 3.118 0 0 1 7.937 8.75 3.118 3.118 0 0 1 5.58 7.676 a1.875 1.875 0 1 1 -.366 -3.583 3.124 3.124 0 0 1 5.462 .024 1.875 1.875 0 1 1 -.346 3.52Z M7.753 4.128 c-.204 .123 -.346 .397 -.31 .623 l.216 1.374 h.674 l.223 -1.423 a.529 .529 0 0 0 -.255 -.533 l-.044 -.027 a.474 .474 0 0 0 -.504 -.014Z M8 7.188 a.375 .375 0 1 0 0 -.75 .375 .375 0 0 0 0 .75Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.46 10.626
                moveTo(x = 8.46f, y = 10.626f)
                // c -0.054 0 -0.089 -0.05 -0.065 -0.093
                curveToRelative(
                    dx1 = -0.054f,
                    dy1 = 0.0f,
                    dx2 = -0.089f,
                    dy2 = -0.05f,
                    dx3 = -0.065f,
                    dy3 = -0.093f,
                )
                // l 0.792 -1.439
                lineToRelative(dx = 0.792f, dy = -1.439f)
                // C 9.21 9.051 9.176 9 9.122 9
                curveTo(
                    x1 = 9.21f,
                    y1 = 9.051f,
                    x2 = 9.176f,
                    y2 = 9.0f,
                    x3 = 9.122f,
                    y3 = 9.0f,
                )
                // H 7.544
                horizontalLineTo(x = 7.544f)
                // a 0.147 0.147 0 0 0 -0.076 0.02
                arcToRelative(
                    a = 0.147f,
                    b = 0.147f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.076f,
                    dy1 = 0.02f,
                )
                // a 0.159 0.159 0 0 0 -0.058 0.057
                arcToRelative(
                    a = 0.159f,
                    b = 0.159f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.058f,
                    dy1 = 0.057f,
                )
                // l -1.397 2.637
                lineToRelative(dx = -1.397f, dy = 2.637f)
                // c -0.042 0.079 0.022 0.17 0.118 0.17
                curveToRelative(
                    dx1 = -0.042f,
                    dy1 = 0.079f,
                    dx2 = 0.022f,
                    dy2 = 0.17f,
                    dx3 = 0.118f,
                    dy3 = 0.17f,
                )
                // h 1.42
                horizontalLineToRelative(dx = 1.42f)
                // c 0.05 0 0.084 0.043 0.069 0.086
                curveToRelative(
                    dx1 = 0.05f,
                    dy1 = 0.0f,
                    dx2 = 0.084f,
                    dy2 = 0.043f,
                    dx3 = 0.069f,
                    dy3 = 0.086f,
                )
                // l -0.739 1.943
                lineToRelative(dx = -0.739f, dy = 1.943f)
                // c -0.027 0.07 0.072 0.118 0.124 0.063
                curveToRelative(
                    dx1 = -0.027f,
                    dy1 = 0.07f,
                    dx2 = 0.072f,
                    dy2 = 0.118f,
                    dx3 = 0.124f,
                    dy3 = 0.063f,
                )
                // l 2.978 -3.243
                lineToRelative(dx = 2.978f, dy = -3.243f)
                // c 0.04 -0.042 0.006 -0.107 -0.055 -0.107
                curveToRelative(
                    dx1 = 0.04f,
                    dy1 = -0.042f,
                    dx2 = 0.006f,
                    dy2 = -0.107f,
                    dx3 = -0.055f,
                    dy3 = -0.107f,
                )
                // H 8.46z
                horizontalLineTo(x = 8.46f)
                close()
                // m 1.87 -2.99
                moveToRelative(dx = 1.87f, dy = -2.99f)
                // A 3.118 3.118 0 0 1 7.937 8.75
                arcTo(
                    horizontalEllipseRadius = 3.118f,
                    verticalEllipseRadius = 3.118f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.937f,
                    y1 = 8.75f,
                )
                // A 3.118 3.118 0 0 1 5.58 7.676
                arcTo(
                    horizontalEllipseRadius = 3.118f,
                    verticalEllipseRadius = 3.118f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 5.58f,
                    y1 = 7.676f,
                )
                // a 1.875 1.875 0 1 1 -0.366 -3.583
                arcToRelative(
                    a = 1.875f,
                    b = 1.875f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.366f,
                    dy1 = -3.583f,
                )
                // a 3.124 3.124 0 0 1 5.462 0.024
                arcToRelative(
                    a = 3.124f,
                    b = 3.124f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 5.462f,
                    dy1 = 0.024f,
                )
                // a 1.875 1.875 0 1 1 -0.346 3.52z
                arcToRelative(
                    a = 1.875f,
                    b = 1.875f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.346f,
                    dy1 = 3.52f,
                )
                close()
                // M 7.753 4.128
                moveTo(x = 7.753f, y = 4.128f)
                // c -0.204 0.123 -0.346 0.397 -0.31 0.623
                curveToRelative(
                    dx1 = -0.204f,
                    dy1 = 0.123f,
                    dx2 = -0.346f,
                    dy2 = 0.397f,
                    dx3 = -0.31f,
                    dy3 = 0.623f,
                )
                // l 0.216 1.374
                lineToRelative(dx = 0.216f, dy = 1.374f)
                // h 0.674
                horizontalLineToRelative(dx = 0.674f)
                // l 0.223 -1.423
                lineToRelative(dx = 0.223f, dy = -1.423f)
                // a 0.529 0.529 0 0 0 -0.255 -0.533
                arcToRelative(
                    a = 0.529f,
                    b = 0.529f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.255f,
                    dy1 = -0.533f,
                )
                // l -0.044 -0.027
                lineToRelative(dx = -0.044f, dy = -0.027f)
                // a 0.474 0.474 0 0 0 -0.504 -0.014z
                arcToRelative(
                    a = 0.474f,
                    b = 0.474f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.504f,
                    dy1 = -0.014f,
                )
                close()
                // M 8 7.188
                moveTo(x = 8.0f, y = 7.188f)
                // a 0.375 0.375 0 1 0 0 -0.75
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.75f,
                )
                // a 0.375 0.375 0 0 0 0 0.75z
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.75f,
                )
                close()
            }
            // M8 16 A8 8 0 1 1 8 0 a8 8 0 0 1 0 16Z m0 -1.3 A6.7 6.7 0 1 0 8 1.3 a6.7 6.7 0 0 0 0 13.4Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8 16
                moveTo(x = 8.0f, y = 16.0f)
                // A 8 8 0 1 1 8 0
                arcTo(
                    horizontalEllipseRadius = 8.0f,
                    verticalEllipseRadius = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 8.0f,
                    y1 = 0.0f,
                )
                // a 8 8 0 0 1 0 16z
                arcToRelative(
                    a = 8.0f,
                    b = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 16.0f,
                )
                close()
                // m 0 -1.3
                moveToRelative(dx = 0.0f, dy = -1.3f)
                // A 6.7 6.7 0 1 0 8 1.3
                arcTo(
                    horizontalEllipseRadius = 6.7f,
                    verticalEllipseRadius = 6.7f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    x1 = 8.0f,
                    y1 = 1.3f,
                )
                // a 6.7 6.7 0 0 0 0 13.4z
                arcToRelative(
                    a = 6.7f,
                    b = 6.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 13.4f,
                )
                close()
            }
        }.build().also { _ic2425 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2425: ImageVector? = null
