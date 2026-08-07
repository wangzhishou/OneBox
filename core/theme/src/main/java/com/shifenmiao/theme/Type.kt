package com.shifenmiao.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

class AppTypography {
    @Composable
    fun getCardTitle(): TextStyle {
        return MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            lineHeight = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    @Composable
    fun getTitle(): TextStyle {
        return MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    @Composable
    fun getRecommendCardTitle(): TextStyle {
        return MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    @Composable
    fun getSubContent(): TextStyle {
        return MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

val appAppTypography = AppTypography()

val LocalAppTypography = staticCompositionLocalOf {
    appAppTypography
}