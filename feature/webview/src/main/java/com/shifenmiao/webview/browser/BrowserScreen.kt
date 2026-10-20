package com.shifenmiao.webview.browser

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.webview.common.WebViewPool
import com.shifenmiao.webview.common.WebViewSettings
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.wanbaohe.core.ui.utils.content_pickers.rememberFileCreator
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.helper.SaveSuccessNotifier
import com.shifenmiao.common.ui.LoadingOverlay
import kotlinx.coroutines.delay

/**
 * 导航进入动画里最长的一段是 scale 500ms
 * （[com.wanbaohe.core.ui.utils.animation.toolboxPredictiveBackAnimation] 的 defaultAnimator）。
 * 这段时间里如果已经把 WebView 挂上，它的合成层不跟着 Compose 的位移/缩放走，
 * 会在最终位置先画出一块白，把顶栏盖住，动画落稳后顶栏才补出来，看起来像卡住又闪一下。
 */
private const val BROWSER_ENTER_SETTLE_MILLIS = 500L

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
    // 进入动画落稳前不创建 WebView，见 [BROWSER_ENTER_SETTLE_MILLIS]。
    var isWebViewReady by remember { mutableStateOf(false) }

    val savePngLauncher = rememberFileCreator(
        mimeType = MimeType.StaticPng,
        onSuccess = { uri ->
            isSaving = true
            component.savePngFile(fileUri = uri) { result ->
                isSaving = false
                when (result) {
                    is SaveResult.Success -> SaveSuccessNotifier.notify(result)
                    is SaveResult.Error -> AppToastHost.showFailureToast(result.throwable)
                    SaveResult.Skipped -> {}
                }
            }
        }
    )

    // 从收藏进来的那条本地 HTML 文件（分享/文件浏览器入口）：交给同一套 WebView 渲染
    LaunchedEffect(component) {
        component.initialLocalUri?.let { uri ->
            component.loadLocalFile(uri, uri.lastPathSegment)
        }
    }

    LaunchedEffect(Unit) {
        delay(BROWSER_ENTER_SETTLE_MILLIS)
        // 再让出一帧，避免和动画最后一帧抢主线程
        withFrameNanos { }
        isWebViewReady = true
    }

    if (!isSearchFocused && addressText != state.currentUrl) {
        addressText = state.currentUrl
    }

    DisposableEffect(Unit) {
        onDispose { component.detachWebView() }
    }

    /** 收藏查看器里顶栏返回 = 回到收藏列表；普通浏览才回退历史。 */
    val onTopBarBack: () -> Unit = {
        when {
            state.selectedPage != BrowserState.BrowserPage.Home -> {
                component.selectPage(BrowserState.BrowserPage.Home)
            }
            state.isBookmarkViewer -> onGoBack()
            state.canGoBack -> component.goBack()
            else -> onGoBack()
        }
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
                onGoBack = onTopBarBack,
                onReload = component::reload,
                onStop = component::stopLoading,
                modifier = Modifier.fillMaxWidth()
            )

            if (!isSearchFocused) {
                if (state.isLoading && state.progress < 1f) {
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { state.progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer,
                        trackColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
                    )
                } else {
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                // WebView 常驻：不再放进 Crossfade 的分支里。
                // 之前切到「标签页 / 书签 / 设置」时 Home 分支离开组合，WebView 被丢弃，
                // 切回来工厂重跑 → 重新 new WebView + loadUrl → 整页重请求，
                // 已渲染内容和前进/后退历史全部丢失（旧实例还只能等 GC）。
                // 现在它始终在组合里，其它页面改成盖在它上面的不透明浮层。
                BrowserWebViewContent(
                    state = state,
                    component = component,
                    backgroundColor = backgroundColor,
                    context = context,
                    isWebViewReady = isWebViewReady,
                    onAddressUpdate = { addressText = it }
                )

                if (isSearchFocused) {
                    // 地址栏聚焦时同样用不透明底盖住，避免 WebView 内容透出来
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(androidx.compose.ui.graphics.Color(backgroundColor))
                    ) {
                        SearchSuggestionPanel(
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
                    }
                } else {
                    Crossfade(
                        targetState = state.selectedPage,
                        label = "browser_page_crossfade"
                    ) { page ->
                        if (page != BrowserState.BrowserPage.Home) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(androidx.compose.ui.graphics.Color(backgroundColor))
                                    // 这些页面本身不一定铺满，空出来的地方要吞掉点击，
                                    // 否则会穿透到底下常驻的 WebView 上。
                                    .pointerInput(Unit) { detectTapGestures { } }
                            ) {
                                when (page) {
                                    BrowserState.BrowserPage.Tabs -> BrowserTabsPage(component = component)
                                    BrowserState.BrowserPage.Bookmarks -> BrowserBookmarksPage(component = component)
                                    BrowserState.BrowserPage.Settings -> BrowserSettingsPage(component = component)
                                    BrowserState.BrowserPage.Home -> Unit
                                }
                            }
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
                isBookmarkViewer = state.isBookmarkViewer,
                onGoBack = component::goBack,
                onGoForward = component::goForward,
                onHome = { component.selectPage(BrowserState.BrowserPage.Home) },
                onEditBookmark = {
                    if (component.onEditBookmark != null) {
                        component.requestEditBookmark()
                    } else {
                        onGoBack()
                    }
                },
                onTabs = { component.selectPage(BrowserState.BrowserPage.Tabs) },
                onAddBookmark = component::toggleBookmark,
                onMoreClick = { showMoreMenu = true }
            )
        }

        if (showMoreMenu) {
            BrowserMoreMenu(
                isBookmarked = state.isCurrentPageBookmarked,
                isBookmarkViewer = state.isBookmarkViewer,
                // 切换项只对「确实存了快照的收藏」有意义
                isShowingSnapshot = state.isShowingSnapshot,
                canToggleSnapshot = state.viewingBookmark?.hasSnapshot == true,
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
                onToggleSnapshot = {
                    showMoreMenu = false
                    component.toggleSnapshotView()
                },
                onOpenExternal = {
                    showMoreMenu = false
                    state.currentUrl
                        .takeIf { it.isNotBlank() && it != "about:blank" }
                        ?.let { ActionUtils.openWebBrowser(context, it) }
                },
                onSharePage = {
                    showMoreMenu = false
                    component.sharePage()
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
            // 收藏查看器：返回即退出查看（历史仍可用底栏前进/后退按钮回走）
            state.isBookmarkViewer -> onGoBack()
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
    isWebViewReady: Boolean,
    onAddressUpdate: (String) -> Unit
) {
    val urlToLoad = state.currentUrl
    // 主页不提前建 WebView。一旦建过就留在组合里，切回主页也不能拆掉，否则历史和页面会丢。
    var keepWebView by remember { mutableStateOf(false) }
    LaunchedEffect(isWebViewReady, state.isHomePage) {
        if (isWebViewReady && !state.isHomePage) {
            keepWebView = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().clipToBounds()) {
        // 有网址时先铺一层和页面同色的底，WebView 在首帧提交前保持不可见，
        // 避免白底或页头先从顶部补出来。
        if (!state.isHomePage) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(backgroundColor))
            )
        }
        if (keepWebView) {
            AndroidView(
                factory = { ctx ->
                    WebViewPool.create(ctx, backgroundColor).apply {
                        // 先藏起来，等主帧真正可画再露。创建当下就是 VISIBLE 的话，
                        // 第一帧经常是一块白，会盖住已经就位的顶栏。
                        visibility = View.INVISIBLE
                        WebViewSettings.applyCommonSettings(this, ctx)
                        this.webViewClient = BrowserWebViewClient(context, component, onAddressUpdate)
                        this.webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                component.onProgressChanged(newProgress)
                            }
                        }
                        // attach 只需要在 WebView 创建时做一次。
                        // 之前放在 update 里，每次重组都会重新 attach + applySettings。
                        val consumedPending = component.attachWebView(this)
                        if (!consumedPending && urlToLoad.isNotBlank()) {
                            loadUrl(urlToLoad)
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    val isBlank = state.currentUrl.isBlank() || state.currentUrl == "about:blank"
                    view.setBackgroundColor(
                        if (isBlank) android.graphics.Color.TRANSPARENT else backgroundColor
                    )
                    // 只在回到主页时藏起来。有内容时的显示时机交给 onPageCommitVisible，
                    // 这里如果直接设 VISIBLE，会把「等首帧」这道闸冲掉。
                    if (isBlank) {
                        view.visibility = View.INVISIBLE
                    }
                }
            )
        }

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

    override fun onPageCommitVisible(view: WebView?, url: String?) {
        super.onPageCommitVisible(view, url)
        reveal(view, url)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        reveal(view, url)
        component.onPageFinished(url, view?.title)
        // 前进/后退可用性的真实变化点在这里，顺带上报，省掉每次重组都去问一遍 WebView
        if (view != null) {
            component.onNavigationStateChanged(view.canGoBack(), view.canGoForward())
        }
        onAddressUpdate(url ?: "")
    }

    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        super.onReceivedError(view, request, error)
        if (request?.isForMainFrame == true) {
            reveal(view, request.url?.toString())
        }
    }

    /** 主帧有像素了再显示。about:blank / 空地址是主页，继续藏着。 */
    private fun reveal(view: WebView?, url: String?) {
        if (url.isNullOrBlank() || url == "about:blank") return
        view?.visibility = View.VISIBLE
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

    /**
     * 渲染进程被系统回收（低内存等）。
     *
     * 不接管的话 App 会跟着一起退出。这里返回 true 并提示用户重新打开，
     * 但没有做自动恢复：出事的 WebView 实例已经不可用，真正的恢复需要重建实例，
     * 而实例由 Compose 的 AndroidView 持有，从 client 里换不掉。
     */
    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
        AppToastHost.showToast("页面渲染进程已终止，请返回后重新打开")
        return true
    }
}
