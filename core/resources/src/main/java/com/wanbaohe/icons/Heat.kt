package com.wanbaohe.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.Heat: ImageVector
    get() {
        val current = _heat
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Heat",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // M15 13 V5 a3 3 0 0 0 -6 0 v8 a5 5 0 1 0 6 0 m-3 -9 a1 1 0 0 1 1 1 h-2 a1 1 0 0 1 1 -1
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 15 13
                moveTo(x = 15.0f, y = 13.0f)
                // V 5
                verticalLineTo(y = 5.0f)
                // a 3 3 0 0 0 -6 0
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -6.0f,
                    dy1 = 0.0f,
                )
                // v 8
                verticalLineToRelative(dy = 8.0f)
                // a 5 5 0 1 0 6 0
                arcToRelative(
                    a = 5.0f,
                    b = 5.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 6.0f,
                    dy1 = 0.0f,
                )
                // m -3 -9
                moveToRelative(dx = -3.0f, dy = -9.0f)
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
                // h -2
                horizontalLineToRelative(dx = -2.0f)
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
            }
        }.build().also { _heat = it }
    }

@Suppress("ObjectPropertyName")
private var _heat: ImageVector? = null
