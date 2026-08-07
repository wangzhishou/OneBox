package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1059: ImageVector
    get() {
        val current = _ic1059
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1059",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M5.735 2.405 a.454 .454 0 0 1 -.235 .062 .466 .466 0 0 1 -.235 -.872 .454 .454 0 0 1 .235 -.062 .454 .454 0 0 1 .403 .23 .45 .45 0 0 1 .065 .236 .467 .467 0 0 1 -.233 .406Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5.735 2.405
                moveTo(x = 5.735f, y = 2.405f)
                // a 0.454 0.454 0 0 1 -0.235 0.062
                arcToRelative(
                    a = 0.454f,
                    b = 0.454f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.235f,
                    dy1 = 0.062f,
                )
                // a 0.466 0.466 0 0 1 -0.235 -0.872
                arcToRelative(
                    a = 0.466f,
                    b = 0.466f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.235f,
                    dy1 = -0.872f,
                )
                // a 0.454 0.454 0 0 1 0.235 -0.062
                arcToRelative(
                    a = 0.454f,
                    b = 0.454f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.235f,
                    dy1 = -0.062f,
                )
                // a 0.454 0.454 0 0 1 0.403 0.23
                arcToRelative(
                    a = 0.454f,
                    b = 0.454f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.403f,
                    dy1 = 0.23f,
                )
                // a 0.45 0.45 0 0 1 0.065 0.236
                arcToRelative(
                    a = 0.45f,
                    b = 0.45f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.065f,
                    dy1 = 0.236f,
                )
                // a 0.467 0.467 0 0 1 -0.233 0.406z
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.233f,
                    dy1 = 0.406f,
                )
                close()
            }
            // M3 .5 a.5 .5 0 0 1 .5 -.5 h9 a.5 .5 0 0 1 .5 .5 v3 h3.072 l-8 6 -8 -6 H3 v-3Z m1.997 2.367 A.975 .975 0 0 0 5.5 3 a.975 .975 0 0 0 .503 -.133 c.151 -.09 .272 -.211 .362 -.362 a.976 .976 0 0 0 .135 -.506 A1.006 1.006 0 0 0 5.5 1 a1.006 1.006 0 0 0 -1 .999 .98 .98 0 0 0 .135 .506 c.09 .15 .21 .272 .362 .362Z m6.503 -.158 a2.102 2.102 0 0 0 -.217 -.723 1.76 1.76 0 0 0 -1 -.872 A2.118 2.118 0 0 0 9.58 1 c-.396 0 -.75 .099 -1.064 .297 a2.04 2.04 0 0 0 -.742 .858 c-.182 .372 -.273 .82 -.273 1.345 0 .523 .09 .971 .27 1.345 .18 .374 .427 .66 .74 .858 .314 .198 .67 .297 1.069 .297 .27 0 .516 -.043 .738 -.128 a1.824 1.824 0 0 0 .981 -.899 c.104 -.202 .171 -.42 .202 -.65 l-.829 -.006 c-.025 .143 -.07 .27 -.137 .38 a1.018 1.018 0 0 1 -.563 .454 1.238 1.238 0 0 1 -.381 .058 c-.245 0 -.463 -.065 -.654 -.193 a1.298 1.298 0 0 1 -.448 -.575 c-.107 -.255 -.161 -.569 -.161 -.941 0 -.366 .054 -.676 .161 -.93 .108 -.254 .257 -.448 .448 -.579 .19 -.133 .41 -.2 .656 -.2 .139 0 .268 .021 .388 .062 a.965 .965 0 0 1 .56 .468 c.064 .113 .108 .242 .131 .388 h.829Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 3 0.5
                moveTo(x = 3.0f, y = 0.5f)
                // a 0.5 0.5 0 0 1 0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = -0.5f,
                )
                // h 9
                horizontalLineToRelative(dx = 9.0f)
                // a 0.5 0.5 0 0 1 0.5 0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = 0.5f,
                )
                // v 3
                verticalLineToRelative(dy = 3.0f)
                // h 3.072
                horizontalLineToRelative(dx = 3.072f)
                // l -8 6
                lineToRelative(dx = -8.0f, dy = 6.0f)
                // l -8 -6
                lineToRelative(dx = -8.0f, dy = -6.0f)
                // H 3
                horizontalLineTo(x = 3.0f)
                // v -3z
                verticalLineToRelative(dy = -3.0f)
                close()
                // m 1.997 2.367
                moveToRelative(dx = 1.997f, dy = 2.367f)
                // A 0.975 0.975 0 0 0 5.5 3
                arcTo(
                    horizontalEllipseRadius = 0.975f,
                    verticalEllipseRadius = 0.975f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 5.5f,
                    y1 = 3.0f,
                )
                // a 0.975 0.975 0 0 0 0.503 -0.133
                arcToRelative(
                    a = 0.975f,
                    b = 0.975f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.503f,
                    dy1 = -0.133f,
                )
                // c 0.151 -0.09 0.272 -0.211 0.362 -0.362
                curveToRelative(
                    dx1 = 0.151f,
                    dy1 = -0.09f,
                    dx2 = 0.272f,
                    dy2 = -0.211f,
                    dx3 = 0.362f,
                    dy3 = -0.362f,
                )
                // a 0.976 0.976 0 0 0 0.135 -0.506
                arcToRelative(
                    a = 0.976f,
                    b = 0.976f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.135f,
                    dy1 = -0.506f,
                )
                // A 1.006 1.006 0 0 0 5.5 1
                arcTo(
                    horizontalEllipseRadius = 1.006f,
                    verticalEllipseRadius = 1.006f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 5.5f,
                    y1 = 1.0f,
                )
                // a 1.006 1.006 0 0 0 -1 0.999
                arcToRelative(
                    a = 1.006f,
                    b = 1.006f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.0f,
                    dy1 = 0.999f,
                )
                // a 0.98 0.98 0 0 0 0.135 0.506
                arcToRelative(
                    a = 0.98f,
                    b = 0.98f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.135f,
                    dy1 = 0.506f,
                )
                // c 0.09 0.15 0.21 0.272 0.362 0.362z
                curveToRelative(
                    dx1 = 0.09f,
                    dy1 = 0.15f,
                    dx2 = 0.21f,
                    dy2 = 0.272f,
                    dx3 = 0.362f,
                    dy3 = 0.362f,
                )
                close()
                // m 6.503 -0.158
                moveToRelative(dx = 6.503f, dy = -0.158f)
                // a 2.102 2.102 0 0 0 -0.217 -0.723
                arcToRelative(
                    a = 2.102f,
                    b = 2.102f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.217f,
                    dy1 = -0.723f,
                )
                // a 1.76 1.76 0 0 0 -1 -0.872
                arcToRelative(
                    a = 1.76f,
                    b = 1.76f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.0f,
                    dy1 = -0.872f,
                )
                // A 2.118 2.118 0 0 0 9.58 1
                arcTo(
                    horizontalEllipseRadius = 2.118f,
                    verticalEllipseRadius = 2.118f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 9.58f,
                    y1 = 1.0f,
                )
                // c -0.396 0 -0.75 0.099 -1.064 0.297
                curveToRelative(
                    dx1 = -0.396f,
                    dy1 = 0.0f,
                    dx2 = -0.75f,
                    dy2 = 0.099f,
                    dx3 = -1.064f,
                    dy3 = 0.297f,
                )
                // a 2.04 2.04 0 0 0 -0.742 0.858
                arcToRelative(
                    a = 2.04f,
                    b = 2.04f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.742f,
                    dy1 = 0.858f,
                )
                // c -0.182 0.372 -0.273 0.82 -0.273 1.345
                curveToRelative(
                    dx1 = -0.182f,
                    dy1 = 0.372f,
                    dx2 = -0.273f,
                    dy2 = 0.82f,
                    dx3 = -0.273f,
                    dy3 = 1.345f,
                )
                // c 0 0.523 0.09 0.971 0.27 1.345
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.523f,
                    dx2 = 0.09f,
                    dy2 = 0.971f,
                    dx3 = 0.27f,
                    dy3 = 1.345f,
                )
                // c 0.18 0.374 0.427 0.66 0.74 0.858
                curveToRelative(
                    dx1 = 0.18f,
                    dy1 = 0.374f,
                    dx2 = 0.427f,
                    dy2 = 0.66f,
                    dx3 = 0.74f,
                    dy3 = 0.858f,
                )
                // c 0.314 0.198 0.67 0.297 1.069 0.297
                curveToRelative(
                    dx1 = 0.314f,
                    dy1 = 0.198f,
                    dx2 = 0.67f,
                    dy2 = 0.297f,
                    dx3 = 1.069f,
                    dy3 = 0.297f,
                )
                // c 0.27 0 0.516 -0.043 0.738 -0.128
                curveToRelative(
                    dx1 = 0.27f,
                    dy1 = 0.0f,
                    dx2 = 0.516f,
                    dy2 = -0.043f,
                    dx3 = 0.738f,
                    dy3 = -0.128f,
                )
                // a 1.824 1.824 0 0 0 0.981 -0.899
                arcToRelative(
                    a = 1.824f,
                    b = 1.824f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.981f,
                    dy1 = -0.899f,
                )
                // c 0.104 -0.202 0.171 -0.42 0.202 -0.65
                curveToRelative(
                    dx1 = 0.104f,
                    dy1 = -0.202f,
                    dx2 = 0.171f,
                    dy2 = -0.42f,
                    dx3 = 0.202f,
                    dy3 = -0.65f,
                )
                // l -0.829 -0.006
                lineToRelative(dx = -0.829f, dy = -0.006f)
                // c -0.025 0.143 -0.07 0.27 -0.137 0.38
                curveToRelative(
                    dx1 = -0.025f,
                    dy1 = 0.143f,
                    dx2 = -0.07f,
                    dy2 = 0.27f,
                    dx3 = -0.137f,
                    dy3 = 0.38f,
                )
                // a 1.018 1.018 0 0 1 -0.563 0.454
                arcToRelative(
                    a = 1.018f,
                    b = 1.018f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.563f,
                    dy1 = 0.454f,
                )
                // a 1.238 1.238 0 0 1 -0.381 0.058
                arcToRelative(
                    a = 1.238f,
                    b = 1.238f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.381f,
                    dy1 = 0.058f,
                )
                // c -0.245 0 -0.463 -0.065 -0.654 -0.193
                curveToRelative(
                    dx1 = -0.245f,
                    dy1 = 0.0f,
                    dx2 = -0.463f,
                    dy2 = -0.065f,
                    dx3 = -0.654f,
                    dy3 = -0.193f,
                )
                // a 1.298 1.298 0 0 1 -0.448 -0.575
                arcToRelative(
                    a = 1.298f,
                    b = 1.298f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.448f,
                    dy1 = -0.575f,
                )
                // c -0.107 -0.255 -0.161 -0.569 -0.161 -0.941
                curveToRelative(
                    dx1 = -0.107f,
                    dy1 = -0.255f,
                    dx2 = -0.161f,
                    dy2 = -0.569f,
                    dx3 = -0.161f,
                    dy3 = -0.941f,
                )
                // c 0 -0.366 0.054 -0.676 0.161 -0.93
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.366f,
                    dx2 = 0.054f,
                    dy2 = -0.676f,
                    dx3 = 0.161f,
                    dy3 = -0.93f,
                )
                // c 0.108 -0.254 0.257 -0.448 0.448 -0.579
                curveToRelative(
                    dx1 = 0.108f,
                    dy1 = -0.254f,
                    dx2 = 0.257f,
                    dy2 = -0.448f,
                    dx3 = 0.448f,
                    dy3 = -0.579f,
                )
                // c 0.19 -0.133 0.41 -0.2 0.656 -0.2
                curveToRelative(
                    dx1 = 0.19f,
                    dy1 = -0.133f,
                    dx2 = 0.41f,
                    dy2 = -0.2f,
                    dx3 = 0.656f,
                    dy3 = -0.2f,
                )
                // c 0.139 0 0.268 0.021 0.388 0.062
                curveToRelative(
                    dx1 = 0.139f,
                    dy1 = 0.0f,
                    dx2 = 0.268f,
                    dy2 = 0.021f,
                    dx3 = 0.388f,
                    dy3 = 0.062f,
                )
                // a 0.965 0.965 0 0 1 0.56 0.468
                arcToRelative(
                    a = 0.965f,
                    b = 0.965f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.56f,
                    dy1 = 0.468f,
                )
                // c 0.064 0.113 0.108 0.242 0.131 0.388
                curveToRelative(
                    dx1 = 0.064f,
                    dy1 = 0.113f,
                    dx2 = 0.108f,
                    dy2 = 0.242f,
                    dx3 = 0.131f,
                    dy3 = 0.388f,
                )
                // h 0.829z
                horizontalLineToRelative(dx = 0.829f)
                close()
            }
            // M13.333 7 8 11 2.667 7 H0 l8 6 8 -6 h-2.667Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 13.333 7
                moveTo(x = 13.333f, y = 7.0f)
                // L 8 11
                lineTo(x = 8.0f, y = 11.0f)
                // L 2.667 7
                lineTo(x = 2.667f, y = 7.0f)
                // H 0
                horizontalLineTo(x = 0.0f)
                // l 8 6
                lineToRelative(dx = 8.0f, dy = 6.0f)
                // l 8 -6
                lineToRelative(dx = 8.0f, dy = -6.0f)
                // h -2.667z
                horizontalLineToRelative(dx = -2.667f)
                close()
            }
            // M13.333 10 8 14 l-5.333 -4 H0 l8 6 8 -6 h-2.667Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 13.333 10
                moveTo(x = 13.333f, y = 10.0f)
                // L 8 14
                lineTo(x = 8.0f, y = 14.0f)
                // l -5.333 -4
                lineToRelative(dx = -5.333f, dy = -4.0f)
                // H 0
                horizontalLineTo(x = 0.0f)
                // l 8 6
                lineToRelative(dx = 8.0f, dy = 6.0f)
                // l 8 -6
                lineToRelative(dx = 8.0f, dy = -6.0f)
                // h -2.667z
                horizontalLineToRelative(dx = -2.667f)
                close()
            }
        }.build().also { _ic1059 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1059: ImageVector? = null
