package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2007: ImageVector
    get() {
        val current = _ic2007
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2007",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M11.586 12.981 c1.314 0 1.965 .777 2.485 1.4 .46 .546 .704 .81 1.22 .81 .225 0 .407 .181 .407 .405 a.402 .402 0 0 1 -.386 .404 c-.923 0 -1.37 -.534 -1.846 -1.1 -.476 -.567 -.932 -1.11 -1.864 -1.11 -.93 0 -1.386 .54 -1.841 1.106 -.456 .566 -.928 1.1 -1.847 1.1 h-.07 c-.922 0 -1.37 -.534 -1.846 -1.1 -.476 -.567 -.931 -1.11 -1.862 -1.11 s-1.403 .543 -1.883 1.11 C1.773 15.462 1.329 16 .406 16 a.406 .406 0 1 1 0 -.81 c.517 0 .761 -.263 1.22 -.81 .525 -.622 1.176 -1.398 2.49 -1.399 1.313 0 1.964 .777 2.488 1.4 .44 .522 .68 .81 1.152 .81 h.158 c.48 0 .724 -.268 1.184 -.81 .524 -.623 1.175 -1.4 2.488 -1.4Z m.227 -3.623 c1.338 0 2 .776 2.53 1.403 .469 .545 .717 .808 1.243 .808 a.41 .41 0 0 1 .414 .405 .408 .408 0 0 1 -.41 .403 c-.822 0 -1.274 -.408 -1.698 -.889 l-.364 -.42 c-.426 -.483 -.885 -.901 -1.715 -.901 -.83 0 -1.288 .402 -1.7 .896 l-.35 .431 c-.416 .49 -.887 .883 -1.705 .883 h-.07 c-.823 0 -1.275 -.408 -1.699 -.889 l-.363 -.42 c-.427 -.483 -.885 -.901 -1.715 -.901 -.948 0 -1.429 .525 -1.917 1.111 -.489 .586 -.94 1.099 -1.88 1.099 A.409 .409 0 0 1 0 11.974 a.41 .41 0 0 1 .414 -.405 c.526 0 .774 -.263 1.242 -.808 .534 -.627 1.197 -1.403 2.534 -1.403 1.338 0 2 .776 2.535 1.403 .45 .53 .7 .808 1.192 .808 h.157 c.489 0 .737 -.267 1.205 -.808 .535 -.627 1.197 -1.402 2.534 -1.403Z M6.023 1 c.467 0 .756 .325 1.107 .786 .248 .324 .524 .732 .78 1.148 .748 1.218 1.128 2.186 1.128 2.872 a3.033 3.033 0 0 1 -3.015 3.043 A3.034 3.034 0 0 1 3 5.809 c0 -.685 .38 -1.662 1.128 -2.872 .239 -.397 .5 -.782 .78 -1.15 C5.26 1.34 5.557 1 6.023 1Z m0 .831 c-.066 0 -.211 .146 -.455 .462 a10.1 10.1 0 0 0 -.73 1.077 C4.19 4.425 3.826 5.294 3.826 5.81 a2.212 2.212 0 0 0 1.068 1.992 2.181 2.181 0 0 0 2.247 0 A2.213 2.213 0 0 0 8.208 5.81 c0 -.516 -.36 -1.385 -1.004 -2.44 a12.274 12.274 0 0 0 -.735 -1.077 c-.231 -.316 -.375 -.461 -.446 -.462Z M11.003 0 c-.31 0 -.507 .218 -.74 .501 -.185 .235 -.358 .48 -.516 .733 C9.252 2.005 9 2.628 9 3.064 9.002 4.134 9.898 5 11.003 5 12.107 4.997 13 4.13 13 3.062 c0 -.437 -.252 -1.055 -.747 -1.83 A9.075 9.075 0 0 0 11.736 .5 c-.232 -.294 -.424 -.5 -.733 -.501Z m0 .53 c.046 0 .142 .092 .295 .293 .175 .22 .338 .45 .487 .687 .427 .672 .665 1.226 .665 1.554 a1.397 1.397 0 0 1 -.707 1.269 1.496 1.496 0 0 1 -1.49 0 1.396 1.396 0 0 1 -.706 -1.269 c0 -.328 .241 -.882 .67 -1.554 .151 -.25 .324 -.486 .485 -.687 .161 -.2 .257 -.294 .3 -.294Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.586 12.981
                moveTo(x = 11.586f, y = 12.981f)
                // c 1.314 0 1.965 0.777 2.485 1.4
                curveToRelative(
                    dx1 = 1.314f,
                    dy1 = 0.0f,
                    dx2 = 1.965f,
                    dy2 = 0.777f,
                    dx3 = 2.485f,
                    dy3 = 1.4f,
                )
                // c 0.46 0.546 0.704 0.81 1.22 0.81
                curveToRelative(
                    dx1 = 0.46f,
                    dy1 = 0.546f,
                    dx2 = 0.704f,
                    dy2 = 0.81f,
                    dx3 = 1.22f,
                    dy3 = 0.81f,
                )
                // c 0.225 0 0.407 0.181 0.407 0.405
                curveToRelative(
                    dx1 = 0.225f,
                    dy1 = 0.0f,
                    dx2 = 0.407f,
                    dy2 = 0.181f,
                    dx3 = 0.407f,
                    dy3 = 0.405f,
                )
                // a 0.402 0.402 0 0 1 -0.386 0.404
                arcToRelative(
                    a = 0.402f,
                    b = 0.402f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.386f,
                    dy1 = 0.404f,
                )
                // c -0.923 0 -1.37 -0.534 -1.846 -1.1
                curveToRelative(
                    dx1 = -0.923f,
                    dy1 = 0.0f,
                    dx2 = -1.37f,
                    dy2 = -0.534f,
                    dx3 = -1.846f,
                    dy3 = -1.1f,
                )
                // c -0.476 -0.567 -0.932 -1.11 -1.864 -1.11
                curveToRelative(
                    dx1 = -0.476f,
                    dy1 = -0.567f,
                    dx2 = -0.932f,
                    dy2 = -1.11f,
                    dx3 = -1.864f,
                    dy3 = -1.11f,
                )
                // c -0.93 0 -1.386 0.54 -1.841 1.106
                curveToRelative(
                    dx1 = -0.93f,
                    dy1 = 0.0f,
                    dx2 = -1.386f,
                    dy2 = 0.54f,
                    dx3 = -1.841f,
                    dy3 = 1.106f,
                )
                // c -0.456 0.566 -0.928 1.1 -1.847 1.1
                curveToRelative(
                    dx1 = -0.456f,
                    dy1 = 0.566f,
                    dx2 = -0.928f,
                    dy2 = 1.1f,
                    dx3 = -1.847f,
                    dy3 = 1.1f,
                )
                // h -0.07
                horizontalLineToRelative(dx = -0.07f)
                // c -0.922 0 -1.37 -0.534 -1.846 -1.1
                curveToRelative(
                    dx1 = -0.922f,
                    dy1 = 0.0f,
                    dx2 = -1.37f,
                    dy2 = -0.534f,
                    dx3 = -1.846f,
                    dy3 = -1.1f,
                )
                // c -0.476 -0.567 -0.931 -1.11 -1.862 -1.11
                curveToRelative(
                    dx1 = -0.476f,
                    dy1 = -0.567f,
                    dx2 = -0.931f,
                    dy2 = -1.11f,
                    dx3 = -1.862f,
                    dy3 = -1.11f,
                )
                // s -1.403 0.543 -1.883 1.11
                reflectiveCurveToRelative(
                    dx1 = -1.403f,
                    dy1 = 0.543f,
                    dx2 = -1.883f,
                    dy2 = 1.11f,
                )
                // C 1.773 15.462 1.329 16 0.406 16
                curveTo(
                    x1 = 1.773f,
                    y1 = 15.462f,
                    x2 = 1.329f,
                    y2 = 16.0f,
                    x3 = 0.406f,
                    y3 = 16.0f,
                )
                // a 0.406 0.406 0 1 1 0 -0.81
                arcToRelative(
                    a = 0.406f,
                    b = 0.406f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -0.81f,
                )
                // c 0.517 0 0.761 -0.263 1.22 -0.81
                curveToRelative(
                    dx1 = 0.517f,
                    dy1 = 0.0f,
                    dx2 = 0.761f,
                    dy2 = -0.263f,
                    dx3 = 1.22f,
                    dy3 = -0.81f,
                )
                // c 0.525 -0.622 1.176 -1.398 2.49 -1.399
                curveToRelative(
                    dx1 = 0.525f,
                    dy1 = -0.622f,
                    dx2 = 1.176f,
                    dy2 = -1.398f,
                    dx3 = 2.49f,
                    dy3 = -1.399f,
                )
                // c 1.313 0 1.964 0.777 2.488 1.4
                curveToRelative(
                    dx1 = 1.313f,
                    dy1 = 0.0f,
                    dx2 = 1.964f,
                    dy2 = 0.777f,
                    dx3 = 2.488f,
                    dy3 = 1.4f,
                )
                // c 0.44 0.522 0.68 0.81 1.152 0.81
                curveToRelative(
                    dx1 = 0.44f,
                    dy1 = 0.522f,
                    dx2 = 0.68f,
                    dy2 = 0.81f,
                    dx3 = 1.152f,
                    dy3 = 0.81f,
                )
                // h 0.158
                horizontalLineToRelative(dx = 0.158f)
                // c 0.48 0 0.724 -0.268 1.184 -0.81
                curveToRelative(
                    dx1 = 0.48f,
                    dy1 = 0.0f,
                    dx2 = 0.724f,
                    dy2 = -0.268f,
                    dx3 = 1.184f,
                    dy3 = -0.81f,
                )
                // c 0.524 -0.623 1.175 -1.4 2.488 -1.4z
                curveToRelative(
                    dx1 = 0.524f,
                    dy1 = -0.623f,
                    dx2 = 1.175f,
                    dy2 = -1.4f,
                    dx3 = 2.488f,
                    dy3 = -1.4f,
                )
                close()
                // m 0.227 -3.623
                moveToRelative(dx = 0.227f, dy = -3.623f)
                // c 1.338 0 2 0.776 2.53 1.403
                curveToRelative(
                    dx1 = 1.338f,
                    dy1 = 0.0f,
                    dx2 = 2.0f,
                    dy2 = 0.776f,
                    dx3 = 2.53f,
                    dy3 = 1.403f,
                )
                // c 0.469 0.545 0.717 0.808 1.243 0.808
                curveToRelative(
                    dx1 = 0.469f,
                    dy1 = 0.545f,
                    dx2 = 0.717f,
                    dy2 = 0.808f,
                    dx3 = 1.243f,
                    dy3 = 0.808f,
                )
                // a 0.41 0.41 0 0 1 0.414 0.405
                arcToRelative(
                    a = 0.41f,
                    b = 0.41f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.414f,
                    dy1 = 0.405f,
                )
                // a 0.408 0.408 0 0 1 -0.41 0.403
                arcToRelative(
                    a = 0.408f,
                    b = 0.408f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.41f,
                    dy1 = 0.403f,
                )
                // c -0.822 0 -1.274 -0.408 -1.698 -0.889
                curveToRelative(
                    dx1 = -0.822f,
                    dy1 = 0.0f,
                    dx2 = -1.274f,
                    dy2 = -0.408f,
                    dx3 = -1.698f,
                    dy3 = -0.889f,
                )
                // l -0.364 -0.42
                lineToRelative(dx = -0.364f, dy = -0.42f)
                // c -0.426 -0.483 -0.885 -0.901 -1.715 -0.901
                curveToRelative(
                    dx1 = -0.426f,
                    dy1 = -0.483f,
                    dx2 = -0.885f,
                    dy2 = -0.901f,
                    dx3 = -1.715f,
                    dy3 = -0.901f,
                )
                // c -0.83 0 -1.288 0.402 -1.7 0.896
                curveToRelative(
                    dx1 = -0.83f,
                    dy1 = 0.0f,
                    dx2 = -1.288f,
                    dy2 = 0.402f,
                    dx3 = -1.7f,
                    dy3 = 0.896f,
                )
                // l -0.35 0.431
                lineToRelative(dx = -0.35f, dy = 0.431f)
                // c -0.416 0.49 -0.887 0.883 -1.705 0.883
                curveToRelative(
                    dx1 = -0.416f,
                    dy1 = 0.49f,
                    dx2 = -0.887f,
                    dy2 = 0.883f,
                    dx3 = -1.705f,
                    dy3 = 0.883f,
                )
                // h -0.07
                horizontalLineToRelative(dx = -0.07f)
                // c -0.823 0 -1.275 -0.408 -1.699 -0.889
                curveToRelative(
                    dx1 = -0.823f,
                    dy1 = 0.0f,
                    dx2 = -1.275f,
                    dy2 = -0.408f,
                    dx3 = -1.699f,
                    dy3 = -0.889f,
                )
                // l -0.363 -0.42
                lineToRelative(dx = -0.363f, dy = -0.42f)
                // c -0.427 -0.483 -0.885 -0.901 -1.715 -0.901
                curveToRelative(
                    dx1 = -0.427f,
                    dy1 = -0.483f,
                    dx2 = -0.885f,
                    dy2 = -0.901f,
                    dx3 = -1.715f,
                    dy3 = -0.901f,
                )
                // c -0.948 0 -1.429 0.525 -1.917 1.111
                curveToRelative(
                    dx1 = -0.948f,
                    dy1 = 0.0f,
                    dx2 = -1.429f,
                    dy2 = 0.525f,
                    dx3 = -1.917f,
                    dy3 = 1.111f,
                )
                // c -0.489 0.586 -0.94 1.099 -1.88 1.099
                curveToRelative(
                    dx1 = -0.489f,
                    dy1 = 0.586f,
                    dx2 = -0.94f,
                    dy2 = 1.099f,
                    dx3 = -1.88f,
                    dy3 = 1.099f,
                )
                // A 0.409 0.409 0 0 1 0 11.974
                arcTo(
                    horizontalEllipseRadius = 0.409f,
                    verticalEllipseRadius = 0.409f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 0.0f,
                    y1 = 11.974f,
                )
                // a 0.41 0.41 0 0 1 0.414 -0.405
                arcToRelative(
                    a = 0.41f,
                    b = 0.41f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.414f,
                    dy1 = -0.405f,
                )
                // c 0.526 0 0.774 -0.263 1.242 -0.808
                curveToRelative(
                    dx1 = 0.526f,
                    dy1 = 0.0f,
                    dx2 = 0.774f,
                    dy2 = -0.263f,
                    dx3 = 1.242f,
                    dy3 = -0.808f,
                )
                // c 0.534 -0.627 1.197 -1.403 2.534 -1.403
                curveToRelative(
                    dx1 = 0.534f,
                    dy1 = -0.627f,
                    dx2 = 1.197f,
                    dy2 = -1.403f,
                    dx3 = 2.534f,
                    dy3 = -1.403f,
                )
                // c 1.338 0 2 0.776 2.535 1.403
                curveToRelative(
                    dx1 = 1.338f,
                    dy1 = 0.0f,
                    dx2 = 2.0f,
                    dy2 = 0.776f,
                    dx3 = 2.535f,
                    dy3 = 1.403f,
                )
                // c 0.45 0.53 0.7 0.808 1.192 0.808
                curveToRelative(
                    dx1 = 0.45f,
                    dy1 = 0.53f,
                    dx2 = 0.7f,
                    dy2 = 0.808f,
                    dx3 = 1.192f,
                    dy3 = 0.808f,
                )
                // h 0.157
                horizontalLineToRelative(dx = 0.157f)
                // c 0.489 0 0.737 -0.267 1.205 -0.808
                curveToRelative(
                    dx1 = 0.489f,
                    dy1 = 0.0f,
                    dx2 = 0.737f,
                    dy2 = -0.267f,
                    dx3 = 1.205f,
                    dy3 = -0.808f,
                )
                // c 0.535 -0.627 1.197 -1.402 2.534 -1.403z
                curveToRelative(
                    dx1 = 0.535f,
                    dy1 = -0.627f,
                    dx2 = 1.197f,
                    dy2 = -1.402f,
                    dx3 = 2.534f,
                    dy3 = -1.403f,
                )
                close()
                // M 6.023 1
                moveTo(x = 6.023f, y = 1.0f)
                // c 0.467 0 0.756 0.325 1.107 0.786
                curveToRelative(
                    dx1 = 0.467f,
                    dy1 = 0.0f,
                    dx2 = 0.756f,
                    dy2 = 0.325f,
                    dx3 = 1.107f,
                    dy3 = 0.786f,
                )
                // c 0.248 0.324 0.524 0.732 0.78 1.148
                curveToRelative(
                    dx1 = 0.248f,
                    dy1 = 0.324f,
                    dx2 = 0.524f,
                    dy2 = 0.732f,
                    dx3 = 0.78f,
                    dy3 = 1.148f,
                )
                // c 0.748 1.218 1.128 2.186 1.128 2.872
                curveToRelative(
                    dx1 = 0.748f,
                    dy1 = 1.218f,
                    dx2 = 1.128f,
                    dy2 = 2.186f,
                    dx3 = 1.128f,
                    dy3 = 2.872f,
                )
                // a 3.033 3.033 0 0 1 -3.015 3.043
                arcToRelative(
                    a = 3.033f,
                    b = 3.033f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.015f,
                    dy1 = 3.043f,
                )
                // A 3.034 3.034 0 0 1 3 5.809
                arcTo(
                    horizontalEllipseRadius = 3.034f,
                    verticalEllipseRadius = 3.034f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 3.0f,
                    y1 = 5.809f,
                )
                // c 0 -0.685 0.38 -1.662 1.128 -2.872
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.685f,
                    dx2 = 0.38f,
                    dy2 = -1.662f,
                    dx3 = 1.128f,
                    dy3 = -2.872f,
                )
                // c 0.239 -0.397 0.5 -0.782 0.78 -1.15
                curveToRelative(
                    dx1 = 0.239f,
                    dy1 = -0.397f,
                    dx2 = 0.5f,
                    dy2 = -0.782f,
                    dx3 = 0.78f,
                    dy3 = -1.15f,
                )
                // C 5.26 1.34 5.557 1 6.023 1z
                curveTo(
                    x1 = 5.26f,
                    y1 = 1.34f,
                    x2 = 5.557f,
                    y2 = 1.0f,
                    x3 = 6.023f,
                    y3 = 1.0f,
                )
                close()
                // m 0 0.831
                moveToRelative(dx = 0.0f, dy = 0.831f)
                // c -0.066 0 -0.211 0.146 -0.455 0.462
                curveToRelative(
                    dx1 = -0.066f,
                    dy1 = 0.0f,
                    dx2 = -0.211f,
                    dy2 = 0.146f,
                    dx3 = -0.455f,
                    dy3 = 0.462f,
                )
                // a 10.1 10.1 0 0 0 -0.73 1.077
                arcToRelative(
                    a = 10.1f,
                    b = 10.1f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.73f,
                    dy1 = 1.077f,
                )
                // C 4.19 4.425 3.826 5.294 3.826 5.81
                curveTo(
                    x1 = 4.19f,
                    y1 = 4.425f,
                    x2 = 3.826f,
                    y2 = 5.294f,
                    x3 = 3.826f,
                    y3 = 5.81f,
                )
                // a 2.212 2.212 0 0 0 1.068 1.992
                arcToRelative(
                    a = 2.212f,
                    b = 2.212f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.068f,
                    dy1 = 1.992f,
                )
                // a 2.181 2.181 0 0 0 2.247 0
                arcToRelative(
                    a = 2.181f,
                    b = 2.181f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.247f,
                    dy1 = 0.0f,
                )
                // A 2.213 2.213 0 0 0 8.208 5.81
                arcTo(
                    horizontalEllipseRadius = 2.213f,
                    verticalEllipseRadius = 2.213f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 8.208f,
                    y1 = 5.81f,
                )
                // c 0 -0.516 -0.36 -1.385 -1.004 -2.44
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.516f,
                    dx2 = -0.36f,
                    dy2 = -1.385f,
                    dx3 = -1.004f,
                    dy3 = -2.44f,
                )
                // a 12.274 12.274 0 0 0 -0.735 -1.077
                arcToRelative(
                    a = 12.274f,
                    b = 12.274f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.735f,
                    dy1 = -1.077f,
                )
                // c -0.231 -0.316 -0.375 -0.461 -0.446 -0.462z
                curveToRelative(
                    dx1 = -0.231f,
                    dy1 = -0.316f,
                    dx2 = -0.375f,
                    dy2 = -0.461f,
                    dx3 = -0.446f,
                    dy3 = -0.462f,
                )
                close()
                // M 11.003 0
                moveTo(x = 11.003f, y = 0.0f)
                // c -0.31 0 -0.507 0.218 -0.74 0.501
                curveToRelative(
                    dx1 = -0.31f,
                    dy1 = 0.0f,
                    dx2 = -0.507f,
                    dy2 = 0.218f,
                    dx3 = -0.74f,
                    dy3 = 0.501f,
                )
                // c -0.185 0.235 -0.358 0.48 -0.516 0.733
                curveToRelative(
                    dx1 = -0.185f,
                    dy1 = 0.235f,
                    dx2 = -0.358f,
                    dy2 = 0.48f,
                    dx3 = -0.516f,
                    dy3 = 0.733f,
                )
                // C 9.252 2.005 9 2.628 9 3.064
                curveTo(
                    x1 = 9.252f,
                    y1 = 2.005f,
                    x2 = 9.0f,
                    y2 = 2.628f,
                    x3 = 9.0f,
                    y3 = 3.064f,
                )
                // C 9.002 4.134 9.898 5 11.003 5
                curveTo(
                    x1 = 9.002f,
                    y1 = 4.134f,
                    x2 = 9.898f,
                    y2 = 5.0f,
                    x3 = 11.003f,
                    y3 = 5.0f,
                )
                // C 12.107 4.997 13 4.13 13 3.062
                curveTo(
                    x1 = 12.107f,
                    y1 = 4.997f,
                    x2 = 13.0f,
                    y2 = 4.13f,
                    x3 = 13.0f,
                    y3 = 3.062f,
                )
                // c 0 -0.437 -0.252 -1.055 -0.747 -1.83
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.437f,
                    dx2 = -0.252f,
                    dy2 = -1.055f,
                    dx3 = -0.747f,
                    dy3 = -1.83f,
                )
                // A 9.075 9.075 0 0 0 11.736 0.5
                arcTo(
                    horizontalEllipseRadius = 9.075f,
                    verticalEllipseRadius = 9.075f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 11.736f,
                    y1 = 0.5f,
                )
                // c -0.232 -0.294 -0.424 -0.5 -0.733 -0.501z
                curveToRelative(
                    dx1 = -0.232f,
                    dy1 = -0.294f,
                    dx2 = -0.424f,
                    dy2 = -0.5f,
                    dx3 = -0.733f,
                    dy3 = -0.501f,
                )
                close()
                // m 0 0.53
                moveToRelative(dx = 0.0f, dy = 0.53f)
                // c 0.046 0 0.142 0.092 0.295 0.293
                curveToRelative(
                    dx1 = 0.046f,
                    dy1 = 0.0f,
                    dx2 = 0.142f,
                    dy2 = 0.092f,
                    dx3 = 0.295f,
                    dy3 = 0.293f,
                )
                // c 0.175 0.22 0.338 0.45 0.487 0.687
                curveToRelative(
                    dx1 = 0.175f,
                    dy1 = 0.22f,
                    dx2 = 0.338f,
                    dy2 = 0.45f,
                    dx3 = 0.487f,
                    dy3 = 0.687f,
                )
                // c 0.427 0.672 0.665 1.226 0.665 1.554
                curveToRelative(
                    dx1 = 0.427f,
                    dy1 = 0.672f,
                    dx2 = 0.665f,
                    dy2 = 1.226f,
                    dx3 = 0.665f,
                    dy3 = 1.554f,
                )
                // a 1.397 1.397 0 0 1 -0.707 1.269
                arcToRelative(
                    a = 1.397f,
                    b = 1.397f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.707f,
                    dy1 = 1.269f,
                )
                // a 1.496 1.496 0 0 1 -1.49 0
                arcToRelative(
                    a = 1.496f,
                    b = 1.496f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.49f,
                    dy1 = 0.0f,
                )
                // a 1.396 1.396 0 0 1 -0.706 -1.269
                arcToRelative(
                    a = 1.396f,
                    b = 1.396f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.706f,
                    dy1 = -1.269f,
                )
                // c 0 -0.328 0.241 -0.882 0.67 -1.554
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.328f,
                    dx2 = 0.241f,
                    dy2 = -0.882f,
                    dx3 = 0.67f,
                    dy3 = -1.554f,
                )
                // c 0.151 -0.25 0.324 -0.486 0.485 -0.687
                curveToRelative(
                    dx1 = 0.151f,
                    dy1 = -0.25f,
                    dx2 = 0.324f,
                    dy2 = -0.486f,
                    dx3 = 0.485f,
                    dy3 = -0.687f,
                )
                // c 0.161 -0.2 0.257 -0.294 0.3 -0.294z
                curveToRelative(
                    dx1 = 0.161f,
                    dy1 = -0.2f,
                    dx2 = 0.257f,
                    dy2 = -0.294f,
                    dx3 = 0.3f,
                    dy3 = -0.294f,
                )
                close()
            }
        }.build().also { _ic2007 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2007: ImageVector? = null
