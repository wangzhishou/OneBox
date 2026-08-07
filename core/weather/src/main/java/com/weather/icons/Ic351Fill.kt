package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic351Fill: ImageVector
    get() {
        val current = _ic351Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic351Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M3.293 14.707 A1 1 0 0 1 3 14 c0 -.5 .555 -1.395 1 -2 .445 .605 1 1.5 1 2 a1 1 0 0 1 -1.707 .707Z m7 0 A1 1 0 0 1 10 14 c0 -.5 .555 -1.395 1 -2 .445 .605 1 1.5 1 2 a1 1 0 0 1 -1.707 .707Z M6.5 15 a1 1 0 1 0 2 0 c0 -.5 -.555 -1.395 -1 -2 -.445 .605 -1 1.5 -1 2Z m4.494 -4.104 A4.758 4.758 0 0 1 7.406 12.5 a4.76 4.76 0 0 1 -3.537 -1.547 2.908 2.908 0 0 1 -1.057 .197 C1.26 11.15 0 9.941 0 8.45 s1.26 -2.7 2.813 -2.7 c.173 0 .342 .015 .507 .044 C4.124 4.424 5.652 3.5 7.406 3.5 c1.769 0 3.308 .94 4.107 2.328 a2.93 2.93 0 0 1 .675 -.078 C13.74 5.75 15 6.959 15 8.45 s-1.26 2.7 -2.813 2.7 a2.9 2.9 0 0 1 -1.193 -.254Z m4.472 -6.681 a.31 .31 0 0 0 -.08 .01 3.066 3.066 0 0 1 -1.866 -.076 A3.183 3.183 0 0 1 11.492 .364 .29 .29 0 0 0 11.22 0 a.28 .28 0 0 0 -.104 .02 3.546 3.546 0 0 0 -2.21 3.096 c.34 .063 .671 .16 .99 .293 a2.56 2.56 0 0 1 .54 -1.671 4.166 4.166 0 0 0 2.755 3.356 c.274 .096 .558 .164 .846 .203 a2.611 2.611 0 0 1 -.239 .163 c.304 .173 .582 .39 .823 .643 a3.553 3.553 0 0 0 1.12 -1.504 .285 .285 0 0 0 -.275 -.384Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 3.293 14.707
                moveTo(x = 3.293f, y = 14.707f)
                // A 1 1 0 0 1 3 14
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 3.0f,
                    y1 = 14.0f,
                )
                // c 0 -0.5 0.555 -1.395 1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = 0.555f,
                    dy2 = -1.395f,
                    dx3 = 1.0f,
                    dy3 = -2.0f,
                )
                // c 0.445 0.605 1 1.5 1 2
                curveToRelative(
                    dx1 = 0.445f,
                    dy1 = 0.605f,
                    dx2 = 1.0f,
                    dy2 = 1.5f,
                    dx3 = 1.0f,
                    dy3 = 2.0f,
                )
                // a 1 1 0 0 1 -1.707 0.707z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.707f,
                    dy1 = 0.707f,
                )
                close()
                // m 7 0
                moveToRelative(dx = 7.0f, dy = 0.0f)
                // A 1 1 0 0 1 10 14
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 10.0f,
                    y1 = 14.0f,
                )
                // c 0 -0.5 0.555 -1.395 1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = 0.555f,
                    dy2 = -1.395f,
                    dx3 = 1.0f,
                    dy3 = -2.0f,
                )
                // c 0.445 0.605 1 1.5 1 2
                curveToRelative(
                    dx1 = 0.445f,
                    dy1 = 0.605f,
                    dx2 = 1.0f,
                    dy2 = 1.5f,
                    dx3 = 1.0f,
                    dy3 = 2.0f,
                )
                // a 1 1 0 0 1 -1.707 0.707z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.707f,
                    dy1 = 0.707f,
                )
                close()
                // M 6.5 15
                moveTo(x = 6.5f, y = 15.0f)
                // a 1 1 0 1 0 2 0
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 2.0f,
                    dy1 = 0.0f,
                )
                // c 0 -0.5 -0.555 -1.395 -1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = -0.555f,
                    dy2 = -1.395f,
                    dx3 = -1.0f,
                    dy3 = -2.0f,
                )
                // c -0.445 0.605 -1 1.5 -1 2z
                curveToRelative(
                    dx1 = -0.445f,
                    dy1 = 0.605f,
                    dx2 = -1.0f,
                    dy2 = 1.5f,
                    dx3 = -1.0f,
                    dy3 = 2.0f,
                )
                close()
                // m 4.494 -4.104
                moveToRelative(dx = 4.494f, dy = -4.104f)
                // A 4.758 4.758 0 0 1 7.406 12.5
                arcTo(
                    horizontalEllipseRadius = 4.758f,
                    verticalEllipseRadius = 4.758f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.406f,
                    y1 = 12.5f,
                )
                // a 4.76 4.76 0 0 1 -3.537 -1.547
                arcToRelative(
                    a = 4.76f,
                    b = 4.76f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.537f,
                    dy1 = -1.547f,
                )
                // a 2.908 2.908 0 0 1 -1.057 0.197
                arcToRelative(
                    a = 2.908f,
                    b = 2.908f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.057f,
                    dy1 = 0.197f,
                )
                // C 1.26 11.15 0 9.941 0 8.45
                curveTo(
                    x1 = 1.26f,
                    y1 = 11.15f,
                    x2 = 0.0f,
                    y2 = 9.941f,
                    x3 = 0.0f,
                    y3 = 8.45f,
                )
                // s 1.26 -2.7 2.813 -2.7
                reflectiveCurveToRelative(
                    dx1 = 1.26f,
                    dy1 = -2.7f,
                    dx2 = 2.813f,
                    dy2 = -2.7f,
                )
                // c 0.173 0 0.342 0.015 0.507 0.044
                curveToRelative(
                    dx1 = 0.173f,
                    dy1 = 0.0f,
                    dx2 = 0.342f,
                    dy2 = 0.015f,
                    dx3 = 0.507f,
                    dy3 = 0.044f,
                )
                // C 4.124 4.424 5.652 3.5 7.406 3.5
                curveTo(
                    x1 = 4.124f,
                    y1 = 4.424f,
                    x2 = 5.652f,
                    y2 = 3.5f,
                    x3 = 7.406f,
                    y3 = 3.5f,
                )
                // c 1.769 0 3.308 0.94 4.107 2.328
                curveToRelative(
                    dx1 = 1.769f,
                    dy1 = 0.0f,
                    dx2 = 3.308f,
                    dy2 = 0.94f,
                    dx3 = 4.107f,
                    dy3 = 2.328f,
                )
                // a 2.93 2.93 0 0 1 0.675 -0.078
                arcToRelative(
                    a = 2.93f,
                    b = 2.93f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.675f,
                    dy1 = -0.078f,
                )
                // C 13.74 5.75 15 6.959 15 8.45
                curveTo(
                    x1 = 13.74f,
                    y1 = 5.75f,
                    x2 = 15.0f,
                    y2 = 6.959f,
                    x3 = 15.0f,
                    y3 = 8.45f,
                )
                // s -1.26 2.7 -2.813 2.7
                reflectiveCurveToRelative(
                    dx1 = -1.26f,
                    dy1 = 2.7f,
                    dx2 = -2.813f,
                    dy2 = 2.7f,
                )
                // a 2.9 2.9 0 0 1 -1.193 -0.254z
                arcToRelative(
                    a = 2.9f,
                    b = 2.9f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.193f,
                    dy1 = -0.254f,
                )
                close()
                // m 4.472 -6.681
                moveToRelative(dx = 4.472f, dy = -6.681f)
                // a 0.31 0.31 0 0 0 -0.08 0.01
                arcToRelative(
                    a = 0.31f,
                    b = 0.31f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.08f,
                    dy1 = 0.01f,
                )
                // a 3.066 3.066 0 0 1 -1.866 -0.076
                arcToRelative(
                    a = 3.066f,
                    b = 3.066f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.866f,
                    dy1 = -0.076f,
                )
                // A 3.183 3.183 0 0 1 11.492 0.364
                arcTo(
                    horizontalEllipseRadius = 3.183f,
                    verticalEllipseRadius = 3.183f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 11.492f,
                    y1 = 0.364f,
                )
                // A 0.29 0.29 0 0 0 11.22 0
                arcTo(
                    horizontalEllipseRadius = 0.29f,
                    verticalEllipseRadius = 0.29f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 11.22f,
                    y1 = 0.0f,
                )
                // a 0.28 0.28 0 0 0 -0.104 0.02
                arcToRelative(
                    a = 0.28f,
                    b = 0.28f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.104f,
                    dy1 = 0.02f,
                )
                // a 3.546 3.546 0 0 0 -2.21 3.096
                arcToRelative(
                    a = 3.546f,
                    b = 3.546f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.21f,
                    dy1 = 3.096f,
                )
                // c 0.34 0.063 0.671 0.16 0.99 0.293
                curveToRelative(
                    dx1 = 0.34f,
                    dy1 = 0.063f,
                    dx2 = 0.671f,
                    dy2 = 0.16f,
                    dx3 = 0.99f,
                    dy3 = 0.293f,
                )
                // a 2.56 2.56 0 0 1 0.54 -1.671
                arcToRelative(
                    a = 2.56f,
                    b = 2.56f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.54f,
                    dy1 = -1.671f,
                )
                // a 4.166 4.166 0 0 0 2.755 3.356
                arcToRelative(
                    a = 4.166f,
                    b = 4.166f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.755f,
                    dy1 = 3.356f,
                )
                // c 0.274 0.096 0.558 0.164 0.846 0.203
                curveToRelative(
                    dx1 = 0.274f,
                    dy1 = 0.096f,
                    dx2 = 0.558f,
                    dy2 = 0.164f,
                    dx3 = 0.846f,
                    dy3 = 0.203f,
                )
                // a 2.611 2.611 0 0 1 -0.239 0.163
                arcToRelative(
                    a = 2.611f,
                    b = 2.611f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.239f,
                    dy1 = 0.163f,
                )
                // c 0.304 0.173 0.582 0.39 0.823 0.643
                curveToRelative(
                    dx1 = 0.304f,
                    dy1 = 0.173f,
                    dx2 = 0.582f,
                    dy2 = 0.39f,
                    dx3 = 0.823f,
                    dy3 = 0.643f,
                )
                // a 3.553 3.553 0 0 0 1.12 -1.504
                arcToRelative(
                    a = 3.553f,
                    b = 3.553f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.12f,
                    dy1 = -1.504f,
                )
                // a 0.285 0.285 0 0 0 -0.275 -0.384z
                arcToRelative(
                    a = 0.285f,
                    b = 0.285f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.275f,
                    dy1 = -0.384f,
                )
                close()
            }
        }.build().also { _ic351Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic351Fill: ImageVector? = null
