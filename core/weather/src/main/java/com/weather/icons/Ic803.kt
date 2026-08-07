package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic803: ImageVector
    get() {
        val current = _ic803
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic803",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8 0 a8 8 0 1 0 0 16 A8 8 0 0 0 8 0Z m0 15 a6.73 6.73 0 0 1 -.948 -.072 .486 .486 0 0 1 -.24 -.106 A8.838 8.838 0 0 1 3.962 8 8.868 8.868 0 0 1 6.76 1.22 a.702 .702 0 0 1 .359 -.157 c.292 -.04 .586 -.062 .881 -.063 a7 7 0 0 1 0 14Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8 0
                moveTo(x = 8.0f, y = 0.0f)
                // a 8 8 0 1 0 0 16
                arcToRelative(
                    a = 8.0f,
                    b = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 16.0f,
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
                // m 0 15
                moveToRelative(dx = 0.0f, dy = 15.0f)
                // a 6.73 6.73 0 0 1 -0.948 -0.072
                arcToRelative(
                    a = 6.73f,
                    b = 6.73f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.948f,
                    dy1 = -0.072f,
                )
                // a 0.486 0.486 0 0 1 -0.24 -0.106
                arcToRelative(
                    a = 0.486f,
                    b = 0.486f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.24f,
                    dy1 = -0.106f,
                )
                // A 8.838 8.838 0 0 1 3.962 8
                arcTo(
                    horizontalEllipseRadius = 8.838f,
                    verticalEllipseRadius = 8.838f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 3.962f,
                    y1 = 8.0f,
                )
                // A 8.868 8.868 0 0 1 6.76 1.22
                arcTo(
                    horizontalEllipseRadius = 8.868f,
                    verticalEllipseRadius = 8.868f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 6.76f,
                    y1 = 1.22f,
                )
                // a 0.702 0.702 0 0 1 0.359 -0.157
                arcToRelative(
                    a = 0.702f,
                    b = 0.702f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.359f,
                    dy1 = -0.157f,
                )
                // c 0.292 -0.04 0.586 -0.062 0.881 -0.063
                curveToRelative(
                    dx1 = 0.292f,
                    dy1 = -0.04f,
                    dx2 = 0.586f,
                    dy2 = -0.062f,
                    dx3 = 0.881f,
                    dy3 = -0.063f,
                )
                // a 7 7 0 0 1 0 14z
                arcToRelative(
                    a = 7.0f,
                    b = 7.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 14.0f,
                )
                close()
            }
        }.build().also { _ic803 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic803: ImageVector? = null
