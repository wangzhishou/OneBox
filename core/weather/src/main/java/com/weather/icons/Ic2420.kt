package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2420: ImageVector
    get() {
        val current = _ic2420
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2420",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M.5 15 a.5 .5 0 0 1 .5 -.5 h2 a.5 .5 0 0 1 0 1 H1 a.5 .5 0 0 1 -.5 -.5Z m4 0 a.5 .5 0 0 1 .5 -.5 h2 a.5 .5 0 0 1 0 1 H5 a.5 .5 0 0 1 -.5 -.5Z m4 0 a.5 .5 0 0 1 .5 -.5 h2 a.5 .5 0 0 1 0 1 H9 a.5 .5 0 0 1 -.5 -.5Z m4 0 a.5 .5 0 0 1 .5 -.5 h2 a.5 .5 0 0 1 0 1 h-2 a.5 .5 0 0 1 -.5 -.5Z m-.163 -5.302 a.485 .485 0 0 1 .685 -.1 l2.478 1.88 -2.478 1.88 a.486 .486 0 0 1 -.685 -.1 .498 .498 0 0 1 .098 -.692 l.782 -.593 h-1.116 c-.042 .11 -.095 .238 -.159 .371 -.092 .19 -.213 .405 -.367 .595 a1.54 1.54 0 0 1 -.63 .485 c-.462 .178 -.885 .016 -1.19 -.206 -.3 -.217 -.552 -.532 -.746 -.823 a6.112 6.112 0 0 1 -.219 -.355 5.91 5.91 0 0 1 -.152 .223 3.88 3.88 0 0 1 -.716 .78 c-.281 .222 -.643 .414 -1.063 .414 -.42 0 -.782 -.192 -1.064 -.415 a3.88 3.88 0 0 1 -.716 -.78 5.989 5.989 0 0 1 -.177 -.26 5.998 5.998 0 0 1 -.177 .26 3.88 3.88 0 0 1 -.716 .78 c-.281 .223 -.643 .415 -1.063 .415 C1.595 13.457 .5 12.349 .5 10.984 c0 -.273 .219 -.495 .49 -.495 .27 0 .488 .222 .488 .495 0 .82 .657 1.483 1.468 1.483 .12 0 .273 -.055 .46 -.203 a2.91 2.91 0 0 0 .527 -.581 5.724 5.724 0 0 0 .516 -.886 l.006 -.013 v-.002 l.447 -1.009 .446 1.008 .002 .003 .006 .013 .026 .055 a5.724 5.724 0 0 0 .49 .83 c.162 .228 .342 .435 .527 .582 .187 .148 .34 .203 .46 .203 s.273 -.055 .46 -.203 a2.91 2.91 0 0 0 .527 -.581 5.725 5.725 0 0 0 .516 -.886 l.006 -.013 v-.002 l.47 -1.06 .43 1.076 .002 .004 .007 .016 a5.083 5.083 0 0 0 .141 .312 c.099 .204 .238 .465 .402 .711 .169 .253 .343 .456 .505 .573 .156 .113 .23 .102 .273 .086 a.586 .586 0 0 0 .22 -.187 c.09 -.111 .173 -.253 .245 -.402 a3.756 3.756 0 0 0 .213 -.556 l.002 -.007 .099 -.364 h1.84 l-.782 -.594 a.498 .498 0 0 1 -.098 -.692Z m-4.96 -6.424 c-.032 -.282 .256 -.524 .623 -.524 s.655 .242 .623 .524 L8.34 5.75 h-.68 l-.282 -2.476Z M8.504 6.75 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0.5 15
                moveTo(x = 0.5f, y = 15.0f)
                // a 0.5 0.5 0 0 1 0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = -0.5f,
                )
                // h 2
                horizontalLineToRelative(dx = 2.0f)
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
                // H 1
                horizontalLineTo(x = 1.0f)
                // a 0.5 0.5 0 0 1 -0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                close()
                // m 4 0
                moveToRelative(dx = 4.0f, dy = 0.0f)
                // a 0.5 0.5 0 0 1 0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = -0.5f,
                )
                // h 2
                horizontalLineToRelative(dx = 2.0f)
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
                // H 5
                horizontalLineTo(x = 5.0f)
                // a 0.5 0.5 0 0 1 -0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                close()
                // m 4 0
                moveToRelative(dx = 4.0f, dy = 0.0f)
                // a 0.5 0.5 0 0 1 0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = -0.5f,
                )
                // h 2
                horizontalLineToRelative(dx = 2.0f)
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
                // H 9
                horizontalLineTo(x = 9.0f)
                // a 0.5 0.5 0 0 1 -0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                close()
                // m 4 0
                moveToRelative(dx = 4.0f, dy = 0.0f)
                // a 0.5 0.5 0 0 1 0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = -0.5f,
                )
                // h 2
                horizontalLineToRelative(dx = 2.0f)
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
                // h -2
                horizontalLineToRelative(dx = -2.0f)
                // a 0.5 0.5 0 0 1 -0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                close()
                // m -0.163 -5.302
                moveToRelative(dx = -0.163f, dy = -5.302f)
                // a 0.485 0.485 0 0 1 0.685 -0.1
                arcToRelative(
                    a = 0.485f,
                    b = 0.485f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.685f,
                    dy1 = -0.1f,
                )
                // l 2.478 1.88
                lineToRelative(dx = 2.478f, dy = 1.88f)
                // l -2.478 1.88
                lineToRelative(dx = -2.478f, dy = 1.88f)
                // a 0.486 0.486 0 0 1 -0.685 -0.1
                arcToRelative(
                    a = 0.486f,
                    b = 0.486f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.685f,
                    dy1 = -0.1f,
                )
                // a 0.498 0.498 0 0 1 0.098 -0.692
                arcToRelative(
                    a = 0.498f,
                    b = 0.498f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.098f,
                    dy1 = -0.692f,
                )
                // l 0.782 -0.593
                lineToRelative(dx = 0.782f, dy = -0.593f)
                // h -1.116
                horizontalLineToRelative(dx = -1.116f)
                // c -0.042 0.11 -0.095 0.238 -0.159 0.371
                curveToRelative(
                    dx1 = -0.042f,
                    dy1 = 0.11f,
                    dx2 = -0.095f,
                    dy2 = 0.238f,
                    dx3 = -0.159f,
                    dy3 = 0.371f,
                )
                // c -0.092 0.19 -0.213 0.405 -0.367 0.595
                curveToRelative(
                    dx1 = -0.092f,
                    dy1 = 0.19f,
                    dx2 = -0.213f,
                    dy2 = 0.405f,
                    dx3 = -0.367f,
                    dy3 = 0.595f,
                )
                // a 1.54 1.54 0 0 1 -0.63 0.485
                arcToRelative(
                    a = 1.54f,
                    b = 1.54f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.63f,
                    dy1 = 0.485f,
                )
                // c -0.462 0.178 -0.885 0.016 -1.19 -0.206
                curveToRelative(
                    dx1 = -0.462f,
                    dy1 = 0.178f,
                    dx2 = -0.885f,
                    dy2 = 0.016f,
                    dx3 = -1.19f,
                    dy3 = -0.206f,
                )
                // c -0.3 -0.217 -0.552 -0.532 -0.746 -0.823
                curveToRelative(
                    dx1 = -0.3f,
                    dy1 = -0.217f,
                    dx2 = -0.552f,
                    dy2 = -0.532f,
                    dx3 = -0.746f,
                    dy3 = -0.823f,
                )
                // a 6.112 6.112 0 0 1 -0.219 -0.355
                arcToRelative(
                    a = 6.112f,
                    b = 6.112f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.219f,
                    dy1 = -0.355f,
                )
                // a 5.91 5.91 0 0 1 -0.152 0.223
                arcToRelative(
                    a = 5.91f,
                    b = 5.91f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.152f,
                    dy1 = 0.223f,
                )
                // a 3.88 3.88 0 0 1 -0.716 0.78
                arcToRelative(
                    a = 3.88f,
                    b = 3.88f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.716f,
                    dy1 = 0.78f,
                )
                // c -0.281 0.222 -0.643 0.414 -1.063 0.414
                curveToRelative(
                    dx1 = -0.281f,
                    dy1 = 0.222f,
                    dx2 = -0.643f,
                    dy2 = 0.414f,
                    dx3 = -1.063f,
                    dy3 = 0.414f,
                )
                // c -0.42 0 -0.782 -0.192 -1.064 -0.415
                curveToRelative(
                    dx1 = -0.42f,
                    dy1 = 0.0f,
                    dx2 = -0.782f,
                    dy2 = -0.192f,
                    dx3 = -1.064f,
                    dy3 = -0.415f,
                )
                // a 3.88 3.88 0 0 1 -0.716 -0.78
                arcToRelative(
                    a = 3.88f,
                    b = 3.88f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.716f,
                    dy1 = -0.78f,
                )
                // a 5.989 5.989 0 0 1 -0.177 -0.26
                arcToRelative(
                    a = 5.989f,
                    b = 5.989f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.177f,
                    dy1 = -0.26f,
                )
                // a 5.998 5.998 0 0 1 -0.177 0.26
                arcToRelative(
                    a = 5.998f,
                    b = 5.998f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.177f,
                    dy1 = 0.26f,
                )
                // a 3.88 3.88 0 0 1 -0.716 0.78
                arcToRelative(
                    a = 3.88f,
                    b = 3.88f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.716f,
                    dy1 = 0.78f,
                )
                // c -0.281 0.223 -0.643 0.415 -1.063 0.415
                curveToRelative(
                    dx1 = -0.281f,
                    dy1 = 0.223f,
                    dx2 = -0.643f,
                    dy2 = 0.415f,
                    dx3 = -1.063f,
                    dy3 = 0.415f,
                )
                // C 1.595 13.457 0.5 12.349 0.5 10.984
                curveTo(
                    x1 = 1.595f,
                    y1 = 13.457f,
                    x2 = 0.5f,
                    y2 = 12.349f,
                    x3 = 0.5f,
                    y3 = 10.984f,
                )
                // c 0 -0.273 0.219 -0.495 0.49 -0.495
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.273f,
                    dx2 = 0.219f,
                    dy2 = -0.495f,
                    dx3 = 0.49f,
                    dy3 = -0.495f,
                )
                // c 0.27 0 0.488 0.222 0.488 0.495
                curveToRelative(
                    dx1 = 0.27f,
                    dy1 = 0.0f,
                    dx2 = 0.488f,
                    dy2 = 0.222f,
                    dx3 = 0.488f,
                    dy3 = 0.495f,
                )
                // c 0 0.82 0.657 1.483 1.468 1.483
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.82f,
                    dx2 = 0.657f,
                    dy2 = 1.483f,
                    dx3 = 1.468f,
                    dy3 = 1.483f,
                )
                // c 0.12 0 0.273 -0.055 0.46 -0.203
                curveToRelative(
                    dx1 = 0.12f,
                    dy1 = 0.0f,
                    dx2 = 0.273f,
                    dy2 = -0.055f,
                    dx3 = 0.46f,
                    dy3 = -0.203f,
                )
                // a 2.91 2.91 0 0 0 0.527 -0.581
                arcToRelative(
                    a = 2.91f,
                    b = 2.91f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.527f,
                    dy1 = -0.581f,
                )
                // a 5.724 5.724 0 0 0 0.516 -0.886
                arcToRelative(
                    a = 5.724f,
                    b = 5.724f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.516f,
                    dy1 = -0.886f,
                )
                // l 0.006 -0.013
                lineToRelative(dx = 0.006f, dy = -0.013f)
                // v -0.002
                verticalLineToRelative(dy = -0.002f)
                // l 0.447 -1.009
                lineToRelative(dx = 0.447f, dy = -1.009f)
                // l 0.446 1.008
                lineToRelative(dx = 0.446f, dy = 1.008f)
                // l 0.002 0.003
                lineToRelative(dx = 0.002f, dy = 0.003f)
                // l 0.006 0.013
                lineToRelative(dx = 0.006f, dy = 0.013f)
                // l 0.026 0.055
                lineToRelative(dx = 0.026f, dy = 0.055f)
                // a 5.724 5.724 0 0 0 0.49 0.83
                arcToRelative(
                    a = 5.724f,
                    b = 5.724f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.49f,
                    dy1 = 0.83f,
                )
                // c 0.162 0.228 0.342 0.435 0.527 0.582
                curveToRelative(
                    dx1 = 0.162f,
                    dy1 = 0.228f,
                    dx2 = 0.342f,
                    dy2 = 0.435f,
                    dx3 = 0.527f,
                    dy3 = 0.582f,
                )
                // c 0.187 0.148 0.34 0.203 0.46 0.203
                curveToRelative(
                    dx1 = 0.187f,
                    dy1 = 0.148f,
                    dx2 = 0.34f,
                    dy2 = 0.203f,
                    dx3 = 0.46f,
                    dy3 = 0.203f,
                )
                // s 0.273 -0.055 0.46 -0.203
                reflectiveCurveToRelative(
                    dx1 = 0.273f,
                    dy1 = -0.055f,
                    dx2 = 0.46f,
                    dy2 = -0.203f,
                )
                // a 2.91 2.91 0 0 0 0.527 -0.581
                arcToRelative(
                    a = 2.91f,
                    b = 2.91f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.527f,
                    dy1 = -0.581f,
                )
                // a 5.725 5.725 0 0 0 0.516 -0.886
                arcToRelative(
                    a = 5.725f,
                    b = 5.725f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.516f,
                    dy1 = -0.886f,
                )
                // l 0.006 -0.013
                lineToRelative(dx = 0.006f, dy = -0.013f)
                // v -0.002
                verticalLineToRelative(dy = -0.002f)
                // l 0.47 -1.06
                lineToRelative(dx = 0.47f, dy = -1.06f)
                // l 0.43 1.076
                lineToRelative(dx = 0.43f, dy = 1.076f)
                // l 0.002 0.004
                lineToRelative(dx = 0.002f, dy = 0.004f)
                // l 0.007 0.016
                lineToRelative(dx = 0.007f, dy = 0.016f)
                // a 5.083 5.083 0 0 0 0.141 0.312
                arcToRelative(
                    a = 5.083f,
                    b = 5.083f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.141f,
                    dy1 = 0.312f,
                )
                // c 0.099 0.204 0.238 0.465 0.402 0.711
                curveToRelative(
                    dx1 = 0.099f,
                    dy1 = 0.204f,
                    dx2 = 0.238f,
                    dy2 = 0.465f,
                    dx3 = 0.402f,
                    dy3 = 0.711f,
                )
                // c 0.169 0.253 0.343 0.456 0.505 0.573
                curveToRelative(
                    dx1 = 0.169f,
                    dy1 = 0.253f,
                    dx2 = 0.343f,
                    dy2 = 0.456f,
                    dx3 = 0.505f,
                    dy3 = 0.573f,
                )
                // c 0.156 0.113 0.23 0.102 0.273 0.086
                curveToRelative(
                    dx1 = 0.156f,
                    dy1 = 0.113f,
                    dx2 = 0.23f,
                    dy2 = 0.102f,
                    dx3 = 0.273f,
                    dy3 = 0.086f,
                )
                // a 0.586 0.586 0 0 0 0.22 -0.187
                arcToRelative(
                    a = 0.586f,
                    b = 0.586f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.22f,
                    dy1 = -0.187f,
                )
                // c 0.09 -0.111 0.173 -0.253 0.245 -0.402
                curveToRelative(
                    dx1 = 0.09f,
                    dy1 = -0.111f,
                    dx2 = 0.173f,
                    dy2 = -0.253f,
                    dx3 = 0.245f,
                    dy3 = -0.402f,
                )
                // a 3.756 3.756 0 0 0 0.213 -0.556
                arcToRelative(
                    a = 3.756f,
                    b = 3.756f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.213f,
                    dy1 = -0.556f,
                )
                // l 0.002 -0.007
                lineToRelative(dx = 0.002f, dy = -0.007f)
                // l 0.099 -0.364
                lineToRelative(dx = 0.099f, dy = -0.364f)
                // h 1.84
                horizontalLineToRelative(dx = 1.84f)
                // l -0.782 -0.594
                lineToRelative(dx = -0.782f, dy = -0.594f)
                // a 0.498 0.498 0 0 1 -0.098 -0.692z
                arcToRelative(
                    a = 0.498f,
                    b = 0.498f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.098f,
                    dy1 = -0.692f,
                )
                close()
                // m -4.96 -6.424
                moveToRelative(dx = -4.96f, dy = -6.424f)
                // c -0.032 -0.282 0.256 -0.524 0.623 -0.524
                curveToRelative(
                    dx1 = -0.032f,
                    dy1 = -0.282f,
                    dx2 = 0.256f,
                    dy2 = -0.524f,
                    dx3 = 0.623f,
                    dy3 = -0.524f,
                )
                // s 0.655 0.242 0.623 0.524
                reflectiveCurveToRelative(
                    dx1 = 0.655f,
                    dy1 = 0.242f,
                    dx2 = 0.623f,
                    dy2 = 0.524f,
                )
                // L 8.34 5.75
                lineTo(x = 8.34f, y = 5.75f)
                // h -0.68
                horizontalLineToRelative(dx = -0.68f)
                // l -0.282 -2.476z
                lineToRelative(dx = -0.282f, dy = -2.476f)
                close()
                // M 8.504 6.75
                moveTo(x = 8.504f, y = 6.75f)
                // a 0.5 0.5 0 1 1 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // a 0.5 0.5 0 0 1 1 0z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                close()
            }
            // M4 5 a4 4 0 1 0 8 0 4 4 0 0 0 -8 0Z m7.35 0 a3.35 3.35 0 1 1 -6.7 0 3.35 3.35 0 0 1 6.7 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4 5
                moveTo(x = 4.0f, y = 5.0f)
                // a 4 4 0 1 0 8 0
                arcToRelative(
                    a = 4.0f,
                    b = 4.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 8.0f,
                    dy1 = 0.0f,
                )
                // a 4 4 0 0 0 -8 0z
                arcToRelative(
                    a = 4.0f,
                    b = 4.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -8.0f,
                    dy1 = 0.0f,
                )
                close()
                // m 7.35 0
                moveToRelative(dx = 7.35f, dy = 0.0f)
                // a 3.35 3.35 0 1 1 -6.7 0
                arcToRelative(
                    a = 3.35f,
                    b = 3.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -6.7f,
                    dy1 = 0.0f,
                )
                // a 3.35 3.35 0 0 1 6.7 0z
                arcToRelative(
                    a = 3.35f,
                    b = 3.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 6.7f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic2420 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2420: ImageVector? = null
