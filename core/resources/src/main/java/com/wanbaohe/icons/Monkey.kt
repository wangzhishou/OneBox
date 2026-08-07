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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMonkey

val Icons.Filled.Monkey: ImageVector
    get() {
        val current = _monkey
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Monkey",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 48.0f,
            viewportHeight = 48.0f,
        ).apply {
            // M21.593 18.135 c1.34 -1.465 2.915 -4.978 -1.508 -7.32 c-1.006 -.652 -1.585 -2.315 -2.514 -5.37 C13.718 3.332 5 3 4 14 v30
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineWidth = 4.0f,
            ) {
                // M 21.593 18.135
                moveTo(x = 21.593f, y = 18.135f)
                // c 1.34 -1.465 2.915 -4.978 -1.508 -7.32
                curveToRelative(
                    dx1 = 1.34f,
                    dy1 = -1.465f,
                    dx2 = 2.915f,
                    dy2 = -4.978f,
                    dx3 = -1.508f,
                    dy3 = -7.32f,
                )
                // c -1.006 -0.652 -1.585 -2.315 -2.514 -5.37
                curveToRelative(
                    dx1 = -1.006f,
                    dy1 = -0.652f,
                    dx2 = -1.585f,
                    dy2 = -2.315f,
                    dx3 = -2.514f,
                    dy3 = -5.37f,
                )
                // C 13.718 3.332 5 3 4 14
                curveTo(
                    x1 = 13.718f,
                    y1 = 3.332f,
                    x2 = 5.0f,
                    y2 = 3.0f,
                    x3 = 4.0f,
                    y3 = 14.0f,
                )
                // v 30
                verticalLineToRelative(dy = 30.0f)
            }
            // M17 25 c4.582 1.673 11.033 5.254 15 16 c1.533 3.754 8.507 4.87 11.5 -1 c.998 -1.957 .5 -5.496 -3.918 -7.55 C36.462 31 34 26 38.5 24 c1.848 -.603 3.93 .08 5.5 3
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineWidth = 4.0f,
            ) {
                // M 17 25
                moveTo(x = 17.0f, y = 25.0f)
                // c 4.582 1.673 11.033 5.254 15 16
                curveToRelative(
                    dx1 = 4.582f,
                    dy1 = 1.673f,
                    dx2 = 11.033f,
                    dy2 = 5.254f,
                    dx3 = 15.0f,
                    dy3 = 16.0f,
                )
                // c 1.533 3.754 8.507 4.87 11.5 -1
                curveToRelative(
                    dx1 = 1.533f,
                    dy1 = 3.754f,
                    dx2 = 8.507f,
                    dy2 = 4.87f,
                    dx3 = 11.5f,
                    dy3 = -1.0f,
                )
                // c 0.998 -1.957 0.5 -5.496 -3.918 -7.55
                curveToRelative(
                    dx1 = 0.998f,
                    dy1 = -1.957f,
                    dx2 = 0.5f,
                    dy2 = -5.496f,
                    dx3 = -3.918f,
                    dy3 = -7.55f,
                )
                // C 36.462 31 34 26 38.5 24
                curveTo(
                    x1 = 36.462f,
                    y1 = 31.0f,
                    x2 = 34.0f,
                    y2 = 26.0f,
                    x3 = 38.5f,
                    y3 = 24.0f,
                )
                // c 1.848 -0.603 3.93 0.08 5.5 3
                curveToRelative(
                    dx1 = 1.848f,
                    dy1 = -0.603f,
                    dx2 = 3.93f,
                    dy2 = 0.08f,
                    dx3 = 5.5f,
                    dy3 = 3.0f,
                )
            }
            // M29 35 c-4.345 -1.106 -13.228 -.481 -14 9
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineWidth = 4.0f,
            ) {
                // M 29 35
                moveTo(x = 29.0f, y = 35.0f)
                // c -4.345 -1.106 -13.228 -0.481 -14 9
                curveToRelative(
                    dx1 = -4.345f,
                    dy1 = -1.106f,
                    dx2 = -13.228f,
                    dy2 = -0.481f,
                    dx3 = -14.0f,
                    dy3 = 9.0f,
                )
            }
        }.build().also { _monkey = it }
    }

@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMonkey,
            contentDescription = null,
            modifier = Modifier
                .width((48.0).dp)
                .height((48.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _monkey: ImageVector? = null
