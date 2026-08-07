package com.halilibo.richtext.ui.mermaid

import android.graphics.Bitmap
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import java.io.File

/**
 * Mermaid 图表渲染提供者接口
 *
 * 通过 [LocalMermaidRenderer] CompositionLocal 注入，
 * 解耦 `:libs:richtext` 与 `:feature:webview` 之间的依赖关系。
 *
 * 使用方式：
 * ```kotlin
 * CompositionLocalProvider(
 *     LocalMermaidRenderer provides MermaidRendererImpl()
 * ) {
 *     // 内部 MermaidCodeBlock 将自动使用 MermaidRendererImpl
 * }
 * ```
 */
interface MermaidRenderer {

    /**
     * 渲染 Mermaid 图表的 Composable
     *
     * @param code          Mermaid 源码
     * @param modifier      修饰符
     * @param onWebViewReady WebView 就绪回调，用于截图
     */
    @Composable
    fun RenderDiagram(
        code: String,
        modifier: Modifier,
        onWebViewReady: ((WebView) -> Unit)?
    )

    /**
     * 对 WebView 进行截图（已弃用，保留接口兼容）
     *
     * @param webView 目标 WebView
     * @return Bitmap 截图，失败返回 null
     */
    fun captureBitmap(webView: WebView?): Bitmap?

    /**
     * 从缓存获取已渲染的 Mermaid 图表 Bitmap
     *
     * 当 WebView 已被销毁时，可通过此方法直接获取缓存的渲染结果，
     * 用于保存 / 全屏查看。
     *
     * 实现方从 SVG 缓存解码为 Bitmap 返回。
     *
     * @param code Mermaid 源码
     * @return 缓存的 Bitmap，未命中返回 null
     */
    suspend fun getCachedBitmap(code: String): Bitmap? = null

    /**
     * 从缓存获取 SVG 字符串
     *
     * 优先使用此方法获取 SVG 文本，比 Bitmap 更轻量、更清晰。
     *
     * @param code Mermaid 源码
     * @return 缓存的 SVG 字符串，未命中返回 null
     */
    fun getCachedSvg(code: String): String? = null

    /**
     * 从缓存获取 SVG 文件
     *
     * 推荐用于保存 / 全屏查看等场景 —— 直接使用 `file.toURI()` 传给
     * ImageViewer 或 Coil，跳过 SVG→Bitmap 转换。
     *
     * @param code Mermaid 源码
     * @return 缓存的 SVG 文件，未命中返回 null
     */
    fun getCachedSvgFile(code: String): File? = null
}

/**
 * CompositionLocal for Mermaid rendering.
 *
 * 默认值为 null — 当未提供时，MermaidCodeBlock 会回退到纯代码显示。
 */
val LocalMermaidRenderer = compositionLocalOf<MermaidRenderer?> { null }
