package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic900Fill: ImageVector
    get() {
        val current = _ic900Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic900Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M11.5 0 A2.5 2.5 0 0 0 9 2.5 v4.99 a.534 .534 0 0 1 -.217 .423 4.5 4.5 0 1 0 5.435 0 A.534 .534 0 0 1 14 7.49 V2.5 A2.5 2.5 0 0 0 11.5 0Z m2 11.5 A2 2 0 1 1 11 9.563 V3.5 a.5 .5 0 0 1 1 0 v6.063 a2 2 0 0 1 1.5 1.937Z m-8.6 -9 c-.9 .3 -1.2 1.1 -1.1 1.6 -.7 -.8 -.7 -1.7 -.6 -3.1 -2.1 .8 -1.6 3.2 -1.7 4 C1 4.5 .9 3.5 .9 3.5 .3 3.8 0 4.6 0 5.3 0 6.9 1.3 8 2.8 8 c1.5 0 2.7 -1.2 2.7 -2.7 0 -1.1 -.6 -1.4 -.6 -2.8Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.5 0
                moveTo(x = 11.5f, y = 0.0f)
                // A 2.5 2.5 0 0 0 9 2.5
                arcTo(
                    horizontalEllipseRadius = 2.5f,
                    verticalEllipseRadius = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 9.0f,
                    y1 = 2.5f,
                )
                // v 4.99
                verticalLineToRelative(dy = 4.99f)
                // a 0.534 0.534 0 0 1 -0.217 0.423
                arcToRelative(
                    a = 0.534f,
                    b = 0.534f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.217f,
                    dy1 = 0.423f,
                )
                // a 4.5 4.5 0 1 0 5.435 0
                arcToRelative(
                    a = 4.5f,
                    b = 4.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 5.435f,
                    dy1 = 0.0f,
                )
                // A 0.534 0.534 0 0 1 14 7.49
                arcTo(
                    horizontalEllipseRadius = 0.534f,
                    verticalEllipseRadius = 0.534f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 14.0f,
                    y1 = 7.49f,
                )
                // V 2.5
                verticalLineTo(y = 2.5f)
                // A 2.5 2.5 0 0 0 11.5 0z
                arcTo(
                    horizontalEllipseRadius = 2.5f,
                    verticalEllipseRadius = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 11.5f,
                    y1 = 0.0f,
                )
                close()
                // m 2 11.5
                moveToRelative(dx = 2.0f, dy = 11.5f)
                // A 2 2 0 1 1 11 9.563
                arcTo(
                    horizontalEllipseRadius = 2.0f,
                    verticalEllipseRadius = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 11.0f,
                    y1 = 9.563f,
                )
                // V 3.5
                verticalLineTo(y = 3.5f)
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
                // v 6.063
                verticalLineToRelative(dy = 6.063f)
                // a 2 2 0 0 1 1.5 1.937z
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.5f,
                    dy1 = 1.937f,
                )
                close()
                // m -8.6 -9
                moveToRelative(dx = -8.6f, dy = -9.0f)
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
                // C 1 4.5 0.9 3.5 0.9 3.5
                curveTo(
                    x1 = 1.0f,
                    y1 = 4.5f,
                    x2 = 0.9f,
                    y2 = 3.5f,
                    x3 = 0.9f,
                    y3 = 3.5f,
                )
                // C 0.3 3.8 0 4.6 0 5.3
                curveTo(
                    x1 = 0.3f,
                    y1 = 3.8f,
                    x2 = 0.0f,
                    y2 = 4.6f,
                    x3 = 0.0f,
                    y3 = 5.3f,
                )
                // C 0 6.9 1.3 8 2.8 8
                curveTo(
                    x1 = 0.0f,
                    y1 = 6.9f,
                    x2 = 1.3f,
                    y2 = 8.0f,
                    x3 = 2.8f,
                    y3 = 8.0f,
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
            }
        }.build().also { _ic900Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic900Fill: ImageVector? = null
