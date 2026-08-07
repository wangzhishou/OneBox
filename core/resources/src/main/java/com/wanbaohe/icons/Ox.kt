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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineOx

val Icons.Filled.Ox: ImageVector
    get() {
        val current = _ox
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Ox",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M10.5 18 a.5 .5 0 0 1 .5 .5 a.5 .5 0 0 1 -.5 .5 a.5 .5 0 0 1 -.5 -.5 a.5 .5 0 0 1 .5 -.5 m3 0 a.5 .5 0 0 1 .5 .5 a.5 .5 0 0 1 -.5 .5 a.5 .5 0 0 1 -.5 -.5 a.5 .5 0 0 1 .5 -.5 M10 11 a1 1 0 0 1 1 1 a1 1 0 0 1 -1 1 a1 1 0 0 1 -1 -1 a1 1 0 0 1 1 -1 m4 0 a1 1 0 0 1 1 1 a1 1 0 0 1 -1 1 a1 1 0 0 1 -1 -1 a1 1 0 0 1 1 -1 m4 7 c0 2.21 -2.69 4 -6 4 s-6 -1.79 -6 -4 c0 -.9 .45 -1.73 1.2 -2.4 c-.75 -1 -1.2 -2.25 -1.2 -3.6 l.12 -1.22 c-.54 .15 -1.19 .15 -1.72 0 c-1.02 -.28 -2.56 -1.43 -2.33 -2.23 s2.14 -.95 3.16 -.65 c.59 .17 1.22 .6 1.59 1.06 l.57 -.81 C6.79 7.05 7 4 10 3 l-.09 .14 c-.28 .44 -1 1.83 -.24 3.33 a6.02 6.02 0 0 1 4.66 0 c.76 -1.5 .04 -2.89 -.24 -3.33 L14 3 c3 1 3.21 4.05 2.61 5.15 l.57 .81 c.37 -.46 1 -.89 1.59 -1.06 c1.02 -.3 2.93 -.15 3.16 .65 s-1.31 1.95 -2.33 2.23 c-.53 .15 -1.18 .15 -1.72 0 L18 12 c0 1.35 -.45 2.6 -1.2 3.6 c.75 .67 1.2 1.5 1.2 2.4 m-6 -2 c-2.21 0 -4 .9 -4 2 s1.79 2 4 2 s4 -.9 4 -2 s-1.79 -2 -4 -2 m0 -2 c1.12 0 2.17 .21 3.07 .56 c.58 -.69 .93 -1.56 .93 -2.56 a4 4 0 0 0 -4 -4 a4 4 0 0 0 -4 4 c0 1 .35 1.87 .93 2.56 c.9 -.35 1.95 -.56 3.07 -.56 m2.09 -10.86
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.5 18
                moveTo(x = 10.5f, y = 18.0f)
                // a 0.5 0.5 0 0 1 0.5 0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = 0.5f,
                )
                // a 0.5 0.5 0 0 1 -0.5 0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = 0.5f,
                )
                // a 0.5 0.5 0 0 1 -0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                // a 0.5 0.5 0 0 1 0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = -0.5f,
                )
                // m 3 0
                moveToRelative(dx = 3.0f, dy = 0.0f)
                // a 0.5 0.5 0 0 1 0.5 0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = 0.5f,
                )
                // a 0.5 0.5 0 0 1 -0.5 0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = 0.5f,
                )
                // a 0.5 0.5 0 0 1 -0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                // a 0.5 0.5 0 0 1 0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = -0.5f,
                )
                // M 10 11
                moveTo(x = 10.0f, y = 11.0f)
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
                // a 1 1 0 0 1 -1 1
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 1.0f,
                )
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
                // a 1 1 0 0 1 1 -1
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = -1.0f,
                )
                // m 4 0
                moveToRelative(dx = 4.0f, dy = 0.0f)
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
                // a 1 1 0 0 1 -1 1
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 1.0f,
                )
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
                // a 1 1 0 0 1 1 -1
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = -1.0f,
                )
                // m 4 7
                moveToRelative(dx = 4.0f, dy = 7.0f)
                // c 0 2.21 -2.69 4 -6 4
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 2.21f,
                    dx2 = -2.69f,
                    dy2 = 4.0f,
                    dx3 = -6.0f,
                    dy3 = 4.0f,
                )
                // s -6 -1.79 -6 -4
                reflectiveCurveToRelative(
                    dx1 = -6.0f,
                    dy1 = -1.79f,
                    dx2 = -6.0f,
                    dy2 = -4.0f,
                )
                // c 0 -0.9 0.45 -1.73 1.2 -2.4
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.9f,
                    dx2 = 0.45f,
                    dy2 = -1.73f,
                    dx3 = 1.2f,
                    dy3 = -2.4f,
                )
                // c -0.75 -1 -1.2 -2.25 -1.2 -3.6
                curveToRelative(
                    dx1 = -0.75f,
                    dy1 = -1.0f,
                    dx2 = -1.2f,
                    dy2 = -2.25f,
                    dx3 = -1.2f,
                    dy3 = -3.6f,
                )
                // l 0.12 -1.22
                lineToRelative(dx = 0.12f, dy = -1.22f)
                // c -0.54 0.15 -1.19 0.15 -1.72 0
                curveToRelative(
                    dx1 = -0.54f,
                    dy1 = 0.15f,
                    dx2 = -1.19f,
                    dy2 = 0.15f,
                    dx3 = -1.72f,
                    dy3 = 0.0f,
                )
                // c -1.02 -0.28 -2.56 -1.43 -2.33 -2.23
                curveToRelative(
                    dx1 = -1.02f,
                    dy1 = -0.28f,
                    dx2 = -2.56f,
                    dy2 = -1.43f,
                    dx3 = -2.33f,
                    dy3 = -2.23f,
                )
                // s 2.14 -0.95 3.16 -0.65
                reflectiveCurveToRelative(
                    dx1 = 2.14f,
                    dy1 = -0.95f,
                    dx2 = 3.16f,
                    dy2 = -0.65f,
                )
                // c 0.59 0.17 1.22 0.6 1.59 1.06
                curveToRelative(
                    dx1 = 0.59f,
                    dy1 = 0.17f,
                    dx2 = 1.22f,
                    dy2 = 0.6f,
                    dx3 = 1.59f,
                    dy3 = 1.06f,
                )
                // l 0.57 -0.81
                lineToRelative(dx = 0.57f, dy = -0.81f)
                // C 6.79 7.05 7 4 10 3
                curveTo(
                    x1 = 6.79f,
                    y1 = 7.05f,
                    x2 = 7.0f,
                    y2 = 4.0f,
                    x3 = 10.0f,
                    y3 = 3.0f,
                )
                // l -0.09 0.14
                lineToRelative(dx = -0.09f, dy = 0.14f)
                // c -0.28 0.44 -1 1.83 -0.24 3.33
                curveToRelative(
                    dx1 = -0.28f,
                    dy1 = 0.44f,
                    dx2 = -1.0f,
                    dy2 = 1.83f,
                    dx3 = -0.24f,
                    dy3 = 3.33f,
                )
                // a 6.02 6.02 0 0 1 4.66 0
                arcToRelative(
                    a = 6.02f,
                    b = 6.02f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 4.66f,
                    dy1 = 0.0f,
                )
                // c 0.76 -1.5 0.04 -2.89 -0.24 -3.33
                curveToRelative(
                    dx1 = 0.76f,
                    dy1 = -1.5f,
                    dx2 = 0.04f,
                    dy2 = -2.89f,
                    dx3 = -0.24f,
                    dy3 = -3.33f,
                )
                // L 14 3
                lineTo(x = 14.0f, y = 3.0f)
                // c 3 1 3.21 4.05 2.61 5.15
                curveToRelative(
                    dx1 = 3.0f,
                    dy1 = 1.0f,
                    dx2 = 3.21f,
                    dy2 = 4.05f,
                    dx3 = 2.61f,
                    dy3 = 5.15f,
                )
                // l 0.57 0.81
                lineToRelative(dx = 0.57f, dy = 0.81f)
                // c 0.37 -0.46 1 -0.89 1.59 -1.06
                curveToRelative(
                    dx1 = 0.37f,
                    dy1 = -0.46f,
                    dx2 = 1.0f,
                    dy2 = -0.89f,
                    dx3 = 1.59f,
                    dy3 = -1.06f,
                )
                // c 1.02 -0.3 2.93 -0.15 3.16 0.65
                curveToRelative(
                    dx1 = 1.02f,
                    dy1 = -0.3f,
                    dx2 = 2.93f,
                    dy2 = -0.15f,
                    dx3 = 3.16f,
                    dy3 = 0.65f,
                )
                // s -1.31 1.95 -2.33 2.23
                reflectiveCurveToRelative(
                    dx1 = -1.31f,
                    dy1 = 1.95f,
                    dx2 = -2.33f,
                    dy2 = 2.23f,
                )
                // c -0.53 0.15 -1.18 0.15 -1.72 0
                curveToRelative(
                    dx1 = -0.53f,
                    dy1 = 0.15f,
                    dx2 = -1.18f,
                    dy2 = 0.15f,
                    dx3 = -1.72f,
                    dy3 = 0.0f,
                )
                // L 18 12
                lineTo(x = 18.0f, y = 12.0f)
                // c 0 1.35 -0.45 2.6 -1.2 3.6
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.35f,
                    dx2 = -0.45f,
                    dy2 = 2.6f,
                    dx3 = -1.2f,
                    dy3 = 3.6f,
                )
                // c 0.75 0.67 1.2 1.5 1.2 2.4
                curveToRelative(
                    dx1 = 0.75f,
                    dy1 = 0.67f,
                    dx2 = 1.2f,
                    dy2 = 1.5f,
                    dx3 = 1.2f,
                    dy3 = 2.4f,
                )
                // m -6 -2
                moveToRelative(dx = -6.0f, dy = -2.0f)
                // c -2.21 0 -4 0.9 -4 2
                curveToRelative(
                    dx1 = -2.21f,
                    dy1 = 0.0f,
                    dx2 = -4.0f,
                    dy2 = 0.9f,
                    dx3 = -4.0f,
                    dy3 = 2.0f,
                )
                // s 1.79 2 4 2
                reflectiveCurveToRelative(
                    dx1 = 1.79f,
                    dy1 = 2.0f,
                    dx2 = 4.0f,
                    dy2 = 2.0f,
                )
                // s 4 -0.9 4 -2
                reflectiveCurveToRelative(
                    dx1 = 4.0f,
                    dy1 = -0.9f,
                    dx2 = 4.0f,
                    dy2 = -2.0f,
                )
                // s -1.79 -2 -4 -2
                reflectiveCurveToRelative(
                    dx1 = -1.79f,
                    dy1 = -2.0f,
                    dx2 = -4.0f,
                    dy2 = -2.0f,
                )
                // m 0 -2
                moveToRelative(dx = 0.0f, dy = -2.0f)
                // c 1.12 0 2.17 0.21 3.07 0.56
                curveToRelative(
                    dx1 = 1.12f,
                    dy1 = 0.0f,
                    dx2 = 2.17f,
                    dy2 = 0.21f,
                    dx3 = 3.07f,
                    dy3 = 0.56f,
                )
                // c 0.58 -0.69 0.93 -1.56 0.93 -2.56
                curveToRelative(
                    dx1 = 0.58f,
                    dy1 = -0.69f,
                    dx2 = 0.93f,
                    dy2 = -1.56f,
                    dx3 = 0.93f,
                    dy3 = -2.56f,
                )
                // a 4 4 0 0 0 -4 -4
                arcToRelative(
                    a = 4.0f,
                    b = 4.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -4.0f,
                    dy1 = -4.0f,
                )
                // a 4 4 0 0 0 -4 4
                arcToRelative(
                    a = 4.0f,
                    b = 4.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -4.0f,
                    dy1 = 4.0f,
                )
                // c 0 1 0.35 1.87 0.93 2.56
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                    dx2 = 0.35f,
                    dy2 = 1.87f,
                    dx3 = 0.93f,
                    dy3 = 2.56f,
                )
                // c 0.9 -0.35 1.95 -0.56 3.07 -0.56
                curveToRelative(
                    dx1 = 0.9f,
                    dy1 = -0.35f,
                    dx2 = 1.95f,
                    dy2 = -0.56f,
                    dx3 = 3.07f,
                    dy3 = -0.56f,
                )
                // m 2.09 -10.86
                moveToRelative(dx = 2.09f, dy = -10.86f)
            }
        }.build().also { _ox = it }
    }

@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineOx,
            contentDescription = null,
            modifier = Modifier
                .width((24.0).dp)
                .height((24.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _ox: ImageVector? = null
