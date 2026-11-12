/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
