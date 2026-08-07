package com.wanbaohe.markdown.edit.webview

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
 * WebView Markdown 编辑器状态
 *
 * 用于管理编辑器的 WebView 引用和内容获取
 */
@Stable
class WebViewMarkdownEditorState {
    internal var webView: WebView? by mutableStateOf(null)

    /**
     * 获取当前编辑器内容
     */
    suspend fun getContent(): String = suspendCancellableCoroutine { continuation ->
        val wv = webView
        if (wv == null) {
            continuation.resume("")
            return@suspendCancellableCoroutine
        }

        wv.evaluateJavascript("(window.AndroidBridge && window.AndroidBridge.getContent) ? window.AndroidBridge.getContent() : ''") { result ->
            // 使用 JSON 解析来正确处理转义字符
            val content = try {
                if (result == null || result == "null" || result == "\"\"") {
                    ""
                } else {
                    // evaluateJavascript 返回的是 JSON 编码的字符串
                    // 用 JSONArray 技巧来正确解析
                    JSONArray("[$result]").getString(0)
                }
            } catch (e: Exception) {
                // 降级处理
                result?.trim()?.removeSurrounding("\"") ?: ""
            }
            continuation.resume(content)
        }
    }

    /**
     * 清除草稿（保存成功后调用）
     */
    fun clearDraft() {
        webView?.evaluateJavascript(
            "if (window.AndroidBridge && window.AndroidBridge.clearDraft) { window.AndroidBridge.clearDraft(); }",
            null
        )
    }

    /**
     * 设置内容
     */
    fun setContent(markdown: String) {
        val escapedValue = markdown
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
        webView?.evaluateJavascript(
            "if (window.AndroidBridge && window.AndroidBridge.setContent) { window.AndroidBridge.setContent(\"$escapedValue\"); }",
            null
        )
    }

    /**
     * 导出为PDF
     */
    fun exportToPdf() {
        webView?.evaluateJavascript("window.print()", null)
    }
}

/**
 * 创建并记住 WebViewMarkdownEditorState
 */
@Composable
fun rememberWebViewMarkdownEditorState(): WebViewMarkdownEditorState {
    return remember { WebViewMarkdownEditorState() }
}

