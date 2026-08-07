package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1011: ImageVector
    get() {
        val current = _ic1011
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1011",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M4.9 1.5 c-.9 .3 -1.2 1.1 -1.1 1.6 -.7 -.8 -.7 -1.7 -.6 -3.1 -2.1 .8 -1.6 3.2 -1.7 4 C1 3.5 .9 2.5 .9 2.5 .3 2.8 0 3.6 0 4.3 0 5.9 1.3 7 2.8 7 c1.5 0 2.7 -1.2 2.7 -2.7 0 -1.1 -.6 -1.4 -.6 -2.8Z M2 9 h6 a1 1 0 1 0 -.943 -1.333 .5 .5 0 1 1 -.943 -.334 A2 2 0 1 1 8 10 H2 a.5 .5 0 0 1 0 -1Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.9 1.5
                moveTo(x = 4.9f, y = 1.5f)
                // c -0.9 0.3 -1.2 1.1 -1.1 1.6
                curveToRelative(
                    dx1 = -0.9f,
                    dy1 = 0.3f,
                    dx2 = -1.2f,
                    dy2 = 1.1f,
                    dx3 = -1.1f,
                    dy3 = 1.6f,
                )
                // c -0.7 -0.8 -0.7 -1.7 -0.6 -3.1
                curveToRelative(
                    dx1 = -0.7f,
                    dy1 = -0.8f,
                    dx2 = -0.7f,
                    dy2 = -1.7f,
                    dx3 = -0.6f,
                    dy3 = -3.1f,
                )
                // c -2.1 0.8 -1.6 3.2 -1.7 4
                curveToRelative(
                    dx1 = -2.1f,
                    dy1 = 0.8f,
                    dx2 = -1.6f,
                    dy2 = 3.2f,
                    dx3 = -1.7f,
                    dy3 = 4.0f,
                )
                // C 1 3.5 0.9 2.5 0.9 2.5
                curveTo(
                    x1 = 1.0f,
                    y1 = 3.5f,
                    x2 = 0.9f,
                    y2 = 2.5f,
                    x3 = 0.9f,
                    y3 = 2.5f,
                )
                // C 0.3 2.8 0 3.6 0 4.3
                curveTo(
                    x1 = 0.3f,
                    y1 = 2.8f,
                    x2 = 0.0f,
                    y2 = 3.6f,
                    x3 = 0.0f,
                    y3 = 4.3f,
                )
                // C 0 5.9 1.3 7 2.8 7
                curveTo(
                    x1 = 0.0f,
                    y1 = 5.9f,
                    x2 = 1.3f,
                    y2 = 7.0f,
                    x3 = 2.8f,
                    y3 = 7.0f,
                )
                // c 1.5 0 2.7 -1.2 2.7 -2.7
                curveToRelative(
                    dx1 = 1.5f,
                    dy1 = 0.0f,
                    dx2 = 2.7f,
                    dy2 = -1.2f,
                    dx3 = 2.7f,
                    dy3 = -2.7f,
                )
                // c 0 -1.1 -0.6 -1.4 -0.6 -2.8z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.1f,
                    dx2 = -0.6f,
                    dy2 = -1.4f,
                    dx3 = -0.6f,
                    dy3 = -2.8f,
                )
                close()
                // M 2 9
                moveTo(x = 2.0f, y = 9.0f)
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
                // A 2 2 0 1 1 8 10
                arcTo(
                    horizontalEllipseRadius = 2.0f,
                    verticalEllipseRadius = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 8.0f,
                    y1 = 10.0f,
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
            // M11.079 8.375 A2.5 2.5 0 0 1 16 9 c0 1.397 -1.24 2.5 -2.5 2.5 H.5 a.5 .5 0 0 1 0 -1 h13 c.74 0 1.5 -.688 1.5 -1.5 a1.5 1.5 0 0 0 -2.953 -.375 .5 .5 0 1 1 -.968 -.25Z M2.5 12.5 A.5 .5 0 0 1 3 12 h8 a2 2 0 1 1 -1.886 2.667 .5 .5 0 1 1 .943 -.334 A1 1 0 1 0 11 13 H3 a.5 .5 0 0 1 -.5 -.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.079 8.375
                moveTo(x = 11.079f, y = 8.375f)
                // A 2.5 2.5 0 0 1 16 9
                arcTo(
                    horizontalEllipseRadius = 2.5f,
                    verticalEllipseRadius = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 16.0f,
                    y1 = 9.0f,
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
                // M 2.5 12.5
                moveTo(x = 2.5f, y = 12.5f)
                // A 0.5 0.5 0 0 1 3 12
                arcTo(
                    horizontalEllipseRadius = 0.5f,
                    verticalEllipseRadius = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 3.0f,
                    y1 = 12.0f,
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
                // A 1 1 0 1 0 11 13
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    x1 = 11.0f,
                    y1 = 13.0f,
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
            }
        }.build().also { _ic1011 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1011: ImageVector? = null
