package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1605: ImageVector
    get() {
        val current = _ic1605
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1605",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.455 1.261 a.526 .526 0 0 0 -.91 0 L.07 14.224 c-.2 .346 .052 .776 .454 .776 h14.952 c.402 0 .654 -.43 .454 -.776 L8.455 1.26Z m.698 7.331 c-.065 -.536 .22 -1.394 1.231 -1.692 -.048 .867 .155 1.351 .341 1.793 .143 .338 .274 .652 .275 1.094 0 1.713 -1.308 2.963 -2.965 2.963 C6.378 12.75 5 11.501 5 9.787 c0 -.71 .308 -1.583 .923 -1.91 0 0 .11 1.178 .692 1.67 .012 -.13 .007 -.31 0 -.524 -.03 -1.08 -.086 -3.02 1.845 -3.773 -.08 1.453 -.03 2.465 .693 3.342Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.455 1.261
                moveTo(x = 8.455f, y = 1.261f)
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
                // m 0.698 7.331
                moveToRelative(dx = 0.698f, dy = 7.331f)
                // c -0.065 -0.536 0.22 -1.394 1.231 -1.692
                curveToRelative(
                    dx1 = -0.065f,
                    dy1 = -0.536f,
                    dx2 = 0.22f,
                    dy2 = -1.394f,
                    dx3 = 1.231f,
                    dy3 = -1.692f,
                )
                // c -0.048 0.867 0.155 1.351 0.341 1.793
                curveToRelative(
                    dx1 = -0.048f,
                    dy1 = 0.867f,
                    dx2 = 0.155f,
                    dy2 = 1.351f,
                    dx3 = 0.341f,
                    dy3 = 1.793f,
                )
                // c 0.143 0.338 0.274 0.652 0.275 1.094
                curveToRelative(
                    dx1 = 0.143f,
                    dy1 = 0.338f,
                    dx2 = 0.274f,
                    dy2 = 0.652f,
                    dx3 = 0.275f,
                    dy3 = 1.094f,
                )
                // c 0 1.713 -1.308 2.963 -2.965 2.963
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.713f,
                    dx2 = -1.308f,
                    dy2 = 2.963f,
                    dx3 = -2.965f,
                    dy3 = 2.963f,
                )
                // C 6.378 12.75 5 11.501 5 9.787
                curveTo(
                    x1 = 6.378f,
                    y1 = 12.75f,
                    x2 = 5.0f,
                    y2 = 11.501f,
                    x3 = 5.0f,
                    y3 = 9.787f,
                )
                // c 0 -0.71 0.308 -1.583 0.923 -1.91
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.71f,
                    dx2 = 0.308f,
                    dy2 = -1.583f,
                    dx3 = 0.923f,
                    dy3 = -1.91f,
                )
                // c 0 0 0.11 1.178 0.692 1.67
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.0f,
                    dx2 = 0.11f,
                    dy2 = 1.178f,
                    dx3 = 0.692f,
                    dy3 = 1.67f,
                )
                // c 0.012 -0.13 0.007 -0.31 0 -0.524
                curveToRelative(
                    dx1 = 0.012f,
                    dy1 = -0.13f,
                    dx2 = 0.007f,
                    dy2 = -0.31f,
                    dx3 = 0.0f,
                    dy3 = -0.524f,
                )
                // c -0.03 -1.08 -0.086 -3.02 1.845 -3.773
                curveToRelative(
                    dx1 = -0.03f,
                    dy1 = -1.08f,
                    dx2 = -0.086f,
                    dy2 = -3.02f,
                    dx3 = 1.845f,
                    dy3 = -3.773f,
                )
                // c -0.08 1.453 -0.03 2.465 0.693 3.342z
                curveToRelative(
                    dx1 = -0.08f,
                    dy1 = 1.453f,
                    dx2 = -0.03f,
                    dy2 = 2.465f,
                    dx3 = 0.693f,
                    dy3 = 3.342f,
                )
                close()
            }
        }.build().also { _ic1605 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1605: ImageVector? = null
