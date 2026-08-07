package com.wanbaohe.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.NewYear: ImageVector
    get() {
        val current = _newYear
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.NewYear",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // m14.53 1.45 l-1.08 1.08 l1.6 1.6 q.33 .375 .33 .87 c0 .495 -.11 .64 -.33 .86 L11.5 9.47 l1 1.08 l3.63 -3.61 c.53 -.59 .79 -1.24 .79 -1.94 s-.26 -1.36 -.79 -1.95z m-3.98 2.02 L9.47 4.55 l.61 .56 c.22 .22 .33 .52 .33 .89 s-.11 .67 -.33 .89 l-.61 .56 l1.08 1.08 l.56 -.61 c.53 -.59 .8 -1.23 .8 -1.92 c0 -.72 -.27 -1.37 -.8 -1.97z M21 5.06 c-.69 0 -1.33 .27 -1.92 .8 l-5.63 5.64 l1.08 1 l5.58 -5.56 c.25 -.25 .55 -.38 .89 -.38 s.64 .13 .89 .38 l.61 .61 l1.03 -1.08 l-.56 -.61 c-.59 -.53 -1.25 -.8 -1.97 -.8 M7 8 L2 22 l14 -5z m12 3.06 c-.7 0 -1.34 .27 -1.94 .8 l-1.59 1.59 l1.08 1.08 l1.59 -1.59 c.25 -.25 .53 -.38 .86 -.38 s.63 .13 .88 .38 l1.62 1.59 l1.05 -1.03 l-1.6 -1.64 c-.59 -.53 -1.25 -.8 -1.95 -.8
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 14.53 1.45
                moveTo(x = 14.53f, y = 1.45f)
                // l -1.08 1.08
                lineToRelative(dx = -1.08f, dy = 1.08f)
                // l 1.6 1.6
                lineToRelative(dx = 1.6f, dy = 1.6f)
                // q 0.33 0.375 0.33 0.87
                quadToRelative(
                    dx1 = 0.33f,
                    dy1 = 0.375f,
                    dx2 = 0.33f,
                    dy2 = 0.87f,
                )
                // c 0 0.495 -0.11 0.64 -0.33 0.86
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.495f,
                    dx2 = -0.11f,
                    dy2 = 0.64f,
                    dx3 = -0.33f,
                    dy3 = 0.86f,
                )
                // L 11.5 9.47
                lineTo(x = 11.5f, y = 9.47f)
                // l 1 1.08
                lineToRelative(dx = 1.0f, dy = 1.08f)
                // l 3.63 -3.61
                lineToRelative(dx = 3.63f, dy = -3.61f)
                // c 0.53 -0.59 0.79 -1.24 0.79 -1.94
                curveToRelative(
                    dx1 = 0.53f,
                    dy1 = -0.59f,
                    dx2 = 0.79f,
                    dy2 = -1.24f,
                    dx3 = 0.79f,
                    dy3 = -1.94f,
                )
                // s -0.26 -1.36 -0.79 -1.95z
                reflectiveCurveToRelative(
                    dx1 = -0.26f,
                    dy1 = -1.36f,
                    dx2 = -0.79f,
                    dy2 = -1.95f,
                )
                close()
                // m -3.98 2.02
                moveToRelative(dx = -3.98f, dy = 2.02f)
                // L 9.47 4.55
                lineTo(x = 9.47f, y = 4.55f)
                // l 0.61 0.56
                lineToRelative(dx = 0.61f, dy = 0.56f)
                // c 0.22 0.22 0.33 0.52 0.33 0.89
                curveToRelative(
                    dx1 = 0.22f,
                    dy1 = 0.22f,
                    dx2 = 0.33f,
                    dy2 = 0.52f,
                    dx3 = 0.33f,
                    dy3 = 0.89f,
                )
                // s -0.11 0.67 -0.33 0.89
                reflectiveCurveToRelative(
                    dx1 = -0.11f,
                    dy1 = 0.67f,
                    dx2 = -0.33f,
                    dy2 = 0.89f,
                )
                // l -0.61 0.56
                lineToRelative(dx = -0.61f, dy = 0.56f)
                // l 1.08 1.08
                lineToRelative(dx = 1.08f, dy = 1.08f)
                // l 0.56 -0.61
                lineToRelative(dx = 0.56f, dy = -0.61f)
                // c 0.53 -0.59 0.8 -1.23 0.8 -1.92
                curveToRelative(
                    dx1 = 0.53f,
                    dy1 = -0.59f,
                    dx2 = 0.8f,
                    dy2 = -1.23f,
                    dx3 = 0.8f,
                    dy3 = -1.92f,
                )
                // c 0 -0.72 -0.27 -1.37 -0.8 -1.97z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.72f,
                    dx2 = -0.27f,
                    dy2 = -1.37f,
                    dx3 = -0.8f,
                    dy3 = -1.97f,
                )
                close()
                // M 21 5.06
                moveTo(x = 21.0f, y = 5.06f)
                // c -0.69 0 -1.33 0.27 -1.92 0.8
                curveToRelative(
                    dx1 = -0.69f,
                    dy1 = 0.0f,
                    dx2 = -1.33f,
                    dy2 = 0.27f,
                    dx3 = -1.92f,
                    dy3 = 0.8f,
                )
                // l -5.63 5.64
                lineToRelative(dx = -5.63f, dy = 5.64f)
                // l 1.08 1
                lineToRelative(dx = 1.08f, dy = 1.0f)
                // l 5.58 -5.56
                lineToRelative(dx = 5.58f, dy = -5.56f)
                // c 0.25 -0.25 0.55 -0.38 0.89 -0.38
                curveToRelative(
                    dx1 = 0.25f,
                    dy1 = -0.25f,
                    dx2 = 0.55f,
                    dy2 = -0.38f,
                    dx3 = 0.89f,
                    dy3 = -0.38f,
                )
                // s 0.64 0.13 0.89 0.38
                reflectiveCurveToRelative(
                    dx1 = 0.64f,
                    dy1 = 0.13f,
                    dx2 = 0.89f,
                    dy2 = 0.38f,
                )
                // l 0.61 0.61
                lineToRelative(dx = 0.61f, dy = 0.61f)
                // l 1.03 -1.08
                lineToRelative(dx = 1.03f, dy = -1.08f)
                // l -0.56 -0.61
                lineToRelative(dx = -0.56f, dy = -0.61f)
                // c -0.59 -0.53 -1.25 -0.8 -1.97 -0.8
                curveToRelative(
                    dx1 = -0.59f,
                    dy1 = -0.53f,
                    dx2 = -1.25f,
                    dy2 = -0.8f,
                    dx3 = -1.97f,
                    dy3 = -0.8f,
                )
                // M 7 8
                moveTo(x = 7.0f, y = 8.0f)
                // L 2 22
                lineTo(x = 2.0f, y = 22.0f)
                // l 14 -5z
                lineToRelative(dx = 14.0f, dy = -5.0f)
                close()
                // m 12 3.06
                moveToRelative(dx = 12.0f, dy = 3.06f)
                // c -0.7 0 -1.34 0.27 -1.94 0.8
                curveToRelative(
                    dx1 = -0.7f,
                    dy1 = 0.0f,
                    dx2 = -1.34f,
                    dy2 = 0.27f,
                    dx3 = -1.94f,
                    dy3 = 0.8f,
                )
                // l -1.59 1.59
                lineToRelative(dx = -1.59f, dy = 1.59f)
                // l 1.08 1.08
                lineToRelative(dx = 1.08f, dy = 1.08f)
                // l 1.59 -1.59
                lineToRelative(dx = 1.59f, dy = -1.59f)
                // c 0.25 -0.25 0.53 -0.38 0.86 -0.38
                curveToRelative(
                    dx1 = 0.25f,
                    dy1 = -0.25f,
                    dx2 = 0.53f,
                    dy2 = -0.38f,
                    dx3 = 0.86f,
                    dy3 = -0.38f,
                )
                // s 0.63 0.13 0.88 0.38
                reflectiveCurveToRelative(
                    dx1 = 0.63f,
                    dy1 = 0.13f,
                    dx2 = 0.88f,
                    dy2 = 0.38f,
                )
                // l 1.62 1.59
                lineToRelative(dx = 1.62f, dy = 1.59f)
                // l 1.05 -1.03
                lineToRelative(dx = 1.05f, dy = -1.03f)
                // l -1.6 -1.64
                lineToRelative(dx = -1.6f, dy = -1.64f)
                // c -0.59 -0.53 -1.25 -0.8 -1.95 -0.8
                curveToRelative(
                    dx1 = -0.59f,
                    dy1 = -0.53f,
                    dx2 = -1.25f,
                    dy2 = -0.8f,
                    dx3 = -1.95f,
                    dy3 = -0.8f,
                )
            }
        }.build().also { _newYear = it }
    }

@Suppress("ObjectPropertyName")
private var _newYear: ImageVector? = null
