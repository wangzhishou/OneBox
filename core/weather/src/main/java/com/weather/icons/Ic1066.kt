package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1066: ImageVector
    get() {
        val current = _ic1066
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1066",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M11.828 1.179 a.25 .25 0 0 0 .427 -.177 v-.75 a.25 .25 0 0 0 -.5 0 v.75 c0 .066 .026 .13 .073 .177Z m-2.04 .931 a.25 .25 0 0 0 .273 -.409 l-.532 -.53 a.25 .25 0 0 0 -.354 .354 l.532 .53 a.25 .25 0 0 0 .08 .054Z M8.252 4.248 h.75 a.25 .25 0 0 0 0 -.5 h-.75 a.25 .25 0 0 0 0 .5Z m.956 2.611 a.25 .25 0 0 0 .316 -.032 l.531 -.53 a.25 .25 0 0 0 -.353 -.354 l-.532 .53 a.25 .25 0 0 0 .038 .386Z m2.613 1.069 a.25 .25 0 0 0 .427 -.177 v-.75 a.25 .25 0 0 0 -.5 0 v.75 c0 .067 .026 .13 .073 .177Z m2.733 -1.042 a.249 .249 0 0 0 .327 -.135 .25 .25 0 0 0 -.054 -.273 l-.53 -.53 a.25 .25 0 1 0 -.354 .354 l.53 .53 a.25 .25 0 0 0 .081 .054Z m.447 -2.631 h.75 a.25 .25 0 0 0 0 -.5 h-.75 a.25 .25 0 0 0 0 .5Z m-1.015 -2.164 a.25 .25 0 0 0 .316 -.031 l.53 -.53 a.25 .25 0 0 0 -.081 -.408 .25 .25 0 0 0 -.273 .054 l-.53 .53 a.25 .25 0 0 0 .038 .385Z m-3.234 .039 a2.25 2.25 0 1 1 2.5 3.74 2.25 2.25 0 0 1 -2.5 -3.74Z m2.223 .415 a1.75 1.75 0 1 0 -1.945 2.91 1.75 1.75 0 0 0 1.945 -2.91Z M7 8.5 a2.5 2.5 0 1 1 -5 0 2.5 2.5 0 0 1 5 0Z m-3.805 .305 a.667 .667 0 0 0 1.138 -.472 c0 -.333 -.37 -.93 -.666 -1.333 C3.37 7.403 3 8 3 8.333 c0 .177 .07 .347 .195 .472Z M4.5 11.5 c-.996 0 -1.636 .006 -2.5 .5 -1.516 .867 -2 4 -2 4 h9 s-.484 -3.133 -2 -4 c-.864 -.494 -1.504 -.5 -2.5 -.5Z M2.35 2.8 a.25 .25 0 0 1 .35 .05 l.14 .186 a.818 .818 0 0 1 -.201 1.172 .318 .318 0 0 0 -.078 .456 l.139 .186 a.25 .25 0 1 1 -.4 .3 l-.14 -.186 a.818 .818 0 0 1 .201 -1.172 .318 .318 0 0 0 .078 -.456 L2.3 3.15 a.25 .25 0 0 1 .05 -.35Z m2 0 a.25 .25 0 0 1 .35 .05 l.14 .186 a.818 .818 0 0 1 -.201 1.172 .318 .318 0 0 0 -.078 .456 l.139 .186 a.25 .25 0 1 1 -.4 .3 l-.14 -.186 a.818 .818 0 0 1 .201 -1.172 .318 .318 0 0 0 .078 -.456 L4.3 3.15 a.25 .25 0 0 1 .05 -.35Z m2.35 .05 a.25 .25 0 0 0 -.4 .3 l.14 .186 a.318 .318 0 0 1 -.079 .456 .818 .818 0 0 0 -.2 1.172 l.139 .186 a.25 .25 0 1 0 .4 -.3 l-.14 -.186 a.318 .318 0 0 1 .079 -.456 .818 .818 0 0 0 .2 -1.172 L6.7 2.85Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.828 1.179
                moveTo(x = 11.828f, y = 1.179f)
                // a 0.25 0.25 0 0 0 0.427 -0.177
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.427f,
                    dy1 = -0.177f,
                )
                // v -0.75
                verticalLineToRelative(dy = -0.75f)
                // a 0.25 0.25 0 0 0 -0.5 0
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.5f,
                    dy1 = 0.0f,
                )
                // v 0.75
                verticalLineToRelative(dy = 0.75f)
                // c 0 0.066 0.026 0.13 0.073 0.177z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.066f,
                    dx2 = 0.026f,
                    dy2 = 0.13f,
                    dx3 = 0.073f,
                    dy3 = 0.177f,
                )
                close()
                // m -2.04 0.931
                moveToRelative(dx = -2.04f, dy = 0.931f)
                // a 0.25 0.25 0 0 0 0.273 -0.409
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.273f,
                    dy1 = -0.409f,
                )
                // l -0.532 -0.53
                lineToRelative(dx = -0.532f, dy = -0.53f)
                // a 0.25 0.25 0 0 0 -0.354 0.354
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.354f,
                    dy1 = 0.354f,
                )
                // l 0.532 0.53
                lineToRelative(dx = 0.532f, dy = 0.53f)
                // a 0.25 0.25 0 0 0 0.08 0.054z
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.08f,
                    dy1 = 0.054f,
                )
                close()
                // M 8.252 4.248
                moveTo(x = 8.252f, y = 4.248f)
                // h 0.75
                horizontalLineToRelative(dx = 0.75f)
                // a 0.25 0.25 0 0 0 0 -0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                )
                // h -0.75
                horizontalLineToRelative(dx = -0.75f)
                // a 0.25 0.25 0 0 0 0 0.5z
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                close()
                // m 0.956 2.611
                moveToRelative(dx = 0.956f, dy = 2.611f)
                // a 0.25 0.25 0 0 0 0.316 -0.032
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.316f,
                    dy1 = -0.032f,
                )
                // l 0.531 -0.53
                lineToRelative(dx = 0.531f, dy = -0.53f)
                // a 0.25 0.25 0 0 0 -0.353 -0.354
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.353f,
                    dy1 = -0.354f,
                )
                // l -0.532 0.53
                lineToRelative(dx = -0.532f, dy = 0.53f)
                // a 0.25 0.25 0 0 0 0.038 0.386z
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.038f,
                    dy1 = 0.386f,
                )
                close()
                // m 2.613 1.069
                moveToRelative(dx = 2.613f, dy = 1.069f)
                // a 0.25 0.25 0 0 0 0.427 -0.177
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.427f,
                    dy1 = -0.177f,
                )
                // v -0.75
                verticalLineToRelative(dy = -0.75f)
                // a 0.25 0.25 0 0 0 -0.5 0
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.5f,
                    dy1 = 0.0f,
                )
                // v 0.75
                verticalLineToRelative(dy = 0.75f)
                // c 0 0.067 0.026 0.13 0.073 0.177z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.067f,
                    dx2 = 0.026f,
                    dy2 = 0.13f,
                    dx3 = 0.073f,
                    dy3 = 0.177f,
                )
                close()
                // m 2.733 -1.042
                moveToRelative(dx = 2.733f, dy = -1.042f)
                // a 0.249 0.249 0 0 0 0.327 -0.135
                arcToRelative(
                    a = 0.249f,
                    b = 0.249f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.327f,
                    dy1 = -0.135f,
                )
                // a 0.25 0.25 0 0 0 -0.054 -0.273
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.054f,
                    dy1 = -0.273f,
                )
                // l -0.53 -0.53
                lineToRelative(dx = -0.53f, dy = -0.53f)
                // a 0.25 0.25 0 1 0 -0.354 0.354
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.354f,
                    dy1 = 0.354f,
                )
                // l 0.53 0.53
                lineToRelative(dx = 0.53f, dy = 0.53f)
                // a 0.25 0.25 0 0 0 0.081 0.054z
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.081f,
                    dy1 = 0.054f,
                )
                close()
                // m 0.447 -2.631
                moveToRelative(dx = 0.447f, dy = -2.631f)
                // h 0.75
                horizontalLineToRelative(dx = 0.75f)
                // a 0.25 0.25 0 0 0 0 -0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                )
                // h -0.75
                horizontalLineToRelative(dx = -0.75f)
                // a 0.25 0.25 0 0 0 0 0.5z
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                close()
                // m -1.015 -2.164
                moveToRelative(dx = -1.015f, dy = -2.164f)
                // a 0.25 0.25 0 0 0 0.316 -0.031
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.316f,
                    dy1 = -0.031f,
                )
                // l 0.53 -0.53
                lineToRelative(dx = 0.53f, dy = -0.53f)
                // a 0.25 0.25 0 0 0 -0.081 -0.408
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.081f,
                    dy1 = -0.408f,
                )
                // a 0.25 0.25 0 0 0 -0.273 0.054
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.273f,
                    dy1 = 0.054f,
                )
                // l -0.53 0.53
                lineToRelative(dx = -0.53f, dy = 0.53f)
                // a 0.25 0.25 0 0 0 0.038 0.385z
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.038f,
                    dy1 = 0.385f,
                )
                close()
                // m -3.234 0.039
                moveToRelative(dx = -3.234f, dy = 0.039f)
                // a 2.25 2.25 0 1 1 2.5 3.74
                arcToRelative(
                    a = 2.25f,
                    b = 2.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 2.5f,
                    dy1 = 3.74f,
                )
                // a 2.25 2.25 0 0 1 -2.5 -3.74z
                arcToRelative(
                    a = 2.25f,
                    b = 2.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.5f,
                    dy1 = -3.74f,
                )
                close()
                // m 2.223 0.415
                moveToRelative(dx = 2.223f, dy = 0.415f)
                // a 1.75 1.75 0 1 0 -1.945 2.91
                arcToRelative(
                    a = 1.75f,
                    b = 1.75f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -1.945f,
                    dy1 = 2.91f,
                )
                // a 1.75 1.75 0 0 0 1.945 -2.91z
                arcToRelative(
                    a = 1.75f,
                    b = 1.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.945f,
                    dy1 = -2.91f,
                )
                close()
                // M 7 8.5
                moveTo(x = 7.0f, y = 8.5f)
                // a 2.5 2.5 0 1 1 -5 0
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -5.0f,
                    dy1 = 0.0f,
                )
                // a 2.5 2.5 0 0 1 5 0z
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 5.0f,
                    dy1 = 0.0f,
                )
                close()
                // m -3.805 0.305
                moveToRelative(dx = -3.805f, dy = 0.305f)
                // a 0.667 0.667 0 0 0 1.138 -0.472
                arcToRelative(
                    a = 0.667f,
                    b = 0.667f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.138f,
                    dy1 = -0.472f,
                )
                // c 0 -0.333 -0.37 -0.93 -0.666 -1.333
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.333f,
                    dx2 = -0.37f,
                    dy2 = -0.93f,
                    dx3 = -0.666f,
                    dy3 = -1.333f,
                )
                // C 3.37 7.403 3 8 3 8.333
                curveTo(
                    x1 = 3.37f,
                    y1 = 7.403f,
                    x2 = 3.0f,
                    y2 = 8.0f,
                    x3 = 3.0f,
                    y3 = 8.333f,
                )
                // c 0 0.177 0.07 0.347 0.195 0.472z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.177f,
                    dx2 = 0.07f,
                    dy2 = 0.347f,
                    dx3 = 0.195f,
                    dy3 = 0.472f,
                )
                close()
                // M 4.5 11.5
                moveTo(x = 4.5f, y = 11.5f)
                // c -0.996 0 -1.636 0.006 -2.5 0.5
                curveToRelative(
                    dx1 = -0.996f,
                    dy1 = 0.0f,
                    dx2 = -1.636f,
                    dy2 = 0.006f,
                    dx3 = -2.5f,
                    dy3 = 0.5f,
                )
                // c -1.516 0.867 -2 4 -2 4
                curveToRelative(
                    dx1 = -1.516f,
                    dy1 = 0.867f,
                    dx2 = -2.0f,
                    dy2 = 4.0f,
                    dx3 = -2.0f,
                    dy3 = 4.0f,
                )
                // h 9
                horizontalLineToRelative(dx = 9.0f)
                // s -0.484 -3.133 -2 -4
                reflectiveCurveToRelative(
                    dx1 = -0.484f,
                    dy1 = -3.133f,
                    dx2 = -2.0f,
                    dy2 = -4.0f,
                )
                // c -0.864 -0.494 -1.504 -0.5 -2.5 -0.5z
                curveToRelative(
                    dx1 = -0.864f,
                    dy1 = -0.494f,
                    dx2 = -1.504f,
                    dy2 = -0.5f,
                    dx3 = -2.5f,
                    dy3 = -0.5f,
                )
                close()
                // M 2.35 2.8
                moveTo(x = 2.35f, y = 2.8f)
                // a 0.25 0.25 0 0 1 0.35 0.05
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.35f,
                    dy1 = 0.05f,
                )
                // l 0.14 0.186
                lineToRelative(dx = 0.14f, dy = 0.186f)
                // a 0.818 0.818 0 0 1 -0.201 1.172
                arcToRelative(
                    a = 0.818f,
                    b = 0.818f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.201f,
                    dy1 = 1.172f,
                )
                // a 0.318 0.318 0 0 0 -0.078 0.456
                arcToRelative(
                    a = 0.318f,
                    b = 0.318f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.078f,
                    dy1 = 0.456f,
                )
                // l 0.139 0.186
                lineToRelative(dx = 0.139f, dy = 0.186f)
                // a 0.25 0.25 0 1 1 -0.4 0.3
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.4f,
                    dy1 = 0.3f,
                )
                // l -0.14 -0.186
                lineToRelative(dx = -0.14f, dy = -0.186f)
                // a 0.818 0.818 0 0 1 0.201 -1.172
                arcToRelative(
                    a = 0.818f,
                    b = 0.818f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.201f,
                    dy1 = -1.172f,
                )
                // a 0.318 0.318 0 0 0 0.078 -0.456
                arcToRelative(
                    a = 0.318f,
                    b = 0.318f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.078f,
                    dy1 = -0.456f,
                )
                // L 2.3 3.15
                lineTo(x = 2.3f, y = 3.15f)
                // a 0.25 0.25 0 0 1 0.05 -0.35z
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.05f,
                    dy1 = -0.35f,
                )
                close()
                // m 2 0
                moveToRelative(dx = 2.0f, dy = 0.0f)
                // a 0.25 0.25 0 0 1 0.35 0.05
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.35f,
                    dy1 = 0.05f,
                )
                // l 0.14 0.186
                lineToRelative(dx = 0.14f, dy = 0.186f)
                // a 0.818 0.818 0 0 1 -0.201 1.172
                arcToRelative(
                    a = 0.818f,
                    b = 0.818f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.201f,
                    dy1 = 1.172f,
                )
                // a 0.318 0.318 0 0 0 -0.078 0.456
                arcToRelative(
                    a = 0.318f,
                    b = 0.318f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.078f,
                    dy1 = 0.456f,
                )
                // l 0.139 0.186
                lineToRelative(dx = 0.139f, dy = 0.186f)
                // a 0.25 0.25 0 1 1 -0.4 0.3
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.4f,
                    dy1 = 0.3f,
                )
                // l -0.14 -0.186
                lineToRelative(dx = -0.14f, dy = -0.186f)
                // a 0.818 0.818 0 0 1 0.201 -1.172
                arcToRelative(
                    a = 0.818f,
                    b = 0.818f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.201f,
                    dy1 = -1.172f,
                )
                // a 0.318 0.318 0 0 0 0.078 -0.456
                arcToRelative(
                    a = 0.318f,
                    b = 0.318f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.078f,
                    dy1 = -0.456f,
                )
                // L 4.3 3.15
                lineTo(x = 4.3f, y = 3.15f)
                // a 0.25 0.25 0 0 1 0.05 -0.35z
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.05f,
                    dy1 = -0.35f,
                )
                close()
                // m 2.35 0.05
                moveToRelative(dx = 2.35f, dy = 0.05f)
                // a 0.25 0.25 0 0 0 -0.4 0.3
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.4f,
                    dy1 = 0.3f,
                )
                // l 0.14 0.186
                lineToRelative(dx = 0.14f, dy = 0.186f)
                // a 0.318 0.318 0 0 1 -0.079 0.456
                arcToRelative(
                    a = 0.318f,
                    b = 0.318f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.079f,
                    dy1 = 0.456f,
                )
                // a 0.818 0.818 0 0 0 -0.2 1.172
                arcToRelative(
                    a = 0.818f,
                    b = 0.818f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.2f,
                    dy1 = 1.172f,
                )
                // l 0.139 0.186
                lineToRelative(dx = 0.139f, dy = 0.186f)
                // a 0.25 0.25 0 1 0 0.4 -0.3
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.4f,
                    dy1 = -0.3f,
                )
                // l -0.14 -0.186
                lineToRelative(dx = -0.14f, dy = -0.186f)
                // a 0.318 0.318 0 0 1 0.079 -0.456
                arcToRelative(
                    a = 0.318f,
                    b = 0.318f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.079f,
                    dy1 = -0.456f,
                )
                // a 0.818 0.818 0 0 0 0.2 -1.172
                arcToRelative(
                    a = 0.818f,
                    b = 0.818f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.2f,
                    dy1 = -1.172f,
                )
                // L 6.7 2.85z
                lineTo(x = 6.7f, y = 2.85f)
                close()
            }
        }.build().also { _ic1066 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1066: ImageVector? = null
