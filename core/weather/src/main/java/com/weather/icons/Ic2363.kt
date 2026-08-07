package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2363: ImageVector
    get() {
        val current = _ic2363
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2363",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M6.849 4.35 a.35 .35 0 1 0 -.7 0 v.544 l-.47 -.272 a.35 .35 0 0 0 -.35 .606 l.47 .272 -.47 .272 a.35 .35 0 0 0 .35 .606 l.47 -.272 v.544 a.35 .35 0 1 0 .7 0 v-.544 l.47 .272 a.35 .35 0 0 0 .35 -.606 L7.2 5.5 l.47 -.272 a.35 .35 0 1 0 -.35 -.606 l-.47 .272 V4.35Z m0 -4 a.35 .35 0 0 0 -.7 0 v.544 l-.47 -.272 a.35 .35 0 1 0 -.35 .606 l.47 .272 -.47 .272 a.35 .35 0 0 0 .35 .606 l.47 -.272 v.544 a.35 .35 0 1 0 .7 0 v-.544 l.47 .272 a.35 .35 0 0 0 .35 -.606 L7.2 1.5 l.47 -.272 a.35 .35 0 1 0 -.35 -.606 l-.47 .272 V.35Z m4 7 a.35 .35 0 1 0 -.7 0 v.544 l-.47 -.272 a.35 .35 0 0 0 -.35 .606 l.47 .272 -.47 .272 a.35 .35 0 0 0 .35 .606 l.47 -.272 v.544 a.35 .35 0 1 0 .7 0 v-.544 l.47 .272 a.35 .35 0 1 0 .35 -.606 L11.2 8.5 l.47 -.272 a.35 .35 0 1 0 -.35 -.606 l-.47 .272 V7.35Z m4 2 a.35 .35 0 1 0 -.7 0 v.544 l-.47 -.272 a.35 .35 0 1 0 -.35 .606 l.47 .272 -.47 .272 a.35 .35 0 1 0 .35 .606 l.47 -.272 v.544 a.35 .35 0 1 0 .7 0 v-.544 l.47 .272 a.35 .35 0 1 0 .35 -.606 l-.47 -.272 .47 -.272 a.35 .35 0 1 0 -.35 -.606 l-.47 .272 V9.35Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6.849 4.35
                moveTo(x = 6.849f, y = 4.35f)
                // a 0.35 0.35 0 1 0 -0.7 0
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.7f,
                    dy1 = 0.0f,
                )
                // v 0.544
                verticalLineToRelative(dy = 0.544f)
                // l -0.47 -0.272
                lineToRelative(dx = -0.47f, dy = -0.272f)
                // a 0.35 0.35 0 0 0 -0.35 0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = 0.606f,
                )
                // l 0.47 0.272
                lineToRelative(dx = 0.47f, dy = 0.272f)
                // l -0.47 0.272
                lineToRelative(dx = -0.47f, dy = 0.272f)
                // a 0.35 0.35 0 0 0 0.35 0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.35f,
                    dy1 = 0.606f,
                )
                // l 0.47 -0.272
                lineToRelative(dx = 0.47f, dy = -0.272f)
                // v 0.544
                verticalLineToRelative(dy = 0.544f)
                // a 0.35 0.35 0 1 0 0.7 0
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.7f,
                    dy1 = 0.0f,
                )
                // v -0.544
                verticalLineToRelative(dy = -0.544f)
                // l 0.47 0.272
                lineToRelative(dx = 0.47f, dy = 0.272f)
                // a 0.35 0.35 0 0 0 0.35 -0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.35f,
                    dy1 = -0.606f,
                )
                // L 7.2 5.5
                lineTo(x = 7.2f, y = 5.5f)
                // l 0.47 -0.272
                lineToRelative(dx = 0.47f, dy = -0.272f)
                // a 0.35 0.35 0 1 0 -0.35 -0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = -0.606f,
                )
                // l -0.47 0.272
                lineToRelative(dx = -0.47f, dy = 0.272f)
                // V 4.35z
                verticalLineTo(y = 4.35f)
                close()
                // m 0 -4
                moveToRelative(dx = 0.0f, dy = -4.0f)
                // a 0.35 0.35 0 0 0 -0.7 0
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.7f,
                    dy1 = 0.0f,
                )
                // v 0.544
                verticalLineToRelative(dy = 0.544f)
                // l -0.47 -0.272
                lineToRelative(dx = -0.47f, dy = -0.272f)
                // a 0.35 0.35 0 1 0 -0.35 0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = 0.606f,
                )
                // l 0.47 0.272
                lineToRelative(dx = 0.47f, dy = 0.272f)
                // l -0.47 0.272
                lineToRelative(dx = -0.47f, dy = 0.272f)
                // a 0.35 0.35 0 0 0 0.35 0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.35f,
                    dy1 = 0.606f,
                )
                // l 0.47 -0.272
                lineToRelative(dx = 0.47f, dy = -0.272f)
                // v 0.544
                verticalLineToRelative(dy = 0.544f)
                // a 0.35 0.35 0 1 0 0.7 0
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.7f,
                    dy1 = 0.0f,
                )
                // v -0.544
                verticalLineToRelative(dy = -0.544f)
                // l 0.47 0.272
                lineToRelative(dx = 0.47f, dy = 0.272f)
                // a 0.35 0.35 0 0 0 0.35 -0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.35f,
                    dy1 = -0.606f,
                )
                // L 7.2 1.5
                lineTo(x = 7.2f, y = 1.5f)
                // l 0.47 -0.272
                lineToRelative(dx = 0.47f, dy = -0.272f)
                // a 0.35 0.35 0 1 0 -0.35 -0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = -0.606f,
                )
                // l -0.47 0.272
                lineToRelative(dx = -0.47f, dy = 0.272f)
                // V 0.35z
                verticalLineTo(y = 0.35f)
                close()
                // m 4 7
                moveToRelative(dx = 4.0f, dy = 7.0f)
                // a 0.35 0.35 0 1 0 -0.7 0
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.7f,
                    dy1 = 0.0f,
                )
                // v 0.544
                verticalLineToRelative(dy = 0.544f)
                // l -0.47 -0.272
                lineToRelative(dx = -0.47f, dy = -0.272f)
                // a 0.35 0.35 0 0 0 -0.35 0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = 0.606f,
                )
                // l 0.47 0.272
                lineToRelative(dx = 0.47f, dy = 0.272f)
                // l -0.47 0.272
                lineToRelative(dx = -0.47f, dy = 0.272f)
                // a 0.35 0.35 0 0 0 0.35 0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.35f,
                    dy1 = 0.606f,
                )
                // l 0.47 -0.272
                lineToRelative(dx = 0.47f, dy = -0.272f)
                // v 0.544
                verticalLineToRelative(dy = 0.544f)
                // a 0.35 0.35 0 1 0 0.7 0
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.7f,
                    dy1 = 0.0f,
                )
                // v -0.544
                verticalLineToRelative(dy = -0.544f)
                // l 0.47 0.272
                lineToRelative(dx = 0.47f, dy = 0.272f)
                // a 0.35 0.35 0 1 0 0.35 -0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.35f,
                    dy1 = -0.606f,
                )
                // L 11.2 8.5
                lineTo(x = 11.2f, y = 8.5f)
                // l 0.47 -0.272
                lineToRelative(dx = 0.47f, dy = -0.272f)
                // a 0.35 0.35 0 1 0 -0.35 -0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = -0.606f,
                )
                // l -0.47 0.272
                lineToRelative(dx = -0.47f, dy = 0.272f)
                // V 7.35z
                verticalLineTo(y = 7.35f)
                close()
                // m 4 2
                moveToRelative(dx = 4.0f, dy = 2.0f)
                // a 0.35 0.35 0 1 0 -0.7 0
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.7f,
                    dy1 = 0.0f,
                )
                // v 0.544
                verticalLineToRelative(dy = 0.544f)
                // l -0.47 -0.272
                lineToRelative(dx = -0.47f, dy = -0.272f)
                // a 0.35 0.35 0 1 0 -0.35 0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = 0.606f,
                )
                // l 0.47 0.272
                lineToRelative(dx = 0.47f, dy = 0.272f)
                // l -0.47 0.272
                lineToRelative(dx = -0.47f, dy = 0.272f)
                // a 0.35 0.35 0 1 0 0.35 0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.35f,
                    dy1 = 0.606f,
                )
                // l 0.47 -0.272
                lineToRelative(dx = 0.47f, dy = -0.272f)
                // v 0.544
                verticalLineToRelative(dy = 0.544f)
                // a 0.35 0.35 0 1 0 0.7 0
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.7f,
                    dy1 = 0.0f,
                )
                // v -0.544
                verticalLineToRelative(dy = -0.544f)
                // l 0.47 0.272
                lineToRelative(dx = 0.47f, dy = 0.272f)
                // a 0.35 0.35 0 1 0 0.35 -0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.35f,
                    dy1 = -0.606f,
                )
                // l -0.47 -0.272
                lineToRelative(dx = -0.47f, dy = -0.272f)
                // l 0.47 -0.272
                lineToRelative(dx = 0.47f, dy = -0.272f)
                // a 0.35 0.35 0 1 0 -0.35 -0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = -0.606f,
                )
                // l -0.47 0.272
                lineToRelative(dx = -0.47f, dy = 0.272f)
                // V 9.35z
                verticalLineTo(y = 9.35f)
                close()
            }
            // m1.411 4 1.472 .835 1.235 2.139 2.44 1.017 L7.664 9.77 l3.588 .905 L15 15.998 H0 L1.411 4Z M11.5 6 a2.5 2.5 0 1 0 0 -5 2.5 2.5 0 0 0 0 5Z m-.39 -3.579 c-.02 -.176 .16 -.327 .39 -.327 s.41 .151 .39 .327 l-.177 1.548 h-.426 L11.11 2.42Z m.705 2.173 a.312 .312 0 1 1 -.625 0 .312 .312 0 0 1 .625 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 1.411 4
                moveTo(x = 1.411f, y = 4.0f)
                // l 1.472 0.835
                lineToRelative(dx = 1.472f, dy = 0.835f)
                // l 1.235 2.139
                lineToRelative(dx = 1.235f, dy = 2.139f)
                // l 2.44 1.017
                lineToRelative(dx = 2.44f, dy = 1.017f)
                // L 7.664 9.77
                lineTo(x = 7.664f, y = 9.77f)
                // l 3.588 0.905
                lineToRelative(dx = 3.588f, dy = 0.905f)
                // L 15 15.998
                lineTo(x = 15.0f, y = 15.998f)
                // H 0
                horizontalLineTo(x = 0.0f)
                // L 1.411 4z
                lineTo(x = 1.411f, y = 4.0f)
                close()
                // M 11.5 6
                moveTo(x = 11.5f, y = 6.0f)
                // a 2.5 2.5 0 1 0 0 -5
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -5.0f,
                )
                // a 2.5 2.5 0 0 0 0 5z
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 5.0f,
                )
                close()
                // m -0.39 -3.579
                moveToRelative(dx = -0.39f, dy = -3.579f)
                // c -0.02 -0.176 0.16 -0.327 0.39 -0.327
                curveToRelative(
                    dx1 = -0.02f,
                    dy1 = -0.176f,
                    dx2 = 0.16f,
                    dy2 = -0.327f,
                    dx3 = 0.39f,
                    dy3 = -0.327f,
                )
                // s 0.41 0.151 0.39 0.327
                reflectiveCurveToRelative(
                    dx1 = 0.41f,
                    dy1 = 0.151f,
                    dx2 = 0.39f,
                    dy2 = 0.327f,
                )
                // l -0.177 1.548
                lineToRelative(dx = -0.177f, dy = 1.548f)
                // h -0.426
                horizontalLineToRelative(dx = -0.426f)
                // L 11.11 2.42z
                lineTo(x = 11.11f, y = 2.42f)
                close()
                // m 0.705 2.173
                moveToRelative(dx = 0.705f, dy = 2.173f)
                // a 0.312 0.312 0 1 1 -0.625 0
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.625f,
                    dy1 = 0.0f,
                )
                // a 0.312 0.312 0 0 1 0.625 0z
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.625f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic2363 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2363: ImageVector? = null
