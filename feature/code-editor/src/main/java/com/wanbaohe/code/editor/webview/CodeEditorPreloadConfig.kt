package com.wanbaohe.code.editor.webview

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.toArgb
import com.shifenmiao.model.colors.EditorColors

/**
 * CodeEditor 预加载配置
 */
data class CodeEditorPreloadConfig(
    val isDarkTheme: Boolean,
    val colors: EditorColors,
    val storageKey: String = "default",
    val fontSizePx: Float = 14f,
    val lineHeightPx: Float = 20f,
    val letterSpacingPx: Float = 0f,
    val fontWeight: Int = 400
) {
    companion object {
        fun fromColorScheme(
            colorScheme: ColorScheme,
            isDarkTheme: Boolean,
            storageKey: String = "default",
            fontSizePx: Float = 14f,
            lineHeightPx: Float = 20f,
            letterSpacingPx: Float = 0f,
            fontWeight: Int = 400
        ) = CodeEditorPreloadConfig(
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
            fontSizePx = fontSizePx,
            lineHeightPx = lineHeightPx,
            letterSpacingPx = letterSpacingPx,
            fontWeight = fontWeight
        )

        private fun colorToHex(color: Int) = String.format("#%06X", 0xFFFFFF and color)
    }
}
