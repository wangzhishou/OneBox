package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1603: ImageVector
    get() {
        val current = _ic1603
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1603",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M2.8 2.4 a1.2 1.2 0 1 1 0 -2.4 1.2 1.2 0 0 1 0 2.4Z m-.11 1.505 L1.317 3 0 16 h14 l-3.498 -5.768 -3.349 -.98 -1.032 -1.928 -2.277 -1.102 L2.69 3.905Z M9.5 8 a1.5 1.5 0 1 1 0 -3 1.5 1.5 0 0 1 0 3Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 2.8 2.4
                moveTo(x = 2.8f, y = 2.4f)
                // a 1.2 1.2 0 1 1 0 -2.4
                arcToRelative(
                    a = 1.2f,
                    b = 1.2f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -2.4f,
                )
                // a 1.2 1.2 0 0 1 0 2.4z
                arcToRelative(
                    a = 1.2f,
                    b = 1.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 2.4f,
                )
                close()
                // m -0.11 1.505
                moveToRelative(dx = -0.11f, dy = 1.505f)
                // L 1.317 3
                lineTo(x = 1.317f, y = 3.0f)
                // L 0 16
                lineTo(x = 0.0f, y = 16.0f)
                // h 14
                horizontalLineToRelative(dx = 14.0f)
                // l -3.498 -5.768
                lineToRelative(dx = -3.498f, dy = -5.768f)
                // l -3.349 -0.98
                lineToRelative(dx = -3.349f, dy = -0.98f)
                // l -1.032 -1.928
                lineToRelative(dx = -1.032f, dy = -1.928f)
                // l -2.277 -1.102
                lineToRelative(dx = -2.277f, dy = -1.102f)
                // L 2.69 3.905z
                lineTo(x = 2.69f, y = 3.905f)
                close()
                // M 9.5 8
                moveTo(x = 9.5f, y = 8.0f)
                // a 1.5 1.5 0 1 1 0 -3
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -3.0f,
                )
                // a 1.5 1.5 0 0 1 0 3z
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 3.0f,
                )
                close()
            }
            // M4.6 5.2 a1.2 1.2 0 1 0 2.4 0 1.2 1.2 0 0 0 -2.4 0Z M12 4 a1 1 0 1 0 2 0 1 1 0 0 0 -2 0Z m1 6 a1 1 0 1 1 0 -2 1 1 0 0 1 0 2Z M7 3 a1 1 0 1 0 2 0 1 1 0 0 0 -2 0Z m7.5 11 a1.5 1.5 0 1 1 0 -3 1.5 1.5 0 0 1 0 3Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.6 5.2
                moveTo(x = 4.6f, y = 5.2f)
                // a 1.2 1.2 0 1 0 2.4 0
                arcToRelative(
                    a = 1.2f,
                    b = 1.2f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 2.4f,
                    dy1 = 0.0f,
                )
                // a 1.2 1.2 0 0 0 -2.4 0z
                arcToRelative(
                    a = 1.2f,
                    b = 1.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.4f,
                    dy1 = 0.0f,
                )
                close()
                // M 12 4
                moveTo(x = 12.0f, y = 4.0f)
                // a 1 1 0 1 0 2 0
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 2.0f,
                    dy1 = 0.0f,
                )
                // a 1 1 0 0 0 -2 0z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.0f,
                    dy1 = 0.0f,
                )
                close()
                // m 1 6
                moveToRelative(dx = 1.0f, dy = 6.0f)
                // a 1 1 0 1 1 0 -2
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -2.0f,
                )
                // a 1 1 0 0 1 0 2z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 2.0f,
                )
                close()
                // M 7 3
                moveTo(x = 7.0f, y = 3.0f)
                // a 1 1 0 1 0 2 0
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 2.0f,
                    dy1 = 0.0f,
                )
                // a 1 1 0 0 0 -2 0z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.0f,
                    dy1 = 0.0f,
                )
                close()
                // m 7.5 11
                moveToRelative(dx = 7.5f, dy = 11.0f)
                // a 1.5 1.5 0 1 1 0 -3
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -3.0f,
                )
                // a 1.5 1.5 0 0 1 0 3z
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 3.0f,
                )
                close()
            }
        }.build().also { _ic1603 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1603: ImageVector? = null
