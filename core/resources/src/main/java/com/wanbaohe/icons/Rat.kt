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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRat

val Icons.Filled.Rat: ImageVector
    get() {
        val current = _rat
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Rat",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M21.33 17.39 c1.4 1.27 .47 3.61 -1.41 3.61 h-8.86 A5.04 5.04 0 0 1 6 15.94 v-.05 c-2.3 -.47 -4 -2.48 -4 -4.89 c0 -2.75 2.22 -5 5 -5 h2.5 c.3 0 .5 -.23 .5 -.5 S9.8 5 9.5 5 H7 V3 h2.5 C10.88 3 12 4.13 12 5.5 C12 6.89 10.88 8 9.5 8 H7 c-1.66 0 -3 1.33 -3 3 c0 1.37 .92 2.5 2.14 2.87 c.56 -2.2 2.53 -3.87 4.92 -3.87 c.8 0 1.6 .22 2.3 .55 c-1.41 .79 -2.36 2.25 -2.36 3.95 c0 1.25 .5 2.37 1.33 3.17 l.7 -.7 c-.65 -.61 -1.03 -1.5 -1.03 -2.47 c0 -2.59 2.34 -3.5 3.5 -3.5 c2.08 0 3.95 1.89 3.44 4.23z M18 19 c.56 0 1 -.44 1 -1 s-.44 -1 -1 -1 s-1 .44 -1 1 s.44 1 1 1
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 21.33 17.39
                moveTo(x = 21.33f, y = 17.39f)
                // c 1.4 1.27 0.47 3.61 -1.41 3.61
                curveToRelative(
                    dx1 = 1.4f,
                    dy1 = 1.27f,
                    dx2 = 0.47f,
                    dy2 = 3.61f,
                    dx3 = -1.41f,
                    dy3 = 3.61f,
                )
                // h -8.86
                horizontalLineToRelative(dx = -8.86f)
                // A 5.04 5.04 0 0 1 6 15.94
                arcTo(
                    horizontalEllipseRadius = 5.04f,
                    verticalEllipseRadius = 5.04f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 6.0f,
                    y1 = 15.94f,
                )
                // v -0.05
                verticalLineToRelative(dy = -0.05f)
                // c -2.3 -0.47 -4 -2.48 -4 -4.89
                curveToRelative(
                    dx1 = -2.3f,
                    dy1 = -0.47f,
                    dx2 = -4.0f,
                    dy2 = -2.48f,
                    dx3 = -4.0f,
                    dy3 = -4.89f,
                )
                // c 0 -2.75 2.22 -5 5 -5
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -2.75f,
                    dx2 = 2.22f,
                    dy2 = -5.0f,
                    dx3 = 5.0f,
                    dy3 = -5.0f,
                )
                // h 2.5
                horizontalLineToRelative(dx = 2.5f)
                // c 0.3 0 0.5 -0.23 0.5 -0.5
                curveToRelative(
                    dx1 = 0.3f,
                    dy1 = 0.0f,
                    dx2 = 0.5f,
                    dy2 = -0.23f,
                    dx3 = 0.5f,
                    dy3 = -0.5f,
                )
                // S 9.8 5 9.5 5
                reflectiveCurveTo(
                    x1 = 9.8f,
                    y1 = 5.0f,
                    x2 = 9.5f,
                    y2 = 5.0f,
                )
                // H 7
                horizontalLineTo(x = 7.0f)
                // V 3
                verticalLineTo(y = 3.0f)
                // h 2.5
                horizontalLineToRelative(dx = 2.5f)
                // C 10.88 3 12 4.13 12 5.5
                curveTo(
                    x1 = 10.88f,
                    y1 = 3.0f,
                    x2 = 12.0f,
                    y2 = 4.13f,
                    x3 = 12.0f,
                    y3 = 5.5f,
                )
                // C 12 6.89 10.88 8 9.5 8
                curveTo(
                    x1 = 12.0f,
                    y1 = 6.89f,
                    x2 = 10.88f,
                    y2 = 8.0f,
                    x3 = 9.5f,
                    y3 = 8.0f,
                )
                // H 7
                horizontalLineTo(x = 7.0f)
                // c -1.66 0 -3 1.33 -3 3
                curveToRelative(
                    dx1 = -1.66f,
                    dy1 = 0.0f,
                    dx2 = -3.0f,
                    dy2 = 1.33f,
                    dx3 = -3.0f,
                    dy3 = 3.0f,
                )
                // c 0 1.37 0.92 2.5 2.14 2.87
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.37f,
                    dx2 = 0.92f,
                    dy2 = 2.5f,
                    dx3 = 2.14f,
                    dy3 = 2.87f,
                )
                // c 0.56 -2.2 2.53 -3.87 4.92 -3.87
                curveToRelative(
                    dx1 = 0.56f,
                    dy1 = -2.2f,
                    dx2 = 2.53f,
                    dy2 = -3.87f,
                    dx3 = 4.92f,
                    dy3 = -3.87f,
                )
                // c 0.8 0 1.6 0.22 2.3 0.55
                curveToRelative(
                    dx1 = 0.8f,
                    dy1 = 0.0f,
                    dx2 = 1.6f,
                    dy2 = 0.22f,
                    dx3 = 2.3f,
                    dy3 = 0.55f,
                )
                // c -1.41 0.79 -2.36 2.25 -2.36 3.95
                curveToRelative(
                    dx1 = -1.41f,
                    dy1 = 0.79f,
                    dx2 = -2.36f,
                    dy2 = 2.25f,
                    dx3 = -2.36f,
                    dy3 = 3.95f,
                )
                // c 0 1.25 0.5 2.37 1.33 3.17
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.25f,
                    dx2 = 0.5f,
                    dy2 = 2.37f,
                    dx3 = 1.33f,
                    dy3 = 3.17f,
                )
                // l 0.7 -0.7
                lineToRelative(dx = 0.7f, dy = -0.7f)
                // c -0.65 -0.61 -1.03 -1.5 -1.03 -2.47
                curveToRelative(
                    dx1 = -0.65f,
                    dy1 = -0.61f,
                    dx2 = -1.03f,
                    dy2 = -1.5f,
                    dx3 = -1.03f,
                    dy3 = -2.47f,
                )
                // c 0 -2.59 2.34 -3.5 3.5 -3.5
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -2.59f,
                    dx2 = 2.34f,
                    dy2 = -3.5f,
                    dx3 = 3.5f,
                    dy3 = -3.5f,
                )
                // c 2.08 0 3.95 1.89 3.44 4.23z
                curveToRelative(
                    dx1 = 2.08f,
                    dy1 = 0.0f,
                    dx2 = 3.95f,
                    dy2 = 1.89f,
                    dx3 = 3.44f,
                    dy3 = 4.23f,
                )
                close()
                // M 18 19
                moveTo(x = 18.0f, y = 19.0f)
                // c 0.56 0 1 -0.44 1 -1
                curveToRelative(
                    dx1 = 0.56f,
                    dy1 = 0.0f,
                    dx2 = 1.0f,
                    dy2 = -0.44f,
                    dx3 = 1.0f,
                    dy3 = -1.0f,
                )
                // s -0.44 -1 -1 -1
                reflectiveCurveToRelative(
                    dx1 = -0.44f,
                    dy1 = -1.0f,
                    dx2 = -1.0f,
                    dy2 = -1.0f,
                )
                // s -1 0.44 -1 1
                reflectiveCurveToRelative(
                    dx1 = -1.0f,
                    dy1 = 0.44f,
                    dx2 = -1.0f,
                    dy2 = 1.0f,
                )
                // s 0.44 1 1 1
                reflectiveCurveToRelative(
                    dx1 = 0.44f,
                    dy1 = 1.0f,
                    dx2 = 1.0f,
                    dy2 = 1.0f,
                )
            }
        }.build().also { _rat = it }
    }

@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRat,
            contentDescription = null,
            modifier = Modifier
                .width((24.0).dp)
                .height((24.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _rat: ImageVector? = null
