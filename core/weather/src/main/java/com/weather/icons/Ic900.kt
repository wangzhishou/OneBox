package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic900: ImageVector
    get() {
        val current = _ic900
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic900",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M4.9 2.5 c-.9 .3 -1.2 1.1 -1.1 1.6 -.7 -.8 -.7 -1.7 -.6 -3.1 -2.1 .8 -1.6 3.2 -1.7 4 C1 4.5 .9 3.5 .9 3.5 .3 3.8 0 4.6 0 5.3 0 6.9 1.3 8 2.8 8 c1.5 0 2.7 -1.2 2.7 -2.7 0 -1.1 -.6 -1.4 -.6 -2.8Z m6.6 .5 a.5 .5 0 0 0 -.5 .5 v6.063 a2 2 0 1 0 1 0 V3.5 a.5 .5 0 0 0 -.5 -.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.9 2.5
                moveTo(x = 4.9f, y = 2.5f)
                // c -0.9 0.3 -1.2 1.1 -1.1 1.6
                curveToRelative(
                    dx1 = -0.9f,
                    dy1 = 0.3f,
                    dx2 = -1.2f,
                    dy2 = 1.1f,
                    dx3 = -1.1f,
                    dy3 = 1.6f,
                )
                // c -0.7 -0.8 -0.7 -1.7 -0.6 -3.1
                curveToRelative(
                    dx1 = -0.7f,
                    dy1 = -0.8f,
                    dx2 = -0.7f,
                    dy2 = -1.7f,
                    dx3 = -0.6f,
                    dy3 = -3.1f,
                )
                // c -2.1 0.8 -1.6 3.2 -1.7 4
                curveToRelative(
                    dx1 = -2.1f,
                    dy1 = 0.8f,
                    dx2 = -1.6f,
                    dy2 = 3.2f,
                    dx3 = -1.7f,
                    dy3 = 4.0f,
                )
                // C 1 4.5 0.9 3.5 0.9 3.5
                curveTo(
                    x1 = 1.0f,
                    y1 = 4.5f,
                    x2 = 0.9f,
                    y2 = 3.5f,
                    x3 = 0.9f,
                    y3 = 3.5f,
                )
                // C 0.3 3.8 0 4.6 0 5.3
                curveTo(
                    x1 = 0.3f,
                    y1 = 3.8f,
                    x2 = 0.0f,
                    y2 = 4.6f,
                    x3 = 0.0f,
                    y3 = 5.3f,
                )
                // C 0 6.9 1.3 8 2.8 8
                curveTo(
                    x1 = 0.0f,
                    y1 = 6.9f,
                    x2 = 1.3f,
                    y2 = 8.0f,
                    x3 = 2.8f,
                    y3 = 8.0f,
                )
                // c 1.5 0 2.7 -1.2 2.7 -2.7
                curveToRelative(
                    dx1 = 1.5f,
                    dy1 = 0.0f,
                    dx2 = 2.7f,
                    dy2 = -1.2f,
                    dx3 = 2.7f,
                    dy3 = -2.7f,
                )
                // c 0 -1.1 -0.6 -1.4 -0.6 -2.8z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.1f,
                    dx2 = -0.6f,
                    dy2 = -1.4f,
                    dx3 = -0.6f,
                    dy3 = -2.8f,
                )
                close()
                // m 6.6 0.5
                moveToRelative(dx = 6.6f, dy = 0.5f)
                // a 0.5 0.5 0 0 0 -0.5 0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.5f,
                    dy1 = 0.5f,
                )
                // v 6.063
                verticalLineToRelative(dy = 6.063f)
                // a 2 2 0 1 0 1 0
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // V 3.5
                verticalLineTo(y = 3.5f)
                // a 0.5 0.5 0 0 0 -0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                close()
            }
            // m10.2 8.399 -.532 .356 a3.3 3.3 0 1 0 3.665 0 l-.533 -.356 V2.5 a1.3 1.3 0 1 0 -2.6 0 v5.899Z M9 2.5 a2.5 2.5 0 0 1 5 0 v5.258 a4.5 4.5 0 1 1 -5 0 V2.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.2 8.399
                moveTo(x = 10.2f, y = 8.399f)
                // l -0.532 0.356
                lineToRelative(dx = -0.532f, dy = 0.356f)
                // a 3.3 3.3 0 1 0 3.665 0
                arcToRelative(
                    a = 3.3f,
                    b = 3.3f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 3.665f,
                    dy1 = 0.0f,
                )
                // l -0.533 -0.356
                lineToRelative(dx = -0.533f, dy = -0.356f)
                // V 2.5
                verticalLineTo(y = 2.5f)
                // a 1.3 1.3 0 1 0 -2.6 0
                arcToRelative(
                    a = 1.3f,
                    b = 1.3f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -2.6f,
                    dy1 = 0.0f,
                )
                // v 5.899z
                verticalLineToRelative(dy = 5.899f)
                close()
                // M 9 2.5
                moveTo(x = 9.0f, y = 2.5f)
                // a 2.5 2.5 0 0 1 5 0
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 5.0f,
                    dy1 = 0.0f,
                )
                // v 5.258
                verticalLineToRelative(dy = 5.258f)
                // a 4.5 4.5 0 1 1 -5 0
                arcToRelative(
                    a = 4.5f,
                    b = 4.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -5.0f,
                    dy1 = 0.0f,
                )
                // V 2.5z
                verticalLineTo(y = 2.5f)
                close()
            }
        }.build().also { _ic900 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic900: ImageVector? = null
