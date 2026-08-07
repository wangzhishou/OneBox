package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic405Fill: ImageVector
    get() {
        val current = _ic405Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic405Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M0 14 a1 1 0 1 0 2 0 c0 -.5 -.555 -1.395 -1 -2 -.445 .605 -1 1.5 -1 2Z m4 0 a1 1 0 1 0 2 0 c0 -.5 -.555 -1.395 -1 -2 -.445 .605 -1 1.5 -1 2Z m5.151 -1.65 a.35 .35 0 1 1 .7 0 v.544 l.47 -.272 a.35 .35 0 1 1 .35 .606 l-.47 .272 .47 .272 a.35 .35 0 1 1 -.35 .606 l-.47 -.272 v.544 a.35 .35 0 1 1 -.7 0 v-.544 l-.47 .272 a.35 .35 0 1 1 -.35 -.606 l.47 -.272 -.47 -.272 a.35 .35 0 1 1 .35 -.606 l.47 .272 v-.544Z m5.35 -.35 a.35 .35 0 0 0 -.35 .35 v.544 l-.47 -.272 a.35 .35 0 1 0 -.35 .606 l.47 .272 -.47 .272 a.35 .35 0 1 0 .35 .606 l.47 -.272 v.544 a.35 .35 0 1 0 .7 0 v-.544 l.47 .272 a.35 .35 0 1 0 .35 -.606 l-.47 -.272 .47 -.272 a.35 .35 0 1 0 -.35 -.606 l-.47 .272 v-.544 a.35 .35 0 0 0 -.35 -.35Z m-2.774 -3.783 A4.99 4.99 0 0 1 7.9 10 a4.988 4.988 0 0 1 -3.773 -1.719 3 3 0 1 1 -.586 -5.732 A4.998 4.998 0 0 1 7.9 0 a4.999 4.999 0 0 1 4.38 2.587 3 3 0 1 1 -.553 5.63Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0 14
                moveTo(x = 0.0f, y = 14.0f)
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
                // m 4 0
                moveToRelative(dx = 4.0f, dy = 0.0f)
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
                // m 5.151 -1.65
                moveToRelative(dx = 5.151f, dy = -1.65f)
                // a 0.35 0.35 0 1 1 0.7 0
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.7f,
                    dy1 = 0.0f,
                )
                // v 0.544
                verticalLineToRelative(dy = 0.544f)
                // l 0.47 -0.272
                lineToRelative(dx = 0.47f, dy = -0.272f)
                // a 0.35 0.35 0 1 1 0.35 0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.35f,
                    dy1 = 0.606f,
                )
                // l -0.47 0.272
                lineToRelative(dx = -0.47f, dy = 0.272f)
                // l 0.47 0.272
                lineToRelative(dx = 0.47f, dy = 0.272f)
                // a 0.35 0.35 0 1 1 -0.35 0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.35f,
                    dy1 = 0.606f,
                )
                // l -0.47 -0.272
                lineToRelative(dx = -0.47f, dy = -0.272f)
                // v 0.544
                verticalLineToRelative(dy = 0.544f)
                // a 0.35 0.35 0 1 1 -0.7 0
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.7f,
                    dy1 = 0.0f,
                )
                // v -0.544
                verticalLineToRelative(dy = -0.544f)
                // l -0.47 0.272
                lineToRelative(dx = -0.47f, dy = 0.272f)
                // a 0.35 0.35 0 1 1 -0.35 -0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.35f,
                    dy1 = -0.606f,
                )
                // l 0.47 -0.272
                lineToRelative(dx = 0.47f, dy = -0.272f)
                // l -0.47 -0.272
                lineToRelative(dx = -0.47f, dy = -0.272f)
                // a 0.35 0.35 0 1 1 0.35 -0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.35f,
                    dy1 = -0.606f,
                )
                // l 0.47 0.272
                lineToRelative(dx = 0.47f, dy = 0.272f)
                // v -0.544z
                verticalLineToRelative(dy = -0.544f)
                close()
                // m 5.35 -0.35
                moveToRelative(dx = 5.35f, dy = -0.35f)
                // a 0.35 0.35 0 0 0 -0.35 0.35
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = 0.35f,
                )
                // v 0.544
                verticalLineToRelative(dy = 0.544f)
                // l -0.47 -0.272
                lineToRelative(dx = -0.47f, dy = -0.272f)
                // a 0.35 0.35 0 1 0 -0.35 0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = 0.606f,
                )
                // l 0.47 0.272
                lineToRelative(dx = 0.47f, dy = 0.272f)
                // l -0.47 0.272
                lineToRelative(dx = -0.47f, dy = 0.272f)
                // a 0.35 0.35 0 1 0 0.35 0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.35f,
                    dy1 = 0.606f,
                )
                // l 0.47 -0.272
                lineToRelative(dx = 0.47f, dy = -0.272f)
                // v 0.544
                verticalLineToRelative(dy = 0.544f)
                // a 0.35 0.35 0 1 0 0.7 0
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.7f,
                    dy1 = 0.0f,
                )
                // v -0.544
                verticalLineToRelative(dy = -0.544f)
                // l 0.47 0.272
                lineToRelative(dx = 0.47f, dy = 0.272f)
                // a 0.35 0.35 0 1 0 0.35 -0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.35f,
                    dy1 = -0.606f,
                )
                // l -0.47 -0.272
                lineToRelative(dx = -0.47f, dy = -0.272f)
                // l 0.47 -0.272
                lineToRelative(dx = 0.47f, dy = -0.272f)
                // a 0.35 0.35 0 1 0 -0.35 -0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = -0.606f,
                )
                // l -0.47 0.272
                lineToRelative(dx = -0.47f, dy = 0.272f)
                // v -0.544
                verticalLineToRelative(dy = -0.544f)
                // a 0.35 0.35 0 0 0 -0.35 -0.35z
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = -0.35f,
                )
                close()
                // m -2.774 -3.783
                moveToRelative(dx = -2.774f, dy = -3.783f)
                // A 4.99 4.99 0 0 1 7.9 10
                arcTo(
                    horizontalEllipseRadius = 4.99f,
                    verticalEllipseRadius = 4.99f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.9f,
                    y1 = 10.0f,
                )
                // a 4.988 4.988 0 0 1 -3.773 -1.719
                arcToRelative(
                    a = 4.988f,
                    b = 4.988f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.773f,
                    dy1 = -1.719f,
                )
                // a 3 3 0 1 1 -0.586 -5.732
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.586f,
                    dy1 = -5.732f,
                )
                // A 4.998 4.998 0 0 1 7.9 0
                arcTo(
                    horizontalEllipseRadius = 4.998f,
                    verticalEllipseRadius = 4.998f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.9f,
                    y1 = 0.0f,
                )
                // a 4.999 4.999 0 0 1 4.38 2.587
                arcToRelative(
                    a = 4.999f,
                    b = 4.999f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 4.38f,
                    dy1 = 2.587f,
                )
                // a 3 3 0 1 1 -0.553 5.63z
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.553f,
                    dy1 = 5.63f,
                )
                close()
            }
        }.build().also { _ic405Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic405Fill: ImageVector? = null
