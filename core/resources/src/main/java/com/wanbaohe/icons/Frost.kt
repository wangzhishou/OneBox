package com.wanbaohe.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.Frost: ImageVector
    get() {
        val current = _frost
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Frost",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M16.46 9.41 L13 7.38 V5.12 l1.71 -1.71 L13.29 2 L12 3.29 L10.71 2 L9.29 3.41 L11 5.12 v2.26 L8.5 8.82 l-2 -1.13 l-.58 -2.33 L4 5.88 l.47 1.77 l-1.77 .47 l.52 1.93 l2.33 -.62 l2 1.13 v2.89 l-2 1.13 l-2.33 -.62 l-.52 1.93 l1.77 .47 L4 18.12 l1.93 .52 l.62 -2.33 l2 -1.13 L11 16.62 v2.26 l-1.71 1.71 L10.71 22 L12 20.71 L13.29 22 l1.41 -1.41 l-1.7 -1.71 v-2.26 l3.46 -2.01 M9.5 10.56 L12 9.11 l2.5 1.45 v2.88 L12 14.89 l-2.5 -1.45 M19 13 V7 h2 v6z m0 4 v-2 h2 v2z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 16.46 9.41
                moveTo(x = 16.46f, y = 9.41f)
                // L 13 7.38
                lineTo(x = 13.0f, y = 7.38f)
                // V 5.12
                verticalLineTo(y = 5.12f)
                // l 1.71 -1.71
                lineToRelative(dx = 1.71f, dy = -1.71f)
                // L 13.29 2
                lineTo(x = 13.29f, y = 2.0f)
                // L 12 3.29
                lineTo(x = 12.0f, y = 3.29f)
                // L 10.71 2
                lineTo(x = 10.71f, y = 2.0f)
                // L 9.29 3.41
                lineTo(x = 9.29f, y = 3.41f)
                // L 11 5.12
                lineTo(x = 11.0f, y = 5.12f)
                // v 2.26
                verticalLineToRelative(dy = 2.26f)
                // L 8.5 8.82
                lineTo(x = 8.5f, y = 8.82f)
                // l -2 -1.13
                lineToRelative(dx = -2.0f, dy = -1.13f)
                // l -0.58 -2.33
                lineToRelative(dx = -0.58f, dy = -2.33f)
                // L 4 5.88
                lineTo(x = 4.0f, y = 5.88f)
                // l 0.47 1.77
                lineToRelative(dx = 0.47f, dy = 1.77f)
                // l -1.77 0.47
                lineToRelative(dx = -1.77f, dy = 0.47f)
                // l 0.52 1.93
                lineToRelative(dx = 0.52f, dy = 1.93f)
                // l 2.33 -0.62
                lineToRelative(dx = 2.33f, dy = -0.62f)
                // l 2 1.13
                lineToRelative(dx = 2.0f, dy = 1.13f)
                // v 2.89
                verticalLineToRelative(dy = 2.89f)
                // l -2 1.13
                lineToRelative(dx = -2.0f, dy = 1.13f)
                // l -2.33 -0.62
                lineToRelative(dx = -2.33f, dy = -0.62f)
                // l -0.52 1.93
                lineToRelative(dx = -0.52f, dy = 1.93f)
                // l 1.77 0.47
                lineToRelative(dx = 1.77f, dy = 0.47f)
                // L 4 18.12
                lineTo(x = 4.0f, y = 18.12f)
                // l 1.93 0.52
                lineToRelative(dx = 1.93f, dy = 0.52f)
                // l 0.62 -2.33
                lineToRelative(dx = 0.62f, dy = -2.33f)
                // l 2 -1.13
                lineToRelative(dx = 2.0f, dy = -1.13f)
                // L 11 16.62
                lineTo(x = 11.0f, y = 16.62f)
                // v 2.26
                verticalLineToRelative(dy = 2.26f)
                // l -1.71 1.71
                lineToRelative(dx = -1.71f, dy = 1.71f)
                // L 10.71 22
                lineTo(x = 10.71f, y = 22.0f)
                // L 12 20.71
                lineTo(x = 12.0f, y = 20.71f)
                // L 13.29 22
                lineTo(x = 13.29f, y = 22.0f)
                // l 1.41 -1.41
                lineToRelative(dx = 1.41f, dy = -1.41f)
                // l -1.7 -1.71
                lineToRelative(dx = -1.7f, dy = -1.71f)
                // v -2.26
                verticalLineToRelative(dy = -2.26f)
                // l 3.46 -2.01
                lineToRelative(dx = 3.46f, dy = -2.01f)
                // M 9.5 10.56
                moveTo(x = 9.5f, y = 10.56f)
                // L 12 9.11
                lineTo(x = 12.0f, y = 9.11f)
                // l 2.5 1.45
                lineToRelative(dx = 2.5f, dy = 1.45f)
                // v 2.88
                verticalLineToRelative(dy = 2.88f)
                // L 12 14.89
                lineTo(x = 12.0f, y = 14.89f)
                // l -2.5 -1.45
                lineToRelative(dx = -2.5f, dy = -1.45f)
                // M 19 13
                moveTo(x = 19.0f, y = 13.0f)
                // V 7
                verticalLineTo(y = 7.0f)
                // h 2
                horizontalLineToRelative(dx = 2.0f)
                // v 6z
                verticalLineToRelative(dy = 6.0f)
                close()
                // m 0 4
                moveToRelative(dx = 0.0f, dy = 4.0f)
                // v -2
                verticalLineToRelative(dy = -2.0f)
                // h 2
                horizontalLineToRelative(dx = 2.0f)
                // v 2z
                verticalLineToRelative(dy = 2.0f)
                close()
            }
        }.build().also { _frost = it }
    }

@Suppress("ObjectPropertyName")
private var _frost: ImageVector? = null
