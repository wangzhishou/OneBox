package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2204: ImageVector
    get() {
        val current = _ic2204
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2204",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M0 3.5 A.5 .5 0 0 1 .5 3 h15 a.5 .5 0 0 1 0 1 H.5 a.5 .5 0 0 1 -.5 -.5Z m0 3 c0 -.276 .249 -.5 .556 -.5 h8.888 c.307 0 .556 .224 .556 .5 s-.249 .5 -.556 .5 H.556 C.249 7 0 6.776 0 6.5Z M6.556 9 C6.249 9 6 9.224 6 9.5 s.249 .5 .556 .5 h8.888 c.307 0 .556 -.224 .556 -.5 s-.249 -.5 -.556 -.5 H6.556Z M11 6.5 c0 -.276 .187 -.5 .417 -.5 h4.166 c.23 0 .417 .224 .417 .5 s-.187 .5 -.417 .5 h-4.166 c-.23 0 -.417 -.224 -.417 -.5Z M.417 9 C.187 9 0 9.224 0 9.5 s.187 .5 .417 .5 h4.166 c.23 0 .417 -.224 .417 -.5 S4.813 9 4.583 9 H.417Z M0 12.5 c0 -.276 .249 -.5 .556 -.5 h8.888 c.307 0 .556 .224 .556 .5 s-.249 .5 -.556 .5 H.556 C.249 13 0 12.776 0 12.5Z m13.5 .5 a.5 .5 0 1 0 0 -1 .5 .5 0 0 0 0 1Z m-1.5 -.5 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z m3.5 .5 a.5 .5 0 1 0 0 -1 .5 .5 0 0 0 0 1Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0 3.5
                moveTo(x = 0.0f, y = 3.5f)
                // A 0.5 0.5 0 0 1 0.5 3
                arcTo(
                    horizontalEllipseRadius = 0.5f,
                    verticalEllipseRadius = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 0.5f,
                    y1 = 3.0f,
                )
                // h 15
                horizontalLineToRelative(dx = 15.0f)
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
                // H 0.5
                horizontalLineTo(x = 0.5f)
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
                // m 0 3
                moveToRelative(dx = 0.0f, dy = 3.0f)
                // c 0 -0.276 0.249 -0.5 0.556 -0.5
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.276f,
                    dx2 = 0.249f,
                    dy2 = -0.5f,
                    dx3 = 0.556f,
                    dy3 = -0.5f,
                )
                // h 8.888
                horizontalLineToRelative(dx = 8.888f)
                // c 0.307 0 0.556 0.224 0.556 0.5
                curveToRelative(
                    dx1 = 0.307f,
                    dy1 = 0.0f,
                    dx2 = 0.556f,
                    dy2 = 0.224f,
                    dx3 = 0.556f,
                    dy3 = 0.5f,
                )
                // s -0.249 0.5 -0.556 0.5
                reflectiveCurveToRelative(
                    dx1 = -0.249f,
                    dy1 = 0.5f,
                    dx2 = -0.556f,
                    dy2 = 0.5f,
                )
                // H 0.556
                horizontalLineTo(x = 0.556f)
                // C 0.249 7 0 6.776 0 6.5z
                curveTo(
                    x1 = 0.249f,
                    y1 = 7.0f,
                    x2 = 0.0f,
                    y2 = 6.776f,
                    x3 = 0.0f,
                    y3 = 6.5f,
                )
                close()
                // M 6.556 9
                moveTo(x = 6.556f, y = 9.0f)
                // C 6.249 9 6 9.224 6 9.5
                curveTo(
                    x1 = 6.249f,
                    y1 = 9.0f,
                    x2 = 6.0f,
                    y2 = 9.224f,
                    x3 = 6.0f,
                    y3 = 9.5f,
                )
                // s 0.249 0.5 0.556 0.5
                reflectiveCurveToRelative(
                    dx1 = 0.249f,
                    dy1 = 0.5f,
                    dx2 = 0.556f,
                    dy2 = 0.5f,
                )
                // h 8.888
                horizontalLineToRelative(dx = 8.888f)
                // c 0.307 0 0.556 -0.224 0.556 -0.5
                curveToRelative(
                    dx1 = 0.307f,
                    dy1 = 0.0f,
                    dx2 = 0.556f,
                    dy2 = -0.224f,
                    dx3 = 0.556f,
                    dy3 = -0.5f,
                )
                // s -0.249 -0.5 -0.556 -0.5
                reflectiveCurveToRelative(
                    dx1 = -0.249f,
                    dy1 = -0.5f,
                    dx2 = -0.556f,
                    dy2 = -0.5f,
                )
                // H 6.556z
                horizontalLineTo(x = 6.556f)
                close()
                // M 11 6.5
                moveTo(x = 11.0f, y = 6.5f)
                // c 0 -0.276 0.187 -0.5 0.417 -0.5
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.276f,
                    dx2 = 0.187f,
                    dy2 = -0.5f,
                    dx3 = 0.417f,
                    dy3 = -0.5f,
                )
                // h 4.166
                horizontalLineToRelative(dx = 4.166f)
                // c 0.23 0 0.417 0.224 0.417 0.5
                curveToRelative(
                    dx1 = 0.23f,
                    dy1 = 0.0f,
                    dx2 = 0.417f,
                    dy2 = 0.224f,
                    dx3 = 0.417f,
                    dy3 = 0.5f,
                )
                // s -0.187 0.5 -0.417 0.5
                reflectiveCurveToRelative(
                    dx1 = -0.187f,
                    dy1 = 0.5f,
                    dx2 = -0.417f,
                    dy2 = 0.5f,
                )
                // h -4.166
                horizontalLineToRelative(dx = -4.166f)
                // c -0.23 0 -0.417 -0.224 -0.417 -0.5z
                curveToRelative(
                    dx1 = -0.23f,
                    dy1 = 0.0f,
                    dx2 = -0.417f,
                    dy2 = -0.224f,
                    dx3 = -0.417f,
                    dy3 = -0.5f,
                )
                close()
                // M 0.417 9
                moveTo(x = 0.417f, y = 9.0f)
                // C 0.187 9 0 9.224 0 9.5
                curveTo(
                    x1 = 0.187f,
                    y1 = 9.0f,
                    x2 = 0.0f,
                    y2 = 9.224f,
                    x3 = 0.0f,
                    y3 = 9.5f,
                )
                // s 0.187 0.5 0.417 0.5
                reflectiveCurveToRelative(
                    dx1 = 0.187f,
                    dy1 = 0.5f,
                    dx2 = 0.417f,
                    dy2 = 0.5f,
                )
                // h 4.166
                horizontalLineToRelative(dx = 4.166f)
                // c 0.23 0 0.417 -0.224 0.417 -0.5
                curveToRelative(
                    dx1 = 0.23f,
                    dy1 = 0.0f,
                    dx2 = 0.417f,
                    dy2 = -0.224f,
                    dx3 = 0.417f,
                    dy3 = -0.5f,
                )
                // S 4.813 9 4.583 9
                reflectiveCurveTo(
                    x1 = 4.813f,
                    y1 = 9.0f,
                    x2 = 4.583f,
                    y2 = 9.0f,
                )
                // H 0.417z
                horizontalLineTo(x = 0.417f)
                close()
                // M 0 12.5
                moveTo(x = 0.0f, y = 12.5f)
                // c 0 -0.276 0.249 -0.5 0.556 -0.5
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.276f,
                    dx2 = 0.249f,
                    dy2 = -0.5f,
                    dx3 = 0.556f,
                    dy3 = -0.5f,
                )
                // h 8.888
                horizontalLineToRelative(dx = 8.888f)
                // c 0.307 0 0.556 0.224 0.556 0.5
                curveToRelative(
                    dx1 = 0.307f,
                    dy1 = 0.0f,
                    dx2 = 0.556f,
                    dy2 = 0.224f,
                    dx3 = 0.556f,
                    dy3 = 0.5f,
                )
                // s -0.249 0.5 -0.556 0.5
                reflectiveCurveToRelative(
                    dx1 = -0.249f,
                    dy1 = 0.5f,
                    dx2 = -0.556f,
                    dy2 = 0.5f,
                )
                // H 0.556
                horizontalLineTo(x = 0.556f)
                // C 0.249 13 0 12.776 0 12.5z
                curveTo(
                    x1 = 0.249f,
                    y1 = 13.0f,
                    x2 = 0.0f,
                    y2 = 12.776f,
                    x3 = 0.0f,
                    y3 = 12.5f,
                )
                close()
                // m 13.5 0.5
                moveToRelative(dx = 13.5f, dy = 0.5f)
                // a 0.5 0.5 0 1 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // a 0.5 0.5 0 0 0 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
                // m -1.5 -0.5
                moveToRelative(dx = -1.5f, dy = -0.5f)
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
                // m 3.5 0.5
                moveToRelative(dx = 3.5f, dy = 0.5f)
                // a 0.5 0.5 0 1 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // a 0.5 0.5 0 0 0 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
            }
        }.build().also { _ic2204 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2204: ImageVector? = null
