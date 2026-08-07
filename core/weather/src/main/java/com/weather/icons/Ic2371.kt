package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2371: ImageVector
    get() {
        val current = _ic2371
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2371",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8 16 A8 8 0 1 1 8 0 a8 8 0 0 1 0 16Z m0 -1.3 A6.7 6.7 0 1 0 8 1.3 a6.7 6.7 0 0 0 0 13.4Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8 16
                moveTo(x = 8.0f, y = 16.0f)
                // A 8 8 0 1 1 8 0
                arcTo(
                    horizontalEllipseRadius = 8.0f,
                    verticalEllipseRadius = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 8.0f,
                    y1 = 0.0f,
                )
                // a 8 8 0 0 1 0 16z
                arcToRelative(
                    a = 8.0f,
                    b = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 16.0f,
                )
                close()
                // m 0 -1.3
                moveToRelative(dx = 0.0f, dy = -1.3f)
                // A 6.7 6.7 0 1 0 8 1.3
                arcTo(
                    horizontalEllipseRadius = 6.7f,
                    verticalEllipseRadius = 6.7f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    x1 = 8.0f,
                    y1 = 1.3f,
                )
                // a 6.7 6.7 0 0 0 0 13.4z
                arcToRelative(
                    a = 6.7f,
                    b = 6.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 13.4f,
                )
                close()
            }
            // M7.408 3.5 h3.378 c.164 0 .264 .195 .174 .342 L9.1 6.25 a.108 .108 0 0 0 .032 .145 c.015 .01 .032 .014 .05 .014 h1.956 c.26 0 .394 .332 .216 .535 L5.572 13.5 6.95 8.449 a.11 .11 0 0 0 -.016 -.091 .1 .1 0 0 0 -.034 -.03 .092 .092 0 0 0 -.043 -.012 h-2 a.282 .282 0 0 1 -.147 -.041 .307 .307 0 0 1 -.109 -.115 .333 .333 0 0 1 -.004 -.314 l2.501 -4.151 a.352 .352 0 0 1 .31 -.195Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.408 3.5
                moveTo(x = 7.408f, y = 3.5f)
                // h 3.378
                horizontalLineToRelative(dx = 3.378f)
                // c 0.164 0 0.264 0.195 0.174 0.342
                curveToRelative(
                    dx1 = 0.164f,
                    dy1 = 0.0f,
                    dx2 = 0.264f,
                    dy2 = 0.195f,
                    dx3 = 0.174f,
                    dy3 = 0.342f,
                )
                // L 9.1 6.25
                lineTo(x = 9.1f, y = 6.25f)
                // a 0.108 0.108 0 0 0 0.032 0.145
                arcToRelative(
                    a = 0.108f,
                    b = 0.108f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.032f,
                    dy1 = 0.145f,
                )
                // c 0.015 0.01 0.032 0.014 0.05 0.014
                curveToRelative(
                    dx1 = 0.015f,
                    dy1 = 0.01f,
                    dx2 = 0.032f,
                    dy2 = 0.014f,
                    dx3 = 0.05f,
                    dy3 = 0.014f,
                )
                // h 1.956
                horizontalLineToRelative(dx = 1.956f)
                // c 0.26 0 0.394 0.332 0.216 0.535
                curveToRelative(
                    dx1 = 0.26f,
                    dy1 = 0.0f,
                    dx2 = 0.394f,
                    dy2 = 0.332f,
                    dx3 = 0.216f,
                    dy3 = 0.535f,
                )
                // L 5.572 13.5
                lineTo(x = 5.572f, y = 13.5f)
                // L 6.95 8.449
                lineTo(x = 6.95f, y = 8.449f)
                // a 0.11 0.11 0 0 0 -0.016 -0.091
                arcToRelative(
                    a = 0.11f,
                    b = 0.11f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.016f,
                    dy1 = -0.091f,
                )
                // a 0.1 0.1 0 0 0 -0.034 -0.03
                arcToRelative(
                    a = 0.1f,
                    b = 0.1f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.034f,
                    dy1 = -0.03f,
                )
                // a 0.092 0.092 0 0 0 -0.043 -0.012
                arcToRelative(
                    a = 0.092f,
                    b = 0.092f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.043f,
                    dy1 = -0.012f,
                )
                // h -2
                horizontalLineToRelative(dx = -2.0f)
                // a 0.282 0.282 0 0 1 -0.147 -0.041
                arcToRelative(
                    a = 0.282f,
                    b = 0.282f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.147f,
                    dy1 = -0.041f,
                )
                // a 0.307 0.307 0 0 1 -0.109 -0.115
                arcToRelative(
                    a = 0.307f,
                    b = 0.307f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.109f,
                    dy1 = -0.115f,
                )
                // a 0.333 0.333 0 0 1 -0.004 -0.314
                arcToRelative(
                    a = 0.333f,
                    b = 0.333f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.004f,
                    dy1 = -0.314f,
                )
                // l 2.501 -4.151
                lineToRelative(dx = 2.501f, dy = -4.151f)
                // a 0.352 0.352 0 0 1 0.31 -0.195z
                arcToRelative(
                    a = 0.352f,
                    b = 0.352f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.31f,
                    dy1 = -0.195f,
                )
                close()
            }
        }.build().also { _ic2371 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2371: ImageVector? = null
