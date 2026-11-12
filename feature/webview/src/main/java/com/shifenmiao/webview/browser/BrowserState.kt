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
    val settings: BrowserSettings = BrowserSettings(),
    /**
     * 当前正在查看的网址收藏（从收藏列表/深链进来时非空）。
     * 非空即「收藏查看器」形态：底栏主页槽位换成编辑入口。
     */
    val viewingBookmark: BookmarkContext? = null,
) {
    val isCurrentPageBookmarked: Boolean
        get() = currentUrl.isNotEmpty() && bookmarks.any { it.url == currentUrl }

    val isHomePage: Boolean
        get() = currentUrl.isBlank() || currentUrl == "about:blank"

    /** 收藏查看器：底栏显示编辑入口而不是主页。 */
    val isBookmarkViewer: Boolean
        get() = viewingBookmark != null

    /** 正在展示的是本地快照（离线），可一键切到线上原页。 */
    val isShowingSnapshot: Boolean
        get() = viewingBookmark?.let { it.hasSnapshot && it.showSnapshot } == true

    fun folderName(folderId: String): String {
        if (folderId.isEmpty()) return ""
        return bookmarkFolders.find { it.id == folderId }?.name ?: ""
    }

    enum class BrowserPage { Home, Tabs, Bookmarks, Settings }

    /** 收藏查看器上下文。 */
    data class BookmarkContext(
        val itemId: Int,
        val title: String,
        val url: String,
        val hasSnapshot: Boolean,
        /** 当前渲染的是快照（true）还是线上原页（false）。 */
        val showSnapshot: Boolean,
    )
}
