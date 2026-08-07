package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1210: ImageVector
    get() {
        val current = _ic1210
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1210",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M2.5 .5 A.5 .5 0 0 1 3 1 v1.5 h4 V2 H6 a.5 .5 0 0 1 0 -1 h3 a.5 .5 0 0 1 0 1 H8 v.5 h4 V1 a.5 .5 0 0 1 1 0 v1.5 h.25 c.69 0 1.25 .56 1.25 1.25 0 .138 .112 .25 .25 .25 h.75 a.5 .5 0 0 1 0 1 h-.75 c-.69 0 -1.25 -.56 -1.25 -1.25 a.25 .25 0 0 0 -.25 -.25 H13 V11 h2 v5 H0 v-5 h2 V3.5 H.5 a.5 .5 0 0 1 0 -1 H2 V1 a.5 .5 0 0 1 .5 -.5Z M3 11 h9 V3.5 H8 V5 h3 l-1.556 5 H5.556 L4 5 h3 V3.5 H3 V11Z m4.5 -2 c.556 0 1 -.444 1 -1 0 -.639 -1 -2 -1 -2 s-1 1.333 -1 2 c0 .556 .444 1 1 1Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 2.5 0.5
                moveTo(x = 2.5f, y = 0.5f)
                // A 0.5 0.5 0 0 1 3 1
                arcTo(
                    horizontalEllipseRadius = 0.5f,
                    verticalEllipseRadius = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 3.0f,
                    y1 = 1.0f,
                )
                // v 1.5
                verticalLineToRelative(dy = 1.5f)
                // h 4
                horizontalLineToRelative(dx = 4.0f)
                // V 2
                verticalLineTo(y = 2.0f)
                // H 6
                horizontalLineTo(x = 6.0f)
                // a 0.5 0.5 0 0 1 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h 3
                horizontalLineToRelative(dx = 3.0f)
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
                // H 8
                horizontalLineTo(x = 8.0f)
                // v 0.5
                verticalLineToRelative(dy = 0.5f)
                // h 4
                horizontalLineToRelative(dx = 4.0f)
                // V 1
                verticalLineTo(y = 1.0f)
                // a 0.5 0.5 0 0 1 1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // v 1.5
                verticalLineToRelative(dy = 1.5f)
                // h 0.25
                horizontalLineToRelative(dx = 0.25f)
                // c 0.69 0 1.25 0.56 1.25 1.25
                curveToRelative(
                    dx1 = 0.69f,
                    dy1 = 0.0f,
                    dx2 = 1.25f,
                    dy2 = 0.56f,
                    dx3 = 1.25f,
                    dy3 = 1.25f,
                )
                // c 0 0.138 0.112 0.25 0.25 0.25
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.138f,
                    dx2 = 0.112f,
                    dy2 = 0.25f,
                    dx3 = 0.25f,
                    dy3 = 0.25f,
                )
                // h 0.75
                horizontalLineToRelative(dx = 0.75f)
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
                // h -0.75
                horizontalLineToRelative(dx = -0.75f)
                // c -0.69 0 -1.25 -0.56 -1.25 -1.25
                curveToRelative(
                    dx1 = -0.69f,
                    dy1 = 0.0f,
                    dx2 = -1.25f,
                    dy2 = -0.56f,
                    dx3 = -1.25f,
                    dy3 = -1.25f,
                )
                // a 0.25 0.25 0 0 0 -0.25 -0.25
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.25f,
                    dy1 = -0.25f,
                )
                // H 13
                horizontalLineTo(x = 13.0f)
                // V 11
                verticalLineTo(y = 11.0f)
                // h 2
                horizontalLineToRelative(dx = 2.0f)
                // v 5
                verticalLineToRelative(dy = 5.0f)
                // H 0
                horizontalLineTo(x = 0.0f)
                // v -5
                verticalLineToRelative(dy = -5.0f)
                // h 2
                horizontalLineToRelative(dx = 2.0f)
                // V 3.5
                verticalLineTo(y = 3.5f)
                // H 0.5
                horizontalLineTo(x = 0.5f)
                // a 0.5 0.5 0 0 1 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // H 2
                horizontalLineTo(x = 2.0f)
                // V 1
                verticalLineTo(y = 1.0f)
                // a 0.5 0.5 0 0 1 0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = -0.5f,
                )
                close()
                // M 3 11
                moveTo(x = 3.0f, y = 11.0f)
                // h 9
                horizontalLineToRelative(dx = 9.0f)
                // V 3.5
                verticalLineTo(y = 3.5f)
                // H 8
                horizontalLineTo(x = 8.0f)
                // V 5
                verticalLineTo(y = 5.0f)
                // h 3
                horizontalLineToRelative(dx = 3.0f)
                // l -1.556 5
                lineToRelative(dx = -1.556f, dy = 5.0f)
                // H 5.556
                horizontalLineTo(x = 5.556f)
                // L 4 5
                lineTo(x = 4.0f, y = 5.0f)
                // h 3
                horizontalLineToRelative(dx = 3.0f)
                // V 3.5
                verticalLineTo(y = 3.5f)
                // H 3
                horizontalLineTo(x = 3.0f)
                // V 11z
                verticalLineTo(y = 11.0f)
                close()
                // m 4.5 -2
                moveToRelative(dx = 4.5f, dy = -2.0f)
                // c 0.556 0 1 -0.444 1 -1
                curveToRelative(
                    dx1 = 0.556f,
                    dy1 = 0.0f,
                    dx2 = 1.0f,
                    dy2 = -0.444f,
                    dx3 = 1.0f,
                    dy3 = -1.0f,
                )
                // c 0 -0.639 -1 -2 -1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.639f,
                    dx2 = -1.0f,
                    dy2 = -2.0f,
                    dx3 = -1.0f,
                    dy3 = -2.0f,
                )
                // s -1 1.333 -1 2
                reflectiveCurveToRelative(
                    dx1 = -1.0f,
                    dy1 = 1.333f,
                    dx2 = -1.0f,
                    dy2 = 2.0f,
                )
                // c 0 0.556 0.444 1 1 1z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.556f,
                    dx2 = 0.444f,
                    dy2 = 1.0f,
                    dx3 = 1.0f,
                    dy3 = 1.0f,
                )
                close()
            }
        }.build().also { _ic1210 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1210: ImageVector? = null
