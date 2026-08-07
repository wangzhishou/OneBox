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
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAquarius

val Icons.Filled.Aquarius: ImageVector
    get() {
        val current = _aquarius
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Aquarius",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // m15 12.41 l-3 -3 l-3 3 l-3 -3 l-2.29 2.3 l-1.42 -1.42 L6 6.59 l3 3 l3 -3 l3 3 l3 -3 l3.71 3.7 l-1.42 1.42 L18 9.41z m3 3 l2.29 2.3 l1.42 -1.42 l-3.71 -3.7 l-3 3 l-3 -3 l-3 3 l-3 -3 l-3.71 3.7 l1.42 1.42 L6 15.41 l3 3 l3 -3 l3 3z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 15 12.41
                moveTo(x = 15.0f, y = 12.41f)
                // l -3 -3
                lineToRelative(dx = -3.0f, dy = -3.0f)
                // l -3 3
                lineToRelative(dx = -3.0f, dy = 3.0f)
                // l -3 -3
                lineToRelative(dx = -3.0f, dy = -3.0f)
                // l -2.29 2.3
                lineToRelative(dx = -2.29f, dy = 2.3f)
                // l -1.42 -1.42
                lineToRelative(dx = -1.42f, dy = -1.42f)
                // L 6 6.59
                lineTo(x = 6.0f, y = 6.59f)
                // l 3 3
                lineToRelative(dx = 3.0f, dy = 3.0f)
                // l 3 -3
                lineToRelative(dx = 3.0f, dy = -3.0f)
                // l 3 3
                lineToRelative(dx = 3.0f, dy = 3.0f)
                // l 3 -3
                lineToRelative(dx = 3.0f, dy = -3.0f)
                // l 3.71 3.7
                lineToRelative(dx = 3.71f, dy = 3.7f)
                // l -1.42 1.42
                lineToRelative(dx = -1.42f, dy = 1.42f)
                // L 18 9.41z
                lineTo(x = 18.0f, y = 9.41f)
                close()
                // m 3 3
                moveToRelative(dx = 3.0f, dy = 3.0f)
                // l 2.29 2.3
                lineToRelative(dx = 2.29f, dy = 2.3f)
                // l 1.42 -1.42
                lineToRelative(dx = 1.42f, dy = -1.42f)
                // l -3.71 -3.7
                lineToRelative(dx = -3.71f, dy = -3.7f)
                // l -3 3
                lineToRelative(dx = -3.0f, dy = 3.0f)
                // l -3 -3
                lineToRelative(dx = -3.0f, dy = -3.0f)
                // l -3 3
                lineToRelative(dx = -3.0f, dy = 3.0f)
                // l -3 -3
                lineToRelative(dx = -3.0f, dy = -3.0f)
                // l -3.71 3.7
                lineToRelative(dx = -3.71f, dy = 3.7f)
                // l 1.42 1.42
                lineToRelative(dx = 1.42f, dy = 1.42f)
                // L 6 15.41
                lineTo(x = 6.0f, y = 15.41f)
                // l 3 3
                lineToRelative(dx = 3.0f, dy = 3.0f)
                // l 3 -3
                lineToRelative(dx = 3.0f, dy = -3.0f)
                // l 3 3z
                lineToRelative(dx = 3.0f, dy = 3.0f)
                close()
            }
        }.build().also { _aquarius = it }
    }

@Preview
@Composable
private fun IconPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAquarius,
            contentDescription = null,
            modifier = Modifier
                .width((24.0).dp)
                .height((24.0).dp),
        )
    }
}

@Suppress("ObjectPropertyName")
private var _aquarius: ImageVector? = null
