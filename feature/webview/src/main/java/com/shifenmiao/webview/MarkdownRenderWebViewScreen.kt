package com.shifenmiao.webview

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.shifenmiao.base.utils.FileUtils
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.model.webview.WebViewParams
import com.shifenmiao.model.webview.WebViewType
import com.shifenmiao.webview.common.CommonWebView
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalComponentActivity
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.LoadingDialog
import io.noties.markwon.utils.MarkdownStringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePrint

@Composable
fun MarkdownRenderWebViewScreen(
    onGoBack: () -> Unit,
    webViewComponent: WebViewComponent,
    initialUri: Uri?
) {
    val context = LocalContext.current
    val localUrlNavigator = LocalUrlNavigator.current

    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val localActivity = LocalComponentActivity.current

    val webViewState by webViewComponent.webViewState.collectAsState()
    val webViewParams by webViewComponent.webViewParams.collectAsState()

    val configuration = LocalConfiguration.current
    val fontSizeSp = MaterialTheme.typography.bodyMedium.fontSize.value * configuration.fontScale

    var isLoading by remember { mutableStateOf(false) }
    var loadJob by remember { mutableStateOf<Job?>(null) }
    var markdownText by remember { mutableStateOf<String?>(null) }
    var isTooLarge by remember { mutableStateOf(false) }

    fun showTooLargeToast() {
        AppToastHost.showToast("文件过大，无法渲染预览")
    }

    fun openEditor(uri: Uri) {
        localUrlNavigator.navigate(Screen.MarkdownEditor(initialUri = FileUtils.normalizeEditorUri(uri)))
    }

    LaunchedEffect(initialUri) {
        markdownText = null
        isTooLarge = false
        loadJob?.cancel()

        val uri = initialUri ?: return@LaunchedEffect
        val file = FileUtils.resolveFile(uri)
        val maxPreviewBytes = 1024L * 1024L
        if (file != null && file.exists() && file.isFile && file.length() > maxPreviewBytes) {
            isTooLarge = true
            showTooLargeToast()
            return@LaunchedEffect
        }

        isLoading = true
        loadJob = scope.launch {
            runCatching {
                val text = withContext(Dispatchers.IO) {
                    readTextFromUri(context, uri)
                }
                markdownText = text
                val title = FileUtils.fileNameFromUri(uri)
                val bodyHtml = withContext(Dispatchers.Default) {
                    MarkdownStringUtils.convertMarkdownToHtmlGfm(text)
                }
                val html = MarkdownRenderHtmlTemplate.wrap(
                    title = title,
                    bodyHtml = bodyHtml,
                    fontSizeSp = fontSizeSp
                )
                webViewComponent.setWebViewParams(
                    WebViewParams(
                        title = title,
                        baseUrl = UrlConstants.WEB_VIEW_BASE_URL,
                        htmlData = html,
                        isHtml = true,
                        type = WebViewType.MARKDOWN_RENDER
                    )
                )
                webViewComponent.updateTitle(title)
                webViewComponent.load()
            }.onFailure { e ->
                AppToastHost.showToast(e.message ?: "加载失败")
            }
            isLoading = false
            loadJob = null
        }
    }

    LoadingDialog(
        visible = isLoading,
        isForSaving = false,
        onCancelLoading = {
            loadJob?.cancel()
            loadJob = null
            isLoading = false
        }
    ) {
        Text(stringResource(com.shifenmiao.core.R.string.file_loading))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        MarkdownRenderTopBar(
            title = webViewState.currentTitle.ifEmpty { webViewParams?.title ?: "" },
            onBackClick = onGoBack,
            scrollBehavior = scrollBehavior,
            onSavePdf = {
                if (isTooLarge) {
                    showTooLargeToast()
                    return@MarkdownRenderTopBar
                }
                webViewComponent.showPdfExportDialog(localActivity)
            },
            onEdit = {
                val uri = initialUri ?: return@MarkdownRenderTopBar
                openEditor(uri)
            }
        )

        if (!isTooLarge) {
            CommonWebView(
                webViewParams = webViewParams,
                webViewState = webViewState,
                modifier = Modifier.fillMaxSize(),
                onWebViewCreated = { webView ->
                    webViewComponent.setWebView(webView)
                },
                onTitleChanged = { newTitle -> webViewComponent.updateTitle(newTitle) },
                client = webViewComponent.getWebViewClient(
                    onShouldOverrideUrlLoading = { url ->
                        localUrlNavigator.navigate(url)
                    }
                ),
                chromeClient = webViewComponent.getWebChromeClient(),
                onReload = { webViewComponent.reload() }
            )
        }
    }

    BackHandler {
        if (webViewState.canGoBack) {
            webViewComponent.goBack()
        } else {
            onGoBack()
        }
    }
}

@Composable
private fun MarkdownRenderTopBar(
    title: String,
    onBackClick: () -> Unit,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior,
    onSavePdf: () -> Unit,
    onEdit: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMore,
                    contentDescription = "Menu"
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("另存为PDF") },
                    leadingIcon = {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePrint,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        onSavePdf()
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("编辑") },
                    leadingIcon = {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        onEdit()
                        showMenu = false
                    }
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

private fun readTextFromUri(context: Context, uri: Uri): String {
    val resolverStream = runCatching { context.contentResolver.openInputStream(uri) }.getOrNull()
    if (resolverStream != null) {
        return resolverStream.bufferedReader().use { it.readText() }
    }

    val path = uri.path ?: error("无法读取文件")
    return File(path).readText()
}
