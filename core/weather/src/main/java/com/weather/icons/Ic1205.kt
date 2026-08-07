package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1205: ImageVector
    get() {
        val current = _ic1205
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1205",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M10.833 11 c1.809 -.038 2.828 .07 4.594 .485 a.47 .47 0 0 0 .562 -.377 .497 .497 0 0 0 -.357 -.593 c-1.84 -.432 -2.934 -.548 -4.818 -.508 -1.174 .025 -2.072 .274 -2.922 .51 l-.012 .003 c-.852 .236 -1.657 .458 -2.713 .48 -1.809 .038 -2.828 -.07 -4.594 -.485 a.47 .47 0 0 0 -.562 .377 .497 .497 0 0 0 .357 .593 c1.84 .432 2.934 .548 4.818 .508 1.174 -.025 2.072 -.274 2.922 -.51 l.012 -.003 c.852 -.236 1.657 -.458 2.713 -.48Z m4.594 2.485 c-1.766 -.416 -2.785 -.523 -4.594 -.485 -1.056 .022 -1.861 .244 -2.713 .48 l-.012 .003 c-.85 .236 -1.748 .485 -2.922 .51 -1.884 .04 -2.978 -.076 -4.818 -.508 a.497 .497 0 0 1 -.357 -.593 .47 .47 0 0 1 .562 -.377 c1.766 .416 2.785 .523 4.594 .485 1.056 -.022 1.861 -.244 2.713 -.48 l.012 -.003 c.85 -.236 1.748 -.485 2.922 -.51 1.884 -.04 2.978 .076 4.818 .508 .253 .06 .413 .325 .357 .593 a.47 .47 0 0 1 -.562 .377Z m0 2 c-1.766 -.416 -2.785 -.523 -4.594 -.485 -1.056 .022 -1.861 .244 -2.713 .48 l-.012 .003 c-.85 .236 -1.748 .485 -2.922 .51 -1.884 .04 -2.978 -.076 -4.818 -.508 a.497 .497 0 0 1 -.357 -.593 .47 .47 0 0 1 .562 -.377 c1.766 .416 2.785 .523 4.594 .485 1.056 -.022 1.861 -.244 2.713 -.48 l.012 -.003 c.85 -.236 1.748 -.485 2.922 -.51 1.884 -.04 2.978 .076 4.818 .508 .253 .06 .413 .325 .357 .593 a.47 .47 0 0 1 -.562 .377Z M7.86 2.706 a.35 .35 0 0 1 .675 .18 l-.14 .526 .525 -.14 a.35 .35 0 0 1 .181 .675 l-.525 .141 .384 .385 a.35 .35 0 1 1 -.495 .495 l-.384 -.385 -.141 .525 a.35 .35 0 0 1 -.676 -.18 l.14 -.526 -.525 .14 a.35 .35 0 0 1 -.18 -.675 l.524 -.141 -.384 -.385 a.35 .35 0 0 1 .495 -.495 l.384 .385 .141 -.525Z
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
                // M 7.86 2.706
                moveTo(x = 7.86f, y = 2.706f)
                // a 0.35 0.35 0 0 1 0.675 0.18
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.675f,
                    dy1 = 0.18f,
                )
                // l -0.14 0.526
                lineToRelative(dx = -0.14f, dy = 0.526f)
                // l 0.525 -0.14
                lineToRelative(dx = 0.525f, dy = -0.14f)
                // a 0.35 0.35 0 0 1 0.181 0.675
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.181f,
                    dy1 = 0.675f,
                )
                // l -0.525 0.141
                lineToRelative(dx = -0.525f, dy = 0.141f)
                // l 0.384 0.385
                lineToRelative(dx = 0.384f, dy = 0.385f)
                // a 0.35 0.35 0 1 1 -0.495 0.495
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.495f,
                    dy1 = 0.495f,
                )
                // l -0.384 -0.385
                lineToRelative(dx = -0.384f, dy = -0.385f)
                // l -0.141 0.525
                lineToRelative(dx = -0.141f, dy = 0.525f)
                // a 0.35 0.35 0 0 1 -0.676 -0.18
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.676f,
                    dy1 = -0.18f,
                )
                // l 0.14 -0.526
                lineToRelative(dx = 0.14f, dy = -0.526f)
                // l -0.525 0.14
                lineToRelative(dx = -0.525f, dy = 0.14f)
                // a 0.35 0.35 0 0 1 -0.18 -0.675
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.18f,
                    dy1 = -0.675f,
                )
                // l 0.524 -0.141
                lineToRelative(dx = 0.524f, dy = -0.141f)
                // l -0.384 -0.385
                lineToRelative(dx = -0.384f, dy = -0.385f)
                // a 0.35 0.35 0 0 1 0.495 -0.495
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.495f,
                    dy1 = -0.495f,
                )
                // l 0.384 0.385
                lineToRelative(dx = 0.384f, dy = 0.385f)
                // l 0.141 -0.525z
                lineToRelative(dx = 0.141f, dy = -0.525f)
                close()
            }
            // M8.793 1.077 10.72 3.01 a.264 .264 0 0 1 .068 .255 l-.708 2.643 a.264 .264 0 0 1 -.187 .186 l-2.635 .71 a.258 .258 0 0 1 -.254 -.068 L5.077 4.803 a.264 .264 0 0 1 -.068 -.254 l.708 -2.643 a.264 .264 0 0 1 .186 -.187 l2.636 -.71 a.26 .26 0 0 1 .254 .068Z M7.269 6.255 l2.342 -.63 .63 -2.35 -1.713 -1.717 -2.342 .632 -.63 2.348 L7.27 6.255Z m4.961 .618 a.35 .35 0 0 1 .657 -.239 l.186 .511 .35 -.417 a.35 .35 0 0 1 .536 .45 l-.35 .417 .536 .094 a.35 .35 0 0 1 -.122 .69 l-.535 -.095 .186 .511 a.35 .35 0 0 1 -.658 .24 l-.186 -.511 -.35 .416 a.35 .35 0 0 1 -.536 -.45 l.35 -.416 -.536 -.095 a.35 .35 0 0 1 .122 -.689 l.535 .094 -.186 -.51Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.793 1.077
                moveTo(x = 8.793f, y = 1.077f)
                // L 10.72 3.01
                lineTo(x = 10.72f, y = 3.01f)
                // a 0.264 0.264 0 0 1 0.068 0.255
                arcToRelative(
                    a = 0.264f,
                    b = 0.264f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.068f,
                    dy1 = 0.255f,
                )
                // l -0.708 2.643
                lineToRelative(dx = -0.708f, dy = 2.643f)
                // a 0.264 0.264 0 0 1 -0.187 0.186
                arcToRelative(
                    a = 0.264f,
                    b = 0.264f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.187f,
                    dy1 = 0.186f,
                )
                // l -2.635 0.71
                lineToRelative(dx = -2.635f, dy = 0.71f)
                // a 0.258 0.258 0 0 1 -0.254 -0.068
                arcToRelative(
                    a = 0.258f,
                    b = 0.258f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.254f,
                    dy1 = -0.068f,
                )
                // L 5.077 4.803
                lineTo(x = 5.077f, y = 4.803f)
                // a 0.264 0.264 0 0 1 -0.068 -0.254
                arcToRelative(
                    a = 0.264f,
                    b = 0.264f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.068f,
                    dy1 = -0.254f,
                )
                // l 0.708 -2.643
                lineToRelative(dx = 0.708f, dy = -2.643f)
                // a 0.264 0.264 0 0 1 0.186 -0.187
                arcToRelative(
                    a = 0.264f,
                    b = 0.264f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.186f,
                    dy1 = -0.187f,
                )
                // l 2.636 -0.71
                lineToRelative(dx = 2.636f, dy = -0.71f)
                // a 0.26 0.26 0 0 1 0.254 0.068z
                arcToRelative(
                    a = 0.26f,
                    b = 0.26f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.254f,
                    dy1 = 0.068f,
                )
                close()
                // M 7.269 6.255
                moveTo(x = 7.269f, y = 6.255f)
                // l 2.342 -0.63
                lineToRelative(dx = 2.342f, dy = -0.63f)
                // l 0.63 -2.35
                lineToRelative(dx = 0.63f, dy = -2.35f)
                // l -1.713 -1.717
                lineToRelative(dx = -1.713f, dy = -1.717f)
                // l -2.342 0.632
                lineToRelative(dx = -2.342f, dy = 0.632f)
                // l -0.63 2.348
                lineToRelative(dx = -0.63f, dy = 2.348f)
                // L 7.27 6.255z
                lineTo(x = 7.27f, y = 6.255f)
                close()
                // m 4.961 0.618
                moveToRelative(dx = 4.961f, dy = 0.618f)
                // a 0.35 0.35 0 0 1 0.657 -0.239
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.657f,
                    dy1 = -0.239f,
                )
                // l 0.186 0.511
                lineToRelative(dx = 0.186f, dy = 0.511f)
                // l 0.35 -0.417
                lineToRelative(dx = 0.35f, dy = -0.417f)
                // a 0.35 0.35 0 0 1 0.536 0.45
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.536f,
                    dy1 = 0.45f,
                )
                // l -0.35 0.417
                lineToRelative(dx = -0.35f, dy = 0.417f)
                // l 0.536 0.094
                lineToRelative(dx = 0.536f, dy = 0.094f)
                // a 0.35 0.35 0 0 1 -0.122 0.69
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.122f,
                    dy1 = 0.69f,
                )
                // l -0.535 -0.095
                lineToRelative(dx = -0.535f, dy = -0.095f)
                // l 0.186 0.511
                lineToRelative(dx = 0.186f, dy = 0.511f)
                // a 0.35 0.35 0 0 1 -0.658 0.24
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.658f,
                    dy1 = 0.24f,
                )
                // l -0.186 -0.511
                lineToRelative(dx = -0.186f, dy = -0.511f)
                // l -0.35 0.416
                lineToRelative(dx = -0.35f, dy = 0.416f)
                // a 0.35 0.35 0 0 1 -0.536 -0.45
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.536f,
                    dy1 = -0.45f,
                )
                // l 0.35 -0.416
                lineToRelative(dx = 0.35f, dy = -0.416f)
                // l -0.536 -0.095
                lineToRelative(dx = -0.536f, dy = -0.095f)
                // a 0.35 0.35 0 0 1 0.122 -0.689
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.122f,
                    dy1 = -0.689f,
                )
                // l 0.535 0.094
                lineToRelative(dx = 0.535f, dy = 0.094f)
                // l -0.186 -0.51z
                lineToRelative(dx = -0.186f, dy = -0.51f)
                close()
            }
            // m12.06 5.004 2.687 .478 a.264 .264 0 0 1 .202 .17 l.936 2.57 a.265 .265 0 0 1 -.046 .26 l-1.476 1.765 c-1.133 -.206 -2.076 -.266 -3.416 -.242 l-.931 -2.558 a.264 .264 0 0 1 .045 -.26 l1.752 -2.093 a.26 .26 0 0 1 .247 -.09Z m1.722 5.116 1.556 -1.86 -.831 -2.286 -2.388 -.424 -1.556 1.86 .83 2.285 2.39 .425Z M3.126 7 a.35 .35 0 0 0 -.35 .35 v.544 l-.47 -.272 a.35 .35 0 0 0 -.35 .606 l.47 .272 -.47 .272 a.35 .35 0 0 0 .35 .606 l.47 -.272 v.544 a.35 .35 0 1 0 .7 0 v-.544 l.47 .272 a.35 .35 0 0 0 .35 -.606 l-.47 -.272 .47 -.272 a.35 .35 0 0 0 -.35 -.606 l-.47 .272 V7.35 a.35 .35 0 0 0 -.35 -.35Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 12.06 5.004
                moveTo(x = 12.06f, y = 5.004f)
                // l 2.687 0.478
                lineToRelative(dx = 2.687f, dy = 0.478f)
                // a 0.264 0.264 0 0 1 0.202 0.17
                arcToRelative(
                    a = 0.264f,
                    b = 0.264f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.202f,
                    dy1 = 0.17f,
                )
                // l 0.936 2.57
                lineToRelative(dx = 0.936f, dy = 2.57f)
                // a 0.265 0.265 0 0 1 -0.046 0.26
                arcToRelative(
                    a = 0.265f,
                    b = 0.265f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.046f,
                    dy1 = 0.26f,
                )
                // l -1.476 1.765
                lineToRelative(dx = -1.476f, dy = 1.765f)
                // c -1.133 -0.206 -2.076 -0.266 -3.416 -0.242
                curveToRelative(
                    dx1 = -1.133f,
                    dy1 = -0.206f,
                    dx2 = -2.076f,
                    dy2 = -0.266f,
                    dx3 = -3.416f,
                    dy3 = -0.242f,
                )
                // l -0.931 -2.558
                lineToRelative(dx = -0.931f, dy = -2.558f)
                // a 0.264 0.264 0 0 1 0.045 -0.26
                arcToRelative(
                    a = 0.264f,
                    b = 0.264f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.045f,
                    dy1 = -0.26f,
                )
                // l 1.752 -2.093
                lineToRelative(dx = 1.752f, dy = -2.093f)
                // a 0.26 0.26 0 0 1 0.247 -0.09z
                arcToRelative(
                    a = 0.26f,
                    b = 0.26f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.247f,
                    dy1 = -0.09f,
                )
                close()
                // m 1.722 5.116
                moveToRelative(dx = 1.722f, dy = 5.116f)
                // l 1.556 -1.86
                lineToRelative(dx = 1.556f, dy = -1.86f)
                // l -0.831 -2.286
                lineToRelative(dx = -0.831f, dy = -2.286f)
                // l -2.388 -0.424
                lineToRelative(dx = -2.388f, dy = -0.424f)
                // l -1.556 1.86
                lineToRelative(dx = -1.556f, dy = 1.86f)
                // l 0.83 2.285
                lineToRelative(dx = 0.83f, dy = 2.285f)
                // l 2.39 0.425z
                lineToRelative(dx = 2.39f, dy = 0.425f)
                close()
                // M 3.126 7
                moveTo(x = 3.126f, y = 7.0f)
                // a 0.35 0.35 0 0 0 -0.35 0.35
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = 0.35f,
                )
                // v 0.544
                verticalLineToRelative(dy = 0.544f)
                // l -0.47 -0.272
                lineToRelative(dx = -0.47f, dy = -0.272f)
                // a 0.35 0.35 0 0 0 -0.35 0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = 0.606f,
                )
                // l 0.47 0.272
                lineToRelative(dx = 0.47f, dy = 0.272f)
                // l -0.47 0.272
                lineToRelative(dx = -0.47f, dy = 0.272f)
                // a 0.35 0.35 0 0 0 0.35 0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.35f,
                    dy1 = 0.606f,
                )
                // l 0.47 -0.272
                lineToRelative(dx = 0.47f, dy = -0.272f)
                // v 0.544
                verticalLineToRelative(dy = 0.544f)
                // a 0.35 0.35 0 1 0 0.7 0
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.7f,
                    dy1 = 0.0f,
                )
                // v -0.544
                verticalLineToRelative(dy = -0.544f)
                // l 0.47 0.272
                lineToRelative(dx = 0.47f, dy = 0.272f)
                // a 0.35 0.35 0 0 0 0.35 -0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.35f,
                    dy1 = -0.606f,
                )
                // l -0.47 -0.272
                lineToRelative(dx = -0.47f, dy = -0.272f)
                // l 0.47 -0.272
                lineToRelative(dx = 0.47f, dy = -0.272f)
                // a 0.35 0.35 0 0 0 -0.35 -0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = -0.606f,
                )
                // l -0.47 0.272
                lineToRelative(dx = -0.47f, dy = 0.272f)
                // V 7.35
                verticalLineTo(y = 7.35f)
                // a 0.35 0.35 0 0 0 -0.35 -0.35z
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = -0.35f,
                )
                close()
            }
            // m4.054 11.002 1.564 -.906 a.264 .264 0 0 0 .132 -.228 V7.132 a.264 .264 0 0 0 -.132 -.228 L3.256 5.536 a.26 .26 0 0 0 -.263 0 L.632 6.904 a.264 .264 0 0 0 -.132 .228 v2.736 c0 .094 .05 .181 .132 .228 l1.173 .68 c.774 .14 1.451 .208 2.25 .226Z m-.929 -.07 L1.026 9.716 V7.285 l2.099 -1.216 2.099 1.215 v2.432 l-2.099 1.216Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.054 11.002
                moveTo(x = 4.054f, y = 11.002f)
                // l 1.564 -0.906
                lineToRelative(dx = 1.564f, dy = -0.906f)
                // a 0.264 0.264 0 0 0 0.132 -0.228
                arcToRelative(
                    a = 0.264f,
                    b = 0.264f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.132f,
                    dy1 = -0.228f,
                )
                // V 7.132
                verticalLineTo(y = 7.132f)
                // a 0.264 0.264 0 0 0 -0.132 -0.228
                arcToRelative(
                    a = 0.264f,
                    b = 0.264f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.132f,
                    dy1 = -0.228f,
                )
                // L 3.256 5.536
                lineTo(x = 3.256f, y = 5.536f)
                // a 0.26 0.26 0 0 0 -0.263 0
                arcToRelative(
                    a = 0.26f,
                    b = 0.26f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.263f,
                    dy1 = 0.0f,
                )
                // L 0.632 6.904
                lineTo(x = 0.632f, y = 6.904f)
                // a 0.264 0.264 0 0 0 -0.132 0.228
                arcToRelative(
                    a = 0.264f,
                    b = 0.264f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.132f,
                    dy1 = 0.228f,
                )
                // v 2.736
                verticalLineToRelative(dy = 2.736f)
                // c 0 0.094 0.05 0.181 0.132 0.228
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.094f,
                    dx2 = 0.05f,
                    dy2 = 0.181f,
                    dx3 = 0.132f,
                    dy3 = 0.228f,
                )
                // l 1.173 0.68
                lineToRelative(dx = 1.173f, dy = 0.68f)
                // c 0.774 0.14 1.451 0.208 2.25 0.226z
                curveToRelative(
                    dx1 = 0.774f,
                    dy1 = 0.14f,
                    dx2 = 1.451f,
                    dy2 = 0.208f,
                    dx3 = 2.25f,
                    dy3 = 0.226f,
                )
                close()
                // m -0.929 -0.07
                moveToRelative(dx = -0.929f, dy = -0.07f)
                // L 1.026 9.716
                lineTo(x = 1.026f, y = 9.716f)
                // V 7.285
                verticalLineTo(y = 7.285f)
                // l 2.099 -1.216
                lineToRelative(dx = 2.099f, dy = -1.216f)
                // l 2.099 1.215
                lineToRelative(dx = 2.099f, dy = 1.215f)
                // v 2.432
                verticalLineToRelative(dy = 2.432f)
                // l -2.099 1.216z
                lineToRelative(dx = -2.099f, dy = 1.216f)
                close()
            }
        }.build().also { _ic1205 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1205: ImageVector? = null
