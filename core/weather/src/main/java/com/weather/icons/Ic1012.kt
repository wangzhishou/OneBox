package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1012: ImageVector
    get() {
        val current = _ic1012
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1012",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M5 10 a.5 .5 0 0 1 .5 .5 V14 a2 2 0 1 1 -2.665 -1.887 .5 .5 0 0 1 .332 .944 A1 1 0 1 0 4.5 14 v-3.5 A.5 .5 0 0 1 5 10Z m3 1 c.276 0 .5 .18 .5 .4 v3.2 c0 .22 -.224 .4 -.5 .4 s-.5 -.18 -.5 -.4 v-3.2 c0 -.22 .224 -.4 .5 -.4Z m2.5 -.5 a.5 .5 0 0 1 1 0 V14 a1 1 0 1 0 1.332 -.944 .5 .5 0 1 1 .332 -.943 A2 2 0 1 1 10.5 14 v-3.5Z M7.9 10 a4.99 4.99 0 0 0 3.827 -1.783 3 3 0 1 0 .553 -5.63 A4.999 4.999 0 0 0 7.9 0 a4.998 4.998 0 0 0 -4.359 2.549 3 3 0 1 0 .586 5.732 A4.988 4.988 0 0 0 7.9 10Z m-.791 -6.398 c-.057 -.362 .17 -.8 .496 -.997 .256 -.153 .551 -.133 .806 .023 l.07 .042 a.846 .846 0 0 1 .409 .853 L8.532 5.8 H7.454 L7.11 3.602Z M8.599 6.9 a.6 .6 0 1 1 -1.2 0 .6 .6 0 0 1 1.2 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5 10
                moveTo(x = 5.0f, y = 10.0f)
                // a 0.5 0.5 0 0 1 0.5 0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = 0.5f,
                )
                // V 14
                verticalLineTo(y = 14.0f)
                // a 2 2 0 1 1 -2.665 -1.887
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -2.665f,
                    dy1 = -1.887f,
                )
                // a 0.5 0.5 0 0 1 0.332 0.944
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.332f,
                    dy1 = 0.944f,
                )
                // A 1 1 0 1 0 4.5 14
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    x1 = 4.5f,
                    y1 = 14.0f,
                )
                // v -3.5
                verticalLineToRelative(dy = -3.5f)
                // A 0.5 0.5 0 0 1 5 10z
                arcTo(
                    horizontalEllipseRadius = 0.5f,
                    verticalEllipseRadius = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 5.0f,
                    y1 = 10.0f,
                )
                close()
                // m 3 1
                moveToRelative(dx = 3.0f, dy = 1.0f)
                // c 0.276 0 0.5 0.18 0.5 0.4
                curveToRelative(
                    dx1 = 0.276f,
                    dy1 = 0.0f,
                    dx2 = 0.5f,
                    dy2 = 0.18f,
                    dx3 = 0.5f,
                    dy3 = 0.4f,
                )
                // v 3.2
                verticalLineToRelative(dy = 3.2f)
                // c 0 0.22 -0.224 0.4 -0.5 0.4
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.22f,
                    dx2 = -0.224f,
                    dy2 = 0.4f,
                    dx3 = -0.5f,
                    dy3 = 0.4f,
                )
                // s -0.5 -0.18 -0.5 -0.4
                reflectiveCurveToRelative(
                    dx1 = -0.5f,
                    dy1 = -0.18f,
                    dx2 = -0.5f,
                    dy2 = -0.4f,
                )
                // v -3.2
                verticalLineToRelative(dy = -3.2f)
                // c 0 -0.22 0.224 -0.4 0.5 -0.4z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.22f,
                    dx2 = 0.224f,
                    dy2 = -0.4f,
                    dx3 = 0.5f,
                    dy3 = -0.4f,
                )
                close()
                // m 2.5 -0.5
                moveToRelative(dx = 2.5f, dy = -0.5f)
                // a 0.5 0.5 0 0 1 1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // V 14
                verticalLineTo(y = 14.0f)
                // a 1 1 0 1 0 1.332 -0.944
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 1.332f,
                    dy1 = -0.944f,
                )
                // a 0.5 0.5 0 1 1 0.332 -0.943
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.332f,
                    dy1 = -0.943f,
                )
                // A 2 2 0 1 1 10.5 14
                arcTo(
                    horizontalEllipseRadius = 2.0f,
                    verticalEllipseRadius = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 10.5f,
                    y1 = 14.0f,
                )
                // v -3.5z
                verticalLineToRelative(dy = -3.5f)
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
        }.build().also { _ic1012 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1012: ImageVector? = null
