package com.shifenmiao.webview.browser

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.print.PrintDocumentAdapter
import android.webkit.WebSettings
import android.webkit.WebView
import com.shifenmiao.webview.utils.WebViewExportUtils
import java.lang.ref.WeakReference

class AndroidWebViewNavigator : WebViewNavigator {

    private var webViewRef: WeakReference<WebView>? = null
    private var pendingContent: PendingContent? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    /** WebView 还没 attach 时先记住要渲染什么，attach 后补投。 */
    private sealed interface PendingContent {
        data class Url(val url: String) : PendingContent
        data class Html(val html: String, val baseUrl: String?) : PendingContent
    }

    override fun attach(webView: WebView) {
        val isSameInstance = webViewRef?.get() === webView
        webViewRef = WeakReference(webView)
        if (isSameInstance) return
        pendingContent?.let { content ->
            pendingContent = null
            postOnMain { applyContent(webView, content) }
        }
    }

    override fun detach() {
        pendingContent = null
        webViewRef?.clear()
        webViewRef = null
    }

    override fun isAttached(): Boolean = webViewRef?.get() != null

    override fun loadUrl(url: String) {
        val webView = webViewRef?.get()
        if (webView != null) {
            postOnMain { webView.loadUrl(url) }
        } else {
            pendingContent = PendingContent.Url(url)
        }
    }

    override fun loadHtml(html: String, baseUrl: String?) {
        val webView = webViewRef?.get()
        if (webView != null) {
            postOnMain { applyContent(webView, PendingContent.Html(html, baseUrl)) }
        } else {
            pendingContent = PendingContent.Html(html, baseUrl)
        }
    }

    private fun applyContent(webView: WebView, content: PendingContent) {
        when (content) {
            is PendingContent.Url -> webView.loadUrl(content.url)
            is PendingContent.Html -> webView.loadDataWithBaseURL(
                content.baseUrl,
                content.html,
                "text/html",
                "utf-8",
                null,
            )
        }
    }

    override fun goBack() {
        webViewRef?.get()?.let { postOnMain { if (it.canGoBack()) it.goBack() } }
    }

    override fun goForward() {
        webViewRef?.get()?.let { postOnMain { if (it.canGoForward()) it.goForward() } }
    }

    override fun reload() {
        webViewRef?.get()?.let { postOnMain { it.reload() } }
    }

    override fun stopLoading() {
        webViewRef?.get()?.let { postOnMain { it.stopLoading() } }
    }

    override fun canGoBack(): Boolean = webViewRef?.get()?.canGoBack() == true

    override fun canGoForward(): Boolean = webViewRef?.get()?.canGoForward() == true

    override fun captureBitmap(): Bitmap? {
        val webView = webViewRef?.get() ?: return null
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return WebViewExportUtils.captureWebViewToBitmap(webView)
        }
        var result: Bitmap? = null
        val latch = java.util.concurrent.CountDownLatch(1)
        mainHandler.post {
            result = try {
                WebViewExportUtils.captureWebViewToBitmap(webView)
            } catch (_: Exception) {
                null
            }
            latch.countDown()
        }
        latch.await(5, java.util.concurrent.TimeUnit.SECONDS)
        return result
    }

    override fun createPrintDocumentAdapter(jobName: String): PrintDocumentAdapter? {
        return webViewRef?.get()?.createPrintDocumentAdapter(jobName)
    }

    override fun applySettings(settings: BrowserSettings) {
        webViewRef?.get()?.let { webView ->
            postOnMain {
                webView.settings.apply {
                    javaScriptEnabled = settings.enableJavaScript
                    textZoom = settings.textSize
                }
                val ua = settings.effectiveUserAgent
                if (ua != null) {
                    webView.settings.userAgentString = ua
                }
            }
        }
    }

    override fun clearCache() {
        webViewRef?.get()?.let { webView ->
            postOnMain {
                webView.clearCache(true)
                webView.clearHistory()
                webView.clearFormData()
            }
        }
    }

    override fun evaluateJavascript(script: String, callback: (String?) -> Unit) {
        val webView = webViewRef?.get()
        if (webView != null) {
            postOnMain { webView.evaluateJavascript(script) { result -> callback(result) } }
        } else {
            callback(null)
        }
    }

    private fun postOnMain(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post(action)
        }
    }
}
