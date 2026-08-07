package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2343: ImageVector
    get() {
        val current = _ic2343
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2343",
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
            // M8.382 8.102 c-.074 0 -.122 -.08 -.089 -.15 l1.089 -2.3 c.033 -.07 -.015 -.152 -.089 -.152 h-2.17 a.18 .18 0 0 0 -.105 .031 .239 .239 0 0 0 -.079 .092 l-1.921 4.22 c-.057 .125 .03 .27 .162 .27 h1.953 c.069 0 .116 .07 .094 .139 l-1.015 3.11 c-.038 .11 .099 .188 .17 .1 l4.095 -5.189 c.053 -.068 .008 -.171 -.076 -.171 H8.382Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.382 8.102
                moveTo(x = 8.382f, y = 8.102f)
                // c -0.074 0 -0.122 -0.08 -0.089 -0.15
                curveToRelative(
                    dx1 = -0.074f,
                    dy1 = 0.0f,
                    dx2 = -0.122f,
                    dy2 = -0.08f,
                    dx3 = -0.089f,
                    dy3 = -0.15f,
                )
                // l 1.089 -2.3
                lineToRelative(dx = 1.089f, dy = -2.3f)
                // c 0.033 -0.07 -0.015 -0.152 -0.089 -0.152
                curveToRelative(
                    dx1 = 0.033f,
                    dy1 = -0.07f,
                    dx2 = -0.015f,
                    dy2 = -0.152f,
                    dx3 = -0.089f,
                    dy3 = -0.152f,
                )
                // h -2.17
                horizontalLineToRelative(dx = -2.17f)
                // a 0.18 0.18 0 0 0 -0.105 0.031
                arcToRelative(
                    a = 0.18f,
                    b = 0.18f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.105f,
                    dy1 = 0.031f,
                )
                // a 0.239 0.239 0 0 0 -0.079 0.092
                arcToRelative(
                    a = 0.239f,
                    b = 0.239f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.079f,
                    dy1 = 0.092f,
                )
                // l -1.921 4.22
                lineToRelative(dx = -1.921f, dy = 4.22f)
                // c -0.057 0.125 0.03 0.27 0.162 0.27
                curveToRelative(
                    dx1 = -0.057f,
                    dy1 = 0.125f,
                    dx2 = 0.03f,
                    dy2 = 0.27f,
                    dx3 = 0.162f,
                    dy3 = 0.27f,
                )
                // h 1.953
                horizontalLineToRelative(dx = 1.953f)
                // c 0.069 0 0.116 0.07 0.094 0.139
                curveToRelative(
                    dx1 = 0.069f,
                    dy1 = 0.0f,
                    dx2 = 0.116f,
                    dy2 = 0.07f,
                    dx3 = 0.094f,
                    dy3 = 0.139f,
                )
                // l -1.015 3.11
                lineToRelative(dx = -1.015f, dy = 3.11f)
                // c -0.038 0.11 0.099 0.188 0.17 0.1
                curveToRelative(
                    dx1 = -0.038f,
                    dy1 = 0.11f,
                    dx2 = 0.099f,
                    dy2 = 0.188f,
                    dx3 = 0.17f,
                    dy3 = 0.1f,
                )
                // l 4.095 -5.189
                lineToRelative(dx = 4.095f, dy = -5.189f)
                // c 0.053 -0.068 0.008 -0.171 -0.076 -0.171
                curveToRelative(
                    dx1 = 0.053f,
                    dy1 = -0.068f,
                    dx2 = 0.008f,
                    dy2 = -0.171f,
                    dx3 = -0.076f,
                    dy3 = -0.171f,
                )
                // H 8.382z
                horizontalLineTo(x = 8.382f)
                close()
            }
        }.build().also { _ic2343 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2343: ImageVector? = null
