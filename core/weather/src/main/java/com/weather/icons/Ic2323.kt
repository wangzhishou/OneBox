package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2323: ImageVector
    get() {
        val current = _ic2323
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2323",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // m1.411 4 1.472 .835 1.235 2.139 2.44 1.017 L7.664 9.77 l3.588 .905 L15 15.998 H0 L1.411 4Z m4.088 -4 a.35 .35 0 0 1 .35 .35 v.544 l.47 -.272 a.35 .35 0 0 1 .35 .606 L6.2 1.5 l.47 .272 a.35 .35 0 0 1 -.35 .606 l-.47 -.272 v.544 a.35 .35 0 1 1 -.7 0 v-.544 l-.471 .272 a.35 .35 0 0 1 -.35 -.606 L4.8 1.5 l-.471 -.272 a.35 .35 0 1 1 .35 -.606 l.471 .272 V.35 A.35 .35 0 0 1 5.5 0Z m4.35 3.35 a.35 .35 0 1 0 -.7 0 v.544 l-.47 -.272 a.35 .35 0 0 0 -.351 .606 l.471 .272 -.471 .272 a.35 .35 0 0 0 .35 .606 l.471 -.272 v.544 a.35 .35 0 1 0 .7 0 v-.544 l.47 .272 a.35 .35 0 1 0 .35 -.606 L10.2 4.5 l.47 -.272 a.35 .35 0 1 0 -.35 -.606 l-.47 .272 V3.35Z
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
                // m 4.088 -4
                moveToRelative(dx = 4.088f, dy = -4.0f)
                // a 0.35 0.35 0 0 1 0.35 0.35
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.35f,
                    dy1 = 0.35f,
                )
                // v 0.544
                verticalLineToRelative(dy = 0.544f)
                // l 0.47 -0.272
                lineToRelative(dx = 0.47f, dy = -0.272f)
                // a 0.35 0.35 0 0 1 0.35 0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.35f,
                    dy1 = 0.606f,
                )
                // L 6.2 1.5
                lineTo(x = 6.2f, y = 1.5f)
                // l 0.47 0.272
                lineToRelative(dx = 0.47f, dy = 0.272f)
                // a 0.35 0.35 0 0 1 -0.35 0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.35f,
                    dy1 = 0.606f,
                )
                // l -0.47 -0.272
                lineToRelative(dx = -0.47f, dy = -0.272f)
                // v 0.544
                verticalLineToRelative(dy = 0.544f)
                // a 0.35 0.35 0 1 1 -0.7 0
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.7f,
                    dy1 = 0.0f,
                )
                // v -0.544
                verticalLineToRelative(dy = -0.544f)
                // l -0.471 0.272
                lineToRelative(dx = -0.471f, dy = 0.272f)
                // a 0.35 0.35 0 0 1 -0.35 -0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.35f,
                    dy1 = -0.606f,
                )
                // L 4.8 1.5
                lineTo(x = 4.8f, y = 1.5f)
                // l -0.471 -0.272
                lineToRelative(dx = -0.471f, dy = -0.272f)
                // a 0.35 0.35 0 1 1 0.35 -0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.35f,
                    dy1 = -0.606f,
                )
                // l 0.471 0.272
                lineToRelative(dx = 0.471f, dy = 0.272f)
                // V 0.35
                verticalLineTo(y = 0.35f)
                // A 0.35 0.35 0 0 1 5.5 0z
                arcTo(
                    horizontalEllipseRadius = 0.35f,
                    verticalEllipseRadius = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 5.5f,
                    y1 = 0.0f,
                )
                close()
                // m 4.35 3.35
                moveToRelative(dx = 4.35f, dy = 3.35f)
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
                // a 0.35 0.35 0 0 0 -0.351 0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.351f,
                    dy1 = 0.606f,
                )
                // l 0.471 0.272
                lineToRelative(dx = 0.471f, dy = 0.272f)
                // l -0.471 0.272
                lineToRelative(dx = -0.471f, dy = 0.272f)
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
                // l 0.471 -0.272
                lineToRelative(dx = 0.471f, dy = -0.272f)
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
                // L 10.2 4.5
                lineTo(x = 10.2f, y = 4.5f)
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
                // V 3.35z
                verticalLineTo(y = 3.35f)
                close()
            }
            // M5.849 4.35 a.35 .35 0 1 0 -.7 0 v.544 l-.471 -.272 a.35 .35 0 0 0 -.35 .606 L4.8 5.5 l-.471 .272 a.35 .35 0 0 0 .35 .606 l.471 -.272 v.544 a.35 .35 0 1 0 .7 0 v-.544 l.47 .272 a.35 .35 0 0 0 .35 -.606 L6.2 5.5 l.47 -.272 a.35 .35 0 0 0 -.35 -.606 l-.47 .272 V4.35Z m5 3 a.35 .35 0 1 0 -.7 0 v.544 l-.47 -.272 a.35 .35 0 0 0 -.351 .606 l.471 .272 -.471 .272 a.35 .35 0 0 0 .35 .606 l.471 -.272 v.544 a.35 .35 0 1 0 .7 0 v-.544 l.47 .272 a.35 .35 0 1 0 .35 -.606 L11.2 8.5 l.47 -.272 a.35 .35 0 1 0 -.35 -.606 l-.47 .272 V7.35Z M13.499 9 a.35 .35 0 0 1 .35 .35 v.544 l.47 -.272 a.35 .35 0 1 1 .35 .606 l-.47 .272 .47 .272 a.35 .35 0 1 1 -.35 .606 l-.47 -.272 v.544 a.35 .35 0 1 1 -.7 0 v-.544 l-.47 .272 a.35 .35 0 1 1 -.35 -.606 l.47 -.272 -.47 -.272 a.35 .35 0 1 1 .35 -.606 l.47 .272 V9.35 a.35 .35 0 0 1 .35 -.35Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5.849 4.35
                moveTo(x = 5.849f, y = 4.35f)
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
                // l -0.471 -0.272
                lineToRelative(dx = -0.471f, dy = -0.272f)
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
                // L 4.8 5.5
                lineTo(x = 4.8f, y = 5.5f)
                // l -0.471 0.272
                lineToRelative(dx = -0.471f, dy = 0.272f)
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
                // l 0.471 -0.272
                lineToRelative(dx = 0.471f, dy = -0.272f)
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
                // L 6.2 5.5
                lineTo(x = 6.2f, y = 5.5f)
                // l 0.47 -0.272
                lineToRelative(dx = 0.47f, dy = -0.272f)
                // a 0.35 0.35 0 0 0 -0.35 -0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = -0.606f,
                )
                // l -0.47 0.272
                lineToRelative(dx = -0.47f, dy = 0.272f)
                // V 4.35z
                verticalLineTo(y = 4.35f)
                close()
                // m 5 3
                moveToRelative(dx = 5.0f, dy = 3.0f)
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
                // a 0.35 0.35 0 0 0 -0.351 0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.351f,
                    dy1 = 0.606f,
                )
                // l 0.471 0.272
                lineToRelative(dx = 0.471f, dy = 0.272f)
                // l -0.471 0.272
                lineToRelative(dx = -0.471f, dy = 0.272f)
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
                // l 0.471 -0.272
                lineToRelative(dx = 0.471f, dy = -0.272f)
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
                // M 13.499 9
                moveTo(x = 13.499f, y = 9.0f)
                // a 0.35 0.35 0 0 1 0.35 0.35
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.35f,
                    dy1 = 0.35f,
                )
                // v 0.544
                verticalLineToRelative(dy = 0.544f)
                // l 0.47 -0.272
                lineToRelative(dx = 0.47f, dy = -0.272f)
                // a 0.35 0.35 0 1 1 0.35 0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.35f,
                    dy1 = 0.606f,
                )
                // l -0.47 0.272
                lineToRelative(dx = -0.47f, dy = 0.272f)
                // l 0.47 0.272
                lineToRelative(dx = 0.47f, dy = 0.272f)
                // a 0.35 0.35 0 1 1 -0.35 0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.35f,
                    dy1 = 0.606f,
                )
                // l -0.47 -0.272
                lineToRelative(dx = -0.47f, dy = -0.272f)
                // v 0.544
                verticalLineToRelative(dy = 0.544f)
                // a 0.35 0.35 0 1 1 -0.7 0
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.7f,
                    dy1 = 0.0f,
                )
                // v -0.544
                verticalLineToRelative(dy = -0.544f)
                // l -0.47 0.272
                lineToRelative(dx = -0.47f, dy = 0.272f)
                // a 0.35 0.35 0 1 1 -0.35 -0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.35f,
                    dy1 = -0.606f,
                )
                // l 0.47 -0.272
                lineToRelative(dx = 0.47f, dy = -0.272f)
                // l -0.47 -0.272
                lineToRelative(dx = -0.47f, dy = -0.272f)
                // a 0.35 0.35 0 1 1 0.35 -0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.35f,
                    dy1 = -0.606f,
                )
                // l 0.47 0.272
                lineToRelative(dx = 0.47f, dy = 0.272f)
                // V 9.35
                verticalLineTo(y = 9.35f)
                // a 0.35 0.35 0 0 1 0.35 -0.35z
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.35f,
                    dy1 = -0.35f,
                )
                close()
            }
        }.build().also { _ic2323 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2323: ImageVector? = null
