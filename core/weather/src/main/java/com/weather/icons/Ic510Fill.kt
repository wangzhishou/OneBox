package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic510Fill: ImageVector
    get() {
        val current = _ic510Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic510Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M.25 11 a.25 .25 0 1 0 0 .5 h9.5 a.25 .25 0 1 0 0 -.5 H.25Z m3 1.5 a.25 .25 0 1 0 0 .5 h3.5 a.25 .25 0 1 0 0 -.5 h-3.5Z M10 14.25 a.25 .25 0 0 1 .25 -.25 h3.5 a.25 .25 0 1 1 0 .5 h-3.5 a.25 .25 0 0 1 -.25 -.25Z M8.25 12.5 a.25 .25 0 1 0 0 .5 h7.5 a.25 .25 0 1 0 0 -.5 h-7.5Z M1 14.25 a.25 .25 0 0 1 .25 -.25 h7.5 a.25 .25 0 1 1 0 .5 h-7.5 a.25 .25 0 0 1 -.25 -.25Z m2.25 1.25 a.25 .25 0 1 0 0 .5 h9.5 a.25 .25 0 1 0 0 -.5 h-9.5Z M7.9 10 a4.99 4.99 0 0 0 3.827 -1.783 3 3 0 0 0 4.215 -3.307 .25 .25 0 0 1 -.192 .09 h-5.5 a.25 .25 0 1 1 0 -.5 h5.5 a.25 .25 0 0 1 .084 .015 A3.008 3.008 0 0 0 14.66 3 h-4.41 a.25 .25 0 1 1 0 -.5 h1.981 A4.998 4.998 0 0 0 7.9 0 a4.998 4.998 0 0 0 -4.359 2.549 3 3 0 1 0 .586 5.732 A4.988 4.988 0 0 0 7.9 10Z M8 3.75 a.25 .25 0 0 1 .25 -.25 h5.5 a.25 .25 0 1 1 0 .5 h-5.5 A.25 .25 0 0 1 8 3.75Z
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
                // M 7.9 10
                moveTo(x = 7.9f, y = 10.0f)
                // a 4.99 4.99 0 0 0 3.827 -1.783
                arcToRelative(
                    a = 4.99f,
                    b = 4.99f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 3.827f,
                    dy1 = -1.783f,
                )
                // a 3 3 0 0 0 4.215 -3.307
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 4.215f,
                    dy1 = -3.307f,
                )
                // a 0.25 0.25 0 0 1 -0.192 0.09
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.192f,
                    dy1 = 0.09f,
                )
                // h -5.5
                horizontalLineToRelative(dx = -5.5f)
                // a 0.25 0.25 0 1 1 0 -0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                )
                // h 5.5
                horizontalLineToRelative(dx = 5.5f)
                // a 0.25 0.25 0 0 1 0.084 0.015
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.084f,
                    dy1 = 0.015f,
                )
                // A 3.008 3.008 0 0 0 14.66 3
                arcTo(
                    horizontalEllipseRadius = 3.008f,
                    verticalEllipseRadius = 3.008f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 14.66f,
                    y1 = 3.0f,
                )
                // h -4.41
                horizontalLineToRelative(dx = -4.41f)
                // a 0.25 0.25 0 1 1 0 -0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                )
                // h 1.981
                horizontalLineToRelative(dx = 1.981f)
                // A 4.998 4.998 0 0 0 7.9 0
                arcTo(
                    horizontalEllipseRadius = 4.998f,
                    verticalEllipseRadius = 4.998f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.9f,
                    y1 = 0.0f,
                )
                // a 4.998 4.998 0 0 0 -4.359 2.549
                arcToRelative(
                    a = 4.998f,
                    b = 4.998f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -4.359f,
                    dy1 = 2.549f,
                )
                // a 3 3 0 1 0 0.586 5.732
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.586f,
                    dy1 = 5.732f,
                )
                // A 4.988 4.988 0 0 0 7.9 10z
                arcTo(
                    horizontalEllipseRadius = 4.988f,
                    verticalEllipseRadius = 4.988f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.9f,
                    y1 = 10.0f,
                )
                close()
                // M 8 3.75
                moveTo(x = 8.0f, y = 3.75f)
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
                // h 5.5
                horizontalLineToRelative(dx = 5.5f)
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
                // h -5.5
                horizontalLineToRelative(dx = -5.5f)
                // A 0.25 0.25 0 0 1 8 3.75z
                arcTo(
                    horizontalEllipseRadius = 0.25f,
                    verticalEllipseRadius = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 8.0f,
                    y1 = 3.75f,
                )
                close()
            }
        }.build().also { _ic510Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic510Fill: ImageVector? = null
