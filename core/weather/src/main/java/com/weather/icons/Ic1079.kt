package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1079: ImageVector
    get() {
        val current = _ic1079
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1079",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M5.063 9 A2 2 0 0 0 7 10.5 h.667 l.277 2.5 c-1.317 .015 -2.918 .54 -4.336 1.146 L3.333 12.5 H4 a2 2 0 0 0 2 -2 H5 a2 2 0 0 0 -2 2 2 2 0 0 0 -2 -2 H0 a2 2 0 0 0 2 2 h.667 l.32 1.921 A30.669 30.669 0 0 0 0 16 h16 s-.556 -.345 -1.415 -.8 c-.302 -.16 -.642 -.335 -1.009 -.513 l-.243 -2.187 H14 a2 2 0 0 0 2 -2 h-1 a2 2 0 0 0 -2 2 2 2 0 0 0 -2 -2 h-1 a2 2 0 0 0 2 2 h.667 l.206 1.857 c-1.34 -.605 -2.903 -1.18 -4.258 -1.323 L8.333 10.5 H9 a2 2 0 0 0 2 -2 h-1 a2 2 0 0 0 -2 2 2 2 0 0 0 -2 -2 H5 c0 .173 .022 .34 .063 .5Z M6 9 a1.5 1.5 0 0 1 1.415 1 H7 a1.5 1.5 0 0 1 -1.415 -1 H6Z m2.585 1 A1.5 1.5 0 0 1 10 9 h.415 A1.5 1.5 0 0 1 9 10 h-.415Z M1 11 a1.5 1.5 0 0 1 1.415 1 H2 a1.5 1.5 0 0 1 -1.415 -1 H1Z m2.585 1 A1.5 1.5 0 0 1 5 11 h.415 A1.5 1.5 0 0 1 4 12 h-.415Z m8.83 0 H12 a1.5 1.5 0 0 1 -1.415 -1 H11 a1.5 1.5 0 0 1 1.415 1Z M15 11 h.415 A1.5 1.5 0 0 1 14 12 h-.415 A1.5 1.5 0 0 1 15 11Z m-7 2.8 c1.325 0 3.14 .653 4.74 1.4 H3.26 c1.6 -.747 3.415 -1.4 4.74 -1.4Z M8.227 .63 a.263 .263 0 0 0 -.454 0 L4.035 7.113 a.26 .26 0 0 0 .227 .388 h7.476 a.26 .26 0 0 0 .227 -.388 L8.227 .63Z m-.85 2.144 c-.032 -.282 .256 -.524 .623 -.524 s.655 .242 .623 .524 L8.34 5.25 h-.68 l-.282 -2.476Z M8.504 6.25 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5.063 9
                moveTo(x = 5.063f, y = 9.0f)
                // A 2 2 0 0 0 7 10.5
                arcTo(
                    horizontalEllipseRadius = 2.0f,
                    verticalEllipseRadius = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.0f,
                    y1 = 10.5f,
                )
                // h 0.667
                horizontalLineToRelative(dx = 0.667f)
                // l 0.277 2.5
                lineToRelative(dx = 0.277f, dy = 2.5f)
                // c -1.317 0.015 -2.918 0.54 -4.336 1.146
                curveToRelative(
                    dx1 = -1.317f,
                    dy1 = 0.015f,
                    dx2 = -2.918f,
                    dy2 = 0.54f,
                    dx3 = -4.336f,
                    dy3 = 1.146f,
                )
                // L 3.333 12.5
                lineTo(x = 3.333f, y = 12.5f)
                // H 4
                horizontalLineTo(x = 4.0f)
                // a 2 2 0 0 0 2 -2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.0f,
                    dy1 = -2.0f,
                )
                // H 5
                horizontalLineTo(x = 5.0f)
                // a 2 2 0 0 0 -2 2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.0f,
                    dy1 = 2.0f,
                )
                // a 2 2 0 0 0 -2 -2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.0f,
                    dy1 = -2.0f,
                )
                // H 0
                horizontalLineTo(x = 0.0f)
                // a 2 2 0 0 0 2 2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.0f,
                    dy1 = 2.0f,
                )
                // h 0.667
                horizontalLineToRelative(dx = 0.667f)
                // l 0.32 1.921
                lineToRelative(dx = 0.32f, dy = 1.921f)
                // A 30.669 30.669 0 0 0 0 16
                arcTo(
                    horizontalEllipseRadius = 30.669f,
                    verticalEllipseRadius = 30.669f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 0.0f,
                    y1 = 16.0f,
                )
                // h 16
                horizontalLineToRelative(dx = 16.0f)
                // s -0.556 -0.345 -1.415 -0.8
                reflectiveCurveToRelative(
                    dx1 = -0.556f,
                    dy1 = -0.345f,
                    dx2 = -1.415f,
                    dy2 = -0.8f,
                )
                // c -0.302 -0.16 -0.642 -0.335 -1.009 -0.513
                curveToRelative(
                    dx1 = -0.302f,
                    dy1 = -0.16f,
                    dx2 = -0.642f,
                    dy2 = -0.335f,
                    dx3 = -1.009f,
                    dy3 = -0.513f,
                )
                // l -0.243 -2.187
                lineToRelative(dx = -0.243f, dy = -2.187f)
                // H 14
                horizontalLineTo(x = 14.0f)
                // a 2 2 0 0 0 2 -2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.0f,
                    dy1 = -2.0f,
                )
                // h -1
                horizontalLineToRelative(dx = -1.0f)
                // a 2 2 0 0 0 -2 2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.0f,
                    dy1 = 2.0f,
                )
                // a 2 2 0 0 0 -2 -2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.0f,
                    dy1 = -2.0f,
                )
                // h -1
                horizontalLineToRelative(dx = -1.0f)
                // a 2 2 0 0 0 2 2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.0f,
                    dy1 = 2.0f,
                )
                // h 0.667
                horizontalLineToRelative(dx = 0.667f)
                // l 0.206 1.857
                lineToRelative(dx = 0.206f, dy = 1.857f)
                // c -1.34 -0.605 -2.903 -1.18 -4.258 -1.323
                curveToRelative(
                    dx1 = -1.34f,
                    dy1 = -0.605f,
                    dx2 = -2.903f,
                    dy2 = -1.18f,
                    dx3 = -4.258f,
                    dy3 = -1.323f,
                )
                // L 8.333 10.5
                lineTo(x = 8.333f, y = 10.5f)
                // H 9
                horizontalLineTo(x = 9.0f)
                // a 2 2 0 0 0 2 -2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.0f,
                    dy1 = -2.0f,
                )
                // h -1
                horizontalLineToRelative(dx = -1.0f)
                // a 2 2 0 0 0 -2 2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.0f,
                    dy1 = 2.0f,
                )
                // a 2 2 0 0 0 -2 -2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.0f,
                    dy1 = -2.0f,
                )
                // H 5
                horizontalLineTo(x = 5.0f)
                // c 0 0.173 0.022 0.34 0.063 0.5z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.173f,
                    dx2 = 0.022f,
                    dy2 = 0.34f,
                    dx3 = 0.063f,
                    dy3 = 0.5f,
                )
                close()
                // M 6 9
                moveTo(x = 6.0f, y = 9.0f)
                // a 1.5 1.5 0 0 1 1.415 1
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.415f,
                    dy1 = 1.0f,
                )
                // H 7
                horizontalLineTo(x = 7.0f)
                // a 1.5 1.5 0 0 1 -1.415 -1
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.415f,
                    dy1 = -1.0f,
                )
                // H 6z
                horizontalLineTo(x = 6.0f)
                close()
                // m 2.585 1
                moveToRelative(dx = 2.585f, dy = 1.0f)
                // A 1.5 1.5 0 0 1 10 9
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 10.0f,
                    y1 = 9.0f,
                )
                // h 0.415
                horizontalLineToRelative(dx = 0.415f)
                // A 1.5 1.5 0 0 1 9 10
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 9.0f,
                    y1 = 10.0f,
                )
                // h -0.415z
                horizontalLineToRelative(dx = -0.415f)
                close()
                // M 1 11
                moveTo(x = 1.0f, y = 11.0f)
                // a 1.5 1.5 0 0 1 1.415 1
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.415f,
                    dy1 = 1.0f,
                )
                // H 2
                horizontalLineTo(x = 2.0f)
                // a 1.5 1.5 0 0 1 -1.415 -1
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.415f,
                    dy1 = -1.0f,
                )
                // H 1z
                horizontalLineTo(x = 1.0f)
                close()
                // m 2.585 1
                moveToRelative(dx = 2.585f, dy = 1.0f)
                // A 1.5 1.5 0 0 1 5 11
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 5.0f,
                    y1 = 11.0f,
                )
                // h 0.415
                horizontalLineToRelative(dx = 0.415f)
                // A 1.5 1.5 0 0 1 4 12
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 4.0f,
                    y1 = 12.0f,
                )
                // h -0.415z
                horizontalLineToRelative(dx = -0.415f)
                close()
                // m 8.83 0
                moveToRelative(dx = 8.83f, dy = 0.0f)
                // H 12
                horizontalLineTo(x = 12.0f)
                // a 1.5 1.5 0 0 1 -1.415 -1
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.415f,
                    dy1 = -1.0f,
                )
                // H 11
                horizontalLineTo(x = 11.0f)
                // a 1.5 1.5 0 0 1 1.415 1z
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.415f,
                    dy1 = 1.0f,
                )
                close()
                // M 15 11
                moveTo(x = 15.0f, y = 11.0f)
                // h 0.415
                horizontalLineToRelative(dx = 0.415f)
                // A 1.5 1.5 0 0 1 14 12
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 14.0f,
                    y1 = 12.0f,
                )
                // h -0.415
                horizontalLineToRelative(dx = -0.415f)
                // A 1.5 1.5 0 0 1 15 11z
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 15.0f,
                    y1 = 11.0f,
                )
                close()
                // m -7 2.8
                moveToRelative(dx = -7.0f, dy = 2.8f)
                // c 1.325 0 3.14 0.653 4.74 1.4
                curveToRelative(
                    dx1 = 1.325f,
                    dy1 = 0.0f,
                    dx2 = 3.14f,
                    dy2 = 0.653f,
                    dx3 = 4.74f,
                    dy3 = 1.4f,
                )
                // H 3.26
                horizontalLineTo(x = 3.26f)
                // c 1.6 -0.747 3.415 -1.4 4.74 -1.4z
                curveToRelative(
                    dx1 = 1.6f,
                    dy1 = -0.747f,
                    dx2 = 3.415f,
                    dy2 = -1.4f,
                    dx3 = 4.74f,
                    dy3 = -1.4f,
                )
                close()
                // M 8.227 0.63
                moveTo(x = 8.227f, y = 0.63f)
                // a 0.263 0.263 0 0 0 -0.454 0
                arcToRelative(
                    a = 0.263f,
                    b = 0.263f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.454f,
                    dy1 = 0.0f,
                )
                // L 4.035 7.113
                lineTo(x = 4.035f, y = 7.113f)
                // a 0.26 0.26 0 0 0 0.227 0.388
                arcToRelative(
                    a = 0.26f,
                    b = 0.26f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.227f,
                    dy1 = 0.388f,
                )
                // h 7.476
                horizontalLineToRelative(dx = 7.476f)
                // a 0.26 0.26 0 0 0 0.227 -0.388
                arcToRelative(
                    a = 0.26f,
                    b = 0.26f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.227f,
                    dy1 = -0.388f,
                )
                // L 8.227 0.63z
                lineTo(x = 8.227f, y = 0.63f)
                close()
                // m -0.85 2.144
                moveToRelative(dx = -0.85f, dy = 2.144f)
                // c -0.032 -0.282 0.256 -0.524 0.623 -0.524
                curveToRelative(
                    dx1 = -0.032f,
                    dy1 = -0.282f,
                    dx2 = 0.256f,
                    dy2 = -0.524f,
                    dx3 = 0.623f,
                    dy3 = -0.524f,
                )
                // s 0.655 0.242 0.623 0.524
                reflectiveCurveToRelative(
                    dx1 = 0.655f,
                    dy1 = 0.242f,
                    dx2 = 0.623f,
                    dy2 = 0.524f,
                )
                // L 8.34 5.25
                lineTo(x = 8.34f, y = 5.25f)
                // h -0.68
                horizontalLineToRelative(dx = -0.68f)
                // l -0.282 -2.476z
                lineToRelative(dx = -0.282f, dy = -2.476f)
                close()
                // M 8.504 6.25
                moveTo(x = 8.504f, y = 6.25f)
                // a 0.5 0.5 0 1 1 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // a 0.5 0.5 0 0 1 1 0z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic1079 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1079: ImageVector? = null
