package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2032: ImageVector
    get() {
        val current = _ic2032
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2032",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M13.5 6 c0 -.355 -.046 -.7 -.133 -1.027 a2.5 2.5 0 1 0 -2.84 -2.84 A4.005 4.005 0 0 0 5.5 6 a5 5 0 1 0 4.88 3.903 A4.002 4.002 0 0 0 13.5 6Z m1 -3.5 A1.5 1.5 0 0 1 12.965 4 4.02 4.02 0 0 0 11.5 2.535 V2.5 a1.5 1.5 0 0 1 3 0Z m-4.44 6.448 A5.009 5.009 0 0 0 6.503 6.1 a3 3 0 1 1 3.56 2.847Z M9.5 11 a4 4 0 1 1 -8 0 4 4 0 0 1 8 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 13.5 6
                moveTo(x = 13.5f, y = 6.0f)
                // c 0 -0.355 -0.046 -0.7 -0.133 -1.027
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.355f,
                    dx2 = -0.046f,
                    dy2 = -0.7f,
                    dx3 = -0.133f,
                    dy3 = -1.027f,
                )
                // a 2.5 2.5 0 1 0 -2.84 -2.84
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -2.84f,
                    dy1 = -2.84f,
                )
                // A 4.005 4.005 0 0 0 5.5 6
                arcTo(
                    horizontalEllipseRadius = 4.005f,
                    verticalEllipseRadius = 4.005f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 5.5f,
                    y1 = 6.0f,
                )
                // a 5 5 0 1 0 4.88 3.903
                arcToRelative(
                    a = 5.0f,
                    b = 5.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 4.88f,
                    dy1 = 3.903f,
                )
                // A 4.002 4.002 0 0 0 13.5 6z
                arcTo(
                    horizontalEllipseRadius = 4.002f,
                    verticalEllipseRadius = 4.002f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 13.5f,
                    y1 = 6.0f,
                )
                close()
                // m 1 -3.5
                moveToRelative(dx = 1.0f, dy = -3.5f)
                // A 1.5 1.5 0 0 1 12.965 4
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 12.965f,
                    y1 = 4.0f,
                )
                // A 4.02 4.02 0 0 0 11.5 2.535
                arcTo(
                    horizontalEllipseRadius = 4.02f,
                    verticalEllipseRadius = 4.02f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 11.5f,
                    y1 = 2.535f,
                )
                // V 2.5
                verticalLineTo(y = 2.5f)
                // a 1.5 1.5 0 0 1 3 0z
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 3.0f,
                    dy1 = 0.0f,
                )
                close()
                // m -4.44 6.448
                moveToRelative(dx = -4.44f, dy = 6.448f)
                // A 5.009 5.009 0 0 0 6.503 6.1
                arcTo(
                    horizontalEllipseRadius = 5.009f,
                    verticalEllipseRadius = 5.009f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 6.503f,
                    y1 = 6.1f,
                )
                // a 3 3 0 1 1 3.56 2.847z
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 3.56f,
                    dy1 = 2.847f,
                )
                close()
                // M 9.5 11
                moveTo(x = 9.5f, y = 11.0f)
                // a 4 4 0 1 1 -8 0
                arcToRelative(
                    a = 4.0f,
                    b = 4.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -8.0f,
                    dy1 = 0.0f,
                )
                // a 4 4 0 0 1 8 0z
                arcToRelative(
                    a = 4.0f,
                    b = 4.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 8.0f,
                    dy1 = 0.0f,
                )
                close()
            }
            // M5.005 8.605 c-.326 .197 -.553 .635 -.496 .997 l.345 2.198 h1.078 l.358 -2.277 a.846 .846 0 0 0 -.408 -.853 l-.07 -.042 c-.256 -.156 -.55 -.176 -.807 -.023Z M5.4 13.5 a.6 .6 0 1 0 0 -1.2 .6 .6 0 0 0 0 1.2Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5.005 8.605
                moveTo(x = 5.005f, y = 8.605f)
                // c -0.326 0.197 -0.553 0.635 -0.496 0.997
                curveToRelative(
                    dx1 = -0.326f,
                    dy1 = 0.197f,
                    dx2 = -0.553f,
                    dy2 = 0.635f,
                    dx3 = -0.496f,
                    dy3 = 0.997f,
                )
                // l 0.345 2.198
                lineToRelative(dx = 0.345f, dy = 2.198f)
                // h 1.078
                horizontalLineToRelative(dx = 1.078f)
                // l 0.358 -2.277
                lineToRelative(dx = 0.358f, dy = -2.277f)
                // a 0.846 0.846 0 0 0 -0.408 -0.853
                arcToRelative(
                    a = 0.846f,
                    b = 0.846f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.408f,
                    dy1 = -0.853f,
                )
                // l -0.07 -0.042
                lineToRelative(dx = -0.07f, dy = -0.042f)
                // c -0.256 -0.156 -0.55 -0.176 -0.807 -0.023z
                curveToRelative(
                    dx1 = -0.256f,
                    dy1 = -0.156f,
                    dx2 = -0.55f,
                    dy2 = -0.176f,
                    dx3 = -0.807f,
                    dy3 = -0.023f,
                )
                close()
                // M 5.4 13.5
                moveTo(x = 5.4f, y = 13.5f)
                // a 0.6 0.6 0 1 0 0 -1.2
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.2f,
                )
                // a 0.6 0.6 0 0 0 0 1.2z
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.2f,
                )
                close()
            }
        }.build().also { _ic2032 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2032: ImageVector? = null
