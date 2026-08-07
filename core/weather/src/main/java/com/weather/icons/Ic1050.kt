package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1050: ImageVector
    get() {
        val current = _ic1050
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1050",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M11.5 7 a.5 .5 0 0 0 -.5 .5 v2.063 a2 2 0 1 0 1 0 V7.5 a.5 .5 0 0 0 -.5 -.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.5 7
                moveTo(x = 11.5f, y = 7.0f)
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
                // v 2.063
                verticalLineToRelative(dy = 2.063f)
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
                // V 7.5
                verticalLineTo(y = 7.5f)
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
            // m10.2 8.399 -.532 .356 a3.3 3.3 0 1 0 3.665 0 l-.533 -.356 V2.5 a1.3 1.3 0 1 0 -2.6 0 v5.899Z M9 2.5 a2.5 2.5 0 0 1 5 0 v5.258 a4.5 4.5 0 1 1 -5 0 V2.5Z M4.002 1 a.7 .7 0 0 0 -.7 .7 v1.088 l-.942 -.544 a.7 .7 0 0 0 -.7 1.212 L2.602 4 l-.942 .544 a.7 .7 0 0 0 .7 1.212 l.942 -.544 V6.3 a.7 .7 0 1 0 1.4 0 V5.212 l.942 .544 a.7 .7 0 1 0 .7 -1.212 L5.402 4 l.942 -.544 a.7 .7 0 1 0 -.7 -1.212 l-.942 .544 V1.7 a.7 .7 0 0 0 -.7 -.7Z M3.001 8 a.467 .467 0 0 0 -.466 .467 v.725 l-.628 -.363 a.467 .467 0 0 0 -.467 .808 l.628 .363 -.628 .363 a.467 .467 0 0 0 .467 .808 l.628 -.363 v.725 a.467 .467 0 1 0 .933 0 v-.725 l.628 .363 a.467 .467 0 1 0 .467 -.808 L3.935 10 l.628 -.363 a.467 .467 0 1 0 -.467 -.808 l-.628 .363 v-.725 A.467 .467 0 0 0 3.001 8Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.2 8.399
                moveTo(x = 10.2f, y = 8.399f)
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
                // l -0.533 -0.356
                lineToRelative(dx = -0.533f, dy = -0.356f)
                // V 2.5
                verticalLineTo(y = 2.5f)
                // a 1.3 1.3 0 1 0 -2.6 0
                arcToRelative(
                    a = 1.3f,
                    b = 1.3f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -2.6f,
                    dy1 = 0.0f,
                )
                // v 5.899z
                verticalLineToRelative(dy = 5.899f)
                close()
                // M 9 2.5
                moveTo(x = 9.0f, y = 2.5f)
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
                // M 4.002 1
                moveTo(x = 4.002f, y = 1.0f)
                // a 0.7 0.7 0 0 0 -0.7 0.7
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.7f,
                    dy1 = 0.7f,
                )
                // v 1.088
                verticalLineToRelative(dy = 1.088f)
                // l -0.942 -0.544
                lineToRelative(dx = -0.942f, dy = -0.544f)
                // a 0.7 0.7 0 0 0 -0.7 1.212
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.7f,
                    dy1 = 1.212f,
                )
                // L 2.602 4
                lineTo(x = 2.602f, y = 4.0f)
                // l -0.942 0.544
                lineToRelative(dx = -0.942f, dy = 0.544f)
                // a 0.7 0.7 0 0 0 0.7 1.212
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.7f,
                    dy1 = 1.212f,
                )
                // l 0.942 -0.544
                lineToRelative(dx = 0.942f, dy = -0.544f)
                // V 6.3
                verticalLineTo(y = 6.3f)
                // a 0.7 0.7 0 1 0 1.4 0
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 1.4f,
                    dy1 = 0.0f,
                )
                // V 5.212
                verticalLineTo(y = 5.212f)
                // l 0.942 0.544
                lineToRelative(dx = 0.942f, dy = 0.544f)
                // a 0.7 0.7 0 1 0 0.7 -1.212
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.7f,
                    dy1 = -1.212f,
                )
                // L 5.402 4
                lineTo(x = 5.402f, y = 4.0f)
                // l 0.942 -0.544
                lineToRelative(dx = 0.942f, dy = -0.544f)
                // a 0.7 0.7 0 1 0 -0.7 -1.212
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.7f,
                    dy1 = -1.212f,
                )
                // l -0.942 0.544
                lineToRelative(dx = -0.942f, dy = 0.544f)
                // V 1.7
                verticalLineTo(y = 1.7f)
                // a 0.7 0.7 0 0 0 -0.7 -0.7z
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.7f,
                    dy1 = -0.7f,
                )
                close()
                // M 3.001 8
                moveTo(x = 3.001f, y = 8.0f)
                // a 0.467 0.467 0 0 0 -0.466 0.467
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.466f,
                    dy1 = 0.467f,
                )
                // v 0.725
                verticalLineToRelative(dy = 0.725f)
                // l -0.628 -0.363
                lineToRelative(dx = -0.628f, dy = -0.363f)
                // a 0.467 0.467 0 0 0 -0.467 0.808
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.467f,
                    dy1 = 0.808f,
                )
                // l 0.628 0.363
                lineToRelative(dx = 0.628f, dy = 0.363f)
                // l -0.628 0.363
                lineToRelative(dx = -0.628f, dy = 0.363f)
                // a 0.467 0.467 0 0 0 0.467 0.808
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.467f,
                    dy1 = 0.808f,
                )
                // l 0.628 -0.363
                lineToRelative(dx = 0.628f, dy = -0.363f)
                // v 0.725
                verticalLineToRelative(dy = 0.725f)
                // a 0.467 0.467 0 1 0 0.933 0
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.933f,
                    dy1 = 0.0f,
                )
                // v -0.725
                verticalLineToRelative(dy = -0.725f)
                // l 0.628 0.363
                lineToRelative(dx = 0.628f, dy = 0.363f)
                // a 0.467 0.467 0 1 0 0.467 -0.808
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.467f,
                    dy1 = -0.808f,
                )
                // L 3.935 10
                lineTo(x = 3.935f, y = 10.0f)
                // l 0.628 -0.363
                lineToRelative(dx = 0.628f, dy = -0.363f)
                // a 0.467 0.467 0 1 0 -0.467 -0.808
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.467f,
                    dy1 = -0.808f,
                )
                // l -0.628 0.363
                lineToRelative(dx = -0.628f, dy = 0.363f)
                // v -0.725
                verticalLineToRelative(dy = -0.725f)
                // A 0.467 0.467 0 0 0 3.001 8z
                arcTo(
                    horizontalEllipseRadius = 0.467f,
                    verticalEllipseRadius = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 3.001f,
                    y1 = 8.0f,
                )
                close()
            }
        }.build().also { _ic1050 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1050: ImageVector? = null
