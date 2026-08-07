package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic806: ImageVector
    get() {
        val current = _ic806
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic806",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8 0 a8.03 8.03 0 0 0 -1.065 .079 7.992 7.992 0 0 0 -.354 15.788 h.001 c.468 .087 .942 .131 1.418 .133 A8 8 0 0 0 8 0Z M1 8 a7.008 7.008 0 0 1 6.204 -6.951 A25.25 25.25 0 0 1 8 7.5 c.032 2.51 -.328 5.01 -1.067 7.41 A7.005 7.005 0 0 1 1 8Z
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
                // M 1 8
                moveTo(x = 1.0f, y = 8.0f)
                // a 7.008 7.008 0 0 1 6.204 -6.951
                arcToRelative(
                    a = 7.008f,
                    b = 7.008f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 6.204f,
                    dy1 = -6.951f,
                )
                // A 25.25 25.25 0 0 1 8 7.5
                arcTo(
                    horizontalEllipseRadius = 25.25f,
                    verticalEllipseRadius = 25.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 8.0f,
                    y1 = 7.5f,
                )
                // c 0.032 2.51 -0.328 5.01 -1.067 7.41
                curveToRelative(
                    dx1 = 0.032f,
                    dy1 = 2.51f,
                    dx2 = -0.328f,
                    dy2 = 5.01f,
                    dx3 = -1.067f,
                    dy3 = 7.41f,
                )
                // A 7.005 7.005 0 0 1 1 8z
                arcTo(
                    horizontalEllipseRadius = 7.005f,
                    verticalEllipseRadius = 7.005f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 1.0f,
                    y1 = 8.0f,
                )
                close()
            }
        }.build().also { _ic806 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic806: ImageVector? = null
