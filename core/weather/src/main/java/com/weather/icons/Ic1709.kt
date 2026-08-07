package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1709: ImageVector
    get() {
        val current = _ic1709
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1709",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M11.833 14 c1.809 -.038 2.828 .07 4.594 .485 a.47 .47 0 0 0 .562 -.377 .497 .497 0 0 0 -.357 -.593 c-1.84 -.432 -2.934 -.548 -4.818 -.508 -1.174 .025 -2.072 .274 -2.922 .51 l-.012 .003 c-.852 .236 -1.657 .458 -2.713 .48 -1.809 .038 -2.828 -.07 -4.594 -.485 a.47 .47 0 0 0 -.562 .377 .497 .497 0 0 0 .357 .593 c1.84 .432 2.934 .548 4.818 .508 1.174 -.025 2.072 -.274 2.922 -.51 l.012 -.003 c.852 -.236 1.657 -.458 2.713 -.48Z m0 2 c1.809 -.038 2.828 .07 4.594 .485 a.47 .47 0 0 0 .562 -.377 .497 .497 0 0 0 -.357 -.593 c-1.84 -.432 -2.934 -.548 -4.818 -.508 -1.174 .025 -2.072 .274 -2.922 .51 l-.012 .003 c-.852 .236 -1.657 .458 -2.713 .48 -1.809 .038 -2.828 -.07 -4.594 -.485 a.47 .47 0 0 0 -.562 .377 .497 .497 0 0 0 .357 .593 c1.84 .432 2.934 .547 4.818 .508 1.174 -.025 2.072 -.274 2.922 -.51 l.012 -.003 c.852 -.236 1.657 -.458 2.713 -.48Z m-.084 -6.458 L14.599 4 17 11 h-4.865 l.264 1 H.892 l3.214 -5.67 1.842 2.9 L9.48 .967 l2.269 8.575Z m.85 .401 h3.063 L14.437 6.37 12.6 9.943Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.833 14
                moveTo(x = 11.833f, y = 14.0f)
                // c 1.809 -0.038 2.828 0.07 4.594 0.485
                curveToRelative(
                    dx1 = 1.809f,
                    dy1 = -0.038f,
                    dx2 = 2.828f,
                    dy2 = 0.07f,
                    dx3 = 4.594f,
                    dy3 = 0.485f,
                )
                // a 0.47 0.47 0 0 0 0.562 -0.377
                arcToRelative(
                    a = 0.47f,
                    b = 0.47f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.562f,
                    dy1 = -0.377f,
                )
                // a 0.497 0.497 0 0 0 -0.357 -0.593
                arcToRelative(
                    a = 0.497f,
                    b = 0.497f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.357f,
                    dy1 = -0.593f,
                )
                // c -1.84 -0.432 -2.934 -0.548 -4.818 -0.508
                curveToRelative(
                    dx1 = -1.84f,
                    dy1 = -0.432f,
                    dx2 = -2.934f,
                    dy2 = -0.548f,
                    dx3 = -4.818f,
                    dy3 = -0.508f,
                )
                // c -1.174 0.025 -2.072 0.274 -2.922 0.51
                curveToRelative(
                    dx1 = -1.174f,
                    dy1 = 0.025f,
                    dx2 = -2.072f,
                    dy2 = 0.274f,
                    dx3 = -2.922f,
                    dy3 = 0.51f,
                )
                // l -0.012 0.003
                lineToRelative(dx = -0.012f, dy = 0.003f)
                // c -0.852 0.236 -1.657 0.458 -2.713 0.48
                curveToRelative(
                    dx1 = -0.852f,
                    dy1 = 0.236f,
                    dx2 = -1.657f,
                    dy2 = 0.458f,
                    dx3 = -2.713f,
                    dy3 = 0.48f,
                )
                // c -1.809 0.038 -2.828 -0.07 -4.594 -0.485
                curveToRelative(
                    dx1 = -1.809f,
                    dy1 = 0.038f,
                    dx2 = -2.828f,
                    dy2 = -0.07f,
                    dx3 = -4.594f,
                    dy3 = -0.485f,
                )
                // a 0.47 0.47 0 0 0 -0.562 0.377
                arcToRelative(
                    a = 0.47f,
                    b = 0.47f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.562f,
                    dy1 = 0.377f,
                )
                // a 0.497 0.497 0 0 0 0.357 0.593
                arcToRelative(
                    a = 0.497f,
                    b = 0.497f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.357f,
                    dy1 = 0.593f,
                )
                // c 1.84 0.432 2.934 0.548 4.818 0.508
                curveToRelative(
                    dx1 = 1.84f,
                    dy1 = 0.432f,
                    dx2 = 2.934f,
                    dy2 = 0.548f,
                    dx3 = 4.818f,
                    dy3 = 0.508f,
                )
                // c 1.174 -0.025 2.072 -0.274 2.922 -0.51
                curveToRelative(
                    dx1 = 1.174f,
                    dy1 = -0.025f,
                    dx2 = 2.072f,
                    dy2 = -0.274f,
                    dx3 = 2.922f,
                    dy3 = -0.51f,
                )
                // l 0.012 -0.003
                lineToRelative(dx = 0.012f, dy = -0.003f)
                // c 0.852 -0.236 1.657 -0.458 2.713 -0.48z
                curveToRelative(
                    dx1 = 0.852f,
                    dy1 = -0.236f,
                    dx2 = 1.657f,
                    dy2 = -0.458f,
                    dx3 = 2.713f,
                    dy3 = -0.48f,
                )
                close()
                // m 0 2
                moveToRelative(dx = 0.0f, dy = 2.0f)
                // c 1.809 -0.038 2.828 0.07 4.594 0.485
                curveToRelative(
                    dx1 = 1.809f,
                    dy1 = -0.038f,
                    dx2 = 2.828f,
                    dy2 = 0.07f,
                    dx3 = 4.594f,
                    dy3 = 0.485f,
                )
                // a 0.47 0.47 0 0 0 0.562 -0.377
                arcToRelative(
                    a = 0.47f,
                    b = 0.47f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.562f,
                    dy1 = -0.377f,
                )
                // a 0.497 0.497 0 0 0 -0.357 -0.593
                arcToRelative(
                    a = 0.497f,
                    b = 0.497f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.357f,
                    dy1 = -0.593f,
                )
                // c -1.84 -0.432 -2.934 -0.548 -4.818 -0.508
                curveToRelative(
                    dx1 = -1.84f,
                    dy1 = -0.432f,
                    dx2 = -2.934f,
                    dy2 = -0.548f,
                    dx3 = -4.818f,
                    dy3 = -0.508f,
                )
                // c -1.174 0.025 -2.072 0.274 -2.922 0.51
                curveToRelative(
                    dx1 = -1.174f,
                    dy1 = 0.025f,
                    dx2 = -2.072f,
                    dy2 = 0.274f,
                    dx3 = -2.922f,
                    dy3 = 0.51f,
                )
                // l -0.012 0.003
                lineToRelative(dx = -0.012f, dy = 0.003f)
                // c -0.852 0.236 -1.657 0.458 -2.713 0.48
                curveToRelative(
                    dx1 = -0.852f,
                    dy1 = 0.236f,
                    dx2 = -1.657f,
                    dy2 = 0.458f,
                    dx3 = -2.713f,
                    dy3 = 0.48f,
                )
                // c -1.809 0.038 -2.828 -0.07 -4.594 -0.485
                curveToRelative(
                    dx1 = -1.809f,
                    dy1 = 0.038f,
                    dx2 = -2.828f,
                    dy2 = -0.07f,
                    dx3 = -4.594f,
                    dy3 = -0.485f,
                )
                // a 0.47 0.47 0 0 0 -0.562 0.377
                arcToRelative(
                    a = 0.47f,
                    b = 0.47f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.562f,
                    dy1 = 0.377f,
                )
                // a 0.497 0.497 0 0 0 0.357 0.593
                arcToRelative(
                    a = 0.497f,
                    b = 0.497f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.357f,
                    dy1 = 0.593f,
                )
                // c 1.84 0.432 2.934 0.547 4.818 0.508
                curveToRelative(
                    dx1 = 1.84f,
                    dy1 = 0.432f,
                    dx2 = 2.934f,
                    dy2 = 0.547f,
                    dx3 = 4.818f,
                    dy3 = 0.508f,
                )
                // c 1.174 -0.025 2.072 -0.274 2.922 -0.51
                curveToRelative(
                    dx1 = 1.174f,
                    dy1 = -0.025f,
                    dx2 = 2.072f,
                    dy2 = -0.274f,
                    dx3 = 2.922f,
                    dy3 = -0.51f,
                )
                // l 0.012 -0.003
                lineToRelative(dx = 0.012f, dy = -0.003f)
                // c 0.852 -0.236 1.657 -0.458 2.713 -0.48z
                curveToRelative(
                    dx1 = 0.852f,
                    dy1 = -0.236f,
                    dx2 = 1.657f,
                    dy2 = -0.458f,
                    dx3 = 2.713f,
                    dy3 = -0.48f,
                )
                close()
                // m -0.084 -6.458
                moveToRelative(dx = -0.084f, dy = -6.458f)
                // L 14.599 4
                lineTo(x = 14.599f, y = 4.0f)
                // L 17 11
                lineTo(x = 17.0f, y = 11.0f)
                // h -4.865
                horizontalLineToRelative(dx = -4.865f)
                // l 0.264 1
                lineToRelative(dx = 0.264f, dy = 1.0f)
                // H 0.892
                horizontalLineTo(x = 0.892f)
                // l 3.214 -5.67
                lineToRelative(dx = 3.214f, dy = -5.67f)
                // l 1.842 2.9
                lineToRelative(dx = 1.842f, dy = 2.9f)
                // L 9.48 0.967
                lineTo(x = 9.48f, y = 0.967f)
                // l 2.269 8.575z
                lineToRelative(dx = 2.269f, dy = 8.575f)
                close()
                // m 0.85 0.401
                moveToRelative(dx = 0.85f, dy = 0.401f)
                // h 3.063
                horizontalLineToRelative(dx = 3.063f)
                // L 14.437 6.37
                lineTo(x = 14.437f, y = 6.37f)
                // L 12.6 9.943z
                lineTo(x = 12.6f, y = 9.943f)
                close()
            }
        }.build().also { _ic1709 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1709: ImageVector? = null
