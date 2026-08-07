package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic901Fill: ImageVector
    get() {
        val current = _ic901Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic901Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M11.5 0 A2.5 2.5 0 0 0 9 2.5 v4.99 a.534 .534 0 0 1 -.217 .423 4.5 4.5 0 1 0 5.435 0 A.534 .534 0 0 1 14 7.49 V2.5 A2.5 2.5 0 0 0 11.5 0Z m2 11.5 A2 2 0 1 1 11 9.563 V7.5 a.5 .5 0 0 1 1 0 v2.063 a2 2 0 0 1 1.5 1.937Z M2.302 1.7 a.7 .7 0 0 1 1.4 0 v1.088 l.942 -.544 a.7 .7 0 1 1 .7 1.212 L4.402 4 l.942 .544 a.7 .7 0 1 1 -.7 1.212 l-.942 -.544 V6.3 a.7 .7 0 1 1 -1.4 0 V5.212 l-.942 .544 a.7 .7 0 0 1 -.7 -1.212 L1.602 4 .66 3.456 a.7 .7 0 0 1 .7 -1.212 l.942 .544 V1.7Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.5 0
                moveTo(x = 11.5f, y = 0.0f)
                // A 2.5 2.5 0 0 0 9 2.5
                arcTo(
                    horizontalEllipseRadius = 2.5f,
                    verticalEllipseRadius = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 9.0f,
                    y1 = 2.5f,
                )
                // v 4.99
                verticalLineToRelative(dy = 4.99f)
                // a 0.534 0.534 0 0 1 -0.217 0.423
                arcToRelative(
                    a = 0.534f,
                    b = 0.534f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.217f,
                    dy1 = 0.423f,
                )
                // a 4.5 4.5 0 1 0 5.435 0
                arcToRelative(
                    a = 4.5f,
                    b = 4.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 5.435f,
                    dy1 = 0.0f,
                )
                // A 0.534 0.534 0 0 1 14 7.49
                arcTo(
                    horizontalEllipseRadius = 0.534f,
                    verticalEllipseRadius = 0.534f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 14.0f,
                    y1 = 7.49f,
                )
                // V 2.5
                verticalLineTo(y = 2.5f)
                // A 2.5 2.5 0 0 0 11.5 0z
                arcTo(
                    horizontalEllipseRadius = 2.5f,
                    verticalEllipseRadius = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 11.5f,
                    y1 = 0.0f,
                )
                close()
                // m 2 11.5
                moveToRelative(dx = 2.0f, dy = 11.5f)
                // A 2 2 0 1 1 11 9.563
                arcTo(
                    horizontalEllipseRadius = 2.0f,
                    verticalEllipseRadius = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 11.0f,
                    y1 = 9.563f,
                )
                // V 7.5
                verticalLineTo(y = 7.5f)
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
                // v 2.063
                verticalLineToRelative(dy = 2.063f)
                // a 2 2 0 0 1 1.5 1.937z
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.5f,
                    dy1 = 1.937f,
                )
                close()
                // M 2.302 1.7
                moveTo(x = 2.302f, y = 1.7f)
                // a 0.7 0.7 0 0 1 1.4 0
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.4f,
                    dy1 = 0.0f,
                )
                // v 1.088
                verticalLineToRelative(dy = 1.088f)
                // l 0.942 -0.544
                lineToRelative(dx = 0.942f, dy = -0.544f)
                // a 0.7 0.7 0 1 1 0.7 1.212
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.7f,
                    dy1 = 1.212f,
                )
                // L 4.402 4
                lineTo(x = 4.402f, y = 4.0f)
                // l 0.942 0.544
                lineToRelative(dx = 0.942f, dy = 0.544f)
                // a 0.7 0.7 0 1 1 -0.7 1.212
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.7f,
                    dy1 = 1.212f,
                )
                // l -0.942 -0.544
                lineToRelative(dx = -0.942f, dy = -0.544f)
                // V 6.3
                verticalLineTo(y = 6.3f)
                // a 0.7 0.7 0 1 1 -1.4 0
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.4f,
                    dy1 = 0.0f,
                )
                // V 5.212
                verticalLineTo(y = 5.212f)
                // l -0.942 0.544
                lineToRelative(dx = -0.942f, dy = 0.544f)
                // a 0.7 0.7 0 0 1 -0.7 -1.212
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.7f,
                    dy1 = -1.212f,
                )
                // L 1.602 4
                lineTo(x = 1.602f, y = 4.0f)
                // L 0.66 3.456
                lineTo(x = 0.66f, y = 3.456f)
                // a 0.7 0.7 0 0 1 0.7 -1.212
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.7f,
                    dy1 = -1.212f,
                )
                // l 0.942 0.544
                lineToRelative(dx = 0.942f, dy = 0.544f)
                // V 1.7z
                verticalLineTo(y = 1.7f)
                close()
            }
        }.build().also { _ic901Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic901Fill: ImageVector? = null
