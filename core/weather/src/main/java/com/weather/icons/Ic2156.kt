package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2156: ImageVector
    get() {
        val current = _ic2156
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2156",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // m.646 5.707 2 2 L3.354 7 l-2 -2 -.708 .707Z M3 8.354 H0 v1 h3 v-1Z M2.646 10 l-2 2 .708 .707 2 -2 L2.646 10Z m12.711 -4.293 -2 2 L12.65 7 l2 -2 .707 .707Z m-2.353 2.647 h3 v1 h-3 v-1Z M13.357 10 l2 2 -.707 .707 -2 -2 .707 -.707Z M8 7 a.5 .5 0 0 0 -.5 .5 v2.063 a2 2 0 1 0 1 0 V7.5 A.5 .5 0 0 0 8 7Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0.646 5.707
                moveTo(x = 0.646f, y = 5.707f)
                // l 2 2
                lineToRelative(dx = 2.0f, dy = 2.0f)
                // L 3.354 7
                lineTo(x = 3.354f, y = 7.0f)
                // l -2 -2
                lineToRelative(dx = -2.0f, dy = -2.0f)
                // l -0.708 0.707z
                lineToRelative(dx = -0.708f, dy = 0.707f)
                close()
                // M 3 8.354
                moveTo(x = 3.0f, y = 8.354f)
                // H 0
                horizontalLineTo(x = 0.0f)
                // v 1
                verticalLineToRelative(dy = 1.0f)
                // h 3
                horizontalLineToRelative(dx = 3.0f)
                // v -1z
                verticalLineToRelative(dy = -1.0f)
                close()
                // M 2.646 10
                moveTo(x = 2.646f, y = 10.0f)
                // l -2 2
                lineToRelative(dx = -2.0f, dy = 2.0f)
                // l 0.708 0.707
                lineToRelative(dx = 0.708f, dy = 0.707f)
                // l 2 -2
                lineToRelative(dx = 2.0f, dy = -2.0f)
                // L 2.646 10z
                lineTo(x = 2.646f, y = 10.0f)
                close()
                // m 12.711 -4.293
                moveToRelative(dx = 12.711f, dy = -4.293f)
                // l -2 2
                lineToRelative(dx = -2.0f, dy = 2.0f)
                // L 12.65 7
                lineTo(x = 12.65f, y = 7.0f)
                // l 2 -2
                lineToRelative(dx = 2.0f, dy = -2.0f)
                // l 0.707 0.707z
                lineToRelative(dx = 0.707f, dy = 0.707f)
                close()
                // m -2.353 2.647
                moveToRelative(dx = -2.353f, dy = 2.647f)
                // h 3
                horizontalLineToRelative(dx = 3.0f)
                // v 1
                verticalLineToRelative(dy = 1.0f)
                // h -3
                horizontalLineToRelative(dx = -3.0f)
                // v -1z
                verticalLineToRelative(dy = -1.0f)
                close()
                // M 13.357 10
                moveTo(x = 13.357f, y = 10.0f)
                // l 2 2
                lineToRelative(dx = 2.0f, dy = 2.0f)
                // l -0.707 0.707
                lineToRelative(dx = -0.707f, dy = 0.707f)
                // l -2 -2
                lineToRelative(dx = -2.0f, dy = -2.0f)
                // l 0.707 -0.707z
                lineToRelative(dx = 0.707f, dy = -0.707f)
                close()
                // M 8 7
                moveTo(x = 8.0f, y = 7.0f)
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
                // A 0.5 0.5 0 0 0 8 7z
                arcTo(
                    horizontalEllipseRadius = 0.5f,
                    verticalEllipseRadius = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 8.0f,
                    y1 = 7.0f,
                )
                close()
            }
            // m6.7 8.399 -.532 .356 a3.3 3.3 0 1 0 3.665 0 L9.3 8.399 V2.5 a1.3 1.3 0 0 0 -2.6 0 v5.899Z M5.5 2.5 a2.5 2.5 0 0 1 5 0 v5.258 a4.5 4.5 0 1 1 -5 0 V2.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6.7 8.399
                moveTo(x = 6.7f, y = 8.399f)
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
                // L 9.3 8.399
                lineTo(x = 9.3f, y = 8.399f)
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
                // M 5.5 2.5
                moveTo(x = 5.5f, y = 2.5f)
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
            }
        }.build().also { _ic2156 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2156: ImageVector? = null
