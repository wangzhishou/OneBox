package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2330: ImageVector
    get() {
        val current = _ic2330
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2330",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M3.408 3.5 C5.28 1.667 8.174 .5 12.086 0 l-2.009 1.967 a6.495 6.495 0 0 1 2.52 1.533 6.274 6.274 0 0 1 0 9 c-1.871 1.833 -4.765 3 -8.678 3.5 l2.01 -1.967 A6.493 6.493 0 0 1 3.408 12.5 a6.261 6.261 0 0 1 0 -9Z M8.17 5.098 a.197 .197 0 0 0 -.34 0 l-2.804 4.86 c-.075 .13 .02 .292 .17 .292 h5.607 a.194 .194 0 0 0 .17 -.291 L8.17 5.098Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 3.408 3.5
                moveTo(x = 3.408f, y = 3.5f)
                // C 5.28 1.667 8.174 0.5 12.086 0
                curveTo(
                    x1 = 5.28f,
                    y1 = 1.667f,
                    x2 = 8.174f,
                    y2 = 0.5f,
                    x3 = 12.086f,
                    y3 = 0.0f,
                )
                // l -2.009 1.967
                lineToRelative(dx = -2.009f, dy = 1.967f)
                // a 6.495 6.495 0 0 1 2.52 1.533
                arcToRelative(
                    a = 6.495f,
                    b = 6.495f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.52f,
                    dy1 = 1.533f,
                )
                // a 6.274 6.274 0 0 1 0 9
                arcToRelative(
                    a = 6.274f,
                    b = 6.274f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 9.0f,
                )
                // c -1.871 1.833 -4.765 3 -8.678 3.5
                curveToRelative(
                    dx1 = -1.871f,
                    dy1 = 1.833f,
                    dx2 = -4.765f,
                    dy2 = 3.0f,
                    dx3 = -8.678f,
                    dy3 = 3.5f,
                )
                // l 2.01 -1.967
                lineToRelative(dx = 2.01f, dy = -1.967f)
                // A 6.493 6.493 0 0 1 3.408 12.5
                arcTo(
                    horizontalEllipseRadius = 6.493f,
                    verticalEllipseRadius = 6.493f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 3.408f,
                    y1 = 12.5f,
                )
                // a 6.261 6.261 0 0 1 0 -9z
                arcToRelative(
                    a = 6.261f,
                    b = 6.261f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -9.0f,
                )
                close()
                // M 8.17 5.098
                moveTo(x = 8.17f, y = 5.098f)
                // a 0.197 0.197 0 0 0 -0.34 0
                arcToRelative(
                    a = 0.197f,
                    b = 0.197f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.34f,
                    dy1 = 0.0f,
                )
                // l -2.804 4.86
                lineToRelative(dx = -2.804f, dy = 4.86f)
                // c -0.075 0.13 0.02 0.292 0.17 0.292
                curveToRelative(
                    dx1 = -0.075f,
                    dy1 = 0.13f,
                    dx2 = 0.02f,
                    dy2 = 0.292f,
                    dx3 = 0.17f,
                    dy3 = 0.292f,
                )
                // h 5.607
                horizontalLineToRelative(dx = 5.607f)
                // a 0.194 0.194 0 0 0 0.17 -0.291
                arcToRelative(
                    a = 0.194f,
                    b = 0.194f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.17f,
                    dy1 = -0.291f,
                )
                // L 8.17 5.098z
                lineTo(x = 8.17f, y = 5.098f)
                close()
            }
            // M8 6.2 c-.293 0 -.524 .194 -.498 .42 l.225 1.98 h.546 l.225 -1.98 c.026 -.226 -.205 -.42 -.498 -.42Z m.003 3.6 a.4 .4 0 1 0 0 -.8 .4 .4 0 0 0 0 .8Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8 6.2
                moveTo(x = 8.0f, y = 6.2f)
                // c -0.293 0 -0.524 0.194 -0.498 0.42
                curveToRelative(
                    dx1 = -0.293f,
                    dy1 = 0.0f,
                    dx2 = -0.524f,
                    dy2 = 0.194f,
                    dx3 = -0.498f,
                    dy3 = 0.42f,
                )
                // l 0.225 1.98
                lineToRelative(dx = 0.225f, dy = 1.98f)
                // h 0.546
                horizontalLineToRelative(dx = 0.546f)
                // l 0.225 -1.98
                lineToRelative(dx = 0.225f, dy = -1.98f)
                // c 0.026 -0.226 -0.205 -0.42 -0.498 -0.42z
                curveToRelative(
                    dx1 = 0.026f,
                    dy1 = -0.226f,
                    dx2 = -0.205f,
                    dy2 = -0.42f,
                    dx3 = -0.498f,
                    dy3 = -0.42f,
                )
                close()
                // m 0.003 3.6
                moveToRelative(dx = 0.003f, dy = 3.6f)
                // a 0.4 0.4 0 1 0 0 -0.8
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.8f,
                )
                // a 0.4 0.4 0 0 0 0 0.8z
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.8f,
                )
                close()
            }
        }.build().also { _ic2330 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2330: ImageVector? = null
