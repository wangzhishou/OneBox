package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1020: ImageVector
    get() {
        val current = _ic1020
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1020",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M2 3 h6 a1 1 0 1 0 -.943 -1.333 .5 .5 0 1 1 -.943 -.334 A2 2 0 1 1 8 4 H2 a.5 .5 0 0 1 0 -1Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 2 3
                moveTo(x = 2.0f, y = 3.0f)
                // h 6
                horizontalLineToRelative(dx = 6.0f)
                // a 1 1 0 1 0 -0.943 -1.333
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.943f,
                    dy1 = -1.333f,
                )
                // a 0.5 0.5 0 1 1 -0.943 -0.334
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.943f,
                    dy1 = -0.334f,
                )
                // A 2 2 0 1 1 8 4
                arcTo(
                    horizontalEllipseRadius = 2.0f,
                    verticalEllipseRadius = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 8.0f,
                    y1 = 4.0f,
                )
                // H 2
                horizontalLineTo(x = 2.0f)
                // a 0.5 0.5 0 0 1 0 -1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                close()
            }
            // M11.079 2.375 A2.5 2.5 0 0 1 16 3 c0 1.397 -1.24 2.5 -2.5 2.5 H.5 a.5 .5 0 0 1 0 -1 h13 c.74 0 1.5 -.688 1.5 -1.5 a1.5 1.5 0 0 0 -2.953 -.375 .5 .5 0 1 1 -.968 -.25Z M2.5 6.5 A.5 .5 0 0 1 3 6 h8 a2 2 0 1 1 -1.886 2.667 .5 .5 0 1 1 .943 -.334 A1 1 0 1 0 11 7 H3 a.5 .5 0 0 1 -.5 -.5Z m2.96 6.126 c-.054 0 -.089 -.05 -.065 -.093 l.792 -1.438 C6.21 11.05 6.176 11 6.122 11 H4.544 a.147 .147 0 0 0 -.076 .02 .158 .158 0 0 0 -.058 .057 l-1.397 2.637 c-.042 .079 .022 .17 .118 .17 h1.42 c.05 0 .084 .043 .069 .086 l-.739 1.943 c-.027 .07 .072 .118 .124 .063 l2.978 -3.243 c.04 -.042 .006 -.107 -.055 -.107 H5.46Z m4.698 2.886 a1.666 1.666 0 0 1 -.488 -1.179 c0 -.833 .926 -2.325 1.667 -3.333 .74 1.008 1.666 2.5 1.666 3.333 a1.666 1.666 0 0 1 -2.845 1.179Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.079 2.375
                moveTo(x = 11.079f, y = 2.375f)
                // A 2.5 2.5 0 0 1 16 3
                arcTo(
                    horizontalEllipseRadius = 2.5f,
                    verticalEllipseRadius = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 16.0f,
                    y1 = 3.0f,
                )
                // c 0 1.397 -1.24 2.5 -2.5 2.5
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.397f,
                    dx2 = -1.24f,
                    dy2 = 2.5f,
                    dx3 = -2.5f,
                    dy3 = 2.5f,
                )
                // H 0.5
                horizontalLineTo(x = 0.5f)
                // a 0.5 0.5 0 0 1 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h 13
                horizontalLineToRelative(dx = 13.0f)
                // c 0.74 0 1.5 -0.688 1.5 -1.5
                curveToRelative(
                    dx1 = 0.74f,
                    dy1 = 0.0f,
                    dx2 = 1.5f,
                    dy2 = -0.688f,
                    dx3 = 1.5f,
                    dy3 = -1.5f,
                )
                // a 1.5 1.5 0 0 0 -2.953 -0.375
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.953f,
                    dy1 = -0.375f,
                )
                // a 0.5 0.5 0 1 1 -0.968 -0.25z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.968f,
                    dy1 = -0.25f,
                )
                close()
                // M 2.5 6.5
                moveTo(x = 2.5f, y = 6.5f)
                // A 0.5 0.5 0 0 1 3 6
                arcTo(
                    horizontalEllipseRadius = 0.5f,
                    verticalEllipseRadius = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 3.0f,
                    y1 = 6.0f,
                )
                // h 8
                horizontalLineToRelative(dx = 8.0f)
                // a 2 2 0 1 1 -1.886 2.667
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.886f,
                    dy1 = 2.667f,
                )
                // a 0.5 0.5 0 1 1 0.943 -0.334
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.943f,
                    dy1 = -0.334f,
                )
                // A 1 1 0 1 0 11 7
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    x1 = 11.0f,
                    y1 = 7.0f,
                )
                // H 3
                horizontalLineTo(x = 3.0f)
                // a 0.5 0.5 0 0 1 -0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                close()
                // m 2.96 6.126
                moveToRelative(dx = 2.96f, dy = 6.126f)
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
                // C 6.21 11.05 6.176 11 6.122 11
                curveTo(
                    x1 = 6.21f,
                    y1 = 11.05f,
                    x2 = 6.176f,
                    y2 = 11.0f,
                    x3 = 6.122f,
                    y3 = 11.0f,
                )
                // H 4.544
                horizontalLineTo(x = 4.544f)
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
                // H 5.46z
                horizontalLineTo(x = 5.46f)
                close()
                // m 4.698 2.886
                moveToRelative(dx = 4.698f, dy = 2.886f)
                // a 1.666 1.666 0 0 1 -0.488 -1.179
                arcToRelative(
                    a = 1.666f,
                    b = 1.666f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.488f,
                    dy1 = -1.179f,
                )
                // c 0 -0.833 0.926 -2.325 1.667 -3.333
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.833f,
                    dx2 = 0.926f,
                    dy2 = -2.325f,
                    dx3 = 1.667f,
                    dy3 = -3.333f,
                )
                // c 0.74 1.008 1.666 2.5 1.666 3.333
                curveToRelative(
                    dx1 = 0.74f,
                    dy1 = 1.008f,
                    dx2 = 1.666f,
                    dy2 = 2.5f,
                    dx3 = 1.666f,
                    dy3 = 3.333f,
                )
                // a 1.666 1.666 0 0 1 -2.845 1.179z
                arcToRelative(
                    a = 1.666f,
                    b = 1.666f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.845f,
                    dy1 = 1.179f,
                )
                close()
            }
        }.build().also { _ic1020 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1020: ImageVector? = null
