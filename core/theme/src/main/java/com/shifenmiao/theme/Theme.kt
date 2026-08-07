package com.shifenmiao.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf


private val LocalAppDimens = staticCompositionLocalOf {
    normalDimensions
}

object AppTheme {
    val dimens: Dimensions
        @Composable
        get() = LocalAppDimens.current

    val colors: AppColors
        @Composable
        get() = LocalAppColors.current

    val typography: AppTypography
        @Composable
        get() = LocalAppTypography.current

    val shapes: AppShapes
        @Composable
        get() = LocalAppShapes.current


    var colorScheme: ColorScheme = lightColorScheme()

    var isDarkTheme: Boolean = false

    var fontScale:Float = 1f
}


