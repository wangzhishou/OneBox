package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2073: ImageVector
    get() {
        val current = _ic2073
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2073",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M11.5 3 a.5 .5 0 0 0 -.5 .5 v6.063 a2 2 0 1 0 1 0 V3.5 a.5 .5 0 0 0 -.5 -.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.5 3
                moveTo(x = 11.5f, y = 3.0f)
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
            // m10.2 8.399 -.532 .356 a3.3 3.3 0 1 0 3.665 0 l-.533 -.356 V2.5 a1.3 1.3 0 1 0 -2.6 0 v5.899Z M9 2.5 a2.5 2.5 0 0 1 5 0 v5.258 a4.5 4.5 0 1 1 -5 0 V2.5Z M2.07 1.026 c.202 -.099 .405 .104 .356 .32 -.265 1.152 -.246 2.447 .636 3.373 .883 .926 2.42 1.263 3.603 1.075 .222 -.035 .414 .177 .302 .37 a3.689 3.689 0 0 1 -.677 .839 3.773 3.773 0 0 1 -5.278 -.164 3.641 3.641 0 0 1 .166 -5.2 c.273 -.253 .574 -.457 .893 -.613Z
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
                // M 2.07 1.026
                moveTo(x = 2.07f, y = 1.026f)
                // c 0.202 -0.099 0.405 0.104 0.356 0.32
                curveToRelative(
                    dx1 = 0.202f,
                    dy1 = -0.099f,
                    dx2 = 0.405f,
                    dy2 = 0.104f,
                    dx3 = 0.356f,
                    dy3 = 0.32f,
                )
                // c -0.265 1.152 -0.246 2.447 0.636 3.373
                curveToRelative(
                    dx1 = -0.265f,
                    dy1 = 1.152f,
                    dx2 = -0.246f,
                    dy2 = 2.447f,
                    dx3 = 0.636f,
                    dy3 = 3.373f,
                )
                // c 0.883 0.926 2.42 1.263 3.603 1.075
                curveToRelative(
                    dx1 = 0.883f,
                    dy1 = 0.926f,
                    dx2 = 2.42f,
                    dy2 = 1.263f,
                    dx3 = 3.603f,
                    dy3 = 1.075f,
                )
                // c 0.222 -0.035 0.414 0.177 0.302 0.37
                curveToRelative(
                    dx1 = 0.222f,
                    dy1 = -0.035f,
                    dx2 = 0.414f,
                    dy2 = 0.177f,
                    dx3 = 0.302f,
                    dy3 = 0.37f,
                )
                // a 3.689 3.689 0 0 1 -0.677 0.839
                arcToRelative(
                    a = 3.689f,
                    b = 3.689f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.677f,
                    dy1 = 0.839f,
                )
                // a 3.773 3.773 0 0 1 -5.278 -0.164
                arcToRelative(
                    a = 3.773f,
                    b = 3.773f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -5.278f,
                    dy1 = -0.164f,
                )
                // a 3.641 3.641 0 0 1 0.166 -5.2
                arcToRelative(
                    a = 3.641f,
                    b = 3.641f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.166f,
                    dy1 = -5.2f,
                )
                // c 0.273 -0.253 0.574 -0.457 0.893 -0.613z
                curveToRelative(
                    dx1 = 0.273f,
                    dy1 = -0.253f,
                    dx2 = 0.574f,
                    dy2 = -0.457f,
                    dx3 = 0.893f,
                    dy3 = -0.613f,
                )
                close()
            }
            // M5.5 .5 A.5 .5 0 0 0 5 1 v1 H4 a.5 .5 0 0 0 0 1 h1 v1 a.5 .5 0 0 0 1 0 V3 h1 a.5 .5 0 0 0 0 -1 H6 V1 a.5 .5 0 0 0 -.5 -.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5.5 0.5
                moveTo(x = 5.5f, y = 0.5f)
                // A 0.5 0.5 0 0 0 5 1
                arcTo(
                    horizontalEllipseRadius = 0.5f,
                    verticalEllipseRadius = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 5.0f,
                    y1 = 1.0f,
                )
                // v 1
                verticalLineToRelative(dy = 1.0f)
                // H 4
                horizontalLineTo(x = 4.0f)
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
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // v 1
                verticalLineToRelative(dy = 1.0f)
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
                // V 3
                verticalLineTo(y = 3.0f)
                // h 1
                horizontalLineToRelative(dx = 1.0f)
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
                // H 6
                horizontalLineTo(x = 6.0f)
                // V 1
                verticalLineTo(y = 1.0f)
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
        }.build().also { _ic2073 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2073: ImageVector? = null
