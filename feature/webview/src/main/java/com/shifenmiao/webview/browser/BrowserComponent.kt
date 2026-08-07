package com.shifenmiao.webview.browser

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.shifenmiao.base.utils.ImageUtils
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.interfaces.browser.BrowserAutomationService
import com.shifenmiao.interfaces.browser.BrowserController
import com.shifenmiao.interfaces.browser.BrowserTabInfo
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
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
    dispatchersHolder: DispatchersHolder,
    private val repository: BrowserRepository,
    private val fileController: FileController,
    private val activityLogRecorder: ActivityLogRecorder,
    private val automationService: BrowserAutomationService,
    @ApplicationContext private val applicationContext: Context,
) : BaseComponent(dispatchersHolder, componentContext), BrowserController {

    private val navigator: WebViewNavigator = AndroidWebViewNavigator()

    private val _state = MutableStateFlow(
        BrowserState(
            tabs = repository.loadTabs(),
            bookmarks = repository.loadBookmarks(),
            bookmarkFolders = repository.loadBookmarkFolders(),
            history = repository.loadHistory(),
            settings = repository.loadSettings(),
        )
    )
    val state: StateFlow<BrowserState> = _state.asStateFlow()

    init {
        initializeTabs(initialUrl)
        automationService.attach(this)
        componentContext.lifecycle.doOnDestroy {
            automationService.detach()
            if (_state.value.settings.clearCacheOnExit) {
                navigator.clearCache()
            }
            navigator.detach()
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

    fun attachWebView(webView: android.webkit.WebView) {
        navigator.attach(webView)
        navigator.applySettings(_state.value.settings)
    }

    fun detachWebView() {
        navigator.detach()
    }

    fun selectPage(page: BrowserState.BrowserPage) {
        _state.update { it.copy(selectedPage = page) }
    }

    override fun loadUrl(url: String) {
        val normalized = normalizeUrl(url, _state.value.settings.currentSearchEngine)
        _state.update { it.copy(currentUrl = normalized, isLoading = true, progress = 0f) }
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
                currentTitle = title ?: it.currentTitle,
                currentFaviconUrl = url?.toFaviconUrl() ?: it.currentFaviconUrl
            )
        }
        url?.let { u ->
            if (u.isNotBlank() && u != "about:blank") {
                updateActiveTabUrl(u, u.toFaviconUrl())
                if (!_state.value.settings.enablePrivacyMode) {
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
        val existing = _state.value.bookmarks.find { it.url == url }
        if (existing != null) {
            _state.update { it.copy(bookmarks = it.bookmarks.filter { b -> b.url != url }) }
        } else {
            val item = BookmarkItem(
                id = uuid(),
                url = url,
                title = _state.value.currentTitle.ifEmpty { url },
                favicon = url.toFaviconUrl()
            )
            _state.update { it.copy(bookmarks = it.bookmarks + item) }
        }
        repository.saveBookmarks(_state.value.bookmarks)
    }

    fun addBookmark(url: String, title: String, folderId: String = "") {
        if (url.isEmpty()) return
        val existing = _state.value.bookmarks.find { it.url == url }
        if (existing != null) {
            _state.update {
                it.copy(bookmarks = it.bookmarks.map { b ->
                    if (b.url == url) b.copy(title = title.ifEmpty { b.title }, folderId = folderId)
                    else b
                })
            }
        } else {
            val item = BookmarkItem(
                id = uuid(),
                url = url,
                title = title.ifEmpty { url },
                folderId = folderId,
                favicon = url.toFaviconUrl()
            )
            _state.update { it.copy(bookmarks = it.bookmarks + item) }
        }
        repository.saveBookmarks(_state.value.bookmarks)
    }

    fun removeBookmark(bookmarkId: String) {
        _state.update { it.copy(bookmarks = it.bookmarks.filter { b -> b.id != bookmarkId }) }
        repository.saveBookmarks(_state.value.bookmarks)
    }

    fun addBookmarkFolder(name: String) {
        val folder = BookmarkFolder(
            id = uuid(),
            name = name,
            order = _state.value.bookmarkFolders.size
        )
        _state.update { it.copy(bookmarkFolders = it.bookmarkFolders + folder) }
        repository.saveBookmarkFolders(_state.value.bookmarkFolders)
    }

    fun renameBookmarkFolder(folderId: String, newName: String) {
        _state.update {
            it.copy(bookmarkFolders = it.bookmarkFolders.map { f ->
                if (f.id == folderId) f.copy(name = newName) else f
            })
        }
        repository.saveBookmarkFolders(_state.value.bookmarkFolders)
    }

    fun deleteBookmarkFolder(folderId: String) {
        _state.update {
            it.copy(
                bookmarkFolders = it.bookmarkFolders.filter { f -> f.id != folderId },
                bookmarks = it.bookmarks.map { b ->
                    if (b.folderId == folderId) b.copy(folderId = "") else b
                }
            )
        }
        repository.saveBookmarkFolders(_state.value.bookmarkFolders)
        repository.saveBookmarks(_state.value.bookmarks)
    }

    fun reorderBookmarkFolders(folders: List<BookmarkFolder>) {
        val reordered = folders.mapIndexed { index, folder -> folder.copy(order = index) }
        _state.update { it.copy(bookmarkFolders = reordered) }
        repository.saveBookmarkFolders(reordered)
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
            if (result is SaveResult.Success) {
                runCatching {
                    activityLogRecorder.recordImageSave(
                        screenId = "browser",
                        screenName = "浏览器",
                        description = "导出网页截图: ${_state.value.currentTitle.ifEmpty { _state.value.currentUrl }}",
                        fileUri = fileUri.toString(),
                        fileName = getExportFileName() + ".png",
                        savePath = result.savingPath.orEmpty()
                    )
                }
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
        _state.update {
            it.copy(
                tabs = listOf(defaultTab),
                activeTabId = defaultTab.id,
                bookmarks = emptyList(),
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
            initialUrl: String = ""
        ): BrowserComponent
    }
}
