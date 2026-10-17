package com.wanbaohe.code.editor.webview

import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.webview.common.HtmlGenerator
import com.shifenmiao.webview.common.WebViewPool

/**
 * CodeEditor HTML 预拼接缓存辅助类
 */
object CodeEditorWebViewPoolHelper {

    const val PRELOAD_KEY = "code_editor"

    fun preloadHtml(config: CodeEditorPreloadConfig) {
        WebViewPool.preloadHtml(PRELOAD_KEY, matchKeyOf(config), config, CodeEditorHtmlGenerator)
    }

    fun getPreloadedHtml(config: CodeEditorPreloadConfig): String? {
        return WebViewPool.getPreloadedHtml(PRELOAD_KEY, matchKeyOf(config))
    }

    fun clearPreloadedHtml() {
        WebViewPool.clearPreloadedHtml(PRELOAD_KEY)
    }

    /**
     * 命中判断用的稳定键。
     *
     * 与 Markdown 编辑器同理：字体/颜色在 onEditorReady 之后由 CodeEditorBridge 的
     * setTextStyle / setColors / setTheme 重新推送，且 fontSizePx 依赖 composition 的 fontScale，
     * 预加载侧拿不到真值。键里只留 storageKey、isDarkTheme 与决定内嵌文案的当前 locale。
     */
    private fun matchKeyOf(config: CodeEditorPreloadConfig): String {
        val locale = AppContext.getContext().resources.configuration.locales[0].toLanguageTag()
        return "${config.storageKey}|${config.isDarkTheme}|$locale"
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
