package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2422: ImageVector
    get() {
        val current = _ic2422
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2422",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.998 7.633 c.234 .26 .68 .509 1.34 .747 l.643 .23 c.225 .082 .397 .194 .515 .336 a.74 .74 0 0 1 .178 .491 .701 .701 0 0 1 -.267 .584 c-.177 .137 -.427 .205 -.75 .205 -.319 0 -.643 -.057 -.971 -.172 a4.521 4.521 0 0 1 -1.012 -.524 v1.28 a4.5 4.5 0 0 0 .954 .327 c.331 .075 .661 .113 .99 .113 .798 0 1.394 -.155 1.787 -.466 .397 -.313 .595 -.786 .595 -1.418 0 -.464 -.126 -.85 -.377 -1.154 -.252 -.305 -.643 -.55 -1.176 -.734 l-.563 -.202 c-.39 -.137 -.648 -.261 -.772 -.373 a.57 .57 0 0 1 -.182 -.433 c0 -.223 .084 -.395 .253 -.516 .168 -.12 .41 -.18 .727 -.18 .284 0 .571 .049 .861 .147 .29 .098 .575 .24 .856 .428 V5.14 a4.782 4.782 0 0 0 -.905 -.29 4.3 4.3 0 0 0 -.914 -.1 c-.671 0 -1.199 .162 -1.584 .487 -.384 .324 -.576 .77 -.576 1.338 0 .442 .117 .795 .35 1.058Z m-4.317 3.495 h1.31 V5.946 h1.68 V4.863 H3 v1.083 h1.681 v5.182Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.998 7.633
                moveTo(x = 8.998f, y = 7.633f)
                // c 0.234 0.26 0.68 0.509 1.34 0.747
                curveToRelative(
                    dx1 = 0.234f,
                    dy1 = 0.26f,
                    dx2 = 0.68f,
                    dy2 = 0.509f,
                    dx3 = 1.34f,
                    dy3 = 0.747f,
                )
                // l 0.643 0.23
                lineToRelative(dx = 0.643f, dy = 0.23f)
                // c 0.225 0.082 0.397 0.194 0.515 0.336
                curveToRelative(
                    dx1 = 0.225f,
                    dy1 = 0.082f,
                    dx2 = 0.397f,
                    dy2 = 0.194f,
                    dx3 = 0.515f,
                    dy3 = 0.336f,
                )
                // a 0.74 0.74 0 0 1 0.178 0.491
                arcToRelative(
                    a = 0.74f,
                    b = 0.74f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.178f,
                    dy1 = 0.491f,
                )
                // a 0.701 0.701 0 0 1 -0.267 0.584
                arcToRelative(
                    a = 0.701f,
                    b = 0.701f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.267f,
                    dy1 = 0.584f,
                )
                // c -0.177 0.137 -0.427 0.205 -0.75 0.205
                curveToRelative(
                    dx1 = -0.177f,
                    dy1 = 0.137f,
                    dx2 = -0.427f,
                    dy2 = 0.205f,
                    dx3 = -0.75f,
                    dy3 = 0.205f,
                )
                // c -0.319 0 -0.643 -0.057 -0.971 -0.172
                curveToRelative(
                    dx1 = -0.319f,
                    dy1 = 0.0f,
                    dx2 = -0.643f,
                    dy2 = -0.057f,
                    dx3 = -0.971f,
                    dy3 = -0.172f,
                )
                // a 4.521 4.521 0 0 1 -1.012 -0.524
                arcToRelative(
                    a = 4.521f,
                    b = 4.521f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.012f,
                    dy1 = -0.524f,
                )
                // v 1.28
                verticalLineToRelative(dy = 1.28f)
                // a 4.5 4.5 0 0 0 0.954 0.327
                arcToRelative(
                    a = 4.5f,
                    b = 4.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.954f,
                    dy1 = 0.327f,
                )
                // c 0.331 0.075 0.661 0.113 0.99 0.113
                curveToRelative(
                    dx1 = 0.331f,
                    dy1 = 0.075f,
                    dx2 = 0.661f,
                    dy2 = 0.113f,
                    dx3 = 0.99f,
                    dy3 = 0.113f,
                )
                // c 0.798 0 1.394 -0.155 1.787 -0.466
                curveToRelative(
                    dx1 = 0.798f,
                    dy1 = 0.0f,
                    dx2 = 1.394f,
                    dy2 = -0.155f,
                    dx3 = 1.787f,
                    dy3 = -0.466f,
                )
                // c 0.397 -0.313 0.595 -0.786 0.595 -1.418
                curveToRelative(
                    dx1 = 0.397f,
                    dy1 = -0.313f,
                    dx2 = 0.595f,
                    dy2 = -0.786f,
                    dx3 = 0.595f,
                    dy3 = -1.418f,
                )
                // c 0 -0.464 -0.126 -0.85 -0.377 -1.154
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.464f,
                    dx2 = -0.126f,
                    dy2 = -0.85f,
                    dx3 = -0.377f,
                    dy3 = -1.154f,
                )
                // c -0.252 -0.305 -0.643 -0.55 -1.176 -0.734
                curveToRelative(
                    dx1 = -0.252f,
                    dy1 = -0.305f,
                    dx2 = -0.643f,
                    dy2 = -0.55f,
                    dx3 = -1.176f,
                    dy3 = -0.734f,
                )
                // l -0.563 -0.202
                lineToRelative(dx = -0.563f, dy = -0.202f)
                // c -0.39 -0.137 -0.648 -0.261 -0.772 -0.373
                curveToRelative(
                    dx1 = -0.39f,
                    dy1 = -0.137f,
                    dx2 = -0.648f,
                    dy2 = -0.261f,
                    dx3 = -0.772f,
                    dy3 = -0.373f,
                )
                // a 0.57 0.57 0 0 1 -0.182 -0.433
                arcToRelative(
                    a = 0.57f,
                    b = 0.57f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.182f,
                    dy1 = -0.433f,
                )
                // c 0 -0.223 0.084 -0.395 0.253 -0.516
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.223f,
                    dx2 = 0.084f,
                    dy2 = -0.395f,
                    dx3 = 0.253f,
                    dy3 = -0.516f,
                )
                // c 0.168 -0.12 0.41 -0.18 0.727 -0.18
                curveToRelative(
                    dx1 = 0.168f,
                    dy1 = -0.12f,
                    dx2 = 0.41f,
                    dy2 = -0.18f,
                    dx3 = 0.727f,
                    dy3 = -0.18f,
                )
                // c 0.284 0 0.571 0.049 0.861 0.147
                curveToRelative(
                    dx1 = 0.284f,
                    dy1 = 0.0f,
                    dx2 = 0.571f,
                    dy2 = 0.049f,
                    dx3 = 0.861f,
                    dy3 = 0.147f,
                )
                // c 0.29 0.098 0.575 0.24 0.856 0.428
                curveToRelative(
                    dx1 = 0.29f,
                    dy1 = 0.098f,
                    dx2 = 0.575f,
                    dy2 = 0.24f,
                    dx3 = 0.856f,
                    dy3 = 0.428f,
                )
                // V 5.14
                verticalLineTo(y = 5.14f)
                // a 4.782 4.782 0 0 0 -0.905 -0.29
                arcToRelative(
                    a = 4.782f,
                    b = 4.782f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.905f,
                    dy1 = -0.29f,
                )
                // a 4.3 4.3 0 0 0 -0.914 -0.1
                arcToRelative(
                    a = 4.3f,
                    b = 4.3f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.914f,
                    dy1 = -0.1f,
                )
                // c -0.671 0 -1.199 0.162 -1.584 0.487
                curveToRelative(
                    dx1 = -0.671f,
                    dy1 = 0.0f,
                    dx2 = -1.199f,
                    dy2 = 0.162f,
                    dx3 = -1.584f,
                    dy3 = 0.487f,
                )
                // c -0.384 0.324 -0.576 0.77 -0.576 1.338
                curveToRelative(
                    dx1 = -0.384f,
                    dy1 = 0.324f,
                    dx2 = -0.576f,
                    dy2 = 0.77f,
                    dx3 = -0.576f,
                    dy3 = 1.338f,
                )
                // c 0 0.442 0.117 0.795 0.35 1.058z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.442f,
                    dx2 = 0.117f,
                    dy2 = 0.795f,
                    dx3 = 0.35f,
                    dy3 = 1.058f,
                )
                close()
                // m -4.317 3.495
                moveToRelative(dx = -4.317f, dy = 3.495f)
                // h 1.31
                horizontalLineToRelative(dx = 1.31f)
                // V 5.946
                verticalLineTo(y = 5.946f)
                // h 1.68
                horizontalLineToRelative(dx = 1.68f)
                // V 4.863
                verticalLineTo(y = 4.863f)
                // H 3
                horizontalLineTo(x = 3.0f)
                // v 1.083
                verticalLineToRelative(dy = 1.083f)
                // h 1.681
                horizontalLineToRelative(dx = 1.681f)
                // v 5.182z
                verticalLineToRelative(dy = 5.182f)
                close()
            }
            // M0 8 a8 8 0 1 0 16 0 A8 8 0 0 0 0 8Z m14.7 0 A6.7 6.7 0 1 1 1.3 8 a6.7 6.7 0 0 1 13.4 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0 8
                moveTo(x = 0.0f, y = 8.0f)
                // a 8 8 0 1 0 16 0
                arcToRelative(
                    a = 8.0f,
                    b = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 16.0f,
                    dy1 = 0.0f,
                )
                // A 8 8 0 0 0 0 8z
                arcTo(
                    horizontalEllipseRadius = 8.0f,
                    verticalEllipseRadius = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 0.0f,
                    y1 = 8.0f,
                )
                close()
                // m 14.7 0
                moveToRelative(dx = 14.7f, dy = 0.0f)
                // A 6.7 6.7 0 1 1 1.3 8
                arcTo(
                    horizontalEllipseRadius = 6.7f,
                    verticalEllipseRadius = 6.7f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 1.3f,
                    y1 = 8.0f,
                )
                // a 6.7 6.7 0 0 1 13.4 0z
                arcToRelative(
                    a = 6.7f,
                    b = 6.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 13.4f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic2422 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2422: ImageVector? = null
