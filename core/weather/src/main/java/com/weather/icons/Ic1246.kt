package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1246: ImageVector
    get() {
        val current = _ic1246
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1246",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M6.45 .052 a.21 .21 0 0 1 .337 .09 l.985 2.913 a.21 .21 0 0 1 -.253 .27 l-.38 -.102 .612 2.003 a.21 .21 0 0 1 -.255 .264 l-.344 -.092 .571 1.995 a.21 .21 0 0 1 -.256 .26 l-1.74 -.466 -.287 1.071 h2.015 v.459 l.918 .917 c.23 .23 .344 .344 .344 .46 0 .114 -.115 .229 -.344 .458 -.284 .284 -.217 .392 -.124 .542 .057 .092 .124 .2 .124 .376 0 .229 .23 .344 .459 .458 .229 .115 .458 .23 .458 .46 0 .366 -.306 .764 -.458 .917 v.917 c0 .367 .305 .459 .458 .459 H0 V8.258 h3.268 l.433 -1.614 -1.596 -.427 a.21 .21 0 0 1 -.091 -.354 l1.492 -1.442 -.344 -.092 a.21 .21 0 0 1 -.088 -.356 l1.532 -1.429 -.381 -.102 a.21 .21 0 0 1 -.085 -.36 L6.45 .052Z M16 9.5 v6.423 h-5.079 c-.135 0 -.407 -.092 -.407 -.459 v-.917 c.136 -.153 .408 -.55 .408 -.918 0 -.23 -.204 -.344 -.408 -.459 -.204 -.114 -.408 -.23 -.408 -.459 a.713 .713 0 0 0 -.11 -.376 c-.083 -.15 -.142 -.258 .11 -.541 .204 -.23 .306 -.344 .306 -.459 0 -.115 -.102 -.23 -.306 -.459 L9.29 9.96 V9.5 H16Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6.45 0.052
                moveTo(x = 6.45f, y = 0.052f)
                // a 0.21 0.21 0 0 1 0.337 0.09
                arcToRelative(
                    a = 0.21f,
                    b = 0.21f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.337f,
                    dy1 = 0.09f,
                )
                // l 0.985 2.913
                lineToRelative(dx = 0.985f, dy = 2.913f)
                // a 0.21 0.21 0 0 1 -0.253 0.27
                arcToRelative(
                    a = 0.21f,
                    b = 0.21f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.253f,
                    dy1 = 0.27f,
                )
                // l -0.38 -0.102
                lineToRelative(dx = -0.38f, dy = -0.102f)
                // l 0.612 2.003
                lineToRelative(dx = 0.612f, dy = 2.003f)
                // a 0.21 0.21 0 0 1 -0.255 0.264
                arcToRelative(
                    a = 0.21f,
                    b = 0.21f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.255f,
                    dy1 = 0.264f,
                )
                // l -0.344 -0.092
                lineToRelative(dx = -0.344f, dy = -0.092f)
                // l 0.571 1.995
                lineToRelative(dx = 0.571f, dy = 1.995f)
                // a 0.21 0.21 0 0 1 -0.256 0.26
                arcToRelative(
                    a = 0.21f,
                    b = 0.21f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.256f,
                    dy1 = 0.26f,
                )
                // l -1.74 -0.466
                lineToRelative(dx = -1.74f, dy = -0.466f)
                // l -0.287 1.071
                lineToRelative(dx = -0.287f, dy = 1.071f)
                // h 2.015
                horizontalLineToRelative(dx = 2.015f)
                // v 0.459
                verticalLineToRelative(dy = 0.459f)
                // l 0.918 0.917
                lineToRelative(dx = 0.918f, dy = 0.917f)
                // c 0.23 0.23 0.344 0.344 0.344 0.46
                curveToRelative(
                    dx1 = 0.23f,
                    dy1 = 0.23f,
                    dx2 = 0.344f,
                    dy2 = 0.344f,
                    dx3 = 0.344f,
                    dy3 = 0.46f,
                )
                // c 0 0.114 -0.115 0.229 -0.344 0.458
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.114f,
                    dx2 = -0.115f,
                    dy2 = 0.229f,
                    dx3 = -0.344f,
                    dy3 = 0.458f,
                )
                // c -0.284 0.284 -0.217 0.392 -0.124 0.542
                curveToRelative(
                    dx1 = -0.284f,
                    dy1 = 0.284f,
                    dx2 = -0.217f,
                    dy2 = 0.392f,
                    dx3 = -0.124f,
                    dy3 = 0.542f,
                )
                // c 0.057 0.092 0.124 0.2 0.124 0.376
                curveToRelative(
                    dx1 = 0.057f,
                    dy1 = 0.092f,
                    dx2 = 0.124f,
                    dy2 = 0.2f,
                    dx3 = 0.124f,
                    dy3 = 0.376f,
                )
                // c 0 0.229 0.23 0.344 0.459 0.458
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.229f,
                    dx2 = 0.23f,
                    dy2 = 0.344f,
                    dx3 = 0.459f,
                    dy3 = 0.458f,
                )
                // c 0.229 0.115 0.458 0.23 0.458 0.46
                curveToRelative(
                    dx1 = 0.229f,
                    dy1 = 0.115f,
                    dx2 = 0.458f,
                    dy2 = 0.23f,
                    dx3 = 0.458f,
                    dy3 = 0.46f,
                )
                // c 0 0.366 -0.306 0.764 -0.458 0.917
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.366f,
                    dx2 = -0.306f,
                    dy2 = 0.764f,
                    dx3 = -0.458f,
                    dy3 = 0.917f,
                )
                // v 0.917
                verticalLineToRelative(dy = 0.917f)
                // c 0 0.367 0.305 0.459 0.458 0.459
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.367f,
                    dx2 = 0.305f,
                    dy2 = 0.459f,
                    dx3 = 0.458f,
                    dy3 = 0.459f,
                )
                // H 0
                horizontalLineTo(x = 0.0f)
                // V 8.258
                verticalLineTo(y = 8.258f)
                // h 3.268
                horizontalLineToRelative(dx = 3.268f)
                // l 0.433 -1.614
                lineToRelative(dx = 0.433f, dy = -1.614f)
                // l -1.596 -0.427
                lineToRelative(dx = -1.596f, dy = -0.427f)
                // a 0.21 0.21 0 0 1 -0.091 -0.354
                arcToRelative(
                    a = 0.21f,
                    b = 0.21f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.091f,
                    dy1 = -0.354f,
                )
                // l 1.492 -1.442
                lineToRelative(dx = 1.492f, dy = -1.442f)
                // l -0.344 -0.092
                lineToRelative(dx = -0.344f, dy = -0.092f)
                // a 0.21 0.21 0 0 1 -0.088 -0.356
                arcToRelative(
                    a = 0.21f,
                    b = 0.21f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.088f,
                    dy1 = -0.356f,
                )
                // l 1.532 -1.429
                lineToRelative(dx = 1.532f, dy = -1.429f)
                // l -0.381 -0.102
                lineToRelative(dx = -0.381f, dy = -0.102f)
                // a 0.21 0.21 0 0 1 -0.085 -0.36
                arcToRelative(
                    a = 0.21f,
                    b = 0.21f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.085f,
                    dy1 = -0.36f,
                )
                // L 6.45 0.052z
                lineTo(x = 6.45f, y = 0.052f)
                close()
                // M 16 9.5
                moveTo(x = 16.0f, y = 9.5f)
                // v 6.423
                verticalLineToRelative(dy = 6.423f)
                // h -5.079
                horizontalLineToRelative(dx = -5.079f)
                // c -0.135 0 -0.407 -0.092 -0.407 -0.459
                curveToRelative(
                    dx1 = -0.135f,
                    dy1 = 0.0f,
                    dx2 = -0.407f,
                    dy2 = -0.092f,
                    dx3 = -0.407f,
                    dy3 = -0.459f,
                )
                // v -0.917
                verticalLineToRelative(dy = -0.917f)
                // c 0.136 -0.153 0.408 -0.55 0.408 -0.918
                curveToRelative(
                    dx1 = 0.136f,
                    dy1 = -0.153f,
                    dx2 = 0.408f,
                    dy2 = -0.55f,
                    dx3 = 0.408f,
                    dy3 = -0.918f,
                )
                // c 0 -0.23 -0.204 -0.344 -0.408 -0.459
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.23f,
                    dx2 = -0.204f,
                    dy2 = -0.344f,
                    dx3 = -0.408f,
                    dy3 = -0.459f,
                )
                // c -0.204 -0.114 -0.408 -0.23 -0.408 -0.459
                curveToRelative(
                    dx1 = -0.204f,
                    dy1 = -0.114f,
                    dx2 = -0.408f,
                    dy2 = -0.23f,
                    dx3 = -0.408f,
                    dy3 = -0.459f,
                )
                // a 0.713 0.713 0 0 0 -0.11 -0.376
                arcToRelative(
                    a = 0.713f,
                    b = 0.713f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.11f,
                    dy1 = -0.376f,
                )
                // c -0.083 -0.15 -0.142 -0.258 0.11 -0.541
                curveToRelative(
                    dx1 = -0.083f,
                    dy1 = -0.15f,
                    dx2 = -0.142f,
                    dy2 = -0.258f,
                    dx3 = 0.11f,
                    dy3 = -0.541f,
                )
                // c 0.204 -0.23 0.306 -0.344 0.306 -0.459
                curveToRelative(
                    dx1 = 0.204f,
                    dy1 = -0.23f,
                    dx2 = 0.306f,
                    dy2 = -0.344f,
                    dx3 = 0.306f,
                    dy3 = -0.459f,
                )
                // c 0 -0.115 -0.102 -0.23 -0.306 -0.459
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.115f,
                    dx2 = -0.102f,
                    dy2 = -0.23f,
                    dx3 = -0.306f,
                    dy3 = -0.459f,
                )
                // L 9.29 9.96
                lineTo(x = 9.29f, y = 9.96f)
                // V 9.5
                verticalLineTo(y = 9.5f)
                // H 16z
                horizontalLineTo(x = 16.0f)
                close()
            }
        }.build().also { _ic1246 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1246: ImageVector? = null
