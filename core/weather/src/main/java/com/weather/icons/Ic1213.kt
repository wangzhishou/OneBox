package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1213: ImageVector
    get() {
        val current = _ic1213
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1213",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M6.5 0 h5 v12 c.354 0 .683 .009 1 .026 V3.732 l2 -1.232 v-2 h1 v11.985 l.132 .03 c.253 .06 .413 .325 .357 .593 a.47 .47 0 0 1 -.562 .377 c-1.766 -.416 -2.785 -.523 -4.594 -.485 -1.056 .022 -1.861 .244 -2.713 .48 l-.012 .003 c-.85 .236 -1.748 .485 -2.922 .51 -1.884 .04 -2.978 -.076 -4.818 -.508 a.497 .497 0 0 1 -.357 -.593 .47 .47 0 0 1 .562 -.377 c.735 .173 1.34 .293 1.927 .37 v-3.26 H.197 a.195 .195 0 0 1 -.17 -.291 L2.83 4.473 a.197 .197 0 0 1 .34 0 l2.804 4.86 a.195 .195 0 0 1 -.17 .292 H3.5 v3.355 c.503 .03 1.037 .033 1.667 .02 a8.01 8.01 0 0 0 1.333 -.14 V0Z m1 5 v7 h1 V5 h-1Z m3 -4 h-3 v1 h3 V1Z m-3 2 v1 h3 V3 h-3Z m3 2 h-1 v7 h1 V5Z m4 0 h-1 v1 h1 V5Z m-1 2 v1 h1 V7 h-1Z m1 2 h-1 v2 h1 V9Z M2.533 6.08 l.211 1.857 h.512 l.21 -1.856 c.025 -.212 -.191 -.394 -.466 -.394 s-.491 .182 -.467 .394Z m.845 2.607 a.375 .375 0 1 0 -.75 0 .375 .375 0 0 0 .75 0Z m12.049 6.798 c-1.766 -.416 -2.785 -.523 -4.594 -.485 -1.056 .022 -1.861 .244 -2.713 .48 l-.012 .003 c-.85 .236 -1.748 .485 -2.922 .51 -1.884 .04 -2.978 -.076 -4.818 -.508 a.497 .497 0 0 1 -.357 -.593 .47 .47 0 0 1 .562 -.377 c1.766 .416 2.785 .523 4.594 .485 1.056 -.022 1.861 -.244 2.713 -.48 l.012 -.003 c.85 -.236 1.748 -.485 2.922 -.51 1.884 -.04 2.978 .076 4.818 .508 .253 .06 .413 .325 .357 .593 a.47 .47 0 0 1 -.562 .377Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6.5 0
                moveTo(x = 6.5f, y = 0.0f)
                // h 5
                horizontalLineToRelative(dx = 5.0f)
                // v 12
                verticalLineToRelative(dy = 12.0f)
                // c 0.354 0 0.683 0.009 1 0.026
                curveToRelative(
                    dx1 = 0.354f,
                    dy1 = 0.0f,
                    dx2 = 0.683f,
                    dy2 = 0.009f,
                    dx3 = 1.0f,
                    dy3 = 0.026f,
                )
                // V 3.732
                verticalLineTo(y = 3.732f)
                // l 2 -1.232
                lineToRelative(dx = 2.0f, dy = -1.232f)
                // v -2
                verticalLineToRelative(dy = -2.0f)
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // v 11.985
                verticalLineToRelative(dy = 11.985f)
                // l 0.132 0.03
                lineToRelative(dx = 0.132f, dy = 0.03f)
                // c 0.253 0.06 0.413 0.325 0.357 0.593
                curveToRelative(
                    dx1 = 0.253f,
                    dy1 = 0.06f,
                    dx2 = 0.413f,
                    dy2 = 0.325f,
                    dx3 = 0.357f,
                    dy3 = 0.593f,
                )
                // a 0.47 0.47 0 0 1 -0.562 0.377
                arcToRelative(
                    a = 0.47f,
                    b = 0.47f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.562f,
                    dy1 = 0.377f,
                )
                // c -1.766 -0.416 -2.785 -0.523 -4.594 -0.485
                curveToRelative(
                    dx1 = -1.766f,
                    dy1 = -0.416f,
                    dx2 = -2.785f,
                    dy2 = -0.523f,
                    dx3 = -4.594f,
                    dy3 = -0.485f,
                )
                // c -1.056 0.022 -1.861 0.244 -2.713 0.48
                curveToRelative(
                    dx1 = -1.056f,
                    dy1 = 0.022f,
                    dx2 = -1.861f,
                    dy2 = 0.244f,
                    dx3 = -2.713f,
                    dy3 = 0.48f,
                )
                // l -0.012 0.003
                lineToRelative(dx = -0.012f, dy = 0.003f)
                // c -0.85 0.236 -1.748 0.485 -2.922 0.51
                curveToRelative(
                    dx1 = -0.85f,
                    dy1 = 0.236f,
                    dx2 = -1.748f,
                    dy2 = 0.485f,
                    dx3 = -2.922f,
                    dy3 = 0.51f,
                )
                // c -1.884 0.04 -2.978 -0.076 -4.818 -0.508
                curveToRelative(
                    dx1 = -1.884f,
                    dy1 = 0.04f,
                    dx2 = -2.978f,
                    dy2 = -0.076f,
                    dx3 = -4.818f,
                    dy3 = -0.508f,
                )
                // a 0.497 0.497 0 0 1 -0.357 -0.593
                arcToRelative(
                    a = 0.497f,
                    b = 0.497f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.357f,
                    dy1 = -0.593f,
                )
                // a 0.47 0.47 0 0 1 0.562 -0.377
                arcToRelative(
                    a = 0.47f,
                    b = 0.47f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.562f,
                    dy1 = -0.377f,
                )
                // c 0.735 0.173 1.34 0.293 1.927 0.37
                curveToRelative(
                    dx1 = 0.735f,
                    dy1 = 0.173f,
                    dx2 = 1.34f,
                    dy2 = 0.293f,
                    dx3 = 1.927f,
                    dy3 = 0.37f,
                )
                // v -3.26
                verticalLineToRelative(dy = -3.26f)
                // H 0.197
                horizontalLineTo(x = 0.197f)
                // a 0.195 0.195 0 0 1 -0.17 -0.291
                arcToRelative(
                    a = 0.195f,
                    b = 0.195f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.17f,
                    dy1 = -0.291f,
                )
                // L 2.83 4.473
                lineTo(x = 2.83f, y = 4.473f)
                // a 0.197 0.197 0 0 1 0.34 0
                arcToRelative(
                    a = 0.197f,
                    b = 0.197f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.34f,
                    dy1 = 0.0f,
                )
                // l 2.804 4.86
                lineToRelative(dx = 2.804f, dy = 4.86f)
                // a 0.195 0.195 0 0 1 -0.17 0.292
                arcToRelative(
                    a = 0.195f,
                    b = 0.195f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.17f,
                    dy1 = 0.292f,
                )
                // H 3.5
                horizontalLineTo(x = 3.5f)
                // v 3.355
                verticalLineToRelative(dy = 3.355f)
                // c 0.503 0.03 1.037 0.033 1.667 0.02
                curveToRelative(
                    dx1 = 0.503f,
                    dy1 = 0.03f,
                    dx2 = 1.037f,
                    dy2 = 0.033f,
                    dx3 = 1.667f,
                    dy3 = 0.02f,
                )
                // a 8.01 8.01 0 0 0 1.333 -0.14
                arcToRelative(
                    a = 8.01f,
                    b = 8.01f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.333f,
                    dy1 = -0.14f,
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
                // h -1z
                horizontalLineToRelative(dx = -1.0f)
                close()
                // m 3 -4
                moveToRelative(dx = 3.0f, dy = -4.0f)
                // h -3
                horizontalLineToRelative(dx = -3.0f)
                // v 1
                verticalLineToRelative(dy = 1.0f)
                // h 3
                horizontalLineToRelative(dx = 3.0f)
                // V 1z
                verticalLineTo(y = 1.0f)
                close()
                // m -3 2
                moveToRelative(dx = -3.0f, dy = 2.0f)
                // v 1
                verticalLineToRelative(dy = 1.0f)
                // h 3
                horizontalLineToRelative(dx = 3.0f)
                // V 3
                verticalLineTo(y = 3.0f)
                // h -3z
                horizontalLineToRelative(dx = -3.0f)
                close()
                // m 3 2
                moveToRelative(dx = 3.0f, dy = 2.0f)
                // h -1
                horizontalLineToRelative(dx = -1.0f)
                // v 7
                verticalLineToRelative(dy = 7.0f)
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // V 5z
                verticalLineTo(y = 5.0f)
                close()
                // m 4 0
                moveToRelative(dx = 4.0f, dy = 0.0f)
                // h -1
                horizontalLineToRelative(dx = -1.0f)
                // v 1
                verticalLineToRelative(dy = 1.0f)
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // V 5z
                verticalLineTo(y = 5.0f)
                close()
                // m -1 2
                moveToRelative(dx = -1.0f, dy = 2.0f)
                // v 1
                verticalLineToRelative(dy = 1.0f)
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // V 7
                verticalLineTo(y = 7.0f)
                // h -1z
                horizontalLineToRelative(dx = -1.0f)
                close()
                // m 1 2
                moveToRelative(dx = 1.0f, dy = 2.0f)
                // h -1
                horizontalLineToRelative(dx = -1.0f)
                // v 2
                verticalLineToRelative(dy = 2.0f)
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // V 9z
                verticalLineTo(y = 9.0f)
                close()
                // M 2.533 6.08
                moveTo(x = 2.533f, y = 6.08f)
                // l 0.211 1.857
                lineToRelative(dx = 0.211f, dy = 1.857f)
                // h 0.512
                horizontalLineToRelative(dx = 0.512f)
                // l 0.21 -1.856
                lineToRelative(dx = 0.21f, dy = -1.856f)
                // c 0.025 -0.212 -0.191 -0.394 -0.466 -0.394
                curveToRelative(
                    dx1 = 0.025f,
                    dy1 = -0.212f,
                    dx2 = -0.191f,
                    dy2 = -0.394f,
                    dx3 = -0.466f,
                    dy3 = -0.394f,
                )
                // s -0.491 0.182 -0.467 0.394z
                reflectiveCurveToRelative(
                    dx1 = -0.491f,
                    dy1 = 0.182f,
                    dx2 = -0.467f,
                    dy2 = 0.394f,
                )
                close()
                // m 0.845 2.607
                moveToRelative(dx = 0.845f, dy = 2.607f)
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
                // a 0.375 0.375 0 0 0 0.75 0z
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.75f,
                    dy1 = 0.0f,
                )
                close()
                // m 12.049 6.798
                moveToRelative(dx = 12.049f, dy = 6.798f)
                // c -1.766 -0.416 -2.785 -0.523 -4.594 -0.485
                curveToRelative(
                    dx1 = -1.766f,
                    dy1 = -0.416f,
                    dx2 = -2.785f,
                    dy2 = -0.523f,
                    dx3 = -4.594f,
                    dy3 = -0.485f,
                )
                // c -1.056 0.022 -1.861 0.244 -2.713 0.48
                curveToRelative(
                    dx1 = -1.056f,
                    dy1 = 0.022f,
                    dx2 = -1.861f,
                    dy2 = 0.244f,
                    dx3 = -2.713f,
                    dy3 = 0.48f,
                )
                // l -0.012 0.003
                lineToRelative(dx = -0.012f, dy = 0.003f)
                // c -0.85 0.236 -1.748 0.485 -2.922 0.51
                curveToRelative(
                    dx1 = -0.85f,
                    dy1 = 0.236f,
                    dx2 = -1.748f,
                    dy2 = 0.485f,
                    dx3 = -2.922f,
                    dy3 = 0.51f,
                )
                // c -1.884 0.04 -2.978 -0.076 -4.818 -0.508
                curveToRelative(
                    dx1 = -1.884f,
                    dy1 = 0.04f,
                    dx2 = -2.978f,
                    dy2 = -0.076f,
                    dx3 = -4.818f,
                    dy3 = -0.508f,
                )
                // a 0.497 0.497 0 0 1 -0.357 -0.593
                arcToRelative(
                    a = 0.497f,
                    b = 0.497f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.357f,
                    dy1 = -0.593f,
                )
                // a 0.47 0.47 0 0 1 0.562 -0.377
                arcToRelative(
                    a = 0.47f,
                    b = 0.47f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.562f,
                    dy1 = -0.377f,
                )
                // c 1.766 0.416 2.785 0.523 4.594 0.485
                curveToRelative(
                    dx1 = 1.766f,
                    dy1 = 0.416f,
                    dx2 = 2.785f,
                    dy2 = 0.523f,
                    dx3 = 4.594f,
                    dy3 = 0.485f,
                )
                // c 1.056 -0.022 1.861 -0.244 2.713 -0.48
                curveToRelative(
                    dx1 = 1.056f,
                    dy1 = -0.022f,
                    dx2 = 1.861f,
                    dy2 = -0.244f,
                    dx3 = 2.713f,
                    dy3 = -0.48f,
                )
                // l 0.012 -0.003
                lineToRelative(dx = 0.012f, dy = -0.003f)
                // c 0.85 -0.236 1.748 -0.485 2.922 -0.51
                curveToRelative(
                    dx1 = 0.85f,
                    dy1 = -0.236f,
                    dx2 = 1.748f,
                    dy2 = -0.485f,
                    dx3 = 2.922f,
                    dy3 = -0.51f,
                )
                // c 1.884 -0.04 2.978 0.076 4.818 0.508
                curveToRelative(
                    dx1 = 1.884f,
                    dy1 = -0.04f,
                    dx2 = 2.978f,
                    dy2 = 0.076f,
                    dx3 = 4.818f,
                    dy3 = 0.508f,
                )
                // c 0.253 0.06 0.413 0.325 0.357 0.593
                curveToRelative(
                    dx1 = 0.253f,
                    dy1 = 0.06f,
                    dx2 = 0.413f,
                    dy2 = 0.325f,
                    dx3 = 0.357f,
                    dy3 = 0.593f,
                )
                // a 0.47 0.47 0 0 1 -0.562 0.377z
                arcToRelative(
                    a = 0.47f,
                    b = 0.47f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.562f,
                    dy1 = 0.377f,
                )
                close()
            }
        }.build().also { _ic1213 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1213: ImageVector? = null
