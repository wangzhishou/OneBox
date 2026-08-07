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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGoat

val Icons.Filled.Goat: ImageVector
    get() {
        val current = _goat
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Goat",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M20 8.5 a2.5 2.5 0 0 1 -2.5 2.5 c-1.08 0 -2 -.69 -2.34 -1.64 c-.44 .39 -1.02 .64 -1.66 .64 c-.56 0 -1.08 -.19 -1.5 -.5 c-.42 .31 -.93 .5 -1.5 .5 c-.64 0 -1.22 -.25 -1.66 -.64 C8.5 10.31 7.58 11 6.5 11 A2.5 2.5 0 0 1 4 8.5 c0 -1.24 .91 -2.27 2.1 -2.46 c-.06 -.17 -.1 -.35 -.1 -.54 A1.5 1.5 0 0 1 7.5 4 c.2 0 .39 .04 .56 .11 C8.23 3.47 8.81 3 9.5 3 c.25 0 .5 .07 .68 .17 C10.5 2.5 11.19 2 12 2 s1.5 .5 1.82 1.17 c.18 -.1 .43 -.17 .68 -.17 c.69 0 1.27 .47 1.44 1.11 c.17 -.07 .36 -.11 .56 -.11 A1.5 1.5 0 0 1 18 5.5 c0 .19 -.04 .37 -.1 .54 c1.19 .19 2.1 1.22 2.1 2.46 M10 12 a1 1 0 0 0 -1 1 a1 1 0 0 0 1 1 a1 1 0 0 0 1 -1 a1 1 0 0 0 -1 -1 m4 0 a1 1 0 0 0 -1 1 a1 1 0 0 0 1 1 a1 1 0 0 0 1 -1 a1 1 0 0 0 -1 -1 m6.23 -1.34 c-.64 .81 -1.62 1.34 -2.73 1.34 c-.45 0 -.88 -.1 -1.29 -.27 c-.01 2.55 -.38 5.63 -1.76 7.22 c-.52 .59 -1.15 .91 -1.95 1.01 V18 h-1 v1.96 c-.8 -.1 -1.43 -.41 -1.95 -1.01 c-1.39 -1.6 -1.76 -4.66 -1.77 -7.21 c-.4 .16 -.83 .26 -1.28 .26 c-1.11 0 -2.09 -.53 -2.73 -1.34 C2.88 11.55 2 12 2 12 s1 2 3 2 c.36 0 .64 -.04 .88 -.09 C6.22 17.73 7.58 22 12 22 s5.78 -4.27 6.12 -8.09 c.24 .05 .52 .09 .88 .09 c2 0 3 -2 3 -2 s-.88 -.45 -1.77 -1.34
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 20 8.5
                moveTo(x = 20.0f, y = 8.5f)
                // a 2.5 2.5 0 0 1 -2.5 2.5
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.5f,
                    dy1 = 2.5f,
                )
                // c -1.08 0 -2 -0.69 -2.34 -1.64
                curveToRelative(
                    dx1 = -1.08f,
                    dy1 = 0.0f,
                    dx2 = -2.0f,
                    dy2 = -0.69f,
                    dx3 = -2.34f,
                    dy3 = -1.64f,
                )
                // c -0.44 0.39 -1.02 0.64 -1.66 0.64
                curveToRelative(
                    dx1 = -0.44f,
                    dy1 = 0.39f,
                    dx2 = -1.02f,
                    dy2 = 0.64f,
                    dx3 = -1.66f,
                    dy3 = 0.64f,
                )
                // c -0.56 0 -1.08 -0.19 -1.5 -0.5
                curveToRelative(
                    dx1 = -0.56f,
                    dy1 = 0.0f,
                    dx2 = -1.08f,
                    dy2 = -0.19f,
                    dx3 = -1.5f,
                    dy3 = -0.5f,
                )
                // c -0.42 0.31 -0.93 0.5 -1.5 0.5
                curveToRelative(
                    dx1 = -0.42f,
                    dy1 = 0.31f,
                    dx2 = -0.93f,
                    dy2 = 0.5f,
                    dx3 = -1.5f,
                    dy3 = 0.5f,
                )
                // c -0.64 0 -1.22 -0.25 -1.66 -0.64
                curveToRelative(
                    dx1 = -0.64f,
                    dy1 = 0.0f,
                    dx2 = -1.22f,
                    dy2 = -0.25f,
                    dx3 = -1.66f,
                    dy3 = -0.64f,
                )
                // C 8.5 10.31 7.58 11 6.5 11
                curveTo(
                    x1 = 8.5f,
                    y1 = 10.31f,
                    x2 = 7.58f,
                    y2 = 11.0f,
                    x3 = 6.5f,
                    y3 = 11.0f,
                )
                // A 2.5 2.5 0 0 1 4 8.5
                arcTo(
                    horizontalEllipseRadius = 2.5f,
                    verticalEllipseRadius = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 4.0f,
                    y1 = 8.5f,
                )
                // c 0 -1.24 0.91 -2.27 2.1 -2.46
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.24f,
                    dx2 = 0.91f,
                    dy2 = -2.27f,
                    dx3 = 2.1f,
                    dy3 = -2.46f,
                )
                // c -0.06 -0.17 -0.1 -0.35 -0.1 -0.54
                curveToRelative(
                    dx1 = -0.06f,
                    dy1 = -0.17f,
                    dx2 = -0.1f,
                    dy2 = -0.35f,
                    dx3 = -0.1f,
                    dy3 = -0.54f,
                )
                // A 1.5 1.5 0 0 1 7.5 4
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.5f,
                    y1 = 4.0f,
                )
                // c 0.2 0 0.39 0.04 0.56 0.11
                curveToRelative(
                    dx1 = 0.2f,
                    dy1 = 0.0f,
                    dx2 = 0.39f,
                    dy2 = 0.04f,
                    dx3 = 0.56f,
                    dy3 = 0.11f,
                )
                // C 8.23 3.47 8.81 3 9.5 3
                curveTo(
                    x1 = 8.23f,
                    y1 = 3.47f,
                    x2 = 8.81f,
                    y2 = 3.0f,
                    x3 = 9.5f,
                    y3 = 3.0f,
                )
                // c 0.25 0 0.5 0.07 0.68 0.17
                curveToRelative(
                    dx1 = 0.25f,
                    dy1 = 0.0f,
                    dx2 = 0.5f,
                    dy2 = 0.07f,
                    dx3 = 0.68f,
                    dy3 = 0.17f,
                )
                // C 10.5 2.5 11.19 2 12 2
                curveTo(
                    x1 = 10.5f,
                    y1 = 2.5f,
                    x2 = 11.19f,
                    y2 = 2.0f,
                    x3 = 12.0f,
                    y3 = 2.0f,
                )
                // s 1.5 0.5 1.82 1.17
                reflectiveCurveToRelative(
                    dx1 = 1.5f,
                    dy1 = 0.5f,
                    dx2 = 1.82f,
                    dy2 = 1.17f,
                )
                // c 0.18 -0.1 0.43 -0.17 0.68 -0.17
                curveToRelative(
                    dx1 = 0.18f,
                    dy1 = -0.1f,
                    dx2 = 0.43f,
                    dy2 = -0.17f,
                    dx3 = 0.68f,
                    dy3 = -0.17f,
                )
                // c 0.69 0 1.27 0.47 1.44 1.11
                curveToRelative(
                    dx1 = 0.69f,
                    dy1 = 0.0f,
                    dx2 = 1.27f,
                    dy2 = 0.47f,
                    dx3 = 1.44f,
                    dy3 = 1.11f,
                )
                // c 0.17 -0.07 0.36 -0.11 0.56 -0.11
                curveToRelative(
                    dx1 = 0.17f,
                    dy1 = -0.07f,
                    dx2 = 0.36f,
                    dy2 = -0.11f,
                    dx3 = 0.56f,
                    dy3 = -0.11f,
                )
                // A 1.5 1.5 0 0 1 18 5.5
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 18.0f,
                    y1 = 5.5f,
                )
                // c 0 0.19 -0.04 0.37 -0.1 0.54
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.19f,
                    dx2 = -0.04f,
                    dy2 = 0.37f,
                    dx3 = -0.1f,
                    dy3 = 0.54f,
                )
                // c 1.19 0.19 2.1 1.22 2.1 2.46
                curveToRelative(
                    dx1 = 1.19f,
                    dy1 = 0.19f,
                    dx2 = 2.1f,
                    dy2 = 1.22f,
                    dx3 = 2.1f,
                    dy3 = 2.46f,
                )
                // M 10 12
                moveTo(x = 10.0f, y = 12.0f)
                // a 1 1 0 0 0 -1 1
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.0f,
                    dy1 = 1.0f,
                )
                // a 1 1 0 0 0 1 1
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.0f,
                    dy1 = 1.0f,
                )
                // a 1 1 0 0 0 1 -1
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.0f,
                    dy1 = -1.0f,
                )
                // a 1 1 0 0 0 -1 -1
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.0f,
                    dy1 = -1.0f,
                )
                // m 4 0
                moveToRelative(dx = 4.0f, dy = 0.0f)
                // a 1 1 0 0 0 -1 1
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.0f,
                    dy1 = 1.0f,
                )
                // a 1 1 0 0 0 1 1
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.0f,
                    dy1 = 1.0f,
                )
                // a 1 1 0 0 0 1 -1
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.0f,
                    dy1 = -1.0f,
                )
                // a 1 1 0 0 0 -1 -1
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.0f,
                    dy1 = -1.0f,
                )
                // m 6.23 -1.34
                moveToRelative(dx = 6.23f, dy = -1.34f)
                // c -0.64 0.81 -1.62 1.34 -2.73 1.34
                curveToRelative(
                    dx1 = -0.64f,
                    dy1 = 0.81f,
                    dx2 = -1.62f,
                    dy2 = 1.34f,
                    dx3 = -2.73f,
                    dy3 = 1.34f,
                )
                // c -0.45 0 -0.88 -0.1 -1.29 -0.27
                curveToRelative(
                    dx1 = -0.45f,
                    dy1 = 0.0f,
                    dx2 = -0.88f,
                    dy2 = -0.1f,
                    dx3 = -1.29f,
                    dy3 = -0.27f,
                )
                // c -0.01 2.55 -0.38 5.63 -1.76 7.22
                curveToRelative(
                    dx1 = -0.01f,
                    dy1 = 2.55f,
                    dx2 = -0.38f,
                    dy2 = 5.63f,
                    dx3 = -1.76f,
                    dy3 = 7.22f,
                )
                // c -0.52 0.59 -1.15 0.91 -1.95 1.01
                curveToRelative(
                    dx1 = -0.52f,
                    dy1 = 0.59f,
                    dx2 = -1.15f,
                    dy2 = 0.91f,
                    dx3 = -1.95f,
                    dy3 = 1.01f,
                )
                // V 18
                verticalLineTo(y = 18.0f)
                // h -1
                horizontalLineToRelative(dx = -1.0f)
                // v 1.96
                verticalLineToRelative(dy = 1.96f)
                // c -0.8 -0.1 -1.43 -0.41 -1.95 -1.01
                curveToRelative(
                    dx1 = -0.8f,
                    dy1 = -0.1f,
                    dx2 = -1.43f,
                    dy2 = -0.41f,
                    dx3 = -1.95f,
                    dy3 = -1.01f,
                )
                // c -1.39 -1.6 -1.76 -4.66 -1.77 -7.21
                curveToRelative(
                    dx1 = -1.39f,
                    dy1 = -1.6f,
                    dx2 = -1.76f,
                    dy2 = -4.66f,
                    dx3 = -1.77f,
                    dy3 = -7.21f,
                )
                // c -0.4 0.16 -0.83 0.26 -1.28 0.26
                curveToRelative(
                    dx1 = -0.4f,
                    dy1 = 0.16f,
                    dx2 = -0.83f,
                    dy2 = 0.26f,
                    dx3 = -1.28f,
                    dy3 = 0.26f,
                )
                // c -1.11 0 -2.09 -0.53 -2.73 -1.34
                curveToRelative(
                    dx1 = -1.11f,
                    dy1 = 0.0f,
                    dx2 = -2.09f,
                    dy2 = -0.53f,
                    dx3 = -2.73f,
                    dy3 = -1.34f,
                )
                // C 2.88 11.55 2 12 2 12
                curveTo(
                    x1 = 2.88f,
                    y1 = 11.55f,
                    x2 = 2.0f,
                    y2 = 12.0f,
                    x3 = 2.0f,
                    y3 = 12.0f,
                )
                // s 1 2 3 2
                reflectiveCurveToRelative(
                    dx1 = 1.0f,
                    dy1 = 2.0f,
                    dx2 = 3.0f,
                    dy2 = 2.0f,
                )
                // c 0.36 0 0.64 -0.04 0.88 -0.09
                curveToRelative(
                    dx1 = 0.36f,
                    dy1 = 0.0f,
                    dx2 = 0.64f,
                    dy2 = -0.04f,
                    dx3 = 0.88f,
                    dy3 = -0.09f,
                )
                // C 6.22 17.73 7.58 22 12 22
                curveTo(
                    x1 = 6.22f,
                    y1 = 17.73f,
                    x2 = 7.58f,
                    y2 = 22.0f,
                    x3 = 12.0f,
                    y3 = 22.0f,
                )
                // s 5.78 -4.27 6.12 -8.09
                reflectiveCurveToRelative(
                    dx1 = 5.78f,
                    dy1 = -4.27f,
                    dx2 = 6.12f,
                    dy2 = -8.09f,
                )
                // c 0.24 0.05 0.52 0.09 0.88 0.09
                curveToRelative(
                    dx1 = 0.24f,
                    dy1 = 0.05f,
                    dx2 = 0.52f,
                    dy2 = 0.09f,
                    dx3 = 0.88f,
                    dy3 = 0.09f,
                )
                // c 2 0 3 -2 3 -2
                curveToRelative(
                    dx1 = 2.0f,
                    dy1 = 0.0f,
                    dx2 = 3.0f,
                    dy2 = -2.0f,
                    dx3 = 3.0f,
                    dy3 = -2.0f,
                )
                // s -0.88 -0.45 -1.77 -1.34
                reflectiveCurveToRelative(
                    dx1 = -0.88f,
                    dy1 = -0.45f,
                    dx2 = -1.77f,
                    dy2 = -1.34f,
                )
            }
        }.build().also { _goat = it }
    }

@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGoat,
            contentDescription = null,
            modifier = Modifier
                .width((24.0).dp)
                .height((24.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _goat: ImageVector? = null
