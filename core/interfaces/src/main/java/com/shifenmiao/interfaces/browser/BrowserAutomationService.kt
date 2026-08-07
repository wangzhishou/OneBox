package com.shifenmiao.interfaces.browser

import kotlinx.coroutines.flow.StateFlow

/**
 * AI Agent 操控内置浏览器的桥接接口。
 *
 * 设计原则：
 * - AI 工具层只依赖此接口，不直接依赖 BrowserComponent 或 WebView
 * - BrowserComponent 在生命周期内主动注册到此 Service，Service 转发操作指令
 * - 如果浏览器未激活，工具返回明确错误信息
 */
interface BrowserAutomationService {

    /** 浏览器是否当前可用（已 attach WebView） */
    val isAvailable: StateFlow<Boolean>

    /** 注册浏览器组件（由 BrowserComponent 在 attachWebView 时调用） */
    fun attach(controller: BrowserController)

    /** 注销浏览器组件（由 BrowserComponent 在 onDestroy 时调用） */
    fun detach()

    // ===== 导航操作 =====

    suspend fun navigateToUrl(url: String): BrowserActionResult
    suspend fun goBack(): BrowserActionResult
    suspend fun goForward(): BrowserActionResult
    suspend fun reload(): BrowserActionResult

    // ===== 读取页面内容 =====

    suspend fun getPageInfo(): BrowserPageInfo?
    suspend fun extractPageText(maxLength: Int = 8192): String?
    suspend fun extractDom(selector: String = "body"): String?

    // ===== JavaScript 执行 =====

    suspend fun executeJavaScript(script: String): String?

    // ===== 标签页管理 =====

    suspend fun getTabsInfo(): List<BrowserTabInfo>
    suspend fun switchTab(tabId: String): BrowserActionResult
    suspend fun createTab(url: String = ""): BrowserActionResult
    suspend fun closeTab(tabId: String): BrowserActionResult

    // ===== 截图 =====

    /** 截取当前页面并返回 Base64 PNG 字符串 */
    suspend fun captureScreenshot(): String?

    // ===== 快照 =====

    /** 获取当前页面状态快照（信息 + 标签页 + 文本预览） */
    suspend fun getSnapshot(): BrowserSnapshot?
}

/**
 * 浏览器控制器，由 BrowserComponent 实现并注册到 Service。
 * 封装所有实际 WebView 操作，Service 只做转发。
 */
interface BrowserController {
    fun loadUrl(url: String)
    fun goBack()
    fun goForward()
    fun reload()
    fun stopLoading()
    fun addTab(url: String = "", title: String = ""): String
    fun closeTab(tabId: String)
    fun switchTab(tabId: String)

    fun getCurrentUrl(): String
    fun getCurrentTitle(): String
    fun canGoBack(): Boolean
    fun canGoForward(): Boolean
    fun isLoading(): Boolean
    fun getTabs(): List<BrowserTabInfo>

    /** 在 WebView 上执行 JS 并通过 callback 返回结果（必须在主线程调用） */
    fun evaluateJavascript(script: String, callback: (String?) -> Unit)

    /** 截取 WebView 位图并返回 Base64 PNG（可能在后台线程） */
    suspend fun captureScreenshotBase64(): String?
}

// ===== 数据类 =====

data class BrowserActionResult(
    val success: Boolean,
    val message: String = "",
    val data: Map<String, Any?> = emptyMap()
) {
    companion object {
        fun success(message: String = "", data: Map<String, Any?> = emptyMap()) =
            BrowserActionResult(true, message, data)

        fun failure(message: String) =
            BrowserActionResult(false, message)
    }
}

data class BrowserPageInfo(
    val url: String,
    val title: String,
    val canGoBack: Boolean,
    val canGoForward: Boolean,
    val isLoading: Boolean
)

data class BrowserTabInfo(
    val id: String,
    val url: String,
    val title: String,
    val isActive: Boolean
)

data class BrowserSnapshot(
    val pageInfo: BrowserPageInfo,
    val tabs: List<BrowserTabInfo>,
    val textPreview: String
)
