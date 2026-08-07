package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1701: ImageVector
    get() {
        val current = _ic1701
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1701",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M4.5 7 a.5 .5 0 0 0 -.5 .5 v2.063 a2 2 0 1 0 1 0 V7.5 a.5 .5 0 0 0 -.5 -.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.5 7
                moveTo(x = 4.5f, y = 7.0f)
                // a 0.5 0.5 0 0 0 -0.5 0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.5f,
                    dy1 = 0.5f,
                )
                // v 2.063
                verticalLineToRelative(dy = 2.063f)
                // a 2 2 0 1 0 1 0
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // V 7.5
                verticalLineTo(y = 7.5f)
                // a 0.5 0.5 0 0 0 -0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                close()
            }
            // m3.2 8.399 -.532 .356 a3.3 3.3 0 1 0 3.665 0 L5.8 8.399 V2.5 a1.3 1.3 0 0 0 -2.6 0 v5.899Z M2 2.5 a2.5 2.5 0 0 1 5 0 v5.258 a4.5 4.5 0 1 1 -5 0 V2.5Z m8 1.124 L10.666 3 12.5 5.476 14.334 3 l.666 .624 L12.5 7 10 3.624Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 3.2 8.399
                moveTo(x = 3.2f, y = 8.399f)
                // l -0.532 0.356
                lineToRelative(dx = -0.532f, dy = 0.356f)
                // a 3.3 3.3 0 1 0 3.665 0
                arcToRelative(
                    a = 3.3f,
                    b = 3.3f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 3.665f,
                    dy1 = 0.0f,
                )
                // L 5.8 8.399
                lineTo(x = 5.8f, y = 8.399f)
                // V 2.5
                verticalLineTo(y = 2.5f)
                // a 1.3 1.3 0 0 0 -2.6 0
                arcToRelative(
                    a = 1.3f,
                    b = 1.3f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.6f,
                    dy1 = 0.0f,
                )
                // v 5.899z
                verticalLineToRelative(dy = 5.899f)
                close()
                // M 2 2.5
                moveTo(x = 2.0f, y = 2.5f)
                // a 2.5 2.5 0 0 1 5 0
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 5.0f,
                    dy1 = 0.0f,
                )
                // v 5.258
                verticalLineToRelative(dy = 5.258f)
                // a 4.5 4.5 0 1 1 -5 0
                arcToRelative(
                    a = 4.5f,
                    b = 4.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -5.0f,
                    dy1 = 0.0f,
                )
                // V 2.5z
                verticalLineTo(y = 2.5f)
                close()
                // m 8 1.124
                moveToRelative(dx = 8.0f, dy = 1.124f)
                // L 10.666 3
                lineTo(x = 10.666f, y = 3.0f)
                // L 12.5 5.476
                lineTo(x = 12.5f, y = 5.476f)
                // L 14.334 3
                lineTo(x = 14.334f, y = 3.0f)
                // l 0.666 0.624
                lineToRelative(dx = 0.666f, dy = 0.624f)
                // L 12.5 7
                lineTo(x = 12.5f, y = 7.0f)
                // L 10 3.624z
                lineTo(x = 10.0f, y = 3.624f)
                close()
            }
            // M10 6.624 10.666 6 12.5 8.476 14.334 6 l.666 .624 L12.5 10 10 6.624Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10 6.624
                moveTo(x = 10.0f, y = 6.624f)
                // L 10.666 6
                lineTo(x = 10.666f, y = 6.0f)
                // L 12.5 8.476
                lineTo(x = 12.5f, y = 8.476f)
                // L 14.334 6
                lineTo(x = 14.334f, y = 6.0f)
                // l 0.666 0.624
                lineToRelative(dx = 0.666f, dy = 0.624f)
                // L 12.5 10
                lineTo(x = 12.5f, y = 10.0f)
                // L 10 6.624z
                lineTo(x = 10.0f, y = 6.624f)
                close()
            }
            // M10.666 9 10 9.624 12.5 13 15 9.624 14.334 9 12.5 11.476 10.666 9Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.666 9
                moveTo(x = 10.666f, y = 9.0f)
                // L 10 9.624
                lineTo(x = 10.0f, y = 9.624f)
                // L 12.5 13
                lineTo(x = 12.5f, y = 13.0f)
                // L 15 9.624
                lineTo(x = 15.0f, y = 9.624f)
                // L 14.334 9
                lineTo(x = 14.334f, y = 9.0f)
                // L 12.5 11.476
                lineTo(x = 12.5f, y = 11.476f)
                // L 10.666 9z
                lineTo(x = 10.666f, y = 9.0f)
                close()
            }
        }.build().also { _ic1701 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1701: ImageVector? = null
