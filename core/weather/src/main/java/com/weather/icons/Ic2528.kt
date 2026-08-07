package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2528: ImageVector
    get() {
        val current = _ic2528
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2528",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M.493 10.866 c2.044 -.355 4.424 -.718 6.792 -.517 1.318 .111 2.242 .49 2.747 1.124 a2.067 2.067 0 0 1 .408 1.92 A1.65 1.65 0 0 1 8.884 14.5 l.001 .001 c-.14 0 -.28 -.018 -.415 -.053 a1.694 1.694 0 0 1 -1.118 -.978 .489 .489 0 0 1 .26 -.643 .49 .49 0 0 1 .64 .269 .703 .703 0 0 0 .466 .41 .679 .679 0 0 0 .805 -.434 1.128 1.128 0 0 0 -.253 -.993 c-.326 -.41 -1.042 -.672 -2.067 -.758 -2.253 -.191 -4.558 .16 -6.543 .505 a.486 .486 0 1 1 -.167 -.959Z m15.02 -.52 a.487 .487 0 0 1 0 .973 h-3.237 a.488 .488 0 1 1 0 -.973 h3.237Z m-7.98 -2.578 a.487 .487 0 0 1 0 .974 H3.009 a.487 .487 0 0 1 0 -.974 h4.524Z m5.278 -4.515 a2.341 2.341 0 0 1 1.214 .124 l.013 .005 a2.316 2.316 0 0 1 1.515 1.947 2.666 2.666 0 0 1 -1.207 2.532 c-1.002 .66 -2.194 .766 -3.329 .766 -.274 0 -.546 -.005 -.81 -.01 a28.255 28.255 0 0 0 -.748 -.013 .488 .488 0 1 1 0 -.973 c.252 0 .515 .006 .77 .012 1.273 .029 2.589 .058 3.581 -.594 a1.695 1.695 0 0 0 .776 -1.608 1.342 1.342 0 0 0 -.88 -1.142 1.39 1.39 0 0 0 -1.374 .197 1.31 1.31 0 0 0 -.409 1.253 .485 .485 0 0 1 -.76 .479 .488 .488 0 0 1 -.2 -.316 2.293 2.293 0 0 1 .736 -2.156 c.32 -.263 .704 -.437 1.112 -.503Z M5.169 1.5 a2.378 2.378 0 0 1 2.378 2.378 A2.477 2.477 0 0 1 5.433 6.26 c-.203 .035 -.41 .051 -.617 .05 a8.474 8.474 0 0 1 -1.956 -.309 l-.236 -.056 c-.496 -.118 -.857 -.221 -1.2 -.318 -.309 -.087 -.627 -.178 -1.052 -.282 a.487 .487 0 1 1 .232 -.948 c.442 .109 .77 .203 1.085 .292 .353 .1 .685 .194 1.161 .307 l.24 .058 c.742 .178 1.509 .364 2.176 .246 a1.512 1.512 0 0 0 1.308 -1.421 1.406 1.406 0 0 0 -.867 -1.298 1.406 1.406 0 0 0 -.538 -.106 A1.078 1.078 0 0 0 4.09 3.553 v.021 a.488 .488 0 1 1 -.973 0 v-.021 A2.053 2.053 0 0 1 5.168 1.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0.493 10.866
                moveTo(x = 0.493f, y = 10.866f)
                // c 2.044 -0.355 4.424 -0.718 6.792 -0.517
                curveToRelative(
                    dx1 = 2.044f,
                    dy1 = -0.355f,
                    dx2 = 4.424f,
                    dy2 = -0.718f,
                    dx3 = 6.792f,
                    dy3 = -0.517f,
                )
                // c 1.318 0.111 2.242 0.49 2.747 1.124
                curveToRelative(
                    dx1 = 1.318f,
                    dy1 = 0.111f,
                    dx2 = 2.242f,
                    dy2 = 0.49f,
                    dx3 = 2.747f,
                    dy3 = 1.124f,
                )
                // a 2.067 2.067 0 0 1 0.408 1.92
                arcToRelative(
                    a = 2.067f,
                    b = 2.067f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.408f,
                    dy1 = 1.92f,
                )
                // A 1.65 1.65 0 0 1 8.884 14.5
                arcTo(
                    horizontalEllipseRadius = 1.65f,
                    verticalEllipseRadius = 1.65f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 8.884f,
                    y1 = 14.5f,
                )
                // l 0.001 0.001
                lineToRelative(dx = 0.001f, dy = 0.001f)
                // c -0.14 0 -0.28 -0.018 -0.415 -0.053
                curveToRelative(
                    dx1 = -0.14f,
                    dy1 = 0.0f,
                    dx2 = -0.28f,
                    dy2 = -0.018f,
                    dx3 = -0.415f,
                    dy3 = -0.053f,
                )
                // a 1.694 1.694 0 0 1 -1.118 -0.978
                arcToRelative(
                    a = 1.694f,
                    b = 1.694f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.118f,
                    dy1 = -0.978f,
                )
                // a 0.489 0.489 0 0 1 0.26 -0.643
                arcToRelative(
                    a = 0.489f,
                    b = 0.489f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.26f,
                    dy1 = -0.643f,
                )
                // a 0.49 0.49 0 0 1 0.64 0.269
                arcToRelative(
                    a = 0.49f,
                    b = 0.49f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.64f,
                    dy1 = 0.269f,
                )
                // a 0.703 0.703 0 0 0 0.466 0.41
                arcToRelative(
                    a = 0.703f,
                    b = 0.703f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.466f,
                    dy1 = 0.41f,
                )
                // a 0.679 0.679 0 0 0 0.805 -0.434
                arcToRelative(
                    a = 0.679f,
                    b = 0.679f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.805f,
                    dy1 = -0.434f,
                )
                // a 1.128 1.128 0 0 0 -0.253 -0.993
                arcToRelative(
                    a = 1.128f,
                    b = 1.128f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.253f,
                    dy1 = -0.993f,
                )
                // c -0.326 -0.41 -1.042 -0.672 -2.067 -0.758
                curveToRelative(
                    dx1 = -0.326f,
                    dy1 = -0.41f,
                    dx2 = -1.042f,
                    dy2 = -0.672f,
                    dx3 = -2.067f,
                    dy3 = -0.758f,
                )
                // c -2.253 -0.191 -4.558 0.16 -6.543 0.505
                curveToRelative(
                    dx1 = -2.253f,
                    dy1 = -0.191f,
                    dx2 = -4.558f,
                    dy2 = 0.16f,
                    dx3 = -6.543f,
                    dy3 = 0.505f,
                )
                // a 0.486 0.486 0 1 1 -0.167 -0.959z
                arcToRelative(
                    a = 0.486f,
                    b = 0.486f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.167f,
                    dy1 = -0.959f,
                )
                close()
                // m 15.02 -0.52
                moveToRelative(dx = 15.02f, dy = -0.52f)
                // a 0.487 0.487 0 0 1 0 0.973
                arcToRelative(
                    a = 0.487f,
                    b = 0.487f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.973f,
                )
                // h -3.237
                horizontalLineToRelative(dx = -3.237f)
                // a 0.488 0.488 0 1 1 0 -0.973
                arcToRelative(
                    a = 0.488f,
                    b = 0.488f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -0.973f,
                )
                // h 3.237z
                horizontalLineToRelative(dx = 3.237f)
                close()
                // m -7.98 -2.578
                moveToRelative(dx = -7.98f, dy = -2.578f)
                // a 0.487 0.487 0 0 1 0 0.974
                arcToRelative(
                    a = 0.487f,
                    b = 0.487f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.974f,
                )
                // H 3.009
                horizontalLineTo(x = 3.009f)
                // a 0.487 0.487 0 0 1 0 -0.974
                arcToRelative(
                    a = 0.487f,
                    b = 0.487f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -0.974f,
                )
                // h 4.524z
                horizontalLineToRelative(dx = 4.524f)
                close()
                // m 5.278 -4.515
                moveToRelative(dx = 5.278f, dy = -4.515f)
                // a 2.341 2.341 0 0 1 1.214 0.124
                arcToRelative(
                    a = 2.341f,
                    b = 2.341f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.214f,
                    dy1 = 0.124f,
                )
                // l 0.013 0.005
                lineToRelative(dx = 0.013f, dy = 0.005f)
                // a 2.316 2.316 0 0 1 1.515 1.947
                arcToRelative(
                    a = 2.316f,
                    b = 2.316f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.515f,
                    dy1 = 1.947f,
                )
                // a 2.666 2.666 0 0 1 -1.207 2.532
                arcToRelative(
                    a = 2.666f,
                    b = 2.666f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.207f,
                    dy1 = 2.532f,
                )
                // c -1.002 0.66 -2.194 0.766 -3.329 0.766
                curveToRelative(
                    dx1 = -1.002f,
                    dy1 = 0.66f,
                    dx2 = -2.194f,
                    dy2 = 0.766f,
                    dx3 = -3.329f,
                    dy3 = 0.766f,
                )
                // c -0.274 0 -0.546 -0.005 -0.81 -0.01
                curveToRelative(
                    dx1 = -0.274f,
                    dy1 = 0.0f,
                    dx2 = -0.546f,
                    dy2 = -0.005f,
                    dx3 = -0.81f,
                    dy3 = -0.01f,
                )
                // a 28.255 28.255 0 0 0 -0.748 -0.013
                arcToRelative(
                    a = 28.255f,
                    b = 28.255f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.748f,
                    dy1 = -0.013f,
                )
                // a 0.488 0.488 0 1 1 0 -0.973
                arcToRelative(
                    a = 0.488f,
                    b = 0.488f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -0.973f,
                )
                // c 0.252 0 0.515 0.006 0.77 0.012
                curveToRelative(
                    dx1 = 0.252f,
                    dy1 = 0.0f,
                    dx2 = 0.515f,
                    dy2 = 0.006f,
                    dx3 = 0.77f,
                    dy3 = 0.012f,
                )
                // c 1.273 0.029 2.589 0.058 3.581 -0.594
                curveToRelative(
                    dx1 = 1.273f,
                    dy1 = 0.029f,
                    dx2 = 2.589f,
                    dy2 = 0.058f,
                    dx3 = 3.581f,
                    dy3 = -0.594f,
                )
                // a 1.695 1.695 0 0 0 0.776 -1.608
                arcToRelative(
                    a = 1.695f,
                    b = 1.695f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.776f,
                    dy1 = -1.608f,
                )
                // a 1.342 1.342 0 0 0 -0.88 -1.142
                arcToRelative(
                    a = 1.342f,
                    b = 1.342f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.88f,
                    dy1 = -1.142f,
                )
                // a 1.39 1.39 0 0 0 -1.374 0.197
                arcToRelative(
                    a = 1.39f,
                    b = 1.39f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.374f,
                    dy1 = 0.197f,
                )
                // a 1.31 1.31 0 0 0 -0.409 1.253
                arcToRelative(
                    a = 1.31f,
                    b = 1.31f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.409f,
                    dy1 = 1.253f,
                )
                // a 0.485 0.485 0 0 1 -0.76 0.479
                arcToRelative(
                    a = 0.485f,
                    b = 0.485f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.76f,
                    dy1 = 0.479f,
                )
                // a 0.488 0.488 0 0 1 -0.2 -0.316
                arcToRelative(
                    a = 0.488f,
                    b = 0.488f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.2f,
                    dy1 = -0.316f,
                )
                // a 2.293 2.293 0 0 1 0.736 -2.156
                arcToRelative(
                    a = 2.293f,
                    b = 2.293f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.736f,
                    dy1 = -2.156f,
                )
                // c 0.32 -0.263 0.704 -0.437 1.112 -0.503z
                curveToRelative(
                    dx1 = 0.32f,
                    dy1 = -0.263f,
                    dx2 = 0.704f,
                    dy2 = -0.437f,
                    dx3 = 1.112f,
                    dy3 = -0.503f,
                )
                close()
                // M 5.169 1.5
                moveTo(x = 5.169f, y = 1.5f)
                // a 2.378 2.378 0 0 1 2.378 2.378
                arcToRelative(
                    a = 2.378f,
                    b = 2.378f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.378f,
                    dy1 = 2.378f,
                )
                // A 2.477 2.477 0 0 1 5.433 6.26
                arcTo(
                    horizontalEllipseRadius = 2.477f,
                    verticalEllipseRadius = 2.477f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 5.433f,
                    y1 = 6.26f,
                )
                // c -0.203 0.035 -0.41 0.051 -0.617 0.05
                curveToRelative(
                    dx1 = -0.203f,
                    dy1 = 0.035f,
                    dx2 = -0.41f,
                    dy2 = 0.051f,
                    dx3 = -0.617f,
                    dy3 = 0.05f,
                )
                // a 8.474 8.474 0 0 1 -1.956 -0.309
                arcToRelative(
                    a = 8.474f,
                    b = 8.474f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.956f,
                    dy1 = -0.309f,
                )
                // l -0.236 -0.056
                lineToRelative(dx = -0.236f, dy = -0.056f)
                // c -0.496 -0.118 -0.857 -0.221 -1.2 -0.318
                curveToRelative(
                    dx1 = -0.496f,
                    dy1 = -0.118f,
                    dx2 = -0.857f,
                    dy2 = -0.221f,
                    dx3 = -1.2f,
                    dy3 = -0.318f,
                )
                // c -0.309 -0.087 -0.627 -0.178 -1.052 -0.282
                curveToRelative(
                    dx1 = -0.309f,
                    dy1 = -0.087f,
                    dx2 = -0.627f,
                    dy2 = -0.178f,
                    dx3 = -1.052f,
                    dy3 = -0.282f,
                )
                // a 0.487 0.487 0 1 1 0.232 -0.948
                arcToRelative(
                    a = 0.487f,
                    b = 0.487f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.232f,
                    dy1 = -0.948f,
                )
                // c 0.442 0.109 0.77 0.203 1.085 0.292
                curveToRelative(
                    dx1 = 0.442f,
                    dy1 = 0.109f,
                    dx2 = 0.77f,
                    dy2 = 0.203f,
                    dx3 = 1.085f,
                    dy3 = 0.292f,
                )
                // c 0.353 0.1 0.685 0.194 1.161 0.307
                curveToRelative(
                    dx1 = 0.353f,
                    dy1 = 0.1f,
                    dx2 = 0.685f,
                    dy2 = 0.194f,
                    dx3 = 1.161f,
                    dy3 = 0.307f,
                )
                // l 0.24 0.058
                lineToRelative(dx = 0.24f, dy = 0.058f)
                // c 0.742 0.178 1.509 0.364 2.176 0.246
                curveToRelative(
                    dx1 = 0.742f,
                    dy1 = 0.178f,
                    dx2 = 1.509f,
                    dy2 = 0.364f,
                    dx3 = 2.176f,
                    dy3 = 0.246f,
                )
                // a 1.512 1.512 0 0 0 1.308 -1.421
                arcToRelative(
                    a = 1.512f,
                    b = 1.512f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.308f,
                    dy1 = -1.421f,
                )
                // a 1.406 1.406 0 0 0 -0.867 -1.298
                arcToRelative(
                    a = 1.406f,
                    b = 1.406f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.867f,
                    dy1 = -1.298f,
                )
                // a 1.406 1.406 0 0 0 -0.538 -0.106
                arcToRelative(
                    a = 1.406f,
                    b = 1.406f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.538f,
                    dy1 = -0.106f,
                )
                // A 1.078 1.078 0 0 0 4.09 3.553
                arcTo(
                    horizontalEllipseRadius = 1.078f,
                    verticalEllipseRadius = 1.078f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 4.09f,
                    y1 = 3.553f,
                )
                // v 0.021
                verticalLineToRelative(dy = 0.021f)
                // a 0.488 0.488 0 1 1 -0.973 0
                arcToRelative(
                    a = 0.488f,
                    b = 0.488f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.973f,
                    dy1 = 0.0f,
                )
                // v -0.021
                verticalLineToRelative(dy = -0.021f)
                // A 2.053 2.053 0 0 1 5.168 1.5z
                arcTo(
                    horizontalEllipseRadius = 2.053f,
                    verticalEllipseRadius = 2.053f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 5.168f,
                    y1 = 1.5f,
                )
                close()
            }
        }.build().also { _ic2528 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2528: ImageVector? = null
