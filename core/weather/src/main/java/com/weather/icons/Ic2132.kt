package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2132: ImageVector
    get() {
        val current = _ic2132
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2132",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M9.311 6.955 a.086 .086 0 0 0 .068 -.14 L5.66 2.034 a.086 .086 0 0 0 -.135 0 l-3.72 4.783 a.086 .086 0 0 0 .068 .139 h1.394 c.07 0 .111 .08 .069 .138 l-2.378 3.17 a.086 .086 0 0 0 .069 .137 h2.122 c.075 0 .114 .088 .065 .144 l-3.191 3.59 a.086 .086 0 0 0 .064 .143 h4.127 c.048 0 .086 .039 .086 .086 v1.55 c0 .048 .039 .087 .087 .087 h2.412 a.086 .086 0 0 0 .086 -.086 v-1.55 c0 -.048 .038 -.087 .086 -.087 h4.127 a.086 .086 0 0 0 .064 -.143 l-3.19 -3.59 a.086 .086 0 0 1 .064 -.144 h2.122 a.086 .086 0 0 0 .07 -.137 L7.85 7.094 a.086 .086 0 0 1 .069 -.138 H9.31Z M14.9 1.5 c-.9 .3 -1.2 1.1 -1.1 1.6 -.7 -.8 -.7 -1.7 -.6 -3.1 -2.1 .8 -1.6 3.2 -1.7 4 -.5 -.5 -.6 -1.5 -.6 -1.5 -.6 .3 -.9 1.1 -.9 1.8 C10 5.9 11.3 7 12.8 7 c1.5 0 2.7 -1.2 2.7 -2.7 0 -1.1 -.6 -1.4 -.6 -2.8Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 9.311 6.955
                moveTo(x = 9.311f, y = 6.955f)
                // a 0.086 0.086 0 0 0 0.068 -0.14
                arcToRelative(
                    a = 0.086f,
                    b = 0.086f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.068f,
                    dy1 = -0.14f,
                )
                // L 5.66 2.034
                lineTo(x = 5.66f, y = 2.034f)
                // a 0.086 0.086 0 0 0 -0.135 0
                arcToRelative(
                    a = 0.086f,
                    b = 0.086f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.135f,
                    dy1 = 0.0f,
                )
                // l -3.72 4.783
                lineToRelative(dx = -3.72f, dy = 4.783f)
                // a 0.086 0.086 0 0 0 0.068 0.139
                arcToRelative(
                    a = 0.086f,
                    b = 0.086f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.068f,
                    dy1 = 0.139f,
                )
                // h 1.394
                horizontalLineToRelative(dx = 1.394f)
                // c 0.07 0 0.111 0.08 0.069 0.138
                curveToRelative(
                    dx1 = 0.07f,
                    dy1 = 0.0f,
                    dx2 = 0.111f,
                    dy2 = 0.08f,
                    dx3 = 0.069f,
                    dy3 = 0.138f,
                )
                // l -2.378 3.17
                lineToRelative(dx = -2.378f, dy = 3.17f)
                // a 0.086 0.086 0 0 0 0.069 0.137
                arcToRelative(
                    a = 0.086f,
                    b = 0.086f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.069f,
                    dy1 = 0.137f,
                )
                // h 2.122
                horizontalLineToRelative(dx = 2.122f)
                // c 0.075 0 0.114 0.088 0.065 0.144
                curveToRelative(
                    dx1 = 0.075f,
                    dy1 = 0.0f,
                    dx2 = 0.114f,
                    dy2 = 0.088f,
                    dx3 = 0.065f,
                    dy3 = 0.144f,
                )
                // l -3.191 3.59
                lineToRelative(dx = -3.191f, dy = 3.59f)
                // a 0.086 0.086 0 0 0 0.064 0.143
                arcToRelative(
                    a = 0.086f,
                    b = 0.086f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.064f,
                    dy1 = 0.143f,
                )
                // h 4.127
                horizontalLineToRelative(dx = 4.127f)
                // c 0.048 0 0.086 0.039 0.086 0.086
                curveToRelative(
                    dx1 = 0.048f,
                    dy1 = 0.0f,
                    dx2 = 0.086f,
                    dy2 = 0.039f,
                    dx3 = 0.086f,
                    dy3 = 0.086f,
                )
                // v 1.55
                verticalLineToRelative(dy = 1.55f)
                // c 0 0.048 0.039 0.087 0.087 0.087
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.048f,
                    dx2 = 0.039f,
                    dy2 = 0.087f,
                    dx3 = 0.087f,
                    dy3 = 0.087f,
                )
                // h 2.412
                horizontalLineToRelative(dx = 2.412f)
                // a 0.086 0.086 0 0 0 0.086 -0.086
                arcToRelative(
                    a = 0.086f,
                    b = 0.086f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.086f,
                    dy1 = -0.086f,
                )
                // v -1.55
                verticalLineToRelative(dy = -1.55f)
                // c 0 -0.048 0.038 -0.087 0.086 -0.087
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.048f,
                    dx2 = 0.038f,
                    dy2 = -0.087f,
                    dx3 = 0.086f,
                    dy3 = -0.087f,
                )
                // h 4.127
                horizontalLineToRelative(dx = 4.127f)
                // a 0.086 0.086 0 0 0 0.064 -0.143
                arcToRelative(
                    a = 0.086f,
                    b = 0.086f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.064f,
                    dy1 = -0.143f,
                )
                // l -3.19 -3.59
                lineToRelative(dx = -3.19f, dy = -3.59f)
                // a 0.086 0.086 0 0 1 0.064 -0.144
                arcToRelative(
                    a = 0.086f,
                    b = 0.086f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.064f,
                    dy1 = -0.144f,
                )
                // h 2.122
                horizontalLineToRelative(dx = 2.122f)
                // a 0.086 0.086 0 0 0 0.07 -0.137
                arcToRelative(
                    a = 0.086f,
                    b = 0.086f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.07f,
                    dy1 = -0.137f,
                )
                // L 7.85 7.094
                lineTo(x = 7.85f, y = 7.094f)
                // a 0.086 0.086 0 0 1 0.069 -0.138
                arcToRelative(
                    a = 0.086f,
                    b = 0.086f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.069f,
                    dy1 = -0.138f,
                )
                // H 9.31z
                horizontalLineTo(x = 9.31f)
                close()
                // M 14.9 1.5
                moveTo(x = 14.9f, y = 1.5f)
                // c -0.9 0.3 -1.2 1.1 -1.1 1.6
                curveToRelative(
                    dx1 = -0.9f,
                    dy1 = 0.3f,
                    dx2 = -1.2f,
                    dy2 = 1.1f,
                    dx3 = -1.1f,
                    dy3 = 1.6f,
                )
                // c -0.7 -0.8 -0.7 -1.7 -0.6 -3.1
                curveToRelative(
                    dx1 = -0.7f,
                    dy1 = -0.8f,
                    dx2 = -0.7f,
                    dy2 = -1.7f,
                    dx3 = -0.6f,
                    dy3 = -3.1f,
                )
                // c -2.1 0.8 -1.6 3.2 -1.7 4
                curveToRelative(
                    dx1 = -2.1f,
                    dy1 = 0.8f,
                    dx2 = -1.6f,
                    dy2 = 3.2f,
                    dx3 = -1.7f,
                    dy3 = 4.0f,
                )
                // c -0.5 -0.5 -0.6 -1.5 -0.6 -1.5
                curveToRelative(
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                    dx2 = -0.6f,
                    dy2 = -1.5f,
                    dx3 = -0.6f,
                    dy3 = -1.5f,
                )
                // c -0.6 0.3 -0.9 1.1 -0.9 1.8
                curveToRelative(
                    dx1 = -0.6f,
                    dy1 = 0.3f,
                    dx2 = -0.9f,
                    dy2 = 1.1f,
                    dx3 = -0.9f,
                    dy3 = 1.8f,
                )
                // C 10 5.9 11.3 7 12.8 7
                curveTo(
                    x1 = 10.0f,
                    y1 = 5.9f,
                    x2 = 11.3f,
                    y2 = 7.0f,
                    x3 = 12.8f,
                    y3 = 7.0f,
                )
                // c 1.5 0 2.7 -1.2 2.7 -2.7
                curveToRelative(
                    dx1 = 1.5f,
                    dy1 = 0.0f,
                    dx2 = 2.7f,
                    dy2 = -1.2f,
                    dx3 = 2.7f,
                    dy3 = -2.7f,
                )
                // c 0 -1.1 -0.6 -1.4 -0.6 -2.8z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.1f,
                    dx2 = -0.6f,
                    dy2 = -1.4f,
                    dx3 = -0.6f,
                    dy3 = -2.8f,
                )
                close()
            }
        }.build().also { _ic2132 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2132: ImageVector? = null
