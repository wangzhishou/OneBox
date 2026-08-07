package com.wanbaohe.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.MidAutumn: ImageVector
    get() {
        val current = _midAutumn
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.MidAutumn",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M12 2 A10 10 0 1 1 2 12 A10 10 0 0 1 12 2
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 12 2
                moveTo(x = 12.0f, y = 2.0f)
                // A 10 10 0 1 1 2 12
                arcTo(
                    horizontalEllipseRadius = 10.0f,
                    verticalEllipseRadius = 10.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 2.0f,
                    y1 = 12.0f,
                )
                // A 10 10 0 0 1 12 2
                arcTo(
                    horizontalEllipseRadius = 10.0f,
                    verticalEllipseRadius = 10.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 12.0f,
                    y1 = 2.0f,
                )
            }
        }.build().also { _midAutumn = it }
    }

@Suppress("ObjectPropertyName")
private var _midAutumn: ImageVector? = null
