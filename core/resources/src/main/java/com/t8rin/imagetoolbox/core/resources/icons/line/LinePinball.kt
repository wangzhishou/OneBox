package com.t8rin.imagetoolbox.core.resources.icons.line

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons

val Icons.Outlined.LinePinball: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    ImageVector.Builder(
        name = "Outlined.LinePinball",
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
            moveTo(5f, 3f)
            lineTo(19f, 3f)
            lineTo(21f, 21f)
            lineTo(3f, 21f)
            close()
            moveTo(7f, 16f)
            lineTo(11f, 13f)
            moveTo(17f, 16f)
            lineTo(13f, 13f)
        }
        path(
            fill = SolidColor(Color.Black),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.2f,
        ) {
            moveTo(12f, 6f)
            curveTo(13.66f, 6f, 15f, 7.34f, 15f, 9f)
            curveTo(15f, 10.66f, 13.66f, 12f, 12f, 12f)
            curveTo(10.34f, 12f, 9f, 10.66f, 9f, 9f)
            curveTo(9f, 7.34f, 10.34f, 6f, 12f, 6f)
            close()
        }
    }.build()
}
