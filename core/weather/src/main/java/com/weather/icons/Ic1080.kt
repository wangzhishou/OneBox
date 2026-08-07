package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1080: ImageVector
    get() {
        val current = _ic1080
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1080",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M3.801 4.095 c.022 -.005 2.188 -.23 3.836 .283 1.08 .338 1.97 .505 2.705 .505 .586 0 1.074 -.107 1.48 -.32 a2.105 2.105 0 0 0 1.046 -1.271 c.253 -.817 .14 -1.617 -.32 -2.254 A2.53 2.53 0 0 0 10.553 0 a2.442 2.442 0 0 0 -1.779 .754 .722 .722 0 1 0 1.043 1 .983 .983 0 0 1 .71 -.307 c.327 .006 .662 .178 .85 .44 .192 .264 .228 .593 .108 .976 a.652 .652 0 0 1 -.335 .42 c-.302 .16 -1.104 .332 -3.08 -.285 -1.943 -.606 -4.326 -.355 -4.426 -.341 a.722 .722 0 0 0 -.639 .797 c.044 .397 .403 .666 .797 .64Z m2.393 1.338 C10.318 6.22 13.923 6.26 16 5 c0 0 -.448 3.885 -6.735 2.813 a49.087 49.087 0 0 1 -2.37 -.489 C4.835 6.865 2.559 6.358 0 6.41 c0 0 2.285 -1.724 6.194 -.977Z m.174 3.113 c2.677 1.365 5.977 .4 5.977 .4 -1.673 1.346 -4.157 2.446 -7.263 .504 C2.103 7.588 .345 7.837 .345 7.837 c1.13 -.573 3.72 -.464 6.023 .71Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 3.801 4.095
                moveTo(x = 3.801f, y = 4.095f)
                // c 0.022 -0.005 2.188 -0.23 3.836 0.283
                curveToRelative(
                    dx1 = 0.022f,
                    dy1 = -0.005f,
                    dx2 = 2.188f,
                    dy2 = -0.23f,
                    dx3 = 3.836f,
                    dy3 = 0.283f,
                )
                // c 1.08 0.338 1.97 0.505 2.705 0.505
                curveToRelative(
                    dx1 = 1.08f,
                    dy1 = 0.338f,
                    dx2 = 1.97f,
                    dy2 = 0.505f,
                    dx3 = 2.705f,
                    dy3 = 0.505f,
                )
                // c 0.586 0 1.074 -0.107 1.48 -0.32
                curveToRelative(
                    dx1 = 0.586f,
                    dy1 = 0.0f,
                    dx2 = 1.074f,
                    dy2 = -0.107f,
                    dx3 = 1.48f,
                    dy3 = -0.32f,
                )
                // a 2.105 2.105 0 0 0 1.046 -1.271
                arcToRelative(
                    a = 2.105f,
                    b = 2.105f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.046f,
                    dy1 = -1.271f,
                )
                // c 0.253 -0.817 0.14 -1.617 -0.32 -2.254
                curveToRelative(
                    dx1 = 0.253f,
                    dy1 = -0.817f,
                    dx2 = 0.14f,
                    dy2 = -1.617f,
                    dx3 = -0.32f,
                    dy3 = -2.254f,
                )
                // A 2.53 2.53 0 0 0 10.553 0
                arcTo(
                    horizontalEllipseRadius = 2.53f,
                    verticalEllipseRadius = 2.53f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 10.553f,
                    y1 = 0.0f,
                )
                // a 2.442 2.442 0 0 0 -1.779 0.754
                arcToRelative(
                    a = 2.442f,
                    b = 2.442f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.779f,
                    dy1 = 0.754f,
                )
                // a 0.722 0.722 0 1 0 1.043 1
                arcToRelative(
                    a = 0.722f,
                    b = 0.722f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 1.043f,
                    dy1 = 1.0f,
                )
                // a 0.983 0.983 0 0 1 0.71 -0.307
                arcToRelative(
                    a = 0.983f,
                    b = 0.983f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.71f,
                    dy1 = -0.307f,
                )
                // c 0.327 0.006 0.662 0.178 0.85 0.44
                curveToRelative(
                    dx1 = 0.327f,
                    dy1 = 0.006f,
                    dx2 = 0.662f,
                    dy2 = 0.178f,
                    dx3 = 0.85f,
                    dy3 = 0.44f,
                )
                // c 0.192 0.264 0.228 0.593 0.108 0.976
                curveToRelative(
                    dx1 = 0.192f,
                    dy1 = 0.264f,
                    dx2 = 0.228f,
                    dy2 = 0.593f,
                    dx3 = 0.108f,
                    dy3 = 0.976f,
                )
                // a 0.652 0.652 0 0 1 -0.335 0.42
                arcToRelative(
                    a = 0.652f,
                    b = 0.652f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.335f,
                    dy1 = 0.42f,
                )
                // c -0.302 0.16 -1.104 0.332 -3.08 -0.285
                curveToRelative(
                    dx1 = -0.302f,
                    dy1 = 0.16f,
                    dx2 = -1.104f,
                    dy2 = 0.332f,
                    dx3 = -3.08f,
                    dy3 = -0.285f,
                )
                // c -1.943 -0.606 -4.326 -0.355 -4.426 -0.341
                curveToRelative(
                    dx1 = -1.943f,
                    dy1 = -0.606f,
                    dx2 = -4.326f,
                    dy2 = -0.355f,
                    dx3 = -4.426f,
                    dy3 = -0.341f,
                )
                // a 0.722 0.722 0 0 0 -0.639 0.797
                arcToRelative(
                    a = 0.722f,
                    b = 0.722f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.639f,
                    dy1 = 0.797f,
                )
                // c 0.044 0.397 0.403 0.666 0.797 0.64z
                curveToRelative(
                    dx1 = 0.044f,
                    dy1 = 0.397f,
                    dx2 = 0.403f,
                    dy2 = 0.666f,
                    dx3 = 0.797f,
                    dy3 = 0.64f,
                )
                close()
                // m 2.393 1.338
                moveToRelative(dx = 2.393f, dy = 1.338f)
                // C 10.318 6.22 13.923 6.26 16 5
                curveTo(
                    x1 = 10.318f,
                    y1 = 6.22f,
                    x2 = 13.923f,
                    y2 = 6.26f,
                    x3 = 16.0f,
                    y3 = 5.0f,
                )
                // c 0 0 -0.448 3.885 -6.735 2.813
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.0f,
                    dx2 = -0.448f,
                    dy2 = 3.885f,
                    dx3 = -6.735f,
                    dy3 = 2.813f,
                )
                // a 49.087 49.087 0 0 1 -2.37 -0.489
                arcToRelative(
                    a = 49.087f,
                    b = 49.087f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.37f,
                    dy1 = -0.489f,
                )
                // C 4.835 6.865 2.559 6.358 0 6.41
                curveTo(
                    x1 = 4.835f,
                    y1 = 6.865f,
                    x2 = 2.559f,
                    y2 = 6.358f,
                    x3 = 0.0f,
                    y3 = 6.41f,
                )
                // c 0 0 2.285 -1.724 6.194 -0.977z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.0f,
                    dx2 = 2.285f,
                    dy2 = -1.724f,
                    dx3 = 6.194f,
                    dy3 = -0.977f,
                )
                close()
                // m 0.174 3.113
                moveToRelative(dx = 0.174f, dy = 3.113f)
                // c 2.677 1.365 5.977 0.4 5.977 0.4
                curveToRelative(
                    dx1 = 2.677f,
                    dy1 = 1.365f,
                    dx2 = 5.977f,
                    dy2 = 0.4f,
                    dx3 = 5.977f,
                    dy3 = 0.4f,
                )
                // c -1.673 1.346 -4.157 2.446 -7.263 0.504
                curveToRelative(
                    dx1 = -1.673f,
                    dy1 = 1.346f,
                    dx2 = -4.157f,
                    dy2 = 2.446f,
                    dx3 = -7.263f,
                    dy3 = 0.504f,
                )
                // C 2.103 7.588 0.345 7.837 0.345 7.837
                curveTo(
                    x1 = 2.103f,
                    y1 = 7.588f,
                    x2 = 0.345f,
                    y2 = 7.837f,
                    x3 = 0.345f,
                    y3 = 7.837f,
                )
                // c 1.13 -0.573 3.72 -0.464 6.023 0.71z
                curveToRelative(
                    dx1 = 1.13f,
                    dy1 = -0.573f,
                    dx2 = 3.72f,
                    dy2 = -0.464f,
                    dx3 = 6.023f,
                    dy3 = 0.71f,
                )
                close()
            }
            // M9.662 11.31 c.806 -.26 1.565 -.504 2.286 -.38 .93 .161 1.656 .824 1.942 1.773 .156 .52 .146 1.08 -.029 1.594 a2.537 2.537 0 0 1 -.942 1.263 c-.437 .306 -.957 .46 -1.485 .438 a2.422 2.422 0 0 1 -1.446 -.56 .736 .736 0 0 1 -.106 -1.01 .683 .683 0 0 1 .979 -.11 1.052 1.052 0 0 0 1.275 .05 c.19 -.132 .333 -.325 .41 -.548 .075 -.224 .08 -.466 .013 -.693 a1.056 1.056 0 0 0 -.841 -.78 c-.387 -.065 -1.004 .13 -1.652 .34 l-.007 .002 c-.527 .17 -1.121 .361 -1.749 .464 -3.11 .505 -5.976 -2.299 -6.096 -2.417 A.718 .718 0 0 1 2 10.232 a.739 .739 0 0 1 .194 -.511 .696 .696 0 0 1 .488 -.22 .677 .677 0 0 1 .498 .2 c.07 .068 2.528 2.425 4.912 2.032 .53 -.086 1.052 -.255 1.556 -.418 l.014 -.004Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 9.662 11.31
                moveTo(x = 9.662f, y = 11.31f)
                // c 0.806 -0.26 1.565 -0.504 2.286 -0.38
                curveToRelative(
                    dx1 = 0.806f,
                    dy1 = -0.26f,
                    dx2 = 1.565f,
                    dy2 = -0.504f,
                    dx3 = 2.286f,
                    dy3 = -0.38f,
                )
                // c 0.93 0.161 1.656 0.824 1.942 1.773
                curveToRelative(
                    dx1 = 0.93f,
                    dy1 = 0.161f,
                    dx2 = 1.656f,
                    dy2 = 0.824f,
                    dx3 = 1.942f,
                    dy3 = 1.773f,
                )
                // c 0.156 0.52 0.146 1.08 -0.029 1.594
                curveToRelative(
                    dx1 = 0.156f,
                    dy1 = 0.52f,
                    dx2 = 0.146f,
                    dy2 = 1.08f,
                    dx3 = -0.029f,
                    dy3 = 1.594f,
                )
                // a 2.537 2.537 0 0 1 -0.942 1.263
                arcToRelative(
                    a = 2.537f,
                    b = 2.537f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.942f,
                    dy1 = 1.263f,
                )
                // c -0.437 0.306 -0.957 0.46 -1.485 0.438
                curveToRelative(
                    dx1 = -0.437f,
                    dy1 = 0.306f,
                    dx2 = -0.957f,
                    dy2 = 0.46f,
                    dx3 = -1.485f,
                    dy3 = 0.438f,
                )
                // a 2.422 2.422 0 0 1 -1.446 -0.56
                arcToRelative(
                    a = 2.422f,
                    b = 2.422f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.446f,
                    dy1 = -0.56f,
                )
                // a 0.736 0.736 0 0 1 -0.106 -1.01
                arcToRelative(
                    a = 0.736f,
                    b = 0.736f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.106f,
                    dy1 = -1.01f,
                )
                // a 0.683 0.683 0 0 1 0.979 -0.11
                arcToRelative(
                    a = 0.683f,
                    b = 0.683f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.979f,
                    dy1 = -0.11f,
                )
                // a 1.052 1.052 0 0 0 1.275 0.05
                arcToRelative(
                    a = 1.052f,
                    b = 1.052f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.275f,
                    dy1 = 0.05f,
                )
                // c 0.19 -0.132 0.333 -0.325 0.41 -0.548
                curveToRelative(
                    dx1 = 0.19f,
                    dy1 = -0.132f,
                    dx2 = 0.333f,
                    dy2 = -0.325f,
                    dx3 = 0.41f,
                    dy3 = -0.548f,
                )
                // c 0.075 -0.224 0.08 -0.466 0.013 -0.693
                curveToRelative(
                    dx1 = 0.075f,
                    dy1 = -0.224f,
                    dx2 = 0.08f,
                    dy2 = -0.466f,
                    dx3 = 0.013f,
                    dy3 = -0.693f,
                )
                // a 1.056 1.056 0 0 0 -0.841 -0.78
                arcToRelative(
                    a = 1.056f,
                    b = 1.056f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.841f,
                    dy1 = -0.78f,
                )
                // c -0.387 -0.065 -1.004 0.13 -1.652 0.34
                curveToRelative(
                    dx1 = -0.387f,
                    dy1 = -0.065f,
                    dx2 = -1.004f,
                    dy2 = 0.13f,
                    dx3 = -1.652f,
                    dy3 = 0.34f,
                )
                // l -0.007 0.002
                lineToRelative(dx = -0.007f, dy = 0.002f)
                // c -0.527 0.17 -1.121 0.361 -1.749 0.464
                curveToRelative(
                    dx1 = -0.527f,
                    dy1 = 0.17f,
                    dx2 = -1.121f,
                    dy2 = 0.361f,
                    dx3 = -1.749f,
                    dy3 = 0.464f,
                )
                // c -3.11 0.505 -5.976 -2.299 -6.096 -2.417
                curveToRelative(
                    dx1 = -3.11f,
                    dy1 = 0.505f,
                    dx2 = -5.976f,
                    dy2 = -2.299f,
                    dx3 = -6.096f,
                    dy3 = -2.417f,
                )
                // A 0.718 0.718 0 0 1 2 10.232
                arcTo(
                    horizontalEllipseRadius = 0.718f,
                    verticalEllipseRadius = 0.718f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 2.0f,
                    y1 = 10.232f,
                )
                // a 0.739 0.739 0 0 1 0.194 -0.511
                arcToRelative(
                    a = 0.739f,
                    b = 0.739f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.194f,
                    dy1 = -0.511f,
                )
                // a 0.696 0.696 0 0 1 0.488 -0.22
                arcToRelative(
                    a = 0.696f,
                    b = 0.696f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.488f,
                    dy1 = -0.22f,
                )
                // a 0.677 0.677 0 0 1 0.498 0.2
                arcToRelative(
                    a = 0.677f,
                    b = 0.677f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.498f,
                    dy1 = 0.2f,
                )
                // c 0.07 0.068 2.528 2.425 4.912 2.032
                curveToRelative(
                    dx1 = 0.07f,
                    dy1 = 0.068f,
                    dx2 = 2.528f,
                    dy2 = 2.425f,
                    dx3 = 4.912f,
                    dy3 = 2.032f,
                )
                // c 0.53 -0.086 1.052 -0.255 1.556 -0.418
                curveToRelative(
                    dx1 = 0.53f,
                    dy1 = -0.086f,
                    dx2 = 1.052f,
                    dy2 = -0.255f,
                    dx3 = 1.556f,
                    dy3 = -0.418f,
                )
                // l 0.014 -0.004z
                lineToRelative(dx = 0.014f, dy = -0.004f)
                close()
            }
        }.build().also { _ic1080 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1080: ImageVector? = null
