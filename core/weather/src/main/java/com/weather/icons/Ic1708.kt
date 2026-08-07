package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1708: ImageVector
    get() {
        val current = _ic1708
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1708",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
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
            // M7.406 2.056 1.196 4.66 c-.368 .153 -.337 .693 .046 .794 l1.192 .313 -1.778 6.77 c1.716 .398 2.73 .502 4.511 .464 1.056 -.022 1.861 -.244 2.713 -.48 l.012 -.003 c.432 -.12 .876 -.243 1.362 -.338 l1.135 -4.324 1.193 .314 c.382 .1 .675 -.355 .43 -.67 l-4.13 -5.317 a.404 .404 0 0 0 -.476 -.126Z M4.704 8.93 l2.386 .627 c.22 .058 .351 .283 .293 .503 l-.626 2.386 a.411 .411 0 0 1 -.503 .293 l-2.386 -.627 a.411 .411 0 0 1 -.293 -.502 L4.2 9.223 a.411 .411 0 0 1 .503 -.294Z m8.406 -7.509 c-.02 -.176 .16 -.327 .39 -.327 s.41 .151 .39 .327 l-.177 1.548 h-.426 L13.11 1.42Z m.705 2.173 a.312 .312 0 1 1 -.625 0 .312 .312 0 0 1 .625 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.406 2.056
                moveTo(x = 7.406f, y = 2.056f)
                // L 1.196 4.66
                lineTo(x = 1.196f, y = 4.66f)
                // c -0.368 0.153 -0.337 0.693 0.046 0.794
                curveToRelative(
                    dx1 = -0.368f,
                    dy1 = 0.153f,
                    dx2 = -0.337f,
                    dy2 = 0.693f,
                    dx3 = 0.046f,
                    dy3 = 0.794f,
                )
                // l 1.192 0.313
                lineToRelative(dx = 1.192f, dy = 0.313f)
                // l -1.778 6.77
                lineToRelative(dx = -1.778f, dy = 6.77f)
                // c 1.716 0.398 2.73 0.502 4.511 0.464
                curveToRelative(
                    dx1 = 1.716f,
                    dy1 = 0.398f,
                    dx2 = 2.73f,
                    dy2 = 0.502f,
                    dx3 = 4.511f,
                    dy3 = 0.464f,
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
                // c 0.432 -0.12 0.876 -0.243 1.362 -0.338
                curveToRelative(
                    dx1 = 0.432f,
                    dy1 = -0.12f,
                    dx2 = 0.876f,
                    dy2 = -0.243f,
                    dx3 = 1.362f,
                    dy3 = -0.338f,
                )
                // l 1.135 -4.324
                lineToRelative(dx = 1.135f, dy = -4.324f)
                // l 1.193 0.314
                lineToRelative(dx = 1.193f, dy = 0.314f)
                // c 0.382 0.1 0.675 -0.355 0.43 -0.67
                curveToRelative(
                    dx1 = 0.382f,
                    dy1 = 0.1f,
                    dx2 = 0.675f,
                    dy2 = -0.355f,
                    dx3 = 0.43f,
                    dy3 = -0.67f,
                )
                // l -4.13 -5.317
                lineToRelative(dx = -4.13f, dy = -5.317f)
                // a 0.404 0.404 0 0 0 -0.476 -0.126z
                arcToRelative(
                    a = 0.404f,
                    b = 0.404f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.476f,
                    dy1 = -0.126f,
                )
                close()
                // M 4.704 8.93
                moveTo(x = 4.704f, y = 8.93f)
                // l 2.386 0.627
                lineToRelative(dx = 2.386f, dy = 0.627f)
                // c 0.22 0.058 0.351 0.283 0.293 0.503
                curveToRelative(
                    dx1 = 0.22f,
                    dy1 = 0.058f,
                    dx2 = 0.351f,
                    dy2 = 0.283f,
                    dx3 = 0.293f,
                    dy3 = 0.503f,
                )
                // l -0.626 2.386
                lineToRelative(dx = -0.626f, dy = 2.386f)
                // a 0.411 0.411 0 0 1 -0.503 0.293
                arcToRelative(
                    a = 0.411f,
                    b = 0.411f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.503f,
                    dy1 = 0.293f,
                )
                // l -2.386 -0.627
                lineToRelative(dx = -2.386f, dy = -0.627f)
                // a 0.411 0.411 0 0 1 -0.293 -0.502
                arcToRelative(
                    a = 0.411f,
                    b = 0.411f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.293f,
                    dy1 = -0.502f,
                )
                // L 4.2 9.223
                lineTo(x = 4.2f, y = 9.223f)
                // a 0.411 0.411 0 0 1 0.503 -0.294z
                arcToRelative(
                    a = 0.411f,
                    b = 0.411f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.503f,
                    dy1 = -0.294f,
                )
                close()
                // m 8.406 -7.509
                moveToRelative(dx = 8.406f, dy = -7.509f)
                // c -0.02 -0.176 0.16 -0.327 0.39 -0.327
                curveToRelative(
                    dx1 = -0.02f,
                    dy1 = -0.176f,
                    dx2 = 0.16f,
                    dy2 = -0.327f,
                    dx3 = 0.39f,
                    dy3 = -0.327f,
                )
                // s 0.41 0.151 0.39 0.327
                reflectiveCurveToRelative(
                    dx1 = 0.41f,
                    dy1 = 0.151f,
                    dx2 = 0.39f,
                    dy2 = 0.327f,
                )
                // l -0.177 1.548
                lineToRelative(dx = -0.177f, dy = 1.548f)
                // h -0.426
                horizontalLineToRelative(dx = -0.426f)
                // L 13.11 1.42z
                lineTo(x = 13.11f, y = 1.42f)
                close()
                // m 0.705 2.173
                moveToRelative(dx = 0.705f, dy = 2.173f)
                // a 0.312 0.312 0 1 1 -0.625 0
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.625f,
                    dy1 = 0.0f,
                )
                // a 0.312 0.312 0 0 1 0.625 0z
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.625f,
                    dy1 = 0.0f,
                )
                close()
            }
            // M11 2.5 a2.5 2.5 0 1 0 5 0 2.5 2.5 0 0 0 -5 0Z m4.594 0 a2.094 2.094 0 1 1 -4.188 0 2.094 2.094 0 0 1 4.188 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11 2.5
                moveTo(x = 11.0f, y = 2.5f)
                // a 2.5 2.5 0 1 0 5 0
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 5.0f,
                    dy1 = 0.0f,
                )
                // a 2.5 2.5 0 0 0 -5 0z
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -5.0f,
                    dy1 = 0.0f,
                )
                close()
                // m 4.594 0
                moveToRelative(dx = 4.594f, dy = 0.0f)
                // a 2.094 2.094 0 1 1 -4.188 0
                arcToRelative(
                    a = 2.094f,
                    b = 2.094f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -4.188f,
                    dy1 = 0.0f,
                )
                // a 2.094 2.094 0 0 1 4.188 0z
                arcToRelative(
                    a = 2.094f,
                    b = 2.094f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 4.188f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic1708 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1708: ImageVector? = null
