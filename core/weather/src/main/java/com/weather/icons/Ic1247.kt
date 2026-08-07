package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1247: ImageVector
    get() {
        val current = _ic1247
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1247",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M6.37 8.637 c-.483 -.483 -3.033 -3.515 -3.033 -3.515 l2.964 1.723 -1.379 -4.618 L7.06 6.569 8.093 0 l.344 6.5 1.585 -5.376 L9.816 6.5 l1.93 -2.757 -1.517 4.136 1.93 -1.034 s-1.654 2.067 -2.068 2.343 c-.413 .276 -1.31 .62 -1.998 .483 -.62 -.07 -1.31 -.552 -1.723 -1.034Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6.37 8.637
                moveTo(x = 6.37f, y = 8.637f)
                // c -0.483 -0.483 -3.033 -3.515 -3.033 -3.515
                curveToRelative(
                    dx1 = -0.483f,
                    dy1 = -0.483f,
                    dx2 = -3.033f,
                    dy2 = -3.515f,
                    dx3 = -3.033f,
                    dy3 = -3.515f,
                )
                // l 2.964 1.723
                lineToRelative(dx = 2.964f, dy = 1.723f)
                // l -1.379 -4.618
                lineToRelative(dx = -1.379f, dy = -4.618f)
                // L 7.06 6.569
                lineTo(x = 7.06f, y = 6.569f)
                // L 8.093 0
                lineTo(x = 8.093f, y = 0.0f)
                // l 0.344 6.5
                lineToRelative(dx = 0.344f, dy = 6.5f)
                // l 1.585 -5.376
                lineToRelative(dx = 1.585f, dy = -5.376f)
                // L 9.816 6.5
                lineTo(x = 9.816f, y = 6.5f)
                // l 1.93 -2.757
                lineToRelative(dx = 1.93f, dy = -2.757f)
                // l -1.517 4.136
                lineToRelative(dx = -1.517f, dy = 4.136f)
                // l 1.93 -1.034
                lineToRelative(dx = 1.93f, dy = -1.034f)
                // s -1.654 2.067 -2.068 2.343
                reflectiveCurveToRelative(
                    dx1 = -1.654f,
                    dy1 = 2.067f,
                    dx2 = -2.068f,
                    dy2 = 2.343f,
                )
                // c -0.413 0.276 -1.31 0.62 -1.998 0.483
                curveToRelative(
                    dx1 = -0.413f,
                    dy1 = 0.276f,
                    dx2 = -1.31f,
                    dy2 = 0.62f,
                    dx3 = -1.998f,
                    dy3 = 0.483f,
                )
                // c -0.62 -0.07 -1.31 -0.552 -1.723 -1.034z
                curveToRelative(
                    dx1 = -0.62f,
                    dy1 = -0.07f,
                    dx2 = -1.31f,
                    dy2 = -0.552f,
                    dx3 = -1.723f,
                    dy3 = -1.034f,
                )
                close()
            }
            // M7.955 10.566 C6.852 10.43 6.3 9.326 6.3 9.326 S4.962 12.07 0 16 h16 c-4.962 -3.446 -6.115 -5.985 -6.115 -5.985 s-.827 .69 -1.93 .551Z M5.75 1.5 a.75 .75 0 1 0 0 -1.5 .75 .75 0 0 0 0 1.5Z M3.286 3.929 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z m10.285 2.285 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z M14 3 a1 1 0 1 1 -2 0 1 1 0 0 1 2 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.955 10.566
                moveTo(x = 7.955f, y = 10.566f)
                // C 6.852 10.43 6.3 9.326 6.3 9.326
                curveTo(
                    x1 = 6.852f,
                    y1 = 10.43f,
                    x2 = 6.3f,
                    y2 = 9.326f,
                    x3 = 6.3f,
                    y3 = 9.326f,
                )
                // S 4.962 12.07 0 16
                reflectiveCurveTo(
                    x1 = 4.962f,
                    y1 = 12.07f,
                    x2 = 0.0f,
                    y2 = 16.0f,
                )
                // h 16
                horizontalLineToRelative(dx = 16.0f)
                // c -4.962 -3.446 -6.115 -5.985 -6.115 -5.985
                curveToRelative(
                    dx1 = -4.962f,
                    dy1 = -3.446f,
                    dx2 = -6.115f,
                    dy2 = -5.985f,
                    dx3 = -6.115f,
                    dy3 = -5.985f,
                )
                // s -0.827 0.69 -1.93 0.551z
                reflectiveCurveToRelative(
                    dx1 = -0.827f,
                    dy1 = 0.69f,
                    dx2 = -1.93f,
                    dy2 = 0.551f,
                )
                close()
                // M 5.75 1.5
                moveTo(x = 5.75f, y = 1.5f)
                // a 0.75 0.75 0 1 0 0 -1.5
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.5f,
                )
                // a 0.75 0.75 0 0 0 0 1.5z
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.5f,
                )
                close()
                // M 3.286 3.929
                moveTo(x = 3.286f, y = 3.929f)
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
                // m 10.285 2.285
                moveToRelative(dx = 10.285f, dy = 2.285f)
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
                // M 14 3
                moveTo(x = 14.0f, y = 3.0f)
                // a 1 1 0 1 1 -2 0
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -2.0f,
                    dy1 = 0.0f,
                )
                // a 1 1 0 0 1 2 0z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.0f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic1247 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1247: ImageVector? = null
