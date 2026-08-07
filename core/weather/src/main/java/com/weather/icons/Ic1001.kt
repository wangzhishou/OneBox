package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1001: ImageVector
    get() {
        val current = _ic1001
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1001",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M9.21 1.491 c-3.757 -.066 -6.613 2.537 -6.88 5.705 -.292 3.494 2.107 4.947 2.428 5.232 C2.853 12.4 .585 10.28 .204 8.296 a.104 .104 0 0 0 -.1 -.085 .103 .103 0 0 0 -.103 .114 c.403 3.526 3.405 6.2 6.79 6.186 3.604 -.016 6.518 -2.147 6.89 -5.646 .35 -3.295 -2.108 -5.008 -2.424 -5.292 2.023 .02 4.162 2.15 4.54 4.133 .008 .048 .05 .084 .098 .085 a.102 .102 0 0 0 .1 -.071 .103 .103 0 0 0 .004 -.043 c-.406 -3.521 -3.405 -6.126 -6.788 -6.185Z M8 9.503 A1.502 1.502 0 1 1 8 6.5 a1.502 1.502 0 0 1 0 3.004Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 9.21 1.491
                moveTo(x = 9.21f, y = 1.491f)
                // c -3.757 -0.066 -6.613 2.537 -6.88 5.705
                curveToRelative(
                    dx1 = -3.757f,
                    dy1 = -0.066f,
                    dx2 = -6.613f,
                    dy2 = 2.537f,
                    dx3 = -6.88f,
                    dy3 = 5.705f,
                )
                // c -0.292 3.494 2.107 4.947 2.428 5.232
                curveToRelative(
                    dx1 = -0.292f,
                    dy1 = 3.494f,
                    dx2 = 2.107f,
                    dy2 = 4.947f,
                    dx3 = 2.428f,
                    dy3 = 5.232f,
                )
                // C 2.853 12.4 0.585 10.28 0.204 8.296
                curveTo(
                    x1 = 2.853f,
                    y1 = 12.4f,
                    x2 = 0.585f,
                    y2 = 10.28f,
                    x3 = 0.204f,
                    y3 = 8.296f,
                )
                // a 0.104 0.104 0 0 0 -0.1 -0.085
                arcToRelative(
                    a = 0.104f,
                    b = 0.104f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.1f,
                    dy1 = -0.085f,
                )
                // a 0.103 0.103 0 0 0 -0.103 0.114
                arcToRelative(
                    a = 0.103f,
                    b = 0.103f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.103f,
                    dy1 = 0.114f,
                )
                // c 0.403 3.526 3.405 6.2 6.79 6.186
                curveToRelative(
                    dx1 = 0.403f,
                    dy1 = 3.526f,
                    dx2 = 3.405f,
                    dy2 = 6.2f,
                    dx3 = 6.79f,
                    dy3 = 6.186f,
                )
                // c 3.604 -0.016 6.518 -2.147 6.89 -5.646
                curveToRelative(
                    dx1 = 3.604f,
                    dy1 = -0.016f,
                    dx2 = 6.518f,
                    dy2 = -2.147f,
                    dx3 = 6.89f,
                    dy3 = -5.646f,
                )
                // c 0.35 -3.295 -2.108 -5.008 -2.424 -5.292
                curveToRelative(
                    dx1 = 0.35f,
                    dy1 = -3.295f,
                    dx2 = -2.108f,
                    dy2 = -5.008f,
                    dx3 = -2.424f,
                    dy3 = -5.292f,
                )
                // c 2.023 0.02 4.162 2.15 4.54 4.133
                curveToRelative(
                    dx1 = 2.023f,
                    dy1 = 0.02f,
                    dx2 = 4.162f,
                    dy2 = 2.15f,
                    dx3 = 4.54f,
                    dy3 = 4.133f,
                )
                // c 0.008 0.048 0.05 0.084 0.098 0.085
                curveToRelative(
                    dx1 = 0.008f,
                    dy1 = 0.048f,
                    dx2 = 0.05f,
                    dy2 = 0.084f,
                    dx3 = 0.098f,
                    dy3 = 0.085f,
                )
                // a 0.102 0.102 0 0 0 0.1 -0.071
                arcToRelative(
                    a = 0.102f,
                    b = 0.102f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.1f,
                    dy1 = -0.071f,
                )
                // a 0.103 0.103 0 0 0 0.004 -0.043
                arcToRelative(
                    a = 0.103f,
                    b = 0.103f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.004f,
                    dy1 = -0.043f,
                )
                // c -0.406 -3.521 -3.405 -6.126 -6.788 -6.185z
                curveToRelative(
                    dx1 = -0.406f,
                    dy1 = -3.521f,
                    dx2 = -3.405f,
                    dy2 = -6.126f,
                    dx3 = -6.788f,
                    dy3 = -6.185f,
                )
                close()
                // M 8 9.503
                moveTo(x = 8.0f, y = 9.503f)
                // A 1.502 1.502 0 1 1 8 6.5
                arcTo(
                    horizontalEllipseRadius = 1.502f,
                    verticalEllipseRadius = 1.502f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 8.0f,
                    y1 = 6.5f,
                )
                // a 1.502 1.502 0 0 1 0 3.004z
                arcToRelative(
                    a = 1.502f,
                    b = 1.502f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 3.004f,
                )
                close()
            }
        }.build().also { _ic1001 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1001: ImageVector? = null
