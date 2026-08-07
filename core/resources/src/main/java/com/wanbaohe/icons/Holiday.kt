package com.wanbaohe.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.Holiday: ImageVector
    get() {
        val current = _holiday
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Holiday",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M19 19 H5 V8 h14 m-3 -7 v2 H8 V1 H6 v2 H5 c-1.1 0 -2 .9 -2 2 v14 a2 2 0 0 0 2 2 h14 c1.11 0 2 -.89 2 -2 V5 a2 2 0 0 0 -2 -2 h-1 V1 m-7.12 11 H7.27 l2.92 2.11 l-1.11 3.45 L12 15.43 l2.92 2.13 l-1.12 -3.44 L16.72 12 h-3.6 L12 8.56z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 19 19
                moveTo(x = 19.0f, y = 19.0f)
                // H 5
                horizontalLineTo(x = 5.0f)
                // V 8
                verticalLineTo(y = 8.0f)
                // h 14
                horizontalLineToRelative(dx = 14.0f)
                // m -3 -7
                moveToRelative(dx = -3.0f, dy = -7.0f)
                // v 2
                verticalLineToRelative(dy = 2.0f)
                // H 8
                horizontalLineTo(x = 8.0f)
                // V 1
                verticalLineTo(y = 1.0f)
                // H 6
                horizontalLineTo(x = 6.0f)
                // v 2
                verticalLineToRelative(dy = 2.0f)
                // H 5
                horizontalLineTo(x = 5.0f)
                // c -1.1 0 -2 0.9 -2 2
                curveToRelative(
                    dx1 = -1.1f,
                    dy1 = 0.0f,
                    dx2 = -2.0f,
                    dy2 = 0.9f,
                    dx3 = -2.0f,
                    dy3 = 2.0f,
                )
                // v 14
                verticalLineToRelative(dy = 14.0f)
                // a 2 2 0 0 0 2 2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.0f,
                    dy1 = 2.0f,
                )
                // h 14
                horizontalLineToRelative(dx = 14.0f)
                // c 1.11 0 2 -0.89 2 -2
                curveToRelative(
                    dx1 = 1.11f,
                    dy1 = 0.0f,
                    dx2 = 2.0f,
                    dy2 = -0.89f,
                    dx3 = 2.0f,
                    dy3 = -2.0f,
                )
                // V 5
                verticalLineTo(y = 5.0f)
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
                // h -1
                horizontalLineToRelative(dx = -1.0f)
                // V 1
                verticalLineTo(y = 1.0f)
                // m -7.12 11
                moveToRelative(dx = -7.12f, dy = 11.0f)
                // H 7.27
                horizontalLineTo(x = 7.27f)
                // l 2.92 2.11
                lineToRelative(dx = 2.92f, dy = 2.11f)
                // l -1.11 3.45
                lineToRelative(dx = -1.11f, dy = 3.45f)
                // L 12 15.43
                lineTo(x = 12.0f, y = 15.43f)
                // l 2.92 2.13
                lineToRelative(dx = 2.92f, dy = 2.13f)
                // l -1.12 -3.44
                lineToRelative(dx = -1.12f, dy = -3.44f)
                // L 16.72 12
                lineTo(x = 16.72f, y = 12.0f)
                // h -3.6
                horizontalLineToRelative(dx = -3.6f)
                // L 12 8.56z
                lineTo(x = 12.0f, y = 8.56f)
                close()
            }
        }.build().also { _holiday = it }
    }

@Suppress("ObjectPropertyName")
private var _holiday: ImageVector? = null
