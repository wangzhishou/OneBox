package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2321: ImageVector
    get() {
        val current = _ic2321
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2321",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M6.538 7.405 a.602 .602 0 0 0 -1.078 .161 c-.087 .291 -.266 .86 -.509 1.469 -.245 .618 -.541 1.242 -.854 1.67 C3.024 12.166 .922 14.35 .308 14.98 -.072 15.371 .223 16 .74 16 h14.502 a.602 .602 0 0 0 .412 -1.04 c-.65 -.61 -2.721 -2.609 -3.729 -4.225 -.56 -.897 -1.208 -2.345 -1.407 -2.8 a.6 .6 0 0 0 -.975 -.184 l-.75 .749 H7.269 l-.73 -1.095Z M5.88 9.404 c.106 -.267 .2 -.526 .281 -.76 l.392 .589 a.6 .6 0 0 0 .5 .267 h1.905 a.6 .6 0 0 0 .424 -.176 l.456 -.456 c.247 .54 .618 1.32 .99 1.979 l-.765 .51 -.624 -.624 a.6 .6 0 0 0 -.924 .091 L8 11.598 l-.516 -.774 a.6 .6 0 0 0 -.924 -.091 l-.624 .623 -.747 -.498 c.269 -.455 .503 -.978 .692 -1.454Z M11.5 .5 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z M3 1.5 a.5 .5 0 1 0 0 -1 .5 .5 0 0 0 0 1Z m5.5 .5 a.5 .5 0 1 0 0 -1 .5 .5 0 0 0 0 1Z m3.554 6.503 a.5 .5 0 1 0 0 -1 .5 .5 0 0 0 0 1Z M5.766 .058 a.5 .5 0 0 1 .676 .208 c.604 1.14 .85 1.882 1.052 3.156 .19 1.209 .19 1.947 0 3.156 a.5 .5 0 0 1 -.988 -.156 c.175 -1.105 .175 -1.738 0 -2.844 -.184 -1.165 -.396 -1.801 -.948 -2.844 a.5 .5 0 0 1 .208 -.676Z m4.86 1.749 a.5 .5 0 0 0 -.79 -.614 c-.576 .742 -.84 1.27 -1.087 2.176 C8.587 3.96 8.5 4.474 8.5 5 s.087 1.04 .249 1.632 a.5 .5 0 0 0 .965 -.263 C9.567 5.83 9.5 5.409 9.5 5 c0 -.41 .067 -.83 .214 -1.368 .212 -.78 .416 -1.188 .912 -1.825Z m-6.246 .368 a.5 .5 0 0 0 -.76 .65 c.515 .601 .72 1.011 .892 1.784 .153 .69 .153 1.092 0 1.783 a.5 .5 0 1 0 .976 .217 c.185 -.834 .185 -1.384 0 -2.217 -.207 -.933 -.487 -1.492 -1.108 -2.217Z m8.894 .746 a.5 .5 0 0 0 -.539 -.842 c-.446 .285 -.803 .589 -1.086 .967 -.284 .378 -.476 .806 -.624 1.314 a3.988 3.988 0 0 0 -.185 1.14 c0 .375 .066 .736 .185 1.14 a.5 .5 0 1 0 .96 -.28 3.003 3.003 0 0 1 -.145 -.86 c0 -.25 .043 -.514 .144 -.86 .126 -.428 .273 -.738 .465 -.994 s.448 -.484 .825 -.725Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6.538 7.405
                moveTo(x = 6.538f, y = 7.405f)
                // a 0.602 0.602 0 0 0 -1.078 0.161
                arcToRelative(
                    a = 0.602f,
                    b = 0.602f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.078f,
                    dy1 = 0.161f,
                )
                // c -0.087 0.291 -0.266 0.86 -0.509 1.469
                curveToRelative(
                    dx1 = -0.087f,
                    dy1 = 0.291f,
                    dx2 = -0.266f,
                    dy2 = 0.86f,
                    dx3 = -0.509f,
                    dy3 = 1.469f,
                )
                // c -0.245 0.618 -0.541 1.242 -0.854 1.67
                curveToRelative(
                    dx1 = -0.245f,
                    dy1 = 0.618f,
                    dx2 = -0.541f,
                    dy2 = 1.242f,
                    dx3 = -0.854f,
                    dy3 = 1.67f,
                )
                // C 3.024 12.166 0.922 14.35 0.308 14.98
                curveTo(
                    x1 = 3.024f,
                    y1 = 12.166f,
                    x2 = 0.922f,
                    y2 = 14.35f,
                    x3 = 0.308f,
                    y3 = 14.98f,
                )
                // C -0.072 15.371 0.223 16 0.74 16
                curveTo(
                    x1 = -0.072f,
                    y1 = 15.371f,
                    x2 = 0.223f,
                    y2 = 16.0f,
                    x3 = 0.74f,
                    y3 = 16.0f,
                )
                // h 14.502
                horizontalLineToRelative(dx = 14.502f)
                // a 0.602 0.602 0 0 0 0.412 -1.04
                arcToRelative(
                    a = 0.602f,
                    b = 0.602f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.412f,
                    dy1 = -1.04f,
                )
                // c -0.65 -0.61 -2.721 -2.609 -3.729 -4.225
                curveToRelative(
                    dx1 = -0.65f,
                    dy1 = -0.61f,
                    dx2 = -2.721f,
                    dy2 = -2.609f,
                    dx3 = -3.729f,
                    dy3 = -4.225f,
                )
                // c -0.56 -0.897 -1.208 -2.345 -1.407 -2.8
                curveToRelative(
                    dx1 = -0.56f,
                    dy1 = -0.897f,
                    dx2 = -1.208f,
                    dy2 = -2.345f,
                    dx3 = -1.407f,
                    dy3 = -2.8f,
                )
                // a 0.6 0.6 0 0 0 -0.975 -0.184
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.975f,
                    dy1 = -0.184f,
                )
                // l -0.75 0.749
                lineToRelative(dx = -0.75f, dy = 0.749f)
                // H 7.269
                horizontalLineTo(x = 7.269f)
                // l -0.73 -1.095z
                lineToRelative(dx = -0.73f, dy = -1.095f)
                close()
                // M 5.88 9.404
                moveTo(x = 5.88f, y = 9.404f)
                // c 0.106 -0.267 0.2 -0.526 0.281 -0.76
                curveToRelative(
                    dx1 = 0.106f,
                    dy1 = -0.267f,
                    dx2 = 0.2f,
                    dy2 = -0.526f,
                    dx3 = 0.281f,
                    dy3 = -0.76f,
                )
                // l 0.392 0.589
                lineToRelative(dx = 0.392f, dy = 0.589f)
                // a 0.6 0.6 0 0 0 0.5 0.267
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.5f,
                    dy1 = 0.267f,
                )
                // h 1.905
                horizontalLineToRelative(dx = 1.905f)
                // a 0.6 0.6 0 0 0 0.424 -0.176
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.424f,
                    dy1 = -0.176f,
                )
                // l 0.456 -0.456
                lineToRelative(dx = 0.456f, dy = -0.456f)
                // c 0.247 0.54 0.618 1.32 0.99 1.979
                curveToRelative(
                    dx1 = 0.247f,
                    dy1 = 0.54f,
                    dx2 = 0.618f,
                    dy2 = 1.32f,
                    dx3 = 0.99f,
                    dy3 = 1.979f,
                )
                // l -0.765 0.51
                lineToRelative(dx = -0.765f, dy = 0.51f)
                // l -0.624 -0.624
                lineToRelative(dx = -0.624f, dy = -0.624f)
                // a 0.6 0.6 0 0 0 -0.924 0.091
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.924f,
                    dy1 = 0.091f,
                )
                // L 8 11.598
                lineTo(x = 8.0f, y = 11.598f)
                // l -0.516 -0.774
                lineToRelative(dx = -0.516f, dy = -0.774f)
                // a 0.6 0.6 0 0 0 -0.924 -0.091
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.924f,
                    dy1 = -0.091f,
                )
                // l -0.624 0.623
                lineToRelative(dx = -0.624f, dy = 0.623f)
                // l -0.747 -0.498
                lineToRelative(dx = -0.747f, dy = -0.498f)
                // c 0.269 -0.455 0.503 -0.978 0.692 -1.454z
                curveToRelative(
                    dx1 = 0.269f,
                    dy1 = -0.455f,
                    dx2 = 0.503f,
                    dy2 = -0.978f,
                    dx3 = 0.692f,
                    dy3 = -1.454f,
                )
                close()
                // M 11.5 0.5
                moveTo(x = 11.5f, y = 0.5f)
                // a 0.5 0.5 0 1 1 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // a 0.5 0.5 0 0 1 1 0z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                close()
                // M 3 1.5
                moveTo(x = 3.0f, y = 1.5f)
                // a 0.5 0.5 0 1 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // a 0.5 0.5 0 0 0 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
                // m 5.5 0.5
                moveToRelative(dx = 5.5f, dy = 0.5f)
                // a 0.5 0.5 0 1 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // a 0.5 0.5 0 0 0 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
                // m 3.554 6.503
                moveToRelative(dx = 3.554f, dy = 6.503f)
                // a 0.5 0.5 0 1 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // a 0.5 0.5 0 0 0 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
                // M 5.766 0.058
                moveTo(x = 5.766f, y = 0.058f)
                // a 0.5 0.5 0 0 1 0.676 0.208
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.676f,
                    dy1 = 0.208f,
                )
                // c 0.604 1.14 0.85 1.882 1.052 3.156
                curveToRelative(
                    dx1 = 0.604f,
                    dy1 = 1.14f,
                    dx2 = 0.85f,
                    dy2 = 1.882f,
                    dx3 = 1.052f,
                    dy3 = 3.156f,
                )
                // c 0.19 1.209 0.19 1.947 0 3.156
                curveToRelative(
                    dx1 = 0.19f,
                    dy1 = 1.209f,
                    dx2 = 0.19f,
                    dy2 = 1.947f,
                    dx3 = 0.0f,
                    dy3 = 3.156f,
                )
                // a 0.5 0.5 0 0 1 -0.988 -0.156
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.988f,
                    dy1 = -0.156f,
                )
                // c 0.175 -1.105 0.175 -1.738 0 -2.844
                curveToRelative(
                    dx1 = 0.175f,
                    dy1 = -1.105f,
                    dx2 = 0.175f,
                    dy2 = -1.738f,
                    dx3 = 0.0f,
                    dy3 = -2.844f,
                )
                // c -0.184 -1.165 -0.396 -1.801 -0.948 -2.844
                curveToRelative(
                    dx1 = -0.184f,
                    dy1 = -1.165f,
                    dx2 = -0.396f,
                    dy2 = -1.801f,
                    dx3 = -0.948f,
                    dy3 = -2.844f,
                )
                // a 0.5 0.5 0 0 1 0.208 -0.676z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.208f,
                    dy1 = -0.676f,
                )
                close()
                // m 4.86 1.749
                moveToRelative(dx = 4.86f, dy = 1.749f)
                // a 0.5 0.5 0 0 0 -0.79 -0.614
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.79f,
                    dy1 = -0.614f,
                )
                // c -0.576 0.742 -0.84 1.27 -1.087 2.176
                curveToRelative(
                    dx1 = -0.576f,
                    dy1 = 0.742f,
                    dx2 = -0.84f,
                    dy2 = 1.27f,
                    dx3 = -1.087f,
                    dy3 = 2.176f,
                )
                // C 8.587 3.96 8.5 4.474 8.5 5
                curveTo(
                    x1 = 8.587f,
                    y1 = 3.96f,
                    x2 = 8.5f,
                    y2 = 4.474f,
                    x3 = 8.5f,
                    y3 = 5.0f,
                )
                // s 0.087 1.04 0.249 1.632
                reflectiveCurveToRelative(
                    dx1 = 0.087f,
                    dy1 = 1.04f,
                    dx2 = 0.249f,
                    dy2 = 1.632f,
                )
                // a 0.5 0.5 0 0 0 0.965 -0.263
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.965f,
                    dy1 = -0.263f,
                )
                // C 9.567 5.83 9.5 5.409 9.5 5
                curveTo(
                    x1 = 9.567f,
                    y1 = 5.83f,
                    x2 = 9.5f,
                    y2 = 5.409f,
                    x3 = 9.5f,
                    y3 = 5.0f,
                )
                // c 0 -0.41 0.067 -0.83 0.214 -1.368
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.41f,
                    dx2 = 0.067f,
                    dy2 = -0.83f,
                    dx3 = 0.214f,
                    dy3 = -1.368f,
                )
                // c 0.212 -0.78 0.416 -1.188 0.912 -1.825z
                curveToRelative(
                    dx1 = 0.212f,
                    dy1 = -0.78f,
                    dx2 = 0.416f,
                    dy2 = -1.188f,
                    dx3 = 0.912f,
                    dy3 = -1.825f,
                )
                close()
                // m -6.246 0.368
                moveToRelative(dx = -6.246f, dy = 0.368f)
                // a 0.5 0.5 0 0 0 -0.76 0.65
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.76f,
                    dy1 = 0.65f,
                )
                // c 0.515 0.601 0.72 1.011 0.892 1.784
                curveToRelative(
                    dx1 = 0.515f,
                    dy1 = 0.601f,
                    dx2 = 0.72f,
                    dy2 = 1.011f,
                    dx3 = 0.892f,
                    dy3 = 1.784f,
                )
                // c 0.153 0.69 0.153 1.092 0 1.783
                curveToRelative(
                    dx1 = 0.153f,
                    dy1 = 0.69f,
                    dx2 = 0.153f,
                    dy2 = 1.092f,
                    dx3 = 0.0f,
                    dy3 = 1.783f,
                )
                // a 0.5 0.5 0 1 0 0.976 0.217
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.976f,
                    dy1 = 0.217f,
                )
                // c 0.185 -0.834 0.185 -1.384 0 -2.217
                curveToRelative(
                    dx1 = 0.185f,
                    dy1 = -0.834f,
                    dx2 = 0.185f,
                    dy2 = -1.384f,
                    dx3 = 0.0f,
                    dy3 = -2.217f,
                )
                // c -0.207 -0.933 -0.487 -1.492 -1.108 -2.217z
                curveToRelative(
                    dx1 = -0.207f,
                    dy1 = -0.933f,
                    dx2 = -0.487f,
                    dy2 = -1.492f,
                    dx3 = -1.108f,
                    dy3 = -2.217f,
                )
                close()
                // m 8.894 0.746
                moveToRelative(dx = 8.894f, dy = 0.746f)
                // a 0.5 0.5 0 0 0 -0.539 -0.842
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.539f,
                    dy1 = -0.842f,
                )
                // c -0.446 0.285 -0.803 0.589 -1.086 0.967
                curveToRelative(
                    dx1 = -0.446f,
                    dy1 = 0.285f,
                    dx2 = -0.803f,
                    dy2 = 0.589f,
                    dx3 = -1.086f,
                    dy3 = 0.967f,
                )
                // c -0.284 0.378 -0.476 0.806 -0.624 1.314
                curveToRelative(
                    dx1 = -0.284f,
                    dy1 = 0.378f,
                    dx2 = -0.476f,
                    dy2 = 0.806f,
                    dx3 = -0.624f,
                    dy3 = 1.314f,
                )
                // a 3.988 3.988 0 0 0 -0.185 1.14
                arcToRelative(
                    a = 3.988f,
                    b = 3.988f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.185f,
                    dy1 = 1.14f,
                )
                // c 0 0.375 0.066 0.736 0.185 1.14
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.375f,
                    dx2 = 0.066f,
                    dy2 = 0.736f,
                    dx3 = 0.185f,
                    dy3 = 1.14f,
                )
                // a 0.5 0.5 0 1 0 0.96 -0.28
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.96f,
                    dy1 = -0.28f,
                )
                // a 3.003 3.003 0 0 1 -0.145 -0.86
                arcToRelative(
                    a = 3.003f,
                    b = 3.003f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.145f,
                    dy1 = -0.86f,
                )
                // c 0 -0.25 0.043 -0.514 0.144 -0.86
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.25f,
                    dx2 = 0.043f,
                    dy2 = -0.514f,
                    dx3 = 0.144f,
                    dy3 = -0.86f,
                )
                // c 0.126 -0.428 0.273 -0.738 0.465 -0.994
                curveToRelative(
                    dx1 = 0.126f,
                    dy1 = -0.428f,
                    dx2 = 0.273f,
                    dy2 = -0.738f,
                    dx3 = 0.465f,
                    dy3 = -0.994f,
                )
                // s 0.448 -0.484 0.825 -0.725z
                reflectiveCurveToRelative(
                    dx1 = 0.448f,
                    dy1 = -0.484f,
                    dx2 = 0.825f,
                    dy2 = -0.725f,
                )
                close()
            }
        }.build().also { _ic2321 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2321: ImageVector? = null
