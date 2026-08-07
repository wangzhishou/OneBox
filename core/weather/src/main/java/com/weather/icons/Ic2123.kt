package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2123: ImageVector
    get() {
        val current = _ic2123
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2123",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M3.5 1.25 a.5 .5 0 0 1 .5 -.5 h10 a.5 .5 0 0 1 0 1 H4 a.5 .5 0 0 1 -.5 -.5Z m-1 1.5 a.5 .5 0 0 1 .5 -.5 h10 a.5 .5 0 0 1 0 1 H3 a.5 .5 0 0 1 -.5 -.5Z m-1 1.5 a.5 .5 0 0 1 .5 -.5 h10 a.5 .5 0 0 1 0 1 H2 a.5 .5 0 0 1 -.5 -.5Z m1 1.5 a.5 .5 0 0 1 .5 -.5 h10 a.5 .5 0 0 1 0 1 H3 a.5 .5 0 0 1 -.5 -.5Z m1 1.5 a.5 .5 0 0 1 .5 -.5 h10 a.5 .5 0 0 1 0 1 H4 a.5 .5 0 0 1 -.5 -.5Z M5 8.75 a.5 .5 0 0 1 .5 -.5 H13 a.5 .5 0 0 1 0 1 H5.5 a.5 .5 0 0 1 -.5 -.5Z m2 1.5 a.5 .5 0 0 1 .5 -.5 H12 a.5 .5 0 0 1 0 1 H7.5 a.5 .5 0 0 1 -.5 -.5Z m1 1.5 a.5 .5 0 0 1 .5 -.5 H11 a.5 .5 0 0 1 0 1 H8.5 a.5 .5 0 0 1 -.5 -.5Z m-1 1.5 a.5 .5 0 0 1 .5 -.5 h2 a.5 .5 0 0 1 0 1 h-2 a.5 .5 0 0 1 -.5 -.5Z m0 1.5 a.5 .5 0 0 1 .5 -.5 h1 a.5 .5 0 0 1 0 1 h-1 a.5 .5 0 0 1 -.5 -.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 3.5 1.25
                moveTo(x = 3.5f, y = 1.25f)
                // a 0.5 0.5 0 0 1 0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = -0.5f,
                )
                // h 10
                horizontalLineToRelative(dx = 10.0f)
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
                // H 4
                horizontalLineTo(x = 4.0f)
                // a 0.5 0.5 0 0 1 -0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                close()
                // m -1 1.5
                moveToRelative(dx = -1.0f, dy = 1.5f)
                // a 0.5 0.5 0 0 1 0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = -0.5f,
                )
                // h 10
                horizontalLineToRelative(dx = 10.0f)
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
                // H 3
                horizontalLineTo(x = 3.0f)
                // a 0.5 0.5 0 0 1 -0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                close()
                // m -1 1.5
                moveToRelative(dx = -1.0f, dy = 1.5f)
                // a 0.5 0.5 0 0 1 0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = -0.5f,
                )
                // h 10
                horizontalLineToRelative(dx = 10.0f)
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
                // H 2
                horizontalLineTo(x = 2.0f)
                // a 0.5 0.5 0 0 1 -0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                close()
                // m 1 1.5
                moveToRelative(dx = 1.0f, dy = 1.5f)
                // a 0.5 0.5 0 0 1 0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = -0.5f,
                )
                // h 10
                horizontalLineToRelative(dx = 10.0f)
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
                // H 3
                horizontalLineTo(x = 3.0f)
                // a 0.5 0.5 0 0 1 -0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                close()
                // m 1 1.5
                moveToRelative(dx = 1.0f, dy = 1.5f)
                // a 0.5 0.5 0 0 1 0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = -0.5f,
                )
                // h 10
                horizontalLineToRelative(dx = 10.0f)
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
                // H 4
                horizontalLineTo(x = 4.0f)
                // a 0.5 0.5 0 0 1 -0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                close()
                // M 5 8.75
                moveTo(x = 5.0f, y = 8.75f)
                // a 0.5 0.5 0 0 1 0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = -0.5f,
                )
                // H 13
                horizontalLineTo(x = 13.0f)
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
                // H 5.5
                horizontalLineTo(x = 5.5f)
                // a 0.5 0.5 0 0 1 -0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                close()
                // m 2 1.5
                moveToRelative(dx = 2.0f, dy = 1.5f)
                // a 0.5 0.5 0 0 1 0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = -0.5f,
                )
                // H 12
                horizontalLineTo(x = 12.0f)
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
                // H 7.5
                horizontalLineTo(x = 7.5f)
                // a 0.5 0.5 0 0 1 -0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                close()
                // m 1 1.5
                moveToRelative(dx = 1.0f, dy = 1.5f)
                // a 0.5 0.5 0 0 1 0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = -0.5f,
                )
                // H 11
                horizontalLineTo(x = 11.0f)
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
                // H 8.5
                horizontalLineTo(x = 8.5f)
                // a 0.5 0.5 0 0 1 -0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                close()
                // m -1 1.5
                moveToRelative(dx = -1.0f, dy = 1.5f)
                // a 0.5 0.5 0 0 1 0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = -0.5f,
                )
                // h 2
                horizontalLineToRelative(dx = 2.0f)
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
                // h -2
                horizontalLineToRelative(dx = -2.0f)
                // a 0.5 0.5 0 0 1 -0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                close()
                // m 0 1.5
                moveToRelative(dx = 0.0f, dy = 1.5f)
                // a 0.5 0.5 0 0 1 0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = -0.5f,
                )
                // h 1
                horizontalLineToRelative(dx = 1.0f)
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
                // h -1
                horizontalLineToRelative(dx = -1.0f)
                // a 0.5 0.5 0 0 1 -0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                close()
            }
        }.build().also { _ic2123 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2123: ImageVector? = null
