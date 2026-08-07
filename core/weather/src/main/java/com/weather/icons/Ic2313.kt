package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2313: ImageVector
    get() {
        val current = _ic2313
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2313",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M13.17 10.473 a.197 .197 0 0 0 -.34 0 l-2.804 4.86 c-.075 .13 .02 .292 .17 .292 h5.607 a.194 .194 0 0 0 .17 -.291 l-2.803 -4.861Z m-.637 1.608 c-.024 -.212 .192 -.393 .467 -.393 s.491 .181 .467 .393 l-.211 1.857 h-.512 l-.21 -1.857Z m.845 2.607 a.375 .375 0 1 1 -.75 0 .375 .375 0 0 1 .75 0Z M1 0 H0 v16 h1 V7.752 c.75 .836 2 1.135 4.703 .431 C8.228 7.525 11 9 11 9 V1.81 C8.12 .65 6.717 1.003 4.57 1.54 l-.303 .075 C1.945 2.193 1 1 1 1 V0Z m3 3 4 -.5 v4 L4 7 V3Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 13.17 10.473
                moveTo(x = 13.17f, y = 10.473f)
                // a 0.197 0.197 0 0 0 -0.34 0
                arcToRelative(
                    a = 0.197f,
                    b = 0.197f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.34f,
                    dy1 = 0.0f,
                )
                // l -2.804 4.86
                lineToRelative(dx = -2.804f, dy = 4.86f)
                // c -0.075 0.13 0.02 0.292 0.17 0.292
                curveToRelative(
                    dx1 = -0.075f,
                    dy1 = 0.13f,
                    dx2 = 0.02f,
                    dy2 = 0.292f,
                    dx3 = 0.17f,
                    dy3 = 0.292f,
                )
                // h 5.607
                horizontalLineToRelative(dx = 5.607f)
                // a 0.194 0.194 0 0 0 0.17 -0.291
                arcToRelative(
                    a = 0.194f,
                    b = 0.194f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.17f,
                    dy1 = -0.291f,
                )
                // l -2.803 -4.861z
                lineToRelative(dx = -2.803f, dy = -4.861f)
                close()
                // m -0.637 1.608
                moveToRelative(dx = -0.637f, dy = 1.608f)
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
                // M 1 0
                moveTo(x = 1.0f, y = 0.0f)
                // H 0
                horizontalLineTo(x = 0.0f)
                // v 16
                verticalLineToRelative(dy = 16.0f)
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // V 7.752
                verticalLineTo(y = 7.752f)
                // c 0.75 0.836 2 1.135 4.703 0.431
                curveToRelative(
                    dx1 = 0.75f,
                    dy1 = 0.836f,
                    dx2 = 2.0f,
                    dy2 = 1.135f,
                    dx3 = 4.703f,
                    dy3 = 0.431f,
                )
                // C 8.228 7.525 11 9 11 9
                curveTo(
                    x1 = 8.228f,
                    y1 = 7.525f,
                    x2 = 11.0f,
                    y2 = 9.0f,
                    x3 = 11.0f,
                    y3 = 9.0f,
                )
                // V 1.81
                verticalLineTo(y = 1.81f)
                // C 8.12 0.65 6.717 1.003 4.57 1.54
                curveTo(
                    x1 = 8.12f,
                    y1 = 0.65f,
                    x2 = 6.717f,
                    y2 = 1.003f,
                    x3 = 4.57f,
                    y3 = 1.54f,
                )
                // l -0.303 0.075
                lineToRelative(dx = -0.303f, dy = 0.075f)
                // C 1.945 2.193 1 1 1 1
                curveTo(
                    x1 = 1.945f,
                    y1 = 2.193f,
                    x2 = 1.0f,
                    y2 = 1.0f,
                    x3 = 1.0f,
                    y3 = 1.0f,
                )
                // V 0z
                verticalLineTo(y = 0.0f)
                close()
                // m 3 3
                moveToRelative(dx = 3.0f, dy = 3.0f)
                // l 4 -0.5
                lineToRelative(dx = 4.0f, dy = -0.5f)
                // v 4
                verticalLineToRelative(dy = 4.0f)
                // L 4 7
                lineTo(x = 4.0f, y = 7.0f)
                // V 3z
                verticalLineTo(y = 3.0f)
                close()
            }
        }.build().also { _ic2313 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2313: ImageVector? = null
