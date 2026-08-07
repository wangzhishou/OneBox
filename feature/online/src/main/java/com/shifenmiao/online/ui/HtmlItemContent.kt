package com.shifenmiao.online.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.loading.EmptyBox
import com.shifenmiao.core.R
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.online.component.CreateHtmlComponent
import com.shifenmiao.online.component.CreateHtmlUiState
import com.shifenmiao.theme.AppTheme
import com.shifenmiao.webview.common.CommonWebView
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.logger.makeLog
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDownloadForOffline


// ===================== New CreateHtmlComponent based functions =====================
@Composable
fun PreviewHtmlContent(
    createHtmlComponent: CreateHtmlComponent
) {
    val webViewComponent = createHtmlComponent.webViewComponent
    val webViewParams by webViewComponent.webViewParams.collectAsState()
    val webViewState by webViewComponent.webViewState.collectAsState()
    if (webViewParams?.url?.isEmpty() == true && webViewParams?.htmlData?.isEmpty() == true) {
        EmptyBox(
            modifier = Modifier.fillMaxSize()
        )
    } else {
        CommonWebView(
            webViewParams = webViewParams,
            webViewState = webViewState,
            modifier = Modifier.fillMaxSize(),
            onWebViewCreated = { webView ->
                makeLog("PreviewHtmlContent", "onWebViewCreated")
                createHtmlComponent.loadWebViewData()
                webViewComponent.setWebView(webView)
            },
            onTitleChanged = { _ ->

            },
            client = webViewComponent.getWebViewClient(),
            chromeClient = webViewComponent.getWebChromeClient(),
        )
    }
}

@Composable
fun CreateHtmlContent(
    createHtmlComponent: CreateHtmlComponent,
    uiState: CreateHtmlUiState
) {
    val uiState by createHtmlComponent.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.paddingNormal, Alignment.Top)
    ) {
        item {
            HtmlCategorySelection(
                createHtmlComponent = createHtmlComponent,
                uiState = uiState
            )
        }
        item {
            ItemTextField(
                value = uiState.title,
                onValueChange = createHtmlComponent::onTitleChange,
                label = { Text(stringResource(R.string.title_placeholder)) }
            )
        }
        item {
            ItemTextField(
                singleLine = false,
                value = uiState.description,
                onValueChange = createHtmlComponent::onDescriptionChange,
                label = { Text(stringResource(R.string.description_placeholder)) },
                minLines = 1,
                maxLines = 3
            )
        }
        item {
            ItemTextField(
                value = uiState.url,
                onValueChange = createHtmlComponent::onUrlChange,
                label = { Text(stringResource(R.string.url_placeholder)) },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            createHtmlComponent.loadHtmlDataFromUrl(
                                onSuccess = {
                                    AppToastHost.showToast(AppContext.getString(R.string.download_successful))
                                },
                                onFailure = { message ->
                                    AppToastHost.showToast(message)
                                }
                            )
                        },
                        enabled = !uiState.isDownloading
                    ) {
                        if (uiState.isDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDownloadForOffline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
        item {
            ItemTextField(
                value = uiState.data,
                singleLine = false,
                onValueChange = createHtmlComponent::onDataChange,
                label = { Text(stringResource(R.string.input_data_placeholder)) },
                minLines = 10
            )
        }
    }
}
