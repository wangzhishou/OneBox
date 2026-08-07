package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic511Fill: ImageVector
    get() {
        val current = _ic511Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic511Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M12.543 4.487 c-1.581 0 -3.876 1.712 -4.57 2.888 C7.282 6.2 4.987 4.487 3.406 4.487 a3.486 3.486 0 0 0 0 6.97 c1.58 0 3.876 -1.75 4.569 -2.906 .693 1.156 2.988 2.906 4.569 2.906 a3.486 3.486 0 0 0 0 -6.97 h-.001Z M3.989 2.481 a1 1 0 1 1 -2 0 1 1 0 0 1 2 0Z m10.011 0 a1 1 0 1 1 -2 0 1 1 0 0 1 2 0Z M2.989 14.457 a1 1 0 1 0 0 -2 1 1 0 0 0 0 2Z m11.011 -1 a1 1 0 1 1 -2 0 1 1 0 0 1 2 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 12.543 4.487
                moveTo(x = 12.543f, y = 4.487f)
                // c -1.581 0 -3.876 1.712 -4.57 2.888
                curveToRelative(
                    dx1 = -1.581f,
                    dy1 = 0.0f,
                    dx2 = -3.876f,
                    dy2 = 1.712f,
                    dx3 = -4.57f,
                    dy3 = 2.888f,
                )
                // C 7.282 6.2 4.987 4.487 3.406 4.487
                curveTo(
                    x1 = 7.282f,
                    y1 = 6.2f,
                    x2 = 4.987f,
                    y2 = 4.487f,
                    x3 = 3.406f,
                    y3 = 4.487f,
                )
                // a 3.486 3.486 0 0 0 0 6.97
                arcToRelative(
                    a = 3.486f,
                    b = 3.486f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 6.97f,
                )
                // c 1.58 0 3.876 -1.75 4.569 -2.906
                curveToRelative(
                    dx1 = 1.58f,
                    dy1 = 0.0f,
                    dx2 = 3.876f,
                    dy2 = -1.75f,
                    dx3 = 4.569f,
                    dy3 = -2.906f,
                )
                // c 0.693 1.156 2.988 2.906 4.569 2.906
                curveToRelative(
                    dx1 = 0.693f,
                    dy1 = 1.156f,
                    dx2 = 2.988f,
                    dy2 = 2.906f,
                    dx3 = 4.569f,
                    dy3 = 2.906f,
                )
                // a 3.486 3.486 0 0 0 0 -6.97
                arcToRelative(
                    a = 3.486f,
                    b = 3.486f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -6.97f,
                )
                // h -0.001z
                horizontalLineToRelative(dx = -0.001f)
                close()
                // M 3.989 2.481
                moveTo(x = 3.989f, y = 2.481f)
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
                // m 10.011 0
                moveToRelative(dx = 10.011f, dy = 0.0f)
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
                // M 2.989 14.457
                moveTo(x = 2.989f, y = 14.457f)
                // a 1 1 0 1 0 0 -2
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -2.0f,
                )
                // a 1 1 0 0 0 0 2z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 2.0f,
                )
                close()
                // m 11.011 -1
                moveToRelative(dx = 11.011f, dy = -1.0f)
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
        }.build().also { _ic511Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic511Fill: ImageVector? = null
