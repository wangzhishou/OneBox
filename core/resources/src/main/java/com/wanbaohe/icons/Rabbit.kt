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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRabbit

val Icons.Filled.Rabbit: ImageVector
    get() {
        val current = _rabbit
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Rabbit",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // m18.05 21 l-2.73 -4.74 c0 -1.73 -1.07 -2.84 -2.37 -2.84 c-.9 0 -1.68 .5 -2.08 1.24 c.33 -.19 .72 -.29 1.13 -.29 c1.3 0 2.36 1.06 2.36 2.36 c0 1.31 -1.05 2.38 -2.36 2.38 h3.3 V21 H6.79 c-.24 0 -.49 -.09 -.67 -.28 a.95 .95 0 0 1 0 -1.34 l.5 -.5 c-.34 -.15 -.62 -.38 -.9 -.62 c-.22 .5 -.72 .85 -1.3 .85 a1.425 1.425 0 0 1 0 -2.85 l.47 .08 v-1.97 a4.73 4.73 0 0 1 4.74 -4.74 h.02 c2.12 .01 3.77 .84 3.77 -.47 c0 -.93 .2 -1.3 .54 -1.82 c-.73 -.34 -1.56 -.55 -2.43 -.55 c-.53 0 -.95 -.42 -.95 -.95 c0 -.43 .28 -.79 .67 -.91 l-.67 -.04 c-.52 0 -.95 -.42 -.95 -.94 c0 -.53 .43 -.95 .95 -.95 h.95 c2.1 0 3.94 1.15 4.93 2.85 l.28 -.01 c.71 0 1.37 .23 1.91 .61 l.45 .38 c2.17 1.95 1.9 3.27 1.9 3.28 c0 1.28 -1.06 2.33 -2.35 2.33 l-.49 -.05 v.08 c0 1.11 -.48 2.1 -1.23 2.8 L20.24 21z m.11 -13.26 c-.53 0 -.95 .42 -.95 .94 c0 .53 .42 .95 .95 .95 c.52 0 .95 -.42 .95 -.95 c0 -.52 -.43 -.94 -.95 -.94
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 18.05 21
                moveTo(x = 18.05f, y = 21.0f)
                // l -2.73 -4.74
                lineToRelative(dx = -2.73f, dy = -4.74f)
                // c 0 -1.73 -1.07 -2.84 -2.37 -2.84
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.73f,
                    dx2 = -1.07f,
                    dy2 = -2.84f,
                    dx3 = -2.37f,
                    dy3 = -2.84f,
                )
                // c -0.9 0 -1.68 0.5 -2.08 1.24
                curveToRelative(
                    dx1 = -0.9f,
                    dy1 = 0.0f,
                    dx2 = -1.68f,
                    dy2 = 0.5f,
                    dx3 = -2.08f,
                    dy3 = 1.24f,
                )
                // c 0.33 -0.19 0.72 -0.29 1.13 -0.29
                curveToRelative(
                    dx1 = 0.33f,
                    dy1 = -0.19f,
                    dx2 = 0.72f,
                    dy2 = -0.29f,
                    dx3 = 1.13f,
                    dy3 = -0.29f,
                )
                // c 1.3 0 2.36 1.06 2.36 2.36
                curveToRelative(
                    dx1 = 1.3f,
                    dy1 = 0.0f,
                    dx2 = 2.36f,
                    dy2 = 1.06f,
                    dx3 = 2.36f,
                    dy3 = 2.36f,
                )
                // c 0 1.31 -1.05 2.38 -2.36 2.38
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.31f,
                    dx2 = -1.05f,
                    dy2 = 2.38f,
                    dx3 = -2.36f,
                    dy3 = 2.38f,
                )
                // h 3.3
                horizontalLineToRelative(dx = 3.3f)
                // V 21
                verticalLineTo(y = 21.0f)
                // H 6.79
                horizontalLineTo(x = 6.79f)
                // c -0.24 0 -0.49 -0.09 -0.67 -0.28
                curveToRelative(
                    dx1 = -0.24f,
                    dy1 = 0.0f,
                    dx2 = -0.49f,
                    dy2 = -0.09f,
                    dx3 = -0.67f,
                    dy3 = -0.28f,
                )
                // a 0.95 0.95 0 0 1 0 -1.34
                arcToRelative(
                    a = 0.95f,
                    b = 0.95f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.34f,
                )
                // l 0.5 -0.5
                lineToRelative(dx = 0.5f, dy = -0.5f)
                // c -0.34 -0.15 -0.62 -0.38 -0.9 -0.62
                curveToRelative(
                    dx1 = -0.34f,
                    dy1 = -0.15f,
                    dx2 = -0.62f,
                    dy2 = -0.38f,
                    dx3 = -0.9f,
                    dy3 = -0.62f,
                )
                // c -0.22 0.5 -0.72 0.85 -1.3 0.85
                curveToRelative(
                    dx1 = -0.22f,
                    dy1 = 0.5f,
                    dx2 = -0.72f,
                    dy2 = 0.85f,
                    dx3 = -1.3f,
                    dy3 = 0.85f,
                )
                // a 1.425 1.425 0 0 1 0 -2.85
                arcToRelative(
                    a = 1.425f,
                    b = 1.425f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -2.85f,
                )
                // l 0.47 0.08
                lineToRelative(dx = 0.47f, dy = 0.08f)
                // v -1.97
                verticalLineToRelative(dy = -1.97f)
                // a 4.73 4.73 0 0 1 4.74 -4.74
                arcToRelative(
                    a = 4.73f,
                    b = 4.73f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 4.74f,
                    dy1 = -4.74f,
                )
                // h 0.02
                horizontalLineToRelative(dx = 0.02f)
                // c 2.12 0.01 3.77 0.84 3.77 -0.47
                curveToRelative(
                    dx1 = 2.12f,
                    dy1 = 0.01f,
                    dx2 = 3.77f,
                    dy2 = 0.84f,
                    dx3 = 3.77f,
                    dy3 = -0.47f,
                )
                // c 0 -0.93 0.2 -1.3 0.54 -1.82
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.93f,
                    dx2 = 0.2f,
                    dy2 = -1.3f,
                    dx3 = 0.54f,
                    dy3 = -1.82f,
                )
                // c -0.73 -0.34 -1.56 -0.55 -2.43 -0.55
                curveToRelative(
                    dx1 = -0.73f,
                    dy1 = -0.34f,
                    dx2 = -1.56f,
                    dy2 = -0.55f,
                    dx3 = -2.43f,
                    dy3 = -0.55f,
                )
                // c -0.53 0 -0.95 -0.42 -0.95 -0.95
                curveToRelative(
                    dx1 = -0.53f,
                    dy1 = 0.0f,
                    dx2 = -0.95f,
                    dy2 = -0.42f,
                    dx3 = -0.95f,
                    dy3 = -0.95f,
                )
                // c 0 -0.43 0.28 -0.79 0.67 -0.91
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.43f,
                    dx2 = 0.28f,
                    dy2 = -0.79f,
                    dx3 = 0.67f,
                    dy3 = -0.91f,
                )
                // l -0.67 -0.04
                lineToRelative(dx = -0.67f, dy = -0.04f)
                // c -0.52 0 -0.95 -0.42 -0.95 -0.94
                curveToRelative(
                    dx1 = -0.52f,
                    dy1 = 0.0f,
                    dx2 = -0.95f,
                    dy2 = -0.42f,
                    dx3 = -0.95f,
                    dy3 = -0.94f,
                )
                // c 0 -0.53 0.43 -0.95 0.95 -0.95
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.53f,
                    dx2 = 0.43f,
                    dy2 = -0.95f,
                    dx3 = 0.95f,
                    dy3 = -0.95f,
                )
                // h 0.95
                horizontalLineToRelative(dx = 0.95f)
                // c 2.1 0 3.94 1.15 4.93 2.85
                curveToRelative(
                    dx1 = 2.1f,
                    dy1 = 0.0f,
                    dx2 = 3.94f,
                    dy2 = 1.15f,
                    dx3 = 4.93f,
                    dy3 = 2.85f,
                )
                // l 0.28 -0.01
                lineToRelative(dx = 0.28f, dy = -0.01f)
                // c 0.71 0 1.37 0.23 1.91 0.61
                curveToRelative(
                    dx1 = 0.71f,
                    dy1 = 0.0f,
                    dx2 = 1.37f,
                    dy2 = 0.23f,
                    dx3 = 1.91f,
                    dy3 = 0.61f,
                )
                // l 0.45 0.38
                lineToRelative(dx = 0.45f, dy = 0.38f)
                // c 2.17 1.95 1.9 3.27 1.9 3.28
                curveToRelative(
                    dx1 = 2.17f,
                    dy1 = 1.95f,
                    dx2 = 1.9f,
                    dy2 = 3.27f,
                    dx3 = 1.9f,
                    dy3 = 3.28f,
                )
                // c 0 1.28 -1.06 2.33 -2.35 2.33
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.28f,
                    dx2 = -1.06f,
                    dy2 = 2.33f,
                    dx3 = -2.35f,
                    dy3 = 2.33f,
                )
                // l -0.49 -0.05
                lineToRelative(dx = -0.49f, dy = -0.05f)
                // v 0.08
                verticalLineToRelative(dy = 0.08f)
                // c 0 1.11 -0.48 2.1 -1.23 2.8
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.11f,
                    dx2 = -0.48f,
                    dy2 = 2.1f,
                    dx3 = -1.23f,
                    dy3 = 2.8f,
                )
                // L 20.24 21z
                lineTo(x = 20.24f, y = 21.0f)
                close()
                // m 0.11 -13.26
                moveToRelative(dx = 0.11f, dy = -13.26f)
                // c -0.53 0 -0.95 0.42 -0.95 0.94
                curveToRelative(
                    dx1 = -0.53f,
                    dy1 = 0.0f,
                    dx2 = -0.95f,
                    dy2 = 0.42f,
                    dx3 = -0.95f,
                    dy3 = 0.94f,
                )
                // c 0 0.53 0.42 0.95 0.95 0.95
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.53f,
                    dx2 = 0.42f,
                    dy2 = 0.95f,
                    dx3 = 0.95f,
                    dy3 = 0.95f,
                )
                // c 0.52 0 0.95 -0.42 0.95 -0.95
                curveToRelative(
                    dx1 = 0.52f,
                    dy1 = 0.0f,
                    dx2 = 0.95f,
                    dy2 = -0.42f,
                    dx3 = 0.95f,
                    dy3 = -0.95f,
                )
                // c 0 -0.52 -0.43 -0.94 -0.95 -0.94
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.52f,
                    dx2 = -0.43f,
                    dy2 = -0.94f,
                    dx3 = -0.95f,
                    dy3 = -0.94f,
                )
            }
        }.build().also { _rabbit = it }
    }

@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRabbit,
            contentDescription = null,
            modifier = Modifier
                .width((24.0).dp)
                .height((24.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _rabbit: ImageVector? = null
