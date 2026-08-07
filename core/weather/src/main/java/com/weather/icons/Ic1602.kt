package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1602: ImageVector
    get() {
        val current = _ic1602
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1602",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M14.245 6 c-.87 0 -1.784 -.425 -2.575 -1.207 l-.022 -.022 L8.08 .782 a.46 .46 0 0 1 .045 -.66 .472 .472 0 0 1 .666 .046 l3.557 3.966 c.61 .592 1.276 .927 1.897 .927 a1.807 1.807 0 0 0 1.818 -1.798 c0 -.738 -.61 -1.341 -1.355 -1.341 -.621 0 -.994 .402 -.994 1.072 0 .492 .384 .503 .407 .503 a.375 .375 0 0 0 .27 -.123 c.068 -.078 .091 -.178 .08 -.324 a.476 .476 0 0 1 .418 -.525 .462 .462 0 0 1 .519 .414 c.045 .402 -.057 .77 -.305 1.05 -.248 .268 -.61 .436 -.982 .436 -.655 0 -1.355 -.503 -1.355 -1.43 0 -1.185 .801 -2 1.93 -2 C15.962 .994 17 2.01 17 3.273 17 4.77 15.758 6 14.245 6Z m-2.213 10 c-1.536 0 -3.14 -.749 -4.517 -2.123 l-.034 -.033 -6.278 -7.017 a.822 .822 0 0 1 .068 -1.162 .843 .843 0 0 1 1.174 .067 l6.256 7.005 c1.061 1.05 2.247 1.62 3.33 1.62 1.762 0 3.185 -1.418 3.185 -3.15 0 -1.296 -1.073 -2.358 -2.382 -2.358 -1.096 0 -1.74 .704 -1.74 1.877 0 .86 .678 .872 .712 .872 a.66 .66 0 0 0 .474 -.213 c.124 -.134 .17 -.323 .136 -.57 a.828 .828 0 0 1 .722 -.916 .833 .833 0 0 1 .926 .716 2.333 2.333 0 0 1 -.542 1.854 2.342 2.342 0 0 1 -1.716 .771 c-1.152 0 -2.371 -.883 -2.371 -2.514 0 -2.067 1.4 -3.52 3.399 -3.52 2.235 0 4.042 1.8 4.042 4 0 2.638 -2.168 4.794 -4.844 4.794Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 14.245 6
                moveTo(x = 14.245f, y = 6.0f)
                // c -0.87 0 -1.784 -0.425 -2.575 -1.207
                curveToRelative(
                    dx1 = -0.87f,
                    dy1 = 0.0f,
                    dx2 = -1.784f,
                    dy2 = -0.425f,
                    dx3 = -2.575f,
                    dy3 = -1.207f,
                )
                // l -0.022 -0.022
                lineToRelative(dx = -0.022f, dy = -0.022f)
                // L 8.08 0.782
                lineTo(x = 8.08f, y = 0.782f)
                // a 0.46 0.46 0 0 1 0.045 -0.66
                arcToRelative(
                    a = 0.46f,
                    b = 0.46f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.045f,
                    dy1 = -0.66f,
                )
                // a 0.472 0.472 0 0 1 0.666 0.046
                arcToRelative(
                    a = 0.472f,
                    b = 0.472f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.666f,
                    dy1 = 0.046f,
                )
                // l 3.557 3.966
                lineToRelative(dx = 3.557f, dy = 3.966f)
                // c 0.61 0.592 1.276 0.927 1.897 0.927
                curveToRelative(
                    dx1 = 0.61f,
                    dy1 = 0.592f,
                    dx2 = 1.276f,
                    dy2 = 0.927f,
                    dx3 = 1.897f,
                    dy3 = 0.927f,
                )
                // a 1.807 1.807 0 0 0 1.818 -1.798
                arcToRelative(
                    a = 1.807f,
                    b = 1.807f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.818f,
                    dy1 = -1.798f,
                )
                // c 0 -0.738 -0.61 -1.341 -1.355 -1.341
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.738f,
                    dx2 = -0.61f,
                    dy2 = -1.341f,
                    dx3 = -1.355f,
                    dy3 = -1.341f,
                )
                // c -0.621 0 -0.994 0.402 -0.994 1.072
                curveToRelative(
                    dx1 = -0.621f,
                    dy1 = 0.0f,
                    dx2 = -0.994f,
                    dy2 = 0.402f,
                    dx3 = -0.994f,
                    dy3 = 1.072f,
                )
                // c 0 0.492 0.384 0.503 0.407 0.503
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.492f,
                    dx2 = 0.384f,
                    dy2 = 0.503f,
                    dx3 = 0.407f,
                    dy3 = 0.503f,
                )
                // a 0.375 0.375 0 0 0 0.27 -0.123
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.27f,
                    dy1 = -0.123f,
                )
                // c 0.068 -0.078 0.091 -0.178 0.08 -0.324
                curveToRelative(
                    dx1 = 0.068f,
                    dy1 = -0.078f,
                    dx2 = 0.091f,
                    dy2 = -0.178f,
                    dx3 = 0.08f,
                    dy3 = -0.324f,
                )
                // a 0.476 0.476 0 0 1 0.418 -0.525
                arcToRelative(
                    a = 0.476f,
                    b = 0.476f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.418f,
                    dy1 = -0.525f,
                )
                // a 0.462 0.462 0 0 1 0.519 0.414
                arcToRelative(
                    a = 0.462f,
                    b = 0.462f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.519f,
                    dy1 = 0.414f,
                )
                // c 0.045 0.402 -0.057 0.77 -0.305 1.05
                curveToRelative(
                    dx1 = 0.045f,
                    dy1 = 0.402f,
                    dx2 = -0.057f,
                    dy2 = 0.77f,
                    dx3 = -0.305f,
                    dy3 = 1.05f,
                )
                // c -0.248 0.268 -0.61 0.436 -0.982 0.436
                curveToRelative(
                    dx1 = -0.248f,
                    dy1 = 0.268f,
                    dx2 = -0.61f,
                    dy2 = 0.436f,
                    dx3 = -0.982f,
                    dy3 = 0.436f,
                )
                // c -0.655 0 -1.355 -0.503 -1.355 -1.43
                curveToRelative(
                    dx1 = -0.655f,
                    dy1 = 0.0f,
                    dx2 = -1.355f,
                    dy2 = -0.503f,
                    dx3 = -1.355f,
                    dy3 = -1.43f,
                )
                // c 0 -1.185 0.801 -2 1.93 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.185f,
                    dx2 = 0.801f,
                    dy2 = -2.0f,
                    dx3 = 1.93f,
                    dy3 = -2.0f,
                )
                // C 15.962 0.994 17 2.01 17 3.273
                curveTo(
                    x1 = 15.962f,
                    y1 = 0.994f,
                    x2 = 17.0f,
                    y2 = 2.01f,
                    x3 = 17.0f,
                    y3 = 3.273f,
                )
                // C 17 4.77 15.758 6 14.245 6z
                curveTo(
                    x1 = 17.0f,
                    y1 = 4.77f,
                    x2 = 15.758f,
                    y2 = 6.0f,
                    x3 = 14.245f,
                    y3 = 6.0f,
                )
                close()
                // m -2.213 10
                moveToRelative(dx = -2.213f, dy = 10.0f)
                // c -1.536 0 -3.14 -0.749 -4.517 -2.123
                curveToRelative(
                    dx1 = -1.536f,
                    dy1 = 0.0f,
                    dx2 = -3.14f,
                    dy2 = -0.749f,
                    dx3 = -4.517f,
                    dy3 = -2.123f,
                )
                // l -0.034 -0.033
                lineToRelative(dx = -0.034f, dy = -0.033f)
                // l -6.278 -7.017
                lineToRelative(dx = -6.278f, dy = -7.017f)
                // a 0.822 0.822 0 0 1 0.068 -1.162
                arcToRelative(
                    a = 0.822f,
                    b = 0.822f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.068f,
                    dy1 = -1.162f,
                )
                // a 0.843 0.843 0 0 1 1.174 0.067
                arcToRelative(
                    a = 0.843f,
                    b = 0.843f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.174f,
                    dy1 = 0.067f,
                )
                // l 6.256 7.005
                lineToRelative(dx = 6.256f, dy = 7.005f)
                // c 1.061 1.05 2.247 1.62 3.33 1.62
                curveToRelative(
                    dx1 = 1.061f,
                    dy1 = 1.05f,
                    dx2 = 2.247f,
                    dy2 = 1.62f,
                    dx3 = 3.33f,
                    dy3 = 1.62f,
                )
                // c 1.762 0 3.185 -1.418 3.185 -3.15
                curveToRelative(
                    dx1 = 1.762f,
                    dy1 = 0.0f,
                    dx2 = 3.185f,
                    dy2 = -1.418f,
                    dx3 = 3.185f,
                    dy3 = -3.15f,
                )
                // c 0 -1.296 -1.073 -2.358 -2.382 -2.358
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.296f,
                    dx2 = -1.073f,
                    dy2 = -2.358f,
                    dx3 = -2.382f,
                    dy3 = -2.358f,
                )
                // c -1.096 0 -1.74 0.704 -1.74 1.877
                curveToRelative(
                    dx1 = -1.096f,
                    dy1 = 0.0f,
                    dx2 = -1.74f,
                    dy2 = 0.704f,
                    dx3 = -1.74f,
                    dy3 = 1.877f,
                )
                // c 0 0.86 0.678 0.872 0.712 0.872
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.86f,
                    dx2 = 0.678f,
                    dy2 = 0.872f,
                    dx3 = 0.712f,
                    dy3 = 0.872f,
                )
                // a 0.66 0.66 0 0 0 0.474 -0.213
                arcToRelative(
                    a = 0.66f,
                    b = 0.66f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.474f,
                    dy1 = -0.213f,
                )
                // c 0.124 -0.134 0.17 -0.323 0.136 -0.57
                curveToRelative(
                    dx1 = 0.124f,
                    dy1 = -0.134f,
                    dx2 = 0.17f,
                    dy2 = -0.323f,
                    dx3 = 0.136f,
                    dy3 = -0.57f,
                )
                // a 0.828 0.828 0 0 1 0.722 -0.916
                arcToRelative(
                    a = 0.828f,
                    b = 0.828f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.722f,
                    dy1 = -0.916f,
                )
                // a 0.833 0.833 0 0 1 0.926 0.716
                arcToRelative(
                    a = 0.833f,
                    b = 0.833f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.926f,
                    dy1 = 0.716f,
                )
                // a 2.333 2.333 0 0 1 -0.542 1.854
                arcToRelative(
                    a = 2.333f,
                    b = 2.333f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.542f,
                    dy1 = 1.854f,
                )
                // a 2.342 2.342 0 0 1 -1.716 0.771
                arcToRelative(
                    a = 2.342f,
                    b = 2.342f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.716f,
                    dy1 = 0.771f,
                )
                // c -1.152 0 -2.371 -0.883 -2.371 -2.514
                curveToRelative(
                    dx1 = -1.152f,
                    dy1 = 0.0f,
                    dx2 = -2.371f,
                    dy2 = -0.883f,
                    dx3 = -2.371f,
                    dy3 = -2.514f,
                )
                // c 0 -2.067 1.4 -3.52 3.399 -3.52
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -2.067f,
                    dx2 = 1.4f,
                    dy2 = -3.52f,
                    dx3 = 3.399f,
                    dy3 = -3.52f,
                )
                // c 2.235 0 4.042 1.8 4.042 4
                curveToRelative(
                    dx1 = 2.235f,
                    dy1 = 0.0f,
                    dx2 = 4.042f,
                    dy2 = 1.8f,
                    dx3 = 4.042f,
                    dy3 = 4.0f,
                )
                // c 0 2.638 -2.168 4.794 -4.844 4.794z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 2.638f,
                    dx2 = -2.168f,
                    dy2 = 4.794f,
                    dx3 = -4.844f,
                    dy3 = 4.794f,
                )
                close()
            }
            // M4.907 5.933 c.632 .637 1.377 .983 2.089 .983 1.242 0 2.247 -.994 2.27 -2.2 0 -1.029 -.848 -1.856 -1.875 -1.856 -.926 0 -1.57 .67 -1.57 1.632 0 .748 .565 1.162 1.096 1.162 .293 0 .587 -.134 .79 -.358 .203 -.235 .282 -.536 .249 -.86 a.383 .383 0 0 0 -.43 -.335 .38 .38 0 0 0 -.338 .424 c.011 .112 -.012 .19 -.068 .257 a.342 .342 0 0 1 -.226 .1 c-.011 0 -.327 0 -.327 -.402 0 -.547 .293 -.871 .801 -.871 .61 0 1.107 .492 1.107 1.095 0 .804 -.666 1.464 -1.48 1.464 -.496 0 -1.038 -.269 -1.535 -.749 l-2.89 -3.24 a.392 .392 0 0 0 -.542 -.034 .382 .382 0 0 0 -.034 .537 l2.902 3.24 .01 .011Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.907 5.933
                moveTo(x = 4.907f, y = 5.933f)
                // c 0.632 0.637 1.377 0.983 2.089 0.983
                curveToRelative(
                    dx1 = 0.632f,
                    dy1 = 0.637f,
                    dx2 = 1.377f,
                    dy2 = 0.983f,
                    dx3 = 2.089f,
                    dy3 = 0.983f,
                )
                // c 1.242 0 2.247 -0.994 2.27 -2.2
                curveToRelative(
                    dx1 = 1.242f,
                    dy1 = 0.0f,
                    dx2 = 2.247f,
                    dy2 = -0.994f,
                    dx3 = 2.27f,
                    dy3 = -2.2f,
                )
                // c 0 -1.029 -0.848 -1.856 -1.875 -1.856
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.029f,
                    dx2 = -0.848f,
                    dy2 = -1.856f,
                    dx3 = -1.875f,
                    dy3 = -1.856f,
                )
                // c -0.926 0 -1.57 0.67 -1.57 1.632
                curveToRelative(
                    dx1 = -0.926f,
                    dy1 = 0.0f,
                    dx2 = -1.57f,
                    dy2 = 0.67f,
                    dx3 = -1.57f,
                    dy3 = 1.632f,
                )
                // c 0 0.748 0.565 1.162 1.096 1.162
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.748f,
                    dx2 = 0.565f,
                    dy2 = 1.162f,
                    dx3 = 1.096f,
                    dy3 = 1.162f,
                )
                // c 0.293 0 0.587 -0.134 0.79 -0.358
                curveToRelative(
                    dx1 = 0.293f,
                    dy1 = 0.0f,
                    dx2 = 0.587f,
                    dy2 = -0.134f,
                    dx3 = 0.79f,
                    dy3 = -0.358f,
                )
                // c 0.203 -0.235 0.282 -0.536 0.249 -0.86
                curveToRelative(
                    dx1 = 0.203f,
                    dy1 = -0.235f,
                    dx2 = 0.282f,
                    dy2 = -0.536f,
                    dx3 = 0.249f,
                    dy3 = -0.86f,
                )
                // a 0.383 0.383 0 0 0 -0.43 -0.335
                arcToRelative(
                    a = 0.383f,
                    b = 0.383f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.43f,
                    dy1 = -0.335f,
                )
                // a 0.38 0.38 0 0 0 -0.338 0.424
                arcToRelative(
                    a = 0.38f,
                    b = 0.38f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.338f,
                    dy1 = 0.424f,
                )
                // c 0.011 0.112 -0.012 0.19 -0.068 0.257
                curveToRelative(
                    dx1 = 0.011f,
                    dy1 = 0.112f,
                    dx2 = -0.012f,
                    dy2 = 0.19f,
                    dx3 = -0.068f,
                    dy3 = 0.257f,
                )
                // a 0.342 0.342 0 0 1 -0.226 0.1
                arcToRelative(
                    a = 0.342f,
                    b = 0.342f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.226f,
                    dy1 = 0.1f,
                )
                // c -0.011 0 -0.327 0 -0.327 -0.402
                curveToRelative(
                    dx1 = -0.011f,
                    dy1 = 0.0f,
                    dx2 = -0.327f,
                    dy2 = 0.0f,
                    dx3 = -0.327f,
                    dy3 = -0.402f,
                )
                // c 0 -0.547 0.293 -0.871 0.801 -0.871
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.547f,
                    dx2 = 0.293f,
                    dy2 = -0.871f,
                    dx3 = 0.801f,
                    dy3 = -0.871f,
                )
                // c 0.61 0 1.107 0.492 1.107 1.095
                curveToRelative(
                    dx1 = 0.61f,
                    dy1 = 0.0f,
                    dx2 = 1.107f,
                    dy2 = 0.492f,
                    dx3 = 1.107f,
                    dy3 = 1.095f,
                )
                // c 0 0.804 -0.666 1.464 -1.48 1.464
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.804f,
                    dx2 = -0.666f,
                    dy2 = 1.464f,
                    dx3 = -1.48f,
                    dy3 = 1.464f,
                )
                // c -0.496 0 -1.038 -0.269 -1.535 -0.749
                curveToRelative(
                    dx1 = -0.496f,
                    dy1 = 0.0f,
                    dx2 = -1.038f,
                    dy2 = -0.269f,
                    dx3 = -1.535f,
                    dy3 = -0.749f,
                )
                // l -2.89 -3.24
                lineToRelative(dx = -2.89f, dy = -3.24f)
                // a 0.392 0.392 0 0 0 -0.542 -0.034
                arcToRelative(
                    a = 0.392f,
                    b = 0.392f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.542f,
                    dy1 = -0.034f,
                )
                // a 0.382 0.382 0 0 0 -0.034 0.537
                arcToRelative(
                    a = 0.382f,
                    b = 0.382f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.034f,
                    dy1 = 0.537f,
                )
                // l 2.902 3.24
                lineToRelative(dx = 2.902f, dy = 3.24f)
                // l 0.01 0.011z
                lineToRelative(dx = 0.01f, dy = 0.011f)
                close()
            }
        }.build().also { _ic1602 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1602: ImageVector? = null
