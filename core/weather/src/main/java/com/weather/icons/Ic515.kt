package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic515: ImageVector
    get() {
        val current = _ic515
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic515",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M.25 11 a.25 .25 0 1 0 0 .5 h5.5 a.25 .25 0 1 0 0 -.5 H.25Z m10 4.5 a.25 .25 0 1 0 0 .5 h5.5 a.25 .25 0 1 0 0 -.5 h-5.5Z M3 12.75 a.25 .25 0 0 1 .25 -.25 h3.5 a.25 .25 0 1 1 0 .5 h-3.5 a.25 .25 0 0 1 -.25 -.25Z M7.25 11 a.25 .25 0 1 0 0 .5 h3.5 a.25 .25 0 1 0 0 -.5 h-3.5Z M0 15.75 a.25 .25 0 0 1 .25 -.25 h3.5 a.25 .25 0 1 1 0 .5 H.25 a.25 .25 0 0 1 -.25 -.25Z m5.25 -.25 a.25 .25 0 1 0 0 .5 h3.5 a.25 .25 0 1 0 0 -.5 h-3.5Z M12 11.25 a.25 .25 0 0 1 .25 -.25 h3.5 a.25 .25 0 1 1 0 .5 h-3.5 a.25 .25 0 0 1 -.25 -.25Z M10.25 14 a.25 .25 0 1 0 0 .5 h3.5 a.25 .25 0 1 0 0 -.5 h-3.5Z M8 12.75 a.25 .25 0 0 1 .25 -.25 h7.5 a.25 .25 0 1 1 0 .5 h-7.5 a.25 .25 0 0 1 -.25 -.25Z M1.25 14 a.25 .25 0 1 0 0 .5 h7.5 a.25 .25 0 1 0 0 -.5 h-7.5Z M10 2.75 a.25 .25 0 0 1 .25 -.25 h5.5 a.25 .25 0 1 1 0 .5 h-5.5 a.25 .25 0 0 1 -.25 -.25Z m-2 1 a.25 .25 0 0 1 .25 -.25 h5.5 a.25 .25 0 1 1 0 .5 h-5.5 A.25 .25 0 0 1 8 3.75Z m2.25 .75 a.25 .25 0 1 0 0 .5 h5.5 a.25 .25 0 1 0 0 -.5 h-5.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0.25 11
                moveTo(x = 0.25f, y = 11.0f)
                // a 0.25 0.25 0 1 0 0 0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                // h 5.5
                horizontalLineToRelative(dx = 5.5f)
                // a 0.25 0.25 0 1 0 0 -0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                )
                // H 0.25z
                horizontalLineTo(x = 0.25f)
                close()
                // m 10 4.5
                moveToRelative(dx = 10.0f, dy = 4.5f)
                // a 0.25 0.25 0 1 0 0 0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                // h 5.5
                horizontalLineToRelative(dx = 5.5f)
                // a 0.25 0.25 0 1 0 0 -0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                )
                // h -5.5z
                horizontalLineToRelative(dx = -5.5f)
                close()
                // M 3 12.75
                moveTo(x = 3.0f, y = 12.75f)
                // a 0.25 0.25 0 0 1 0.25 -0.25
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.25f,
                    dy1 = -0.25f,
                )
                // h 3.5
                horizontalLineToRelative(dx = 3.5f)
                // a 0.25 0.25 0 1 1 0 0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                // h -3.5
                horizontalLineToRelative(dx = -3.5f)
                // a 0.25 0.25 0 0 1 -0.25 -0.25z
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.25f,
                    dy1 = -0.25f,
                )
                close()
                // M 7.25 11
                moveTo(x = 7.25f, y = 11.0f)
                // a 0.25 0.25 0 1 0 0 0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                // h 3.5
                horizontalLineToRelative(dx = 3.5f)
                // a 0.25 0.25 0 1 0 0 -0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                )
                // h -3.5z
                horizontalLineToRelative(dx = -3.5f)
                close()
                // M 0 15.75
                moveTo(x = 0.0f, y = 15.75f)
                // a 0.25 0.25 0 0 1 0.25 -0.25
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.25f,
                    dy1 = -0.25f,
                )
                // h 3.5
                horizontalLineToRelative(dx = 3.5f)
                // a 0.25 0.25 0 1 1 0 0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                // H 0.25
                horizontalLineTo(x = 0.25f)
                // a 0.25 0.25 0 0 1 -0.25 -0.25z
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.25f,
                    dy1 = -0.25f,
                )
                close()
                // m 5.25 -0.25
                moveToRelative(dx = 5.25f, dy = -0.25f)
                // a 0.25 0.25 0 1 0 0 0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                // h 3.5
                horizontalLineToRelative(dx = 3.5f)
                // a 0.25 0.25 0 1 0 0 -0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                )
                // h -3.5z
                horizontalLineToRelative(dx = -3.5f)
                close()
                // M 12 11.25
                moveTo(x = 12.0f, y = 11.25f)
                // a 0.25 0.25 0 0 1 0.25 -0.25
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.25f,
                    dy1 = -0.25f,
                )
                // h 3.5
                horizontalLineToRelative(dx = 3.5f)
                // a 0.25 0.25 0 1 1 0 0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                // h -3.5
                horizontalLineToRelative(dx = -3.5f)
                // a 0.25 0.25 0 0 1 -0.25 -0.25z
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.25f,
                    dy1 = -0.25f,
                )
                close()
                // M 10.25 14
                moveTo(x = 10.25f, y = 14.0f)
                // a 0.25 0.25 0 1 0 0 0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                // h 3.5
                horizontalLineToRelative(dx = 3.5f)
                // a 0.25 0.25 0 1 0 0 -0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                )
                // h -3.5z
                horizontalLineToRelative(dx = -3.5f)
                close()
                // M 8 12.75
                moveTo(x = 8.0f, y = 12.75f)
                // a 0.25 0.25 0 0 1 0.25 -0.25
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.25f,
                    dy1 = -0.25f,
                )
                // h 7.5
                horizontalLineToRelative(dx = 7.5f)
                // a 0.25 0.25 0 1 1 0 0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                // h -7.5
                horizontalLineToRelative(dx = -7.5f)
                // a 0.25 0.25 0 0 1 -0.25 -0.25z
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.25f,
                    dy1 = -0.25f,
                )
                close()
                // M 1.25 14
                moveTo(x = 1.25f, y = 14.0f)
                // a 0.25 0.25 0 1 0 0 0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                // h 7.5
                horizontalLineToRelative(dx = 7.5f)
                // a 0.25 0.25 0 1 0 0 -0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                )
                // h -7.5z
                horizontalLineToRelative(dx = -7.5f)
                close()
                // M 10 2.75
                moveTo(x = 10.0f, y = 2.75f)
                // a 0.25 0.25 0 0 1 0.25 -0.25
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.25f,
                    dy1 = -0.25f,
                )
                // h 5.5
                horizontalLineToRelative(dx = 5.5f)
                // a 0.25 0.25 0 1 1 0 0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                // h -5.5
                horizontalLineToRelative(dx = -5.5f)
                // a 0.25 0.25 0 0 1 -0.25 -0.25z
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.25f,
                    dy1 = -0.25f,
                )
                close()
                // m -2 1
                moveToRelative(dx = -2.0f, dy = 1.0f)
                // a 0.25 0.25 0 0 1 0.25 -0.25
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.25f,
                    dy1 = -0.25f,
                )
                // h 5.5
                horizontalLineToRelative(dx = 5.5f)
                // a 0.25 0.25 0 1 1 0 0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                // h -5.5
                horizontalLineToRelative(dx = -5.5f)
                // A 0.25 0.25 0 0 1 8 3.75z
                arcTo(
                    horizontalEllipseRadius = 0.25f,
                    verticalEllipseRadius = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 8.0f,
                    y1 = 3.75f,
                )
                close()
                // m 2.25 0.75
                moveToRelative(dx = 2.25f, dy = 0.75f)
                // a 0.25 0.25 0 1 0 0 0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                // h 5.5
                horizontalLineToRelative(dx = 5.5f)
                // a 0.25 0.25 0 1 0 0 -0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                )
                // h -5.5z
                horizontalLineToRelative(dx = -5.5f)
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
        }.build().also { _ic515 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic515: ImageVector? = null
