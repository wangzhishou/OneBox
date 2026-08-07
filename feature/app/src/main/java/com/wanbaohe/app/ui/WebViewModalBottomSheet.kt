package com.wanbaohe.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.DisableContainer
import com.shifenmiao.base.utils.CoreUtils
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.core.R
import com.shifenmiao.webview.WebViewComponent
import com.shifenmiao.webview.common.CommonWebView
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFileCreator
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalComponentActivity
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.modalsheet.FullscreenPopup
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePdf
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowForwardIos
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePrint

private const val SheetAnimationDurationMs = 300

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewModalBottomSheet(
    appComponent: AppComponent,
    webViewComponent: WebViewComponent,
) {
    val webViewState by webViewComponent.webViewState.collectAsState()
    val webViewParams by webViewComponent.webViewParams.collectAsState()
    val localActivity = LocalComponentActivity.current

    var isExporting by remember { mutableStateOf(false) }
    // 使用 rememberSaveable 保存临时文件路径，防止选择器期间进程被杀后回来丢失已准备好的文件。
    var pendingTempUri by rememberSaveable { mutableStateOf<String?>(null) }
    var isVisible by remember { mutableStateOf(true) }

    val dismissWithAnimation = remember {
        {
            isVisible = false
        }
    }

    val destroyWebView = remember {
        fun() {
            if (isExporting) return
            dismissWithAnimation()
        }
    }

    LaunchedEffect(isVisible) {
        if (!isVisible) {
            delay(SheetAnimationDurationMs.toLong().milliseconds)
            appComponent.hideWebView()
            webViewComponent.clearWebView()
        }
    }

    val savePngLauncher = rememberFileCreator(
        mimeType = MimeType.StaticPng,
        onFailure = {
            pendingTempUri = null
            isExporting = false
        },
        onSuccess = { uri ->
            val tempUri = pendingTempUri
            pendingTempUri = null
            if (tempUri != null) {
                webViewComponent.copyTempFileToUri(
                    tempUri = tempUri,
                    targetUri = uri,
                    onResult = {
                        isExporting = false
                        webViewComponent.parseFileSaveResult(it)
                    }
                )
            } else {
                isExporting = false
            }
        }
    )

    val savePdfLauncher = rememberFileCreator(
        mimeType = MimeType.Pdf,
        onFailure = {
            pendingTempUri = null
            isExporting = false
        },
        onSuccess = { uri ->
            val tempUri = pendingTempUri
            pendingTempUri = null
            if (tempUri != null) {
                webViewComponent.copyTempFileToUri(
                    tempUri = tempUri,
                    targetUri = uri,
                    onResult = {
                        isExporting = false
                        webViewComponent.parseFileSaveResult(it)
                    }
                )
            } else {
                isExporting = false
            }
        }
    )

    FullscreenPopup(
        onDismiss = { destroyWebView() }
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(SheetAnimationDurationMs)) +
                    slideInVertically(tween(SheetAnimationDurationMs)) { it },
            exit = fadeOut(tween(SheetAnimationDurationMs)) +
                    slideOutVertically(tween(SheetAnimationDurationMs)) { it },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding().navigationBarsPadding(),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    CenterAlignedTopAppBar(
                        title = {},
                        navigationIcon = {
                            if (webViewState.canGoBack) {
                                IconButton(onClick = {
                                    webViewComponent.getCurrentWebView()?.goBack()
                                }) {
                                    Icon(
                                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                                        contentDescription = stringResource(R.string.webview_go_back),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        actions = {
                            if (webViewState.canGoForward) {
                                IconButton(onClick = {
                                    webViewComponent.getCurrentWebView()?.goForward()
                                }) {
                                    Icon(
                                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowForwardIos,
                                        contentDescription = stringResource(R.string.webview_go_forward),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = { destroyWebView() }) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                                    contentDescription = "close",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                    CommonWebView(
                        webViewParams = webViewParams,
                        webViewState = webViewState,
                        onWebViewCreated = { webView ->
                            webViewComponent.setWebView(webView)
                        },
                        modifier = Modifier.weight(1f),
                        onTitleChanged = { newTitle ->
                            webViewComponent.updateTitle(newTitle)
                        },
                        client = webViewComponent.getWebViewClient(),
                        chromeClient = webViewComponent.getWebChromeClient(),
                        onReload = { webViewComponent.reload() },
                        isLoading = webViewComponent.isSaving,
                        onCancelLoading = {
                            webViewComponent.cancelSaving()
                        },
                        canLoadingCancel = true,
                    )
                    if (webViewParams?.enableShare == true) {
                        DisableContainer(enabled = !webViewState.isLoading && !isExporting) {
                            Box(
                                modifier = Modifier
                                    .glassBackground(
                                        style = GlassStyle.Regular,
                                        shape = RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp
                                        ),
                                        color = MaterialTheme.colorScheme.surface,
                                        borderWidth = 0.dp,
                                    )
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                contentAlignment = Alignment.CenterEnd,
                            ) {
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                                ) {
                                    if (!CoreUtils.isHuawei()) {
                                    GlassTonalButton(
                                        onClick = {
                                            isExporting = true
                                            webViewComponent.shareHtml { isExporting = false }
                                        },
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                    ) {
                                        Icon(
                                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShare,
                                            contentDescription = "Share",
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Spacer(Modifier.size(4.dp))
                                        Text(
                                            text = stringResource(R.string.share_image),
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                }
                                GlassTonalButton(
                                    onClick = {
                                        isExporting = true
                                        webViewComponent.preparePdfToTempFile { tempUri ->
                                            if (tempUri != null) {
                                                pendingTempUri = tempUri
                                                savePdfLauncher.make(webViewComponent.getExportFileName() + ".pdf")
                                            } else {
                                                isExporting = false
                                            }
                                        }
                                    },
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                ) {
                                    Icon(
                                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePdf,
                                        contentDescription = "Pdf",
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(Modifier.size(4.dp))
                                    Text(
                                        text = stringResource(R.string.button_export_pdf),
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                                GlassTonalButton(
                                    onClick = {
                                        isExporting = true
                                        webViewComponent.showPdfExportDialog(localActivity)
                                        isExporting = false
                                    },
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                ) {
                                    Icon(
                                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePrint,
                                        contentDescription = "Print",
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(Modifier.size(4.dp))
                                    Text(
                                        text = stringResource(R.string.button_print),
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                                GlassTonalButton(
                                    onClick = {
                                        isExporting = true
                                        webViewComponent.capturePngToTempFile { tempUri ->
                                            if (tempUri != null) {
                                                pendingTempUri = tempUri
                                                savePngLauncher.make(webViewComponent.getExportFileName() + ".png")
                                            } else {
                                                isExporting = false
                                            }
                                        }
                                    },
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                ) {
                                    Icon(
                                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage,
                                        contentDescription = "Image",
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(Modifier.size(4.dp))
                                    Text(
                                        text = stringResource(R.string.button_export_image),
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                            }
                            }
                        }
                    }
                }
            }
        }
    }

    BackHandler {
        if (isExporting) return@BackHandler
        if (webViewState.canGoBack) {
            webViewComponent.getCurrentWebView()?.goBack()
        } else {
            destroyWebView()
        }
    }
}
