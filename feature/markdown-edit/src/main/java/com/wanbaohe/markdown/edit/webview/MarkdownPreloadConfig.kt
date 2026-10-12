package com.wanbaohe.markdown.edit.webview

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.toArgb
import com.shifenmiao.model.colors.EditorColors

/**
 * Markdown 编辑器预加载配置
 *
 * 包含所有影响 HTML 生成的参数，用于区分不同的缓存
 *
 * 排版默认值必须与 [com.t8rin.imagetoolbox.core.ui.widget.text.EditorUiDefaults.contentTextStyle]
 * （正文 15sp / 行高 22sp / 字距 0.25sp、字重 400）保持一致。它们只决定 JS 推送真实样式之前的
 * 首帧观感；命中判断不依赖这些字段（见 MarkdownWebViewPoolHelper 的 matchKeyOf）。
 */
data class MarkdownPreloadConfig(
    val isDarkTheme: Boolean,
    val colors: EditorColors,
    val storageKey: String = "default",
    val fontSizeSp: Float = 15f,
    val lineHeightSp: Float = 22f,
    val letterSpacingSp: Float = 0.25f,
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
            fontSizeSp: Float = 15f,
            lineHeightSp: Float = 22f,
            letterSpacingSp: Float = 0.25f,
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

