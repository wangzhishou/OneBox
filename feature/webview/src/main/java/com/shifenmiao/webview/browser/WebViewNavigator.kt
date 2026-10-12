package com.shifenmiao.webview.browser

import android.graphics.Bitmap
import android.print.PrintDocumentAdapter
import android.webkit.WebView

interface WebViewNavigator {
    /**
     * 把 WebView 接上。
     *
     * @return 挂上之前已经排队的内容（网址或收藏快照）是否已投给这个实例。
     * 为 true 时调用方不要再 loadUrl，否则会把快照盖掉。
     */
    fun attach(webView: WebView): Boolean
    fun detach()
    fun isAttached(): Boolean
    fun loadUrl(url: String)

    /**
     * 用 [baseUrl] 作为文档基准地址渲染一段 HTML 字符串（收藏快照离线查看）。
     * baseUrl 只影响相对路径/Cookie 归属，地址栏与历史仍是原始网址。
     */
    fun loadHtml(html: String, baseUrl: String?)

    fun goBack()
    fun goForward()
    fun reload()
    fun stopLoading()
    fun canGoBack(): Boolean
    fun canGoForward(): Boolean
    fun captureBitmap(): Bitmap?
    fun createPrintDocumentAdapter(jobName: String): PrintDocumentAdapter?
    fun evaluateJavascript(script: String, callback: (String?) -> Unit)
    fun applySettings(settings: BrowserSettings)
    fun clearCache()
}
