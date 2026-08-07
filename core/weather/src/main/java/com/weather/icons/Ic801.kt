package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic801: ImageVector
    get() {
        val current = _ic801
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic801",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8 0 c-.265 0 -.53 .013 -.795 .04 a7.985 7.985 0 0 0 -.63 .094 c-.044 .008 -.088 .011 -.13 .02 a7.998 7.998 0 0 0 0 15.692 c.042 .009 .086 .012 .13 .02 .207 .037 .416 .073 .63 .094 A8 8 0 1 0 8 0Z m0 15.5 a7.46 7.46 0 0 1 -1.668 -.188 8.496 8.496 0 0 0 0 -14.623 A7.5 7.5 0 1 1 8 15.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8 0
                moveTo(x = 8.0f, y = 0.0f)
                // c -0.265 0 -0.53 0.013 -0.795 0.04
                curveToRelative(
                    dx1 = -0.265f,
                    dy1 = 0.0f,
                    dx2 = -0.53f,
                    dy2 = 0.013f,
                    dx3 = -0.795f,
                    dy3 = 0.04f,
                )
                // a 7.985 7.985 0 0 0 -0.63 0.094
                arcToRelative(
                    a = 7.985f,
                    b = 7.985f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.63f,
                    dy1 = 0.094f,
                )
                // c -0.044 0.008 -0.088 0.011 -0.13 0.02
                curveToRelative(
                    dx1 = -0.044f,
                    dy1 = 0.008f,
                    dx2 = -0.088f,
                    dy2 = 0.011f,
                    dx3 = -0.13f,
                    dy3 = 0.02f,
                )
                // a 7.998 7.998 0 0 0 0 15.692
                arcToRelative(
                    a = 7.998f,
                    b = 7.998f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 15.692f,
                )
                // c 0.042 0.009 0.086 0.012 0.13 0.02
                curveToRelative(
                    dx1 = 0.042f,
                    dy1 = 0.009f,
                    dx2 = 0.086f,
                    dy2 = 0.012f,
                    dx3 = 0.13f,
                    dy3 = 0.02f,
                )
                // c 0.207 0.037 0.416 0.073 0.63 0.094
                curveToRelative(
                    dx1 = 0.207f,
                    dy1 = 0.037f,
                    dx2 = 0.416f,
                    dy2 = 0.073f,
                    dx3 = 0.63f,
                    dy3 = 0.094f,
                )
                // A 8 8 0 1 0 8 0z
                arcTo(
                    horizontalEllipseRadius = 8.0f,
                    verticalEllipseRadius = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    x1 = 8.0f,
                    y1 = 0.0f,
                )
                close()
                // m 0 15.5
                moveToRelative(dx = 0.0f, dy = 15.5f)
                // a 7.46 7.46 0 0 1 -1.668 -0.188
                arcToRelative(
                    a = 7.46f,
                    b = 7.46f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.668f,
                    dy1 = -0.188f,
                )
                // a 8.496 8.496 0 0 0 0 -14.623
                arcToRelative(
                    a = 8.496f,
                    b = 8.496f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -14.623f,
                )
                // A 7.5 7.5 0 1 1 8 15.5z
                arcTo(
                    horizontalEllipseRadius = 7.5f,
                    verticalEllipseRadius = 7.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 8.0f,
                    y1 = 15.5f,
                )
                close()
            }
        }.build().also { _ic801 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic801: ImageVector? = null
