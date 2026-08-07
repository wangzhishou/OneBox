package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1041: ImageVector
    get() {
        val current = _ic1041
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1041",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M13.475 2.625 c-.787 .263 -1.05 .962 -.963 1.4 -.612 -.7 -.612 -1.488 -.524 -2.713 -1.838 .7 -1.4 2.8 -1.488 3.5 -.438 -.437 -.525 -1.312 -.525 -1.312 -.525 .263 -.787 .963 -.787 1.575 0 1.4 1.137 2.362 2.45 2.362 A2.353 2.353 0 0 0 14 5.075 c0 -.963 -.525 -1.225 -.525 -2.45Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 13.475 2.625
                moveTo(x = 13.475f, y = 2.625f)
                // c -0.787 0.263 -1.05 0.962 -0.963 1.4
                curveToRelative(
                    dx1 = -0.787f,
                    dy1 = 0.263f,
                    dx2 = -1.05f,
                    dy2 = 0.962f,
                    dx3 = -0.963f,
                    dy3 = 1.4f,
                )
                // c -0.612 -0.7 -0.612 -1.488 -0.524 -2.713
                curveToRelative(
                    dx1 = -0.612f,
                    dy1 = -0.7f,
                    dx2 = -0.612f,
                    dy2 = -1.488f,
                    dx3 = -0.524f,
                    dy3 = -2.713f,
                )
                // c -1.838 0.7 -1.4 2.8 -1.488 3.5
                curveToRelative(
                    dx1 = -1.838f,
                    dy1 = 0.7f,
                    dx2 = -1.4f,
                    dy2 = 2.8f,
                    dx3 = -1.488f,
                    dy3 = 3.5f,
                )
                // c -0.438 -0.437 -0.525 -1.312 -0.525 -1.312
                curveToRelative(
                    dx1 = -0.438f,
                    dy1 = -0.437f,
                    dx2 = -0.525f,
                    dy2 = -1.312f,
                    dx3 = -0.525f,
                    dy3 = -1.312f,
                )
                // c -0.525 0.263 -0.787 0.963 -0.787 1.575
                curveToRelative(
                    dx1 = -0.525f,
                    dy1 = 0.263f,
                    dx2 = -0.787f,
                    dy2 = 0.963f,
                    dx3 = -0.787f,
                    dy3 = 1.575f,
                )
                // c 0 1.4 1.137 2.362 2.45 2.362
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.4f,
                    dx2 = 1.137f,
                    dy2 = 2.362f,
                    dx3 = 2.45f,
                    dy3 = 2.362f,
                )
                // A 2.353 2.353 0 0 0 14 5.075
                arcTo(
                    horizontalEllipseRadius = 2.353f,
                    verticalEllipseRadius = 2.353f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 14.0f,
                    y1 = 5.075f,
                )
                // c 0 -0.963 -0.525 -1.225 -0.525 -2.45z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.963f,
                    dx2 = -0.525f,
                    dy2 = -1.225f,
                    dx3 = -0.525f,
                    dy3 = -2.45f,
                )
                close()
            }
            // M8.313 4.988 c0 1.575 1.05 2.8 2.45 3.15 H8.75 l3.37 3.69 a.2 .2 0 0 1 -.148 .335 H7.175 v1.717 a46.583 46.583 0 0 0 -1.925 -.265 v-1.453 H.447 a.2 .2 0 0 1 -.15 -.333 L3.676 8.05 H1.988 a.2 .2 0 0 1 -.157 -.324 L4.2 4.725 H3.028 a.2 .2 0 0 1 -.16 -.32 l3.188 -4.2 a.2 .2 0 0 1 .317 -.002 l2.465 3.122 a3.092 3.092 0 0 0 -.525 1.663Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.313 4.988
                moveTo(x = 8.313f, y = 4.988f)
                // c 0 1.575 1.05 2.8 2.45 3.15
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.575f,
                    dx2 = 1.05f,
                    dy2 = 2.8f,
                    dx3 = 2.45f,
                    dy3 = 3.15f,
                )
                // H 8.75
                horizontalLineTo(x = 8.75f)
                // l 3.37 3.69
                lineToRelative(dx = 3.37f, dy = 3.69f)
                // a 0.2 0.2 0 0 1 -0.148 0.335
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.148f,
                    dy1 = 0.335f,
                )
                // H 7.175
                horizontalLineTo(x = 7.175f)
                // v 1.717
                verticalLineToRelative(dy = 1.717f)
                // a 46.583 46.583 0 0 0 -1.925 -0.265
                arcToRelative(
                    a = 46.583f,
                    b = 46.583f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.925f,
                    dy1 = -0.265f,
                )
                // v -1.453
                verticalLineToRelative(dy = -1.453f)
                // H 0.447
                horizontalLineTo(x = 0.447f)
                // a 0.2 0.2 0 0 1 -0.15 -0.333
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.15f,
                    dy1 = -0.333f,
                )
                // L 3.676 8.05
                lineTo(x = 3.676f, y = 8.05f)
                // H 1.988
                horizontalLineTo(x = 1.988f)
                // a 0.2 0.2 0 0 1 -0.157 -0.324
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.157f,
                    dy1 = -0.324f,
                )
                // L 4.2 4.725
                lineTo(x = 4.2f, y = 4.725f)
                // H 3.028
                horizontalLineTo(x = 3.028f)
                // a 0.2 0.2 0 0 1 -0.16 -0.32
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.16f,
                    dy1 = -0.32f,
                )
                // l 3.188 -4.2
                lineToRelative(dx = 3.188f, dy = -4.2f)
                // a 0.2 0.2 0 0 1 0.317 -0.002
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.317f,
                    dy1 = -0.002f,
                )
                // l 2.465 3.122
                lineToRelative(dx = 2.465f, dy = 3.122f)
                // a 3.092 3.092 0 0 0 -0.525 1.663z
                arcToRelative(
                    a = 3.092f,
                    b = 3.092f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.525f,
                    dy1 = 1.663f,
                )
                close()
            }
            // M0 14 s1 -.5 3.5 -.5 6.5 1 8.5 1 4 -.5 4 -.5 v2 H0 v-2Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0 14
                moveTo(x = 0.0f, y = 14.0f)
                // s 1 -0.5 3.5 -0.5
                reflectiveCurveToRelative(
                    dx1 = 1.0f,
                    dy1 = -0.5f,
                    dx2 = 3.5f,
                    dy2 = -0.5f,
                )
                // s 6.5 1 8.5 1
                reflectiveCurveToRelative(
                    dx1 = 6.5f,
                    dy1 = 1.0f,
                    dx2 = 8.5f,
                    dy2 = 1.0f,
                )
                // s 4 -0.5 4 -0.5
                reflectiveCurveToRelative(
                    dx1 = 4.0f,
                    dy1 = -0.5f,
                    dx2 = 4.0f,
                    dy2 = -0.5f,
                )
                // v 2
                verticalLineToRelative(dy = 2.0f)
                // H 0
                horizontalLineTo(x = 0.0f)
                // v -2z
                verticalLineToRelative(dy = -2.0f)
                close()
            }
        }.build().also { _ic1041 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1041: ImageVector? = null
