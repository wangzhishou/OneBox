package com.wanbaohe.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.Autumn: ImageVector
    get() {
        val current = _autumn
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Autumn",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M17 8 C8 10 5.9 16.17 3.82 21.34 l1.89 .66 l.95 -2.3 c.48 .17 .98 .3 1.34 .3 C19 20 22 3 22 3 c-1 2 -8 2.25 -13 3.25 S2 11.5 2 13.5 s1.75 3.75 1.75 3.75 C7 8 17 8 17 8
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 17 8
                moveTo(x = 17.0f, y = 8.0f)
                // C 8 10 5.9 16.17 3.82 21.34
                curveTo(
                    x1 = 8.0f,
                    y1 = 10.0f,
                    x2 = 5.9f,
                    y2 = 16.17f,
                    x3 = 3.82f,
                    y3 = 21.34f,
                )
                // l 1.89 0.66
                lineToRelative(dx = 1.89f, dy = 0.66f)
                // l 0.95 -2.3
                lineToRelative(dx = 0.95f, dy = -2.3f)
                // c 0.48 0.17 0.98 0.3 1.34 0.3
                curveToRelative(
                    dx1 = 0.48f,
                    dy1 = 0.17f,
                    dx2 = 0.98f,
                    dy2 = 0.3f,
                    dx3 = 1.34f,
                    dy3 = 0.3f,
                )
                // C 19 20 22 3 22 3
                curveTo(
                    x1 = 19.0f,
                    y1 = 20.0f,
                    x2 = 22.0f,
                    y2 = 3.0f,
                    x3 = 22.0f,
                    y3 = 3.0f,
                )
                // c -1 2 -8 2.25 -13 3.25
                curveToRelative(
                    dx1 = -1.0f,
                    dy1 = 2.0f,
                    dx2 = -8.0f,
                    dy2 = 2.25f,
                    dx3 = -13.0f,
                    dy3 = 3.25f,
                )
                // S 2 11.5 2 13.5
                reflectiveCurveTo(
                    x1 = 2.0f,
                    y1 = 11.5f,
                    x2 = 2.0f,
                    y2 = 13.5f,
                )
                // s 1.75 3.75 1.75 3.75
                reflectiveCurveToRelative(
                    dx1 = 1.75f,
                    dy1 = 3.75f,
                    dx2 = 1.75f,
                    dy2 = 3.75f,
                )
                // C 7 8 17 8 17 8
                curveTo(
                    x1 = 7.0f,
                    y1 = 8.0f,
                    x2 = 17.0f,
                    y2 = 8.0f,
                    x3 = 17.0f,
                    y3 = 8.0f,
                )
            }
        }.build().also { _autumn = it }
    }

@Suppress("ObjectPropertyName")
private var _autumn: ImageVector? = null
