package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1006: ImageVector
    get() {
        val current = _ic1006
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1006",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M15.497 3.077 S7.525 .722 4.845 .023 a.794 .794 0 0 0 -.956 .568 L.024 15 a.81 .81 0 0 0 .544 .968 .811 .811 0 0 0 1 -.554 l1.671 -6.23 12.373 -4.817 a.696 .696 0 0 0 -.115 -1.291Z
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
            }
            // M11.756 7.71 a.296 .296 0 0 0 -.512 0 L7.04 15 a.292 .292 0 0 0 .256 .438 h8.41 a.292 .292 0 0 0 .256 -.437 L11.756 7.71Z m-.956 2.41 c-.036 -.317 .287 -.59 .7 -.59 .412 0 .736 .273 .7 .59 l-.316 2.785 h-.768 L10.8 10.12Z m1.267 3.91 a.563 .563 0 1 1 -1.125 0 .563 .563 0 0 1 1.125 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.756 7.71
                moveTo(x = 11.756f, y = 7.71f)
                // a 0.296 0.296 0 0 0 -0.512 0
                arcToRelative(
                    a = 0.296f,
                    b = 0.296f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.512f,
                    dy1 = 0.0f,
                )
                // L 7.04 15
                lineTo(x = 7.04f, y = 15.0f)
                // a 0.292 0.292 0 0 0 0.256 0.438
                arcToRelative(
                    a = 0.292f,
                    b = 0.292f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.256f,
                    dy1 = 0.438f,
                )
                // h 8.41
                horizontalLineToRelative(dx = 8.41f)
                // a 0.292 0.292 0 0 0 0.256 -0.437
                arcToRelative(
                    a = 0.292f,
                    b = 0.292f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.256f,
                    dy1 = -0.437f,
                )
                // L 11.756 7.71z
                lineTo(x = 11.756f, y = 7.71f)
                close()
                // m -0.956 2.41
                moveToRelative(dx = -0.956f, dy = 2.41f)
                // c -0.036 -0.317 0.287 -0.59 0.7 -0.59
                curveToRelative(
                    dx1 = -0.036f,
                    dy1 = -0.317f,
                    dx2 = 0.287f,
                    dy2 = -0.59f,
                    dx3 = 0.7f,
                    dy3 = -0.59f,
                )
                // c 0.412 0 0.736 0.273 0.7 0.59
                curveToRelative(
                    dx1 = 0.412f,
                    dy1 = 0.0f,
                    dx2 = 0.736f,
                    dy2 = 0.273f,
                    dx3 = 0.7f,
                    dy3 = 0.59f,
                )
                // l -0.316 2.785
                lineToRelative(dx = -0.316f, dy = 2.785f)
                // h -0.768
                horizontalLineToRelative(dx = -0.768f)
                // L 10.8 10.12z
                lineTo(x = 10.8f, y = 10.12f)
                close()
                // m 1.267 3.91
                moveToRelative(dx = 1.267f, dy = 3.91f)
                // a 0.563 0.563 0 1 1 -1.125 0
                arcToRelative(
                    a = 0.563f,
                    b = 0.563f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.125f,
                    dy1 = 0.0f,
                )
                // a 0.563 0.563 0 0 1 1.125 0z
                arcToRelative(
                    a = 0.563f,
                    b = 0.563f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.125f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic1006 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1006: ImageVector? = null
