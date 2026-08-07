package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1805: ImageVector
    get() {
        val current = _ic1805
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1805",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M9.558 0 h5.403 c.264 0 .422 .311 .279 .548 l-2.975 3.85 a.173 .173 0 0 0 .05 .232 .147 .147 0 0 0 .08 .024 h3.13 c.416 0 .63 .53 .345 .855 L6.62 16 l2.203 -8.082 a.177 .177 0 0 0 -.025 -.146 .159 .159 0 0 0 -.055 -.048 .148 .148 0 0 0 -.07 -.018 H5.476 a.451 .451 0 0 1 -.236 -.067 .49 .49 0 0 1 -.173 -.183 .532 .532 0 0 1 -.006 -.503 L9.06 .311 C9.16 .119 9.35 0 9.557 0Z M3.266 9.376 a.171 .171 0 0 1 -.195 -.181 L3.43 4 .02 10.607 c-.064 .125 .042 .268 .185 .248 l1.673 -.231 a.171 .171 0 0 1 .195 .181 L1.714 16 l3.41 -6.607 c.064 -.125 -.043 -.268 -.185 -.248 l-1.673 .231Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 9.558 0
                moveTo(x = 9.558f, y = 0.0f)
                // h 5.403
                horizontalLineToRelative(dx = 5.403f)
                // c 0.264 0 0.422 0.311 0.279 0.548
                curveToRelative(
                    dx1 = 0.264f,
                    dy1 = 0.0f,
                    dx2 = 0.422f,
                    dy2 = 0.311f,
                    dx3 = 0.279f,
                    dy3 = 0.548f,
                )
                // l -2.975 3.85
                lineToRelative(dx = -2.975f, dy = 3.85f)
                // a 0.173 0.173 0 0 0 0.05 0.232
                arcToRelative(
                    a = 0.173f,
                    b = 0.173f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.05f,
                    dy1 = 0.232f,
                )
                // a 0.147 0.147 0 0 0 0.08 0.024
                arcToRelative(
                    a = 0.147f,
                    b = 0.147f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.08f,
                    dy1 = 0.024f,
                )
                // h 3.13
                horizontalLineToRelative(dx = 3.13f)
                // c 0.416 0 0.63 0.53 0.345 0.855
                curveToRelative(
                    dx1 = 0.416f,
                    dy1 = 0.0f,
                    dx2 = 0.63f,
                    dy2 = 0.53f,
                    dx3 = 0.345f,
                    dy3 = 0.855f,
                )
                // L 6.62 16
                lineTo(x = 6.62f, y = 16.0f)
                // l 2.203 -8.082
                lineToRelative(dx = 2.203f, dy = -8.082f)
                // a 0.177 0.177 0 0 0 -0.025 -0.146
                arcToRelative(
                    a = 0.177f,
                    b = 0.177f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.025f,
                    dy1 = -0.146f,
                )
                // a 0.159 0.159 0 0 0 -0.055 -0.048
                arcToRelative(
                    a = 0.159f,
                    b = 0.159f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.055f,
                    dy1 = -0.048f,
                )
                // a 0.148 0.148 0 0 0 -0.07 -0.018
                arcToRelative(
                    a = 0.148f,
                    b = 0.148f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.07f,
                    dy1 = -0.018f,
                )
                // H 5.476
                horizontalLineTo(x = 5.476f)
                // a 0.451 0.451 0 0 1 -0.236 -0.067
                arcToRelative(
                    a = 0.451f,
                    b = 0.451f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.236f,
                    dy1 = -0.067f,
                )
                // a 0.49 0.49 0 0 1 -0.173 -0.183
                arcToRelative(
                    a = 0.49f,
                    b = 0.49f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.173f,
                    dy1 = -0.183f,
                )
                // a 0.532 0.532 0 0 1 -0.006 -0.503
                arcToRelative(
                    a = 0.532f,
                    b = 0.532f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.006f,
                    dy1 = -0.503f,
                )
                // L 9.06 0.311
                lineTo(x = 9.06f, y = 0.311f)
                // C 9.16 0.119 9.35 0 9.557 0z
                curveTo(
                    x1 = 9.16f,
                    y1 = 0.119f,
                    x2 = 9.35f,
                    y2 = 0.0f,
                    x3 = 9.557f,
                    y3 = 0.0f,
                )
                close()
                // M 3.266 9.376
                moveTo(x = 3.266f, y = 9.376f)
                // a 0.171 0.171 0 0 1 -0.195 -0.181
                arcToRelative(
                    a = 0.171f,
                    b = 0.171f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.195f,
                    dy1 = -0.181f,
                )
                // L 3.43 4
                lineTo(x = 3.43f, y = 4.0f)
                // L 0.02 10.607
                lineTo(x = 0.02f, y = 10.607f)
                // c -0.064 0.125 0.042 0.268 0.185 0.248
                curveToRelative(
                    dx1 = -0.064f,
                    dy1 = 0.125f,
                    dx2 = 0.042f,
                    dy2 = 0.268f,
                    dx3 = 0.185f,
                    dy3 = 0.248f,
                )
                // l 1.673 -0.231
                lineToRelative(dx = 1.673f, dy = -0.231f)
                // a 0.171 0.171 0 0 1 0.195 0.181
                arcToRelative(
                    a = 0.171f,
                    b = 0.171f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.195f,
                    dy1 = 0.181f,
                )
                // L 1.714 16
                lineTo(x = 1.714f, y = 16.0f)
                // l 3.41 -6.607
                lineToRelative(dx = 3.41f, dy = -6.607f)
                // c 0.064 -0.125 -0.043 -0.268 -0.185 -0.248
                curveToRelative(
                    dx1 = 0.064f,
                    dy1 = -0.125f,
                    dx2 = -0.043f,
                    dy2 = -0.268f,
                    dx3 = -0.185f,
                    dy3 = -0.248f,
                )
                // l -1.673 0.231z
                lineToRelative(dx = -1.673f, dy = 0.231f)
                close()
            }
            // M3.266 9.376 a.171 .171 0 0 1 -.195 -.181 L3.43 4 .02 10.607 c-.064 .125 .042 .268 .185 .248 l1.673 -.231 a.171 .171 0 0 1 .195 .181 L1.714 16 l3.41 -6.607 c.064 -.125 -.043 -.268 -.185 -.248 l-1.673 .231Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 3.266 9.376
                moveTo(x = 3.266f, y = 9.376f)
                // a 0.171 0.171 0 0 1 -0.195 -0.181
                arcToRelative(
                    a = 0.171f,
                    b = 0.171f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.195f,
                    dy1 = -0.181f,
                )
                // L 3.43 4
                lineTo(x = 3.43f, y = 4.0f)
                // L 0.02 10.607
                lineTo(x = 0.02f, y = 10.607f)
                // c -0.064 0.125 0.042 0.268 0.185 0.248
                curveToRelative(
                    dx1 = -0.064f,
                    dy1 = 0.125f,
                    dx2 = 0.042f,
                    dy2 = 0.268f,
                    dx3 = 0.185f,
                    dy3 = 0.248f,
                )
                // l 1.673 -0.231
                lineToRelative(dx = 1.673f, dy = -0.231f)
                // a 0.171 0.171 0 0 1 0.195 0.181
                arcToRelative(
                    a = 0.171f,
                    b = 0.171f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.195f,
                    dy1 = 0.181f,
                )
                // L 1.714 16
                lineTo(x = 1.714f, y = 16.0f)
                // l 3.41 -6.607
                lineToRelative(dx = 3.41f, dy = -6.607f)
                // c 0.064 -0.125 -0.043 -0.268 -0.185 -0.248
                curveToRelative(
                    dx1 = 0.064f,
                    dy1 = -0.125f,
                    dx2 = -0.043f,
                    dy2 = -0.268f,
                    dx3 = -0.185f,
                    dy3 = -0.248f,
                )
                // l -1.673 0.231z
                lineToRelative(dx = -1.673f, dy = 0.231f)
                close()
            }
        }.build().also { _ic1805 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1805: ImageVector? = null
