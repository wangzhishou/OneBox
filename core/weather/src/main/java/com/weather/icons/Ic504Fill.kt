package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic504Fill: ImageVector
    get() {
        val current = _ic504Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic504Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7 1.5 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z m-4 7 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z M7.5 8 a.5 .5 0 1 0 0 -1 .5 .5 0 0 0 0 1Z m0 6.75 a.75 .75 0 1 1 -1.5 0 .75 .75 0 0 1 1.5 0Z m6.25 -1.25 a.75 .75 0 1 0 0 -1.5 .75 .75 0 0 0 0 1.5Z M10 4 a1 1 0 1 1 -2 0 1 1 0 0 1 2 0Z M5 7 a1 1 0 1 0 0 -2 1 1 0 0 0 0 2Z m6.5 7.25 a1.25 1.25 0 1 1 -2.5 0 1.25 1.25 0 0 1 2.5 0Z M2 4 a1 1 0 1 0 0 -2 1 1 0 0 0 0 2Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7 1.5
                moveTo(x = 7.0f, y = 1.5f)
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
                // m -4 7
                moveToRelative(dx = -4.0f, dy = 7.0f)
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
                // M 7.5 8
                moveTo(x = 7.5f, y = 8.0f)
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
                // m 0 6.75
                moveToRelative(dx = 0.0f, dy = 6.75f)
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
                // m 6.25 -1.25
                moveToRelative(dx = 6.25f, dy = -1.25f)
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
                // M 10 4
                moveTo(x = 10.0f, y = 4.0f)
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
                // M 5 7
                moveTo(x = 5.0f, y = 7.0f)
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
                // m 6.5 7.25
                moveToRelative(dx = 6.5f, dy = 7.25f)
                // a 1.25 1.25 0 1 1 -2.5 0
                arcToRelative(
                    a = 1.25f,
                    b = 1.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -2.5f,
                    dy1 = 0.0f,
                )
                // a 1.25 1.25 0 0 1 2.5 0z
                arcToRelative(
                    a = 1.25f,
                    b = 1.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.5f,
                    dy1 = 0.0f,
                )
                close()
                // M 2 4
                moveTo(x = 2.0f, y = 4.0f)
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
            }
            // M9.97 1 c1.904 1.043 3.099 2.538 3.099 4.201 0 3.148 -4.275 5.7 -9.55 5.7 a15.332 15.332 0 0 1 -3.519 -.4 c2.06 1.036 4.25 1.544 6.451 1.496 2.075 .037 4.14 -.408 6.1 -1.315 a5.19 5.19 0 0 0 1.43 -.882 C15.224 8.782 16 7.618 16 6.298 16 3.894 13.502 1.836 9.97 1Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 9.97 1
                moveTo(x = 9.97f, y = 1.0f)
                // c 1.904 1.043 3.099 2.538 3.099 4.201
                curveToRelative(
                    dx1 = 1.904f,
                    dy1 = 1.043f,
                    dx2 = 3.099f,
                    dy2 = 2.538f,
                    dx3 = 3.099f,
                    dy3 = 4.201f,
                )
                // c 0 3.148 -4.275 5.7 -9.55 5.7
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 3.148f,
                    dx2 = -4.275f,
                    dy2 = 5.7f,
                    dx3 = -9.55f,
                    dy3 = 5.7f,
                )
                // a 15.332 15.332 0 0 1 -3.519 -0.4
                arcToRelative(
                    a = 15.332f,
                    b = 15.332f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.519f,
                    dy1 = -0.4f,
                )
                // c 2.06 1.036 4.25 1.544 6.451 1.496
                curveToRelative(
                    dx1 = 2.06f,
                    dy1 = 1.036f,
                    dx2 = 4.25f,
                    dy2 = 1.544f,
                    dx3 = 6.451f,
                    dy3 = 1.496f,
                )
                // c 2.075 0.037 4.14 -0.408 6.1 -1.315
                curveToRelative(
                    dx1 = 2.075f,
                    dy1 = 0.037f,
                    dx2 = 4.14f,
                    dy2 = -0.408f,
                    dx3 = 6.1f,
                    dy3 = -1.315f,
                )
                // a 5.19 5.19 0 0 0 1.43 -0.882
                arcToRelative(
                    a = 5.19f,
                    b = 5.19f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.43f,
                    dy1 = -0.882f,
                )
                // C 15.224 8.782 16 7.618 16 6.298
                curveTo(
                    x1 = 15.224f,
                    y1 = 8.782f,
                    x2 = 16.0f,
                    y2 = 7.618f,
                    x3 = 16.0f,
                    y3 = 6.298f,
                )
                // C 16 3.894 13.502 1.836 9.97 1z
                curveTo(
                    x1 = 16.0f,
                    y1 = 3.894f,
                    x2 = 13.502f,
                    y2 = 1.836f,
                    x3 = 9.97f,
                    y3 = 1.0f,
                )
                close()
            }
        }.build().also { _ic504Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic504Fill: ImageVector? = null
