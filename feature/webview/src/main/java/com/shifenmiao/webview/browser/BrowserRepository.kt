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

    /** 书签迁移标记：旧 MMKV 书签是否已迁入 Room 网址收藏。 */
    fun isBookmarkMigrationDone(): Boolean
    fun markBookmarkMigrationDone()

    fun clearAll()

    companion object {
        const val DEFAULT_MAX_HISTORY = 500
    }
}
