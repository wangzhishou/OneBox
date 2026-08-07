package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2205: ImageVector
    get() {
        val current = _ic2205
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2205",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M11.5 3 a.5 .5 0 0 0 -.5 .5 v6.063 a2 2 0 1 0 1 0 V3.5 a.5 .5 0 0 0 -.5 -.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.5 3
                moveTo(x = 11.5f, y = 3.0f)
                // a 0.5 0.5 0 0 0 -0.5 0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.5f,
                    dy1 = 0.5f,
                )
                // v 6.063
                verticalLineToRelative(dy = 6.063f)
                // a 2 2 0 1 0 1 0
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // V 3.5
                verticalLineTo(y = 3.5f)
                // a 0.5 0.5 0 0 0 -0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                close()
            }
            // m10.2 8.399 -.532 .356 a3.3 3.3 0 1 0 3.665 0 l-.533 -.356 V2.5 a1.3 1.3 0 1 0 -2.6 0 v5.899Z M9 2.5 a2.5 2.5 0 0 1 5 0 v5.258 a4.5 4.5 0 1 1 -5 0 V2.5Z m-2.503 .577 a.658 .658 0 0 1 -.92 -.516 L5.44 1.484 A.658 .658 0 0 1 6.008 .75 a.669 .669 0 0 1 .734 .569 l.136 1.077 a.658 .658 0 0 1 -.38 .681Z M3.122 4.784 a.653 .653 0 0 1 -.229 -.115 l-.857 -.665 a.66 .66 0 0 1 -.117 -.922 .658 .658 0 0 1 .92 -.117 l.857 .664 a.658 .658 0 0 1 -.574 1.155Z M2.72 6.877 a.664 .664 0 0 1 .25 .433 .658 .658 0 0 1 -.568 .734 l-1.075 .138 a.657 .657 0 0 1 -.166 -1.304 l1.077 -.137 c.172 -.02 .345 .03 .482 .136Z m-.044 4.132 .664 -.859 a.657 .657 0 1 1 1.037 .804 l-.664 .859 a.65 .65 0 0 1 -.92 .118 .66 .66 0 0 1 -.117 -.922Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.2 8.399
                moveTo(x = 10.2f, y = 8.399f)
                // l -0.532 0.356
                lineToRelative(dx = -0.532f, dy = 0.356f)
                // a 3.3 3.3 0 1 0 3.665 0
                arcToRelative(
                    a = 3.3f,
                    b = 3.3f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 3.665f,
                    dy1 = 0.0f,
                )
                // l -0.533 -0.356
                lineToRelative(dx = -0.533f, dy = -0.356f)
                // V 2.5
                verticalLineTo(y = 2.5f)
                // a 1.3 1.3 0 1 0 -2.6 0
                arcToRelative(
                    a = 1.3f,
                    b = 1.3f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -2.6f,
                    dy1 = 0.0f,
                )
                // v 5.899z
                verticalLineToRelative(dy = 5.899f)
                close()
                // M 9 2.5
                moveTo(x = 9.0f, y = 2.5f)
                // a 2.5 2.5 0 0 1 5 0
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 5.0f,
                    dy1 = 0.0f,
                )
                // v 5.258
                verticalLineToRelative(dy = 5.258f)
                // a 4.5 4.5 0 1 1 -5 0
                arcToRelative(
                    a = 4.5f,
                    b = 4.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -5.0f,
                    dy1 = 0.0f,
                )
                // V 2.5z
                verticalLineTo(y = 2.5f)
                close()
                // m -2.503 0.577
                moveToRelative(dx = -2.503f, dy = 0.577f)
                // a 0.658 0.658 0 0 1 -0.92 -0.516
                arcToRelative(
                    a = 0.658f,
                    b = 0.658f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.92f,
                    dy1 = -0.516f,
                )
                // L 5.44 1.484
                lineTo(x = 5.44f, y = 1.484f)
                // A 0.658 0.658 0 0 1 6.008 0.75
                arcTo(
                    horizontalEllipseRadius = 0.658f,
                    verticalEllipseRadius = 0.658f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 6.008f,
                    y1 = 0.75f,
                )
                // a 0.669 0.669 0 0 1 0.734 0.569
                arcToRelative(
                    a = 0.669f,
                    b = 0.669f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.734f,
                    dy1 = 0.569f,
                )
                // l 0.136 1.077
                lineToRelative(dx = 0.136f, dy = 1.077f)
                // a 0.658 0.658 0 0 1 -0.38 0.681z
                arcToRelative(
                    a = 0.658f,
                    b = 0.658f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.38f,
                    dy1 = 0.681f,
                )
                close()
                // M 3.122 4.784
                moveTo(x = 3.122f, y = 4.784f)
                // a 0.653 0.653 0 0 1 -0.229 -0.115
                arcToRelative(
                    a = 0.653f,
                    b = 0.653f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.229f,
                    dy1 = -0.115f,
                )
                // l -0.857 -0.665
                lineToRelative(dx = -0.857f, dy = -0.665f)
                // a 0.66 0.66 0 0 1 -0.117 -0.922
                arcToRelative(
                    a = 0.66f,
                    b = 0.66f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.117f,
                    dy1 = -0.922f,
                )
                // a 0.658 0.658 0 0 1 0.92 -0.117
                arcToRelative(
                    a = 0.658f,
                    b = 0.658f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.92f,
                    dy1 = -0.117f,
                )
                // l 0.857 0.664
                lineToRelative(dx = 0.857f, dy = 0.664f)
                // a 0.658 0.658 0 0 1 -0.574 1.155z
                arcToRelative(
                    a = 0.658f,
                    b = 0.658f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.574f,
                    dy1 = 1.155f,
                )
                close()
                // M 2.72 6.877
                moveTo(x = 2.72f, y = 6.877f)
                // a 0.664 0.664 0 0 1 0.25 0.433
                arcToRelative(
                    a = 0.664f,
                    b = 0.664f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.25f,
                    dy1 = 0.433f,
                )
                // a 0.658 0.658 0 0 1 -0.568 0.734
                arcToRelative(
                    a = 0.658f,
                    b = 0.658f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.568f,
                    dy1 = 0.734f,
                )
                // l -1.075 0.138
                lineToRelative(dx = -1.075f, dy = 0.138f)
                // a 0.657 0.657 0 0 1 -0.166 -1.304
                arcToRelative(
                    a = 0.657f,
                    b = 0.657f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.166f,
                    dy1 = -1.304f,
                )
                // l 1.077 -0.137
                lineToRelative(dx = 1.077f, dy = -0.137f)
                // c 0.172 -0.02 0.345 0.03 0.482 0.136z
                curveToRelative(
                    dx1 = 0.172f,
                    dy1 = -0.02f,
                    dx2 = 0.345f,
                    dy2 = 0.03f,
                    dx3 = 0.482f,
                    dy3 = 0.136f,
                )
                close()
                // m -0.044 4.132
                moveToRelative(dx = -0.044f, dy = 4.132f)
                // l 0.664 -0.859
                lineToRelative(dx = 0.664f, dy = -0.859f)
                // a 0.657 0.657 0 1 1 1.037 0.804
                arcToRelative(
                    a = 0.657f,
                    b = 0.657f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 1.037f,
                    dy1 = 0.804f,
                )
                // l -0.664 0.859
                lineToRelative(dx = -0.664f, dy = 0.859f)
                // a 0.65 0.65 0 0 1 -0.92 0.118
                arcToRelative(
                    a = 0.65f,
                    b = 0.65f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.92f,
                    dy1 = 0.118f,
                )
                // a 0.66 0.66 0 0 1 -0.117 -0.922z
                arcToRelative(
                    a = 0.66f,
                    b = 0.66f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.117f,
                    dy1 = -0.922f,
                )
                close()
            }
            // M6.748 4.068 a2.986 2.986 0 0 1 1.743 .415 l-.388 .94 a2 2 0 1 0 -1.488 3.61 l-.387 .94 a3.001 3.001 0 0 1 .52 -5.905Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6.748 4.068
                moveTo(x = 6.748f, y = 4.068f)
                // a 2.986 2.986 0 0 1 1.743 0.415
                arcToRelative(
                    a = 2.986f,
                    b = 2.986f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.743f,
                    dy1 = 0.415f,
                )
                // l -0.388 0.94
                lineToRelative(dx = -0.388f, dy = 0.94f)
                // a 2 2 0 1 0 -1.488 3.61
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -1.488f,
                    dy1 = 3.61f,
                )
                // l -0.387 0.94
                lineToRelative(dx = -0.387f, dy = 0.94f)
                // a 3.001 3.001 0 0 1 0.52 -5.905z
                arcToRelative(
                    a = 3.001f,
                    b = 3.001f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.52f,
                    dy1 = -5.905f,
                )
                close()
            }
        }.build().also { _ic2205 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2205: ImageVector? = null
