package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic150: ImageVector
    get() {
        val current = _ic150
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic150",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M4.403 1.393 c-.448 2.58 -.261 5.558 1.873 7.797 2.09 2.192 5.477 3.06 8.284 2.851 -.255 .333 -.543 .65 -.863 .946 -3.035 2.808 -7.81 2.66 -10.66 -.33 a7.323 7.323 0 0 1 .334 -10.463 7.57 7.57 0 0 1 1.032 -.801Z M5.544 .79 c.114 -.494 -.351 -.958 -.811 -.732 a8.538 8.538 0 0 0 -2.04 1.401 8.323 8.323 0 0 0 -.38 11.887 c3.227 3.386 8.628 3.553 12.064 .374 a8.432 8.432 0 0 0 1.547 -1.92 c.258 -.438 -.183 -.924 -.69 -.843 -2.705 .43 -6.217 -.342 -8.234 -2.458 C4.983 6.384 4.939 3.424 5.544 .79Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.403 1.393
                moveTo(x = 4.403f, y = 1.393f)
                // c -0.448 2.58 -0.261 5.558 1.873 7.797
                curveToRelative(
                    dx1 = -0.448f,
                    dy1 = 2.58f,
                    dx2 = -0.261f,
                    dy2 = 5.558f,
                    dx3 = 1.873f,
                    dy3 = 7.797f,
                )
                // c 2.09 2.192 5.477 3.06 8.284 2.851
                curveToRelative(
                    dx1 = 2.09f,
                    dy1 = 2.192f,
                    dx2 = 5.477f,
                    dy2 = 3.06f,
                    dx3 = 8.284f,
                    dy3 = 2.851f,
                )
                // c -0.255 0.333 -0.543 0.65 -0.863 0.946
                curveToRelative(
                    dx1 = -0.255f,
                    dy1 = 0.333f,
                    dx2 = -0.543f,
                    dy2 = 0.65f,
                    dx3 = -0.863f,
                    dy3 = 0.946f,
                )
                // c -3.035 2.808 -7.81 2.66 -10.66 -0.33
                curveToRelative(
                    dx1 = -3.035f,
                    dy1 = 2.808f,
                    dx2 = -7.81f,
                    dy2 = 2.66f,
                    dx3 = -10.66f,
                    dy3 = -0.33f,
                )
                // a 7.323 7.323 0 0 1 0.334 -10.463
                arcToRelative(
                    a = 7.323f,
                    b = 7.323f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.334f,
                    dy1 = -10.463f,
                )
                // a 7.57 7.57 0 0 1 1.032 -0.801z
                arcToRelative(
                    a = 7.57f,
                    b = 7.57f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.032f,
                    dy1 = -0.801f,
                )
                close()
                // M 5.544 0.79
                moveTo(x = 5.544f, y = 0.79f)
                // c 0.114 -0.494 -0.351 -0.958 -0.811 -0.732
                curveToRelative(
                    dx1 = 0.114f,
                    dy1 = -0.494f,
                    dx2 = -0.351f,
                    dy2 = -0.958f,
                    dx3 = -0.811f,
                    dy3 = -0.732f,
                )
                // a 8.538 8.538 0 0 0 -2.04 1.401
                arcToRelative(
                    a = 8.538f,
                    b = 8.538f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.04f,
                    dy1 = 1.401f,
                )
                // a 8.323 8.323 0 0 0 -0.38 11.887
                arcToRelative(
                    a = 8.323f,
                    b = 8.323f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.38f,
                    dy1 = 11.887f,
                )
                // c 3.227 3.386 8.628 3.553 12.064 0.374
                curveToRelative(
                    dx1 = 3.227f,
                    dy1 = 3.386f,
                    dx2 = 8.628f,
                    dy2 = 3.553f,
                    dx3 = 12.064f,
                    dy3 = 0.374f,
                )
                // a 8.432 8.432 0 0 0 1.547 -1.92
                arcToRelative(
                    a = 8.432f,
                    b = 8.432f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.547f,
                    dy1 = -1.92f,
                )
                // c 0.258 -0.438 -0.183 -0.924 -0.69 -0.843
                curveToRelative(
                    dx1 = 0.258f,
                    dy1 = -0.438f,
                    dx2 = -0.183f,
                    dy2 = -0.924f,
                    dx3 = -0.69f,
                    dy3 = -0.843f,
                )
                // c -2.705 0.43 -6.217 -0.342 -8.234 -2.458
                curveToRelative(
                    dx1 = -2.705f,
                    dy1 = 0.43f,
                    dx2 = -6.217f,
                    dy2 = -0.342f,
                    dx3 = -8.234f,
                    dy3 = -2.458f,
                )
                // C 4.983 6.384 4.939 3.424 5.544 0.79z
                curveTo(
                    x1 = 4.983f,
                    y1 = 6.384f,
                    x2 = 4.939f,
                    y2 = 3.424f,
                    x3 = 5.544f,
                    y3 = 0.79f,
                )
                close()
            }
        }.build().also { _ic150 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic150: ImageVector? = null
