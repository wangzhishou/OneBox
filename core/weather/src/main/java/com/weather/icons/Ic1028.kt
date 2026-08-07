package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1028: ImageVector
    get() {
        val current = _ic1028
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1028",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M15.25 4.13 a2.08 2.08 0 0 0 .486 -1.328 2.102 2.102 0 0 0 -3.284 -1.734 A7.305 7.305 0 0 0 8.652 0 7.35 7.35 0 0 0 1.3 7.35 c0 .879 .162 1.717 .445 2.498 A3.14 3.14 0 0 0 0 12.654 a3.15 3.15 0 0 0 3.15 3.15 3.135 3.135 0 0 0 2.748 -1.64 7.327 7.327 0 0 0 5.564 -.021 A7.35 7.35 0 0 0 16 7.35 a7.3 7.3 0 0 0 -.75 -3.222Z M3.15 14.333 a1.682 1.682 0 0 1 -1.68 -1.68 c0 -.927 .754 -1.68 1.68 -1.68 a1.681 1.681 0 0 1 0 3.36Z m5.5 -1.101 c-.812 0 -1.615 -.17 -2.357 -.497 0 -.027 .008 -.054 .008 -.081 a3.15 3.15 0 0 0 -3.117 -3.147 5.838 5.838 0 0 1 -.415 -2.155 5.888 5.888 0 0 1 5.881 -5.88 5.84 5.84 0 0 1 2.958 .806 2.08 2.08 0 0 0 -.074 .524 c0 1.16 .94 2.1 2.1 2.1 .116 0 .228 -.015 .34 -.033 a5.83 5.83 0 0 1 .557 2.483 5.889 5.889 0 0 1 -5.88 5.88Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 15.25 4.13
                moveTo(x = 15.25f, y = 4.13f)
                // a 2.08 2.08 0 0 0 0.486 -1.328
                arcToRelative(
                    a = 2.08f,
                    b = 2.08f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.486f,
                    dy1 = -1.328f,
                )
                // a 2.102 2.102 0 0 0 -3.284 -1.734
                arcToRelative(
                    a = 2.102f,
                    b = 2.102f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -3.284f,
                    dy1 = -1.734f,
                )
                // A 7.305 7.305 0 0 0 8.652 0
                arcTo(
                    horizontalEllipseRadius = 7.305f,
                    verticalEllipseRadius = 7.305f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 8.652f,
                    y1 = 0.0f,
                )
                // A 7.35 7.35 0 0 0 1.3 7.35
                arcTo(
                    horizontalEllipseRadius = 7.35f,
                    verticalEllipseRadius = 7.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 1.3f,
                    y1 = 7.35f,
                )
                // c 0 0.879 0.162 1.717 0.445 2.498
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.879f,
                    dx2 = 0.162f,
                    dy2 = 1.717f,
                    dx3 = 0.445f,
                    dy3 = 2.498f,
                )
                // A 3.14 3.14 0 0 0 0 12.654
                arcTo(
                    horizontalEllipseRadius = 3.14f,
                    verticalEllipseRadius = 3.14f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 0.0f,
                    y1 = 12.654f,
                )
                // a 3.15 3.15 0 0 0 3.15 3.15
                arcToRelative(
                    a = 3.15f,
                    b = 3.15f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 3.15f,
                    dy1 = 3.15f,
                )
                // a 3.135 3.135 0 0 0 2.748 -1.64
                arcToRelative(
                    a = 3.135f,
                    b = 3.135f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.748f,
                    dy1 = -1.64f,
                )
                // a 7.327 7.327 0 0 0 5.564 -0.021
                arcToRelative(
                    a = 7.327f,
                    b = 7.327f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 5.564f,
                    dy1 = -0.021f,
                )
                // A 7.35 7.35 0 0 0 16 7.35
                arcTo(
                    horizontalEllipseRadius = 7.35f,
                    verticalEllipseRadius = 7.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 16.0f,
                    y1 = 7.35f,
                )
                // a 7.3 7.3 0 0 0 -0.75 -3.222z
                arcToRelative(
                    a = 7.3f,
                    b = 7.3f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.75f,
                    dy1 = -3.222f,
                )
                close()
                // M 3.15 14.333
                moveTo(x = 3.15f, y = 14.333f)
                // a 1.682 1.682 0 0 1 -1.68 -1.68
                arcToRelative(
                    a = 1.682f,
                    b = 1.682f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.68f,
                    dy1 = -1.68f,
                )
                // c 0 -0.927 0.754 -1.68 1.68 -1.68
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.927f,
                    dx2 = 0.754f,
                    dy2 = -1.68f,
                    dx3 = 1.68f,
                    dy3 = -1.68f,
                )
                // a 1.681 1.681 0 0 1 0 3.36z
                arcToRelative(
                    a = 1.681f,
                    b = 1.681f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 3.36f,
                )
                close()
                // m 5.5 -1.101
                moveToRelative(dx = 5.5f, dy = -1.101f)
                // c -0.812 0 -1.615 -0.17 -2.357 -0.497
                curveToRelative(
                    dx1 = -0.812f,
                    dy1 = 0.0f,
                    dx2 = -1.615f,
                    dy2 = -0.17f,
                    dx3 = -2.357f,
                    dy3 = -0.497f,
                )
                // c 0 -0.027 0.008 -0.054 0.008 -0.081
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.027f,
                    dx2 = 0.008f,
                    dy2 = -0.054f,
                    dx3 = 0.008f,
                    dy3 = -0.081f,
                )
                // a 3.15 3.15 0 0 0 -3.117 -3.147
                arcToRelative(
                    a = 3.15f,
                    b = 3.15f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -3.117f,
                    dy1 = -3.147f,
                )
                // a 5.838 5.838 0 0 1 -0.415 -2.155
                arcToRelative(
                    a = 5.838f,
                    b = 5.838f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.415f,
                    dy1 = -2.155f,
                )
                // a 5.888 5.888 0 0 1 5.881 -5.88
                arcToRelative(
                    a = 5.888f,
                    b = 5.888f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 5.881f,
                    dy1 = -5.88f,
                )
                // a 5.84 5.84 0 0 1 2.958 0.806
                arcToRelative(
                    a = 5.84f,
                    b = 5.84f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.958f,
                    dy1 = 0.806f,
                )
                // a 2.08 2.08 0 0 0 -0.074 0.524
                arcToRelative(
                    a = 2.08f,
                    b = 2.08f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.074f,
                    dy1 = 0.524f,
                )
                // c 0 1.16 0.94 2.1 2.1 2.1
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.16f,
                    dx2 = 0.94f,
                    dy2 = 2.1f,
                    dx3 = 2.1f,
                    dy3 = 2.1f,
                )
                // c 0.116 0 0.228 -0.015 0.34 -0.033
                curveToRelative(
                    dx1 = 0.116f,
                    dy1 = 0.0f,
                    dx2 = 0.228f,
                    dy2 = -0.015f,
                    dx3 = 0.34f,
                    dy3 = -0.033f,
                )
                // a 5.83 5.83 0 0 1 0.557 2.483
                arcToRelative(
                    a = 5.83f,
                    b = 5.83f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.557f,
                    dy1 = 2.483f,
                )
                // a 5.889 5.889 0 0 1 -5.88 5.88z
                arcToRelative(
                    a = 5.889f,
                    b = 5.889f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -5.88f,
                    dy1 = 5.88f,
                )
                close()
            }
            // M8.584 4.829 a2.688 2.688 0 1 0 0 5.376 2.688 2.688 0 0 0 0 -5.376Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.584 4.829
                moveTo(x = 8.584f, y = 4.829f)
                // a 2.688 2.688 0 1 0 0 5.376
                arcToRelative(
                    a = 2.688f,
                    b = 2.688f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 5.376f,
                )
                // a 2.688 2.688 0 0 0 0 -5.376z
                arcToRelative(
                    a = 2.688f,
                    b = 2.688f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -5.376f,
                )
                close()
            }
        }.build().also { _ic1028 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1028: ImageVector? = null
