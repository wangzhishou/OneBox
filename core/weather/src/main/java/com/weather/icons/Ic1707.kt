package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1707: ImageVector
    get() {
        val current = _ic1707
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1707",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M4.5 3 a.5 .5 0 0 0 -.5 .5 v6.063 a2 2 0 1 0 1 0 V3.5 a.5 .5 0 0 0 -.5 -.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.5 3
                moveTo(x = 4.5f, y = 3.0f)
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
                // v 6.063
                verticalLineToRelative(dy = 6.063f)
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
                // V 3.5
                verticalLineTo(y = 3.5f)
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
            // m3.2 8.399 -.532 .356 a3.3 3.3 0 1 0 3.665 0 L5.8 8.399 V2.5 a1.3 1.3 0 0 0 -2.6 0 v5.899Z M2 2.5 a2.5 2.5 0 0 1 5 0 v5.258 a4.5 4.5 0 1 1 -5 0 V2.5Z m8 9.876 .666 .624 1.834 -2.476 L14.334 13 l.666 -.624 L12.5 9 10 12.376Z
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
                // m 8 9.876
                moveToRelative(dx = 8.0f, dy = 9.876f)
                // l 0.666 0.624
                lineToRelative(dx = 0.666f, dy = 0.624f)
                // l 1.834 -2.476
                lineToRelative(dx = 1.834f, dy = -2.476f)
                // L 14.334 13
                lineTo(x = 14.334f, y = 13.0f)
                // l 0.666 -0.624
                lineToRelative(dx = 0.666f, dy = -0.624f)
                // L 12.5 9
                lineTo(x = 12.5f, y = 9.0f)
                // L 10 12.376z
                lineTo(x = 10.0f, y = 12.376f)
                close()
            }
            // m10 9.376 .666 .624 L12.5 7.524 14.334 10 15 9.376 12.5 6 10 9.376Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10 9.376
                moveTo(x = 10.0f, y = 9.376f)
                // l 0.666 0.624
                lineToRelative(dx = 0.666f, dy = 0.624f)
                // L 12.5 7.524
                lineTo(x = 12.5f, y = 7.524f)
                // L 14.334 10
                lineTo(x = 14.334f, y = 10.0f)
                // L 15 9.376
                lineTo(x = 15.0f, y = 9.376f)
                // L 12.5 6
                lineTo(x = 12.5f, y = 6.0f)
                // L 10 9.376z
                lineTo(x = 10.0f, y = 9.376f)
                close()
            }
            // M10.666 7 10 6.376 12.5 3 15 6.376 14.334 7 12.5 4.524 10.666 7Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.666 7
                moveTo(x = 10.666f, y = 7.0f)
                // L 10 6.376
                lineTo(x = 10.0f, y = 6.376f)
                // L 12.5 3
                lineTo(x = 12.5f, y = 3.0f)
                // L 15 6.376
                lineTo(x = 15.0f, y = 6.376f)
                // L 14.334 7
                lineTo(x = 14.334f, y = 7.0f)
                // L 12.5 4.524
                lineTo(x = 12.5f, y = 4.524f)
                // L 10.666 7z
                lineTo(x = 10.666f, y = 7.0f)
                close()
            }
        }.build().also { _ic1707 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1707: ImageVector? = null
