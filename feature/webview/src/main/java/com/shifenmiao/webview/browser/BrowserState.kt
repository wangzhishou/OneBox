package com.shifenmiao.webview.browser

data class BrowserState(
    val selectedPage: BrowserPage = BrowserPage.Home,
    val currentUrl: String = "",
    val currentTitle: String = "",
    val currentFaviconUrl: String = "",
    val isLoading: Boolean = false,
    val progress: Float = 1f,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val tabs: List<BrowserTab> = emptyList(),
    val activeTabId: String? = null,
    val bookmarks: List<BookmarkItem> = emptyList(),
    val bookmarkFolders: List<BookmarkFolder> = emptyList(),
    val history: List<HistoryItem> = emptyList(),
    val settings: BrowserSettings = BrowserSettings()
) {
    val isCurrentPageBookmarked: Boolean
        get() = currentUrl.isNotEmpty() && bookmarks.any { it.url == currentUrl }

    val isHomePage: Boolean
        get() = currentUrl.isBlank() || currentUrl == "about:blank"

    fun folderName(folderId: String): String {
        if (folderId.isEmpty()) return ""
        return bookmarkFolders.find { it.id == folderId }?.name ?: ""
    }

    enum class BrowserPage { Home, Tabs, Bookmarks, Settings }
}
