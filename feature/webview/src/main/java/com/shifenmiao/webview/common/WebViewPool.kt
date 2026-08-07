package com.shifenmiao.webview.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.os.MessageQueue
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import com.shifenmiao.webview.utils.Utils
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * HTML 生成器接口
 *
 * 用于解耦 WebView Pool 与具体的 HTML 模版实现
 */
fun interface HtmlGenerator<T> {
    /**
     * 根据配置生成 HTML
     */
    fun generate(config: T): String
}

/**
 * 预加载的 HTML 缓存项
 *
 * @param config 配置对象，用于匹配检查
 * @param html 预生成的 HTML 内容
 */
data class PreloadedEntry(
    val config: Any,
    val html: String
)

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
 * WebView 内核预热 & HTML 预拼接缓存
 *
 * **不复用 WebView 实例**——每个页面独立创建、独立销毁，彻底杜绝跨页面状态污染。
 * 本对象的职责仅为：
 * 1. 在 Application 启动后利用主线程空闲时间创建并销毁一个临时 WebView，
 *    触发 Chromium 引擎初始化（~200ms），后续 `new WebView()` 将降至 ~5ms。
 * 2. 提供 HTML 模版预拼接缓存（[preloadHtml] / [getPreloadedHtml]），
 *    让业务模块在 Component 初始化时提前生成 HTML 字符串，减少页面进入延迟。
 * 3. 提供 [create] 工厂方法，统一 WebView 的 LayoutParams 和背景色设置。
 *
 * 使用方式：
 * 1. 在 Application.onCreate() 中调用 [init]
 * 2. 调用 [initWhenIdle] 在主线程空闲时预热 Chromium 引擎
 * 3. 各业务模块调用 [preloadHtml] 预拼接 HTML
 * 4. 使用 [create] 创建新 WebView，使用完毕后自行调用 `webView.destroy()`
 * 5. 使用 [getPreloadedHtml] 获取预拼接的 HTML
 */
object WebViewPool {

    private val isInitialized = AtomicBoolean(false)
    private val isEngineWarmedUp = AtomicBoolean(false)

    private var applicationContext: Context? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    // 后台线程池，用于 HTML 生成等耗时操作
    private val executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "WebViewPool-Background").apply {
            isDaemon = true
        }
    }

    // 多业务预加载缓存: key -> PreloadedEntry（使用 ConcurrentHashMap 保证线程安全）
    private val preloadedCache = ConcurrentHashMap<String, PreloadedEntry>()

    /**
     * 初始化（应在 Application 中调用）
     */
    fun init(context: Context) {
        if (isInitialized.compareAndSet(false, true)) {
            applicationContext = context.applicationContext
        }
    }

    /**
     * 检查是否已初始化
     */
    fun isInitialized(): Boolean = isInitialized.get()

    /**
     * 检查 WebView 内核是否已预热
     */
    fun isWebViewReady(): Boolean = isEngineWarmedUp.get()

    /**
     * 在主线程空闲时预热 WebView 内核
     *
     * 创建一个临时 WebView 并加载 about:blank 来触发 Chromium 引擎初始化，
     * 完成后立即销毁该 WebView。后续 new WebView() 都将是轻量操作。
     */
    fun initWhenIdle() {
        if (!isInitialized.get()) return
        if (isEngineWarmedUp.get()) return

        val context = applicationContext ?: return

        mainHandler.post {
            Looper.myQueue().addIdleHandler(object : MessageQueue.IdleHandler {
                override fun queueIdle(): Boolean {
                    if (isEngineWarmedUp.compareAndSet(false, true)) {
                        warmUpEngine(context)
                    }
                    return false
                }
            })
        }
    }

    /**
     * 预热 Chromium 引擎：创建临时 WebView，加载空白页触发引擎初始化，然后销毁
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

    // ======================== HTML 预拼接缓存 ========================

    /**
     * 预加载 HTML 配置并提前拼接 HTML 模版（同步执行）
     *
     * 在页面 Component 初始化时调用，保存配置信息并预先生成 HTML
     * 每个业务模块使用独立的 key 区分，互不影响
     *
     * 注意：HTML 拼接耗时极短（1-5ms），同步执行比线程调度更高效
     *
     * @param key 业务标识 key，用于区分不同业务的预加载内容
     * @param config 预加载配置
     * @param generator HTML 生成器
     */
    fun <T : Any> preloadHtml(key: String, config: T, generator: HtmlGenerator<T>) {
        if (!isInitialized.get()) return

        val html = generator.generate(config)
        preloadedCache[key] = PreloadedEntry(config, html)
    }

    /**
     * 预加载 HTML 配置并提前拼接 HTML 模版（异步执行）
     *
     * 适用于 HTML 生成耗时较长（>50ms）的场景
     *
     * @param key 业务标识 key，用于区分不同业务的预加载内容
     * @param config 预加载配置
     * @param generator HTML 生成器
     */
    fun <T : Any> preloadHtmlAsync(key: String, config: T, generator: HtmlGenerator<T>) {
        if (!isInitialized.get()) return

        executor.execute {
            val html = generator.generate(config)
            preloadedCache[key] = PreloadedEntry(config, html)
        }
    }

    /**
     * 获取预拼接的 HTML
     *
     * @param key 业务标识 key
     * @param config 配置对象，用于验证缓存是否匹配
     * @return 预拼接的 HTML，如果配置未变化则返回缓存，否则返回 null
     */
    fun <T : Any> getPreloadedHtml(key: String, config: T): String? {
        val entry = preloadedCache[key] ?: return null
        return if (entry.config == config) {
            entry.html
        } else {
            null
        }
    }

    /**
     * 清除指定业务的预加载缓存
     *
     * @param key 业务标识 key
     */
    fun clearPreloadedHtml(key: String) {
        preloadedCache.remove(key)
    }

    // ======================== WebView 工厂 ========================

    /**
     * 创建新的 WebView 实例
     *
     * 每次调用都会创建全新的 WebView，不会复用旧实例。
     * 调用方用完后应自行调用 [destroyWebView] 或直接 `webView.destroy()` 销毁。
     *
     * @param context Context
     * @param backgroundColor 背景色
     * @return 全新的 WebView 实例
     */
    fun create(context: Context, backgroundColor: Int = Color.TRANSPARENT): WebView {
        return WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(backgroundColor)
        }
    }

    /**
     * 安全销毁 WebView
     *
     * 立即停止加载、移除 JS Bridge、暂停渲染、从父视图移除；
     * **延迟**执行 clearHistory / clearCache / destroy，
     * 确保 RenderThread 完成当前帧后再释放原生 GPU 资源，
     * 避免在 Compose AndroidView 退出组合时出现 SIGSEGV。
     *
     * 建议在 DisposableEffect.onDispose 或 Lifecycle.onDestroy 中调用。
     *
     * @param webView 要销毁的 WebView
     * @param bridgeName 可选的 JavaScript 接口名称，销毁前移除
     */
    fun destroyWebView(webView: WebView, bridgeName: String? = null) {
        webView.apply {
            // ---- 同步：立刻切断内容 & 停止渲染 ----
            stopLoading()
            if (bridgeName != null) {
                removeJavascriptInterface(bridgeName)
            }
            // 暂停 WebView 渲染管线，防止 RenderThread 继续访问 GPU 资源
            onPause()
            // 切换为无 layer，脱离硬件加速渲染管线
            setLayerType(android.view.View.LAYER_TYPE_NONE, null)
            // 加载空白页，让 Chromium 释放当前页面的渲染资源
            loadUrl("about:blank")
            (parent as? ViewGroup)?.removeView(this)
        }

        // ---- 异步：等 RenderThread 完成当前帧后再销毁 ----
        mainHandler.postDelayed({
            try {
                webView.clearHistory()
                webView.clearCache(false)
                webView.clearFormData()
                webView.destroy()
            } catch (_: Exception) {
                // WebView 可能已被回收，忽略
            }
        }, 500L)
    }

    /**
     * 清空所有预加载缓存
     */
    fun clear() {
        preloadedCache.clear()
    }
}
