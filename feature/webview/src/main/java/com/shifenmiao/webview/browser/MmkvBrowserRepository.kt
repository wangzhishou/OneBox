package com.shifenmiao.webview.browser

import com.tencent.mmkv.MMKV
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MmkvBrowserRepository @Inject constructor() : BrowserRepository {

    private val mmkv: MMKV = MMKV.mmkvWithID(MMKV_ID)
    private val json = Json { ignoreUnknownKeys = true }

    override fun loadTabs(): List<BrowserTab> {
        val data = mmkv.decodeString(KEY_TABS) ?: return emptyList()
        return runCatching { json.decodeFromString<List<BrowserTab>>(data) }.getOrNull() ?: emptyList()
    }

    override fun saveTabs(tabs: List<BrowserTab>) {
        mmkv.encode(KEY_TABS, json.encodeToString(tabs))
    }

    override fun loadBookmarks(): List<BookmarkItem> {
        val data = mmkv.decodeString(KEY_BOOKMARKS) ?: return emptyList()
        return runCatching { json.decodeFromString<List<BookmarkItem>>(data) }.getOrNull() ?: emptyList()
    }

    override fun saveBookmarks(bookmarks: List<BookmarkItem>) {
        mmkv.encode(KEY_BOOKMARKS, json.encodeToString(bookmarks))
    }

    override fun loadBookmarkFolders(): List<BookmarkFolder> {
        val data = mmkv.decodeString(KEY_BOOKMARK_FOLDERS) ?: return emptyList()
        return runCatching { json.decodeFromString<List<BookmarkFolder>>(data) }.getOrNull() ?: emptyList()
    }

    override fun saveBookmarkFolders(folders: List<BookmarkFolder>) {
        mmkv.encode(KEY_BOOKMARK_FOLDERS, json.encodeToString(folders))
    }

    override fun loadHistory(): List<HistoryItem> {
        val data = mmkv.decodeString(KEY_HISTORY)
        val list = data?.let {
            runCatching { json.decodeFromString<List<HistoryItem>>(it) }.getOrNull()
        }
        return if (list.isNullOrEmpty()) {
            defaultHistory.also { saveHistory(it) }
        } else {
            list
        }
    }

    override fun addHistory(item: HistoryItem, maxSize: Int) {
        val current = loadHistory().toMutableList()
        current.removeAll { it.url == item.url }
        current.add(0, item)
        if (current.size > maxSize) {
            current.subList(maxSize, current.size).clear()
        }
        mmkv.encode(KEY_HISTORY, json.encodeToString(current))
    }

    override fun saveHistory(history: List<HistoryItem>) {
        mmkv.encode(KEY_HISTORY, json.encodeToString(history))
    }

    override fun clearHistory() {
        mmkv.removeValueForKey(KEY_HISTORY)
    }

    override fun loadSettings(): BrowserSettings {
        val data = mmkv.decodeString(KEY_SETTINGS) ?: return BrowserSettings()
        return runCatching { json.decodeFromString<BrowserSettings>(data) }.getOrNull() ?: BrowserSettings()
    }

    override fun saveSettings(settings: BrowserSettings) {
        mmkv.encode(KEY_SETTINGS, json.encodeToString(settings))
    }

    override fun clearAll() {
        mmkv.removeValuesForKeys(
            arrayOf(KEY_TABS, KEY_BOOKMARKS, KEY_BOOKMARK_FOLDERS, KEY_HISTORY, KEY_SETTINGS)
        )
    }

    companion object {
        private const val MMKV_ID = "web_browser"
        private const val KEY_TABS = "browser_tabs"
        private const val KEY_BOOKMARKS = "browser_bookmarks"
        private const val KEY_BOOKMARK_FOLDERS = "browser_bookmark_folders"
        private const val KEY_HISTORY = "browser_history"
        private const val KEY_SETTINGS = "browser_settings"

        private val defaultHistory: List<HistoryItem>
            get() {
                val now = System.currentTimeMillis()
                return listOf(
                    HistoryItem("default_1", "https://www.baidu.com", "百度一下，你就知道", "https://www.baidu.com".toFaviconUrl(), now),
                    HistoryItem("default_2", "https://www.bilibili.com", "哔哩哔哩", "https://www.bilibili.com".toFaviconUrl(), now - 1000),
                    HistoryItem("default_3", "https://www.zhihu.com", "知乎", "https://www.zhihu.com".toFaviconUrl(), now - 2000),
                    HistoryItem("default_4", "https://www.weibo.com", "微博", "https://www.weibo.com".toFaviconUrl(), now - 3000),
                    HistoryItem("default_5", "https://www.jd.com", "京东", "https://www.jd.com".toFaviconUrl(), now - 4000),
                    HistoryItem("default_6", "https://www.taobao.com", "淘宝", "https://www.taobao.com".toFaviconUrl(), now - 5000),
                    HistoryItem("default_7", "https://news.qq.com", "腾讯新闻", "https://news.qq.com".toFaviconUrl(), now - 6000),
                    HistoryItem("default_8", "https://www.163.com", "网易", "https://www.163.com".toFaviconUrl(), now - 7000),
                )
            }
    }
}
