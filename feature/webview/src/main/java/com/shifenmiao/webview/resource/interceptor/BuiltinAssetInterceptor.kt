package com.shifenmiao.webview.resource.interceptor

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import com.shifenmiao.webview.resource.WebResourceEngine
import com.shifenmiao.webview.resource.WebResourceRule
import com.shifenmiao.webview.resource.firstMatchingOrNull

/**
 * 内置资源拦截器：优先匹配代码内硬编码的 [com.shifenmiao.webview.resource.BuiltinResources]。
 *
 * 实现要点：
 * - 不直接读 `AssetManager`，而是把请求改写到 `https://appassets.androidplatform.net/assets/<path>` 再让
 *   [androidx.webkit.WebViewAssetLoader] 解析，**完全复用**其缓存与并发安全保证。
 * - 具体改写逻辑在 [WebResourceEngine.serveAssetFromLoader]。
 */
class BuiltinAssetInterceptor : WebResourceInterceptor {

    override fun intercept(
        request: WebResourceRequest,
        engine: WebResourceEngine,
    ): WebResourceResponse? {
        val rule = engine.builtinRules.firstMatchingOrNull(request) as? WebResourceRule.AssetRule
            ?: return null
        return engine.serveAssetFromLoader(rule.assetPath)
    }
}
