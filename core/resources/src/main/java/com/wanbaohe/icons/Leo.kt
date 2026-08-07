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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLeo

val Icons.Filled.Leo: ImageVector
    get() {
        val current = _leo
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Leo",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M20 17 a3 3 0 0 1 -3 3 a3.163 3.163 0 0 1 -3 -3 c.16 -1.61 .5 -3.2 1 -4.74 c.54 -1.71 .87 -3.47 1 -5.26 a5.136 5.136 0 0 0 -5 -5 a5.136 5.136 0 0 0 -5 5 c.15 1.53 .5 3.03 1 4.5 l.21 .7 c-2.11 -.67 -4.35 .5 -5.02 2.6 c-.69 2.11 .49 4.36 2.6 5.03 s4.35 -.5 5.02 -2.61 c.13 -.39 .19 -.81 .19 -1.22 c-.16 -1.73 -.5 -3.44 -1.09 -5.08 A18.8 18.8 0 0 1 8 7 a3.163 3.163 0 0 1 3 -3 c1.62 .08 2.92 1.38 3 3 a22.6 22.6 0 0 1 -1 4.74 c-.54 1.71 -.87 3.47 -1 5.26 a5.136 5.136 0 0 0 5 5 a5 5 0 0 0 5 -5z M6 18 a2 2 0 0 1 -2 -2 a2 2 0 0 1 2 -2 a2 2 0 0 1 2 2 a2 2 0 0 1 -2 2
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 20 17
                moveTo(x = 20.0f, y = 17.0f)
                // a 3 3 0 0 1 -3 3
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.0f,
                    dy1 = 3.0f,
                )
                // a 3.163 3.163 0 0 1 -3 -3
                arcToRelative(
                    a = 3.163f,
                    b = 3.163f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.0f,
                    dy1 = -3.0f,
                )
                // c 0.16 -1.61 0.5 -3.2 1 -4.74
                curveToRelative(
                    dx1 = 0.16f,
                    dy1 = -1.61f,
                    dx2 = 0.5f,
                    dy2 = -3.2f,
                    dx3 = 1.0f,
                    dy3 = -4.74f,
                )
                // c 0.54 -1.71 0.87 -3.47 1 -5.26
                curveToRelative(
                    dx1 = 0.54f,
                    dy1 = -1.71f,
                    dx2 = 0.87f,
                    dy2 = -3.47f,
                    dx3 = 1.0f,
                    dy3 = -5.26f,
                )
                // a 5.136 5.136 0 0 0 -5 -5
                arcToRelative(
                    a = 5.136f,
                    b = 5.136f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -5.0f,
                    dy1 = -5.0f,
                )
                // a 5.136 5.136 0 0 0 -5 5
                arcToRelative(
                    a = 5.136f,
                    b = 5.136f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -5.0f,
                    dy1 = 5.0f,
                )
                // c 0.15 1.53 0.5 3.03 1 4.5
                curveToRelative(
                    dx1 = 0.15f,
                    dy1 = 1.53f,
                    dx2 = 0.5f,
                    dy2 = 3.03f,
                    dx3 = 1.0f,
                    dy3 = 4.5f,
                )
                // l 0.21 0.7
                lineToRelative(dx = 0.21f, dy = 0.7f)
                // c -2.11 -0.67 -4.35 0.5 -5.02 2.6
                curveToRelative(
                    dx1 = -2.11f,
                    dy1 = -0.67f,
                    dx2 = -4.35f,
                    dy2 = 0.5f,
                    dx3 = -5.02f,
                    dy3 = 2.6f,
                )
                // c -0.69 2.11 0.49 4.36 2.6 5.03
                curveToRelative(
                    dx1 = -0.69f,
                    dy1 = 2.11f,
                    dx2 = 0.49f,
                    dy2 = 4.36f,
                    dx3 = 2.6f,
                    dy3 = 5.03f,
                )
                // s 4.35 -0.5 5.02 -2.61
                reflectiveCurveToRelative(
                    dx1 = 4.35f,
                    dy1 = -0.5f,
                    dx2 = 5.02f,
                    dy2 = -2.61f,
                )
                // c 0.13 -0.39 0.19 -0.81 0.19 -1.22
                curveToRelative(
                    dx1 = 0.13f,
                    dy1 = -0.39f,
                    dx2 = 0.19f,
                    dy2 = -0.81f,
                    dx3 = 0.19f,
                    dy3 = -1.22f,
                )
                // c -0.16 -1.73 -0.5 -3.44 -1.09 -5.08
                curveToRelative(
                    dx1 = -0.16f,
                    dy1 = -1.73f,
                    dx2 = -0.5f,
                    dy2 = -3.44f,
                    dx3 = -1.09f,
                    dy3 = -5.08f,
                )
                // A 18.8 18.8 0 0 1 8 7
                arcTo(
                    horizontalEllipseRadius = 18.8f,
                    verticalEllipseRadius = 18.8f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 8.0f,
                    y1 = 7.0f,
                )
                // a 3.163 3.163 0 0 1 3 -3
                arcToRelative(
                    a = 3.163f,
                    b = 3.163f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 3.0f,
                    dy1 = -3.0f,
                )
                // c 1.62 0.08 2.92 1.38 3 3
                curveToRelative(
                    dx1 = 1.62f,
                    dy1 = 0.08f,
                    dx2 = 2.92f,
                    dy2 = 1.38f,
                    dx3 = 3.0f,
                    dy3 = 3.0f,
                )
                // a 22.6 22.6 0 0 1 -1 4.74
                arcToRelative(
                    a = 22.6f,
                    b = 22.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 4.74f,
                )
                // c -0.54 1.71 -0.87 3.47 -1 5.26
                curveToRelative(
                    dx1 = -0.54f,
                    dy1 = 1.71f,
                    dx2 = -0.87f,
                    dy2 = 3.47f,
                    dx3 = -1.0f,
                    dy3 = 5.26f,
                )
                // a 5.136 5.136 0 0 0 5 5
                arcToRelative(
                    a = 5.136f,
                    b = 5.136f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 5.0f,
                    dy1 = 5.0f,
                )
                // a 5 5 0 0 0 5 -5z
                arcToRelative(
                    a = 5.0f,
                    b = 5.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 5.0f,
                    dy1 = -5.0f,
                )
                close()
                // M 6 18
                moveTo(x = 6.0f, y = 18.0f)
                // a 2 2 0 0 1 -2 -2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.0f,
                    dy1 = -2.0f,
                )
                // a 2 2 0 0 1 2 -2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.0f,
                    dy1 = -2.0f,
                )
                // a 2 2 0 0 1 2 2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.0f,
                    dy1 = 2.0f,
                )
                // a 2 2 0 0 1 -2 2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.0f,
                    dy1 = 2.0f,
                )
            }
        }.build().also { _leo = it }
    }

@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLeo,
            contentDescription = null,
            modifier = Modifier
                .width((24.0).dp)
                .height((24.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _leo: ImageVector? = null
