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
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePig

val Icons.Filled.Pig: ImageVector
    get() {
        val current = _pig
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Pig",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M9.5 9 A1.5 1.5 0 0 0 8 10.5 A1.5 1.5 0 0 0 9.5 12 a1.5 1.5 0 0 0 1.5 -1.5 A1.5 1.5 0 0 0 9.5 9 m5 0 a1.5 1.5 0 0 0 -1.5 1.5 a1.5 1.5 0 0 0 1.5 1.5 a1.5 1.5 0 0 0 1.5 -1.5 A1.5 1.5 0 0 0 14.5 9 M12 4 l.68 .03 c.94 -.79 2.14 -1.44 3.04 -1.68 c1.87 -.5 5.16 -.12 5.59 1.48 c.31 1.17 -.71 2.62 -2.28 3.55 A8.97 8.97 0 0 1 21 13 a9 9 0 0 1 -9 9 a9 9 0 0 1 -9 -9 c0 -2.13 .74 -4.08 1.97 -5.62 C3.4 6.45 2.38 5 2.69 3.83 c.43 -1.6 3.72 -1.98 5.59 -1.48 c.9 .24 2.1 .89 3.04 1.68z m-2 12 a1 1 0 0 1 1 1 a1 1 0 0 1 -1 1 a1 1 0 0 1 -1 -1 a1 1 0 0 1 1 -1 m4 0 a1 1 0 0 1 1 1 a1 1 0 0 1 -1 1 a1 1 0 0 1 -1 -1 a1 1 0 0 1 1 -1 m-2 -3 c-2.76 0 -5 2.34 -5 4 s2.24 3 5 3 s5 -1.34 5 -3 s-2.24 -4 -5 -4 M7.76 4.28 c-.45 -.12 -3.17 .07 -3.17 .07 S6.8 6.1 7.24 6.22 c.45 .12 2.53 .21 2.67 -.32 c.15 -.54 -1.71 -1.5 -2.15 -1.62 m8.48 0 c-.44 .12 -2.3 1.08 -2.15 1.62 c.14 .53 2.22 .44 2.67 .32 c.44 -.12 2.65 -1.87 2.65 -1.87 s-2.72 -.19 -3.17 -.07
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 9.5 9
                moveTo(x = 9.5f, y = 9.0f)
                // A 1.5 1.5 0 0 0 8 10.5
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 8.0f,
                    y1 = 10.5f,
                )
                // A 1.5 1.5 0 0 0 9.5 12
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 9.5f,
                    y1 = 12.0f,
                )
                // a 1.5 1.5 0 0 0 1.5 -1.5
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.5f,
                    dy1 = -1.5f,
                )
                // A 1.5 1.5 0 0 0 9.5 9
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 9.5f,
                    y1 = 9.0f,
                )
                // m 5 0
                moveToRelative(dx = 5.0f, dy = 0.0f)
                // a 1.5 1.5 0 0 0 -1.5 1.5
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.5f,
                    dy1 = 1.5f,
                )
                // a 1.5 1.5 0 0 0 1.5 1.5
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.5f,
                    dy1 = 1.5f,
                )
                // a 1.5 1.5 0 0 0 1.5 -1.5
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.5f,
                    dy1 = -1.5f,
                )
                // A 1.5 1.5 0 0 0 14.5 9
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 14.5f,
                    y1 = 9.0f,
                )
                // M 12 4
                moveTo(x = 12.0f, y = 4.0f)
                // l 0.68 0.03
                lineToRelative(dx = 0.68f, dy = 0.03f)
                // c 0.94 -0.79 2.14 -1.44 3.04 -1.68
                curveToRelative(
                    dx1 = 0.94f,
                    dy1 = -0.79f,
                    dx2 = 2.14f,
                    dy2 = -1.44f,
                    dx3 = 3.04f,
                    dy3 = -1.68f,
                )
                // c 1.87 -0.5 5.16 -0.12 5.59 1.48
                curveToRelative(
                    dx1 = 1.87f,
                    dy1 = -0.5f,
                    dx2 = 5.16f,
                    dy2 = -0.12f,
                    dx3 = 5.59f,
                    dy3 = 1.48f,
                )
                // c 0.31 1.17 -0.71 2.62 -2.28 3.55
                curveToRelative(
                    dx1 = 0.31f,
                    dy1 = 1.17f,
                    dx2 = -0.71f,
                    dy2 = 2.62f,
                    dx3 = -2.28f,
                    dy3 = 3.55f,
                )
                // A 8.97 8.97 0 0 1 21 13
                arcTo(
                    horizontalEllipseRadius = 8.97f,
                    verticalEllipseRadius = 8.97f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 21.0f,
                    y1 = 13.0f,
                )
                // a 9 9 0 0 1 -9 9
                arcToRelative(
                    a = 9.0f,
                    b = 9.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -9.0f,
                    dy1 = 9.0f,
                )
                // a 9 9 0 0 1 -9 -9
                arcToRelative(
                    a = 9.0f,
                    b = 9.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -9.0f,
                    dy1 = -9.0f,
                )
                // c 0 -2.13 0.74 -4.08 1.97 -5.62
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -2.13f,
                    dx2 = 0.74f,
                    dy2 = -4.08f,
                    dx3 = 1.97f,
                    dy3 = -5.62f,
                )
                // C 3.4 6.45 2.38 5 2.69 3.83
                curveTo(
                    x1 = 3.4f,
                    y1 = 6.45f,
                    x2 = 2.38f,
                    y2 = 5.0f,
                    x3 = 2.69f,
                    y3 = 3.83f,
                )
                // c 0.43 -1.6 3.72 -1.98 5.59 -1.48
                curveToRelative(
                    dx1 = 0.43f,
                    dy1 = -1.6f,
                    dx2 = 3.72f,
                    dy2 = -1.98f,
                    dx3 = 5.59f,
                    dy3 = -1.48f,
                )
                // c 0.9 0.24 2.1 0.89 3.04 1.68z
                curveToRelative(
                    dx1 = 0.9f,
                    dy1 = 0.24f,
                    dx2 = 2.1f,
                    dy2 = 0.89f,
                    dx3 = 3.04f,
                    dy3 = 1.68f,
                )
                close()
                // m -2 12
                moveToRelative(dx = -2.0f, dy = 12.0f)
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
                // m -2 -3
                moveToRelative(dx = -2.0f, dy = -3.0f)
                // c -2.76 0 -5 2.34 -5 4
                curveToRelative(
                    dx1 = -2.76f,
                    dy1 = 0.0f,
                    dx2 = -5.0f,
                    dy2 = 2.34f,
                    dx3 = -5.0f,
                    dy3 = 4.0f,
                )
                // s 2.24 3 5 3
                reflectiveCurveToRelative(
                    dx1 = 2.24f,
                    dy1 = 3.0f,
                    dx2 = 5.0f,
                    dy2 = 3.0f,
                )
                // s 5 -1.34 5 -3
                reflectiveCurveToRelative(
                    dx1 = 5.0f,
                    dy1 = -1.34f,
                    dx2 = 5.0f,
                    dy2 = -3.0f,
                )
                // s -2.24 -4 -5 -4
                reflectiveCurveToRelative(
                    dx1 = -2.24f,
                    dy1 = -4.0f,
                    dx2 = -5.0f,
                    dy2 = -4.0f,
                )
                // M 7.76 4.28
                moveTo(x = 7.76f, y = 4.28f)
                // c -0.45 -0.12 -3.17 0.07 -3.17 0.07
                curveToRelative(
                    dx1 = -0.45f,
                    dy1 = -0.12f,
                    dx2 = -3.17f,
                    dy2 = 0.07f,
                    dx3 = -3.17f,
                    dy3 = 0.07f,
                )
                // S 6.8 6.1 7.24 6.22
                reflectiveCurveTo(
                    x1 = 6.8f,
                    y1 = 6.1f,
                    x2 = 7.24f,
                    y2 = 6.22f,
                )
                // c 0.45 0.12 2.53 0.21 2.67 -0.32
                curveToRelative(
                    dx1 = 0.45f,
                    dy1 = 0.12f,
                    dx2 = 2.53f,
                    dy2 = 0.21f,
                    dx3 = 2.67f,
                    dy3 = -0.32f,
                )
                // c 0.15 -0.54 -1.71 -1.5 -2.15 -1.62
                curveToRelative(
                    dx1 = 0.15f,
                    dy1 = -0.54f,
                    dx2 = -1.71f,
                    dy2 = -1.5f,
                    dx3 = -2.15f,
                    dy3 = -1.62f,
                )
                // m 8.48 0
                moveToRelative(dx = 8.48f, dy = 0.0f)
                // c -0.44 0.12 -2.3 1.08 -2.15 1.62
                curveToRelative(
                    dx1 = -0.44f,
                    dy1 = 0.12f,
                    dx2 = -2.3f,
                    dy2 = 1.08f,
                    dx3 = -2.15f,
                    dy3 = 1.62f,
                )
                // c 0.14 0.53 2.22 0.44 2.67 0.32
                curveToRelative(
                    dx1 = 0.14f,
                    dy1 = 0.53f,
                    dx2 = 2.22f,
                    dy2 = 0.44f,
                    dx3 = 2.67f,
                    dy3 = 0.32f,
                )
                // c 0.44 -0.12 2.65 -1.87 2.65 -1.87
                curveToRelative(
                    dx1 = 0.44f,
                    dy1 = -0.12f,
                    dx2 = 2.65f,
                    dy2 = -1.87f,
                    dx3 = 2.65f,
                    dy3 = -1.87f,
                )
                // s -2.72 -0.19 -3.17 -0.07
                reflectiveCurveToRelative(
                    dx1 = -2.72f,
                    dy1 = -0.19f,
                    dx2 = -3.17f,
                    dy2 = -0.07f,
                )
            }
        }.build().also { _pig = it }
    }

@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePig,
            contentDescription = null,
            modifier = Modifier
                .width((24.0).dp)
                .height((24.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _pig: ImageVector? = null
