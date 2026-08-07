package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2109: ImageVector
    get() {
        val current = _ic2109
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2109",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8 16.001 a8 8 0 1 1 0 -16 8 8 0 0 1 0 16Z m0 -1.3 a6.7 6.7 0 1 0 0 -13.4 6.7 6.7 0 0 0 0 13.4Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8 16.001
                moveTo(x = 8.0f, y = 16.001f)
                // a 8 8 0 1 1 0 -16
                arcToRelative(
                    a = 8.0f,
                    b = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -16.0f,
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
                // a 6.7 6.7 0 1 0 0 -13.4
                arcToRelative(
                    a = 6.7f,
                    b = 6.7f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -13.4f,
                )
                // a 6.7 6.7 0 0 0 0 13.4z
                arcToRelative(
                    a = 6.7f,
                    b = 6.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 13.4f,
                )
                close()
            }
            // M8.375 2.375 a.375 .375 0 1 0 -.75 0 v.97 L7.14 2.86 a.375 .375 0 1 0 -.53 .53 l1.015 1.015 V5.75 l.001 .03 a2.248 2.248 0 0 0 -1.36 .787 .378 .378 0 0 0 -.027 -.017 l-1.165 -.672 -.371 -1.387 a.375 .375 0 1 0 -.725 .194 l.178 .663 -.84 -.485 a.375 .375 0 1 0 -.375 .65 l.84 .484 -.662 .178 a.375 .375 0 0 0 .194 .724 l1.386 -.372 1.165 .673 a.38 .38 0 0 0 .027 .014 2.245 2.245 0 0 0 0 1.572 .377 .377 0 0 0 -.027 .014 l-1.165 .673 L3.313 9.1 a.375 .375 0 1 0 -.194 .724 l.662 .178 -.84 .485 a.375 .375 0 0 0 .375 .65 l.84 -.486 -.178 .663 a.375 .375 0 1 0 .725 .194 l.371 -1.387 L6.24 9.45 a.382 .382 0 0 0 .026 -.017 c.338 .408 .816 .695 1.361 .786 a.38 .38 0 0 0 0 .031 v1.345 L6.61 12.61 a.375 .375 0 0 0 .53 .53 l.485 -.485 v.97 a.375 .375 0 0 0 .75 0 v-.97 l.485 .485 a.375 .375 0 0 0 .53 -.53 l-1.015 -1.015 V10.25 l-.001 -.03 a2.248 2.248 0 0 0 1.36 -.787 .383 .383 0 0 0 .027 .017 l1.165 .672 .371 1.387 a.375 .375 0 1 0 .725 -.194 l-.178 -.663 .84 .485 a.375 .375 0 0 0 .375 -.65 l-.84 -.484 .663 -.178 a.375 .375 0 0 0 -.195 -.724 l-1.386 .372 -1.165 -.673 a.364 .364 0 0 0 -.027 -.014 2.246 2.246 0 0 0 0 -1.572 .364 .364 0 0 0 .027 -.014 l1.165 -.673 1.386 .372 a.375 .375 0 0 0 .195 -.724 l-.663 -.178 .84 -.485 a.375 .375 0 0 0 -.375 -.65 l-.84 .486 .178 -.663 a.375 .375 0 1 0 -.725 -.194 l-.371 1.387 -1.165 .672 a.378 .378 0 0 0 -.026 .017 2.248 2.248 0 0 0 -1.361 -.786 l.001 -.031 V4.405 L9.39 3.39 a.375 .375 0 0 0 -.53 -.53 l-.485 .485 v-.97Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.375 2.375
                moveTo(x = 8.375f, y = 2.375f)
                // a 0.375 0.375 0 1 0 -0.75 0
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.75f,
                    dy1 = 0.0f,
                )
                // v 0.97
                verticalLineToRelative(dy = 0.97f)
                // L 7.14 2.86
                lineTo(x = 7.14f, y = 2.86f)
                // a 0.375 0.375 0 1 0 -0.53 0.53
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.53f,
                    dy1 = 0.53f,
                )
                // l 1.015 1.015
                lineToRelative(dx = 1.015f, dy = 1.015f)
                // V 5.75
                verticalLineTo(y = 5.75f)
                // l 0.001 0.03
                lineToRelative(dx = 0.001f, dy = 0.03f)
                // a 2.248 2.248 0 0 0 -1.36 0.787
                arcToRelative(
                    a = 2.248f,
                    b = 2.248f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.36f,
                    dy1 = 0.787f,
                )
                // a 0.378 0.378 0 0 0 -0.027 -0.017
                arcToRelative(
                    a = 0.378f,
                    b = 0.378f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.027f,
                    dy1 = -0.017f,
                )
                // l -1.165 -0.672
                lineToRelative(dx = -1.165f, dy = -0.672f)
                // l -0.371 -1.387
                lineToRelative(dx = -0.371f, dy = -1.387f)
                // a 0.375 0.375 0 1 0 -0.725 0.194
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.725f,
                    dy1 = 0.194f,
                )
                // l 0.178 0.663
                lineToRelative(dx = 0.178f, dy = 0.663f)
                // l -0.84 -0.485
                lineToRelative(dx = -0.84f, dy = -0.485f)
                // a 0.375 0.375 0 1 0 -0.375 0.65
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.375f,
                    dy1 = 0.65f,
                )
                // l 0.84 0.484
                lineToRelative(dx = 0.84f, dy = 0.484f)
                // l -0.662 0.178
                lineToRelative(dx = -0.662f, dy = 0.178f)
                // a 0.375 0.375 0 0 0 0.194 0.724
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.194f,
                    dy1 = 0.724f,
                )
                // l 1.386 -0.372
                lineToRelative(dx = 1.386f, dy = -0.372f)
                // l 1.165 0.673
                lineToRelative(dx = 1.165f, dy = 0.673f)
                // a 0.38 0.38 0 0 0 0.027 0.014
                arcToRelative(
                    a = 0.38f,
                    b = 0.38f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.027f,
                    dy1 = 0.014f,
                )
                // a 2.245 2.245 0 0 0 0 1.572
                arcToRelative(
                    a = 2.245f,
                    b = 2.245f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.572f,
                )
                // a 0.377 0.377 0 0 0 -0.027 0.014
                arcToRelative(
                    a = 0.377f,
                    b = 0.377f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.027f,
                    dy1 = 0.014f,
                )
                // l -1.165 0.673
                lineToRelative(dx = -1.165f, dy = 0.673f)
                // L 3.313 9.1
                lineTo(x = 3.313f, y = 9.1f)
                // a 0.375 0.375 0 1 0 -0.194 0.724
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.194f,
                    dy1 = 0.724f,
                )
                // l 0.662 0.178
                lineToRelative(dx = 0.662f, dy = 0.178f)
                // l -0.84 0.485
                lineToRelative(dx = -0.84f, dy = 0.485f)
                // a 0.375 0.375 0 0 0 0.375 0.65
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.375f,
                    dy1 = 0.65f,
                )
                // l 0.84 -0.486
                lineToRelative(dx = 0.84f, dy = -0.486f)
                // l -0.178 0.663
                lineToRelative(dx = -0.178f, dy = 0.663f)
                // a 0.375 0.375 0 1 0 0.725 0.194
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.725f,
                    dy1 = 0.194f,
                )
                // l 0.371 -1.387
                lineToRelative(dx = 0.371f, dy = -1.387f)
                // L 6.24 9.45
                lineTo(x = 6.24f, y = 9.45f)
                // a 0.382 0.382 0 0 0 0.026 -0.017
                arcToRelative(
                    a = 0.382f,
                    b = 0.382f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.026f,
                    dy1 = -0.017f,
                )
                // c 0.338 0.408 0.816 0.695 1.361 0.786
                curveToRelative(
                    dx1 = 0.338f,
                    dy1 = 0.408f,
                    dx2 = 0.816f,
                    dy2 = 0.695f,
                    dx3 = 1.361f,
                    dy3 = 0.786f,
                )
                // a 0.38 0.38 0 0 0 0 0.031
                arcToRelative(
                    a = 0.38f,
                    b = 0.38f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.031f,
                )
                // v 1.345
                verticalLineToRelative(dy = 1.345f)
                // L 6.61 12.61
                lineTo(x = 6.61f, y = 12.61f)
                // a 0.375 0.375 0 0 0 0.53 0.53
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.53f,
                    dy1 = 0.53f,
                )
                // l 0.485 -0.485
                lineToRelative(dx = 0.485f, dy = -0.485f)
                // v 0.97
                verticalLineToRelative(dy = 0.97f)
                // a 0.375 0.375 0 0 0 0.75 0
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.75f,
                    dy1 = 0.0f,
                )
                // v -0.97
                verticalLineToRelative(dy = -0.97f)
                // l 0.485 0.485
                lineToRelative(dx = 0.485f, dy = 0.485f)
                // a 0.375 0.375 0 0 0 0.53 -0.53
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.53f,
                    dy1 = -0.53f,
                )
                // l -1.015 -1.015
                lineToRelative(dx = -1.015f, dy = -1.015f)
                // V 10.25
                verticalLineTo(y = 10.25f)
                // l -0.001 -0.03
                lineToRelative(dx = -0.001f, dy = -0.03f)
                // a 2.248 2.248 0 0 0 1.36 -0.787
                arcToRelative(
                    a = 2.248f,
                    b = 2.248f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.36f,
                    dy1 = -0.787f,
                )
                // a 0.383 0.383 0 0 0 0.027 0.017
                arcToRelative(
                    a = 0.383f,
                    b = 0.383f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.027f,
                    dy1 = 0.017f,
                )
                // l 1.165 0.672
                lineToRelative(dx = 1.165f, dy = 0.672f)
                // l 0.371 1.387
                lineToRelative(dx = 0.371f, dy = 1.387f)
                // a 0.375 0.375 0 1 0 0.725 -0.194
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.725f,
                    dy1 = -0.194f,
                )
                // l -0.178 -0.663
                lineToRelative(dx = -0.178f, dy = -0.663f)
                // l 0.84 0.485
                lineToRelative(dx = 0.84f, dy = 0.485f)
                // a 0.375 0.375 0 0 0 0.375 -0.65
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.375f,
                    dy1 = -0.65f,
                )
                // l -0.84 -0.484
                lineToRelative(dx = -0.84f, dy = -0.484f)
                // l 0.663 -0.178
                lineToRelative(dx = 0.663f, dy = -0.178f)
                // a 0.375 0.375 0 0 0 -0.195 -0.724
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.195f,
                    dy1 = -0.724f,
                )
                // l -1.386 0.372
                lineToRelative(dx = -1.386f, dy = 0.372f)
                // l -1.165 -0.673
                lineToRelative(dx = -1.165f, dy = -0.673f)
                // a 0.364 0.364 0 0 0 -0.027 -0.014
                arcToRelative(
                    a = 0.364f,
                    b = 0.364f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.027f,
                    dy1 = -0.014f,
                )
                // a 2.246 2.246 0 0 0 0 -1.572
                arcToRelative(
                    a = 2.246f,
                    b = 2.246f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.572f,
                )
                // a 0.364 0.364 0 0 0 0.027 -0.014
                arcToRelative(
                    a = 0.364f,
                    b = 0.364f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.027f,
                    dy1 = -0.014f,
                )
                // l 1.165 -0.673
                lineToRelative(dx = 1.165f, dy = -0.673f)
                // l 1.386 0.372
                lineToRelative(dx = 1.386f, dy = 0.372f)
                // a 0.375 0.375 0 0 0 0.195 -0.724
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.195f,
                    dy1 = -0.724f,
                )
                // l -0.663 -0.178
                lineToRelative(dx = -0.663f, dy = -0.178f)
                // l 0.84 -0.485
                lineToRelative(dx = 0.84f, dy = -0.485f)
                // a 0.375 0.375 0 0 0 -0.375 -0.65
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.375f,
                    dy1 = -0.65f,
                )
                // l -0.84 0.486
                lineToRelative(dx = -0.84f, dy = 0.486f)
                // l 0.178 -0.663
                lineToRelative(dx = 0.178f, dy = -0.663f)
                // a 0.375 0.375 0 1 0 -0.725 -0.194
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.725f,
                    dy1 = -0.194f,
                )
                // l -0.371 1.387
                lineToRelative(dx = -0.371f, dy = 1.387f)
                // l -1.165 0.672
                lineToRelative(dx = -1.165f, dy = 0.672f)
                // a 0.378 0.378 0 0 0 -0.026 0.017
                arcToRelative(
                    a = 0.378f,
                    b = 0.378f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.026f,
                    dy1 = 0.017f,
                )
                // a 2.248 2.248 0 0 0 -1.361 -0.786
                arcToRelative(
                    a = 2.248f,
                    b = 2.248f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.361f,
                    dy1 = -0.786f,
                )
                // l 0.001 -0.031
                lineToRelative(dx = 0.001f, dy = -0.031f)
                // V 4.405
                verticalLineTo(y = 4.405f)
                // L 9.39 3.39
                lineTo(x = 9.39f, y = 3.39f)
                // a 0.375 0.375 0 0 0 -0.53 -0.53
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.53f,
                    dy1 = -0.53f,
                )
                // l -0.485 0.485
                lineToRelative(dx = -0.485f, dy = 0.485f)
                // v -0.97z
                verticalLineToRelative(dy = -0.97f)
                close()
            }
        }.build().also { _ic2109 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2109: ImageVector? = null
