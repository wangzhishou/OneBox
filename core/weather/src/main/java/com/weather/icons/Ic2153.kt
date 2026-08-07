package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2153: ImageVector
    get() {
        val current = _ic2153
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2153",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M2.88 .721 8.651 0 l3.662 6.104 -2.039 1.02 2.658 3.188 -1.269 .634 L13.107 16 6.37 10.012 l2.059 -1.03 -4.376 -3.28 3.271 -1.09 L2.88 .721Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 2.88 0.721
                moveTo(x = 2.88f, y = 0.721f)
                // L 8.651 0
                lineTo(x = 8.651f, y = 0.0f)
                // l 3.662 6.104
                lineToRelative(dx = 3.662f, dy = 6.104f)
                // l -2.039 1.02
                lineToRelative(dx = -2.039f, dy = 1.02f)
                // l 2.658 3.188
                lineToRelative(dx = 2.658f, dy = 3.188f)
                // l -1.269 0.634
                lineToRelative(dx = -1.269f, dy = 0.634f)
                // L 13.107 16
                lineTo(x = 13.107f, y = 16.0f)
                // L 6.37 10.012
                lineTo(x = 6.37f, y = 10.012f)
                // l 2.059 -1.03
                lineToRelative(dx = 2.059f, dy = -1.03f)
                // l -4.376 -3.28
                lineToRelative(dx = -4.376f, dy = -3.28f)
                // l 3.271 -1.09
                lineToRelative(dx = 3.271f, dy = -1.09f)
                // L 2.88 0.721z
                lineTo(x = 2.88f, y = 0.721f)
                close()
            }
        }.build().also { _ic2153 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2153: ImageVector? = null
