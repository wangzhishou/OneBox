package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2411: ImageVector
    get() {
        val current = _ic2411
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2411",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M6.586 8.414 A2 2 0 0 1 6 7 c0 -1 1.11 -2.79 2 -4 .89 1.21 2 3 2 4 a2 2 0 0 1 -3.414 1.414Z m.353 -.353 A1.5 1.5 0 0 0 9.5 7 c0 -.75 -.833 -2.093 -1.5 -3 -.667 .907 -1.5 2.25 -1.5 3 0 .398 .158 .78 .44 1.06Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6.586 8.414
                moveTo(x = 6.586f, y = 8.414f)
                // A 2 2 0 0 1 6 7
                arcTo(
                    horizontalEllipseRadius = 2.0f,
                    verticalEllipseRadius = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 6.0f,
                    y1 = 7.0f,
                )
                // c 0 -1 1.11 -2.79 2 -4
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                    dx2 = 1.11f,
                    dy2 = -2.79f,
                    dx3 = 2.0f,
                    dy3 = -4.0f,
                )
                // c 0.89 1.21 2 3 2 4
                curveToRelative(
                    dx1 = 0.89f,
                    dy1 = 1.21f,
                    dx2 = 2.0f,
                    dy2 = 3.0f,
                    dx3 = 2.0f,
                    dy3 = 4.0f,
                )
                // a 2 2 0 0 1 -3.414 1.414z
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.414f,
                    dy1 = 1.414f,
                )
                close()
                // m 0.353 -0.353
                moveToRelative(dx = 0.353f, dy = -0.353f)
                // A 1.5 1.5 0 0 0 9.5 7
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 9.5f,
                    y1 = 7.0f,
                )
                // c 0 -0.75 -0.833 -2.093 -1.5 -3
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.75f,
                    dx2 = -0.833f,
                    dy2 = -2.093f,
                    dx3 = -1.5f,
                    dy3 = -3.0f,
                )
                // c -0.667 0.907 -1.5 2.25 -1.5 3
                curveToRelative(
                    dx1 = -0.667f,
                    dy1 = 0.907f,
                    dx2 = -1.5f,
                    dy2 = 2.25f,
                    dx3 = -1.5f,
                    dy3 = 3.0f,
                )
                // c 0 0.398 0.158 0.78 0.44 1.06z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.398f,
                    dx2 = 0.158f,
                    dy2 = 0.78f,
                    dx3 = 0.44f,
                    dy3 = 1.06f,
                )
                close()
            }
            // M8 16 A8 8 0 1 1 8 0 a8 8 0 0 1 0 16Z m0 -1.3 a6.68 6.68 0 0 0 4.734 -1.958 1.73 1.73 0 0 0 -.136 -.081 c-.187 -.098 -.41 -.161 -1.098 -.161 -.584 0 -1.01 .2 -1.533 .45 l-.022 .011 c-.512 .247 -1.12 .539 -1.945 .539 s-1.433 -.292 -1.945 -.539 l-.022 -.01 c-.522 -.252 -.949 -.451 -1.533 -.451 -.688 0 -.911 .063 -1.097 .16 -.044 .024 -.088 .05 -.137 .082 A6.68 6.68 0 0 0 8 14.7Z m5.063 -2.924 c.114 .06 .222 .13 .328 .203 .226 -.305 .426 -.63 .598 -.972 a4.881 4.881 0 0 0 -.796 -.308 c-.488 -.14 -1.012 -.199 -1.693 -.199 -.584 0 -1.01 .2 -1.533 .45 l-.022 .011 c-.512 .247 -1.12 .539 -1.945 .539 s-1.433 -.292 -1.945 -.539 l-.022 -.01 c-.522 -.252 -.949 -.451 -1.533 -.451 -.681 0 -1.205 .06 -1.693 .2 a4.89 4.89 0 0 0 -.796 .307 6.7 6.7 0 0 0 .598 .972 3.08 3.08 0 0 1 .328 -.203 c.406 -.214 .83 -.276 1.563 -.276 .825 0 1.433 .292 1.945 .539 l.022 .01 c.522 .252 .949 .451 1.533 .451 s1.01 -.2 1.533 -.45 l.022 -.011 c.512 -.247 1.12 -.539 1.945 -.539 .733 0 1.157 .062 1.563 .276Z m.405 -2.038 c.307 .087 .603 .201 .902 .343 a6.7 6.7 0 1 0 -12.741 0 c.3 -.142 .596 -.256 .903 -.343 C3.137 9.565 3.76 9.5 4.5 9.5 c.825 0 1.433 .292 1.945 .539 l.022 .01 c.522 .252 .949 .451 1.533 .451 s1.01 -.2 1.533 -.45 l.022 -.011 c.512 -.247 1.12 -.539 1.945 -.539 .74 0 1.363 .065 1.968 .238Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8 16
                moveTo(x = 8.0f, y = 16.0f)
                // A 8 8 0 1 1 8 0
                arcTo(
                    horizontalEllipseRadius = 8.0f,
                    verticalEllipseRadius = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 8.0f,
                    y1 = 0.0f,
                )
                // a 8 8 0 0 1 0 16z
                arcToRelative(
                    a = 8.0f,
                    b = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 16.0f,
                )
                close()
                // m 0 -1.3
                moveToRelative(dx = 0.0f, dy = -1.3f)
                // a 6.68 6.68 0 0 0 4.734 -1.958
                arcToRelative(
                    a = 6.68f,
                    b = 6.68f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 4.734f,
                    dy1 = -1.958f,
                )
                // a 1.73 1.73 0 0 0 -0.136 -0.081
                arcToRelative(
                    a = 1.73f,
                    b = 1.73f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.136f,
                    dy1 = -0.081f,
                )
                // c -0.187 -0.098 -0.41 -0.161 -1.098 -0.161
                curveToRelative(
                    dx1 = -0.187f,
                    dy1 = -0.098f,
                    dx2 = -0.41f,
                    dy2 = -0.161f,
                    dx3 = -1.098f,
                    dy3 = -0.161f,
                )
                // c -0.584 0 -1.01 0.2 -1.533 0.45
                curveToRelative(
                    dx1 = -0.584f,
                    dy1 = 0.0f,
                    dx2 = -1.01f,
                    dy2 = 0.2f,
                    dx3 = -1.533f,
                    dy3 = 0.45f,
                )
                // l -0.022 0.011
                lineToRelative(dx = -0.022f, dy = 0.011f)
                // c -0.512 0.247 -1.12 0.539 -1.945 0.539
                curveToRelative(
                    dx1 = -0.512f,
                    dy1 = 0.247f,
                    dx2 = -1.12f,
                    dy2 = 0.539f,
                    dx3 = -1.945f,
                    dy3 = 0.539f,
                )
                // s -1.433 -0.292 -1.945 -0.539
                reflectiveCurveToRelative(
                    dx1 = -1.433f,
                    dy1 = -0.292f,
                    dx2 = -1.945f,
                    dy2 = -0.539f,
                )
                // l -0.022 -0.01
                lineToRelative(dx = -0.022f, dy = -0.01f)
                // c -0.522 -0.252 -0.949 -0.451 -1.533 -0.451
                curveToRelative(
                    dx1 = -0.522f,
                    dy1 = -0.252f,
                    dx2 = -0.949f,
                    dy2 = -0.451f,
                    dx3 = -1.533f,
                    dy3 = -0.451f,
                )
                // c -0.688 0 -0.911 0.063 -1.097 0.16
                curveToRelative(
                    dx1 = -0.688f,
                    dy1 = 0.0f,
                    dx2 = -0.911f,
                    dy2 = 0.063f,
                    dx3 = -1.097f,
                    dy3 = 0.16f,
                )
                // c -0.044 0.024 -0.088 0.05 -0.137 0.082
                curveToRelative(
                    dx1 = -0.044f,
                    dy1 = 0.024f,
                    dx2 = -0.088f,
                    dy2 = 0.05f,
                    dx3 = -0.137f,
                    dy3 = 0.082f,
                )
                // A 6.68 6.68 0 0 0 8 14.7z
                arcTo(
                    horizontalEllipseRadius = 6.68f,
                    verticalEllipseRadius = 6.68f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 8.0f,
                    y1 = 14.7f,
                )
                close()
                // m 5.063 -2.924
                moveToRelative(dx = 5.063f, dy = -2.924f)
                // c 0.114 0.06 0.222 0.13 0.328 0.203
                curveToRelative(
                    dx1 = 0.114f,
                    dy1 = 0.06f,
                    dx2 = 0.222f,
                    dy2 = 0.13f,
                    dx3 = 0.328f,
                    dy3 = 0.203f,
                )
                // c 0.226 -0.305 0.426 -0.63 0.598 -0.972
                curveToRelative(
                    dx1 = 0.226f,
                    dy1 = -0.305f,
                    dx2 = 0.426f,
                    dy2 = -0.63f,
                    dx3 = 0.598f,
                    dy3 = -0.972f,
                )
                // a 4.881 4.881 0 0 0 -0.796 -0.308
                arcToRelative(
                    a = 4.881f,
                    b = 4.881f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.796f,
                    dy1 = -0.308f,
                )
                // c -0.488 -0.14 -1.012 -0.199 -1.693 -0.199
                curveToRelative(
                    dx1 = -0.488f,
                    dy1 = -0.14f,
                    dx2 = -1.012f,
                    dy2 = -0.199f,
                    dx3 = -1.693f,
                    dy3 = -0.199f,
                )
                // c -0.584 0 -1.01 0.2 -1.533 0.45
                curveToRelative(
                    dx1 = -0.584f,
                    dy1 = 0.0f,
                    dx2 = -1.01f,
                    dy2 = 0.2f,
                    dx3 = -1.533f,
                    dy3 = 0.45f,
                )
                // l -0.022 0.011
                lineToRelative(dx = -0.022f, dy = 0.011f)
                // c -0.512 0.247 -1.12 0.539 -1.945 0.539
                curveToRelative(
                    dx1 = -0.512f,
                    dy1 = 0.247f,
                    dx2 = -1.12f,
                    dy2 = 0.539f,
                    dx3 = -1.945f,
                    dy3 = 0.539f,
                )
                // s -1.433 -0.292 -1.945 -0.539
                reflectiveCurveToRelative(
                    dx1 = -1.433f,
                    dy1 = -0.292f,
                    dx2 = -1.945f,
                    dy2 = -0.539f,
                )
                // l -0.022 -0.01
                lineToRelative(dx = -0.022f, dy = -0.01f)
                // c -0.522 -0.252 -0.949 -0.451 -1.533 -0.451
                curveToRelative(
                    dx1 = -0.522f,
                    dy1 = -0.252f,
                    dx2 = -0.949f,
                    dy2 = -0.451f,
                    dx3 = -1.533f,
                    dy3 = -0.451f,
                )
                // c -0.681 0 -1.205 0.06 -1.693 0.2
                curveToRelative(
                    dx1 = -0.681f,
                    dy1 = 0.0f,
                    dx2 = -1.205f,
                    dy2 = 0.06f,
                    dx3 = -1.693f,
                    dy3 = 0.2f,
                )
                // a 4.89 4.89 0 0 0 -0.796 0.307
                arcToRelative(
                    a = 4.89f,
                    b = 4.89f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.796f,
                    dy1 = 0.307f,
                )
                // a 6.7 6.7 0 0 0 0.598 0.972
                arcToRelative(
                    a = 6.7f,
                    b = 6.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.598f,
                    dy1 = 0.972f,
                )
                // a 3.08 3.08 0 0 1 0.328 -0.203
                arcToRelative(
                    a = 3.08f,
                    b = 3.08f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.328f,
                    dy1 = -0.203f,
                )
                // c 0.406 -0.214 0.83 -0.276 1.563 -0.276
                curveToRelative(
                    dx1 = 0.406f,
                    dy1 = -0.214f,
                    dx2 = 0.83f,
                    dy2 = -0.276f,
                    dx3 = 1.563f,
                    dy3 = -0.276f,
                )
                // c 0.825 0 1.433 0.292 1.945 0.539
                curveToRelative(
                    dx1 = 0.825f,
                    dy1 = 0.0f,
                    dx2 = 1.433f,
                    dy2 = 0.292f,
                    dx3 = 1.945f,
                    dy3 = 0.539f,
                )
                // l 0.022 0.01
                lineToRelative(dx = 0.022f, dy = 0.01f)
                // c 0.522 0.252 0.949 0.451 1.533 0.451
                curveToRelative(
                    dx1 = 0.522f,
                    dy1 = 0.252f,
                    dx2 = 0.949f,
                    dy2 = 0.451f,
                    dx3 = 1.533f,
                    dy3 = 0.451f,
                )
                // s 1.01 -0.2 1.533 -0.45
                reflectiveCurveToRelative(
                    dx1 = 1.01f,
                    dy1 = -0.2f,
                    dx2 = 1.533f,
                    dy2 = -0.45f,
                )
                // l 0.022 -0.011
                lineToRelative(dx = 0.022f, dy = -0.011f)
                // c 0.512 -0.247 1.12 -0.539 1.945 -0.539
                curveToRelative(
                    dx1 = 0.512f,
                    dy1 = -0.247f,
                    dx2 = 1.12f,
                    dy2 = -0.539f,
                    dx3 = 1.945f,
                    dy3 = -0.539f,
                )
                // c 0.733 0 1.157 0.062 1.563 0.276z
                curveToRelative(
                    dx1 = 0.733f,
                    dy1 = 0.0f,
                    dx2 = 1.157f,
                    dy2 = 0.062f,
                    dx3 = 1.563f,
                    dy3 = 0.276f,
                )
                close()
                // m 0.405 -2.038
                moveToRelative(dx = 0.405f, dy = -2.038f)
                // c 0.307 0.087 0.603 0.201 0.902 0.343
                curveToRelative(
                    dx1 = 0.307f,
                    dy1 = 0.087f,
                    dx2 = 0.603f,
                    dy2 = 0.201f,
                    dx3 = 0.902f,
                    dy3 = 0.343f,
                )
                // a 6.7 6.7 0 1 0 -12.741 0
                arcToRelative(
                    a = 6.7f,
                    b = 6.7f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -12.741f,
                    dy1 = 0.0f,
                )
                // c 0.3 -0.142 0.596 -0.256 0.903 -0.343
                curveToRelative(
                    dx1 = 0.3f,
                    dy1 = -0.142f,
                    dx2 = 0.596f,
                    dy2 = -0.256f,
                    dx3 = 0.903f,
                    dy3 = -0.343f,
                )
                // C 3.137 9.565 3.76 9.5 4.5 9.5
                curveTo(
                    x1 = 3.137f,
                    y1 = 9.565f,
                    x2 = 3.76f,
                    y2 = 9.5f,
                    x3 = 4.5f,
                    y3 = 9.5f,
                )
                // c 0.825 0 1.433 0.292 1.945 0.539
                curveToRelative(
                    dx1 = 0.825f,
                    dy1 = 0.0f,
                    dx2 = 1.433f,
                    dy2 = 0.292f,
                    dx3 = 1.945f,
                    dy3 = 0.539f,
                )
                // l 0.022 0.01
                lineToRelative(dx = 0.022f, dy = 0.01f)
                // c 0.522 0.252 0.949 0.451 1.533 0.451
                curveToRelative(
                    dx1 = 0.522f,
                    dy1 = 0.252f,
                    dx2 = 0.949f,
                    dy2 = 0.451f,
                    dx3 = 1.533f,
                    dy3 = 0.451f,
                )
                // s 1.01 -0.2 1.533 -0.45
                reflectiveCurveToRelative(
                    dx1 = 1.01f,
                    dy1 = -0.2f,
                    dx2 = 1.533f,
                    dy2 = -0.45f,
                )
                // l 0.022 -0.011
                lineToRelative(dx = 0.022f, dy = -0.011f)
                // c 0.512 -0.247 1.12 -0.539 1.945 -0.539
                curveToRelative(
                    dx1 = 0.512f,
                    dy1 = -0.247f,
                    dx2 = 1.12f,
                    dy2 = -0.539f,
                    dx3 = 1.945f,
                    dy3 = -0.539f,
                )
                // c 0.74 0 1.363 0.065 1.968 0.238z
                curveToRelative(
                    dx1 = 0.74f,
                    dy1 = 0.0f,
                    dx2 = 1.363f,
                    dy2 = 0.065f,
                    dx3 = 1.968f,
                    dy3 = 0.238f,
                )
                close()
            }
        }.build().also { _ic2411 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2411: ImageVector? = null
