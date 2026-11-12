/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
import com.wanbaohe.core.ui.utils.content_pickers.rememberFileCreator
import com.wanbaohe.core.ui.utils.helper.Clipboard
import com.wanbaohe.core.ui.utils.provider.LocalComponentActivity

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
