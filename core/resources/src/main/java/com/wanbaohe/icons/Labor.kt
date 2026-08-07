package com.wanbaohe.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.Labor: ImageVector
    get() {
        val current = _labor
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Labor",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // m13.78 15.3 l6 6 l2.11 -2.16 l-6 -6z m3.72 -5.2 c-.39 0 -.81 -.05 -1.14 -.19 L4.97 21.25 l-2.11 -2.11 l7.41 -7.4 L8.5 9.96 l-.72 .7 l-1.45 -1.41 v2.86 l-.7 .7 l-3.52 -3.56 l.7 -.7 h2.81 l-1.4 -1.41 l3.56 -3.56 a2.976 2.976 0 0 1 4.22 0 L9.89 5.74 l1.41 1.4 l-.71 .71 l1.79 1.78 l1.82 -1.88 c-.14 -.33 -.2 -.75 -.2 -1.12 a3.49 3.49 0 0 1 3.5 -3.52 c.59 0 1.11 .14 1.58 .42 L16.41 6.2 l1.5 1.5 l2.67 -2.67 c.28 .47 .42 .97 .42 1.6 c0 1.92 -1.55 3.47 -3.5 3.47
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 13.78 15.3
                moveTo(x = 13.78f, y = 15.3f)
                // l 6 6
                lineToRelative(dx = 6.0f, dy = 6.0f)
                // l 2.11 -2.16
                lineToRelative(dx = 2.11f, dy = -2.16f)
                // l -6 -6z
                lineToRelative(dx = -6.0f, dy = -6.0f)
                close()
                // m 3.72 -5.2
                moveToRelative(dx = 3.72f, dy = -5.2f)
                // c -0.39 0 -0.81 -0.05 -1.14 -0.19
                curveToRelative(
                    dx1 = -0.39f,
                    dy1 = 0.0f,
                    dx2 = -0.81f,
                    dy2 = -0.05f,
                    dx3 = -1.14f,
                    dy3 = -0.19f,
                )
                // L 4.97 21.25
                lineTo(x = 4.97f, y = 21.25f)
                // l -2.11 -2.11
                lineToRelative(dx = -2.11f, dy = -2.11f)
                // l 7.41 -7.4
                lineToRelative(dx = 7.41f, dy = -7.4f)
                // L 8.5 9.96
                lineTo(x = 8.5f, y = 9.96f)
                // l -0.72 0.7
                lineToRelative(dx = -0.72f, dy = 0.7f)
                // l -1.45 -1.41
                lineToRelative(dx = -1.45f, dy = -1.41f)
                // v 2.86
                verticalLineToRelative(dy = 2.86f)
                // l -0.7 0.7
                lineToRelative(dx = -0.7f, dy = 0.7f)
                // l -3.52 -3.56
                lineToRelative(dx = -3.52f, dy = -3.56f)
                // l 0.7 -0.7
                lineToRelative(dx = 0.7f, dy = -0.7f)
                // h 2.81
                horizontalLineToRelative(dx = 2.81f)
                // l -1.4 -1.41
                lineToRelative(dx = -1.4f, dy = -1.41f)
                // l 3.56 -3.56
                lineToRelative(dx = 3.56f, dy = -3.56f)
                // a 2.976 2.976 0 0 1 4.22 0
                arcToRelative(
                    a = 2.976f,
                    b = 2.976f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 4.22f,
                    dy1 = 0.0f,
                )
                // L 9.89 5.74
                lineTo(x = 9.89f, y = 5.74f)
                // l 1.41 1.4
                lineToRelative(dx = 1.41f, dy = 1.4f)
                // l -0.71 0.71
                lineToRelative(dx = -0.71f, dy = 0.71f)
                // l 1.79 1.78
                lineToRelative(dx = 1.79f, dy = 1.78f)
                // l 1.82 -1.88
                lineToRelative(dx = 1.82f, dy = -1.88f)
                // c -0.14 -0.33 -0.2 -0.75 -0.2 -1.12
                curveToRelative(
                    dx1 = -0.14f,
                    dy1 = -0.33f,
                    dx2 = -0.2f,
                    dy2 = -0.75f,
                    dx3 = -0.2f,
                    dy3 = -1.12f,
                )
                // a 3.49 3.49 0 0 1 3.5 -3.52
                arcToRelative(
                    a = 3.49f,
                    b = 3.49f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 3.5f,
                    dy1 = -3.52f,
                )
                // c 0.59 0 1.11 0.14 1.58 0.42
                curveToRelative(
                    dx1 = 0.59f,
                    dy1 = 0.0f,
                    dx2 = 1.11f,
                    dy2 = 0.14f,
                    dx3 = 1.58f,
                    dy3 = 0.42f,
                )
                // L 16.41 6.2
                lineTo(x = 16.41f, y = 6.2f)
                // l 1.5 1.5
                lineToRelative(dx = 1.5f, dy = 1.5f)
                // l 2.67 -2.67
                lineToRelative(dx = 2.67f, dy = -2.67f)
                // c 0.28 0.47 0.42 0.97 0.42 1.6
                curveToRelative(
                    dx1 = 0.28f,
                    dy1 = 0.47f,
                    dx2 = 0.42f,
                    dy2 = 0.97f,
                    dx3 = 0.42f,
                    dy3 = 1.6f,
                )
                // c 0 1.92 -1.55 3.47 -3.5 3.47
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.92f,
                    dx2 = -1.55f,
                    dy2 = 3.47f,
                    dx3 = -3.5f,
                    dy3 = 3.47f,
                )
            }
        }.build().also { _labor = it }
    }

@Suppress("ObjectPropertyName")
private var _labor: ImageVector? = null
