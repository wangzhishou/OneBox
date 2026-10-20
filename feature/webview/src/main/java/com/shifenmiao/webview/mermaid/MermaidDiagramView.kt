package com.shifenmiao.webview.mermaid

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.annotation.Keep
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import coil3.memory.MemoryCache
import coil3.request.ImageRequest
import coil3.svg.SvgDecoder
import com.shifenmiao.webview.common.WebViewPool
import com.shifenmiao.webview.common.WebViewSettings
import com.shifenmiao.webview.diagnostics.WebViewDiagnostics
import com.shifenmiao.webview.di.WebViewEntryPoint
import com.shifenmiao.webview.resource.WebResourceEngine
import com.wanbaohe.core.ui.widget.modifier.shimmer
import dagger.hilt.android.EntryPointAccessors
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.delay

private const val TAG = "MermaidDiagram"

/**
 * mermaid 源码稳定多久之后才认为它是最终内容（毫秒）。
 *
 * AI 回答是流式输出的：每来一个 token，```mermaid 代码块的内容都会变。若直接拿 code 当渲染 key，
 * 每变一次就会走一遍「新建 WebView → 重新加载 2.5 MB mermaid.min.js → 渲染半张图 → 写进缓存」，
 * 一次回答能连续建好几个 WebView，既掉帧又会把不完整的图缓存下来。
 *
 * 首屏（内容已定型、code 不再变化）不受影响：stableCode 初值就是首个 code，
 * LaunchedEffect 第一次进来发现没有变化会直接返回，不做任何等待。
 */
private const val CODE_SETTLE_DEBOUNCE_MS = 400L

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
    val context = LocalContext.current

    // 内核跑不动 mermaid 时不要建 WebView。
    // mermaid.min.js 用了 ES2020（可选链/空值合并），Chromium < 80 的内核会在**解析阶段**就抛
    // SyntaxError，window.mermaid 永远是 undefined。而模板的失败路径会把源码容器
    // （.mermaid-container）隐藏、只留一句 "Mermaid 渲染失败: mermaid library not loaded"，
    // 用户既看不到图也看不到源码。这里直接原生展示源码，比走那条路更有用，也省掉 2.5 MB 白读。
    if (!remember(context) { WebViewDiagnostics.supportsEs2020(context) }) {
        MermaidSourceFallback(code = code, modifier = modifier)
        return
    }

    // 流式输出时 code 每个 token 都会变，先把内容稳定下来再拿去渲染（见 CODE_SETTLE_DEBOUNCE_MS）。
    // 下面所有缓存查询、HTML 生成、缓存写入一律用 stableCode，不再直接用 code。
    var stableCode by remember { mutableStateOf(code) }
    LaunchedEffect(code) {
        if (code == stableCode) return@LaunchedEffect
        delay(CODE_SETTLE_DEBOUNCE_MS)
        stableCode = code
    }

    // ── 优先查缓存 ──
    val initialCache = remember(stableCode, isDark) { MermaidBitmapCache.get(stableCode, isDark) }
    var cachedFile by remember(stableCode, isDark) { mutableStateOf(initialCache?.svgFile) }

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
        Log.d(TAG, "CACHE HIT: hash=${stableCode.hashCode()}, isDark=$isDark, h=${cachedHeightDp}")
        AsyncImage(
            model = remember(cachedFile) {
                ImageRequest.Builder(context)
                    .data(cachedFile!!)
                    .decoderFactory(SvgDecoder.Factory())
                    // 设置内存缓存 key，滚动回来时直接从内存读取，无需重新解码
                    .memoryCacheKey(MemoryCache.Key("mermaid_${stableCode.hashCode()}_$isDark"))
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
        Log.d(TAG, "CACHE MISS: hash=${stableCode.hashCode()}, isDark=$isDark")

        var isRendering by remember { mutableStateOf(true) }
        var webViewRef by remember { mutableStateOf<WebView?>(null) }
        val destroyed = remember { AtomicBoolean(false) }

        // 通过 Hilt EntryPoint 获取单例 WebResourceEngine
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
                    // 快照本次实际渲染的内容与主题。
                    // bridge 回调是异步的，那里再去读 stableCode 会读到"更新后的值"——
                    // 上一张图渲染完成时可能已经把内容换掉了，于是旧图的 SVG 会被写进新 code 的缓存键。
                    val renderCode = stableCode
                    val renderDark = isDark

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
                            // 本回调跑在 WebView 的 JavaBridge 线程，不是主线程。
                            // 写文件/MMKV 留在当前线程（是 IO），涉及 Compose state 与 View 的操作
                            // 全部 post 回主线程：destroyWebView 内部会做 onPause / setLayerType /
                            // loadUrl / removeView，非主线程调用属未定义行为。
                            val file = MermaidBitmapCache.put(renderCode, renderDark, svgString, height)
                            wv.post {
                                // 等待期间内容可能又稳定成了新的（stableCode 读到的是当前值）。
                                // 这份结果对应的是 renderCode，过期就只跳过写缓存——否则旧图会被写进
                                // 新 code 的缓存键，后续命中就是错的图。清理动作照常走完，避免 WebView 泄漏。
                                if (renderCode == stableCode && file != null) cachedFile = file
                                isRendering = false
                                // 立即销毁 WebView，无需等待
                                if (destroyed.compareAndSet(false, true)) {
                                    webViewRef = null
                                    WebViewPool.destroyWebView(wv, "Android")
                                }
                            }
                        },
                        onRendered = { height ->
                            // 回退路径：SVG 提取失败时仅报告高度（不截图）
                            Log.d(TAG, "JS onRendered (fallback): h=$height")
                        },
                        onError = { msg ->
                            Log.e(TAG, "JS onError: $msg")
                            wv.post { isRendering = false }
                        }
                    )
                    wv.addJavascriptInterface(bridge, "Android")

                    // 资源拦截统一走 WebResourceEngine（覆盖 /assets/, /js/, /res/, local-file, 远程规则等）
                    wv.webViewClient = object : android.webkit.WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView,
                            request: android.webkit.WebResourceRequest,
                        ) = engine.intercept(request)

                        override fun onRenderProcessGone(
                            view: WebView?,
                            detail: android.webkit.RenderProcessGoneDetail?,
                        ): Boolean {
                            // 不接管这个回调的话，渲染进程被回收会直接把 App 带走。
                            // 图表本来就有一条 onError 路径：停掉 shimmer 即可，页面会显示 mermaid 源码。
                            Log.w(TAG, "render process gone, stop shimmer")
                            wv.post { isRendering = false }
                            return true
                        }
                    }

                    val html = MermaidHtmlTemplate.generate(
                        code = renderCode,
                        isDark = renderDark,
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

/**
 * 内核不支持 mermaid（Chromium < 80，见 [WebViewDiagnostics.MIN_CHROMIUM_MAJOR_FOR_MERMAID]）时的兜底。
 *
 * 直接展示 mermaid 源码。做成等宽字体代码块而不是走 WebView：
 * 后者会在解析 2.5 MB 脚本时抛 SyntaxError，然后把源码容器隐藏掉只留一句错误提示，
 * 用户既看不到图也看不到源码。
 *
 * 代码块属于"内容渲染"，按项目规范不走玻璃管线，用中性的 surfaceContainer 做底。
 */
@Composable
private fun MermaidSourceFallback(
    code: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(12.dp)
    ) {
        Text(
            text = code.trim(),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
