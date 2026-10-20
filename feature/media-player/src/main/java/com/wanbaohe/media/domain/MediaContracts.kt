package com.wanbaohe.media.domain

import android.graphics.Bitmap
import android.view.TextureView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * 最近播放/最近观看的数据源。
 *
 * 与文件浏览器共用同一张"最近访问"表，所以从任何入口打开过的音频/视频都会出现在
 * 媒体库的"最近播放"里，点开也走同一条播放链路。
 */
interface MediaLibraryRepository {

    /** 观察某一类媒体最近的打开记录（按时间倒序）。 */
    fun observeRecent(kind: MediaKind): Flow<List<MediaEntry>>

    /** 记录一次打开（写入公共最近访问表）。 */
    suspend fun recordOpened(entry: MediaEntry)

    /** 清空某一类媒体的最近记录（音频不会连带动视频的历史）。 */
    suspend fun clearRecent(kind: MediaKind)
}

/**
 * 收藏（原型里的心形按钮）。
 *
 * 只存 URI 集合，按文件身份而不是位置，文件改名后收藏会自然失效 —— 比留一条错记录好。
 */
interface MediaFavoritesRepository {
    val favorites: StateFlow<Set<String>>

    /** URI → 收藏时的显示名。文件被移出最近列表后，收藏页还能靠它显示条目。 */
    val favoriteNames: StateFlow<Map<String, String>>

    fun toggle(entry: MediaEntry)
}

/**
 * 本机媒体库（MediaStore）。
 *
 * 与"最近打开"分开：最近记录只包含用户经本 App 打开过的文件，而"全部视频/全部歌曲"
 * 应该是手机里真实存在的媒体。权限被拒时返回空表，页面退回只看最近记录。
 */
interface MediaDeviceLibrarySource {
    suspend fun load(kind: MediaKind): List<MediaEntry>
}

/** 读取本机媒体所需的运行时权限。 */
interface MediaPermissionChecker {

    /** API 33+ 是 READ_MEDIA_AUDIO/VIDEO，33 以下是 READ_EXTERNAL_STORAGE。 */
    fun requiredPermissions(kind: MediaKind): List<String>

    fun hasAccess(kind: MediaKind): Boolean

    /** Android 13+ 需要 POST_NOTIFICATIONS 才能显示播放通知；低版本恒为 true。 */
    fun hasNotificationPermission(): Boolean

    /** 需要申请的权限名；低版本返回 null。 */
    fun notificationPermission(): String?
}

/** 媒体文件元信息读取（标题/作者/时长/尺寸/封面帧）。 */
interface MediaMetadataSource {

    /** 读取一次元信息；失败返回空对象，不抛异常。 */
    suspend fun metadata(entry: MediaEntry): MediaMetadata

    /**
     * 取封面/首帧。
     *
     * 视频取一帧画面，音频取内嵌专辑图；都没有时返回 null，由界面画占位图。
     * [maxSize] 是期望的最大边长，实现侧据此采样，避免把 4K 帧塞进列表。
     */
    suspend fun artwork(entry: MediaEntry, maxSize: Int): Bitmap?
}

/** 把系统选择器/Intent 给的 URI 解析成可播放条目。 */
interface MediaEntryResolver {
    suspend fun resolve(uri: String): MediaEntry?
}

/**
 * 播放内核。
 *
 * 全 App 只有一个实例：页面销毁后音频继续放，重新进页面能接着播；
 * 视频画面通过 [attachVideoTexture] 交给 Compose 里的 TextureView。
 */
interface MediaPlaybackController {

    val state: StateFlow<PlaybackState>

    /**
     * 设置播放队列并从第 [startIndex] 个开始。
     *
     * [autoPlay] = false 时只 prepare 不自动播放（通知点回来不该把用户刚暂停的歌续上）。
     */
    fun openQueue(
        entries: List<MediaEntry>,
        startIndex: Int,
        kind: MediaKind,
        autoPlay: Boolean = true,
    )

    /** 单曲播放（队列只有它自己）。 */
    fun open(entry: MediaEntry, kind: MediaKind)

    /**
     * 从当前队列里选一首。
     *
     * 队列里已有这首时不重建队列：同一首再点一次只是继续播（不从头开始），
     * 换一首则跳到那一项，队列本身保持不变。
     */
    fun playFromQueue(entry: MediaEntry)

    fun togglePlayPause()

    fun seekTo(positionMs: Long)

    fun seekToFraction(fraction: Float)

    fun next()

    fun previous()

    fun setMode(mode: PlaybackMode)

    /** 停止并释放当前媒体（页面真正退出时用）。 */
    fun stop()

    /**
     * 把播放器的画面接到 [TextureView] 上。
     *
     * 用 TextureView 而不是 SurfaceView：画中画窗口里 SurfaceView 会被单独合成，
     * 缩进小窗后经常只剩一块黑底；TextureView 跟着 View 层级走，PiP 里能正常出画面。
     */
    fun attachVideoTexture(view: TextureView)

    /**
     * 视频页离开时清掉画面，避免播放器一直持有已销毁的 View。
     *
     * 只对"当前真正挂在播放器上的那个 View"生效：进入画中画时新旧两个画面节点
     * 会在同一帧里一进一出，旧节点的清理不能把新节点刚接好的画面又摘掉。
     */
    fun detachVideoView(view: TextureView)
}
