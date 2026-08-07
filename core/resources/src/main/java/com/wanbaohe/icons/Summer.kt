package com.wanbaohe.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.Summer: ImageVector
    get() {
        val current = _summer
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Summer",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M9.7 4.3 L12 1 l2.3 3.3 c-.7 -.2 -1.5 -.3 -2.3 -.3 s-1.6 .1 -2.3 .3 m7.8 1.9 c1.1 1.1 2 2.5 2.3 4.1 l1.7 -3.7z M5 8.1 q.15 -.15 0 0 c.1 -.1 .1 -.1 .1 -.2 c.4 -.6 .9 -1.2 1.4 -1.7 l-4 .3 l1.7 3.7 c.2 -.7 .5 -1.5 .8 -2.1 m14.2 7.3 s0 .1 0 0 c-.1 .2 -.2 .4 -.3 .5 v.2 c-.4 .7 -.9 1.2 -1.4 1.8 l4.1 -.3 l-1.7 -3.7 c-.2 .5 -.4 1 -.7 1.5 m-14 .8 c0 -.1 -.1 -.1 -.1 -.2 c-.1 -.1 -.1 -.1 -.1 -.2 c-.1 -.2 -.2 -.3 -.2 -.5 c-.2 -.5 -.4 -1 -.5 -1.5 l-1.7 3.7 l4.1 .3 c-.7 -.5 -1.1 -1 -1.5 -1.6 m7.4 3.8 h-1.2 c-.6 0 -1.2 -.2 -1.7 -.3 L12 23 l2.3 -3.3 c-.5 .1 -1.1 .2 -1.7 .3 m3.6 -12.2 c-2.3 -2.3 -6.1 -2.3 -8.5 0 s-2.3 6.1 0 8.5 s6.1 2.3 8.5 0 s2.4 -6.2 0 -8.5 m-7.7 7.7 l2.1 -4.9 l5 -2.2 l-2.1 4.9z m4.2 -2.8 c-.4 .4 -1 .4 -1.4 0 s-.4 -1 0 -1.4 s1 -.4 1.4 0 s.4 1 0 1.4
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 9.7 4.3
                moveTo(x = 9.7f, y = 4.3f)
                // L 12 1
                lineTo(x = 12.0f, y = 1.0f)
                // l 2.3 3.3
                lineToRelative(dx = 2.3f, dy = 3.3f)
                // c -0.7 -0.2 -1.5 -0.3 -2.3 -0.3
                curveToRelative(
                    dx1 = -0.7f,
                    dy1 = -0.2f,
                    dx2 = -1.5f,
                    dy2 = -0.3f,
                    dx3 = -2.3f,
                    dy3 = -0.3f,
                )
                // s -1.6 0.1 -2.3 0.3
                reflectiveCurveToRelative(
                    dx1 = -1.6f,
                    dy1 = 0.1f,
                    dx2 = -2.3f,
                    dy2 = 0.3f,
                )
                // m 7.8 1.9
                moveToRelative(dx = 7.8f, dy = 1.9f)
                // c 1.1 1.1 2 2.5 2.3 4.1
                curveToRelative(
                    dx1 = 1.1f,
                    dy1 = 1.1f,
                    dx2 = 2.0f,
                    dy2 = 2.5f,
                    dx3 = 2.3f,
                    dy3 = 4.1f,
                )
                // l 1.7 -3.7z
                lineToRelative(dx = 1.7f, dy = -3.7f)
                close()
                // M 5 8.1
                moveTo(x = 5.0f, y = 8.1f)
                // q 0.15 -0.15 0 0
                quadToRelative(
                    dx1 = 0.15f,
                    dy1 = -0.15f,
                    dx2 = 0.0f,
                    dy2 = 0.0f,
                )
                // c 0.1 -0.1 0.1 -0.1 0.1 -0.2
                curveToRelative(
                    dx1 = 0.1f,
                    dy1 = -0.1f,
                    dx2 = 0.1f,
                    dy2 = -0.1f,
                    dx3 = 0.1f,
                    dy3 = -0.2f,
                )
                // c 0.4 -0.6 0.9 -1.2 1.4 -1.7
                curveToRelative(
                    dx1 = 0.4f,
                    dy1 = -0.6f,
                    dx2 = 0.9f,
                    dy2 = -1.2f,
                    dx3 = 1.4f,
                    dy3 = -1.7f,
                )
                // l -4 0.3
                lineToRelative(dx = -4.0f, dy = 0.3f)
                // l 1.7 3.7
                lineToRelative(dx = 1.7f, dy = 3.7f)
                // c 0.2 -0.7 0.5 -1.5 0.8 -2.1
                curveToRelative(
                    dx1 = 0.2f,
                    dy1 = -0.7f,
                    dx2 = 0.5f,
                    dy2 = -1.5f,
                    dx3 = 0.8f,
                    dy3 = -2.1f,
                )
                // m 14.2 7.3
                moveToRelative(dx = 14.2f, dy = 7.3f)
                // s 0 0.1 0 0
                reflectiveCurveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.1f,
                    dx2 = 0.0f,
                    dy2 = 0.0f,
                )
                // c -0.1 0.2 -0.2 0.4 -0.3 0.5
                curveToRelative(
                    dx1 = -0.1f,
                    dy1 = 0.2f,
                    dx2 = -0.2f,
                    dy2 = 0.4f,
                    dx3 = -0.3f,
                    dy3 = 0.5f,
                )
                // v 0.2
                verticalLineToRelative(dy = 0.2f)
                // c -0.4 0.7 -0.9 1.2 -1.4 1.8
                curveToRelative(
                    dx1 = -0.4f,
                    dy1 = 0.7f,
                    dx2 = -0.9f,
                    dy2 = 1.2f,
                    dx3 = -1.4f,
                    dy3 = 1.8f,
                )
                // l 4.1 -0.3
                lineToRelative(dx = 4.1f, dy = -0.3f)
                // l -1.7 -3.7
                lineToRelative(dx = -1.7f, dy = -3.7f)
                // c -0.2 0.5 -0.4 1 -0.7 1.5
                curveToRelative(
                    dx1 = -0.2f,
                    dy1 = 0.5f,
                    dx2 = -0.4f,
                    dy2 = 1.0f,
                    dx3 = -0.7f,
                    dy3 = 1.5f,
                )
                // m -14 0.8
                moveToRelative(dx = -14.0f, dy = 0.8f)
                // c 0 -0.1 -0.1 -0.1 -0.1 -0.2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.1f,
                    dx2 = -0.1f,
                    dy2 = -0.1f,
                    dx3 = -0.1f,
                    dy3 = -0.2f,
                )
                // c -0.1 -0.1 -0.1 -0.1 -0.1 -0.2
                curveToRelative(
                    dx1 = -0.1f,
                    dy1 = -0.1f,
                    dx2 = -0.1f,
                    dy2 = -0.1f,
                    dx3 = -0.1f,
                    dy3 = -0.2f,
                )
                // c -0.1 -0.2 -0.2 -0.3 -0.2 -0.5
                curveToRelative(
                    dx1 = -0.1f,
                    dy1 = -0.2f,
                    dx2 = -0.2f,
                    dy2 = -0.3f,
                    dx3 = -0.2f,
                    dy3 = -0.5f,
                )
                // c -0.2 -0.5 -0.4 -1 -0.5 -1.5
                curveToRelative(
                    dx1 = -0.2f,
                    dy1 = -0.5f,
                    dx2 = -0.4f,
                    dy2 = -1.0f,
                    dx3 = -0.5f,
                    dy3 = -1.5f,
                )
                // l -1.7 3.7
                lineToRelative(dx = -1.7f, dy = 3.7f)
                // l 4.1 0.3
                lineToRelative(dx = 4.1f, dy = 0.3f)
                // c -0.7 -0.5 -1.1 -1 -1.5 -1.6
                curveToRelative(
                    dx1 = -0.7f,
                    dy1 = -0.5f,
                    dx2 = -1.1f,
                    dy2 = -1.0f,
                    dx3 = -1.5f,
                    dy3 = -1.6f,
                )
                // m 7.4 3.8
                moveToRelative(dx = 7.4f, dy = 3.8f)
                // h -1.2
                horizontalLineToRelative(dx = -1.2f)
                // c -0.6 0 -1.2 -0.2 -1.7 -0.3
                curveToRelative(
                    dx1 = -0.6f,
                    dy1 = 0.0f,
                    dx2 = -1.2f,
                    dy2 = -0.2f,
                    dx3 = -1.7f,
                    dy3 = -0.3f,
                )
                // L 12 23
                lineTo(x = 12.0f, y = 23.0f)
                // l 2.3 -3.3
                lineToRelative(dx = 2.3f, dy = -3.3f)
                // c -0.5 0.1 -1.1 0.2 -1.7 0.3
                curveToRelative(
                    dx1 = -0.5f,
                    dy1 = 0.1f,
                    dx2 = -1.1f,
                    dy2 = 0.2f,
                    dx3 = -1.7f,
                    dy3 = 0.3f,
                )
                // m 3.6 -12.2
                moveToRelative(dx = 3.6f, dy = -12.2f)
                // c -2.3 -2.3 -6.1 -2.3 -8.5 0
                curveToRelative(
                    dx1 = -2.3f,
                    dy1 = -2.3f,
                    dx2 = -6.1f,
                    dy2 = -2.3f,
                    dx3 = -8.5f,
                    dy3 = 0.0f,
                )
                // s -2.3 6.1 0 8.5
                reflectiveCurveToRelative(
                    dx1 = -2.3f,
                    dy1 = 6.1f,
                    dx2 = 0.0f,
                    dy2 = 8.5f,
                )
                // s 6.1 2.3 8.5 0
                reflectiveCurveToRelative(
                    dx1 = 6.1f,
                    dy1 = 2.3f,
                    dx2 = 8.5f,
                    dy2 = 0.0f,
                )
                // s 2.4 -6.2 0 -8.5
                reflectiveCurveToRelative(
                    dx1 = 2.4f,
                    dy1 = -6.2f,
                    dx2 = 0.0f,
                    dy2 = -8.5f,
                )
                // m -7.7 7.7
                moveToRelative(dx = -7.7f, dy = 7.7f)
                // l 2.1 -4.9
                lineToRelative(dx = 2.1f, dy = -4.9f)
                // l 5 -2.2
                lineToRelative(dx = 5.0f, dy = -2.2f)
                // l -2.1 4.9z
                lineToRelative(dx = -2.1f, dy = 4.9f)
                close()
                // m 4.2 -2.8
                moveToRelative(dx = 4.2f, dy = -2.8f)
                // c -0.4 0.4 -1 0.4 -1.4 0
                curveToRelative(
                    dx1 = -0.4f,
                    dy1 = 0.4f,
                    dx2 = -1.0f,
                    dy2 = 0.4f,
                    dx3 = -1.4f,
                    dy3 = 0.0f,
                )
                // s -0.4 -1 0 -1.4
                reflectiveCurveToRelative(
                    dx1 = -0.4f,
                    dy1 = -1.0f,
                    dx2 = 0.0f,
                    dy2 = -1.4f,
                )
                // s 1 -0.4 1.4 0
                reflectiveCurveToRelative(
                    dx1 = 1.0f,
                    dy1 = -0.4f,
                    dx2 = 1.4f,
                    dy2 = 0.0f,
                )
                // s 0.4 1 0 1.4
                reflectiveCurveToRelative(
                    dx1 = 0.4f,
                    dy1 = 1.0f,
                    dx2 = 0.0f,
                    dy2 = 1.4f,
                )
            }
        }.build().also { _summer = it }
    }

@Suppress("ObjectPropertyName")
private var _summer: ImageVector? = null
