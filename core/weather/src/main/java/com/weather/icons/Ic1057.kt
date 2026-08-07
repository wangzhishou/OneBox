package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1057: ImageVector
    get() {
        val current = _ic1057
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1057",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // m10.591 8.276 -3.254 .264 c-.364 .03 -.717 .13 -1.043 .297 l-1.266 .65 -.868 -2.415 c1.095 -.75 1.94 -1.436 2.276 -2.545 a.409 .409 0 0 0 -.424 -.525 .397 .397 0 0 0 -.348 .286 c-.307 1.009 -1.179 1.593 -2.498 2.475 l-.017 .011 c-.448 .3 -.937 .627 -1.457 1.01 C-.146 9.134 -.137 10.97 .126 11.75 a.405 .405 0 0 0 .51 .256 .406 .406 0 0 0 .256 -.514 C.871 11.427 .418 9.94 1.904 8.655 l1.329 2.385 c.221 .395 .603 .674 1.044 .762 l2.01 .404 c.368 .074 .7 .272 .94 .56 l1.746 2.098 a.378 .378 0 0 0 .38 .125 l.024 -.006 a.378 .378 0 0 0 .289 -.381 .384 .384 0 0 0 -.043 -.167 l-1.603 -3.11 a1.113 1.113 0 0 0 -.685 -.56 l-.662 -.186 .252 -.211 a2.74 2.74 0 0 1 .998 -.53 l2.803 -.813 a.38 .38 0 0 0 .268 -.436 .383 .383 0 0 0 -.403 -.313Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.591 8.276
                moveTo(x = 10.591f, y = 8.276f)
                // l -3.254 0.264
                lineToRelative(dx = -3.254f, dy = 0.264f)
                // c -0.364 0.03 -0.717 0.13 -1.043 0.297
                curveToRelative(
                    dx1 = -0.364f,
                    dy1 = 0.03f,
                    dx2 = -0.717f,
                    dy2 = 0.13f,
                    dx3 = -1.043f,
                    dy3 = 0.297f,
                )
                // l -1.266 0.65
                lineToRelative(dx = -1.266f, dy = 0.65f)
                // l -0.868 -2.415
                lineToRelative(dx = -0.868f, dy = -2.415f)
                // c 1.095 -0.75 1.94 -1.436 2.276 -2.545
                curveToRelative(
                    dx1 = 1.095f,
                    dy1 = -0.75f,
                    dx2 = 1.94f,
                    dy2 = -1.436f,
                    dx3 = 2.276f,
                    dy3 = -2.545f,
                )
                // a 0.409 0.409 0 0 0 -0.424 -0.525
                arcToRelative(
                    a = 0.409f,
                    b = 0.409f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.424f,
                    dy1 = -0.525f,
                )
                // a 0.397 0.397 0 0 0 -0.348 0.286
                arcToRelative(
                    a = 0.397f,
                    b = 0.397f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.348f,
                    dy1 = 0.286f,
                )
                // c -0.307 1.009 -1.179 1.593 -2.498 2.475
                curveToRelative(
                    dx1 = -0.307f,
                    dy1 = 1.009f,
                    dx2 = -1.179f,
                    dy2 = 1.593f,
                    dx3 = -2.498f,
                    dy3 = 2.475f,
                )
                // l -0.017 0.011
                lineToRelative(dx = -0.017f, dy = 0.011f)
                // c -0.448 0.3 -0.937 0.627 -1.457 1.01
                curveToRelative(
                    dx1 = -0.448f,
                    dy1 = 0.3f,
                    dx2 = -0.937f,
                    dy2 = 0.627f,
                    dx3 = -1.457f,
                    dy3 = 1.01f,
                )
                // C -0.146 9.134 -0.137 10.97 0.126 11.75
                curveTo(
                    x1 = -0.146f,
                    y1 = 9.134f,
                    x2 = -0.137f,
                    y2 = 10.97f,
                    x3 = 0.126f,
                    y3 = 11.75f,
                )
                // a 0.405 0.405 0 0 0 0.51 0.256
                arcToRelative(
                    a = 0.405f,
                    b = 0.405f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.51f,
                    dy1 = 0.256f,
                )
                // a 0.406 0.406 0 0 0 0.256 -0.514
                arcToRelative(
                    a = 0.406f,
                    b = 0.406f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.256f,
                    dy1 = -0.514f,
                )
                // C 0.871 11.427 0.418 9.94 1.904 8.655
                curveTo(
                    x1 = 0.871f,
                    y1 = 11.427f,
                    x2 = 0.418f,
                    y2 = 9.94f,
                    x3 = 1.904f,
                    y3 = 8.655f,
                )
                // l 1.329 2.385
                lineToRelative(dx = 1.329f, dy = 2.385f)
                // c 0.221 0.395 0.603 0.674 1.044 0.762
                curveToRelative(
                    dx1 = 0.221f,
                    dy1 = 0.395f,
                    dx2 = 0.603f,
                    dy2 = 0.674f,
                    dx3 = 1.044f,
                    dy3 = 0.762f,
                )
                // l 2.01 0.404
                lineToRelative(dx = 2.01f, dy = 0.404f)
                // c 0.368 0.074 0.7 0.272 0.94 0.56
                curveToRelative(
                    dx1 = 0.368f,
                    dy1 = 0.074f,
                    dx2 = 0.7f,
                    dy2 = 0.272f,
                    dx3 = 0.94f,
                    dy3 = 0.56f,
                )
                // l 1.746 2.098
                lineToRelative(dx = 1.746f, dy = 2.098f)
                // a 0.378 0.378 0 0 0 0.38 0.125
                arcToRelative(
                    a = 0.378f,
                    b = 0.378f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.38f,
                    dy1 = 0.125f,
                )
                // l 0.024 -0.006
                lineToRelative(dx = 0.024f, dy = -0.006f)
                // a 0.378 0.378 0 0 0 0.289 -0.381
                arcToRelative(
                    a = 0.378f,
                    b = 0.378f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.289f,
                    dy1 = -0.381f,
                )
                // a 0.384 0.384 0 0 0 -0.043 -0.167
                arcToRelative(
                    a = 0.384f,
                    b = 0.384f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.043f,
                    dy1 = -0.167f,
                )
                // l -1.603 -3.11
                lineToRelative(dx = -1.603f, dy = -3.11f)
                // a 1.113 1.113 0 0 0 -0.685 -0.56
                arcToRelative(
                    a = 1.113f,
                    b = 1.113f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.685f,
                    dy1 = -0.56f,
                )
                // l -0.662 -0.186
                lineToRelative(dx = -0.662f, dy = -0.186f)
                // l 0.252 -0.211
                lineToRelative(dx = 0.252f, dy = -0.211f)
                // a 2.74 2.74 0 0 1 0.998 -0.53
                arcToRelative(
                    a = 2.74f,
                    b = 2.74f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.998f,
                    dy1 = -0.53f,
                )
                // l 2.803 -0.813
                lineToRelative(dx = 2.803f, dy = -0.813f)
                // a 0.38 0.38 0 0 0 0.268 -0.436
                arcToRelative(
                    a = 0.38f,
                    b = 0.38f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.268f,
                    dy1 = -0.436f,
                )
                // a 0.383 0.383 0 0 0 -0.403 -0.313z
                arcToRelative(
                    a = 0.383f,
                    b = 0.383f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.403f,
                    dy1 = -0.313f,
                )
                close()
            }
            // M7.052 14.708 c-2.037 -.36 -3.766 -1.441 -5.352 -2.68 a.13 .13 0 0 0 -.18 .02 l-.488 .59 a.137 .137 0 0 0 -.03 .108 .139 .139 0 0 0 .059 .095 c1.733 1.14 3.816 1.709 5.985 1.91 l.006 -.043Z m-4.284 -.452 a.13 .13 0 0 0 -.094 -.003 .133 .133 0 0 0 -.072 .062 l-.254 .462 a.139 .139 0 0 0 .078 .197 A23.178 23.178 0 0 0 9 16 l-.002 -.057 c-1.967 -.106 -4.174 -.814 -6.23 -1.687Z m-.796 -7.492 a1 1 0 1 0 0 -2 1 1 0 0 0 0 2Z m9.563 -4.297 a.467 .467 0 0 1 .933 0 v.725 l.628 -.363 a.467 .467 0 0 1 .467 .808 L12.935 4 l.628 .363 a.467 .467 0 0 1 -.467 .808 l-.628 -.363 v.725 a.467 .467 0 1 1 -.933 0 v-.725 l-.628 .363 a.467 .467 0 1 1 -.467 -.808 L11.068 4 l-.628 -.363 a.467 .467 0 0 1 .467 -.808 l.628 .363 v-.725Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.052 14.708
                moveTo(x = 7.052f, y = 14.708f)
                // c -2.037 -0.36 -3.766 -1.441 -5.352 -2.68
                curveToRelative(
                    dx1 = -2.037f,
                    dy1 = -0.36f,
                    dx2 = -3.766f,
                    dy2 = -1.441f,
                    dx3 = -5.352f,
                    dy3 = -2.68f,
                )
                // a 0.13 0.13 0 0 0 -0.18 0.02
                arcToRelative(
                    a = 0.13f,
                    b = 0.13f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.18f,
                    dy1 = 0.02f,
                )
                // l -0.488 0.59
                lineToRelative(dx = -0.488f, dy = 0.59f)
                // a 0.137 0.137 0 0 0 -0.03 0.108
                arcToRelative(
                    a = 0.137f,
                    b = 0.137f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.03f,
                    dy1 = 0.108f,
                )
                // a 0.139 0.139 0 0 0 0.059 0.095
                arcToRelative(
                    a = 0.139f,
                    b = 0.139f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.059f,
                    dy1 = 0.095f,
                )
                // c 1.733 1.14 3.816 1.709 5.985 1.91
                curveToRelative(
                    dx1 = 1.733f,
                    dy1 = 1.14f,
                    dx2 = 3.816f,
                    dy2 = 1.709f,
                    dx3 = 5.985f,
                    dy3 = 1.91f,
                )
                // l 0.006 -0.043z
                lineToRelative(dx = 0.006f, dy = -0.043f)
                close()
                // m -4.284 -0.452
                moveToRelative(dx = -4.284f, dy = -0.452f)
                // a 0.13 0.13 0 0 0 -0.094 -0.003
                arcToRelative(
                    a = 0.13f,
                    b = 0.13f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.094f,
                    dy1 = -0.003f,
                )
                // a 0.133 0.133 0 0 0 -0.072 0.062
                arcToRelative(
                    a = 0.133f,
                    b = 0.133f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.072f,
                    dy1 = 0.062f,
                )
                // l -0.254 0.462
                lineToRelative(dx = -0.254f, dy = 0.462f)
                // a 0.139 0.139 0 0 0 0.078 0.197
                arcToRelative(
                    a = 0.139f,
                    b = 0.139f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.078f,
                    dy1 = 0.197f,
                )
                // A 23.178 23.178 0 0 0 9 16
                arcTo(
                    horizontalEllipseRadius = 23.178f,
                    verticalEllipseRadius = 23.178f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 9.0f,
                    y1 = 16.0f,
                )
                // l -0.002 -0.057
                lineToRelative(dx = -0.002f, dy = -0.057f)
                // c -1.967 -0.106 -4.174 -0.814 -6.23 -1.687z
                curveToRelative(
                    dx1 = -1.967f,
                    dy1 = -0.106f,
                    dx2 = -4.174f,
                    dy2 = -0.814f,
                    dx3 = -6.23f,
                    dy3 = -1.687f,
                )
                close()
                // m -0.796 -7.492
                moveToRelative(dx = -0.796f, dy = -7.492f)
                // a 1 1 0 1 0 0 -2
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -2.0f,
                )
                // a 1 1 0 0 0 0 2z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 2.0f,
                )
                close()
                // m 9.563 -4.297
                moveToRelative(dx = 9.563f, dy = -4.297f)
                // a 0.467 0.467 0 0 1 0.933 0
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.933f,
                    dy1 = 0.0f,
                )
                // v 0.725
                verticalLineToRelative(dy = 0.725f)
                // l 0.628 -0.363
                lineToRelative(dx = 0.628f, dy = -0.363f)
                // a 0.467 0.467 0 0 1 0.467 0.808
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.467f,
                    dy1 = 0.808f,
                )
                // L 12.935 4
                lineTo(x = 12.935f, y = 4.0f)
                // l 0.628 0.363
                lineToRelative(dx = 0.628f, dy = 0.363f)
                // a 0.467 0.467 0 0 1 -0.467 0.808
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.467f,
                    dy1 = 0.808f,
                )
                // l -0.628 -0.363
                lineToRelative(dx = -0.628f, dy = -0.363f)
                // v 0.725
                verticalLineToRelative(dy = 0.725f)
                // a 0.467 0.467 0 1 1 -0.933 0
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.933f,
                    dy1 = 0.0f,
                )
                // v -0.725
                verticalLineToRelative(dy = -0.725f)
                // l -0.628 0.363
                lineToRelative(dx = -0.628f, dy = 0.363f)
                // a 0.467 0.467 0 1 1 -0.467 -0.808
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.467f,
                    dy1 = -0.808f,
                )
                // L 11.068 4
                lineTo(x = 11.068f, y = 4.0f)
                // l -0.628 -0.363
                lineToRelative(dx = -0.628f, dy = -0.363f)
                // a 0.467 0.467 0 0 1 0.467 -0.808
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.467f,
                    dy1 = -0.808f,
                )
                // l 0.628 0.363
                lineToRelative(dx = 0.628f, dy = 0.363f)
                // v -0.725z
                verticalLineToRelative(dy = -0.725f)
                close()
            }
            // M15.325 1.872 12.175 .048 a.346 .346 0 0 0 -.35 0 l-3.15 1.824 a.353 .353 0 0 0 -.175 .304 v3.648 c0 .126 .067 .242 .175 .305 l3.15 1.824 a.344 .344 0 0 0 .35 0 l3.149 -1.824 a.351 .351 0 0 0 .176 -.305 V2.176 a.353 .353 0 0 0 -.175 -.304Z m-.526 3.75 L12 7.241 l-2.799 -1.62 V2.38 L12 .758 l2.799 1.621 v3.242Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 15.325 1.872
                moveTo(x = 15.325f, y = 1.872f)
                // L 12.175 0.048
                lineTo(x = 12.175f, y = 0.048f)
                // a 0.346 0.346 0 0 0 -0.35 0
                arcToRelative(
                    a = 0.346f,
                    b = 0.346f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = 0.0f,
                )
                // l -3.15 1.824
                lineToRelative(dx = -3.15f, dy = 1.824f)
                // a 0.353 0.353 0 0 0 -0.175 0.304
                arcToRelative(
                    a = 0.353f,
                    b = 0.353f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.175f,
                    dy1 = 0.304f,
                )
                // v 3.648
                verticalLineToRelative(dy = 3.648f)
                // c 0 0.126 0.067 0.242 0.175 0.305
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.126f,
                    dx2 = 0.067f,
                    dy2 = 0.242f,
                    dx3 = 0.175f,
                    dy3 = 0.305f,
                )
                // l 3.15 1.824
                lineToRelative(dx = 3.15f, dy = 1.824f)
                // a 0.344 0.344 0 0 0 0.35 0
                arcToRelative(
                    a = 0.344f,
                    b = 0.344f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.35f,
                    dy1 = 0.0f,
                )
                // l 3.149 -1.824
                lineToRelative(dx = 3.149f, dy = -1.824f)
                // a 0.351 0.351 0 0 0 0.176 -0.305
                arcToRelative(
                    a = 0.351f,
                    b = 0.351f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.176f,
                    dy1 = -0.305f,
                )
                // V 2.176
                verticalLineTo(y = 2.176f)
                // a 0.353 0.353 0 0 0 -0.175 -0.304z
                arcToRelative(
                    a = 0.353f,
                    b = 0.353f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.175f,
                    dy1 = -0.304f,
                )
                close()
                // m -0.526 3.75
                moveToRelative(dx = -0.526f, dy = 3.75f)
                // L 12 7.241
                lineTo(x = 12.0f, y = 7.241f)
                // l -2.799 -1.62
                lineToRelative(dx = -2.799f, dy = -1.62f)
                // V 2.38
                verticalLineTo(y = 2.38f)
                // L 12 0.758
                lineTo(x = 12.0f, y = 0.758f)
                // l 2.799 1.621
                lineToRelative(dx = 2.799f, dy = 1.621f)
                // v 3.242z
                verticalLineToRelative(dy = 3.242f)
                close()
            }
        }.build().also { _ic1057 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1057: ImageVector? = null
