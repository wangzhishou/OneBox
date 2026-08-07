package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2352: ImageVector
    get() {
        val current = _ic2352
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2352",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M13.5 2.5 a2.5 2.5 0 0 0 -2.421 1.875 .5 .5 0 0 0 .968 .25 A1.5 1.5 0 0 1 15 5 c0 .812 -.76 1.5 -1.5 1.5 H.5 a.5 .5 0 1 0 0 1 h13 C14.76 7.5 16 6.397 16 5 a2.5 2.5 0 0 0 -2.5 -2.5Z m-13 6 a.5 .5 0 1 0 0 1 h11 a1 1 0 1 1 -.943 1.333 .5 .5 0 0 0 -.943 .334 A2 2 0 1 0 11.5 8.5 H.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 13.5 2.5
                moveTo(x = 13.5f, y = 2.5f)
                // a 2.5 2.5 0 0 0 -2.421 1.875
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.421f,
                    dy1 = 1.875f,
                )
                // a 0.5 0.5 0 0 0 0.968 0.25
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.968f,
                    dy1 = 0.25f,
                )
                // A 1.5 1.5 0 0 1 15 5
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 15.0f,
                    y1 = 5.0f,
                )
                // c 0 0.812 -0.76 1.5 -1.5 1.5
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.812f,
                    dx2 = -0.76f,
                    dy2 = 1.5f,
                    dx3 = -1.5f,
                    dy3 = 1.5f,
                )
                // H 0.5
                horizontalLineTo(x = 0.5f)
                // a 0.5 0.5 0 1 0 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // h 13
                horizontalLineToRelative(dx = 13.0f)
                // C 14.76 7.5 16 6.397 16 5
                curveTo(
                    x1 = 14.76f,
                    y1 = 7.5f,
                    x2 = 16.0f,
                    y2 = 6.397f,
                    x3 = 16.0f,
                    y3 = 5.0f,
                )
                // a 2.5 2.5 0 0 0 -2.5 -2.5z
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.5f,
                    dy1 = -2.5f,
                )
                close()
                // m -13 6
                moveToRelative(dx = -13.0f, dy = 6.0f)
                // a 0.5 0.5 0 1 0 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // h 11
                horizontalLineToRelative(dx = 11.0f)
                // a 1 1 0 1 1 -0.943 1.333
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.943f,
                    dy1 = 1.333f,
                )
                // a 0.5 0.5 0 0 0 -0.943 0.334
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.943f,
                    dy1 = 0.334f,
                )
                // A 2 2 0 1 0 11.5 8.5
                arcTo(
                    horizontalEllipseRadius = 2.0f,
                    verticalEllipseRadius = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    x1 = 11.5f,
                    y1 = 8.5f,
                )
                // H 0.5z
                horizontalLineTo(x = 0.5f)
                close()
            }
            // M6.26 3 8 4.16 l-.555 .833 -1.258 -.839 -1.964 .982 L2 4.024 l.447 -.894 1.777 .888 L6.26 3Z m0 8 L8 12.161 l-.555 .832 -1.258 -.839 -1.964 .982 L2 12.024 l.447 -.894 1.777 .888 L6.26 11Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6.26 3
                moveTo(x = 6.26f, y = 3.0f)
                // L 8 4.16
                lineTo(x = 8.0f, y = 4.16f)
                // l -0.555 0.833
                lineToRelative(dx = -0.555f, dy = 0.833f)
                // l -1.258 -0.839
                lineToRelative(dx = -1.258f, dy = -0.839f)
                // l -1.964 0.982
                lineToRelative(dx = -1.964f, dy = 0.982f)
                // L 2 4.024
                lineTo(x = 2.0f, y = 4.024f)
                // l 0.447 -0.894
                lineToRelative(dx = 0.447f, dy = -0.894f)
                // l 1.777 0.888
                lineToRelative(dx = 1.777f, dy = 0.888f)
                // L 6.26 3z
                lineTo(x = 6.26f, y = 3.0f)
                close()
                // m 0 8
                moveToRelative(dx = 0.0f, dy = 8.0f)
                // L 8 12.161
                lineTo(x = 8.0f, y = 12.161f)
                // l -0.555 0.832
                lineToRelative(dx = -0.555f, dy = 0.832f)
                // l -1.258 -0.839
                lineToRelative(dx = -1.258f, dy = -0.839f)
                // l -1.964 0.982
                lineToRelative(dx = -1.964f, dy = 0.982f)
                // L 2 12.024
                lineTo(x = 2.0f, y = 12.024f)
                // l 0.447 -0.894
                lineToRelative(dx = 0.447f, dy = -0.894f)
                // l 1.777 0.888
                lineToRelative(dx = 1.777f, dy = 0.888f)
                // L 6.26 11z
                lineTo(x = 6.26f, y = 11.0f)
                close()
            }
        }.build().also { _ic2352 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2352: ImageVector? = null
