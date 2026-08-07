package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1710: ImageVector
    get() {
        val current = _ic1710
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1710",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // m12.521 1.77 -3.63 3.631 1.833 3.664 4.895 -.98 -.13 1.558 -2.647 .882 -.113 .037 -.082 .085 L10 14 l2.5 -1.793 L13 16 H0 V6.5 L1 4 l2.584 -.827 1.83 3.773 -1.271 1.697 -2.18 1.634 L3 14 l.036 -3.278 1.47 -1.103 2.955 2.111 L7.5 15 l1.038 -3.73 -3.331 -2.38 L6.4 7.3 l.185 -.247 -.138 -.277 L4.65 2.832 10.5 .962 l2.021 .809Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 12.521 1.77
                moveTo(x = 12.521f, y = 1.77f)
                // l -3.63 3.631
                lineToRelative(dx = -3.63f, dy = 3.631f)
                // l 1.833 3.664
                lineToRelative(dx = 1.833f, dy = 3.664f)
                // l 4.895 -0.98
                lineToRelative(dx = 4.895f, dy = -0.98f)
                // l -0.13 1.558
                lineToRelative(dx = -0.13f, dy = 1.558f)
                // l -2.647 0.882
                lineToRelative(dx = -2.647f, dy = 0.882f)
                // l -0.113 0.037
                lineToRelative(dx = -0.113f, dy = 0.037f)
                // l -0.082 0.085
                lineToRelative(dx = -0.082f, dy = 0.085f)
                // L 10 14
                lineTo(x = 10.0f, y = 14.0f)
                // l 2.5 -1.793
                lineToRelative(dx = 2.5f, dy = -1.793f)
                // L 13 16
                lineTo(x = 13.0f, y = 16.0f)
                // H 0
                horizontalLineTo(x = 0.0f)
                // V 6.5
                verticalLineTo(y = 6.5f)
                // L 1 4
                lineTo(x = 1.0f, y = 4.0f)
                // l 2.584 -0.827
                lineToRelative(dx = 2.584f, dy = -0.827f)
                // l 1.83 3.773
                lineToRelative(dx = 1.83f, dy = 3.773f)
                // l -1.271 1.697
                lineToRelative(dx = -1.271f, dy = 1.697f)
                // l -2.18 1.634
                lineToRelative(dx = -2.18f, dy = 1.634f)
                // L 3 14
                lineTo(x = 3.0f, y = 14.0f)
                // l 0.036 -3.278
                lineToRelative(dx = 0.036f, dy = -3.278f)
                // l 1.47 -1.103
                lineToRelative(dx = 1.47f, dy = -1.103f)
                // l 2.955 2.111
                lineToRelative(dx = 2.955f, dy = 2.111f)
                // L 7.5 15
                lineTo(x = 7.5f, y = 15.0f)
                // l 1.038 -3.73
                lineToRelative(dx = 1.038f, dy = -3.73f)
                // l -3.331 -2.38
                lineToRelative(dx = -3.331f, dy = -2.38f)
                // L 6.4 7.3
                lineTo(x = 6.4f, y = 7.3f)
                // l 0.185 -0.247
                lineToRelative(dx = 0.185f, dy = -0.247f)
                // l -0.138 -0.277
                lineToRelative(dx = -0.138f, dy = -0.277f)
                // L 4.65 2.832
                lineTo(x = 4.65f, y = 2.832f)
                // L 10.5 0.962
                lineTo(x = 10.5f, y = 0.962f)
                // l 2.021 0.809z
                lineToRelative(dx = 2.021f, dy = 0.809f)
                close()
            }
            // M14.96 16 H13 l.5 -4.64 1.9 -.633 L14.96 16Z m1.07 -12.827 -.325 3.876 -4.429 .886 -1.168 -2.336 3.424 -3.425 2.497 .999Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 14.96 16
                moveTo(x = 14.96f, y = 16.0f)
                // H 13
                horizontalLineTo(x = 13.0f)
                // l 0.5 -4.64
                lineToRelative(dx = 0.5f, dy = -4.64f)
                // l 1.9 -0.633
                lineToRelative(dx = 1.9f, dy = -0.633f)
                // L 14.96 16z
                lineTo(x = 14.96f, y = 16.0f)
                close()
                // m 1.07 -12.827
                moveToRelative(dx = 1.07f, dy = -12.827f)
                // l -0.325 3.876
                lineToRelative(dx = -0.325f, dy = 3.876f)
                // l -4.429 0.886
                lineToRelative(dx = -4.429f, dy = 0.886f)
                // l -1.168 -2.336
                lineToRelative(dx = -1.168f, dy = -2.336f)
                // l 3.424 -3.425
                lineToRelative(dx = 3.424f, dy = -3.425f)
                // l 2.497 0.999z
                lineToRelative(dx = 2.497f, dy = 0.999f)
                close()
            }
        }.build().also { _ic1710 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1710: ImageVector? = null
