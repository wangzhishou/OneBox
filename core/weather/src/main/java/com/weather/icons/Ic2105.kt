package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2105: ImageVector
    get() {
        val current = _ic2105
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2105",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M13.5 2.75 a2.5 2.5 0 0 0 -2.421 1.875 .5 .5 0 0 0 .968 .25 A1.5 1.5 0 0 1 15 5.25 c0 .812 -.76 1.5 -1.5 1.5 H.5 a.5 .5 0 0 0 0 1 h13 c1.26 0 2.5 -1.103 2.5 -2.5 a2.5 2.5 0 0 0 -2.5 -2.5Z m-13 6.5 a.5 .5 0 0 0 0 1 h11 a1 1 0 1 1 -.943 1.333 .5 .5 0 0 0 -.943 .334 A2 2 0 1 0 11.5 9.25 H.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 13.5 2.75
                moveTo(x = 13.5f, y = 2.75f)
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
                // A 1.5 1.5 0 0 1 15 5.25
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 15.0f,
                    y1 = 5.25f,
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
                // a 0.5 0.5 0 0 0 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // h 13
                horizontalLineToRelative(dx = 13.0f)
                // c 1.26 0 2.5 -1.103 2.5 -2.5
                curveToRelative(
                    dx1 = 1.26f,
                    dy1 = 0.0f,
                    dx2 = 2.5f,
                    dy2 = -1.103f,
                    dx3 = 2.5f,
                    dy3 = -2.5f,
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
                // m -13 6.5
                moveToRelative(dx = -13.0f, dy = 6.5f)
                // a 0.5 0.5 0 0 0 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
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
                // A 2 2 0 1 0 11.5 9.25
                arcTo(
                    horizontalEllipseRadius = 2.0f,
                    verticalEllipseRadius = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    x1 = 11.5f,
                    y1 = 9.25f,
                )
                // H 0.5z
                horizontalLineTo(x = 0.5f)
                close()
            }
        }.build().also { _ic2105 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2105: ImageVector? = null
