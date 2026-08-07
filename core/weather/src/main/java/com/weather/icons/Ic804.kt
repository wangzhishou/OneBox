package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic804: ImageVector
    get() {
        val current = _ic804
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic804",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8 1 a7 7 0 1 1 -7 7 7.008 7.008 0 0 1 7 -7Z m0 -1 a8 8 0 1 0 0 16 A8 8 0 0 0 8 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8 1
                moveTo(x = 8.0f, y = 1.0f)
                // a 7 7 0 1 1 -7 7
                arcToRelative(
                    a = 7.0f,
                    b = 7.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -7.0f,
                    dy1 = 7.0f,
                )
                // a 7.008 7.008 0 0 1 7 -7z
                arcToRelative(
                    a = 7.008f,
                    b = 7.008f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 7.0f,
                    dy1 = -7.0f,
                )
                close()
                // m 0 -1
                moveToRelative(dx = 0.0f, dy = -1.0f)
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
            }
        }.build().also { _ic804 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic804: ImageVector? = null
