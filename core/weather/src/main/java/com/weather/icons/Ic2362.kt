package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2362: ImageVector
    get() {
        val current = _ic2362
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2362",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M1 0 H0 v16 h1 v-1 l8 -3.5 L1 8 l8 -3.5 L1 1 V0Z m12 6 a3 3 0 1 0 0 -6 3 3 0 0 0 0 6Z m-.467 -4.294 c-.024 -.212 .192 -.393 .467 -.393 s.491 .181 .467 .393 l-.211 1.857 h-.512 l-.21 -1.857Z m.845 2.607 a.375 .375 0 1 1 -.75 0 .375 .375 0 0 1 .75 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 1 0
                moveTo(x = 1.0f, y = 0.0f)
                // H 0
                horizontalLineTo(x = 0.0f)
                // v 16
                verticalLineToRelative(dy = 16.0f)
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // v -1
                verticalLineToRelative(dy = -1.0f)
                // l 8 -3.5
                lineToRelative(dx = 8.0f, dy = -3.5f)
                // L 1 8
                lineTo(x = 1.0f, y = 8.0f)
                // l 8 -3.5
                lineToRelative(dx = 8.0f, dy = -3.5f)
                // L 1 1
                lineTo(x = 1.0f, y = 1.0f)
                // V 0z
                verticalLineTo(y = 0.0f)
                close()
                // m 12 6
                moveToRelative(dx = 12.0f, dy = 6.0f)
                // a 3 3 0 1 0 0 -6
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -6.0f,
                )
                // a 3 3 0 0 0 0 6z
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 6.0f,
                )
                close()
                // m -0.467 -4.294
                moveToRelative(dx = -0.467f, dy = -4.294f)
                // c -0.024 -0.212 0.192 -0.393 0.467 -0.393
                curveToRelative(
                    dx1 = -0.024f,
                    dy1 = -0.212f,
                    dx2 = 0.192f,
                    dy2 = -0.393f,
                    dx3 = 0.467f,
                    dy3 = -0.393f,
                )
                // s 0.491 0.181 0.467 0.393
                reflectiveCurveToRelative(
                    dx1 = 0.491f,
                    dy1 = 0.181f,
                    dx2 = 0.467f,
                    dy2 = 0.393f,
                )
                // l -0.211 1.857
                lineToRelative(dx = -0.211f, dy = 1.857f)
                // h -0.512
                horizontalLineToRelative(dx = -0.512f)
                // l -0.21 -1.857z
                lineToRelative(dx = -0.21f, dy = -1.857f)
                close()
                // m 0.845 2.607
                moveToRelative(dx = 0.845f, dy = 2.607f)
                // a 0.375 0.375 0 1 1 -0.75 0
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.75f,
                    dy1 = 0.0f,
                )
                // a 0.375 0.375 0 0 1 0.75 0z
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.75f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic2362 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2362: ImageVector? = null
