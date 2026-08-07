package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic102Fill: ImageVector
    get() {
        val current = _ic102Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic102Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M13.268 15.162 a.224 .224 0 0 0 -.233 .042 A2.99 2.99 0 0 1 11 16 a2.99 2.99 0 0 1 -2.035 -.796 .224 .224 0 0 0 -.233 -.042 2 2 0 1 1 -.383 -3.832 .224 .224 0 0 0 .22 -.087 A2.996 2.996 0 0 1 11 10 c1 0 1.887 .49 2.432 1.243 .05 .069 .136 .102 .22 .087 a2 2 0 1 1 -.383 3.832Z M11 15.25 c.752 0 1.418 -.37 1.827 -.936 .086 -.12 .273 -.134 .388 -.041 a1.25 1.25 0 1 0 .209 -2.082 c-.132 .068 -.312 .017 -.373 -.118 a2.25 2.25 0 0 0 -4.102 0 c-.06 .135 -.241 .186 -.372 .118 a1.25 1.25 0 1 0 .209 2.082 c.114 -.093 .301 -.079 .387 .04 A2.247 2.247 0 0 0 11 15.25Z m-7.6 -3.373 .002 -.002 a.5 .5 0 1 1 .707 .707 l-1.065 1.064 a.5 .5 0 1 1 -.707 -.707 l.003 -.003 h.001 l1.057 -1.059 H3.4Z m4.957 -9.523 A.5 .5 0 0 1 7.503 2 V.5 a.5 .5 0 0 1 1 0 V2 a.5 .5 0 0 1 -.146 .354Z M3.76 4.245 a.5 .5 0 0 1 -.352 -.14 L2.347 3.044 a.5 .5 0 0 1 .708 -.706 l1.06 1.06 a.5 .5 0 0 1 -.355 .847Z M2.354 7.636 A.5 .5 0 0 1 2 8.49 H.5 a.5 .5 0 0 1 0 -1 H2 a.5 .5 0 0 1 .354 .146Z m11.286 .721 a.5 .5 0 0 1 .353 -.854 h1.5 a.5 .5 0 0 1 0 1 h-1.5 a.5 .5 0 0 1 -.354 -.146Z m-1.207 -4.134 a.498 .498 0 0 1 -.47 -.046 .5 .5 0 0 1 -.075 -.77 l1.061 -1.06 a.5 .5 0 0 1 .706 .707 l-1.06 1.061 a.498 .498 0 0 1 -.162 .108Z
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
                // m -7.6 -3.373
                moveToRelative(dx = -7.6f, dy = -3.373f)
                // l 0.002 -0.002
                lineToRelative(dx = 0.002f, dy = -0.002f)
                // a 0.5 0.5 0 1 1 0.707 0.707
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.707f,
                    dy1 = 0.707f,
                )
                // l -1.065 1.064
                lineToRelative(dx = -1.065f, dy = 1.064f)
                // a 0.5 0.5 0 1 1 -0.707 -0.707
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.707f,
                    dy1 = -0.707f,
                )
                // l 0.003 -0.003
                lineToRelative(dx = 0.003f, dy = -0.003f)
                // h 0.001
                horizontalLineToRelative(dx = 0.001f)
                // l 1.057 -1.059
                lineToRelative(dx = 1.057f, dy = -1.059f)
                // H 3.4z
                horizontalLineTo(x = 3.4f)
                close()
                // m 4.957 -9.523
                moveToRelative(dx = 4.957f, dy = -9.523f)
                // A 0.5 0.5 0 0 1 7.503 2
                arcTo(
                    horizontalEllipseRadius = 0.5f,
                    verticalEllipseRadius = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.503f,
                    y1 = 2.0f,
                )
                // V 0.5
                verticalLineTo(y = 0.5f)
                // a 0.5 0.5 0 0 1 1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // V 2
                verticalLineTo(y = 2.0f)
                // a 0.5 0.5 0 0 1 -0.146 0.354z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.146f,
                    dy1 = 0.354f,
                )
                close()
                // M 3.76 4.245
                moveTo(x = 3.76f, y = 4.245f)
                // a 0.5 0.5 0 0 1 -0.352 -0.14
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.352f,
                    dy1 = -0.14f,
                )
                // L 2.347 3.044
                lineTo(x = 2.347f, y = 3.044f)
                // a 0.5 0.5 0 0 1 0.708 -0.706
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.708f,
                    dy1 = -0.706f,
                )
                // l 1.06 1.06
                lineToRelative(dx = 1.06f, dy = 1.06f)
                // a 0.5 0.5 0 0 1 -0.355 0.847z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.355f,
                    dy1 = 0.847f,
                )
                close()
                // M 2.354 7.636
                moveTo(x = 2.354f, y = 7.636f)
                // A 0.5 0.5 0 0 1 2 8.49
                arcTo(
                    horizontalEllipseRadius = 0.5f,
                    verticalEllipseRadius = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 2.0f,
                    y1 = 8.49f,
                )
                // H 0.5
                horizontalLineTo(x = 0.5f)
                // a 0.5 0.5 0 0 1 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // H 2
                horizontalLineTo(x = 2.0f)
                // a 0.5 0.5 0 0 1 0.354 0.146z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.354f,
                    dy1 = 0.146f,
                )
                close()
                // m 11.286 0.721
                moveToRelative(dx = 11.286f, dy = 0.721f)
                // a 0.5 0.5 0 0 1 0.353 -0.854
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.353f,
                    dy1 = -0.854f,
                )
                // h 1.5
                horizontalLineToRelative(dx = 1.5f)
                // a 0.5 0.5 0 0 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // h -1.5
                horizontalLineToRelative(dx = -1.5f)
                // a 0.5 0.5 0 0 1 -0.354 -0.146z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.354f,
                    dy1 = -0.146f,
                )
                close()
                // m -1.207 -4.134
                moveToRelative(dx = -1.207f, dy = -4.134f)
                // a 0.498 0.498 0 0 1 -0.47 -0.046
                arcToRelative(
                    a = 0.498f,
                    b = 0.498f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.47f,
                    dy1 = -0.046f,
                )
                // a 0.5 0.5 0 0 1 -0.075 -0.77
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.075f,
                    dy1 = -0.77f,
                )
                // l 1.061 -1.06
                lineToRelative(dx = 1.061f, dy = -1.06f)
                // a 0.5 0.5 0 0 1 0.706 0.707
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.706f,
                    dy1 = 0.707f,
                )
                // l -1.06 1.061
                lineToRelative(dx = -1.06f, dy = 1.061f)
                // a 0.498 0.498 0 0 1 -0.162 0.108z
                arcToRelative(
                    a = 0.498f,
                    b = 0.498f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.162f,
                    dy1 = 0.108f,
                )
                close()
            }
            // M9.975 9.166 a3.846 3.846 0 0 0 -1.961 1.244 v.001 a2.923 2.923 0 0 0 -2.381 1.397 4.5 4.5 0 1 1 6.868 -3.812 4.453 4.453 0 0 1 -.207 1.282 3.846 3.846 0 0 0 -2.32 -.112Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 9.975 9.166
                moveTo(x = 9.975f, y = 9.166f)
                // a 3.846 3.846 0 0 0 -1.961 1.244
                arcToRelative(
                    a = 3.846f,
                    b = 3.846f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.961f,
                    dy1 = 1.244f,
                )
                // v 0.001
                verticalLineToRelative(dy = 0.001f)
                // a 2.923 2.923 0 0 0 -2.381 1.397
                arcToRelative(
                    a = 2.923f,
                    b = 2.923f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.381f,
                    dy1 = 1.397f,
                )
                // a 4.5 4.5 0 1 1 6.868 -3.812
                arcToRelative(
                    a = 4.5f,
                    b = 4.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 6.868f,
                    dy1 = -3.812f,
                )
                // a 4.453 4.453 0 0 1 -0.207 1.282
                arcToRelative(
                    a = 4.453f,
                    b = 4.453f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.207f,
                    dy1 = 1.282f,
                )
                // a 3.846 3.846 0 0 0 -2.32 -0.112z
                arcToRelative(
                    a = 3.846f,
                    b = 3.846f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.32f,
                    dy1 = -0.112f,
                )
                close()
            }
        }.build().also { _ic102Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic102Fill: ImageVector? = null
