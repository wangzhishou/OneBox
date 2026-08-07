package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1058: ImageVector
    get() {
        val current = _ic1058
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1058",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M15.497 3.077 S7.525 .722 4.845 .023 a.794 .794 0 0 0 -.956 .568 L.024 15 a.81 .81 0 0 0 .544 .968 .811 .811 0 0 0 1 -.554 l1.671 -6.23 12.373 -4.817 a.696 .696 0 0 0 -.115 -1.291Z m-2.562 7.525 c-.085 0 -.141 -.08 -.103 -.15 l1.267 -2.3 c.038 -.07 -.018 -.152 -.103 -.152 H11.47 a.234 .234 0 0 0 -.122 .031 .253 .253 0 0 0 -.092 .092 l-2.235 4.22 c-.067 .125 .035 .27 .189 .27 h2.272 c.08 0 .135 .07 .11 .139 l-1.182 3.11 c-.044 .11 .115 .188 .198 .1 l4.765 -5.189 c.062 -.067 .01 -.171 -.088 -.171 h-2.35Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 15.497 3.077
                moveTo(x = 15.497f, y = 3.077f)
                // S 7.525 0.722 4.845 0.023
                reflectiveCurveTo(
                    x1 = 7.525f,
                    y1 = 0.722f,
                    x2 = 4.845f,
                    y2 = 0.023f,
                )
                // a 0.794 0.794 0 0 0 -0.956 0.568
                arcToRelative(
                    a = 0.794f,
                    b = 0.794f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.956f,
                    dy1 = 0.568f,
                )
                // L 0.024 15
                lineTo(x = 0.024f, y = 15.0f)
                // a 0.81 0.81 0 0 0 0.544 0.968
                arcToRelative(
                    a = 0.81f,
                    b = 0.81f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.544f,
                    dy1 = 0.968f,
                )
                // a 0.811 0.811 0 0 0 1 -0.554
                arcToRelative(
                    a = 0.811f,
                    b = 0.811f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.0f,
                    dy1 = -0.554f,
                )
                // l 1.671 -6.23
                lineToRelative(dx = 1.671f, dy = -6.23f)
                // l 12.373 -4.817
                lineToRelative(dx = 12.373f, dy = -4.817f)
                // a 0.696 0.696 0 0 0 -0.115 -1.291z
                arcToRelative(
                    a = 0.696f,
                    b = 0.696f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.115f,
                    dy1 = -1.291f,
                )
                close()
                // m -2.562 7.525
                moveToRelative(dx = -2.562f, dy = 7.525f)
                // c -0.085 0 -0.141 -0.08 -0.103 -0.15
                curveToRelative(
                    dx1 = -0.085f,
                    dy1 = 0.0f,
                    dx2 = -0.141f,
                    dy2 = -0.08f,
                    dx3 = -0.103f,
                    dy3 = -0.15f,
                )
                // l 1.267 -2.3
                lineToRelative(dx = 1.267f, dy = -2.3f)
                // c 0.038 -0.07 -0.018 -0.152 -0.103 -0.152
                curveToRelative(
                    dx1 = 0.038f,
                    dy1 = -0.07f,
                    dx2 = -0.018f,
                    dy2 = -0.152f,
                    dx3 = -0.103f,
                    dy3 = -0.152f,
                )
                // H 11.47
                horizontalLineTo(x = 11.47f)
                // a 0.234 0.234 0 0 0 -0.122 0.031
                arcToRelative(
                    a = 0.234f,
                    b = 0.234f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.122f,
                    dy1 = 0.031f,
                )
                // a 0.253 0.253 0 0 0 -0.092 0.092
                arcToRelative(
                    a = 0.253f,
                    b = 0.253f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.092f,
                    dy1 = 0.092f,
                )
                // l -2.235 4.22
                lineToRelative(dx = -2.235f, dy = 4.22f)
                // c -0.067 0.125 0.035 0.27 0.189 0.27
                curveToRelative(
                    dx1 = -0.067f,
                    dy1 = 0.125f,
                    dx2 = 0.035f,
                    dy2 = 0.27f,
                    dx3 = 0.189f,
                    dy3 = 0.27f,
                )
                // h 2.272
                horizontalLineToRelative(dx = 2.272f)
                // c 0.08 0 0.135 0.07 0.11 0.139
                curveToRelative(
                    dx1 = 0.08f,
                    dy1 = 0.0f,
                    dx2 = 0.135f,
                    dy2 = 0.07f,
                    dx3 = 0.11f,
                    dy3 = 0.139f,
                )
                // l -1.182 3.11
                lineToRelative(dx = -1.182f, dy = 3.11f)
                // c -0.044 0.11 0.115 0.188 0.198 0.1
                curveToRelative(
                    dx1 = -0.044f,
                    dy1 = 0.11f,
                    dx2 = 0.115f,
                    dy2 = 0.188f,
                    dx3 = 0.198f,
                    dy3 = 0.1f,
                )
                // l 4.765 -5.189
                lineToRelative(dx = 4.765f, dy = -5.189f)
                // c 0.062 -0.067 0.01 -0.171 -0.088 -0.171
                curveToRelative(
                    dx1 = 0.062f,
                    dy1 = -0.067f,
                    dx2 = 0.01f,
                    dy2 = -0.171f,
                    dx3 = -0.088f,
                    dy3 = -0.171f,
                )
                // h -2.35z
                horizontalLineToRelative(dx = -2.35f)
                close()
            }
        }.build().also { _ic1058 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1058: ImageVector? = null
