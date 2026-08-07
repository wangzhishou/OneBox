package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic399: ImageVector
    get() {
        val current = _ic399
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic399",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M1 11 a4 4 0 1 0 8 0 c0 -.46 -.132 -1.083 -.41 -1.853 -.273 -.757 -.666 -1.6 -1.137 -2.477 C6.736 5.337 5.859 3.958 5 2.72 c-.86 1.238 -1.736 2.617 -2.453 3.95 -.471 .878 -.864 1.72 -1.138 2.477 C1.132 9.917 1 10.541 1 11Z m3.385 -9.146 C4.593 1.56 4.8 1.274 5 1 c.201 .274 .407 .56 .615 .854 C7.703 4.82 10 8.726 10 11 a5 5 0 1 1 -10 0 c0 -2.274 2.297 -6.181 4.385 -9.146Z M10.8 5.333 c0 -.184 .062 -.472 .223 -.868 .155 -.384 .383 -.819 .662 -1.28 .384 -.636 .85 -1.295 1.315 -1.901 .466 .606 .93 1.265 1.316 1.901 .278 .461 .505 .896 .661 1.28 .16 .396 .223 .684 .223 .868 0 .463 -.206 .929 -.61 1.288 A2.401 2.401 0 0 1 13 7.2 a2.401 2.401 0 0 1 -1.59 -.579 c-.404 -.36 -.61 -.825 -.61 -1.288Z M12.488 .637 C11.276 2.193 10 4.167 10 5.333 c0 .708 .316 1.386 .879 1.886 .562 .5 1.325 .781 2.121 .781 s1.559 -.28 2.121 -.781 c.563 -.5 .879 -1.178 .879 -1.886 0 -1.166 -1.276 -3.14 -2.488 -4.696 A28.476 28.476 0 0 0 13 0 c-.167 .202 -.338 .415 -.512 .637Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 1 11
                moveTo(x = 1.0f, y = 11.0f)
                // a 4 4 0 1 0 8 0
                arcToRelative(
                    a = 4.0f,
                    b = 4.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 8.0f,
                    dy1 = 0.0f,
                )
                // c 0 -0.46 -0.132 -1.083 -0.41 -1.853
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.46f,
                    dx2 = -0.132f,
                    dy2 = -1.083f,
                    dx3 = -0.41f,
                    dy3 = -1.853f,
                )
                // c -0.273 -0.757 -0.666 -1.6 -1.137 -2.477
                curveToRelative(
                    dx1 = -0.273f,
                    dy1 = -0.757f,
                    dx2 = -0.666f,
                    dy2 = -1.6f,
                    dx3 = -1.137f,
                    dy3 = -2.477f,
                )
                // C 6.736 5.337 5.859 3.958 5 2.72
                curveTo(
                    x1 = 6.736f,
                    y1 = 5.337f,
                    x2 = 5.859f,
                    y2 = 3.958f,
                    x3 = 5.0f,
                    y3 = 2.72f,
                )
                // c -0.86 1.238 -1.736 2.617 -2.453 3.95
                curveToRelative(
                    dx1 = -0.86f,
                    dy1 = 1.238f,
                    dx2 = -1.736f,
                    dy2 = 2.617f,
                    dx3 = -2.453f,
                    dy3 = 3.95f,
                )
                // c -0.471 0.878 -0.864 1.72 -1.138 2.477
                curveToRelative(
                    dx1 = -0.471f,
                    dy1 = 0.878f,
                    dx2 = -0.864f,
                    dy2 = 1.72f,
                    dx3 = -1.138f,
                    dy3 = 2.477f,
                )
                // C 1.132 9.917 1 10.541 1 11z
                curveTo(
                    x1 = 1.132f,
                    y1 = 9.917f,
                    x2 = 1.0f,
                    y2 = 10.541f,
                    x3 = 1.0f,
                    y3 = 11.0f,
                )
                close()
                // m 3.385 -9.146
                moveToRelative(dx = 3.385f, dy = -9.146f)
                // C 4.593 1.56 4.8 1.274 5 1
                curveTo(
                    x1 = 4.593f,
                    y1 = 1.56f,
                    x2 = 4.8f,
                    y2 = 1.274f,
                    x3 = 5.0f,
                    y3 = 1.0f,
                )
                // c 0.201 0.274 0.407 0.56 0.615 0.854
                curveToRelative(
                    dx1 = 0.201f,
                    dy1 = 0.274f,
                    dx2 = 0.407f,
                    dy2 = 0.56f,
                    dx3 = 0.615f,
                    dy3 = 0.854f,
                )
                // C 7.703 4.82 10 8.726 10 11
                curveTo(
                    x1 = 7.703f,
                    y1 = 4.82f,
                    x2 = 10.0f,
                    y2 = 8.726f,
                    x3 = 10.0f,
                    y3 = 11.0f,
                )
                // a 5 5 0 1 1 -10 0
                arcToRelative(
                    a = 5.0f,
                    b = 5.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -10.0f,
                    dy1 = 0.0f,
                )
                // c 0 -2.274 2.297 -6.181 4.385 -9.146z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -2.274f,
                    dx2 = 2.297f,
                    dy2 = -6.181f,
                    dx3 = 4.385f,
                    dy3 = -9.146f,
                )
                close()
                // M 10.8 5.333
                moveTo(x = 10.8f, y = 5.333f)
                // c 0 -0.184 0.062 -0.472 0.223 -0.868
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.184f,
                    dx2 = 0.062f,
                    dy2 = -0.472f,
                    dx3 = 0.223f,
                    dy3 = -0.868f,
                )
                // c 0.155 -0.384 0.383 -0.819 0.662 -1.28
                curveToRelative(
                    dx1 = 0.155f,
                    dy1 = -0.384f,
                    dx2 = 0.383f,
                    dy2 = -0.819f,
                    dx3 = 0.662f,
                    dy3 = -1.28f,
                )
                // c 0.384 -0.636 0.85 -1.295 1.315 -1.901
                curveToRelative(
                    dx1 = 0.384f,
                    dy1 = -0.636f,
                    dx2 = 0.85f,
                    dy2 = -1.295f,
                    dx3 = 1.315f,
                    dy3 = -1.901f,
                )
                // c 0.466 0.606 0.93 1.265 1.316 1.901
                curveToRelative(
                    dx1 = 0.466f,
                    dy1 = 0.606f,
                    dx2 = 0.93f,
                    dy2 = 1.265f,
                    dx3 = 1.316f,
                    dy3 = 1.901f,
                )
                // c 0.278 0.461 0.505 0.896 0.661 1.28
                curveToRelative(
                    dx1 = 0.278f,
                    dy1 = 0.461f,
                    dx2 = 0.505f,
                    dy2 = 0.896f,
                    dx3 = 0.661f,
                    dy3 = 1.28f,
                )
                // c 0.16 0.396 0.223 0.684 0.223 0.868
                curveToRelative(
                    dx1 = 0.16f,
                    dy1 = 0.396f,
                    dx2 = 0.223f,
                    dy2 = 0.684f,
                    dx3 = 0.223f,
                    dy3 = 0.868f,
                )
                // c 0 0.463 -0.206 0.929 -0.61 1.288
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.463f,
                    dx2 = -0.206f,
                    dy2 = 0.929f,
                    dx3 = -0.61f,
                    dy3 = 1.288f,
                )
                // A 2.401 2.401 0 0 1 13 7.2
                arcTo(
                    horizontalEllipseRadius = 2.401f,
                    verticalEllipseRadius = 2.401f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 13.0f,
                    y1 = 7.2f,
                )
                // a 2.401 2.401 0 0 1 -1.59 -0.579
                arcToRelative(
                    a = 2.401f,
                    b = 2.401f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.59f,
                    dy1 = -0.579f,
                )
                // c -0.404 -0.36 -0.61 -0.825 -0.61 -1.288z
                curveToRelative(
                    dx1 = -0.404f,
                    dy1 = -0.36f,
                    dx2 = -0.61f,
                    dy2 = -0.825f,
                    dx3 = -0.61f,
                    dy3 = -1.288f,
                )
                close()
                // M 12.488 0.637
                moveTo(x = 12.488f, y = 0.637f)
                // C 11.276 2.193 10 4.167 10 5.333
                curveTo(
                    x1 = 11.276f,
                    y1 = 2.193f,
                    x2 = 10.0f,
                    y2 = 4.167f,
                    x3 = 10.0f,
                    y3 = 5.333f,
                )
                // c 0 0.708 0.316 1.386 0.879 1.886
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.708f,
                    dx2 = 0.316f,
                    dy2 = 1.386f,
                    dx3 = 0.879f,
                    dy3 = 1.886f,
                )
                // c 0.562 0.5 1.325 0.781 2.121 0.781
                curveToRelative(
                    dx1 = 0.562f,
                    dy1 = 0.5f,
                    dx2 = 1.325f,
                    dy2 = 0.781f,
                    dx3 = 2.121f,
                    dy3 = 0.781f,
                )
                // s 1.559 -0.28 2.121 -0.781
                reflectiveCurveToRelative(
                    dx1 = 1.559f,
                    dy1 = -0.28f,
                    dx2 = 2.121f,
                    dy2 = -0.781f,
                )
                // c 0.563 -0.5 0.879 -1.178 0.879 -1.886
                curveToRelative(
                    dx1 = 0.563f,
                    dy1 = -0.5f,
                    dx2 = 0.879f,
                    dy2 = -1.178f,
                    dx3 = 0.879f,
                    dy3 = -1.886f,
                )
                // c 0 -1.166 -1.276 -3.14 -2.488 -4.696
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.166f,
                    dx2 = -1.276f,
                    dy2 = -3.14f,
                    dx3 = -2.488f,
                    dy3 = -4.696f,
                )
                // A 28.476 28.476 0 0 0 13 0
                arcTo(
                    horizontalEllipseRadius = 28.476f,
                    verticalEllipseRadius = 28.476f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 13.0f,
                    y1 = 0.0f,
                )
                // c -0.167 0.202 -0.338 0.415 -0.512 0.637z
                curveToRelative(
                    dx1 = -0.167f,
                    dy1 = 0.202f,
                    dx2 = -0.338f,
                    dy2 = 0.415f,
                    dx3 = -0.512f,
                    dy3 = 0.637f,
                )
                close()
            }
        }.build().also { _ic399 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic399: ImageVector? = null
