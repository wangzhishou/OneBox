package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1069: ImageVector
    get() {
        val current = _ic1069
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1069",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.5 16 a7.5 7.5 0 1 0 -7.188 -5.351 l1.13 -.486 a6.281 6.281 0 1 1 2.561 3.556 l-.614 1.055 A7.465 7.465 0 0 0 8.5 16Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.5 16
                moveTo(x = 8.5f, y = 16.0f)
                // a 7.5 7.5 0 1 0 -7.188 -5.351
                arcToRelative(
                    a = 7.5f,
                    b = 7.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -7.188f,
                    dy1 = -5.351f,
                )
                // l 1.13 -0.486
                lineToRelative(dx = 1.13f, dy = -0.486f)
                // a 6.281 6.281 0 1 1 2.561 3.556
                arcToRelative(
                    a = 6.281f,
                    b = 6.281f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 2.561f,
                    dy1 = 3.556f,
                )
                // l -0.614 1.055
                lineToRelative(dx = -0.614f, dy = 1.055f)
                // A 7.465 7.465 0 0 0 8.5 16z
                arcTo(
                    horizontalEllipseRadius = 7.465f,
                    verticalEllipseRadius = 7.465f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 8.5f,
                    y1 = 16.0f,
                )
                close()
            }
            // m5.913 12.15 .153 .152 .768 -.768 1.449 .966 1.449 -.966 .768 .768 .566 -.566 -1.232 -1.232 -1.551 1.035 -1.492 -.903 -.878 1.513Z m1.374 -6.153 L5.589 4.3 l-.565 .566 1.128 1.128 L5 7.146 l.566 .566 L6.718 6.56 l.003 .003 .566 -.566Z m3.41 -1.694 L9 6 l.566 .566 .003 -.003 1.152 1.152 .566 -.566 -1.153 -1.152 1.129 -1.128 -.566 -.566Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5.913 12.15
                moveTo(x = 5.913f, y = 12.15f)
                // l 0.153 0.152
                lineToRelative(dx = 0.153f, dy = 0.152f)
                // l 0.768 -0.768
                lineToRelative(dx = 0.768f, dy = -0.768f)
                // l 1.449 0.966
                lineToRelative(dx = 1.449f, dy = 0.966f)
                // l 1.449 -0.966
                lineToRelative(dx = 1.449f, dy = -0.966f)
                // l 0.768 0.768
                lineToRelative(dx = 0.768f, dy = 0.768f)
                // l 0.566 -0.566
                lineToRelative(dx = 0.566f, dy = -0.566f)
                // l -1.232 -1.232
                lineToRelative(dx = -1.232f, dy = -1.232f)
                // l -1.551 1.035
                lineToRelative(dx = -1.551f, dy = 1.035f)
                // l -1.492 -0.903
                lineToRelative(dx = -1.492f, dy = -0.903f)
                // l -0.878 1.513z
                lineToRelative(dx = -0.878f, dy = 1.513f)
                close()
                // m 1.374 -6.153
                moveToRelative(dx = 1.374f, dy = -6.153f)
                // L 5.589 4.3
                lineTo(x = 5.589f, y = 4.3f)
                // l -0.565 0.566
                lineToRelative(dx = -0.565f, dy = 0.566f)
                // l 1.128 1.128
                lineToRelative(dx = 1.128f, dy = 1.128f)
                // L 5 7.146
                lineTo(x = 5.0f, y = 7.146f)
                // l 0.566 0.566
                lineToRelative(dx = 0.566f, dy = 0.566f)
                // L 6.718 6.56
                lineTo(x = 6.718f, y = 6.56f)
                // l 0.003 0.003
                lineToRelative(dx = 0.003f, dy = 0.003f)
                // l 0.566 -0.566z
                lineToRelative(dx = 0.566f, dy = -0.566f)
                close()
                // m 3.41 -1.694
                moveToRelative(dx = 3.41f, dy = -1.694f)
                // L 9 6
                lineTo(x = 9.0f, y = 6.0f)
                // l 0.566 0.566
                lineToRelative(dx = 0.566f, dy = 0.566f)
                // l 0.003 -0.003
                lineToRelative(dx = 0.003f, dy = -0.003f)
                // l 1.152 1.152
                lineToRelative(dx = 1.152f, dy = 1.152f)
                // l 0.566 -0.566
                lineToRelative(dx = 0.566f, dy = -0.566f)
                // l -1.153 -1.152
                lineToRelative(dx = -1.153f, dy = -1.152f)
                // l 1.129 -1.128
                lineToRelative(dx = 1.129f, dy = -1.128f)
                // l -0.566 -0.566z
                lineToRelative(dx = -0.566f, dy = -0.566f)
                close()
            }
            // M7.886 8.145 c.128 .14 .15 .346 .055 .51 L4.386 14.78 a.448 .448 0 0 1 -.597 .168 l-1.185 -.629 a.435 .435 0 0 1 -.226 -.465 l.225 -1.185 -2.3 -.754 A.438 .438 0 0 1 0 11.52 a.437 .437 0 0 1 .266 -.42 l7.111 -3.063 a.45 .45 0 0 1 .51 .108Z m-6.204 3.3 1.57 .515 c.21 .069 .337 .28 .296 .495 L3.311 13.7 l.518 .275 2.674 -4.607 -4.821 2.077Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.886 8.145
                moveTo(x = 7.886f, y = 8.145f)
                // c 0.128 0.14 0.15 0.346 0.055 0.51
                curveToRelative(
                    dx1 = 0.128f,
                    dy1 = 0.14f,
                    dx2 = 0.15f,
                    dy2 = 0.346f,
                    dx3 = 0.055f,
                    dy3 = 0.51f,
                )
                // L 4.386 14.78
                lineTo(x = 4.386f, y = 14.78f)
                // a 0.448 0.448 0 0 1 -0.597 0.168
                arcToRelative(
                    a = 0.448f,
                    b = 0.448f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.597f,
                    dy1 = 0.168f,
                )
                // l -1.185 -0.629
                lineToRelative(dx = -1.185f, dy = -0.629f)
                // a 0.435 0.435 0 0 1 -0.226 -0.465
                arcToRelative(
                    a = 0.435f,
                    b = 0.435f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.226f,
                    dy1 = -0.465f,
                )
                // l 0.225 -1.185
                lineToRelative(dx = 0.225f, dy = -1.185f)
                // l -2.3 -0.754
                lineToRelative(dx = -2.3f, dy = -0.754f)
                // A 0.438 0.438 0 0 1 0 11.52
                arcTo(
                    horizontalEllipseRadius = 0.438f,
                    verticalEllipseRadius = 0.438f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 0.0f,
                    y1 = 11.52f,
                )
                // a 0.437 0.437 0 0 1 0.266 -0.42
                arcToRelative(
                    a = 0.437f,
                    b = 0.437f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.266f,
                    dy1 = -0.42f,
                )
                // l 7.111 -3.063
                lineToRelative(dx = 7.111f, dy = -3.063f)
                // a 0.45 0.45 0 0 1 0.51 0.108z
                arcToRelative(
                    a = 0.45f,
                    b = 0.45f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.51f,
                    dy1 = 0.108f,
                )
                close()
                // m -6.204 3.3
                moveToRelative(dx = -6.204f, dy = 3.3f)
                // l 1.57 0.515
                lineToRelative(dx = 1.57f, dy = 0.515f)
                // c 0.21 0.069 0.337 0.28 0.296 0.495
                curveToRelative(
                    dx1 = 0.21f,
                    dy1 = 0.069f,
                    dx2 = 0.337f,
                    dy2 = 0.28f,
                    dx3 = 0.296f,
                    dy3 = 0.495f,
                )
                // L 3.311 13.7
                lineTo(x = 3.311f, y = 13.7f)
                // l 0.518 0.275
                lineToRelative(dx = 0.518f, dy = 0.275f)
                // l 2.674 -4.607
                lineToRelative(dx = 2.674f, dy = -4.607f)
                // l -4.821 2.077z
                lineToRelative(dx = -4.821f, dy = 2.077f)
                close()
            }
        }.build().also { _ic1069 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1069: ImageVector? = null
