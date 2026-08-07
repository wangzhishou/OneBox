package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1049: ImageVector
    get() {
        val current = _ic1049
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1049",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M1.293 14.707 A1 1 0 0 1 1 14 c0 -.5 .555 -1.395 1 -2 .445 .605 1 1.5 1 2 a1 1 0 0 1 -1.707 .707Z m3 1 A1 1 0 0 1 4 15 c0 -.5 .555 -1.395 1 -2 .445 .605 1 1.5 1 2 a1 1 0 0 1 -1.707 .707Z M7 15 a1 1 0 1 0 2 0 c0 -.5 -.555 -1.395 -1 -2 -.445 .605 -1 1.5 -1 2Z m3.293 .707 A1 1 0 0 1 10 15 c0 -.5 .555 -1.395 1 -2 .445 .605 1 1.5 1 2 a1 1 0 0 1 -1.707 .707Z M13 14 a1 1 0 0 0 2 0 c0 -.5 -.555 -1.395 -1 -2 -.445 .605 -1 1.5 -1 2Z M9.114 .001 C5.651 -.059 3.018 2.34 2.774 5.26 c-.27 3.22 1.94 4.56 2.236 4.823 -1.756 -.027 -3.846 -1.98 -4.197 -3.81 a.096 .096 0 0 0 -.093 -.078 .095 .095 0 0 0 -.094 .105 c.371 3.25 3.138 5.715 6.258 5.7 3.322 -.014 6.008 -1.978 6.35 -5.203 C13.558 3.76 11.292 2.181 11 1.92 c1.865 .018 3.837 1.982 4.184 3.81 a.095 .095 0 0 0 .091 .077 .095 .095 0 0 0 .096 -.105 c-.374 -3.245 -3.138 -5.645 -6.257 -5.7Z M8 7.386 a1.385 1.385 0 1 1 0 -2.77 1.385 1.385 0 0 1 0 2.77Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 1.293 14.707
                moveTo(x = 1.293f, y = 14.707f)
                // A 1 1 0 0 1 1 14
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 1.0f,
                    y1 = 14.0f,
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
                // m 3 1
                moveToRelative(dx = 3.0f, dy = 1.0f)
                // A 1 1 0 0 1 4 15
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 4.0f,
                    y1 = 15.0f,
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
                // m 3.293 0.707
                moveToRelative(dx = 3.293f, dy = 0.707f)
                // A 1 1 0 0 1 10 15
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 10.0f,
                    y1 = 15.0f,
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
                // M 13 14
                moveTo(x = 13.0f, y = 14.0f)
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
                // M 9.114 0.001
                moveTo(x = 9.114f, y = 0.001f)
                // C 5.651 -0.059 3.018 2.34 2.774 5.26
                curveTo(
                    x1 = 5.651f,
                    y1 = -0.059f,
                    x2 = 3.018f,
                    y2 = 2.34f,
                    x3 = 2.774f,
                    y3 = 5.26f,
                )
                // c -0.27 3.22 1.94 4.56 2.236 4.823
                curveToRelative(
                    dx1 = -0.27f,
                    dy1 = 3.22f,
                    dx2 = 1.94f,
                    dy2 = 4.56f,
                    dx3 = 2.236f,
                    dy3 = 4.823f,
                )
                // c -1.756 -0.027 -3.846 -1.98 -4.197 -3.81
                curveToRelative(
                    dx1 = -1.756f,
                    dy1 = -0.027f,
                    dx2 = -3.846f,
                    dy2 = -1.98f,
                    dx3 = -4.197f,
                    dy3 = -3.81f,
                )
                // a 0.096 0.096 0 0 0 -0.093 -0.078
                arcToRelative(
                    a = 0.096f,
                    b = 0.096f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.093f,
                    dy1 = -0.078f,
                )
                // a 0.095 0.095 0 0 0 -0.094 0.105
                arcToRelative(
                    a = 0.095f,
                    b = 0.095f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.094f,
                    dy1 = 0.105f,
                )
                // c 0.371 3.25 3.138 5.715 6.258 5.7
                curveToRelative(
                    dx1 = 0.371f,
                    dy1 = 3.25f,
                    dx2 = 3.138f,
                    dy2 = 5.715f,
                    dx3 = 6.258f,
                    dy3 = 5.7f,
                )
                // c 3.322 -0.014 6.008 -1.978 6.35 -5.203
                curveToRelative(
                    dx1 = 3.322f,
                    dy1 = -0.014f,
                    dx2 = 6.008f,
                    dy2 = -1.978f,
                    dx3 = 6.35f,
                    dy3 = -5.203f,
                )
                // C 13.558 3.76 11.292 2.181 11 1.92
                curveTo(
                    x1 = 13.558f,
                    y1 = 3.76f,
                    x2 = 11.292f,
                    y2 = 2.181f,
                    x3 = 11.0f,
                    y3 = 1.92f,
                )
                // c 1.865 0.018 3.837 1.982 4.184 3.81
                curveToRelative(
                    dx1 = 1.865f,
                    dy1 = 0.018f,
                    dx2 = 3.837f,
                    dy2 = 1.982f,
                    dx3 = 4.184f,
                    dy3 = 3.81f,
                )
                // a 0.095 0.095 0 0 0 0.091 0.077
                arcToRelative(
                    a = 0.095f,
                    b = 0.095f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.091f,
                    dy1 = 0.077f,
                )
                // a 0.095 0.095 0 0 0 0.096 -0.105
                arcToRelative(
                    a = 0.095f,
                    b = 0.095f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.096f,
                    dy1 = -0.105f,
                )
                // c -0.374 -3.245 -3.138 -5.645 -6.257 -5.7z
                curveToRelative(
                    dx1 = -0.374f,
                    dy1 = -3.245f,
                    dx2 = -3.138f,
                    dy2 = -5.645f,
                    dx3 = -6.257f,
                    dy3 = -5.7f,
                )
                close()
                // M 8 7.386
                moveTo(x = 8.0f, y = 7.386f)
                // a 1.385 1.385 0 1 1 0 -2.77
                arcToRelative(
                    a = 1.385f,
                    b = 1.385f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -2.77f,
                )
                // a 1.385 1.385 0 0 1 0 2.77z
                arcToRelative(
                    a = 1.385f,
                    b = 1.385f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 2.77f,
                )
                close()
            }
        }.build().also { _ic1049 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1049: ImageVector? = null
