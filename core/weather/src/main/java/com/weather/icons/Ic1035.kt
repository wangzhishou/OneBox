package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1035: ImageVector
    get() {
        val current = _ic1035
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1035",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.293 15.707 A1 1 0 0 1 8 15 c0 -.5 .555 -1.395 1 -2 .445 .605 1 1.5 1 2 a1 1 0 0 1 -1.707 .707Z m-6 -6 A1 1 0 0 1 2 9 c0 -.5 .555 -1.395 1 -2 .445 .605 1 1.5 1 2 a1 1 0 0 1 -1.707 .707Z m10 6 A1 1 0 0 1 12 15 c0 -.5 .555 -1.395 1 -2 .445 .605 1 1.5 1 2 a1 1 0 0 1 -1.707 .707Z M7.268 5.162 a.224 .224 0 0 0 -.233 .042 A2.99 2.99 0 0 1 5 6 a2.99 2.99 0 0 1 -2.035 -.796 .224 .224 0 0 0 -.233 -.042 2 2 0 1 1 -.383 -3.832 .224 .224 0 0 0 .22 -.087 A2.996 2.996 0 0 1 5 0 c1 0 1.887 .49 2.432 1.243 .05 .069 .136 .102 .22 .087 a2 2 0 1 1 -.383 3.832Z M5 5.25 c.752 0 1.418 -.37 1.827 -.936 .086 -.12 .273 -.134 .388 -.041 a1.25 1.25 0 1 0 .209 -2.082 c-.132 .068 -.312 .017 -.373 -.118 a2.25 2.25 0 0 0 -4.102 0 c-.06 .135 -.241 .186 -.372 .118 a1.25 1.25 0 1 0 .209 2.082 c.114 -.093 .301 -.079 .387 .04 A2.247 2.247 0 0 0 5 5.25Z m8.137 5.855 A2.99 2.99 0 0 1 11 12 a2.99 2.99 0 0 1 -2.138 -.895 2 2 0 1 1 -.375 -3.745 A2.997 2.997 0 0 1 11 6 c1.052 0 1.977 .541 2.512 1.36 A2.004 2.004 0 0 1 16 9.3 a2 2 0 0 1 -2.863 1.805Z m-2.85 -3.224 .276 1.759 h.863 l.286 -1.822 a.677 .677 0 0 0 -.327 -.682 l-.056 -.034 a.607 .607 0 0 0 -.645 -.018 c-.26 .157 -.442 .508 -.397 .797Z m1.193 2.639 a.48 .48 0 1 0 -.96 0 .48 .48 0 0 0 .96 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.293 15.707
                moveTo(x = 8.293f, y = 15.707f)
                // A 1 1 0 0 1 8 15
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 8.0f,
                    y1 = 15.0f,
                )
                // c 0 -0.5 0.555 -1.395 1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = 0.555f,
                    dy2 = -1.395f,
                    dx3 = 1.0f,
                    dy3 = -2.0f,
                )
                // c 0.445 0.605 1 1.5 1 2
                curveToRelative(
                    dx1 = 0.445f,
                    dy1 = 0.605f,
                    dx2 = 1.0f,
                    dy2 = 1.5f,
                    dx3 = 1.0f,
                    dy3 = 2.0f,
                )
                // a 1 1 0 0 1 -1.707 0.707z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.707f,
                    dy1 = 0.707f,
                )
                close()
                // m -6 -6
                moveToRelative(dx = -6.0f, dy = -6.0f)
                // A 1 1 0 0 1 2 9
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 2.0f,
                    y1 = 9.0f,
                )
                // c 0 -0.5 0.555 -1.395 1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = 0.555f,
                    dy2 = -1.395f,
                    dx3 = 1.0f,
                    dy3 = -2.0f,
                )
                // c 0.445 0.605 1 1.5 1 2
                curveToRelative(
                    dx1 = 0.445f,
                    dy1 = 0.605f,
                    dx2 = 1.0f,
                    dy2 = 1.5f,
                    dx3 = 1.0f,
                    dy3 = 2.0f,
                )
                // a 1 1 0 0 1 -1.707 0.707z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.707f,
                    dy1 = 0.707f,
                )
                close()
                // m 10 6
                moveToRelative(dx = 10.0f, dy = 6.0f)
                // A 1 1 0 0 1 12 15
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 12.0f,
                    y1 = 15.0f,
                )
                // c 0 -0.5 0.555 -1.395 1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = 0.555f,
                    dy2 = -1.395f,
                    dx3 = 1.0f,
                    dy3 = -2.0f,
                )
                // c 0.445 0.605 1 1.5 1 2
                curveToRelative(
                    dx1 = 0.445f,
                    dy1 = 0.605f,
                    dx2 = 1.0f,
                    dy2 = 1.5f,
                    dx3 = 1.0f,
                    dy3 = 2.0f,
                )
                // a 1 1 0 0 1 -1.707 0.707z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.707f,
                    dy1 = 0.707f,
                )
                close()
                // M 7.268 5.162
                moveTo(x = 7.268f, y = 5.162f)
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
                // A 2.99 2.99 0 0 1 5 6
                arcTo(
                    horizontalEllipseRadius = 2.99f,
                    verticalEllipseRadius = 2.99f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 5.0f,
                    y1 = 6.0f,
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
                // A 2.996 2.996 0 0 1 5 0
                arcTo(
                    horizontalEllipseRadius = 2.996f,
                    verticalEllipseRadius = 2.996f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 5.0f,
                    y1 = 0.0f,
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
                // M 5 5.25
                moveTo(x = 5.0f, y = 5.25f)
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
                // A 2.247 2.247 0 0 0 5 5.25z
                arcTo(
                    horizontalEllipseRadius = 2.247f,
                    verticalEllipseRadius = 2.247f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 5.0f,
                    y1 = 5.25f,
                )
                close()
                // m 8.137 5.855
                moveToRelative(dx = 8.137f, dy = 5.855f)
                // A 2.99 2.99 0 0 1 11 12
                arcTo(
                    horizontalEllipseRadius = 2.99f,
                    verticalEllipseRadius = 2.99f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 11.0f,
                    y1 = 12.0f,
                )
                // a 2.99 2.99 0 0 1 -2.138 -0.895
                arcToRelative(
                    a = 2.99f,
                    b = 2.99f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.138f,
                    dy1 = -0.895f,
                )
                // a 2 2 0 1 1 -0.375 -3.745
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.375f,
                    dy1 = -3.745f,
                )
                // A 2.997 2.997 0 0 1 11 6
                arcTo(
                    horizontalEllipseRadius = 2.997f,
                    verticalEllipseRadius = 2.997f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 11.0f,
                    y1 = 6.0f,
                )
                // c 1.052 0 1.977 0.541 2.512 1.36
                curveToRelative(
                    dx1 = 1.052f,
                    dy1 = 0.0f,
                    dx2 = 1.977f,
                    dy2 = 0.541f,
                    dx3 = 2.512f,
                    dy3 = 1.36f,
                )
                // A 2.004 2.004 0 0 1 16 9.3
                arcTo(
                    horizontalEllipseRadius = 2.004f,
                    verticalEllipseRadius = 2.004f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 16.0f,
                    y1 = 9.3f,
                )
                // a 2 2 0 0 1 -2.863 1.805z
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.863f,
                    dy1 = 1.805f,
                )
                close()
                // m -2.85 -3.224
                moveToRelative(dx = -2.85f, dy = -3.224f)
                // l 0.276 1.759
                lineToRelative(dx = 0.276f, dy = 1.759f)
                // h 0.863
                horizontalLineToRelative(dx = 0.863f)
                // l 0.286 -1.822
                lineToRelative(dx = 0.286f, dy = -1.822f)
                // a 0.677 0.677 0 0 0 -0.327 -0.682
                arcToRelative(
                    a = 0.677f,
                    b = 0.677f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.327f,
                    dy1 = -0.682f,
                )
                // l -0.056 -0.034
                lineToRelative(dx = -0.056f, dy = -0.034f)
                // a 0.607 0.607 0 0 0 -0.645 -0.018
                arcToRelative(
                    a = 0.607f,
                    b = 0.607f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.645f,
                    dy1 = -0.018f,
                )
                // c -0.26 0.157 -0.442 0.508 -0.397 0.797z
                curveToRelative(
                    dx1 = -0.26f,
                    dy1 = 0.157f,
                    dx2 = -0.442f,
                    dy2 = 0.508f,
                    dx3 = -0.397f,
                    dy3 = 0.797f,
                )
                close()
                // m 1.193 2.639
                moveToRelative(dx = 1.193f, dy = 2.639f)
                // a 0.48 0.48 0 1 0 -0.96 0
                arcToRelative(
                    a = 0.48f,
                    b = 0.48f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.96f,
                    dy1 = 0.0f,
                )
                // a 0.48 0.48 0 0 0 0.96 0z
                arcToRelative(
                    a = 0.48f,
                    b = 0.48f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.96f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic1035 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1035: ImageVector? = null
