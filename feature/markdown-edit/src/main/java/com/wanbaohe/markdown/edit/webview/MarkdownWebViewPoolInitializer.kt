package com.wanbaohe.markdown.edit.webview

import com.shifenmiao.webview.common.HtmlGenerator
import com.shifenmiao.webview.common.WebViewPool

/**
 * Markdown 编辑器 HTML 预拼接缓存辅助类
 *
 * 提供 Markdown 编辑器专用的 HTML 预拼接方法。
 * WebView 实例不再复用，每个页面独立创建、独立销毁。
 */
object MarkdownWebViewPoolHelper {

    /**
     * Markdown 编辑器预加载 key
     */
    const val PRELOAD_KEY = "markdown_editor"

    /**
     * 预加载 Markdown 编辑器 HTML
     *
     * @param config Markdown 编辑器配置
     */
    fun preloadHtml(config: MarkdownPreloadConfig) {
        WebViewPool.preloadHtml(PRELOAD_KEY, config, MarkdownHtmlGenerator)
    }

    /**
     * 获取预加载的 HTML
     *
     * @param config 配置，用于验证缓存是否匹配
     * @return 预加载的 HTML，如果配置不匹配则返回 null
     */
    fun getPreloadedHtml(config: MarkdownPreloadConfig): String? {
        return WebViewPool.getPreloadedHtml(PRELOAD_KEY, config)
    }

    /**
     * 清除预加载的 HTML 缓存
     *
     * 应在业务模块销毁时调用，释放内存
     */
    fun clearPreloadedHtml() {
        WebViewPool.clearPreloadedHtml(PRELOAD_KEY)
    }
}

/**
 * Markdown 编辑器 HTML 生成器
 */
internal object MarkdownHtmlGenerator : HtmlGenerator<MarkdownPreloadConfig> {
    override fun generate(config: MarkdownPreloadConfig): String {
        return MarkdownEditorHtmlTemplate.generate(
            isDarkTheme = config.isDarkTheme,
            colors = config.colors,
            storageKey = config.storageKey,
            fontSizeSp = config.fontSizeSp
        )
    }
}
