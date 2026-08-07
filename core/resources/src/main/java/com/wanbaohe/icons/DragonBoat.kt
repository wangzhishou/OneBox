package com.wanbaohe.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.DragonBoat: ImageVector
    get() {
        val current = _dragonBoat
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.DragonBoat",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // m3 13.5 l8 -11.47 V13.5z m9.5 0 c1.35 -3.75 1.17 -8.79 0 -12.5 c4.76 1.54 8.4 7.4 8.46 12.5z m8.6 3.58 c-.41 .64 -.89 1.19 -1.45 1.66 c-.65 -.29 -1.23 -.74 -1.69 -1.24 c-1.49 1.93 -4.5 1.93 -5.99 0 c-1.47 1.93 -4.5 1.93 -5.97 0 c-.5 .5 -1.05 .95 -1.7 1.24 c-1.14 -.94 -2 -2.28 -2.3 -3.74 h19.94 a6.4 6.4 0 0 1 -.84 2.08 M20.96 23 q-1.59 0 -3 -.75 c-1.84 1 -4.15 1 -5.99 0 c-1.84 1 -4.15 1 -5.97 0 c-1.23 .69 -2.64 .8 -4 .75 v-2 c1.41 .05 2.77 -.1 4 -1 c1.74 1.25 4.21 1.25 5.97 0 c1.77 1.25 4.23 1.25 5.99 0 c1.21 .9 2.58 1.05 3.98 1 v2z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 3 13.5
                moveTo(x = 3.0f, y = 13.5f)
                // l 8 -11.47
                lineToRelative(dx = 8.0f, dy = -11.47f)
                // V 13.5z
                verticalLineTo(y = 13.5f)
                close()
                // m 9.5 0
                moveToRelative(dx = 9.5f, dy = 0.0f)
                // c 1.35 -3.75 1.17 -8.79 0 -12.5
                curveToRelative(
                    dx1 = 1.35f,
                    dy1 = -3.75f,
                    dx2 = 1.17f,
                    dy2 = -8.79f,
                    dx3 = 0.0f,
                    dy3 = -12.5f,
                )
                // c 4.76 1.54 8.4 7.4 8.46 12.5z
                curveToRelative(
                    dx1 = 4.76f,
                    dy1 = 1.54f,
                    dx2 = 8.4f,
                    dy2 = 7.4f,
                    dx3 = 8.46f,
                    dy3 = 12.5f,
                )
                close()
                // m 8.6 3.58
                moveToRelative(dx = 8.6f, dy = 3.58f)
                // c -0.41 0.64 -0.89 1.19 -1.45 1.66
                curveToRelative(
                    dx1 = -0.41f,
                    dy1 = 0.64f,
                    dx2 = -0.89f,
                    dy2 = 1.19f,
                    dx3 = -1.45f,
                    dy3 = 1.66f,
                )
                // c -0.65 -0.29 -1.23 -0.74 -1.69 -1.24
                curveToRelative(
                    dx1 = -0.65f,
                    dy1 = -0.29f,
                    dx2 = -1.23f,
                    dy2 = -0.74f,
                    dx3 = -1.69f,
                    dy3 = -1.24f,
                )
                // c -1.49 1.93 -4.5 1.93 -5.99 0
                curveToRelative(
                    dx1 = -1.49f,
                    dy1 = 1.93f,
                    dx2 = -4.5f,
                    dy2 = 1.93f,
                    dx3 = -5.99f,
                    dy3 = 0.0f,
                )
                // c -1.47 1.93 -4.5 1.93 -5.97 0
                curveToRelative(
                    dx1 = -1.47f,
                    dy1 = 1.93f,
                    dx2 = -4.5f,
                    dy2 = 1.93f,
                    dx3 = -5.97f,
                    dy3 = 0.0f,
                )
                // c -0.5 0.5 -1.05 0.95 -1.7 1.24
                curveToRelative(
                    dx1 = -0.5f,
                    dy1 = 0.5f,
                    dx2 = -1.05f,
                    dy2 = 0.95f,
                    dx3 = -1.7f,
                    dy3 = 1.24f,
                )
                // c -1.14 -0.94 -2 -2.28 -2.3 -3.74
                curveToRelative(
                    dx1 = -1.14f,
                    dy1 = -0.94f,
                    dx2 = -2.0f,
                    dy2 = -2.28f,
                    dx3 = -2.3f,
                    dy3 = -3.74f,
                )
                // h 19.94
                horizontalLineToRelative(dx = 19.94f)
                // a 6.4 6.4 0 0 1 -0.84 2.08
                arcToRelative(
                    a = 6.4f,
                    b = 6.4f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.84f,
                    dy1 = 2.08f,
                )
                // M 20.96 23
                moveTo(x = 20.96f, y = 23.0f)
                // q -1.59 0 -3 -0.75
                quadToRelative(
                    dx1 = -1.59f,
                    dy1 = 0.0f,
                    dx2 = -3.0f,
                    dy2 = -0.75f,
                )
                // c -1.84 1 -4.15 1 -5.99 0
                curveToRelative(
                    dx1 = -1.84f,
                    dy1 = 1.0f,
                    dx2 = -4.15f,
                    dy2 = 1.0f,
                    dx3 = -5.99f,
                    dy3 = 0.0f,
                )
                // c -1.84 1 -4.15 1 -5.97 0
                curveToRelative(
                    dx1 = -1.84f,
                    dy1 = 1.0f,
                    dx2 = -4.15f,
                    dy2 = 1.0f,
                    dx3 = -5.97f,
                    dy3 = 0.0f,
                )
                // c -1.23 0.69 -2.64 0.8 -4 0.75
                curveToRelative(
                    dx1 = -1.23f,
                    dy1 = 0.69f,
                    dx2 = -2.64f,
                    dy2 = 0.8f,
                    dx3 = -4.0f,
                    dy3 = 0.75f,
                )
                // v -2
                verticalLineToRelative(dy = -2.0f)
                // c 1.41 0.05 2.77 -0.1 4 -1
                curveToRelative(
                    dx1 = 1.41f,
                    dy1 = 0.05f,
                    dx2 = 2.77f,
                    dy2 = -0.1f,
                    dx3 = 4.0f,
                    dy3 = -1.0f,
                )
                // c 1.74 1.25 4.21 1.25 5.97 0
                curveToRelative(
                    dx1 = 1.74f,
                    dy1 = 1.25f,
                    dx2 = 4.21f,
                    dy2 = 1.25f,
                    dx3 = 5.97f,
                    dy3 = 0.0f,
                )
                // c 1.77 1.25 4.23 1.25 5.99 0
                curveToRelative(
                    dx1 = 1.77f,
                    dy1 = 1.25f,
                    dx2 = 4.23f,
                    dy2 = 1.25f,
                    dx3 = 5.99f,
                    dy3 = 0.0f,
                )
                // c 1.21 0.9 2.58 1.05 3.98 1
                curveToRelative(
                    dx1 = 1.21f,
                    dy1 = 0.9f,
                    dx2 = 2.58f,
                    dy2 = 1.05f,
                    dx3 = 3.98f,
                    dy3 = 1.0f,
                )
                // v 2z
                verticalLineToRelative(dy = 2.0f)
                close()
            }
        }.build().also { _dragonBoat = it }
    }

@Suppress("ObjectPropertyName")
private var _dragonBoat: ImageVector? = null
