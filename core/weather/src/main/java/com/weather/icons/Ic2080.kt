package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2080: ImageVector
    get() {
        val current = _ic2080
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2080",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M4.603 1.616 c0 1.12 1.684 1.622 3.354 1.622 1.67 0 3.354 -.496 3.354 -1.615 C11.311 .503 9.628 0 7.957 0 c-1.67 0 -3.354 .496 -3.354 1.616Z m3.354 .496 c-1.223 0 -1.992 -.305 -2.242 -.49 .25 -.19 1.02 -.488 2.242 -.488 1.223 0 1.993 .304 2.243 .489 -.25 .191 -1.02 .489 -2.243 .489Z m-.519 10.232 c-1.48 0 -2.874 -.185 -3.933 -.51 -1.295 -.404 -1.946 -.986 -1.946 -1.722 a1.2 1.2 0 0 1 .256 -.73 .493 .493 0 0 1 .737 -.085 .594 .594 0 0 1 .059 .822 c.164 .333 1.749 1.098 4.827 1.098 3.077 0 4.662 -.765 4.827 -1.098 a.045 .045 0 0 1 -.007 -.01 .593 .593 0 0 1 .065 -.813 .498 .498 0 0 1 .737 .086 1.2 1.2 0 0 1 .257 .73 c0 .744 -.658 1.325 -1.947 1.722 -1.058 .332 -2.453 .51 -3.932 .51Z m-.02 2.664 c-1.887 0 -3.788 -.539 -3.788 -1.736 0 -.206 .06 -.404 .178 -.588 a.503 .503 0 0 1 .73 -.142 .596 .596 0 0 1 .17 .723 c.218 .212 1.138 .616 2.71 .616 s2.492 -.396 2.71 -.616 a.595 .595 0 0 1 .17 -.716 .498 .498 0 0 1 .73 .142 c.119 .184 .178 .382 .178 .588 0 .411 -.23 .978 -1.315 1.368 -.665 .226 -1.546 .361 -2.473 .361Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.603 1.616
                moveTo(x = 4.603f, y = 1.616f)
                // c 0 1.12 1.684 1.622 3.354 1.622
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.12f,
                    dx2 = 1.684f,
                    dy2 = 1.622f,
                    dx3 = 3.354f,
                    dy3 = 1.622f,
                )
                // c 1.67 0 3.354 -0.496 3.354 -1.615
                curveToRelative(
                    dx1 = 1.67f,
                    dy1 = 0.0f,
                    dx2 = 3.354f,
                    dy2 = -0.496f,
                    dx3 = 3.354f,
                    dy3 = -1.615f,
                )
                // C 11.311 0.503 9.628 0 7.957 0
                curveTo(
                    x1 = 11.311f,
                    y1 = 0.503f,
                    x2 = 9.628f,
                    y2 = 0.0f,
                    x3 = 7.957f,
                    y3 = 0.0f,
                )
                // c -1.67 0 -3.354 0.496 -3.354 1.616z
                curveToRelative(
                    dx1 = -1.67f,
                    dy1 = 0.0f,
                    dx2 = -3.354f,
                    dy2 = 0.496f,
                    dx3 = -3.354f,
                    dy3 = 1.616f,
                )
                close()
                // m 3.354 0.496
                moveToRelative(dx = 3.354f, dy = 0.496f)
                // c -1.223 0 -1.992 -0.305 -2.242 -0.49
                curveToRelative(
                    dx1 = -1.223f,
                    dy1 = 0.0f,
                    dx2 = -1.992f,
                    dy2 = -0.305f,
                    dx3 = -2.242f,
                    dy3 = -0.49f,
                )
                // c 0.25 -0.19 1.02 -0.488 2.242 -0.488
                curveToRelative(
                    dx1 = 0.25f,
                    dy1 = -0.19f,
                    dx2 = 1.02f,
                    dy2 = -0.488f,
                    dx3 = 2.242f,
                    dy3 = -0.488f,
                )
                // c 1.223 0 1.993 0.304 2.243 0.489
                curveToRelative(
                    dx1 = 1.223f,
                    dy1 = 0.0f,
                    dx2 = 1.993f,
                    dy2 = 0.304f,
                    dx3 = 2.243f,
                    dy3 = 0.489f,
                )
                // c -0.25 0.191 -1.02 0.489 -2.243 0.489z
                curveToRelative(
                    dx1 = -0.25f,
                    dy1 = 0.191f,
                    dx2 = -1.02f,
                    dy2 = 0.489f,
                    dx3 = -2.243f,
                    dy3 = 0.489f,
                )
                close()
                // m -0.519 10.232
                moveToRelative(dx = -0.519f, dy = 10.232f)
                // c -1.48 0 -2.874 -0.185 -3.933 -0.51
                curveToRelative(
                    dx1 = -1.48f,
                    dy1 = 0.0f,
                    dx2 = -2.874f,
                    dy2 = -0.185f,
                    dx3 = -3.933f,
                    dy3 = -0.51f,
                )
                // c -1.295 -0.404 -1.946 -0.986 -1.946 -1.722
                curveToRelative(
                    dx1 = -1.295f,
                    dy1 = -0.404f,
                    dx2 = -1.946f,
                    dy2 = -0.986f,
                    dx3 = -1.946f,
                    dy3 = -1.722f,
                )
                // a 1.2 1.2 0 0 1 0.256 -0.73
                arcToRelative(
                    a = 1.2f,
                    b = 1.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.256f,
                    dy1 = -0.73f,
                )
                // a 0.493 0.493 0 0 1 0.737 -0.085
                arcToRelative(
                    a = 0.493f,
                    b = 0.493f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.737f,
                    dy1 = -0.085f,
                )
                // a 0.594 0.594 0 0 1 0.059 0.822
                arcToRelative(
                    a = 0.594f,
                    b = 0.594f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.059f,
                    dy1 = 0.822f,
                )
                // c 0.164 0.333 1.749 1.098 4.827 1.098
                curveToRelative(
                    dx1 = 0.164f,
                    dy1 = 0.333f,
                    dx2 = 1.749f,
                    dy2 = 1.098f,
                    dx3 = 4.827f,
                    dy3 = 1.098f,
                )
                // c 3.077 0 4.662 -0.765 4.827 -1.098
                curveToRelative(
                    dx1 = 3.077f,
                    dy1 = 0.0f,
                    dx2 = 4.662f,
                    dy2 = -0.765f,
                    dx3 = 4.827f,
                    dy3 = -1.098f,
                )
                // a 0.045 0.045 0 0 1 -0.007 -0.01
                arcToRelative(
                    a = 0.045f,
                    b = 0.045f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.007f,
                    dy1 = -0.01f,
                )
                // a 0.593 0.593 0 0 1 0.065 -0.813
                arcToRelative(
                    a = 0.593f,
                    b = 0.593f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.065f,
                    dy1 = -0.813f,
                )
                // a 0.498 0.498 0 0 1 0.737 0.086
                arcToRelative(
                    a = 0.498f,
                    b = 0.498f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.737f,
                    dy1 = 0.086f,
                )
                // a 1.2 1.2 0 0 1 0.257 0.73
                arcToRelative(
                    a = 1.2f,
                    b = 1.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.257f,
                    dy1 = 0.73f,
                )
                // c 0 0.744 -0.658 1.325 -1.947 1.722
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.744f,
                    dx2 = -0.658f,
                    dy2 = 1.325f,
                    dx3 = -1.947f,
                    dy3 = 1.722f,
                )
                // c -1.058 0.332 -2.453 0.51 -3.932 0.51z
                curveToRelative(
                    dx1 = -1.058f,
                    dy1 = 0.332f,
                    dx2 = -2.453f,
                    dy2 = 0.51f,
                    dx3 = -3.932f,
                    dy3 = 0.51f,
                )
                close()
                // m -0.02 2.664
                moveToRelative(dx = -0.02f, dy = 2.664f)
                // c -1.887 0 -3.788 -0.539 -3.788 -1.736
                curveToRelative(
                    dx1 = -1.887f,
                    dy1 = 0.0f,
                    dx2 = -3.788f,
                    dy2 = -0.539f,
                    dx3 = -3.788f,
                    dy3 = -1.736f,
                )
                // c 0 -0.206 0.06 -0.404 0.178 -0.588
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.206f,
                    dx2 = 0.06f,
                    dy2 = -0.404f,
                    dx3 = 0.178f,
                    dy3 = -0.588f,
                )
                // a 0.503 0.503 0 0 1 0.73 -0.142
                arcToRelative(
                    a = 0.503f,
                    b = 0.503f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.73f,
                    dy1 = -0.142f,
                )
                // a 0.596 0.596 0 0 1 0.17 0.723
                arcToRelative(
                    a = 0.596f,
                    b = 0.596f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.17f,
                    dy1 = 0.723f,
                )
                // c 0.218 0.212 1.138 0.616 2.71 0.616
                curveToRelative(
                    dx1 = 0.218f,
                    dy1 = 0.212f,
                    dx2 = 1.138f,
                    dy2 = 0.616f,
                    dx3 = 2.71f,
                    dy3 = 0.616f,
                )
                // s 2.492 -0.396 2.71 -0.616
                reflectiveCurveToRelative(
                    dx1 = 2.492f,
                    dy1 = -0.396f,
                    dx2 = 2.71f,
                    dy2 = -0.616f,
                )
                // a 0.595 0.595 0 0 1 0.17 -0.716
                arcToRelative(
                    a = 0.595f,
                    b = 0.595f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.17f,
                    dy1 = -0.716f,
                )
                // a 0.498 0.498 0 0 1 0.73 0.142
                arcToRelative(
                    a = 0.498f,
                    b = 0.498f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.73f,
                    dy1 = 0.142f,
                )
                // c 0.119 0.184 0.178 0.382 0.178 0.588
                curveToRelative(
                    dx1 = 0.119f,
                    dy1 = 0.184f,
                    dx2 = 0.178f,
                    dy2 = 0.382f,
                    dx3 = 0.178f,
                    dy3 = 0.588f,
                )
                // c 0 0.411 -0.23 0.978 -1.315 1.368
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.411f,
                    dx2 = -0.23f,
                    dy2 = 0.978f,
                    dx3 = -1.315f,
                    dy3 = 1.368f,
                )
                // c -0.665 0.226 -1.546 0.361 -2.473 0.361z
                curveToRelative(
                    dx1 = -0.665f,
                    dy1 = 0.226f,
                    dx2 = -1.546f,
                    dy2 = 0.361f,
                    dx3 = -2.473f,
                    dy3 = 0.361f,
                )
                close()
            }
            // M7.78 9.389 c-1.986 0 -3.867 -.262 -5.288 -.744 C.434 7.957 0 7.036 0 6.385 c0 -.808 .658 -1.368 1.203 -1.694 a.514 .514 0 0 1 .717 .22 .595 .595 0 0 1 -.204 .772 c-.493 .29 -.664 .553 -.664 .701 0 .638 2.374 1.871 6.728 1.871 4.353 0 6.727 -1.24 6.727 -1.87 0 -.15 -.17 -.412 -.664 -.702 -.257 -.149 -.348 -.496 -.204 -.772 a.502 .502 0 0 1 .717 -.22 c.552 .326 1.203 .886 1.203 1.693 0 .652 -.434 1.573 -2.492 2.268 -1.42 .475 -3.301 .737 -5.287 .737Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.78 9.389
                moveTo(x = 7.78f, y = 9.389f)
                // c -1.986 0 -3.867 -0.262 -5.288 -0.744
                curveToRelative(
                    dx1 = -1.986f,
                    dy1 = 0.0f,
                    dx2 = -3.867f,
                    dy2 = -0.262f,
                    dx3 = -5.288f,
                    dy3 = -0.744f,
                )
                // C 0.434 7.957 0 7.036 0 6.385
                curveTo(
                    x1 = 0.434f,
                    y1 = 7.957f,
                    x2 = 0.0f,
                    y2 = 7.036f,
                    x3 = 0.0f,
                    y3 = 6.385f,
                )
                // c 0 -0.808 0.658 -1.368 1.203 -1.694
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.808f,
                    dx2 = 0.658f,
                    dy2 = -1.368f,
                    dx3 = 1.203f,
                    dy3 = -1.694f,
                )
                // a 0.514 0.514 0 0 1 0.717 0.22
                arcToRelative(
                    a = 0.514f,
                    b = 0.514f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.717f,
                    dy1 = 0.22f,
                )
                // a 0.595 0.595 0 0 1 -0.204 0.772
                arcToRelative(
                    a = 0.595f,
                    b = 0.595f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.204f,
                    dy1 = 0.772f,
                )
                // c -0.493 0.29 -0.664 0.553 -0.664 0.701
                curveToRelative(
                    dx1 = -0.493f,
                    dy1 = 0.29f,
                    dx2 = -0.664f,
                    dy2 = 0.553f,
                    dx3 = -0.664f,
                    dy3 = 0.701f,
                )
                // c 0 0.638 2.374 1.871 6.728 1.871
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.638f,
                    dx2 = 2.374f,
                    dy2 = 1.871f,
                    dx3 = 6.728f,
                    dy3 = 1.871f,
                )
                // c 4.353 0 6.727 -1.24 6.727 -1.87
                curveToRelative(
                    dx1 = 4.353f,
                    dy1 = 0.0f,
                    dx2 = 6.727f,
                    dy2 = -1.24f,
                    dx3 = 6.727f,
                    dy3 = -1.87f,
                )
                // c 0 -0.15 -0.17 -0.412 -0.664 -0.702
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.15f,
                    dx2 = -0.17f,
                    dy2 = -0.412f,
                    dx3 = -0.664f,
                    dy3 = -0.702f,
                )
                // c -0.257 -0.149 -0.348 -0.496 -0.204 -0.772
                curveToRelative(
                    dx1 = -0.257f,
                    dy1 = -0.149f,
                    dx2 = -0.348f,
                    dy2 = -0.496f,
                    dx3 = -0.204f,
                    dy3 = -0.772f,
                )
                // a 0.502 0.502 0 0 1 0.717 -0.22
                arcToRelative(
                    a = 0.502f,
                    b = 0.502f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.717f,
                    dy1 = -0.22f,
                )
                // c 0.552 0.326 1.203 0.886 1.203 1.693
                curveToRelative(
                    dx1 = 0.552f,
                    dy1 = 0.326f,
                    dx2 = 1.203f,
                    dy2 = 0.886f,
                    dx3 = 1.203f,
                    dy3 = 1.693f,
                )
                // c 0 0.652 -0.434 1.573 -2.492 2.268
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.652f,
                    dx2 = -0.434f,
                    dy2 = 1.573f,
                    dx3 = -2.492f,
                    dy3 = 2.268f,
                )
                // c -1.42 0.475 -3.301 0.737 -5.287 0.737z
                curveToRelative(
                    dx1 = -1.42f,
                    dy1 = 0.475f,
                    dx2 = -3.301f,
                    dy2 = 0.737f,
                    dx3 = -5.287f,
                    dy3 = 0.737f,
                )
                close()
            }
            // M4.32 5.747 c.934 .354 2.177 .545 3.486 .545 1.309 0 2.545 -.191 3.485 -.545 1.427 -.539 1.73 -1.262 1.73 -1.779 0 -.383 -.178 -.95 -1 -1.438 a.51 .51 0 0 0 -.716 .226 c-.139 .277 -.046 .624 .21 .773 .368 .22 .454 .404 .454 .446 0 .277 -1.296 1.19 -4.163 1.19 s-4.163 -.913 -4.163 -1.19 c0 -.05 .08 -.227 .454 -.446 a.595 .595 0 0 0 .21 -.773 c-.144 -.276 -.46 -.375 -.716 -.226 -.829 .489 -1 1.055 -1 1.438 0 .517 .303 1.247 1.73 1.779Z M4.058 16 c.308 0 .559 -.27 .559 -.602 0 -.333 -.25 -.603 -.56 -.603 -.308 0 -.558 .27 -.558 .603 0 .332 .25 .602 .559 .602Z m8.207 -.68 c0 .332 -.25 .602 -.56 .602 -.308 0 -.558 -.27 -.558 -.602 0 -.333 .25 -.603 .559 -.603 .308 0 .559 .27 .559 .603Z M.822 12.372 c.36 0 .651 -.314 .651 -.701 0 -.388 -.291 -.702 -.651 -.702 -.36 0 -.651 .314 -.651 .702 0 .387 .291 .701 .651 .701Z M16 10.012 c0 .388 -.291 .702 -.651 .702 -.36 0 -.651 -.314 -.651 -.702 0 -.387 .291 -.701 .651 -.701 .36 0 .651 .314 .651 .701Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.32 5.747
                moveTo(x = 4.32f, y = 5.747f)
                // c 0.934 0.354 2.177 0.545 3.486 0.545
                curveToRelative(
                    dx1 = 0.934f,
                    dy1 = 0.354f,
                    dx2 = 2.177f,
                    dy2 = 0.545f,
                    dx3 = 3.486f,
                    dy3 = 0.545f,
                )
                // c 1.309 0 2.545 -0.191 3.485 -0.545
                curveToRelative(
                    dx1 = 1.309f,
                    dy1 = 0.0f,
                    dx2 = 2.545f,
                    dy2 = -0.191f,
                    dx3 = 3.485f,
                    dy3 = -0.545f,
                )
                // c 1.427 -0.539 1.73 -1.262 1.73 -1.779
                curveToRelative(
                    dx1 = 1.427f,
                    dy1 = -0.539f,
                    dx2 = 1.73f,
                    dy2 = -1.262f,
                    dx3 = 1.73f,
                    dy3 = -1.779f,
                )
                // c 0 -0.383 -0.178 -0.95 -1 -1.438
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.383f,
                    dx2 = -0.178f,
                    dy2 = -0.95f,
                    dx3 = -1.0f,
                    dy3 = -1.438f,
                )
                // a 0.51 0.51 0 0 0 -0.716 0.226
                arcToRelative(
                    a = 0.51f,
                    b = 0.51f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.716f,
                    dy1 = 0.226f,
                )
                // c -0.139 0.277 -0.046 0.624 0.21 0.773
                curveToRelative(
                    dx1 = -0.139f,
                    dy1 = 0.277f,
                    dx2 = -0.046f,
                    dy2 = 0.624f,
                    dx3 = 0.21f,
                    dy3 = 0.773f,
                )
                // c 0.368 0.22 0.454 0.404 0.454 0.446
                curveToRelative(
                    dx1 = 0.368f,
                    dy1 = 0.22f,
                    dx2 = 0.454f,
                    dy2 = 0.404f,
                    dx3 = 0.454f,
                    dy3 = 0.446f,
                )
                // c 0 0.277 -1.296 1.19 -4.163 1.19
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.277f,
                    dx2 = -1.296f,
                    dy2 = 1.19f,
                    dx3 = -4.163f,
                    dy3 = 1.19f,
                )
                // s -4.163 -0.913 -4.163 -1.19
                reflectiveCurveToRelative(
                    dx1 = -4.163f,
                    dy1 = -0.913f,
                    dx2 = -4.163f,
                    dy2 = -1.19f,
                )
                // c 0 -0.05 0.08 -0.227 0.454 -0.446
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.05f,
                    dx2 = 0.08f,
                    dy2 = -0.227f,
                    dx3 = 0.454f,
                    dy3 = -0.446f,
                )
                // a 0.595 0.595 0 0 0 0.21 -0.773
                arcToRelative(
                    a = 0.595f,
                    b = 0.595f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.21f,
                    dy1 = -0.773f,
                )
                // c -0.144 -0.276 -0.46 -0.375 -0.716 -0.226
                curveToRelative(
                    dx1 = -0.144f,
                    dy1 = -0.276f,
                    dx2 = -0.46f,
                    dy2 = -0.375f,
                    dx3 = -0.716f,
                    dy3 = -0.226f,
                )
                // c -0.829 0.489 -1 1.055 -1 1.438
                curveToRelative(
                    dx1 = -0.829f,
                    dy1 = 0.489f,
                    dx2 = -1.0f,
                    dy2 = 1.055f,
                    dx3 = -1.0f,
                    dy3 = 1.438f,
                )
                // c 0 0.517 0.303 1.247 1.73 1.779z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.517f,
                    dx2 = 0.303f,
                    dy2 = 1.247f,
                    dx3 = 1.73f,
                    dy3 = 1.779f,
                )
                close()
                // M 4.058 16
                moveTo(x = 4.058f, y = 16.0f)
                // c 0.308 0 0.559 -0.27 0.559 -0.602
                curveToRelative(
                    dx1 = 0.308f,
                    dy1 = 0.0f,
                    dx2 = 0.559f,
                    dy2 = -0.27f,
                    dx3 = 0.559f,
                    dy3 = -0.602f,
                )
                // c 0 -0.333 -0.25 -0.603 -0.56 -0.603
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.333f,
                    dx2 = -0.25f,
                    dy2 = -0.603f,
                    dx3 = -0.56f,
                    dy3 = -0.603f,
                )
                // c -0.308 0 -0.558 0.27 -0.558 0.603
                curveToRelative(
                    dx1 = -0.308f,
                    dy1 = 0.0f,
                    dx2 = -0.558f,
                    dy2 = 0.27f,
                    dx3 = -0.558f,
                    dy3 = 0.603f,
                )
                // c 0 0.332 0.25 0.602 0.559 0.602z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.332f,
                    dx2 = 0.25f,
                    dy2 = 0.602f,
                    dx3 = 0.559f,
                    dy3 = 0.602f,
                )
                close()
                // m 8.207 -0.68
                moveToRelative(dx = 8.207f, dy = -0.68f)
                // c 0 0.332 -0.25 0.602 -0.56 0.602
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.332f,
                    dx2 = -0.25f,
                    dy2 = 0.602f,
                    dx3 = -0.56f,
                    dy3 = 0.602f,
                )
                // c -0.308 0 -0.558 -0.27 -0.558 -0.602
                curveToRelative(
                    dx1 = -0.308f,
                    dy1 = 0.0f,
                    dx2 = -0.558f,
                    dy2 = -0.27f,
                    dx3 = -0.558f,
                    dy3 = -0.602f,
                )
                // c 0 -0.333 0.25 -0.603 0.559 -0.603
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.333f,
                    dx2 = 0.25f,
                    dy2 = -0.603f,
                    dx3 = 0.559f,
                    dy3 = -0.603f,
                )
                // c 0.308 0 0.559 0.27 0.559 0.603z
                curveToRelative(
                    dx1 = 0.308f,
                    dy1 = 0.0f,
                    dx2 = 0.559f,
                    dy2 = 0.27f,
                    dx3 = 0.559f,
                    dy3 = 0.603f,
                )
                close()
                // M 0.822 12.372
                moveTo(x = 0.822f, y = 12.372f)
                // c 0.36 0 0.651 -0.314 0.651 -0.701
                curveToRelative(
                    dx1 = 0.36f,
                    dy1 = 0.0f,
                    dx2 = 0.651f,
                    dy2 = -0.314f,
                    dx3 = 0.651f,
                    dy3 = -0.701f,
                )
                // c 0 -0.388 -0.291 -0.702 -0.651 -0.702
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.388f,
                    dx2 = -0.291f,
                    dy2 = -0.702f,
                    dx3 = -0.651f,
                    dy3 = -0.702f,
                )
                // c -0.36 0 -0.651 0.314 -0.651 0.702
                curveToRelative(
                    dx1 = -0.36f,
                    dy1 = 0.0f,
                    dx2 = -0.651f,
                    dy2 = 0.314f,
                    dx3 = -0.651f,
                    dy3 = 0.702f,
                )
                // c 0 0.387 0.291 0.701 0.651 0.701z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.387f,
                    dx2 = 0.291f,
                    dy2 = 0.701f,
                    dx3 = 0.651f,
                    dy3 = 0.701f,
                )
                close()
                // M 16 10.012
                moveTo(x = 16.0f, y = 10.012f)
                // c 0 0.388 -0.291 0.702 -0.651 0.702
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.388f,
                    dx2 = -0.291f,
                    dy2 = 0.702f,
                    dx3 = -0.651f,
                    dy3 = 0.702f,
                )
                // c -0.36 0 -0.651 -0.314 -0.651 -0.702
                curveToRelative(
                    dx1 = -0.36f,
                    dy1 = 0.0f,
                    dx2 = -0.651f,
                    dy2 = -0.314f,
                    dx3 = -0.651f,
                    dy3 = -0.702f,
                )
                // c 0 -0.387 0.291 -0.701 0.651 -0.701
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.387f,
                    dx2 = 0.291f,
                    dy2 = -0.701f,
                    dx3 = 0.651f,
                    dy3 = -0.701f,
                )
                // c 0.36 0 0.651 0.314 0.651 0.701z
                curveToRelative(
                    dx1 = 0.36f,
                    dy1 = 0.0f,
                    dx2 = 0.651f,
                    dy2 = 0.314f,
                    dx3 = 0.651f,
                    dy3 = 0.701f,
                )
                close()
            }
        }.build().also { _ic2080 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2080: ImageVector? = null
