package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1017: ImageVector
    get() {
        val current = _ic1017
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1017",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M12.227 8.13 a.263 .263 0 0 0 -.454 0 l-3.738 6.482 a.26 .26 0 0 0 .227 .388 h7.476 a.26 .26 0 0 0 .227 -.388 L12.227 8.13Z m-.85 2.144 c-.032 -.282 .256 -.524 .623 -.524 s.655 .242 .623 .524 l-.282 2.476 h-.682 l-.282 -2.476Z m1.127 3.476 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z M.75 2 a.75 .75 0 0 0 0 1.5 h14.5 a.75 .75 0 0 0 0 -1.5 H.75Z m7 3.5 a.75 .75 0 0 0 0 1.5 h7.5 a.75 .75 0 0 0 0 -1.5 h-7.5Z M0 9.75 A.75 .75 0 0 1 .75 9 h7.5 a.75 .75 0 0 1 0 1.5 H.75 A.75 .75 0 0 1 0 9.75Z M.75 5.5 a.75 .75 0 0 0 0 1.5 h4.5 a.75 .75 0 1 0 0 -1.5 H.75Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 12.227 8.13
                moveTo(x = 12.227f, y = 8.13f)
                // a 0.263 0.263 0 0 0 -0.454 0
                arcToRelative(
                    a = 0.263f,
                    b = 0.263f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.454f,
                    dy1 = 0.0f,
                )
                // l -3.738 6.482
                lineToRelative(dx = -3.738f, dy = 6.482f)
                // a 0.26 0.26 0 0 0 0.227 0.388
                arcToRelative(
                    a = 0.26f,
                    b = 0.26f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.227f,
                    dy1 = 0.388f,
                )
                // h 7.476
                horizontalLineToRelative(dx = 7.476f)
                // a 0.26 0.26 0 0 0 0.227 -0.388
                arcToRelative(
                    a = 0.26f,
                    b = 0.26f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.227f,
                    dy1 = -0.388f,
                )
                // L 12.227 8.13z
                lineTo(x = 12.227f, y = 8.13f)
                close()
                // m -0.85 2.144
                moveToRelative(dx = -0.85f, dy = 2.144f)
                // c -0.032 -0.282 0.256 -0.524 0.623 -0.524
                curveToRelative(
                    dx1 = -0.032f,
                    dy1 = -0.282f,
                    dx2 = 0.256f,
                    dy2 = -0.524f,
                    dx3 = 0.623f,
                    dy3 = -0.524f,
                )
                // s 0.655 0.242 0.623 0.524
                reflectiveCurveToRelative(
                    dx1 = 0.655f,
                    dy1 = 0.242f,
                    dx2 = 0.623f,
                    dy2 = 0.524f,
                )
                // l -0.282 2.476
                lineToRelative(dx = -0.282f, dy = 2.476f)
                // h -0.682
                horizontalLineToRelative(dx = -0.682f)
                // l -0.282 -2.476z
                lineToRelative(dx = -0.282f, dy = -2.476f)
                close()
                // m 1.127 3.476
                moveToRelative(dx = 1.127f, dy = 3.476f)
                // a 0.5 0.5 0 1 1 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // a 0.5 0.5 0 0 1 1 0z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                close()
                // M 0.75 2
                moveTo(x = 0.75f, y = 2.0f)
                // a 0.75 0.75 0 0 0 0 1.5
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.5f,
                )
                // h 14.5
                horizontalLineToRelative(dx = 14.5f)
                // a 0.75 0.75 0 0 0 0 -1.5
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.5f,
                )
                // H 0.75z
                horizontalLineTo(x = 0.75f)
                close()
                // m 7 3.5
                moveToRelative(dx = 7.0f, dy = 3.5f)
                // a 0.75 0.75 0 0 0 0 1.5
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.5f,
                )
                // h 7.5
                horizontalLineToRelative(dx = 7.5f)
                // a 0.75 0.75 0 0 0 0 -1.5
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.5f,
                )
                // h -7.5z
                horizontalLineToRelative(dx = -7.5f)
                close()
                // M 0 9.75
                moveTo(x = 0.0f, y = 9.75f)
                // A 0.75 0.75 0 0 1 0.75 9
                arcTo(
                    horizontalEllipseRadius = 0.75f,
                    verticalEllipseRadius = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 0.75f,
                    y1 = 9.0f,
                )
                // h 7.5
                horizontalLineToRelative(dx = 7.5f)
                // a 0.75 0.75 0 0 1 0 1.5
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.5f,
                )
                // H 0.75
                horizontalLineTo(x = 0.75f)
                // A 0.75 0.75 0 0 1 0 9.75z
                arcTo(
                    horizontalEllipseRadius = 0.75f,
                    verticalEllipseRadius = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 0.0f,
                    y1 = 9.75f,
                )
                close()
                // M 0.75 5.5
                moveTo(x = 0.75f, y = 5.5f)
                // a 0.75 0.75 0 0 0 0 1.5
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.5f,
                )
                // h 4.5
                horizontalLineToRelative(dx = 4.5f)
                // a 0.75 0.75 0 1 0 0 -1.5
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.5f,
                )
                // H 0.75z
                horizontalLineTo(x = 0.75f)
                close()
            }
        }.build().also { _ic1017 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1017: ImageVector? = null
