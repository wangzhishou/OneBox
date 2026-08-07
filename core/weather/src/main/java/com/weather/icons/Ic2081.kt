package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2081: ImageVector
    get() {
        val current = _ic2081
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2081",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M13 2 a.6 .6 0 0 1 .6 .6 v1.8 h1.8 a.6 .6 0 1 1 0 1.2 h-1.8 v1.8 a.6 .6 0 1 1 -1.2 0 V5.6 h-1.8 a.6 .6 0 1 1 0 -1.2 h1.8 V2.6 A.6 .6 0 0 1 13 2Z M4.5 3 a.5 .5 0 0 1 .5 .5 v6.063 a2 2 0 1 1 -1 0 V3.5 a.5 .5 0 0 1 .5 -.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 13 2
                moveTo(x = 13.0f, y = 2.0f)
                // a 0.6 0.6 0 0 1 0.6 0.6
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.6f,
                    dy1 = 0.6f,
                )
                // v 1.8
                verticalLineToRelative(dy = 1.8f)
                // h 1.8
                horizontalLineToRelative(dx = 1.8f)
                // a 0.6 0.6 0 1 1 0 1.2
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.2f,
                )
                // h -1.8
                horizontalLineToRelative(dx = -1.8f)
                // v 1.8
                verticalLineToRelative(dy = 1.8f)
                // a 0.6 0.6 0 1 1 -1.2 0
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.2f,
                    dy1 = 0.0f,
                )
                // V 5.6
                verticalLineTo(y = 5.6f)
                // h -1.8
                horizontalLineToRelative(dx = -1.8f)
                // a 0.6 0.6 0 1 1 0 -1.2
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.2f,
                )
                // h 1.8
                horizontalLineToRelative(dx = 1.8f)
                // V 2.6
                verticalLineTo(y = 2.6f)
                // A 0.6 0.6 0 0 1 13 2z
                arcTo(
                    horizontalEllipseRadius = 0.6f,
                    verticalEllipseRadius = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 13.0f,
                    y1 = 2.0f,
                )
                close()
                // M 4.5 3
                moveTo(x = 4.5f, y = 3.0f)
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
                // v 6.063
                verticalLineToRelative(dy = 6.063f)
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
                // V 3.5
                verticalLineTo(y = 3.5f)
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
        }.build().also { _ic2081 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2081: ImageVector? = null
