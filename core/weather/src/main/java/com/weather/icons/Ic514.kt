package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic514: ImageVector
    get() {
        val current = _ic514
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic514",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.5 2 a.5 .5 0 0 0 0 1 h5 a.5 .5 0 0 0 0 -1 h-5Z m3 2 a.5 .5 0 0 0 0 1 h5 a.5 .5 0 0 0 0 -1 h-5Z m-10 7 a.5 .5 0 0 0 0 1 h9 a.5 .5 0 0 0 0 -1 h-9Z m3 2 a.5 .5 0 0 0 0 1 h3 a.5 .5 0 0 0 0 -1 h-3Z m4.5 .5 a.5 .5 0 0 1 .5 -.5 h7 a.5 .5 0 0 1 0 1 h-7 a.5 .5 0 0 1 -.5 -.5Z M4.5 15 a.5 .5 0 0 0 0 1 h9 a.5 .5 0 0 0 0 -1 h-9Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.5 2
                moveTo(x = 7.5f, y = 2.0f)
                // a 0.5 0.5 0 0 0 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // h 5
                horizontalLineToRelative(dx = 5.0f)
                // a 0.5 0.5 0 0 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h -5z
                horizontalLineToRelative(dx = -5.0f)
                close()
                // m 3 2
                moveToRelative(dx = 3.0f, dy = 2.0f)
                // a 0.5 0.5 0 0 0 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // h 5
                horizontalLineToRelative(dx = 5.0f)
                // a 0.5 0.5 0 0 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h -5z
                horizontalLineToRelative(dx = -5.0f)
                close()
                // m -10 7
                moveToRelative(dx = -10.0f, dy = 7.0f)
                // a 0.5 0.5 0 0 0 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // h 9
                horizontalLineToRelative(dx = 9.0f)
                // a 0.5 0.5 0 0 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h -9z
                horizontalLineToRelative(dx = -9.0f)
                close()
                // m 3 2
                moveToRelative(dx = 3.0f, dy = 2.0f)
                // a 0.5 0.5 0 0 0 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // h 3
                horizontalLineToRelative(dx = 3.0f)
                // a 0.5 0.5 0 0 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h -3z
                horizontalLineToRelative(dx = -3.0f)
                close()
                // m 4.5 0.5
                moveToRelative(dx = 4.5f, dy = 0.5f)
                // a 0.5 0.5 0 0 1 0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = -0.5f,
                )
                // h 7
                horizontalLineToRelative(dx = 7.0f)
                // a 0.5 0.5 0 0 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // h -7
                horizontalLineToRelative(dx = -7.0f)
                // a 0.5 0.5 0 0 1 -0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                close()
                // M 4.5 15
                moveTo(x = 4.5f, y = 15.0f)
                // a 0.5 0.5 0 0 0 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // h 9
                horizontalLineToRelative(dx = 9.0f)
                // a 0.5 0.5 0 0 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h -9z
                horizontalLineToRelative(dx = -9.0f)
                close()
            }
            // M9 .121 a4.997 4.997 0 0 0 -5.35 2.243 .334 .334 0 0 1 -.32 .154 3 3 0 1 0 .596 5.836 .334 .334 0 0 1 .345 .086 A4.99 4.99 0 0 0 7.9 10 c1.453 0 2.761 -.62 3.675 -1.61 a.335 .335 0 0 1 .365 -.083 A3 3 0 0 0 15.959 6 h-1.022 a2 2 0 0 1 -3.132 1.104 c-.172 -.129 -.438 -.097 -.555 .083 A3.997 3.997 0 0 1 7.9 9 a3.996 3.996 0 0 1 -3.297 -1.734 c-.112 -.163 -.347 -.197 -.513 -.089 a2 2 0 1 1 -.362 -3.54 c.184 .072 .408 -.01 .485 -.192 A4.001 4.001 0 0 1 9 1.153 V.121Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 9 0.121
                moveTo(x = 9.0f, y = 0.121f)
                // a 4.997 4.997 0 0 0 -5.35 2.243
                arcToRelative(
                    a = 4.997f,
                    b = 4.997f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -5.35f,
                    dy1 = 2.243f,
                )
                // a 0.334 0.334 0 0 1 -0.32 0.154
                arcToRelative(
                    a = 0.334f,
                    b = 0.334f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.32f,
                    dy1 = 0.154f,
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
                // A 4.99 4.99 0 0 0 7.9 10
                arcTo(
                    horizontalEllipseRadius = 4.99f,
                    verticalEllipseRadius = 4.99f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.9f,
                    y1 = 10.0f,
                )
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
                // A 3 3 0 0 0 15.959 6
                arcTo(
                    horizontalEllipseRadius = 3.0f,
                    verticalEllipseRadius = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 15.959f,
                    y1 = 6.0f,
                )
                // h -1.022
                horizontalLineToRelative(dx = -1.022f)
                // a 2 2 0 0 1 -3.132 1.104
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.132f,
                    dy1 = 1.104f,
                )
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
                // A 4.001 4.001 0 0 1 9 1.153
                arcTo(
                    horizontalEllipseRadius = 4.001f,
                    verticalEllipseRadius = 4.001f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 9.0f,
                    y1 = 1.153f,
                )
                // V 0.121z
                verticalLineTo(y = 0.121f)
                close()
            }
        }.build().also { _ic514 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic514: ImageVector? = null
