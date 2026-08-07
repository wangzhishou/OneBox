package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic802: ImageVector
    get() {
        val current = _ic802
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic802",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8 0 a8.03 8.03 0 0 0 -1.065 .079 7.992 7.992 0 0 0 -.354 15.788 h.001 c.468 .087 .942 .131 1.418 .133 A8 8 0 0 0 8 0Z m0 15.5 a6.76 6.76 0 0 1 -.725 -.04 A24.01 24.01 0 0 0 8.5 7.5 25.67 25.67 0 0 0 7.593 .514 C7.734 .504 7.868 .5 8 .5 a7.5 7.5 0 0 1 0 15Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8 0
                moveTo(x = 8.0f, y = 0.0f)
                // a 8.03 8.03 0 0 0 -1.065 0.079
                arcToRelative(
                    a = 8.03f,
                    b = 8.03f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.065f,
                    dy1 = 0.079f,
                )
                // a 7.992 7.992 0 0 0 -0.354 15.788
                arcToRelative(
                    a = 7.992f,
                    b = 7.992f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.354f,
                    dy1 = 15.788f,
                )
                // h 0.001
                horizontalLineToRelative(dx = 0.001f)
                // c 0.468 0.087 0.942 0.131 1.418 0.133
                curveToRelative(
                    dx1 = 0.468f,
                    dy1 = 0.087f,
                    dx2 = 0.942f,
                    dy2 = 0.131f,
                    dx3 = 1.418f,
                    dy3 = 0.133f,
                )
                // A 8 8 0 0 0 8 0z
                arcTo(
                    horizontalEllipseRadius = 8.0f,
                    verticalEllipseRadius = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 8.0f,
                    y1 = 0.0f,
                )
                close()
                // m 0 15.5
                moveToRelative(dx = 0.0f, dy = 15.5f)
                // a 6.76 6.76 0 0 1 -0.725 -0.04
                arcToRelative(
                    a = 6.76f,
                    b = 6.76f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.725f,
                    dy1 = -0.04f,
                )
                // A 24.01 24.01 0 0 0 8.5 7.5
                arcTo(
                    horizontalEllipseRadius = 24.01f,
                    verticalEllipseRadius = 24.01f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 8.5f,
                    y1 = 7.5f,
                )
                // A 25.67 25.67 0 0 0 7.593 0.514
                arcTo(
                    horizontalEllipseRadius = 25.67f,
                    verticalEllipseRadius = 25.67f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.593f,
                    y1 = 0.514f,
                )
                // C 7.734 0.504 7.868 0.5 8 0.5
                curveTo(
                    x1 = 7.734f,
                    y1 = 0.504f,
                    x2 = 7.868f,
                    y2 = 0.5f,
                    x3 = 8.0f,
                    y3 = 0.5f,
                )
                // a 7.5 7.5 0 0 1 0 15z
                arcToRelative(
                    a = 7.5f,
                    b = 7.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 15.0f,
                )
                close()
            }
        }.build().also { _ic802 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic802: ImageVector? = null
