package com.shifenmiao.webview

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.shifenmiao.webview.common.CommonWebView
import com.shifenmiao.webview.common.WebViewTopAppBar

@Composable
fun ColumnWebViewScreen(
    onGoBack: () -> Unit,
    webViewComponent: WebViewComponent
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val webViewParams by webViewComponent.webViewParams.collectAsState()
    val webViewState by webViewComponent.webViewState.collectAsState()

    Column(
        Modifier
            .fillMaxSize().navigationBarsPadding()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        WebViewTopAppBar(
            title = webViewParams?.title ?: "",
            onBackClick = onGoBack,
            scrollBehavior = scrollBehavior
        )

        CommonWebView(
            webViewParams = webViewParams,
            webViewState = webViewState,
            modifier = Modifier.fillMaxSize(),
            onWebViewCreated = { webView -> webViewComponent.setWebView(webView) },
            onTitleChanged = { newTitle -> webViewComponent.updateTitle(newTitle) },
            client = webViewComponent.getWebViewClient(),
            chromeClient = webViewComponent.getWebChromeClient(),
            onReload = { webViewComponent.reload() }
        )
    }

    BackHandler {
        onGoBack()
    }
}
