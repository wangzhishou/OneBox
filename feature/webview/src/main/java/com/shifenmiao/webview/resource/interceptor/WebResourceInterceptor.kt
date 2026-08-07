package com.shifenmiao.webview.resource.interceptor

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import com.shifenmiao.webview.resource.WebResourceEngine

/**
 * 资源拦截器节点。链式调用，**第一个返回非 null 的拦截器胜出**。
 *
 * 设计原则：
 * - 拦截器**无状态**，可被多 WebView 并发复用。
 * - 拦截器**只读** [WebResourceEngine] 暴露的规则 / 缓存 / 资源，不持有可变状态。
 * - 拦截失败 / 资源不存在时返回 null，**绝不抛异常**（异常会被 WebView 当作 network error 处理，
 *   污染主框架错误状态）。
 */
fun interface WebResourceInterceptor {
    fun intercept(request: WebResourceRequest, engine: WebResourceEngine): WebResourceResponse?
}
