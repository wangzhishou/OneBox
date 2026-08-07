package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic151Fill: ImageVector
    get() {
        val current = _ic151Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic151Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M10.994 14.396 A4.758 4.758 0 0 1 7.406 16 a4.76 4.76 0 0 1 -3.537 -1.547 2.908 2.908 0 0 1 -1.057 .197 C1.26 14.65 0 13.441 0 11.95 s1.26 -2.7 2.813 -2.7 c.173 0 .342 .015 .507 .044 C4.124 7.924 5.652 7 7.406 7 c1.769 0 3.308 .94 4.107 2.328 a2.93 2.93 0 0 1 .675 -.078 c1.553 0 2.812 1.209 2.812 2.7 s-1.26 2.7 -2.813 2.7 a2.9 2.9 0 0 1 -1.193 -.254Z m4.611 -7.762 a.412 .412 0 0 0 -.109 .015 4.127 4.127 0 0 1 -1.082 .145 4.303 4.303 0 0 1 -1.424 -.248 4.276 4.276 0 0 1 -2.725 -5.086 A.389 .389 0 0 0 9.9 .972 a.374 .374 0 0 0 -.14 .027 A4.772 4.772 0 0 0 6.779 5.72 c.007 .12 .038 .233 .055 .35 a5.29 5.29 0 0 1 .667 -.045 c.113 0 .224 .012 .336 .02 a3.558 3.558 0 0 1 -.06 -.384 3.782 3.782 0 0 1 1.357 -3.138 c.003 .553 .092 1.103 .262 1.629 A5.25 5.25 0 0 0 12.66 7.49 c.563 .2 1.156 .302 1.754 .304 h.047 a3.79 3.79 0 0 1 -.886 .771 c.32 .165 .614 .374 .874 .622 a4.774 4.774 0 0 0 1.525 -2.037 .384 .384 0 0 0 -.37 -.516 h.001Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.994 14.396
                moveTo(x = 10.994f, y = 14.396f)
                // A 4.758 4.758 0 0 1 7.406 16
                arcTo(
                    horizontalEllipseRadius = 4.758f,
                    verticalEllipseRadius = 4.758f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.406f,
                    y1 = 16.0f,
                )
                // a 4.76 4.76 0 0 1 -3.537 -1.547
                arcToRelative(
                    a = 4.76f,
                    b = 4.76f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.537f,
                    dy1 = -1.547f,
                )
                // a 2.908 2.908 0 0 1 -1.057 0.197
                arcToRelative(
                    a = 2.908f,
                    b = 2.908f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.057f,
                    dy1 = 0.197f,
                )
                // C 1.26 14.65 0 13.441 0 11.95
                curveTo(
                    x1 = 1.26f,
                    y1 = 14.65f,
                    x2 = 0.0f,
                    y2 = 13.441f,
                    x3 = 0.0f,
                    y3 = 11.95f,
                )
                // s 1.26 -2.7 2.813 -2.7
                reflectiveCurveToRelative(
                    dx1 = 1.26f,
                    dy1 = -2.7f,
                    dx2 = 2.813f,
                    dy2 = -2.7f,
                )
                // c 0.173 0 0.342 0.015 0.507 0.044
                curveToRelative(
                    dx1 = 0.173f,
                    dy1 = 0.0f,
                    dx2 = 0.342f,
                    dy2 = 0.015f,
                    dx3 = 0.507f,
                    dy3 = 0.044f,
                )
                // C 4.124 7.924 5.652 7 7.406 7
                curveTo(
                    x1 = 4.124f,
                    y1 = 7.924f,
                    x2 = 5.652f,
                    y2 = 7.0f,
                    x3 = 7.406f,
                    y3 = 7.0f,
                )
                // c 1.769 0 3.308 0.94 4.107 2.328
                curveToRelative(
                    dx1 = 1.769f,
                    dy1 = 0.0f,
                    dx2 = 3.308f,
                    dy2 = 0.94f,
                    dx3 = 4.107f,
                    dy3 = 2.328f,
                )
                // a 2.93 2.93 0 0 1 0.675 -0.078
                arcToRelative(
                    a = 2.93f,
                    b = 2.93f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.675f,
                    dy1 = -0.078f,
                )
                // c 1.553 0 2.812 1.209 2.812 2.7
                curveToRelative(
                    dx1 = 1.553f,
                    dy1 = 0.0f,
                    dx2 = 2.812f,
                    dy2 = 1.209f,
                    dx3 = 2.812f,
                    dy3 = 2.7f,
                )
                // s -1.26 2.7 -2.813 2.7
                reflectiveCurveToRelative(
                    dx1 = -1.26f,
                    dy1 = 2.7f,
                    dx2 = -2.813f,
                    dy2 = 2.7f,
                )
                // a 2.9 2.9 0 0 1 -1.193 -0.254z
                arcToRelative(
                    a = 2.9f,
                    b = 2.9f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.193f,
                    dy1 = -0.254f,
                )
                close()
                // m 4.611 -7.762
                moveToRelative(dx = 4.611f, dy = -7.762f)
                // a 0.412 0.412 0 0 0 -0.109 0.015
                arcToRelative(
                    a = 0.412f,
                    b = 0.412f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.109f,
                    dy1 = 0.015f,
                )
                // a 4.127 4.127 0 0 1 -1.082 0.145
                arcToRelative(
                    a = 4.127f,
                    b = 4.127f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.082f,
                    dy1 = 0.145f,
                )
                // a 4.303 4.303 0 0 1 -1.424 -0.248
                arcToRelative(
                    a = 4.303f,
                    b = 4.303f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.424f,
                    dy1 = -0.248f,
                )
                // a 4.276 4.276 0 0 1 -2.725 -5.086
                arcToRelative(
                    a = 4.276f,
                    b = 4.276f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.725f,
                    dy1 = -5.086f,
                )
                // A 0.389 0.389 0 0 0 9.9 0.972
                arcTo(
                    horizontalEllipseRadius = 0.389f,
                    verticalEllipseRadius = 0.389f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 9.9f,
                    y1 = 0.972f,
                )
                // a 0.374 0.374 0 0 0 -0.14 0.027
                arcToRelative(
                    a = 0.374f,
                    b = 0.374f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.14f,
                    dy1 = 0.027f,
                )
                // A 4.772 4.772 0 0 0 6.779 5.72
                arcTo(
                    horizontalEllipseRadius = 4.772f,
                    verticalEllipseRadius = 4.772f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 6.779f,
                    y1 = 5.72f,
                )
                // c 0.007 0.12 0.038 0.233 0.055 0.35
                curveToRelative(
                    dx1 = 0.007f,
                    dy1 = 0.12f,
                    dx2 = 0.038f,
                    dy2 = 0.233f,
                    dx3 = 0.055f,
                    dy3 = 0.35f,
                )
                // a 5.29 5.29 0 0 1 0.667 -0.045
                arcToRelative(
                    a = 5.29f,
                    b = 5.29f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.667f,
                    dy1 = -0.045f,
                )
                // c 0.113 0 0.224 0.012 0.336 0.02
                curveToRelative(
                    dx1 = 0.113f,
                    dy1 = 0.0f,
                    dx2 = 0.224f,
                    dy2 = 0.012f,
                    dx3 = 0.336f,
                    dy3 = 0.02f,
                )
                // a 3.558 3.558 0 0 1 -0.06 -0.384
                arcToRelative(
                    a = 3.558f,
                    b = 3.558f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.06f,
                    dy1 = -0.384f,
                )
                // a 3.782 3.782 0 0 1 1.357 -3.138
                arcToRelative(
                    a = 3.782f,
                    b = 3.782f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.357f,
                    dy1 = -3.138f,
                )
                // c 0.003 0.553 0.092 1.103 0.262 1.629
                curveToRelative(
                    dx1 = 0.003f,
                    dy1 = 0.553f,
                    dx2 = 0.092f,
                    dy2 = 1.103f,
                    dx3 = 0.262f,
                    dy3 = 1.629f,
                )
                // A 5.25 5.25 0 0 0 12.66 7.49
                arcTo(
                    horizontalEllipseRadius = 5.25f,
                    verticalEllipseRadius = 5.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 12.66f,
                    y1 = 7.49f,
                )
                // c 0.563 0.2 1.156 0.302 1.754 0.304
                curveToRelative(
                    dx1 = 0.563f,
                    dy1 = 0.2f,
                    dx2 = 1.156f,
                    dy2 = 0.302f,
                    dx3 = 1.754f,
                    dy3 = 0.304f,
                )
                // h 0.047
                horizontalLineToRelative(dx = 0.047f)
                // a 3.79 3.79 0 0 1 -0.886 0.771
                arcToRelative(
                    a = 3.79f,
                    b = 3.79f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.886f,
                    dy1 = 0.771f,
                )
                // c 0.32 0.165 0.614 0.374 0.874 0.622
                curveToRelative(
                    dx1 = 0.32f,
                    dy1 = 0.165f,
                    dx2 = 0.614f,
                    dy2 = 0.374f,
                    dx3 = 0.874f,
                    dy3 = 0.622f,
                )
                // a 4.774 4.774 0 0 0 1.525 -2.037
                arcToRelative(
                    a = 4.774f,
                    b = 4.774f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.525f,
                    dy1 = -2.037f,
                )
                // a 0.384 0.384 0 0 0 -0.37 -0.516
                arcToRelative(
                    a = 0.384f,
                    b = 0.384f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.37f,
                    dy1 = -0.516f,
                )
                // h 0.001z
                horizontalLineToRelative(dx = 0.001f)
                close()
            }
        }.build().also { _ic151Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic151Fill: ImageVector? = null
