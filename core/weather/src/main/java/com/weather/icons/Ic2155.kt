package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2155: ImageVector
    get() {
        val current = _ic2155
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2155",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M1.854 .146 a.5 .5 0 1 0 -.708 .708 l2 2 a.5 .5 0 1 0 .708 -.708 l-2 -2Z M.5 3.5 a.5 .5 0 0 0 0 1 h3 a.5 .5 0 0 0 0 -1 h-3Z m3.354 2.354 a.5 .5 0 1 0 -.708 -.708 l-2 2 a.5 .5 0 1 0 .708 .708 l2 -2Z M14.146 .146 a.5 .5 0 0 1 .708 .708 l-2 2 a.5 .5 0 0 1 -.708 -.708 l2 -2Z M15.5 3.5 a.5 .5 0 0 1 0 1 h-3 a.5 .5 0 0 1 0 -1 h3Z m-3.354 2.354 a.5 .5 0 0 1 .708 -.708 l2 2 a.5 .5 0 0 1 -.708 .708 l-2 -2Z M8 3 a.5 .5 0 0 0 -.5 .5 v6.063 a2 2 0 1 0 1 0 V3.5 A.5 .5 0 0 0 8 3Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 1.854 0.146
                moveTo(x = 1.854f, y = 0.146f)
                // a 0.5 0.5 0 1 0 -0.708 0.708
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.708f,
                    dy1 = 0.708f,
                )
                // l 2 2
                lineToRelative(dx = 2.0f, dy = 2.0f)
                // a 0.5 0.5 0 1 0 0.708 -0.708
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.708f,
                    dy1 = -0.708f,
                )
                // l -2 -2z
                lineToRelative(dx = -2.0f, dy = -2.0f)
                close()
                // M 0.5 3.5
                moveTo(x = 0.5f, y = 3.5f)
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
                // m 3.354 2.354
                moveToRelative(dx = 3.354f, dy = 2.354f)
                // a 0.5 0.5 0 1 0 -0.708 -0.708
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.708f,
                    dy1 = -0.708f,
                )
                // l -2 2
                lineToRelative(dx = -2.0f, dy = 2.0f)
                // a 0.5 0.5 0 1 0 0.708 0.708
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.708f,
                    dy1 = 0.708f,
                )
                // l 2 -2z
                lineToRelative(dx = 2.0f, dy = -2.0f)
                close()
                // M 14.146 0.146
                moveTo(x = 14.146f, y = 0.146f)
                // a 0.5 0.5 0 0 1 0.708 0.708
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.708f,
                    dy1 = 0.708f,
                )
                // l -2 2
                lineToRelative(dx = -2.0f, dy = 2.0f)
                // a 0.5 0.5 0 0 1 -0.708 -0.708
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.708f,
                    dy1 = -0.708f,
                )
                // l 2 -2z
                lineToRelative(dx = 2.0f, dy = -2.0f)
                close()
                // M 15.5 3.5
                moveTo(x = 15.5f, y = 3.5f)
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
                // h -3
                horizontalLineToRelative(dx = -3.0f)
                // a 0.5 0.5 0 0 1 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h 3z
                horizontalLineToRelative(dx = 3.0f)
                close()
                // m -3.354 2.354
                moveToRelative(dx = -3.354f, dy = 2.354f)
                // a 0.5 0.5 0 0 1 0.708 -0.708
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.708f,
                    dy1 = -0.708f,
                )
                // l 2 2
                lineToRelative(dx = 2.0f, dy = 2.0f)
                // a 0.5 0.5 0 0 1 -0.708 0.708
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.708f,
                    dy1 = 0.708f,
                )
                // l -2 -2z
                lineToRelative(dx = -2.0f, dy = -2.0f)
                close()
                // M 8 3
                moveTo(x = 8.0f, y = 3.0f)
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
                // v 6.063
                verticalLineToRelative(dy = 6.063f)
                // a 2 2 0 1 0 1 0
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // V 3.5
                verticalLineTo(y = 3.5f)
                // A 0.5 0.5 0 0 0 8 3z
                arcTo(
                    horizontalEllipseRadius = 0.5f,
                    verticalEllipseRadius = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 8.0f,
                    y1 = 3.0f,
                )
                close()
            }
            // m6.7 8.399 -.532 .356 a3.3 3.3 0 1 0 3.665 0 L9.3 8.399 V2.5 a1.3 1.3 0 0 0 -2.6 0 v5.899Z M5.5 2.5 a2.5 2.5 0 0 1 5 0 v5.258 a4.5 4.5 0 1 1 -5 0 V2.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6.7 8.399
                moveTo(x = 6.7f, y = 8.399f)
                // l -0.532 0.356
                lineToRelative(dx = -0.532f, dy = 0.356f)
                // a 3.3 3.3 0 1 0 3.665 0
                arcToRelative(
                    a = 3.3f,
                    b = 3.3f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 3.665f,
                    dy1 = 0.0f,
                )
                // L 9.3 8.399
                lineTo(x = 9.3f, y = 8.399f)
                // V 2.5
                verticalLineTo(y = 2.5f)
                // a 1.3 1.3 0 0 0 -2.6 0
                arcToRelative(
                    a = 1.3f,
                    b = 1.3f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.6f,
                    dy1 = 0.0f,
                )
                // v 5.899z
                verticalLineToRelative(dy = 5.899f)
                close()
                // M 5.5 2.5
                moveTo(x = 5.5f, y = 2.5f)
                // a 2.5 2.5 0 0 1 5 0
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 5.0f,
                    dy1 = 0.0f,
                )
                // v 5.258
                verticalLineToRelative(dy = 5.258f)
                // a 4.5 4.5 0 1 1 -5 0
                arcToRelative(
                    a = 4.5f,
                    b = 4.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -5.0f,
                    dy1 = 0.0f,
                )
                // V 2.5z
                verticalLineTo(y = 2.5f)
                close()
            }
        }.build().also { _ic2155 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2155: ImageVector? = null
