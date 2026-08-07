package com.wanbaohe.markdown.edit.webview

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.JavascriptInterface
import androidx.annotation.Keep

/**
 * Markdown 编辑器 JavaScript Bridge
 *
 * 提供 Android 与 WebView 中 Milkdown 编辑器的双向通信
 *
 * 注意：类和方法使用 @Keep 注解防止被 ProGuard/R8 混淆
 */
@Keep
class MarkdownEditorBridge(
    private val context: Context,
    private val onContentChanged: (String) -> Unit,
    private val onEditorReady: () -> Unit,
    private val onPickImage: () -> Unit,
    private val onShowImageResizeDialog: ((Int, Int, Int, Int) -> Unit)? = null,
    private val onShowImageDialog: (() -> Unit)? = null,
    private val onShowLinkDialog: ((String) -> Unit)? = null,
    private val onEditorScroll: ((Int, Int) -> Unit)? = null
) {
    private val clipboardManager: ClipboardManager by lazy {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    /**
     * 当编辑器内容变化时由 JS 调用
     */
    @Keep
    @JavascriptInterface
    fun onContentChanged(markdown: String) {
        onContentChanged.invoke(markdown)
    }

    /**
     * 当编辑器初始化完成时由 JS 调用
     */
    @Keep
    @JavascriptInterface
    fun onEditorReady() {
        onEditorReady.invoke()
    }

    /**
     * 请求从相册选择图片
     */
    @Keep
    @JavascriptInterface
    fun pickImage() {
        onPickImage.invoke()
    }

    /**
     * 显示图片缩放弹窗
     * @param index 图片索引
     * @param currentWidth 当前宽度
     * @param maxWidth 最大宽度
     * @param naturalWidth 原始宽度
     */
    @Keep
    @JavascriptInterface
    fun showImageResizeDialog(index: Int, currentWidth: Int, maxWidth: Int, naturalWidth: Int) {
        onShowImageResizeDialog?.invoke(index, currentWidth, maxWidth, naturalWidth)
    }

    /**
     * 显示插入图片对话框
     */
    @Keep
    @JavascriptInterface
    fun showImageDialog() {
        onShowImageDialog?.invoke()
    }

    /**
     * 显示插入链接对话框
     * @param selectedText 当前选中的文字，作为链接文字的默认值
     */
    @Keep
    @JavascriptInterface
    fun showLinkDialog(selectedText: String) {
        onShowLinkDialog?.invoke(selectedText)
    }

    /**
     * 当编辑区内部滚动时调用
     */
    @Keep
    @JavascriptInterface
    fun onEditorScroll(scrollTop: Int, deltaY: Int) {
        onEditorScroll?.invoke(scrollTop, deltaY)
    }

    /**
     * 把文本写入系统剪贴板
     *
     * WebView 的 navigator.clipboard 在 Android 上不稳定,这里走原生 ClipboardManager。
     * @return 是否成功
     */
    @Keep
    @JavascriptInterface
    fun copyToClipboard(text: String): Boolean {
        return try {
            clipboardManager.setPrimaryClip(ClipData.newPlainText("milkdown", text))
            true
        } catch (e: Exception) {
            android.util.Log.e("MarkdownEditorBridge", "copyToClipboard failed", e)
            false
        }
    }

    /**
     * 从系统剪贴板读取纯文本
     *
     * 若剪贴板无文本或读取失败,返回空字符串。
     */
    @Keep
    @JavascriptInterface
    fun pasteFromClipboard(): String {
        return try {
            val clip = clipboardManager.primaryClip
            if (clip != null && clip.itemCount > 0) {
                clip.getItemAt(0).text?.toString().orEmpty()
            } else {
                ""
            }
        } catch (e: Exception) {
            android.util.Log.e("MarkdownEditorBridge", "pasteFromClipboard failed", e)
            ""
        }
    }

    companion object {
        const val BRIDGE_NAME = "Android"
    }
}
