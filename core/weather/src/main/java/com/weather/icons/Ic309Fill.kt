package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic309Fill: ImageVector
    get() {
        val current = _ic309Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic309Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.534 12.125 a.25 .25 0 0 1 .433 .25 l-1.75 3.031 a.25 .25 0 1 1 -.433 -.25 l1.75 -3.031Z m4.25 .7 a.25 .25 0 0 1 .432 .25 l-1 1.732 a.25 .25 0 1 1 -.432 -.25 l1 -1.732Z m-9 0 a.25 .25 0 0 1 .433 .25 l-1 1.732 a.25 .25 0 0 1 -.433 -.25 l1 -1.732Z m7.943 -4.608 A4.99 4.99 0 0 1 7.9 10 a4.988 4.988 0 0 1 -3.773 -1.719 3 3 0 1 1 -.586 -5.732 A4.998 4.998 0 0 1 7.9 0 a4.999 4.999 0 0 1 4.38 2.587 3 3 0 1 1 -.553 5.63Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.534 12.125
                moveTo(x = 8.534f, y = 12.125f)
                // a 0.25 0.25 0 0 1 0.433 0.25
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.433f,
                    dy1 = 0.25f,
                )
                // l -1.75 3.031
                lineToRelative(dx = -1.75f, dy = 3.031f)
                // a 0.25 0.25 0 1 1 -0.433 -0.25
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.433f,
                    dy1 = -0.25f,
                )
                // l 1.75 -3.031z
                lineToRelative(dx = 1.75f, dy = -3.031f)
                close()
                // m 4.25 0.7
                moveToRelative(dx = 4.25f, dy = 0.7f)
                // a 0.25 0.25 0 0 1 0.432 0.25
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.432f,
                    dy1 = 0.25f,
                )
                // l -1 1.732
                lineToRelative(dx = -1.0f, dy = 1.732f)
                // a 0.25 0.25 0 1 1 -0.432 -0.25
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.432f,
                    dy1 = -0.25f,
                )
                // l 1 -1.732z
                lineToRelative(dx = 1.0f, dy = -1.732f)
                close()
                // m -9 0
                moveToRelative(dx = -9.0f, dy = 0.0f)
                // a 0.25 0.25 0 0 1 0.433 0.25
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.433f,
                    dy1 = 0.25f,
                )
                // l -1 1.732
                lineToRelative(dx = -1.0f, dy = 1.732f)
                // a 0.25 0.25 0 0 1 -0.433 -0.25
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.433f,
                    dy1 = -0.25f,
                )
                // l 1 -1.732z
                lineToRelative(dx = 1.0f, dy = -1.732f)
                close()
                // m 7.943 -4.608
                moveToRelative(dx = 7.943f, dy = -4.608f)
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
        }.build().also { _ic309Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic309Fill: ImageVector? = null
