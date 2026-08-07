package com.wanbaohe.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDog

val Icons.Filled.Dog: ImageVector
    get() {
        val current = _dog
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Dog",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M18 4 c-1.71 0 -2.75 .33 -3.35 .61 C13.88 4.23 13 4 12 4 s-1.88 .23 -2.65 .61 C8.75 4.33 7.71 4 6 4 c-3 0 -5 8 -5 10 c0 .83 1.32 1.59 3.14 1.9 c.64 2.24 3.66 3.95 7.36 4.1 v-4.28 c-.59 -.37 -1.5 -1.04 -1.5 -1.72 c0 -1 2 -1 2 -1 s2 0 2 1 c0 .68 -.91 1.35 -1.5 1.72 V20 c3.7 -.15 6.72 -1.86 7.36 -4.1 C21.68 15.59 23 14.83 23 14 c0 -2 -2 -10 -5 -10 M4.15 13.87 c-.5 -.12 -.89 -.26 -1.15 -.37 c.25 -2.77 2.2 -7.1 3.05 -7.5 c.54 0 .95 .06 1.32 .11 c-2.1 2.31 -2.93 5.93 -3.22 7.76 M9 12 a1 1 0 0 1 -1 -1 c0 -.54 .45 -1 1 -1 a1 1 0 0 1 1 1 c0 .56 -.45 1 -1 1 m6 0 a1 1 0 0 1 -1 -1 c0 -.54 .45 -1 1 -1 a1 1 0 0 1 1 1 c0 .56 -.45 1 -1 1 m4.85 1.87 c-.29 -1.83 -1.12 -5.45 -3.22 -7.76 c.37 -.05 .78 -.11 1.32 -.11 c.85 .4 2.8 4.73 3.05 7.5 c-.25 .11 -.64 .25 -1.15 .37
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 18 4
                moveTo(x = 18.0f, y = 4.0f)
                // c -1.71 0 -2.75 0.33 -3.35 0.61
                curveToRelative(
                    dx1 = -1.71f,
                    dy1 = 0.0f,
                    dx2 = -2.75f,
                    dy2 = 0.33f,
                    dx3 = -3.35f,
                    dy3 = 0.61f,
                )
                // C 13.88 4.23 13 4 12 4
                curveTo(
                    x1 = 13.88f,
                    y1 = 4.23f,
                    x2 = 13.0f,
                    y2 = 4.0f,
                    x3 = 12.0f,
                    y3 = 4.0f,
                )
                // s -1.88 0.23 -2.65 0.61
                reflectiveCurveToRelative(
                    dx1 = -1.88f,
                    dy1 = 0.23f,
                    dx2 = -2.65f,
                    dy2 = 0.61f,
                )
                // C 8.75 4.33 7.71 4 6 4
                curveTo(
                    x1 = 8.75f,
                    y1 = 4.33f,
                    x2 = 7.71f,
                    y2 = 4.0f,
                    x3 = 6.0f,
                    y3 = 4.0f,
                )
                // c -3 0 -5 8 -5 10
                curveToRelative(
                    dx1 = -3.0f,
                    dy1 = 0.0f,
                    dx2 = -5.0f,
                    dy2 = 8.0f,
                    dx3 = -5.0f,
                    dy3 = 10.0f,
                )
                // c 0 0.83 1.32 1.59 3.14 1.9
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.83f,
                    dx2 = 1.32f,
                    dy2 = 1.59f,
                    dx3 = 3.14f,
                    dy3 = 1.9f,
                )
                // c 0.64 2.24 3.66 3.95 7.36 4.1
                curveToRelative(
                    dx1 = 0.64f,
                    dy1 = 2.24f,
                    dx2 = 3.66f,
                    dy2 = 3.95f,
                    dx3 = 7.36f,
                    dy3 = 4.1f,
                )
                // v -4.28
                verticalLineToRelative(dy = -4.28f)
                // c -0.59 -0.37 -1.5 -1.04 -1.5 -1.72
                curveToRelative(
                    dx1 = -0.59f,
                    dy1 = -0.37f,
                    dx2 = -1.5f,
                    dy2 = -1.04f,
                    dx3 = -1.5f,
                    dy3 = -1.72f,
                )
                // c 0 -1 2 -1 2 -1
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                    dx2 = 2.0f,
                    dy2 = -1.0f,
                    dx3 = 2.0f,
                    dy3 = -1.0f,
                )
                // s 2 0 2 1
                reflectiveCurveToRelative(
                    dx1 = 2.0f,
                    dy1 = 0.0f,
                    dx2 = 2.0f,
                    dy2 = 1.0f,
                )
                // c 0 0.68 -0.91 1.35 -1.5 1.72
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.68f,
                    dx2 = -0.91f,
                    dy2 = 1.35f,
                    dx3 = -1.5f,
                    dy3 = 1.72f,
                )
                // V 20
                verticalLineTo(y = 20.0f)
                // c 3.7 -0.15 6.72 -1.86 7.36 -4.1
                curveToRelative(
                    dx1 = 3.7f,
                    dy1 = -0.15f,
                    dx2 = 6.72f,
                    dy2 = -1.86f,
                    dx3 = 7.36f,
                    dy3 = -4.1f,
                )
                // C 21.68 15.59 23 14.83 23 14
                curveTo(
                    x1 = 21.68f,
                    y1 = 15.59f,
                    x2 = 23.0f,
                    y2 = 14.83f,
                    x3 = 23.0f,
                    y3 = 14.0f,
                )
                // c 0 -2 -2 -10 -5 -10
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -2.0f,
                    dx2 = -2.0f,
                    dy2 = -10.0f,
                    dx3 = -5.0f,
                    dy3 = -10.0f,
                )
                // M 4.15 13.87
                moveTo(x = 4.15f, y = 13.87f)
                // c -0.5 -0.12 -0.89 -0.26 -1.15 -0.37
                curveToRelative(
                    dx1 = -0.5f,
                    dy1 = -0.12f,
                    dx2 = -0.89f,
                    dy2 = -0.26f,
                    dx3 = -1.15f,
                    dy3 = -0.37f,
                )
                // c 0.25 -2.77 2.2 -7.1 3.05 -7.5
                curveToRelative(
                    dx1 = 0.25f,
                    dy1 = -2.77f,
                    dx2 = 2.2f,
                    dy2 = -7.1f,
                    dx3 = 3.05f,
                    dy3 = -7.5f,
                )
                // c 0.54 0 0.95 0.06 1.32 0.11
                curveToRelative(
                    dx1 = 0.54f,
                    dy1 = 0.0f,
                    dx2 = 0.95f,
                    dy2 = 0.06f,
                    dx3 = 1.32f,
                    dy3 = 0.11f,
                )
                // c -2.1 2.31 -2.93 5.93 -3.22 7.76
                curveToRelative(
                    dx1 = -2.1f,
                    dy1 = 2.31f,
                    dx2 = -2.93f,
                    dy2 = 5.93f,
                    dx3 = -3.22f,
                    dy3 = 7.76f,
                )
                // M 9 12
                moveTo(x = 9.0f, y = 12.0f)
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
                // c 0 -0.54 0.45 -1 1 -1
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.54f,
                    dx2 = 0.45f,
                    dy2 = -1.0f,
                    dx3 = 1.0f,
                    dy3 = -1.0f,
                )
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
                // c 0 0.56 -0.45 1 -1 1
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.56f,
                    dx2 = -0.45f,
                    dy2 = 1.0f,
                    dx3 = -1.0f,
                    dy3 = 1.0f,
                )
                // m 6 0
                moveToRelative(dx = 6.0f, dy = 0.0f)
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
                // c 0 -0.54 0.45 -1 1 -1
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.54f,
                    dx2 = 0.45f,
                    dy2 = -1.0f,
                    dx3 = 1.0f,
                    dy3 = -1.0f,
                )
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
                // c 0 0.56 -0.45 1 -1 1
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.56f,
                    dx2 = -0.45f,
                    dy2 = 1.0f,
                    dx3 = -1.0f,
                    dy3 = 1.0f,
                )
                // m 4.85 1.87
                moveToRelative(dx = 4.85f, dy = 1.87f)
                // c -0.29 -1.83 -1.12 -5.45 -3.22 -7.76
                curveToRelative(
                    dx1 = -0.29f,
                    dy1 = -1.83f,
                    dx2 = -1.12f,
                    dy2 = -5.45f,
                    dx3 = -3.22f,
                    dy3 = -7.76f,
                )
                // c 0.37 -0.05 0.78 -0.11 1.32 -0.11
                curveToRelative(
                    dx1 = 0.37f,
                    dy1 = -0.05f,
                    dx2 = 0.78f,
                    dy2 = -0.11f,
                    dx3 = 1.32f,
                    dy3 = -0.11f,
                )
                // c 0.85 0.4 2.8 4.73 3.05 7.5
                curveToRelative(
                    dx1 = 0.85f,
                    dy1 = 0.4f,
                    dx2 = 2.8f,
                    dy2 = 4.73f,
                    dx3 = 3.05f,
                    dy3 = 7.5f,
                )
                // c -0.25 0.11 -0.64 0.25 -1.15 0.37
                curveToRelative(
                    dx1 = -0.25f,
                    dy1 = 0.11f,
                    dx2 = -0.64f,
                    dy2 = 0.25f,
                    dx3 = -1.15f,
                    dy3 = 0.37f,
                )
            }
        }.build().also { _dog = it }
    }

@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDog,
            contentDescription = null,
            modifier = Modifier
                .width((24.0).dp)
                .height((24.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _dog: ImageVector? = null
