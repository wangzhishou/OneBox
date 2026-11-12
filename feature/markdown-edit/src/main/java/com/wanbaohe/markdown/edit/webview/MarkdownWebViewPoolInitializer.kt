/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.markdown.edit.webview

import com.shifenmiao.interfaces.singleton.AppContext
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
     * Markdown 编辑器预加载 key 前缀。
     *
     * 实际 cacheKey 是 `"$PRELOAD_KEY:$storageKey"`：记事本全屏页、新建笔记、编辑提示词
     * 三个组件会各自在 init 里预拼接 HTML，共用一个 key 时后写的会覆盖先写的，
     * 而 matchKey 里又含 storageKey，被覆盖的那个页面取缓存必然 miss、白做一次拼接；
     * 更糟的是任意组件 doOnDestroy 都会把整个桶清掉，连别的页面刚预热的也一起没了。
     */
    const val PRELOAD_KEY = "markdown_editor"

    private fun cacheKeyOf(storageKey: String) = "$PRELOAD_KEY:$storageKey"

    /**
     * 预加载 Markdown 编辑器 HTML
     *
     * @param config Markdown 编辑器配置
     */
    fun preloadHtml(config: MarkdownPreloadConfig) {
        WebViewPool.preloadHtml(cacheKeyOf(config.storageKey), matchKeyOf(config), config, MarkdownHtmlGenerator)
    }

    /**
     * 获取预加载的 HTML
     *
     * @param config 配置，用于计算命中键
     * @return 预加载的 HTML，如果命中键不匹配则返回 null
     */
    fun getPreloadedHtml(config: MarkdownPreloadConfig): String? {
        return WebViewPool.getPreloadedHtml(cacheKeyOf(config.storageKey), matchKeyOf(config))
    }

    /**
     * 只清除当前页面的预加载 HTML，不动其它页面刚预热的缓存。
     * 应在业务模块销毁时调用，释放内存。
     *
     * @param storageKey 与 [preloadHtml] 时传入的 storageKey 一致
     */
    fun clearPreloadedHtml(storageKey: String) {
        WebViewPool.clearPreloadedHtml(cacheKeyOf(storageKey))
    }

    /**
     * 清除 Markdown 编辑器全部预加载缓存（切主题 / 换语言时整体失效用）。
     */
    fun clearPreloadedHtml() {
        WebViewPool.clearPreloadedHtmlByPrefix("$PRELOAD_KEY:")
    }
}

/**
 * 命中判断用的稳定键。
 *
 * 只取 storageKey（决定 WebView localStorage 里读哪份草稿，是真正的语义身份）、isDarkTheme
 * （只影响 JS 到位前的首帧底色）与当前 locale（决定 HTML 内嵌文案）。字体/字距/字重/颜色都不进键：
 * 编辑器 onEditorReady 之后会由 AndroidBridge.setTextStyle / setColors / setTheme 重新推送一遍，
 * HTML 里烘焙的那份不决定最终呈现；而 fontSizeSp 依赖 composition 的 fontScale（LocalConfiguration），
 * Component.init 里本来就拿不到真值，做成全字段相等只会让预加载永久静默失效。
 */
private fun matchKeyOf(config: MarkdownPreloadConfig): String {
    val locale = AppContext.getContext().resources.configuration.locales[0].toLanguageTag()
    return "${config.storageKey}|${config.isDarkTheme}|$locale"
}

/**
 * Markdown 编辑器 HTML 生成器
 */
internal object MarkdownHtmlGenerator : HtmlGenerator<MarkdownPreloadConfig> {
    override fun generate(config: MarkdownPreloadConfig): String {
        // 必须与 WebViewMarkdownEditor 的非预加载分支传参完全一致（含行高/字距/字重），
        // 否则命中预加载时生成的 HTML 与非预加载时不同，会变成"有时行高 24 有时 22"的排版漂移。
        return MarkdownEditorHtmlTemplate.generate(
            isDarkTheme = config.isDarkTheme,
            colors = config.colors,
            storageKey = config.storageKey,
            fontSizeSp = config.fontSizeSp,
            lineHeightSp = config.lineHeightSp,
            letterSpacingSp = config.letterSpacingSp,
            fontWeight = config.fontWeight,
            toolbarExtras = config.toolbarExtras
        )
    }
}
