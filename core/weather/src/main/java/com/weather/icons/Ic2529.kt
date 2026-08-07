package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2529: ImageVector
    get() {
        val current = _ic2529
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2529",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M4.1 15 a.5 .5 0 1 1 0 1 .5 .5 0 0 1 0 -1Z M12.807 .76 c.995 0 1.843 .722 2.017 1.718 .025 .174 .025 .349 0 .523 v5.553 c0 .124 0 .248 .025 .373 0 .05 .025 .099 .075 .124 1.32 1.17 1.444 3.188 .273 4.532 a3.186 3.186 0 0 1 -1.693 .996 l-.448 .074 -.548 .026 c-.2 -.05 -.398 -.075 -.572 -.125 a3.227 3.227 0 0 1 -2.341 -3.113 c0 -.896 .373 -1.743 1.046 -2.34 .1 -.075 .149 -.2 .149 -.35 V6.139 c0 -1.12 -.024 -2.24 -.024 -3.336 0 -1.12 .92 -2.042 2.04 -2.042Z m-1.245 8.39 a.533 .533 0 0 1 -.199 .424 2.313 2.313 0 0 0 -.946 1.793 2.414 2.414 0 0 0 2.29 2.515 c.174 0 .349 0 .498 -.026 a2.28 2.28 0 0 0 1.918 -1.767 2.354 2.354 0 0 0 -.872 -2.49 .42 .42 0 0 1 -.2 -.423 V7.98 c-.248 .075 -.473 .15 -.697 .2 l-.149 -.014 v1.906 c0 .075 .05 .15 .125 .174 .299 .125 .573 .374 .697 .697 .3 .673 -.025 1.445 -.697 1.744 -.672 .298 -1.444 -.025 -1.743 -.698 -.299 -.672 .025 -1.444 .697 -1.743 .1 -.025 .149 -.1 .124 -.174 V8.094 l-.426 -.038 a6.155 6.155 0 0 0 -.42 -.125 v1.22Z m1.22 1.793 a.544 .544 0 0 0 -.523 .524 c0 .274 .25 .522 .523 .522 a.51 .51 0 0 0 .523 -.522 .543 .543 0 0 0 -.523 -.524Z m1.22 -8.39 a1.254 1.254 0 0 0 -1.444 -.947 1.224 1.224 0 0 0 -.971 1.196 V6.76 c0 .05 0 .074 .05 .124 .224 .199 .473 .348 .771 .398 V3.55 c-.025 -.075 0 -.125 0 -.2 .025 -.174 .175 -.299 .349 -.324 a.404 .404 0 0 1 .424 .25 c.025 .074 .024 .174 .024 .248 v3.785 l.1 .004 a1.7 1.7 0 0 0 .623 -.328 c.074 -.05 .1 -.124 .1 -.223 V2.9 c0 -.124 -.001 -.224 -.026 -.348Z m-8.879 2.6 a4.646 4.646 0 0 1 4.656 .722 .514 .514 0 1 1 -.647 .797 c-1.021 -.822 -2.39 -1.046 -3.61 -.548 a3.584 3.584 0 0 0 -1.967 4.682 3.579 3.579 0 0 0 4.63 1.967 c.274 -.1 .574 .024 .673 .298 .1 .274 -.025 .573 -.274 .672 -2.365 .946 -5.054 -.199 -6.025 -2.564 -.946 -2.366 .199 -5.055 2.564 -6.026Z M.5 11.35 a.5 .5 0 1 1 0 1 .5 .5 0 0 1 0 -1Z m.06 -5.14 a.5 .5 0 1 1 0 1 .5 .5 0 0 1 0 -1Z m8.51 -2.88 a.5 .5 0 1 1 0 1 .5 .5 0 0 1 0 -1Z m-4.84 -.71 a.5 .5 0 1 1 0 1 .5 .5 0 0 1 0 -1Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.1 15
                moveTo(x = 4.1f, y = 15.0f)
                // a 0.5 0.5 0 1 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // a 0.5 0.5 0 0 1 0 -1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                close()
                // M 12.807 0.76
                moveTo(x = 12.807f, y = 0.76f)
                // c 0.995 0 1.843 0.722 2.017 1.718
                curveToRelative(
                    dx1 = 0.995f,
                    dy1 = 0.0f,
                    dx2 = 1.843f,
                    dy2 = 0.722f,
                    dx3 = 2.017f,
                    dy3 = 1.718f,
                )
                // c 0.025 0.174 0.025 0.349 0 0.523
                curveToRelative(
                    dx1 = 0.025f,
                    dy1 = 0.174f,
                    dx2 = 0.025f,
                    dy2 = 0.349f,
                    dx3 = 0.0f,
                    dy3 = 0.523f,
                )
                // v 5.553
                verticalLineToRelative(dy = 5.553f)
                // c 0 0.124 0 0.248 0.025 0.373
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.124f,
                    dx2 = 0.0f,
                    dy2 = 0.248f,
                    dx3 = 0.025f,
                    dy3 = 0.373f,
                )
                // c 0 0.05 0.025 0.099 0.075 0.124
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.05f,
                    dx2 = 0.025f,
                    dy2 = 0.099f,
                    dx3 = 0.075f,
                    dy3 = 0.124f,
                )
                // c 1.32 1.17 1.444 3.188 0.273 4.532
                curveToRelative(
                    dx1 = 1.32f,
                    dy1 = 1.17f,
                    dx2 = 1.444f,
                    dy2 = 3.188f,
                    dx3 = 0.273f,
                    dy3 = 4.532f,
                )
                // a 3.186 3.186 0 0 1 -1.693 0.996
                arcToRelative(
                    a = 3.186f,
                    b = 3.186f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.693f,
                    dy1 = 0.996f,
                )
                // l -0.448 0.074
                lineToRelative(dx = -0.448f, dy = 0.074f)
                // l -0.548 0.026
                lineToRelative(dx = -0.548f, dy = 0.026f)
                // c -0.2 -0.05 -0.398 -0.075 -0.572 -0.125
                curveToRelative(
                    dx1 = -0.2f,
                    dy1 = -0.05f,
                    dx2 = -0.398f,
                    dy2 = -0.075f,
                    dx3 = -0.572f,
                    dy3 = -0.125f,
                )
                // a 3.227 3.227 0 0 1 -2.341 -3.113
                arcToRelative(
                    a = 3.227f,
                    b = 3.227f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.341f,
                    dy1 = -3.113f,
                )
                // c 0 -0.896 0.373 -1.743 1.046 -2.34
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.896f,
                    dx2 = 0.373f,
                    dy2 = -1.743f,
                    dx3 = 1.046f,
                    dy3 = -2.34f,
                )
                // c 0.1 -0.075 0.149 -0.2 0.149 -0.35
                curveToRelative(
                    dx1 = 0.1f,
                    dy1 = -0.075f,
                    dx2 = 0.149f,
                    dy2 = -0.2f,
                    dx3 = 0.149f,
                    dy3 = -0.35f,
                )
                // V 6.139
                verticalLineTo(y = 6.139f)
                // c 0 -1.12 -0.024 -2.24 -0.024 -3.336
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.12f,
                    dx2 = -0.024f,
                    dy2 = -2.24f,
                    dx3 = -0.024f,
                    dy3 = -3.336f,
                )
                // c 0 -1.12 0.92 -2.042 2.04 -2.042z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.12f,
                    dx2 = 0.92f,
                    dy2 = -2.042f,
                    dx3 = 2.04f,
                    dy3 = -2.042f,
                )
                close()
                // m -1.245 8.39
                moveToRelative(dx = -1.245f, dy = 8.39f)
                // a 0.533 0.533 0 0 1 -0.199 0.424
                arcToRelative(
                    a = 0.533f,
                    b = 0.533f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.199f,
                    dy1 = 0.424f,
                )
                // a 2.313 2.313 0 0 0 -0.946 1.793
                arcToRelative(
                    a = 2.313f,
                    b = 2.313f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.946f,
                    dy1 = 1.793f,
                )
                // a 2.414 2.414 0 0 0 2.29 2.515
                arcToRelative(
                    a = 2.414f,
                    b = 2.414f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.29f,
                    dy1 = 2.515f,
                )
                // c 0.174 0 0.349 0 0.498 -0.026
                curveToRelative(
                    dx1 = 0.174f,
                    dy1 = 0.0f,
                    dx2 = 0.349f,
                    dy2 = 0.0f,
                    dx3 = 0.498f,
                    dy3 = -0.026f,
                )
                // a 2.28 2.28 0 0 0 1.918 -1.767
                arcToRelative(
                    a = 2.28f,
                    b = 2.28f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.918f,
                    dy1 = -1.767f,
                )
                // a 2.354 2.354 0 0 0 -0.872 -2.49
                arcToRelative(
                    a = 2.354f,
                    b = 2.354f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.872f,
                    dy1 = -2.49f,
                )
                // a 0.42 0.42 0 0 1 -0.2 -0.423
                arcToRelative(
                    a = 0.42f,
                    b = 0.42f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.2f,
                    dy1 = -0.423f,
                )
                // V 7.98
                verticalLineTo(y = 7.98f)
                // c -0.248 0.075 -0.473 0.15 -0.697 0.2
                curveToRelative(
                    dx1 = -0.248f,
                    dy1 = 0.075f,
                    dx2 = -0.473f,
                    dy2 = 0.15f,
                    dx3 = -0.697f,
                    dy3 = 0.2f,
                )
                // l -0.149 -0.014
                lineToRelative(dx = -0.149f, dy = -0.014f)
                // v 1.906
                verticalLineToRelative(dy = 1.906f)
                // c 0 0.075 0.05 0.15 0.125 0.174
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.075f,
                    dx2 = 0.05f,
                    dy2 = 0.15f,
                    dx3 = 0.125f,
                    dy3 = 0.174f,
                )
                // c 0.299 0.125 0.573 0.374 0.697 0.697
                curveToRelative(
                    dx1 = 0.299f,
                    dy1 = 0.125f,
                    dx2 = 0.573f,
                    dy2 = 0.374f,
                    dx3 = 0.697f,
                    dy3 = 0.697f,
                )
                // c 0.3 0.673 -0.025 1.445 -0.697 1.744
                curveToRelative(
                    dx1 = 0.3f,
                    dy1 = 0.673f,
                    dx2 = -0.025f,
                    dy2 = 1.445f,
                    dx3 = -0.697f,
                    dy3 = 1.744f,
                )
                // c -0.672 0.298 -1.444 -0.025 -1.743 -0.698
                curveToRelative(
                    dx1 = -0.672f,
                    dy1 = 0.298f,
                    dx2 = -1.444f,
                    dy2 = -0.025f,
                    dx3 = -1.743f,
                    dy3 = -0.698f,
                )
                // c -0.299 -0.672 0.025 -1.444 0.697 -1.743
                curveToRelative(
                    dx1 = -0.299f,
                    dy1 = -0.672f,
                    dx2 = 0.025f,
                    dy2 = -1.444f,
                    dx3 = 0.697f,
                    dy3 = -1.743f,
                )
                // c 0.1 -0.025 0.149 -0.1 0.124 -0.174
                curveToRelative(
                    dx1 = 0.1f,
                    dy1 = -0.025f,
                    dx2 = 0.149f,
                    dy2 = -0.1f,
                    dx3 = 0.124f,
                    dy3 = -0.174f,
                )
                // V 8.094
                verticalLineTo(y = 8.094f)
                // l -0.426 -0.038
                lineToRelative(dx = -0.426f, dy = -0.038f)
                // a 6.155 6.155 0 0 0 -0.42 -0.125
                arcToRelative(
                    a = 6.155f,
                    b = 6.155f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.42f,
                    dy1 = -0.125f,
                )
                // v 1.22z
                verticalLineToRelative(dy = 1.22f)
                close()
                // m 1.22 1.793
                moveToRelative(dx = 1.22f, dy = 1.793f)
                // a 0.544 0.544 0 0 0 -0.523 0.524
                arcToRelative(
                    a = 0.544f,
                    b = 0.544f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.523f,
                    dy1 = 0.524f,
                )
                // c 0 0.274 0.25 0.522 0.523 0.522
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.274f,
                    dx2 = 0.25f,
                    dy2 = 0.522f,
                    dx3 = 0.523f,
                    dy3 = 0.522f,
                )
                // a 0.51 0.51 0 0 0 0.523 -0.522
                arcToRelative(
                    a = 0.51f,
                    b = 0.51f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.523f,
                    dy1 = -0.522f,
                )
                // a 0.543 0.543 0 0 0 -0.523 -0.524z
                arcToRelative(
                    a = 0.543f,
                    b = 0.543f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.523f,
                    dy1 = -0.524f,
                )
                close()
                // m 1.22 -8.39
                moveToRelative(dx = 1.22f, dy = -8.39f)
                // a 1.254 1.254 0 0 0 -1.444 -0.947
                arcToRelative(
                    a = 1.254f,
                    b = 1.254f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.444f,
                    dy1 = -0.947f,
                )
                // a 1.224 1.224 0 0 0 -0.971 1.196
                arcToRelative(
                    a = 1.224f,
                    b = 1.224f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.971f,
                    dy1 = 1.196f,
                )
                // V 6.76
                verticalLineTo(y = 6.76f)
                // c 0 0.05 0 0.074 0.05 0.124
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.05f,
                    dx2 = 0.0f,
                    dy2 = 0.074f,
                    dx3 = 0.05f,
                    dy3 = 0.124f,
                )
                // c 0.224 0.199 0.473 0.348 0.771 0.398
                curveToRelative(
                    dx1 = 0.224f,
                    dy1 = 0.199f,
                    dx2 = 0.473f,
                    dy2 = 0.348f,
                    dx3 = 0.771f,
                    dy3 = 0.398f,
                )
                // V 3.55
                verticalLineTo(y = 3.55f)
                // c -0.025 -0.075 0 -0.125 0 -0.2
                curveToRelative(
                    dx1 = -0.025f,
                    dy1 = -0.075f,
                    dx2 = 0.0f,
                    dy2 = -0.125f,
                    dx3 = 0.0f,
                    dy3 = -0.2f,
                )
                // c 0.025 -0.174 0.175 -0.299 0.349 -0.324
                curveToRelative(
                    dx1 = 0.025f,
                    dy1 = -0.174f,
                    dx2 = 0.175f,
                    dy2 = -0.299f,
                    dx3 = 0.349f,
                    dy3 = -0.324f,
                )
                // a 0.404 0.404 0 0 1 0.424 0.25
                arcToRelative(
                    a = 0.404f,
                    b = 0.404f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.424f,
                    dy1 = 0.25f,
                )
                // c 0.025 0.074 0.024 0.174 0.024 0.248
                curveToRelative(
                    dx1 = 0.025f,
                    dy1 = 0.074f,
                    dx2 = 0.024f,
                    dy2 = 0.174f,
                    dx3 = 0.024f,
                    dy3 = 0.248f,
                )
                // v 3.785
                verticalLineToRelative(dy = 3.785f)
                // l 0.1 0.004
                lineToRelative(dx = 0.1f, dy = 0.004f)
                // a 1.7 1.7 0 0 0 0.623 -0.328
                arcToRelative(
                    a = 1.7f,
                    b = 1.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.623f,
                    dy1 = -0.328f,
                )
                // c 0.074 -0.05 0.1 -0.124 0.1 -0.223
                curveToRelative(
                    dx1 = 0.074f,
                    dy1 = -0.05f,
                    dx2 = 0.1f,
                    dy2 = -0.124f,
                    dx3 = 0.1f,
                    dy3 = -0.223f,
                )
                // V 2.9
                verticalLineTo(y = 2.9f)
                // c 0 -0.124 -0.001 -0.224 -0.026 -0.348z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.124f,
                    dx2 = -0.001f,
                    dy2 = -0.224f,
                    dx3 = -0.026f,
                    dy3 = -0.348f,
                )
                close()
                // m -8.879 2.6
                moveToRelative(dx = -8.879f, dy = 2.6f)
                // a 4.646 4.646 0 0 1 4.656 0.722
                arcToRelative(
                    a = 4.646f,
                    b = 4.646f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 4.656f,
                    dy1 = 0.722f,
                )
                // a 0.514 0.514 0 1 1 -0.647 0.797
                arcToRelative(
                    a = 0.514f,
                    b = 0.514f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.647f,
                    dy1 = 0.797f,
                )
                // c -1.021 -0.822 -2.39 -1.046 -3.61 -0.548
                curveToRelative(
                    dx1 = -1.021f,
                    dy1 = -0.822f,
                    dx2 = -2.39f,
                    dy2 = -1.046f,
                    dx3 = -3.61f,
                    dy3 = -0.548f,
                )
                // a 3.584 3.584 0 0 0 -1.967 4.682
                arcToRelative(
                    a = 3.584f,
                    b = 3.584f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.967f,
                    dy1 = 4.682f,
                )
                // a 3.579 3.579 0 0 0 4.63 1.967
                arcToRelative(
                    a = 3.579f,
                    b = 3.579f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 4.63f,
                    dy1 = 1.967f,
                )
                // c 0.274 -0.1 0.574 0.024 0.673 0.298
                curveToRelative(
                    dx1 = 0.274f,
                    dy1 = -0.1f,
                    dx2 = 0.574f,
                    dy2 = 0.024f,
                    dx3 = 0.673f,
                    dy3 = 0.298f,
                )
                // c 0.1 0.274 -0.025 0.573 -0.274 0.672
                curveToRelative(
                    dx1 = 0.1f,
                    dy1 = 0.274f,
                    dx2 = -0.025f,
                    dy2 = 0.573f,
                    dx3 = -0.274f,
                    dy3 = 0.672f,
                )
                // c -2.365 0.946 -5.054 -0.199 -6.025 -2.564
                curveToRelative(
                    dx1 = -2.365f,
                    dy1 = 0.946f,
                    dx2 = -5.054f,
                    dy2 = -0.199f,
                    dx3 = -6.025f,
                    dy3 = -2.564f,
                )
                // c -0.946 -2.366 0.199 -5.055 2.564 -6.026z
                curveToRelative(
                    dx1 = -0.946f,
                    dy1 = -2.366f,
                    dx2 = 0.199f,
                    dy2 = -5.055f,
                    dx3 = 2.564f,
                    dy3 = -6.026f,
                )
                close()
                // M 0.5 11.35
                moveTo(x = 0.5f, y = 11.35f)
                // a 0.5 0.5 0 1 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // a 0.5 0.5 0 0 1 0 -1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                close()
                // m 0.06 -5.14
                moveToRelative(dx = 0.06f, dy = -5.14f)
                // a 0.5 0.5 0 1 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // a 0.5 0.5 0 0 1 0 -1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                close()
                // m 8.51 -2.88
                moveToRelative(dx = 8.51f, dy = -2.88f)
                // a 0.5 0.5 0 1 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // a 0.5 0.5 0 0 1 0 -1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                close()
                // m -4.84 -0.71
                moveToRelative(dx = -4.84f, dy = -0.71f)
                // a 0.5 0.5 0 1 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // a 0.5 0.5 0 0 1 0 -1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                close()
            }
        }.build().also { _ic2529 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2529: ImageVector? = null
