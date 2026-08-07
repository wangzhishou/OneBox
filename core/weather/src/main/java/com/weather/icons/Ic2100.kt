package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2100: ImageVector
    get() {
        val current = _ic2100
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2100",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M0 4 a1 1 0 0 1 1 -1 h14 a1 1 0 1 1 0 2 H1 a1 1 0 0 1 -1 -1Z m0 4 a1 1 0 0 1 1 -1 h14 a1 1 0 1 1 0 2 H1 a1 1 0 0 1 -1 -1Z m1 3 a1 1 0 1 0 0 2 h14 a1 1 0 1 0 0 -2 H1Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0 4
                moveTo(x = 0.0f, y = 4.0f)
                // a 1 1 0 0 1 1 -1
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = -1.0f,
                )
                // h 14
                horizontalLineToRelative(dx = 14.0f)
                // a 1 1 0 1 1 0 2
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 2.0f,
                )
                // H 1
                horizontalLineTo(x = 1.0f)
                // a 1 1 0 0 1 -1 -1z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = -1.0f,
                )
                close()
                // m 0 4
                moveToRelative(dx = 0.0f, dy = 4.0f)
                // a 1 1 0 0 1 1 -1
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = -1.0f,
                )
                // h 14
                horizontalLineToRelative(dx = 14.0f)
                // a 1 1 0 1 1 0 2
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 2.0f,
                )
                // H 1
                horizontalLineTo(x = 1.0f)
                // a 1 1 0 0 1 -1 -1z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = -1.0f,
                )
                close()
                // m 1 3
                moveToRelative(dx = 1.0f, dy = 3.0f)
                // a 1 1 0 1 0 0 2
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 2.0f,
                )
                // h 14
                horizontalLineToRelative(dx = 14.0f)
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
                // H 1z
                horizontalLineTo(x = 1.0f)
                close()
            }
        }.build().also { _ic2100 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2100: ImageVector? = null
