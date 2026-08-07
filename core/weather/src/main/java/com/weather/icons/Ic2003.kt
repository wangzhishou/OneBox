package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2003: ImageVector
    get() {
        val current = _ic2003
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2003",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M4.5 12 a.5 .5 0 0 1 0 1 h-4 a.5 .5 0 0 1 0 -1 h4Z m11 0 a.5 .5 0 0 1 0 1 h-8 a.5 .5 0 0 1 0 -1 h8Z m-7 -3 a.5 .5 0 0 1 0 1 h-8 a.5 .5 0 0 1 0 -1 h8Z m7 0 a.5 .5 0 0 1 0 1 h-4 a.5 .5 0 0 1 0 -1 h4Z M6 6.5 a.5 .5 0 0 1 0 1 H.5 a.5 .5 0 0 1 0 -1 H6Z m9.5 0 a.5 .5 0 0 1 0 1 H9 a.5 .5 0 0 1 0 -1 h6.5Z m0 -2.5 a.5 .5 0 0 1 0 1 H.5 a.5 .5 0 0 1 0 -1 h15Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.5 12
                moveTo(x = 4.5f, y = 12.0f)
                // a 0.5 0.5 0 0 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // h -4
                horizontalLineToRelative(dx = -4.0f)
                // a 0.5 0.5 0 0 1 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h 4z
                horizontalLineToRelative(dx = 4.0f)
                close()
                // m 11 0
                moveToRelative(dx = 11.0f, dy = 0.0f)
                // a 0.5 0.5 0 0 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // h -8
                horizontalLineToRelative(dx = -8.0f)
                // a 0.5 0.5 0 0 1 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h 8z
                horizontalLineToRelative(dx = 8.0f)
                close()
                // m -7 -3
                moveToRelative(dx = -7.0f, dy = -3.0f)
                // a 0.5 0.5 0 0 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // h -8
                horizontalLineToRelative(dx = -8.0f)
                // a 0.5 0.5 0 0 1 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h 8z
                horizontalLineToRelative(dx = 8.0f)
                close()
                // m 7 0
                moveToRelative(dx = 7.0f, dy = 0.0f)
                // a 0.5 0.5 0 0 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // h -4
                horizontalLineToRelative(dx = -4.0f)
                // a 0.5 0.5 0 0 1 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h 4z
                horizontalLineToRelative(dx = 4.0f)
                close()
                // M 6 6.5
                moveTo(x = 6.0f, y = 6.5f)
                // a 0.5 0.5 0 0 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // H 0.5
                horizontalLineTo(x = 0.5f)
                // a 0.5 0.5 0 0 1 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // H 6z
                horizontalLineTo(x = 6.0f)
                close()
                // m 9.5 0
                moveToRelative(dx = 9.5f, dy = 0.0f)
                // a 0.5 0.5 0 0 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // H 9
                horizontalLineTo(x = 9.0f)
                // a 0.5 0.5 0 0 1 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h 6.5z
                horizontalLineToRelative(dx = 6.5f)
                close()
                // m 0 -2.5
                moveToRelative(dx = 0.0f, dy = -2.5f)
                // a 0.5 0.5 0 0 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // H 0.5
                horizontalLineTo(x = 0.5f)
                // a 0.5 0.5 0 0 1 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h 15z
                horizontalLineToRelative(dx = 15.0f)
                close()
            }
        }.build().also { _ic2003 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2003: ImageVector? = null
