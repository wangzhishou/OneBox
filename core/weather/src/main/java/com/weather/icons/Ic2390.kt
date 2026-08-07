package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2390: ImageVector
    get() {
        val current = _ic2390
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2390",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M11.502 10.998 c-.026 -.268 .205 -.498 .498 -.498 .293 0 .524 .23 .498 .498 l-.225 2.352 h-.546 l-.225 -2.352Z m.873 3.127 a.375 .375 0 1 1 -.75 0 .375 .375 0 0 1 .75 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.502 10.998
                moveTo(x = 11.502f, y = 10.998f)
                // c -0.026 -0.268 0.205 -0.498 0.498 -0.498
                curveToRelative(
                    dx1 = -0.026f,
                    dy1 = -0.268f,
                    dx2 = 0.205f,
                    dy2 = -0.498f,
                    dx3 = 0.498f,
                    dy3 = -0.498f,
                )
                // c 0.293 0 0.524 0.23 0.498 0.498
                curveToRelative(
                    dx1 = 0.293f,
                    dy1 = 0.0f,
                    dx2 = 0.524f,
                    dy2 = 0.23f,
                    dx3 = 0.498f,
                    dy3 = 0.498f,
                )
                // l -0.225 2.352
                lineToRelative(dx = -0.225f, dy = 2.352f)
                // h -0.546
                horizontalLineToRelative(dx = -0.546f)
                // l -0.225 -2.352z
                lineToRelative(dx = -0.225f, dy = -2.352f)
                close()
                // m 0.873 3.127
                moveToRelative(dx = 0.873f, dy = 3.127f)
                // a 0.375 0.375 0 1 1 -0.75 0
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.75f,
                    dy1 = 0.0f,
                )
                // a 0.375 0.375 0 0 1 0.75 0z
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.75f,
                    dy1 = 0.0f,
                )
                close()
            }
            // M11.773 8.63 a.263 .263 0 0 1 .454 0 l3.738 6.482 a.26 .26 0 0 1 -.227 .388 H8.262 a.26 .26 0 0 1 -.227 -.388 l3.738 -6.481Z m3.377 6.27 L12 9.438 8.85 14.9 h6.3Z M.5 1.5 a.5 .5 0 0 0 0 1 h15 a.5 .5 0 0 0 0 -1 H.5Z M.5 4 a.5 .5 0 0 0 0 1 h8 a.5 .5 0 0 0 0 -1 h-8Z M7 7 a.5 .5 0 0 1 .5 -.5 h8 a.5 .5 0 0 1 0 1 h-8 A.5 .5 0 0 1 7 7Z m3.5 -3 a.5 .5 0 0 0 0 1 h5 a.5 .5 0 0 0 0 -1 h-5Z M0 7 a.5 .5 0 0 1 .5 -.5 h5 a.5 .5 0 0 1 0 1 h-5 A.5 .5 0 0 1 0 7Z m.5 2 a.5 .5 0 0 0 0 1 h8 a.5 .5 0 0 0 0 -1 h-8Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.773 8.63
                moveTo(x = 11.773f, y = 8.63f)
                // a 0.263 0.263 0 0 1 0.454 0
                arcToRelative(
                    a = 0.263f,
                    b = 0.263f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.454f,
                    dy1 = 0.0f,
                )
                // l 3.738 6.482
                lineToRelative(dx = 3.738f, dy = 6.482f)
                // a 0.26 0.26 0 0 1 -0.227 0.388
                arcToRelative(
                    a = 0.26f,
                    b = 0.26f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.227f,
                    dy1 = 0.388f,
                )
                // H 8.262
                horizontalLineTo(x = 8.262f)
                // a 0.26 0.26 0 0 1 -0.227 -0.388
                arcToRelative(
                    a = 0.26f,
                    b = 0.26f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.227f,
                    dy1 = -0.388f,
                )
                // l 3.738 -6.481z
                lineToRelative(dx = 3.738f, dy = -6.481f)
                close()
                // m 3.377 6.27
                moveToRelative(dx = 3.377f, dy = 6.27f)
                // L 12 9.438
                lineTo(x = 12.0f, y = 9.438f)
                // L 8.85 14.9
                lineTo(x = 8.85f, y = 14.9f)
                // h 6.3z
                horizontalLineToRelative(dx = 6.3f)
                close()
                // M 0.5 1.5
                moveTo(x = 0.5f, y = 1.5f)
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
                // h 15
                horizontalLineToRelative(dx = 15.0f)
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
                // H 0.5z
                horizontalLineTo(x = 0.5f)
                close()
                // M 0.5 4
                moveTo(x = 0.5f, y = 4.0f)
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
                // h 8
                horizontalLineToRelative(dx = 8.0f)
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
                // h -8z
                horizontalLineToRelative(dx = -8.0f)
                close()
                // M 7 7
                moveTo(x = 7.0f, y = 7.0f)
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
                // h 8
                horizontalLineToRelative(dx = 8.0f)
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
                // h -8
                horizontalLineToRelative(dx = -8.0f)
                // A 0.5 0.5 0 0 1 7 7z
                arcTo(
                    horizontalEllipseRadius = 0.5f,
                    verticalEllipseRadius = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.0f,
                    y1 = 7.0f,
                )
                close()
                // m 3.5 -3
                moveToRelative(dx = 3.5f, dy = -3.0f)
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
                // M 0 7
                moveTo(x = 0.0f, y = 7.0f)
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
                // h 5
                horizontalLineToRelative(dx = 5.0f)
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
                // h -5
                horizontalLineToRelative(dx = -5.0f)
                // A 0.5 0.5 0 0 1 0 7z
                arcTo(
                    horizontalEllipseRadius = 0.5f,
                    verticalEllipseRadius = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 0.0f,
                    y1 = 7.0f,
                )
                close()
                // m 0.5 2
                moveToRelative(dx = 0.5f, dy = 2.0f)
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
                // h 8
                horizontalLineToRelative(dx = 8.0f)
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
                // h -8z
                horizontalLineToRelative(dx = -8.0f)
                close()
            }
        }.build().also { _ic2390 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2390: ImageVector? = null
