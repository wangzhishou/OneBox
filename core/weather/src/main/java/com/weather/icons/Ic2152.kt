package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2152: ImageVector
    get() {
        val current = _ic2152
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2152",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M13 6.5 H3 v3 h10 v-3Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 13 6.5
                moveTo(x = 13.0f, y = 6.5f)
                // H 3
                horizontalLineTo(x = 3.0f)
                // v 3
                verticalLineToRelative(dy = 3.0f)
                // h 10
                horizontalLineToRelative(dx = 10.0f)
                // v -3z
                verticalLineToRelative(dy = -3.0f)
                close()
            }
            // m10.384 .455 5.14 5.154 a.705 .705 0 0 1 .182 .68 l-1.889 7.047 a.703 .703 0 0 1 -.497 .497 l-7.028 1.893 a.686 .686 0 0 1 -.677 -.181 l-5.14 -5.154 a.705 .705 0 0 1 -.18 -.679 l1.888 -7.047 a.705 .705 0 0 1 .496 -.498 L9.707 .274 a.693 .693 0 0 1 .677 .181Z M6.322 14.263 l6.245 -1.683 1.678 -6.263 -4.567 -4.58 -6.245 1.684 -1.678 6.262 4.567 4.58Z
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
                // a 0.703 0.703 0 0 1 -0.497 0.497
                arcToRelative(
                    a = 0.703f,
                    b = 0.703f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.497f,
                    dy1 = 0.497f,
                )
                // l -7.028 1.893
                lineToRelative(dx = -7.028f, dy = 1.893f)
                // a 0.686 0.686 0 0 1 -0.677 -0.181
                arcToRelative(
                    a = 0.686f,
                    b = 0.686f,
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
        }.build().also { _ic2152 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2152: ImageVector? = null
