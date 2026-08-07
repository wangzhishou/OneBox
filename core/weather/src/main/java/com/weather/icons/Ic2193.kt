package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2193: ImageVector
    get() {
        val current = _ic2193
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2193",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M12.922 7.717 10 8.5 l.783 -2.922 2.14 2.14Z M13.86 2.5 16 4.64 l-2.892 2.89 -2.14 -2.14 L13.86 2.5Z m.555 1.22 -2.228 2.227 .366 .367 2.229 -2.229 -.367 -.366Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 12.922 7.717
                moveTo(x = 12.922f, y = 7.717f)
                // L 10 8.5
                lineTo(x = 10.0f, y = 8.5f)
                // l 0.783 -2.922
                lineToRelative(dx = 0.783f, dy = -2.922f)
                // l 2.14 2.14z
                lineToRelative(dx = 2.14f, dy = 2.14f)
                close()
                // M 13.86 2.5
                moveTo(x = 13.86f, y = 2.5f)
                // L 16 4.64
                lineTo(x = 16.0f, y = 4.64f)
                // l -2.892 2.89
                lineToRelative(dx = -2.892f, dy = 2.89f)
                // l -2.14 -2.14
                lineToRelative(dx = -2.14f, dy = -2.14f)
                // L 13.86 2.5z
                lineTo(x = 13.86f, y = 2.5f)
                close()
                // m 0.555 1.22
                moveToRelative(dx = 0.555f, dy = 1.22f)
                // l -2.228 2.227
                lineToRelative(dx = -2.228f, dy = 2.227f)
                // l 0.366 0.367
                lineToRelative(dx = 0.366f, dy = 0.367f)
                // l 2.229 -2.229
                lineToRelative(dx = 2.229f, dy = -2.229f)
                // l -0.367 -0.366z
                lineToRelative(dx = -0.367f, dy = -0.366f)
                close()
            }
            // M11.575 11.89 A4.986 4.986 0 0 1 7.9 13.5 a4.99 4.99 0 0 1 -3.629 -1.56 .334 .334 0 0 0 -.345 -.086 3 3 0 1 1 -.596 -5.836 .334 .334 0 0 0 .32 -.153 A4.997 4.997 0 0 1 7.9 3.5 c.75 0 1.461 .165 2.1 .461 v1.134 a4.001 4.001 0 0 0 -5.787 1.85 c-.077 .183 -.3 .264 -.485 .191 a2 2 0 1 0 .362 3.54 c.166 -.107 .401 -.073 .513 .09 A3.996 3.996 0 0 0 7.9 12.5 a3.996 3.996 0 0 0 3.35 -1.813 c.117 -.18 .383 -.211 .555 -.083 A2 2 0 0 0 14.323 7.5 h1.276 a3 3 0 0 1 -3.66 4.307 .335 .335 0 0 0 -.364 .083Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.575 11.89
                moveTo(x = 11.575f, y = 11.89f)
                // A 4.986 4.986 0 0 1 7.9 13.5
                arcTo(
                    horizontalEllipseRadius = 4.986f,
                    verticalEllipseRadius = 4.986f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.9f,
                    y1 = 13.5f,
                )
                // a 4.99 4.99 0 0 1 -3.629 -1.56
                arcToRelative(
                    a = 4.99f,
                    b = 4.99f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.629f,
                    dy1 = -1.56f,
                )
                // a 0.334 0.334 0 0 0 -0.345 -0.086
                arcToRelative(
                    a = 0.334f,
                    b = 0.334f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.345f,
                    dy1 = -0.086f,
                )
                // a 3 3 0 1 1 -0.596 -5.836
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.596f,
                    dy1 = -5.836f,
                )
                // a 0.334 0.334 0 0 0 0.32 -0.153
                arcToRelative(
                    a = 0.334f,
                    b = 0.334f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.32f,
                    dy1 = -0.153f,
                )
                // A 4.997 4.997 0 0 1 7.9 3.5
                arcTo(
                    horizontalEllipseRadius = 4.997f,
                    verticalEllipseRadius = 4.997f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.9f,
                    y1 = 3.5f,
                )
                // c 0.75 0 1.461 0.165 2.1 0.461
                curveToRelative(
                    dx1 = 0.75f,
                    dy1 = 0.0f,
                    dx2 = 1.461f,
                    dy2 = 0.165f,
                    dx3 = 2.1f,
                    dy3 = 0.461f,
                )
                // v 1.134
                verticalLineToRelative(dy = 1.134f)
                // a 4.001 4.001 0 0 0 -5.787 1.85
                arcToRelative(
                    a = 4.001f,
                    b = 4.001f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -5.787f,
                    dy1 = 1.85f,
                )
                // c -0.077 0.183 -0.3 0.264 -0.485 0.191
                curveToRelative(
                    dx1 = -0.077f,
                    dy1 = 0.183f,
                    dx2 = -0.3f,
                    dy2 = 0.264f,
                    dx3 = -0.485f,
                    dy3 = 0.191f,
                )
                // a 2 2 0 1 0 0.362 3.54
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.362f,
                    dy1 = 3.54f,
                )
                // c 0.166 -0.107 0.401 -0.073 0.513 0.09
                curveToRelative(
                    dx1 = 0.166f,
                    dy1 = -0.107f,
                    dx2 = 0.401f,
                    dy2 = -0.073f,
                    dx3 = 0.513f,
                    dy3 = 0.09f,
                )
                // A 3.996 3.996 0 0 0 7.9 12.5
                arcTo(
                    horizontalEllipseRadius = 3.996f,
                    verticalEllipseRadius = 3.996f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.9f,
                    y1 = 12.5f,
                )
                // a 3.996 3.996 0 0 0 3.35 -1.813
                arcToRelative(
                    a = 3.996f,
                    b = 3.996f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 3.35f,
                    dy1 = -1.813f,
                )
                // c 0.117 -0.18 0.383 -0.211 0.555 -0.083
                curveToRelative(
                    dx1 = 0.117f,
                    dy1 = -0.18f,
                    dx2 = 0.383f,
                    dy2 = -0.211f,
                    dx3 = 0.555f,
                    dy3 = -0.083f,
                )
                // A 2 2 0 0 0 14.323 7.5
                arcTo(
                    horizontalEllipseRadius = 2.0f,
                    verticalEllipseRadius = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 14.323f,
                    y1 = 7.5f,
                )
                // h 1.276
                horizontalLineToRelative(dx = 1.276f)
                // a 3 3 0 0 1 -3.66 4.307
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.66f,
                    dy1 = 4.307f,
                )
                // a 0.335 0.335 0 0 0 -0.364 0.083z
                arcToRelative(
                    a = 0.335f,
                    b = 0.335f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.364f,
                    dy1 = 0.083f,
                )
                close()
            }
            // M7.605 6.105 c-.326 .197 -.553 .635 -.496 .997 L7.454 9.3 h1.078 l.358 -2.277 a.846 .846 0 0 0 -.408 -.853 l-.07 -.042 c-.256 -.156 -.55 -.176 -.807 -.023Z M8 11 a.6 .6 0 1 0 0 -1.2 .6 .6 0 0 0 0 1.2Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.605 6.105
                moveTo(x = 7.605f, y = 6.105f)
                // c -0.326 0.197 -0.553 0.635 -0.496 0.997
                curveToRelative(
                    dx1 = -0.326f,
                    dy1 = 0.197f,
                    dx2 = -0.553f,
                    dy2 = 0.635f,
                    dx3 = -0.496f,
                    dy3 = 0.997f,
                )
                // L 7.454 9.3
                lineTo(x = 7.454f, y = 9.3f)
                // h 1.078
                horizontalLineToRelative(dx = 1.078f)
                // l 0.358 -2.277
                lineToRelative(dx = 0.358f, dy = -2.277f)
                // a 0.846 0.846 0 0 0 -0.408 -0.853
                arcToRelative(
                    a = 0.846f,
                    b = 0.846f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.408f,
                    dy1 = -0.853f,
                )
                // l -0.07 -0.042
                lineToRelative(dx = -0.07f, dy = -0.042f)
                // c -0.256 -0.156 -0.55 -0.176 -0.807 -0.023z
                curveToRelative(
                    dx1 = -0.256f,
                    dy1 = -0.156f,
                    dx2 = -0.55f,
                    dy2 = -0.176f,
                    dx3 = -0.807f,
                    dy3 = -0.023f,
                )
                close()
                // M 8 11
                moveTo(x = 8.0f, y = 11.0f)
                // a 0.6 0.6 0 1 0 0 -1.2
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.2f,
                )
                // a 0.6 0.6 0 0 0 0 1.2z
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.2f,
                )
                close()
            }
        }.build().also { _ic2193 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2193: ImageVector? = null
