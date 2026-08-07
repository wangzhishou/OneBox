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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSagittarius

val Icons.Filled.Sagittarius: ImageVector
    get() {
        val current = _sagittarius
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Sagittarius",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M22 2 v10 h-2 V5.41 L10.41 15 l2.3 2.29 l-1.42 1.42 L9 16.41 l-5.29 5.3 l-1.42 -1.42 L7.59 15 l-2.3 -2.29 l1.42 -1.42 L9 13.59 L18.59 4 H12 V2z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 22 2
                moveTo(x = 22.0f, y = 2.0f)
                // v 10
                verticalLineToRelative(dy = 10.0f)
                // h -2
                horizontalLineToRelative(dx = -2.0f)
                // V 5.41
                verticalLineTo(y = 5.41f)
                // L 10.41 15
                lineTo(x = 10.41f, y = 15.0f)
                // l 2.3 2.29
                lineToRelative(dx = 2.3f, dy = 2.29f)
                // l -1.42 1.42
                lineToRelative(dx = -1.42f, dy = 1.42f)
                // L 9 16.41
                lineTo(x = 9.0f, y = 16.41f)
                // l -5.29 5.3
                lineToRelative(dx = -5.29f, dy = 5.3f)
                // l -1.42 -1.42
                lineToRelative(dx = -1.42f, dy = -1.42f)
                // L 7.59 15
                lineTo(x = 7.59f, y = 15.0f)
                // l -2.3 -2.29
                lineToRelative(dx = -2.3f, dy = -2.29f)
                // l 1.42 -1.42
                lineToRelative(dx = 1.42f, dy = -1.42f)
                // L 9 13.59
                lineTo(x = 9.0f, y = 13.59f)
                // L 18.59 4
                lineTo(x = 18.59f, y = 4.0f)
                // H 12
                horizontalLineTo(x = 12.0f)
                // V 2z
                verticalLineTo(y = 2.0f)
                close()
            }
        }.build().also { _sagittarius = it }
    }

@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSagittarius,
            contentDescription = null,
            modifier = Modifier
                .width((24.0).dp)
                .height((24.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _sagittarius: ImageVector? = null
