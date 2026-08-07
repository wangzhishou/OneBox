package com.wanbaohe.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.Rain: ImageVector
    get() {
        val current = _rain
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Rain",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M6 14.03 a1 1 0 0 1 1 1 c0 .55 -.45 1 -1 1 c-2.76 0 -5 -2.24 -5 -5 s2.24 -5 5 -5 c1 -2.35 3.3 -4 6 -4 c3.43 0 6.24 2.66 6.5 6.03 l.5 -.03 a4 4 0 0 1 4 4 c0 2.2 -1.79 4 -4 4 h-1 c-.55 0 -1 -.45 -1 -1 c0 -.56 .45 -1 1 -1 h1 a2 2 0 0 0 2 -2 a2 2 0 0 0 -2 -2 h-2 v-1 c0 -2.76 -2.24 -5 -5 -5 c-2.5 0 -4.55 1.81 -4.94 4.18 c-.33 -.12 -.69 -.18 -1.06 -.18 a3 3 0 0 0 -3 3 a3 3 0 0 0 3 3 m6 .12 c.18 .24 .37 .51 .56 .79 C13 15.56 14 17.03 14 18 a2 2 0 0 1 -2 2 a2 2 0 0 1 -2 -2 c0 -.97 1 -2.44 1.44 -3.06 c.19 -.28 .38 -.54 .56 -.79 m0 -3.12 l-.5 .56 s-.85 .96 -1.71 2.22 C8.93 15.06 8 16.56 8 18 a4 4 0 0 0 4 4 a4 4 0 0 0 4 -4 c0 -1.44 -.93 -2.94 -1.79 -4.19 c-.86 -1.26 -1.71 -2.22 -1.71 -2.22
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6 14.03
                moveTo(x = 6.0f, y = 14.03f)
                // a 1 1 0 0 1 1 1
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 1.0f,
                )
                // c 0 0.55 -0.45 1 -1 1
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.55f,
                    dx2 = -0.45f,
                    dy2 = 1.0f,
                    dx3 = -1.0f,
                    dy3 = 1.0f,
                )
                // c -2.76 0 -5 -2.24 -5 -5
                curveToRelative(
                    dx1 = -2.76f,
                    dy1 = 0.0f,
                    dx2 = -5.0f,
                    dy2 = -2.24f,
                    dx3 = -5.0f,
                    dy3 = -5.0f,
                )
                // s 2.24 -5 5 -5
                reflectiveCurveToRelative(
                    dx1 = 2.24f,
                    dy1 = -5.0f,
                    dx2 = 5.0f,
                    dy2 = -5.0f,
                )
                // c 1 -2.35 3.3 -4 6 -4
                curveToRelative(
                    dx1 = 1.0f,
                    dy1 = -2.35f,
                    dx2 = 3.3f,
                    dy2 = -4.0f,
                    dx3 = 6.0f,
                    dy3 = -4.0f,
                )
                // c 3.43 0 6.24 2.66 6.5 6.03
                curveToRelative(
                    dx1 = 3.43f,
                    dy1 = 0.0f,
                    dx2 = 6.24f,
                    dy2 = 2.66f,
                    dx3 = 6.5f,
                    dy3 = 6.03f,
                )
                // l 0.5 -0.03
                lineToRelative(dx = 0.5f, dy = -0.03f)
                // a 4 4 0 0 1 4 4
                arcToRelative(
                    a = 4.0f,
                    b = 4.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 4.0f,
                    dy1 = 4.0f,
                )
                // c 0 2.2 -1.79 4 -4 4
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 2.2f,
                    dx2 = -1.79f,
                    dy2 = 4.0f,
                    dx3 = -4.0f,
                    dy3 = 4.0f,
                )
                // h -1
                horizontalLineToRelative(dx = -1.0f)
                // c -0.55 0 -1 -0.45 -1 -1
                curveToRelative(
                    dx1 = -0.55f,
                    dy1 = 0.0f,
                    dx2 = -1.0f,
                    dy2 = -0.45f,
                    dx3 = -1.0f,
                    dy3 = -1.0f,
                )
                // c 0 -0.56 0.45 -1 1 -1
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.56f,
                    dx2 = 0.45f,
                    dy2 = -1.0f,
                    dx3 = 1.0f,
                    dy3 = -1.0f,
                )
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // a 2 2 0 0 0 2 -2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.0f,
                    dy1 = -2.0f,
                )
                // a 2 2 0 0 0 -2 -2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.0f,
                    dy1 = -2.0f,
                )
                // h -2
                horizontalLineToRelative(dx = -2.0f)
                // v -1
                verticalLineToRelative(dy = -1.0f)
                // c 0 -2.76 -2.24 -5 -5 -5
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -2.76f,
                    dx2 = -2.24f,
                    dy2 = -5.0f,
                    dx3 = -5.0f,
                    dy3 = -5.0f,
                )
                // c -2.5 0 -4.55 1.81 -4.94 4.18
                curveToRelative(
                    dx1 = -2.5f,
                    dy1 = 0.0f,
                    dx2 = -4.55f,
                    dy2 = 1.81f,
                    dx3 = -4.94f,
                    dy3 = 4.18f,
                )
                // c -0.33 -0.12 -0.69 -0.18 -1.06 -0.18
                curveToRelative(
                    dx1 = -0.33f,
                    dy1 = -0.12f,
                    dx2 = -0.69f,
                    dy2 = -0.18f,
                    dx3 = -1.06f,
                    dy3 = -0.18f,
                )
                // a 3 3 0 0 0 -3 3
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -3.0f,
                    dy1 = 3.0f,
                )
                // a 3 3 0 0 0 3 3
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 3.0f,
                    dy1 = 3.0f,
                )
                // m 6 0.12
                moveToRelative(dx = 6.0f, dy = 0.12f)
                // c 0.18 0.24 0.37 0.51 0.56 0.79
                curveToRelative(
                    dx1 = 0.18f,
                    dy1 = 0.24f,
                    dx2 = 0.37f,
                    dy2 = 0.51f,
                    dx3 = 0.56f,
                    dy3 = 0.79f,
                )
                // C 13 15.56 14 17.03 14 18
                curveTo(
                    x1 = 13.0f,
                    y1 = 15.56f,
                    x2 = 14.0f,
                    y2 = 17.03f,
                    x3 = 14.0f,
                    y3 = 18.0f,
                )
                // a 2 2 0 0 1 -2 2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.0f,
                    dy1 = 2.0f,
                )
                // a 2 2 0 0 1 -2 -2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.0f,
                    dy1 = -2.0f,
                )
                // c 0 -0.97 1 -2.44 1.44 -3.06
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.97f,
                    dx2 = 1.0f,
                    dy2 = -2.44f,
                    dx3 = 1.44f,
                    dy3 = -3.06f,
                )
                // c 0.19 -0.28 0.38 -0.54 0.56 -0.79
                curveToRelative(
                    dx1 = 0.19f,
                    dy1 = -0.28f,
                    dx2 = 0.38f,
                    dy2 = -0.54f,
                    dx3 = 0.56f,
                    dy3 = -0.79f,
                )
                // m 0 -3.12
                moveToRelative(dx = 0.0f, dy = -3.12f)
                // l -0.5 0.56
                lineToRelative(dx = -0.5f, dy = 0.56f)
                // s -0.85 0.96 -1.71 2.22
                reflectiveCurveToRelative(
                    dx1 = -0.85f,
                    dy1 = 0.96f,
                    dx2 = -1.71f,
                    dy2 = 2.22f,
                )
                // C 8.93 15.06 8 16.56 8 18
                curveTo(
                    x1 = 8.93f,
                    y1 = 15.06f,
                    x2 = 8.0f,
                    y2 = 16.56f,
                    x3 = 8.0f,
                    y3 = 18.0f,
                )
                // a 4 4 0 0 0 4 4
                arcToRelative(
                    a = 4.0f,
                    b = 4.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 4.0f,
                    dy1 = 4.0f,
                )
                // a 4 4 0 0 0 4 -4
                arcToRelative(
                    a = 4.0f,
                    b = 4.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 4.0f,
                    dy1 = -4.0f,
                )
                // c 0 -1.44 -0.93 -2.94 -1.79 -4.19
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.44f,
                    dx2 = -0.93f,
                    dy2 = -2.94f,
                    dx3 = -1.79f,
                    dy3 = -4.19f,
                )
                // c -0.86 -1.26 -1.71 -2.22 -1.71 -2.22
                curveToRelative(
                    dx1 = -0.86f,
                    dy1 = -1.26f,
                    dx2 = -1.71f,
                    dy2 = -2.22f,
                    dx3 = -1.71f,
                    dy3 = -2.22f,
                )
            }
        }.build().also { _rain = it }
    }

@Suppress("ObjectPropertyName")
private var _rain: ImageVector? = null
