package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1216: ImageVector
    get() {
        val current = _ic1216
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1216",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M1.842 .842 c0 -.465 .377 -.842 .843 -.842 h8.42 a.842 .842 0 0 1 0 1.684 H7.738 v.842 h3.21 A5.053 5.053 0 0 1 16 7.58 v1.684 h-3.368 V7.58 c0 -.93 -.754 -1.684 -1.685 -1.684 H0 v-3.37 h6.053 v-.842 H2.685 a.842 .842 0 0 1 -.843 -.842Z M14.267 16 a1.66 1.66 0 0 0 1.666 -1.667 c0 -1.065 -1.666 -3.333 -1.666 -3.333 S12.6 13.222 12.6 14.333 c0 .926 .74 1.667 1.667 1.667Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 1.842 0.842
                moveTo(x = 1.842f, y = 0.842f)
                // c 0 -0.465 0.377 -0.842 0.843 -0.842
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.465f,
                    dx2 = 0.377f,
                    dy2 = -0.842f,
                    dx3 = 0.843f,
                    dy3 = -0.842f,
                )
                // h 8.42
                horizontalLineToRelative(dx = 8.42f)
                // a 0.842 0.842 0 0 1 0 1.684
                arcToRelative(
                    a = 0.842f,
                    b = 0.842f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.684f,
                )
                // H 7.738
                horizontalLineTo(x = 7.738f)
                // v 0.842
                verticalLineToRelative(dy = 0.842f)
                // h 3.21
                horizontalLineToRelative(dx = 3.21f)
                // A 5.053 5.053 0 0 1 16 7.58
                arcTo(
                    horizontalEllipseRadius = 5.053f,
                    verticalEllipseRadius = 5.053f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 16.0f,
                    y1 = 7.58f,
                )
                // v 1.684
                verticalLineToRelative(dy = 1.684f)
                // h -3.368
                horizontalLineToRelative(dx = -3.368f)
                // V 7.58
                verticalLineTo(y = 7.58f)
                // c 0 -0.93 -0.754 -1.684 -1.685 -1.684
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.93f,
                    dx2 = -0.754f,
                    dy2 = -1.684f,
                    dx3 = -1.685f,
                    dy3 = -1.684f,
                )
                // H 0
                horizontalLineTo(x = 0.0f)
                // v -3.37
                verticalLineToRelative(dy = -3.37f)
                // h 6.053
                horizontalLineToRelative(dx = 6.053f)
                // v -0.842
                verticalLineToRelative(dy = -0.842f)
                // H 2.685
                horizontalLineTo(x = 2.685f)
                // a 0.842 0.842 0 0 1 -0.843 -0.842z
                arcToRelative(
                    a = 0.842f,
                    b = 0.842f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.843f,
                    dy1 = -0.842f,
                )
                close()
                // M 14.267 16
                moveTo(x = 14.267f, y = 16.0f)
                // a 1.66 1.66 0 0 0 1.666 -1.667
                arcToRelative(
                    a = 1.66f,
                    b = 1.66f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.666f,
                    dy1 = -1.667f,
                )
                // c 0 -1.065 -1.666 -3.333 -1.666 -3.333
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.065f,
                    dx2 = -1.666f,
                    dy2 = -3.333f,
                    dx3 = -1.666f,
                    dy3 = -3.333f,
                )
                // S 12.6 13.222 12.6 14.333
                reflectiveCurveTo(
                    x1 = 12.6f,
                    y1 = 13.222f,
                    x2 = 12.6f,
                    y2 = 14.333f,
                )
                // c 0 0.926 0.74 1.667 1.667 1.667z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.926f,
                    dx2 = 0.74f,
                    dy2 = 1.667f,
                    dx3 = 1.667f,
                    dy3 = 1.667f,
                )
                close()
            }
            // M7.227 7.63 a.263 .263 0 0 0 -.454 0 l-3.738 6.482 a.26 .26 0 0 0 .227 .388 h7.476 a.26 .26 0 0 0 .227 -.388 L7.227 7.63Z m-.85 2.144 c-.032 -.282 .256 -.524 .623 -.524 s.655 .242 .623 .524 L7.34 12.25 h-.68 l-.282 -2.476Z m1.127 3.476 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.227 7.63
                moveTo(x = 7.227f, y = 7.63f)
                // a 0.263 0.263 0 0 0 -0.454 0
                arcToRelative(
                    a = 0.263f,
                    b = 0.263f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.454f,
                    dy1 = 0.0f,
                )
                // l -3.738 6.482
                lineToRelative(dx = -3.738f, dy = 6.482f)
                // a 0.26 0.26 0 0 0 0.227 0.388
                arcToRelative(
                    a = 0.26f,
                    b = 0.26f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.227f,
                    dy1 = 0.388f,
                )
                // h 7.476
                horizontalLineToRelative(dx = 7.476f)
                // a 0.26 0.26 0 0 0 0.227 -0.388
                arcToRelative(
                    a = 0.26f,
                    b = 0.26f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.227f,
                    dy1 = -0.388f,
                )
                // L 7.227 7.63z
                lineTo(x = 7.227f, y = 7.63f)
                close()
                // m -0.85 2.144
                moveToRelative(dx = -0.85f, dy = 2.144f)
                // c -0.032 -0.282 0.256 -0.524 0.623 -0.524
                curveToRelative(
                    dx1 = -0.032f,
                    dy1 = -0.282f,
                    dx2 = 0.256f,
                    dy2 = -0.524f,
                    dx3 = 0.623f,
                    dy3 = -0.524f,
                )
                // s 0.655 0.242 0.623 0.524
                reflectiveCurveToRelative(
                    dx1 = 0.655f,
                    dy1 = 0.242f,
                    dx2 = 0.623f,
                    dy2 = 0.524f,
                )
                // L 7.34 12.25
                lineTo(x = 7.34f, y = 12.25f)
                // h -0.68
                horizontalLineToRelative(dx = -0.68f)
                // l -0.282 -2.476z
                lineToRelative(dx = -0.282f, dy = -2.476f)
                close()
                // m 1.127 3.476
                moveToRelative(dx = 1.127f, dy = 3.476f)
                // a 0.5 0.5 0 1 1 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // a 0.5 0.5 0 0 1 1 0z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic1216 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1216: ImageVector? = null
