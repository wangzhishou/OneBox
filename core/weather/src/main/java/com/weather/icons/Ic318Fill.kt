package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic318Fill: ImageVector
    get() {
        val current = _ic318Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic318Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M1 8.5 a.5 .5 0 0 1 1 0 v2 a.5 .5 0 0 1 -1 0 v-2Z m2 0 a.5 .5 0 0 1 1 0 v2 a.5 .5 0 0 1 -1 0 v-2Z M5.5 8 a.5 .5 0 0 0 -.5 .5 v2 a.5 .5 0 0 0 1 0 v-2 a.5 .5 0 0 0 -.5 -.5Z M4 12.5 a.5 .5 0 0 1 1 0 v2 a.5 .5 0 0 1 -1 0 v-2Z M2.5 12 a.5 .5 0 0 0 -.5 .5 v2 a.5 .5 0 0 0 1 0 v-2 a.5 .5 0 0 0 -.5 -.5Z M10 8.5 a.5 .5 0 0 1 1 0 v2 a.5 .5 0 0 1 -1 0 v-2Z m2.5 -.5 a.5 .5 0 0 0 -.5 .5 v2 a.5 .5 0 0 0 1 0 v-2 a.5 .5 0 0 0 -.5 -.5Z m1.5 .5 a.5 .5 0 0 1 1 0 v2 a.5 .5 0 0 1 -1 0 v-2Z m.5 3.5 a.5 .5 0 0 0 -.5 .5 v2 a.5 .5 0 0 0 1 0 v-2 a.5 .5 0 0 0 -.5 -.5Z m-4.5 .5 a.5 .5 0 0 1 1 0 v2 a.5 .5 0 0 1 -1 0 v-2Z m2.5 -.5 a.5 .5 0 0 0 -.5 .5 v2 a.5 .5 0 0 0 1 0 v-2 a.5 .5 0 0 0 -.5 -.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 1 8.5
                moveTo(x = 1.0f, y = 8.5f)
                // a 0.5 0.5 0 0 1 1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // v 2
                verticalLineToRelative(dy = 2.0f)
                // a 0.5 0.5 0 0 1 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // v -2z
                verticalLineToRelative(dy = -2.0f)
                close()
                // m 2 0
                moveToRelative(dx = 2.0f, dy = 0.0f)
                // a 0.5 0.5 0 0 1 1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // v 2
                verticalLineToRelative(dy = 2.0f)
                // a 0.5 0.5 0 0 1 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // v -2z
                verticalLineToRelative(dy = -2.0f)
                close()
                // M 5.5 8
                moveTo(x = 5.5f, y = 8.0f)
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
                // v 2
                verticalLineToRelative(dy = 2.0f)
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
                // v -2
                verticalLineToRelative(dy = -2.0f)
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
                // M 4 12.5
                moveTo(x = 4.0f, y = 12.5f)
                // a 0.5 0.5 0 0 1 1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // v 2
                verticalLineToRelative(dy = 2.0f)
                // a 0.5 0.5 0 0 1 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // v -2z
                verticalLineToRelative(dy = -2.0f)
                close()
                // M 2.5 12
                moveTo(x = 2.5f, y = 12.0f)
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
                // v 2
                verticalLineToRelative(dy = 2.0f)
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
                // v -2
                verticalLineToRelative(dy = -2.0f)
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
                // M 10 8.5
                moveTo(x = 10.0f, y = 8.5f)
                // a 0.5 0.5 0 0 1 1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // v 2
                verticalLineToRelative(dy = 2.0f)
                // a 0.5 0.5 0 0 1 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // v -2z
                verticalLineToRelative(dy = -2.0f)
                close()
                // m 2.5 -0.5
                moveToRelative(dx = 2.5f, dy = -0.5f)
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
                // v 2
                verticalLineToRelative(dy = 2.0f)
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
                // v -2
                verticalLineToRelative(dy = -2.0f)
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
                // m 1.5 0.5
                moveToRelative(dx = 1.5f, dy = 0.5f)
                // a 0.5 0.5 0 0 1 1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // v 2
                verticalLineToRelative(dy = 2.0f)
                // a 0.5 0.5 0 0 1 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // v -2z
                verticalLineToRelative(dy = -2.0f)
                close()
                // m 0.5 3.5
                moveToRelative(dx = 0.5f, dy = 3.5f)
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
                // v 2
                verticalLineToRelative(dy = 2.0f)
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
                // v -2
                verticalLineToRelative(dy = -2.0f)
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
                // m -4.5 0.5
                moveToRelative(dx = -4.5f, dy = 0.5f)
                // a 0.5 0.5 0 0 1 1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // v 2
                verticalLineToRelative(dy = 2.0f)
                // a 0.5 0.5 0 0 1 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // v -2z
                verticalLineToRelative(dy = -2.0f)
                close()
                // m 2.5 -0.5
                moveToRelative(dx = 2.5f, dy = -0.5f)
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
                // v 2
                verticalLineToRelative(dy = 2.0f)
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
                // v -2
                verticalLineToRelative(dy = -2.0f)
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
        }.build().also { _ic318Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic318Fill: ImageVector? = null
