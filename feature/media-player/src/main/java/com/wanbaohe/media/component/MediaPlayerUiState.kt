package com.wanbaohe.media.component

import com.wanbaohe.media.domain.MediaEntry
import com.wanbaohe.media.domain.MediaKind
import com.wanbaohe.media.domain.MediaMetadata
import com.wanbaohe.media.domain.PlaybackMode
import com.wanbaohe.media.domain.PlaybackState

/** 播放页 / 媒体库页 —— 原型底部两个 Tab。 */
enum class MediaTab {
    PLAYER,
    LIBRARY,
}

/** 媒体库顶部的筛选胶囊。 */
enum class MediaFilter {
    ALL,
    FAVORITES,
    RECENT,
}

/**
 * 媒体页的全部界面状态。
 *
 * [library] 是"全部视频/全部歌曲"（本机媒体，读不到权限时退回最近记录），
 * [recentEntries] 是"最近观看/最近播放"。两者分开存，筛选和搜索都要能同时覆盖。
 */
data class MediaPlayerUiState(
    val kind: MediaKind,
    val tab: MediaTab = MediaTab.LIBRARY,
    val library: List<MediaEntry> = emptyList(),
    val recentEntries: List<MediaEntry> = emptyList(),
    val favorites: Set<String> = emptySet(),
    val favoriteFallbacks: List<MediaEntry> = emptyList(),
    val current: MediaEntry? = null,
    val metadata: MediaMetadata = MediaMetadata(),
    val playback: PlaybackState = PlaybackState(),
    val filter: MediaFilter = MediaFilter.ALL,
    val isLoading: Boolean = true,
    val hasMediaPermission: Boolean = true,
    val isPermissionRequestPending: Boolean = false,
    val isNotificationPermissionPending: Boolean = false,
    val showQueueSheet: Boolean = false,
    val showModeSheet: Boolean = false,
    val showInfoDialog: Boolean = false,
    val showClearRecentDialog: Boolean = false,
    val isFullscreen: Boolean = false,
) {
    val isFavorite: Boolean get() = current != null && current.uri in favorites

    /** 当前媒体是否真的在放（标题区/播放按钮的状态来源）。 */
    val isPlaying: Boolean get() = playback.isPlaying

    val isBuffering: Boolean get() = playback.isBuffering

    /** 播放器的真实队列（不是当前页面的库）。 */
    val queue: List<MediaEntry> get() = playback.queue

    val mode: PlaybackMode get() = playback.mode

    val hasLibrary: Boolean get() = library.isNotEmpty() || recentEntries.isNotEmpty()

    private fun passesFilter(entry: MediaEntry): Boolean = when (filter) {
        // "最近"的数据源本身就是访问记录，不再叠时间窗口——
        // 否则一周没用过 App 时这个筛选会直接空列表
        MediaFilter.ALL, MediaFilter.RECENT -> true
        MediaFilter.FAVORITES -> entry.uri in favorites
    }

    /** 胶囊筛选之后的"全部"列表（没有搜索，顺序沿用数据源按时间排好的顺序）。 */
    val visibleLibrary: List<MediaEntry>
        get() {
            val source = when (filter) {
                MediaFilter.RECENT -> recentEntries
                // 收藏：本机库里的 + 已经不在库里的收藏兜底条目
                MediaFilter.FAVORITES -> (library + favoriteFallbacks).distinctBy { it.uri }
                else -> library
            }
            return source.filter(::passesFilter)
        }

    /**
     * 最近区块：和筛选保持同一份数据，避免横滑和下面列表对不上。
     *
     * 只在展示层按显示名去重：同一个文件常以 MediaStore URI 和文档 URI 各记一条，
     * 横滑里并排出现两张一模一样的缩略图很奇怪；"全部"列表仍保留完整条目。
     */
    val visibleRecents: List<MediaEntry>
        get() = recentEntries.filter(::passesFilter).distinctBy { it.displayName }

    /** 最近区块只放前几项，跟原型里四个大缩略图一致。 */
    val recentHighlights: List<MediaEntry> get() = visibleRecents.take(4)

}
