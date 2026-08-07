package com.shifenmiao.webview.browser

import android.graphics.Bitmap
import android.print.PrintDocumentAdapter
import android.webkit.WebView

interface WebViewNavigator {
    fun attach(webView: WebView)
    fun detach()
    fun isAttached(): Boolean
    fun loadUrl(url: String)
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
