package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2122: ImageVector
    get() {
        val current = _ic2122
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2122",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M3.837 4.167 a1.167 1.167 0 0 1 2.333 0 v1.812 l1.57 -.906 a1.167 1.167 0 1 1 1.166 2.02 L7.336 8 l1.57 .906 a1.167 1.167 0 1 1 -1.166 2.021 l-1.57 -.906 v1.812 a1.167 1.167 0 1 1 -2.333 0 v-1.812 l-1.57 .906 A1.167 1.167 0 1 1 1.1 8.907 L2.67 8 1.1 7.094 a1.167 1.167 0 0 1 1.167 -2.021 l1.57 .906 V4.167Z M14.26 .5 16 1.661 l-.555 .832 -1.258 -.839 -1.964 .982 -2 -1 -2 1 -2 -1 -2 1 -2 -1 -1.777 .888 L0 1.63 2.224 .518 l2 1 2 -1 2 1 2 -1 2 1 L14.259 .5Z m1.517 4.811 L14.036 4.15 12 5.168 l-1.776 -.888 -.448 .894 L12 6.286 l1.964 -.982 1.259 .839 .554 -.832Z M14.26 6.727 16 7.887 l-.555 .833 -1.258 -.84 -1.964 .983 L10 7.751 l.447 -.894 1.777 .888 2.035 -1.018Z m0 3 1.74 1.16 -.555 .833 -1.258 -.84 -1.964 .983 L10 10.751 l.447 -.894 1.777 .888 2.035 -1.018Z m1.741 4.794 -1.742 -1.161 -2.035 1.018 -2 -1 -2 1 -2 -1 -2 1 -2 -1 L0 14.49 l.447 .894 1.777 -.888 2 1 2 -1 2 1 2 -1 2 1 1.964 -.982 1.258 .839 .555 -.832Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 3.837 4.167
                moveTo(x = 3.837f, y = 4.167f)
                // a 1.167 1.167 0 0 1 2.333 0
                arcToRelative(
                    a = 1.167f,
                    b = 1.167f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.333f,
                    dy1 = 0.0f,
                )
                // v 1.812
                verticalLineToRelative(dy = 1.812f)
                // l 1.57 -0.906
                lineToRelative(dx = 1.57f, dy = -0.906f)
                // a 1.167 1.167 0 1 1 1.166 2.02
                arcToRelative(
                    a = 1.167f,
                    b = 1.167f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 1.166f,
                    dy1 = 2.02f,
                )
                // L 7.336 8
                lineTo(x = 7.336f, y = 8.0f)
                // l 1.57 0.906
                lineToRelative(dx = 1.57f, dy = 0.906f)
                // a 1.167 1.167 0 1 1 -1.166 2.021
                arcToRelative(
                    a = 1.167f,
                    b = 1.167f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.166f,
                    dy1 = 2.021f,
                )
                // l -1.57 -0.906
                lineToRelative(dx = -1.57f, dy = -0.906f)
                // v 1.812
                verticalLineToRelative(dy = 1.812f)
                // a 1.167 1.167 0 1 1 -2.333 0
                arcToRelative(
                    a = 1.167f,
                    b = 1.167f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -2.333f,
                    dy1 = 0.0f,
                )
                // v -1.812
                verticalLineToRelative(dy = -1.812f)
                // l -1.57 0.906
                lineToRelative(dx = -1.57f, dy = 0.906f)
                // A 1.167 1.167 0 1 1 1.1 8.907
                arcTo(
                    horizontalEllipseRadius = 1.167f,
                    verticalEllipseRadius = 1.167f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 1.1f,
                    y1 = 8.907f,
                )
                // L 2.67 8
                lineTo(x = 2.67f, y = 8.0f)
                // L 1.1 7.094
                lineTo(x = 1.1f, y = 7.094f)
                // a 1.167 1.167 0 0 1 1.167 -2.021
                arcToRelative(
                    a = 1.167f,
                    b = 1.167f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.167f,
                    dy1 = -2.021f,
                )
                // l 1.57 0.906
                lineToRelative(dx = 1.57f, dy = 0.906f)
                // V 4.167z
                verticalLineTo(y = 4.167f)
                close()
                // M 14.26 0.5
                moveTo(x = 14.26f, y = 0.5f)
                // L 16 1.661
                lineTo(x = 16.0f, y = 1.661f)
                // l -0.555 0.832
                lineToRelative(dx = -0.555f, dy = 0.832f)
                // l -1.258 -0.839
                lineToRelative(dx = -1.258f, dy = -0.839f)
                // l -1.964 0.982
                lineToRelative(dx = -1.964f, dy = 0.982f)
                // l -2 -1
                lineToRelative(dx = -2.0f, dy = -1.0f)
                // l -2 1
                lineToRelative(dx = -2.0f, dy = 1.0f)
                // l -2 -1
                lineToRelative(dx = -2.0f, dy = -1.0f)
                // l -2 1
                lineToRelative(dx = -2.0f, dy = 1.0f)
                // l -2 -1
                lineToRelative(dx = -2.0f, dy = -1.0f)
                // l -1.777 0.888
                lineToRelative(dx = -1.777f, dy = 0.888f)
                // L 0 1.63
                lineTo(x = 0.0f, y = 1.63f)
                // L 2.224 0.518
                lineTo(x = 2.224f, y = 0.518f)
                // l 2 1
                lineToRelative(dx = 2.0f, dy = 1.0f)
                // l 2 -1
                lineToRelative(dx = 2.0f, dy = -1.0f)
                // l 2 1
                lineToRelative(dx = 2.0f, dy = 1.0f)
                // l 2 -1
                lineToRelative(dx = 2.0f, dy = -1.0f)
                // l 2 1
                lineToRelative(dx = 2.0f, dy = 1.0f)
                // L 14.259 0.5z
                lineTo(x = 14.259f, y = 0.5f)
                close()
                // m 1.517 4.811
                moveToRelative(dx = 1.517f, dy = 4.811f)
                // L 14.036 4.15
                lineTo(x = 14.036f, y = 4.15f)
                // L 12 5.168
                lineTo(x = 12.0f, y = 5.168f)
                // l -1.776 -0.888
                lineToRelative(dx = -1.776f, dy = -0.888f)
                // l -0.448 0.894
                lineToRelative(dx = -0.448f, dy = 0.894f)
                // L 12 6.286
                lineTo(x = 12.0f, y = 6.286f)
                // l 1.964 -0.982
                lineToRelative(dx = 1.964f, dy = -0.982f)
                // l 1.259 0.839
                lineToRelative(dx = 1.259f, dy = 0.839f)
                // l 0.554 -0.832z
                lineToRelative(dx = 0.554f, dy = -0.832f)
                close()
                // M 14.26 6.727
                moveTo(x = 14.26f, y = 6.727f)
                // L 16 7.887
                lineTo(x = 16.0f, y = 7.887f)
                // l -0.555 0.833
                lineToRelative(dx = -0.555f, dy = 0.833f)
                // l -1.258 -0.84
                lineToRelative(dx = -1.258f, dy = -0.84f)
                // l -1.964 0.983
                lineToRelative(dx = -1.964f, dy = 0.983f)
                // L 10 7.751
                lineTo(x = 10.0f, y = 7.751f)
                // l 0.447 -0.894
                lineToRelative(dx = 0.447f, dy = -0.894f)
                // l 1.777 0.888
                lineToRelative(dx = 1.777f, dy = 0.888f)
                // l 2.035 -1.018z
                lineToRelative(dx = 2.035f, dy = -1.018f)
                close()
                // m 0 3
                moveToRelative(dx = 0.0f, dy = 3.0f)
                // l 1.74 1.16
                lineToRelative(dx = 1.74f, dy = 1.16f)
                // l -0.555 0.833
                lineToRelative(dx = -0.555f, dy = 0.833f)
                // l -1.258 -0.84
                lineToRelative(dx = -1.258f, dy = -0.84f)
                // l -1.964 0.983
                lineToRelative(dx = -1.964f, dy = 0.983f)
                // L 10 10.751
                lineTo(x = 10.0f, y = 10.751f)
                // l 0.447 -0.894
                lineToRelative(dx = 0.447f, dy = -0.894f)
                // l 1.777 0.888
                lineToRelative(dx = 1.777f, dy = 0.888f)
                // l 2.035 -1.018z
                lineToRelative(dx = 2.035f, dy = -1.018f)
                close()
                // m 1.741 4.794
                moveToRelative(dx = 1.741f, dy = 4.794f)
                // l -1.742 -1.161
                lineToRelative(dx = -1.742f, dy = -1.161f)
                // l -2.035 1.018
                lineToRelative(dx = -2.035f, dy = 1.018f)
                // l -2 -1
                lineToRelative(dx = -2.0f, dy = -1.0f)
                // l -2 1
                lineToRelative(dx = -2.0f, dy = 1.0f)
                // l -2 -1
                lineToRelative(dx = -2.0f, dy = -1.0f)
                // l -2 1
                lineToRelative(dx = -2.0f, dy = 1.0f)
                // l -2 -1
                lineToRelative(dx = -2.0f, dy = -1.0f)
                // L 0 14.49
                lineTo(x = 0.0f, y = 14.49f)
                // l 0.447 0.894
                lineToRelative(dx = 0.447f, dy = 0.894f)
                // l 1.777 -0.888
                lineToRelative(dx = 1.777f, dy = -0.888f)
                // l 2 1
                lineToRelative(dx = 2.0f, dy = 1.0f)
                // l 2 -1
                lineToRelative(dx = 2.0f, dy = -1.0f)
                // l 2 1
                lineToRelative(dx = 2.0f, dy = 1.0f)
                // l 2 -1
                lineToRelative(dx = 2.0f, dy = -1.0f)
                // l 2 1
                lineToRelative(dx = 2.0f, dy = 1.0f)
                // l 1.964 -0.982
                lineToRelative(dx = 1.964f, dy = -0.982f)
                // l 1.258 0.839
                lineToRelative(dx = 1.258f, dy = 0.839f)
                // l 0.555 -0.832z
                lineToRelative(dx = 0.555f, dy = -0.832f)
                close()
            }
        }.build().also { _ic2122 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2122: ImageVector? = null
