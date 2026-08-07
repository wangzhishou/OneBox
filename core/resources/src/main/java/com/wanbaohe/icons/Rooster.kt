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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRooster

val Icons.Filled.Rooster: ImageVector
    get() {
        val current = _rooster
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Rooster",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // m23 11.5 l-3.05 -1.13 c-.26 -1.15 -.91 -1.81 -.91 -1.81 a4.19 4.19 0 0 0 -5.93 0 l-1.48 1.48 L5 3 c-1 4 0 8 2.45 11.22 L2 19.5 s8.89 2 14.07 -2.05 c2.76 -2.16 3.38 -3.42 3.77 -4.75z m-5.29 .22 c-.39 .39 -1.03 .39 -1.42 0 a.996 .996 0 0 1 0 -1.41 c.39 -.39 1.03 -.39 1.42 0 s.39 1.02 0 1.41
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 23 11.5
                moveTo(x = 23.0f, y = 11.5f)
                // l -3.05 -1.13
                lineToRelative(dx = -3.05f, dy = -1.13f)
                // c -0.26 -1.15 -0.91 -1.81 -0.91 -1.81
                curveToRelative(
                    dx1 = -0.26f,
                    dy1 = -1.15f,
                    dx2 = -0.91f,
                    dy2 = -1.81f,
                    dx3 = -0.91f,
                    dy3 = -1.81f,
                )
                // a 4.19 4.19 0 0 0 -5.93 0
                arcToRelative(
                    a = 4.19f,
                    b = 4.19f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -5.93f,
                    dy1 = 0.0f,
                )
                // l -1.48 1.48
                lineToRelative(dx = -1.48f, dy = 1.48f)
                // L 5 3
                lineTo(x = 5.0f, y = 3.0f)
                // c -1 4 0 8 2.45 11.22
                curveToRelative(
                    dx1 = -1.0f,
                    dy1 = 4.0f,
                    dx2 = 0.0f,
                    dy2 = 8.0f,
                    dx3 = 2.45f,
                    dy3 = 11.22f,
                )
                // L 2 19.5
                lineTo(x = 2.0f, y = 19.5f)
                // s 8.89 2 14.07 -2.05
                reflectiveCurveToRelative(
                    dx1 = 8.89f,
                    dy1 = 2.0f,
                    dx2 = 14.07f,
                    dy2 = -2.05f,
                )
                // c 2.76 -2.16 3.38 -3.42 3.77 -4.75z
                curveToRelative(
                    dx1 = 2.76f,
                    dy1 = -2.16f,
                    dx2 = 3.38f,
                    dy2 = -3.42f,
                    dx3 = 3.77f,
                    dy3 = -4.75f,
                )
                close()
                // m -5.29 0.22
                moveToRelative(dx = -5.29f, dy = 0.22f)
                // c -0.39 0.39 -1.03 0.39 -1.42 0
                curveToRelative(
                    dx1 = -0.39f,
                    dy1 = 0.39f,
                    dx2 = -1.03f,
                    dy2 = 0.39f,
                    dx3 = -1.42f,
                    dy3 = 0.0f,
                )
                // a 0.996 0.996 0 0 1 0 -1.41
                arcToRelative(
                    a = 0.996f,
                    b = 0.996f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.41f,
                )
                // c 0.39 -0.39 1.03 -0.39 1.42 0
                curveToRelative(
                    dx1 = 0.39f,
                    dy1 = -0.39f,
                    dx2 = 1.03f,
                    dy2 = -0.39f,
                    dx3 = 1.42f,
                    dy3 = 0.0f,
                )
                // s 0.39 1.02 0 1.41
                reflectiveCurveToRelative(
                    dx1 = 0.39f,
                    dy1 = 1.02f,
                    dx2 = 0.0f,
                    dy2 = 1.41f,
                )
            }
        }.build().also { _rooster = it }
    }

@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRooster,
            contentDescription = null,
            modifier = Modifier
                .width((24.0).dp)
                .height((24.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _rooster: ImageVector? = null
