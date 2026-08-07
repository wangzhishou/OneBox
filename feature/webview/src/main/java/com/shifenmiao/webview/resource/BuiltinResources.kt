package com.shifenmiao.webview.resource

/**
 * 代码内硬编码的内置资源规则。
 *
 * 与远程下发的 [com.shifenmiao.model.webview.WebResourceRuleDto] 不同：
 * - **不参与** RemoteConfig 序列化 / 合并流程
 * - **优先级最高**（由 [com.shifenmiao.webview.resource.WebResourceEngine] 链路确保）
 * - 用于那些"必须用本地资源、不应被远程覆盖"的关键资源（如 tailwindcss、mermaid）
 *
 * 新增内置资源时，**只在 [all] 中追加一条**；引擎会在刷新时自动装载。
 */
object BuiltinResources {

    /**
     * tailwindcss CDN 兜底：HTML 中引用 `https://cdn.tailwindcss.com/...` 时，
     * 直接返回 APK 内置的 `assets/js/tailwindcss.js`。
     *
     * 背景：
     * 1. CDN 在国内访问慢或被墙。
     * 2. 我们已经按需打包了精简版 tailwindcss 进 APK。
     * 3. 旧实现 [com.shifenmiao.webview.client.CustomWebViewClient] 中用 `host.equals()`
     *    做整域匹配；这里升级为 [WebResourceMatch.Host]，行为兼容。
     */
    private val TAILWINDCSS = WebResourceRule.AssetRule(
        match = WebResourceMatch.Host(host = "cdn.tailwindcss.com"),
        assetPath = "js/tailwindcss.js",
    )

    /**
     * 全部内置规则。引擎启动 / 刷新时拷贝当前列表作为内置层。
     */
    fun all(): List<WebResourceRule> = listOf(TAILWINDCSS)
}
