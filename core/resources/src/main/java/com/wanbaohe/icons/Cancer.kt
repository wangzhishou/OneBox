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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCancer

val Icons.Filled.Cancer: ImageVector
    get() {
        val current = _cancer
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Cancer",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M12 4 C6.5 4 2 7.58 2 12 c0 2.12 1.65 3.87 3.76 4 H6 a4 4 0 0 0 4 -4 a4 4 0 0 0 -4 -4 h-.24 A8.8 8.8 0 0 1 12 5.6 c1.77 -.02 3.5 .47 5 1.4 l1.25 -1.25 A11.5 11.5 0 0 0 12 4 m-6 6 a2 2 0 0 1 2 2 c0 1.11 -.92 2 -2 2 a2 2 0 0 1 -2 -1.8 v-.4 A2 2 0 0 1 6 10 m12.24 -2 H18 a4 4 0 0 0 -4 4 a4 4 0 0 0 4 4 h.24 A8.8 8.8 0 0 1 12 18.4 c-1.77 .02 -3.5 -.47 -5 -1.4 l-1.24 1.24 C7.63 19.41 9.79 20 12 20 c5.5 0 10 -3.58 10 -8 c0 -2.12 -1.65 -3.87 -3.76 -4 M18 14 a2 2 0 0 1 -2 -2 c0 -1.11 .92 -2 2 -2 a2 2 0 0 1 2 1.8 v.4 a2 2 0 0 1 -2 1.8
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 12 4
                moveTo(x = 12.0f, y = 4.0f)
                // C 6.5 4 2 7.58 2 12
                curveTo(
                    x1 = 6.5f,
                    y1 = 4.0f,
                    x2 = 2.0f,
                    y2 = 7.58f,
                    x3 = 2.0f,
                    y3 = 12.0f,
                )
                // c 0 2.12 1.65 3.87 3.76 4
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 2.12f,
                    dx2 = 1.65f,
                    dy2 = 3.87f,
                    dx3 = 3.76f,
                    dy3 = 4.0f,
                )
                // H 6
                horizontalLineTo(x = 6.0f)
                // a 4 4 0 0 0 4 -4
                arcToRelative(
                    a = 4.0f,
                    b = 4.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 4.0f,
                    dy1 = -4.0f,
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
                // h -0.24
                horizontalLineToRelative(dx = -0.24f)
                // A 8.8 8.8 0 0 1 12 5.6
                arcTo(
                    horizontalEllipseRadius = 8.8f,
                    verticalEllipseRadius = 8.8f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 12.0f,
                    y1 = 5.6f,
                )
                // c 1.77 -0.02 3.5 0.47 5 1.4
                curveToRelative(
                    dx1 = 1.77f,
                    dy1 = -0.02f,
                    dx2 = 3.5f,
                    dy2 = 0.47f,
                    dx3 = 5.0f,
                    dy3 = 1.4f,
                )
                // l 1.25 -1.25
                lineToRelative(dx = 1.25f, dy = -1.25f)
                // A 11.5 11.5 0 0 0 12 4
                arcTo(
                    horizontalEllipseRadius = 11.5f,
                    verticalEllipseRadius = 11.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 12.0f,
                    y1 = 4.0f,
                )
                // m -6 6
                moveToRelative(dx = -6.0f, dy = 6.0f)
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
                // c 0 1.11 -0.92 2 -2 2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.11f,
                    dx2 = -0.92f,
                    dy2 = 2.0f,
                    dx3 = -2.0f,
                    dy3 = 2.0f,
                )
                // a 2 2 0 0 1 -2 -1.8
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.0f,
                    dy1 = -1.8f,
                )
                // v -0.4
                verticalLineToRelative(dy = -0.4f)
                // A 2 2 0 0 1 6 10
                arcTo(
                    horizontalEllipseRadius = 2.0f,
                    verticalEllipseRadius = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 6.0f,
                    y1 = 10.0f,
                )
                // m 12.24 -2
                moveToRelative(dx = 12.24f, dy = -2.0f)
                // H 18
                horizontalLineTo(x = 18.0f)
                // a 4 4 0 0 0 -4 4
                arcToRelative(
                    a = 4.0f,
                    b = 4.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -4.0f,
                    dy1 = 4.0f,
                )
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
                // h 0.24
                horizontalLineToRelative(dx = 0.24f)
                // A 8.8 8.8 0 0 1 12 18.4
                arcTo(
                    horizontalEllipseRadius = 8.8f,
                    verticalEllipseRadius = 8.8f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 12.0f,
                    y1 = 18.4f,
                )
                // c -1.77 0.02 -3.5 -0.47 -5 -1.4
                curveToRelative(
                    dx1 = -1.77f,
                    dy1 = 0.02f,
                    dx2 = -3.5f,
                    dy2 = -0.47f,
                    dx3 = -5.0f,
                    dy3 = -1.4f,
                )
                // l -1.24 1.24
                lineToRelative(dx = -1.24f, dy = 1.24f)
                // C 7.63 19.41 9.79 20 12 20
                curveTo(
                    x1 = 7.63f,
                    y1 = 19.41f,
                    x2 = 9.79f,
                    y2 = 20.0f,
                    x3 = 12.0f,
                    y3 = 20.0f,
                )
                // c 5.5 0 10 -3.58 10 -8
                curveToRelative(
                    dx1 = 5.5f,
                    dy1 = 0.0f,
                    dx2 = 10.0f,
                    dy2 = -3.58f,
                    dx3 = 10.0f,
                    dy3 = -8.0f,
                )
                // c 0 -2.12 -1.65 -3.87 -3.76 -4
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -2.12f,
                    dx2 = -1.65f,
                    dy2 = -3.87f,
                    dx3 = -3.76f,
                    dy3 = -4.0f,
                )
                // M 18 14
                moveTo(x = 18.0f, y = 14.0f)
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
                // c 0 -1.11 0.92 -2 2 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.11f,
                    dx2 = 0.92f,
                    dy2 = -2.0f,
                    dx3 = 2.0f,
                    dy3 = -2.0f,
                )
                // a 2 2 0 0 1 2 1.8
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.0f,
                    dy1 = 1.8f,
                )
                // v 0.4
                verticalLineToRelative(dy = 0.4f)
                // a 2 2 0 0 1 -2 1.8
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.0f,
                    dy1 = 1.8f,
                )
            }
        }.build().also { _cancer = it }
    }

@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCancer,
            contentDescription = null,
            modifier = Modifier
                .width((24.0).dp)
                .height((24.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _cancer: ImageVector? = null
