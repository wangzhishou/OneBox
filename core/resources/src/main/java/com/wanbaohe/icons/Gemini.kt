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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGemini

val Icons.Filled.Gemini: ImageVector
    get() {
        val current = _gemini
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Gemini",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M18 5.3 c1.35 -.33 2.66 -.76 3.94 -1.3 l-.76 -1.86 a23.75 23.75 0 0 1 -18.36 .03 L2.06 4 c1.28 .54 2.59 .97 3.94 1.3 v13.4 c-1.35 .33 -2.66 .76 -3.94 1.3 l.76 1.86 a23.94 23.94 0 0 1 18.36 0 l.76 -1.86 c-1.28 -.54 -2.59 -.97 -3.94 -1.3z m-10 13 V5.69 c1.32 .2 2.66 .31 4 .31 s2.68 -.11 4 -.31 v12.62 a26.2 26.2 0 0 0 -8 0z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 18 5.3
                moveTo(x = 18.0f, y = 5.3f)
                // c 1.35 -0.33 2.66 -0.76 3.94 -1.3
                curveToRelative(
                    dx1 = 1.35f,
                    dy1 = -0.33f,
                    dx2 = 2.66f,
                    dy2 = -0.76f,
                    dx3 = 3.94f,
                    dy3 = -1.3f,
                )
                // l -0.76 -1.86
                lineToRelative(dx = -0.76f, dy = -1.86f)
                // a 23.75 23.75 0 0 1 -18.36 0.03
                arcToRelative(
                    a = 23.75f,
                    b = 23.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -18.36f,
                    dy1 = 0.03f,
                )
                // L 2.06 4
                lineTo(x = 2.06f, y = 4.0f)
                // c 1.28 0.54 2.59 0.97 3.94 1.3
                curveToRelative(
                    dx1 = 1.28f,
                    dy1 = 0.54f,
                    dx2 = 2.59f,
                    dy2 = 0.97f,
                    dx3 = 3.94f,
                    dy3 = 1.3f,
                )
                // v 13.4
                verticalLineToRelative(dy = 13.4f)
                // c -1.35 0.33 -2.66 0.76 -3.94 1.3
                curveToRelative(
                    dx1 = -1.35f,
                    dy1 = 0.33f,
                    dx2 = -2.66f,
                    dy2 = 0.76f,
                    dx3 = -3.94f,
                    dy3 = 1.3f,
                )
                // l 0.76 1.86
                lineToRelative(dx = 0.76f, dy = 1.86f)
                // a 23.94 23.94 0 0 1 18.36 0
                arcToRelative(
                    a = 23.94f,
                    b = 23.94f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 18.36f,
                    dy1 = 0.0f,
                )
                // l 0.76 -1.86
                lineToRelative(dx = 0.76f, dy = -1.86f)
                // c -1.28 -0.54 -2.59 -0.97 -3.94 -1.3z
                curveToRelative(
                    dx1 = -1.28f,
                    dy1 = -0.54f,
                    dx2 = -2.59f,
                    dy2 = -0.97f,
                    dx3 = -3.94f,
                    dy3 = -1.3f,
                )
                close()
                // m -10 13
                moveToRelative(dx = -10.0f, dy = 13.0f)
                // V 5.69
                verticalLineTo(y = 5.69f)
                // c 1.32 0.2 2.66 0.31 4 0.31
                curveToRelative(
                    dx1 = 1.32f,
                    dy1 = 0.2f,
                    dx2 = 2.66f,
                    dy2 = 0.31f,
                    dx3 = 4.0f,
                    dy3 = 0.31f,
                )
                // s 2.68 -0.11 4 -0.31
                reflectiveCurveToRelative(
                    dx1 = 2.68f,
                    dy1 = -0.11f,
                    dx2 = 4.0f,
                    dy2 = -0.31f,
                )
                // v 12.62
                verticalLineToRelative(dy = 12.62f)
                // a 26.2 26.2 0 0 0 -8 0z
                arcToRelative(
                    a = 26.2f,
                    b = 26.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -8.0f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _gemini = it }
    }

@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGemini,
            contentDescription = null,
            modifier = Modifier
                .width((24.0).dp)
                .height((24.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _gemini: ImageVector? = null
