package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2111: ImageVector
    get() {
        val current = _ic2111
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2111",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8 9.8 a1.8 1.8 0 1 1 0 -3.6 1.8 1.8 0 0 1 0 3.6Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8 9.8
                moveTo(x = 8.0f, y = 9.8f)
                // a 1.8 1.8 0 1 1 0 -3.6
                arcToRelative(
                    a = 1.8f,
                    b = 1.8f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -3.6f,
                )
                // a 1.8 1.8 0 0 1 0 3.6z
                arcToRelative(
                    a = 1.8f,
                    b = 1.8f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 3.6f,
                )
                close()
            }
            // M14 8 c0 1.167 -3.59 3.5 -6 3.5 S2 9.167 2 8 c0 -1.167 3.59 -3.5 6 -3.5 s6 2.333 6 3.5Z m-1.25 -.211 .001 -.003 -.004 .008 a.11 .11 0 0 0 .003 -.005Z m-.259 .1 c-.278 -.286 -.71 -.623 -1.245 -.949 C10.14 6.267 8.875 5.812 8 5.812 s-2.14 .455 -3.246 1.128 c-.536 .326 -.967 .663 -1.245 .948 -.04 .04 -.074 .078 -.104 .112 .03 .034 .064 .071 .104 .112 .278 .285 .71 .622 1.245 .948 C5.86 9.733 7.125 10.187 8 10.187 s2.14 -.454 3.246 -1.127 c.536 -.326 .967 -.663 1.245 -.948 .04 -.04 .075 -.078 .104 -.112 a2.783 2.783 0 0 0 -.104 -.112Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 14 8
                moveTo(x = 14.0f, y = 8.0f)
                // c 0 1.167 -3.59 3.5 -6 3.5
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.167f,
                    dx2 = -3.59f,
                    dy2 = 3.5f,
                    dx3 = -6.0f,
                    dy3 = 3.5f,
                )
                // S 2 9.167 2 8
                reflectiveCurveTo(
                    x1 = 2.0f,
                    y1 = 9.167f,
                    x2 = 2.0f,
                    y2 = 8.0f,
                )
                // c 0 -1.167 3.59 -3.5 6 -3.5
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.167f,
                    dx2 = 3.59f,
                    dy2 = -3.5f,
                    dx3 = 6.0f,
                    dy3 = -3.5f,
                )
                // s 6 2.333 6 3.5z
                reflectiveCurveToRelative(
                    dx1 = 6.0f,
                    dy1 = 2.333f,
                    dx2 = 6.0f,
                    dy2 = 3.5f,
                )
                close()
                // m -1.25 -0.211
                moveToRelative(dx = -1.25f, dy = -0.211f)
                // l 0.001 -0.003
                lineToRelative(dx = 0.001f, dy = -0.003f)
                // l -0.004 0.008
                lineToRelative(dx = -0.004f, dy = 0.008f)
                // a 0.11 0.11 0 0 0 0.003 -0.005z
                arcToRelative(
                    a = 0.11f,
                    b = 0.11f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.003f,
                    dy1 = -0.005f,
                )
                close()
                // m -0.259 0.1
                moveToRelative(dx = -0.259f, dy = 0.1f)
                // c -0.278 -0.286 -0.71 -0.623 -1.245 -0.949
                curveToRelative(
                    dx1 = -0.278f,
                    dy1 = -0.286f,
                    dx2 = -0.71f,
                    dy2 = -0.623f,
                    dx3 = -1.245f,
                    dy3 = -0.949f,
                )
                // C 10.14 6.267 8.875 5.812 8 5.812
                curveTo(
                    x1 = 10.14f,
                    y1 = 6.267f,
                    x2 = 8.875f,
                    y2 = 5.812f,
                    x3 = 8.0f,
                    y3 = 5.812f,
                )
                // s -2.14 0.455 -3.246 1.128
                reflectiveCurveToRelative(
                    dx1 = -2.14f,
                    dy1 = 0.455f,
                    dx2 = -3.246f,
                    dy2 = 1.128f,
                )
                // c -0.536 0.326 -0.967 0.663 -1.245 0.948
                curveToRelative(
                    dx1 = -0.536f,
                    dy1 = 0.326f,
                    dx2 = -0.967f,
                    dy2 = 0.663f,
                    dx3 = -1.245f,
                    dy3 = 0.948f,
                )
                // c -0.04 0.04 -0.074 0.078 -0.104 0.112
                curveToRelative(
                    dx1 = -0.04f,
                    dy1 = 0.04f,
                    dx2 = -0.074f,
                    dy2 = 0.078f,
                    dx3 = -0.104f,
                    dy3 = 0.112f,
                )
                // c 0.03 0.034 0.064 0.071 0.104 0.112
                curveToRelative(
                    dx1 = 0.03f,
                    dy1 = 0.034f,
                    dx2 = 0.064f,
                    dy2 = 0.071f,
                    dx3 = 0.104f,
                    dy3 = 0.112f,
                )
                // c 0.278 0.285 0.71 0.622 1.245 0.948
                curveToRelative(
                    dx1 = 0.278f,
                    dy1 = 0.285f,
                    dx2 = 0.71f,
                    dy2 = 0.622f,
                    dx3 = 1.245f,
                    dy3 = 0.948f,
                )
                // C 5.86 9.733 7.125 10.187 8 10.187
                curveTo(
                    x1 = 5.86f,
                    y1 = 9.733f,
                    x2 = 7.125f,
                    y2 = 10.187f,
                    x3 = 8.0f,
                    y3 = 10.187f,
                )
                // s 2.14 -0.454 3.246 -1.127
                reflectiveCurveToRelative(
                    dx1 = 2.14f,
                    dy1 = -0.454f,
                    dx2 = 3.246f,
                    dy2 = -1.127f,
                )
                // c 0.536 -0.326 0.967 -0.663 1.245 -0.948
                curveToRelative(
                    dx1 = 0.536f,
                    dy1 = -0.326f,
                    dx2 = 0.967f,
                    dy2 = -0.663f,
                    dx3 = 1.245f,
                    dy3 = -0.948f,
                )
                // c 0.04 -0.04 0.075 -0.078 0.104 -0.112
                curveToRelative(
                    dx1 = 0.04f,
                    dy1 = -0.04f,
                    dx2 = 0.075f,
                    dy2 = -0.078f,
                    dx3 = 0.104f,
                    dy3 = -0.112f,
                )
                // a 2.783 2.783 0 0 0 -0.104 -0.112z
                arcToRelative(
                    a = 2.783f,
                    b = 2.783f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.104f,
                    dy1 = -0.112f,
                )
                close()
            }
            // M16 8 A8 8 0 1 1 0 8 a8 8 0 0 1 16 0Z m-1.3 0 a6.67 6.67 0 0 0 -1.352 -4.037 l-9.385 9.385 A6.7 6.7 0 0 0 14.7 8Z m-2.385 -5.126 a6.7 6.7 0 0 0 -9.44 9.44 l9.44 -9.44Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 16 8
                moveTo(x = 16.0f, y = 8.0f)
                // A 8 8 0 1 1 0 8
                arcTo(
                    horizontalEllipseRadius = 8.0f,
                    verticalEllipseRadius = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 0.0f,
                    y1 = 8.0f,
                )
                // a 8 8 0 0 1 16 0z
                arcToRelative(
                    a = 8.0f,
                    b = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 16.0f,
                    dy1 = 0.0f,
                )
                close()
                // m -1.3 0
                moveToRelative(dx = -1.3f, dy = 0.0f)
                // a 6.67 6.67 0 0 0 -1.352 -4.037
                arcToRelative(
                    a = 6.67f,
                    b = 6.67f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.352f,
                    dy1 = -4.037f,
                )
                // l -9.385 9.385
                lineToRelative(dx = -9.385f, dy = 9.385f)
                // A 6.7 6.7 0 0 0 14.7 8z
                arcTo(
                    horizontalEllipseRadius = 6.7f,
                    verticalEllipseRadius = 6.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 14.7f,
                    y1 = 8.0f,
                )
                close()
                // m -2.385 -5.126
                moveToRelative(dx = -2.385f, dy = -5.126f)
                // a 6.7 6.7 0 0 0 -9.44 9.44
                arcToRelative(
                    a = 6.7f,
                    b = 6.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -9.44f,
                    dy1 = 9.44f,
                )
                // l 9.44 -9.44z
                lineToRelative(dx = 9.44f, dy = -9.44f)
                close()
            }
        }.build().also { _ic2111 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2111: ImageVector? = null
