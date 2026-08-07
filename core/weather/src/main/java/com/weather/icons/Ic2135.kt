package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2135: ImageVector
    get() {
        val current = _ic2135
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2135",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.9 10 a4.99 4.99 0 0 0 3.827 -1.783 3 3 0 1 0 .553 -5.63 A4.999 4.999 0 0 0 7.9 0 a4.998 4.998 0 0 0 -4.359 2.549 3 3 0 1 0 .586 5.732 A4.988 4.988 0 0 0 7.9 10Z m-.791 -6.398 c-.057 -.362 .17 -.8 .496 -.997 .256 -.153 .551 -.133 .806 .023 l.07 .042 a.846 .846 0 0 1 .409 .853 L8.532 5.8 H7.454 L7.11 3.602Z M8.599 6.9 a.6 .6 0 1 1 -1.2 0 .6 .6 0 0 1 1.2 0Z m-6.653 5.725 a.427 .427 0 0 0 -.146 -.57 .391 .391 0 0 0 -.546 .154 l-1.2 2.166 a.428 .428 0 0 0 .146 .57 .391 .391 0 0 0 .546 -.154 l1.2 -2.166Z m3 0 a.427 .427 0 0 0 -.146 -.57 .391 .391 0 0 0 -.546 .154 l-1.2 2.166 a.428 .428 0 0 0 .146 .57 .391 .391 0 0 0 .546 -.154 l1.2 -2.166Z m7.854 -.569 c.191 .115 .257 .37 .146 .57 l-1.2 2.165 a.391 .391 0 0 1 -.546 .153 .427 .427 0 0 1 -.146 -.57 l1.2 -2.165 a.39 .39 0 0 1 .546 -.153Z m3.146 .569 a.427 .427 0 0 0 -.146 -.57 .391 .391 0 0 0 -.546 .154 l-1.2 2.166 a.428 .428 0 0 0 .146 .57 .391 .391 0 0 0 .546 -.154 l1.2 -2.166Z m-7.486 .001 c-.054 0 -.089 -.05 -.065 -.093 l.792 -1.438 C9.21 11.05 9.176 11 9.122 11 H7.544 a.147 .147 0 0 0 -.076 .02 .158 .158 0 0 0 -.058 .057 l-1.397 2.637 c-.042 .079 .022 .17 .118 .17 h1.42 c.05 0 .084 .043 .069 .086 l-.739 1.943 c-.027 .07 .072 .118 .124 .063 l2.978 -3.243 c.04 -.042 .006 -.107 -.055 -.107 H8.46Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
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
                // m -6.653 5.725
                moveToRelative(dx = -6.653f, dy = 5.725f)
                // a 0.427 0.427 0 0 0 -0.146 -0.57
                arcToRelative(
                    a = 0.427f,
                    b = 0.427f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.146f,
                    dy1 = -0.57f,
                )
                // a 0.391 0.391 0 0 0 -0.546 0.154
                arcToRelative(
                    a = 0.391f,
                    b = 0.391f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.546f,
                    dy1 = 0.154f,
                )
                // l -1.2 2.166
                lineToRelative(dx = -1.2f, dy = 2.166f)
                // a 0.428 0.428 0 0 0 0.146 0.57
                arcToRelative(
                    a = 0.428f,
                    b = 0.428f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.146f,
                    dy1 = 0.57f,
                )
                // a 0.391 0.391 0 0 0 0.546 -0.154
                arcToRelative(
                    a = 0.391f,
                    b = 0.391f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.546f,
                    dy1 = -0.154f,
                )
                // l 1.2 -2.166z
                lineToRelative(dx = 1.2f, dy = -2.166f)
                close()
                // m 3 0
                moveToRelative(dx = 3.0f, dy = 0.0f)
                // a 0.427 0.427 0 0 0 -0.146 -0.57
                arcToRelative(
                    a = 0.427f,
                    b = 0.427f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.146f,
                    dy1 = -0.57f,
                )
                // a 0.391 0.391 0 0 0 -0.546 0.154
                arcToRelative(
                    a = 0.391f,
                    b = 0.391f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.546f,
                    dy1 = 0.154f,
                )
                // l -1.2 2.166
                lineToRelative(dx = -1.2f, dy = 2.166f)
                // a 0.428 0.428 0 0 0 0.146 0.57
                arcToRelative(
                    a = 0.428f,
                    b = 0.428f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.146f,
                    dy1 = 0.57f,
                )
                // a 0.391 0.391 0 0 0 0.546 -0.154
                arcToRelative(
                    a = 0.391f,
                    b = 0.391f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.546f,
                    dy1 = -0.154f,
                )
                // l 1.2 -2.166z
                lineToRelative(dx = 1.2f, dy = -2.166f)
                close()
                // m 7.854 -0.569
                moveToRelative(dx = 7.854f, dy = -0.569f)
                // c 0.191 0.115 0.257 0.37 0.146 0.57
                curveToRelative(
                    dx1 = 0.191f,
                    dy1 = 0.115f,
                    dx2 = 0.257f,
                    dy2 = 0.37f,
                    dx3 = 0.146f,
                    dy3 = 0.57f,
                )
                // l -1.2 2.165
                lineToRelative(dx = -1.2f, dy = 2.165f)
                // a 0.391 0.391 0 0 1 -0.546 0.153
                arcToRelative(
                    a = 0.391f,
                    b = 0.391f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.546f,
                    dy1 = 0.153f,
                )
                // a 0.427 0.427 0 0 1 -0.146 -0.57
                arcToRelative(
                    a = 0.427f,
                    b = 0.427f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.146f,
                    dy1 = -0.57f,
                )
                // l 1.2 -2.165
                lineToRelative(dx = 1.2f, dy = -2.165f)
                // a 0.39 0.39 0 0 1 0.546 -0.153z
                arcToRelative(
                    a = 0.39f,
                    b = 0.39f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.546f,
                    dy1 = -0.153f,
                )
                close()
                // m 3.146 0.569
                moveToRelative(dx = 3.146f, dy = 0.569f)
                // a 0.427 0.427 0 0 0 -0.146 -0.57
                arcToRelative(
                    a = 0.427f,
                    b = 0.427f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.146f,
                    dy1 = -0.57f,
                )
                // a 0.391 0.391 0 0 0 -0.546 0.154
                arcToRelative(
                    a = 0.391f,
                    b = 0.391f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.546f,
                    dy1 = 0.154f,
                )
                // l -1.2 2.166
                lineToRelative(dx = -1.2f, dy = 2.166f)
                // a 0.428 0.428 0 0 0 0.146 0.57
                arcToRelative(
                    a = 0.428f,
                    b = 0.428f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.146f,
                    dy1 = 0.57f,
                )
                // a 0.391 0.391 0 0 0 0.546 -0.154
                arcToRelative(
                    a = 0.391f,
                    b = 0.391f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.546f,
                    dy1 = -0.154f,
                )
                // l 1.2 -2.166z
                lineToRelative(dx = 1.2f, dy = -2.166f)
                close()
                // m -7.486 0.001
                moveToRelative(dx = -7.486f, dy = 0.001f)
                // c -0.054 0 -0.089 -0.05 -0.065 -0.093
                curveToRelative(
                    dx1 = -0.054f,
                    dy1 = 0.0f,
                    dx2 = -0.089f,
                    dy2 = -0.05f,
                    dx3 = -0.065f,
                    dy3 = -0.093f,
                )
                // l 0.792 -1.438
                lineToRelative(dx = 0.792f, dy = -1.438f)
                // C 9.21 11.05 9.176 11 9.122 11
                curveTo(
                    x1 = 9.21f,
                    y1 = 11.05f,
                    x2 = 9.176f,
                    y2 = 11.0f,
                    x3 = 9.122f,
                    y3 = 11.0f,
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
                // a 0.158 0.158 0 0 0 -0.058 0.057
                arcToRelative(
                    a = 0.158f,
                    b = 0.158f,
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
            }
        }.build().also { _ic2135 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2135: ImageVector? = null
