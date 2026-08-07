package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1209: ImageVector
    get() {
        val current = _ic1209
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1209",
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
            // m7.406 2.056 -6.21 2.602 c-.368 .154 -.337 .694 .046 .795 l1.192 .313 -1.778 6.769 c1.716 .4 2.73 .503 4.511 .465 1.056 -.022 1.861 -.244 2.713 -.48 l.012 -.003 c.432 -.12 .876 -.243 1.362 -.338 l1.135 -4.324 1.193 .314 c.382 .1 .675 -.355 .43 -.67 l-4.13 -5.317 a.404 .404 0 0 0 -.476 -.126Z M4.704 8.93 l2.386 .627 a.41 .41 0 0 1 .293 .502 l-.626 2.387 a.411 .411 0 0 1 -.503 .293 l-2.386 -.627 a.411 .411 0 0 1 -.293 -.502 L4.2 9.223 a.411 .411 0 0 1 .503 -.294Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.406 2.056
                moveTo(x = 7.406f, y = 2.056f)
                // l -6.21 2.602
                lineToRelative(dx = -6.21f, dy = 2.602f)
                // c -0.368 0.154 -0.337 0.694 0.046 0.795
                curveToRelative(
                    dx1 = -0.368f,
                    dy1 = 0.154f,
                    dx2 = -0.337f,
                    dy2 = 0.694f,
                    dx3 = 0.046f,
                    dy3 = 0.795f,
                )
                // l 1.192 0.313
                lineToRelative(dx = 1.192f, dy = 0.313f)
                // l -1.778 6.769
                lineToRelative(dx = -1.778f, dy = 6.769f)
                // c 1.716 0.4 2.73 0.503 4.511 0.465
                curveToRelative(
                    dx1 = 1.716f,
                    dy1 = 0.4f,
                    dx2 = 2.73f,
                    dy2 = 0.503f,
                    dx3 = 4.511f,
                    dy3 = 0.465f,
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
                // a 0.41 0.41 0 0 1 0.293 0.502
                arcToRelative(
                    a = 0.41f,
                    b = 0.41f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.293f,
                    dy1 = 0.502f,
                )
                // l -0.626 2.387
                lineToRelative(dx = -0.626f, dy = 2.387f)
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
            }
            // M14.746 6.125 a.17 .17 0 0 0 -.283 -.076 l-3.414 3.416 a.167 .167 0 0 0 .077 .28 l1.8 .482 -.482 1.796 c.347 .017 .678 .045 1.013 .086 l.434 -1.623 1.897 .508 c.125 .034 .24 -.08 .206 -.204 l-1.248 -4.665Z m-.888 1.195 c.027 -.181 .247 -.284 .476 -.223 .229 .061 .368 .26 .3 .43 l-.589 1.497 -.425 -.114 .238 -1.59Z m.122 2.355 a.323 .323 0 1 1 -.624 -.168 .323 .323 0 0 1 .624 .168Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 14.746 6.125
                moveTo(x = 14.746f, y = 6.125f)
                // a 0.17 0.17 0 0 0 -0.283 -0.076
                arcToRelative(
                    a = 0.17f,
                    b = 0.17f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.283f,
                    dy1 = -0.076f,
                )
                // l -3.414 3.416
                lineToRelative(dx = -3.414f, dy = 3.416f)
                // a 0.167 0.167 0 0 0 0.077 0.28
                arcToRelative(
                    a = 0.167f,
                    b = 0.167f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.077f,
                    dy1 = 0.28f,
                )
                // l 1.8 0.482
                lineToRelative(dx = 1.8f, dy = 0.482f)
                // l -0.482 1.796
                lineToRelative(dx = -0.482f, dy = 1.796f)
                // c 0.347 0.017 0.678 0.045 1.013 0.086
                curveToRelative(
                    dx1 = 0.347f,
                    dy1 = 0.017f,
                    dx2 = 0.678f,
                    dy2 = 0.045f,
                    dx3 = 1.013f,
                    dy3 = 0.086f,
                )
                // l 0.434 -1.623
                lineToRelative(dx = 0.434f, dy = -1.623f)
                // l 1.897 0.508
                lineToRelative(dx = 1.897f, dy = 0.508f)
                // c 0.125 0.034 0.24 -0.08 0.206 -0.204
                curveToRelative(
                    dx1 = 0.125f,
                    dy1 = 0.034f,
                    dx2 = 0.24f,
                    dy2 = -0.08f,
                    dx3 = 0.206f,
                    dy3 = -0.204f,
                )
                // l -1.248 -4.665z
                lineToRelative(dx = -1.248f, dy = -4.665f)
                close()
                // m -0.888 1.195
                moveToRelative(dx = -0.888f, dy = 1.195f)
                // c 0.027 -0.181 0.247 -0.284 0.476 -0.223
                curveToRelative(
                    dx1 = 0.027f,
                    dy1 = -0.181f,
                    dx2 = 0.247f,
                    dy2 = -0.284f,
                    dx3 = 0.476f,
                    dy3 = -0.223f,
                )
                // c 0.229 0.061 0.368 0.26 0.3 0.43
                curveToRelative(
                    dx1 = 0.229f,
                    dy1 = 0.061f,
                    dx2 = 0.368f,
                    dy2 = 0.26f,
                    dx3 = 0.3f,
                    dy3 = 0.43f,
                )
                // l -0.589 1.497
                lineToRelative(dx = -0.589f, dy = 1.497f)
                // l -0.425 -0.114
                lineToRelative(dx = -0.425f, dy = -0.114f)
                // l 0.238 -1.59z
                lineToRelative(dx = 0.238f, dy = -1.59f)
                close()
                // m 0.122 2.355
                moveToRelative(dx = 0.122f, dy = 2.355f)
                // a 0.323 0.323 0 1 1 -0.624 -0.168
                arcToRelative(
                    a = 0.323f,
                    b = 0.323f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.624f,
                    dy1 = -0.168f,
                )
                // a 0.323 0.323 0 0 1 0.624 0.168z
                arcToRelative(
                    a = 0.323f,
                    b = 0.323f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.624f,
                    dy1 = 0.168f,
                )
                close()
            }
        }.build().also { _ic1209 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1209: ImageVector? = null
