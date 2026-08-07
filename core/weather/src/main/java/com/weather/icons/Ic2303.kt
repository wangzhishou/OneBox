package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2303: ImageVector
    get() {
        val current = _ic2303
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2303",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.003 4 a.933 .933 0 0 0 -.934 .933 v1.45 l-1.256 -.725 a.933 .933 0 1 0 -.933 1.617 L6.136 8 l-1.256 .725 a.933 .933 0 1 0 .933 1.617 l1.256 -.725 v1.45 a.933 .933 0 0 0 1.867 0 v-1.45 l1.256 .725 a.933 .933 0 1 0 .933 -1.617 L9.87 8 l1.256 -.725 a.933 .933 0 1 0 -.933 -1.617 l-1.256 .725 v-1.45 A.933 .933 0 0 0 8.003 4Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.003 4
                moveTo(x = 8.003f, y = 4.0f)
                // a 0.933 0.933 0 0 0 -0.934 0.933
                arcToRelative(
                    a = 0.933f,
                    b = 0.933f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.934f,
                    dy1 = 0.933f,
                )
                // v 1.45
                verticalLineToRelative(dy = 1.45f)
                // l -1.256 -0.725
                lineToRelative(dx = -1.256f, dy = -0.725f)
                // a 0.933 0.933 0 1 0 -0.933 1.617
                arcToRelative(
                    a = 0.933f,
                    b = 0.933f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.933f,
                    dy1 = 1.617f,
                )
                // L 6.136 8
                lineTo(x = 6.136f, y = 8.0f)
                // l -1.256 0.725
                lineToRelative(dx = -1.256f, dy = 0.725f)
                // a 0.933 0.933 0 1 0 0.933 1.617
                arcToRelative(
                    a = 0.933f,
                    b = 0.933f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.933f,
                    dy1 = 1.617f,
                )
                // l 1.256 -0.725
                lineToRelative(dx = 1.256f, dy = -0.725f)
                // v 1.45
                verticalLineToRelative(dy = 1.45f)
                // a 0.933 0.933 0 0 0 1.867 0
                arcToRelative(
                    a = 0.933f,
                    b = 0.933f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.867f,
                    dy1 = 0.0f,
                )
                // v -1.45
                verticalLineToRelative(dy = -1.45f)
                // l 1.256 0.725
                lineToRelative(dx = 1.256f, dy = 0.725f)
                // a 0.933 0.933 0 1 0 0.933 -1.617
                arcToRelative(
                    a = 0.933f,
                    b = 0.933f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.933f,
                    dy1 = -1.617f,
                )
                // L 9.87 8
                lineTo(x = 9.87f, y = 8.0f)
                // l 1.256 -0.725
                lineToRelative(dx = 1.256f, dy = -0.725f)
                // a 0.933 0.933 0 1 0 -0.933 -1.617
                arcToRelative(
                    a = 0.933f,
                    b = 0.933f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.933f,
                    dy1 = -1.617f,
                )
                // l -1.256 0.725
                lineToRelative(dx = -1.256f, dy = 0.725f)
                // v -1.45
                verticalLineToRelative(dy = -1.45f)
                // A 0.933 0.933 0 0 0 8.003 4z
                arcTo(
                    horizontalEllipseRadius = 0.933f,
                    verticalEllipseRadius = 0.933f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 8.003f,
                    y1 = 4.0f,
                )
                close()
            }
            // m10.384 .455 5.14 5.154 a.705 .705 0 0 1 .182 .68 l-1.889 7.047 a.704 .704 0 0 1 -.497 .497 l-7.028 1.893 a.688 .688 0 0 1 -.677 -.181 l-5.14 -5.154 a.705 .705 0 0 1 -.18 -.679 l1.888 -7.047 a.705 .705 0 0 1 .496 -.498 L9.707 .274 a.693 .693 0 0 1 .677 .181Z M6.322 14.263 l6.245 -1.683 1.678 -6.263 -4.567 -4.58 -6.245 1.684 -1.678 6.262 4.567 4.58Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.384 0.455
                moveTo(x = 10.384f, y = 0.455f)
                // l 5.14 5.154
                lineToRelative(dx = 5.14f, dy = 5.154f)
                // a 0.705 0.705 0 0 1 0.182 0.68
                arcToRelative(
                    a = 0.705f,
                    b = 0.705f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.182f,
                    dy1 = 0.68f,
                )
                // l -1.889 7.047
                lineToRelative(dx = -1.889f, dy = 7.047f)
                // a 0.704 0.704 0 0 1 -0.497 0.497
                arcToRelative(
                    a = 0.704f,
                    b = 0.704f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.497f,
                    dy1 = 0.497f,
                )
                // l -7.028 1.893
                lineToRelative(dx = -7.028f, dy = 1.893f)
                // a 0.688 0.688 0 0 1 -0.677 -0.181
                arcToRelative(
                    a = 0.688f,
                    b = 0.688f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.677f,
                    dy1 = -0.181f,
                )
                // l -5.14 -5.154
                lineToRelative(dx = -5.14f, dy = -5.154f)
                // a 0.705 0.705 0 0 1 -0.18 -0.679
                arcToRelative(
                    a = 0.705f,
                    b = 0.705f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.18f,
                    dy1 = -0.679f,
                )
                // l 1.888 -7.047
                lineToRelative(dx = 1.888f, dy = -7.047f)
                // a 0.705 0.705 0 0 1 0.496 -0.498
                arcToRelative(
                    a = 0.705f,
                    b = 0.705f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.496f,
                    dy1 = -0.498f,
                )
                // L 9.707 0.274
                lineTo(x = 9.707f, y = 0.274f)
                // a 0.693 0.693 0 0 1 0.677 0.181z
                arcToRelative(
                    a = 0.693f,
                    b = 0.693f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.677f,
                    dy1 = 0.181f,
                )
                close()
                // M 6.322 14.263
                moveTo(x = 6.322f, y = 14.263f)
                // l 6.245 -1.683
                lineToRelative(dx = 6.245f, dy = -1.683f)
                // l 1.678 -6.263
                lineToRelative(dx = 1.678f, dy = -6.263f)
                // l -4.567 -4.58
                lineToRelative(dx = -4.567f, dy = -4.58f)
                // l -6.245 1.684
                lineToRelative(dx = -6.245f, dy = 1.684f)
                // l -1.678 6.262
                lineToRelative(dx = -1.678f, dy = 6.262f)
                // l 4.567 4.58z
                lineToRelative(dx = 4.567f, dy = 4.58f)
                close()
            }
        }.build().also { _ic2303 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2303: ImageVector? = null
