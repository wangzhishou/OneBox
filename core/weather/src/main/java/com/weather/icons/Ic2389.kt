package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2389: ImageVector
    get() {
        val current = _ic2389
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2389",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.253 8.497 c-.039 -.401 .307 -.747 .747 -.747 s.786 .346 .747 .747 l-.338 3.528 h-.818 l-.338 -3.528Z m1.309 4.691 a.562 .562 0 1 1 -1.124 0 .562 .562 0 0 1 1.124 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.253 8.497
                moveTo(x = 7.253f, y = 8.497f)
                // c -0.039 -0.401 0.307 -0.747 0.747 -0.747
                curveToRelative(
                    dx1 = -0.039f,
                    dy1 = -0.401f,
                    dx2 = 0.307f,
                    dy2 = -0.747f,
                    dx3 = 0.747f,
                    dy3 = -0.747f,
                )
                // s 0.786 0.346 0.747 0.747
                reflectiveCurveToRelative(
                    dx1 = 0.786f,
                    dy1 = 0.346f,
                    dx2 = 0.747f,
                    dy2 = 0.747f,
                )
                // l -0.338 3.528
                lineToRelative(dx = -0.338f, dy = 3.528f)
                // h -0.818
                horizontalLineToRelative(dx = -0.818f)
                // l -0.338 -3.528z
                lineToRelative(dx = -0.338f, dy = -3.528f)
                close()
                // m 1.309 4.691
                moveToRelative(dx = 1.309f, dy = 4.691f)
                // a 0.562 0.562 0 1 1 -1.124 0
                arcToRelative(
                    a = 0.562f,
                    b = 0.562f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.124f,
                    dy1 = 0.0f,
                )
                // a 0.562 0.562 0 0 1 1.124 0z
                arcToRelative(
                    a = 0.562f,
                    b = 0.562f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.124f,
                    dy1 = 0.0f,
                )
                close()
            }
            // M7.659 4.946 a.395 .395 0 0 1 .682 0 l5.607 9.722 a.39 .39 0 0 1 -.341 .582 H2.393 a.39 .39 0 0 1 -.34 -.582 l5.606 -9.722Z m5.066 9.404 L8 6.156 3.275 14.35 h9.45Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.659 4.946
                moveTo(x = 7.659f, y = 4.946f)
                // a 0.395 0.395 0 0 1 0.682 0
                arcToRelative(
                    a = 0.395f,
                    b = 0.395f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.682f,
                    dy1 = 0.0f,
                )
                // l 5.607 9.722
                lineToRelative(dx = 5.607f, dy = 9.722f)
                // a 0.39 0.39 0 0 1 -0.341 0.582
                arcToRelative(
                    a = 0.39f,
                    b = 0.39f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.341f,
                    dy1 = 0.582f,
                )
                // H 2.393
                horizontalLineTo(x = 2.393f)
                // a 0.39 0.39 0 0 1 -0.34 -0.582
                arcToRelative(
                    a = 0.39f,
                    b = 0.39f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.34f,
                    dy1 = -0.582f,
                )
                // l 5.606 -9.722z
                lineToRelative(dx = 5.606f, dy = -9.722f)
                close()
                // m 5.066 9.404
                moveToRelative(dx = 5.066f, dy = 9.404f)
                // L 8 6.156
                lineTo(x = 8.0f, y = 6.156f)
                // L 3.275 14.35
                lineTo(x = 3.275f, y = 14.35f)
                // h 9.45z
                horizontalLineToRelative(dx = 9.45f)
                close()
            }
            // M15.317 9.016 a2.75 2.75 0 0 1 -2.206 2.684 l-.435 -.597 a2.095 2.095 0 0 0 1.976 -2.08 c0 -.693 -.343 -1.307 -.865 -1.686 a2.076 2.076 0 0 0 1.148 -1.856 2.094 2.094 0 0 0 -2.103 -2.084 c-.246 0 -.482 .042 -.702 .118 .04 -.165 .06 -.34 .06 -.517 A2.349 2.349 0 0 0 9.83 .66 a2.36 2.36 0 0 0 -2.21 1.526 2.109 2.109 0 0 0 -1.714 -.877 2.094 2.094 0 0 0 -2.102 2.084 c0 .178 .023 .353 .066 .518 a2.114 2.114 0 0 0 -1.108 -.313 A2.094 2.094 0 0 0 .66 5.682 c0 .877 .549 1.629 1.324 1.936 a2.064 2.064 0 0 0 -.456 1.296 c0 1.088 .838 1.981 1.91 2.077 l-.296 .62 A2.751 2.751 0 0 1 .868 8.914 c0 -.363 .074 -.723 .213 -1.056 A2.727 2.727 0 0 1 0 5.682 c0 -1.51 1.241 -2.74 2.768 -2.74 .136 0 .276 .01 .413 .03 A2.76 2.76 0 0 1 5.912 .65 c.562 0 1.105 .171 1.56 .481 A3.042 3.042 0 0 1 9.834 0 a3.015 3.015 0 0 1 3.008 2.737 c1.52 .003 2.758 1.23 2.758 2.74 0 .739 -.296 1.432 -.808 1.936 .336 .462 .525 1.022 .525 1.603Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 15.317 9.016
                moveTo(x = 15.317f, y = 9.016f)
                // a 2.75 2.75 0 0 1 -2.206 2.684
                arcToRelative(
                    a = 2.75f,
                    b = 2.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.206f,
                    dy1 = 2.684f,
                )
                // l -0.435 -0.597
                lineToRelative(dx = -0.435f, dy = -0.597f)
                // a 2.095 2.095 0 0 0 1.976 -2.08
                arcToRelative(
                    a = 2.095f,
                    b = 2.095f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.976f,
                    dy1 = -2.08f,
                )
                // c 0 -0.693 -0.343 -1.307 -0.865 -1.686
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.693f,
                    dx2 = -0.343f,
                    dy2 = -1.307f,
                    dx3 = -0.865f,
                    dy3 = -1.686f,
                )
                // a 2.076 2.076 0 0 0 1.148 -1.856
                arcToRelative(
                    a = 2.076f,
                    b = 2.076f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.148f,
                    dy1 = -1.856f,
                )
                // a 2.094 2.094 0 0 0 -2.103 -2.084
                arcToRelative(
                    a = 2.094f,
                    b = 2.094f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.103f,
                    dy1 = -2.084f,
                )
                // c -0.246 0 -0.482 0.042 -0.702 0.118
                curveToRelative(
                    dx1 = -0.246f,
                    dy1 = 0.0f,
                    dx2 = -0.482f,
                    dy2 = 0.042f,
                    dx3 = -0.702f,
                    dy3 = 0.118f,
                )
                // c 0.04 -0.165 0.06 -0.34 0.06 -0.517
                curveToRelative(
                    dx1 = 0.04f,
                    dy1 = -0.165f,
                    dx2 = 0.06f,
                    dy2 = -0.34f,
                    dx3 = 0.06f,
                    dy3 = -0.517f,
                )
                // A 2.349 2.349 0 0 0 9.83 0.66
                arcTo(
                    horizontalEllipseRadius = 2.349f,
                    verticalEllipseRadius = 2.349f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 9.83f,
                    y1 = 0.66f,
                )
                // a 2.36 2.36 0 0 0 -2.21 1.526
                arcToRelative(
                    a = 2.36f,
                    b = 2.36f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.21f,
                    dy1 = 1.526f,
                )
                // a 2.109 2.109 0 0 0 -1.714 -0.877
                arcToRelative(
                    a = 2.109f,
                    b = 2.109f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.714f,
                    dy1 = -0.877f,
                )
                // a 2.094 2.094 0 0 0 -2.102 2.084
                arcToRelative(
                    a = 2.094f,
                    b = 2.094f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.102f,
                    dy1 = 2.084f,
                )
                // c 0 0.178 0.023 0.353 0.066 0.518
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.178f,
                    dx2 = 0.023f,
                    dy2 = 0.353f,
                    dx3 = 0.066f,
                    dy3 = 0.518f,
                )
                // a 2.114 2.114 0 0 0 -1.108 -0.313
                arcToRelative(
                    a = 2.114f,
                    b = 2.114f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.108f,
                    dy1 = -0.313f,
                )
                // A 2.094 2.094 0 0 0 0.66 5.682
                arcTo(
                    horizontalEllipseRadius = 2.094f,
                    verticalEllipseRadius = 2.094f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 0.66f,
                    y1 = 5.682f,
                )
                // c 0 0.877 0.549 1.629 1.324 1.936
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.877f,
                    dx2 = 0.549f,
                    dy2 = 1.629f,
                    dx3 = 1.324f,
                    dy3 = 1.936f,
                )
                // a 2.064 2.064 0 0 0 -0.456 1.296
                arcToRelative(
                    a = 2.064f,
                    b = 2.064f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.456f,
                    dy1 = 1.296f,
                )
                // c 0 1.088 0.838 1.981 1.91 2.077
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.088f,
                    dx2 = 0.838f,
                    dy2 = 1.981f,
                    dx3 = 1.91f,
                    dy3 = 2.077f,
                )
                // l -0.296 0.62
                lineToRelative(dx = -0.296f, dy = 0.62f)
                // A 2.751 2.751 0 0 1 0.868 8.914
                arcTo(
                    horizontalEllipseRadius = 2.751f,
                    verticalEllipseRadius = 2.751f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 0.868f,
                    y1 = 8.914f,
                )
                // c 0 -0.363 0.074 -0.723 0.213 -1.056
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.363f,
                    dx2 = 0.074f,
                    dy2 = -0.723f,
                    dx3 = 0.213f,
                    dy3 = -1.056f,
                )
                // A 2.727 2.727 0 0 1 0 5.682
                arcTo(
                    horizontalEllipseRadius = 2.727f,
                    verticalEllipseRadius = 2.727f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 0.0f,
                    y1 = 5.682f,
                )
                // c 0 -1.51 1.241 -2.74 2.768 -2.74
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.51f,
                    dx2 = 1.241f,
                    dy2 = -2.74f,
                    dx3 = 2.768f,
                    dy3 = -2.74f,
                )
                // c 0.136 0 0.276 0.01 0.413 0.03
                curveToRelative(
                    dx1 = 0.136f,
                    dy1 = 0.0f,
                    dx2 = 0.276f,
                    dy2 = 0.01f,
                    dx3 = 0.413f,
                    dy3 = 0.03f,
                )
                // A 2.76 2.76 0 0 1 5.912 0.65
                arcTo(
                    horizontalEllipseRadius = 2.76f,
                    verticalEllipseRadius = 2.76f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 5.912f,
                    y1 = 0.65f,
                )
                // c 0.562 0 1.105 0.171 1.56 0.481
                curveToRelative(
                    dx1 = 0.562f,
                    dy1 = 0.0f,
                    dx2 = 1.105f,
                    dy2 = 0.171f,
                    dx3 = 1.56f,
                    dy3 = 0.481f,
                )
                // A 3.042 3.042 0 0 1 9.834 0
                arcTo(
                    horizontalEllipseRadius = 3.042f,
                    verticalEllipseRadius = 3.042f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 9.834f,
                    y1 = 0.0f,
                )
                // a 3.015 3.015 0 0 1 3.008 2.737
                arcToRelative(
                    a = 3.015f,
                    b = 3.015f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 3.008f,
                    dy1 = 2.737f,
                )
                // c 1.52 0.003 2.758 1.23 2.758 2.74
                curveToRelative(
                    dx1 = 1.52f,
                    dy1 = 0.003f,
                    dx2 = 2.758f,
                    dy2 = 1.23f,
                    dx3 = 2.758f,
                    dy3 = 2.74f,
                )
                // c 0 0.739 -0.296 1.432 -0.808 1.936
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.739f,
                    dx2 = -0.296f,
                    dy2 = 1.432f,
                    dx3 = -0.808f,
                    dy3 = 1.936f,
                )
                // c 0.336 0.462 0.525 1.022 0.525 1.603z
                curveToRelative(
                    dx1 = 0.336f,
                    dy1 = 0.462f,
                    dx2 = 0.525f,
                    dy2 = 1.022f,
                    dx3 = 0.525f,
                    dy3 = 1.603f,
                )
                close()
            }
            // M3.548 4.276 c-.518 -.1 -1.057 .095 -1.419 .546 v.002 c-.34 .43 -.449 .992 -.336 1.502 .069 .315 .22 .615 .462 .852 a.25 .25 0 0 0 .35 -.356 1.198 1.198 0 0 1 -.324 -.603 1.308 1.308 0 0 1 .239 -1.085 c.25 -.312 .605 -.43 .933 -.368 a.25 .25 0 1 0 .095 -.49Z m7.376 -2.483 c.577 .4 .875 1.054 .84 1.709 a.25 .25 0 1 1 -.499 -.026 1.453 1.453 0 0 0 -.625 -1.272 1.477 1.477 0 0 0 -2.044 .367 .25 .25 0 0 1 -.411 -.284 1.977 1.977 0 0 1 2.738 -.495 h.001Z M11.5 5 a.5 .5 0 1 0 0 -1 .5 .5 0 0 0 0 1Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 3.548 4.276
                moveTo(x = 3.548f, y = 4.276f)
                // c -0.518 -0.1 -1.057 0.095 -1.419 0.546
                curveToRelative(
                    dx1 = -0.518f,
                    dy1 = -0.1f,
                    dx2 = -1.057f,
                    dy2 = 0.095f,
                    dx3 = -1.419f,
                    dy3 = 0.546f,
                )
                // v 0.002
                verticalLineToRelative(dy = 0.002f)
                // c -0.34 0.43 -0.449 0.992 -0.336 1.502
                curveToRelative(
                    dx1 = -0.34f,
                    dy1 = 0.43f,
                    dx2 = -0.449f,
                    dy2 = 0.992f,
                    dx3 = -0.336f,
                    dy3 = 1.502f,
                )
                // c 0.069 0.315 0.22 0.615 0.462 0.852
                curveToRelative(
                    dx1 = 0.069f,
                    dy1 = 0.315f,
                    dx2 = 0.22f,
                    dy2 = 0.615f,
                    dx3 = 0.462f,
                    dy3 = 0.852f,
                )
                // a 0.25 0.25 0 0 0 0.35 -0.356
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.35f,
                    dy1 = -0.356f,
                )
                // a 1.198 1.198 0 0 1 -0.324 -0.603
                arcToRelative(
                    a = 1.198f,
                    b = 1.198f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.324f,
                    dy1 = -0.603f,
                )
                // a 1.308 1.308 0 0 1 0.239 -1.085
                arcToRelative(
                    a = 1.308f,
                    b = 1.308f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.239f,
                    dy1 = -1.085f,
                )
                // c 0.25 -0.312 0.605 -0.43 0.933 -0.368
                curveToRelative(
                    dx1 = 0.25f,
                    dy1 = -0.312f,
                    dx2 = 0.605f,
                    dy2 = -0.43f,
                    dx3 = 0.933f,
                    dy3 = -0.368f,
                )
                // a 0.25 0.25 0 1 0 0.095 -0.49z
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.095f,
                    dy1 = -0.49f,
                )
                close()
                // m 7.376 -2.483
                moveToRelative(dx = 7.376f, dy = -2.483f)
                // c 0.577 0.4 0.875 1.054 0.84 1.709
                curveToRelative(
                    dx1 = 0.577f,
                    dy1 = 0.4f,
                    dx2 = 0.875f,
                    dy2 = 1.054f,
                    dx3 = 0.84f,
                    dy3 = 1.709f,
                )
                // a 0.25 0.25 0 1 1 -0.499 -0.026
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.499f,
                    dy1 = -0.026f,
                )
                // a 1.453 1.453 0 0 0 -0.625 -1.272
                arcToRelative(
                    a = 1.453f,
                    b = 1.453f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.625f,
                    dy1 = -1.272f,
                )
                // a 1.477 1.477 0 0 0 -2.044 0.367
                arcToRelative(
                    a = 1.477f,
                    b = 1.477f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.044f,
                    dy1 = 0.367f,
                )
                // a 0.25 0.25 0 0 1 -0.411 -0.284
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.411f,
                    dy1 = -0.284f,
                )
                // a 1.977 1.977 0 0 1 2.738 -0.495
                arcToRelative(
                    a = 1.977f,
                    b = 1.977f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.738f,
                    dy1 = -0.495f,
                )
                // h 0.001z
                horizontalLineToRelative(dx = 0.001f)
                close()
                // M 11.5 5
                moveTo(x = 11.5f, y = 5.0f)
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
            }
        }.build().also { _ic2389 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2389: ImageVector? = null
