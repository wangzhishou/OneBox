package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1801: ImageVector
    get() {
        val current = _ic1801
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1801",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M15 8 A7 7 0 1 1 1 8 a7 7 0 0 1 14 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 15 8
                moveTo(x = 15.0f, y = 8.0f)
                // A 7 7 0 1 1 1 8
                arcTo(
                    horizontalEllipseRadius = 7.0f,
                    verticalEllipseRadius = 7.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 1.0f,
                    y1 = 8.0f,
                )
                // a 7 7 0 0 1 14 0z
                arcToRelative(
                    a = 7.0f,
                    b = 7.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 14.0f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic1801 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1801: ImageVector? = null
