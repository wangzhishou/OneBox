package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic805: ImageVector
    get() {
        val current = _ic805
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic805",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M0 8 a8 8 0 1 0 16 0 A8 8 0 0 0 0 8Z m1 0 a7.008 7.008 0 0 1 7 -7 c.295 .001 .59 .022 .881 .063 a.702 .702 0 0 1 .36 .157 A8.868 8.868 0 0 1 12.036 8 a8.838 8.838 0 0 1 -2.849 6.822 .486 .486 0 0 1 -.24 .106 A6.73 6.73 0 0 1 8 15 a7.008 7.008 0 0 1 -7 -7Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0 8
                moveTo(x = 0.0f, y = 8.0f)
                // a 8 8 0 1 0 16 0
                arcToRelative(
                    a = 8.0f,
                    b = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 16.0f,
                    dy1 = 0.0f,
                )
                // A 8 8 0 0 0 0 8z
                arcTo(
                    horizontalEllipseRadius = 8.0f,
                    verticalEllipseRadius = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 0.0f,
                    y1 = 8.0f,
                )
                close()
                // m 1 0
                moveToRelative(dx = 1.0f, dy = 0.0f)
                // a 7.008 7.008 0 0 1 7 -7
                arcToRelative(
                    a = 7.008f,
                    b = 7.008f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 7.0f,
                    dy1 = -7.0f,
                )
                // c 0.295 0.001 0.59 0.022 0.881 0.063
                curveToRelative(
                    dx1 = 0.295f,
                    dy1 = 0.001f,
                    dx2 = 0.59f,
                    dy2 = 0.022f,
                    dx3 = 0.881f,
                    dy3 = 0.063f,
                )
                // a 0.702 0.702 0 0 1 0.36 0.157
                arcToRelative(
                    a = 0.702f,
                    b = 0.702f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.36f,
                    dy1 = 0.157f,
                )
                // A 8.868 8.868 0 0 1 12.036 8
                arcTo(
                    horizontalEllipseRadius = 8.868f,
                    verticalEllipseRadius = 8.868f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 12.036f,
                    y1 = 8.0f,
                )
                // a 8.838 8.838 0 0 1 -2.849 6.822
                arcToRelative(
                    a = 8.838f,
                    b = 8.838f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.849f,
                    dy1 = 6.822f,
                )
                // a 0.486 0.486 0 0 1 -0.24 0.106
                arcToRelative(
                    a = 0.486f,
                    b = 0.486f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.24f,
                    dy1 = 0.106f,
                )
                // A 6.73 6.73 0 0 1 8 15
                arcTo(
                    horizontalEllipseRadius = 6.73f,
                    verticalEllipseRadius = 6.73f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 8.0f,
                    y1 = 15.0f,
                )
                // a 7.008 7.008 0 0 1 -7 -7z
                arcToRelative(
                    a = 7.008f,
                    b = 7.008f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -7.0f,
                    dy1 = -7.0f,
                )
                close()
            }
        }.build().also { _ic805 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic805: ImageVector? = null
