package com.shifenmiao.webview.client

import android.content.Context
import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.shifenmiao.webview.resource.WebResourceEngine

/**
 * 统一的 WebViewClient 包装：
 * - **资源拦截**完全委托给 [WebResourceEngine]（[shouldInterceptRequest] 一行透传）。
 * - **错误处理**（SSL / DNS / 超时 / bad url 等）仍由本类负责，因为这些事件不走资源拦截链。
 *
 * 不再持有 [com.shifenmiao.webview.resource.cache.WebResourceCache] 或
 * [androidx.webkit.WebViewAssetLoader] 实例——这些都收敛到 [WebResourceEngine] 内部。
 */
class CustomWebViewClient(
    private val context: Context,
    private val engine: WebResourceEngine,
    private val ignoreSslError: Boolean,
    private val onShouldOverrideUrlLoading: ((String) -> Boolean)? = null,
    private val onFinished: (WebView?, String?) -> Unit,
    private val onStarted: (WebView?, String?, Bitmap?) -> Unit,
    private val onError: ((WebView?, WebResourceRequest?, String, String) -> Unit)? = null
) : WebViewClient() {

    private val sslHandshakeHttpFallbackTried = HashSet<String>()

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onFinished(view, url)
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onStarted(view, url, favicon)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString().orEmpty()
        if (url.isBlank()) return false
        return onShouldOverrideUrlLoading?.invoke(url) == true
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        val normalized = url.orEmpty()
        if (normalized.isBlank()) return false
        return onShouldOverrideUrlLoading?.invoke(normalized) == true
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest,
    ): WebResourceResponse? = engine.intercept(request)

    // ===== 错误处理（与资源拦截无关）=====

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?,
    ) {
        super.onReceivedError(view, request, error)

        // 只处理主页面错误，不处理资源错误
        if (request?.isForMainFrame == true) {
            val errorCode = error?.errorCode ?: -1
            val errorDescription = error?.description?.toString() ?: "未知错误"

            if (ignoreSslError && (errorCode == ERROR_FAILED_SSL_HANDSHAKE)) {
                val failingUrl = request.url
                if ((failingUrl != null) && failingUrl.scheme.equals("https", ignoreCase = true)) {
                    val key = failingUrl.toString()
                    if (sslHandshakeHttpFallbackTried.add(key)) {
                        view?.loadUrl(failingUrl.buildUpon().scheme("http").build().toString())
                        return
                    }
                }
            }

            val errorMessage = when (errorCode) {
                ERROR_HOST_LOOKUP -> "找不到网站 (DNS查询失败)"
                ERROR_CONNECT -> "连接服务器失败"
                ERROR_TIMEOUT -> "连接超时"
                ERROR_BAD_URL -> "无效的网址"
                ERROR_UNSUPPORTED_SCHEME -> "不支持的协议"
                ERROR_FILE_NOT_FOUND -> "文件不存在"
                ERROR_FAILED_SSL_HANDSHAKE -> "SSL握手失败"
                ERROR_UNKNOWN -> "未知错误"
                else -> "加载失败 (错误码: $errorCode)"
            }

            val errorDetails = "net::${getErrorCodeName(errorCode)}"

            onError?.invoke(view, request, errorMessage, errorDetails)
        }
    }

    private fun getErrorCodeName(errorCode: Int): String = when (errorCode) {
        ERROR_HOST_LOOKUP -> "ERR_NAME_NOT_RESOLVED"
        ERROR_CONNECT -> "ERR_CONNECTION_REFUSED"
        ERROR_TIMEOUT -> "ERR_CONNECTION_TIMED_OUT"
        ERROR_BAD_URL -> "ERR_INVALID_URL"
        ERROR_UNSUPPORTED_SCHEME -> "ERR_UNKNOWN_URL_SCHEME"
        ERROR_FILE_NOT_FOUND -> "ERR_FILE_NOT_FOUND"
        ERROR_FAILED_SSL_HANDSHAKE -> "ERR_SSL_PROTOCOL_ERROR"
        ERROR_IO -> "ERR_CONNECTION_FAILED"
        ERROR_AUTHENTICATION -> "ERR_ACCESS_DENIED"
        ERROR_PROXY_AUTHENTICATION -> "ERR_PROXY_AUTH_UNSUPPORTED"
        else -> "ERR_FAILED"
    }

    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        if (ignoreSslError) {
            handler?.proceed()
            return
        }

        val errorMessage = when (error?.primaryError) {
            SslError.SSL_UNTRUSTED -> "SSL证书不可信"
            SslError.SSL_EXPIRED -> "SSL证书已过期"
            SslError.SSL_IDMISMATCH -> "SSL证书主机名不匹配"
            SslError.SSL_NOTYETVALID -> "SSL证书尚未生效"
            SslError.SSL_DATE_INVALID -> "SSL证书日期无效"
            else -> "SSL证书错误"
        }

        val errorDetails = "net::ERR_SSL_PROTOCOL_ERROR"

        onError?.invoke(view, null, errorMessage, errorDetails)
        handler?.proceed() // 取消加载
    }
}
