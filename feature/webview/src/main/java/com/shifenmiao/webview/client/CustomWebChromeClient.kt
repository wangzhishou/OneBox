package com.shifenmiao.webview.client

import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView

class CustomWebChromeClient(
    private val onProgress: (WebView?, Int) -> Unit
) : WebChromeClient() {

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        onProgress(view, newProgress)
    }

    override fun onConsoleMessage(message: ConsoleMessage): Boolean {
        message.messageLevel()
        Log.d(
            "Console.log", "${message.message()} -- From line " +
                    "${message.lineNumber()} of ${message.sourceId()}"
        )
        return true
    }
}
