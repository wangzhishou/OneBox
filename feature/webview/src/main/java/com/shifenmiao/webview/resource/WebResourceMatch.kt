package com.shifenmiao.webview.resource

import android.webkit.WebResourceRequest

/**
 * 资源匹配策略。
 *
 * 设计要点：
 * - **短路求值**：[matches] 内部按成本递增顺序（精确 → 前缀 → host）排列，
 *   让热路径（如 full-URL 等值）零开销命中。
 * - **大小写不敏感**：[Host] 匹配按 host lowercase 比对；[ExactUrl] / [UrlPrefix]
 *   按字符串 equals（调用方负责大小写，URL 在 shouldInterceptRequest 阶段已规范化）。
 */
sealed interface WebResourceMatch {

    fun matches(request: WebResourceRequest): Boolean

    /** 匹配 host（path 任意）。如 `cdn.tailwindcss.com` 命中该 host 下所有请求。 */
    data class Host(val host: String) : WebResourceMatch {
        override fun matches(request: WebResourceRequest): Boolean =
            request.url.host?.equals(host, ignoreCase = true) == true
    }

    /** 完整 URL 等值匹配（包含 query）。 */
    data class ExactUrl(val url: String) : WebResourceMatch {
        override fun matches(request: WebResourceRequest): Boolean =
            request.url.toString() == url
    }

    /** 完整 URL 前缀匹配（包含 query）。 */
    data class UrlPrefix(val prefix: String) : WebResourceMatch {
        override fun matches(request: WebResourceRequest): Boolean =
            request.url.toString().startsWith(prefix)
    }
}
