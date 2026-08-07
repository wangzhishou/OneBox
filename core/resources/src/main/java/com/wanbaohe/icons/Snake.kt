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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSnake

val Icons.Filled.Snake: ImageVector
    get() {
        val current = _snake
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Snake",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M19.5 17 H18 c1.1 0 2 -.9 2 -2 s-.9 -2 -2 -2 V9 c0 -1 0 -2 -1.08 -2.86 c.05 -.21 .08 -.42 .08 -.64 C17 3.57 15 2 12.5 2 c-2.26 0 -4.12 1.31 -4.43 3 H6 L3.71 2.79 L3 3.5 l2 2 l-2 2 l.71 .71 L6 6 h2.07 c.31 1.69 2.17 3 4.43 3 c.5 0 1 -.08 1.43 -.2 c.04 .07 .07 .14 .07 .2 v4 H8 c-1.1 0 -2 .9 -2 2 s.9 2 2 2 H6.5 A2.5 2.5 0 0 0 4 19.5 c0 .17 0 .34 .05 .5 H4 c-1.1 0 -2 .9 -2 2 h17.5 a2.5 2.5 0 0 0 0 -5 M12 5 c-.55 0 -1 -.45 -1 -1 s.45 -1 1 -1 s1 .45 1 1 s-.45 1 -1 1
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 19.5 17
                moveTo(x = 19.5f, y = 17.0f)
                // H 18
                horizontalLineTo(x = 18.0f)
                // c 1.1 0 2 -0.9 2 -2
                curveToRelative(
                    dx1 = 1.1f,
                    dy1 = 0.0f,
                    dx2 = 2.0f,
                    dy2 = -0.9f,
                    dx3 = 2.0f,
                    dy3 = -2.0f,
                )
                // s -0.9 -2 -2 -2
                reflectiveCurveToRelative(
                    dx1 = -0.9f,
                    dy1 = -2.0f,
                    dx2 = -2.0f,
                    dy2 = -2.0f,
                )
                // V 9
                verticalLineTo(y = 9.0f)
                // c 0 -1 0 -2 -1.08 -2.86
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                    dx2 = 0.0f,
                    dy2 = -2.0f,
                    dx3 = -1.08f,
                    dy3 = -2.86f,
                )
                // c 0.05 -0.21 0.08 -0.42 0.08 -0.64
                curveToRelative(
                    dx1 = 0.05f,
                    dy1 = -0.21f,
                    dx2 = 0.08f,
                    dy2 = -0.42f,
                    dx3 = 0.08f,
                    dy3 = -0.64f,
                )
                // C 17 3.57 15 2 12.5 2
                curveTo(
                    x1 = 17.0f,
                    y1 = 3.57f,
                    x2 = 15.0f,
                    y2 = 2.0f,
                    x3 = 12.5f,
                    y3 = 2.0f,
                )
                // c -2.26 0 -4.12 1.31 -4.43 3
                curveToRelative(
                    dx1 = -2.26f,
                    dy1 = 0.0f,
                    dx2 = -4.12f,
                    dy2 = 1.31f,
                    dx3 = -4.43f,
                    dy3 = 3.0f,
                )
                // H 6
                horizontalLineTo(x = 6.0f)
                // L 3.71 2.79
                lineTo(x = 3.71f, y = 2.79f)
                // L 3 3.5
                lineTo(x = 3.0f, y = 3.5f)
                // l 2 2
                lineToRelative(dx = 2.0f, dy = 2.0f)
                // l -2 2
                lineToRelative(dx = -2.0f, dy = 2.0f)
                // l 0.71 0.71
                lineToRelative(dx = 0.71f, dy = 0.71f)
                // L 6 6
                lineTo(x = 6.0f, y = 6.0f)
                // h 2.07
                horizontalLineToRelative(dx = 2.07f)
                // c 0.31 1.69 2.17 3 4.43 3
                curveToRelative(
                    dx1 = 0.31f,
                    dy1 = 1.69f,
                    dx2 = 2.17f,
                    dy2 = 3.0f,
                    dx3 = 4.43f,
                    dy3 = 3.0f,
                )
                // c 0.5 0 1 -0.08 1.43 -0.2
                curveToRelative(
                    dx1 = 0.5f,
                    dy1 = 0.0f,
                    dx2 = 1.0f,
                    dy2 = -0.08f,
                    dx3 = 1.43f,
                    dy3 = -0.2f,
                )
                // c 0.04 0.07 0.07 0.14 0.07 0.2
                curveToRelative(
                    dx1 = 0.04f,
                    dy1 = 0.07f,
                    dx2 = 0.07f,
                    dy2 = 0.14f,
                    dx3 = 0.07f,
                    dy3 = 0.2f,
                )
                // v 4
                verticalLineToRelative(dy = 4.0f)
                // H 8
                horizontalLineTo(x = 8.0f)
                // c -1.1 0 -2 0.9 -2 2
                curveToRelative(
                    dx1 = -1.1f,
                    dy1 = 0.0f,
                    dx2 = -2.0f,
                    dy2 = 0.9f,
                    dx3 = -2.0f,
                    dy3 = 2.0f,
                )
                // s 0.9 2 2 2
                reflectiveCurveToRelative(
                    dx1 = 0.9f,
                    dy1 = 2.0f,
                    dx2 = 2.0f,
                    dy2 = 2.0f,
                )
                // H 6.5
                horizontalLineTo(x = 6.5f)
                // A 2.5 2.5 0 0 0 4 19.5
                arcTo(
                    horizontalEllipseRadius = 2.5f,
                    verticalEllipseRadius = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 4.0f,
                    y1 = 19.5f,
                )
                // c 0 0.17 0 0.34 0.05 0.5
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.17f,
                    dx2 = 0.0f,
                    dy2 = 0.34f,
                    dx3 = 0.05f,
                    dy3 = 0.5f,
                )
                // H 4
                horizontalLineTo(x = 4.0f)
                // c -1.1 0 -2 0.9 -2 2
                curveToRelative(
                    dx1 = -1.1f,
                    dy1 = 0.0f,
                    dx2 = -2.0f,
                    dy2 = 0.9f,
                    dx3 = -2.0f,
                    dy3 = 2.0f,
                )
                // h 17.5
                horizontalLineToRelative(dx = 17.5f)
                // a 2.5 2.5 0 0 0 0 -5
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -5.0f,
                )
                // M 12 5
                moveTo(x = 12.0f, y = 5.0f)
                // c -0.55 0 -1 -0.45 -1 -1
                curveToRelative(
                    dx1 = -0.55f,
                    dy1 = 0.0f,
                    dx2 = -1.0f,
                    dy2 = -0.45f,
                    dx3 = -1.0f,
                    dy3 = -1.0f,
                )
                // s 0.45 -1 1 -1
                reflectiveCurveToRelative(
                    dx1 = 0.45f,
                    dy1 = -1.0f,
                    dx2 = 1.0f,
                    dy2 = -1.0f,
                )
                // s 1 0.45 1 1
                reflectiveCurveToRelative(
                    dx1 = 1.0f,
                    dy1 = 0.45f,
                    dx2 = 1.0f,
                    dy2 = 1.0f,
                )
                // s -0.45 1 -1 1
                reflectiveCurveToRelative(
                    dx1 = -0.45f,
                    dy1 = 1.0f,
                    dx2 = -1.0f,
                    dy2 = 1.0f,
                )
            }
        }.build().also { _snake = it }
    }

@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSnake,
            contentDescription = null,
            modifier = Modifier
                .width((24.0).dp)
                .height((24.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _snake: ImageVector? = null
