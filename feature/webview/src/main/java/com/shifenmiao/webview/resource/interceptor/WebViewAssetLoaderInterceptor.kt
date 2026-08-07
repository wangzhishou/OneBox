package com.shifenmiao.webview.resource.interceptor

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import androidx.webkit.WebViewAssetLoader
import com.shifenmiao.webview.resource.WebResourceEngine

/**
 * 兜底拦截器：把 `https://appassets.androidplatform.net/...` 的请求交由 [WebViewAssetLoader] 处理。
 *
 * [WebViewAssetLoader] 内置 AssetsPathHandler / ResourcesPathHandler，
 * 处理 `/assets/`、`/res/`、`/resources/` 等标准路径。
 * 命中失败返回 null，引擎继续放行 WebView 默认网络流程。
 */
class WebViewAssetLoaderInterceptor : WebResourceInterceptor {

    override fun intercept(
        request: WebResourceRequest,
        engine: WebResourceEngine,
    ): WebResourceResponse? {
        if (request.url.host != WebViewAssetLoader.DEFAULT_DOMAIN) return null
        return engine.assetLoader.shouldInterceptRequest(request.url)
    }
}
