package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2030: ImageVector
    get() {
        val current = _ic2030
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2030",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M6.382 0 C7.59 .028 8.554 1 8.556 2.19 a.407 .407 0 0 1 -.41 .404 .407 .407 0 0 1 -.41 -.405 A1.417 1.417 0 0 0 6.369 .808 h-.04 A1.402 1.402 0 0 0 4.92 2.194 v8.452 l-.217 .186 a2.451 2.451 0 0 0 -.29 3.487 2.55 2.55 0 0 0 2.42 .808 2.463 2.463 0 0 0 1.591 -1.054 2.444 2.444 0 0 0 -.468 -3.233 l-.221 -.186 V7.56 c0 -.223 .183 -.405 .41 -.405 .226 0 .41 .181 .41 .405 v2.72 a3.245 3.245 0 0 1 .994 3.303 3.307 3.307 0 0 1 -2.557 2.352 A3.356 3.356 0 0 1 3.79 14.84 a3.249 3.249 0 0 1 .313 -4.56 V2.2 C4.104 .988 5.096 .006 6.325 0 h.057Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6.382 0
                moveTo(x = 6.382f, y = 0.0f)
                // C 7.59 0.028 8.554 1 8.556 2.19
                curveTo(
                    x1 = 7.59f,
                    y1 = 0.028f,
                    x2 = 8.554f,
                    y2 = 1.0f,
                    x3 = 8.556f,
                    y3 = 2.19f,
                )
                // a 0.407 0.407 0 0 1 -0.41 0.404
                arcToRelative(
                    a = 0.407f,
                    b = 0.407f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.41f,
                    dy1 = 0.404f,
                )
                // a 0.407 0.407 0 0 1 -0.41 -0.405
                arcToRelative(
                    a = 0.407f,
                    b = 0.407f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.41f,
                    dy1 = -0.405f,
                )
                // A 1.417 1.417 0 0 0 6.369 0.808
                arcTo(
                    horizontalEllipseRadius = 1.417f,
                    verticalEllipseRadius = 1.417f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 6.369f,
                    y1 = 0.808f,
                )
                // h -0.04
                horizontalLineToRelative(dx = -0.04f)
                // A 1.402 1.402 0 0 0 4.92 2.194
                arcTo(
                    horizontalEllipseRadius = 1.402f,
                    verticalEllipseRadius = 1.402f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 4.92f,
                    y1 = 2.194f,
                )
                // v 8.452
                verticalLineToRelative(dy = 8.452f)
                // l -0.217 0.186
                lineToRelative(dx = -0.217f, dy = 0.186f)
                // a 2.451 2.451 0 0 0 -0.29 3.487
                arcToRelative(
                    a = 2.451f,
                    b = 2.451f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.29f,
                    dy1 = 3.487f,
                )
                // a 2.55 2.55 0 0 0 2.42 0.808
                arcToRelative(
                    a = 2.55f,
                    b = 2.55f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.42f,
                    dy1 = 0.808f,
                )
                // a 2.463 2.463 0 0 0 1.591 -1.054
                arcToRelative(
                    a = 2.463f,
                    b = 2.463f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.591f,
                    dy1 = -1.054f,
                )
                // a 2.444 2.444 0 0 0 -0.468 -3.233
                arcToRelative(
                    a = 2.444f,
                    b = 2.444f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.468f,
                    dy1 = -3.233f,
                )
                // l -0.221 -0.186
                lineToRelative(dx = -0.221f, dy = -0.186f)
                // V 7.56
                verticalLineTo(y = 7.56f)
                // c 0 -0.223 0.183 -0.405 0.41 -0.405
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.223f,
                    dx2 = 0.183f,
                    dy2 = -0.405f,
                    dx3 = 0.41f,
                    dy3 = -0.405f,
                )
                // c 0.226 0 0.41 0.181 0.41 0.405
                curveToRelative(
                    dx1 = 0.226f,
                    dy1 = 0.0f,
                    dx2 = 0.41f,
                    dy2 = 0.181f,
                    dx3 = 0.41f,
                    dy3 = 0.405f,
                )
                // v 2.72
                verticalLineToRelative(dy = 2.72f)
                // a 3.245 3.245 0 0 1 0.994 3.303
                arcToRelative(
                    a = 3.245f,
                    b = 3.245f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.994f,
                    dy1 = 3.303f,
                )
                // a 3.307 3.307 0 0 1 -2.557 2.352
                arcToRelative(
                    a = 3.307f,
                    b = 3.307f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.557f,
                    dy1 = 2.352f,
                )
                // A 3.356 3.356 0 0 1 3.79 14.84
                arcTo(
                    horizontalEllipseRadius = 3.356f,
                    verticalEllipseRadius = 3.356f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 3.79f,
                    y1 = 14.84f,
                )
                // a 3.249 3.249 0 0 1 0.313 -4.56
                arcToRelative(
                    a = 3.249f,
                    b = 3.249f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.313f,
                    dy1 = -4.56f,
                )
                // V 2.2
                verticalLineTo(y = 2.2f)
                // C 4.104 0.988 5.096 0.006 6.325 0
                curveTo(
                    x1 = 4.104f,
                    y1 = 0.988f,
                    x2 = 5.096f,
                    y2 = 0.006f,
                    x3 = 6.325f,
                    y3 = 0.0f,
                )
                // h 0.057z
                horizontalLineToRelative(dx = 0.057f)
                close()
            }
            // M6.333 11.586 c.614 0 1.112 .494 1.112 1.104 0 .609 -.498 1.103 -1.112 1.103 a1.108 1.108 0 0 1 -1.111 -1.103 c0 -.61 .498 -1.104 1.111 -1.104Z M10.5 2 c.276 0 .5 .206 .5 .46 V5 h2.54 c.254 0 .46 .224 .46 .5 s-.206 .5 -.46 .5 H11 v2.54 c0 .254 -.224 .46 -.5 .46 s-.5 -.206 -.5 -.46 V6 H7.46 C7.206 6 7 5.776 7 5.5 s.206 -.5 .46 -.5 H10 V2.46 c0 -.254 .224 -.46 .5 -.46Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6.333 11.586
                moveTo(x = 6.333f, y = 11.586f)
                // c 0.614 0 1.112 0.494 1.112 1.104
                curveToRelative(
                    dx1 = 0.614f,
                    dy1 = 0.0f,
                    dx2 = 1.112f,
                    dy2 = 0.494f,
                    dx3 = 1.112f,
                    dy3 = 1.104f,
                )
                // c 0 0.609 -0.498 1.103 -1.112 1.103
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.609f,
                    dx2 = -0.498f,
                    dy2 = 1.103f,
                    dx3 = -1.112f,
                    dy3 = 1.103f,
                )
                // a 1.108 1.108 0 0 1 -1.111 -1.103
                arcToRelative(
                    a = 1.108f,
                    b = 1.108f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.111f,
                    dy1 = -1.103f,
                )
                // c 0 -0.61 0.498 -1.104 1.111 -1.104z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.61f,
                    dx2 = 0.498f,
                    dy2 = -1.104f,
                    dx3 = 1.111f,
                    dy3 = -1.104f,
                )
                close()
                // M 10.5 2
                moveTo(x = 10.5f, y = 2.0f)
                // c 0.276 0 0.5 0.206 0.5 0.46
                curveToRelative(
                    dx1 = 0.276f,
                    dy1 = 0.0f,
                    dx2 = 0.5f,
                    dy2 = 0.206f,
                    dx3 = 0.5f,
                    dy3 = 0.46f,
                )
                // V 5
                verticalLineTo(y = 5.0f)
                // h 2.54
                horizontalLineToRelative(dx = 2.54f)
                // c 0.254 0 0.46 0.224 0.46 0.5
                curveToRelative(
                    dx1 = 0.254f,
                    dy1 = 0.0f,
                    dx2 = 0.46f,
                    dy2 = 0.224f,
                    dx3 = 0.46f,
                    dy3 = 0.5f,
                )
                // s -0.206 0.5 -0.46 0.5
                reflectiveCurveToRelative(
                    dx1 = -0.206f,
                    dy1 = 0.5f,
                    dx2 = -0.46f,
                    dy2 = 0.5f,
                )
                // H 11
                horizontalLineTo(x = 11.0f)
                // v 2.54
                verticalLineToRelative(dy = 2.54f)
                // c 0 0.254 -0.224 0.46 -0.5 0.46
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.254f,
                    dx2 = -0.224f,
                    dy2 = 0.46f,
                    dx3 = -0.5f,
                    dy3 = 0.46f,
                )
                // s -0.5 -0.206 -0.5 -0.46
                reflectiveCurveToRelative(
                    dx1 = -0.5f,
                    dy1 = -0.206f,
                    dx2 = -0.5f,
                    dy2 = -0.46f,
                )
                // V 6
                verticalLineTo(y = 6.0f)
                // H 7.46
                horizontalLineTo(x = 7.46f)
                // C 7.206 6 7 5.776 7 5.5
                curveTo(
                    x1 = 7.206f,
                    y1 = 6.0f,
                    x2 = 7.0f,
                    y2 = 5.776f,
                    x3 = 7.0f,
                    y3 = 5.5f,
                )
                // s 0.206 -0.5 0.46 -0.5
                reflectiveCurveToRelative(
                    dx1 = 0.206f,
                    dy1 = -0.5f,
                    dx2 = 0.46f,
                    dy2 = -0.5f,
                )
                // H 10
                horizontalLineTo(x = 10.0f)
                // V 2.46
                verticalLineTo(y = 2.46f)
                // c 0 -0.254 0.224 -0.46 0.5 -0.46z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.254f,
                    dx2 = 0.224f,
                    dy2 = -0.46f,
                    dx3 = 0.5f,
                    dy3 = -0.46f,
                )
                close()
            }
        }.build().also { _ic2030 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2030: ImageVector? = null
