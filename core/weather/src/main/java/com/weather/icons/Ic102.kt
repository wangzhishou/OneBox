package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic102: ImageVector
    get() {
        val current = _ic102
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic102",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M13.268 15.162 a.224 .224 0 0 0 -.233 .042 A2.99 2.99 0 0 1 11 16 a2.99 2.99 0 0 1 -2.035 -.796 .224 .224 0 0 0 -.233 -.042 2 2 0 1 1 -.383 -3.832 .224 .224 0 0 0 .22 -.087 A2.996 2.996 0 0 1 11 10 c1 0 1.887 .49 2.432 1.243 .05 .069 .136 .102 .22 .087 a2 2 0 1 1 -.383 3.832Z M11 15.25 c.752 0 1.418 -.37 1.827 -.936 .086 -.12 .273 -.134 .388 -.041 a1.25 1.25 0 1 0 .209 -2.082 c-.132 .068 -.312 .017 -.373 -.118 a2.25 2.25 0 0 0 -4.102 0 c-.06 .135 -.241 .186 -.372 .118 a1.25 1.25 0 1 0 .209 2.082 c.114 -.093 .301 -.079 .387 .04 A2.247 2.247 0 0 0 11 15.25Z M7.655 2.357 a.5 .5 0 0 0 .854 -.353 v-1.5 a.5 .5 0 1 0 -1 0 v1.5 a.5 .5 0 0 0 .146 .353Z m-4.08 1.861 c.06 .026 .126 .039 .191 .039 l.001 -.001 a.5 .5 0 0 0 .355 -.855 l-1.064 -1.06 a.5 .5 0 0 0 -.707 .708 l1.062 1.06 a.498 .498 0 0 0 .162 .11Z M.503 8.496 h1.5 a.5 .5 0 1 0 0 -1 h-1.5 a.5 .5 0 0 0 0 1Z m1.914 5.221 a.501 .501 0 0 0 .631 -.063 l1.063 -1.06 a.5 .5 0 0 0 -.708 -.707 l-1.062 1.06 a.5 .5 0 0 0 .076 .77Z M12.393 9 a4.5 4.5 0 1 0 -7.033 2.64 l.718 -.718 A3.501 3.501 0 0 1 4.505 8 a3.504 3.504 0 0 1 3.5 -3.5 A3.5 3.5 0 0 1 11.359 9 h1.034Z m1.609 -.49 h1.5 a.5 .5 0 1 0 0 -1 h-1.5 a.5 .5 0 1 0 0 1Z m-2.031 -4.327 a.5 .5 0 0 0 .633 -.063 l1.06 -1.06 a.5 .5 0 1 0 -.708 -.708 l-1.06 1.06 a.5 .5 0 0 0 .075 .77Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 13.268 15.162
                moveTo(x = 13.268f, y = 15.162f)
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
                // A 2.99 2.99 0 0 1 11 16
                arcTo(
                    horizontalEllipseRadius = 2.99f,
                    verticalEllipseRadius = 2.99f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 11.0f,
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
                // A 2.996 2.996 0 0 1 11 10
                arcTo(
                    horizontalEllipseRadius = 2.996f,
                    verticalEllipseRadius = 2.996f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 11.0f,
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
                // M 11 15.25
                moveTo(x = 11.0f, y = 15.25f)
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
                // A 2.247 2.247 0 0 0 11 15.25z
                arcTo(
                    horizontalEllipseRadius = 2.247f,
                    verticalEllipseRadius = 2.247f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 11.0f,
                    y1 = 15.25f,
                )
                close()
                // M 7.655 2.357
                moveTo(x = 7.655f, y = 2.357f)
                // a 0.5 0.5 0 0 0 0.854 -0.353
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.854f,
                    dy1 = -0.353f,
                )
                // v -1.5
                verticalLineToRelative(dy = -1.5f)
                // a 0.5 0.5 0 1 0 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // v 1.5
                verticalLineToRelative(dy = 1.5f)
                // a 0.5 0.5 0 0 0 0.146 0.353z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.146f,
                    dy1 = 0.353f,
                )
                close()
                // m -4.08 1.861
                moveToRelative(dx = -4.08f, dy = 1.861f)
                // c 0.06 0.026 0.126 0.039 0.191 0.039
                curveToRelative(
                    dx1 = 0.06f,
                    dy1 = 0.026f,
                    dx2 = 0.126f,
                    dy2 = 0.039f,
                    dx3 = 0.191f,
                    dy3 = 0.039f,
                )
                // l 0.001 -0.001
                lineToRelative(dx = 0.001f, dy = -0.001f)
                // a 0.5 0.5 0 0 0 0.355 -0.855
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.355f,
                    dy1 = -0.855f,
                )
                // l -1.064 -1.06
                lineToRelative(dx = -1.064f, dy = -1.06f)
                // a 0.5 0.5 0 0 0 -0.707 0.708
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.707f,
                    dy1 = 0.708f,
                )
                // l 1.062 1.06
                lineToRelative(dx = 1.062f, dy = 1.06f)
                // a 0.498 0.498 0 0 0 0.162 0.11z
                arcToRelative(
                    a = 0.498f,
                    b = 0.498f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.162f,
                    dy1 = 0.11f,
                )
                close()
                // M 0.503 8.496
                moveTo(x = 0.503f, y = 8.496f)
                // h 1.5
                horizontalLineToRelative(dx = 1.5f)
                // a 0.5 0.5 0 1 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h -1.5
                horizontalLineToRelative(dx = -1.5f)
                // a 0.5 0.5 0 0 0 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
                // m 1.914 5.221
                moveToRelative(dx = 1.914f, dy = 5.221f)
                // a 0.501 0.501 0 0 0 0.631 -0.063
                arcToRelative(
                    a = 0.501f,
                    b = 0.501f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.631f,
                    dy1 = -0.063f,
                )
                // l 1.063 -1.06
                lineToRelative(dx = 1.063f, dy = -1.06f)
                // a 0.5 0.5 0 0 0 -0.708 -0.707
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.708f,
                    dy1 = -0.707f,
                )
                // l -1.062 1.06
                lineToRelative(dx = -1.062f, dy = 1.06f)
                // a 0.5 0.5 0 0 0 0.076 0.77z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.076f,
                    dy1 = 0.77f,
                )
                close()
                // M 12.393 9
                moveTo(x = 12.393f, y = 9.0f)
                // a 4.5 4.5 0 1 0 -7.033 2.64
                arcToRelative(
                    a = 4.5f,
                    b = 4.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -7.033f,
                    dy1 = 2.64f,
                )
                // l 0.718 -0.718
                lineToRelative(dx = 0.718f, dy = -0.718f)
                // A 3.501 3.501 0 0 1 4.505 8
                arcTo(
                    horizontalEllipseRadius = 3.501f,
                    verticalEllipseRadius = 3.501f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 4.505f,
                    y1 = 8.0f,
                )
                // a 3.504 3.504 0 0 1 3.5 -3.5
                arcToRelative(
                    a = 3.504f,
                    b = 3.504f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 3.5f,
                    dy1 = -3.5f,
                )
                // A 3.5 3.5 0 0 1 11.359 9
                arcTo(
                    horizontalEllipseRadius = 3.5f,
                    verticalEllipseRadius = 3.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 11.359f,
                    y1 = 9.0f,
                )
                // h 1.034z
                horizontalLineToRelative(dx = 1.034f)
                close()
                // m 1.609 -0.49
                moveToRelative(dx = 1.609f, dy = -0.49f)
                // h 1.5
                horizontalLineToRelative(dx = 1.5f)
                // a 0.5 0.5 0 1 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h -1.5
                horizontalLineToRelative(dx = -1.5f)
                // a 0.5 0.5 0 1 0 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
                // m -2.031 -4.327
                moveToRelative(dx = -2.031f, dy = -4.327f)
                // a 0.5 0.5 0 0 0 0.633 -0.063
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.633f,
                    dy1 = -0.063f,
                )
                // l 1.06 -1.06
                lineToRelative(dx = 1.06f, dy = -1.06f)
                // a 0.5 0.5 0 1 0 -0.708 -0.708
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.708f,
                    dy1 = -0.708f,
                )
                // l -1.06 1.06
                lineToRelative(dx = -1.06f, dy = 1.06f)
                // a 0.5 0.5 0 0 0 0.075 0.77z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.075f,
                    dy1 = 0.77f,
                )
                close()
            }
        }.build().also { _ic102 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic102: ImageVector? = null
