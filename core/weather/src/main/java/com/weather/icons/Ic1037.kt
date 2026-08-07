package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1037: ImageVector
    get() {
        val current = _ic1037
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1037",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // m9 4.866 -3.027 5.462 .696 1.729 -1.761 3.927 L.212 16 a.184 .184 0 0 1 -.103 -.031 .232 .232 0 0 1 -.077 -.088 .29 .29 0 0 1 -.01 -.246 L6.69 .149 a.247 .247 0 0 1 .077 -.107 A.188 .188 0 0 1 6.88 0 c.04 0 .08 .012 .113 .037 a.243 .243 0 0 1 .08 .105 L9 4.866Z M13.5 11 l2.482 4.676 c.057 .15 -.03 .324 -.163 .324 L10 15.969 13.5 11Z m-1.595 -3.44 .46 -4.56 -4.16 7.05 1.605 -.212 L8 15.068 l5.653 -7.362 -1.748 -.145Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 9 4.866
                moveTo(x = 9.0f, y = 4.866f)
                // l -3.027 5.462
                lineToRelative(dx = -3.027f, dy = 5.462f)
                // l 0.696 1.729
                lineToRelative(dx = 0.696f, dy = 1.729f)
                // l -1.761 3.927
                lineToRelative(dx = -1.761f, dy = 3.927f)
                // L 0.212 16
                lineTo(x = 0.212f, y = 16.0f)
                // a 0.184 0.184 0 0 1 -0.103 -0.031
                arcToRelative(
                    a = 0.184f,
                    b = 0.184f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.103f,
                    dy1 = -0.031f,
                )
                // a 0.232 0.232 0 0 1 -0.077 -0.088
                arcToRelative(
                    a = 0.232f,
                    b = 0.232f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.077f,
                    dy1 = -0.088f,
                )
                // a 0.29 0.29 0 0 1 -0.01 -0.246
                arcToRelative(
                    a = 0.29f,
                    b = 0.29f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.01f,
                    dy1 = -0.246f,
                )
                // L 6.69 0.149
                lineTo(x = 6.69f, y = 0.149f)
                // a 0.247 0.247 0 0 1 0.077 -0.107
                arcToRelative(
                    a = 0.247f,
                    b = 0.247f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.077f,
                    dy1 = -0.107f,
                )
                // A 0.188 0.188 0 0 1 6.88 0
                arcTo(
                    horizontalEllipseRadius = 0.188f,
                    verticalEllipseRadius = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 6.88f,
                    y1 = 0.0f,
                )
                // c 0.04 0 0.08 0.012 0.113 0.037
                curveToRelative(
                    dx1 = 0.04f,
                    dy1 = 0.0f,
                    dx2 = 0.08f,
                    dy2 = 0.012f,
                    dx3 = 0.113f,
                    dy3 = 0.037f,
                )
                // a 0.243 0.243 0 0 1 0.08 0.105
                arcToRelative(
                    a = 0.243f,
                    b = 0.243f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.08f,
                    dy1 = 0.105f,
                )
                // L 9 4.866z
                lineTo(x = 9.0f, y = 4.866f)
                close()
                // M 13.5 11
                moveTo(x = 13.5f, y = 11.0f)
                // l 2.482 4.676
                lineToRelative(dx = 2.482f, dy = 4.676f)
                // c 0.057 0.15 -0.03 0.324 -0.163 0.324
                curveToRelative(
                    dx1 = 0.057f,
                    dy1 = 0.15f,
                    dx2 = -0.03f,
                    dy2 = 0.324f,
                    dx3 = -0.163f,
                    dy3 = 0.324f,
                )
                // L 10 15.969
                lineTo(x = 10.0f, y = 15.969f)
                // L 13.5 11z
                lineTo(x = 13.5f, y = 11.0f)
                close()
                // m -1.595 -3.44
                moveToRelative(dx = -1.595f, dy = -3.44f)
                // l 0.46 -4.56
                lineToRelative(dx = 0.46f, dy = -4.56f)
                // l -4.16 7.05
                lineToRelative(dx = -4.16f, dy = 7.05f)
                // l 1.605 -0.212
                lineToRelative(dx = 1.605f, dy = -0.212f)
                // L 8 15.068
                lineTo(x = 8.0f, y = 15.068f)
                // l 5.653 -7.362
                lineToRelative(dx = 5.653f, dy = -7.362f)
                // l -1.748 -0.145z
                lineToRelative(dx = -1.748f, dy = -0.145f)
                close()
            }
        }.build().also { _ic1037 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1037: ImageVector? = null
