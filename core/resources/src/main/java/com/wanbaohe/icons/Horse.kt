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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHorse

val Icons.Filled.Horse: ImageVector
    get() {
        val current = _horse
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Horse",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M22 6 v3.5 l-1.5 .5 l-1.54 -2.46 c-.13 -.21 -.46 -.12 -.46 .13 v3.58 c0 .98 -.39 1.86 -1 2.53 V21 H15 v-6 h-.25 c-.21 0 -.42 -.03 -.62 -.06 l-4.44 -.74 l-1.12 2.01 l.96 4.79 H7 l-1 -4.75 c-.03 -.3 0 -.6 .16 -.86 l1.02 -1.81 a3.27 3.27 0 0 1 -1.68 -2.77 c-.04 .15 -.06 .37 -.03 .69 c.03 .44 .14 1.09 .07 1.81 c-.04 .72 -.37 1.46 -.79 1.95 c-.43 .49 -.9 .83 -1.4 1.09 l-.7 -.7 c.19 -.47 .38 -.89 .42 -1.28 c.06 -.37 -.01 -.67 -.12 -.94 l-.53 -1.13 c-.21 -.51 -.47 -1.25 -.42 -2.12 c.03 -.85 .5 -1.96 1.39 -2.57 c.9 -.61 1.87 -.69 2.66 -.53 c.5 .1 1.01 .34 1.45 .68 c.37 -.17 .8 -.26 1.25 -.26 h5.75 V7 c0 -2.21 1.79 -4 4 -4 H22 l-.89 1.34 c.54 .36 .89 .97 .89 1.66
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 22 6
                moveTo(x = 22.0f, y = 6.0f)
                // v 3.5
                verticalLineToRelative(dy = 3.5f)
                // l -1.5 0.5
                lineToRelative(dx = -1.5f, dy = 0.5f)
                // l -1.54 -2.46
                lineToRelative(dx = -1.54f, dy = -2.46f)
                // c -0.13 -0.21 -0.46 -0.12 -0.46 0.13
                curveToRelative(
                    dx1 = -0.13f,
                    dy1 = -0.21f,
                    dx2 = -0.46f,
                    dy2 = -0.12f,
                    dx3 = -0.46f,
                    dy3 = 0.13f,
                )
                // v 3.58
                verticalLineToRelative(dy = 3.58f)
                // c 0 0.98 -0.39 1.86 -1 2.53
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.98f,
                    dx2 = -0.39f,
                    dy2 = 1.86f,
                    dx3 = -1.0f,
                    dy3 = 2.53f,
                )
                // V 21
                verticalLineTo(y = 21.0f)
                // H 15
                horizontalLineTo(x = 15.0f)
                // v -6
                verticalLineToRelative(dy = -6.0f)
                // h -0.25
                horizontalLineToRelative(dx = -0.25f)
                // c -0.21 0 -0.42 -0.03 -0.62 -0.06
                curveToRelative(
                    dx1 = -0.21f,
                    dy1 = 0.0f,
                    dx2 = -0.42f,
                    dy2 = -0.03f,
                    dx3 = -0.62f,
                    dy3 = -0.06f,
                )
                // l -4.44 -0.74
                lineToRelative(dx = -4.44f, dy = -0.74f)
                // l -1.12 2.01
                lineToRelative(dx = -1.12f, dy = 2.01f)
                // l 0.96 4.79
                lineToRelative(dx = 0.96f, dy = 4.79f)
                // H 7
                horizontalLineTo(x = 7.0f)
                // l -1 -4.75
                lineToRelative(dx = -1.0f, dy = -4.75f)
                // c -0.03 -0.3 0 -0.6 0.16 -0.86
                curveToRelative(
                    dx1 = -0.03f,
                    dy1 = -0.3f,
                    dx2 = 0.0f,
                    dy2 = -0.6f,
                    dx3 = 0.16f,
                    dy3 = -0.86f,
                )
                // l 1.02 -1.81
                lineToRelative(dx = 1.02f, dy = -1.81f)
                // a 3.27 3.27 0 0 1 -1.68 -2.77
                arcToRelative(
                    a = 3.27f,
                    b = 3.27f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.68f,
                    dy1 = -2.77f,
                )
                // c -0.04 0.15 -0.06 0.37 -0.03 0.69
                curveToRelative(
                    dx1 = -0.04f,
                    dy1 = 0.15f,
                    dx2 = -0.06f,
                    dy2 = 0.37f,
                    dx3 = -0.03f,
                    dy3 = 0.69f,
                )
                // c 0.03 0.44 0.14 1.09 0.07 1.81
                curveToRelative(
                    dx1 = 0.03f,
                    dy1 = 0.44f,
                    dx2 = 0.14f,
                    dy2 = 1.09f,
                    dx3 = 0.07f,
                    dy3 = 1.81f,
                )
                // c -0.04 0.72 -0.37 1.46 -0.79 1.95
                curveToRelative(
                    dx1 = -0.04f,
                    dy1 = 0.72f,
                    dx2 = -0.37f,
                    dy2 = 1.46f,
                    dx3 = -0.79f,
                    dy3 = 1.95f,
                )
                // c -0.43 0.49 -0.9 0.83 -1.4 1.09
                curveToRelative(
                    dx1 = -0.43f,
                    dy1 = 0.49f,
                    dx2 = -0.9f,
                    dy2 = 0.83f,
                    dx3 = -1.4f,
                    dy3 = 1.09f,
                )
                // l -0.7 -0.7
                lineToRelative(dx = -0.7f, dy = -0.7f)
                // c 0.19 -0.47 0.38 -0.89 0.42 -1.28
                curveToRelative(
                    dx1 = 0.19f,
                    dy1 = -0.47f,
                    dx2 = 0.38f,
                    dy2 = -0.89f,
                    dx3 = 0.42f,
                    dy3 = -1.28f,
                )
                // c 0.06 -0.37 -0.01 -0.67 -0.12 -0.94
                curveToRelative(
                    dx1 = 0.06f,
                    dy1 = -0.37f,
                    dx2 = -0.01f,
                    dy2 = -0.67f,
                    dx3 = -0.12f,
                    dy3 = -0.94f,
                )
                // l -0.53 -1.13
                lineToRelative(dx = -0.53f, dy = -1.13f)
                // c -0.21 -0.51 -0.47 -1.25 -0.42 -2.12
                curveToRelative(
                    dx1 = -0.21f,
                    dy1 = -0.51f,
                    dx2 = -0.47f,
                    dy2 = -1.25f,
                    dx3 = -0.42f,
                    dy3 = -2.12f,
                )
                // c 0.03 -0.85 0.5 -1.96 1.39 -2.57
                curveToRelative(
                    dx1 = 0.03f,
                    dy1 = -0.85f,
                    dx2 = 0.5f,
                    dy2 = -1.96f,
                    dx3 = 1.39f,
                    dy3 = -2.57f,
                )
                // c 0.9 -0.61 1.87 -0.69 2.66 -0.53
                curveToRelative(
                    dx1 = 0.9f,
                    dy1 = -0.61f,
                    dx2 = 1.87f,
                    dy2 = -0.69f,
                    dx3 = 2.66f,
                    dy3 = -0.53f,
                )
                // c 0.5 0.1 1.01 0.34 1.45 0.68
                curveToRelative(
                    dx1 = 0.5f,
                    dy1 = 0.1f,
                    dx2 = 1.01f,
                    dy2 = 0.34f,
                    dx3 = 1.45f,
                    dy3 = 0.68f,
                )
                // c 0.37 -0.17 0.8 -0.26 1.25 -0.26
                curveToRelative(
                    dx1 = 0.37f,
                    dy1 = -0.17f,
                    dx2 = 0.8f,
                    dy2 = -0.26f,
                    dx3 = 1.25f,
                    dy3 = -0.26f,
                )
                // h 5.75
                horizontalLineToRelative(dx = 5.75f)
                // V 7
                verticalLineTo(y = 7.0f)
                // c 0 -2.21 1.79 -4 4 -4
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -2.21f,
                    dx2 = 1.79f,
                    dy2 = -4.0f,
                    dx3 = 4.0f,
                    dy3 = -4.0f,
                )
                // H 22
                horizontalLineTo(x = 22.0f)
                // l -0.89 1.34
                lineToRelative(dx = -0.89f, dy = 1.34f)
                // c 0.54 0.36 0.89 0.97 0.89 1.66
                curveToRelative(
                    dx1 = 0.54f,
                    dy1 = 0.36f,
                    dx2 = 0.89f,
                    dy2 = 0.97f,
                    dx3 = 0.89f,
                    dy3 = 1.66f,
                )
            }
        }.build().also { _horse = it }
    }

@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHorse,
            contentDescription = null,
            modifier = Modifier
                .width((24.0).dp)
                .height((24.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _horse: ImageVector? = null
