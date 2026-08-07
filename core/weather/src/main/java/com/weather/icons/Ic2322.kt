package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2322: ImageVector
    get() {
        val current = _ic2322
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2322",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // m5.138 4.941 -1.072 8.576 c-.083 .666 -1.03 .714 -1.18 .061 L1.755 8.675 .9 9.815 a.5 .5 0 1 1 -.8 -.6 l1.371 -1.828 a.6 .6 0 0 1 1.065 .225 l.817 3.544 1.08 -8.63 c.083 -.669 1.034 -.713 1.18 -.056 l1.503 6.762 1.295 -3.561 c.206 -.566 1.025 -.51 1.15 .08 l1.02 4.754 .883 -2.944 a.6 .6 0 0 1 1.055 -.188 l1.231 1.642 h1.75 a.5 .5 0 1 1 0 1 h-1.95 a.6 .6 0 0 1 -.48 -.24 l-.876 -1.168 -1.103 3.678 c-.178 .595 -1.031 .56 -1.161 -.046 L8.876 7.32 l-1.289 3.543 c-.205 .564 -1.02 .511 -1.15 -.074 L5.139 4.94Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5.138 4.941
                moveTo(x = 5.138f, y = 4.941f)
                // l -1.072 8.576
                lineToRelative(dx = -1.072f, dy = 8.576f)
                // c -0.083 0.666 -1.03 0.714 -1.18 0.061
                curveToRelative(
                    dx1 = -0.083f,
                    dy1 = 0.666f,
                    dx2 = -1.03f,
                    dy2 = 0.714f,
                    dx3 = -1.18f,
                    dy3 = 0.061f,
                )
                // L 1.755 8.675
                lineTo(x = 1.755f, y = 8.675f)
                // L 0.9 9.815
                lineTo(x = 0.9f, y = 9.815f)
                // a 0.5 0.5 0 1 1 -0.8 -0.6
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.8f,
                    dy1 = -0.6f,
                )
                // l 1.371 -1.828
                lineToRelative(dx = 1.371f, dy = -1.828f)
                // a 0.6 0.6 0 0 1 1.065 0.225
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.065f,
                    dy1 = 0.225f,
                )
                // l 0.817 3.544
                lineToRelative(dx = 0.817f, dy = 3.544f)
                // l 1.08 -8.63
                lineToRelative(dx = 1.08f, dy = -8.63f)
                // c 0.083 -0.669 1.034 -0.713 1.18 -0.056
                curveToRelative(
                    dx1 = 0.083f,
                    dy1 = -0.669f,
                    dx2 = 1.034f,
                    dy2 = -0.713f,
                    dx3 = 1.18f,
                    dy3 = -0.056f,
                )
                // l 1.503 6.762
                lineToRelative(dx = 1.503f, dy = 6.762f)
                // l 1.295 -3.561
                lineToRelative(dx = 1.295f, dy = -3.561f)
                // c 0.206 -0.566 1.025 -0.51 1.15 0.08
                curveToRelative(
                    dx1 = 0.206f,
                    dy1 = -0.566f,
                    dx2 = 1.025f,
                    dy2 = -0.51f,
                    dx3 = 1.15f,
                    dy3 = 0.08f,
                )
                // l 1.02 4.754
                lineToRelative(dx = 1.02f, dy = 4.754f)
                // l 0.883 -2.944
                lineToRelative(dx = 0.883f, dy = -2.944f)
                // a 0.6 0.6 0 0 1 1.055 -0.188
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.055f,
                    dy1 = -0.188f,
                )
                // l 1.231 1.642
                lineToRelative(dx = 1.231f, dy = 1.642f)
                // h 1.75
                horizontalLineToRelative(dx = 1.75f)
                // a 0.5 0.5 0 1 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // h -1.95
                horizontalLineToRelative(dx = -1.95f)
                // a 0.6 0.6 0 0 1 -0.48 -0.24
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.48f,
                    dy1 = -0.24f,
                )
                // l -0.876 -1.168
                lineToRelative(dx = -0.876f, dy = -1.168f)
                // l -1.103 3.678
                lineToRelative(dx = -1.103f, dy = 3.678f)
                // c -0.178 0.595 -1.031 0.56 -1.161 -0.046
                curveToRelative(
                    dx1 = -0.178f,
                    dy1 = 0.595f,
                    dx2 = -1.031f,
                    dy2 = 0.56f,
                    dx3 = -1.161f,
                    dy3 = -0.046f,
                )
                // L 8.876 7.32
                lineTo(x = 8.876f, y = 7.32f)
                // l -1.289 3.543
                lineToRelative(dx = -1.289f, dy = 3.543f)
                // c -0.205 0.564 -1.02 0.511 -1.15 -0.074
                curveToRelative(
                    dx1 = -0.205f,
                    dy1 = 0.564f,
                    dx2 = -1.02f,
                    dy2 = 0.511f,
                    dx3 = -1.15f,
                    dy3 = -0.074f,
                )
                // L 5.139 4.94z
                lineTo(x = 5.139f, y = 4.94f)
                close()
            }
        }.build().also { _ic2322 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2322: ImageVector? = null
