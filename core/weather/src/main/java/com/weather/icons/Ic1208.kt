package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1208: ImageVector
    get() {
        val current = _ic1208
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1208",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M11.979 10.603 c.014 .137 .021 .27 .021 .397 a5 5 0 1 1 -10 0 c0 -2.2 2.152 -5.931 4.183 -8.857 .277 -.398 .55 -.78 .817 -1.143 .266 .362 .54 .745 .817 1.143 .872 1.256 1.766 2.66 2.503 4.028 .372 -.175 .653 -.351 .826 -.525 .163 -.163 .288 -.411 .369 -.736 .08 -.322 .11 -.684 .11 -1.035 0 -.098 -.002 -.194 -.006 -.287 l-.265 .266 a.5 .5 0 0 1 -.708 -.708 L12 1.793 l1.354 1.353 a.5 .5 0 0 1 -.708 .708 l-.021 -.022 v.043 c0 .4 -.033 .85 -.14 1.277 -.106 .426 -.294 .865 -.631 1.202 -.285 .284 -.673 .515 -1.076 .707 .443 .901 .797 1.768 1.007 2.545 .157 -.061 .31 -.124 .454 -.19 .412 -.187 .721 -.376 .907 -.562 .164 -.164 .288 -.412 .369 -.737 .08 -.322 .11 -.684 .11 -1.035 0 -.098 -.002 -.194 -.006 -.287 l-.265 .266 a.5 .5 0 1 1 -.707 -.707 L14 5 l1.354 1.354 a.5 .5 0 1 1 -.707 .707 l-.022 -.022 v.043 c0 .4 -.033 .85 -.14 1.278 -.106 .425 -.294 .864 -.631 1.2 -.314 .315 -.755 .563 -1.202 .766 -.216 .099 -.443 .19 -.673 .277Z M3.333 11 a3.666 3.666 0 0 0 7.333 .05 c-.264 .087 -.506 .166 -.722 .244 a4.793 4.793 0 0 0 -.431 .175 1.28 1.28 0 0 0 -.124 .067 l-.036 .025 c-.19 .191 -.238 .309 -.265 .43 a2.502 2.502 0 0 0 -.04 .303 l-.005 .049 c-.01 .121 -.024 .268 -.048 .435 a.5 .5 0 0 1 -.99 -.142 c.02 -.14 .031 -.261 .042 -.383 l.005 -.052 c.013 -.133 .027 -.28 .06 -.427 .073 -.33 .226 -.612 .535 -.92 a1.59 1.59 0 0 1 .432 -.286 c.154 -.074 .334 -.145 .524 -.214 .249 -.09 .541 -.185 .842 -.283 l.077 -.025 a8.76 8.76 0 0 0 -.245 -.786 16.436 16.436 0 0 0 -.742 -1.705 29.005 29.005 0 0 1 -.803 .267 c-.29 .095 -.555 .18 -.788 .265 a4.776 4.776 0 0 0 -.431 .175 1.263 1.263 0 0 0 -.124 .067 .277 .277 0 0 0 -.036 .025 c-.19 .19 -.238 .309 -.265 .43 a2.503 2.503 0 0 0 -.04 .303 l-.005 .049 a7.24 7.24 0 0 1 -.048 .435 .5 .5 0 1 1 -.99 -.142 c.02 -.14 .031 -.261 .042 -.383 l.005 -.052 c.013 -.133 .027 -.28 .06 -.428 .073 -.329 .226 -.611 .534 -.92 .124 -.124 .288 -.215 .433 -.285 .154 -.074 .334 -.145 .524 -.214 .249 -.09 .541 -.185 .842 -.283 l.46 -.15 .162 -.056 A41.105 41.105 0 0 0 7 3.308 a40.457 40.457 0 0 0 -2.159 3.52 c-.467 .869 -.852 1.695 -1.118 2.432 -.272 .754 -.39 1.336 -.39 1.74Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.979 10.603
                moveTo(x = 11.979f, y = 10.603f)
                // c 0.014 0.137 0.021 0.27 0.021 0.397
                curveToRelative(
                    dx1 = 0.014f,
                    dy1 = 0.137f,
                    dx2 = 0.021f,
                    dy2 = 0.27f,
                    dx3 = 0.021f,
                    dy3 = 0.397f,
                )
                // a 5 5 0 1 1 -10 0
                arcToRelative(
                    a = 5.0f,
                    b = 5.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -10.0f,
                    dy1 = 0.0f,
                )
                // c 0 -2.2 2.152 -5.931 4.183 -8.857
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -2.2f,
                    dx2 = 2.152f,
                    dy2 = -5.931f,
                    dx3 = 4.183f,
                    dy3 = -8.857f,
                )
                // c 0.277 -0.398 0.55 -0.78 0.817 -1.143
                curveToRelative(
                    dx1 = 0.277f,
                    dy1 = -0.398f,
                    dx2 = 0.55f,
                    dy2 = -0.78f,
                    dx3 = 0.817f,
                    dy3 = -1.143f,
                )
                // c 0.266 0.362 0.54 0.745 0.817 1.143
                curveToRelative(
                    dx1 = 0.266f,
                    dy1 = 0.362f,
                    dx2 = 0.54f,
                    dy2 = 0.745f,
                    dx3 = 0.817f,
                    dy3 = 1.143f,
                )
                // c 0.872 1.256 1.766 2.66 2.503 4.028
                curveToRelative(
                    dx1 = 0.872f,
                    dy1 = 1.256f,
                    dx2 = 1.766f,
                    dy2 = 2.66f,
                    dx3 = 2.503f,
                    dy3 = 4.028f,
                )
                // c 0.372 -0.175 0.653 -0.351 0.826 -0.525
                curveToRelative(
                    dx1 = 0.372f,
                    dy1 = -0.175f,
                    dx2 = 0.653f,
                    dy2 = -0.351f,
                    dx3 = 0.826f,
                    dy3 = -0.525f,
                )
                // c 0.163 -0.163 0.288 -0.411 0.369 -0.736
                curveToRelative(
                    dx1 = 0.163f,
                    dy1 = -0.163f,
                    dx2 = 0.288f,
                    dy2 = -0.411f,
                    dx3 = 0.369f,
                    dy3 = -0.736f,
                )
                // c 0.08 -0.322 0.11 -0.684 0.11 -1.035
                curveToRelative(
                    dx1 = 0.08f,
                    dy1 = -0.322f,
                    dx2 = 0.11f,
                    dy2 = -0.684f,
                    dx3 = 0.11f,
                    dy3 = -1.035f,
                )
                // c 0 -0.098 -0.002 -0.194 -0.006 -0.287
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.098f,
                    dx2 = -0.002f,
                    dy2 = -0.194f,
                    dx3 = -0.006f,
                    dy3 = -0.287f,
                )
                // l -0.265 0.266
                lineToRelative(dx = -0.265f, dy = 0.266f)
                // a 0.5 0.5 0 0 1 -0.708 -0.708
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.708f,
                    dy1 = -0.708f,
                )
                // L 12 1.793
                lineTo(x = 12.0f, y = 1.793f)
                // l 1.354 1.353
                lineToRelative(dx = 1.354f, dy = 1.353f)
                // a 0.5 0.5 0 0 1 -0.708 0.708
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.708f,
                    dy1 = 0.708f,
                )
                // l -0.021 -0.022
                lineToRelative(dx = -0.021f, dy = -0.022f)
                // v 0.043
                verticalLineToRelative(dy = 0.043f)
                // c 0 0.4 -0.033 0.85 -0.14 1.277
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.4f,
                    dx2 = -0.033f,
                    dy2 = 0.85f,
                    dx3 = -0.14f,
                    dy3 = 1.277f,
                )
                // c -0.106 0.426 -0.294 0.865 -0.631 1.202
                curveToRelative(
                    dx1 = -0.106f,
                    dy1 = 0.426f,
                    dx2 = -0.294f,
                    dy2 = 0.865f,
                    dx3 = -0.631f,
                    dy3 = 1.202f,
                )
                // c -0.285 0.284 -0.673 0.515 -1.076 0.707
                curveToRelative(
                    dx1 = -0.285f,
                    dy1 = 0.284f,
                    dx2 = -0.673f,
                    dy2 = 0.515f,
                    dx3 = -1.076f,
                    dy3 = 0.707f,
                )
                // c 0.443 0.901 0.797 1.768 1.007 2.545
                curveToRelative(
                    dx1 = 0.443f,
                    dy1 = 0.901f,
                    dx2 = 0.797f,
                    dy2 = 1.768f,
                    dx3 = 1.007f,
                    dy3 = 2.545f,
                )
                // c 0.157 -0.061 0.31 -0.124 0.454 -0.19
                curveToRelative(
                    dx1 = 0.157f,
                    dy1 = -0.061f,
                    dx2 = 0.31f,
                    dy2 = -0.124f,
                    dx3 = 0.454f,
                    dy3 = -0.19f,
                )
                // c 0.412 -0.187 0.721 -0.376 0.907 -0.562
                curveToRelative(
                    dx1 = 0.412f,
                    dy1 = -0.187f,
                    dx2 = 0.721f,
                    dy2 = -0.376f,
                    dx3 = 0.907f,
                    dy3 = -0.562f,
                )
                // c 0.164 -0.164 0.288 -0.412 0.369 -0.737
                curveToRelative(
                    dx1 = 0.164f,
                    dy1 = -0.164f,
                    dx2 = 0.288f,
                    dy2 = -0.412f,
                    dx3 = 0.369f,
                    dy3 = -0.737f,
                )
                // c 0.08 -0.322 0.11 -0.684 0.11 -1.035
                curveToRelative(
                    dx1 = 0.08f,
                    dy1 = -0.322f,
                    dx2 = 0.11f,
                    dy2 = -0.684f,
                    dx3 = 0.11f,
                    dy3 = -1.035f,
                )
                // c 0 -0.098 -0.002 -0.194 -0.006 -0.287
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.098f,
                    dx2 = -0.002f,
                    dy2 = -0.194f,
                    dx3 = -0.006f,
                    dy3 = -0.287f,
                )
                // l -0.265 0.266
                lineToRelative(dx = -0.265f, dy = 0.266f)
                // a 0.5 0.5 0 1 1 -0.707 -0.707
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.707f,
                    dy1 = -0.707f,
                )
                // L 14 5
                lineTo(x = 14.0f, y = 5.0f)
                // l 1.354 1.354
                lineToRelative(dx = 1.354f, dy = 1.354f)
                // a 0.5 0.5 0 1 1 -0.707 0.707
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.707f,
                    dy1 = 0.707f,
                )
                // l -0.022 -0.022
                lineToRelative(dx = -0.022f, dy = -0.022f)
                // v 0.043
                verticalLineToRelative(dy = 0.043f)
                // c 0 0.4 -0.033 0.85 -0.14 1.278
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.4f,
                    dx2 = -0.033f,
                    dy2 = 0.85f,
                    dx3 = -0.14f,
                    dy3 = 1.278f,
                )
                // c -0.106 0.425 -0.294 0.864 -0.631 1.2
                curveToRelative(
                    dx1 = -0.106f,
                    dy1 = 0.425f,
                    dx2 = -0.294f,
                    dy2 = 0.864f,
                    dx3 = -0.631f,
                    dy3 = 1.2f,
                )
                // c -0.314 0.315 -0.755 0.563 -1.202 0.766
                curveToRelative(
                    dx1 = -0.314f,
                    dy1 = 0.315f,
                    dx2 = -0.755f,
                    dy2 = 0.563f,
                    dx3 = -1.202f,
                    dy3 = 0.766f,
                )
                // c -0.216 0.099 -0.443 0.19 -0.673 0.277z
                curveToRelative(
                    dx1 = -0.216f,
                    dy1 = 0.099f,
                    dx2 = -0.443f,
                    dy2 = 0.19f,
                    dx3 = -0.673f,
                    dy3 = 0.277f,
                )
                close()
                // M 3.333 11
                moveTo(x = 3.333f, y = 11.0f)
                // a 3.666 3.666 0 0 0 7.333 0.05
                arcToRelative(
                    a = 3.666f,
                    b = 3.666f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 7.333f,
                    dy1 = 0.05f,
                )
                // c -0.264 0.087 -0.506 0.166 -0.722 0.244
                curveToRelative(
                    dx1 = -0.264f,
                    dy1 = 0.087f,
                    dx2 = -0.506f,
                    dy2 = 0.166f,
                    dx3 = -0.722f,
                    dy3 = 0.244f,
                )
                // a 4.793 4.793 0 0 0 -0.431 0.175
                arcToRelative(
                    a = 4.793f,
                    b = 4.793f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.431f,
                    dy1 = 0.175f,
                )
                // a 1.28 1.28 0 0 0 -0.124 0.067
                arcToRelative(
                    a = 1.28f,
                    b = 1.28f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.124f,
                    dy1 = 0.067f,
                )
                // l -0.036 0.025
                lineToRelative(dx = -0.036f, dy = 0.025f)
                // c -0.19 0.191 -0.238 0.309 -0.265 0.43
                curveToRelative(
                    dx1 = -0.19f,
                    dy1 = 0.191f,
                    dx2 = -0.238f,
                    dy2 = 0.309f,
                    dx3 = -0.265f,
                    dy3 = 0.43f,
                )
                // a 2.502 2.502 0 0 0 -0.04 0.303
                arcToRelative(
                    a = 2.502f,
                    b = 2.502f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.04f,
                    dy1 = 0.303f,
                )
                // l -0.005 0.049
                lineToRelative(dx = -0.005f, dy = 0.049f)
                // c -0.01 0.121 -0.024 0.268 -0.048 0.435
                curveToRelative(
                    dx1 = -0.01f,
                    dy1 = 0.121f,
                    dx2 = -0.024f,
                    dy2 = 0.268f,
                    dx3 = -0.048f,
                    dy3 = 0.435f,
                )
                // a 0.5 0.5 0 0 1 -0.99 -0.142
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.99f,
                    dy1 = -0.142f,
                )
                // c 0.02 -0.14 0.031 -0.261 0.042 -0.383
                curveToRelative(
                    dx1 = 0.02f,
                    dy1 = -0.14f,
                    dx2 = 0.031f,
                    dy2 = -0.261f,
                    dx3 = 0.042f,
                    dy3 = -0.383f,
                )
                // l 0.005 -0.052
                lineToRelative(dx = 0.005f, dy = -0.052f)
                // c 0.013 -0.133 0.027 -0.28 0.06 -0.427
                curveToRelative(
                    dx1 = 0.013f,
                    dy1 = -0.133f,
                    dx2 = 0.027f,
                    dy2 = -0.28f,
                    dx3 = 0.06f,
                    dy3 = -0.427f,
                )
                // c 0.073 -0.33 0.226 -0.612 0.535 -0.92
                curveToRelative(
                    dx1 = 0.073f,
                    dy1 = -0.33f,
                    dx2 = 0.226f,
                    dy2 = -0.612f,
                    dx3 = 0.535f,
                    dy3 = -0.92f,
                )
                // a 1.59 1.59 0 0 1 0.432 -0.286
                arcToRelative(
                    a = 1.59f,
                    b = 1.59f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.432f,
                    dy1 = -0.286f,
                )
                // c 0.154 -0.074 0.334 -0.145 0.524 -0.214
                curveToRelative(
                    dx1 = 0.154f,
                    dy1 = -0.074f,
                    dx2 = 0.334f,
                    dy2 = -0.145f,
                    dx3 = 0.524f,
                    dy3 = -0.214f,
                )
                // c 0.249 -0.09 0.541 -0.185 0.842 -0.283
                curveToRelative(
                    dx1 = 0.249f,
                    dy1 = -0.09f,
                    dx2 = 0.541f,
                    dy2 = -0.185f,
                    dx3 = 0.842f,
                    dy3 = -0.283f,
                )
                // l 0.077 -0.025
                lineToRelative(dx = 0.077f, dy = -0.025f)
                // a 8.76 8.76 0 0 0 -0.245 -0.786
                arcToRelative(
                    a = 8.76f,
                    b = 8.76f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.245f,
                    dy1 = -0.786f,
                )
                // a 16.436 16.436 0 0 0 -0.742 -1.705
                arcToRelative(
                    a = 16.436f,
                    b = 16.436f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.742f,
                    dy1 = -1.705f,
                )
                // a 29.005 29.005 0 0 1 -0.803 0.267
                arcToRelative(
                    a = 29.005f,
                    b = 29.005f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.803f,
                    dy1 = 0.267f,
                )
                // c -0.29 0.095 -0.555 0.18 -0.788 0.265
                curveToRelative(
                    dx1 = -0.29f,
                    dy1 = 0.095f,
                    dx2 = -0.555f,
                    dy2 = 0.18f,
                    dx3 = -0.788f,
                    dy3 = 0.265f,
                )
                // a 4.776 4.776 0 0 0 -0.431 0.175
                arcToRelative(
                    a = 4.776f,
                    b = 4.776f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.431f,
                    dy1 = 0.175f,
                )
                // a 1.263 1.263 0 0 0 -0.124 0.067
                arcToRelative(
                    a = 1.263f,
                    b = 1.263f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.124f,
                    dy1 = 0.067f,
                )
                // a 0.277 0.277 0 0 0 -0.036 0.025
                arcToRelative(
                    a = 0.277f,
                    b = 0.277f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.036f,
                    dy1 = 0.025f,
                )
                // c -0.19 0.19 -0.238 0.309 -0.265 0.43
                curveToRelative(
                    dx1 = -0.19f,
                    dy1 = 0.19f,
                    dx2 = -0.238f,
                    dy2 = 0.309f,
                    dx3 = -0.265f,
                    dy3 = 0.43f,
                )
                // a 2.503 2.503 0 0 0 -0.04 0.303
                arcToRelative(
                    a = 2.503f,
                    b = 2.503f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.04f,
                    dy1 = 0.303f,
                )
                // l -0.005 0.049
                lineToRelative(dx = -0.005f, dy = 0.049f)
                // a 7.24 7.24 0 0 1 -0.048 0.435
                arcToRelative(
                    a = 7.24f,
                    b = 7.24f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.048f,
                    dy1 = 0.435f,
                )
                // a 0.5 0.5 0 1 1 -0.99 -0.142
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.99f,
                    dy1 = -0.142f,
                )
                // c 0.02 -0.14 0.031 -0.261 0.042 -0.383
                curveToRelative(
                    dx1 = 0.02f,
                    dy1 = -0.14f,
                    dx2 = 0.031f,
                    dy2 = -0.261f,
                    dx3 = 0.042f,
                    dy3 = -0.383f,
                )
                // l 0.005 -0.052
                lineToRelative(dx = 0.005f, dy = -0.052f)
                // c 0.013 -0.133 0.027 -0.28 0.06 -0.428
                curveToRelative(
                    dx1 = 0.013f,
                    dy1 = -0.133f,
                    dx2 = 0.027f,
                    dy2 = -0.28f,
                    dx3 = 0.06f,
                    dy3 = -0.428f,
                )
                // c 0.073 -0.329 0.226 -0.611 0.534 -0.92
                curveToRelative(
                    dx1 = 0.073f,
                    dy1 = -0.329f,
                    dx2 = 0.226f,
                    dy2 = -0.611f,
                    dx3 = 0.534f,
                    dy3 = -0.92f,
                )
                // c 0.124 -0.124 0.288 -0.215 0.433 -0.285
                curveToRelative(
                    dx1 = 0.124f,
                    dy1 = -0.124f,
                    dx2 = 0.288f,
                    dy2 = -0.215f,
                    dx3 = 0.433f,
                    dy3 = -0.285f,
                )
                // c 0.154 -0.074 0.334 -0.145 0.524 -0.214
                curveToRelative(
                    dx1 = 0.154f,
                    dy1 = -0.074f,
                    dx2 = 0.334f,
                    dy2 = -0.145f,
                    dx3 = 0.524f,
                    dy3 = -0.214f,
                )
                // c 0.249 -0.09 0.541 -0.185 0.842 -0.283
                curveToRelative(
                    dx1 = 0.249f,
                    dy1 = -0.09f,
                    dx2 = 0.541f,
                    dy2 = -0.185f,
                    dx3 = 0.842f,
                    dy3 = -0.283f,
                )
                // l 0.46 -0.15
                lineToRelative(dx = 0.46f, dy = -0.15f)
                // l 0.162 -0.056
                lineToRelative(dx = 0.162f, dy = -0.056f)
                // A 41.105 41.105 0 0 0 7 3.308
                arcTo(
                    horizontalEllipseRadius = 41.105f,
                    verticalEllipseRadius = 41.105f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.0f,
                    y1 = 3.308f,
                )
                // a 40.457 40.457 0 0 0 -2.159 3.52
                arcToRelative(
                    a = 40.457f,
                    b = 40.457f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.159f,
                    dy1 = 3.52f,
                )
                // c -0.467 0.869 -0.852 1.695 -1.118 2.432
                curveToRelative(
                    dx1 = -0.467f,
                    dy1 = 0.869f,
                    dx2 = -0.852f,
                    dy2 = 1.695f,
                    dx3 = -1.118f,
                    dy3 = 2.432f,
                )
                // c -0.272 0.754 -0.39 1.336 -0.39 1.74z
                curveToRelative(
                    dx1 = -0.272f,
                    dy1 = 0.754f,
                    dx2 = -0.39f,
                    dy2 = 1.336f,
                    dx3 = -0.39f,
                    dy3 = 1.74f,
                )
                close()
            }
        }.build().also { _ic1208 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1208: ImageVector? = null
