package com.wanbaohe.markdown.edit.webview

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.toArgb
import com.shifenmiao.model.colors.EditorColors

/**
 * Markdown 编辑器预加载配置
 *
 * 包含所有影响 HTML 生成的参数，用于区分不同的缓存
 */
data class MarkdownPreloadConfig(
    val isDarkTheme: Boolean,
    val colors: EditorColors,
    val storageKey: String = "default",
    val fontSizeSp: Float = 16f,
    val lineHeightSp: Float = 24f,
    val letterSpacingSp: Float = 0f,
    val fontWeight: Int = 400,
    val toolbarExtras: String = ""
) {
    companion object {
        /**
         * 从 ColorScheme 创建配置
         */
        fun fromColorScheme(
            colorScheme: ColorScheme,
            isDarkTheme: Boolean,
            storageKey: String = "default",
            fontSizeSp: Float = 16f,
            lineHeightSp: Float = 24f,
            letterSpacingSp: Float = 0f,
            fontWeight: Int = 400,
            toolbarExtras: String = ""
        ) = MarkdownPreloadConfig(
            isDarkTheme = isDarkTheme,
            colors = EditorColors(
                primary = colorToHex(colorScheme.primary.toArgb()),
                onPrimary = colorToHex(colorScheme.onPrimary.toArgb()),
                primaryContainer = colorToHex(colorScheme.primaryContainer.toArgb()),
                onPrimaryContainer = colorToHex(colorScheme.onPrimaryContainer.toArgb()),
                secondary = colorToHex(colorScheme.secondary.toArgb()),
                onSecondary = colorToHex(colorScheme.onSecondary.toArgb()),
                secondaryContainer = colorToHex(colorScheme.secondaryContainer.toArgb()),
                onSecondaryContainer = colorToHex(colorScheme.onSecondaryContainer.toArgb()),
                surface = colorToHex(colorScheme.surface.toArgb()),
                onSurface = colorToHex(colorScheme.onSurface.toArgb()),
                surfaceVariant = colorToHex(colorScheme.surfaceVariant.toArgb()),
                onSurfaceVariant = colorToHex(colorScheme.onSurfaceVariant.toArgb()),
                outline = colorToHex(colorScheme.outline.toArgb()),
                outlineVariant = colorToHex(colorScheme.outlineVariant.toArgb()),
                background = colorToHex(colorScheme.background.toArgb()),
                onBackground = colorToHex(colorScheme.onBackground.toArgb()),
                error = colorToHex(colorScheme.error.toArgb()),
                onError = colorToHex(colorScheme.onError.toArgb())
            ),
            storageKey = storageKey,
            fontSizeSp = fontSizeSp,
            lineHeightSp = lineHeightSp,
            letterSpacingSp = letterSpacingSp,
            fontWeight = fontWeight,
            toolbarExtras = toolbarExtras
        )

        private fun colorToHex(color: Int) = String.format("#%06X", 0xFFFFFF and color)
    }
}

