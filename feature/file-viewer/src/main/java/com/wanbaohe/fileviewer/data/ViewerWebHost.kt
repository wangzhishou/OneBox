package com.wanbaohe.fileviewer.data

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.webkit.WebMessageCompat
import androidx.webkit.WebMessagePortCompat
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import androidx.webkit.WebViewCompat
import com.wanbaohe.fileviewer.domain.DocumentSource
import java.io.ByteArrayInputStream

/**
 * 一次预览请求：打开哪个页面、叫什名、多大、要不要夜间、读到哪。
 */
internal data class ViewerRequest(
    val uri: Uri,
    val page: String,
    val webViewFloor: Int,
    val extension: String,
    val displayName: String,
    val length: Long,
    val nightMode: Boolean = false,
    /** 页面自己没有深色样式时，交给 WebView 的算法暗化 */
    val forceDark: Boolean = false,
    val bookPosition: String? = null,
)

/** 页面通过 message port 回报的东西。全在主线程回调。 */
internal interface ViewerPageListener {
    /** 当前页（从 1 开始）与总页数。 */
    fun onPage(page: Int, total: Int)

    /** 搜索计数；[done] 为 false 表示索引还在建。 */
    fun onSearchCount(at: Int, total: Int, done: Boolean)

    /** 电子书阅读位置，形如 `at 3 1200`，原样存取。 */
    fun onBookPosition(value: String)
}

/**
 * WebView 宿主——整块移植自 gander 的 `ViewerActivity.showWeb`（MIT）。
 *
 * 设计要点（改之前先读）：
 * 1. 页面来自 `https://appassets.androidplatform.net/assets/viewer/<page>.html`，
 *    由 [WebViewAssetLoader] 从 APK 的 assets 直接供给。
 * 2. 文档**不走** AssetLoader，而是同一域名下的 `/doc/file.<ext>`。原因是
 *    `PathHandler` 只拿得到 path、拿不到 `Range` 头，而大文件必须支持 206。
 * 3. JS 永远看不到 `content://`，也拿不到 `file://`——`allowFileAccess` 与
 *    `allowContentAccess` 都是 false，非本域 http(s) 一律 404。
 * 4. 与页面之间只用 HTML5 MessageChannel（不用 `addJavascriptInterface`）：
 *    宿主发 `q<词>`/`n`/`p`/`c`/`g<页>`/`i1|i0`，页面回 `page <n> <总>`、
 *    `<at> <总> <done>`、`at <章> <偏移>`。页面侧解析在 `find.js`，
 *    解析不出来的一律丢弃。
 *
 * 刻意**没有**照抄 gander 的两处全局副作用：
 * `CookieManager.setAcceptCookie(false)`（进程级）与 Manifest 里的
 * `WebView.EnableSafeBrowsing/MetricsOptOut`（全 App 级）。
 */
internal class ViewerWebHost(
    private val source: DocumentSource,
    private val listener: ViewerPageListener,
) {

    private var webView: WebView? = null
    private var port: WebMessagePortCompat? = null
    private var released = false

    @SuppressLint("SetJavaScriptEnabled")
    fun create(context: Context, request: ViewerRequest): WebView {
        val web = WebView(context)
        webView = web

        with(web.settings) {
            javaScriptEnabled = true
            // 页面不需要本地存储：里面渲染的是不可信文档，留着只会给越界脚本一个落脚点
            domStorageEnabled = false
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            useWideViewPort = true
            loadWithOverviewMode = true
            allowFileAccess = false
            allowContentAccess = false
        }

        runCatching { android.webkit.CookieManager.getInstance() }
            .getOrNull()
            ?.setAcceptThirdPartyCookies(web, false)


        val assetLoader = WebViewAssetLoader.Builder()
            .setDomain(ASSET_HOST)
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
            .build()

        val mime = source.mimeType(request.uri, request.extension)
        val total = request.length

        web.webViewClient = object : WebViewClientCompat() {

            override fun shouldInterceptRequest(
                view: WebView,
                request0: WebResourceRequest,
            ): WebResourceResponse? {
                val url = request0.url
                if (url.host == ASSET_HOST && url.path?.startsWith("/doc/") == true) {
                    return documentResponse(
                        uri = request.uri,
                        mime = mime,
                        total = total,
                        rangeHeader = request0.requestHeaders?.get("Range"),
                    )
                }
                return assetLoader.shouldInterceptRequest(url)?.withViewerPolicy()
                    ?: when (url.scheme) {
                        // 页面里漏网的网络请求不打到真实网络，也不交给 WebView 的默认实现
                        "http", "https" -> notFound()
                        // data:/blob: 是页面自己手里的字节；file:/content: 已被上面的开关挡掉
                        else -> null
                    }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request0: WebResourceRequest,
            ): Boolean = !isViewerPage(request0.url)

            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                if (request.forceDark) applyForceDarkTo(view, on = true)
                openPort(view)
            }

            override fun onRenderProcessGone(
                view: WebView,
                detail: android.webkit.RenderProcessGoneDetail,
            ): Boolean {
                // 返回 true 表示"我处理了"；返回 false 会让系统杀掉整个 App 进程
                Log.w(TAG, "WebView renderer gone, didCrash=${detail.didCrash()}")
                return true
            }
        }

        web.loadUrl(buildPageUrl(request, context, web.settings.userAgentString, total))
        return web
    }

    /** 页面离开时调用：关端口、断引用，WebView 本身由 Compose 的 onRelease 销毁。 */
    fun release() {
        released = true
        runCatching { port?.close() }
        port = null
        webView = null
    }

    /** 给页面发一条命令；端口还没开时静默丢弃。 */
    fun send(command: String) {
        if (released) return
        val target = port ?: return
        runCatching {
            target.postMessage(WebMessageCompat(command))
        }.onFailure { Log.w(TAG, "cannot send command", it) }
    }

    fun find(query: String) = send("q$query")

    fun findNext() = send("n")

    fun findPrevious() = send("p")

    fun clearFind() = send("c")

    fun goToPage(page: Int) = send("g${page.coerceAtLeast(1)}")

    fun setNightMode(on: Boolean) = send(if (on) "i1" else "i0")

    /**
     * 没有独立夜间样式的页面（表格/幻灯片/RTF/EPUB）跟随 App 深色。
     *
     * 不用 WebView 自带的算法暗化：它只看系统/Activity 的 uiMode，而万宝盒的深色是
     * Compose 层的，WebView 会认为 App 仍为浅色。改为在页面里挂 `dark.css` 并打类，
     * 由 CSS 滤镜确定性翻色；图片/画布再翻一次抵消。
     */
    fun applyForceDark(on: Boolean) {
        applyForceDarkTo(webView ?: return, on = on)
    }

    private fun applyForceDarkTo(web: WebView, on: Boolean) {
        val script = "(function(){var on=$on;" +
            // 系统本身是深色时，页面自己的 prefers-color-scheme 已经生效，再翻一次会翻回去
            "var alreadyDark=window.matchMedia&&matchMedia('(prefers-color-scheme: dark)').matches;" +
            "if(on&&alreadyDark)on=false;" +
            "var id='vw-dark-style',link=document.getElementById(id);" +
                        "if(on){if(!link){link=document.createElement('link');link.id=id;link.rel='stylesheet';" +
            "link.href='/assets/viewer/dark.css';document.head.appendChild(link);}" +
            "var sheets=document.querySelectorAll('section');" +
            "for(var i=0;i<sheets.length;i++)sheets[i].classList.add('vw-dark-sheet');" +
            "document.documentElement.classList.add('vw-force-dark');}" +
            "else{var marked=document.querySelectorAll('.vw-dark-sheet');" +
            "for(var i=0;i<marked.length;i++)marked[i].classList.remove('vw-dark-sheet');" +
            "document.documentElement.classList.remove('vw-force-dark');" +
            "if(link&&link.parentNode)link.parentNode.removeChild(link);}})();"
        runCatching { web.evaluateJavascript(script, null) }
            .onFailure { Log.w(TAG, "cannot apply dark page", it) }
    }

    /**
     * 告诉页面顶部被浮层挡掉多少 dp（查找条的高度），页面会用 `--vw-top` 让出这块。
     * 0 表示没有浮层。
     */
    fun setTopInset(dp: Int) = send("t${dp.coerceAtLeast(0)}")

    /**
     * onPageFinished 后开销很小的一次握手：把端口的一端交给页面。
     *
     * 页面在 `find.js` 里 `window.addEventListener("message")` 取 `event.ports[0]`，
     * 之后所有命令都走这条通道；拿不到端口的页面照样渲染，只是没有页码/查找/夜间。
     */
    private fun openPort(web: WebView) {
        if (released || port != null) return
        runCatching {
            val ends = WebViewCompat.createWebMessageChannel(web)
            val local = ends[0]
            val remote = ends[1]
            local.setWebMessageCallback(
                Handler(Looper.getMainLooper()),
                object : WebMessagePortCompat.WebMessageCallbackCompat() {
                    override fun onMessage(port: WebMessagePortCompat, message: WebMessageCompat?) {
                        message?.data?.let(::handleMessage)
                    }
                },
            )
            port = local
            WebViewCompat.postWebMessage(
                web,
                WebMessageCompat("vw-port", arrayOf(remote)),
                Uri.parse("https://$ASSET_HOST"),
            )
        }.onFailure { Log.w(TAG, "cannot open message port", it) }
    }

    private fun handleMessage(raw: String) {
        when {
            raw.startsWith("page ") -> parsePage(raw)?.let { (page, total) ->
                listener.onPage(page, total)
            }

            raw.startsWith("at ") -> listener.onBookPosition(raw)

            SEARCH_COUNT.matches(raw) -> {
                val values = raw.split(' ')
                val at = values[0].toIntOrNull() ?: return
                val total = values[1].toIntOrNull() ?: return
                val done = values[2] == "1"
                listener.onSearchCount(at, total, done)
            }

            // locked / cover / reflow：当前没有 PDF 页，电子书的覆盖层与重排也不做交互
            else -> Unit
        }
    }

    private fun parsePage(raw: String): Pair<Int, Int>? {
        val parts = raw.split(' ')
        if (parts.size < 3) return null
        val page = parts[1].toIntOrNull() ?: return null
        val total = parts[2].toIntOrNull() ?: return null
        if (page < 1 || total < 1) return null
        return page to total
    }

    private fun buildPageUrl(
        request: ViewerRequest,
        context: Context,
        userAgent: String?,
        total: Long,
    ): String {
        val builder = StringBuilder()
            .append("https://").append(ASSET_HOST)
            .append("/assets/viewer/").append(request.page)
            .append("?name=").append(Uri.encode(request.displayName))
            .append("&ext=").append(Uri.encode(request.extension))
        if (total >= 0) builder.append("&length=").append(total)
        if (request.nightMode) builder.append("&night=1")
        // 电子书的阅读位置在加载时给，页面自己跳到那一章再算重排
        request.bookPosition?.let {
            builder.append("&at=").append(Uri.encode(it.removePrefix("at ")))
        }

        val major = chromiumMajor(userAgent)
        val floor = request.webViewFloor
        if (major != null && floor > 0 && major < floor) {
            builder.append("&webview=").append(major).append("&needs=").append(floor)
        }
        if (isLockedWebViewProvider(context)) builder.append("&locked=1")
        return builder.toString()
    }

    /**
     * 解析 `bytes=start-end`。只认单段；越界或写坏一律当没有 Range 处理。
     */
    private fun parseRange(header: String?, total: Long): LongRange? {
        if (header == null || total <= 0) return null
        if (!header.startsWith("bytes=")) return null
        val spec = header.removePrefix("bytes=").substringBefore(',')
        val parts = spec.split('-', limit = 2)
        if (parts.size != 2) return null

        val start = parts[0].trim().toLongOrNull() ?: return null
        val end = parts[1].trim().toLongOrNull() ?: (total - 1)
        if (start < 0 || start >= total) return null
        val last = end.coerceIn(start, total - 1)
        return start..last
    }

    /**
     * 提供 `/doc/file.<ext>`。
     *
     * ≥16MiB 且带 `Range` 时回 206；其余回 200。16MiB 是 gander 在低端机上量出来的
     * 缓冲上限，也是"整读一次更快"与"分段拉省内存"的分界。
     */
    private fun documentResponse(
        uri: Uri,
        mime: String,
        total: Long,
        rangeHeader: String?,
    ): WebResourceResponse {
        return try {
            val rangeable = total >= RANGE_THRESHOLD_BYTES
            val span = if (rangeable) parseRange(rangeHeader, total) else null
            val baseHeaders = mapOf(
                // 页面只能按声明的类型解释这些字节，杜绝"把文档当脚本跑"
                "X-Content-Type-Options" to "nosniff",
                "Cache-Control" to "no-store",
            )

            if (span == null) {
                val stream = source.open(uri) ?: return notFound()
                val headers = baseHeaders.toMutableMap()
                if (rangeable) headers["Accept-Ranges"] = "bytes"
                if (total >= 0) headers["Content-Length"] = total.toString()
                WebResourceResponse(mime, null, 200, "OK", headers, stream)
            } else {
                val stream = source.openRange(uri, span.first, span.last) ?: return notFound()
                WebResourceResponse(
                    mime,
                    null,
                    206,
                    "Partial Content",
                    baseHeaders + mapOf(
                        "Accept-Ranges" to "bytes",
                        "Content-Range" to "bytes ${span.first}-${span.last}/$total",
                        "Content-Length" to (span.last - span.first + 1).toString(),
                    ),
                    stream,
                )
            }
        } catch (t: Throwable) {
            Log.w(TAG, "cannot serve ${uri.lastPathSegment}", t)
            notFound()
        }
    }

    private fun WebResourceResponse.withViewerPolicy(): WebResourceResponse = apply {
        responseHeaders = responseHeaders.orEmpty() + ("Content-Security-Policy" to VIEWER_CSP)
    }

    private fun notFound() = WebResourceResponse(
        "text/plain",
        "utf-8",
        404,
        "Not Found",
        null,
        ByteArrayInputStream(ByteArray(0)),
    )

    private fun isViewerPage(url: Uri): Boolean =
        url.scheme == "https" &&
            url.host == ASSET_HOST &&
            url.path?.startsWith("/assets/viewer/") == true &&
            url.path?.endsWith(".html") == true

    private fun chromiumMajor(userAgent: String?): Int? =
        userAgent?.let { Regex("Chrome/(\\d+)").find(it)?.groupValues?.getOrNull(1)?.toIntOrNull() }

    /**
     * 厂商内置且不可升级的 WebView（华为）。命中时页面不再劝用户"去更新"，
     * 因为那个按钮在他手机上不存在。
     */
    private fun isLockedWebViewProvider(context: Context): Boolean = runCatching {
        WebViewCompat.getCurrentWebViewPackage(context)?.packageName == "com.huawei.webview"
    }.getOrDefault(false)

    private companion object {
        const val TAG = "FileViewer"
        const val ASSET_HOST = "appassets.androidplatform.net"
        const val RANGE_THRESHOLD_BYTES = 16L * 1024 * 1024
        val SEARCH_COUNT = Regex("""^\d+ \d+ [01]$""")

        /**
         * 和页面 `<meta http-equiv="Content-Security-Policy">` 逐字一致。
         *
         * 改这里必须同步改页面，否则从 worker 取策略的库会拿不到同一份。
         */
        const val VIEWER_CSP =
            "default-src 'none'; " +
                "script-src 'self' 'wasm-unsafe-eval'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: blob:; " +
                "font-src 'self' data: blob:; " +
                "media-src 'self' data: blob:; " +
                "connect-src 'self'; " +
                "worker-src 'self'; " +
                "frame-src 'none'; " +
                "object-src 'none'; " +
                "form-action 'none'; " +
                "base-uri 'none'"
    }
}
