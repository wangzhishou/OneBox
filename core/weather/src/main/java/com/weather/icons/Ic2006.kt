package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2006: ImageVector
    get() {
        val current = _ic2006
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2006",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M4.9 11.8 a.6 .6 0 1 1 0 1.2 .6 .6 0 0 1 .001 -1.2Z m-.395 -3.695 c.256 -.153 .552 -.132 .807 .023 l.07 .042 a.847 .847 0 0 1 .408 .853 L5.432 11.3 H4.354 L4.01 9.102 c-.057 -.362 .17 -.8 .496 -.997Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.9 11.8
                moveTo(x = 4.9f, y = 11.8f)
                // a 0.6 0.6 0 1 1 0 1.2
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.2f,
                )
                // a 0.6 0.6 0 0 1 0.001 -1.2z
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.001f,
                    dy1 = -1.2f,
                )
                close()
                // m -0.395 -3.695
                moveToRelative(dx = -0.395f, dy = -3.695f)
                // c 0.256 -0.153 0.552 -0.132 0.807 0.023
                curveToRelative(
                    dx1 = 0.256f,
                    dy1 = -0.153f,
                    dx2 = 0.552f,
                    dy2 = -0.132f,
                    dx3 = 0.807f,
                    dy3 = 0.023f,
                )
                // l 0.07 0.042
                lineToRelative(dx = 0.07f, dy = 0.042f)
                // a 0.847 0.847 0 0 1 0.408 0.853
                arcToRelative(
                    a = 0.847f,
                    b = 0.847f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.408f,
                    dy1 = 0.853f,
                )
                // L 5.432 11.3
                lineTo(x = 5.432f, y = 11.3f)
                // H 4.354
                horizontalLineTo(x = 4.354f)
                // L 4.01 9.102
                lineTo(x = 4.01f, y = 9.102f)
                // c -0.057 -0.362 0.17 -0.8 0.496 -0.997z
                curveToRelative(
                    dx1 = -0.057f,
                    dy1 = -0.362f,
                    dx2 = 0.17f,
                    dy2 = -0.8f,
                    dx3 = 0.496f,
                    dy3 = -0.997f,
                )
                close()
            }
            // M5.614 1.854 C7.702 4.82 10 8.726 10 11 a5 5 0 0 1 -10 0 c0 -2.274 2.298 -6.181 4.386 -9.146 .207 -.294 .413 -.58 .614 -.854 .201 .274 .407 .56 .614 .854Z M5 2.721 C4.14 3.959 3.263 5.337 2.547 6.67 c-.472 .878 -.865 1.72 -1.138 2.477 C1.132 9.917 1 10.541 1 11 a4 4 0 0 0 8 0 c0 -.46 -.132 -1.083 -.41 -1.853 -.272 -.757 -.665 -1.6 -1.137 -2.477 C6.737 5.337 5.86 3.959 5 2.72Z M13.512 .638 C14.724 2.193 16 4.166 16 5.333 c0 .707 -.316 1.386 -.879 1.886 S13.796 8 13 8 s-1.559 -.281 -2.121 -.781 C10.316 6.719 10 6.04 10 5.333 c0 -1.167 1.276 -3.14 2.488 -4.695 .173 -.222 .345 -.436 .512 -.638 .167 .202 .339 .416 .512 .638Z M13 1.283 c-.466 .607 -.93 1.266 -1.315 1.903 -.279 .46 -.507 .894 -.662 1.279 -.16 .395 -.223 .684 -.223 .868 0 .463 .206 .929 .61 1.288 .408 .362 .978 .58 1.59 .58 .612 0 1.182 -.218 1.59 -.58 .404 -.36 .61 -.825 .61 -1.288 0 -.184 -.062 -.473 -.223 -.868 a9.347 9.347 0 0 0 -.662 -1.28 A21.309 21.309 0 0 0 13 1.284Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5.614 1.854
                moveTo(x = 5.614f, y = 1.854f)
                // C 7.702 4.82 10 8.726 10 11
                curveTo(
                    x1 = 7.702f,
                    y1 = 4.82f,
                    x2 = 10.0f,
                    y2 = 8.726f,
                    x3 = 10.0f,
                    y3 = 11.0f,
                )
                // a 5 5 0 0 1 -10 0
                arcToRelative(
                    a = 5.0f,
                    b = 5.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -10.0f,
                    dy1 = 0.0f,
                )
                // c 0 -2.274 2.298 -6.181 4.386 -9.146
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -2.274f,
                    dx2 = 2.298f,
                    dy2 = -6.181f,
                    dx3 = 4.386f,
                    dy3 = -9.146f,
                )
                // c 0.207 -0.294 0.413 -0.58 0.614 -0.854
                curveToRelative(
                    dx1 = 0.207f,
                    dy1 = -0.294f,
                    dx2 = 0.413f,
                    dy2 = -0.58f,
                    dx3 = 0.614f,
                    dy3 = -0.854f,
                )
                // c 0.201 0.274 0.407 0.56 0.614 0.854z
                curveToRelative(
                    dx1 = 0.201f,
                    dy1 = 0.274f,
                    dx2 = 0.407f,
                    dy2 = 0.56f,
                    dx3 = 0.614f,
                    dy3 = 0.854f,
                )
                close()
                // M 5 2.721
                moveTo(x = 5.0f, y = 2.721f)
                // C 4.14 3.959 3.263 5.337 2.547 6.67
                curveTo(
                    x1 = 4.14f,
                    y1 = 3.959f,
                    x2 = 3.263f,
                    y2 = 5.337f,
                    x3 = 2.547f,
                    y3 = 6.67f,
                )
                // c -0.472 0.878 -0.865 1.72 -1.138 2.477
                curveToRelative(
                    dx1 = -0.472f,
                    dy1 = 0.878f,
                    dx2 = -0.865f,
                    dy2 = 1.72f,
                    dx3 = -1.138f,
                    dy3 = 2.477f,
                )
                // C 1.132 9.917 1 10.541 1 11
                curveTo(
                    x1 = 1.132f,
                    y1 = 9.917f,
                    x2 = 1.0f,
                    y2 = 10.541f,
                    x3 = 1.0f,
                    y3 = 11.0f,
                )
                // a 4 4 0 0 0 8 0
                arcToRelative(
                    a = 4.0f,
                    b = 4.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 8.0f,
                    dy1 = 0.0f,
                )
                // c 0 -0.46 -0.132 -1.083 -0.41 -1.853
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.46f,
                    dx2 = -0.132f,
                    dy2 = -1.083f,
                    dx3 = -0.41f,
                    dy3 = -1.853f,
                )
                // c -0.272 -0.757 -0.665 -1.6 -1.137 -2.477
                curveToRelative(
                    dx1 = -0.272f,
                    dy1 = -0.757f,
                    dx2 = -0.665f,
                    dy2 = -1.6f,
                    dx3 = -1.137f,
                    dy3 = -2.477f,
                )
                // C 6.737 5.337 5.86 3.959 5 2.72z
                curveTo(
                    x1 = 6.737f,
                    y1 = 5.337f,
                    x2 = 5.86f,
                    y2 = 3.959f,
                    x3 = 5.0f,
                    y3 = 2.72f,
                )
                close()
                // M 13.512 0.638
                moveTo(x = 13.512f, y = 0.638f)
                // C 14.724 2.193 16 4.166 16 5.333
                curveTo(
                    x1 = 14.724f,
                    y1 = 2.193f,
                    x2 = 16.0f,
                    y2 = 4.166f,
                    x3 = 16.0f,
                    y3 = 5.333f,
                )
                // c 0 0.707 -0.316 1.386 -0.879 1.886
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.707f,
                    dx2 = -0.316f,
                    dy2 = 1.386f,
                    dx3 = -0.879f,
                    dy3 = 1.886f,
                )
                // S 13.796 8 13 8
                reflectiveCurveTo(
                    x1 = 13.796f,
                    y1 = 8.0f,
                    x2 = 13.0f,
                    y2 = 8.0f,
                )
                // s -1.559 -0.281 -2.121 -0.781
                reflectiveCurveToRelative(
                    dx1 = -1.559f,
                    dy1 = -0.281f,
                    dx2 = -2.121f,
                    dy2 = -0.781f,
                )
                // C 10.316 6.719 10 6.04 10 5.333
                curveTo(
                    x1 = 10.316f,
                    y1 = 6.719f,
                    x2 = 10.0f,
                    y2 = 6.04f,
                    x3 = 10.0f,
                    y3 = 5.333f,
                )
                // c 0 -1.167 1.276 -3.14 2.488 -4.695
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.167f,
                    dx2 = 1.276f,
                    dy2 = -3.14f,
                    dx3 = 2.488f,
                    dy3 = -4.695f,
                )
                // c 0.173 -0.222 0.345 -0.436 0.512 -0.638
                curveToRelative(
                    dx1 = 0.173f,
                    dy1 = -0.222f,
                    dx2 = 0.345f,
                    dy2 = -0.436f,
                    dx3 = 0.512f,
                    dy3 = -0.638f,
                )
                // c 0.167 0.202 0.339 0.416 0.512 0.638z
                curveToRelative(
                    dx1 = 0.167f,
                    dy1 = 0.202f,
                    dx2 = 0.339f,
                    dy2 = 0.416f,
                    dx3 = 0.512f,
                    dy3 = 0.638f,
                )
                close()
                // M 13 1.283
                moveTo(x = 13.0f, y = 1.283f)
                // c -0.466 0.607 -0.93 1.266 -1.315 1.903
                curveToRelative(
                    dx1 = -0.466f,
                    dy1 = 0.607f,
                    dx2 = -0.93f,
                    dy2 = 1.266f,
                    dx3 = -1.315f,
                    dy3 = 1.903f,
                )
                // c -0.279 0.46 -0.507 0.894 -0.662 1.279
                curveToRelative(
                    dx1 = -0.279f,
                    dy1 = 0.46f,
                    dx2 = -0.507f,
                    dy2 = 0.894f,
                    dx3 = -0.662f,
                    dy3 = 1.279f,
                )
                // c -0.16 0.395 -0.223 0.684 -0.223 0.868
                curveToRelative(
                    dx1 = -0.16f,
                    dy1 = 0.395f,
                    dx2 = -0.223f,
                    dy2 = 0.684f,
                    dx3 = -0.223f,
                    dy3 = 0.868f,
                )
                // c 0 0.463 0.206 0.929 0.61 1.288
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.463f,
                    dx2 = 0.206f,
                    dy2 = 0.929f,
                    dx3 = 0.61f,
                    dy3 = 1.288f,
                )
                // c 0.408 0.362 0.978 0.58 1.59 0.58
                curveToRelative(
                    dx1 = 0.408f,
                    dy1 = 0.362f,
                    dx2 = 0.978f,
                    dy2 = 0.58f,
                    dx3 = 1.59f,
                    dy3 = 0.58f,
                )
                // c 0.612 0 1.182 -0.218 1.59 -0.58
                curveToRelative(
                    dx1 = 0.612f,
                    dy1 = 0.0f,
                    dx2 = 1.182f,
                    dy2 = -0.218f,
                    dx3 = 1.59f,
                    dy3 = -0.58f,
                )
                // c 0.404 -0.36 0.61 -0.825 0.61 -1.288
                curveToRelative(
                    dx1 = 0.404f,
                    dy1 = -0.36f,
                    dx2 = 0.61f,
                    dy2 = -0.825f,
                    dx3 = 0.61f,
                    dy3 = -1.288f,
                )
                // c 0 -0.184 -0.062 -0.473 -0.223 -0.868
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.184f,
                    dx2 = -0.062f,
                    dy2 = -0.473f,
                    dx3 = -0.223f,
                    dy3 = -0.868f,
                )
                // a 9.347 9.347 0 0 0 -0.662 -1.28
                arcToRelative(
                    a = 9.347f,
                    b = 9.347f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.662f,
                    dy1 = -1.28f,
                )
                // A 21.309 21.309 0 0 0 13 1.284z
                arcTo(
                    horizontalEllipseRadius = 21.309f,
                    verticalEllipseRadius = 21.309f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 13.0f,
                    y1 = 1.284f,
                )
                close()
            }
        }.build().also { _ic2006 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2006: ImageVector? = null
