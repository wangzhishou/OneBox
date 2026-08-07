package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1031: ImageVector
    get() {
        val current = _ic1031
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1031",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // m1.959 8.648 3.43 -2.169 a.288 .288 0 0 0 -.067 -.517 l-1.853 -.579 a5.17 5.17 0 0 1 .808 -1.08 5.254 5.254 0 0 1 1.668 -1.126 5.298 5.298 0 0 1 4.084 0 5.22 5.22 0 0 1 1.668 1.125 c.48 .478 .861 1.045 1.125 1.668 a.701 .701 0 0 0 1.292 -.546 6.612 6.612 0 0 0 -1.425 -2.113 6.635 6.635 0 0 0 -2.114 -1.425 6.7 6.7 0 0 0 -5.177 0 6.618 6.618 0 0 0 -2.114 1.425 c-.485 .484 -.86 1.047 -1.172 1.647 l-1.74 -.543 a.287 .287 0 0 0 -.35 .382 l1.517 3.717 a.289 .289 0 0 0 .42 .134Z m14.02 2.402 L14.46 7.334 a.286 .286 0 0 0 -.419 -.134 l-3.43 2.168 a.287 .287 0 0 0 .067 .517 l1.91 .598 a5.238 5.238 0 0 1 -.892 1.24 5.22 5.22 0 0 1 -3.71 1.535 5.285 5.285 0 0 1 -2.042 -.41 5.242 5.242 0 0 1 -1.668 -1.124 5.24 5.24 0 0 1 -1.125 -1.67 .701 .701 0 0 0 -1.292 .547 6.608 6.608 0 0 0 1.425 2.113 6.629 6.629 0 0 0 4.701 1.948 6.615 6.615 0 0 0 4.702 -1.948 6.63 6.63 0 0 0 1.258 -1.807 l1.681 .526 a.287 .287 0 0 0 .352 -.383Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 1.959 8.648
                moveTo(x = 1.959f, y = 8.648f)
                // l 3.43 -2.169
                lineToRelative(dx = 3.43f, dy = -2.169f)
                // a 0.288 0.288 0 0 0 -0.067 -0.517
                arcToRelative(
                    a = 0.288f,
                    b = 0.288f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.067f,
                    dy1 = -0.517f,
                )
                // l -1.853 -0.579
                lineToRelative(dx = -1.853f, dy = -0.579f)
                // a 5.17 5.17 0 0 1 0.808 -1.08
                arcToRelative(
                    a = 5.17f,
                    b = 5.17f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.808f,
                    dy1 = -1.08f,
                )
                // a 5.254 5.254 0 0 1 1.668 -1.126
                arcToRelative(
                    a = 5.254f,
                    b = 5.254f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.668f,
                    dy1 = -1.126f,
                )
                // a 5.298 5.298 0 0 1 4.084 0
                arcToRelative(
                    a = 5.298f,
                    b = 5.298f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 4.084f,
                    dy1 = 0.0f,
                )
                // a 5.22 5.22 0 0 1 1.668 1.125
                arcToRelative(
                    a = 5.22f,
                    b = 5.22f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.668f,
                    dy1 = 1.125f,
                )
                // c 0.48 0.478 0.861 1.045 1.125 1.668
                curveToRelative(
                    dx1 = 0.48f,
                    dy1 = 0.478f,
                    dx2 = 0.861f,
                    dy2 = 1.045f,
                    dx3 = 1.125f,
                    dy3 = 1.668f,
                )
                // a 0.701 0.701 0 0 0 1.292 -0.546
                arcToRelative(
                    a = 0.701f,
                    b = 0.701f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.292f,
                    dy1 = -0.546f,
                )
                // a 6.612 6.612 0 0 0 -1.425 -2.113
                arcToRelative(
                    a = 6.612f,
                    b = 6.612f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.425f,
                    dy1 = -2.113f,
                )
                // a 6.635 6.635 0 0 0 -2.114 -1.425
                arcToRelative(
                    a = 6.635f,
                    b = 6.635f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.114f,
                    dy1 = -1.425f,
                )
                // a 6.7 6.7 0 0 0 -5.177 0
                arcToRelative(
                    a = 6.7f,
                    b = 6.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -5.177f,
                    dy1 = 0.0f,
                )
                // a 6.618 6.618 0 0 0 -2.114 1.425
                arcToRelative(
                    a = 6.618f,
                    b = 6.618f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.114f,
                    dy1 = 1.425f,
                )
                // c -0.485 0.484 -0.86 1.047 -1.172 1.647
                curveToRelative(
                    dx1 = -0.485f,
                    dy1 = 0.484f,
                    dx2 = -0.86f,
                    dy2 = 1.047f,
                    dx3 = -1.172f,
                    dy3 = 1.647f,
                )
                // l -1.74 -0.543
                lineToRelative(dx = -1.74f, dy = -0.543f)
                // a 0.287 0.287 0 0 0 -0.35 0.382
                arcToRelative(
                    a = 0.287f,
                    b = 0.287f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = 0.382f,
                )
                // l 1.517 3.717
                lineToRelative(dx = 1.517f, dy = 3.717f)
                // a 0.289 0.289 0 0 0 0.42 0.134z
                arcToRelative(
                    a = 0.289f,
                    b = 0.289f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.42f,
                    dy1 = 0.134f,
                )
                close()
                // m 14.02 2.402
                moveToRelative(dx = 14.02f, dy = 2.402f)
                // L 14.46 7.334
                lineTo(x = 14.46f, y = 7.334f)
                // a 0.286 0.286 0 0 0 -0.419 -0.134
                arcToRelative(
                    a = 0.286f,
                    b = 0.286f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.419f,
                    dy1 = -0.134f,
                )
                // l -3.43 2.168
                lineToRelative(dx = -3.43f, dy = 2.168f)
                // a 0.287 0.287 0 0 0 0.067 0.517
                arcToRelative(
                    a = 0.287f,
                    b = 0.287f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.067f,
                    dy1 = 0.517f,
                )
                // l 1.91 0.598
                lineToRelative(dx = 1.91f, dy = 0.598f)
                // a 5.238 5.238 0 0 1 -0.892 1.24
                arcToRelative(
                    a = 5.238f,
                    b = 5.238f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.892f,
                    dy1 = 1.24f,
                )
                // a 5.22 5.22 0 0 1 -3.71 1.535
                arcToRelative(
                    a = 5.22f,
                    b = 5.22f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.71f,
                    dy1 = 1.535f,
                )
                // a 5.285 5.285 0 0 1 -2.042 -0.41
                arcToRelative(
                    a = 5.285f,
                    b = 5.285f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.042f,
                    dy1 = -0.41f,
                )
                // a 5.242 5.242 0 0 1 -1.668 -1.124
                arcToRelative(
                    a = 5.242f,
                    b = 5.242f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.668f,
                    dy1 = -1.124f,
                )
                // a 5.24 5.24 0 0 1 -1.125 -1.67
                arcToRelative(
                    a = 5.24f,
                    b = 5.24f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.125f,
                    dy1 = -1.67f,
                )
                // a 0.701 0.701 0 0 0 -1.292 0.547
                arcToRelative(
                    a = 0.701f,
                    b = 0.701f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.292f,
                    dy1 = 0.547f,
                )
                // a 6.608 6.608 0 0 0 1.425 2.113
                arcToRelative(
                    a = 6.608f,
                    b = 6.608f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.425f,
                    dy1 = 2.113f,
                )
                // a 6.629 6.629 0 0 0 4.701 1.948
                arcToRelative(
                    a = 6.629f,
                    b = 6.629f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 4.701f,
                    dy1 = 1.948f,
                )
                // a 6.615 6.615 0 0 0 4.702 -1.948
                arcToRelative(
                    a = 6.615f,
                    b = 6.615f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 4.702f,
                    dy1 = -1.948f,
                )
                // a 6.63 6.63 0 0 0 1.258 -1.807
                arcToRelative(
                    a = 6.63f,
                    b = 6.63f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.258f,
                    dy1 = -1.807f,
                )
                // l 1.681 0.526
                lineToRelative(dx = 1.681f, dy = 0.526f)
                // a 0.287 0.287 0 0 0 0.352 -0.383z
                arcToRelative(
                    a = 0.287f,
                    b = 0.287f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.352f,
                    dy1 = -0.383f,
                )
                close()
            }
        }.build().also { _ic1031 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1031: ImageVector? = null
