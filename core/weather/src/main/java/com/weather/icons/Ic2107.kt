package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2107: ImageVector
    get() {
        val current = _ic2107
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2107",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M10.8 3.5 a.9 .9 0 1 1 -1.8 0 .9 .9 0 0 1 1.8 0Z m-3.5 .9 a.7 .7 0 1 0 0 -1.4 .7 .7 0 0 0 0 1.4Z m-1.7 -.6 a.6 .6 0 1 1 -1.2 0 .6 .6 0 0 1 1.2 0Z m-2.2 .1 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z m9.3 .5 a.9 .9 0 1 0 0 -1.8 .9 .9 0 0 0 0 1.8Z m2.3 6.2 a1 1 0 1 0 0 -2 1 1 0 0 0 0 2Z m-5.7 0 a.9 .9 0 1 0 0 -1.8 .9 .9 0 0 0 0 1.8Z m-1.9 -.7 a.7 .7 0 1 1 -1.4 0 .7 .7 0 0 1 1.4 0Z m-3 .7 a.6 .6 0 1 0 0 -1.2 .6 .6 0 0 0 0 1.2Z m-2.1 0 a.5 .5 0 1 0 0 -1 .5 .5 0 0 0 0 1Z M13 9.7 a.9 .9 0 1 1 -1.8 0 .9 .9 0 0 1 1.8 0Z M2.4 12.5 a.9 .9 0 1 0 1.8 0 .9 .9 0 0 0 -1.8 0Z m3.7 .9 a.9 .9 0 1 1 0 -1.8 .9 .9 0 0 1 0 1.8Z m1.9 -.7 a.7 .7 0 1 0 1.4 0 .7 .7 0 0 0 -1.4 0Z m3 .7 a.6 .6 0 1 1 0 -1.2 .6 .6 0 0 1 0 1.2Z m2.1 0 a.5 .5 0 1 1 0 -1 .5 .5 0 0 1 0 1Z M1 7.6 a1 1 0 1 1 0 -2 1 1 0 0 1 0 2Z m2 -.9 a.9 .9 0 1 0 1.8 0 .9 .9 0 0 0 -1.8 0Z m3.7 .9 a.9 .9 0 1 1 0 -1.8 .9 .9 0 0 1 0 1.8Z m1.9 -.7 a.7 .7 0 1 0 1.4 0 .7 .7 0 0 0 -1.4 0Z m3 .7 a.6 .6 0 1 1 0 -1.2 .6 .6 0 0 1 0 1.2Z m2.1 0 a.5 .5 0 1 1 0 -1 .5 .5 0 0 1 0 1Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.8 3.5
                moveTo(x = 10.8f, y = 3.5f)
                // a 0.9 0.9 0 1 1 -1.8 0
                arcToRelative(
                    a = 0.9f,
                    b = 0.9f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.8f,
                    dy1 = 0.0f,
                )
                // a 0.9 0.9 0 0 1 1.8 0z
                arcToRelative(
                    a = 0.9f,
                    b = 0.9f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.8f,
                    dy1 = 0.0f,
                )
                close()
                // m -3.5 0.9
                moveToRelative(dx = -3.5f, dy = 0.9f)
                // a 0.7 0.7 0 1 0 0 -1.4
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.4f,
                )
                // a 0.7 0.7 0 0 0 0 1.4z
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.4f,
                )
                close()
                // m -1.7 -0.6
                moveToRelative(dx = -1.7f, dy = -0.6f)
                // a 0.6 0.6 0 1 1 -1.2 0
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.2f,
                    dy1 = 0.0f,
                )
                // a 0.6 0.6 0 0 1 1.2 0z
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.2f,
                    dy1 = 0.0f,
                )
                close()
                // m -2.2 0.1
                moveToRelative(dx = -2.2f, dy = 0.1f)
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
                // m 9.3 0.5
                moveToRelative(dx = 9.3f, dy = 0.5f)
                // a 0.9 0.9 0 1 0 0 -1.8
                arcToRelative(
                    a = 0.9f,
                    b = 0.9f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.8f,
                )
                // a 0.9 0.9 0 0 0 0 1.8z
                arcToRelative(
                    a = 0.9f,
                    b = 0.9f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.8f,
                )
                close()
                // m 2.3 6.2
                moveToRelative(dx = 2.3f, dy = 6.2f)
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
                // m -5.7 0
                moveToRelative(dx = -5.7f, dy = 0.0f)
                // a 0.9 0.9 0 1 0 0 -1.8
                arcToRelative(
                    a = 0.9f,
                    b = 0.9f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.8f,
                )
                // a 0.9 0.9 0 0 0 0 1.8z
                arcToRelative(
                    a = 0.9f,
                    b = 0.9f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.8f,
                )
                close()
                // m -1.9 -0.7
                moveToRelative(dx = -1.9f, dy = -0.7f)
                // a 0.7 0.7 0 1 1 -1.4 0
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.4f,
                    dy1 = 0.0f,
                )
                // a 0.7 0.7 0 0 1 1.4 0z
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.4f,
                    dy1 = 0.0f,
                )
                close()
                // m -3 0.7
                moveToRelative(dx = -3.0f, dy = 0.7f)
                // a 0.6 0.6 0 1 0 0 -1.2
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.2f,
                )
                // a 0.6 0.6 0 0 0 0 1.2z
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.2f,
                )
                close()
                // m -2.1 0
                moveToRelative(dx = -2.1f, dy = 0.0f)
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
                // M 13 9.7
                moveTo(x = 13.0f, y = 9.7f)
                // a 0.9 0.9 0 1 1 -1.8 0
                arcToRelative(
                    a = 0.9f,
                    b = 0.9f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.8f,
                    dy1 = 0.0f,
                )
                // a 0.9 0.9 0 0 1 1.8 0z
                arcToRelative(
                    a = 0.9f,
                    b = 0.9f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.8f,
                    dy1 = 0.0f,
                )
                close()
                // M 2.4 12.5
                moveTo(x = 2.4f, y = 12.5f)
                // a 0.9 0.9 0 1 0 1.8 0
                arcToRelative(
                    a = 0.9f,
                    b = 0.9f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 1.8f,
                    dy1 = 0.0f,
                )
                // a 0.9 0.9 0 0 0 -1.8 0z
                arcToRelative(
                    a = 0.9f,
                    b = 0.9f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.8f,
                    dy1 = 0.0f,
                )
                close()
                // m 3.7 0.9
                moveToRelative(dx = 3.7f, dy = 0.9f)
                // a 0.9 0.9 0 1 1 0 -1.8
                arcToRelative(
                    a = 0.9f,
                    b = 0.9f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.8f,
                )
                // a 0.9 0.9 0 0 1 0 1.8z
                arcToRelative(
                    a = 0.9f,
                    b = 0.9f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.8f,
                )
                close()
                // m 1.9 -0.7
                moveToRelative(dx = 1.9f, dy = -0.7f)
                // a 0.7 0.7 0 1 0 1.4 0
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 1.4f,
                    dy1 = 0.0f,
                )
                // a 0.7 0.7 0 0 0 -1.4 0z
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.4f,
                    dy1 = 0.0f,
                )
                close()
                // m 3 0.7
                moveToRelative(dx = 3.0f, dy = 0.7f)
                // a 0.6 0.6 0 1 1 0 -1.2
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.2f,
                )
                // a 0.6 0.6 0 0 1 0 1.2z
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.2f,
                )
                close()
                // m 2.1 0
                moveToRelative(dx = 2.1f, dy = 0.0f)
                // a 0.5 0.5 0 1 1 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // a 0.5 0.5 0 0 1 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
                // M 1 7.6
                moveTo(x = 1.0f, y = 7.6f)
                // a 1 1 0 1 1 0 -2
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -2.0f,
                )
                // a 1 1 0 0 1 0 2z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 2.0f,
                )
                close()
                // m 2 -0.9
                moveToRelative(dx = 2.0f, dy = -0.9f)
                // a 0.9 0.9 0 1 0 1.8 0
                arcToRelative(
                    a = 0.9f,
                    b = 0.9f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 1.8f,
                    dy1 = 0.0f,
                )
                // a 0.9 0.9 0 0 0 -1.8 0z
                arcToRelative(
                    a = 0.9f,
                    b = 0.9f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.8f,
                    dy1 = 0.0f,
                )
                close()
                // m 3.7 0.9
                moveToRelative(dx = 3.7f, dy = 0.9f)
                // a 0.9 0.9 0 1 1 0 -1.8
                arcToRelative(
                    a = 0.9f,
                    b = 0.9f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.8f,
                )
                // a 0.9 0.9 0 0 1 0 1.8z
                arcToRelative(
                    a = 0.9f,
                    b = 0.9f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.8f,
                )
                close()
                // m 1.9 -0.7
                moveToRelative(dx = 1.9f, dy = -0.7f)
                // a 0.7 0.7 0 1 0 1.4 0
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 1.4f,
                    dy1 = 0.0f,
                )
                // a 0.7 0.7 0 0 0 -1.4 0z
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.4f,
                    dy1 = 0.0f,
                )
                close()
                // m 3 0.7
                moveToRelative(dx = 3.0f, dy = 0.7f)
                // a 0.6 0.6 0 1 1 0 -1.2
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.2f,
                )
                // a 0.6 0.6 0 0 1 0 1.2z
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.2f,
                )
                close()
                // m 2.1 0
                moveToRelative(dx = 2.1f, dy = 0.0f)
                // a 0.5 0.5 0 1 1 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // a 0.5 0.5 0 0 1 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
            }
        }.build().also { _ic2107 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2107: ImageVector? = null
