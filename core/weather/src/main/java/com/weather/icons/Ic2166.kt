package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2166: ImageVector
    get() {
        val current = _ic2166
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2166",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
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
            // M8 3.5 c-.733 0 -1.31 .485 -1.245 1.049 L7.318 9.5 h1.364 l.563 -4.951 C9.31 3.985 8.733 3.5 8 3.5Z m.008 9 a1 1 0 1 0 0 -2 1 1 0 0 0 0 2Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8 3.5
                moveTo(x = 8.0f, y = 3.5f)
                // c -0.733 0 -1.31 0.485 -1.245 1.049
                curveToRelative(
                    dx1 = -0.733f,
                    dy1 = 0.0f,
                    dx2 = -1.31f,
                    dy2 = 0.485f,
                    dx3 = -1.245f,
                    dy3 = 1.049f,
                )
                // L 7.318 9.5
                lineTo(x = 7.318f, y = 9.5f)
                // h 1.364
                horizontalLineToRelative(dx = 1.364f)
                // l 0.563 -4.951
                lineToRelative(dx = 0.563f, dy = -4.951f)
                // C 9.31 3.985 8.733 3.5 8 3.5z
                curveTo(
                    x1 = 9.31f,
                    y1 = 3.985f,
                    x2 = 8.733f,
                    y2 = 3.5f,
                    x3 = 8.0f,
                    y3 = 3.5f,
                )
                close()
                // m 0.008 9
                moveToRelative(dx = 0.008f, dy = 9.0f)
                // a 1 1 0 1 0 0 -2
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -2.0f,
                )
                // a 1 1 0 0 0 0 2z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 2.0f,
                )
                close()
            }
        }.build().also { _ic2166 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2166: ImageVector? = null
