package com.wanbaohe.code.editor.webview

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import kotlin.coroutines.resume

/**
 * WebView CodeEditor 状态
 *
 * **所有 evaluateJavascript 调用都通过 [WebView.post] 投递到主线程**,
 * 这样不论调用方在哪个协程/线程上下文,都能避免 "WebView method was called on
 * thread 'DefaultDispatcher-worker-X'" 异常。
 *
 * **WebView 引用安全**: getContent() 等异步回调内,二次校验 `wv === webView`,
 * 避免对已替换/已销毁的旧 WebView 调用。
 *
 * **JS 字符串转义**: 所有用户文本嵌入 JS 字面量前都通过 [escapeJsString],
 * 避免 `\u2028`/`\u2029` 等特殊 Unicode 字符导致 "Unterminated string literal"。
 */
@Stable
class WebViewCodeEditorState {
    internal var webView: WebView? by mutableStateOf(null)

    var isPopupOpen: Boolean by mutableStateOf(false)

    suspend fun getContent(): String = suspendCancellableCoroutine { continuation ->
        val wv = webView
        if (wv == null) {
            continuation.resume("")
            return@suspendCancellableCoroutine
        }
        wv.post {
            // WebView 引用安全:WebView 可能已被替换/销毁
            if (wv !== webView) {
                continuation.resume("")
                return@post
            }
            wv.evaluateJavascript(
                "(window.CodeEditorBridge && window.CodeEditorBridge.getContent) ? window.CodeEditorBridge.getContent() : ''"
            ) { result ->
                val content = try {
                    if (result == null || result == "null" || result == "\"\"") {
                        ""
                    } else {
                        JSONArray("[$result]").getString(0)
                    }
                } catch (e: Exception) {
                    result?.trim()?.removeSurrounding("\"") ?: ""
                }
                continuation.resume(content)
            }
        }
    }

    fun clearDraft() {
        postToWebView(
            "if (window.CodeEditorBridge && window.CodeEditorBridge.clearDraft) { window.CodeEditorBridge.clearDraft(); }"
        )
    }

    fun setContent(text: String) {
        val escaped = escapeJsString(text)
        postToWebView(
            "if (window.CodeEditorBridge && window.CodeEditorBridge.setContent) { window.CodeEditorBridge.setContent(\"$escaped\"); }"
        )
    }

    fun setLanguage(language: String) {
        val escaped = escapeJsString(language)
        postToWebView(
            "if (window.CodeEditorBridge && window.CodeEditorBridge.setLanguage) { window.CodeEditorBridge.setLanguage(\"$escaped\"); }"
        )
    }

    fun focus() {
        postToWebView(
            "if (window.CodeEditorBridge && window.CodeEditorBridge.focus) { window.CodeEditorBridge.focus(); }"
        )
    }

    fun blur() {
        postToWebView(
            "if (window.CodeEditorBridge && window.CodeEditorBridge.blur) { window.CodeEditorBridge.blur(); }"
        )
    }

    fun undo() {
        postToWebView(
            "if (window.CodeEditorBridge && window.CodeEditorBridge.undo) { window.CodeEditorBridge.undo(); }"
        )
    }

    fun redo() {
        postToWebView(
            "if (window.CodeEditorBridge && window.CodeEditorBridge.redo) { window.CodeEditorBridge.redo(); }"
        )
    }

    fun selectAll() {
        postToWebView(
            "if (window.CodeEditorBridge && window.CodeEditorBridge.selectAll) { window.CodeEditorBridge.selectAll(); }"
        )
    }

    fun insertText(text: String) {
        val escaped = escapeJsString(text)
        postToWebView(
            "if (window.CodeEditorBridge && window.CodeEditorBridge.insertText) { window.CodeEditorBridge.insertText(\"$escaped\"); }"
        )
    }

    fun setSelection(from: Int, to: Int) {
        postToWebView(
            "if (window.CodeEditorBridge && window.CodeEditorBridge.setSelection) { window.CodeEditorBridge.setSelection($from, $to); }"
        )
    }

    fun find(query: String, caseSensitive: Boolean = false) {
        val escaped = escapeJsString(query)
        postToWebView(
            "if (window.CodeEditorBridge && window.CodeEditorBridge.find) { window.CodeEditorBridge.find(\"$escaped\", $caseSensitive); }"
        )
    }

    fun format() {
        postToWebView(
            "if (window.CodeEditorBridge && window.CodeEditorBridge.format) { window.CodeEditorBridge.format(); }"
        )
    }

    fun openSearch() {
        postToWebView(
            "if (window.CodeEditorBridge && window.CodeEditorBridge.openSearch) { window.CodeEditorBridge.openSearch(); }"
        )
    }

    fun closePopup() {
        postToWebView(
            "(window.CodeEditorBridge && window.CodeEditorBridge.closePopup) ? window.CodeEditorBridge.closePopup() : false"
        )
    }

    /**
     * 通过 [WebView.post] 把 JS 调用投递到主线程
     *
     * 这样不论调用方在哪个协程/线程上下文(包括 Dispatchers.IO、Dispatchers.Default、JS 桥接回调等),
     * 都能保证 evaluateJavascript 在主线程上执行,避免 WebView 跨线程异常。
     *
     * 同时在校验 `wv === webView`,防止 WebView 在 post 排队与执行之间被替换时,
     * JS 命令发到错误的实例(跨实例的状态污染)。
     */
    private fun postToWebView(js: String) {
        val wv = webView ?: return
        wv.post {
            if (wv !== webView) return@post
            wv.evaluateJavascript(js, null)
        }
    }
}

@Composable
fun rememberWebViewCodeEditorState(): WebViewCodeEditorState {
    return remember { WebViewCodeEditorState() }
}
