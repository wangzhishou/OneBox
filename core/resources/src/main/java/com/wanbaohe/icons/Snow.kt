package com.wanbaohe.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.Snow: ImageVector
    get() {
        val current = _snow
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Snow",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M6 14 a1 1 0 0 1 1 1 a1 1 0 0 1 -1 1 a5 5 0 0 1 -5 -5 a5 5 0 0 1 5 -5 c1 -2.35 3.3 -4 6 -4 c3.43 0 6.24 2.66 6.5 6.03 L19 8 a4 4 0 0 1 4 4 a4 4 0 0 1 -4 4 h-1 a1 1 0 0 1 -1 -1 a1 1 0 0 1 1 -1 h1 a2 2 0 0 0 2 -2 a2 2 0 0 0 -2 -2 h-2 V9 a5 5 0 0 0 -5 -5 C9.5 4 7.45 5.82 7.06 8.19 C6.73 8.07 6.37 8 6 8 a3 3 0 0 0 -3 3 a3 3 0 0 0 3 3 m1.88 4.07 l2.19 -.57 l-1.61 -1.62 c-.39 -.38 -.39 -1.02 0 -1.42 c.39 -.39 1.04 -.39 1.42 0 l1.62 1.61 l.57 -2.19 a1 1 0 1 1 1.93 .52 l-.59 2.19 L15.6 16 a1 1 0 1 1 .52 1.93 l-2.19 .57 l1.61 1.62 c.39 .38 .39 1.03 0 1.42 s-1.04 .39 -1.42 0 l-1.62 -1.61 l-.57 2.19 A1 1 0 1 1 10 21.6 l.59 -2.19 L8.4 20 a1 1 0 1 1 -.52 -1.93
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6 14
                moveTo(x = 6.0f, y = 14.0f)
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
                // a 1 1 0 0 1 -1 1
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 1.0f,
                )
                // a 5 5 0 0 1 -5 -5
                arcToRelative(
                    a = 5.0f,
                    b = 5.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -5.0f,
                    dy1 = -5.0f,
                )
                // a 5 5 0 0 1 5 -5
                arcToRelative(
                    a = 5.0f,
                    b = 5.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 5.0f,
                    dy1 = -5.0f,
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
                // L 19 8
                lineTo(x = 19.0f, y = 8.0f)
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
                // a 4 4 0 0 1 -4 4
                arcToRelative(
                    a = 4.0f,
                    b = 4.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -4.0f,
                    dy1 = 4.0f,
                )
                // h -1
                horizontalLineToRelative(dx = -1.0f)
                // a 1 1 0 0 1 -1 -1
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = -1.0f,
                )
                // a 1 1 0 0 1 1 -1
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = -1.0f,
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
                // V 9
                verticalLineTo(y = 9.0f)
                // a 5 5 0 0 0 -5 -5
                arcToRelative(
                    a = 5.0f,
                    b = 5.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -5.0f,
                    dy1 = -5.0f,
                )
                // C 9.5 4 7.45 5.82 7.06 8.19
                curveTo(
                    x1 = 9.5f,
                    y1 = 4.0f,
                    x2 = 7.45f,
                    y2 = 5.82f,
                    x3 = 7.06f,
                    y3 = 8.19f,
                )
                // C 6.73 8.07 6.37 8 6 8
                curveTo(
                    x1 = 6.73f,
                    y1 = 8.07f,
                    x2 = 6.37f,
                    y2 = 8.0f,
                    x3 = 6.0f,
                    y3 = 8.0f,
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
                // m 1.88 4.07
                moveToRelative(dx = 1.88f, dy = 4.07f)
                // l 2.19 -0.57
                lineToRelative(dx = 2.19f, dy = -0.57f)
                // l -1.61 -1.62
                lineToRelative(dx = -1.61f, dy = -1.62f)
                // c -0.39 -0.38 -0.39 -1.02 0 -1.42
                curveToRelative(
                    dx1 = -0.39f,
                    dy1 = -0.38f,
                    dx2 = -0.39f,
                    dy2 = -1.02f,
                    dx3 = 0.0f,
                    dy3 = -1.42f,
                )
                // c 0.39 -0.39 1.04 -0.39 1.42 0
                curveToRelative(
                    dx1 = 0.39f,
                    dy1 = -0.39f,
                    dx2 = 1.04f,
                    dy2 = -0.39f,
                    dx3 = 1.42f,
                    dy3 = 0.0f,
                )
                // l 1.62 1.61
                lineToRelative(dx = 1.62f, dy = 1.61f)
                // l 0.57 -2.19
                lineToRelative(dx = 0.57f, dy = -2.19f)
                // a 1 1 0 1 1 1.93 0.52
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 1.93f,
                    dy1 = 0.52f,
                )
                // l -0.59 2.19
                lineToRelative(dx = -0.59f, dy = 2.19f)
                // L 15.6 16
                lineTo(x = 15.6f, y = 16.0f)
                // a 1 1 0 1 1 0.52 1.93
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.52f,
                    dy1 = 1.93f,
                )
                // l -2.19 0.57
                lineToRelative(dx = -2.19f, dy = 0.57f)
                // l 1.61 1.62
                lineToRelative(dx = 1.61f, dy = 1.62f)
                // c 0.39 0.38 0.39 1.03 0 1.42
                curveToRelative(
                    dx1 = 0.39f,
                    dy1 = 0.38f,
                    dx2 = 0.39f,
                    dy2 = 1.03f,
                    dx3 = 0.0f,
                    dy3 = 1.42f,
                )
                // s -1.04 0.39 -1.42 0
                reflectiveCurveToRelative(
                    dx1 = -1.04f,
                    dy1 = 0.39f,
                    dx2 = -1.42f,
                    dy2 = 0.0f,
                )
                // l -1.62 -1.61
                lineToRelative(dx = -1.62f, dy = -1.61f)
                // l -0.57 2.19
                lineToRelative(dx = -0.57f, dy = 2.19f)
                // A 1 1 0 1 1 10 21.6
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 10.0f,
                    y1 = 21.6f,
                )
                // l 0.59 -2.19
                lineToRelative(dx = 0.59f, dy = -2.19f)
                // L 8.4 20
                lineTo(x = 8.4f, y = 20.0f)
                // a 1 1 0 1 1 -0.52 -1.93
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.52f,
                    dy1 = -1.93f,
                )
            }
        }.build().also { _snow = it }
    }

@Suppress("ObjectPropertyName")
private var _snow: ImageVector? = null
