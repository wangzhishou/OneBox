package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic807: ImageVector
    get() {
        val current = _ic807
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic807",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M9.555 .154 C9.513 .145 9.47 .142 9.426 .134 A7.983 7.983 0 0 0 8.795 .04 a8 8 0 1 0 0 15.92 c.214 -.021 .423 -.057 .631 -.094 .043 -.008 .087 -.011 .13 -.02 a7.998 7.998 0 0 0 0 -15.692 h-.001Z m.113 15.158 a7.499 7.499 0 0 1 -7.26 -12.31 A7.5 7.5 0 0 1 9.667 .689 a8.497 8.497 0 0 0 0 14.623Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 9.555 0.154
                moveTo(x = 9.555f, y = 0.154f)
                // C 9.513 0.145 9.47 0.142 9.426 0.134
                curveTo(
                    x1 = 9.513f,
                    y1 = 0.145f,
                    x2 = 9.47f,
                    y2 = 0.142f,
                    x3 = 9.426f,
                    y3 = 0.134f,
                )
                // A 7.983 7.983 0 0 0 8.795 0.04
                arcTo(
                    horizontalEllipseRadius = 7.983f,
                    verticalEllipseRadius = 7.983f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 8.795f,
                    y1 = 0.04f,
                )
                // a 8 8 0 1 0 0 15.92
                arcToRelative(
                    a = 8.0f,
                    b = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 15.92f,
                )
                // c 0.214 -0.021 0.423 -0.057 0.631 -0.094
                curveToRelative(
                    dx1 = 0.214f,
                    dy1 = -0.021f,
                    dx2 = 0.423f,
                    dy2 = -0.057f,
                    dx3 = 0.631f,
                    dy3 = -0.094f,
                )
                // c 0.043 -0.008 0.087 -0.011 0.13 -0.02
                curveToRelative(
                    dx1 = 0.043f,
                    dy1 = -0.008f,
                    dx2 = 0.087f,
                    dy2 = -0.011f,
                    dx3 = 0.13f,
                    dy3 = -0.02f,
                )
                // a 7.998 7.998 0 0 0 0 -15.692
                arcToRelative(
                    a = 7.998f,
                    b = 7.998f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -15.692f,
                )
                // h -0.001z
                horizontalLineToRelative(dx = -0.001f)
                close()
                // m 0.113 15.158
                moveToRelative(dx = 0.113f, dy = 15.158f)
                // a 7.499 7.499 0 0 1 -7.26 -12.31
                arcToRelative(
                    a = 7.499f,
                    b = 7.499f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -7.26f,
                    dy1 = -12.31f,
                )
                // A 7.5 7.5 0 0 1 9.667 0.689
                arcTo(
                    horizontalEllipseRadius = 7.5f,
                    verticalEllipseRadius = 7.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 9.667f,
                    y1 = 0.689f,
                )
                // a 8.497 8.497 0 0 0 0 14.623z
                arcToRelative(
                    a = 8.497f,
                    b = 8.497f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 14.623f,
                )
                close()
            }
        }.build().also { _ic807 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic807: ImageVector? = null
