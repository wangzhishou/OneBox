package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2212: ImageVector
    get() {
        val current = _ic2212
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2212",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.704 5.954 c-.245 .147 -.415 .476 -.372 .747 L7.59 8.35 h.808 l.269 -1.708 a.635 .635 0 0 0 -.307 -.64 l-.053 -.031 a.57 .57 0 0 0 -.604 -.017Z M8 9.625 a.45 .45 0 1 0 0 -.9 .45 .45 0 0 0 0 .9Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.704 5.954
                moveTo(x = 7.704f, y = 5.954f)
                // c -0.245 0.147 -0.415 0.476 -0.372 0.747
                curveToRelative(
                    dx1 = -0.245f,
                    dy1 = 0.147f,
                    dx2 = -0.415f,
                    dy2 = 0.476f,
                    dx3 = -0.372f,
                    dy3 = 0.747f,
                )
                // L 7.59 8.35
                lineTo(x = 7.59f, y = 8.35f)
                // h 0.808
                horizontalLineToRelative(dx = 0.808f)
                // l 0.269 -1.708
                lineToRelative(dx = 0.269f, dy = -1.708f)
                // a 0.635 0.635 0 0 0 -0.307 -0.64
                arcToRelative(
                    a = 0.635f,
                    b = 0.635f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.307f,
                    dy1 = -0.64f,
                )
                // l -0.053 -0.031
                lineToRelative(dx = -0.053f, dy = -0.031f)
                // a 0.57 0.57 0 0 0 -0.604 -0.017z
                arcToRelative(
                    a = 0.57f,
                    b = 0.57f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.604f,
                    dy1 = -0.017f,
                )
                close()
                // M 8 9.625
                moveTo(x = 8.0f, y = 9.625f)
                // a 0.45 0.45 0 1 0 0 -0.9
                arcToRelative(
                    a = 0.45f,
                    b = 0.45f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.9f,
                )
                // a 0.45 0.45 0 0 0 0 0.9z
                arcToRelative(
                    a = 0.45f,
                    b = 0.45f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.9f,
                )
                close()
            }
            // M7.925 11.5 a3.74 3.74 0 0 0 2.756 -1.207 .252 .252 0 0 1 .274 -.063 2.25 2.25 0 1 0 .424 -4.325 .251 .251 0 0 1 -.256 -.114 A3.748 3.748 0 0 0 7.925 4 a3.748 3.748 0 0 0 -3.188 1.774 .25 .25 0 0 1 -.24 .114 2.25 2.25 0 1 0 .447 4.377 .25 .25 0 0 1 .26 .065 3.74 3.74 0 0 0 2.721 1.17Z m2.929 -2.172 c-.13 -.096 -.329 -.073 -.417 .062 a2.998 2.998 0 0 1 -2.512 1.36 2.997 2.997 0 0 1 -2.472 -1.3 c-.085 -.123 -.261 -.148 -.386 -.067 a1.5 1.5 0 1 1 -.272 -2.656 c.14 .054 .307 -.006 .365 -.143 a3 3 0 0 1 5.548 .044 c.06 .15 .252 .212 .397 .142 a1.5 1.5 0 1 1 -.251 2.558Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.925 11.5
                moveTo(x = 7.925f, y = 11.5f)
                // a 3.74 3.74 0 0 0 2.756 -1.207
                arcToRelative(
                    a = 3.74f,
                    b = 3.74f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.756f,
                    dy1 = -1.207f,
                )
                // a 0.252 0.252 0 0 1 0.274 -0.063
                arcToRelative(
                    a = 0.252f,
                    b = 0.252f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.274f,
                    dy1 = -0.063f,
                )
                // a 2.25 2.25 0 1 0 0.424 -4.325
                arcToRelative(
                    a = 2.25f,
                    b = 2.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.424f,
                    dy1 = -4.325f,
                )
                // a 0.251 0.251 0 0 1 -0.256 -0.114
                arcToRelative(
                    a = 0.251f,
                    b = 0.251f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.256f,
                    dy1 = -0.114f,
                )
                // A 3.748 3.748 0 0 0 7.925 4
                arcTo(
                    horizontalEllipseRadius = 3.748f,
                    verticalEllipseRadius = 3.748f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.925f,
                    y1 = 4.0f,
                )
                // a 3.748 3.748 0 0 0 -3.188 1.774
                arcToRelative(
                    a = 3.748f,
                    b = 3.748f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -3.188f,
                    dy1 = 1.774f,
                )
                // a 0.25 0.25 0 0 1 -0.24 0.114
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.24f,
                    dy1 = 0.114f,
                )
                // a 2.25 2.25 0 1 0 0.447 4.377
                arcToRelative(
                    a = 2.25f,
                    b = 2.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.447f,
                    dy1 = 4.377f,
                )
                // a 0.25 0.25 0 0 1 0.26 0.065
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.26f,
                    dy1 = 0.065f,
                )
                // a 3.74 3.74 0 0 0 2.721 1.17z
                arcToRelative(
                    a = 3.74f,
                    b = 3.74f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.721f,
                    dy1 = 1.17f,
                )
                close()
                // m 2.929 -2.172
                moveToRelative(dx = 2.929f, dy = -2.172f)
                // c -0.13 -0.096 -0.329 -0.073 -0.417 0.062
                curveToRelative(
                    dx1 = -0.13f,
                    dy1 = -0.096f,
                    dx2 = -0.329f,
                    dy2 = -0.073f,
                    dx3 = -0.417f,
                    dy3 = 0.062f,
                )
                // a 2.998 2.998 0 0 1 -2.512 1.36
                arcToRelative(
                    a = 2.998f,
                    b = 2.998f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.512f,
                    dy1 = 1.36f,
                )
                // a 2.997 2.997 0 0 1 -2.472 -1.3
                arcToRelative(
                    a = 2.997f,
                    b = 2.997f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.472f,
                    dy1 = -1.3f,
                )
                // c -0.085 -0.123 -0.261 -0.148 -0.386 -0.067
                curveToRelative(
                    dx1 = -0.085f,
                    dy1 = -0.123f,
                    dx2 = -0.261f,
                    dy2 = -0.148f,
                    dx3 = -0.386f,
                    dy3 = -0.067f,
                )
                // a 1.5 1.5 0 1 1 -0.272 -2.656
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.272f,
                    dy1 = -2.656f,
                )
                // c 0.14 0.054 0.307 -0.006 0.365 -0.143
                curveToRelative(
                    dx1 = 0.14f,
                    dy1 = 0.054f,
                    dx2 = 0.307f,
                    dy2 = -0.006f,
                    dx3 = 0.365f,
                    dy3 = -0.143f,
                )
                // a 3 3 0 0 1 5.548 0.044
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 5.548f,
                    dy1 = 0.044f,
                )
                // c 0.06 0.15 0.252 0.212 0.397 0.142
                curveToRelative(
                    dx1 = 0.06f,
                    dy1 = 0.15f,
                    dx2 = 0.252f,
                    dy2 = 0.212f,
                    dx3 = 0.397f,
                    dy3 = 0.142f,
                )
                // a 1.5 1.5 0 1 1 -0.251 2.558z
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.251f,
                    dy1 = 2.558f,
                )
                close()
            }
            // M0 8 a8 8 0 1 0 16 0 A8 8 0 0 0 0 8Z m14.7 0 A6.7 6.7 0 1 1 1.3 8 a6.7 6.7 0 0 1 13.4 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0 8
                moveTo(x = 0.0f, y = 8.0f)
                // a 8 8 0 1 0 16 0
                arcToRelative(
                    a = 8.0f,
                    b = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 16.0f,
                    dy1 = 0.0f,
                )
                // A 8 8 0 0 0 0 8z
                arcTo(
                    horizontalEllipseRadius = 8.0f,
                    verticalEllipseRadius = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 0.0f,
                    y1 = 8.0f,
                )
                close()
                // m 14.7 0
                moveToRelative(dx = 14.7f, dy = 0.0f)
                // A 6.7 6.7 0 1 1 1.3 8
                arcTo(
                    horizontalEllipseRadius = 6.7f,
                    verticalEllipseRadius = 6.7f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 1.3f,
                    y1 = 8.0f,
                )
                // a 6.7 6.7 0 0 1 13.4 0z
                arcToRelative(
                    a = 6.7f,
                    b = 6.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 13.4f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic2212 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2212: ImageVector? = null
