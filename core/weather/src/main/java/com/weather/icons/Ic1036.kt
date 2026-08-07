package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1036: ImageVector
    get() {
        val current = _ic1036
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1036",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M3 10.76 c-.683 -.13 -1.336 -.353 -2.139 -.705 -.298 -.132 -.657 -.018 -.801 .253 -.144 .27 -.02 .597 .279 .728 1.034 .454 1.872 .72 2.794 .854 a5.438 5.438 0 0 0 1.331 2.548 C5.402 15.438 6.674 16 8 16 s2.598 -.562 3.536 -1.562 c.937 -1 1.464 -2.357 1.464 -3.771 C13 8.24 10.703 4.073 8.615 .91 A78.36 78.36 0 0 0 8 0 a61.87 61.87 0 0 0 -.615 .911 49.363 49.363 0 0 0 -2.39 3.999 c-1.648 .01 -2.627 -.194 -4.134 -.856 -.298 -.13 -.657 -.017 -.801 .254 -.144 .27 -.02 .597 .279 .728 1.496 .657 2.581 .92 4.105 .959 a22.42 22.42 0 0 0 -.805 1.858 c-.939 -.1 -1.73 -.339 -2.778 -.799 -.298 -.13 -.657 -.017 -.801 .254 -.144 .27 -.02 .597 .279 .728 1.092 .48 1.964 .749 2.948 .875 C3.104 9.557 3 10.15 3 10.667 v.092Z m1.005 .124 A4.554 4.554 0 0 1 4 10.667 c0 -.43 .101 -.992 .312 -1.676 .252 .009 .516 .01 .796 .008 1.249 -.015 1.993 -.128 3.18 -.482 .314 -.094 .486 -.402 .382 -.688 -.103 -.286 -.442 -.441 -.757 -.347 -1.07 .319 -1.694 .413 -2.82 .427 h-.394 a24.089 24.089 0 0 1 .88 -1.922 c.982 -.037 1.678 -.163 2.708 -.47 .315 -.094 .487 -.402 .383 -.688 -.103 -.286 -.442 -.441 -.757 -.347 -.677 .201 -1.175 .313 -1.734 .372 A49.378 49.378 0 0 1 8 1.835 c.86 1.32 1.736 2.791 2.453 4.213 .471 .936 .864 1.834 1.138 2.643 .277 .82 .409 1.486 .409 1.976 a4.415 4.415 0 0 1 -1.172 3.017 c-.75 .8 -1.767 1.25 -2.828 1.25 -1.06 0 -2.078 -.45 -2.828 -1.25 a4.324 4.324 0 0 1 -.976 -1.698 c.287 .013 .59 .016 .912 .013 1.249 -.015 1.993 -.128 3.18 -.482 .314 -.094 .486 -.402 .382 -.688 -.103 -.286 -.442 -.441 -.757 -.348 -1.07 .32 -1.694 .414 -2.82 .428 -.396 .004 -.754 -.003 -1.088 -.025Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 3 10.76
                moveTo(x = 3.0f, y = 10.76f)
                // c -0.683 -0.13 -1.336 -0.353 -2.139 -0.705
                curveToRelative(
                    dx1 = -0.683f,
                    dy1 = -0.13f,
                    dx2 = -1.336f,
                    dy2 = -0.353f,
                    dx3 = -2.139f,
                    dy3 = -0.705f,
                )
                // c -0.298 -0.132 -0.657 -0.018 -0.801 0.253
                curveToRelative(
                    dx1 = -0.298f,
                    dy1 = -0.132f,
                    dx2 = -0.657f,
                    dy2 = -0.018f,
                    dx3 = -0.801f,
                    dy3 = 0.253f,
                )
                // c -0.144 0.27 -0.02 0.597 0.279 0.728
                curveToRelative(
                    dx1 = -0.144f,
                    dy1 = 0.27f,
                    dx2 = -0.02f,
                    dy2 = 0.597f,
                    dx3 = 0.279f,
                    dy3 = 0.728f,
                )
                // c 1.034 0.454 1.872 0.72 2.794 0.854
                curveToRelative(
                    dx1 = 1.034f,
                    dy1 = 0.454f,
                    dx2 = 1.872f,
                    dy2 = 0.72f,
                    dx3 = 2.794f,
                    dy3 = 0.854f,
                )
                // a 5.438 5.438 0 0 0 1.331 2.548
                arcToRelative(
                    a = 5.438f,
                    b = 5.438f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.331f,
                    dy1 = 2.548f,
                )
                // C 5.402 15.438 6.674 16 8 16
                curveTo(
                    x1 = 5.402f,
                    y1 = 15.438f,
                    x2 = 6.674f,
                    y2 = 16.0f,
                    x3 = 8.0f,
                    y3 = 16.0f,
                )
                // s 2.598 -0.562 3.536 -1.562
                reflectiveCurveToRelative(
                    dx1 = 2.598f,
                    dy1 = -0.562f,
                    dx2 = 3.536f,
                    dy2 = -1.562f,
                )
                // c 0.937 -1 1.464 -2.357 1.464 -3.771
                curveToRelative(
                    dx1 = 0.937f,
                    dy1 = -1.0f,
                    dx2 = 1.464f,
                    dy2 = -2.357f,
                    dx3 = 1.464f,
                    dy3 = -3.771f,
                )
                // C 13 8.24 10.703 4.073 8.615 0.91
                curveTo(
                    x1 = 13.0f,
                    y1 = 8.24f,
                    x2 = 10.703f,
                    y2 = 4.073f,
                    x3 = 8.615f,
                    y3 = 0.91f,
                )
                // A 78.36 78.36 0 0 0 8 0
                arcTo(
                    horizontalEllipseRadius = 78.36f,
                    verticalEllipseRadius = 78.36f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 8.0f,
                    y1 = 0.0f,
                )
                // a 61.87 61.87 0 0 0 -0.615 0.911
                arcToRelative(
                    a = 61.87f,
                    b = 61.87f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.615f,
                    dy1 = 0.911f,
                )
                // a 49.363 49.363 0 0 0 -2.39 3.999
                arcToRelative(
                    a = 49.363f,
                    b = 49.363f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.39f,
                    dy1 = 3.999f,
                )
                // c -1.648 0.01 -2.627 -0.194 -4.134 -0.856
                curveToRelative(
                    dx1 = -1.648f,
                    dy1 = 0.01f,
                    dx2 = -2.627f,
                    dy2 = -0.194f,
                    dx3 = -4.134f,
                    dy3 = -0.856f,
                )
                // c -0.298 -0.13 -0.657 -0.017 -0.801 0.254
                curveToRelative(
                    dx1 = -0.298f,
                    dy1 = -0.13f,
                    dx2 = -0.657f,
                    dy2 = -0.017f,
                    dx3 = -0.801f,
                    dy3 = 0.254f,
                )
                // c -0.144 0.27 -0.02 0.597 0.279 0.728
                curveToRelative(
                    dx1 = -0.144f,
                    dy1 = 0.27f,
                    dx2 = -0.02f,
                    dy2 = 0.597f,
                    dx3 = 0.279f,
                    dy3 = 0.728f,
                )
                // c 1.496 0.657 2.581 0.92 4.105 0.959
                curveToRelative(
                    dx1 = 1.496f,
                    dy1 = 0.657f,
                    dx2 = 2.581f,
                    dy2 = 0.92f,
                    dx3 = 4.105f,
                    dy3 = 0.959f,
                )
                // a 22.42 22.42 0 0 0 -0.805 1.858
                arcToRelative(
                    a = 22.42f,
                    b = 22.42f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.805f,
                    dy1 = 1.858f,
                )
                // c -0.939 -0.1 -1.73 -0.339 -2.778 -0.799
                curveToRelative(
                    dx1 = -0.939f,
                    dy1 = -0.1f,
                    dx2 = -1.73f,
                    dy2 = -0.339f,
                    dx3 = -2.778f,
                    dy3 = -0.799f,
                )
                // c -0.298 -0.13 -0.657 -0.017 -0.801 0.254
                curveToRelative(
                    dx1 = -0.298f,
                    dy1 = -0.13f,
                    dx2 = -0.657f,
                    dy2 = -0.017f,
                    dx3 = -0.801f,
                    dy3 = 0.254f,
                )
                // c -0.144 0.27 -0.02 0.597 0.279 0.728
                curveToRelative(
                    dx1 = -0.144f,
                    dy1 = 0.27f,
                    dx2 = -0.02f,
                    dy2 = 0.597f,
                    dx3 = 0.279f,
                    dy3 = 0.728f,
                )
                // c 1.092 0.48 1.964 0.749 2.948 0.875
                curveToRelative(
                    dx1 = 1.092f,
                    dy1 = 0.48f,
                    dx2 = 1.964f,
                    dy2 = 0.749f,
                    dx3 = 2.948f,
                    dy3 = 0.875f,
                )
                // C 3.104 9.557 3 10.15 3 10.667
                curveTo(
                    x1 = 3.104f,
                    y1 = 9.557f,
                    x2 = 3.0f,
                    y2 = 10.15f,
                    x3 = 3.0f,
                    y3 = 10.667f,
                )
                // v 0.092z
                verticalLineToRelative(dy = 0.092f)
                close()
                // m 1.005 0.124
                moveToRelative(dx = 1.005f, dy = 0.124f)
                // A 4.554 4.554 0 0 1 4 10.667
                arcTo(
                    horizontalEllipseRadius = 4.554f,
                    verticalEllipseRadius = 4.554f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 4.0f,
                    y1 = 10.667f,
                )
                // c 0 -0.43 0.101 -0.992 0.312 -1.676
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.43f,
                    dx2 = 0.101f,
                    dy2 = -0.992f,
                    dx3 = 0.312f,
                    dy3 = -1.676f,
                )
                // c 0.252 0.009 0.516 0.01 0.796 0.008
                curveToRelative(
                    dx1 = 0.252f,
                    dy1 = 0.009f,
                    dx2 = 0.516f,
                    dy2 = 0.01f,
                    dx3 = 0.796f,
                    dy3 = 0.008f,
                )
                // c 1.249 -0.015 1.993 -0.128 3.18 -0.482
                curveToRelative(
                    dx1 = 1.249f,
                    dy1 = -0.015f,
                    dx2 = 1.993f,
                    dy2 = -0.128f,
                    dx3 = 3.18f,
                    dy3 = -0.482f,
                )
                // c 0.314 -0.094 0.486 -0.402 0.382 -0.688
                curveToRelative(
                    dx1 = 0.314f,
                    dy1 = -0.094f,
                    dx2 = 0.486f,
                    dy2 = -0.402f,
                    dx3 = 0.382f,
                    dy3 = -0.688f,
                )
                // c -0.103 -0.286 -0.442 -0.441 -0.757 -0.347
                curveToRelative(
                    dx1 = -0.103f,
                    dy1 = -0.286f,
                    dx2 = -0.442f,
                    dy2 = -0.441f,
                    dx3 = -0.757f,
                    dy3 = -0.347f,
                )
                // c -1.07 0.319 -1.694 0.413 -2.82 0.427
                curveToRelative(
                    dx1 = -1.07f,
                    dy1 = 0.319f,
                    dx2 = -1.694f,
                    dy2 = 0.413f,
                    dx3 = -2.82f,
                    dy3 = 0.427f,
                )
                // h -0.394
                horizontalLineToRelative(dx = -0.394f)
                // a 24.089 24.089 0 0 1 0.88 -1.922
                arcToRelative(
                    a = 24.089f,
                    b = 24.089f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.88f,
                    dy1 = -1.922f,
                )
                // c 0.982 -0.037 1.678 -0.163 2.708 -0.47
                curveToRelative(
                    dx1 = 0.982f,
                    dy1 = -0.037f,
                    dx2 = 1.678f,
                    dy2 = -0.163f,
                    dx3 = 2.708f,
                    dy3 = -0.47f,
                )
                // c 0.315 -0.094 0.487 -0.402 0.383 -0.688
                curveToRelative(
                    dx1 = 0.315f,
                    dy1 = -0.094f,
                    dx2 = 0.487f,
                    dy2 = -0.402f,
                    dx3 = 0.383f,
                    dy3 = -0.688f,
                )
                // c -0.103 -0.286 -0.442 -0.441 -0.757 -0.347
                curveToRelative(
                    dx1 = -0.103f,
                    dy1 = -0.286f,
                    dx2 = -0.442f,
                    dy2 = -0.441f,
                    dx3 = -0.757f,
                    dy3 = -0.347f,
                )
                // c -0.677 0.201 -1.175 0.313 -1.734 0.372
                curveToRelative(
                    dx1 = -0.677f,
                    dy1 = 0.201f,
                    dx2 = -1.175f,
                    dy2 = 0.313f,
                    dx3 = -1.734f,
                    dy3 = 0.372f,
                )
                // A 49.378 49.378 0 0 1 8 1.835
                arcTo(
                    horizontalEllipseRadius = 49.378f,
                    verticalEllipseRadius = 49.378f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 8.0f,
                    y1 = 1.835f,
                )
                // c 0.86 1.32 1.736 2.791 2.453 4.213
                curveToRelative(
                    dx1 = 0.86f,
                    dy1 = 1.32f,
                    dx2 = 1.736f,
                    dy2 = 2.791f,
                    dx3 = 2.453f,
                    dy3 = 4.213f,
                )
                // c 0.471 0.936 0.864 1.834 1.138 2.643
                curveToRelative(
                    dx1 = 0.471f,
                    dy1 = 0.936f,
                    dx2 = 0.864f,
                    dy2 = 1.834f,
                    dx3 = 1.138f,
                    dy3 = 2.643f,
                )
                // c 0.277 0.82 0.409 1.486 0.409 1.976
                curveToRelative(
                    dx1 = 0.277f,
                    dy1 = 0.82f,
                    dx2 = 0.409f,
                    dy2 = 1.486f,
                    dx3 = 0.409f,
                    dy3 = 1.976f,
                )
                // a 4.415 4.415 0 0 1 -1.172 3.017
                arcToRelative(
                    a = 4.415f,
                    b = 4.415f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.172f,
                    dy1 = 3.017f,
                )
                // c -0.75 0.8 -1.767 1.25 -2.828 1.25
                curveToRelative(
                    dx1 = -0.75f,
                    dy1 = 0.8f,
                    dx2 = -1.767f,
                    dy2 = 1.25f,
                    dx3 = -2.828f,
                    dy3 = 1.25f,
                )
                // c -1.06 0 -2.078 -0.45 -2.828 -1.25
                curveToRelative(
                    dx1 = -1.06f,
                    dy1 = 0.0f,
                    dx2 = -2.078f,
                    dy2 = -0.45f,
                    dx3 = -2.828f,
                    dy3 = -1.25f,
                )
                // a 4.324 4.324 0 0 1 -0.976 -1.698
                arcToRelative(
                    a = 4.324f,
                    b = 4.324f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.976f,
                    dy1 = -1.698f,
                )
                // c 0.287 0.013 0.59 0.016 0.912 0.013
                curveToRelative(
                    dx1 = 0.287f,
                    dy1 = 0.013f,
                    dx2 = 0.59f,
                    dy2 = 0.016f,
                    dx3 = 0.912f,
                    dy3 = 0.013f,
                )
                // c 1.249 -0.015 1.993 -0.128 3.18 -0.482
                curveToRelative(
                    dx1 = 1.249f,
                    dy1 = -0.015f,
                    dx2 = 1.993f,
                    dy2 = -0.128f,
                    dx3 = 3.18f,
                    dy3 = -0.482f,
                )
                // c 0.314 -0.094 0.486 -0.402 0.382 -0.688
                curveToRelative(
                    dx1 = 0.314f,
                    dy1 = -0.094f,
                    dx2 = 0.486f,
                    dy2 = -0.402f,
                    dx3 = 0.382f,
                    dy3 = -0.688f,
                )
                // c -0.103 -0.286 -0.442 -0.441 -0.757 -0.348
                curveToRelative(
                    dx1 = -0.103f,
                    dy1 = -0.286f,
                    dx2 = -0.442f,
                    dy2 = -0.441f,
                    dx3 = -0.757f,
                    dy3 = -0.348f,
                )
                // c -1.07 0.32 -1.694 0.414 -2.82 0.428
                curveToRelative(
                    dx1 = -1.07f,
                    dy1 = 0.32f,
                    dx2 = -1.694f,
                    dy2 = 0.414f,
                    dx3 = -2.82f,
                    dy3 = 0.428f,
                )
                // c -0.396 0.004 -0.754 -0.003 -1.088 -0.025z
                curveToRelative(
                    dx1 = -0.396f,
                    dy1 = 0.004f,
                    dx2 = -0.754f,
                    dy2 = -0.003f,
                    dx3 = -1.088f,
                    dy3 = -0.025f,
                )
                close()
            }
            // M11.052 4.996 c1.675 -.025 2.681 .09 4.375 .488 a.47 .47 0 0 0 .562 -.376 .497 .497 0 0 0 -.357 -.592 c-1.84 -.433 -2.934 -.549 -4.818 -.509 a8.9 8.9 0 0 0 -.295 .012 c.183 .325 .361 .651 .533 .977Z m1.369 3.019 c.975 .051 1.823 .191 3.006 .47 a.47 .47 0 0 0 .562 -.377 .497 .497 0 0 0 -.357 -.592 c-1.412 -.332 -2.385 -.478 -3.612 -.51 .148 .344 .282 .682 .402 1.01Z m3.006 3.47 c-.95 -.224 -1.684 -.358 -2.44 -.429 a4.745 4.745 0 0 0 -.029 -.999 c.838 .072 1.637 .215 2.674 .458 .253 .06 .413 .325 .357 .593 a.47 .47 0 0 1 -.562 .377Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.052 4.996
                moveTo(x = 11.052f, y = 4.996f)
                // c 1.675 -0.025 2.681 0.09 4.375 0.488
                curveToRelative(
                    dx1 = 1.675f,
                    dy1 = -0.025f,
                    dx2 = 2.681f,
                    dy2 = 0.09f,
                    dx3 = 4.375f,
                    dy3 = 0.488f,
                )
                // a 0.47 0.47 0 0 0 0.562 -0.376
                arcToRelative(
                    a = 0.47f,
                    b = 0.47f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.562f,
                    dy1 = -0.376f,
                )
                // a 0.497 0.497 0 0 0 -0.357 -0.592
                arcToRelative(
                    a = 0.497f,
                    b = 0.497f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.357f,
                    dy1 = -0.592f,
                )
                // c -1.84 -0.433 -2.934 -0.549 -4.818 -0.509
                curveToRelative(
                    dx1 = -1.84f,
                    dy1 = -0.433f,
                    dx2 = -2.934f,
                    dy2 = -0.549f,
                    dx3 = -4.818f,
                    dy3 = -0.509f,
                )
                // a 8.9 8.9 0 0 0 -0.295 0.012
                arcToRelative(
                    a = 8.9f,
                    b = 8.9f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.295f,
                    dy1 = 0.012f,
                )
                // c 0.183 0.325 0.361 0.651 0.533 0.977z
                curveToRelative(
                    dx1 = 0.183f,
                    dy1 = 0.325f,
                    dx2 = 0.361f,
                    dy2 = 0.651f,
                    dx3 = 0.533f,
                    dy3 = 0.977f,
                )
                close()
                // m 1.369 3.019
                moveToRelative(dx = 1.369f, dy = 3.019f)
                // c 0.975 0.051 1.823 0.191 3.006 0.47
                curveToRelative(
                    dx1 = 0.975f,
                    dy1 = 0.051f,
                    dx2 = 1.823f,
                    dy2 = 0.191f,
                    dx3 = 3.006f,
                    dy3 = 0.47f,
                )
                // a 0.47 0.47 0 0 0 0.562 -0.377
                arcToRelative(
                    a = 0.47f,
                    b = 0.47f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.562f,
                    dy1 = -0.377f,
                )
                // a 0.497 0.497 0 0 0 -0.357 -0.592
                arcToRelative(
                    a = 0.497f,
                    b = 0.497f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.357f,
                    dy1 = -0.592f,
                )
                // c -1.412 -0.332 -2.385 -0.478 -3.612 -0.51
                curveToRelative(
                    dx1 = -1.412f,
                    dy1 = -0.332f,
                    dx2 = -2.385f,
                    dy2 = -0.478f,
                    dx3 = -3.612f,
                    dy3 = -0.51f,
                )
                // c 0.148 0.344 0.282 0.682 0.402 1.01z
                curveToRelative(
                    dx1 = 0.148f,
                    dy1 = 0.344f,
                    dx2 = 0.282f,
                    dy2 = 0.682f,
                    dx3 = 0.402f,
                    dy3 = 1.01f,
                )
                close()
                // m 3.006 3.47
                moveToRelative(dx = 3.006f, dy = 3.47f)
                // c -0.95 -0.224 -1.684 -0.358 -2.44 -0.429
                curveToRelative(
                    dx1 = -0.95f,
                    dy1 = -0.224f,
                    dx2 = -1.684f,
                    dy2 = -0.358f,
                    dx3 = -2.44f,
                    dy3 = -0.429f,
                )
                // a 4.745 4.745 0 0 0 -0.029 -0.999
                arcToRelative(
                    a = 4.745f,
                    b = 4.745f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.029f,
                    dy1 = -0.999f,
                )
                // c 0.838 0.072 1.637 0.215 2.674 0.458
                curveToRelative(
                    dx1 = 0.838f,
                    dy1 = 0.072f,
                    dx2 = 1.637f,
                    dy2 = 0.215f,
                    dx3 = 2.674f,
                    dy3 = 0.458f,
                )
                // c 0.253 0.06 0.413 0.325 0.357 0.593
                curveToRelative(
                    dx1 = 0.253f,
                    dy1 = 0.06f,
                    dx2 = 0.413f,
                    dy2 = 0.325f,
                    dx3 = 0.357f,
                    dy3 = 0.593f,
                )
                // a 0.47 0.47 0 0 1 -0.562 0.377z
                arcToRelative(
                    a = 0.47f,
                    b = 0.47f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.562f,
                    dy1 = 0.377f,
                )
                close()
            }
        }.build().also { _ic1036 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1036: ImageVector? = null
