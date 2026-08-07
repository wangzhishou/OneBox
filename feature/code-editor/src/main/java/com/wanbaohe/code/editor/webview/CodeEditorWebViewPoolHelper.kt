package com.wanbaohe.code.editor.webview

import com.shifenmiao.webview.common.HtmlGenerator
import com.shifenmiao.webview.common.WebViewPool

/**
 * CodeEditor HTML 预拼接缓存辅助类
 */
object CodeEditorWebViewPoolHelper {

    const val PRELOAD_KEY = "code_editor"

    fun preloadHtml(config: CodeEditorPreloadConfig) {
        WebViewPool.preloadHtml(PRELOAD_KEY, config, CodeEditorHtmlGenerator)
    }

    fun getPreloadedHtml(config: CodeEditorPreloadConfig): String? {
        return WebViewPool.getPreloadedHtml(PRELOAD_KEY, config)
    }

    fun clearPreloadedHtml() {
        WebViewPool.clearPreloadedHtml(PRELOAD_KEY)
    }
}

internal object CodeEditorHtmlGenerator : HtmlGenerator<CodeEditorPreloadConfig> {
    override fun generate(config: CodeEditorPreloadConfig): String {
        return CodeEditorHtmlTemplate.generate(
            isDarkTheme = config.isDarkTheme,
            colors = config.colors,
            storageKey = config.storageKey,
            fontSizePx = config.fontSizePx,
            lineHeightPx = config.lineHeightPx,
            letterSpacingPx = config.letterSpacingPx,
            fontWeight = config.fontWeight
        )
    }
}
