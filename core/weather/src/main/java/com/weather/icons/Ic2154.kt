package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2154: ImageVector
    get() {
        val current = _ic2154
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2154",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M5 3.5 a.5 .5 0 0 1 .5 -.5 h6 a.5 .5 0 0 1 0 1 h-6 a.5 .5 0 0 1 -.5 -.5Z m-4 2 a.5 .5 0 0 1 .5 -.5 H9 a.5 .5 0 0 1 0 1 H1.5 a.5 .5 0 0 1 -.5 -.5Z m5 2 a.5 .5 0 0 1 .5 -.5 h8 a.5 .5 0 0 1 0 1 h-8 a.5 .5 0 0 1 -.5 -.5Z M2 9 a.5 .5 0 0 1 .5 -.5 h6 a.5 .5 0 0 1 0 1 h-6 A.5 .5 0 0 1 2 9Z m3 1.5 a.5 .5 0 0 1 .5 -.5 H13 a.5 .5 0 0 1 0 1 H5.5 a.5 .5 0 0 1 -.5 -.5Z m-1.5 2 A.5 .5 0 0 1 4 12 h6 a.5 .5 0 0 1 0 1 H4 a.5 .5 0 0 1 -.5 -.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5 3.5
                moveTo(x = 5.0f, y = 3.5f)
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
                // h 6
                horizontalLineToRelative(dx = 6.0f)
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
                // h -6
                horizontalLineToRelative(dx = -6.0f)
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
                // m -4 2
                moveToRelative(dx = -4.0f, dy = 2.0f)
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
                // H 9
                horizontalLineTo(x = 9.0f)
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
                // H 1.5
                horizontalLineTo(x = 1.5f)
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
                // m 5 2
                moveToRelative(dx = 5.0f, dy = 2.0f)
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
                // h 8
                horizontalLineToRelative(dx = 8.0f)
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
                // M 2 9
                moveTo(x = 2.0f, y = 9.0f)
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
                // h 6
                horizontalLineToRelative(dx = 6.0f)
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
                // h -6
                horizontalLineToRelative(dx = -6.0f)
                // A 0.5 0.5 0 0 1 2 9z
                arcTo(
                    horizontalEllipseRadius = 0.5f,
                    verticalEllipseRadius = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 2.0f,
                    y1 = 9.0f,
                )
                close()
                // m 3 1.5
                moveToRelative(dx = 3.0f, dy = 1.5f)
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
                // m -1.5 2
                moveToRelative(dx = -1.5f, dy = 2.0f)
                // A 0.5 0.5 0 0 1 4 12
                arcTo(
                    horizontalEllipseRadius = 0.5f,
                    verticalEllipseRadius = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 4.0f,
                    y1 = 12.0f,
                )
                // h 6
                horizontalLineToRelative(dx = 6.0f)
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
            }
        }.build().also { _ic2154 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2154: ImageVector? = null
