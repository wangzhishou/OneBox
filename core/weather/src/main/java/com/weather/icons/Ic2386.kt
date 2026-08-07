package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2386: ImageVector
    get() {
        val current = _ic2386
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2386",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.25 10.875 H11 a.625 .625 0 1 0 -.59 -.833 .312 .312 0 1 1 -.589 -.209 A1.25 1.25 0 1 1 11 11.5 H7.25 a.312 .312 0 1 1 0 -.625Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.25 10.875
                moveTo(x = 7.25f, y = 10.875f)
                // H 11
                horizontalLineTo(x = 11.0f)
                // a 0.625 0.625 0 1 0 -0.59 -0.833
                arcToRelative(
                    a = 0.625f,
                    b = 0.625f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.59f,
                    dy1 = -0.833f,
                )
                // a 0.312 0.312 0 1 1 -0.589 -0.209
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.589f,
                    dy1 = -0.209f,
                )
                // A 1.25 1.25 0 1 1 11 11.5
                arcTo(
                    horizontalEllipseRadius = 1.25f,
                    verticalEllipseRadius = 1.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 11.0f,
                    y1 = 11.5f,
                )
                // H 7.25
                horizontalLineTo(x = 7.25f)
                // a 0.312 0.312 0 1 1 0 -0.625z
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -0.625f,
                )
                close()
            }
            // M12.924 10.485 a1.563 1.563 0 0 1 3.076 .39 c0 .873 -.775 1.563 -1.563 1.563 H6.313 a.312 .312 0 1 1 0 -.626 h8.125 c.462 0 .937 -.43 .937 -.937 a.937 .937 0 0 0 -1.845 -.235 .312 .312 0 1 1 -.606 -.155Z m-5.361 2.578 c0 -.173 .14 -.313 .312 -.313 h5 a1.25 1.25 0 1 1 -1.179 1.667 .312 .312 0 1 1 .59 -.209 .625 .625 0 1 0 .589 -.833 h-5 a.312 .312 0 0 1 -.313 -.313Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 12.924 10.485
                moveTo(x = 12.924f, y = 10.485f)
                // a 1.563 1.563 0 0 1 3.076 0.39
                arcToRelative(
                    a = 1.563f,
                    b = 1.563f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 3.076f,
                    dy1 = 0.39f,
                )
                // c 0 0.873 -0.775 1.563 -1.563 1.563
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.873f,
                    dx2 = -0.775f,
                    dy2 = 1.563f,
                    dx3 = -1.563f,
                    dy3 = 1.563f,
                )
                // H 6.313
                horizontalLineTo(x = 6.313f)
                // a 0.312 0.312 0 1 1 0 -0.626
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -0.626f,
                )
                // h 8.125
                horizontalLineToRelative(dx = 8.125f)
                // c 0.462 0 0.937 -0.43 0.937 -0.937
                curveToRelative(
                    dx1 = 0.462f,
                    dy1 = 0.0f,
                    dx2 = 0.937f,
                    dy2 = -0.43f,
                    dx3 = 0.937f,
                    dy3 = -0.937f,
                )
                // a 0.937 0.937 0 0 0 -1.845 -0.235
                arcToRelative(
                    a = 0.937f,
                    b = 0.937f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.845f,
                    dy1 = -0.235f,
                )
                // a 0.312 0.312 0 1 1 -0.606 -0.155z
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.606f,
                    dy1 = -0.155f,
                )
                close()
                // m -5.361 2.578
                moveToRelative(dx = -5.361f, dy = 2.578f)
                // c 0 -0.173 0.14 -0.313 0.312 -0.313
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.173f,
                    dx2 = 0.14f,
                    dy2 = -0.313f,
                    dx3 = 0.312f,
                    dy3 = -0.313f,
                )
                // h 5
                horizontalLineToRelative(dx = 5.0f)
                // a 1.25 1.25 0 1 1 -1.179 1.667
                arcToRelative(
                    a = 1.25f,
                    b = 1.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.179f,
                    dy1 = 1.667f,
                )
                // a 0.312 0.312 0 1 1 0.59 -0.209
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.59f,
                    dy1 = -0.209f,
                )
                // a 0.625 0.625 0 1 0 0.589 -0.833
                arcToRelative(
                    a = 0.625f,
                    b = 0.625f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.589f,
                    dy1 = -0.833f,
                )
                // h -5
                horizontalLineToRelative(dx = -5.0f)
                // a 0.312 0.312 0 0 1 -0.313 -0.313z
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.313f,
                    dy1 = -0.313f,
                )
                close()
            }
            // M2.5 0 a.5 .5 0 0 0 -.5 .5 v15 a.5 .5 0 0 0 1 0 V9 l11 -4 L3 1 V.5 a.5 .5 0 0 0 -.5 -.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 2.5 0
                moveTo(x = 2.5f, y = 0.0f)
                // a 0.5 0.5 0 0 0 -0.5 0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.5f,
                    dy1 = 0.5f,
                )
                // v 15
                verticalLineToRelative(dy = 15.0f)
                // a 0.5 0.5 0 0 0 1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // V 9
                verticalLineTo(y = 9.0f)
                // l 11 -4
                lineToRelative(dx = 11.0f, dy = -4.0f)
                // L 3 1
                lineTo(x = 3.0f, y = 1.0f)
                // V 0.5
                verticalLineTo(y = 0.5f)
                // a 0.5 0.5 0 0 0 -0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                close()
            }
        }.build().also { _ic2386 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2386: ImageVector? = null
