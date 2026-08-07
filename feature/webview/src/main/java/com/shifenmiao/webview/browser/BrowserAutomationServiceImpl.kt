package com.shifenmiao.webview.browser

import com.shifenmiao.interfaces.browser.BrowserActionResult
import com.shifenmiao.interfaces.browser.BrowserAutomationService
import com.shifenmiao.interfaces.browser.BrowserController
import com.shifenmiao.interfaces.browser.BrowserPageInfo
import com.shifenmiao.interfaces.browser.BrowserSnapshot
import com.shifenmiao.interfaces.browser.BrowserTabInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BrowserAutomationService 的 Hilt 单例实现。
 *
 * 内部持有一个 [BrowserController] 引用（由 BrowserComponent 主动注册），
 * 所有操作委托给 controller。浏览器未激活时返回错误/空值。
 */
@Singleton
class BrowserAutomationServiceImpl @Inject constructor() : BrowserAutomationService {

    @Volatile
    private var controller: BrowserController? = null

    private val _isAvailable = MutableStateFlow(false)
    override val isAvailable: StateFlow<Boolean> = _isAvailable

    override fun attach(controller: BrowserController) {
        this.controller = controller
        _isAvailable.value = true
    }

    override fun detach() {
        this.controller = null
        _isAvailable.value = false
    }

    // ===== 导航操作 =====

    override suspend fun navigateToUrl(url: String): BrowserActionResult {
        val ctrl = controller ?: return browserUnavailable()
        return withContext(Dispatchers.Main) {
            ctrl.loadUrl(url)
            BrowserActionResult.success("Navigating to: $url")
        }
    }

    override suspend fun goBack(): BrowserActionResult {
        val ctrl = controller ?: return browserUnavailable()
        return withContext(Dispatchers.Main) {
            ctrl.goBack()
            BrowserActionResult.success("Navigated back")
        }
    }

    override suspend fun goForward(): BrowserActionResult {
        val ctrl = controller ?: return browserUnavailable()
        return withContext(Dispatchers.Main) {
            ctrl.goForward()
            BrowserActionResult.success("Navigated forward")
        }
    }

    override suspend fun reload(): BrowserActionResult {
        val ctrl = controller ?: return browserUnavailable()
        return withContext(Dispatchers.Main) {
            ctrl.reload()
            BrowserActionResult.success("Page reloaded")
        }
    }

    // ===== 读取页面内容 =====

    override suspend fun getPageInfo(): BrowserPageInfo? {
        val ctrl = controller ?: return null
        return withContext(Dispatchers.Main) {
            BrowserPageInfo(
                url = ctrl.getCurrentUrl(),
                title = ctrl.getCurrentTitle(),
                canGoBack = ctrl.canGoBack(),
                canGoForward = ctrl.canGoForward(),
                isLoading = ctrl.isLoading()
            )
        }
    }

    override suspend fun extractPageText(maxLength: Int): String? {
        val ctrl = controller ?: return null
        val js = """
            (function() {
                var text = document.body ? document.body.innerText : '';
                return text.substring(0, $maxLength);
            })()
        """.trimIndent()
        return evaluateJs(ctrl, js)
    }

    override suspend fun extractDom(selector: String): String? {
        val ctrl = controller ?: return null
        val escaped = selector.replace("'", "\\'")
        val js = """
            (function() {
                var el = document.querySelector('$escaped');
                return el ? el.outerHTML : null;
            })()
        """.trimIndent()
        return evaluateJs(ctrl, js)
    }

    // ===== JavaScript 执行 =====

    override suspend fun executeJavaScript(script: String): String? {
        val ctrl = controller ?: return null
        return evaluateJs(ctrl, script)
    }

    // ===== 标签页管理 =====

    override suspend fun getTabsInfo(): List<BrowserTabInfo> {
        val ctrl = controller ?: return emptyList()
        return withContext(Dispatchers.Main) {
            ctrl.getTabs()
        }
    }

    override suspend fun switchTab(tabId: String): BrowserActionResult {
        val ctrl = controller ?: return browserUnavailable()
        return withContext(Dispatchers.Main) {
            ctrl.switchTab(tabId)
            BrowserActionResult.success("Switched to tab: $tabId")
        }
    }

    override suspend fun createTab(url: String): BrowserActionResult {
        val ctrl = controller ?: return browserUnavailable()
        return withContext(Dispatchers.Main) {
            val tabId = ctrl.addTab(url)
            BrowserActionResult.success(
                message = "Created new tab: $tabId",
                data = mapOf("tabId" to tabId)
            )
        }
    }

    override suspend fun closeTab(tabId: String): BrowserActionResult {
        val ctrl = controller ?: return browserUnavailable()
        return withContext(Dispatchers.Main) {
            ctrl.closeTab(tabId)
            BrowserActionResult.success("Closed tab: $tabId")
        }
    }

    // ===== 截图 =====

    override suspend fun captureScreenshot(): String? {
        val ctrl = controller ?: return null
        return ctrl.captureScreenshotBase64()
    }

    // ===== 快照 =====

    override suspend fun getSnapshot(): BrowserSnapshot? {
        val ctrl = controller ?: return null
        val pageInfo = getPageInfo() ?: return null
        val tabs = getTabsInfo()
        val textPreview = extractPageText(maxLength = 2048).orEmpty()
        return BrowserSnapshot(
            pageInfo = pageInfo,
            tabs = tabs,
            textPreview = textPreview
        )
    }

    // ===== 内部工具 =====

    private suspend fun evaluateJs(ctrl: BrowserController, script: String): String? {
        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { cont ->
                ctrl.evaluateJavascript(script) { result ->
                    if (cont.isActive) {
                        cont.resumeWith(Result.success(unquoteJsResult(result)))
                    }
                }
            }
        }
    }

    /**
     * WebView.evaluateJavascript 返回值会带双引号包裹字符串，
     * null 字面量返回 "null"。这里做统一清洗。
     */
    private fun unquoteJsResult(raw: String?): String? {
        if (raw == null || raw == "null") return null
        var result = raw
        if (result.startsWith("\"") && result.endsWith("\"")) {
            result = result.substring(1, result.length - 1)
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
        }
        return result
    }

    private fun browserUnavailable(): BrowserActionResult =
        BrowserActionResult.failure("浏览器当前不可用，请先打开内置浏览器页面")
}
