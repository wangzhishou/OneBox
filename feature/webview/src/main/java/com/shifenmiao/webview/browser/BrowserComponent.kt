package com.shifenmiao.webview.browser

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnStart
import com.shifenmiao.base.utils.ImageUtils
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.data_draft.DataDraftHelper
import com.shifenmiao.interfaces.bookmark.UrlBookmark
import com.shifenmiao.interfaces.bookmark.UrlBookmarkService
import com.shifenmiao.interfaces.browser.BrowserAutomationService
import com.shifenmiao.interfaces.browser.BrowserController
import com.shifenmiao.interfaces.browser.BrowserTabInfo
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.event.AppEventBus
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.shifenmiao.webview.R
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BrowserComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val initialUrl: String = "",
    @Assisted val bookmarkItemId: Int = 0,
    @Assisted val initialLocalUri: Uri? = null,
    @Assisted val onEditBookmark: ((itemId: Int, draftId: Long) -> Unit)? = null,
    dispatchersHolder: DispatchersHolder,
    private val repository: BrowserRepository,
    private val bookmarkService: UrlBookmarkService,
    private val fileController: FileController,
    private val automationService: BrowserAutomationService,
    private val dataDraftHelper: DataDraftHelper,
    private val shareProvider: ImageShareProvider<Bitmap>,
    appDatabase: AppDatabase,
    @ApplicationContext private val applicationContext: Context,
) : BaseComponent(dispatchersHolder, componentContext), BrowserController {

    private val navigator: WebViewNavigator = AndroidWebViewNavigator()

    private val itemDao = appDatabase.itemEntityDao()
    private val itemDataDao = appDatabase.itemDataDao()

    /** init 载入过目标收藏：可见时跳过首次重复加载。 */
    @Volatile
    private var isInitialBookmarkLoaded = false

    private val _state = MutableStateFlow(
        BrowserState(
            tabs = repository.loadTabs(),
            history = repository.loadHistory(),
            settings = repository.loadSettings(),
        )
    )
    val state: StateFlow<BrowserState> = _state.asStateFlow()

    init {
        when {
            bookmarkItemId > 0 -> {
                isInitialBookmarkLoaded = true
                openBookmark(itemId = bookmarkItemId, asNewTab = true)
            }
            else -> initializeTabs(initialUrl)
        }
        observeUrlBookmarks()
        observeBookmarkCategories()
        observeBookmarkChanges()
        automationService.attach(this)
        // 生命周期 STARTED = 页面重新可见：从编辑页返回时按最新数据重载收藏
        componentContext.lifecycle.doOnStart { onResumed() }
        componentContext.lifecycle.doOnDestroy {
            automationService.detach()
            if (_state.value.settings.clearCacheOnExit) {
                navigator.clearCache()
            }
            navigator.detach()
        }
    }

    /**
     * 页面重新可见时按最新数据重载收藏。
     *
     * 从编辑页返回、或去别的页面转一圈回来时，收藏可能已改过标题/正文/分类；
     * 本组件常驻返回栈、并不会离场重建，所以在这一刻补齐刷新最稳。
     */
    private fun onResumed() {
        if (isInitialBookmarkLoaded) {
            // init 已经载入过：首次可见跳过，避免重复渲染同一份快照
            isInitialBookmarkLoaded = false
            return
        }
        val context = _state.value.viewingBookmark
        val target = context?.itemId?.takeIf { it > 0 }
            ?: bookmarkItemId.takeIf { it > 0 }
            ?: return
        openBookmark(itemId = target, asNewTab = false, showSnapshot = true)
    }

    /**
     * 本地 HTML 文件（file:// / content://）：直接交给 WebView 渲染，
     * 标题用文件名兜底，避免列表里只显示一串 URI。
     */
    fun loadLocalFile(uri: Uri, displayName: String? = null) {
        val url = uri.toString()
        val title = displayName?.takeIf { it.isNotBlank() } ?: uri.lastPathSegment.orEmpty()
        _state.update {
            it.copy(
                currentTitle = title,
                viewingBookmark = null,
            )
        }
        updateActiveTabTitle(title)
        loadUrl(url)
    }

    /** 书签与「网址收藏」共用 Room 存储，由 Flow 驱动 state，写操作无需手动刷新。 */
    private fun observeUrlBookmarks() {
        componentScope.launch {
            bookmarkService.observeBookmarks().collect { bookmarks ->
                _state.update { it.copy(bookmarks = bookmarks.map(::toBookmarkItem)) }
            }
        }
    }

    private fun observeBookmarkCategories() {
        componentScope.launch {
            bookmarkService.observeCategories().collect { categories ->
                _state.update {
                    it.copy(
                        bookmarkFolders = categories.mapIndexed { index, category ->
                            BookmarkFolder(
                                id = category.id.toString(),
                                name = category.name,
                                order = index,
                                canEdit = category.canEdit,
                                isLocal = category.isLocal,
                            )
                        }
                    )
                }
            }
        }
    }

    private fun toBookmarkItem(bookmark: UrlBookmark) = BookmarkItem(
        id = bookmark.itemId.toString(),
        url = bookmark.url,
        title = bookmark.title,
        folderId = bookmark.categoryIds.firstOrNull()?.toString() ?: "",
        favicon = bookmark.url.toFaviconUrl(),
        timestamp = bookmark.createdAt,
    )

    // ── 网址收藏查看器 ──────────────────────────────────────────────────

    /**
     * 打开一条网址收藏：有快照就离线渲染快照（base 用原始网址，相对路径/图片仍按站点解析），
     * 没有快照则直接加载线上页面。地址栏始终显示原始网址，不暴露 loadDataWithBaseURL 的伪装地址。
     */
    fun openBookmark(itemId: Int, asNewTab: Boolean = false, showSnapshot: Boolean = true) {
        componentScope.launch(ioDispatcher) {
            val item = itemDao.getItemEntityById(itemId) ?: return@launch
            val snapshot = itemDataDao.getByItemId(itemId)?.data.orEmpty()
            val hasSnapshot = snapshot.isNotBlank()
            val useSnapshot = showSnapshot && hasSnapshot
            val displayUrl = item.url.ifBlank { if (useSnapshot) SNAPSHOT_URL_PREFIX + itemId else "" }

            val context = BrowserState.BookmarkContext(
                itemId = itemId,
                title = item.title,
                url = displayUrl,
                hasSnapshot = hasSnapshot,
                showSnapshot = useSnapshot,
            )

            _state.update { current ->
                // 首开时浏览器会给一个空标签占位，直接复用它，
                // 否则「每看一条收藏多一个标签」，标签数会越滚越大
                val blankActiveTabId = current.activeTabId?.takeIf { activeId ->
                    current.tabs.any { it.id == activeId && it.url.isBlank() }
                }
                val tabId = when {
                    !asNewTab -> current.activeTabId ?: uuid()
                    blankActiveTabId != null -> blankActiveTabId
                    else -> uuid()
                }
                val newTab = BrowserTab(
                    id = tabId,
                    url = displayUrl,
                    title = item.title,
                    favicon = displayUrl.toFaviconUrl(),
                    isActive = true,
                )
                val tabs = if (current.tabs.any { it.id == tabId }) {
                    current.tabs.map { if (it.id == tabId) newTab else it }
                } else {
                    current.tabs.map { it.copy(isActive = false) } + newTab
                }
                current.copy(
                    tabs = tabs,
                    activeTabId = tabId,
                    currentUrl = displayUrl,
                    currentTitle = item.title,
                    currentFaviconUrl = displayUrl.toFaviconUrl(),
                    canGoBack = false,
                    canGoForward = false,
                    viewingBookmark = context,
                    selectedPage = BrowserState.BrowserPage.Home,
                )
            }
            persistTabs()

            withContext(uiDispatcher) {
                if (useSnapshot) {
                    navigator.loadHtml(snapshot, displayUrl.takeIf { it.isNotBlank() })
                } else if (displayUrl.isNotBlank()) {
                    navigator.loadUrl(displayUrl)
                }
            }
        }
    }

    /** 快照 ⇄ 线上原页 切换（仅当前查看的收藏有快照时可用）。 */
    fun toggleSnapshotView() {
        val context = _state.value.viewingBookmark ?: return
        if (!context.hasSnapshot) return
        openBookmark(
            itemId = context.itemId,
            asNewTab = false,
            showSnapshot = !context.showSnapshot,
        )
    }

    /** 回到收藏快照（从原页返回离线内容）。 */
    fun showBookmarkSnapshot() {
        val context = _state.value.viewingBookmark ?: return
        if (context.showSnapshot) return
        openBookmark(itemId = context.itemId, asNewTab = false, showSnapshot = true)
    }

    /**
     * 进入编辑：由页面层提供 [BrowserScreen] 的导航回调时可用。
     * 这里只负责准备草稿并交回 itemId + draftId，导航实现留在 UI 层。
     */
    fun requestEditBookmark() {
        val context = _state.value.viewingBookmark ?: return
        val navigate = onEditBookmark ?: return
        componentScope.launch(ioDispatcher) {
            val draftId = dataDraftHelper.createDraft(
                draftType = ListItemType.HTML.id,
                itemId = context.itemId,
            )
            withContext(uiDispatcher) { navigate(context.itemId, draftId) }
        }
    }

    /** 编辑页保存/删除后广播：正在看这条收藏就重载，保证不显示旧快照。 */
    private fun observeBookmarkChanges() {
        componentScope.launch {
            AppEventBus.bookmarkChangedEvents.collect { event ->
                val context = _state.value.viewingBookmark ?: return@collect
                val isSelf = event.itemId == 0 || event.itemId == context.itemId
                if (!isSelf) return@collect
                if (event.isDeleted) {
                    onGoBack()
                } else {
                    openBookmark(itemId = context.itemId, asNewTab = false, showSnapshot = true)
                }
            }
        }
    }

    private fun initializeTabs(url: String) {
        val currentTabs = _state.value.tabs
        when {
            url.isNotBlank() -> {
                val tab = BrowserTab(id = uuid(), url = url, title = url, isActive = true)
                _state.update {
                    it.copy(tabs = listOf(tab), activeTabId = tab.id, currentUrl = url)
                }
                persistTabs()
            }
            currentTabs.isEmpty() -> {
                val tab = BrowserTab(id = uuid(), isActive = true)
                _state.update {
                    it.copy(tabs = listOf(tab), activeTabId = tab.id)
                }
                persistTabs()
            }
            else -> {
                val active = currentTabs.find { it.isActive }
                if (active != null) {
                    _state.update { it.copy(activeTabId = active.id, currentUrl = active.url) }
                } else if (currentTabs.isNotEmpty()) {
                    val first = currentTabs.first().copy(isActive = true)
                    val rest = currentTabs.drop(1).map { t -> t.copy(isActive = false) }
                    _state.update {
                        it.copy(tabs = listOf(first) + rest, activeTabId = first.id, currentUrl = first.url)
                    }
                    persistTabs()
                }
            }
        }
    }

    /** @return 见 [WebViewNavigator.attach] */
    fun attachWebView(webView: android.webkit.WebView): Boolean {
        val consumedPending = navigator.attach(webView)
        navigator.applySettings(_state.value.settings)
        return consumedPending
    }

    fun detachWebView() {
        navigator.detach()
    }

    fun selectPage(page: BrowserState.BrowserPage) {
        _state.update { it.copy(selectedPage = page) }
    }

    override fun loadUrl(url: String) {
        val normalized = normalizeUrl(url, _state.value.settings.currentSearchEngine)
        // 手动输入/跳转即离开收藏快照语义：快照标记清掉，底栏恢复浏览器形态
        _state.update {
            it.copy(
                currentUrl = normalized,
                isLoading = true,
                progress = 0f,
                viewingBookmark = null,
            )
        }
        navigator.loadUrl(normalized)
        updateActiveTabUrl(normalized)
    }

    override fun goBack() = navigator.goBack()
    override fun goForward() = navigator.goForward()
    override fun reload() = navigator.reload()
    override fun stopLoading() = navigator.stopLoading()

    fun onPageStarted(url: String?) {
        _state.update { it.copy(isLoading = true, progress = 0f) }
    }

    fun onPageFinished(url: String?, title: String?) {
        _state.update {
            it.copy(
                isLoading = false,
                progress = 1f,
                currentUrl = url ?: it.currentUrl,
                currentTitle = title?.takeIf { t -> t.isNotBlank() } ?: it.currentTitle,
                currentFaviconUrl = url?.toFaviconUrl() ?: it.currentFaviconUrl
            )
        }
        url?.let { u ->
            if (u.isNotBlank() && u != "about:blank") {
                updateActiveTabUrl(u, u.toFaviconUrl())
                // 本地文件 / 收藏快照不进浏览历史：前者是外部文件，后者是收藏自身的离线副本
                if (!_state.value.settings.enablePrivacyMode && isWebUrl(u)) {
                    recordHistory(u, title ?: "")
                }
            }
        }
    }

    fun onProgressChanged(progress: Int) {
        _state.update {
            it.copy(progress = progress / 100f, isLoading = progress < 100)
        }
    }

    fun onNavigationStateChanged(canGoBack: Boolean, canGoForward: Boolean) {
        _state.update { it.copy(canGoBack = canGoBack, canGoForward = canGoForward) }
    }

    override fun addTab(url: String, title: String): String {
        val id = uuid()
        val newTab = BrowserTab(
            id = id,
            url = url,
            title = title.ifEmpty { url },
            isActive = true
        )
        _state.update { s ->
            s.copy(
                tabs = s.tabs.map { it.copy(isActive = false) } + newTab,
                activeTabId = id,
                currentUrl = url
            )
        }
        if (url.isNotEmpty()) {
            navigator.loadUrl(url)
        }
        persistTabs()
        return id
    }

    override fun closeTab(tabId: String) {
        val currentTabs = _state.value.tabs.toMutableList()
        val index = currentTabs.indexOfFirst { it.id == tabId }
        if (index == -1) return
        currentTabs.removeAt(index)

        val wasActive = _state.value.activeTabId == tabId
        var newActiveId: String? = _state.value.activeTabId
        var newUrl = _state.value.currentUrl

        if (wasActive && currentTabs.isNotEmpty()) {
            val newIndex = index.coerceAtMost(currentTabs.lastIndex)
            currentTabs[newIndex] = currentTabs[newIndex].copy(isActive = true)
            newActiveId = currentTabs[newIndex].id
            newUrl = currentTabs[newIndex].url
            navigator.loadUrl(newUrl.ifEmpty { "about:blank" })
        } else if (currentTabs.isEmpty()) {
            newActiveId = null
            newUrl = ""
        }

        _state.update {
            it.copy(tabs = currentTabs, activeTabId = newActiveId, currentUrl = newUrl)
        }
        persistTabs()
    }

    override fun switchTab(tabId: String) {
        if (_state.value.activeTabId == tabId) return

        _state.update { s ->
            s.copy(tabs = s.tabs.map { it.copy(isActive = it.id == tabId) }, activeTabId = tabId)
        }
        val tab = _state.value.tabs.find { it.id == tabId }
        tab?.let {
            _state.update { s -> s.copy(currentUrl = it.url) }
            navigator.loadUrl(it.url.ifEmpty { "about:blank" })
        }
        persistTabs()
    }

    fun closeAllTabs() {
        val defaultTab = BrowserTab(id = uuid(), isActive = true)
        _state.update {
            it.copy(
                tabs = listOf(defaultTab),
                activeTabId = defaultTab.id,
                currentUrl = "",
                currentTitle = "",
                currentFaviconUrl = "",
                canGoBack = false,
                canGoForward = false
            )
        }
        navigator.loadUrl("about:blank")
        persistTabs()
    }

    fun toggleBookmark() {
        val url = _state.value.currentUrl
        if (url.isEmpty()) return
        val title = _state.value.currentTitle.ifEmpty { url }
        componentScope.launch(ioDispatcher) {
            if (bookmarkService.findByUrl(url) != null) {
                bookmarkService.removeByUrl(url)
            } else {
                bookmarkService.saveBookmark(url, title)
            }
        }
    }

    fun addBookmark(url: String, title: String, folderId: String = "") {
        if (url.isEmpty()) return
        componentScope.launch(ioDispatcher) {
            bookmarkService.saveBookmark(
                url = url,
                title = title.ifEmpty { url },
                categoryId = folderId.toLongOrNull(),
            )
        }
    }

    fun removeBookmark(bookmarkId: String) {
        val itemId = bookmarkId.toLongOrNull() ?: return
        componentScope.launch(ioDispatcher) {
            bookmarkService.removeBookmark(itemId)
        }
    }

    fun addBookmarkFolder(name: String) {
        if (name.isBlank()) return
        componentScope.launch(ioDispatcher) {
            bookmarkService.createCategory(name)
        }
    }

    fun renameBookmarkFolder(folderId: String, newName: String) {
        val categoryId = folderId.toLongOrNull() ?: return
        componentScope.launch(ioDispatcher) {
            bookmarkService.renameCategory(categoryId, newName)
        }
    }

    fun deleteBookmarkFolder(folderId: String) {
        val categoryId = folderId.toLongOrNull() ?: return
        componentScope.launch(ioDispatcher) {
            bookmarkService.deleteCategory(categoryId)
        }
    }

    fun clearHistory() {
        repository.clearHistory()
        _state.update { it.copy(history = emptyList()) }
    }

    fun removeHistoryItem(historyId: String) {
        _state.update { it.copy(history = it.history.filter { h -> h.id != historyId }) }
        repository.saveHistory(_state.value.history)
    }

    fun updateSettings(newSettings: BrowserSettings) {
        _state.update { it.copy(settings = newSettings) }
        repository.saveSettings(newSettings)
        navigator.applySettings(newSettings)
    }

    fun exportPdf(context: android.content.Context) {
        val jobName = getExportFileName()
        val printAdapter = navigator.createPrintDocumentAdapter(jobName) ?: return
        val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE)
            as? android.print.PrintManager ?: return
        printManager.print(
            jobName,
            printAdapter,
            android.print.PrintAttributes.Builder()
                .setMediaSize(android.print.PrintAttributes.MediaSize.ISO_A4)
                .setResolution(android.print.PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                .setMinMargins(android.print.PrintAttributes.Margins.NO_MARGINS)
                .build()
        )
    }

    /**
     * 分享当前页面：整页截图走系统分享（与旧预览页的分享能力对齐）。
     * WebView 尚未就绪时给出提示而不是静默失败。
     */
    fun sharePage() {
        componentScope.launch {
            val bitmap = withContext(defaultDispatcher) { navigator.captureBitmap() }
            if (bitmap == null) {
                withContext(uiDispatcher) {
                    AppToastHost.showToast(
                        applicationContext.getString(R.string.browser_share_unavailable)
                    )
                }
                return@launch
            }
            val imageInfo = ImageInfo(
                width = bitmap.width,
                height = bitmap.height,
                imageFormat = ImageFormat.Png.Lossless,
            )
            val shared = runCatching {
                val cachedUri = shareProvider.cacheImage(
                    image = bitmap,
                    imageInfo = imageInfo,
                    filename = getExportFileName() + ".png",
                ) ?: return@runCatching false
                shareProvider.shareUri(
                    uri = cachedUri,
                    type = imageInfo.imageFormat.mimeType,
                    onComplete = {},
                )
                true
            }.getOrElse { false }
            bitmap.recycle()
            withContext(uiDispatcher) {
                AppToastHost.showToast(
                    applicationContext.getString(
                        if (shared) R.string.browser_shared else R.string.browser_share_unavailable
                    )
                )
            }
        }
    }

    fun savePngFile(fileUri: Uri, onResult: (SaveResult) -> Unit) {
        componentScope.launch {
            val bitmap = withContext(uiDispatcher) { navigator.captureBitmap() }
            val result = withContext(defaultDispatcher) {
                runCatching {
                    if (bitmap != null) {
                        fileController.writeBytes(
                            uri = fileUri.toString(),
                            block = { it.writeBytes(ImageUtils.bitmapToByteArray(bitmap)) }
                        )
                    } else {
                        SaveResult.Error.Exception(Exception("WebView not available"))
                    }
                }.getOrElse { SaveResult.Error.Exception(it) }
            }
            withContext(uiDispatcher) { onResult(result) }
        }
    }

    fun getExportFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HH", Locale.getDefault()).format(Date())
        val pageTitle = _state.value.currentTitle.takeIf { it.isNotEmpty() }
            ?: _state.value.currentUrl.takeIf { it.isNotEmpty() }
            ?: "untitled"
        val sanitized = pageTitle.take(20).replace("[\\\\/:*?\"<>|]".toRegex(), "_")
        return "${sanitized}_$timestamp"
    }

    fun clearCache() {
        navigator.clearCache()
    }

    fun clearAllData() {
        navigator.clearCache()
        repository.clearAll()
        val defaultTab = BrowserTab(id = uuid(), isActive = true)
        // 书签已迁入 Room 网址收藏体系，不属于浏览器私有数据，清浏览数据不动它
        _state.update {
            it.copy(
                tabs = listOf(defaultTab),
                activeTabId = defaultTab.id,
                history = emptyList(),
                settings = BrowserSettings(),
                currentUrl = "",
                currentTitle = "",
                currentFaviconUrl = "",
                canGoBack = false,
                canGoForward = false
            )
        }
    }

    private fun updateActiveTabUrl(url: String, favicon: String = "") {
        val activeId = _state.value.activeTabId ?: return
        _state.update { s ->
            s.copy(tabs = s.tabs.map {
                if (it.id == activeId) {
                    it.copy(
                        url = url,
                        title = s.currentTitle.ifEmpty { it.title },
                        favicon = favicon.ifEmpty { it.favicon }
                    )
                } else it
            })
        }
        persistTabs()
    }

    private fun updateActiveTabTitle(title: String) {
        val activeId = _state.value.activeTabId ?: return
        _state.update { s ->
            s.copy(tabs = s.tabs.map { if (it.id == activeId) it.copy(title = title) else it })
        }
        persistTabs()
    }

    private fun recordHistory(url: String, title: String) {
        val item = HistoryItem(
            id = uuid(),
            url = url,
            title = title.ifEmpty { url },
            favicon = url.toFaviconUrl()
        )
        repository.addHistory(item)
        _state.update { it.copy(history = repository.loadHistory()) }
    }

    private fun persistTabs() {
        repository.saveTabs(_state.value.tabs)
    }

    private fun uuid(): String = UUID.randomUUID().toString()

    // ===== BrowserController 实现 =====

    override fun getCurrentUrl(): String = _state.value.currentUrl
    override fun getCurrentTitle(): String = _state.value.currentTitle
    override fun canGoBack(): Boolean = _state.value.canGoBack
    override fun canGoForward(): Boolean = _state.value.canGoForward
    override fun isLoading(): Boolean = _state.value.isLoading

    override fun getTabs(): List<BrowserTabInfo> = _state.value.tabs.map { tab ->
        BrowserTabInfo(
            id = tab.id,
            url = tab.url,
            title = tab.title,
            isActive = tab.id == _state.value.activeTabId
        )
    }

    override fun evaluateJavascript(script: String, callback: (String?) -> Unit) {
        navigator.evaluateJavascript(script, callback)
    }

    override suspend fun captureScreenshotBase64(): String? {
        val bitmap = withContext(uiDispatcher) { navigator.captureBitmap() } ?: return null
        return withContext(defaultDispatcher) {
            runCatching {
                val stream = ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
                Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
            }.getOrNull()
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            initialUrl: String = "",
            bookmarkItemId: Int = 0,
            initialLocalUri: Uri? = null,
            onEditBookmark: ((itemId: Int, draftId: Long) -> Unit)? = null,
        ): BrowserComponent
    }

    companion object {
        /** 无 url 的纯快照收藏展示用的伪地址（仅用于列表/标签标题，不参与网络请求）。 */
        const val SNAPSHOT_URL_PREFIX = "onebox:snapshot/"

        fun isWebUrl(url: String): Boolean =
            url.startsWith("http://") || url.startsWith("https://")
    }
}
