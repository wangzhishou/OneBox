package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2051: ImageVector
    get() {
        val current = _ic2051
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2051",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M2 7 h6 a1 1 0 1 0 -.943 -1.333 .5 .5 0 1 1 -.943 -.334 A2 2 0 1 1 8 8 H2 a.5 .5 0 0 1 0 -1Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 2 7
                moveTo(x = 2.0f, y = 7.0f)
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
                // A 2 2 0 1 1 8 8
                arcTo(
                    horizontalEllipseRadius = 2.0f,
                    verticalEllipseRadius = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 8.0f,
                    y1 = 8.0f,
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
            // M11.079 6.375 A2.5 2.5 0 0 1 16 7 c0 1.397 -1.24 2.5 -2.5 2.5 H.5 a.5 .5 0 0 1 0 -1 h13 c.74 0 1.5 -.688 1.5 -1.5 a1.5 1.5 0 0 0 -2.953 -.375 .5 .5 0 1 1 -.968 -.25Z M2.5 10.5 A.5 .5 0 0 1 3 10 h8 a2 2 0 1 1 -1.886 2.667 .5 .5 0 1 1 .943 -.334 A1 1 0 1 0 11 11 H3 a.5 .5 0 0 1 -.5 -.5Z m0 -5.5 a2.5 2.5 0 1 0 0 -5 2.5 2.5 0 0 0 0 5Z m-.39 -3.579 c-.02 -.176 .16 -.327 .39 -.327 s.41 .151 .39 .327 L2.712 2.97 h-.426 L2.11 1.42Z m.705 2.173 a.312 .312 0 1 1 -.625 0 .312 .312 0 0 1 .625 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.079 6.375
                moveTo(x = 11.079f, y = 6.375f)
                // A 2.5 2.5 0 0 1 16 7
                arcTo(
                    horizontalEllipseRadius = 2.5f,
                    verticalEllipseRadius = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 16.0f,
                    y1 = 7.0f,
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
                // M 2.5 10.5
                moveTo(x = 2.5f, y = 10.5f)
                // A 0.5 0.5 0 0 1 3 10
                arcTo(
                    horizontalEllipseRadius = 0.5f,
                    verticalEllipseRadius = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 3.0f,
                    y1 = 10.0f,
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
                // A 1 1 0 1 0 11 11
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    x1 = 11.0f,
                    y1 = 11.0f,
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
                // m 0 -5.5
                moveToRelative(dx = 0.0f, dy = -5.5f)
                // a 2.5 2.5 0 1 0 0 -5
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -5.0f,
                )
                // a 2.5 2.5 0 0 0 0 5z
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 5.0f,
                )
                close()
                // m -0.39 -3.579
                moveToRelative(dx = -0.39f, dy = -3.579f)
                // c -0.02 -0.176 0.16 -0.327 0.39 -0.327
                curveToRelative(
                    dx1 = -0.02f,
                    dy1 = -0.176f,
                    dx2 = 0.16f,
                    dy2 = -0.327f,
                    dx3 = 0.39f,
                    dy3 = -0.327f,
                )
                // s 0.41 0.151 0.39 0.327
                reflectiveCurveToRelative(
                    dx1 = 0.41f,
                    dy1 = 0.151f,
                    dx2 = 0.39f,
                    dy2 = 0.327f,
                )
                // L 2.712 2.97
                lineTo(x = 2.712f, y = 2.97f)
                // h -0.426
                horizontalLineToRelative(dx = -0.426f)
                // L 2.11 1.42z
                lineTo(x = 2.11f, y = 1.42f)
                close()
                // m 0.705 2.173
                moveToRelative(dx = 0.705f, dy = 2.173f)
                // a 0.312 0.312 0 1 1 -0.625 0
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.625f,
                    dy1 = 0.0f,
                )
                // a 0.312 0.312 0 0 1 0.625 0z
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.625f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic2051 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2051: ImageVector? = null
