package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2346: ImageVector
    get() {
        val current = _ic2346
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2346",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M.287 8.634 c0 .491 .257 .907 .766 1.257 .934 .659 2.347 .984 4.24 .984 .85 0 1.638 -.072 2.37 -.219 .765 -.162 1.396 -.425 1.878 -.781 .48 -.353 .73 -.766 .73 -1.247 0 -.15 -.027 -.29 -.084 -.44 .985 -.241 1.76 -.57 2.32 -.979 .559 -.409 .843 -.887 .843 -1.418 a1.61 1.61 0 0 0 -.063 -.425 C14.91 4.79 15.72 4.019 15.72 3.04 c0 -.625 -.353 -1.175 -1.056 -1.66 C13.274 .463 11.246 0 8.575 0 7.312 0 6.172 .113 5.15 .334 4.037 .562 3.144 .916 2.462 1.397 c-.68 .481 -1.018 1.034 -1.018 1.653 0 .369 .112 .703 .34 1.006 C.947 4.531 .528 5.112 .528 5.8 c0 .531 .263 1 .781 1.403 C.63 7.6 .287 8.075 .287 8.634Z m.425 3.191 c0 .519 .32 .928 .957 1.219 .637 .29 1.447 .44 2.418 .44 .985 0 1.794 -.15 2.438 -.44 .644 -.29 .962 -.703 .962 -1.219 a.714 .714 0 0 0 -.184 -.49 .606 .606 0 0 0 -.475 -.2 .651 .651 0 0 0 -.425 .162 .712 .712 0 0 0 -.24 .403 c-.141 .113 -.397 .213 -.782 .303 -.384 .094 -.815 .14 -1.297 .14 -.78 0 -1.418 -.112 -1.9 -.334 a.664 .664 0 0 0 .185 -.425 .689 .689 0 0 0 -.107 -.44 .717 .717 0 0 0 -.43 -.285 .694 .694 0 0 0 -.498 .091 c-.415 .303 -.622 .65 -.622 1.075Z m.907 -3.19 a.86 .86 0 0 1 .134 -.129 c.063 -.062 .2 -.14 .397 -.24 .197 -.1 .431 -.178 .681 -.247 l.085 -.044 c1.15 .384 2.487 .575 4.018 .575 .675 0 1.285 -.034 1.828 -.113 l.185 .163 c-.063 .113 -.213 .228 -.447 .353 -.284 .15 -.722 .29 -1.319 .403 -.597 .113 -1.225 .178 -1.894 .178 a9.436 9.436 0 0 1 -1.893 -.178 c-.597 -.122 -1.04 -.256 -1.332 -.403 -.237 -.1 -.384 -.206 -.443 -.319Z m.078 5.968 c0 .447 .256 .794 .765 1.034 .51 .241 1.141 .363 1.894 .363 .766 0 1.41 -.122 1.928 -.363 .52 -.24 .782 -.587 .782 -1.034 a.643 .643 0 0 0 -.2 -.475 .648 .648 0 0 0 -.47 -.206 c-.334 0 -.552 .169 -.652 .51 -.275 .168 -.738 .262 -1.39 .262 -.567 0 -1.023 -.085 -1.363 -.263 -.104 -.337 -.316 -.51 -.647 -.51 a.582 .582 0 0 0 -.453 .198 .708 .708 0 0 0 -.194 .484Z m.15 -8.803 c0 -.056 .034 -.122 .106 -.197 .169 -.212 .51 -.425 1.006 -.625 1.363 .731 3.232 1.09 5.607 1.09 1.212 0 2.353 -.112 3.415 -.334 v.063 c0 .106 -.062 .212 -.2 .319 -.29 .256 -.828 .497 -1.622 .73 -.856 .257 -1.934 .385 -3.23 .385 -1.304 0 -2.382 -.128 -3.242 -.384 -.821 -.228 -1.368 -.469 -1.643 -.722 -.135 -.106 -.197 -.213 -.197 -.325Z m.922 -2.75 c0 -.128 .084 -.262 .247 -.419 .318 -.297 .956 -.581 1.9 -.856 1.012 -.297 2.225 -.447 3.643 -.447 1.425 0 2.65 .15 3.679 .447 .956 .278 1.587 .569 1.9 .866 .156 .156 .24 .296 .24 .418 s-.078 .247 -.24 .397 c-.313 .297 -.944 .588 -1.9 .872 -1.029 .297 -2.247 .447 -3.679 .447 -1.418 0 -2.637 -.15 -3.643 -.447 -.95 -.284 -1.588 -.575 -1.9 -.878 -.163 -.153 -.247 -.281 -.247 -.4Z m10.401 7.423 a.197 .197 0 0 0 -.34 0 l-2.804 4.86 c-.075 .13 .02 .292 .17 .292 h5.607 a.194 .194 0 0 0 .17 -.291 l-2.803 -4.861Z m-.637 1.608 c-.024 -.212 .192 -.393 .467 -.393 s.491 .181 .467 .393 l-.211 1.857 h-.512 l-.21 -1.857Z m.845 2.607 a.375 .375 0 1 1 -.75 0 .375 .375 0 0 1 .75 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0.287 8.634
                moveTo(x = 0.287f, y = 8.634f)
                // c 0 0.491 0.257 0.907 0.766 1.257
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.491f,
                    dx2 = 0.257f,
                    dy2 = 0.907f,
                    dx3 = 0.766f,
                    dy3 = 1.257f,
                )
                // c 0.934 0.659 2.347 0.984 4.24 0.984
                curveToRelative(
                    dx1 = 0.934f,
                    dy1 = 0.659f,
                    dx2 = 2.347f,
                    dy2 = 0.984f,
                    dx3 = 4.24f,
                    dy3 = 0.984f,
                )
                // c 0.85 0 1.638 -0.072 2.37 -0.219
                curveToRelative(
                    dx1 = 0.85f,
                    dy1 = 0.0f,
                    dx2 = 1.638f,
                    dy2 = -0.072f,
                    dx3 = 2.37f,
                    dy3 = -0.219f,
                )
                // c 0.765 -0.162 1.396 -0.425 1.878 -0.781
                curveToRelative(
                    dx1 = 0.765f,
                    dy1 = -0.162f,
                    dx2 = 1.396f,
                    dy2 = -0.425f,
                    dx3 = 1.878f,
                    dy3 = -0.781f,
                )
                // c 0.48 -0.353 0.73 -0.766 0.73 -1.247
                curveToRelative(
                    dx1 = 0.48f,
                    dy1 = -0.353f,
                    dx2 = 0.73f,
                    dy2 = -0.766f,
                    dx3 = 0.73f,
                    dy3 = -1.247f,
                )
                // c 0 -0.15 -0.027 -0.29 -0.084 -0.44
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.15f,
                    dx2 = -0.027f,
                    dy2 = -0.29f,
                    dx3 = -0.084f,
                    dy3 = -0.44f,
                )
                // c 0.985 -0.241 1.76 -0.57 2.32 -0.979
                curveToRelative(
                    dx1 = 0.985f,
                    dy1 = -0.241f,
                    dx2 = 1.76f,
                    dy2 = -0.57f,
                    dx3 = 2.32f,
                    dy3 = -0.979f,
                )
                // c 0.559 -0.409 0.843 -0.887 0.843 -1.418
                curveToRelative(
                    dx1 = 0.559f,
                    dy1 = -0.409f,
                    dx2 = 0.843f,
                    dy2 = -0.887f,
                    dx3 = 0.843f,
                    dy3 = -1.418f,
                )
                // a 1.61 1.61 0 0 0 -0.063 -0.425
                arcToRelative(
                    a = 1.61f,
                    b = 1.61f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.063f,
                    dy1 = -0.425f,
                )
                // C 14.91 4.79 15.72 4.019 15.72 3.04
                curveTo(
                    x1 = 14.91f,
                    y1 = 4.79f,
                    x2 = 15.72f,
                    y2 = 4.019f,
                    x3 = 15.72f,
                    y3 = 3.04f,
                )
                // c 0 -0.625 -0.353 -1.175 -1.056 -1.66
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.625f,
                    dx2 = -0.353f,
                    dy2 = -1.175f,
                    dx3 = -1.056f,
                    dy3 = -1.66f,
                )
                // C 13.274 0.463 11.246 0 8.575 0
                curveTo(
                    x1 = 13.274f,
                    y1 = 0.463f,
                    x2 = 11.246f,
                    y2 = 0.0f,
                    x3 = 8.575f,
                    y3 = 0.0f,
                )
                // C 7.312 0 6.172 0.113 5.15 0.334
                curveTo(
                    x1 = 7.312f,
                    y1 = 0.0f,
                    x2 = 6.172f,
                    y2 = 0.113f,
                    x3 = 5.15f,
                    y3 = 0.334f,
                )
                // C 4.037 0.562 3.144 0.916 2.462 1.397
                curveTo(
                    x1 = 4.037f,
                    y1 = 0.562f,
                    x2 = 3.144f,
                    y2 = 0.916f,
                    x3 = 2.462f,
                    y3 = 1.397f,
                )
                // c -0.68 0.481 -1.018 1.034 -1.018 1.653
                curveToRelative(
                    dx1 = -0.68f,
                    dy1 = 0.481f,
                    dx2 = -1.018f,
                    dy2 = 1.034f,
                    dx3 = -1.018f,
                    dy3 = 1.653f,
                )
                // c 0 0.369 0.112 0.703 0.34 1.006
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.369f,
                    dx2 = 0.112f,
                    dy2 = 0.703f,
                    dx3 = 0.34f,
                    dy3 = 1.006f,
                )
                // C 0.947 4.531 0.528 5.112 0.528 5.8
                curveTo(
                    x1 = 0.947f,
                    y1 = 4.531f,
                    x2 = 0.528f,
                    y2 = 5.112f,
                    x3 = 0.528f,
                    y3 = 5.8f,
                )
                // c 0 0.531 0.263 1 0.781 1.403
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.531f,
                    dx2 = 0.263f,
                    dy2 = 1.0f,
                    dx3 = 0.781f,
                    dy3 = 1.403f,
                )
                // C 0.63 7.6 0.287 8.075 0.287 8.634z
                curveTo(
                    x1 = 0.63f,
                    y1 = 7.6f,
                    x2 = 0.287f,
                    y2 = 8.075f,
                    x3 = 0.287f,
                    y3 = 8.634f,
                )
                close()
                // m 0.425 3.191
                moveToRelative(dx = 0.425f, dy = 3.191f)
                // c 0 0.519 0.32 0.928 0.957 1.219
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.519f,
                    dx2 = 0.32f,
                    dy2 = 0.928f,
                    dx3 = 0.957f,
                    dy3 = 1.219f,
                )
                // c 0.637 0.29 1.447 0.44 2.418 0.44
                curveToRelative(
                    dx1 = 0.637f,
                    dy1 = 0.29f,
                    dx2 = 1.447f,
                    dy2 = 0.44f,
                    dx3 = 2.418f,
                    dy3 = 0.44f,
                )
                // c 0.985 0 1.794 -0.15 2.438 -0.44
                curveToRelative(
                    dx1 = 0.985f,
                    dy1 = 0.0f,
                    dx2 = 1.794f,
                    dy2 = -0.15f,
                    dx3 = 2.438f,
                    dy3 = -0.44f,
                )
                // c 0.644 -0.29 0.962 -0.703 0.962 -1.219
                curveToRelative(
                    dx1 = 0.644f,
                    dy1 = -0.29f,
                    dx2 = 0.962f,
                    dy2 = -0.703f,
                    dx3 = 0.962f,
                    dy3 = -1.219f,
                )
                // a 0.714 0.714 0 0 0 -0.184 -0.49
                arcToRelative(
                    a = 0.714f,
                    b = 0.714f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.184f,
                    dy1 = -0.49f,
                )
                // a 0.606 0.606 0 0 0 -0.475 -0.2
                arcToRelative(
                    a = 0.606f,
                    b = 0.606f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.475f,
                    dy1 = -0.2f,
                )
                // a 0.651 0.651 0 0 0 -0.425 0.162
                arcToRelative(
                    a = 0.651f,
                    b = 0.651f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.425f,
                    dy1 = 0.162f,
                )
                // a 0.712 0.712 0 0 0 -0.24 0.403
                arcToRelative(
                    a = 0.712f,
                    b = 0.712f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.24f,
                    dy1 = 0.403f,
                )
                // c -0.141 0.113 -0.397 0.213 -0.782 0.303
                curveToRelative(
                    dx1 = -0.141f,
                    dy1 = 0.113f,
                    dx2 = -0.397f,
                    dy2 = 0.213f,
                    dx3 = -0.782f,
                    dy3 = 0.303f,
                )
                // c -0.384 0.094 -0.815 0.14 -1.297 0.14
                curveToRelative(
                    dx1 = -0.384f,
                    dy1 = 0.094f,
                    dx2 = -0.815f,
                    dy2 = 0.14f,
                    dx3 = -1.297f,
                    dy3 = 0.14f,
                )
                // c -0.78 0 -1.418 -0.112 -1.9 -0.334
                curveToRelative(
                    dx1 = -0.78f,
                    dy1 = 0.0f,
                    dx2 = -1.418f,
                    dy2 = -0.112f,
                    dx3 = -1.9f,
                    dy3 = -0.334f,
                )
                // a 0.664 0.664 0 0 0 0.185 -0.425
                arcToRelative(
                    a = 0.664f,
                    b = 0.664f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.185f,
                    dy1 = -0.425f,
                )
                // a 0.689 0.689 0 0 0 -0.107 -0.44
                arcToRelative(
                    a = 0.689f,
                    b = 0.689f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.107f,
                    dy1 = -0.44f,
                )
                // a 0.717 0.717 0 0 0 -0.43 -0.285
                arcToRelative(
                    a = 0.717f,
                    b = 0.717f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.43f,
                    dy1 = -0.285f,
                )
                // a 0.694 0.694 0 0 0 -0.498 0.091
                arcToRelative(
                    a = 0.694f,
                    b = 0.694f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.498f,
                    dy1 = 0.091f,
                )
                // c -0.415 0.303 -0.622 0.65 -0.622 1.075z
                curveToRelative(
                    dx1 = -0.415f,
                    dy1 = 0.303f,
                    dx2 = -0.622f,
                    dy2 = 0.65f,
                    dx3 = -0.622f,
                    dy3 = 1.075f,
                )
                close()
                // m 0.907 -3.19
                moveToRelative(dx = 0.907f, dy = -3.19f)
                // a 0.86 0.86 0 0 1 0.134 -0.129
                arcToRelative(
                    a = 0.86f,
                    b = 0.86f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.134f,
                    dy1 = -0.129f,
                )
                // c 0.063 -0.062 0.2 -0.14 0.397 -0.24
                curveToRelative(
                    dx1 = 0.063f,
                    dy1 = -0.062f,
                    dx2 = 0.2f,
                    dy2 = -0.14f,
                    dx3 = 0.397f,
                    dy3 = -0.24f,
                )
                // c 0.197 -0.1 0.431 -0.178 0.681 -0.247
                curveToRelative(
                    dx1 = 0.197f,
                    dy1 = -0.1f,
                    dx2 = 0.431f,
                    dy2 = -0.178f,
                    dx3 = 0.681f,
                    dy3 = -0.247f,
                )
                // l 0.085 -0.044
                lineToRelative(dx = 0.085f, dy = -0.044f)
                // c 1.15 0.384 2.487 0.575 4.018 0.575
                curveToRelative(
                    dx1 = 1.15f,
                    dy1 = 0.384f,
                    dx2 = 2.487f,
                    dy2 = 0.575f,
                    dx3 = 4.018f,
                    dy3 = 0.575f,
                )
                // c 0.675 0 1.285 -0.034 1.828 -0.113
                curveToRelative(
                    dx1 = 0.675f,
                    dy1 = 0.0f,
                    dx2 = 1.285f,
                    dy2 = -0.034f,
                    dx3 = 1.828f,
                    dy3 = -0.113f,
                )
                // l 0.185 0.163
                lineToRelative(dx = 0.185f, dy = 0.163f)
                // c -0.063 0.113 -0.213 0.228 -0.447 0.353
                curveToRelative(
                    dx1 = -0.063f,
                    dy1 = 0.113f,
                    dx2 = -0.213f,
                    dy2 = 0.228f,
                    dx3 = -0.447f,
                    dy3 = 0.353f,
                )
                // c -0.284 0.15 -0.722 0.29 -1.319 0.403
                curveToRelative(
                    dx1 = -0.284f,
                    dy1 = 0.15f,
                    dx2 = -0.722f,
                    dy2 = 0.29f,
                    dx3 = -1.319f,
                    dy3 = 0.403f,
                )
                // c -0.597 0.113 -1.225 0.178 -1.894 0.178
                curveToRelative(
                    dx1 = -0.597f,
                    dy1 = 0.113f,
                    dx2 = -1.225f,
                    dy2 = 0.178f,
                    dx3 = -1.894f,
                    dy3 = 0.178f,
                )
                // a 9.436 9.436 0 0 1 -1.893 -0.178
                arcToRelative(
                    a = 9.436f,
                    b = 9.436f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.893f,
                    dy1 = -0.178f,
                )
                // c -0.597 -0.122 -1.04 -0.256 -1.332 -0.403
                curveToRelative(
                    dx1 = -0.597f,
                    dy1 = -0.122f,
                    dx2 = -1.04f,
                    dy2 = -0.256f,
                    dx3 = -1.332f,
                    dy3 = -0.403f,
                )
                // c -0.237 -0.1 -0.384 -0.206 -0.443 -0.319z
                curveToRelative(
                    dx1 = -0.237f,
                    dy1 = -0.1f,
                    dx2 = -0.384f,
                    dy2 = -0.206f,
                    dx3 = -0.443f,
                    dy3 = -0.319f,
                )
                close()
                // m 0.078 5.968
                moveToRelative(dx = 0.078f, dy = 5.968f)
                // c 0 0.447 0.256 0.794 0.765 1.034
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.447f,
                    dx2 = 0.256f,
                    dy2 = 0.794f,
                    dx3 = 0.765f,
                    dy3 = 1.034f,
                )
                // c 0.51 0.241 1.141 0.363 1.894 0.363
                curveToRelative(
                    dx1 = 0.51f,
                    dy1 = 0.241f,
                    dx2 = 1.141f,
                    dy2 = 0.363f,
                    dx3 = 1.894f,
                    dy3 = 0.363f,
                )
                // c 0.766 0 1.41 -0.122 1.928 -0.363
                curveToRelative(
                    dx1 = 0.766f,
                    dy1 = 0.0f,
                    dx2 = 1.41f,
                    dy2 = -0.122f,
                    dx3 = 1.928f,
                    dy3 = -0.363f,
                )
                // c 0.52 -0.24 0.782 -0.587 0.782 -1.034
                curveToRelative(
                    dx1 = 0.52f,
                    dy1 = -0.24f,
                    dx2 = 0.782f,
                    dy2 = -0.587f,
                    dx3 = 0.782f,
                    dy3 = -1.034f,
                )
                // a 0.643 0.643 0 0 0 -0.2 -0.475
                arcToRelative(
                    a = 0.643f,
                    b = 0.643f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.2f,
                    dy1 = -0.475f,
                )
                // a 0.648 0.648 0 0 0 -0.47 -0.206
                arcToRelative(
                    a = 0.648f,
                    b = 0.648f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.47f,
                    dy1 = -0.206f,
                )
                // c -0.334 0 -0.552 0.169 -0.652 0.51
                curveToRelative(
                    dx1 = -0.334f,
                    dy1 = 0.0f,
                    dx2 = -0.552f,
                    dy2 = 0.169f,
                    dx3 = -0.652f,
                    dy3 = 0.51f,
                )
                // c -0.275 0.168 -0.738 0.262 -1.39 0.262
                curveToRelative(
                    dx1 = -0.275f,
                    dy1 = 0.168f,
                    dx2 = -0.738f,
                    dy2 = 0.262f,
                    dx3 = -1.39f,
                    dy3 = 0.262f,
                )
                // c -0.567 0 -1.023 -0.085 -1.363 -0.263
                curveToRelative(
                    dx1 = -0.567f,
                    dy1 = 0.0f,
                    dx2 = -1.023f,
                    dy2 = -0.085f,
                    dx3 = -1.363f,
                    dy3 = -0.263f,
                )
                // c -0.104 -0.337 -0.316 -0.51 -0.647 -0.51
                curveToRelative(
                    dx1 = -0.104f,
                    dy1 = -0.337f,
                    dx2 = -0.316f,
                    dy2 = -0.51f,
                    dx3 = -0.647f,
                    dy3 = -0.51f,
                )
                // a 0.582 0.582 0 0 0 -0.453 0.198
                arcToRelative(
                    a = 0.582f,
                    b = 0.582f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.453f,
                    dy1 = 0.198f,
                )
                // a 0.708 0.708 0 0 0 -0.194 0.484z
                arcToRelative(
                    a = 0.708f,
                    b = 0.708f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.194f,
                    dy1 = 0.484f,
                )
                close()
                // m 0.15 -8.803
                moveToRelative(dx = 0.15f, dy = -8.803f)
                // c 0 -0.056 0.034 -0.122 0.106 -0.197
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.056f,
                    dx2 = 0.034f,
                    dy2 = -0.122f,
                    dx3 = 0.106f,
                    dy3 = -0.197f,
                )
                // c 0.169 -0.212 0.51 -0.425 1.006 -0.625
                curveToRelative(
                    dx1 = 0.169f,
                    dy1 = -0.212f,
                    dx2 = 0.51f,
                    dy2 = -0.425f,
                    dx3 = 1.006f,
                    dy3 = -0.625f,
                )
                // c 1.363 0.731 3.232 1.09 5.607 1.09
                curveToRelative(
                    dx1 = 1.363f,
                    dy1 = 0.731f,
                    dx2 = 3.232f,
                    dy2 = 1.09f,
                    dx3 = 5.607f,
                    dy3 = 1.09f,
                )
                // c 1.212 0 2.353 -0.112 3.415 -0.334
                curveToRelative(
                    dx1 = 1.212f,
                    dy1 = 0.0f,
                    dx2 = 2.353f,
                    dy2 = -0.112f,
                    dx3 = 3.415f,
                    dy3 = -0.334f,
                )
                // v 0.063
                verticalLineToRelative(dy = 0.063f)
                // c 0 0.106 -0.062 0.212 -0.2 0.319
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.106f,
                    dx2 = -0.062f,
                    dy2 = 0.212f,
                    dx3 = -0.2f,
                    dy3 = 0.319f,
                )
                // c -0.29 0.256 -0.828 0.497 -1.622 0.73
                curveToRelative(
                    dx1 = -0.29f,
                    dy1 = 0.256f,
                    dx2 = -0.828f,
                    dy2 = 0.497f,
                    dx3 = -1.622f,
                    dy3 = 0.73f,
                )
                // c -0.856 0.257 -1.934 0.385 -3.23 0.385
                curveToRelative(
                    dx1 = -0.856f,
                    dy1 = 0.257f,
                    dx2 = -1.934f,
                    dy2 = 0.385f,
                    dx3 = -3.23f,
                    dy3 = 0.385f,
                )
                // c -1.304 0 -2.382 -0.128 -3.242 -0.384
                curveToRelative(
                    dx1 = -1.304f,
                    dy1 = 0.0f,
                    dx2 = -2.382f,
                    dy2 = -0.128f,
                    dx3 = -3.242f,
                    dy3 = -0.384f,
                )
                // c -0.821 -0.228 -1.368 -0.469 -1.643 -0.722
                curveToRelative(
                    dx1 = -0.821f,
                    dy1 = -0.228f,
                    dx2 = -1.368f,
                    dy2 = -0.469f,
                    dx3 = -1.643f,
                    dy3 = -0.722f,
                )
                // c -0.135 -0.106 -0.197 -0.213 -0.197 -0.325z
                curveToRelative(
                    dx1 = -0.135f,
                    dy1 = -0.106f,
                    dx2 = -0.197f,
                    dy2 = -0.213f,
                    dx3 = -0.197f,
                    dy3 = -0.325f,
                )
                close()
                // m 0.922 -2.75
                moveToRelative(dx = 0.922f, dy = -2.75f)
                // c 0 -0.128 0.084 -0.262 0.247 -0.419
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.128f,
                    dx2 = 0.084f,
                    dy2 = -0.262f,
                    dx3 = 0.247f,
                    dy3 = -0.419f,
                )
                // c 0.318 -0.297 0.956 -0.581 1.9 -0.856
                curveToRelative(
                    dx1 = 0.318f,
                    dy1 = -0.297f,
                    dx2 = 0.956f,
                    dy2 = -0.581f,
                    dx3 = 1.9f,
                    dy3 = -0.856f,
                )
                // c 1.012 -0.297 2.225 -0.447 3.643 -0.447
                curveToRelative(
                    dx1 = 1.012f,
                    dy1 = -0.297f,
                    dx2 = 2.225f,
                    dy2 = -0.447f,
                    dx3 = 3.643f,
                    dy3 = -0.447f,
                )
                // c 1.425 0 2.65 0.15 3.679 0.447
                curveToRelative(
                    dx1 = 1.425f,
                    dy1 = 0.0f,
                    dx2 = 2.65f,
                    dy2 = 0.15f,
                    dx3 = 3.679f,
                    dy3 = 0.447f,
                )
                // c 0.956 0.278 1.587 0.569 1.9 0.866
                curveToRelative(
                    dx1 = 0.956f,
                    dy1 = 0.278f,
                    dx2 = 1.587f,
                    dy2 = 0.569f,
                    dx3 = 1.9f,
                    dy3 = 0.866f,
                )
                // c 0.156 0.156 0.24 0.296 0.24 0.418
                curveToRelative(
                    dx1 = 0.156f,
                    dy1 = 0.156f,
                    dx2 = 0.24f,
                    dy2 = 0.296f,
                    dx3 = 0.24f,
                    dy3 = 0.418f,
                )
                // s -0.078 0.247 -0.24 0.397
                reflectiveCurveToRelative(
                    dx1 = -0.078f,
                    dy1 = 0.247f,
                    dx2 = -0.24f,
                    dy2 = 0.397f,
                )
                // c -0.313 0.297 -0.944 0.588 -1.9 0.872
                curveToRelative(
                    dx1 = -0.313f,
                    dy1 = 0.297f,
                    dx2 = -0.944f,
                    dy2 = 0.588f,
                    dx3 = -1.9f,
                    dy3 = 0.872f,
                )
                // c -1.029 0.297 -2.247 0.447 -3.679 0.447
                curveToRelative(
                    dx1 = -1.029f,
                    dy1 = 0.297f,
                    dx2 = -2.247f,
                    dy2 = 0.447f,
                    dx3 = -3.679f,
                    dy3 = 0.447f,
                )
                // c -1.418 0 -2.637 -0.15 -3.643 -0.447
                curveToRelative(
                    dx1 = -1.418f,
                    dy1 = 0.0f,
                    dx2 = -2.637f,
                    dy2 = -0.15f,
                    dx3 = -3.643f,
                    dy3 = -0.447f,
                )
                // c -0.95 -0.284 -1.588 -0.575 -1.9 -0.878
                curveToRelative(
                    dx1 = -0.95f,
                    dy1 = -0.284f,
                    dx2 = -1.588f,
                    dy2 = -0.575f,
                    dx3 = -1.9f,
                    dy3 = -0.878f,
                )
                // c -0.163 -0.153 -0.247 -0.281 -0.247 -0.4z
                curveToRelative(
                    dx1 = -0.163f,
                    dy1 = -0.153f,
                    dx2 = -0.247f,
                    dy2 = -0.281f,
                    dx3 = -0.247f,
                    dy3 = -0.4f,
                )
                close()
                // m 10.401 7.423
                moveToRelative(dx = 10.401f, dy = 7.423f)
                // a 0.197 0.197 0 0 0 -0.34 0
                arcToRelative(
                    a = 0.197f,
                    b = 0.197f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.34f,
                    dy1 = 0.0f,
                )
                // l -2.804 4.86
                lineToRelative(dx = -2.804f, dy = 4.86f)
                // c -0.075 0.13 0.02 0.292 0.17 0.292
                curveToRelative(
                    dx1 = -0.075f,
                    dy1 = 0.13f,
                    dx2 = 0.02f,
                    dy2 = 0.292f,
                    dx3 = 0.17f,
                    dy3 = 0.292f,
                )
                // h 5.607
                horizontalLineToRelative(dx = 5.607f)
                // a 0.194 0.194 0 0 0 0.17 -0.291
                arcToRelative(
                    a = 0.194f,
                    b = 0.194f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.17f,
                    dy1 = -0.291f,
                )
                // l -2.803 -4.861z
                lineToRelative(dx = -2.803f, dy = -4.861f)
                close()
                // m -0.637 1.608
                moveToRelative(dx = -0.637f, dy = 1.608f)
                // c -0.024 -0.212 0.192 -0.393 0.467 -0.393
                curveToRelative(
                    dx1 = -0.024f,
                    dy1 = -0.212f,
                    dx2 = 0.192f,
                    dy2 = -0.393f,
                    dx3 = 0.467f,
                    dy3 = -0.393f,
                )
                // s 0.491 0.181 0.467 0.393
                reflectiveCurveToRelative(
                    dx1 = 0.491f,
                    dy1 = 0.181f,
                    dx2 = 0.467f,
                    dy2 = 0.393f,
                )
                // l -0.211 1.857
                lineToRelative(dx = -0.211f, dy = 1.857f)
                // h -0.512
                horizontalLineToRelative(dx = -0.512f)
                // l -0.21 -1.857z
                lineToRelative(dx = -0.21f, dy = -1.857f)
                close()
                // m 0.845 2.607
                moveToRelative(dx = 0.845f, dy = 2.607f)
                // a 0.375 0.375 0 1 1 -0.75 0
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.75f,
                    dy1 = 0.0f,
                )
                // a 0.375 0.375 0 0 1 0.75 0z
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.75f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic2346 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2346: ImageVector? = null
