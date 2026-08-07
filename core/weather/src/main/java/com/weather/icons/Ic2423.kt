package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2423: ImageVector
    get() {
        val current = _ic2423
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2423",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M4.6 11.25 h1.245 V5.873 h1.6 V4.75 H3 v1.123 h1.6 v5.377Z m4.605 -6.5 H7.863 l1.946 3.94 v2.56 h1.245 V8.69 L13 4.75 h-1.342 l-1.229 2.612 L9.205 4.75Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.6 11.25
                moveTo(x = 4.6f, y = 11.25f)
                // h 1.245
                horizontalLineToRelative(dx = 1.245f)
                // V 5.873
                verticalLineTo(y = 5.873f)
                // h 1.6
                horizontalLineToRelative(dx = 1.6f)
                // V 4.75
                verticalLineTo(y = 4.75f)
                // H 3
                horizontalLineTo(x = 3.0f)
                // v 1.123
                verticalLineToRelative(dy = 1.123f)
                // h 1.6
                horizontalLineToRelative(dx = 1.6f)
                // v 5.377z
                verticalLineToRelative(dy = 5.377f)
                close()
                // m 4.605 -6.5
                moveToRelative(dx = 4.605f, dy = -6.5f)
                // H 7.863
                horizontalLineTo(x = 7.863f)
                // l 1.946 3.94
                lineToRelative(dx = 1.946f, dy = 3.94f)
                // v 2.56
                verticalLineToRelative(dy = 2.56f)
                // h 1.245
                horizontalLineToRelative(dx = 1.245f)
                // V 8.69
                verticalLineTo(y = 8.69f)
                // L 13 4.75
                lineTo(x = 13.0f, y = 4.75f)
                // h -1.342
                horizontalLineToRelative(dx = -1.342f)
                // l -1.229 2.612
                lineToRelative(dx = -1.229f, dy = 2.612f)
                // L 9.205 4.75z
                lineTo(x = 9.205f, y = 4.75f)
                close()
            }
            // M0 8 a8 8 0 1 0 16 0 A8 8 0 0 0 0 8Z m14.7 0 A6.7 6.7 0 1 1 1.3 8 a6.7 6.7 0 0 1 13.4 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0 8
                moveTo(x = 0.0f, y = 8.0f)
                // a 8 8 0 1 0 16 0
                arcToRelative(
                    a = 8.0f,
                    b = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 16.0f,
                    dy1 = 0.0f,
                )
                // A 8 8 0 0 0 0 8z
                arcTo(
                    horizontalEllipseRadius = 8.0f,
                    verticalEllipseRadius = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 0.0f,
                    y1 = 8.0f,
                )
                close()
                // m 14.7 0
                moveToRelative(dx = 14.7f, dy = 0.0f)
                // A 6.7 6.7 0 1 1 1.3 8
                arcTo(
                    horizontalEllipseRadius = 6.7f,
                    verticalEllipseRadius = 6.7f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 1.3f,
                    y1 = 8.0f,
                )
                // a 6.7 6.7 0 0 1 13.4 0z
                arcToRelative(
                    a = 6.7f,
                    b = 6.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 13.4f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic2423 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2423: ImageVector? = null
