package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic313Fill: ImageVector
    get() {
        val current = _ic313Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic313Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // m1 9 1 1.5 L1 12 l-1 -1.5 L1 9Z m15 1.5 L15 9 l-1 1.5 1 1.5 1 -1.5Z m-7 4 L8 13 l-1 1.5 L8 16 l1 -1.5Z M4.5 11 l1 1.5 -1 1.5 -1 -1.5 1 -1.5Z m8 1.5 -1 -1.5 -1 1.5 1 1.5 1 -1.5Z m-.773 -4.283 A4.99 4.99 0 0 1 7.9 10 a4.988 4.988 0 0 1 -3.773 -1.719 3 3 0 1 1 -.586 -5.732 A4.998 4.998 0 0 1 7.9 0 a4.999 4.999 0 0 1 4.38 2.587 3 3 0 1 1 -.553 5.63Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 1 9
                moveTo(x = 1.0f, y = 9.0f)
                // l 1 1.5
                lineToRelative(dx = 1.0f, dy = 1.5f)
                // L 1 12
                lineTo(x = 1.0f, y = 12.0f)
                // l -1 -1.5
                lineToRelative(dx = -1.0f, dy = -1.5f)
                // L 1 9z
                lineTo(x = 1.0f, y = 9.0f)
                close()
                // m 15 1.5
                moveToRelative(dx = 15.0f, dy = 1.5f)
                // L 15 9
                lineTo(x = 15.0f, y = 9.0f)
                // l -1 1.5
                lineToRelative(dx = -1.0f, dy = 1.5f)
                // l 1 1.5
                lineToRelative(dx = 1.0f, dy = 1.5f)
                // l 1 -1.5z
                lineToRelative(dx = 1.0f, dy = -1.5f)
                close()
                // m -7 4
                moveToRelative(dx = -7.0f, dy = 4.0f)
                // L 8 13
                lineTo(x = 8.0f, y = 13.0f)
                // l -1 1.5
                lineToRelative(dx = -1.0f, dy = 1.5f)
                // L 8 16
                lineTo(x = 8.0f, y = 16.0f)
                // l 1 -1.5z
                lineToRelative(dx = 1.0f, dy = -1.5f)
                close()
                // M 4.5 11
                moveTo(x = 4.5f, y = 11.0f)
                // l 1 1.5
                lineToRelative(dx = 1.0f, dy = 1.5f)
                // l -1 1.5
                lineToRelative(dx = -1.0f, dy = 1.5f)
                // l -1 -1.5
                lineToRelative(dx = -1.0f, dy = -1.5f)
                // l 1 -1.5z
                lineToRelative(dx = 1.0f, dy = -1.5f)
                close()
                // m 8 1.5
                moveToRelative(dx = 8.0f, dy = 1.5f)
                // l -1 -1.5
                lineToRelative(dx = -1.0f, dy = -1.5f)
                // l -1 1.5
                lineToRelative(dx = -1.0f, dy = 1.5f)
                // l 1 1.5
                lineToRelative(dx = 1.0f, dy = 1.5f)
                // l 1 -1.5z
                lineToRelative(dx = 1.0f, dy = -1.5f)
                close()
                // m -0.773 -4.283
                moveToRelative(dx = -0.773f, dy = -4.283f)
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
        }.build().also { _ic313Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic313Fill: ImageVector? = null
