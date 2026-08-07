package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1215: ImageVector
    get() {
        val current = _ic1215
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1215",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M12.031 1.421 C11.591 1.87 11 2.633 11 3.5 11 4.88 12.5 6 12.5 6 S14 4.88 14 3.5 c0 -.61 -.292 -1.168 -.619 -1.602 C12.863 .594 11.724 0 10.586 0 h-.08 C9.292 0 8.08 .727 7.515 2.101 c-.082 -.162 -.243 -.323 -.405 -.485 A2.822 2.822 0 0 0 5.17 .808 c-1.173 0 -2.413 .732 -2.632 2.137 -.192 .306 -.34 .668 -.34 1.055 .001 1.105 1.201 2 1.201 2 s1.2 -.895 1.2 -2 c0 -.745 -.547 -1.396 -.902 -1.74 .364 -.435 .934 -.644 1.474 -.644 .565 0 1.05 .162 1.374 .566 .404 .404 .646 .97 .646 1.697 V8 H3.313 l1.455 2.182 2.505 .242 1.535 3.313 -.727 .324 -1.374 -2.91 -2.02 -.161 -1.293 1.858 .97 2.182 -.728 .324 -.97 -2.101 L0 14.14 V16 h10.424 l2.425 -2.99 -1.697 -1.131 -1.617 .404 -.161 -.808 1.616 -.404 L11.879 8 H8 V3.879 C8 1.778 9.293 .809 10.505 .809 c.552 0 1.103 .2 1.526 .612Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 12.031 1.421
                moveTo(x = 12.031f, y = 1.421f)
                // C 11.591 1.87 11 2.633 11 3.5
                curveTo(
                    x1 = 11.591f,
                    y1 = 1.87f,
                    x2 = 11.0f,
                    y2 = 2.633f,
                    x3 = 11.0f,
                    y3 = 3.5f,
                )
                // C 11 4.88 12.5 6 12.5 6
                curveTo(
                    x1 = 11.0f,
                    y1 = 4.88f,
                    x2 = 12.5f,
                    y2 = 6.0f,
                    x3 = 12.5f,
                    y3 = 6.0f,
                )
                // S 14 4.88 14 3.5
                reflectiveCurveTo(
                    x1 = 14.0f,
                    y1 = 4.88f,
                    x2 = 14.0f,
                    y2 = 3.5f,
                )
                // c 0 -0.61 -0.292 -1.168 -0.619 -1.602
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.61f,
                    dx2 = -0.292f,
                    dy2 = -1.168f,
                    dx3 = -0.619f,
                    dy3 = -1.602f,
                )
                // C 12.863 0.594 11.724 0 10.586 0
                curveTo(
                    x1 = 12.863f,
                    y1 = 0.594f,
                    x2 = 11.724f,
                    y2 = 0.0f,
                    x3 = 10.586f,
                    y3 = 0.0f,
                )
                // h -0.08
                horizontalLineToRelative(dx = -0.08f)
                // C 9.292 0 8.08 0.727 7.515 2.101
                curveTo(
                    x1 = 9.292f,
                    y1 = 0.0f,
                    x2 = 8.08f,
                    y2 = 0.727f,
                    x3 = 7.515f,
                    y3 = 2.101f,
                )
                // c -0.082 -0.162 -0.243 -0.323 -0.405 -0.485
                curveToRelative(
                    dx1 = -0.082f,
                    dy1 = -0.162f,
                    dx2 = -0.243f,
                    dy2 = -0.323f,
                    dx3 = -0.405f,
                    dy3 = -0.485f,
                )
                // A 2.822 2.822 0 0 0 5.17 0.808
                arcTo(
                    horizontalEllipseRadius = 2.822f,
                    verticalEllipseRadius = 2.822f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 5.17f,
                    y1 = 0.808f,
                )
                // c -1.173 0 -2.413 0.732 -2.632 2.137
                curveToRelative(
                    dx1 = -1.173f,
                    dy1 = 0.0f,
                    dx2 = -2.413f,
                    dy2 = 0.732f,
                    dx3 = -2.632f,
                    dy3 = 2.137f,
                )
                // c -0.192 0.306 -0.34 0.668 -0.34 1.055
                curveToRelative(
                    dx1 = -0.192f,
                    dy1 = 0.306f,
                    dx2 = -0.34f,
                    dy2 = 0.668f,
                    dx3 = -0.34f,
                    dy3 = 1.055f,
                )
                // c 0.001 1.105 1.201 2 1.201 2
                curveToRelative(
                    dx1 = 0.001f,
                    dy1 = 1.105f,
                    dx2 = 1.201f,
                    dy2 = 2.0f,
                    dx3 = 1.201f,
                    dy3 = 2.0f,
                )
                // s 1.2 -0.895 1.2 -2
                reflectiveCurveToRelative(
                    dx1 = 1.2f,
                    dy1 = -0.895f,
                    dx2 = 1.2f,
                    dy2 = -2.0f,
                )
                // c 0 -0.745 -0.547 -1.396 -0.902 -1.74
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.745f,
                    dx2 = -0.547f,
                    dy2 = -1.396f,
                    dx3 = -0.902f,
                    dy3 = -1.74f,
                )
                // c 0.364 -0.435 0.934 -0.644 1.474 -0.644
                curveToRelative(
                    dx1 = 0.364f,
                    dy1 = -0.435f,
                    dx2 = 0.934f,
                    dy2 = -0.644f,
                    dx3 = 1.474f,
                    dy3 = -0.644f,
                )
                // c 0.565 0 1.05 0.162 1.374 0.566
                curveToRelative(
                    dx1 = 0.565f,
                    dy1 = 0.0f,
                    dx2 = 1.05f,
                    dy2 = 0.162f,
                    dx3 = 1.374f,
                    dy3 = 0.566f,
                )
                // c 0.404 0.404 0.646 0.97 0.646 1.697
                curveToRelative(
                    dx1 = 0.404f,
                    dy1 = 0.404f,
                    dx2 = 0.646f,
                    dy2 = 0.97f,
                    dx3 = 0.646f,
                    dy3 = 1.697f,
                )
                // V 8
                verticalLineTo(y = 8.0f)
                // H 3.313
                horizontalLineTo(x = 3.313f)
                // l 1.455 2.182
                lineToRelative(dx = 1.455f, dy = 2.182f)
                // l 2.505 0.242
                lineToRelative(dx = 2.505f, dy = 0.242f)
                // l 1.535 3.313
                lineToRelative(dx = 1.535f, dy = 3.313f)
                // l -0.727 0.324
                lineToRelative(dx = -0.727f, dy = 0.324f)
                // l -1.374 -2.91
                lineToRelative(dx = -1.374f, dy = -2.91f)
                // l -2.02 -0.161
                lineToRelative(dx = -2.02f, dy = -0.161f)
                // l -1.293 1.858
                lineToRelative(dx = -1.293f, dy = 1.858f)
                // l 0.97 2.182
                lineToRelative(dx = 0.97f, dy = 2.182f)
                // l -0.728 0.324
                lineToRelative(dx = -0.728f, dy = 0.324f)
                // l -0.97 -2.101
                lineToRelative(dx = -0.97f, dy = -2.101f)
                // L 0 14.14
                lineTo(x = 0.0f, y = 14.14f)
                // V 16
                verticalLineTo(y = 16.0f)
                // h 10.424
                horizontalLineToRelative(dx = 10.424f)
                // l 2.425 -2.99
                lineToRelative(dx = 2.425f, dy = -2.99f)
                // l -1.697 -1.131
                lineToRelative(dx = -1.697f, dy = -1.131f)
                // l -1.617 0.404
                lineToRelative(dx = -1.617f, dy = 0.404f)
                // l -0.161 -0.808
                lineToRelative(dx = -0.161f, dy = -0.808f)
                // l 1.616 -0.404
                lineToRelative(dx = 1.616f, dy = -0.404f)
                // L 11.879 8
                lineTo(x = 11.879f, y = 8.0f)
                // H 8
                horizontalLineTo(x = 8.0f)
                // V 3.879
                verticalLineTo(y = 3.879f)
                // C 8 1.778 9.293 0.809 10.505 0.809
                curveTo(
                    x1 = 8.0f,
                    y1 = 1.778f,
                    x2 = 9.293f,
                    y2 = 0.809f,
                    x3 = 10.505f,
                    y3 = 0.809f,
                )
                // c 0.552 0 1.103 0.2 1.526 0.612z
                curveToRelative(
                    dx1 = 0.552f,
                    dy1 = 0.0f,
                    dx2 = 1.103f,
                    dy2 = 0.2f,
                    dx3 = 1.526f,
                    dy3 = 0.612f,
                )
                close()
            }
            // m4.04 10.505 -1.293 1.94 L0 13.332 V8 h2.343 l1.697 2.505Z M12.687 8 l-.97 3.232 1.94 1.374 .97 .808 -.486 .647 -.646 -.485 L11.5 16 H16 V8.08 C14.95 8 13.818 8 12.687 8Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.04 10.505
                moveTo(x = 4.04f, y = 10.505f)
                // l -1.293 1.94
                lineToRelative(dx = -1.293f, dy = 1.94f)
                // L 0 13.332
                lineTo(x = 0.0f, y = 13.332f)
                // V 8
                verticalLineTo(y = 8.0f)
                // h 2.343
                horizontalLineToRelative(dx = 2.343f)
                // l 1.697 2.505z
                lineToRelative(dx = 1.697f, dy = 2.505f)
                close()
                // M 12.687 8
                moveTo(x = 12.687f, y = 8.0f)
                // l -0.97 3.232
                lineToRelative(dx = -0.97f, dy = 3.232f)
                // l 1.94 1.374
                lineToRelative(dx = 1.94f, dy = 1.374f)
                // l 0.97 0.808
                lineToRelative(dx = 0.97f, dy = 0.808f)
                // l -0.486 0.647
                lineToRelative(dx = -0.486f, dy = 0.647f)
                // l -0.646 -0.485
                lineToRelative(dx = -0.646f, dy = -0.485f)
                // L 11.5 16
                lineTo(x = 11.5f, y = 16.0f)
                // H 16
                horizontalLineTo(x = 16.0f)
                // V 8.08
                verticalLineTo(y = 8.08f)
                // C 14.95 8 13.818 8 12.687 8z
                curveTo(
                    x1 = 14.95f,
                    y1 = 8.0f,
                    x2 = 13.818f,
                    y2 = 8.0f,
                    x3 = 12.687f,
                    y3 = 8.0f,
                )
                close()
            }
        }.build().also { _ic1215 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1215: ImageVector? = null
