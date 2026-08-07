package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1042: ImageVector
    get() {
        val current = _ic1042
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1042",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M6.5 4 h3 v2.5 H12 v3 H9.5 V12 h-3 V9.5 H4 v-3 h2.5 V4Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6.5 4
                moveTo(x = 6.5f, y = 4.0f)
                // h 3
                horizontalLineToRelative(dx = 3.0f)
                // v 2.5
                verticalLineToRelative(dy = 2.5f)
                // H 12
                horizontalLineTo(x = 12.0f)
                // v 3
                verticalLineToRelative(dy = 3.0f)
                // H 9.5
                horizontalLineTo(x = 9.5f)
                // V 12
                verticalLineTo(y = 12.0f)
                // h -3
                horizontalLineToRelative(dx = -3.0f)
                // V 9.5
                verticalLineTo(y = 9.5f)
                // H 4
                horizontalLineTo(x = 4.0f)
                // v -3
                verticalLineToRelative(dy = -3.0f)
                // h 2.5
                horizontalLineToRelative(dx = 2.5f)
                // V 4z
                verticalLineTo(y = 4.0f)
                close()
            }
            // m.095 7.65 3.648 -6.298 A.705 .705 0 0 1 4.352 1 h7.296 a.703 .703 0 0 1 .609 .352 l3.648 6.298 a.688 .688 0 0 1 0 .7 l-3.648 6.298 a.705 .705 0 0 1 -.61 .352 H4.353 a.705 .705 0 0 1 -.61 -.351 L.096 8.351 a.693 .693 0 0 1 0 -.701Z M14.484 8 l-3.242 -5.596 H4.758 L1.516 8 l3.243 5.597 h6.483 L14.484 8Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0.095 7.65
                moveTo(x = 0.095f, y = 7.65f)
                // l 3.648 -6.298
                lineToRelative(dx = 3.648f, dy = -6.298f)
                // A 0.705 0.705 0 0 1 4.352 1
                arcTo(
                    horizontalEllipseRadius = 0.705f,
                    verticalEllipseRadius = 0.705f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 4.352f,
                    y1 = 1.0f,
                )
                // h 7.296
                horizontalLineToRelative(dx = 7.296f)
                // a 0.703 0.703 0 0 1 0.609 0.352
                arcToRelative(
                    a = 0.703f,
                    b = 0.703f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.609f,
                    dy1 = 0.352f,
                )
                // l 3.648 6.298
                lineToRelative(dx = 3.648f, dy = 6.298f)
                // a 0.688 0.688 0 0 1 0 0.7
                arcToRelative(
                    a = 0.688f,
                    b = 0.688f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.7f,
                )
                // l -3.648 6.298
                lineToRelative(dx = -3.648f, dy = 6.298f)
                // a 0.705 0.705 0 0 1 -0.61 0.352
                arcToRelative(
                    a = 0.705f,
                    b = 0.705f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.61f,
                    dy1 = 0.352f,
                )
                // H 4.353
                horizontalLineTo(x = 4.353f)
                // a 0.705 0.705 0 0 1 -0.61 -0.351
                arcToRelative(
                    a = 0.705f,
                    b = 0.705f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.61f,
                    dy1 = -0.351f,
                )
                // L 0.096 8.351
                lineTo(x = 0.096f, y = 8.351f)
                // a 0.693 0.693 0 0 1 0 -0.701z
                arcToRelative(
                    a = 0.693f,
                    b = 0.693f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -0.701f,
                )
                close()
                // M 14.484 8
                moveTo(x = 14.484f, y = 8.0f)
                // l -3.242 -5.596
                lineToRelative(dx = -3.242f, dy = -5.596f)
                // H 4.758
                horizontalLineTo(x = 4.758f)
                // L 1.516 8
                lineTo(x = 1.516f, y = 8.0f)
                // l 3.243 5.597
                lineToRelative(dx = 3.243f, dy = 5.597f)
                // h 6.483
                horizontalLineToRelative(dx = 6.483f)
                // L 14.484 8z
                lineTo(x = 14.484f, y = 8.0f)
                close()
            }
        }.build().also { _ic1042 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1042: ImageVector? = null
