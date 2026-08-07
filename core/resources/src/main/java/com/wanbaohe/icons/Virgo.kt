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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVirgo

val Icons.Filled.Virgo: ImageVector
    get() {
        val current = _virgo
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Virgo",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M18.5 19.13 C20 17.77 20 15.18 20 14 a4 4 0 0 0 -4 -4 c-.7 0 -1.4 .2 -2 .56 V6 a3 3 0 0 0 -3 -3 c-.75 0 -1.45 .29 -2 .78 a2.997 2.997 0 0 0 -4 0 C4.45 3.28 3.74 3 3 3 v2 a1 1 0 0 1 1 1 v10 h2 V6 a1 1 0 0 1 1 -1 a1 1 0 0 1 1 1 v10 h2 V6 a1 1 0 0 1 1 -1 a1 1 0 0 1 1 1 v8 c0 1.18 0 3.77 1.5 5.13 c-.78 .41 -1.62 .71 -2.5 .87 v2 c1.29 0 3.84 -1.26 5 -1.87 c1.16 .61 3.71 1.87 5 1.87 v-2 c-.88 -.16 -1.72 -.46 -2.5 -.87 M16 12 a2 2 0 0 1 2 2 c0 2.92 -.54 4 -2 4 s-2 -1.08 -2 -4 a2 2 0 0 1 2 -2
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 18.5 19.13
                moveTo(x = 18.5f, y = 19.13f)
                // C 20 17.77 20 15.18 20 14
                curveTo(
                    x1 = 20.0f,
                    y1 = 17.77f,
                    x2 = 20.0f,
                    y2 = 15.18f,
                    x3 = 20.0f,
                    y3 = 14.0f,
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
                // c -0.7 0 -1.4 0.2 -2 0.56
                curveToRelative(
                    dx1 = -0.7f,
                    dy1 = 0.0f,
                    dx2 = -1.4f,
                    dy2 = 0.2f,
                    dx3 = -2.0f,
                    dy3 = 0.56f,
                )
                // V 6
                verticalLineTo(y = 6.0f)
                // a 3 3 0 0 0 -3 -3
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -3.0f,
                    dy1 = -3.0f,
                )
                // c -0.75 0 -1.45 0.29 -2 0.78
                curveToRelative(
                    dx1 = -0.75f,
                    dy1 = 0.0f,
                    dx2 = -1.45f,
                    dy2 = 0.29f,
                    dx3 = -2.0f,
                    dy3 = 0.78f,
                )
                // a 2.997 2.997 0 0 0 -4 0
                arcToRelative(
                    a = 2.997f,
                    b = 2.997f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -4.0f,
                    dy1 = 0.0f,
                )
                // C 4.45 3.28 3.74 3 3 3
                curveTo(
                    x1 = 4.45f,
                    y1 = 3.28f,
                    x2 = 3.74f,
                    y2 = 3.0f,
                    x3 = 3.0f,
                    y3 = 3.0f,
                )
                // v 2
                verticalLineToRelative(dy = 2.0f)
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
                // v 10
                verticalLineToRelative(dy = 10.0f)
                // h 2
                horizontalLineToRelative(dx = 2.0f)
                // V 6
                verticalLineTo(y = 6.0f)
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
                // v 10
                verticalLineToRelative(dy = 10.0f)
                // h 2
                horizontalLineToRelative(dx = 2.0f)
                // V 6
                verticalLineTo(y = 6.0f)
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
                // v 8
                verticalLineToRelative(dy = 8.0f)
                // c 0 1.18 0 3.77 1.5 5.13
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.18f,
                    dx2 = 0.0f,
                    dy2 = 3.77f,
                    dx3 = 1.5f,
                    dy3 = 5.13f,
                )
                // c -0.78 0.41 -1.62 0.71 -2.5 0.87
                curveToRelative(
                    dx1 = -0.78f,
                    dy1 = 0.41f,
                    dx2 = -1.62f,
                    dy2 = 0.71f,
                    dx3 = -2.5f,
                    dy3 = 0.87f,
                )
                // v 2
                verticalLineToRelative(dy = 2.0f)
                // c 1.29 0 3.84 -1.26 5 -1.87
                curveToRelative(
                    dx1 = 1.29f,
                    dy1 = 0.0f,
                    dx2 = 3.84f,
                    dy2 = -1.26f,
                    dx3 = 5.0f,
                    dy3 = -1.87f,
                )
                // c 1.16 0.61 3.71 1.87 5 1.87
                curveToRelative(
                    dx1 = 1.16f,
                    dy1 = 0.61f,
                    dx2 = 3.71f,
                    dy2 = 1.87f,
                    dx3 = 5.0f,
                    dy3 = 1.87f,
                )
                // v -2
                verticalLineToRelative(dy = -2.0f)
                // c -0.88 -0.16 -1.72 -0.46 -2.5 -0.87
                curveToRelative(
                    dx1 = -0.88f,
                    dy1 = -0.16f,
                    dx2 = -1.72f,
                    dy2 = -0.46f,
                    dx3 = -2.5f,
                    dy3 = -0.87f,
                )
                // M 16 12
                moveTo(x = 16.0f, y = 12.0f)
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
                // c 0 2.92 -0.54 4 -2 4
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 2.92f,
                    dx2 = -0.54f,
                    dy2 = 4.0f,
                    dx3 = -2.0f,
                    dy3 = 4.0f,
                )
                // s -2 -1.08 -2 -4
                reflectiveCurveToRelative(
                    dx1 = -2.0f,
                    dy1 = -1.08f,
                    dx2 = -2.0f,
                    dy2 = -4.0f,
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
            }
        }.build().also { _virgo = it }
    }

@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVirgo,
            contentDescription = null,
            modifier = Modifier
                .width((24.0).dp)
                .height((24.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _virgo: ImageVector? = null
