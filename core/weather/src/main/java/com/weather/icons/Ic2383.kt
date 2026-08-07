package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2383: ImageVector
    get() {
        val current = _ic2383
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2383",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.253 8.497 c-.039 -.401 .307 -.747 .747 -.747 s.786 .346 .747 .747 l-.338 3.528 h-.818 l-.338 -3.528Z m1.309 4.691 a.562 .562 0 1 1 -1.124 0 .562 .562 0 0 1 1.124 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.253 8.497
                moveTo(x = 7.253f, y = 8.497f)
                // c -0.039 -0.401 0.307 -0.747 0.747 -0.747
                curveToRelative(
                    dx1 = -0.039f,
                    dy1 = -0.401f,
                    dx2 = 0.307f,
                    dy2 = -0.747f,
                    dx3 = 0.747f,
                    dy3 = -0.747f,
                )
                // s 0.786 0.346 0.747 0.747
                reflectiveCurveToRelative(
                    dx1 = 0.786f,
                    dy1 = 0.346f,
                    dx2 = 0.747f,
                    dy2 = 0.747f,
                )
                // l -0.338 3.528
                lineToRelative(dx = -0.338f, dy = 3.528f)
                // h -0.818
                horizontalLineToRelative(dx = -0.818f)
                // l -0.338 -3.528z
                lineToRelative(dx = -0.338f, dy = -3.528f)
                close()
                // m 1.309 4.691
                moveToRelative(dx = 1.309f, dy = 4.691f)
                // a 0.562 0.562 0 1 1 -1.124 0
                arcToRelative(
                    a = 0.562f,
                    b = 0.562f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.124f,
                    dy1 = 0.0f,
                )
                // a 0.562 0.562 0 0 1 1.124 0z
                arcToRelative(
                    a = 0.562f,
                    b = 0.562f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.124f,
                    dy1 = 0.0f,
                )
                close()
            }
            // M7.659 4.946 a.395 .395 0 0 1 .682 0 l5.607 9.722 a.39 .39 0 0 1 -.341 .582 H2.393 a.39 .39 0 0 1 -.34 -.582 l5.606 -9.722Z m5.066 9.404 L8 6.156 3.275 14.35 h9.45Z M5.5 1.75 a.75 .75 0 1 1 -1.5 0 .75 .75 0 0 1 1.5 0Z m5 1.25 a.5 .5 0 1 0 0 -1 .5 .5 0 0 0 0 1Z M3 4.5 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z m-1 3 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z M2 11 a1 1 0 1 0 0 -2 1 1 0 0 0 0 2Z m1.5 -3 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z M11 7 a.75 .75 0 1 0 0 -1.5 .75 .75 0 0 0 0 1.5Z m3.75 -.25 a.75 .75 0 1 1 -1.5 0 .75 .75 0 0 1 1.5 0Z M11.5 9 a.5 .5 0 1 0 0 -1 .5 .5 0 0 0 0 1Z m2 -1 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z M6 4.5 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z m8 7.5 a1 1 0 1 0 0 -2 1 1 0 0 0 0 2Z M8 3 a1 1 0 1 1 -2 0 1 1 0 0 1 2 0Z m4.5 2 a.5 .5 0 1 0 0 -1 .5 .5 0 0 0 0 1Z m-2 -1 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z M4.75 7.5 a.75 .75 0 1 0 0 -1.5 .75 .75 0 0 0 0 1.5Z M3.5 10 a.5 .5 0 1 0 0 -1 .5 .5 0 0 0 0 1Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.659 4.946
                moveTo(x = 7.659f, y = 4.946f)
                // a 0.395 0.395 0 0 1 0.682 0
                arcToRelative(
                    a = 0.395f,
                    b = 0.395f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.682f,
                    dy1 = 0.0f,
                )
                // l 5.607 9.722
                lineToRelative(dx = 5.607f, dy = 9.722f)
                // a 0.39 0.39 0 0 1 -0.341 0.582
                arcToRelative(
                    a = 0.39f,
                    b = 0.39f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.341f,
                    dy1 = 0.582f,
                )
                // H 2.393
                horizontalLineTo(x = 2.393f)
                // a 0.39 0.39 0 0 1 -0.34 -0.582
                arcToRelative(
                    a = 0.39f,
                    b = 0.39f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.34f,
                    dy1 = -0.582f,
                )
                // l 5.606 -9.722z
                lineToRelative(dx = 5.606f, dy = -9.722f)
                close()
                // m 5.066 9.404
                moveToRelative(dx = 5.066f, dy = 9.404f)
                // L 8 6.156
                lineTo(x = 8.0f, y = 6.156f)
                // L 3.275 14.35
                lineTo(x = 3.275f, y = 14.35f)
                // h 9.45z
                horizontalLineToRelative(dx = 9.45f)
                close()
                // M 5.5 1.75
                moveTo(x = 5.5f, y = 1.75f)
                // a 0.75 0.75 0 1 1 -1.5 0
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.5f,
                    dy1 = 0.0f,
                )
                // a 0.75 0.75 0 0 1 1.5 0z
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.5f,
                    dy1 = 0.0f,
                )
                close()
                // m 5 1.25
                moveToRelative(dx = 5.0f, dy = 1.25f)
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
                // M 3 4.5
                moveTo(x = 3.0f, y = 4.5f)
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
                // m -1 3
                moveToRelative(dx = -1.0f, dy = 3.0f)
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
                // M 2 11
                moveTo(x = 2.0f, y = 11.0f)
                // a 1 1 0 1 0 0 -2
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -2.0f,
                )
                // a 1 1 0 0 0 0 2z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 2.0f,
                )
                close()
                // m 1.5 -3
                moveToRelative(dx = 1.5f, dy = -3.0f)
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
                // M 11 7
                moveTo(x = 11.0f, y = 7.0f)
                // a 0.75 0.75 0 1 0 0 -1.5
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.5f,
                )
                // a 0.75 0.75 0 0 0 0 1.5z
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.5f,
                )
                close()
                // m 3.75 -0.25
                moveToRelative(dx = 3.75f, dy = -0.25f)
                // a 0.75 0.75 0 1 1 -1.5 0
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.5f,
                    dy1 = 0.0f,
                )
                // a 0.75 0.75 0 0 1 1.5 0z
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.5f,
                    dy1 = 0.0f,
                )
                close()
                // M 11.5 9
                moveTo(x = 11.5f, y = 9.0f)
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
                // m 2 -1
                moveToRelative(dx = 2.0f, dy = -1.0f)
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
                // M 6 4.5
                moveTo(x = 6.0f, y = 4.5f)
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
                // m 8 7.5
                moveToRelative(dx = 8.0f, dy = 7.5f)
                // a 1 1 0 1 0 0 -2
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -2.0f,
                )
                // a 1 1 0 0 0 0 2z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 2.0f,
                )
                close()
                // M 8 3
                moveTo(x = 8.0f, y = 3.0f)
                // a 1 1 0 1 1 -2 0
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -2.0f,
                    dy1 = 0.0f,
                )
                // a 1 1 0 0 1 2 0z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.0f,
                    dy1 = 0.0f,
                )
                close()
                // m 4.5 2
                moveToRelative(dx = 4.5f, dy = 2.0f)
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
                // m -2 -1
                moveToRelative(dx = -2.0f, dy = -1.0f)
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
                // M 4.75 7.5
                moveTo(x = 4.75f, y = 7.5f)
                // a 0.75 0.75 0 1 0 0 -1.5
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.5f,
                )
                // a 0.75 0.75 0 0 0 0 1.5z
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.5f,
                )
                close()
                // M 3.5 10
                moveTo(x = 3.5f, y = 10.0f)
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
            }
        }.build().also { _ic2383 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2383: ImageVector? = null
