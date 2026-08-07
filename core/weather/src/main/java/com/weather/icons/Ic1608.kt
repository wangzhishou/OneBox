package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1608: ImageVector
    get() {
        val current = _ic1608
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1608",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M12.487 .349 9.948 6.18 l2.398 .49 -6.463 6.183 2.537 -5.83 -2.397 -.493 L12.487 .35Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 12.487 0.349
                moveTo(x = 12.487f, y = 0.349f)
                // L 9.948 6.18
                lineTo(x = 9.948f, y = 6.18f)
                // l 2.398 0.49
                lineToRelative(dx = 2.398f, dy = 0.49f)
                // l -6.463 6.183
                lineToRelative(dx = -6.463f, dy = 6.183f)
                // l 2.537 -5.83
                lineToRelative(dx = 2.537f, dy = -5.83f)
                // l -2.397 -0.493
                lineToRelative(dx = -2.397f, dy = -0.493f)
                // L 12.487 0.35z
                lineTo(x = 12.487f, y = 0.35f)
                close()
            }
            // m4 2 -.423 6.346 L6 8 l-4 8 .423 -6.346 L0 10 l4 -8Z m8.699 7.552 a.23 .23 0 0 0 -.398 0 l-3.27 5.67 a.227 .227 0 0 0 .198 .34 h6.542 a.227 .227 0 0 0 .198 -.34 l-3.27 -5.67Z m-.744 1.876 c-.028 -.247 .224 -.46 .545 -.46 .32 0 .573 .213 .545 .46 l-.246 2.166 H12.2 l-.246 -2.166Z m.986 3.04 a.438 .438 0 1 1 -.875 0 .438 .438 0 0 1 .875 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4 2
                moveTo(x = 4.0f, y = 2.0f)
                // l -0.423 6.346
                lineToRelative(dx = -0.423f, dy = 6.346f)
                // L 6 8
                lineTo(x = 6.0f, y = 8.0f)
                // l -4 8
                lineToRelative(dx = -4.0f, dy = 8.0f)
                // l 0.423 -6.346
                lineToRelative(dx = 0.423f, dy = -6.346f)
                // L 0 10
                lineTo(x = 0.0f, y = 10.0f)
                // l 4 -8z
                lineToRelative(dx = 4.0f, dy = -8.0f)
                close()
                // m 8.699 7.552
                moveToRelative(dx = 8.699f, dy = 7.552f)
                // a 0.23 0.23 0 0 0 -0.398 0
                arcToRelative(
                    a = 0.23f,
                    b = 0.23f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.398f,
                    dy1 = 0.0f,
                )
                // l -3.27 5.67
                lineToRelative(dx = -3.27f, dy = 5.67f)
                // a 0.227 0.227 0 0 0 0.198 0.34
                arcToRelative(
                    a = 0.227f,
                    b = 0.227f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.198f,
                    dy1 = 0.34f,
                )
                // h 6.542
                horizontalLineToRelative(dx = 6.542f)
                // a 0.227 0.227 0 0 0 0.198 -0.34
                arcToRelative(
                    a = 0.227f,
                    b = 0.227f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.198f,
                    dy1 = -0.34f,
                )
                // l -3.27 -5.67z
                lineToRelative(dx = -3.27f, dy = -5.67f)
                close()
                // m -0.744 1.876
                moveToRelative(dx = -0.744f, dy = 1.876f)
                // c -0.028 -0.247 0.224 -0.46 0.545 -0.46
                curveToRelative(
                    dx1 = -0.028f,
                    dy1 = -0.247f,
                    dx2 = 0.224f,
                    dy2 = -0.46f,
                    dx3 = 0.545f,
                    dy3 = -0.46f,
                )
                // c 0.32 0 0.573 0.213 0.545 0.46
                curveToRelative(
                    dx1 = 0.32f,
                    dy1 = 0.0f,
                    dx2 = 0.573f,
                    dy2 = 0.213f,
                    dx3 = 0.545f,
                    dy3 = 0.46f,
                )
                // l -0.246 2.166
                lineToRelative(dx = -0.246f, dy = 2.166f)
                // H 12.2
                horizontalLineTo(x = 12.2f)
                // l -0.246 -2.166z
                lineToRelative(dx = -0.246f, dy = -2.166f)
                close()
                // m 0.986 3.04
                moveToRelative(dx = 0.986f, dy = 3.04f)
                // a 0.438 0.438 0 1 1 -0.875 0
                arcToRelative(
                    a = 0.438f,
                    b = 0.438f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.875f,
                    dy1 = 0.0f,
                )
                // a 0.438 0.438 0 0 1 0.875 0z
                arcToRelative(
                    a = 0.438f,
                    b = 0.438f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.875f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic1608 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1608: ImageVector? = null
