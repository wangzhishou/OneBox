package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2160: ImageVector
    get() {
        val current = _ic2160
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2160",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M2.068 5.982 a2 2 0 0 1 .2 -1.518 C2.768 3.598 4.625 2.604 6 2 c.165 1.492 .232 3.598 -.268 4.464 a2 2 0 0 1 -3.664 -.482Z m.034 6.991 a3 3 0 0 1 .3 -2.277 C3.152 9.397 5.937 7.905 8 7 c.248 2.239 .348 5.397 -.402 6.696 a3 3 0 0 1 -5.496 -.723Z m9.166 -6.509 a2 2 0 1 0 3.464 2 c.5 -.866 .433 -2.972 .268 -4.464 -1.375 .603 -3.232 1.598 -3.732 2.464Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 2.068 5.982
                moveTo(x = 2.068f, y = 5.982f)
                // a 2 2 0 0 1 0.2 -1.518
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.2f,
                    dy1 = -1.518f,
                )
                // C 2.768 3.598 4.625 2.604 6 2
                curveTo(
                    x1 = 2.768f,
                    y1 = 3.598f,
                    x2 = 4.625f,
                    y2 = 2.604f,
                    x3 = 6.0f,
                    y3 = 2.0f,
                )
                // c 0.165 1.492 0.232 3.598 -0.268 4.464
                curveToRelative(
                    dx1 = 0.165f,
                    dy1 = 1.492f,
                    dx2 = 0.232f,
                    dy2 = 3.598f,
                    dx3 = -0.268f,
                    dy3 = 4.464f,
                )
                // a 2 2 0 0 1 -3.664 -0.482z
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.664f,
                    dy1 = -0.482f,
                )
                close()
                // m 0.034 6.991
                moveToRelative(dx = 0.034f, dy = 6.991f)
                // a 3 3 0 0 1 0.3 -2.277
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.3f,
                    dy1 = -2.277f,
                )
                // C 3.152 9.397 5.937 7.905 8 7
                curveTo(
                    x1 = 3.152f,
                    y1 = 9.397f,
                    x2 = 5.937f,
                    y2 = 7.905f,
                    x3 = 8.0f,
                    y3 = 7.0f,
                )
                // c 0.248 2.239 0.348 5.397 -0.402 6.696
                curveToRelative(
                    dx1 = 0.248f,
                    dy1 = 2.239f,
                    dx2 = 0.348f,
                    dy2 = 5.397f,
                    dx3 = -0.402f,
                    dy3 = 6.696f,
                )
                // a 3 3 0 0 1 -5.496 -0.723z
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -5.496f,
                    dy1 = -0.723f,
                )
                close()
                // m 9.166 -6.509
                moveToRelative(dx = 9.166f, dy = -6.509f)
                // a 2 2 0 1 0 3.464 2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 3.464f,
                    dy1 = 2.0f,
                )
                // c 0.5 -0.866 0.433 -2.972 0.268 -4.464
                curveToRelative(
                    dx1 = 0.5f,
                    dy1 = -0.866f,
                    dx2 = 0.433f,
                    dy2 = -2.972f,
                    dx3 = 0.268f,
                    dy3 = -4.464f,
                )
                // c -1.375 0.603 -3.232 1.598 -3.732 2.464z
                curveToRelative(
                    dx1 = -1.375f,
                    dy1 = 0.603f,
                    dx2 = -3.232f,
                    dy2 = 1.598f,
                    dx3 = -3.732f,
                    dy3 = 2.464f,
                )
                close()
            }
        }.build().also { _ic2160 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2160: ImageVector? = null
