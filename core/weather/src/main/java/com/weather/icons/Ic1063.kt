package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1063: ImageVector
    get() {
        val current = _ic1063
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1063",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M.293 10.707 A1 1 0 0 1 0 10 c0 -.5 .555 -1.395 1 -2 .445 .605 1 1.5 1 2 a1 1 0 0 1 -1.707 .707Z m3 3 A1 1 0 0 1 3 13 c0 -.5 .555 -1.395 1 -2 .445 .605 1 1.5 1 2 a1 1 0 0 1 -1.707 .707Z M7 15 a1 1 0 1 0 2 0 c0 -.5 -.555 -1.395 -1 -2 -.445 .605 -1 1.5 -1 2Z m4.293 -1.293 A1 1 0 0 1 11 13 c0 -.5 .555 -1.395 1 -2 .445 .605 1 1.5 1 2 a1 1 0 0 1 -1.707 .707Z M14 10 a1 1 0 0 0 2 0 c0 -.5 -.555 -1.395 -1 -2 -.445 .605 -1 1.5 -1 2Z M8.5 4.5 a.5 .5 0 0 0 -1 0 v2.3 a.7 .7 0 0 0 .7 .7 h2.3 a.5 .5 0 0 0 0 -1 h-2 v-2Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0.293 10.707
                moveTo(x = 0.293f, y = 10.707f)
                // A 1 1 0 0 1 0 10
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 0.0f,
                    y1 = 10.0f,
                )
                // c 0 -0.5 0.555 -1.395 1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = 0.555f,
                    dy2 = -1.395f,
                    dx3 = 1.0f,
                    dy3 = -2.0f,
                )
                // c 0.445 0.605 1 1.5 1 2
                curveToRelative(
                    dx1 = 0.445f,
                    dy1 = 0.605f,
                    dx2 = 1.0f,
                    dy2 = 1.5f,
                    dx3 = 1.0f,
                    dy3 = 2.0f,
                )
                // a 1 1 0 0 1 -1.707 0.707z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.707f,
                    dy1 = 0.707f,
                )
                close()
                // m 3 3
                moveToRelative(dx = 3.0f, dy = 3.0f)
                // A 1 1 0 0 1 3 13
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 3.0f,
                    y1 = 13.0f,
                )
                // c 0 -0.5 0.555 -1.395 1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = 0.555f,
                    dy2 = -1.395f,
                    dx3 = 1.0f,
                    dy3 = -2.0f,
                )
                // c 0.445 0.605 1 1.5 1 2
                curveToRelative(
                    dx1 = 0.445f,
                    dy1 = 0.605f,
                    dx2 = 1.0f,
                    dy2 = 1.5f,
                    dx3 = 1.0f,
                    dy3 = 2.0f,
                )
                // a 1 1 0 0 1 -1.707 0.707z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.707f,
                    dy1 = 0.707f,
                )
                close()
                // M 7 15
                moveTo(x = 7.0f, y = 15.0f)
                // a 1 1 0 1 0 2 0
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 2.0f,
                    dy1 = 0.0f,
                )
                // c 0 -0.5 -0.555 -1.395 -1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = -0.555f,
                    dy2 = -1.395f,
                    dx3 = -1.0f,
                    dy3 = -2.0f,
                )
                // c -0.445 0.605 -1 1.5 -1 2z
                curveToRelative(
                    dx1 = -0.445f,
                    dy1 = 0.605f,
                    dx2 = -1.0f,
                    dy2 = 1.5f,
                    dx3 = -1.0f,
                    dy3 = 2.0f,
                )
                close()
                // m 4.293 -1.293
                moveToRelative(dx = 4.293f, dy = -1.293f)
                // A 1 1 0 0 1 11 13
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 11.0f,
                    y1 = 13.0f,
                )
                // c 0 -0.5 0.555 -1.395 1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = 0.555f,
                    dy2 = -1.395f,
                    dx3 = 1.0f,
                    dy3 = -2.0f,
                )
                // c 0.445 0.605 1 1.5 1 2
                curveToRelative(
                    dx1 = 0.445f,
                    dy1 = 0.605f,
                    dx2 = 1.0f,
                    dy2 = 1.5f,
                    dx3 = 1.0f,
                    dy3 = 2.0f,
                )
                // a 1 1 0 0 1 -1.707 0.707z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.707f,
                    dy1 = 0.707f,
                )
                close()
                // M 14 10
                moveTo(x = 14.0f, y = 10.0f)
                // a 1 1 0 0 0 2 0
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.0f,
                    dy1 = 0.0f,
                )
                // c 0 -0.5 -0.555 -1.395 -1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = -0.555f,
                    dy2 = -1.395f,
                    dx3 = -1.0f,
                    dy3 = -2.0f,
                )
                // c -0.445 0.605 -1 1.5 -1 2z
                curveToRelative(
                    dx1 = -0.445f,
                    dy1 = 0.605f,
                    dx2 = -1.0f,
                    dy2 = 1.5f,
                    dx3 = -1.0f,
                    dy3 = 2.0f,
                )
                close()
                // M 8.5 4.5
                moveTo(x = 8.5f, y = 4.5f)
                // a 0.5 0.5 0 0 0 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // v 2.3
                verticalLineToRelative(dy = 2.3f)
                // a 0.7 0.7 0 0 0 0.7 0.7
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.7f,
                    dy1 = 0.7f,
                )
                // h 2.3
                horizontalLineToRelative(dx = 2.3f)
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
                // h -2
                horizontalLineToRelative(dx = -2.0f)
                // v -2z
                verticalLineToRelative(dy = -2.0f)
                close()
            }
            // M7.1 0 a.6 .6 0 0 0 0 1.2 h.3 v.836 a5 5 0 1 0 4.407 1.723 L12 3.566 l.217 .217 a.4 .4 0 1 0 .566 -.566 l-1 -1 a.4 .4 0 1 0 -.566 .566 l.217 .217 -.193 .193 A4.979 4.979 0 0 0 8.6 2.036 V1.2 h.3 a.6 .6 0 0 0 0 -1.2 H7.1Z m4.7 7 a3.8 3.8 0 1 1 -7.6 0 3.8 3.8 0 0 1 7.6 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.1 0
                moveTo(x = 7.1f, y = 0.0f)
                // a 0.6 0.6 0 0 0 0 1.2
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.2f,
                )
                // h 0.3
                horizontalLineToRelative(dx = 0.3f)
                // v 0.836
                verticalLineToRelative(dy = 0.836f)
                // a 5 5 0 1 0 4.407 1.723
                arcToRelative(
                    a = 5.0f,
                    b = 5.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 4.407f,
                    dy1 = 1.723f,
                )
                // L 12 3.566
                lineTo(x = 12.0f, y = 3.566f)
                // l 0.217 0.217
                lineToRelative(dx = 0.217f, dy = 0.217f)
                // a 0.4 0.4 0 1 0 0.566 -0.566
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.566f,
                    dy1 = -0.566f,
                )
                // l -1 -1
                lineToRelative(dx = -1.0f, dy = -1.0f)
                // a 0.4 0.4 0 1 0 -0.566 0.566
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.566f,
                    dy1 = 0.566f,
                )
                // l 0.217 0.217
                lineToRelative(dx = 0.217f, dy = 0.217f)
                // l -0.193 0.193
                lineToRelative(dx = -0.193f, dy = 0.193f)
                // A 4.979 4.979 0 0 0 8.6 2.036
                arcTo(
                    horizontalEllipseRadius = 4.979f,
                    verticalEllipseRadius = 4.979f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 8.6f,
                    y1 = 2.036f,
                )
                // V 1.2
                verticalLineTo(y = 1.2f)
                // h 0.3
                horizontalLineToRelative(dx = 0.3f)
                // a 0.6 0.6 0 0 0 0 -1.2
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.2f,
                )
                // H 7.1z
                horizontalLineTo(x = 7.1f)
                close()
                // m 4.7 7
                moveToRelative(dx = 4.7f, dy = 7.0f)
                // a 3.8 3.8 0 1 1 -7.6 0
                arcToRelative(
                    a = 3.8f,
                    b = 3.8f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -7.6f,
                    dy1 = 0.0f,
                )
                // a 3.8 3.8 0 0 1 7.6 0z
                arcToRelative(
                    a = 3.8f,
                    b = 3.8f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 7.6f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic1063 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1063: ImageVector? = null
