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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCapricorn

val Icons.Filled.Capricorn: ImageVector
    get() {
        val current = _capricorn
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Capricorn",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M15 13 c-.7 0 -1.39 .19 -2 .55 V6 a3 3 0 0 0 -3 -3 c-.75 0 -1.45 .29 -2 .78 C7.45 3.28 6.74 3 6 3 v2 a1 1 0 0 1 1 1 v10 h2 V6 a1 1 0 0 1 1 -1 a1 1 0 0 1 1 1 v11 a2 2 0 0 1 -2 2 v2 c1.15 0 2.25 -.5 3 -1.38 a3.974 3.974 0 0 0 5.64 .38 c1.67 -1.42 1.86 -3.95 .4 -5.62 A4.01 4.01 0 0 0 15 13 m0 6 a2 2 0 0 1 -2 -2 a2 2 0 0 1 2 -2 a2 2 0 0 1 2 2 a2 2 0 0 1 -2 2
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 15 13
                moveTo(x = 15.0f, y = 13.0f)
                // c -0.7 0 -1.39 0.19 -2 0.55
                curveToRelative(
                    dx1 = -0.7f,
                    dy1 = 0.0f,
                    dx2 = -1.39f,
                    dy2 = 0.19f,
                    dx3 = -2.0f,
                    dy3 = 0.55f,
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
                // C 7.45 3.28 6.74 3 6 3
                curveTo(
                    x1 = 7.45f,
                    y1 = 3.28f,
                    x2 = 6.74f,
                    y2 = 3.0f,
                    x3 = 6.0f,
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
                // v 11
                verticalLineToRelative(dy = 11.0f)
                // a 2 2 0 0 1 -2 2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.0f,
                    dy1 = 2.0f,
                )
                // v 2
                verticalLineToRelative(dy = 2.0f)
                // c 1.15 0 2.25 -0.5 3 -1.38
                curveToRelative(
                    dx1 = 1.15f,
                    dy1 = 0.0f,
                    dx2 = 2.25f,
                    dy2 = -0.5f,
                    dx3 = 3.0f,
                    dy3 = -1.38f,
                )
                // a 3.974 3.974 0 0 0 5.64 0.38
                arcToRelative(
                    a = 3.974f,
                    b = 3.974f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 5.64f,
                    dy1 = 0.38f,
                )
                // c 1.67 -1.42 1.86 -3.95 0.4 -5.62
                curveToRelative(
                    dx1 = 1.67f,
                    dy1 = -1.42f,
                    dx2 = 1.86f,
                    dy2 = -3.95f,
                    dx3 = 0.4f,
                    dy3 = -5.62f,
                )
                // A 4.01 4.01 0 0 0 15 13
                arcTo(
                    horizontalEllipseRadius = 4.01f,
                    verticalEllipseRadius = 4.01f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 15.0f,
                    y1 = 13.0f,
                )
                // m 0 6
                moveToRelative(dx = 0.0f, dy = 6.0f)
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
                // a 2 2 0 0 1 -2 2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.0f,
                    dy1 = 2.0f,
                )
            }
        }.build().also { _capricorn = it }
    }

@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCapricorn,
            contentDescription = null,
            modifier = Modifier
                .width((24.0).dp)
                .height((24.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _capricorn: ImageVector? = null
