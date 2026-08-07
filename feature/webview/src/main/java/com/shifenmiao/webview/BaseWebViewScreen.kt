package com.shifenmiao.webview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.model.webview.WebViewType

@Composable
fun BaseWebViewScreen(
    webViewComponent: WebViewComponent
) {
    val webViewParams by webViewComponent.webViewParams.collectAsState()
    if (webViewParams != null) {
        when (webViewParams!!.type) {
            WebViewType.DEFAULT -> {
                ColumnWebViewScreen(
                    onGoBack = webViewComponent.onGoBack,
                    webViewComponent = webViewComponent
                )
            }

            WebViewType.EXTERNAL -> {
                webViewParams?.url?.let {
                    // 处理外部WebView，使用系统默认浏览器打开
                    val context = LocalContext.current
                    ActionUtils.openWebBrowser(context, it)
                }
            }

            WebViewType.PREVIEW -> {
                PreviewWebViewScreen(
                    onGoBack = webViewComponent.onGoBack,
                    webViewComponent = webViewComponent
                )
            }

            WebViewType.MARKDOWN_RENDER -> {
                PreviewWebViewScreen(
                    onGoBack = webViewComponent.onGoBack,
                    webViewComponent = webViewComponent
                )
            }


            WebViewType.COLUMN -> {
                ColumnWebViewScreen(
                    onGoBack = webViewComponent.onGoBack,
                    webViewComponent = webViewComponent
                )
            }
        }
    }
}
