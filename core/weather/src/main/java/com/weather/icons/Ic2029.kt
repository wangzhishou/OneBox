package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2029: ImageVector
    get() {
        val current = _ic2029
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2029",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M9.353 .196 a.501 .501 0 0 1 .89 .386 L9.34 6 h3.41 a.5 .5 0 0 1 .405 .793 l-6.5 9 a.5 .5 0 0 1 -.9 -.37 L6.668 9.5 H3.25 a.5 .5 0 0 1 -.397 -.804 l6.5 -8.5Z M4.262 8.5 H7.25 a.501 .501 0 0 1 .494 .576 l-.684 4.448 L11.773 7 H8.75 a.5 .5 0 0 1 -.493 -.582 L8.926 2.4 4.262 8.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 9.353 0.196
                moveTo(x = 9.353f, y = 0.196f)
                // a 0.501 0.501 0 0 1 0.89 0.386
                arcToRelative(
                    a = 0.501f,
                    b = 0.501f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.89f,
                    dy1 = 0.386f,
                )
                // L 9.34 6
                lineTo(x = 9.34f, y = 6.0f)
                // h 3.41
                horizontalLineToRelative(dx = 3.41f)
                // a 0.5 0.5 0 0 1 0.405 0.793
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.405f,
                    dy1 = 0.793f,
                )
                // l -6.5 9
                lineToRelative(dx = -6.5f, dy = 9.0f)
                // a 0.5 0.5 0 0 1 -0.9 -0.37
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.9f,
                    dy1 = -0.37f,
                )
                // L 6.668 9.5
                lineTo(x = 6.668f, y = 9.5f)
                // H 3.25
                horizontalLineTo(x = 3.25f)
                // a 0.5 0.5 0 0 1 -0.397 -0.804
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.397f,
                    dy1 = -0.804f,
                )
                // l 6.5 -8.5z
                lineToRelative(dx = 6.5f, dy = -8.5f)
                close()
                // M 4.262 8.5
                moveTo(x = 4.262f, y = 8.5f)
                // H 7.25
                horizontalLineTo(x = 7.25f)
                // a 0.501 0.501 0 0 1 0.494 0.576
                arcToRelative(
                    a = 0.501f,
                    b = 0.501f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.494f,
                    dy1 = 0.576f,
                )
                // l -0.684 4.448
                lineToRelative(dx = -0.684f, dy = 4.448f)
                // L 11.773 7
                lineTo(x = 11.773f, y = 7.0f)
                // H 8.75
                horizontalLineTo(x = 8.75f)
                // a 0.5 0.5 0 0 1 -0.493 -0.582
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.493f,
                    dy1 = -0.582f,
                )
                // L 8.926 2.4
                lineTo(x = 8.926f, y = 2.4f)
                // L 4.262 8.5z
                lineTo(x = 4.262f, y = 8.5f)
                close()
            }
        }.build().also { _ic2029 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2029: ImageVector? = null
