package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic514Fill: ImageVector
    get() {
        val current = _ic514Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic514Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M.5 11 a.5 .5 0 0 0 0 1 h9 a.5 .5 0 0 0 0 -1 h-9Z m3 2 a.5 .5 0 0 0 0 1 h3 a.5 .5 0 0 0 0 -1 h-3Z m4.5 .5 a.5 .5 0 0 1 .5 -.5 h7 a.5 .5 0 0 1 0 1 h-7 a.5 .5 0 0 1 -.5 -.5Z M4.5 15 a.5 .5 0 0 0 0 1 h9 a.5 .5 0 0 0 0 -1 h-9Z m7.227 -6.783 A4.99 4.99 0 0 1 7.9 10 a4.988 4.988 0 0 1 -3.773 -1.719 3 3 0 1 1 -.586 -5.732 A4.998 4.998 0 0 1 11.901 2 H7.5 a.5 .5 0 0 0 0 1 h7 c.046 0 .09 -.006 .132 -.018 .397 .258 .73 .607 .967 1.018 H10.5 a.5 .5 0 0 0 0 1 h5.459 a3 3 0 0 1 -4.231 3.217Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0.5 11
                moveTo(x = 0.5f, y = 11.0f)
                // a 0.5 0.5 0 0 0 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // h 9
                horizontalLineToRelative(dx = 9.0f)
                // a 0.5 0.5 0 0 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h -9z
                horizontalLineToRelative(dx = -9.0f)
                close()
                // m 3 2
                moveToRelative(dx = 3.0f, dy = 2.0f)
                // a 0.5 0.5 0 0 0 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // h 3
                horizontalLineToRelative(dx = 3.0f)
                // a 0.5 0.5 0 0 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h -3z
                horizontalLineToRelative(dx = -3.0f)
                close()
                // m 4.5 0.5
                moveToRelative(dx = 4.5f, dy = 0.5f)
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
                // h 7
                horizontalLineToRelative(dx = 7.0f)
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
                // h -7
                horizontalLineToRelative(dx = -7.0f)
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
                // M 4.5 15
                moveTo(x = 4.5f, y = 15.0f)
                // a 0.5 0.5 0 0 0 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // h 9
                horizontalLineToRelative(dx = 9.0f)
                // a 0.5 0.5 0 0 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h -9z
                horizontalLineToRelative(dx = -9.0f)
                close()
                // m 7.227 -6.783
                moveToRelative(dx = 7.227f, dy = -6.783f)
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
                // A 4.998 4.998 0 0 1 11.901 2
                arcTo(
                    horizontalEllipseRadius = 4.998f,
                    verticalEllipseRadius = 4.998f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 11.901f,
                    y1 = 2.0f,
                )
                // H 7.5
                horizontalLineTo(x = 7.5f)
                // a 0.5 0.5 0 0 0 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // h 7
                horizontalLineToRelative(dx = 7.0f)
                // c 0.046 0 0.09 -0.006 0.132 -0.018
                curveToRelative(
                    dx1 = 0.046f,
                    dy1 = 0.0f,
                    dx2 = 0.09f,
                    dy2 = -0.006f,
                    dx3 = 0.132f,
                    dy3 = -0.018f,
                )
                // c 0.397 0.258 0.73 0.607 0.967 1.018
                curveToRelative(
                    dx1 = 0.397f,
                    dy1 = 0.258f,
                    dx2 = 0.73f,
                    dy2 = 0.607f,
                    dx3 = 0.967f,
                    dy3 = 1.018f,
                )
                // H 10.5
                horizontalLineTo(x = 10.5f)
                // a 0.5 0.5 0 0 0 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // h 5.459
                horizontalLineToRelative(dx = 5.459f)
                // a 3 3 0 0 1 -4.231 3.217z
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -4.231f,
                    dy1 = 3.217f,
                )
                close()
            }
        }.build().also { _ic514Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic514Fill: ImageVector? = null
