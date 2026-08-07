package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2163: ImageVector
    get() {
        val current = _ic2163
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2163",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // m16 5.857 -2.986 -4.779 L3.066 16 H16 V5.857Z M8 4 a2 2 0 1 0 0 -4 2 2 0 0 0 0 4Z M4.5 4 l-.5 .866 .866 .5 .5 -.866 L4.5 4Z M2.414 6 1 7.414 l1.414 1.414 1.414 -1.414 L2.414 6Z M4 11.259 4.966 11 l.259 .966 -.966 .259 L4 11.259Z M3 13.5 a1.5 1.5 0 1 1 -3 0 1.5 1.5 0 0 1 3 0Z M6.5 9 a1.5 1.5 0 1 0 0 -3 1.5 1.5 0 0 0 0 3Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 16 5.857
                moveTo(x = 16.0f, y = 5.857f)
                // l -2.986 -4.779
                lineToRelative(dx = -2.986f, dy = -4.779f)
                // L 3.066 16
                lineTo(x = 3.066f, y = 16.0f)
                // H 16
                horizontalLineTo(x = 16.0f)
                // V 5.857z
                verticalLineTo(y = 5.857f)
                close()
                // M 8 4
                moveTo(x = 8.0f, y = 4.0f)
                // a 2 2 0 1 0 0 -4
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -4.0f,
                )
                // a 2 2 0 0 0 0 4z
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 4.0f,
                )
                close()
                // M 4.5 4
                moveTo(x = 4.5f, y = 4.0f)
                // l -0.5 0.866
                lineToRelative(dx = -0.5f, dy = 0.866f)
                // l 0.866 0.5
                lineToRelative(dx = 0.866f, dy = 0.5f)
                // l 0.5 -0.866
                lineToRelative(dx = 0.5f, dy = -0.866f)
                // L 4.5 4z
                lineTo(x = 4.5f, y = 4.0f)
                close()
                // M 2.414 6
                moveTo(x = 2.414f, y = 6.0f)
                // L 1 7.414
                lineTo(x = 1.0f, y = 7.414f)
                // l 1.414 1.414
                lineToRelative(dx = 1.414f, dy = 1.414f)
                // l 1.414 -1.414
                lineToRelative(dx = 1.414f, dy = -1.414f)
                // L 2.414 6z
                lineTo(x = 2.414f, y = 6.0f)
                close()
                // M 4 11.259
                moveTo(x = 4.0f, y = 11.259f)
                // L 4.966 11
                lineTo(x = 4.966f, y = 11.0f)
                // l 0.259 0.966
                lineToRelative(dx = 0.259f, dy = 0.966f)
                // l -0.966 0.259
                lineToRelative(dx = -0.966f, dy = 0.259f)
                // L 4 11.259z
                lineTo(x = 4.0f, y = 11.259f)
                close()
                // M 3 13.5
                moveTo(x = 3.0f, y = 13.5f)
                // a 1.5 1.5 0 1 1 -3 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -3.0f,
                    dy1 = 0.0f,
                )
                // a 1.5 1.5 0 0 1 3 0z
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 3.0f,
                    dy1 = 0.0f,
                )
                close()
                // M 6.5 9
                moveTo(x = 6.5f, y = 9.0f)
                // a 1.5 1.5 0 1 0 0 -3
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -3.0f,
                )
                // a 1.5 1.5 0 0 0 0 3z
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 3.0f,
                )
                close()
            }
        }.build().also { _ic2163 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2163: ImageVector? = null
