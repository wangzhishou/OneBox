package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic152: ImageVector
    get() {
        val current = _ic152
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic152",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.268 15.162 a.224 .224 0 0 0 -.233 .042 A2.99 2.99 0 0 1 5 16 a2.99 2.99 0 0 1 -2.035 -.796 .224 .224 0 0 0 -.233 -.042 2 2 0 1 1 -.383 -3.832 .224 .224 0 0 0 .22 -.087 A2.996 2.996 0 0 1 5 10 c1 0 1.887 .49 2.432 1.243 .05 .069 .136 .102 .22 .087 a2 2 0 1 1 -.383 3.832Z M5 15.25 c.752 0 1.418 -.37 1.827 -.936 .086 -.12 .273 -.134 .388 -.041 a1.25 1.25 0 1 0 .209 -2.082 c-.132 .068 -.312 .017 -.373 -.118 a2.25 2.25 0 0 0 -4.102 0 c-.06 .135 -.241 .186 -.372 .118 a1.25 1.25 0 1 0 .209 2.082 c.114 -.093 .301 -.079 .387 .04 A2.247 2.247 0 0 0 5 15.25Z m3.526 -8.185 C7.013 5.477 6.755 3.428 6.97 1.594 a5.241 5.241 0 0 0 -.787 7.413 3.142 3.142 0 0 0 -1.144 .133 6.242 6.242 0 0 1 .98 -8.045 A6.404 6.404 0 0 1 7.55 .044 c.346 -.17 .694 .179 .61 .549 -.455 1.975 -.422 4.195 1.091 5.782 1.513 1.587 4.147 2.166 6.175 1.843 .38 -.06 .711 .305 .518 .633 a6.42 6.42 0 0 1 -5.064 3.131 2.09 2.09 0 0 0 -.54 -.982 5.435 5.435 0 0 0 4.02 -1.697 c-2.036 .013 -4.347 -.678 -5.834 -2.238Z
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
                // m 3.526 -8.185
                moveToRelative(dx = 3.526f, dy = -8.185f)
                // C 7.013 5.477 6.755 3.428 6.97 1.594
                curveTo(
                    x1 = 7.013f,
                    y1 = 5.477f,
                    x2 = 6.755f,
                    y2 = 3.428f,
                    x3 = 6.97f,
                    y3 = 1.594f,
                )
                // a 5.241 5.241 0 0 0 -0.787 7.413
                arcToRelative(
                    a = 5.241f,
                    b = 5.241f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.787f,
                    dy1 = 7.413f,
                )
                // a 3.142 3.142 0 0 0 -1.144 0.133
                arcToRelative(
                    a = 3.142f,
                    b = 3.142f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.144f,
                    dy1 = 0.133f,
                )
                // a 6.242 6.242 0 0 1 0.98 -8.045
                arcToRelative(
                    a = 6.242f,
                    b = 6.242f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.98f,
                    dy1 = -8.045f,
                )
                // A 6.404 6.404 0 0 1 7.55 0.044
                arcTo(
                    horizontalEllipseRadius = 6.404f,
                    verticalEllipseRadius = 6.404f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.55f,
                    y1 = 0.044f,
                )
                // c 0.346 -0.17 0.694 0.179 0.61 0.549
                curveToRelative(
                    dx1 = 0.346f,
                    dy1 = -0.17f,
                    dx2 = 0.694f,
                    dy2 = 0.179f,
                    dx3 = 0.61f,
                    dy3 = 0.549f,
                )
                // c -0.455 1.975 -0.422 4.195 1.091 5.782
                curveToRelative(
                    dx1 = -0.455f,
                    dy1 = 1.975f,
                    dx2 = -0.422f,
                    dy2 = 4.195f,
                    dx3 = 1.091f,
                    dy3 = 5.782f,
                )
                // c 1.513 1.587 4.147 2.166 6.175 1.843
                curveToRelative(
                    dx1 = 1.513f,
                    dy1 = 1.587f,
                    dx2 = 4.147f,
                    dy2 = 2.166f,
                    dx3 = 6.175f,
                    dy3 = 1.843f,
                )
                // c 0.38 -0.06 0.711 0.305 0.518 0.633
                curveToRelative(
                    dx1 = 0.38f,
                    dy1 = -0.06f,
                    dx2 = 0.711f,
                    dy2 = 0.305f,
                    dx3 = 0.518f,
                    dy3 = 0.633f,
                )
                // a 6.42 6.42 0 0 1 -5.064 3.131
                arcToRelative(
                    a = 6.42f,
                    b = 6.42f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -5.064f,
                    dy1 = 3.131f,
                )
                // a 2.09 2.09 0 0 0 -0.54 -0.982
                arcToRelative(
                    a = 2.09f,
                    b = 2.09f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.54f,
                    dy1 = -0.982f,
                )
                // a 5.435 5.435 0 0 0 4.02 -1.697
                arcToRelative(
                    a = 5.435f,
                    b = 5.435f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 4.02f,
                    dy1 = -1.697f,
                )
                // c -2.036 0.013 -4.347 -0.678 -5.834 -2.238z
                curveToRelative(
                    dx1 = -2.036f,
                    dy1 = 0.013f,
                    dx2 = -4.347f,
                    dy2 = -0.678f,
                    dx3 = -5.834f,
                    dy3 = -2.238f,
                )
                close()
            }
        }.build().also { _ic152 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic152: ImageVector? = null
