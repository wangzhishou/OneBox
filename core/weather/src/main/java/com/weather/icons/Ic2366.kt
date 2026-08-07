package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2366: ImageVector
    get() {
        val current = _ic2366
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2366",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.502 6.62 c-.026 -.226 .205 -.42 .498 -.42 .293 0 .524 .194 .498 .42 L8.273 8.6 h-.546 l-.225 -1.98Z m.901 2.78 a.4 .4 0 1 1 -.8 0 .4 .4 0 0 1 .8 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.502 6.62
                moveTo(x = 7.502f, y = 6.62f)
                // c -0.026 -0.226 0.205 -0.42 0.498 -0.42
                curveToRelative(
                    dx1 = -0.026f,
                    dy1 = -0.226f,
                    dx2 = 0.205f,
                    dy2 = -0.42f,
                    dx3 = 0.498f,
                    dy3 = -0.42f,
                )
                // c 0.293 0 0.524 0.194 0.498 0.42
                curveToRelative(
                    dx1 = 0.293f,
                    dy1 = 0.0f,
                    dx2 = 0.524f,
                    dy2 = 0.194f,
                    dx3 = 0.498f,
                    dy3 = 0.42f,
                )
                // L 8.273 8.6
                lineTo(x = 8.273f, y = 8.6f)
                // h -0.546
                horizontalLineToRelative(dx = -0.546f)
                // l -0.225 -1.98z
                lineToRelative(dx = -0.225f, dy = -1.98f)
                close()
                // m 0.901 2.78
                moveToRelative(dx = 0.901f, dy = 2.78f)
                // a 0.4 0.4 0 1 1 -0.8 0
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.8f,
                    dy1 = 0.0f,
                )
                // a 0.4 0.4 0 0 1 0.8 0z
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.8f,
                    dy1 = 0.0f,
                )
                close()
            }
            // M12.086 0 C8.174 .5 5.28 1.667 3.408 3.5 a6.261 6.261 0 0 0 0 9 6.493 6.493 0 0 0 2.52 1.533 L3.92 16 c3.913 -.5 6.807 -1.667 8.679 -3.5 a6.274 6.274 0 0 0 0 -9 6.495 6.495 0 0 0 -2.52 -1.533 L12.085 0Z M11 8 a3 3 0 1 1 -6 0 3 3 0 0 1 6 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 12.086 0
                moveTo(x = 12.086f, y = 0.0f)
                // C 8.174 0.5 5.28 1.667 3.408 3.5
                curveTo(
                    x1 = 8.174f,
                    y1 = 0.5f,
                    x2 = 5.28f,
                    y2 = 1.667f,
                    x3 = 3.408f,
                    y3 = 3.5f,
                )
                // a 6.261 6.261 0 0 0 0 9
                arcToRelative(
                    a = 6.261f,
                    b = 6.261f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 9.0f,
                )
                // a 6.493 6.493 0 0 0 2.52 1.533
                arcToRelative(
                    a = 6.493f,
                    b = 6.493f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.52f,
                    dy1 = 1.533f,
                )
                // L 3.92 16
                lineTo(x = 3.92f, y = 16.0f)
                // c 3.913 -0.5 6.807 -1.667 8.679 -3.5
                curveToRelative(
                    dx1 = 3.913f,
                    dy1 = -0.5f,
                    dx2 = 6.807f,
                    dy2 = -1.667f,
                    dx3 = 8.679f,
                    dy3 = -3.5f,
                )
                // a 6.274 6.274 0 0 0 0 -9
                arcToRelative(
                    a = 6.274f,
                    b = 6.274f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -9.0f,
                )
                // a 6.495 6.495 0 0 0 -2.52 -1.533
                arcToRelative(
                    a = 6.495f,
                    b = 6.495f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.52f,
                    dy1 = -1.533f,
                )
                // L 12.085 0z
                lineTo(x = 12.085f, y = 0.0f)
                close()
                // M 11 8
                moveTo(x = 11.0f, y = 8.0f)
                // a 3 3 0 1 1 -6 0
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -6.0f,
                    dy1 = 0.0f,
                )
                // a 3 3 0 0 1 6 0z
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 6.0f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic2366 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2366: ImageVector? = null
