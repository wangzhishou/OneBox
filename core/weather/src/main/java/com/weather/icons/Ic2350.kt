package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2350: ImageVector
    get() {
        val current = _ic2350
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2350",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M9.038 4.137 a.933 .933 0 0 0 -1.143 .66 l-.376 1.4 -1.025 -1.025 a.933 .933 0 1 0 -1.32 1.32 L6.2 7.518 l-1.4 .375 a.933 .933 0 0 0 .483 1.803 l1.4 -.375 -.375 1.4 a.933 .933 0 1 0 1.803 .483 l.376 -1.4 1.025 1.025 a.933 .933 0 0 0 1.32 -1.32 L9.806 8.484 l1.4 -.376 a.933 .933 0 1 0 -.483 -1.803 l-1.4 .376 .375 -1.4 a.933 .933 0 0 0 -.66 -1.144Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 9.038 4.137
                moveTo(x = 9.038f, y = 4.137f)
                // a 0.933 0.933 0 0 0 -1.143 0.66
                arcToRelative(
                    a = 0.933f,
                    b = 0.933f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.143f,
                    dy1 = 0.66f,
                )
                // l -0.376 1.4
                lineToRelative(dx = -0.376f, dy = 1.4f)
                // l -1.025 -1.025
                lineToRelative(dx = -1.025f, dy = -1.025f)
                // a 0.933 0.933 0 1 0 -1.32 1.32
                arcToRelative(
                    a = 0.933f,
                    b = 0.933f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -1.32f,
                    dy1 = 1.32f,
                )
                // L 6.2 7.518
                lineTo(x = 6.2f, y = 7.518f)
                // l -1.4 0.375
                lineToRelative(dx = -1.4f, dy = 0.375f)
                // a 0.933 0.933 0 0 0 0.483 1.803
                arcToRelative(
                    a = 0.933f,
                    b = 0.933f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.483f,
                    dy1 = 1.803f,
                )
                // l 1.4 -0.375
                lineToRelative(dx = 1.4f, dy = -0.375f)
                // l -0.375 1.4
                lineToRelative(dx = -0.375f, dy = 1.4f)
                // a 0.933 0.933 0 1 0 1.803 0.483
                arcToRelative(
                    a = 0.933f,
                    b = 0.933f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 1.803f,
                    dy1 = 0.483f,
                )
                // l 0.376 -1.4
                lineToRelative(dx = 0.376f, dy = -1.4f)
                // l 1.025 1.025
                lineToRelative(dx = 1.025f, dy = 1.025f)
                // a 0.933 0.933 0 0 0 1.32 -1.32
                arcToRelative(
                    a = 0.933f,
                    b = 0.933f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.32f,
                    dy1 = -1.32f,
                )
                // L 9.806 8.484
                lineTo(x = 9.806f, y = 8.484f)
                // l 1.4 -0.376
                lineToRelative(dx = 1.4f, dy = -0.376f)
                // a 0.933 0.933 0 1 0 -0.483 -1.803
                arcToRelative(
                    a = 0.933f,
                    b = 0.933f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.483f,
                    dy1 = -1.803f,
                )
                // l -1.4 0.376
                lineToRelative(dx = -1.4f, dy = 0.376f)
                // l 0.375 -1.4
                lineToRelative(dx = 0.375f, dy = -1.4f)
                // a 0.933 0.933 0 0 0 -0.66 -1.144z
                arcToRelative(
                    a = 0.933f,
                    b = 0.933f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.66f,
                    dy1 = -1.144f,
                )
                close()
            }
            // M12.746 1.52 a.47 .47 0 0 1 .284 .219 l2.907 5.035 a.468 .468 0 0 1 .047 .355 l-1.505 5.617 a.467 .467 0 0 1 -.218 .284 l-5.035 2.907 a.468 .468 0 0 1 -.355 .047 L3.254 14.48 a.468 .468 0 0 1 -.284 -.218 L.063 9.226 a.468 .468 0 0 1 -.047 -.355 L1.52 3.254 a.468 .468 0 0 1 .218 -.284 L6.774 .063 A.468 .468 0 0 1 7.13 .016 l5.617 1.505Z m1.685 5.453 -2.326 -4.029 a.468 .468 0 0 0 -.284 -.218 L7.328 1.522 a.468 .468 0 0 0 -.355 .047 L2.944 3.895 a.468 .468 0 0 0 -.218 .284 L1.522 8.672 a.468 .468 0 0 0 .047 .355 l2.326 4.029 a.468 .468 0 0 0 .284 .218 l4.493 1.204 c.12 .032 .248 .015 .355 -.047 l4.029 -2.326 a.468 .468 0 0 0 .218 -.284 l1.204 -4.493 a.468 .468 0 0 0 -.047 -.355Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 12.746 1.52
                moveTo(x = 12.746f, y = 1.52f)
                // a 0.47 0.47 0 0 1 0.284 0.219
                arcToRelative(
                    a = 0.47f,
                    b = 0.47f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.284f,
                    dy1 = 0.219f,
                )
                // l 2.907 5.035
                lineToRelative(dx = 2.907f, dy = 5.035f)
                // a 0.468 0.468 0 0 1 0.047 0.355
                arcToRelative(
                    a = 0.468f,
                    b = 0.468f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.047f,
                    dy1 = 0.355f,
                )
                // l -1.505 5.617
                lineToRelative(dx = -1.505f, dy = 5.617f)
                // a 0.467 0.467 0 0 1 -0.218 0.284
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.218f,
                    dy1 = 0.284f,
                )
                // l -5.035 2.907
                lineToRelative(dx = -5.035f, dy = 2.907f)
                // a 0.468 0.468 0 0 1 -0.355 0.047
                arcToRelative(
                    a = 0.468f,
                    b = 0.468f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.355f,
                    dy1 = 0.047f,
                )
                // L 3.254 14.48
                lineTo(x = 3.254f, y = 14.48f)
                // a 0.468 0.468 0 0 1 -0.284 -0.218
                arcToRelative(
                    a = 0.468f,
                    b = 0.468f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.284f,
                    dy1 = -0.218f,
                )
                // L 0.063 9.226
                lineTo(x = 0.063f, y = 9.226f)
                // a 0.468 0.468 0 0 1 -0.047 -0.355
                arcToRelative(
                    a = 0.468f,
                    b = 0.468f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.047f,
                    dy1 = -0.355f,
                )
                // L 1.52 3.254
                lineTo(x = 1.52f, y = 3.254f)
                // a 0.468 0.468 0 0 1 0.218 -0.284
                arcToRelative(
                    a = 0.468f,
                    b = 0.468f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.218f,
                    dy1 = -0.284f,
                )
                // L 6.774 0.063
                lineTo(x = 6.774f, y = 0.063f)
                // A 0.468 0.468 0 0 1 7.13 0.016
                arcTo(
                    horizontalEllipseRadius = 0.468f,
                    verticalEllipseRadius = 0.468f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.13f,
                    y1 = 0.016f,
                )
                // l 5.617 1.505z
                lineToRelative(dx = 5.617f, dy = 1.505f)
                close()
                // m 1.685 5.453
                moveToRelative(dx = 1.685f, dy = 5.453f)
                // l -2.326 -4.029
                lineToRelative(dx = -2.326f, dy = -4.029f)
                // a 0.468 0.468 0 0 0 -0.284 -0.218
                arcToRelative(
                    a = 0.468f,
                    b = 0.468f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.284f,
                    dy1 = -0.218f,
                )
                // L 7.328 1.522
                lineTo(x = 7.328f, y = 1.522f)
                // a 0.468 0.468 0 0 0 -0.355 0.047
                arcToRelative(
                    a = 0.468f,
                    b = 0.468f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.355f,
                    dy1 = 0.047f,
                )
                // L 2.944 3.895
                lineTo(x = 2.944f, y = 3.895f)
                // a 0.468 0.468 0 0 0 -0.218 0.284
                arcToRelative(
                    a = 0.468f,
                    b = 0.468f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.218f,
                    dy1 = 0.284f,
                )
                // L 1.522 8.672
                lineTo(x = 1.522f, y = 8.672f)
                // a 0.468 0.468 0 0 0 0.047 0.355
                arcToRelative(
                    a = 0.468f,
                    b = 0.468f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.047f,
                    dy1 = 0.355f,
                )
                // l 2.326 4.029
                lineToRelative(dx = 2.326f, dy = 4.029f)
                // a 0.468 0.468 0 0 0 0.284 0.218
                arcToRelative(
                    a = 0.468f,
                    b = 0.468f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.284f,
                    dy1 = 0.218f,
                )
                // l 4.493 1.204
                lineToRelative(dx = 4.493f, dy = 1.204f)
                // c 0.12 0.032 0.248 0.015 0.355 -0.047
                curveToRelative(
                    dx1 = 0.12f,
                    dy1 = 0.032f,
                    dx2 = 0.248f,
                    dy2 = 0.015f,
                    dx3 = 0.355f,
                    dy3 = -0.047f,
                )
                // l 4.029 -2.326
                lineToRelative(dx = 4.029f, dy = -2.326f)
                // a 0.468 0.468 0 0 0 0.218 -0.284
                arcToRelative(
                    a = 0.468f,
                    b = 0.468f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.218f,
                    dy1 = -0.284f,
                )
                // l 1.204 -4.493
                lineToRelative(dx = 1.204f, dy = -4.493f)
                // a 0.468 0.468 0 0 0 -0.047 -0.355z
                arcToRelative(
                    a = 0.468f,
                    b = 0.468f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.047f,
                    dy1 = -0.355f,
                )
                close()
            }
        }.build().also { _ic2350 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2350: ImageVector? = null
