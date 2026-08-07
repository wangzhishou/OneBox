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
import androidx.compose.ui.platform.LocalContext
import com.shifenmiao.webview.common.CommonWebView
import com.shifenmiao.webview.common.WebViewTopAppBarWithExport
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFileCreator
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalComponentActivity

@Composable
fun PreviewWebViewScreen(
    onGoBack: () -> Unit,
    webViewComponent: WebViewComponent
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val webViewState by webViewComponent.webViewState.collectAsState()
    val webViewParams by webViewComponent.webViewParams.collectAsState()
    val localActivity = LocalComponentActivity.current

    val savePngLauncher = rememberFileCreator(
        mimeType = MimeType.StaticPng,
        onSuccess = { uri ->
            webViewComponent.savePngFile(
                fileUri = uri,
                onResult = webViewComponent::parseFileSaveResult
            )
        }
    )
    val savePdfLauncher = rememberFileCreator(
        mimeType = MimeType.Pdf,
        onSuccess = { uri ->
            webViewComponent.savePdfFile(
                fileUri = uri,
                onResult = webViewComponent::parseFileSaveResult
            )
        }
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        WebViewTopAppBarWithExport(
            title = webViewState.currentTitle.ifEmpty { webViewParams?.title ?: "" },
            onBackClick = onGoBack,
            scrollBehavior = scrollBehavior,
            showExportOptions = webViewParams?.enableSlowWholeDocumentDraw == true,
            onShareImage = {
                webViewComponent.share()
            },
            onExportPng = {
                savePngLauncher.make(webViewComponent.getExportFileName() + ".png")
            },
            onPrintPdf = {
                webViewComponent.showPdfExportDialog(localActivity)
            },
            onExportImagePdf = {
                savePdfLauncher.make(webViewComponent.getExportFileName() + ".pdf")
            },
            onCopyContent = {
                webViewComponent.copyWebContent(context, copyToClipboard = { str ->
                    Clipboard.copy(str)
                })
            },
            onOpenInBrowser = {
                webViewComponent.openInBrowser()
            }
        )

        CommonWebView(
            webViewParams = webViewParams,
            webViewState = webViewState,
            modifier = Modifier.fillMaxSize(),
            onWebViewCreated = { webView ->
                webViewComponent.setWebView(webView)
            },
            onTitleChanged = { newTitle -> webViewComponent.updateTitle(newTitle) },
            client = webViewComponent.getWebViewClient(),
            chromeClient = webViewComponent.getWebChromeClient(),
            onReload = { webViewComponent.reload() },
            isLoading = webViewComponent.isSaving,
            onCancelLoading = {
                webViewComponent.cancelSaving()
            },
            canLoadingCancel = true,
        )
    }

    BackHandler {
        if (webViewState.canGoBack) {
            webViewComponent.goBack()
        } else {
            onGoBack()
        }
    }
}
