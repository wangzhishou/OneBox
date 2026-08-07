package com.shifenmiao.webview.browser

interface BrowserRepository {
    fun loadTabs(): List<BrowserTab>
    fun saveTabs(tabs: List<BrowserTab>)

    fun loadBookmarks(): List<BookmarkItem>
    fun saveBookmarks(bookmarks: List<BookmarkItem>)

    fun loadBookmarkFolders(): List<BookmarkFolder>
    fun saveBookmarkFolders(folders: List<BookmarkFolder>)

    fun loadHistory(): List<HistoryItem>
    fun addHistory(item: HistoryItem, maxSize: Int = DEFAULT_MAX_HISTORY)
    fun saveHistory(history: List<HistoryItem>)
    fun clearHistory()

    fun loadSettings(): BrowserSettings
    fun saveSettings(settings: BrowserSettings)

    fun clearAll()

    companion object {
        const val DEFAULT_MAX_HISTORY = 500
    }
}
