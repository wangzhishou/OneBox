package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic9999: ImageVector
    get() {
        val current = _ic9999
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic9999",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.455 1.261 a.526 .526 0 0 0 -.91 0 L.07 14.224 c-.2 .346 .052 .776 .454 .776 h14.952 c.402 0 .654 -.43 .454 -.776 L8.455 1.26Z m-1.7 4.288 C6.69 4.985 7.267 4.5 8 4.5 c.733 0 1.31 .485 1.245 1.049 L8.682 10.5 H7.318 l-.563 -4.951Z M9.008 12.5 a1 1 0 1 1 -2 0 1 1 0 0 1 2 0Z
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
                // m -1.7 4.288
                moveToRelative(dx = -1.7f, dy = 4.288f)
                // C 6.69 4.985 7.267 4.5 8 4.5
                curveTo(
                    x1 = 6.69f,
                    y1 = 4.985f,
                    x2 = 7.267f,
                    y2 = 4.5f,
                    x3 = 8.0f,
                    y3 = 4.5f,
                )
                // c 0.733 0 1.31 0.485 1.245 1.049
                curveToRelative(
                    dx1 = 0.733f,
                    dy1 = 0.0f,
                    dx2 = 1.31f,
                    dy2 = 0.485f,
                    dx3 = 1.245f,
                    dy3 = 1.049f,
                )
                // L 8.682 10.5
                lineTo(x = 8.682f, y = 10.5f)
                // H 7.318
                horizontalLineTo(x = 7.318f)
                // l -0.563 -4.951z
                lineToRelative(dx = -0.563f, dy = -4.951f)
                close()
                // M 9.008 12.5
                moveTo(x = 9.008f, y = 12.5f)
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
            }
        }.build().also { _ic9999 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic9999: ImageVector? = null
