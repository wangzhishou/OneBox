package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic100Fill: ImageVector
    get() {
        val current = _ic100Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic100Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.005 3.5 a4.5 4.5 0 1 0 0 9 4.5 4.5 0 0 0 0 -9Z m.004 -.997 a.5 .5 0 0 1 -.5 -.5 v-1.5 a.5 .5 0 0 1 1 0 v1.5 a.5 .5 0 0 1 -.5 .5Z M3.766 4.255 a.498 .498 0 0 1 -.353 -.147 l-1.062 -1.06 a.5 .5 0 0 1 .707 -.707 L4.122 3.4 a.5 .5 0 0 1 -.355 .854 v.001Z M2.004 8.493 h-1.5 a.5 .5 0 1 1 0 -1 h1.5 a.5 .5 0 1 1 0 1Z m.691 5.303 a.5 .5 0 0 1 -.354 -.854 l1.062 -1.06 a.5 .5 0 0 1 .708 .707 l-1.063 1.06 a.497 .497 0 0 1 -.353 .147Z m5.301 2.201 a.5 .5 0 0 1 -.5 -.5 v-1.5 a.5 .5 0 0 1 1 0 v1.5 a.5 .5 0 0 1 -.5 .5Z m5.304 -2.191 a.496 .496 0 0 1 -.353 -.147 l-1.06 -1.06 a.5 .5 0 1 1 .706 -.707 l1.06 1.06 a.5 .5 0 0 1 -.353 .854Z m2.203 -5.299 h-1.5 a.5 .5 0 0 1 0 -1 h1.5 a.5 .5 0 1 1 0 1Z M12.25 4.265 a.5 .5 0 0 1 -.354 -.854 l1.06 -1.06 a.5 .5 0 1 1 .708 .707 l-1.06 1.06 a.498 .498 0 0 1 -.354 .147Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.005 3.5
                moveTo(x = 8.005f, y = 3.5f)
                // a 4.5 4.5 0 1 0 0 9
                arcToRelative(
                    a = 4.5f,
                    b = 4.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 9.0f,
                )
                // a 4.5 4.5 0 0 0 0 -9z
                arcToRelative(
                    a = 4.5f,
                    b = 4.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -9.0f,
                )
                close()
                // m 0.004 -0.997
                moveToRelative(dx = 0.004f, dy = -0.997f)
                // a 0.5 0.5 0 0 1 -0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                // v -1.5
                verticalLineToRelative(dy = -1.5f)
                // a 0.5 0.5 0 0 1 1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // v 1.5
                verticalLineToRelative(dy = 1.5f)
                // a 0.5 0.5 0 0 1 -0.5 0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = 0.5f,
                )
                close()
                // M 3.766 4.255
                moveTo(x = 3.766f, y = 4.255f)
                // a 0.498 0.498 0 0 1 -0.353 -0.147
                arcToRelative(
                    a = 0.498f,
                    b = 0.498f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.353f,
                    dy1 = -0.147f,
                )
                // l -1.062 -1.06
                lineToRelative(dx = -1.062f, dy = -1.06f)
                // a 0.5 0.5 0 0 1 0.707 -0.707
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.707f,
                    dy1 = -0.707f,
                )
                // L 4.122 3.4
                lineTo(x = 4.122f, y = 3.4f)
                // a 0.5 0.5 0 0 1 -0.355 0.854
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.355f,
                    dy1 = 0.854f,
                )
                // v 0.001z
                verticalLineToRelative(dy = 0.001f)
                close()
                // M 2.004 8.493
                moveTo(x = 2.004f, y = 8.493f)
                // h -1.5
                horizontalLineToRelative(dx = -1.5f)
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
                // h 1.5
                horizontalLineToRelative(dx = 1.5f)
                // a 0.5 0.5 0 1 1 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
                // m 0.691 5.303
                moveToRelative(dx = 0.691f, dy = 5.303f)
                // a 0.5 0.5 0 0 1 -0.354 -0.854
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.354f,
                    dy1 = -0.854f,
                )
                // l 1.062 -1.06
                lineToRelative(dx = 1.062f, dy = -1.06f)
                // a 0.5 0.5 0 0 1 0.708 0.707
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.708f,
                    dy1 = 0.707f,
                )
                // l -1.063 1.06
                lineToRelative(dx = -1.063f, dy = 1.06f)
                // a 0.497 0.497 0 0 1 -0.353 0.147z
                arcToRelative(
                    a = 0.497f,
                    b = 0.497f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.353f,
                    dy1 = 0.147f,
                )
                close()
                // m 5.301 2.201
                moveToRelative(dx = 5.301f, dy = 2.201f)
                // a 0.5 0.5 0 0 1 -0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                // v -1.5
                verticalLineToRelative(dy = -1.5f)
                // a 0.5 0.5 0 0 1 1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // v 1.5
                verticalLineToRelative(dy = 1.5f)
                // a 0.5 0.5 0 0 1 -0.5 0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = 0.5f,
                )
                close()
                // m 5.304 -2.191
                moveToRelative(dx = 5.304f, dy = -2.191f)
                // a 0.496 0.496 0 0 1 -0.353 -0.147
                arcToRelative(
                    a = 0.496f,
                    b = 0.496f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.353f,
                    dy1 = -0.147f,
                )
                // l -1.06 -1.06
                lineToRelative(dx = -1.06f, dy = -1.06f)
                // a 0.5 0.5 0 1 1 0.706 -0.707
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.706f,
                    dy1 = -0.707f,
                )
                // l 1.06 1.06
                lineToRelative(dx = 1.06f, dy = 1.06f)
                // a 0.5 0.5 0 0 1 -0.353 0.854z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.353f,
                    dy1 = 0.854f,
                )
                close()
                // m 2.203 -5.299
                moveToRelative(dx = 2.203f, dy = -5.299f)
                // h -1.5
                horizontalLineToRelative(dx = -1.5f)
                // a 0.5 0.5 0 0 1 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h 1.5
                horizontalLineToRelative(dx = 1.5f)
                // a 0.5 0.5 0 1 1 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
                // M 12.25 4.265
                moveTo(x = 12.25f, y = 4.265f)
                // a 0.5 0.5 0 0 1 -0.354 -0.854
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.354f,
                    dy1 = -0.854f,
                )
                // l 1.06 -1.06
                lineToRelative(dx = 1.06f, dy = -1.06f)
                // a 0.5 0.5 0 1 1 0.708 0.707
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.708f,
                    dy1 = 0.707f,
                )
                // l -1.06 1.06
                lineToRelative(dx = -1.06f, dy = 1.06f)
                // a 0.498 0.498 0 0 1 -0.354 0.147z
                arcToRelative(
                    a = 0.498f,
                    b = 0.498f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.354f,
                    dy1 = 0.147f,
                )
                close()
            }
        }.build().also { _ic100Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic100Fill: ImageVector? = null
