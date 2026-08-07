package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2349: ImageVector
    get() {
        val current = _ic2349
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2349",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8 2.875 14.3 13.8 H1.7 L8 2.875Z m.455 -1.614 a.526 .526 0 0 0 -.91 0 L.07 14.224 c-.2 .346 .052 .776 .454 .776 h14.952 c.402 0 .654 -.43 .454 -.776 L8.455 1.26Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8 2.875
                moveTo(x = 8.0f, y = 2.875f)
                // L 14.3 13.8
                lineTo(x = 14.3f, y = 13.8f)
                // H 1.7
                horizontalLineTo(x = 1.7f)
                // L 8 2.875z
                lineTo(x = 8.0f, y = 2.875f)
                close()
                // m 0.455 -1.614
                moveToRelative(dx = 0.455f, dy = -1.614f)
                // a 0.526 0.526 0 0 0 -0.91 0
                arcToRelative(
                    a = 0.526f,
                    b = 0.526f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.91f,
                    dy1 = 0.0f,
                )
                // L 0.07 14.224
                lineTo(x = 0.07f, y = 14.224f)
                // c -0.2 0.346 0.052 0.776 0.454 0.776
                curveToRelative(
                    dx1 = -0.2f,
                    dy1 = 0.346f,
                    dx2 = 0.052f,
                    dy2 = 0.776f,
                    dx3 = 0.454f,
                    dy3 = 0.776f,
                )
                // h 14.952
                horizontalLineToRelative(dx = 14.952f)
                // c 0.402 0 0.654 -0.43 0.454 -0.776
                curveToRelative(
                    dx1 = 0.402f,
                    dy1 = 0.0f,
                    dx2 = 0.654f,
                    dy2 = -0.43f,
                    dx3 = 0.454f,
                    dy3 = -0.776f,
                )
                // L 8.455 1.26z
                lineTo(x = 8.455f, y = 1.26f)
                close()
            }
            // M9.955 7.84 c-.819 .267 -1.091 .981 -1 1.428 -.637 -.714 -.637 -1.518 -.546 -2.768 -1.909 .714 -1.454 2.857 -1.545 3.571 -.455 -.446 -.546 -1.339 -.546 -1.339 C5.773 9 5.5 9.714 5.5 10.34 c0 1.429 1.182 2.411 2.545 2.411 1.364 0 2.455 -1.071 2.455 -2.41 0 -.983 -.545 -1.25 -.545 -2.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 9.955 7.84
                moveTo(x = 9.955f, y = 7.84f)
                // c -0.819 0.267 -1.091 0.981 -1 1.428
                curveToRelative(
                    dx1 = -0.819f,
                    dy1 = 0.267f,
                    dx2 = -1.091f,
                    dy2 = 0.981f,
                    dx3 = -1.0f,
                    dy3 = 1.428f,
                )
                // c -0.637 -0.714 -0.637 -1.518 -0.546 -2.768
                curveToRelative(
                    dx1 = -0.637f,
                    dy1 = -0.714f,
                    dx2 = -0.637f,
                    dy2 = -1.518f,
                    dx3 = -0.546f,
                    dy3 = -2.768f,
                )
                // c -1.909 0.714 -1.454 2.857 -1.545 3.571
                curveToRelative(
                    dx1 = -1.909f,
                    dy1 = 0.714f,
                    dx2 = -1.454f,
                    dy2 = 2.857f,
                    dx3 = -1.545f,
                    dy3 = 3.571f,
                )
                // c -0.455 -0.446 -0.546 -1.339 -0.546 -1.339
                curveToRelative(
                    dx1 = -0.455f,
                    dy1 = -0.446f,
                    dx2 = -0.546f,
                    dy2 = -1.339f,
                    dx3 = -0.546f,
                    dy3 = -1.339f,
                )
                // C 5.773 9 5.5 9.714 5.5 10.34
                curveTo(
                    x1 = 5.773f,
                    y1 = 9.0f,
                    x2 = 5.5f,
                    y2 = 9.714f,
                    x3 = 5.5f,
                    y3 = 10.34f,
                )
                // c 0 1.429 1.182 2.411 2.545 2.411
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.429f,
                    dx2 = 1.182f,
                    dy2 = 2.411f,
                    dx3 = 2.545f,
                    dy3 = 2.411f,
                )
                // c 1.364 0 2.455 -1.071 2.455 -2.41
                curveToRelative(
                    dx1 = 1.364f,
                    dy1 = 0.0f,
                    dx2 = 2.455f,
                    dy2 = -1.071f,
                    dx3 = 2.455f,
                    dy3 = -2.41f,
                )
                // c 0 -0.983 -0.545 -1.25 -0.545 -2.5z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.983f,
                    dx2 = -0.545f,
                    dy2 = -1.25f,
                    dx3 = -0.545f,
                    dy3 = -2.5f,
                )
                close()
            }
        }.build().also { _ic2349 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2349: ImageVector? = null
