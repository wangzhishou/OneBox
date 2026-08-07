package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1606: ImageVector
    get() {
        val current = _ic1606
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1606",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M10.833 11 c1.809 -.038 2.828 .07 4.594 .485 a.47 .47 0 0 0 .562 -.377 .497 .497 0 0 0 -.357 -.593 c-1.84 -.432 -2.934 -.548 -4.818 -.508 -1.174 .025 -2.072 .274 -2.922 .51 l-.012 .003 c-.852 .236 -1.657 .458 -2.713 .48 -1.809 .038 -2.828 -.07 -4.594 -.485 a.47 .47 0 0 0 -.562 .377 .497 .497 0 0 0 .357 .593 c1.84 .432 2.934 .548 4.818 .508 1.174 -.025 2.072 -.274 2.922 -.51 l.012 -.003 c.852 -.236 1.657 -.458 2.713 -.48Z m4.594 2.485 c-1.766 -.416 -2.785 -.523 -4.594 -.485 -1.056 .022 -1.861 .244 -2.713 .48 l-.012 .003 c-.85 .236 -1.748 .485 -2.922 .51 -1.884 .04 -2.978 -.076 -4.818 -.508 a.497 .497 0 0 1 -.357 -.593 .47 .47 0 0 1 .562 -.377 c1.766 .416 2.785 .523 4.594 .485 1.056 -.022 1.861 -.244 2.713 -.48 l.012 -.003 c.85 -.236 1.748 -.485 2.922 -.51 1.884 -.04 2.978 .076 4.818 .508 .253 .06 .413 .325 .357 .593 a.47 .47 0 0 1 -.562 .377Z m0 2 c-1.766 -.416 -2.785 -.523 -4.594 -.485 -1.056 .022 -1.861 .244 -2.713 .48 l-.012 .003 c-.85 .236 -1.748 .485 -2.922 .51 -1.884 .04 -2.978 -.076 -4.818 -.508 a.497 .497 0 0 1 -.357 -.593 .47 .47 0 0 1 .562 -.377 c1.766 .416 2.785 .523 4.594 .485 1.056 -.022 1.861 -.244 2.713 -.48 l.012 -.003 c.85 -.236 1.748 -.485 2.922 -.51 1.884 -.04 2.978 .076 4.818 .508 .253 .06 .413 .325 .357 .593 a.47 .47 0 0 1 -.562 .377Z M2.417 3.984 l1.236 .323 -.513 1.866 c-.82 .141 -1.549 .221 -2.113 .228 -.283 .004 -.53 .222 -.527 .498 l.006 .753 c.001 .217 .202 .377 .424 .352 a19.243 19.243 0 0 0 1.797 -.326 l-.233 .848 c-.095 .347 -.168 .591 -.236 .796 a.479 .479 0 0 0 .332 .617 l.621 .162 a.505 .505 0 0 0 .614 -.381 c.044 -.197 .105 -.43 .2 -.774 L5.795 2.5 c.081 -.296 .142 -.509 .198 -.684 a.499 .499 0 0 0 -.34 -.639 l-.612 -.16 a.493 .493 0 0 0 -.603 .374 c-.04 .182 -.093 .398 -.178 .708 l-.22 .8 -1.236 -.324 a16.79 16.79 0 0 1 -.439 -.12 c-.276 -.08 -.58 .064 -.655 .335 l-.129 .469 c-.075 .274 .118 .55 .4 .617 .121 .028 .262 .063 .436 .109Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.833 11
                moveTo(x = 10.833f, y = 11.0f)
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
                // m 4.594 2.485
                moveToRelative(dx = 4.594f, dy = 2.485f)
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
                // m 0 2
                moveToRelative(dx = 0.0f, dy = 2.0f)
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
                // M 2.417 3.984
                moveTo(x = 2.417f, y = 3.984f)
                // l 1.236 0.323
                lineToRelative(dx = 1.236f, dy = 0.323f)
                // l -0.513 1.866
                lineToRelative(dx = -0.513f, dy = 1.866f)
                // c -0.82 0.141 -1.549 0.221 -2.113 0.228
                curveToRelative(
                    dx1 = -0.82f,
                    dy1 = 0.141f,
                    dx2 = -1.549f,
                    dy2 = 0.221f,
                    dx3 = -2.113f,
                    dy3 = 0.228f,
                )
                // c -0.283 0.004 -0.53 0.222 -0.527 0.498
                curveToRelative(
                    dx1 = -0.283f,
                    dy1 = 0.004f,
                    dx2 = -0.53f,
                    dy2 = 0.222f,
                    dx3 = -0.527f,
                    dy3 = 0.498f,
                )
                // l 0.006 0.753
                lineToRelative(dx = 0.006f, dy = 0.753f)
                // c 0.001 0.217 0.202 0.377 0.424 0.352
                curveToRelative(
                    dx1 = 0.001f,
                    dy1 = 0.217f,
                    dx2 = 0.202f,
                    dy2 = 0.377f,
                    dx3 = 0.424f,
                    dy3 = 0.352f,
                )
                // a 19.243 19.243 0 0 0 1.797 -0.326
                arcToRelative(
                    a = 19.243f,
                    b = 19.243f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.797f,
                    dy1 = -0.326f,
                )
                // l -0.233 0.848
                lineToRelative(dx = -0.233f, dy = 0.848f)
                // c -0.095 0.347 -0.168 0.591 -0.236 0.796
                curveToRelative(
                    dx1 = -0.095f,
                    dy1 = 0.347f,
                    dx2 = -0.168f,
                    dy2 = 0.591f,
                    dx3 = -0.236f,
                    dy3 = 0.796f,
                )
                // a 0.479 0.479 0 0 0 0.332 0.617
                arcToRelative(
                    a = 0.479f,
                    b = 0.479f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.332f,
                    dy1 = 0.617f,
                )
                // l 0.621 0.162
                lineToRelative(dx = 0.621f, dy = 0.162f)
                // a 0.505 0.505 0 0 0 0.614 -0.381
                arcToRelative(
                    a = 0.505f,
                    b = 0.505f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.614f,
                    dy1 = -0.381f,
                )
                // c 0.044 -0.197 0.105 -0.43 0.2 -0.774
                curveToRelative(
                    dx1 = 0.044f,
                    dy1 = -0.197f,
                    dx2 = 0.105f,
                    dy2 = -0.43f,
                    dx3 = 0.2f,
                    dy3 = -0.774f,
                )
                // L 5.795 2.5
                lineTo(x = 5.795f, y = 2.5f)
                // c 0.081 -0.296 0.142 -0.509 0.198 -0.684
                curveToRelative(
                    dx1 = 0.081f,
                    dy1 = -0.296f,
                    dx2 = 0.142f,
                    dy2 = -0.509f,
                    dx3 = 0.198f,
                    dy3 = -0.684f,
                )
                // a 0.499 0.499 0 0 0 -0.34 -0.639
                arcToRelative(
                    a = 0.499f,
                    b = 0.499f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.34f,
                    dy1 = -0.639f,
                )
                // l -0.612 -0.16
                lineToRelative(dx = -0.612f, dy = -0.16f)
                // a 0.493 0.493 0 0 0 -0.603 0.374
                arcToRelative(
                    a = 0.493f,
                    b = 0.493f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.603f,
                    dy1 = 0.374f,
                )
                // c -0.04 0.182 -0.093 0.398 -0.178 0.708
                curveToRelative(
                    dx1 = -0.04f,
                    dy1 = 0.182f,
                    dx2 = -0.093f,
                    dy2 = 0.398f,
                    dx3 = -0.178f,
                    dy3 = 0.708f,
                )
                // l -0.22 0.8
                lineToRelative(dx = -0.22f, dy = 0.8f)
                // l -1.236 -0.324
                lineToRelative(dx = -1.236f, dy = -0.324f)
                // a 16.79 16.79 0 0 1 -0.439 -0.12
                arcToRelative(
                    a = 16.79f,
                    b = 16.79f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.439f,
                    dy1 = -0.12f,
                )
                // c -0.276 -0.08 -0.58 0.064 -0.655 0.335
                curveToRelative(
                    dx1 = -0.276f,
                    dy1 = -0.08f,
                    dx2 = -0.58f,
                    dy2 = 0.064f,
                    dx3 = -0.655f,
                    dy3 = 0.335f,
                )
                // l -0.129 0.469
                lineToRelative(dx = -0.129f, dy = 0.469f)
                // c -0.075 0.274 0.118 0.55 0.4 0.617
                curveToRelative(
                    dx1 = -0.075f,
                    dy1 = 0.274f,
                    dx2 = 0.118f,
                    dy2 = 0.55f,
                    dx3 = 0.4f,
                    dy3 = 0.617f,
                )
                // c 0.121 0.028 0.262 0.063 0.436 0.109z
                curveToRelative(
                    dx1 = 0.121f,
                    dy1 = 0.028f,
                    dx2 = 0.262f,
                    dy2 = 0.063f,
                    dx3 = 0.436f,
                    dy3 = 0.109f,
                )
                close()
            }
            // m6.526 9.07 .772 -2.81 c1.036 -.23 2.288 -.606 2.993 -.912 a.329 .329 0 0 0 .179 -.44 l-.322 -.686 c-.124 -.262 -.464 -.356 -.728 -.226 a10.44 10.44 0 0 1 -1.672 .626 l.413 -1.504 c.083 -.303 .143 -.513 .202 -.692 a.49 .49 0 0 0 -.335 -.63 l-.582 -.151 a.493 .493 0 0 0 -.603 .373 c-.04 .183 -.094 .398 -.179 .708 l-1.75 6.371 c-.154 .562 -.116 .878 .165 1.135 .19 .173 .528 .302 1.416 .534 l.208 .053 c.402 -.085 .785 -.19 1.177 -.3 l.012 -.002 c.343 -.095 .694 -.193 1.068 -.276 .191 -.33 .38 -.788 .59 -1.425 a.485 .485 0 0 0 -.22 -.557 5.264 5.264 0 0 1 -.76 -.58 c-.069 -.063 -.183 -.034 -.207 .054 l-.1 .362 a8.228 8.228 0 0 1 -.403 1.13 c-.068 .135 -.118 .203 -.179 .238 -.154 .072 -.317 .06 -.751 -.053 -.377 -.099 -.45 -.17 -.404 -.34Z m8.22 -4.945 a.17 .17 0 0 0 -.283 -.076 l-3.414 3.416 a.167 .167 0 0 0 .077 .28 l1.8 .482 -.482 1.796 c.347 .017 .678 .045 1.013 .086 l.434 -1.623 1.897 .508 c.125 .034 .24 -.08 .206 -.204 l-1.248 -4.665Z m-.888 1.195 c.027 -.181 .247 -.284 .476 -.223 .229 .061 .368 .26 .3 .43 l-.589 1.497 -.425 -.114 .238 -1.59Z m.122 2.355 a.323 .323 0 1 1 -.624 -.168 .323 .323 0 0 1 .624 .168Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6.526 9.07
                moveTo(x = 6.526f, y = 9.07f)
                // l 0.772 -2.81
                lineToRelative(dx = 0.772f, dy = -2.81f)
                // c 1.036 -0.23 2.288 -0.606 2.993 -0.912
                curveToRelative(
                    dx1 = 1.036f,
                    dy1 = -0.23f,
                    dx2 = 2.288f,
                    dy2 = -0.606f,
                    dx3 = 2.993f,
                    dy3 = -0.912f,
                )
                // a 0.329 0.329 0 0 0 0.179 -0.44
                arcToRelative(
                    a = 0.329f,
                    b = 0.329f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.179f,
                    dy1 = -0.44f,
                )
                // l -0.322 -0.686
                lineToRelative(dx = -0.322f, dy = -0.686f)
                // c -0.124 -0.262 -0.464 -0.356 -0.728 -0.226
                curveToRelative(
                    dx1 = -0.124f,
                    dy1 = -0.262f,
                    dx2 = -0.464f,
                    dy2 = -0.356f,
                    dx3 = -0.728f,
                    dy3 = -0.226f,
                )
                // a 10.44 10.44 0 0 1 -1.672 0.626
                arcToRelative(
                    a = 10.44f,
                    b = 10.44f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.672f,
                    dy1 = 0.626f,
                )
                // l 0.413 -1.504
                lineToRelative(dx = 0.413f, dy = -1.504f)
                // c 0.083 -0.303 0.143 -0.513 0.202 -0.692
                curveToRelative(
                    dx1 = 0.083f,
                    dy1 = -0.303f,
                    dx2 = 0.143f,
                    dy2 = -0.513f,
                    dx3 = 0.202f,
                    dy3 = -0.692f,
                )
                // a 0.49 0.49 0 0 0 -0.335 -0.63
                arcToRelative(
                    a = 0.49f,
                    b = 0.49f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.335f,
                    dy1 = -0.63f,
                )
                // l -0.582 -0.151
                lineToRelative(dx = -0.582f, dy = -0.151f)
                // a 0.493 0.493 0 0 0 -0.603 0.373
                arcToRelative(
                    a = 0.493f,
                    b = 0.493f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.603f,
                    dy1 = 0.373f,
                )
                // c -0.04 0.183 -0.094 0.398 -0.179 0.708
                curveToRelative(
                    dx1 = -0.04f,
                    dy1 = 0.183f,
                    dx2 = -0.094f,
                    dy2 = 0.398f,
                    dx3 = -0.179f,
                    dy3 = 0.708f,
                )
                // l -1.75 6.371
                lineToRelative(dx = -1.75f, dy = 6.371f)
                // c -0.154 0.562 -0.116 0.878 0.165 1.135
                curveToRelative(
                    dx1 = -0.154f,
                    dy1 = 0.562f,
                    dx2 = -0.116f,
                    dy2 = 0.878f,
                    dx3 = 0.165f,
                    dy3 = 1.135f,
                )
                // c 0.19 0.173 0.528 0.302 1.416 0.534
                curveToRelative(
                    dx1 = 0.19f,
                    dy1 = 0.173f,
                    dx2 = 0.528f,
                    dy2 = 0.302f,
                    dx3 = 1.416f,
                    dy3 = 0.534f,
                )
                // l 0.208 0.053
                lineToRelative(dx = 0.208f, dy = 0.053f)
                // c 0.402 -0.085 0.785 -0.19 1.177 -0.3
                curveToRelative(
                    dx1 = 0.402f,
                    dy1 = -0.085f,
                    dx2 = 0.785f,
                    dy2 = -0.19f,
                    dx3 = 1.177f,
                    dy3 = -0.3f,
                )
                // l 0.012 -0.002
                lineToRelative(dx = 0.012f, dy = -0.002f)
                // c 0.343 -0.095 0.694 -0.193 1.068 -0.276
                curveToRelative(
                    dx1 = 0.343f,
                    dy1 = -0.095f,
                    dx2 = 0.694f,
                    dy2 = -0.193f,
                    dx3 = 1.068f,
                    dy3 = -0.276f,
                )
                // c 0.191 -0.33 0.38 -0.788 0.59 -1.425
                curveToRelative(
                    dx1 = 0.191f,
                    dy1 = -0.33f,
                    dx2 = 0.38f,
                    dy2 = -0.788f,
                    dx3 = 0.59f,
                    dy3 = -1.425f,
                )
                // a 0.485 0.485 0 0 0 -0.22 -0.557
                arcToRelative(
                    a = 0.485f,
                    b = 0.485f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.22f,
                    dy1 = -0.557f,
                )
                // a 5.264 5.264 0 0 1 -0.76 -0.58
                arcToRelative(
                    a = 5.264f,
                    b = 5.264f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.76f,
                    dy1 = -0.58f,
                )
                // c -0.069 -0.063 -0.183 -0.034 -0.207 0.054
                curveToRelative(
                    dx1 = -0.069f,
                    dy1 = -0.063f,
                    dx2 = -0.183f,
                    dy2 = -0.034f,
                    dx3 = -0.207f,
                    dy3 = 0.054f,
                )
                // l -0.1 0.362
                lineToRelative(dx = -0.1f, dy = 0.362f)
                // a 8.228 8.228 0 0 1 -0.403 1.13
                arcToRelative(
                    a = 8.228f,
                    b = 8.228f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.403f,
                    dy1 = 1.13f,
                )
                // c -0.068 0.135 -0.118 0.203 -0.179 0.238
                curveToRelative(
                    dx1 = -0.068f,
                    dy1 = 0.135f,
                    dx2 = -0.118f,
                    dy2 = 0.203f,
                    dx3 = -0.179f,
                    dy3 = 0.238f,
                )
                // c -0.154 0.072 -0.317 0.06 -0.751 -0.053
                curveToRelative(
                    dx1 = -0.154f,
                    dy1 = 0.072f,
                    dx2 = -0.317f,
                    dy2 = 0.06f,
                    dx3 = -0.751f,
                    dy3 = -0.053f,
                )
                // c -0.377 -0.099 -0.45 -0.17 -0.404 -0.34z
                curveToRelative(
                    dx1 = -0.377f,
                    dy1 = -0.099f,
                    dx2 = -0.45f,
                    dy2 = -0.17f,
                    dx3 = -0.404f,
                    dy3 = -0.34f,
                )
                close()
                // m 8.22 -4.945
                moveToRelative(dx = 8.22f, dy = -4.945f)
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
        }.build().also { _ic1606 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1606: ImageVector? = null
