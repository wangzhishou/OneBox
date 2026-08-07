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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAries

val Icons.Filled.Aries: ImageVector
    get() {
        val current = _aries
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Aries",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M16 2 c-1.05 0 -2.09 .27 -3 .81 c-.36 .19 -.7 .45 -1 .73 c-.3 -.28 -.64 -.54 -1 -.73 C10.09 2.27 9.05 2 8 2 a6 6 0 0 0 -6 6 a6 6 0 0 0 6 6 v-2 a4 4 0 0 1 -4 -4 a4 4 0 0 1 4 -4 a4.03 4.03 0 0 1 3 1.36 V22 h2 V5.36 c.08 -.09 .16 -.18 .25 -.26 a4 4 0 0 1 5.66 .15 a3.997 3.997 0 0 1 -.15 5.65 C18 11.61 17.03 12 16 12 v2 a6 6 0 0 0 6 -6 a6 6 0 0 0 -6 -6
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 16 2
                moveTo(x = 16.0f, y = 2.0f)
                // c -1.05 0 -2.09 0.27 -3 0.81
                curveToRelative(
                    dx1 = -1.05f,
                    dy1 = 0.0f,
                    dx2 = -2.09f,
                    dy2 = 0.27f,
                    dx3 = -3.0f,
                    dy3 = 0.81f,
                )
                // c -0.36 0.19 -0.7 0.45 -1 0.73
                curveToRelative(
                    dx1 = -0.36f,
                    dy1 = 0.19f,
                    dx2 = -0.7f,
                    dy2 = 0.45f,
                    dx3 = -1.0f,
                    dy3 = 0.73f,
                )
                // c -0.3 -0.28 -0.64 -0.54 -1 -0.73
                curveToRelative(
                    dx1 = -0.3f,
                    dy1 = -0.28f,
                    dx2 = -0.64f,
                    dy2 = -0.54f,
                    dx3 = -1.0f,
                    dy3 = -0.73f,
                )
                // C 10.09 2.27 9.05 2 8 2
                curveTo(
                    x1 = 10.09f,
                    y1 = 2.27f,
                    x2 = 9.05f,
                    y2 = 2.0f,
                    x3 = 8.0f,
                    y3 = 2.0f,
                )
                // a 6 6 0 0 0 -6 6
                arcToRelative(
                    a = 6.0f,
                    b = 6.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -6.0f,
                    dy1 = 6.0f,
                )
                // a 6 6 0 0 0 6 6
                arcToRelative(
                    a = 6.0f,
                    b = 6.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 6.0f,
                    dy1 = 6.0f,
                )
                // v -2
                verticalLineToRelative(dy = -2.0f)
                // a 4 4 0 0 1 -4 -4
                arcToRelative(
                    a = 4.0f,
                    b = 4.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -4.0f,
                    dy1 = -4.0f,
                )
                // a 4 4 0 0 1 4 -4
                arcToRelative(
                    a = 4.0f,
                    b = 4.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 4.0f,
                    dy1 = -4.0f,
                )
                // a 4.03 4.03 0 0 1 3 1.36
                arcToRelative(
                    a = 4.03f,
                    b = 4.03f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 3.0f,
                    dy1 = 1.36f,
                )
                // V 22
                verticalLineTo(y = 22.0f)
                // h 2
                horizontalLineToRelative(dx = 2.0f)
                // V 5.36
                verticalLineTo(y = 5.36f)
                // c 0.08 -0.09 0.16 -0.18 0.25 -0.26
                curveToRelative(
                    dx1 = 0.08f,
                    dy1 = -0.09f,
                    dx2 = 0.16f,
                    dy2 = -0.18f,
                    dx3 = 0.25f,
                    dy3 = -0.26f,
                )
                // a 4 4 0 0 1 5.66 0.15
                arcToRelative(
                    a = 4.0f,
                    b = 4.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 5.66f,
                    dy1 = 0.15f,
                )
                // a 3.997 3.997 0 0 1 -0.15 5.65
                arcToRelative(
                    a = 3.997f,
                    b = 3.997f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.15f,
                    dy1 = 5.65f,
                )
                // C 18 11.61 17.03 12 16 12
                curveTo(
                    x1 = 18.0f,
                    y1 = 11.61f,
                    x2 = 17.03f,
                    y2 = 12.0f,
                    x3 = 16.0f,
                    y3 = 12.0f,
                )
                // v 2
                verticalLineToRelative(dy = 2.0f)
                // a 6 6 0 0 0 6 -6
                arcToRelative(
                    a = 6.0f,
                    b = 6.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 6.0f,
                    dy1 = -6.0f,
                )
                // a 6 6 0 0 0 -6 -6
                arcToRelative(
                    a = 6.0f,
                    b = 6.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -6.0f,
                    dy1 = -6.0f,
                )
            }
        }.build().also { _aries = it }
    }

@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAries,
            contentDescription = null,
            modifier = Modifier
                .width((24.0).dp)
                .height((24.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _aries: ImageVector? = null
