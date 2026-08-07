package com.wanbaohe.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.TombSweeping: ImageVector
    get() {
        val current = _tombSweeping
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.TombSweeping",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M3 13 a9 9 0 0 0 9 9 a9 9 0 0 0 -9 -9 m2.44 2.44 c1.91 .71 3.41 2.21 4.12 4.12 a7 7 0 0 1 -4.12 -4.12 M12 22 a9 9 0 0 0 9 -9 a9 9 0 0 0 -9 9 m2.42 -2.43 a6.88 6.88 0 0 1 4.15 -4.15 a7.03 7.03 0 0 1 -4.15 4.15 M12 14 a6 6 0 0 0 6 -6 V3 a5.9 5.9 0 0 0 -2.16 .39 c-.55 .23 -1.04 .57 -1.45 1 L12 2 L9.61 4.39 c-.41 -.43 -.9 -.77 -1.45 -1 A5.9 5.9 0 0 0 6 3 v5 a6 6 0 0 0 6 6 M8 5.61 l1.57 1.65 L12 4.83 l2.43 2.43 L16 5.61 V8 a4 4 0 0 1 -4 4 a4 4 0 0 1 -4 -4z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 3 13
                moveTo(x = 3.0f, y = 13.0f)
                // a 9 9 0 0 0 9 9
                arcToRelative(
                    a = 9.0f,
                    b = 9.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 9.0f,
                    dy1 = 9.0f,
                )
                // a 9 9 0 0 0 -9 -9
                arcToRelative(
                    a = 9.0f,
                    b = 9.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -9.0f,
                    dy1 = -9.0f,
                )
                // m 2.44 2.44
                moveToRelative(dx = 2.44f, dy = 2.44f)
                // c 1.91 0.71 3.41 2.21 4.12 4.12
                curveToRelative(
                    dx1 = 1.91f,
                    dy1 = 0.71f,
                    dx2 = 3.41f,
                    dy2 = 2.21f,
                    dx3 = 4.12f,
                    dy3 = 4.12f,
                )
                // a 7 7 0 0 1 -4.12 -4.12
                arcToRelative(
                    a = 7.0f,
                    b = 7.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -4.12f,
                    dy1 = -4.12f,
                )
                // M 12 22
                moveTo(x = 12.0f, y = 22.0f)
                // a 9 9 0 0 0 9 -9
                arcToRelative(
                    a = 9.0f,
                    b = 9.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 9.0f,
                    dy1 = -9.0f,
                )
                // a 9 9 0 0 0 -9 9
                arcToRelative(
                    a = 9.0f,
                    b = 9.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -9.0f,
                    dy1 = 9.0f,
                )
                // m 2.42 -2.43
                moveToRelative(dx = 2.42f, dy = -2.43f)
                // a 6.88 6.88 0 0 1 4.15 -4.15
                arcToRelative(
                    a = 6.88f,
                    b = 6.88f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 4.15f,
                    dy1 = -4.15f,
                )
                // a 7.03 7.03 0 0 1 -4.15 4.15
                arcToRelative(
                    a = 7.03f,
                    b = 7.03f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -4.15f,
                    dy1 = 4.15f,
                )
                // M 12 14
                moveTo(x = 12.0f, y = 14.0f)
                // a 6 6 0 0 0 6 -6
                arcToRelative(
                    a = 6.0f,
                    b = 6.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 6.0f,
                    dy1 = -6.0f,
                )
                // V 3
                verticalLineTo(y = 3.0f)
                // a 5.9 5.9 0 0 0 -2.16 0.39
                arcToRelative(
                    a = 5.9f,
                    b = 5.9f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.16f,
                    dy1 = 0.39f,
                )
                // c -0.55 0.23 -1.04 0.57 -1.45 1
                curveToRelative(
                    dx1 = -0.55f,
                    dy1 = 0.23f,
                    dx2 = -1.04f,
                    dy2 = 0.57f,
                    dx3 = -1.45f,
                    dy3 = 1.0f,
                )
                // L 12 2
                lineTo(x = 12.0f, y = 2.0f)
                // L 9.61 4.39
                lineTo(x = 9.61f, y = 4.39f)
                // c -0.41 -0.43 -0.9 -0.77 -1.45 -1
                curveToRelative(
                    dx1 = -0.41f,
                    dy1 = -0.43f,
                    dx2 = -0.9f,
                    dy2 = -0.77f,
                    dx3 = -1.45f,
                    dy3 = -1.0f,
                )
                // A 5.9 5.9 0 0 0 6 3
                arcTo(
                    horizontalEllipseRadius = 5.9f,
                    verticalEllipseRadius = 5.9f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 6.0f,
                    y1 = 3.0f,
                )
                // v 5
                verticalLineToRelative(dy = 5.0f)
                // a 6 6 0 0 0 6 6
                arcToRelative(
                    a = 6.0f,
                    b = 6.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 6.0f,
                    dy1 = 6.0f,
                )
                // M 8 5.61
                moveTo(x = 8.0f, y = 5.61f)
                // l 1.57 1.65
                lineToRelative(dx = 1.57f, dy = 1.65f)
                // L 12 4.83
                lineTo(x = 12.0f, y = 4.83f)
                // l 2.43 2.43
                lineToRelative(dx = 2.43f, dy = 2.43f)
                // L 16 5.61
                lineTo(x = 16.0f, y = 5.61f)
                // V 8
                verticalLineTo(y = 8.0f)
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
                // a 4 4 0 0 1 -4 -4z
                arcToRelative(
                    a = 4.0f,
                    b = 4.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -4.0f,
                    dy1 = -4.0f,
                )
                close()
            }
        }.build().also { _tombSweeping = it }
    }

@Suppress("ObjectPropertyName")
private var _tombSweeping: ImageVector? = null
