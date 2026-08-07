package com.wanbaohe.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.Spring: ImageVector
    get() {
        val current = _spring
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Spring",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M8.66 13.07 c-1.74 0 -3.16 -1.41 -3.16 -3.14 c0 -.71 .26 -1.39 .69 -1.93 c-.42 -.54 -.69 -1.22 -.69 -1.93 c0 -1.73 1.43 -3.14 3.16 -3.14 l.43 .03 A3.15 3.15 0 0 1 12 1 c1.31 0 2.44 .81 2.91 1.96 l.43 -.03 c1.73 0 3.16 1.41 3.16 3.14 c0 .71 -.26 1.39 -.69 1.93 c.42 .54 .69 1.22 .69 1.93 c0 1.73 -1.43 3.14 -3.16 3.14 l-.43 -.03 A3.15 3.15 0 0 1 12 15 a3.15 3.15 0 0 1 -2.91 -1.96z M12 13 c.62 0 1.12 -.5 1.14 -1.1 l-.11 -1.09 c-.32 .12 -.67 .19 -1.03 .19 s-.7 -.07 -1 -.19 l-.14 1.09 c.02 .6 .52 1.1 1.14 1.1 m3.34 -1.93 c.63 0 1.16 -.51 1.16 -1.15 c0 -.42 -.27 -.84 -.67 -1.03 l-.88 -.42 c-.12 .74 -.51 1.38 -1.06 1.83 l.81 .57 c.18 .13 .4 .2 .64 .2 m-.65 -5.94 l-.82 .56 c.56 .45 .95 1.09 1.07 1.81 l.88 -.4 c.41 -.2 .68 -.6 .68 -1.03 c0 -.63 -.53 -1.14 -1.16 -1.14 c-.23 0 -.45 .07 -.65 .2 M12 3 c-.62 0 -1.12 .5 -1.14 1.1 L11 5.19 c.3 -.12 .64 -.19 1 -.19 s.71 .07 1.03 .19 l.11 -1.09 C13.12 3.5 12.62 3 12 3 M8.66 4.93 c-.63 0 -1.16 .51 -1.16 1.14 c0 .43 .27 .83 .67 1.03 l.88 .4 c.12 -.72 .51 -1.36 1.06 -1.81 l-.81 -.56 c-.18 -.13 -.4 -.2 -.64 -.2 M8.17 8.9 c-.4 .2 -.67 .6 -.67 1.02 c0 .63 .53 1.14 1.16 1.14 c.23 0 .45 -.06 .65 -.2 l.81 -.55 c-.56 -.45 -.95 -1.09 -1.07 -1.81z M12 22 a9 9 0 0 1 -9 -9 a9 9 0 0 1 9 9 a9 9 0 0 1 9 -9 a9 9 0 0 1 -9 9 m2.44 -2.44 a7.04 7.04 0 0 0 4.12 -4.12 c-1.9 .7 -3.41 2.22 -4.12 4.12 m-9 -4.12 a7.04 7.04 0 0 0 4.12 4.12 c-.7 -1.9 -2.22 -3.41 -4.12 -4.12
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.66 13.07
                moveTo(x = 8.66f, y = 13.07f)
                // c -1.74 0 -3.16 -1.41 -3.16 -3.14
                curveToRelative(
                    dx1 = -1.74f,
                    dy1 = 0.0f,
                    dx2 = -3.16f,
                    dy2 = -1.41f,
                    dx3 = -3.16f,
                    dy3 = -3.14f,
                )
                // c 0 -0.71 0.26 -1.39 0.69 -1.93
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.71f,
                    dx2 = 0.26f,
                    dy2 = -1.39f,
                    dx3 = 0.69f,
                    dy3 = -1.93f,
                )
                // c -0.42 -0.54 -0.69 -1.22 -0.69 -1.93
                curveToRelative(
                    dx1 = -0.42f,
                    dy1 = -0.54f,
                    dx2 = -0.69f,
                    dy2 = -1.22f,
                    dx3 = -0.69f,
                    dy3 = -1.93f,
                )
                // c 0 -1.73 1.43 -3.14 3.16 -3.14
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.73f,
                    dx2 = 1.43f,
                    dy2 = -3.14f,
                    dx3 = 3.16f,
                    dy3 = -3.14f,
                )
                // l 0.43 0.03
                lineToRelative(dx = 0.43f, dy = 0.03f)
                // A 3.15 3.15 0 0 1 12 1
                arcTo(
                    horizontalEllipseRadius = 3.15f,
                    verticalEllipseRadius = 3.15f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 12.0f,
                    y1 = 1.0f,
                )
                // c 1.31 0 2.44 0.81 2.91 1.96
                curveToRelative(
                    dx1 = 1.31f,
                    dy1 = 0.0f,
                    dx2 = 2.44f,
                    dy2 = 0.81f,
                    dx3 = 2.91f,
                    dy3 = 1.96f,
                )
                // l 0.43 -0.03
                lineToRelative(dx = 0.43f, dy = -0.03f)
                // c 1.73 0 3.16 1.41 3.16 3.14
                curveToRelative(
                    dx1 = 1.73f,
                    dy1 = 0.0f,
                    dx2 = 3.16f,
                    dy2 = 1.41f,
                    dx3 = 3.16f,
                    dy3 = 3.14f,
                )
                // c 0 0.71 -0.26 1.39 -0.69 1.93
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.71f,
                    dx2 = -0.26f,
                    dy2 = 1.39f,
                    dx3 = -0.69f,
                    dy3 = 1.93f,
                )
                // c 0.42 0.54 0.69 1.22 0.69 1.93
                curveToRelative(
                    dx1 = 0.42f,
                    dy1 = 0.54f,
                    dx2 = 0.69f,
                    dy2 = 1.22f,
                    dx3 = 0.69f,
                    dy3 = 1.93f,
                )
                // c 0 1.73 -1.43 3.14 -3.16 3.14
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.73f,
                    dx2 = -1.43f,
                    dy2 = 3.14f,
                    dx3 = -3.16f,
                    dy3 = 3.14f,
                )
                // l -0.43 -0.03
                lineToRelative(dx = -0.43f, dy = -0.03f)
                // A 3.15 3.15 0 0 1 12 15
                arcTo(
                    horizontalEllipseRadius = 3.15f,
                    verticalEllipseRadius = 3.15f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 12.0f,
                    y1 = 15.0f,
                )
                // a 3.15 3.15 0 0 1 -2.91 -1.96z
                arcToRelative(
                    a = 3.15f,
                    b = 3.15f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.91f,
                    dy1 = -1.96f,
                )
                close()
                // M 12 13
                moveTo(x = 12.0f, y = 13.0f)
                // c 0.62 0 1.12 -0.5 1.14 -1.1
                curveToRelative(
                    dx1 = 0.62f,
                    dy1 = 0.0f,
                    dx2 = 1.12f,
                    dy2 = -0.5f,
                    dx3 = 1.14f,
                    dy3 = -1.1f,
                )
                // l -0.11 -1.09
                lineToRelative(dx = -0.11f, dy = -1.09f)
                // c -0.32 0.12 -0.67 0.19 -1.03 0.19
                curveToRelative(
                    dx1 = -0.32f,
                    dy1 = 0.12f,
                    dx2 = -0.67f,
                    dy2 = 0.19f,
                    dx3 = -1.03f,
                    dy3 = 0.19f,
                )
                // s -0.7 -0.07 -1 -0.19
                reflectiveCurveToRelative(
                    dx1 = -0.7f,
                    dy1 = -0.07f,
                    dx2 = -1.0f,
                    dy2 = -0.19f,
                )
                // l -0.14 1.09
                lineToRelative(dx = -0.14f, dy = 1.09f)
                // c 0.02 0.6 0.52 1.1 1.14 1.1
                curveToRelative(
                    dx1 = 0.02f,
                    dy1 = 0.6f,
                    dx2 = 0.52f,
                    dy2 = 1.1f,
                    dx3 = 1.14f,
                    dy3 = 1.1f,
                )
                // m 3.34 -1.93
                moveToRelative(dx = 3.34f, dy = -1.93f)
                // c 0.63 0 1.16 -0.51 1.16 -1.15
                curveToRelative(
                    dx1 = 0.63f,
                    dy1 = 0.0f,
                    dx2 = 1.16f,
                    dy2 = -0.51f,
                    dx3 = 1.16f,
                    dy3 = -1.15f,
                )
                // c 0 -0.42 -0.27 -0.84 -0.67 -1.03
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.42f,
                    dx2 = -0.27f,
                    dy2 = -0.84f,
                    dx3 = -0.67f,
                    dy3 = -1.03f,
                )
                // l -0.88 -0.42
                lineToRelative(dx = -0.88f, dy = -0.42f)
                // c -0.12 0.74 -0.51 1.38 -1.06 1.83
                curveToRelative(
                    dx1 = -0.12f,
                    dy1 = 0.74f,
                    dx2 = -0.51f,
                    dy2 = 1.38f,
                    dx3 = -1.06f,
                    dy3 = 1.83f,
                )
                // l 0.81 0.57
                lineToRelative(dx = 0.81f, dy = 0.57f)
                // c 0.18 0.13 0.4 0.2 0.64 0.2
                curveToRelative(
                    dx1 = 0.18f,
                    dy1 = 0.13f,
                    dx2 = 0.4f,
                    dy2 = 0.2f,
                    dx3 = 0.64f,
                    dy3 = 0.2f,
                )
                // m -0.65 -5.94
                moveToRelative(dx = -0.65f, dy = -5.94f)
                // l -0.82 0.56
                lineToRelative(dx = -0.82f, dy = 0.56f)
                // c 0.56 0.45 0.95 1.09 1.07 1.81
                curveToRelative(
                    dx1 = 0.56f,
                    dy1 = 0.45f,
                    dx2 = 0.95f,
                    dy2 = 1.09f,
                    dx3 = 1.07f,
                    dy3 = 1.81f,
                )
                // l 0.88 -0.4
                lineToRelative(dx = 0.88f, dy = -0.4f)
                // c 0.41 -0.2 0.68 -0.6 0.68 -1.03
                curveToRelative(
                    dx1 = 0.41f,
                    dy1 = -0.2f,
                    dx2 = 0.68f,
                    dy2 = -0.6f,
                    dx3 = 0.68f,
                    dy3 = -1.03f,
                )
                // c 0 -0.63 -0.53 -1.14 -1.16 -1.14
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.63f,
                    dx2 = -0.53f,
                    dy2 = -1.14f,
                    dx3 = -1.16f,
                    dy3 = -1.14f,
                )
                // c -0.23 0 -0.45 0.07 -0.65 0.2
                curveToRelative(
                    dx1 = -0.23f,
                    dy1 = 0.0f,
                    dx2 = -0.45f,
                    dy2 = 0.07f,
                    dx3 = -0.65f,
                    dy3 = 0.2f,
                )
                // M 12 3
                moveTo(x = 12.0f, y = 3.0f)
                // c -0.62 0 -1.12 0.5 -1.14 1.1
                curveToRelative(
                    dx1 = -0.62f,
                    dy1 = 0.0f,
                    dx2 = -1.12f,
                    dy2 = 0.5f,
                    dx3 = -1.14f,
                    dy3 = 1.1f,
                )
                // L 11 5.19
                lineTo(x = 11.0f, y = 5.19f)
                // c 0.3 -0.12 0.64 -0.19 1 -0.19
                curveToRelative(
                    dx1 = 0.3f,
                    dy1 = -0.12f,
                    dx2 = 0.64f,
                    dy2 = -0.19f,
                    dx3 = 1.0f,
                    dy3 = -0.19f,
                )
                // s 0.71 0.07 1.03 0.19
                reflectiveCurveToRelative(
                    dx1 = 0.71f,
                    dy1 = 0.07f,
                    dx2 = 1.03f,
                    dy2 = 0.19f,
                )
                // l 0.11 -1.09
                lineToRelative(dx = 0.11f, dy = -1.09f)
                // C 13.12 3.5 12.62 3 12 3
                curveTo(
                    x1 = 13.12f,
                    y1 = 3.5f,
                    x2 = 12.62f,
                    y2 = 3.0f,
                    x3 = 12.0f,
                    y3 = 3.0f,
                )
                // M 8.66 4.93
                moveTo(x = 8.66f, y = 4.93f)
                // c -0.63 0 -1.16 0.51 -1.16 1.14
                curveToRelative(
                    dx1 = -0.63f,
                    dy1 = 0.0f,
                    dx2 = -1.16f,
                    dy2 = 0.51f,
                    dx3 = -1.16f,
                    dy3 = 1.14f,
                )
                // c 0 0.43 0.27 0.83 0.67 1.03
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.43f,
                    dx2 = 0.27f,
                    dy2 = 0.83f,
                    dx3 = 0.67f,
                    dy3 = 1.03f,
                )
                // l 0.88 0.4
                lineToRelative(dx = 0.88f, dy = 0.4f)
                // c 0.12 -0.72 0.51 -1.36 1.06 -1.81
                curveToRelative(
                    dx1 = 0.12f,
                    dy1 = -0.72f,
                    dx2 = 0.51f,
                    dy2 = -1.36f,
                    dx3 = 1.06f,
                    dy3 = -1.81f,
                )
                // l -0.81 -0.56
                lineToRelative(dx = -0.81f, dy = -0.56f)
                // c -0.18 -0.13 -0.4 -0.2 -0.64 -0.2
                curveToRelative(
                    dx1 = -0.18f,
                    dy1 = -0.13f,
                    dx2 = -0.4f,
                    dy2 = -0.2f,
                    dx3 = -0.64f,
                    dy3 = -0.2f,
                )
                // M 8.17 8.9
                moveTo(x = 8.17f, y = 8.9f)
                // c -0.4 0.2 -0.67 0.6 -0.67 1.02
                curveToRelative(
                    dx1 = -0.4f,
                    dy1 = 0.2f,
                    dx2 = -0.67f,
                    dy2 = 0.6f,
                    dx3 = -0.67f,
                    dy3 = 1.02f,
                )
                // c 0 0.63 0.53 1.14 1.16 1.14
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.63f,
                    dx2 = 0.53f,
                    dy2 = 1.14f,
                    dx3 = 1.16f,
                    dy3 = 1.14f,
                )
                // c 0.23 0 0.45 -0.06 0.65 -0.2
                curveToRelative(
                    dx1 = 0.23f,
                    dy1 = 0.0f,
                    dx2 = 0.45f,
                    dy2 = -0.06f,
                    dx3 = 0.65f,
                    dy3 = -0.2f,
                )
                // l 0.81 -0.55
                lineToRelative(dx = 0.81f, dy = -0.55f)
                // c -0.56 -0.45 -0.95 -1.09 -1.07 -1.81z
                curveToRelative(
                    dx1 = -0.56f,
                    dy1 = -0.45f,
                    dx2 = -0.95f,
                    dy2 = -1.09f,
                    dx3 = -1.07f,
                    dy3 = -1.81f,
                )
                close()
                // M 12 22
                moveTo(x = 12.0f, y = 22.0f)
                // a 9 9 0 0 1 -9 -9
                arcToRelative(
                    a = 9.0f,
                    b = 9.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -9.0f,
                    dy1 = -9.0f,
                )
                // a 9 9 0 0 1 9 9
                arcToRelative(
                    a = 9.0f,
                    b = 9.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 9.0f,
                    dy1 = 9.0f,
                )
                // a 9 9 0 0 1 9 -9
                arcToRelative(
                    a = 9.0f,
                    b = 9.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 9.0f,
                    dy1 = -9.0f,
                )
                // a 9 9 0 0 1 -9 9
                arcToRelative(
                    a = 9.0f,
                    b = 9.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -9.0f,
                    dy1 = 9.0f,
                )
                // m 2.44 -2.44
                moveToRelative(dx = 2.44f, dy = -2.44f)
                // a 7.04 7.04 0 0 0 4.12 -4.12
                arcToRelative(
                    a = 7.04f,
                    b = 7.04f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 4.12f,
                    dy1 = -4.12f,
                )
                // c -1.9 0.7 -3.41 2.22 -4.12 4.12
                curveToRelative(
                    dx1 = -1.9f,
                    dy1 = 0.7f,
                    dx2 = -3.41f,
                    dy2 = 2.22f,
                    dx3 = -4.12f,
                    dy3 = 4.12f,
                )
                // m -9 -4.12
                moveToRelative(dx = -9.0f, dy = -4.12f)
                // a 7.04 7.04 0 0 0 4.12 4.12
                arcToRelative(
                    a = 7.04f,
                    b = 7.04f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 4.12f,
                    dy1 = 4.12f,
                )
                // c -0.7 -1.9 -2.22 -3.41 -4.12 -4.12
                curveToRelative(
                    dx1 = -0.7f,
                    dy1 = -1.9f,
                    dx2 = -2.22f,
                    dy2 = -3.41f,
                    dx3 = -4.12f,
                    dy3 = -4.12f,
                )
            }
        }.build().also { _spring = it }
    }

@Suppress("ObjectPropertyName")
private var _spring: ImageVector? = null
