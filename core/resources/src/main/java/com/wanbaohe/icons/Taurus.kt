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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTaurus

val Icons.Filled.Taurus: ImageVector
    get() {
        val current = _taurus
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Taurus",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M15.59 9 A7 7 0 0 0 19 3 h-2 a5 5 0 0 1 -5 5 a5 5 0 0 1 -5 -5 H5 c0 2.46 1.3 4.74 3.41 6 C5.09 11 4 15.28 6 18.6 c1.97 3.32 6.27 4.4 9.59 2.4 c3.32 -1.96 4.41 -6.26 2.41 -9.58 A6.9 6.9 0 0 0 15.59 9 M12 20 a5 5 0 0 1 -5 -5 a5 5 0 0 1 5 -5 a5 5 0 0 1 5 5 a5 5 0 0 1 -5 5
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 15.59 9
                moveTo(x = 15.59f, y = 9.0f)
                // A 7 7 0 0 0 19 3
                arcTo(
                    horizontalEllipseRadius = 7.0f,
                    verticalEllipseRadius = 7.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 19.0f,
                    y1 = 3.0f,
                )
                // h -2
                horizontalLineToRelative(dx = -2.0f)
                // a 5 5 0 0 1 -5 5
                arcToRelative(
                    a = 5.0f,
                    b = 5.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -5.0f,
                    dy1 = 5.0f,
                )
                // a 5 5 0 0 1 -5 -5
                arcToRelative(
                    a = 5.0f,
                    b = 5.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -5.0f,
                    dy1 = -5.0f,
                )
                // H 5
                horizontalLineTo(x = 5.0f)
                // c 0 2.46 1.3 4.74 3.41 6
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 2.46f,
                    dx2 = 1.3f,
                    dy2 = 4.74f,
                    dx3 = 3.41f,
                    dy3 = 6.0f,
                )
                // C 5.09 11 4 15.28 6 18.6
                curveTo(
                    x1 = 5.09f,
                    y1 = 11.0f,
                    x2 = 4.0f,
                    y2 = 15.28f,
                    x3 = 6.0f,
                    y3 = 18.6f,
                )
                // c 1.97 3.32 6.27 4.4 9.59 2.4
                curveToRelative(
                    dx1 = 1.97f,
                    dy1 = 3.32f,
                    dx2 = 6.27f,
                    dy2 = 4.4f,
                    dx3 = 9.59f,
                    dy3 = 2.4f,
                )
                // c 3.32 -1.96 4.41 -6.26 2.41 -9.58
                curveToRelative(
                    dx1 = 3.32f,
                    dy1 = -1.96f,
                    dx2 = 4.41f,
                    dy2 = -6.26f,
                    dx3 = 2.41f,
                    dy3 = -9.58f,
                )
                // A 6.9 6.9 0 0 0 15.59 9
                arcTo(
                    horizontalEllipseRadius = 6.9f,
                    verticalEllipseRadius = 6.9f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 15.59f,
                    y1 = 9.0f,
                )
                // M 12 20
                moveTo(x = 12.0f, y = 20.0f)
                // a 5 5 0 0 1 -5 -5
                arcToRelative(
                    a = 5.0f,
                    b = 5.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -5.0f,
                    dy1 = -5.0f,
                )
                // a 5 5 0 0 1 5 -5
                arcToRelative(
                    a = 5.0f,
                    b = 5.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 5.0f,
                    dy1 = -5.0f,
                )
                // a 5 5 0 0 1 5 5
                arcToRelative(
                    a = 5.0f,
                    b = 5.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 5.0f,
                    dy1 = 5.0f,
                )
                // a 5 5 0 0 1 -5 5
                arcToRelative(
                    a = 5.0f,
                    b = 5.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -5.0f,
                    dy1 = 5.0f,
                )
            }
        }.build().also { _taurus = it }
    }

@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTaurus,
            contentDescription = null,
            modifier = Modifier
                .width((24.0).dp)
                .height((24.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _taurus: ImageVector? = null
