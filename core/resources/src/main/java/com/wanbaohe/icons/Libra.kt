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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLibra

val Icons.Filled.Libra: ImageVector
    get() {
        val current = _libra
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Libra",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M20 16 v2 h-7 v-2.09 c3 -.55 4.96 -3.41 4.41 -6.41 S14 4.54 11 5.09 C8 5.65 6.04 8.5 6.59 11.5 c.41 2.24 2.17 4 4.41 4.41 V18 H4 v-2 h2.92 a7.43 7.43 0 0 1 -2.42 -5.5 A7.5 7.5 0 0 1 12 3 a7.5 7.5 0 0 1 7.5 7.5 c0 2.09 -.87 4.09 -2.42 5.5z m0 3 H4 v2 h16z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 20 16
                moveTo(x = 20.0f, y = 16.0f)
                // v 2
                verticalLineToRelative(dy = 2.0f)
                // h -7
                horizontalLineToRelative(dx = -7.0f)
                // v -2.09
                verticalLineToRelative(dy = -2.09f)
                // c 3 -0.55 4.96 -3.41 4.41 -6.41
                curveToRelative(
                    dx1 = 3.0f,
                    dy1 = -0.55f,
                    dx2 = 4.96f,
                    dy2 = -3.41f,
                    dx3 = 4.41f,
                    dy3 = -6.41f,
                )
                // S 14 4.54 11 5.09
                reflectiveCurveTo(
                    x1 = 14.0f,
                    y1 = 4.54f,
                    x2 = 11.0f,
                    y2 = 5.09f,
                )
                // C 8 5.65 6.04 8.5 6.59 11.5
                curveTo(
                    x1 = 8.0f,
                    y1 = 5.65f,
                    x2 = 6.04f,
                    y2 = 8.5f,
                    x3 = 6.59f,
                    y3 = 11.5f,
                )
                // c 0.41 2.24 2.17 4 4.41 4.41
                curveToRelative(
                    dx1 = 0.41f,
                    dy1 = 2.24f,
                    dx2 = 2.17f,
                    dy2 = 4.0f,
                    dx3 = 4.41f,
                    dy3 = 4.41f,
                )
                // V 18
                verticalLineTo(y = 18.0f)
                // H 4
                horizontalLineTo(x = 4.0f)
                // v -2
                verticalLineToRelative(dy = -2.0f)
                // h 2.92
                horizontalLineToRelative(dx = 2.92f)
                // a 7.43 7.43 0 0 1 -2.42 -5.5
                arcToRelative(
                    a = 7.43f,
                    b = 7.43f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.42f,
                    dy1 = -5.5f,
                )
                // A 7.5 7.5 0 0 1 12 3
                arcTo(
                    horizontalEllipseRadius = 7.5f,
                    verticalEllipseRadius = 7.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 12.0f,
                    y1 = 3.0f,
                )
                // a 7.5 7.5 0 0 1 7.5 7.5
                arcToRelative(
                    a = 7.5f,
                    b = 7.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 7.5f,
                    dy1 = 7.5f,
                )
                // c 0 2.09 -0.87 4.09 -2.42 5.5z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 2.09f,
                    dx2 = -0.87f,
                    dy2 = 4.09f,
                    dx3 = -2.42f,
                    dy3 = 5.5f,
                )
                close()
                // m 0 3
                moveToRelative(dx = 0.0f, dy = 3.0f)
                // H 4
                horizontalLineTo(x = 4.0f)
                // v 2
                verticalLineToRelative(dy = 2.0f)
                // h 16z
                horizontalLineToRelative(dx = 16.0f)
                close()
            }
        }.build().also { _libra = it }
    }

@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLibra,
            contentDescription = null,
            modifier = Modifier
                .width((24.0).dp)
                .height((24.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _libra: ImageVector? = null
