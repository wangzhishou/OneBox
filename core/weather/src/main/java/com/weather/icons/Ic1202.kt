package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1202: ImageVector
    get() {
        val current = _ic1202
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1202",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M6 0 h5 v12.004 l-.186 .003 c-1.174 .025 -2.072 .274 -2.922 .51 l-.012 .003 c-.614 .17 -1.203 .333 -1.88 .419 V0Z m1 5 v7 h1 V5 H7Z m3 -4 H7 v1 h3 V1Z M7 3 v1 h3 V3 H7Z m3 2 H9 v7 h1 V5Z m-5 8.003 V5 a.5 .5 0 0 0 -.5 -.5 h-2 A.5 .5 0 0 0 2 5 v7.81 c.975 .165 1.821 .213 3 .193Z M3.5 8.5 a.5 .5 0 1 1 0 -1 .5 .5 0 0 1 0 1Z M4 10 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z m-.5 -3.5 a.5 .5 0 1 1 0 -1 .5 .5 0 0 1 0 1Z M15 12.373 V.5 h-1 v2 l-2 1.232 v8.274 c1.045 .026 1.903 .133 3 .367Z M14 5 v1 h-1 V5 h1Z m-1 2 h1 v1 h-1 V7Z m1 2 v2 h-1 V9 h1Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6 0
                moveTo(x = 6.0f, y = 0.0f)
                // h 5
                horizontalLineToRelative(dx = 5.0f)
                // v 12.004
                verticalLineToRelative(dy = 12.004f)
                // l -0.186 0.003
                lineToRelative(dx = -0.186f, dy = 0.003f)
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
                // c -0.614 0.17 -1.203 0.333 -1.88 0.419
                curveToRelative(
                    dx1 = -0.614f,
                    dy1 = 0.17f,
                    dx2 = -1.203f,
                    dy2 = 0.333f,
                    dx3 = -1.88f,
                    dy3 = 0.419f,
                )
                // V 0z
                verticalLineTo(y = 0.0f)
                close()
                // m 1 5
                moveToRelative(dx = 1.0f, dy = 5.0f)
                // v 7
                verticalLineToRelative(dy = 7.0f)
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // V 5
                verticalLineTo(y = 5.0f)
                // H 7z
                horizontalLineTo(x = 7.0f)
                close()
                // m 3 -4
                moveToRelative(dx = 3.0f, dy = -4.0f)
                // H 7
                horizontalLineTo(x = 7.0f)
                // v 1
                verticalLineToRelative(dy = 1.0f)
                // h 3
                horizontalLineToRelative(dx = 3.0f)
                // V 1z
                verticalLineTo(y = 1.0f)
                close()
                // M 7 3
                moveTo(x = 7.0f, y = 3.0f)
                // v 1
                verticalLineToRelative(dy = 1.0f)
                // h 3
                horizontalLineToRelative(dx = 3.0f)
                // V 3
                verticalLineTo(y = 3.0f)
                // H 7z
                horizontalLineTo(x = 7.0f)
                close()
                // m 3 2
                moveToRelative(dx = 3.0f, dy = 2.0f)
                // H 9
                horizontalLineTo(x = 9.0f)
                // v 7
                verticalLineToRelative(dy = 7.0f)
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // V 5z
                verticalLineTo(y = 5.0f)
                close()
                // m -5 8.003
                moveToRelative(dx = -5.0f, dy = 8.003f)
                // V 5
                verticalLineTo(y = 5.0f)
                // a 0.5 0.5 0 0 0 -0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                // h -2
                horizontalLineToRelative(dx = -2.0f)
                // A 0.5 0.5 0 0 0 2 5
                arcTo(
                    horizontalEllipseRadius = 0.5f,
                    verticalEllipseRadius = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 2.0f,
                    y1 = 5.0f,
                )
                // v 7.81
                verticalLineToRelative(dy = 7.81f)
                // c 0.975 0.165 1.821 0.213 3 0.193z
                curveToRelative(
                    dx1 = 0.975f,
                    dy1 = 0.165f,
                    dx2 = 1.821f,
                    dy2 = 0.213f,
                    dx3 = 3.0f,
                    dy3 = 0.193f,
                )
                close()
                // M 3.5 8.5
                moveTo(x = 3.5f, y = 8.5f)
                // a 0.5 0.5 0 1 1 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // a 0.5 0.5 0 0 1 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
                // M 4 10
                moveTo(x = 4.0f, y = 10.0f)
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
                // m -0.5 -3.5
                moveToRelative(dx = -0.5f, dy = -3.5f)
                // a 0.5 0.5 0 1 1 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // a 0.5 0.5 0 0 1 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
                // M 15 12.373
                moveTo(x = 15.0f, y = 12.373f)
                // V 0.5
                verticalLineTo(y = 0.5f)
                // h -1
                horizontalLineToRelative(dx = -1.0f)
                // v 2
                verticalLineToRelative(dy = 2.0f)
                // l -2 1.232
                lineToRelative(dx = -2.0f, dy = 1.232f)
                // v 8.274
                verticalLineToRelative(dy = 8.274f)
                // c 1.045 0.026 1.903 0.133 3 0.367z
                curveToRelative(
                    dx1 = 1.045f,
                    dy1 = 0.026f,
                    dx2 = 1.903f,
                    dy2 = 0.133f,
                    dx3 = 3.0f,
                    dy3 = 0.367f,
                )
                close()
                // M 14 5
                moveTo(x = 14.0f, y = 5.0f)
                // v 1
                verticalLineToRelative(dy = 1.0f)
                // h -1
                horizontalLineToRelative(dx = -1.0f)
                // V 5
                verticalLineTo(y = 5.0f)
                // h 1z
                horizontalLineToRelative(dx = 1.0f)
                close()
                // m -1 2
                moveToRelative(dx = -1.0f, dy = 2.0f)
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // v 1
                verticalLineToRelative(dy = 1.0f)
                // h -1
                horizontalLineToRelative(dx = -1.0f)
                // V 7z
                verticalLineTo(y = 7.0f)
                close()
                // m 1 2
                moveToRelative(dx = 1.0f, dy = 2.0f)
                // v 2
                verticalLineToRelative(dy = 2.0f)
                // h -1
                horizontalLineToRelative(dx = -1.0f)
                // V 9
                verticalLineTo(y = 9.0f)
                // h 1z
                horizontalLineToRelative(dx = 1.0f)
                close()
            }
            // M10.833 13 c1.809 -.038 2.828 .07 4.594 .485 a.47 .47 0 0 0 .562 -.377 .497 .497 0 0 0 -.357 -.593 c-1.84 -.432 -2.934 -.548 -4.818 -.508 -1.174 .025 -2.072 .274 -2.922 .51 l-.012 .003 c-.852 .236 -1.657 .458 -2.713 .48 -1.809 .038 -2.828 -.07 -4.594 -.485 a.47 .47 0 0 0 -.562 .377 .497 .497 0 0 0 .357 .593 c1.84 .432 2.934 .548 4.818 .508 1.174 -.025 2.072 -.274 2.922 -.51 l.012 -.003 c.852 -.236 1.657 -.458 2.713 -.48Z m0 2 c1.809 -.038 2.828 .07 4.594 .485 a.47 .47 0 0 0 .562 -.377 .497 .497 0 0 0 -.357 -.593 c-1.84 -.432 -2.934 -.548 -4.818 -.508 -1.174 .025 -2.072 .274 -2.922 .51 l-.012 .003 c-.852 .236 -1.657 .458 -2.713 .48 -1.809 .038 -2.828 -.07 -4.594 -.485 a.47 .47 0 0 0 -.562 .377 .497 .497 0 0 0 .357 .593 c1.84 .432 2.934 .547 4.818 .508 1.174 -.025 2.072 -.274 2.922 -.51 l.012 -.003 c.852 -.236 1.657 -.458 2.713 -.48Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.833 13
                moveTo(x = 10.833f, y = 13.0f)
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
            }
        }.build().also { _ic1202 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1202: ImageVector? = null
