package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic152Fill: ImageVector
    get() {
        val current = _ic152Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic152Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.268 15.162 a.224 .224 0 0 0 -.233 .042 A2.99 2.99 0 0 1 5 16 a2.99 2.99 0 0 1 -2.035 -.796 .224 .224 0 0 0 -.233 -.042 2 2 0 1 1 -.383 -3.832 .224 .224 0 0 0 .22 -.087 A2.996 2.996 0 0 1 5 10 c1 0 1.887 .49 2.432 1.243 .05 .069 .136 .102 .22 .087 a2 2 0 1 1 -.383 3.832Z M5 15.25 c.752 0 1.418 -.37 1.827 -.936 .086 -.12 .273 -.134 .388 -.041 a1.25 1.25 0 1 0 .209 -2.082 c-.132 .068 -.312 .017 -.373 -.118 a2.25 2.25 0 0 0 -4.102 0 c-.06 .135 -.241 .186 -.372 .118 a1.25 1.25 0 1 0 .209 2.082 c.114 -.093 .301 -.079 .387 .04 A2.247 2.247 0 0 0 5 15.25Z M8.158 .593 c.085 -.37 -.263 -.718 -.608 -.549 a6.404 6.404 0 0 0 -1.53 1.051 6.242 6.242 0 0 0 -.98 8.045 3.144 3.144 0 0 1 3.539 1.255 2.16 2.16 0 0 1 .265 -.026 2.09 2.09 0 0 1 2.035 1.614 6.42 6.42 0 0 0 5.064 -3.132 c.193 -.328 -.137 -.693 -.518 -.633 -2.028 .323 -4.662 -.256 -6.175 -1.843 S7.704 2.568 8.158 .593Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.268 15.162
                moveTo(x = 7.268f, y = 15.162f)
                // a 0.224 0.224 0 0 0 -0.233 0.042
                arcToRelative(
                    a = 0.224f,
                    b = 0.224f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.233f,
                    dy1 = 0.042f,
                )
                // A 2.99 2.99 0 0 1 5 16
                arcTo(
                    horizontalEllipseRadius = 2.99f,
                    verticalEllipseRadius = 2.99f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 5.0f,
                    y1 = 16.0f,
                )
                // a 2.99 2.99 0 0 1 -2.035 -0.796
                arcToRelative(
                    a = 2.99f,
                    b = 2.99f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.035f,
                    dy1 = -0.796f,
                )
                // a 0.224 0.224 0 0 0 -0.233 -0.042
                arcToRelative(
                    a = 0.224f,
                    b = 0.224f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.233f,
                    dy1 = -0.042f,
                )
                // a 2 2 0 1 1 -0.383 -3.832
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.383f,
                    dy1 = -3.832f,
                )
                // a 0.224 0.224 0 0 0 0.22 -0.087
                arcToRelative(
                    a = 0.224f,
                    b = 0.224f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.22f,
                    dy1 = -0.087f,
                )
                // A 2.996 2.996 0 0 1 5 10
                arcTo(
                    horizontalEllipseRadius = 2.996f,
                    verticalEllipseRadius = 2.996f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 5.0f,
                    y1 = 10.0f,
                )
                // c 1 0 1.887 0.49 2.432 1.243
                curveToRelative(
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                    dx2 = 1.887f,
                    dy2 = 0.49f,
                    dx3 = 2.432f,
                    dy3 = 1.243f,
                )
                // c 0.05 0.069 0.136 0.102 0.22 0.087
                curveToRelative(
                    dx1 = 0.05f,
                    dy1 = 0.069f,
                    dx2 = 0.136f,
                    dy2 = 0.102f,
                    dx3 = 0.22f,
                    dy3 = 0.087f,
                )
                // a 2 2 0 1 1 -0.383 3.832z
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.383f,
                    dy1 = 3.832f,
                )
                close()
                // M 5 15.25
                moveTo(x = 5.0f, y = 15.25f)
                // c 0.752 0 1.418 -0.37 1.827 -0.936
                curveToRelative(
                    dx1 = 0.752f,
                    dy1 = 0.0f,
                    dx2 = 1.418f,
                    dy2 = -0.37f,
                    dx3 = 1.827f,
                    dy3 = -0.936f,
                )
                // c 0.086 -0.12 0.273 -0.134 0.388 -0.041
                curveToRelative(
                    dx1 = 0.086f,
                    dy1 = -0.12f,
                    dx2 = 0.273f,
                    dy2 = -0.134f,
                    dx3 = 0.388f,
                    dy3 = -0.041f,
                )
                // a 1.25 1.25 0 1 0 0.209 -2.082
                arcToRelative(
                    a = 1.25f,
                    b = 1.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.209f,
                    dy1 = -2.082f,
                )
                // c -0.132 0.068 -0.312 0.017 -0.373 -0.118
                curveToRelative(
                    dx1 = -0.132f,
                    dy1 = 0.068f,
                    dx2 = -0.312f,
                    dy2 = 0.017f,
                    dx3 = -0.373f,
                    dy3 = -0.118f,
                )
                // a 2.25 2.25 0 0 0 -4.102 0
                arcToRelative(
                    a = 2.25f,
                    b = 2.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -4.102f,
                    dy1 = 0.0f,
                )
                // c -0.06 0.135 -0.241 0.186 -0.372 0.118
                curveToRelative(
                    dx1 = -0.06f,
                    dy1 = 0.135f,
                    dx2 = -0.241f,
                    dy2 = 0.186f,
                    dx3 = -0.372f,
                    dy3 = 0.118f,
                )
                // a 1.25 1.25 0 1 0 0.209 2.082
                arcToRelative(
                    a = 1.25f,
                    b = 1.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.209f,
                    dy1 = 2.082f,
                )
                // c 0.114 -0.093 0.301 -0.079 0.387 0.04
                curveToRelative(
                    dx1 = 0.114f,
                    dy1 = -0.093f,
                    dx2 = 0.301f,
                    dy2 = -0.079f,
                    dx3 = 0.387f,
                    dy3 = 0.04f,
                )
                // A 2.247 2.247 0 0 0 5 15.25z
                arcTo(
                    horizontalEllipseRadius = 2.247f,
                    verticalEllipseRadius = 2.247f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 5.0f,
                    y1 = 15.25f,
                )
                close()
                // M 8.158 0.593
                moveTo(x = 8.158f, y = 0.593f)
                // c 0.085 -0.37 -0.263 -0.718 -0.608 -0.549
                curveToRelative(
                    dx1 = 0.085f,
                    dy1 = -0.37f,
                    dx2 = -0.263f,
                    dy2 = -0.718f,
                    dx3 = -0.608f,
                    dy3 = -0.549f,
                )
                // a 6.404 6.404 0 0 0 -1.53 1.051
                arcToRelative(
                    a = 6.404f,
                    b = 6.404f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.53f,
                    dy1 = 1.051f,
                )
                // a 6.242 6.242 0 0 0 -0.98 8.045
                arcToRelative(
                    a = 6.242f,
                    b = 6.242f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.98f,
                    dy1 = 8.045f,
                )
                // a 3.144 3.144 0 0 1 3.539 1.255
                arcToRelative(
                    a = 3.144f,
                    b = 3.144f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 3.539f,
                    dy1 = 1.255f,
                )
                // a 2.16 2.16 0 0 1 0.265 -0.026
                arcToRelative(
                    a = 2.16f,
                    b = 2.16f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.265f,
                    dy1 = -0.026f,
                )
                // a 2.09 2.09 0 0 1 2.035 1.614
                arcToRelative(
                    a = 2.09f,
                    b = 2.09f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.035f,
                    dy1 = 1.614f,
                )
                // a 6.42 6.42 0 0 0 5.064 -3.132
                arcToRelative(
                    a = 6.42f,
                    b = 6.42f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 5.064f,
                    dy1 = -3.132f,
                )
                // c 0.193 -0.328 -0.137 -0.693 -0.518 -0.633
                curveToRelative(
                    dx1 = 0.193f,
                    dy1 = -0.328f,
                    dx2 = -0.137f,
                    dy2 = -0.693f,
                    dx3 = -0.518f,
                    dy3 = -0.633f,
                )
                // c -2.028 0.323 -4.662 -0.256 -6.175 -1.843
                curveToRelative(
                    dx1 = -2.028f,
                    dy1 = 0.323f,
                    dx2 = -4.662f,
                    dy2 = -0.256f,
                    dx3 = -6.175f,
                    dy3 = -1.843f,
                )
                // S 7.704 2.568 8.158 0.593z
                reflectiveCurveTo(
                    x1 = 7.704f,
                    y1 = 2.568f,
                    x2 = 8.158f,
                    y2 = 0.593f,
                )
                close()
            }
        }.build().also { _ic152Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic152Fill: ImageVector? = null
