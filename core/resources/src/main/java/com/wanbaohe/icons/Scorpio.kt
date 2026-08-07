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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineScorpio

val Icons.Filled.Scorpio: ImageVector
    get() {
        val current = _scorpio
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Scorpio",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // m17.71 15.29 l-1.42 1.42 l1.3 1.29 H16 a2 2 0 0 1 -2 -2 V6 a3 3 0 0 0 -3 -3 c-.75 0 -1.45 .29 -2 .78 a2.997 2.997 0 0 0 -4 0 C4.45 3.28 3.74 3 3 3 v2 a1 1 0 0 1 1 1 v10 h2 V6 a1 1 0 0 1 1 -1 a1 1 0 0 1 1 1 v10 h2 V6 a1 1 0 0 1 1 -1 a1 1 0 0 1 1 1 v10 a4 4 0 0 0 4 4 h1.59 l-1.3 1.29 l1.42 1.42 l3.7 -3.71z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 17.71 15.29
                moveTo(x = 17.71f, y = 15.29f)
                // l -1.42 1.42
                lineToRelative(dx = -1.42f, dy = 1.42f)
                // l 1.3 1.29
                lineToRelative(dx = 1.3f, dy = 1.29f)
                // H 16
                horizontalLineTo(x = 16.0f)
                // a 2 2 0 0 1 -2 -2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.0f,
                    dy1 = -2.0f,
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
                // v 10
                verticalLineToRelative(dy = 10.0f)
                // a 4 4 0 0 0 4 4
                arcToRelative(
                    a = 4.0f,
                    b = 4.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 4.0f,
                    dy1 = 4.0f,
                )
                // h 1.59
                horizontalLineToRelative(dx = 1.59f)
                // l -1.3 1.29
                lineToRelative(dx = -1.3f, dy = 1.29f)
                // l 1.42 1.42
                lineToRelative(dx = 1.42f, dy = 1.42f)
                // l 3.7 -3.71z
                lineToRelative(dx = 3.7f, dy = -3.71f)
                close()
            }
        }.build().also { _scorpio = it }
    }

@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineScorpio,
            contentDescription = null,
            modifier = Modifier
                .width((24.0).dp)
                .height((24.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _scorpio: ImageVector? = null
