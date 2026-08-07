package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic408Fill: ImageVector
    get() {
        val current = _ic408Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic408Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M12.151 8.35 a.35 .35 0 1 1 .7 0 v.544 l.47 -.272 a.35 .35 0 1 1 .35 .606 l-.47 .272 .47 .272 a.35 .35 0 1 1 -.35 .606 l-.47 -.272 v.544 a.35 .35 0 1 1 -.7 0 v-.544 l-.47 .272 a.35 .35 0 1 1 -.35 -.606 l.47 -.272 -.47 -.272 a.35 .35 0 1 1 .35 -.606 l.47 .272 V8.35Z m-9 2 a.35 .35 0 1 1 .7 0 v.544 l.47 -.272 a.35 .35 0 1 1 .35 .606 l-.47 .272 .47 .272 a.35 .35 0 1 1 -.35 .606 l-.47 -.272 v.544 a.35 .35 0 1 1 -.7 0 v-.544 l-.47 .272 a.35 .35 0 1 1 -.35 -.606 l.47 -.272 -.47 -.272 a.35 .35 0 1 1 .35 -.606 l.47 .272 v-.544Z m9.35 1.65 a.35 .35 0 0 0 -.35 .35 v.544 l-.47 -.272 a.35 .35 0 1 0 -.35 .606 l.47 .272 -.47 .272 a.35 .35 0 1 0 .35 .606 l.47 -.272 v.544 a.35 .35 0 1 0 .7 0 v-.544 l.47 .272 a.35 .35 0 1 0 .35 -.606 l-.47 -.272 .47 -.272 a.35 .35 0 1 0 -.35 -.606 l-.47 .272 v-.544 a.35 .35 0 0 0 -.35 -.35Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 12.151 8.35
                moveTo(x = 12.151f, y = 8.35f)
                // a 0.35 0.35 0 1 1 0.7 0
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.7f,
                    dy1 = 0.0f,
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
                // V 8.35z
                verticalLineTo(y = 8.35f)
                close()
                // m -9 2
                moveToRelative(dx = -9.0f, dy = 2.0f)
                // a 0.35 0.35 0 1 1 0.7 0
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.7f,
                    dy1 = 0.0f,
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
                // v -0.544z
                verticalLineToRelative(dy = -0.544f)
                close()
                // m 9.35 1.65
                moveToRelative(dx = 9.35f, dy = 1.65f)
                // a 0.35 0.35 0 0 0 -0.35 0.35
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = 0.35f,
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
                // v -0.544
                verticalLineToRelative(dy = -0.544f)
                // a 0.35 0.35 0 0 0 -0.35 -0.35z
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = -0.35f,
                )
                close()
            }
            // M7.857 0 a.5 .5 0 0 0 -.474 .342 l-.167 .5 a.5 .5 0 0 0 -.025 .191 c-3.729 .31 -6.71 2.74 -7.19 5.824 -.033 .216 .397 .393 .593 .283 .412 -.232 1.055 -.445 2.029 -.445 1.359 0 2.074 .414 2.416 .718 .13 .116 .415 .116 .545 0 .294 -.262 .864 -.604 1.883 -.695 v8.749 a.533 .533 0 0 0 1.066 0 V6.718 c1.019 .091 1.589 .433 1.883 .695 .13 .116 .415 .116 .545 0 .342 -.304 1.057 -.718 2.416 -.718 .974 0 1.617 .213 2.03 .445 .195 .11 .625 -.067 .591 -.283 -.48 -3.087 -3.464 -5.517 -7.196 -5.824 a.5 .5 0 0 0 -.025 -.191 l-.166 -.5 A.5 .5 0 0 0 8.136 0 h-.279Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.857 0
                moveTo(x = 7.857f, y = 0.0f)
                // a 0.5 0.5 0 0 0 -0.474 0.342
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.474f,
                    dy1 = 0.342f,
                )
                // l -0.167 0.5
                lineToRelative(dx = -0.167f, dy = 0.5f)
                // a 0.5 0.5 0 0 0 -0.025 0.191
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.025f,
                    dy1 = 0.191f,
                )
                // c -3.729 0.31 -6.71 2.74 -7.19 5.824
                curveToRelative(
                    dx1 = -3.729f,
                    dy1 = 0.31f,
                    dx2 = -6.71f,
                    dy2 = 2.74f,
                    dx3 = -7.19f,
                    dy3 = 5.824f,
                )
                // c -0.033 0.216 0.397 0.393 0.593 0.283
                curveToRelative(
                    dx1 = -0.033f,
                    dy1 = 0.216f,
                    dx2 = 0.397f,
                    dy2 = 0.393f,
                    dx3 = 0.593f,
                    dy3 = 0.283f,
                )
                // c 0.412 -0.232 1.055 -0.445 2.029 -0.445
                curveToRelative(
                    dx1 = 0.412f,
                    dy1 = -0.232f,
                    dx2 = 1.055f,
                    dy2 = -0.445f,
                    dx3 = 2.029f,
                    dy3 = -0.445f,
                )
                // c 1.359 0 2.074 0.414 2.416 0.718
                curveToRelative(
                    dx1 = 1.359f,
                    dy1 = 0.0f,
                    dx2 = 2.074f,
                    dy2 = 0.414f,
                    dx3 = 2.416f,
                    dy3 = 0.718f,
                )
                // c 0.13 0.116 0.415 0.116 0.545 0
                curveToRelative(
                    dx1 = 0.13f,
                    dy1 = 0.116f,
                    dx2 = 0.415f,
                    dy2 = 0.116f,
                    dx3 = 0.545f,
                    dy3 = 0.0f,
                )
                // c 0.294 -0.262 0.864 -0.604 1.883 -0.695
                curveToRelative(
                    dx1 = 0.294f,
                    dy1 = -0.262f,
                    dx2 = 0.864f,
                    dy2 = -0.604f,
                    dx3 = 1.883f,
                    dy3 = -0.695f,
                )
                // v 8.749
                verticalLineToRelative(dy = 8.749f)
                // a 0.533 0.533 0 0 0 1.066 0
                arcToRelative(
                    a = 0.533f,
                    b = 0.533f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.066f,
                    dy1 = 0.0f,
                )
                // V 6.718
                verticalLineTo(y = 6.718f)
                // c 1.019 0.091 1.589 0.433 1.883 0.695
                curveToRelative(
                    dx1 = 1.019f,
                    dy1 = 0.091f,
                    dx2 = 1.589f,
                    dy2 = 0.433f,
                    dx3 = 1.883f,
                    dy3 = 0.695f,
                )
                // c 0.13 0.116 0.415 0.116 0.545 0
                curveToRelative(
                    dx1 = 0.13f,
                    dy1 = 0.116f,
                    dx2 = 0.415f,
                    dy2 = 0.116f,
                    dx3 = 0.545f,
                    dy3 = 0.0f,
                )
                // c 0.342 -0.304 1.057 -0.718 2.416 -0.718
                curveToRelative(
                    dx1 = 0.342f,
                    dy1 = -0.304f,
                    dx2 = 1.057f,
                    dy2 = -0.718f,
                    dx3 = 2.416f,
                    dy3 = -0.718f,
                )
                // c 0.974 0 1.617 0.213 2.03 0.445
                curveToRelative(
                    dx1 = 0.974f,
                    dy1 = 0.0f,
                    dx2 = 1.617f,
                    dy2 = 0.213f,
                    dx3 = 2.03f,
                    dy3 = 0.445f,
                )
                // c 0.195 0.11 0.625 -0.067 0.591 -0.283
                curveToRelative(
                    dx1 = 0.195f,
                    dy1 = 0.11f,
                    dx2 = 0.625f,
                    dy2 = -0.067f,
                    dx3 = 0.591f,
                    dy3 = -0.283f,
                )
                // c -0.48 -3.087 -3.464 -5.517 -7.196 -5.824
                curveToRelative(
                    dx1 = -0.48f,
                    dy1 = -3.087f,
                    dx2 = -3.464f,
                    dy2 = -5.517f,
                    dx3 = -7.196f,
                    dy3 = -5.824f,
                )
                // a 0.5 0.5 0 0 0 -0.025 -0.191
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.025f,
                    dy1 = -0.191f,
                )
                // l -0.166 -0.5
                lineToRelative(dx = -0.166f, dy = -0.5f)
                // A 0.5 0.5 0 0 0 8.136 0
                arcTo(
                    horizontalEllipseRadius = 0.5f,
                    verticalEllipseRadius = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 8.136f,
                    y1 = 0.0f,
                )
                // h -0.279z
                horizontalLineToRelative(dx = -0.279f)
                close()
            }
        }.build().also { _ic408Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic408Fill: ImageVector? = null
