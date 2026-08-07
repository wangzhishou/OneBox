package com.shifenmiao.webview.resource

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse

/**
 * 一条 WebView 资源拦截规则的强类型表达。
 *
 * 规则执行由 [com.shifenmiao.webview.resource.WebResourceEngine] 调度；
 * 匹配成功时直接返回非 null [WebResourceResponse]，否则返回 null 让引擎继续走下一个拦截器。
 *
 * 两种规则：
 * - [AssetRule] — 命中后从 APK `assets/` 目录读取；不消耗网络。
 * - [RemoteUrlRule] — 命中后从 [realUrl] 拉取（带 HTTP 缓存），即使请求方是虚拟 URL 也能用真 URL 取到资源。
 */
sealed interface WebResourceRule {

    val match: WebResourceMatch

    fun matches(request: WebResourceRequest): Boolean = match.matches(request)

    /**
     * Asset 规则：匹配后用 APK 内置资源响应。
     *
     * @param assetPath 相对 `assets/` 的路径，如 `js/tailwindcss.js`。
     */
    data class AssetRule(
        override val match: WebResourceMatch,
        val assetPath: String,
    ) : WebResourceRule

    /**
     * 远程 URL 重定向 + 缓存规则。
     *
     * @param realUrl 真实资源 URL。
     * @param cacheTtlMillis 可选 TTL；null 表示走 OkHttp Cache 默认行为。
     */
    data class RemoteUrlRule(
        override val match: WebResourceMatch,
        val realUrl: String,
        val cacheTtlMillis: Long? = null,
    ) : WebResourceRule
}

/** 列表中第一个匹配 [request] 的规则；按声明顺序求值。 */
fun List<WebResourceRule>.firstMatchingOrNull(request: android.webkit.WebResourceRequest): WebResourceRule? =
    firstOrNull { it.matches(request) }
