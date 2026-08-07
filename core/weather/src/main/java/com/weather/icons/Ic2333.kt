package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2333: ImageVector
    get() {
        val current = _ic2333
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2333",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M5.436 9.245 c.141 -.251 .274 -.492 .4 -.72 .464 -.841 .845 -1.53 1.28 -2.058 C7.636 5.835 8.185 5.5 9 5.5 c.896 0 1.793 .3 2.457 .779 C12.125 6.761 12.5 7.376 12.5 8 c0 .38 -.278 .73 -.81 1.022 .031 -.138 .047 -.28 .047 -.423 0 -.506 -.255 -1.047 -.68 -1.389 -.455 -.367 -1.098 -.498 -1.78 -.157 -.645 .322 -1.246 1.098 -1.634 1.9 -.389 .803 -.645 1.802 -.415 2.631 .217 .785 .656 1.282 1.21 1.564 .528 .27 1.117 .321 1.61 .321 .649 0 1.098 -.335 1.407 -.567 .348 -.26 .504 -.367 .7 -.367 .2 0 .38 .113 .704 .363 l.051 .04 c.266 .207 .681 .531 1.21 .531 .5 0 .838 -.175 1.113 -.431 .12 -.111 .226 -.238 .317 -.346 l.006 -.007 a5.18 5.18 0 0 1 .298 -.331 .5 .5 0 0 0 -.708 -.708 c-.138 .139 -.252 .274 -.346 .384 l-.007 .01 c-.1 .118 -.171 .2 -.242 .267 -.112 .103 -.213 .162 -.432 .162 -.155 0 -.306 -.097 -.649 -.362 l-.022 -.018 c-.282 -.218 -.717 -.554 -1.293 -.554 -.552 0 -.965 .313 -1.24 .523 l-.06 .044 c-.313 .235 -.523 .367 -.807 .367 -.441 0 -.84 -.05 -1.156 -.212 -.291 -.149 -.554 -.412 -.7 -.94 -.135 -.484 .004 -1.21 .351 -1.929 .349 -.72 .825 -1.263 1.18 -1.44 .318 -.16 .543 -.092 .707 .04 a.849 .849 0 0 1 .307 .611 c0 .228 -.089 .465 -.238 .64 -.146 .175 -.324 .261 -.499 .261 a.5 .5 0 0 0 0 1 c.495 0 1.321 -.17 2.034 -.529 C12.726 9.623 13.5 8.995 13.5 8 c0 -1.047 -.625 -1.931 -1.457 -2.532 A5.301 5.301 0 0 0 9 4.5 c-1.185 0 -1.996 .53 -2.656 1.331 -.5 .606 -.94 1.405 -1.41 2.26 -.121 .218 -.244 .44 -.37 .664 -.163 .29 -.325 .59 -.488 .891 -.318 .591 -.638 1.184 -.97 1.701 -.247 .386 -.479 .693 -.693 .9 -.224 .216 -.359 .253 -.413 .253 -.077 0 -.218 -.045 -.445 -.214 a6.863 6.863 0 0 1 -.701 -.64 .5 .5 0 0 0 -.708 .708 c.247 .246 .526 .522 .812 .735 .277 .206 .636 .411 1.042 .411 .446 0 .82 -.257 1.107 -.534 .298 -.286 .58 -.67 .841 -1.08 .351 -.546 .705 -1.202 1.035 -1.813 .157 -.291 .31 -.573 .453 -.828Z M.777 14.144 a.5 .5 0 0 0 -.554 .832 l1.017 .678 a1.5 1.5 0 0 0 1.503 .094 l1.033 -.517 a.5 .5 0 0 1 .448 0 l1.105 .553 a1.5 1.5 0 0 0 1.342 0 l1.105 -.553 a.5 .5 0 0 1 .448 0 l1.105 .553 a1.5 1.5 0 0 0 1.342 0 l1.105 -.553 a.5 .5 0 0 1 .448 0 l1.033 .517 a1.5 1.5 0 0 0 1.503 -.094 l1.017 -.678 a.5 .5 0 1 0 -.554 -.832 l-1.017 .678 a.5 .5 0 0 1 -.501 .031 l-1.034 -.517 a1.5 1.5 0 0 0 -1.342 0 l-1.105 .553 a.5 .5 0 0 1 -.448 0 l-1.105 -.553 a1.5 1.5 0 0 0 -1.342 0 l-1.105 .553 a.5 .5 0 0 1 -.448 0 l-1.105 -.553 a1.5 1.5 0 0 0 -1.342 0 l-1.034 .517 a.5 .5 0 0 1 -.5 -.03 l-1.018 -.679Z M3.17 .473 a.197 .197 0 0 0 -.34 0 L.026 5.333 c-.075 .13 .02 .292 .17 .292 h5.607 a.194 .194 0 0 0 .17 -.291 L3.17 .473Z m-.637 1.608 c-.024 -.212 .192 -.393 .467 -.393 s.491 .181 .467 .393 l-.211 1.857 h-.512 l-.21 -1.857Z m.845 2.607 a.375 .375 0 1 1 -.75 0 .375 .375 0 0 1 .75 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5.436 9.245
                moveTo(x = 5.436f, y = 9.245f)
                // c 0.141 -0.251 0.274 -0.492 0.4 -0.72
                curveToRelative(
                    dx1 = 0.141f,
                    dy1 = -0.251f,
                    dx2 = 0.274f,
                    dy2 = -0.492f,
                    dx3 = 0.4f,
                    dy3 = -0.72f,
                )
                // c 0.464 -0.841 0.845 -1.53 1.28 -2.058
                curveToRelative(
                    dx1 = 0.464f,
                    dy1 = -0.841f,
                    dx2 = 0.845f,
                    dy2 = -1.53f,
                    dx3 = 1.28f,
                    dy3 = -2.058f,
                )
                // C 7.636 5.835 8.185 5.5 9 5.5
                curveTo(
                    x1 = 7.636f,
                    y1 = 5.835f,
                    x2 = 8.185f,
                    y2 = 5.5f,
                    x3 = 9.0f,
                    y3 = 5.5f,
                )
                // c 0.896 0 1.793 0.3 2.457 0.779
                curveToRelative(
                    dx1 = 0.896f,
                    dy1 = 0.0f,
                    dx2 = 1.793f,
                    dy2 = 0.3f,
                    dx3 = 2.457f,
                    dy3 = 0.779f,
                )
                // C 12.125 6.761 12.5 7.376 12.5 8
                curveTo(
                    x1 = 12.125f,
                    y1 = 6.761f,
                    x2 = 12.5f,
                    y2 = 7.376f,
                    x3 = 12.5f,
                    y3 = 8.0f,
                )
                // c 0 0.38 -0.278 0.73 -0.81 1.022
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.38f,
                    dx2 = -0.278f,
                    dy2 = 0.73f,
                    dx3 = -0.81f,
                    dy3 = 1.022f,
                )
                // c 0.031 -0.138 0.047 -0.28 0.047 -0.423
                curveToRelative(
                    dx1 = 0.031f,
                    dy1 = -0.138f,
                    dx2 = 0.047f,
                    dy2 = -0.28f,
                    dx3 = 0.047f,
                    dy3 = -0.423f,
                )
                // c 0 -0.506 -0.255 -1.047 -0.68 -1.389
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.506f,
                    dx2 = -0.255f,
                    dy2 = -1.047f,
                    dx3 = -0.68f,
                    dy3 = -1.389f,
                )
                // c -0.455 -0.367 -1.098 -0.498 -1.78 -0.157
                curveToRelative(
                    dx1 = -0.455f,
                    dy1 = -0.367f,
                    dx2 = -1.098f,
                    dy2 = -0.498f,
                    dx3 = -1.78f,
                    dy3 = -0.157f,
                )
                // c -0.645 0.322 -1.246 1.098 -1.634 1.9
                curveToRelative(
                    dx1 = -0.645f,
                    dy1 = 0.322f,
                    dx2 = -1.246f,
                    dy2 = 1.098f,
                    dx3 = -1.634f,
                    dy3 = 1.9f,
                )
                // c -0.389 0.803 -0.645 1.802 -0.415 2.631
                curveToRelative(
                    dx1 = -0.389f,
                    dy1 = 0.803f,
                    dx2 = -0.645f,
                    dy2 = 1.802f,
                    dx3 = -0.415f,
                    dy3 = 2.631f,
                )
                // c 0.217 0.785 0.656 1.282 1.21 1.564
                curveToRelative(
                    dx1 = 0.217f,
                    dy1 = 0.785f,
                    dx2 = 0.656f,
                    dy2 = 1.282f,
                    dx3 = 1.21f,
                    dy3 = 1.564f,
                )
                // c 0.528 0.27 1.117 0.321 1.61 0.321
                curveToRelative(
                    dx1 = 0.528f,
                    dy1 = 0.27f,
                    dx2 = 1.117f,
                    dy2 = 0.321f,
                    dx3 = 1.61f,
                    dy3 = 0.321f,
                )
                // c 0.649 0 1.098 -0.335 1.407 -0.567
                curveToRelative(
                    dx1 = 0.649f,
                    dy1 = 0.0f,
                    dx2 = 1.098f,
                    dy2 = -0.335f,
                    dx3 = 1.407f,
                    dy3 = -0.567f,
                )
                // c 0.348 -0.26 0.504 -0.367 0.7 -0.367
                curveToRelative(
                    dx1 = 0.348f,
                    dy1 = -0.26f,
                    dx2 = 0.504f,
                    dy2 = -0.367f,
                    dx3 = 0.7f,
                    dy3 = -0.367f,
                )
                // c 0.2 0 0.38 0.113 0.704 0.363
                curveToRelative(
                    dx1 = 0.2f,
                    dy1 = 0.0f,
                    dx2 = 0.38f,
                    dy2 = 0.113f,
                    dx3 = 0.704f,
                    dy3 = 0.363f,
                )
                // l 0.051 0.04
                lineToRelative(dx = 0.051f, dy = 0.04f)
                // c 0.266 0.207 0.681 0.531 1.21 0.531
                curveToRelative(
                    dx1 = 0.266f,
                    dy1 = 0.207f,
                    dx2 = 0.681f,
                    dy2 = 0.531f,
                    dx3 = 1.21f,
                    dy3 = 0.531f,
                )
                // c 0.5 0 0.838 -0.175 1.113 -0.431
                curveToRelative(
                    dx1 = 0.5f,
                    dy1 = 0.0f,
                    dx2 = 0.838f,
                    dy2 = -0.175f,
                    dx3 = 1.113f,
                    dy3 = -0.431f,
                )
                // c 0.12 -0.111 0.226 -0.238 0.317 -0.346
                curveToRelative(
                    dx1 = 0.12f,
                    dy1 = -0.111f,
                    dx2 = 0.226f,
                    dy2 = -0.238f,
                    dx3 = 0.317f,
                    dy3 = -0.346f,
                )
                // l 0.006 -0.007
                lineToRelative(dx = 0.006f, dy = -0.007f)
                // a 5.18 5.18 0 0 1 0.298 -0.331
                arcToRelative(
                    a = 5.18f,
                    b = 5.18f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.298f,
                    dy1 = -0.331f,
                )
                // a 0.5 0.5 0 0 0 -0.708 -0.708
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.708f,
                    dy1 = -0.708f,
                )
                // c -0.138 0.139 -0.252 0.274 -0.346 0.384
                curveToRelative(
                    dx1 = -0.138f,
                    dy1 = 0.139f,
                    dx2 = -0.252f,
                    dy2 = 0.274f,
                    dx3 = -0.346f,
                    dy3 = 0.384f,
                )
                // l -0.007 0.01
                lineToRelative(dx = -0.007f, dy = 0.01f)
                // c -0.1 0.118 -0.171 0.2 -0.242 0.267
                curveToRelative(
                    dx1 = -0.1f,
                    dy1 = 0.118f,
                    dx2 = -0.171f,
                    dy2 = 0.2f,
                    dx3 = -0.242f,
                    dy3 = 0.267f,
                )
                // c -0.112 0.103 -0.213 0.162 -0.432 0.162
                curveToRelative(
                    dx1 = -0.112f,
                    dy1 = 0.103f,
                    dx2 = -0.213f,
                    dy2 = 0.162f,
                    dx3 = -0.432f,
                    dy3 = 0.162f,
                )
                // c -0.155 0 -0.306 -0.097 -0.649 -0.362
                curveToRelative(
                    dx1 = -0.155f,
                    dy1 = 0.0f,
                    dx2 = -0.306f,
                    dy2 = -0.097f,
                    dx3 = -0.649f,
                    dy3 = -0.362f,
                )
                // l -0.022 -0.018
                lineToRelative(dx = -0.022f, dy = -0.018f)
                // c -0.282 -0.218 -0.717 -0.554 -1.293 -0.554
                curveToRelative(
                    dx1 = -0.282f,
                    dy1 = -0.218f,
                    dx2 = -0.717f,
                    dy2 = -0.554f,
                    dx3 = -1.293f,
                    dy3 = -0.554f,
                )
                // c -0.552 0 -0.965 0.313 -1.24 0.523
                curveToRelative(
                    dx1 = -0.552f,
                    dy1 = 0.0f,
                    dx2 = -0.965f,
                    dy2 = 0.313f,
                    dx3 = -1.24f,
                    dy3 = 0.523f,
                )
                // l -0.06 0.044
                lineToRelative(dx = -0.06f, dy = 0.044f)
                // c -0.313 0.235 -0.523 0.367 -0.807 0.367
                curveToRelative(
                    dx1 = -0.313f,
                    dy1 = 0.235f,
                    dx2 = -0.523f,
                    dy2 = 0.367f,
                    dx3 = -0.807f,
                    dy3 = 0.367f,
                )
                // c -0.441 0 -0.84 -0.05 -1.156 -0.212
                curveToRelative(
                    dx1 = -0.441f,
                    dy1 = 0.0f,
                    dx2 = -0.84f,
                    dy2 = -0.05f,
                    dx3 = -1.156f,
                    dy3 = -0.212f,
                )
                // c -0.291 -0.149 -0.554 -0.412 -0.7 -0.94
                curveToRelative(
                    dx1 = -0.291f,
                    dy1 = -0.149f,
                    dx2 = -0.554f,
                    dy2 = -0.412f,
                    dx3 = -0.7f,
                    dy3 = -0.94f,
                )
                // c -0.135 -0.484 0.004 -1.21 0.351 -1.929
                curveToRelative(
                    dx1 = -0.135f,
                    dy1 = -0.484f,
                    dx2 = 0.004f,
                    dy2 = -1.21f,
                    dx3 = 0.351f,
                    dy3 = -1.929f,
                )
                // c 0.349 -0.72 0.825 -1.263 1.18 -1.44
                curveToRelative(
                    dx1 = 0.349f,
                    dy1 = -0.72f,
                    dx2 = 0.825f,
                    dy2 = -1.263f,
                    dx3 = 1.18f,
                    dy3 = -1.44f,
                )
                // c 0.318 -0.16 0.543 -0.092 0.707 0.04
                curveToRelative(
                    dx1 = 0.318f,
                    dy1 = -0.16f,
                    dx2 = 0.543f,
                    dy2 = -0.092f,
                    dx3 = 0.707f,
                    dy3 = 0.04f,
                )
                // a 0.849 0.849 0 0 1 0.307 0.611
                arcToRelative(
                    a = 0.849f,
                    b = 0.849f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.307f,
                    dy1 = 0.611f,
                )
                // c 0 0.228 -0.089 0.465 -0.238 0.64
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.228f,
                    dx2 = -0.089f,
                    dy2 = 0.465f,
                    dx3 = -0.238f,
                    dy3 = 0.64f,
                )
                // c -0.146 0.175 -0.324 0.261 -0.499 0.261
                curveToRelative(
                    dx1 = -0.146f,
                    dy1 = 0.175f,
                    dx2 = -0.324f,
                    dy2 = 0.261f,
                    dx3 = -0.499f,
                    dy3 = 0.261f,
                )
                // a 0.5 0.5 0 0 0 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // c 0.495 0 1.321 -0.17 2.034 -0.529
                curveToRelative(
                    dx1 = 0.495f,
                    dy1 = 0.0f,
                    dx2 = 1.321f,
                    dy2 = -0.17f,
                    dx3 = 2.034f,
                    dy3 = -0.529f,
                )
                // C 12.726 9.623 13.5 8.995 13.5 8
                curveTo(
                    x1 = 12.726f,
                    y1 = 9.623f,
                    x2 = 13.5f,
                    y2 = 8.995f,
                    x3 = 13.5f,
                    y3 = 8.0f,
                )
                // c 0 -1.047 -0.625 -1.931 -1.457 -2.532
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.047f,
                    dx2 = -0.625f,
                    dy2 = -1.931f,
                    dx3 = -1.457f,
                    dy3 = -2.532f,
                )
                // A 5.301 5.301 0 0 0 9 4.5
                arcTo(
                    horizontalEllipseRadius = 5.301f,
                    verticalEllipseRadius = 5.301f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 9.0f,
                    y1 = 4.5f,
                )
                // c -1.185 0 -1.996 0.53 -2.656 1.331
                curveToRelative(
                    dx1 = -1.185f,
                    dy1 = 0.0f,
                    dx2 = -1.996f,
                    dy2 = 0.53f,
                    dx3 = -2.656f,
                    dy3 = 1.331f,
                )
                // c -0.5 0.606 -0.94 1.405 -1.41 2.26
                curveToRelative(
                    dx1 = -0.5f,
                    dy1 = 0.606f,
                    dx2 = -0.94f,
                    dy2 = 1.405f,
                    dx3 = -1.41f,
                    dy3 = 2.26f,
                )
                // c -0.121 0.218 -0.244 0.44 -0.37 0.664
                curveToRelative(
                    dx1 = -0.121f,
                    dy1 = 0.218f,
                    dx2 = -0.244f,
                    dy2 = 0.44f,
                    dx3 = -0.37f,
                    dy3 = 0.664f,
                )
                // c -0.163 0.29 -0.325 0.59 -0.488 0.891
                curveToRelative(
                    dx1 = -0.163f,
                    dy1 = 0.29f,
                    dx2 = -0.325f,
                    dy2 = 0.59f,
                    dx3 = -0.488f,
                    dy3 = 0.891f,
                )
                // c -0.318 0.591 -0.638 1.184 -0.97 1.701
                curveToRelative(
                    dx1 = -0.318f,
                    dy1 = 0.591f,
                    dx2 = -0.638f,
                    dy2 = 1.184f,
                    dx3 = -0.97f,
                    dy3 = 1.701f,
                )
                // c -0.247 0.386 -0.479 0.693 -0.693 0.9
                curveToRelative(
                    dx1 = -0.247f,
                    dy1 = 0.386f,
                    dx2 = -0.479f,
                    dy2 = 0.693f,
                    dx3 = -0.693f,
                    dy3 = 0.9f,
                )
                // c -0.224 0.216 -0.359 0.253 -0.413 0.253
                curveToRelative(
                    dx1 = -0.224f,
                    dy1 = 0.216f,
                    dx2 = -0.359f,
                    dy2 = 0.253f,
                    dx3 = -0.413f,
                    dy3 = 0.253f,
                )
                // c -0.077 0 -0.218 -0.045 -0.445 -0.214
                curveToRelative(
                    dx1 = -0.077f,
                    dy1 = 0.0f,
                    dx2 = -0.218f,
                    dy2 = -0.045f,
                    dx3 = -0.445f,
                    dy3 = -0.214f,
                )
                // a 6.863 6.863 0 0 1 -0.701 -0.64
                arcToRelative(
                    a = 6.863f,
                    b = 6.863f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.701f,
                    dy1 = -0.64f,
                )
                // a 0.5 0.5 0 0 0 -0.708 0.708
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.708f,
                    dy1 = 0.708f,
                )
                // c 0.247 0.246 0.526 0.522 0.812 0.735
                curveToRelative(
                    dx1 = 0.247f,
                    dy1 = 0.246f,
                    dx2 = 0.526f,
                    dy2 = 0.522f,
                    dx3 = 0.812f,
                    dy3 = 0.735f,
                )
                // c 0.277 0.206 0.636 0.411 1.042 0.411
                curveToRelative(
                    dx1 = 0.277f,
                    dy1 = 0.206f,
                    dx2 = 0.636f,
                    dy2 = 0.411f,
                    dx3 = 1.042f,
                    dy3 = 0.411f,
                )
                // c 0.446 0 0.82 -0.257 1.107 -0.534
                curveToRelative(
                    dx1 = 0.446f,
                    dy1 = 0.0f,
                    dx2 = 0.82f,
                    dy2 = -0.257f,
                    dx3 = 1.107f,
                    dy3 = -0.534f,
                )
                // c 0.298 -0.286 0.58 -0.67 0.841 -1.08
                curveToRelative(
                    dx1 = 0.298f,
                    dy1 = -0.286f,
                    dx2 = 0.58f,
                    dy2 = -0.67f,
                    dx3 = 0.841f,
                    dy3 = -1.08f,
                )
                // c 0.351 -0.546 0.705 -1.202 1.035 -1.813
                curveToRelative(
                    dx1 = 0.351f,
                    dy1 = -0.546f,
                    dx2 = 0.705f,
                    dy2 = -1.202f,
                    dx3 = 1.035f,
                    dy3 = -1.813f,
                )
                // c 0.157 -0.291 0.31 -0.573 0.453 -0.828z
                curveToRelative(
                    dx1 = 0.157f,
                    dy1 = -0.291f,
                    dx2 = 0.31f,
                    dy2 = -0.573f,
                    dx3 = 0.453f,
                    dy3 = -0.828f,
                )
                close()
                // M 0.777 14.144
                moveTo(x = 0.777f, y = 14.144f)
                // a 0.5 0.5 0 0 0 -0.554 0.832
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.554f,
                    dy1 = 0.832f,
                )
                // l 1.017 0.678
                lineToRelative(dx = 1.017f, dy = 0.678f)
                // a 1.5 1.5 0 0 0 1.503 0.094
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.503f,
                    dy1 = 0.094f,
                )
                // l 1.033 -0.517
                lineToRelative(dx = 1.033f, dy = -0.517f)
                // a 0.5 0.5 0 0 1 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 1.105 0.553
                lineToRelative(dx = 1.105f, dy = 0.553f)
                // a 1.5 1.5 0 0 0 1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.342f,
                    dy1 = 0.0f,
                )
                // l 1.105 -0.553
                lineToRelative(dx = 1.105f, dy = -0.553f)
                // a 0.5 0.5 0 0 1 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 1.105 0.553
                lineToRelative(dx = 1.105f, dy = 0.553f)
                // a 1.5 1.5 0 0 0 1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.342f,
                    dy1 = 0.0f,
                )
                // l 1.105 -0.553
                lineToRelative(dx = 1.105f, dy = -0.553f)
                // a 0.5 0.5 0 0 1 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 1.033 0.517
                lineToRelative(dx = 1.033f, dy = 0.517f)
                // a 1.5 1.5 0 0 0 1.503 -0.094
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.503f,
                    dy1 = -0.094f,
                )
                // l 1.017 -0.678
                lineToRelative(dx = 1.017f, dy = -0.678f)
                // a 0.5 0.5 0 1 0 -0.554 -0.832
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.554f,
                    dy1 = -0.832f,
                )
                // l -1.017 0.678
                lineToRelative(dx = -1.017f, dy = 0.678f)
                // a 0.5 0.5 0 0 1 -0.501 0.031
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.501f,
                    dy1 = 0.031f,
                )
                // l -1.034 -0.517
                lineToRelative(dx = -1.034f, dy = -0.517f)
                // a 1.5 1.5 0 0 0 -1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.342f,
                    dy1 = 0.0f,
                )
                // l -1.105 0.553
                lineToRelative(dx = -1.105f, dy = 0.553f)
                // a 0.5 0.5 0 0 1 -0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.448f,
                    dy1 = 0.0f,
                )
                // l -1.105 -0.553
                lineToRelative(dx = -1.105f, dy = -0.553f)
                // a 1.5 1.5 0 0 0 -1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.342f,
                    dy1 = 0.0f,
                )
                // l -1.105 0.553
                lineToRelative(dx = -1.105f, dy = 0.553f)
                // a 0.5 0.5 0 0 1 -0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.448f,
                    dy1 = 0.0f,
                )
                // l -1.105 -0.553
                lineToRelative(dx = -1.105f, dy = -0.553f)
                // a 1.5 1.5 0 0 0 -1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.342f,
                    dy1 = 0.0f,
                )
                // l -1.034 0.517
                lineToRelative(dx = -1.034f, dy = 0.517f)
                // a 0.5 0.5 0 0 1 -0.5 -0.03
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.03f,
                )
                // l -1.018 -0.679z
                lineToRelative(dx = -1.018f, dy = -0.679f)
                close()
                // M 3.17 0.473
                moveTo(x = 3.17f, y = 0.473f)
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
                // L 0.026 5.333
                lineTo(x = 0.026f, y = 5.333f)
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
                // L 3.17 0.473z
                lineTo(x = 3.17f, y = 0.473f)
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
        }.build().also { _ic2333 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2333: ImageVector? = null
