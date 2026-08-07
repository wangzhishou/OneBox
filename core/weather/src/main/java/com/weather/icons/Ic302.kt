package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic302: ImageVector
    get() {
        val current = _ic302
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic302",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.46 12.626 c-.054 0 -.089 -.05 -.065 -.093 l.792 -1.438 C9.21 11.05 9.176 11 9.122 11 H7.544 a.147 .147 0 0 0 -.076 .02 .158 .158 0 0 0 -.058 .057 l-1.397 2.637 c-.042 .079 .022 .17 .118 .17 h1.42 c.05 0 .084 .043 .069 .086 l-.739 1.943 c-.027 .07 .072 .118 .124 .063 l2.978 -3.243 c.04 -.042 .006 -.107 -.055 -.107 H8.46Z M3 13 a1 1 0 1 0 2 0 c0 -.5 -.555 -1.395 -1 -2 -.445 .605 -1 1.5 -1 2Z m8 0 a1 1 0 0 0 2 0 c0 -.5 -.555 -1.395 -1 -2 -.445 .605 -1 1.5 -1 2Z m-3.1 -3 c1.453 0 2.761 -.62 3.675 -1.61 a.335 .335 0 0 1 .365 -.083 3 3 0 1 0 .566 -5.767 .335 .335 0 0 1 -.341 -.152 A4.997 4.997 0 0 0 7.9 0 a4.997 4.997 0 0 0 -4.25 2.365 .334 .334 0 0 1 -.32 .153 3 3 0 1 0 .596 5.836 .334 .334 0 0 1 .345 .086 A4.99 4.99 0 0 0 7.9 10Z m3.905 -2.896 c-.172 -.129 -.438 -.097 -.555 .083 A3.997 3.997 0 0 1 7.9 9 a3.996 3.996 0 0 1 -3.297 -1.734 c-.112 -.163 -.347 -.197 -.513 -.089 a2 2 0 1 1 -.362 -3.54 c.184 .072 .408 -.01 .485 -.192 a4.001 4.001 0 0 1 7.398 .059 c.08 .2 .335 .282 .53 .19 a2 2 0 1 1 -.335 3.41Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.46 12.626
                moveTo(x = 8.46f, y = 12.626f)
                // c -0.054 0 -0.089 -0.05 -0.065 -0.093
                curveToRelative(
                    dx1 = -0.054f,
                    dy1 = 0.0f,
                    dx2 = -0.089f,
                    dy2 = -0.05f,
                    dx3 = -0.065f,
                    dy3 = -0.093f,
                )
                // l 0.792 -1.438
                lineToRelative(dx = 0.792f, dy = -1.438f)
                // C 9.21 11.05 9.176 11 9.122 11
                curveTo(
                    x1 = 9.21f,
                    y1 = 11.05f,
                    x2 = 9.176f,
                    y2 = 11.0f,
                    x3 = 9.122f,
                    y3 = 11.0f,
                )
                // H 7.544
                horizontalLineTo(x = 7.544f)
                // a 0.147 0.147 0 0 0 -0.076 0.02
                arcToRelative(
                    a = 0.147f,
                    b = 0.147f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.076f,
                    dy1 = 0.02f,
                )
                // a 0.158 0.158 0 0 0 -0.058 0.057
                arcToRelative(
                    a = 0.158f,
                    b = 0.158f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.058f,
                    dy1 = 0.057f,
                )
                // l -1.397 2.637
                lineToRelative(dx = -1.397f, dy = 2.637f)
                // c -0.042 0.079 0.022 0.17 0.118 0.17
                curveToRelative(
                    dx1 = -0.042f,
                    dy1 = 0.079f,
                    dx2 = 0.022f,
                    dy2 = 0.17f,
                    dx3 = 0.118f,
                    dy3 = 0.17f,
                )
                // h 1.42
                horizontalLineToRelative(dx = 1.42f)
                // c 0.05 0 0.084 0.043 0.069 0.086
                curveToRelative(
                    dx1 = 0.05f,
                    dy1 = 0.0f,
                    dx2 = 0.084f,
                    dy2 = 0.043f,
                    dx3 = 0.069f,
                    dy3 = 0.086f,
                )
                // l -0.739 1.943
                lineToRelative(dx = -0.739f, dy = 1.943f)
                // c -0.027 0.07 0.072 0.118 0.124 0.063
                curveToRelative(
                    dx1 = -0.027f,
                    dy1 = 0.07f,
                    dx2 = 0.072f,
                    dy2 = 0.118f,
                    dx3 = 0.124f,
                    dy3 = 0.063f,
                )
                // l 2.978 -3.243
                lineToRelative(dx = 2.978f, dy = -3.243f)
                // c 0.04 -0.042 0.006 -0.107 -0.055 -0.107
                curveToRelative(
                    dx1 = 0.04f,
                    dy1 = -0.042f,
                    dx2 = 0.006f,
                    dy2 = -0.107f,
                    dx3 = -0.055f,
                    dy3 = -0.107f,
                )
                // H 8.46z
                horizontalLineTo(x = 8.46f)
                close()
                // M 3 13
                moveTo(x = 3.0f, y = 13.0f)
                // a 1 1 0 1 0 2 0
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 2.0f,
                    dy1 = 0.0f,
                )
                // c 0 -0.5 -0.555 -1.395 -1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = -0.555f,
                    dy2 = -1.395f,
                    dx3 = -1.0f,
                    dy3 = -2.0f,
                )
                // c -0.445 0.605 -1 1.5 -1 2z
                curveToRelative(
                    dx1 = -0.445f,
                    dy1 = 0.605f,
                    dx2 = -1.0f,
                    dy2 = 1.5f,
                    dx3 = -1.0f,
                    dy3 = 2.0f,
                )
                close()
                // m 8 0
                moveToRelative(dx = 8.0f, dy = 0.0f)
                // a 1 1 0 0 0 2 0
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.0f,
                    dy1 = 0.0f,
                )
                // c 0 -0.5 -0.555 -1.395 -1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = -0.555f,
                    dy2 = -1.395f,
                    dx3 = -1.0f,
                    dy3 = -2.0f,
                )
                // c -0.445 0.605 -1 1.5 -1 2z
                curveToRelative(
                    dx1 = -0.445f,
                    dy1 = 0.605f,
                    dx2 = -1.0f,
                    dy2 = 1.5f,
                    dx3 = -1.0f,
                    dy3 = 2.0f,
                )
                close()
                // m -3.1 -3
                moveToRelative(dx = -3.1f, dy = -3.0f)
                // c 1.453 0 2.761 -0.62 3.675 -1.61
                curveToRelative(
                    dx1 = 1.453f,
                    dy1 = 0.0f,
                    dx2 = 2.761f,
                    dy2 = -0.62f,
                    dx3 = 3.675f,
                    dy3 = -1.61f,
                )
                // a 0.335 0.335 0 0 1 0.365 -0.083
                arcToRelative(
                    a = 0.335f,
                    b = 0.335f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.365f,
                    dy1 = -0.083f,
                )
                // a 3 3 0 1 0 0.566 -5.767
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.566f,
                    dy1 = -5.767f,
                )
                // a 0.335 0.335 0 0 1 -0.341 -0.152
                arcToRelative(
                    a = 0.335f,
                    b = 0.335f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.341f,
                    dy1 = -0.152f,
                )
                // A 4.997 4.997 0 0 0 7.9 0
                arcTo(
                    horizontalEllipseRadius = 4.997f,
                    verticalEllipseRadius = 4.997f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.9f,
                    y1 = 0.0f,
                )
                // a 4.997 4.997 0 0 0 -4.25 2.365
                arcToRelative(
                    a = 4.997f,
                    b = 4.997f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -4.25f,
                    dy1 = 2.365f,
                )
                // a 0.334 0.334 0 0 1 -0.32 0.153
                arcToRelative(
                    a = 0.334f,
                    b = 0.334f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.32f,
                    dy1 = 0.153f,
                )
                // a 3 3 0 1 0 0.596 5.836
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.596f,
                    dy1 = 5.836f,
                )
                // a 0.334 0.334 0 0 1 0.345 0.086
                arcToRelative(
                    a = 0.334f,
                    b = 0.334f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.345f,
                    dy1 = 0.086f,
                )
                // A 4.99 4.99 0 0 0 7.9 10z
                arcTo(
                    horizontalEllipseRadius = 4.99f,
                    verticalEllipseRadius = 4.99f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.9f,
                    y1 = 10.0f,
                )
                close()
                // m 3.905 -2.896
                moveToRelative(dx = 3.905f, dy = -2.896f)
                // c -0.172 -0.129 -0.438 -0.097 -0.555 0.083
                curveToRelative(
                    dx1 = -0.172f,
                    dy1 = -0.129f,
                    dx2 = -0.438f,
                    dy2 = -0.097f,
                    dx3 = -0.555f,
                    dy3 = 0.083f,
                )
                // A 3.997 3.997 0 0 1 7.9 9
                arcTo(
                    horizontalEllipseRadius = 3.997f,
                    verticalEllipseRadius = 3.997f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.9f,
                    y1 = 9.0f,
                )
                // a 3.996 3.996 0 0 1 -3.297 -1.734
                arcToRelative(
                    a = 3.996f,
                    b = 3.996f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.297f,
                    dy1 = -1.734f,
                )
                // c -0.112 -0.163 -0.347 -0.197 -0.513 -0.089
                curveToRelative(
                    dx1 = -0.112f,
                    dy1 = -0.163f,
                    dx2 = -0.347f,
                    dy2 = -0.197f,
                    dx3 = -0.513f,
                    dy3 = -0.089f,
                )
                // a 2 2 0 1 1 -0.362 -3.54
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.362f,
                    dy1 = -3.54f,
                )
                // c 0.184 0.072 0.408 -0.01 0.485 -0.192
                curveToRelative(
                    dx1 = 0.184f,
                    dy1 = 0.072f,
                    dx2 = 0.408f,
                    dy2 = -0.01f,
                    dx3 = 0.485f,
                    dy3 = -0.192f,
                )
                // a 4.001 4.001 0 0 1 7.398 0.059
                arcToRelative(
                    a = 4.001f,
                    b = 4.001f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 7.398f,
                    dy1 = 0.059f,
                )
                // c 0.08 0.2 0.335 0.282 0.53 0.19
                curveToRelative(
                    dx1 = 0.08f,
                    dy1 = 0.2f,
                    dx2 = 0.335f,
                    dy2 = 0.282f,
                    dx3 = 0.53f,
                    dy3 = 0.19f,
                )
                // a 2 2 0 1 1 -0.335 3.41z
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.335f,
                    dy1 = 3.41f,
                )
                close()
            }
        }.build().also { _ic302 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic302: ImageVector? = null
