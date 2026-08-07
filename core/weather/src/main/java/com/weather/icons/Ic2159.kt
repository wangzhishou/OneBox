package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2159: ImageVector
    get() {
        val current = _ic2159
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2159",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M16 13.756 3.96 4.333 0 8.293 V16 h16 v-2.244Z M10 2.5 a2.5 2.5 0 1 1 -5 0 2.5 2.5 0 0 1 5 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 16 13.756
                moveTo(x = 16.0f, y = 13.756f)
                // L 3.96 4.333
                lineTo(x = 3.96f, y = 4.333f)
                // L 0 8.293
                lineTo(x = 0.0f, y = 8.293f)
                // V 16
                verticalLineTo(y = 16.0f)
                // h 16
                horizontalLineToRelative(dx = 16.0f)
                // v -2.244z
                verticalLineToRelative(dy = -2.244f)
                close()
                // M 10 2.5
                moveTo(x = 10.0f, y = 2.5f)
                // a 2.5 2.5 0 1 1 -5 0
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -5.0f,
                    dy1 = 0.0f,
                )
                // a 2.5 2.5 0 0 1 5 0z
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 5.0f,
                    dy1 = 0.0f,
                )
                close()
            }
            // M11 6.5 a1.5 1.5 0 1 1 -3 0 1.5 1.5 0 0 1 3 0Z m3 4.5 a2 2 0 1 0 0 -4 2 2 0 0 0 0 4Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11 6.5
                moveTo(x = 11.0f, y = 6.5f)
                // a 1.5 1.5 0 1 1 -3 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -3.0f,
                    dy1 = 0.0f,
                )
                // a 1.5 1.5 0 0 1 3 0z
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 3.0f,
                    dy1 = 0.0f,
                )
                close()
                // m 3 4.5
                moveToRelative(dx = 3.0f, dy = 4.5f)
                // a 2 2 0 1 0 0 -4
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -4.0f,
                )
                // a 2 2 0 0 0 0 4z
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 4.0f,
                )
                close()
            }
        }.build().also { _ic2159 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2159: ImageVector? = null
