package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic399Fill: ImageVector
    get() {
        val current = _ic399Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic399Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M1.464 14.536 A5 5 0 0 1 0 11 C0 8.5 2.777 4.025 5 1 c2.223 3.025 5 7.5 5 10 a5 5 0 0 1 -8.536 3.536Z m9.415 -7.317 c-.563 -.5 -.879 -1.178 -.879 -1.886 C10 4 11.666 1.613 13 0 c1.334 1.613 3 4 3 5.333 0 .708 -.316 1.386 -.879 1.886 C14.56 7.719 13.796 8 13 8 s-1.559 -.28 -2.121 -.781Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 1.464 14.536
                moveTo(x = 1.464f, y = 14.536f)
                // A 5 5 0 0 1 0 11
                arcTo(
                    horizontalEllipseRadius = 5.0f,
                    verticalEllipseRadius = 5.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 0.0f,
                    y1 = 11.0f,
                )
                // C 0 8.5 2.777 4.025 5 1
                curveTo(
                    x1 = 0.0f,
                    y1 = 8.5f,
                    x2 = 2.777f,
                    y2 = 4.025f,
                    x3 = 5.0f,
                    y3 = 1.0f,
                )
                // c 2.223 3.025 5 7.5 5 10
                curveToRelative(
                    dx1 = 2.223f,
                    dy1 = 3.025f,
                    dx2 = 5.0f,
                    dy2 = 7.5f,
                    dx3 = 5.0f,
                    dy3 = 10.0f,
                )
                // a 5 5 0 0 1 -8.536 3.536z
                arcToRelative(
                    a = 5.0f,
                    b = 5.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -8.536f,
                    dy1 = 3.536f,
                )
                close()
                // m 9.415 -7.317
                moveToRelative(dx = 9.415f, dy = -7.317f)
                // c -0.563 -0.5 -0.879 -1.178 -0.879 -1.886
                curveToRelative(
                    dx1 = -0.563f,
                    dy1 = -0.5f,
                    dx2 = -0.879f,
                    dy2 = -1.178f,
                    dx3 = -0.879f,
                    dy3 = -1.886f,
                )
                // C 10 4 11.666 1.613 13 0
                curveTo(
                    x1 = 10.0f,
                    y1 = 4.0f,
                    x2 = 11.666f,
                    y2 = 1.613f,
                    x3 = 13.0f,
                    y3 = 0.0f,
                )
                // c 1.334 1.613 3 4 3 5.333
                curveToRelative(
                    dx1 = 1.334f,
                    dy1 = 1.613f,
                    dx2 = 3.0f,
                    dy2 = 4.0f,
                    dx3 = 3.0f,
                    dy3 = 5.333f,
                )
                // c 0 0.708 -0.316 1.386 -0.879 1.886
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.708f,
                    dx2 = -0.316f,
                    dy2 = 1.386f,
                    dx3 = -0.879f,
                    dy3 = 1.886f,
                )
                // C 14.56 7.719 13.796 8 13 8
                curveTo(
                    x1 = 14.56f,
                    y1 = 7.719f,
                    x2 = 13.796f,
                    y2 = 8.0f,
                    x3 = 13.0f,
                    y3 = 8.0f,
                )
                // s -1.559 -0.28 -2.121 -0.781z
                reflectiveCurveToRelative(
                    dx1 = -1.559f,
                    dy1 = -0.28f,
                    dx2 = -2.121f,
                    dy2 = -0.781f,
                )
                close()
            }
        }.build().also { _ic399Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic399Fill: ImageVector? = null
