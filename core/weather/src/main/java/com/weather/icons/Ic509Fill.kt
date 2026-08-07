package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic509Fill: ImageVector
    get() {
        val current = _ic509Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic509Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M.25 11 a.25 .25 0 1 0 0 .5 h9.5 a.25 .25 0 1 0 0 -.5 H.25Z m3 1.5 a.25 .25 0 1 0 0 .5 h3.5 a.25 .25 0 1 0 0 -.5 h-3.5Z M10 14.25 a.25 .25 0 0 1 .25 -.25 h3.5 a.25 .25 0 1 1 0 .5 h-3.5 a.25 .25 0 0 1 -.25 -.25Z M8.25 12.5 a.25 .25 0 1 0 0 .5 h7.5 a.25 .25 0 1 0 0 -.5 h-7.5Z M1 14.25 a.25 .25 0 0 1 .25 -.25 h7.5 a.25 .25 0 1 1 0 .5 h-7.5 a.25 .25 0 0 1 -.25 -.25Z m2.25 1.25 a.25 .25 0 1 0 0 .5 h9.5 a.25 .25 0 1 0 0 -.5 h-9.5Z m8.477 -7.283 A4.99 4.99 0 0 1 7.9 10 a4.988 4.988 0 0 1 -3.773 -1.719 3 3 0 1 1 -.586 -5.732 A4.998 4.998 0 0 1 7.9 0 a4.999 4.999 0 0 1 4.38 2.587 3 3 0 1 1 -.553 5.63Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0.25 11
                moveTo(x = 0.25f, y = 11.0f)
                // a 0.25 0.25 0 1 0 0 0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                // h 9.5
                horizontalLineToRelative(dx = 9.5f)
                // a 0.25 0.25 0 1 0 0 -0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                )
                // H 0.25z
                horizontalLineTo(x = 0.25f)
                close()
                // m 3 1.5
                moveToRelative(dx = 3.0f, dy = 1.5f)
                // a 0.25 0.25 0 1 0 0 0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                // h 3.5
                horizontalLineToRelative(dx = 3.5f)
                // a 0.25 0.25 0 1 0 0 -0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                )
                // h -3.5z
                horizontalLineToRelative(dx = -3.5f)
                close()
                // M 10 14.25
                moveTo(x = 10.0f, y = 14.25f)
                // a 0.25 0.25 0 0 1 0.25 -0.25
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.25f,
                    dy1 = -0.25f,
                )
                // h 3.5
                horizontalLineToRelative(dx = 3.5f)
                // a 0.25 0.25 0 1 1 0 0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                // h -3.5
                horizontalLineToRelative(dx = -3.5f)
                // a 0.25 0.25 0 0 1 -0.25 -0.25z
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.25f,
                    dy1 = -0.25f,
                )
                close()
                // M 8.25 12.5
                moveTo(x = 8.25f, y = 12.5f)
                // a 0.25 0.25 0 1 0 0 0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                // h 7.5
                horizontalLineToRelative(dx = 7.5f)
                // a 0.25 0.25 0 1 0 0 -0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                )
                // h -7.5z
                horizontalLineToRelative(dx = -7.5f)
                close()
                // M 1 14.25
                moveTo(x = 1.0f, y = 14.25f)
                // a 0.25 0.25 0 0 1 0.25 -0.25
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.25f,
                    dy1 = -0.25f,
                )
                // h 7.5
                horizontalLineToRelative(dx = 7.5f)
                // a 0.25 0.25 0 1 1 0 0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                // h -7.5
                horizontalLineToRelative(dx = -7.5f)
                // a 0.25 0.25 0 0 1 -0.25 -0.25z
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.25f,
                    dy1 = -0.25f,
                )
                close()
                // m 2.25 1.25
                moveToRelative(dx = 2.25f, dy = 1.25f)
                // a 0.25 0.25 0 1 0 0 0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                // h 9.5
                horizontalLineToRelative(dx = 9.5f)
                // a 0.25 0.25 0 1 0 0 -0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                )
                // h -9.5z
                horizontalLineToRelative(dx = -9.5f)
                close()
                // m 8.477 -7.283
                moveToRelative(dx = 8.477f, dy = -7.283f)
                // A 4.99 4.99 0 0 1 7.9 10
                arcTo(
                    horizontalEllipseRadius = 4.99f,
                    verticalEllipseRadius = 4.99f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.9f,
                    y1 = 10.0f,
                )
                // a 4.988 4.988 0 0 1 -3.773 -1.719
                arcToRelative(
                    a = 4.988f,
                    b = 4.988f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.773f,
                    dy1 = -1.719f,
                )
                // a 3 3 0 1 1 -0.586 -5.732
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.586f,
                    dy1 = -5.732f,
                )
                // A 4.998 4.998 0 0 1 7.9 0
                arcTo(
                    horizontalEllipseRadius = 4.998f,
                    verticalEllipseRadius = 4.998f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.9f,
                    y1 = 0.0f,
                )
                // a 4.999 4.999 0 0 1 4.38 2.587
                arcToRelative(
                    a = 4.999f,
                    b = 4.999f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 4.38f,
                    dy1 = 2.587f,
                )
                // a 3 3 0 1 1 -0.553 5.63z
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.553f,
                    dy1 = 5.63f,
                )
                close()
            }
        }.build().also { _ic509Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic509Fill: ImageVector? = null
