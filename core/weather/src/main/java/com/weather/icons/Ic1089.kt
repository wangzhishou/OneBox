package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1089: ImageVector
    get() {
        val current = _ic1089
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1089",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // m13 4.232 -.866 -.5 A2 2 0 0 1 11.402 1 l.866 .5 A2 2 0 0 1 13 4.232Z m-.982 -2.299 -.36 -.207 a1.5 1.5 0 0 0 .726 1.573 l.359 .207 a1.5 1.5 0 0 0 -.725 -1.573Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 13 4.232
                moveTo(x = 13.0f, y = 4.232f)
                // l -0.866 -0.5
                lineToRelative(dx = -0.866f, dy = -0.5f)
                // A 2 2 0 0 1 11.402 1
                arcTo(
                    horizontalEllipseRadius = 2.0f,
                    verticalEllipseRadius = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 11.402f,
                    y1 = 1.0f,
                )
                // l 0.866 0.5
                lineToRelative(dx = 0.866f, dy = 0.5f)
                // A 2 2 0 0 1 13 4.232z
                arcTo(
                    horizontalEllipseRadius = 2.0f,
                    verticalEllipseRadius = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 13.0f,
                    y1 = 4.232f,
                )
                close()
                // m -0.982 -2.299
                moveToRelative(dx = -0.982f, dy = -2.299f)
                // l -0.36 -0.207
                lineToRelative(dx = -0.36f, dy = -0.207f)
                // a 1.5 1.5 0 0 0 0.726 1.573
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.726f,
                    dy1 = 1.573f,
                )
                // l 0.359 0.207
                lineToRelative(dx = 0.359f, dy = 0.207f)
                // a 1.5 1.5 0 0 0 -0.725 -1.573z
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.725f,
                    dy1 = -1.573f,
                )
                close()
            }
            // M10.063 4.5 A2 2 0 0 0 12 6 h2 a2 2 0 0 0 2 -2 h-1 a2 2 0 0 0 -2 1.982 A2 2 0 0 0 11 4 h-1 c0 .173 .022 .34 .063 .5Z m.937 0 a1.5 1.5 0 0 1 1.415 1 H12 a1.5 1.5 0 0 1 -1.415 -1 H11Z m4 0 h.415 A1.5 1.5 0 0 1 14 5.5 h-.415 A1.5 1.5 0 0 1 15 4.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.063 4.5
                moveTo(x = 10.063f, y = 4.5f)
                // A 2 2 0 0 0 12 6
                arcTo(
                    horizontalEllipseRadius = 2.0f,
                    verticalEllipseRadius = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 12.0f,
                    y1 = 6.0f,
                )
                // h 2
                horizontalLineToRelative(dx = 2.0f)
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
                // a 2 2 0 0 0 -2 1.982
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.0f,
                    dy1 = 1.982f,
                )
                // A 2 2 0 0 0 11 4
                arcTo(
                    horizontalEllipseRadius = 2.0f,
                    verticalEllipseRadius = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 11.0f,
                    y1 = 4.0f,
                )
                // h -1
                horizontalLineToRelative(dx = -1.0f)
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
                // m 0.937 0
                moveToRelative(dx = 0.937f, dy = 0.0f)
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
                // H 11z
                horizontalLineTo(x = 11.0f)
                close()
                // m 4 0
                moveToRelative(dx = 4.0f, dy = 0.0f)
                // h 0.415
                horizontalLineToRelative(dx = 0.415f)
                // A 1.5 1.5 0 0 1 14 5.5
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 14.0f,
                    y1 = 5.5f,
                )
                // h -0.415
                horizontalLineToRelative(dx = -0.415f)
                // A 1.5 1.5 0 0 1 15 4.5z
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 15.0f,
                    y1 = 4.5f,
                )
                close()
            }
            // M14 8 a2 2 0 0 0 2 -2 h-1 a2 2 0 0 0 -2 1.982 A2 2 0 0 0 11 6 h-1 a2 2 0 0 0 2 2 h2Z m-.415 -.5 A1.5 1.5 0 0 1 15 6.5 h.415 A1.5 1.5 0 0 1 14 7.5 h-.415Z M11 6.5 a1.5 1.5 0 0 1 1.415 1 H12 a1.5 1.5 0 0 1 -1.415 -1 H11Z m2.09 7.956 C14.669 14.333 16 14 16 14 v2 H0 v-2 s1 -.5 3.5 -.5 c1.32 0 3.057 .279 4.698 .542 1.468 .235 2.858 .458 3.802 .458 h.083 L12.5 12 H12 a2 2 0 0 1 -2 -2 h1 a2 2 0 0 1 2 2 2 2 0 0 1 2 -2 h1 a2 2 0 0 1 -2 2 h-.5 l-.41 2.456Z m-.675 -2.956 a1.5 1.5 0 0 0 -1.415 -1 h-.415 a1.5 1.5 0 0 0 1.415 1 h.415Z m2.585 -1 a1.5 1.5 0 0 0 -1.415 1 H14 a1.5 1.5 0 0 0 1.415 -1 H15Z m-3 -.5 a2 2 0 0 1 -2 -2 h1 a2 2 0 0 1 2 2 h-1Z m.415 -.5 A1.5 1.5 0 0 0 11 8.5 h-.415 A1.5 1.5 0 0 0 12 9.5 h.415Z M13 10 a2 2 0 0 1 2 -2 h1 a2 2 0 0 1 -2 2 h-1Z m2 -1.5 a1.5 1.5 0 0 0 -1.415 1 H14 a1.5 1.5 0 0 0 1.415 -1 H15Z M6.232 6.024 A1.407 1.407 0 0 1 9 6.375 c0 .786 -.698 1.406 -1.406 1.406 H.28 a.281 .281 0 0 1 0 -.562 h7.313 c.416 0 .843 -.387 .843 -.844 a.844 .844 0 0 0 -1.66 -.211 .281 .281 0 0 1 -.545 -.14Z M2.25 8.344 a.28 .28 0 0 1 .281 -.281 h4.5 a1.125 1.125 0 1 1 -1.06 1.5 .281 .281 0 1 1 .53 -.188 .563 .563 0 1 0 .53 -.75 h-4.5 a.281 .281 0 0 1 -.281 -.281Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 14 8
                moveTo(x = 14.0f, y = 8.0f)
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
                // a 2 2 0 0 0 -2 1.982
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.0f,
                    dy1 = 1.982f,
                )
                // A 2 2 0 0 0 11 6
                arcTo(
                    horizontalEllipseRadius = 2.0f,
                    verticalEllipseRadius = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 11.0f,
                    y1 = 6.0f,
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
                // h 2z
                horizontalLineToRelative(dx = 2.0f)
                close()
                // m -0.415 -0.5
                moveToRelative(dx = -0.415f, dy = -0.5f)
                // A 1.5 1.5 0 0 1 15 6.5
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 15.0f,
                    y1 = 6.5f,
                )
                // h 0.415
                horizontalLineToRelative(dx = 0.415f)
                // A 1.5 1.5 0 0 1 14 7.5
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 14.0f,
                    y1 = 7.5f,
                )
                // h -0.415z
                horizontalLineToRelative(dx = -0.415f)
                close()
                // M 11 6.5
                moveTo(x = 11.0f, y = 6.5f)
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
                // H 11z
                horizontalLineTo(x = 11.0f)
                close()
                // m 2.09 7.956
                moveToRelative(dx = 2.09f, dy = 7.956f)
                // C 14.669 14.333 16 14 16 14
                curveTo(
                    x1 = 14.669f,
                    y1 = 14.333f,
                    x2 = 16.0f,
                    y2 = 14.0f,
                    x3 = 16.0f,
                    y3 = 14.0f,
                )
                // v 2
                verticalLineToRelative(dy = 2.0f)
                // H 0
                horizontalLineTo(x = 0.0f)
                // v -2
                verticalLineToRelative(dy = -2.0f)
                // s 1 -0.5 3.5 -0.5
                reflectiveCurveToRelative(
                    dx1 = 1.0f,
                    dy1 = -0.5f,
                    dx2 = 3.5f,
                    dy2 = -0.5f,
                )
                // c 1.32 0 3.057 0.279 4.698 0.542
                curveToRelative(
                    dx1 = 1.32f,
                    dy1 = 0.0f,
                    dx2 = 3.057f,
                    dy2 = 0.279f,
                    dx3 = 4.698f,
                    dy3 = 0.542f,
                )
                // c 1.468 0.235 2.858 0.458 3.802 0.458
                curveToRelative(
                    dx1 = 1.468f,
                    dy1 = 0.235f,
                    dx2 = 2.858f,
                    dy2 = 0.458f,
                    dx3 = 3.802f,
                    dy3 = 0.458f,
                )
                // h 0.083
                horizontalLineToRelative(dx = 0.083f)
                // L 12.5 12
                lineTo(x = 12.5f, y = 12.0f)
                // H 12
                horizontalLineTo(x = 12.0f)
                // a 2 2 0 0 1 -2 -2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.0f,
                    dy1 = -2.0f,
                )
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // a 2 2 0 0 1 2 2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.0f,
                    dy1 = 2.0f,
                )
                // a 2 2 0 0 1 2 -2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.0f,
                    dy1 = -2.0f,
                )
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // a 2 2 0 0 1 -2 2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.0f,
                    dy1 = 2.0f,
                )
                // h -0.5
                horizontalLineToRelative(dx = -0.5f)
                // l -0.41 2.456z
                lineToRelative(dx = -0.41f, dy = 2.456f)
                close()
                // m -0.675 -2.956
                moveToRelative(dx = -0.675f, dy = -2.956f)
                // a 1.5 1.5 0 0 0 -1.415 -1
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.415f,
                    dy1 = -1.0f,
                )
                // h -0.415
                horizontalLineToRelative(dx = -0.415f)
                // a 1.5 1.5 0 0 0 1.415 1
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.415f,
                    dy1 = 1.0f,
                )
                // h 0.415z
                horizontalLineToRelative(dx = 0.415f)
                close()
                // m 2.585 -1
                moveToRelative(dx = 2.585f, dy = -1.0f)
                // a 1.5 1.5 0 0 0 -1.415 1
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.415f,
                    dy1 = 1.0f,
                )
                // H 14
                horizontalLineTo(x = 14.0f)
                // a 1.5 1.5 0 0 0 1.415 -1
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.415f,
                    dy1 = -1.0f,
                )
                // H 15z
                horizontalLineTo(x = 15.0f)
                close()
                // m -3 -0.5
                moveToRelative(dx = -3.0f, dy = -0.5f)
                // a 2 2 0 0 1 -2 -2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.0f,
                    dy1 = -2.0f,
                )
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // a 2 2 0 0 1 2 2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.0f,
                    dy1 = 2.0f,
                )
                // h -1z
                horizontalLineToRelative(dx = -1.0f)
                close()
                // m 0.415 -0.5
                moveToRelative(dx = 0.415f, dy = -0.5f)
                // A 1.5 1.5 0 0 0 11 8.5
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 11.0f,
                    y1 = 8.5f,
                )
                // h -0.415
                horizontalLineToRelative(dx = -0.415f)
                // A 1.5 1.5 0 0 0 12 9.5
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 12.0f,
                    y1 = 9.5f,
                )
                // h 0.415z
                horizontalLineToRelative(dx = 0.415f)
                close()
                // M 13 10
                moveTo(x = 13.0f, y = 10.0f)
                // a 2 2 0 0 1 2 -2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.0f,
                    dy1 = -2.0f,
                )
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // a 2 2 0 0 1 -2 2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.0f,
                    dy1 = 2.0f,
                )
                // h -1z
                horizontalLineToRelative(dx = -1.0f)
                close()
                // m 2 -1.5
                moveToRelative(dx = 2.0f, dy = -1.5f)
                // a 1.5 1.5 0 0 0 -1.415 1
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.415f,
                    dy1 = 1.0f,
                )
                // H 14
                horizontalLineTo(x = 14.0f)
                // a 1.5 1.5 0 0 0 1.415 -1
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.415f,
                    dy1 = -1.0f,
                )
                // H 15z
                horizontalLineTo(x = 15.0f)
                close()
                // M 6.232 6.024
                moveTo(x = 6.232f, y = 6.024f)
                // A 1.407 1.407 0 0 1 9 6.375
                arcTo(
                    horizontalEllipseRadius = 1.407f,
                    verticalEllipseRadius = 1.407f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 9.0f,
                    y1 = 6.375f,
                )
                // c 0 0.786 -0.698 1.406 -1.406 1.406
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.786f,
                    dx2 = -0.698f,
                    dy2 = 1.406f,
                    dx3 = -1.406f,
                    dy3 = 1.406f,
                )
                // H 0.28
                horizontalLineTo(x = 0.28f)
                // a 0.281 0.281 0 0 1 0 -0.562
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -0.562f,
                )
                // h 7.313
                horizontalLineToRelative(dx = 7.313f)
                // c 0.416 0 0.843 -0.387 0.843 -0.844
                curveToRelative(
                    dx1 = 0.416f,
                    dy1 = 0.0f,
                    dx2 = 0.843f,
                    dy2 = -0.387f,
                    dx3 = 0.843f,
                    dy3 = -0.844f,
                )
                // a 0.844 0.844 0 0 0 -1.66 -0.211
                arcToRelative(
                    a = 0.844f,
                    b = 0.844f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.66f,
                    dy1 = -0.211f,
                )
                // a 0.281 0.281 0 0 1 -0.545 -0.14z
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.545f,
                    dy1 = -0.14f,
                )
                close()
                // M 2.25 8.344
                moveTo(x = 2.25f, y = 8.344f)
                // a 0.28 0.28 0 0 1 0.281 -0.281
                arcToRelative(
                    a = 0.28f,
                    b = 0.28f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.281f,
                    dy1 = -0.281f,
                )
                // h 4.5
                horizontalLineToRelative(dx = 4.5f)
                // a 1.125 1.125 0 1 1 -1.06 1.5
                arcToRelative(
                    a = 1.125f,
                    b = 1.125f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.06f,
                    dy1 = 1.5f,
                )
                // a 0.281 0.281 0 1 1 0.53 -0.188
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.53f,
                    dy1 = -0.188f,
                )
                // a 0.563 0.563 0 1 0 0.53 -0.75
                arcToRelative(
                    a = 0.563f,
                    b = 0.563f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.53f,
                    dy1 = -0.75f,
                )
                // h -4.5
                horizontalLineToRelative(dx = -4.5f)
                // a 0.281 0.281 0 0 1 -0.281 -0.281z
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.281f,
                    dy1 = -0.281f,
                )
                close()
            }
            // M1.125 9.469 H4.5 a.562 .562 0 1 1 -.53 .75 .281 .281 0 1 0 -.53 .187 1.125 1.125 0 1 0 1.06 -1.5 H1.125 a.281 .281 0 0 0 0 .563Z m3.672 -3.095 a.16 .16 0 0 0 -.051 .117 .16 .16 0 0 0 .051 .117 .182 .182 0 0 0 .125 .048 h.527 a.182 .182 0 0 0 .125 -.048 .16 .16 0 0 0 .051 -.117 .16 .16 0 0 0 -.051 -.117 .182 .182 0 0 0 -.125 -.049 h-.527 a.182 .182 0 0 0 -.125 .049Z m-.654 -1.349 a.156 .156 0 0 0 .016 .155 .186 .186 0 0 0 .27 .025 l.374 -.351 a.16 .16 0 0 0 .05 -.116 .16 .16 0 0 0 -.052 -.116 .182 .182 0 0 0 -.123 -.048 .182 .182 0 0 0 -.124 .046 l-.373 .351 a.165 .165 0 0 0 -.038 .054Z M2.64 4.01 v.495 a.16 .16 0 0 0 .05 .117 .182 .182 0 0 0 .125 .049 .182 .182 0 0 0 .124 -.049 .16 .16 0 0 0 .052 -.117 V4.01 a.16 .16 0 0 0 -.052 -.117 .182 .182 0 0 0 -.124 -.048 .182 .182 0 0 0 -.124 .048 .16 .16 0 0 0 -.052 .117Z m-1.836 .632 a.158 .158 0 0 0 .022 .209 l.373 .35 a.182 .182 0 0 0 .247 -.002 .16 .16 0 0 0 .05 -.115 .16 .16 0 0 0 -.049 -.116 l-.373 -.351 a.186 .186 0 0 0 -.192 -.036 .174 .174 0 0 0 -.078 .06Z M.052 6.37 A.16 .16 0 0 0 0 6.487 a.16 .16 0 0 0 .052 .117 .182 .182 0 0 0 .124 .048 h.527 a.182 .182 0 0 0 .125 -.048 .16 .16 0 0 0 .051 -.117 .16 .16 0 0 0 -.051 -.117 .182 .182 0 0 0 -.125 -.049 H.176 a.182 .182 0 0 0 -.124 .049Z m1.188 .283 a1.4 1.4 0 0 1 .02 -.454 1.47 1.47 0 0 1 .433 -.762 c.222 -.208 .504 -.35 .81 -.407 .308 -.057 .626 -.028 .915 .085 .29 .113 .536 .303 .71 .548 a1.425 1.425 0 0 1 .258 .99 H1.239Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 1.125 9.469
                moveTo(x = 1.125f, y = 9.469f)
                // H 4.5
                horizontalLineTo(x = 4.5f)
                // a 0.562 0.562 0 1 1 -0.53 0.75
                arcToRelative(
                    a = 0.562f,
                    b = 0.562f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.53f,
                    dy1 = 0.75f,
                )
                // a 0.281 0.281 0 1 0 -0.53 0.187
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.53f,
                    dy1 = 0.187f,
                )
                // a 1.125 1.125 0 1 0 1.06 -1.5
                arcToRelative(
                    a = 1.125f,
                    b = 1.125f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 1.06f,
                    dy1 = -1.5f,
                )
                // H 1.125
                horizontalLineTo(x = 1.125f)
                // a 0.281 0.281 0 0 0 0 0.563z
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.563f,
                )
                close()
                // m 3.672 -3.095
                moveToRelative(dx = 3.672f, dy = -3.095f)
                // a 0.16 0.16 0 0 0 -0.051 0.117
                arcToRelative(
                    a = 0.16f,
                    b = 0.16f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.051f,
                    dy1 = 0.117f,
                )
                // a 0.16 0.16 0 0 0 0.051 0.117
                arcToRelative(
                    a = 0.16f,
                    b = 0.16f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.051f,
                    dy1 = 0.117f,
                )
                // a 0.182 0.182 0 0 0 0.125 0.048
                arcToRelative(
                    a = 0.182f,
                    b = 0.182f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.125f,
                    dy1 = 0.048f,
                )
                // h 0.527
                horizontalLineToRelative(dx = 0.527f)
                // a 0.182 0.182 0 0 0 0.125 -0.048
                arcToRelative(
                    a = 0.182f,
                    b = 0.182f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.125f,
                    dy1 = -0.048f,
                )
                // a 0.16 0.16 0 0 0 0.051 -0.117
                arcToRelative(
                    a = 0.16f,
                    b = 0.16f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.051f,
                    dy1 = -0.117f,
                )
                // a 0.16 0.16 0 0 0 -0.051 -0.117
                arcToRelative(
                    a = 0.16f,
                    b = 0.16f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.051f,
                    dy1 = -0.117f,
                )
                // a 0.182 0.182 0 0 0 -0.125 -0.049
                arcToRelative(
                    a = 0.182f,
                    b = 0.182f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.125f,
                    dy1 = -0.049f,
                )
                // h -0.527
                horizontalLineToRelative(dx = -0.527f)
                // a 0.182 0.182 0 0 0 -0.125 0.049z
                arcToRelative(
                    a = 0.182f,
                    b = 0.182f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.125f,
                    dy1 = 0.049f,
                )
                close()
                // m -0.654 -1.349
                moveToRelative(dx = -0.654f, dy = -1.349f)
                // a 0.156 0.156 0 0 0 0.016 0.155
                arcToRelative(
                    a = 0.156f,
                    b = 0.156f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.016f,
                    dy1 = 0.155f,
                )
                // a 0.186 0.186 0 0 0 0.27 0.025
                arcToRelative(
                    a = 0.186f,
                    b = 0.186f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.27f,
                    dy1 = 0.025f,
                )
                // l 0.374 -0.351
                lineToRelative(dx = 0.374f, dy = -0.351f)
                // a 0.16 0.16 0 0 0 0.05 -0.116
                arcToRelative(
                    a = 0.16f,
                    b = 0.16f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.05f,
                    dy1 = -0.116f,
                )
                // a 0.16 0.16 0 0 0 -0.052 -0.116
                arcToRelative(
                    a = 0.16f,
                    b = 0.16f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.052f,
                    dy1 = -0.116f,
                )
                // a 0.182 0.182 0 0 0 -0.123 -0.048
                arcToRelative(
                    a = 0.182f,
                    b = 0.182f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.123f,
                    dy1 = -0.048f,
                )
                // a 0.182 0.182 0 0 0 -0.124 0.046
                arcToRelative(
                    a = 0.182f,
                    b = 0.182f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.124f,
                    dy1 = 0.046f,
                )
                // l -0.373 0.351
                lineToRelative(dx = -0.373f, dy = 0.351f)
                // a 0.165 0.165 0 0 0 -0.038 0.054z
                arcToRelative(
                    a = 0.165f,
                    b = 0.165f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.038f,
                    dy1 = 0.054f,
                )
                close()
                // M 2.64 4.01
                moveTo(x = 2.64f, y = 4.01f)
                // v 0.495
                verticalLineToRelative(dy = 0.495f)
                // a 0.16 0.16 0 0 0 0.05 0.117
                arcToRelative(
                    a = 0.16f,
                    b = 0.16f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.05f,
                    dy1 = 0.117f,
                )
                // a 0.182 0.182 0 0 0 0.125 0.049
                arcToRelative(
                    a = 0.182f,
                    b = 0.182f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.125f,
                    dy1 = 0.049f,
                )
                // a 0.182 0.182 0 0 0 0.124 -0.049
                arcToRelative(
                    a = 0.182f,
                    b = 0.182f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.124f,
                    dy1 = -0.049f,
                )
                // a 0.16 0.16 0 0 0 0.052 -0.117
                arcToRelative(
                    a = 0.16f,
                    b = 0.16f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.052f,
                    dy1 = -0.117f,
                )
                // V 4.01
                verticalLineTo(y = 4.01f)
                // a 0.16 0.16 0 0 0 -0.052 -0.117
                arcToRelative(
                    a = 0.16f,
                    b = 0.16f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.052f,
                    dy1 = -0.117f,
                )
                // a 0.182 0.182 0 0 0 -0.124 -0.048
                arcToRelative(
                    a = 0.182f,
                    b = 0.182f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.124f,
                    dy1 = -0.048f,
                )
                // a 0.182 0.182 0 0 0 -0.124 0.048
                arcToRelative(
                    a = 0.182f,
                    b = 0.182f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.124f,
                    dy1 = 0.048f,
                )
                // a 0.16 0.16 0 0 0 -0.052 0.117z
                arcToRelative(
                    a = 0.16f,
                    b = 0.16f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.052f,
                    dy1 = 0.117f,
                )
                close()
                // m -1.836 0.632
                moveToRelative(dx = -1.836f, dy = 0.632f)
                // a 0.158 0.158 0 0 0 0.022 0.209
                arcToRelative(
                    a = 0.158f,
                    b = 0.158f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.022f,
                    dy1 = 0.209f,
                )
                // l 0.373 0.35
                lineToRelative(dx = 0.373f, dy = 0.35f)
                // a 0.182 0.182 0 0 0 0.247 -0.002
                arcToRelative(
                    a = 0.182f,
                    b = 0.182f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.247f,
                    dy1 = -0.002f,
                )
                // a 0.16 0.16 0 0 0 0.05 -0.115
                arcToRelative(
                    a = 0.16f,
                    b = 0.16f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.05f,
                    dy1 = -0.115f,
                )
                // a 0.16 0.16 0 0 0 -0.049 -0.116
                arcToRelative(
                    a = 0.16f,
                    b = 0.16f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.049f,
                    dy1 = -0.116f,
                )
                // l -0.373 -0.351
                lineToRelative(dx = -0.373f, dy = -0.351f)
                // a 0.186 0.186 0 0 0 -0.192 -0.036
                arcToRelative(
                    a = 0.186f,
                    b = 0.186f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.192f,
                    dy1 = -0.036f,
                )
                // a 0.174 0.174 0 0 0 -0.078 0.06z
                arcToRelative(
                    a = 0.174f,
                    b = 0.174f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.078f,
                    dy1 = 0.06f,
                )
                close()
                // M 0.052 6.37
                moveTo(x = 0.052f, y = 6.37f)
                // A 0.16 0.16 0 0 0 0 6.487
                arcTo(
                    horizontalEllipseRadius = 0.16f,
                    verticalEllipseRadius = 0.16f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 0.0f,
                    y1 = 6.487f,
                )
                // a 0.16 0.16 0 0 0 0.052 0.117
                arcToRelative(
                    a = 0.16f,
                    b = 0.16f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.052f,
                    dy1 = 0.117f,
                )
                // a 0.182 0.182 0 0 0 0.124 0.048
                arcToRelative(
                    a = 0.182f,
                    b = 0.182f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.124f,
                    dy1 = 0.048f,
                )
                // h 0.527
                horizontalLineToRelative(dx = 0.527f)
                // a 0.182 0.182 0 0 0 0.125 -0.048
                arcToRelative(
                    a = 0.182f,
                    b = 0.182f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.125f,
                    dy1 = -0.048f,
                )
                // a 0.16 0.16 0 0 0 0.051 -0.117
                arcToRelative(
                    a = 0.16f,
                    b = 0.16f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.051f,
                    dy1 = -0.117f,
                )
                // a 0.16 0.16 0 0 0 -0.051 -0.117
                arcToRelative(
                    a = 0.16f,
                    b = 0.16f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.051f,
                    dy1 = -0.117f,
                )
                // a 0.182 0.182 0 0 0 -0.125 -0.049
                arcToRelative(
                    a = 0.182f,
                    b = 0.182f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.125f,
                    dy1 = -0.049f,
                )
                // H 0.176
                horizontalLineTo(x = 0.176f)
                // a 0.182 0.182 0 0 0 -0.124 0.049z
                arcToRelative(
                    a = 0.182f,
                    b = 0.182f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.124f,
                    dy1 = 0.049f,
                )
                close()
                // m 1.188 0.283
                moveToRelative(dx = 1.188f, dy = 0.283f)
                // a 1.4 1.4 0 0 1 0.02 -0.454
                arcToRelative(
                    a = 1.4f,
                    b = 1.4f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.02f,
                    dy1 = -0.454f,
                )
                // a 1.47 1.47 0 0 1 0.433 -0.762
                arcToRelative(
                    a = 1.47f,
                    b = 1.47f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.433f,
                    dy1 = -0.762f,
                )
                // c 0.222 -0.208 0.504 -0.35 0.81 -0.407
                curveToRelative(
                    dx1 = 0.222f,
                    dy1 = -0.208f,
                    dx2 = 0.504f,
                    dy2 = -0.35f,
                    dx3 = 0.81f,
                    dy3 = -0.407f,
                )
                // c 0.308 -0.057 0.626 -0.028 0.915 0.085
                curveToRelative(
                    dx1 = 0.308f,
                    dy1 = -0.057f,
                    dx2 = 0.626f,
                    dy2 = -0.028f,
                    dx3 = 0.915f,
                    dy3 = 0.085f,
                )
                // c 0.29 0.113 0.536 0.303 0.71 0.548
                curveToRelative(
                    dx1 = 0.29f,
                    dy1 = 0.113f,
                    dx2 = 0.536f,
                    dy2 = 0.303f,
                    dx3 = 0.71f,
                    dy3 = 0.548f,
                )
                // a 1.425 1.425 0 0 1 0.258 0.99
                arcToRelative(
                    a = 1.425f,
                    b = 1.425f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.258f,
                    dy1 = 0.99f,
                )
                // H 1.239z
                horizontalLineTo(x = 1.239f)
                close()
            }
        }.build().also { _ic1089 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1089: ImageVector? = null
