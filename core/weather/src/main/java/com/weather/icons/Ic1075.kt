package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1075: ImageVector
    get() {
        val current = _ic1075
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1075",
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
            // M12.9 0 h-3 v12.074 c.287 -.036 .59 -.06 .914 -.067 a20.209 20.209 0 0 1 2.086 .046 V0Z m-.75 1.885 v.961 h-1.5 v-.961 h1.5Z m-1.5 2.48 h1.5 v.962 h-1.5 v-.962Z m1.5 2.48 v1.923 h-1.5 V6.845 h1.5Z M9.2 4.5 v7.69 c-.465 .093 -.892 .212 -1.308 .327 l-.012 .003 c-.228 .063 -.453 .125 -.68 .183 V4.5 a.5 .5 0 0 1 .5 -.5 h1 a.5 .5 0 0 1 .5 .5Z M8.2 8 a.5 .5 0 1 0 0 -1 .5 .5 0 0 0 0 1Z m.5 -2.5 a.5 .5 0 1 0 -1 0 .5 .5 0 0 0 1 0Z M5.4 10 v2.992 a24.018 24.018 0 0 1 -1.5 .006 V11 h-1 v1.933 c-.484 -.05 -.963 -.126 -1.5 -.235 V10 H.637 c-.234 0 -.326 -.262 -.131 -.374 l2.763 -1.592 a.268 .268 0 0 1 .262 0 l2.763 1.592 c.195 .112 .103 .374 -.13 .374 H5.4Z m10.2 -7.5 v10.008 a20.4 20.4 0 0 0 -2 -.381 V2.5 a.5 .5 0 0 1 .5 -.5 h1 a.5 .5 0 0 1 .5 .5Z m-.5 .5 h-1 v1 h1 V3Z m-1 2 v1 h1 V5 h-1Z m1 2 h-1 v1 h1 V7Z m-1 2 v1 h1 V9 h-1Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 12.9 0
                moveTo(x = 12.9f, y = 0.0f)
                // h -3
                horizontalLineToRelative(dx = -3.0f)
                // v 12.074
                verticalLineToRelative(dy = 12.074f)
                // c 0.287 -0.036 0.59 -0.06 0.914 -0.067
                curveToRelative(
                    dx1 = 0.287f,
                    dy1 = -0.036f,
                    dx2 = 0.59f,
                    dy2 = -0.06f,
                    dx3 = 0.914f,
                    dy3 = -0.067f,
                )
                // a 20.209 20.209 0 0 1 2.086 0.046
                arcToRelative(
                    a = 20.209f,
                    b = 20.209f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.086f,
                    dy1 = 0.046f,
                )
                // V 0z
                verticalLineTo(y = 0.0f)
                close()
                // m -0.75 1.885
                moveToRelative(dx = -0.75f, dy = 1.885f)
                // v 0.961
                verticalLineToRelative(dy = 0.961f)
                // h -1.5
                horizontalLineToRelative(dx = -1.5f)
                // v -0.961
                verticalLineToRelative(dy = -0.961f)
                // h 1.5z
                horizontalLineToRelative(dx = 1.5f)
                close()
                // m -1.5 2.48
                moveToRelative(dx = -1.5f, dy = 2.48f)
                // h 1.5
                horizontalLineToRelative(dx = 1.5f)
                // v 0.962
                verticalLineToRelative(dy = 0.962f)
                // h -1.5
                horizontalLineToRelative(dx = -1.5f)
                // v -0.962z
                verticalLineToRelative(dy = -0.962f)
                close()
                // m 1.5 2.48
                moveToRelative(dx = 1.5f, dy = 2.48f)
                // v 1.923
                verticalLineToRelative(dy = 1.923f)
                // h -1.5
                horizontalLineToRelative(dx = -1.5f)
                // V 6.845
                verticalLineTo(y = 6.845f)
                // h 1.5z
                horizontalLineToRelative(dx = 1.5f)
                close()
                // M 9.2 4.5
                moveTo(x = 9.2f, y = 4.5f)
                // v 7.69
                verticalLineToRelative(dy = 7.69f)
                // c -0.465 0.093 -0.892 0.212 -1.308 0.327
                curveToRelative(
                    dx1 = -0.465f,
                    dy1 = 0.093f,
                    dx2 = -0.892f,
                    dy2 = 0.212f,
                    dx3 = -1.308f,
                    dy3 = 0.327f,
                )
                // l -0.012 0.003
                lineToRelative(dx = -0.012f, dy = 0.003f)
                // c -0.228 0.063 -0.453 0.125 -0.68 0.183
                curveToRelative(
                    dx1 = -0.228f,
                    dy1 = 0.063f,
                    dx2 = -0.453f,
                    dy2 = 0.125f,
                    dx3 = -0.68f,
                    dy3 = 0.183f,
                )
                // V 4.5
                verticalLineTo(y = 4.5f)
                // a 0.5 0.5 0 0 1 0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = -0.5f,
                )
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // a 0.5 0.5 0 0 1 0.5 0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = 0.5f,
                )
                close()
                // M 8.2 8
                moveTo(x = 8.2f, y = 8.0f)
                // a 0.5 0.5 0 1 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // a 0.5 0.5 0 0 0 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
                // m 0.5 -2.5
                moveToRelative(dx = 0.5f, dy = -2.5f)
                // a 0.5 0.5 0 1 0 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // a 0.5 0.5 0 0 0 1 0z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                close()
                // M 5.4 10
                moveTo(x = 5.4f, y = 10.0f)
                // v 2.992
                verticalLineToRelative(dy = 2.992f)
                // a 24.018 24.018 0 0 1 -1.5 0.006
                arcToRelative(
                    a = 24.018f,
                    b = 24.018f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.5f,
                    dy1 = 0.006f,
                )
                // V 11
                verticalLineTo(y = 11.0f)
                // h -1
                horizontalLineToRelative(dx = -1.0f)
                // v 1.933
                verticalLineToRelative(dy = 1.933f)
                // c -0.484 -0.05 -0.963 -0.126 -1.5 -0.235
                curveToRelative(
                    dx1 = -0.484f,
                    dy1 = -0.05f,
                    dx2 = -0.963f,
                    dy2 = -0.126f,
                    dx3 = -1.5f,
                    dy3 = -0.235f,
                )
                // V 10
                verticalLineTo(y = 10.0f)
                // H 0.637
                horizontalLineTo(x = 0.637f)
                // c -0.234 0 -0.326 -0.262 -0.131 -0.374
                curveToRelative(
                    dx1 = -0.234f,
                    dy1 = 0.0f,
                    dx2 = -0.326f,
                    dy2 = -0.262f,
                    dx3 = -0.131f,
                    dy3 = -0.374f,
                )
                // l 2.763 -1.592
                lineToRelative(dx = 2.763f, dy = -1.592f)
                // a 0.268 0.268 0 0 1 0.262 0
                arcToRelative(
                    a = 0.268f,
                    b = 0.268f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.262f,
                    dy1 = 0.0f,
                )
                // l 2.763 1.592
                lineToRelative(dx = 2.763f, dy = 1.592f)
                // c 0.195 0.112 0.103 0.374 -0.13 0.374
                curveToRelative(
                    dx1 = 0.195f,
                    dy1 = 0.112f,
                    dx2 = 0.103f,
                    dy2 = 0.374f,
                    dx3 = -0.13f,
                    dy3 = 0.374f,
                )
                // H 5.4z
                horizontalLineTo(x = 5.4f)
                close()
                // m 10.2 -7.5
                moveToRelative(dx = 10.2f, dy = -7.5f)
                // v 10.008
                verticalLineToRelative(dy = 10.008f)
                // a 20.4 20.4 0 0 0 -2 -0.381
                arcToRelative(
                    a = 20.4f,
                    b = 20.4f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.0f,
                    dy1 = -0.381f,
                )
                // V 2.5
                verticalLineTo(y = 2.5f)
                // a 0.5 0.5 0 0 1 0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = -0.5f,
                )
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // a 0.5 0.5 0 0 1 0.5 0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = 0.5f,
                )
                close()
                // m -0.5 0.5
                moveToRelative(dx = -0.5f, dy = 0.5f)
                // h -1
                horizontalLineToRelative(dx = -1.0f)
                // v 1
                verticalLineToRelative(dy = 1.0f)
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // V 3z
                verticalLineTo(y = 3.0f)
                close()
                // m -1 2
                moveToRelative(dx = -1.0f, dy = 2.0f)
                // v 1
                verticalLineToRelative(dy = 1.0f)
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // V 5
                verticalLineTo(y = 5.0f)
                // h -1z
                horizontalLineToRelative(dx = -1.0f)
                close()
                // m 1 2
                moveToRelative(dx = 1.0f, dy = 2.0f)
                // h -1
                horizontalLineToRelative(dx = -1.0f)
                // v 1
                verticalLineToRelative(dy = 1.0f)
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // V 7z
                verticalLineTo(y = 7.0f)
                close()
                // m -1 2
                moveToRelative(dx = -1.0f, dy = 2.0f)
                // v 1
                verticalLineToRelative(dy = 1.0f)
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // V 9
                verticalLineTo(y = 9.0f)
                // h -1z
                horizontalLineToRelative(dx = -1.0f)
                close()
            }
        }.build().also { _ic1075 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1075: ImageVector? = null
