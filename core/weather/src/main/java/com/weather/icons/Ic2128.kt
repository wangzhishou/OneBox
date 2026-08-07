package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2128: ImageVector
    get() {
        val current = _ic2128
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2128",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M2.4 11.3 a.5 .5 0 0 0 -.8 -.6 3.192 3.192 0 0 0 -.6 1.8 c0 .288 .141 .521 .256 .675 .116 .154 .262 .3 .379 .417 l.011 .012 c.132 .131 .23 .23 .298 .321 .038 .052 .052 .08 .056 .09 a2.2 2.2 0 0 1 -.4 1.185 .5 .5 0 0 0 .8 .6 c.365 -.487 .6 -1.192 .6 -1.8 0 -.288 -.141 -.521 -.256 -.675 a4.068 4.068 0 0 0 -.379 -.417 l-.011 -.011 a3.335 3.335 0 0 1 -.298 -.322 .444 .444 0 0 1 -.056 -.09 c.004 -.39 .168 -.875 .4 -1.185Z m4 0 a.5 .5 0 0 0 -.8 -.6 3.192 3.192 0 0 0 -.6 1.8 c0 .288 .141 .521 .256 .675 .116 .154 .262 .3 .379 .417 l.011 .012 c.132 .131 .23 .23 .298 .321 .038 .052 .052 .08 .056 .09 a2.2 2.2 0 0 1 -.4 1.185 .5 .5 0 0 0 .8 .6 c.365 -.487 .6 -1.192 .6 -1.8 0 -.288 -.141 -.521 -.256 -.675 a4.068 4.068 0 0 0 -.379 -.417 l-.011 -.011 a3.335 3.335 0 0 1 -.298 -.322 .444 .444 0 0 1 -.056 -.09 c.004 -.39 .168 -.875 .4 -1.185Z m3.9 -.7 a.5 .5 0 0 1 .1 .7 c-.232 .31 -.396 .795 -.4 1.185 .004 .01 .018 .038 .056 .09 .069 .091 .166 .19 .298 .322 l.011 .011 c.117 .117 .263 .263 .379 .417 .115 .154 .256 .387 .256 .675 0 .608 -.235 1.313 -.6 1.8 a.5 .5 0 0 1 -.8 -.6 c.232 -.31 .396 -.795 .4 -1.185 a.446 .446 0 0 0 -.056 -.09 3.324 3.324 0 0 0 -.298 -.321 l-.011 -.012 a4.13 4.13 0 0 1 -.379 -.417 C9.141 13.021 9 12.788 9 12.5 c0 -.608 .235 -1.313 .6 -1.8 a.5 .5 0 0 1 .7 -.1Z m4.1 .7 a.5 .5 0 0 0 -.8 -.6 3.192 3.192 0 0 0 -.6 1.8 c0 .288 .141 .521 .256 .675 .116 .154 .262 .3 .379 .417 l.011 .012 c.132 .131 .23 .23 .298 .321 .038 .052 .052 .08 .056 .09 a2.2 2.2 0 0 1 -.4 1.185 .5 .5 0 0 0 .8 .6 c.365 -.487 .6 -1.192 .6 -1.8 0 -.288 -.141 -.521 -.256 -.675 a4.058 4.058 0 0 0 -.379 -.417 l-.011 -.011 a3.334 3.334 0 0 1 -.298 -.322 .441 .441 0 0 1 -.056 -.09 c.004 -.39 .168 -.875 .4 -1.185Z M8.003 2.188 a2.813 2.813 0 1 0 0 5.625 2.813 2.813 0 0 0 0 -5.625Z m.003 -.624 a.313 .313 0 0 1 -.313 -.312 V.314 a.312 .312 0 1 1 .625 0 v.938 a.312 .312 0 0 1 -.312 .312Z M5.354 2.66 a.311 .311 0 0 1 -.22 -.091 l-.665 -.663 a.312 .312 0 0 1 .442 -.442 l.665 .662 a.312 .312 0 0 1 -.221 .534 h-.001Z M4.252 5.31 h-.937 a.313 .313 0 1 1 0 -.626 h.937 a.313 .313 0 1 1 0 .625Z m.432 3.314 a.313 .313 0 0 1 -.22 -.534 l.663 -.663 a.313 .313 0 0 1 .442 .442 l-.664 .663 a.311 .311 0 0 1 -.22 .091Z m3.314 1.375 a.313 .313 0 0 1 -.313 -.312 v-.938 a.312 .312 0 1 1 .625 0 v.938 a.312 .312 0 0 1 -.312 .312Z m3.314 -1.37 a.311 .311 0 0 1 -.22 -.091 l-.663 -.663 a.312 .312 0 1 1 .441 -.441 l.663 .662 a.312 .312 0 0 1 -.22 .534Z m1.377 -3.311 h-.938 a.313 .313 0 1 1 0 -.625 h.938 a.313 .313 0 1 1 0 .625Z m-2.033 -2.651 a.313 .313 0 0 1 -.221 -.534 l.662 -.663 a.313 .313 0 0 1 .443 .442 l-.663 .663 a.31 .31 0 0 1 -.221 .092Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 2.4 11.3
                moveTo(x = 2.4f, y = 11.3f)
                // a 0.5 0.5 0 0 0 -0.8 -0.6
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.8f,
                    dy1 = -0.6f,
                )
                // a 3.192 3.192 0 0 0 -0.6 1.8
                arcToRelative(
                    a = 3.192f,
                    b = 3.192f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.6f,
                    dy1 = 1.8f,
                )
                // c 0 0.288 0.141 0.521 0.256 0.675
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.288f,
                    dx2 = 0.141f,
                    dy2 = 0.521f,
                    dx3 = 0.256f,
                    dy3 = 0.675f,
                )
                // c 0.116 0.154 0.262 0.3 0.379 0.417
                curveToRelative(
                    dx1 = 0.116f,
                    dy1 = 0.154f,
                    dx2 = 0.262f,
                    dy2 = 0.3f,
                    dx3 = 0.379f,
                    dy3 = 0.417f,
                )
                // l 0.011 0.012
                lineToRelative(dx = 0.011f, dy = 0.012f)
                // c 0.132 0.131 0.23 0.23 0.298 0.321
                curveToRelative(
                    dx1 = 0.132f,
                    dy1 = 0.131f,
                    dx2 = 0.23f,
                    dy2 = 0.23f,
                    dx3 = 0.298f,
                    dy3 = 0.321f,
                )
                // c 0.038 0.052 0.052 0.08 0.056 0.09
                curveToRelative(
                    dx1 = 0.038f,
                    dy1 = 0.052f,
                    dx2 = 0.052f,
                    dy2 = 0.08f,
                    dx3 = 0.056f,
                    dy3 = 0.09f,
                )
                // a 2.2 2.2 0 0 1 -0.4 1.185
                arcToRelative(
                    a = 2.2f,
                    b = 2.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.4f,
                    dy1 = 1.185f,
                )
                // a 0.5 0.5 0 0 0 0.8 0.6
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.8f,
                    dy1 = 0.6f,
                )
                // c 0.365 -0.487 0.6 -1.192 0.6 -1.8
                curveToRelative(
                    dx1 = 0.365f,
                    dy1 = -0.487f,
                    dx2 = 0.6f,
                    dy2 = -1.192f,
                    dx3 = 0.6f,
                    dy3 = -1.8f,
                )
                // c 0 -0.288 -0.141 -0.521 -0.256 -0.675
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.288f,
                    dx2 = -0.141f,
                    dy2 = -0.521f,
                    dx3 = -0.256f,
                    dy3 = -0.675f,
                )
                // a 4.068 4.068 0 0 0 -0.379 -0.417
                arcToRelative(
                    a = 4.068f,
                    b = 4.068f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.379f,
                    dy1 = -0.417f,
                )
                // l -0.011 -0.011
                lineToRelative(dx = -0.011f, dy = -0.011f)
                // a 3.335 3.335 0 0 1 -0.298 -0.322
                arcToRelative(
                    a = 3.335f,
                    b = 3.335f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.298f,
                    dy1 = -0.322f,
                )
                // a 0.444 0.444 0 0 1 -0.056 -0.09
                arcToRelative(
                    a = 0.444f,
                    b = 0.444f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.056f,
                    dy1 = -0.09f,
                )
                // c 0.004 -0.39 0.168 -0.875 0.4 -1.185z
                curveToRelative(
                    dx1 = 0.004f,
                    dy1 = -0.39f,
                    dx2 = 0.168f,
                    dy2 = -0.875f,
                    dx3 = 0.4f,
                    dy3 = -1.185f,
                )
                close()
                // m 4 0
                moveToRelative(dx = 4.0f, dy = 0.0f)
                // a 0.5 0.5 0 0 0 -0.8 -0.6
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.8f,
                    dy1 = -0.6f,
                )
                // a 3.192 3.192 0 0 0 -0.6 1.8
                arcToRelative(
                    a = 3.192f,
                    b = 3.192f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.6f,
                    dy1 = 1.8f,
                )
                // c 0 0.288 0.141 0.521 0.256 0.675
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.288f,
                    dx2 = 0.141f,
                    dy2 = 0.521f,
                    dx3 = 0.256f,
                    dy3 = 0.675f,
                )
                // c 0.116 0.154 0.262 0.3 0.379 0.417
                curveToRelative(
                    dx1 = 0.116f,
                    dy1 = 0.154f,
                    dx2 = 0.262f,
                    dy2 = 0.3f,
                    dx3 = 0.379f,
                    dy3 = 0.417f,
                )
                // l 0.011 0.012
                lineToRelative(dx = 0.011f, dy = 0.012f)
                // c 0.132 0.131 0.23 0.23 0.298 0.321
                curveToRelative(
                    dx1 = 0.132f,
                    dy1 = 0.131f,
                    dx2 = 0.23f,
                    dy2 = 0.23f,
                    dx3 = 0.298f,
                    dy3 = 0.321f,
                )
                // c 0.038 0.052 0.052 0.08 0.056 0.09
                curveToRelative(
                    dx1 = 0.038f,
                    dy1 = 0.052f,
                    dx2 = 0.052f,
                    dy2 = 0.08f,
                    dx3 = 0.056f,
                    dy3 = 0.09f,
                )
                // a 2.2 2.2 0 0 1 -0.4 1.185
                arcToRelative(
                    a = 2.2f,
                    b = 2.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.4f,
                    dy1 = 1.185f,
                )
                // a 0.5 0.5 0 0 0 0.8 0.6
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.8f,
                    dy1 = 0.6f,
                )
                // c 0.365 -0.487 0.6 -1.192 0.6 -1.8
                curveToRelative(
                    dx1 = 0.365f,
                    dy1 = -0.487f,
                    dx2 = 0.6f,
                    dy2 = -1.192f,
                    dx3 = 0.6f,
                    dy3 = -1.8f,
                )
                // c 0 -0.288 -0.141 -0.521 -0.256 -0.675
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.288f,
                    dx2 = -0.141f,
                    dy2 = -0.521f,
                    dx3 = -0.256f,
                    dy3 = -0.675f,
                )
                // a 4.068 4.068 0 0 0 -0.379 -0.417
                arcToRelative(
                    a = 4.068f,
                    b = 4.068f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.379f,
                    dy1 = -0.417f,
                )
                // l -0.011 -0.011
                lineToRelative(dx = -0.011f, dy = -0.011f)
                // a 3.335 3.335 0 0 1 -0.298 -0.322
                arcToRelative(
                    a = 3.335f,
                    b = 3.335f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.298f,
                    dy1 = -0.322f,
                )
                // a 0.444 0.444 0 0 1 -0.056 -0.09
                arcToRelative(
                    a = 0.444f,
                    b = 0.444f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.056f,
                    dy1 = -0.09f,
                )
                // c 0.004 -0.39 0.168 -0.875 0.4 -1.185z
                curveToRelative(
                    dx1 = 0.004f,
                    dy1 = -0.39f,
                    dx2 = 0.168f,
                    dy2 = -0.875f,
                    dx3 = 0.4f,
                    dy3 = -1.185f,
                )
                close()
                // m 3.9 -0.7
                moveToRelative(dx = 3.9f, dy = -0.7f)
                // a 0.5 0.5 0 0 1 0.1 0.7
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.1f,
                    dy1 = 0.7f,
                )
                // c -0.232 0.31 -0.396 0.795 -0.4 1.185
                curveToRelative(
                    dx1 = -0.232f,
                    dy1 = 0.31f,
                    dx2 = -0.396f,
                    dy2 = 0.795f,
                    dx3 = -0.4f,
                    dy3 = 1.185f,
                )
                // c 0.004 0.01 0.018 0.038 0.056 0.09
                curveToRelative(
                    dx1 = 0.004f,
                    dy1 = 0.01f,
                    dx2 = 0.018f,
                    dy2 = 0.038f,
                    dx3 = 0.056f,
                    dy3 = 0.09f,
                )
                // c 0.069 0.091 0.166 0.19 0.298 0.322
                curveToRelative(
                    dx1 = 0.069f,
                    dy1 = 0.091f,
                    dx2 = 0.166f,
                    dy2 = 0.19f,
                    dx3 = 0.298f,
                    dy3 = 0.322f,
                )
                // l 0.011 0.011
                lineToRelative(dx = 0.011f, dy = 0.011f)
                // c 0.117 0.117 0.263 0.263 0.379 0.417
                curveToRelative(
                    dx1 = 0.117f,
                    dy1 = 0.117f,
                    dx2 = 0.263f,
                    dy2 = 0.263f,
                    dx3 = 0.379f,
                    dy3 = 0.417f,
                )
                // c 0.115 0.154 0.256 0.387 0.256 0.675
                curveToRelative(
                    dx1 = 0.115f,
                    dy1 = 0.154f,
                    dx2 = 0.256f,
                    dy2 = 0.387f,
                    dx3 = 0.256f,
                    dy3 = 0.675f,
                )
                // c 0 0.608 -0.235 1.313 -0.6 1.8
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.608f,
                    dx2 = -0.235f,
                    dy2 = 1.313f,
                    dx3 = -0.6f,
                    dy3 = 1.8f,
                )
                // a 0.5 0.5 0 0 1 -0.8 -0.6
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.8f,
                    dy1 = -0.6f,
                )
                // c 0.232 -0.31 0.396 -0.795 0.4 -1.185
                curveToRelative(
                    dx1 = 0.232f,
                    dy1 = -0.31f,
                    dx2 = 0.396f,
                    dy2 = -0.795f,
                    dx3 = 0.4f,
                    dy3 = -1.185f,
                )
                // a 0.446 0.446 0 0 0 -0.056 -0.09
                arcToRelative(
                    a = 0.446f,
                    b = 0.446f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.056f,
                    dy1 = -0.09f,
                )
                // a 3.324 3.324 0 0 0 -0.298 -0.321
                arcToRelative(
                    a = 3.324f,
                    b = 3.324f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.298f,
                    dy1 = -0.321f,
                )
                // l -0.011 -0.012
                lineToRelative(dx = -0.011f, dy = -0.012f)
                // a 4.13 4.13 0 0 1 -0.379 -0.417
                arcToRelative(
                    a = 4.13f,
                    b = 4.13f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.379f,
                    dy1 = -0.417f,
                )
                // C 9.141 13.021 9 12.788 9 12.5
                curveTo(
                    x1 = 9.141f,
                    y1 = 13.021f,
                    x2 = 9.0f,
                    y2 = 12.788f,
                    x3 = 9.0f,
                    y3 = 12.5f,
                )
                // c 0 -0.608 0.235 -1.313 0.6 -1.8
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.608f,
                    dx2 = 0.235f,
                    dy2 = -1.313f,
                    dx3 = 0.6f,
                    dy3 = -1.8f,
                )
                // a 0.5 0.5 0 0 1 0.7 -0.1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.7f,
                    dy1 = -0.1f,
                )
                close()
                // m 4.1 0.7
                moveToRelative(dx = 4.1f, dy = 0.7f)
                // a 0.5 0.5 0 0 0 -0.8 -0.6
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.8f,
                    dy1 = -0.6f,
                )
                // a 3.192 3.192 0 0 0 -0.6 1.8
                arcToRelative(
                    a = 3.192f,
                    b = 3.192f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.6f,
                    dy1 = 1.8f,
                )
                // c 0 0.288 0.141 0.521 0.256 0.675
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.288f,
                    dx2 = 0.141f,
                    dy2 = 0.521f,
                    dx3 = 0.256f,
                    dy3 = 0.675f,
                )
                // c 0.116 0.154 0.262 0.3 0.379 0.417
                curveToRelative(
                    dx1 = 0.116f,
                    dy1 = 0.154f,
                    dx2 = 0.262f,
                    dy2 = 0.3f,
                    dx3 = 0.379f,
                    dy3 = 0.417f,
                )
                // l 0.011 0.012
                lineToRelative(dx = 0.011f, dy = 0.012f)
                // c 0.132 0.131 0.23 0.23 0.298 0.321
                curveToRelative(
                    dx1 = 0.132f,
                    dy1 = 0.131f,
                    dx2 = 0.23f,
                    dy2 = 0.23f,
                    dx3 = 0.298f,
                    dy3 = 0.321f,
                )
                // c 0.038 0.052 0.052 0.08 0.056 0.09
                curveToRelative(
                    dx1 = 0.038f,
                    dy1 = 0.052f,
                    dx2 = 0.052f,
                    dy2 = 0.08f,
                    dx3 = 0.056f,
                    dy3 = 0.09f,
                )
                // a 2.2 2.2 0 0 1 -0.4 1.185
                arcToRelative(
                    a = 2.2f,
                    b = 2.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.4f,
                    dy1 = 1.185f,
                )
                // a 0.5 0.5 0 0 0 0.8 0.6
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.8f,
                    dy1 = 0.6f,
                )
                // c 0.365 -0.487 0.6 -1.192 0.6 -1.8
                curveToRelative(
                    dx1 = 0.365f,
                    dy1 = -0.487f,
                    dx2 = 0.6f,
                    dy2 = -1.192f,
                    dx3 = 0.6f,
                    dy3 = -1.8f,
                )
                // c 0 -0.288 -0.141 -0.521 -0.256 -0.675
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.288f,
                    dx2 = -0.141f,
                    dy2 = -0.521f,
                    dx3 = -0.256f,
                    dy3 = -0.675f,
                )
                // a 4.058 4.058 0 0 0 -0.379 -0.417
                arcToRelative(
                    a = 4.058f,
                    b = 4.058f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.379f,
                    dy1 = -0.417f,
                )
                // l -0.011 -0.011
                lineToRelative(dx = -0.011f, dy = -0.011f)
                // a 3.334 3.334 0 0 1 -0.298 -0.322
                arcToRelative(
                    a = 3.334f,
                    b = 3.334f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.298f,
                    dy1 = -0.322f,
                )
                // a 0.441 0.441 0 0 1 -0.056 -0.09
                arcToRelative(
                    a = 0.441f,
                    b = 0.441f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.056f,
                    dy1 = -0.09f,
                )
                // c 0.004 -0.39 0.168 -0.875 0.4 -1.185z
                curveToRelative(
                    dx1 = 0.004f,
                    dy1 = -0.39f,
                    dx2 = 0.168f,
                    dy2 = -0.875f,
                    dx3 = 0.4f,
                    dy3 = -1.185f,
                )
                close()
                // M 8.003 2.188
                moveTo(x = 8.003f, y = 2.188f)
                // a 2.813 2.813 0 1 0 0 5.625
                arcToRelative(
                    a = 2.813f,
                    b = 2.813f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 5.625f,
                )
                // a 2.813 2.813 0 0 0 0 -5.625z
                arcToRelative(
                    a = 2.813f,
                    b = 2.813f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -5.625f,
                )
                close()
                // m 0.003 -0.624
                moveToRelative(dx = 0.003f, dy = -0.624f)
                // a 0.313 0.313 0 0 1 -0.313 -0.312
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.313f,
                    dy1 = -0.312f,
                )
                // V 0.314
                verticalLineTo(y = 0.314f)
                // a 0.312 0.312 0 1 1 0.625 0
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.625f,
                    dy1 = 0.0f,
                )
                // v 0.938
                verticalLineToRelative(dy = 0.938f)
                // a 0.312 0.312 0 0 1 -0.312 0.312z
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.312f,
                    dy1 = 0.312f,
                )
                close()
                // M 5.354 2.66
                moveTo(x = 5.354f, y = 2.66f)
                // a 0.311 0.311 0 0 1 -0.22 -0.091
                arcToRelative(
                    a = 0.311f,
                    b = 0.311f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.22f,
                    dy1 = -0.091f,
                )
                // l -0.665 -0.663
                lineToRelative(dx = -0.665f, dy = -0.663f)
                // a 0.312 0.312 0 0 1 0.442 -0.442
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.442f,
                    dy1 = -0.442f,
                )
                // l 0.665 0.662
                lineToRelative(dx = 0.665f, dy = 0.662f)
                // a 0.312 0.312 0 0 1 -0.221 0.534
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.221f,
                    dy1 = 0.534f,
                )
                // h -0.001z
                horizontalLineToRelative(dx = -0.001f)
                close()
                // M 4.252 5.31
                moveTo(x = 4.252f, y = 5.31f)
                // h -0.937
                horizontalLineToRelative(dx = -0.937f)
                // a 0.313 0.313 0 1 1 0 -0.626
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -0.626f,
                )
                // h 0.937
                horizontalLineToRelative(dx = 0.937f)
                // a 0.313 0.313 0 1 1 0 0.625z
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.625f,
                )
                close()
                // m 0.432 3.314
                moveToRelative(dx = 0.432f, dy = 3.314f)
                // a 0.313 0.313 0 0 1 -0.22 -0.534
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.22f,
                    dy1 = -0.534f,
                )
                // l 0.663 -0.663
                lineToRelative(dx = 0.663f, dy = -0.663f)
                // a 0.313 0.313 0 0 1 0.442 0.442
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.442f,
                    dy1 = 0.442f,
                )
                // l -0.664 0.663
                lineToRelative(dx = -0.664f, dy = 0.663f)
                // a 0.311 0.311 0 0 1 -0.22 0.091z
                arcToRelative(
                    a = 0.311f,
                    b = 0.311f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.22f,
                    dy1 = 0.091f,
                )
                close()
                // m 3.314 1.375
                moveToRelative(dx = 3.314f, dy = 1.375f)
                // a 0.313 0.313 0 0 1 -0.313 -0.312
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.313f,
                    dy1 = -0.312f,
                )
                // v -0.938
                verticalLineToRelative(dy = -0.938f)
                // a 0.312 0.312 0 1 1 0.625 0
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.625f,
                    dy1 = 0.0f,
                )
                // v 0.938
                verticalLineToRelative(dy = 0.938f)
                // a 0.312 0.312 0 0 1 -0.312 0.312z
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.312f,
                    dy1 = 0.312f,
                )
                close()
                // m 3.314 -1.37
                moveToRelative(dx = 3.314f, dy = -1.37f)
                // a 0.311 0.311 0 0 1 -0.22 -0.091
                arcToRelative(
                    a = 0.311f,
                    b = 0.311f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.22f,
                    dy1 = -0.091f,
                )
                // l -0.663 -0.663
                lineToRelative(dx = -0.663f, dy = -0.663f)
                // a 0.312 0.312 0 1 1 0.441 -0.441
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.441f,
                    dy1 = -0.441f,
                )
                // l 0.663 0.662
                lineToRelative(dx = 0.663f, dy = 0.662f)
                // a 0.312 0.312 0 0 1 -0.22 0.534z
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.22f,
                    dy1 = 0.534f,
                )
                close()
                // m 1.377 -3.311
                moveToRelative(dx = 1.377f, dy = -3.311f)
                // h -0.938
                horizontalLineToRelative(dx = -0.938f)
                // a 0.313 0.313 0 1 1 0 -0.625
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -0.625f,
                )
                // h 0.938
                horizontalLineToRelative(dx = 0.938f)
                // a 0.313 0.313 0 1 1 0 0.625z
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.625f,
                )
                close()
                // m -2.033 -2.651
                moveToRelative(dx = -2.033f, dy = -2.651f)
                // a 0.313 0.313 0 0 1 -0.221 -0.534
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.221f,
                    dy1 = -0.534f,
                )
                // l 0.662 -0.663
                lineToRelative(dx = 0.662f, dy = -0.663f)
                // a 0.313 0.313 0 0 1 0.443 0.442
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.443f,
                    dy1 = 0.442f,
                )
                // l -0.663 0.663
                lineToRelative(dx = -0.663f, dy = 0.663f)
                // a 0.31 0.31 0 0 1 -0.221 0.092z
                arcToRelative(
                    a = 0.31f,
                    b = 0.31f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.221f,
                    dy1 = 0.092f,
                )
                close()
            }
        }.build().also { _ic2128 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2128: ImageVector? = null
