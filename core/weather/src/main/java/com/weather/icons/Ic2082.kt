package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2082: ImageVector
    get() {
        val current = _ic2082
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2082",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // <rect width="6" height="1" rx="0.6" fill="currentColor" />
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 16 5.1
                moveTo(x = 16.0f, y = 5.1f)
                // a 0.6 0.6 0 0 0 -0.6 -0.6
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.6f,
                    dy1 = -0.6f,
                )
                // l -4.8 0
                lineToRelative(dx = -4.8f, dy = 0.0f)
                // a 0.6 0.6 0 0 0 -0.6 0.6
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.6f,
                    dy1 = 0.6f,
                )
                // l -0 -0.2
                lineToRelative(dx = -0.0f, dy = -0.2f)
                // a 0.6 0.6 0 0 0 0.6 0.6
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.6f,
                    dy1 = 0.6f,
                )
                // l 4.8 0
                lineToRelative(dx = 4.8f, dy = 0.0f)
                // a 0.6 0.6 0 0 0 0.6 -0.6z
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.6f,
                    dy1 = -0.6f,
                )
                close()
            }
            // M4.5 7 a.5 .5 0 0 1 .5 .5 v2.063 a2 2 0 1 1 -1 0 V7.5 a.5 .5 0 0 1 .5 -.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.5 7
                moveTo(x = 4.5f, y = 7.0f)
                // a 0.5 0.5 0 0 1 0.5 0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = 0.5f,
                )
                // v 2.063
                verticalLineToRelative(dy = 2.063f)
                // a 2 2 0 1 1 -1 0
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // V 7.5
                verticalLineTo(y = 7.5f)
                // a 0.5 0.5 0 0 1 0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = -0.5f,
                )
                close()
            }
            // m5.8 8.399 .532 .356 a3.3 3.3 0 1 1 -3.665 0 l.533 -.356 V2.5 a1.3 1.3 0 0 1 2.6 0 v5.899Z M7 2.5 a2.5 2.5 0 0 0 -5 0 v5.258 a4.5 4.5 0 1 0 5 0 V2.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5.8 8.399
                moveTo(x = 5.8f, y = 8.399f)
                // l 0.532 0.356
                lineToRelative(dx = 0.532f, dy = 0.356f)
                // a 3.3 3.3 0 1 1 -3.665 0
                arcToRelative(
                    a = 3.3f,
                    b = 3.3f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -3.665f,
                    dy1 = 0.0f,
                )
                // l 0.533 -0.356
                lineToRelative(dx = 0.533f, dy = -0.356f)
                // V 2.5
                verticalLineTo(y = 2.5f)
                // a 1.3 1.3 0 0 1 2.6 0
                arcToRelative(
                    a = 1.3f,
                    b = 1.3f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.6f,
                    dy1 = 0.0f,
                )
                // v 5.899z
                verticalLineToRelative(dy = 5.899f)
                close()
                // M 7 2.5
                moveTo(x = 7.0f, y = 2.5f)
                // a 2.5 2.5 0 0 0 -5 0
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -5.0f,
                    dy1 = 0.0f,
                )
                // v 5.258
                verticalLineToRelative(dy = 5.258f)
                // a 4.5 4.5 0 1 0 5 0
                arcToRelative(
                    a = 4.5f,
                    b = 4.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 5.0f,
                    dy1 = 0.0f,
                )
                // V 2.5z
                verticalLineTo(y = 2.5f)
                close()
            }
        }.build().also { _ic2082 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2082: ImageVector? = null
