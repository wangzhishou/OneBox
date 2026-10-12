package com.shifenmiao.webview.diagnostics

import android.content.Context
import android.util.Log
import android.webkit.WebView
import androidx.webkit.NavigationListener
import androidx.webkit.Page
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.shifenmiao.webview.BuildConfig

private const val TAG = "WebViewDiagnostics"

/**
 * WebView 内核能力探测 + 页面性能埋点。
 *
 * 存在的理由：全仓此前没有任何 `WebViewFeature` 能力检查，也没有页面级性能数字，
 * 所有"哪台设备上会怎样"的判断都只能靠猜。这里把两件事收敛到一个地方：
 *
 * 1. **能力探测**——按内核真实版本/特性判断该走哪条路，而不是按版本号硬 gate。
 *    当前唯一的使用点是 mermaid：它要求 ES2020 语法（见 [MIN_CHROMIUM_MAJOR_FOR_MERMAID]）。
 * 2. **性能埋点**——FCP / LCP 只在 debug 构建里打日志，用于量现状。
 *    正式包不打：URL 不该进日志，也不需要这点噪声。
 *
 * 未来如果要接内核版本上报（给国内渠道统计碎片用），[kernelVersionName] 就是取数口子，
 * 不用再动这里以外的逻辑。
 */
object WebViewDiagnostics {

    /**
     * mermaid 所需的 Chromium 主版本下限。
     *
     * `assets/js/mermaid.min.js` 是第三方预编译产物、`<script>` 直接引入、不过 esbuild 降级，
     * 里面实测有 543 处可选链 `?.` 与 422 处空值合并 `??`——ES2020 语法。
     * Chromium 80（2020-02）是硬下限，低于它的内核对这个 2.5 MB 脚本会在**解析阶段**直接抛
     * SyntaxError，`window.mermaid` 永远是 undefined。
     *
     * 对照：App 自己的两个 bundle 分别打 `es2015` / `es2017`，下限比它低得多。
     */
    const val MIN_CHROMIUM_MAJOR_FOR_MERMAID = 80

    /**
     * 当前 WebView 内核的包版本名，形如 `153.0.8010.36`。
     *
     * 取不到的情况是存在的（例如在独立进程里尚未加载），返回 null 而不是抛异常。
     */
    fun kernelVersionName(context: Context): String? =
        runCatching {
            WebViewCompat.getCurrentWebViewPackage(context)?.versionName
        }.getOrNull()

    /**
     * 从内核包版本推出的 Chromium 主版本号。取不到返回 null。
     */
    fun chromiumMajor(context: Context): Int? =
        kernelVersionName(context)
            ?.substringBefore('.')
            ?.toIntOrNull()

    /**
     * 内核是否具备 mermaid 所需的 ES2020 语法能力。
     *
     * 版本取不到时**返回 true**：宁可保持现状（让 WebView 去试、失败走既有错误路径），
     * 也不要因为读不到版本就误判成"不支持"而把正常设备上的图表关掉。
     */
    fun supportsEs2020(context: Context): Boolean {
        val major = chromiumMajor(context) ?: return true
        return major >= MIN_CHROMIUM_MAJOR_FOR_MERMAID
    }

    /**
     * 打一行内核信息。只在 debug 构建输出。
     */
    fun logKernelInfo(context: Context) {
        if (!BuildConfig.DEBUG) return
        Log.d(
            TAG,
            "kernel=${kernelVersionName(context) ?: "unknown"} " +
                "chromiumMajor=${chromiumMajor(context) ?: -1} " +
                "es2020=${supportsEs2020(context)}"
        )
    }

    /**
     * 给单个 WebView 挂页面性能埋点（FCP / LCP）。只在 debug 构建生效。
     *
     * 每条 WebView 都要单独挂；[com.shifenmiao.webview.common.WebViewPool.create] 是全应用唯一的
     * WebView 工厂，在那里挂一次就覆盖了所有页面。
     *
     * 注意：这个回调需要内核支持 `NAVIGATION_LISTENER`，老内核上直接跳过（不报错）。
     */
    fun attachPageMetrics(webView: WebView, tag: String) {
        if (!BuildConfig.DEBUG) return
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.NAVIGATION_LISTENER)) return

        runCatching {
            WebViewCompat.addNavigationListener(
                webView,
                object : NavigationListener {
                    override fun onPageLoadEvent(page: Page) {
                        Log.d(TAG, "[$tag] load ${page.url}")
                    }

                    override fun onFirstContentfulPaintMillis(page: Page, millis: Long) {
                        Log.d(TAG, "[$tag] FCP ${millis}ms ${page.url}")
                    }

                    override fun onLargestContentfulPaintMillis(page: Page, millis: Long) {
                        Log.d(TAG, "[$tag] LCP ${millis}ms ${page.url}")
                    }
                }
            )
        }.onFailure {
            Log.w(TAG, "attachPageMetrics failed (tag=$tag)", it)
        }
    }
}
