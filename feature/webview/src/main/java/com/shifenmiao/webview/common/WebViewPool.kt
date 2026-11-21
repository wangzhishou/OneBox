/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.webview.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.os.MessageQueue
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.web.WebContentView
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewOutcomeReceiver
import androidx.webkit.WebViewStartUpConfig
import androidx.webkit.WebViewStartUpResult
import androidx.webkit.WebViewStartupException
import com.shifenmiao.webview.diagnostics.WebViewDiagnostics
import com.shifenmiao.webview.utils.Utils
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "WebViewPool"

/**
 * WebView 通用配置工具
 */
object WebViewSettings {

    /**
     * 应用通用的 WebView 设置
     *
     * @param webView 要配置的 WebView
     * @param context Context，用于获取缓存路径等
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun applyCommonSettings(webView: WebView, context: Context) {
        webView.settings.apply {
            // 基础设置（所有 WebView 都需要）
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

            // 缓存
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

            // 缩放设置
            setSupportZoom(true)
            builtInZoomControls = false
            displayZoomControls = false

            // 文本和字体
            textZoom = 100
            defaultFontSize = 16
            minimumFontSize = 12
            defaultTextEncodingName = "utf-8"

            // 图片加载
            loadsImagesAutomatically = true

            // 窗口和布局
            setSupportMultipleWindows(false)
            javaScriptCanOpenWindowsAutomatically = true
            layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
            loadWithOverviewMode = false
            useWideViewPort = true

            // 安全设置
            allowFileAccess = true
            allowContentAccess = true

            // 地理位置
            setGeolocationEnabled(true)

            // 焦点
            setNeedInitialFocus(true)

            // 自定义 UA
            userAgentString = Utils.customUserAgent(webView)
        }
    }
}

/**
 * WebView 内核预热与视图工厂。
 *
 * 页面会话由 PersistentWebView 管理，这里不持有或复用页面视图。
 */
object WebViewPool {

    private val isInitialized = AtomicBoolean(false)

    /** 预热是否已经发起过（与"是否已完成"分开：startUpWebView 是异步的） */
    private val isStartupRequested = AtomicBoolean(false)

    private var applicationContext: Context? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val startupExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "WebViewPool-Startup").apply {
            isDaemon = true
        }
    }

    /**
     * 初始化（应在 Application 中调用）
     */
    fun init(context: Context) {
        if (isInitialized.compareAndSet(false, true)) {
            applicationContext = context.applicationContext
            // 打一行内核信息（debug 构建）：版本、Chromium 主版本、是否具备 mermaid 所需的 ES2020。
            // 国内渠道没有任何回传通道，这是目前唯一能看到"设备上到底是什么内核"的地方。
            WebViewDiagnostics.logKernelInfo(context)
        }
    }

    /**
     * 在主线程空闲时预热 WebView 内核
     *
     * 优先走官方的 [WebViewCompat.startUpWebView]（androidx.webkit 1.16.0 起 stable）。
     * 官方文档明确说明：隐式启动 WebView 是 "entirely on the main thread" 的，
     * 会 "drastically increases the risk of Application Not Responding (ANRs)"；
     * 而靠 getUserAgentString() 之类的手工预热属于 "unsupported practices"，
     * 行为可能在后续 WebView 版本里变化。startUpWebView 把启动负载挪到后台线程并分块执行
     * （UI 线程那部分由 androidx.webkit 按当前内核能力决定怎么切）。
     *
     * 老内核上如果这一步失败，回退到原来的手工预热，保证引擎照样被拉起来。
     */
    fun initWhenIdle() {
        if (!isInitialized.get()) return
        if (isStartupRequested.get()) return

        val context = applicationContext ?: return

        mainHandler.post {
            Looper.myQueue().addIdleHandler(object : MessageQueue.IdleHandler {
                override fun queueIdle(): Boolean {
                    if (isStartupRequested.compareAndSet(false, true)) {
                        startUpEngine(context)
                    }
                    return false
                }
            })
        }
    }

    /**
     * 走官方启动路径；失败则回退手工预热。
     *
     * 用的是接 [WebViewOutcomeReceiver] 的那个重载 —— 另一个接
     * [WebViewCompat.WebViewStartUpCallback] 的重载在 androidx.webkit 1.17.1 里
     * 仍标着 `@ExperimentalAsyncStartUp`（lint 会以 UnsafeOptInUsageError 拦下），
     * OutcomeReceiver 这个才是 stable 的。重载有二义性，所以接收器必须写成显式的 object 表达式。
     */
    private fun startUpEngine(context: Context) {
        val started = runCatching {
            val config = WebViewStartUpConfig.Builder(startupExecutor).build()
            WebViewCompat.startUpWebView(
                context,
                config,
                object : WebViewOutcomeReceiver<WebViewStartUpResult, WebViewStartupException> {
                    override fun onResult(result: WebViewStartUpResult) {
                        Log.d(TAG, "startUpWebView finished: $result")
                    }

                    override fun onError(error: WebViewStartupException) {
                        Log.w(TAG, "startUpWebView failed", error)
                        // 启动失败不阻塞后续 new WebView()：那条路会自己触发隐式启动
                    }
                }
            )
        }.isSuccess

        if (!started) {
            warmUpEngine(context)
        }
    }

    /**
     * 老内核的兜底预热：创建临时 WebView，加载空白页触发引擎初始化，然后销毁
     */
    private fun warmUpEngine(context: Context) {
        mainHandler.post {
            val tempWebView = WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(0, 0)
                loadUrl("about:blank")
            }
            // 引擎初始化在 WebView 构造时已完成，loadUrl 进一步确保渲染管线就绪
            // 延迟销毁，确保内核初始化充分完成
            mainHandler.postDelayed({
                tempWebView.stopLoading()
                tempWebView.destroy()
            }, 1000)
        }
    }

    // ======================== WebView 工厂 ========================

    /**
     * 创建新的 WebView 实例
     *
     * 每次调用都会创建全新的 WebContentView，不会复用旧实例。
     *
     * @param context Context
     * @param backgroundColor 背景色
     * @return 全新的 WebView 实例
     */
    fun create(context: Context, backgroundColor: Int = Color.TRANSPARENT): WebContentView {
        return WebContentView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(backgroundColor)
            // 这里是全应用唯一的 WebView 工厂，埋点挂这一次就覆盖了所有页面。
            // 内部按 debug 构建 + 内核能力双重判断，正式包是空操作。
            WebViewDiagnostics.attachPageMetrics(this, tag = "webview")
        }
    }

}
