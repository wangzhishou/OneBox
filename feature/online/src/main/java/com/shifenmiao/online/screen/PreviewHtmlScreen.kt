package com.shifenmiao.online.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.provider.LocalDataDraftHelper
import com.shifenmiao.base.ui.loading.EmptyBox
import com.shifenmiao.base.utils.FileUtils
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.model.ListItemType
import com.shifenmiao.online.component.PreviewHtmlComponent
import com.shifenmiao.theme.AppTheme
import com.shifenmiao.webview.common.CommonWebView
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalIconButton
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.Edit

@Composable
fun PreviewHtmlScreen(
    previewHtmlComponent: PreviewHtmlComponent,
    appComponent: AppComponent
) {
    val uiState by previewHtmlComponent.uiState.collectAsState()
    val webViewComponent = previewHtmlComponent.webViewComponent
    val webViewParams by webViewComponent.webViewParams.collectAsState()
    val webViewState by webViewComponent.webViewState.collectAsState()
    val navigator = LocalUrlNavigator.current
    val dataDraftHelper = LocalDataDraftHelper.current
    val scope = rememberCoroutineScope()

    // 页面显示时刷新数据，确保显示最新的编辑内容
    LaunchedEffect(Unit) {
        previewHtmlComponent.refreshData()
    }

    BaseScreen(
        onGoBack = {
            appComponent.onGoBack()
        },
        navigationIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(8.dp))
                GlassTonalIconButton(
                    modifier = Modifier.size(36.dp),
                    onClick = {
                        appComponent.onGoBack()
                    },
                    colors = AppTheme.colors.filledIconButtonColors().copy(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    glassBorderWidth = 0.dp,
                    style = GlassStyle.Thin
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                        contentDescription = null
                    )
                }
            }

        },
        actions = {
            GlassTonalIconButton(
                modifier = Modifier.size(36.dp),
                onClick = {
                    scope.launch {
                        val localUri = uiState.localUri
                        when {
                            localUri != null -> {
                                navigator.navigate(
                                    Screen.CodeEditor(
                                        initialUri = FileUtils.normalizeEditorUri(localUri),
                                        editTitle = uiState.localName
                                    )
                                )
                            }
                            uiState.itemId != 0 -> {
                                val draftId = dataDraftHelper.createDraft(
                                    draftType = ListItemType.HTML.id,
                                    itemId = uiState.itemId
                                )
                                navigator.navigate(Screen.CreateHtml(draftId = draftId))
                            }
                        }
                    }
                },
                colors = AppTheme.colors.filledIconButtonColors().copy(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                glassBorderWidth = 0.dp,
                style = GlassStyle.Thin
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        },
        showNavigationBarsPadding = false,
        foreground = {
            if (uiState.url.isEmpty() && uiState.data.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyBox(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        text = stringResource(R.string.load_empty_toast),
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        background = {
            CommonWebView(
                webViewParams = webViewParams,
                webViewState = webViewState,
                modifier = Modifier.fillMaxSize(),
                onWebViewCreated = { webView ->
                    previewHtmlComponent.loadWebViewData()
                    webViewComponent.setWebView(webView)
                },
                onTitleChanged = { _ -> },
                client = webViewComponent.getWebViewClient(),
                chromeClient = webViewComponent.getWebChromeClient(),
            )
        }
    )

    BackHandler {
        appComponent.onGoBack()
    }
}

