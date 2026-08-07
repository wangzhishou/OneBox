package com.wanbaohe.code.editor.webview

import android.webkit.JavascriptInterface
import androidx.annotation.Keep

/**
 * CodeEditor JavaScript Bridge
 *
 * 提供 Android ↔ CodeMirror 6 WebView 双向通信
 *
 * 注意：类和方法使用 @Keep 注解防止被 ProGuard/R8 混淆
 */
@Keep
class CodeEditorBridge(
    private val onContentChanged: (String) -> Unit,
    private val onEditorReady: () -> Unit,
    private val onCursorChange: ((Int, Int) -> Unit)? = null,
    private val onSelectionChange: ((Int, Int) -> Unit)? = null,
    private val onEditorScroll: ((Int, Int) -> Unit)? = null,
    private val onPopupStateChanged: ((Boolean) -> Unit)? = null
) {
    @Keep
    @JavascriptInterface
    fun onContentChanged(text: String) {
        onContentChanged.invoke(text)
    }

    @Keep
    @JavascriptInterface
    fun onEditorReady() {
        onEditorReady.invoke()
    }

    @Keep
    @JavascriptInterface
    fun onCursorChange(line: Int, column: Int) {
        onCursorChange?.invoke(line, column)
    }

    @Keep
    @JavascriptInterface
    fun onSelectionChange(from: Int, to: Int) {
        onSelectionChange?.invoke(from, to)
    }

    @Keep
    @JavascriptInterface
    fun onEditorScroll(scrollTop: Int, deltaY: Int) {
        onEditorScroll?.invoke(scrollTop, deltaY)
    }

    @Keep
    @JavascriptInterface
    fun onPopupStateChanged(isOpen: Boolean) {
        onPopupStateChanged?.invoke(isOpen)
    }

    companion object {
        const val BRIDGE_NAME = "Android"
    }
}
