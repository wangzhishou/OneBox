package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2387: ImageVector
    get() {
        val current = _ic2387
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2387",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M2.5 0 a.5 .5 0 0 0 -.5 .5 v15 a.5 .5 0 0 0 1 0 V9 l11 -4 L3 1 V.5 a.5 .5 0 0 0 -.5 -.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 2.5 0
                moveTo(x = 2.5f, y = 0.0f)
                // a 0.5 0.5 0 0 0 -0.5 0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.5f,
                    dy1 = 0.5f,
                )
                // v 15
                verticalLineToRelative(dy = 15.0f)
                // a 0.5 0.5 0 0 0 1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // V 9
                verticalLineTo(y = 9.0f)
                // l 11 -4
                lineToRelative(dx = 11.0f, dy = -4.0f)
                // L 3 1
                lineTo(x = 3.0f, y = 1.0f)
                // V 0.5
                verticalLineTo(y = 0.5f)
                // a 0.5 0.5 0 0 0 -0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                close()
            }
            // M5.8 10.1 a.5 .5 0 0 0 -.6 .8 c.523 .392 1.011 .673 1.55 .852 .537 .18 1.097 .248 1.75 .248 .75 0 1.29 -.306 1.722 -.551 l.024 -.014 c.447 -.253 .783 -.435 1.254 -.435 .582 0 1.028 .061 1.434 .197 .407 .135 .8 .354 1.266 .703 a.5 .5 0 0 0 .6 -.8 c-.523 -.392 -1.011 -.673 -1.55 -.852 -.537 -.18 -1.097 -.248 -1.75 -.248 -.75 0 -1.29 .306 -1.722 .551 l-.024 .014 C9.307 10.818 8.97 11 8.5 11 c-.582 0 -1.028 -.061 -1.434 -.196 -.407 -.136 -.8 -.355 -1.266 -.704Z m1 2.5 a.5 .5 0 1 0 -.6 .8 c.523 .392 1.011 .673 1.55 .852 .537 .18 1.097 .248 1.75 .248 .75 0 1.29 -.306 1.722 -.551 l.024 -.014 c.447 -.253 .783 -.435 1.254 -.435 .582 0 1.028 .061 1.434 .197 .407 .135 .8 .354 1.266 .703 a.5 .5 0 0 0 .6 -.8 c-.523 -.392 -1.011 -.673 -1.55 -.852 -.537 -.18 -1.097 -.248 -1.75 -.248 -.75 0 -1.29 .306 -1.722 .551 l-.024 .014 c-.447 .253 -.783 .435 -1.254 .435 -.582 0 -1.028 -.061 -1.434 -.196 -.407 -.136 -.8 -.355 -1.266 -.704Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5.8 10.1
                moveTo(x = 5.8f, y = 10.1f)
                // a 0.5 0.5 0 0 0 -0.6 0.8
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.6f,
                    dy1 = 0.8f,
                )
                // c 0.523 0.392 1.011 0.673 1.55 0.852
                curveToRelative(
                    dx1 = 0.523f,
                    dy1 = 0.392f,
                    dx2 = 1.011f,
                    dy2 = 0.673f,
                    dx3 = 1.55f,
                    dy3 = 0.852f,
                )
                // c 0.537 0.18 1.097 0.248 1.75 0.248
                curveToRelative(
                    dx1 = 0.537f,
                    dy1 = 0.18f,
                    dx2 = 1.097f,
                    dy2 = 0.248f,
                    dx3 = 1.75f,
                    dy3 = 0.248f,
                )
                // c 0.75 0 1.29 -0.306 1.722 -0.551
                curveToRelative(
                    dx1 = 0.75f,
                    dy1 = 0.0f,
                    dx2 = 1.29f,
                    dy2 = -0.306f,
                    dx3 = 1.722f,
                    dy3 = -0.551f,
                )
                // l 0.024 -0.014
                lineToRelative(dx = 0.024f, dy = -0.014f)
                // c 0.447 -0.253 0.783 -0.435 1.254 -0.435
                curveToRelative(
                    dx1 = 0.447f,
                    dy1 = -0.253f,
                    dx2 = 0.783f,
                    dy2 = -0.435f,
                    dx3 = 1.254f,
                    dy3 = -0.435f,
                )
                // c 0.582 0 1.028 0.061 1.434 0.197
                curveToRelative(
                    dx1 = 0.582f,
                    dy1 = 0.0f,
                    dx2 = 1.028f,
                    dy2 = 0.061f,
                    dx3 = 1.434f,
                    dy3 = 0.197f,
                )
                // c 0.407 0.135 0.8 0.354 1.266 0.703
                curveToRelative(
                    dx1 = 0.407f,
                    dy1 = 0.135f,
                    dx2 = 0.8f,
                    dy2 = 0.354f,
                    dx3 = 1.266f,
                    dy3 = 0.703f,
                )
                // a 0.5 0.5 0 0 0 0.6 -0.8
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.6f,
                    dy1 = -0.8f,
                )
                // c -0.523 -0.392 -1.011 -0.673 -1.55 -0.852
                curveToRelative(
                    dx1 = -0.523f,
                    dy1 = -0.392f,
                    dx2 = -1.011f,
                    dy2 = -0.673f,
                    dx3 = -1.55f,
                    dy3 = -0.852f,
                )
                // c -0.537 -0.18 -1.097 -0.248 -1.75 -0.248
                curveToRelative(
                    dx1 = -0.537f,
                    dy1 = -0.18f,
                    dx2 = -1.097f,
                    dy2 = -0.248f,
                    dx3 = -1.75f,
                    dy3 = -0.248f,
                )
                // c -0.75 0 -1.29 0.306 -1.722 0.551
                curveToRelative(
                    dx1 = -0.75f,
                    dy1 = 0.0f,
                    dx2 = -1.29f,
                    dy2 = 0.306f,
                    dx3 = -1.722f,
                    dy3 = 0.551f,
                )
                // l -0.024 0.014
                lineToRelative(dx = -0.024f, dy = 0.014f)
                // C 9.307 10.818 8.97 11 8.5 11
                curveTo(
                    x1 = 9.307f,
                    y1 = 10.818f,
                    x2 = 8.97f,
                    y2 = 11.0f,
                    x3 = 8.5f,
                    y3 = 11.0f,
                )
                // c -0.582 0 -1.028 -0.061 -1.434 -0.196
                curveToRelative(
                    dx1 = -0.582f,
                    dy1 = 0.0f,
                    dx2 = -1.028f,
                    dy2 = -0.061f,
                    dx3 = -1.434f,
                    dy3 = -0.196f,
                )
                // c -0.407 -0.136 -0.8 -0.355 -1.266 -0.704z
                curveToRelative(
                    dx1 = -0.407f,
                    dy1 = -0.136f,
                    dx2 = -0.8f,
                    dy2 = -0.355f,
                    dx3 = -1.266f,
                    dy3 = -0.704f,
                )
                close()
                // m 1 2.5
                moveToRelative(dx = 1.0f, dy = 2.5f)
                // a 0.5 0.5 0 1 0 -0.6 0.8
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.6f,
                    dy1 = 0.8f,
                )
                // c 0.523 0.392 1.011 0.673 1.55 0.852
                curveToRelative(
                    dx1 = 0.523f,
                    dy1 = 0.392f,
                    dx2 = 1.011f,
                    dy2 = 0.673f,
                    dx3 = 1.55f,
                    dy3 = 0.852f,
                )
                // c 0.537 0.18 1.097 0.248 1.75 0.248
                curveToRelative(
                    dx1 = 0.537f,
                    dy1 = 0.18f,
                    dx2 = 1.097f,
                    dy2 = 0.248f,
                    dx3 = 1.75f,
                    dy3 = 0.248f,
                )
                // c 0.75 0 1.29 -0.306 1.722 -0.551
                curveToRelative(
                    dx1 = 0.75f,
                    dy1 = 0.0f,
                    dx2 = 1.29f,
                    dy2 = -0.306f,
                    dx3 = 1.722f,
                    dy3 = -0.551f,
                )
                // l 0.024 -0.014
                lineToRelative(dx = 0.024f, dy = -0.014f)
                // c 0.447 -0.253 0.783 -0.435 1.254 -0.435
                curveToRelative(
                    dx1 = 0.447f,
                    dy1 = -0.253f,
                    dx2 = 0.783f,
                    dy2 = -0.435f,
                    dx3 = 1.254f,
                    dy3 = -0.435f,
                )
                // c 0.582 0 1.028 0.061 1.434 0.197
                curveToRelative(
                    dx1 = 0.582f,
                    dy1 = 0.0f,
                    dx2 = 1.028f,
                    dy2 = 0.061f,
                    dx3 = 1.434f,
                    dy3 = 0.197f,
                )
                // c 0.407 0.135 0.8 0.354 1.266 0.703
                curveToRelative(
                    dx1 = 0.407f,
                    dy1 = 0.135f,
                    dx2 = 0.8f,
                    dy2 = 0.354f,
                    dx3 = 1.266f,
                    dy3 = 0.703f,
                )
                // a 0.5 0.5 0 0 0 0.6 -0.8
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.6f,
                    dy1 = -0.8f,
                )
                // c -0.523 -0.392 -1.011 -0.673 -1.55 -0.852
                curveToRelative(
                    dx1 = -0.523f,
                    dy1 = -0.392f,
                    dx2 = -1.011f,
                    dy2 = -0.673f,
                    dx3 = -1.55f,
                    dy3 = -0.852f,
                )
                // c -0.537 -0.18 -1.097 -0.248 -1.75 -0.248
                curveToRelative(
                    dx1 = -0.537f,
                    dy1 = -0.18f,
                    dx2 = -1.097f,
                    dy2 = -0.248f,
                    dx3 = -1.75f,
                    dy3 = -0.248f,
                )
                // c -0.75 0 -1.29 0.306 -1.722 0.551
                curveToRelative(
                    dx1 = -0.75f,
                    dy1 = 0.0f,
                    dx2 = -1.29f,
                    dy2 = 0.306f,
                    dx3 = -1.722f,
                    dy3 = 0.551f,
                )
                // l -0.024 0.014
                lineToRelative(dx = -0.024f, dy = 0.014f)
                // c -0.447 0.253 -0.783 0.435 -1.254 0.435
                curveToRelative(
                    dx1 = -0.447f,
                    dy1 = 0.253f,
                    dx2 = -0.783f,
                    dy2 = 0.435f,
                    dx3 = -1.254f,
                    dy3 = 0.435f,
                )
                // c -0.582 0 -1.028 -0.061 -1.434 -0.196
                curveToRelative(
                    dx1 = -0.582f,
                    dy1 = 0.0f,
                    dx2 = -1.028f,
                    dy2 = -0.061f,
                    dx3 = -1.434f,
                    dy3 = -0.196f,
                )
                // c -0.407 -0.136 -0.8 -0.355 -1.266 -0.704z
                curveToRelative(
                    dx1 = -0.407f,
                    dy1 = -0.136f,
                    dx2 = -0.8f,
                    dy2 = -0.355f,
                    dx3 = -1.266f,
                    dy3 = -0.704f,
                )
                close()
            }
        }.build().also { _ic2387 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2387: ImageVector? = null
