package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1044: ImageVector
    get() {
        val current = _ic1044
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1044",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M3.981 3.721 a.194 .194 0 0 0 -.26 .26 l1.45 3.312 2.122 -2.121 -3.312 -1.45Z m1.544 3.926 2.121 -2.122 4.597 4.596 -2.122 2.122 -4.596 -4.596Z m4.95 4.949 2.121 -2.121 .354 .354 a.5 .5 0 0 1 0 .707 l-1.414 1.414 a.5 .5 0 0 1 -.708 0 l-.353 -.354Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 3.981 3.721
                moveTo(x = 3.981f, y = 3.721f)
                // a 0.194 0.194 0 0 0 -0.26 0.26
                arcToRelative(
                    a = 0.194f,
                    b = 0.194f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.26f,
                    dy1 = 0.26f,
                )
                // l 1.45 3.312
                lineToRelative(dx = 1.45f, dy = 3.312f)
                // l 2.122 -2.121
                lineToRelative(dx = 2.122f, dy = -2.121f)
                // l -3.312 -1.45z
                lineToRelative(dx = -3.312f, dy = -1.45f)
                close()
                // m 1.544 3.926
                moveToRelative(dx = 1.544f, dy = 3.926f)
                // l 2.121 -2.122
                lineToRelative(dx = 2.121f, dy = -2.122f)
                // l 4.597 4.596
                lineToRelative(dx = 4.597f, dy = 4.596f)
                // l -2.122 2.122
                lineToRelative(dx = -2.122f, dy = 2.122f)
                // l -4.596 -4.596z
                lineToRelative(dx = -4.596f, dy = -4.596f)
                close()
                // m 4.95 4.949
                moveToRelative(dx = 4.95f, dy = 4.949f)
                // l 2.121 -2.121
                lineToRelative(dx = 2.121f, dy = -2.121f)
                // l 0.354 0.354
                lineToRelative(dx = 0.354f, dy = 0.354f)
                // a 0.5 0.5 0 0 1 0 0.707
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.707f,
                )
                // l -1.414 1.414
                lineToRelative(dx = -1.414f, dy = 1.414f)
                // a 0.5 0.5 0 0 1 -0.708 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.708f,
                    dy1 = 0.0f,
                )
                // l -0.353 -0.354z
                lineToRelative(dx = -0.353f, dy = -0.354f)
                close()
            }
            // M16 8 A8 8 0 1 1 0 8 a8 8 0 0 1 16 0Z m-1.3 0 a6.67 6.67 0 0 0 -1.352 -4.037 l-9.385 9.385 A6.7 6.7 0 0 0 14.7 8Z m-2.385 -5.126 a6.7 6.7 0 0 0 -9.44 9.44 l9.44 -9.44Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 16 8
                moveTo(x = 16.0f, y = 8.0f)
                // A 8 8 0 1 1 0 8
                arcTo(
                    horizontalEllipseRadius = 8.0f,
                    verticalEllipseRadius = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 0.0f,
                    y1 = 8.0f,
                )
                // a 8 8 0 0 1 16 0z
                arcToRelative(
                    a = 8.0f,
                    b = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 16.0f,
                    dy1 = 0.0f,
                )
                close()
                // m -1.3 0
                moveToRelative(dx = -1.3f, dy = 0.0f)
                // a 6.67 6.67 0 0 0 -1.352 -4.037
                arcToRelative(
                    a = 6.67f,
                    b = 6.67f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.352f,
                    dy1 = -4.037f,
                )
                // l -9.385 9.385
                lineToRelative(dx = -9.385f, dy = 9.385f)
                // A 6.7 6.7 0 0 0 14.7 8z
                arcTo(
                    horizontalEllipseRadius = 6.7f,
                    verticalEllipseRadius = 6.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 14.7f,
                    y1 = 8.0f,
                )
                close()
                // m -2.385 -5.126
                moveToRelative(dx = -2.385f, dy = -5.126f)
                // a 6.7 6.7 0 0 0 -9.44 9.44
                arcToRelative(
                    a = 6.7f,
                    b = 6.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -9.44f,
                    dy1 = 9.44f,
                )
                // l 9.44 -9.44z
                lineToRelative(dx = 9.44f, dy = -9.44f)
                close()
            }
        }.build().also { _ic1044 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1044: ImageVector? = null
