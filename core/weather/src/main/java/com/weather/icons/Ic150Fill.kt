package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic150Fill: ImageVector
    get() {
        val current = _ic150Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic150Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M4.733 .059 c.46 -.226 .925 .238 .811 .732 C4.94 3.424 4.984 6.384 7 8.5 c2.017 2.116 5.529 2.888 8.234 2.458 .507 -.08 .948 .405 .69 .844 a8.432 8.432 0 0 1 -1.547 1.919 C10.94 16.9 5.54 16.733 2.313 13.347 a8.323 8.323 0 0 1 .38 -11.887 A8.538 8.538 0 0 1 4.732 .06Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.733 0.059
                moveTo(x = 4.733f, y = 0.059f)
                // c 0.46 -0.226 0.925 0.238 0.811 0.732
                curveToRelative(
                    dx1 = 0.46f,
                    dy1 = -0.226f,
                    dx2 = 0.925f,
                    dy2 = 0.238f,
                    dx3 = 0.811f,
                    dy3 = 0.732f,
                )
                // C 4.94 3.424 4.984 6.384 7 8.5
                curveTo(
                    x1 = 4.94f,
                    y1 = 3.424f,
                    x2 = 4.984f,
                    y2 = 6.384f,
                    x3 = 7.0f,
                    y3 = 8.5f,
                )
                // c 2.017 2.116 5.529 2.888 8.234 2.458
                curveToRelative(
                    dx1 = 2.017f,
                    dy1 = 2.116f,
                    dx2 = 5.529f,
                    dy2 = 2.888f,
                    dx3 = 8.234f,
                    dy3 = 2.458f,
                )
                // c 0.507 -0.08 0.948 0.405 0.69 0.844
                curveToRelative(
                    dx1 = 0.507f,
                    dy1 = -0.08f,
                    dx2 = 0.948f,
                    dy2 = 0.405f,
                    dx3 = 0.69f,
                    dy3 = 0.844f,
                )
                // a 8.432 8.432 0 0 1 -1.547 1.919
                arcToRelative(
                    a = 8.432f,
                    b = 8.432f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.547f,
                    dy1 = 1.919f,
                )
                // C 10.94 16.9 5.54 16.733 2.313 13.347
                curveTo(
                    x1 = 10.94f,
                    y1 = 16.9f,
                    x2 = 5.54f,
                    y2 = 16.733f,
                    x3 = 2.313f,
                    y3 = 13.347f,
                )
                // a 8.323 8.323 0 0 1 0.38 -11.887
                arcToRelative(
                    a = 8.323f,
                    b = 8.323f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.38f,
                    dy1 = -11.887f,
                )
                // A 8.538 8.538 0 0 1 4.732 0.06z
                arcTo(
                    horizontalEllipseRadius = 8.538f,
                    verticalEllipseRadius = 8.538f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 4.732f,
                    y1 = 0.06f,
                )
                close()
            }
        }.build().also { _ic150Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic150Fill: ImageVector? = null
