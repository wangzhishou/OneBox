package com.shifenmiao.webview.mermaid

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.annotation.Keep
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import coil3.memory.MemoryCache
import coil3.request.ImageRequest
import coil3.svg.SvgDecoder
import com.shifenmiao.webview.common.WebViewPool
import com.shifenmiao.webview.common.WebViewSettings
import com.shifenmiao.webview.di.WebViewEntryPoint
import com.shifenmiao.webview.resource.WebResourceEngine
import com.t8rin.imagetoolbox.core.ui.widget.modifier.shimmer
import dagger.hilt.android.EntryPointAccessors
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "MermaidDiagram"

/**
 * Mermaid 图表 Compose 组件（SVG 缓存 + Shimmer 骨架屏）
 *
 * 渲染策略：
 * 1. **缓存命中** → 直接用 `AsyncImage(cachedSvg)` 展示，跳过 WebView，零延迟。
 * 2. **缓存未命中** → 显示 shimmer 骨架屏 + WebView 渲染；
 *    JS 提取 SVG 字符串后通过 Bridge 传给 Native，缓存到 MMKV，
 *    切换到 AsyncImage 展示并销毁 WebView。
 *
 * 资源拦截统一委托给 [WebResourceEngine]（由 Hilt EntryPoint 获取）；
 * 不再持有本地的 [androidx.webkit.WebViewAssetLoader]，所有 `appassets.androidplatform.net` 路径
 * 由引擎兜底（`/assets/`, `/js/`, `/res/`）。
 *
 * @param code           Mermaid 源码
 * @param modifier       修饰符
 * @param onWebViewReady WebView 就绪回调，外部可用于截图等操作
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MermaidDiagramView(
    code: String,
    modifier: Modifier = Modifier,
    onWebViewReady: ((WebView) -> Unit)? = null,
) {
    val isDark = isSystemInDarkTheme()
    val colorScheme = MaterialTheme.colorScheme
    val surfaceColor = colorScheme.surface.toArgb()
    val defaultMinHeight = 120.dp

    // ── 优先查缓存 ──
    val initialCache = remember(code, isDark) { MermaidBitmapCache.get(code, isDark) }
    var cachedFile by remember(code, isDark) { mutableStateOf(initialCache?.svgFile) }

    // 使用缓存高度作为初始高度（JS getBoundingClientRect CSS px ≈ Android dp），
    // 避免从 120dp 动画展开到实际高度
    val cachedHeightDp = remember(initialCache?.heightPx) {
        initialCache?.heightPx?.takeIf { it > 0 }?.dp
    }

    if (cachedFile != null) {
        // ══════════════════════════════════════════════════
        //  路径 A：缓存命中 → Coil AsyncImage 从文件渲染 SVG
        //  使用缓存高度固定初始尺寸，消除滚动回来时的展开动画
        // ══════════════════════════════════════════════════
        Log.d(TAG, "CACHE HIT: hash=${code.hashCode()}, isDark=$isDark, h=${cachedHeightDp}")
        val context = LocalContext.current
        AsyncImage(
            model = remember(cachedFile) {
                ImageRequest.Builder(context)
                    .data(cachedFile!!)
                    .decoderFactory(SvgDecoder.Factory())
                    // 设置内存缓存 key，滚动回来时直接从内存读取，无需重新解码
                    .memoryCacheKey(MemoryCache.Key("mermaid_${code.hashCode()}_$isDark"))
                    .build()
            },
            contentDescription = "Mermaid Diagram",
            modifier = modifier
                .fillMaxWidth()
                .then(
                    if (cachedHeightDp != null) {
                        // 有缓存高度 → 固定高度，Coil 加载完后 wrapContentHeight 自动接管
                        Modifier.heightIn(min = cachedHeightDp)
                    } else {
                        Modifier.heightIn(min = defaultMinHeight)
                    }
                ),
            contentScale = ContentScale.FillWidth,
            alignment = Alignment.TopCenter
        )
    } else {
        // ══════════════════════════════════════════════════
        //  路径 B：缓存未命中 → Shimmer + WebView 渲染
        // ══════════════════════════════════════════════════
        Log.d(TAG, "CACHE MISS: hash=${code.hashCode()}, isDark=$isDark")

        var isRendering by remember { mutableStateOf(true) }
        var webViewRef by remember { mutableStateOf<WebView?>(null) }
        val destroyed = remember { AtomicBoolean(false) }

        // 通过 Hilt EntryPoint 获取单例 WebResourceEngine
        val context = LocalContext.current
        val engine: WebResourceEngine = remember(context) {
            EntryPointAccessors.fromApplication(
                context = context.applicationContext,
                entryPoint = WebViewEntryPoint::class.java,
            ).webResourceEngine()
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = defaultMinHeight),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    val wv = WebViewPool.create(ctx, surfaceColor).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        setBackgroundColor(surfaceColor)
                        WebViewSettings.applyCommonSettings(this, ctx)
                        isLongClickable = false
                        setOnLongClickListener { true }
                        isHapticFeedbackEnabled = false
                        // 禁用滚动条，防止闪烁
                        isVerticalScrollBarEnabled = false
                        isHorizontalScrollBarEnabled = false
                    }

                    val bridge = MermaidBridge(
                        onSvgReady = { svgString, height ->
                            // 缓存 SVG 到文件，触发重组切换到路径 A
                            val file = MermaidBitmapCache.put(code, isDark, svgString, height)
                            if (file != null) cachedFile = file
                            isRendering = false
                            // 立即销毁 WebView，无需等待
                            if (destroyed.compareAndSet(false, true)) {
                                webViewRef = null
                                WebViewPool.destroyWebView(wv, "Android")
                            }
                        },
                        onRendered = { height ->
                            // 回退路径：SVG 提取失败时仅报告高度（不截图）
                            Log.d(TAG, "JS onRendered (fallback): h=$height")
                        },
                        onError = { msg ->
                            Log.e(TAG, "JS onError: $msg")
                            isRendering = false
                        }
                    )
                    wv.addJavascriptInterface(bridge, "Android")

                    // 资源拦截统一走 WebResourceEngine（覆盖 /assets/, /js/, /res/, local-file, 远程规则等）
                    wv.webViewClient = object : android.webkit.WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView,
                            request: android.webkit.WebResourceRequest,
                        ) = engine.intercept(request)
                    }

                    val html = MermaidHtmlTemplate.generate(
                        code = code,
                        isDark = isDark,
                        colors = colorScheme
                    )
                    wv.loadDataWithBaseURL(
                        "https://appassets.androidplatform.net/",
                        html,
                        "text/html",
                        "UTF-8",
                        null
                    )

                    webViewRef = wv
                    onWebViewReady?.invoke(wv)
                    wv
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = defaultMinHeight)
                    .shimmer(visible = isRendering)
            )
        }

        // 组件销毁时安全销毁 WebView
        DisposableEffect(Unit) {
            onDispose {
                val wv = webViewRef ?: return@onDispose
                Log.d(TAG, "onDispose: cached=${cachedFile != null}, destroyed=${destroyed.get()}")
                if (destroyed.compareAndSet(false, true)) {
                    webViewRef = null
                    WebViewPool.destroyWebView(wv, "Android")
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  JS Bridge
// ══════════════════════════════════════════════════════════════

/**
 * Mermaid WebView JavaScript Bridge
 *
 * - [onSvgReady]:   主路径 — JS 提取 SVG 字符串后回调，Native 缓存 SVG 文本
 * - [onRendered]:   回退路径 — SVG 提取失败时仅报告高度
 * - [onError]:      渲染失败
 */
@Keep
class MermaidBridge(
    private val onSvgReady: (String, Int) -> Unit,
    private val onRendered: (Int) -> Unit,
    private val onError: (String) -> Unit
) {
    @JavascriptInterface
    fun onSvgReady(svgString: String, height: Int) {
        onSvgReady.invoke(svgString, height)
    }

    @JavascriptInterface
    fun onBitmapReady(base64DataUrl: String, height: Int) {
        // 兼容旧版 JS 模版：不再使用 base64，尝试走 SVG 路径
        onRendered.invoke(height)
    }

    @JavascriptInterface
    fun onRendered(height: Int) {
        onRendered.invoke(height)
    }

    @JavascriptInterface
    fun onError(message: String) {
        onError.invoke(message)
    }
}
