package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2399: ImageVector
    get() {
        val current = _ic2399
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2399",
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
            // m1.411 4 1.472 .835 1.235 2.139 2.44 1.017 L7.664 9.77 l3.588 .905 L15 15.998 H0 L1.411 4Z m9.778 -1.126 c-.016 -.168 .128 -.312 .311 -.312 s.327 .144 .311 .312 l-.14 1.47 h-.341 l-.141 -1.47Z m.545 1.954 a.234 .234 0 1 1 -.468 0 .234 .234 0 0 1 .468 0Z
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
                // m 9.778 -1.126
                moveToRelative(dx = 9.778f, dy = -1.126f)
                // c -0.016 -0.168 0.128 -0.312 0.311 -0.312
                curveToRelative(
                    dx1 = -0.016f,
                    dy1 = -0.168f,
                    dx2 = 0.128f,
                    dy2 = -0.312f,
                    dx3 = 0.311f,
                    dy3 = -0.312f,
                )
                // s 0.327 0.144 0.311 0.312
                reflectiveCurveToRelative(
                    dx1 = 0.327f,
                    dy1 = 0.144f,
                    dx2 = 0.311f,
                    dy2 = 0.312f,
                )
                // l -0.14 1.47
                lineToRelative(dx = -0.14f, dy = 1.47f)
                // h -0.341
                horizontalLineToRelative(dx = -0.341f)
                // l -0.141 -1.47z
                lineToRelative(dx = -0.141f, dy = -1.47f)
                close()
                // m 0.545 1.954
                moveToRelative(dx = 0.545f, dy = 1.954f)
                // a 0.234 0.234 0 1 1 -0.468 0
                arcToRelative(
                    a = 0.234f,
                    b = 0.234f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.468f,
                    dy1 = 0.0f,
                )
                // a 0.234 0.234 0 0 1 0.468 0z
                arcToRelative(
                    a = 0.234f,
                    b = 0.234f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.468f,
                    dy1 = 0.0f,
                )
                close()
            }
            // M11.358 1.394 a.164 .164 0 0 1 .284 0 l2.336 4.05 a.162 .162 0 0 1 -.142 .244 H9.164 a.162 .162 0 0 1 -.142 -.243 l2.336 -4.05Z m2.11 3.918 L11.5 1.898 9.531 5.313 h3.938Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.358 1.394
                moveTo(x = 11.358f, y = 1.394f)
                // a 0.164 0.164 0 0 1 0.284 0
                arcToRelative(
                    a = 0.164f,
                    b = 0.164f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.284f,
                    dy1 = 0.0f,
                )
                // l 2.336 4.05
                lineToRelative(dx = 2.336f, dy = 4.05f)
                // a 0.162 0.162 0 0 1 -0.142 0.244
                arcToRelative(
                    a = 0.162f,
                    b = 0.162f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.142f,
                    dy1 = 0.244f,
                )
                // H 9.164
                horizontalLineTo(x = 9.164f)
                // a 0.162 0.162 0 0 1 -0.142 -0.243
                arcToRelative(
                    a = 0.162f,
                    b = 0.162f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.142f,
                    dy1 = -0.243f,
                )
                // l 2.336 -4.05z
                lineToRelative(dx = 2.336f, dy = -4.05f)
                close()
                // m 2.11 3.918
                moveToRelative(dx = 2.11f, dy = 3.918f)
                // L 11.5 1.898
                lineTo(x = 11.5f, y = 1.898f)
                // L 9.531 5.313
                lineTo(x = 9.531f, y = 5.313f)
                // h 3.938z
                horizontalLineToRelative(dx = 3.938f)
                close()
            }
        }.build().also { _ic2399 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2399: ImageVector? = null
