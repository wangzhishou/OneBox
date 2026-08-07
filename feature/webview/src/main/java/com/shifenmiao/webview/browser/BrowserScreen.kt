package com.shifenmiao.webview.browser

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.shifenmiao.webview.common.WebViewPool
import com.shifenmiao.webview.common.WebViewSettings
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFileCreator
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.shifenmiao.common.ui.LoadingOverlay

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    component: BrowserComponent,
    onGoBack: () -> Unit
) {
    val state by component.state.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val backgroundColor = androidx.compose.material3.MaterialTheme.colorScheme.background.toArgb()

    var addressText by remember { mutableStateOf(state.currentUrl) }
    var isSearchFocused by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val savePngLauncher = rememberFileCreator(
        mimeType = MimeType.StaticPng,
        onSuccess = { uri ->
            isSaving = true
            component.savePngFile(fileUri = uri) { result ->
                isSaving = false
                when (result) {
                    is SaveResult.Success -> AppToastHost.showToast("已保存为 PNG")
                    is SaveResult.Error -> AppToastHost.showToast("保存失败: ${result.throwable.localizedMessage ?: "未知错误"}")
                    SaveResult.Skipped -> {}
                }
            }
        }
    )

    if (!isSearchFocused && addressText != state.currentUrl) {
        addressText = state.currentUrl
    }

    DisposableEffect(Unit) {
        onDispose { component.detachWebView() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            BrowserTopBar(
                addressText = addressText,
                onAddressTextChange = { addressText = it },
                onSubmit = {
                    if (addressText.isNotBlank()) {
                        component.loadUrl(addressText)
                        focusManager.clearFocus()
                        isSearchFocused = false
                    }
                },
                isLoading = state.isLoading,
                isSearchFocused = isSearchFocused,
                onSearchFocusChange = { isSearchFocused = it },
                faviconUrl = state.currentFaviconUrl,
                onGoBack = {
                    if (state.selectedPage != BrowserState.BrowserPage.Home) {
                        component.selectPage(BrowserState.BrowserPage.Home)
                    } else {
                        onGoBack()
                    }
                },
                onReload = component::reload,
                onStop = component::stopLoading,
                modifier = Modifier.fillMaxWidth()
            )

            if (!isSearchFocused) {
                if (state.isLoading && state.progress < 1f) {
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { state.progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        trackColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
                    )
                } else {
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when {
                    isSearchFocused -> SearchSuggestionPanel(
                        query = addressText,
                        history = state.history,
                        bookmarks = state.bookmarks,
                        onItemClick = { url ->
                            component.loadUrl(url)
                            focusManager.clearFocus()
                            isSearchFocused = false
                        },
                        onDismiss = {
                            focusManager.clearFocus()
                            isSearchFocused = false
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    else -> Crossfade(
                        targetState = state.selectedPage,
                        label = "browser_page_crossfade"
                    ) { page ->
                        when (page) {
                            BrowserState.BrowserPage.Home -> {
                                BrowserWebViewContent(
                                    state = state,
                                    component = component,
                                    backgroundColor = backgroundColor,
                                    context = context,
                                    onAddressUpdate = { addressText = it }
                                )
                            }
                            BrowserState.BrowserPage.Tabs -> BrowserTabsPage(component = component)
                            BrowserState.BrowserPage.Bookmarks -> BrowserBookmarksPage(component = component)
                            BrowserState.BrowserPage.Settings -> BrowserSettingsPage(component = component)
                        }
                    }
                }
            }

            BrowserBottomBar(
                canGoBack = state.canGoBack,
                canGoForward = state.canGoForward,
                tabCount = state.tabs.size,
                isBookmarked = state.isCurrentPageBookmarked,
                selectedPage = state.selectedPage,
                onGoBack = component::goBack,
                onGoForward = component::goForward,
                onHome = { component.selectPage(BrowserState.BrowserPage.Home) },
                onTabs = { component.selectPage(BrowserState.BrowserPage.Tabs) },
                onAddBookmark = component::toggleBookmark,
                onMoreClick = { showMoreMenu = true }
            )
        }

        if (showMoreMenu) {
            BrowserMoreMenu(
                isBookmarked = state.isCurrentPageBookmarked,
                onBookmarkToggle = {
                    component.toggleBookmark()
                    showMoreMenu = false
                },
                onExportPng = {
                    showMoreMenu = false
                    savePngLauncher.make(component.getExportFileName() + ".png")
                },
                onExportPdf = {
                    showMoreMenu = false
                    component.exportPdf(context)
                },
                onBookmarks = {
                    component.selectPage(BrowserState.BrowserPage.Bookmarks)
                    showMoreMenu = false
                },
                onSettings = {
                    component.selectPage(BrowserState.BrowserPage.Settings)
                    showMoreMenu = false
                },
                onDismiss = { showMoreMenu = false }
            )
        }

        LoadingOverlay(
            visible = isSaving,
            onCancelLoading = { isSaving = false },
            canCancel = true
        )
    }

    BackHandler {
        when {
            isSearchFocused -> {
                focusManager.clearFocus()
                isSearchFocused = false
            }
            state.selectedPage != BrowserState.BrowserPage.Home -> {
                component.selectPage(BrowserState.BrowserPage.Home)
            }
            state.canGoBack -> component.goBack()
            else -> onGoBack()
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun BrowserWebViewContent(
    state: BrowserState,
    component: BrowserComponent,
    backgroundColor: Int,
    context: android.content.Context,
    onAddressUpdate: (String) -> Unit
) {
    val urlToLoad = state.currentUrl

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                WebViewPool.create(ctx, backgroundColor).apply {
                    WebViewSettings.applyCommonSettings(this, ctx)
                    this.webViewClient = BrowserWebViewClient(context, component, onAddressUpdate)
                    this.webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            component.onProgressChanged(newProgress)
                        }
                    }
                    if (urlToLoad.isNotBlank()) {
                        loadUrl(urlToLoad)
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                val isBlank = state.currentUrl.isBlank()
                view.setBackgroundColor(
                    if (isBlank) android.graphics.Color.TRANSPARENT else backgroundColor
                )
                view.visibility = if (isBlank) android.view.View.INVISIBLE else android.view.View.VISIBLE
                component.attachWebView(view)
                component.onNavigationStateChanged(view.canGoBack(), view.canGoForward())
            }
        )

        if (state.isHomePage) {
            BrowserHomePage(
                onSiteClick = { url -> component.loadUrl(url) }
            )
        }
    }
}

private class BrowserWebViewClient(
    private val context: android.content.Context,
    private val component: BrowserComponent,
    private val onAddressUpdate: (String) -> Unit
) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        component.onPageStarted(url)
        url?.let { onAddressUpdate(it) }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        component.onPageFinished(url, view?.title)
        onAddressUpdate(url ?: "")
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return request?.url?.let { handleExternalUri(it) } ?: false
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        return url?.let { handleExternalUri(Uri.parse(it)) } ?: false
    }

    private fun handleExternalUri(uri: Uri): Boolean {
        val scheme = uri.scheme?.lowercase() ?: return false
        if (scheme == "http" || scheme == "https") return false
        return try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            true
        } catch (_: Exception) {
            false
        }
    }
}
