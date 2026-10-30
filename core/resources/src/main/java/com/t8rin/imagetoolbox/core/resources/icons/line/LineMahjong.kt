package com.t8rin.imagetoolbox.core.resources.icons.line

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons

val Icons.Outlined.LineMahjong: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    Builder(
        name = "Outlined.LineMahjong",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(7f, 2.8f)
            lineTo(17f, 2.8f)
            curveTo(18.2f, 2.8f, 19f, 3.8f, 19f, 5f)
            lineTo(19f, 19f)
            curveTo(19f, 20.2f, 18.2f, 21.2f, 17f, 21.2f)
            lineTo(7f, 21.2f)
            curveTo(5.8f, 21.2f, 5f, 20.2f, 5f, 19f)
            lineTo(5f, 5f)
            curveTo(5f, 3.8f, 5.8f, 2.8f, 7f, 2.8f)
            close()
            moveTo(9f, 8f)
            lineTo(15f, 8f)
            lineTo(15f, 15.5f)
            lineTo(9f, 15.5f)
            close()
            moveTo(12f, 5.2f)
            lineTo(12f, 18.8f)
            moveTo(9f, 11.8f)
            lineTo(15f, 11.8f)
        }
    }.build()
}
