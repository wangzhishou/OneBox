package com.shifenmiao.webview.mermaid

import android.graphics.Bitmap
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.halilibo.richtext.ui.mermaid.MermaidRenderer
import com.shifenmiao.interfaces.singleton.AppContext
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.size.Size
import coil3.svg.SvgDecoder
import coil3.toBitmap
import java.io.File

/**
 * [MermaidRenderer] 的实际实现，桥接到 [MermaidDiagramView] 和 SVG 文件缓存。
 *
 * 在宿主层（如 feature:ai 或 feature:app）通过 [com.halilibo.richtext.ui.mermaid.LocalMermaidRenderer]
 * 注入到 Compose 树中：
 *
 * ```kotlin
 * CompositionLocalProvider(
 *     LocalMermaidRenderer provides MermaidRendererImpl()
 * ) {
 *     // 内部的 MermaidCodeBlock 自动使用 WebView 渲染
 * }
 * ```
 */
class MermaidRendererImpl : MermaidRenderer {

    @Composable
    override fun RenderDiagram(
        code: String,
        modifier: Modifier,
        onWebViewReady: ((WebView) -> Unit)?
    ) {
        MermaidDiagramView(
            code = code,
            modifier = modifier,
            onWebViewReady = onWebViewReady
        )
    }

    override fun captureBitmap(webView: WebView?): Bitmap? {
        // SVG 模式下不再需要截图，返回 null
        return null
    }

    override suspend fun getCachedBitmap(code: String): Bitmap? {
        // 从 SVG 文件解码为 Bitmap（用于保存/全屏等需要 Bitmap 的场景）
        val file = getCachedSvgFile(code) ?: return null
        return try {
            val context = AppContext.getContext()
            val imageLoader = ImageLoader.Builder(context)
                .components { add(SvgDecoder.Factory()) }
                .build()

            val request = ImageRequest.Builder(context)
                .data(file)
                .size(Size.ORIGINAL)
                .allowHardware(false)
                .build()

            val result = imageLoader.execute(request)
            result.image?.toBitmap()
        } catch (_: Exception) {
            null
        }
    }

    override fun getCachedSvg(code: String): String? {
        // 尝试两种主题，优先 dark（运行时无法确定当前主题，两种都试）
        return MermaidBitmapCache.get(code, isDark = true)?.svgString
            ?: MermaidBitmapCache.get(code, isDark = false)?.svgString
    }

    override fun getCachedSvgFile(code: String): File? {
        // 尝试两种主题，优先 dark
        return MermaidBitmapCache.get(code, isDark = true)?.svgFile
            ?: MermaidBitmapCache.get(code, isDark = false)?.svgFile
    }
}
