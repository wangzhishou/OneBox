package com.shifenmiao.webview.mermaid

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.halilibo.richtext.ui.mermaid.LocalMermaidRenderer
import com.halilibo.richtext.ui.mermaid.MermaidRenderer
import java.util.concurrent.atomic.AtomicReference

/**
 * 在真正需要 Markdown / Mermaid 渲染的子树中局部提供 MermaidRenderer。
 *
 * 这里仍保留懒初始化：只有首次触发 Mermaid 相关调用时，才真正创建 [MermaidRendererImpl]。
 */
@Composable
fun ProvideMermaidRenderer(
    content: @Composable () -> Unit
) {
    val renderer = rememberMermaidRenderer()
    CompositionLocalProvider(
        LocalMermaidRenderer provides renderer,
        content = content
    )
}

@Composable
fun rememberMermaidRenderer(): MermaidRenderer = remember {
    LazyMermaidRenderer { MermaidRendererImpl() }
}

private class LazyMermaidRenderer(
    private val factory: () -> MermaidRenderer
) : MermaidRenderer {

    private val delegateRef = AtomicReference<MermaidRenderer?>(null)

    private fun delegate(): MermaidRenderer {
        delegateRef.get()?.let { return it }

        val created = factory()
        return if (delegateRef.compareAndSet(null, created)) {
            created
        } else {
            delegateRef.get() ?: created
        }
    }

    @Composable
    override fun RenderDiagram(
        code: String,
        modifier: Modifier,
        onWebViewReady: ((WebView) -> Unit)?
    ) {
        delegate().RenderDiagram(
            code = code,
            modifier = modifier,
            onWebViewReady = onWebViewReady
        )
    }

    override fun captureBitmap(webView: WebView?): android.graphics.Bitmap? =
        delegate().captureBitmap(webView)

    override suspend fun getCachedBitmap(code: String) = delegate().getCachedBitmap(code)

    override fun getCachedSvg(code: String) = delegate().getCachedSvg(code)

    override fun getCachedSvgFile(code: String) = delegate().getCachedSvgFile(code)
}

