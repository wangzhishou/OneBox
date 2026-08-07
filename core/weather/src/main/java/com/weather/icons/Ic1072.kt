package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1072: ImageVector
    get() {
        val current = _ic1072
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1072",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M3.931 1.01 a.25 .25 0 0 1 -.032 .499 C2.595 1.424 1.254 1.95 .45 3.036 a.25 .25 0 1 1 -.402 -.297 C.966 1.499 2.48 .916 3.93 1.009Z m-.383 1.459 a.25 .25 0 0 1 .035 .498 3.392 3.392 0 0 0 -2.042 .85 .25 .25 0 0 1 -.336 -.37 3.892 3.892 0 0 1 2.343 -.978Z m8.521 -1.459 a.25 .25 0 1 0 .032 .499 c1.304 -.085 2.645 .442 3.448 1.527 a.25 .25 0 0 0 .402 -.297 c-.918 -1.24 -2.431 -1.823 -3.882 -1.73Z m.383 1.459 a.25 .25 0 0 0 -.035 .498 3.4 3.4 0 0 1 2.042 .85 .25 .25 0 0 0 .336 -.37 3.892 3.892 0 0 0 -2.343 -.978Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 3.931 1.01
                moveTo(x = 3.931f, y = 1.01f)
                // a 0.25 0.25 0 0 1 -0.032 0.499
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.032f,
                    dy1 = 0.499f,
                )
                // C 2.595 1.424 1.254 1.95 0.45 3.036
                curveTo(
                    x1 = 2.595f,
                    y1 = 1.424f,
                    x2 = 1.254f,
                    y2 = 1.95f,
                    x3 = 0.45f,
                    y3 = 3.036f,
                )
                // a 0.25 0.25 0 1 1 -0.402 -0.297
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.402f,
                    dy1 = -0.297f,
                )
                // C 0.966 1.499 2.48 0.916 3.93 1.009z
                curveTo(
                    x1 = 0.966f,
                    y1 = 1.499f,
                    x2 = 2.48f,
                    y2 = 0.916f,
                    x3 = 3.93f,
                    y3 = 1.009f,
                )
                close()
                // m -0.383 1.459
                moveToRelative(dx = -0.383f, dy = 1.459f)
                // a 0.25 0.25 0 0 1 0.035 0.498
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.035f,
                    dy1 = 0.498f,
                )
                // a 3.392 3.392 0 0 0 -2.042 0.85
                arcToRelative(
                    a = 3.392f,
                    b = 3.392f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.042f,
                    dy1 = 0.85f,
                )
                // a 0.25 0.25 0 0 1 -0.336 -0.37
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.336f,
                    dy1 = -0.37f,
                )
                // a 3.892 3.892 0 0 1 2.343 -0.978z
                arcToRelative(
                    a = 3.892f,
                    b = 3.892f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.343f,
                    dy1 = -0.978f,
                )
                close()
                // m 8.521 -1.459
                moveToRelative(dx = 8.521f, dy = -1.459f)
                // a 0.25 0.25 0 1 0 0.032 0.499
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.032f,
                    dy1 = 0.499f,
                )
                // c 1.304 -0.085 2.645 0.442 3.448 1.527
                curveToRelative(
                    dx1 = 1.304f,
                    dy1 = -0.085f,
                    dx2 = 2.645f,
                    dy2 = 0.442f,
                    dx3 = 3.448f,
                    dy3 = 1.527f,
                )
                // a 0.25 0.25 0 0 0 0.402 -0.297
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.402f,
                    dy1 = -0.297f,
                )
                // c -0.918 -1.24 -2.431 -1.823 -3.882 -1.73z
                curveToRelative(
                    dx1 = -0.918f,
                    dy1 = -1.24f,
                    dx2 = -2.431f,
                    dy2 = -1.823f,
                    dx3 = -3.882f,
                    dy3 = -1.73f,
                )
                close()
                // m 0.383 1.459
                moveToRelative(dx = 0.383f, dy = 1.459f)
                // a 0.25 0.25 0 0 0 -0.035 0.498
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.035f,
                    dy1 = 0.498f,
                )
                // a 3.4 3.4 0 0 1 2.042 0.85
                arcToRelative(
                    a = 3.4f,
                    b = 3.4f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.042f,
                    dy1 = 0.85f,
                )
                // a 0.25 0.25 0 0 0 0.336 -0.37
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.336f,
                    dy1 = -0.37f,
                )
                // a 3.892 3.892 0 0 0 -2.343 -0.978z
                arcToRelative(
                    a = 3.892f,
                    b = 3.892f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.343f,
                    dy1 = -0.978f,
                )
                close()
            }
            // M8 4.685 c3.884 -3.767 13.593 2.825 0 11.3 C-5.592 7.51 4.116 .918 8 4.685Z M8 6 c-.587 0 -1.047 .346 -.996 .747 l.45 3.528 h1.092 l.45 -3.528 C9.047 6.346 8.586 6 8 6Z m0 6 c.414 0 .75 -.252 .75 -.563 0 -.31 -.336 -.562 -.75 -.562 s-.75 .252 -.75 .563 c0 .31 .336 .562 .75 .562Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8 4.685
                moveTo(x = 8.0f, y = 4.685f)
                // c 3.884 -3.767 13.593 2.825 0 11.3
                curveToRelative(
                    dx1 = 3.884f,
                    dy1 = -3.767f,
                    dx2 = 13.593f,
                    dy2 = 2.825f,
                    dx3 = 0.0f,
                    dy3 = 11.3f,
                )
                // C -5.592 7.51 4.116 0.918 8 4.685z
                curveTo(
                    x1 = -5.592f,
                    y1 = 7.51f,
                    x2 = 4.116f,
                    y2 = 0.918f,
                    x3 = 8.0f,
                    y3 = 4.685f,
                )
                close()
                // M 8 6
                moveTo(x = 8.0f, y = 6.0f)
                // c -0.587 0 -1.047 0.346 -0.996 0.747
                curveToRelative(
                    dx1 = -0.587f,
                    dy1 = 0.0f,
                    dx2 = -1.047f,
                    dy2 = 0.346f,
                    dx3 = -0.996f,
                    dy3 = 0.747f,
                )
                // l 0.45 3.528
                lineToRelative(dx = 0.45f, dy = 3.528f)
                // h 1.092
                horizontalLineToRelative(dx = 1.092f)
                // l 0.45 -3.528
                lineToRelative(dx = 0.45f, dy = -3.528f)
                // C 9.047 6.346 8.586 6 8 6z
                curveTo(
                    x1 = 9.047f,
                    y1 = 6.346f,
                    x2 = 8.586f,
                    y2 = 6.0f,
                    x3 = 8.0f,
                    y3 = 6.0f,
                )
                close()
                // m 0 6
                moveToRelative(dx = 0.0f, dy = 6.0f)
                // c 0.414 0 0.75 -0.252 0.75 -0.563
                curveToRelative(
                    dx1 = 0.414f,
                    dy1 = 0.0f,
                    dx2 = 0.75f,
                    dy2 = -0.252f,
                    dx3 = 0.75f,
                    dy3 = -0.563f,
                )
                // c 0 -0.31 -0.336 -0.562 -0.75 -0.562
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.31f,
                    dx2 = -0.336f,
                    dy2 = -0.562f,
                    dx3 = -0.75f,
                    dy3 = -0.562f,
                )
                // s -0.75 0.252 -0.75 0.563
                reflectiveCurveToRelative(
                    dx1 = -0.75f,
                    dy1 = 0.252f,
                    dx2 = -0.75f,
                    dy2 = 0.563f,
                )
                // c 0 0.31 0.336 0.562 0.75 0.562z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.31f,
                    dx2 = 0.336f,
                    dy2 = 0.562f,
                    dx3 = 0.75f,
                    dy3 = 0.562f,
                )
                close()
            }
        }.build().also { _ic1072 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1072: ImageVector? = null
