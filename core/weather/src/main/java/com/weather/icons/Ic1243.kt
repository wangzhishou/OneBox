package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1243: ImageVector
    get() {
        val current = _ic1243
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1243",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M0 16 8 0 v3.2 l1.263 2.133 L8 6.4 l-.842 3.2 L8 10.667 l-.842 2.666 2.105 1.6 .842 1.067 H0Z M9.684 2.133 l.421 2.134 h1.263 L9.684 2.133Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0 16
                moveTo(x = 0.0f, y = 16.0f)
                // L 8 0
                lineTo(x = 8.0f, y = 0.0f)
                // v 3.2
                verticalLineToRelative(dy = 3.2f)
                // l 1.263 2.133
                lineToRelative(dx = 1.263f, dy = 2.133f)
                // L 8 6.4
                lineTo(x = 8.0f, y = 6.4f)
                // l -0.842 3.2
                lineToRelative(dx = -0.842f, dy = 3.2f)
                // L 8 10.667
                lineTo(x = 8.0f, y = 10.667f)
                // l -0.842 2.666
                lineToRelative(dx = -0.842f, dy = 2.666f)
                // l 2.105 1.6
                lineToRelative(dx = 2.105f, dy = 1.6f)
                // l 0.842 1.067
                lineToRelative(dx = 0.842f, dy = 1.067f)
                // H 0z
                horizontalLineTo(x = 0.0f)
                close()
                // M 9.684 2.133
                moveTo(x = 9.684f, y = 2.133f)
                // l 0.421 2.134
                lineToRelative(dx = 0.421f, dy = 2.134f)
                // h 1.263
                horizontalLineToRelative(dx = 1.263f)
                // L 9.684 2.133z
                lineTo(x = 9.684f, y = 2.133f)
                close()
            }
            // m9.263 8 1.684 -1.067 .421 3.2 L9.263 9.6 V8Z m3.369 2.133 -.842 1.6 1.684 1.067 -.842 -2.667Z m-3.369 3.734 .842 -1.6 2.948 2.133 -2.106 1.067 -1.684 -1.6Z m5.053 .533 .842 -1.067 .42 2.134 -1.262 -1.067Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 9.263 8
                moveTo(x = 9.263f, y = 8.0f)
                // l 1.684 -1.067
                lineToRelative(dx = 1.684f, dy = -1.067f)
                // l 0.421 3.2
                lineToRelative(dx = 0.421f, dy = 3.2f)
                // L 9.263 9.6
                lineTo(x = 9.263f, y = 9.6f)
                // V 8z
                verticalLineTo(y = 8.0f)
                close()
                // m 3.369 2.133
                moveToRelative(dx = 3.369f, dy = 2.133f)
                // l -0.842 1.6
                lineToRelative(dx = -0.842f, dy = 1.6f)
                // l 1.684 1.067
                lineToRelative(dx = 1.684f, dy = 1.067f)
                // l -0.842 -2.667z
                lineToRelative(dx = -0.842f, dy = -2.667f)
                close()
                // m -3.369 3.734
                moveToRelative(dx = -3.369f, dy = 3.734f)
                // l 0.842 -1.6
                lineToRelative(dx = 0.842f, dy = -1.6f)
                // l 2.948 2.133
                lineToRelative(dx = 2.948f, dy = 2.133f)
                // l -2.106 1.067
                lineToRelative(dx = -2.106f, dy = 1.067f)
                // l -1.684 -1.6z
                lineToRelative(dx = -1.684f, dy = -1.6f)
                close()
                // m 5.053 0.533
                moveToRelative(dx = 5.053f, dy = 0.533f)
                // l 0.842 -1.067
                lineToRelative(dx = 0.842f, dy = -1.067f)
                // l 0.42 2.134
                lineToRelative(dx = 0.42f, dy = 2.134f)
                // l -1.262 -1.067z
                lineToRelative(dx = -1.262f, dy = -1.067f)
                close()
            }
        }.build().also { _ic1243 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1243: ImageVector? = null
