package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1251: ImageVector
    get() {
        val current = _ic1251
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1251",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M5.973 10.328 9 4.866 7.073 .142 a.243 .243 0 0 0 -.08 -.105 A.186 .186 0 0 0 6.88 0 a.18 .18 0 0 0 -.113 .042 .247 .247 0 0 0 -.077 .107 L.022 15.635 a.296 .296 0 0 0 .01 .246 .232 .232 0 0 0 .077 .088 c.031 .02 .067 .031 .103 .031 l4.696 -.016 1.76 -3.927 -.695 -1.729Z m5.632 -2.768 .46 -4.56 -4.16 7.05 1.605 -.212 -1.81 5.23 5.653 -7.362 -1.748 -.145Z m1.225 2.913 a.197 .197 0 0 1 .34 0 l2.804 4.86 a.195 .195 0 0 1 -.17 .292 h-5.607 a.195 .195 0 0 1 -.17 -.291 l2.803 -4.861Z m.17 1.214 c-.275 0 -.491 .182 -.467 .394 l.211 1.857 h.512 l.21 -1.857 c.025 -.212 -.191 -.393 -.466 -.393Z m.003 3.376 a.375 .375 0 1 0 0 -.75 .375 .375 0 0 0 0 .75Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5.973 10.328
                moveTo(x = 5.973f, y = 10.328f)
                // L 9 4.866
                lineTo(x = 9.0f, y = 4.866f)
                // L 7.073 0.142
                lineTo(x = 7.073f, y = 0.142f)
                // a 0.243 0.243 0 0 0 -0.08 -0.105
                arcToRelative(
                    a = 0.243f,
                    b = 0.243f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.08f,
                    dy1 = -0.105f,
                )
                // A 0.186 0.186 0 0 0 6.88 0
                arcTo(
                    horizontalEllipseRadius = 0.186f,
                    verticalEllipseRadius = 0.186f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 6.88f,
                    y1 = 0.0f,
                )
                // a 0.18 0.18 0 0 0 -0.113 0.042
                arcToRelative(
                    a = 0.18f,
                    b = 0.18f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.113f,
                    dy1 = 0.042f,
                )
                // a 0.247 0.247 0 0 0 -0.077 0.107
                arcToRelative(
                    a = 0.247f,
                    b = 0.247f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.077f,
                    dy1 = 0.107f,
                )
                // L 0.022 15.635
                lineTo(x = 0.022f, y = 15.635f)
                // a 0.296 0.296 0 0 0 0.01 0.246
                arcToRelative(
                    a = 0.296f,
                    b = 0.296f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.01f,
                    dy1 = 0.246f,
                )
                // a 0.232 0.232 0 0 0 0.077 0.088
                arcToRelative(
                    a = 0.232f,
                    b = 0.232f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.077f,
                    dy1 = 0.088f,
                )
                // c 0.031 0.02 0.067 0.031 0.103 0.031
                curveToRelative(
                    dx1 = 0.031f,
                    dy1 = 0.02f,
                    dx2 = 0.067f,
                    dy2 = 0.031f,
                    dx3 = 0.103f,
                    dy3 = 0.031f,
                )
                // l 4.696 -0.016
                lineToRelative(dx = 4.696f, dy = -0.016f)
                // l 1.76 -3.927
                lineToRelative(dx = 1.76f, dy = -3.927f)
                // l -0.695 -1.729z
                lineToRelative(dx = -0.695f, dy = -1.729f)
                close()
                // m 5.632 -2.768
                moveToRelative(dx = 5.632f, dy = -2.768f)
                // l 0.46 -4.56
                lineToRelative(dx = 0.46f, dy = -4.56f)
                // l -4.16 7.05
                lineToRelative(dx = -4.16f, dy = 7.05f)
                // l 1.605 -0.212
                lineToRelative(dx = 1.605f, dy = -0.212f)
                // l -1.81 5.23
                lineToRelative(dx = -1.81f, dy = 5.23f)
                // l 5.653 -7.362
                lineToRelative(dx = 5.653f, dy = -7.362f)
                // l -1.748 -0.145z
                lineToRelative(dx = -1.748f, dy = -0.145f)
                close()
                // m 1.225 2.913
                moveToRelative(dx = 1.225f, dy = 2.913f)
                // a 0.197 0.197 0 0 1 0.34 0
                arcToRelative(
                    a = 0.197f,
                    b = 0.197f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.34f,
                    dy1 = 0.0f,
                )
                // l 2.804 4.86
                lineToRelative(dx = 2.804f, dy = 4.86f)
                // a 0.195 0.195 0 0 1 -0.17 0.292
                arcToRelative(
                    a = 0.195f,
                    b = 0.195f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.17f,
                    dy1 = 0.292f,
                )
                // h -5.607
                horizontalLineToRelative(dx = -5.607f)
                // a 0.195 0.195 0 0 1 -0.17 -0.291
                arcToRelative(
                    a = 0.195f,
                    b = 0.195f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.17f,
                    dy1 = -0.291f,
                )
                // l 2.803 -4.861z
                lineToRelative(dx = 2.803f, dy = -4.861f)
                close()
                // m 0.17 1.214
                moveToRelative(dx = 0.17f, dy = 1.214f)
                // c -0.275 0 -0.491 0.182 -0.467 0.394
                curveToRelative(
                    dx1 = -0.275f,
                    dy1 = 0.0f,
                    dx2 = -0.491f,
                    dy2 = 0.182f,
                    dx3 = -0.467f,
                    dy3 = 0.394f,
                )
                // l 0.211 1.857
                lineToRelative(dx = 0.211f, dy = 1.857f)
                // h 0.512
                horizontalLineToRelative(dx = 0.512f)
                // l 0.21 -1.857
                lineToRelative(dx = 0.21f, dy = -1.857f)
                // c 0.025 -0.212 -0.191 -0.393 -0.466 -0.393z
                curveToRelative(
                    dx1 = 0.025f,
                    dy1 = -0.212f,
                    dx2 = -0.191f,
                    dy2 = -0.393f,
                    dx3 = -0.466f,
                    dy3 = -0.393f,
                )
                close()
                // m 0.003 3.376
                moveToRelative(dx = 0.003f, dy = 3.376f)
                // a 0.375 0.375 0 1 0 0 -0.75
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.75f,
                )
                // a 0.375 0.375 0 0 0 0 0.75z
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.75f,
                )
                close()
            }
        }.build().also { _ic1251 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1251: ImageVector? = null
