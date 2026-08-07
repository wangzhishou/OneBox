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
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePisces

val Icons.Filled.Pisces: ImageVector
    get() {
        val current = _pisces
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Pisces",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M20 11 h-2 c.11 -2.81 .73 -5.58 1.81 -8.18 L18 2.06 A26 26 0 0 0 16 11 H8 c-.13 -3.08 -.81 -6.1 -2 -8.94 l-1.86 .76 C5.24 5.41 5.87 8.18 6 11 H4 v2 h2 a23.8 23.8 0 0 1 -1.81 8.18 l1.81 .76 C7.19 19.1 7.87 16.08 8 13 h8 c.13 3.08 .81 6.1 2 8.94 l1.86 -.76 c-1.1 -2.59 -1.73 -5.36 -1.86 -8.18 h2z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 20 11
                moveTo(x = 20.0f, y = 11.0f)
                // h -2
                horizontalLineToRelative(dx = -2.0f)
                // c 0.11 -2.81 0.73 -5.58 1.81 -8.18
                curveToRelative(
                    dx1 = 0.11f,
                    dy1 = -2.81f,
                    dx2 = 0.73f,
                    dy2 = -5.58f,
                    dx3 = 1.81f,
                    dy3 = -8.18f,
                )
                // L 18 2.06
                lineTo(x = 18.0f, y = 2.06f)
                // A 26 26 0 0 0 16 11
                arcTo(
                    horizontalEllipseRadius = 26.0f,
                    verticalEllipseRadius = 26.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 16.0f,
                    y1 = 11.0f,
                )
                // H 8
                horizontalLineTo(x = 8.0f)
                // c -0.13 -3.08 -0.81 -6.1 -2 -8.94
                curveToRelative(
                    dx1 = -0.13f,
                    dy1 = -3.08f,
                    dx2 = -0.81f,
                    dy2 = -6.1f,
                    dx3 = -2.0f,
                    dy3 = -8.94f,
                )
                // l -1.86 0.76
                lineToRelative(dx = -1.86f, dy = 0.76f)
                // C 5.24 5.41 5.87 8.18 6 11
                curveTo(
                    x1 = 5.24f,
                    y1 = 5.41f,
                    x2 = 5.87f,
                    y2 = 8.18f,
                    x3 = 6.0f,
                    y3 = 11.0f,
                )
                // H 4
                horizontalLineTo(x = 4.0f)
                // v 2
                verticalLineToRelative(dy = 2.0f)
                // h 2
                horizontalLineToRelative(dx = 2.0f)
                // a 23.8 23.8 0 0 1 -1.81 8.18
                arcToRelative(
                    a = 23.8f,
                    b = 23.8f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.81f,
                    dy1 = 8.18f,
                )
                // l 1.81 0.76
                lineToRelative(dx = 1.81f, dy = 0.76f)
                // C 7.19 19.1 7.87 16.08 8 13
                curveTo(
                    x1 = 7.19f,
                    y1 = 19.1f,
                    x2 = 7.87f,
                    y2 = 16.08f,
                    x3 = 8.0f,
                    y3 = 13.0f,
                )
                // h 8
                horizontalLineToRelative(dx = 8.0f)
                // c 0.13 3.08 0.81 6.1 2 8.94
                curveToRelative(
                    dx1 = 0.13f,
                    dy1 = 3.08f,
                    dx2 = 0.81f,
                    dy2 = 6.1f,
                    dx3 = 2.0f,
                    dy3 = 8.94f,
                )
                // l 1.86 -0.76
                lineToRelative(dx = 1.86f, dy = -0.76f)
                // c -1.1 -2.59 -1.73 -5.36 -1.86 -8.18
                curveToRelative(
                    dx1 = -1.1f,
                    dy1 = -2.59f,
                    dx2 = -1.73f,
                    dy2 = -5.36f,
                    dx3 = -1.86f,
                    dy3 = -8.18f,
                )
                // h 2z
                horizontalLineToRelative(dx = 2.0f)
                close()
            }
        }.build().also { _pisces = it }
    }

@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePisces,
            contentDescription = null,
            modifier = Modifier
                .width((24.0).dp)
                .height((24.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _pisces: ImageVector? = null
