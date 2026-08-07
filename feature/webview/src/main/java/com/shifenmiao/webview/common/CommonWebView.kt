package com.shifenmiao.webview.common

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.shifenmiao.webview.R
import com.shifenmiao.common.ui.LoadingOverlay
import com.shifenmiao.model.webview.WebViewParams
import com.shifenmiao.model.webview.WebViewType
import com.shifenmiao.webview.WebViewComponent
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LineError

/**
 * 通用 WebView Compose 容器。
 *
 * **client / chromeClient 必填**：去掉默认值，强制调用方走 [com.shifenmiao.webview.client.CustomWebViewClient]，
 * 避免漏传导致资源拦截静默失效。
 *
 * 使用示例：
 * ```
 * CommonWebView(
 *     webViewParams = params,
 *     webViewState = state,
 *     client = component.getWebViewClient(),
 *     chromeClient = component.getWebChromeClient(),
 * )
 * ```
 */
@Composable
fun CommonWebView(
    webViewParams: WebViewParams?,
    webViewState: WebViewComponent.WebViewState,
    client: WebViewClient,
    chromeClient: WebChromeClient,
    modifier: Modifier = Modifier,
    onTitleChanged: (String) -> Unit = {},
    onWebViewCreated: (WebView) -> Unit = {},
    onReload: () -> Unit = {},
    isLoading: Boolean = false,
    onCancelLoading: () -> Unit = {},
    canLoadingCancel: Boolean = true,
) {
    var pageTitle by remember { mutableStateOf<String?>(null) }

    // 获取Material3主题背景色
    val backgroundColor = MaterialTheme.colorScheme.background.toArgb()

    // 是否需要完整文档绘制（用于截图整个页面）
    val needSlowWholeDocumentDraw = webViewParams?.enableSlowWholeDocumentDraw == true

    // 将页面标题传递给调用方
    LaunchedEffect(pageTitle) {
        pageTitle?.let { onTitleChanged(it) }
    }

    Box(modifier = modifier) {
        // 显示加载进度条
        if (webViewState.isLoading) {
            LinearProgressIndicator(
                progress = { webViewState.loadingProgress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer,
                gapSize = 1.dp
            )
        }

        // 错误状态UI
        if (webViewState.isError) {
            ErrorStateView(
                errorMessage = webViewState.errorMessage,
                errorDetails = webViewState.errorDetails,
                url = webViewParams?.url ?: "",
                onReload = onReload
            )
        } else {
            AndroidView(
                factory = { ctx ->
                    if (needSlowWholeDocumentDraw) {
                        // 需要完整文档绘制时，必须在创建 WebView 之前调用
                        WebView.enableSlowWholeDocumentDraw()
                    }

                    // 每次创建全新 WebView，不复用旧实例
                    WebViewPool.create(ctx, backgroundColor).apply {
                        WebViewSettings.applyCommonSettings(this, context)
                        this.settings.blockNetworkImage = webViewParams?.type == WebViewType.PREVIEW
                        this.webViewClient = client
                        this.webChromeClient = chromeClient
                        onWebViewCreated(this)
                    }
                },
                modifier = Modifier.fillMaxSize().imePadding(),
                update = { view ->
                    // 确保背景颜色与主题一致
                    view.setBackgroundColor(backgroundColor)
                }
            )
        }

        // 显示导出进度
        LoadingOverlay(
            visible = isLoading,
            onCancelLoading = onCancelLoading,
            canCancel = canLoadingCancel,
        )
    }
}

@Composable
private fun ErrorStateView(
    errorMessage: String,
    errorDetails: String,
    url: String,
    onReload: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // 错误图标
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineError,
                contentDescription = stringResource(R.string.browser_error),
                modifier = Modifier
                    .height(72.dp)
                    .width(72.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 主错误信息
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 网址显示
            Text(
                text = stringResource(R.string.browser_url_label, url),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 技术细节区域
            if (errorDetails.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = errorDetails,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 重试按钮
            Button(
                onClick = onReload,
                modifier = Modifier.padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                        contentDescription = stringResource(R.string.browser_reload),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(text = stringResource(R.string.browser_reload))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 建议信息
            Text(
                text = stringResource(R.string.browser_check_network_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
