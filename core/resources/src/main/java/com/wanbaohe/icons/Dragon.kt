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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDragon

val Icons.Filled.Dragon: ImageVector
    get() {
        val current = _dragon
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Dragon",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 48.0f,
            viewportHeight = 48.0f,
        ).apply {
            // m34.021 42.494 l3.74 -3.74 a6 6 0 0 0 0 -8.485 v0 a6 6 0 0 0 -8.485 0 L27.045 32.5 m-9.97 -7 l6.544 -6.544 a6 6 0 0 0 0 -8.486 v0 a6 6 0 0 0 -8.485 0 l-7.071 7.071 m9.012 7.959 L8.77 33.806 a6 6 0 0 0 0 8.485 v0 a6 6 0 0 0 8.485 0 l9.766 -9.766
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineCap = StrokeCap.Round,
                strokeLineWidth = 4.0f,
            ) {
                // M 34.021 42.494
                moveTo(x = 34.021f, y = 42.494f)
                // l 3.74 -3.74
                lineToRelative(dx = 3.74f, dy = -3.74f)
                // a 6 6 0 0 0 0 -8.485
                arcToRelative(
                    a = 6.0f,
                    b = 6.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -8.485f,
                )
                // v 0
                verticalLineToRelative(dy = 0.0f)
                // a 6 6 0 0 0 -8.485 0
                arcToRelative(
                    a = 6.0f,
                    b = 6.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -8.485f,
                    dy1 = 0.0f,
                )
                // L 27.045 32.5
                lineTo(x = 27.045f, y = 32.5f)
                // m -9.97 -7
                moveToRelative(dx = -9.97f, dy = -7.0f)
                // l 6.544 -6.544
                lineToRelative(dx = 6.544f, dy = -6.544f)
                // a 6 6 0 0 0 0 -8.486
                arcToRelative(
                    a = 6.0f,
                    b = 6.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -8.486f,
                )
                // v 0
                verticalLineToRelative(dy = 0.0f)
                // a 6 6 0 0 0 -8.485 0
                arcToRelative(
                    a = 6.0f,
                    b = 6.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -8.485f,
                    dy1 = 0.0f,
                )
                // l -7.071 7.071
                lineToRelative(dx = -7.071f, dy = 7.071f)
                // m 9.012 7.959
                moveToRelative(dx = 9.012f, dy = 7.959f)
                // L 8.77 33.806
                lineTo(x = 8.77f, y = 33.806f)
                // a 6 6 0 0 0 0 8.485
                arcToRelative(
                    a = 6.0f,
                    b = 6.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 8.485f,
                )
                // v 0
                verticalLineToRelative(dy = 0.0f)
                // a 6 6 0 0 0 8.485 0
                arcToRelative(
                    a = 6.0f,
                    b = 6.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 8.485f,
                    dy1 = 0.0f,
                )
                // l 9.766 -9.766
                lineToRelative(dx = 9.766f, dy = -9.766f)
            }
            // M13 12 V4 m25 26 l5 -5
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineWidth = 4.0f,
            ) {
                // M 13 12
                moveTo(x = 13.0f, y = 12.0f)
                // V 4
                verticalLineTo(y = 4.0f)
                // m 25 26
                moveToRelative(dx = 25.0f, dy = 26.0f)
                // l 5 -5
                lineToRelative(dx = 5.0f, dy = -5.0f)
            }
        }.build().also { _dragon = it }
    }

@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDragon,
            contentDescription = null,
            modifier = Modifier
                .width((48.0).dp)
                .height((48.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _dragon: ImageVector? = null
