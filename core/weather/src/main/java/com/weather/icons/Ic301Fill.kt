package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic301Fill: ImageVector
    get() {
        val current = _ic301Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic301Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M4.293 14.707 A1 1 0 0 1 4 14 c0 -.5 .555 -1.395 1 -2 .445 .605 1 1.5 1 2 a1 1 0 0 1 -1.707 .707Z m7 0 A1 1 0 0 1 11 14 c0 -.5 .555 -1.395 1 -2 .445 .605 1 1.5 1 2 a1 1 0 0 1 -1.707 .707Z M7.5 15 a1 1 0 1 0 2 0 c0 -.5 -.555 -1.395 -1 -2 -.445 .605 -1 1.5 -1 2Z m4.494 -4.104 A4.758 4.758 0 0 1 8.406 12.5 a4.76 4.76 0 0 1 -3.537 -1.547 2.908 2.908 0 0 1 -1.056 .197 C2.258 11.15 1 9.941 1 8.45 s1.26 -2.7 2.813 -2.7 c.173 0 .342 .015 .507 .044 C5.124 4.424 6.652 3.5 8.406 3.5 c1.769 0 3.308 .94 4.107 2.328 a2.93 2.93 0 0 1 .675 -.078 C14.74 5.75 16 6.959 16 8.45 s-1.26 2.7 -2.813 2.7 a2.9 2.9 0 0 1 -1.193 -.254Z M4.979 1.904 h.007 a.5 .5 0 0 0 .493 -.506 L5.467 .493 a.5 .5 0 0 0 -.5 -.493 H4.96 a.5 .5 0 0 0 -.493 .506 l.012 .904 a.5 .5 0 0 0 .5 .494Z m-2.892 .946 a.5 .5 0 1 0 .698 -.716 l-.648 -.63 a.5 .5 0 1 0 -.697 .715 l.647 .631Z m-.179 2.203 a.5 .5 0 0 0 -.5 -.494 h-.007 l-.904 .012 a.5 .5 0 0 0 .006 1 H.51 l.905 -.012 a.5 .5 0 0 0 .493 -.506Z m5.638 -2.121 a.5 .5 0 0 0 .359 -.15 l.63 -.648 a.5 .5 0 0 0 -.716 -.698 l-.631 .647 a.5 .5 0 0 0 .358 .85 v-.001Z M2.254 5.315 a3.53 3.53 0 0 1 1.018 -.288 1.831 1.831 0 0 1 1.811 -1.603 c.188 .002 .375 .034 .553 .094 a4.927 4.927 0 0 1 1.282 -.404 2.82 2.82 0 0 0 -4.67 2.145 c0 .02 .006 .037 .006 .056Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.293 14.707
                moveTo(x = 4.293f, y = 14.707f)
                // A 1 1 0 0 1 4 14
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 4.0f,
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
                // A 1 1 0 0 1 11 14
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 11.0f,
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
                // M 7.5 15
                moveTo(x = 7.5f, y = 15.0f)
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
                // A 4.758 4.758 0 0 1 8.406 12.5
                arcTo(
                    horizontalEllipseRadius = 4.758f,
                    verticalEllipseRadius = 4.758f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 8.406f,
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
                // a 2.908 2.908 0 0 1 -1.056 0.197
                arcToRelative(
                    a = 2.908f,
                    b = 2.908f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.056f,
                    dy1 = 0.197f,
                )
                // C 2.258 11.15 1 9.941 1 8.45
                curveTo(
                    x1 = 2.258f,
                    y1 = 11.15f,
                    x2 = 1.0f,
                    y2 = 9.941f,
                    x3 = 1.0f,
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
                // C 5.124 4.424 6.652 3.5 8.406 3.5
                curveTo(
                    x1 = 5.124f,
                    y1 = 4.424f,
                    x2 = 6.652f,
                    y2 = 3.5f,
                    x3 = 8.406f,
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
                // C 14.74 5.75 16 6.959 16 8.45
                curveTo(
                    x1 = 14.74f,
                    y1 = 5.75f,
                    x2 = 16.0f,
                    y2 = 6.959f,
                    x3 = 16.0f,
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
                // M 4.979 1.904
                moveTo(x = 4.979f, y = 1.904f)
                // h 0.007
                horizontalLineToRelative(dx = 0.007f)
                // a 0.5 0.5 0 0 0 0.493 -0.506
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.493f,
                    dy1 = -0.506f,
                )
                // L 5.467 0.493
                lineTo(x = 5.467f, y = 0.493f)
                // a 0.5 0.5 0 0 0 -0.5 -0.493
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.5f,
                    dy1 = -0.493f,
                )
                // H 4.96
                horizontalLineTo(x = 4.96f)
                // a 0.5 0.5 0 0 0 -0.493 0.506
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.493f,
                    dy1 = 0.506f,
                )
                // l 0.012 0.904
                lineToRelative(dx = 0.012f, dy = 0.904f)
                // a 0.5 0.5 0 0 0 0.5 0.494z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.5f,
                    dy1 = 0.494f,
                )
                close()
                // m -2.892 0.946
                moveToRelative(dx = -2.892f, dy = 0.946f)
                // a 0.5 0.5 0 1 0 0.698 -0.716
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.698f,
                    dy1 = -0.716f,
                )
                // l -0.648 -0.63
                lineToRelative(dx = -0.648f, dy = -0.63f)
                // a 0.5 0.5 0 1 0 -0.697 0.715
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.697f,
                    dy1 = 0.715f,
                )
                // l 0.647 0.631z
                lineToRelative(dx = 0.647f, dy = 0.631f)
                close()
                // m -0.179 2.203
                moveToRelative(dx = -0.179f, dy = 2.203f)
                // a 0.5 0.5 0 0 0 -0.5 -0.494
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.5f,
                    dy1 = -0.494f,
                )
                // h -0.007
                horizontalLineToRelative(dx = -0.007f)
                // l -0.904 0.012
                lineToRelative(dx = -0.904f, dy = 0.012f)
                // a 0.5 0.5 0 0 0 0.006 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.006f,
                    dy1 = 1.0f,
                )
                // H 0.51
                horizontalLineTo(x = 0.51f)
                // l 0.905 -0.012
                lineToRelative(dx = 0.905f, dy = -0.012f)
                // a 0.5 0.5 0 0 0 0.493 -0.506z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.493f,
                    dy1 = -0.506f,
                )
                close()
                // m 5.638 -2.121
                moveToRelative(dx = 5.638f, dy = -2.121f)
                // a 0.5 0.5 0 0 0 0.359 -0.15
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.359f,
                    dy1 = -0.15f,
                )
                // l 0.63 -0.648
                lineToRelative(dx = 0.63f, dy = -0.648f)
                // a 0.5 0.5 0 0 0 -0.716 -0.698
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.716f,
                    dy1 = -0.698f,
                )
                // l -0.631 0.647
                lineToRelative(dx = -0.631f, dy = 0.647f)
                // a 0.5 0.5 0 0 0 0.358 0.85
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.358f,
                    dy1 = 0.85f,
                )
                // v -0.001z
                verticalLineToRelative(dy = -0.001f)
                close()
                // M 2.254 5.315
                moveTo(x = 2.254f, y = 5.315f)
                // a 3.53 3.53 0 0 1 1.018 -0.288
                arcToRelative(
                    a = 3.53f,
                    b = 3.53f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.018f,
                    dy1 = -0.288f,
                )
                // a 1.831 1.831 0 0 1 1.811 -1.603
                arcToRelative(
                    a = 1.831f,
                    b = 1.831f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.811f,
                    dy1 = -1.603f,
                )
                // c 0.188 0.002 0.375 0.034 0.553 0.094
                curveToRelative(
                    dx1 = 0.188f,
                    dy1 = 0.002f,
                    dx2 = 0.375f,
                    dy2 = 0.034f,
                    dx3 = 0.553f,
                    dy3 = 0.094f,
                )
                // a 4.927 4.927 0 0 1 1.282 -0.404
                arcToRelative(
                    a = 4.927f,
                    b = 4.927f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.282f,
                    dy1 = -0.404f,
                )
                // a 2.82 2.82 0 0 0 -4.67 2.145
                arcToRelative(
                    a = 2.82f,
                    b = 2.82f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -4.67f,
                    dy1 = 2.145f,
                )
                // c 0 0.02 0.006 0.037 0.006 0.056z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.02f,
                    dx2 = 0.006f,
                    dy2 = 0.037f,
                    dx3 = 0.006f,
                    dy3 = 0.056f,
                )
                close()
            }
        }.build().also { _ic301Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic301Fill: ImageVector? = null
