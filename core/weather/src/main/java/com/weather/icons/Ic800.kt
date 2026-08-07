package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic800: ImageVector
    get() {
        val current = _ic800
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic800",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8 16 A8 8 0 1 0 8 0 a8 8 0 0 0 0 16Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8 16
                moveTo(x = 8.0f, y = 16.0f)
                // A 8 8 0 1 0 8 0
                arcTo(
                    horizontalEllipseRadius = 8.0f,
                    verticalEllipseRadius = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    x1 = 8.0f,
                    y1 = 0.0f,
                )
                // a 8 8 0 0 0 0 16z
                arcToRelative(
                    a = 8.0f,
                    b = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 16.0f,
                )
                close()
            }
        }.build().also { _ic800 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic800: ImageVector? = null
