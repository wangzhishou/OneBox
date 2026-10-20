package com.wanbaohe.media.component

import android.content.ClipData
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.shifenmiao.base.pip.VideoPipState
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.media.R
import com.wanbaohe.media.domain.MediaDeviceLibrarySource
import com.wanbaohe.media.domain.MediaEntry
import com.wanbaohe.media.domain.MediaEntryResolver
import com.wanbaohe.media.domain.MediaFavoritesRepository
import com.wanbaohe.media.domain.MediaKind
import com.wanbaohe.media.domain.matchesFileName
import com.wanbaohe.media.domain.MediaLibraryRepository
import com.wanbaohe.media.domain.MediaMetadata
import com.wanbaohe.media.domain.MediaMetadataSource
import com.wanbaohe.media.domain.MediaPermissionChecker
import com.wanbaohe.media.domain.MediaPlaybackController
import com.wanbaohe.media.domain.PlaybackMode
import com.wanbaohe.media.domain.PlaybackState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * 视频/音乐页的 Decompose 组件。
 *
 * 一个组件同时管两个 Tab：播放页和媒体库。音频与视频共用一个播放内核，所以这里
 * 用 [PlaybackState.kind] 给自己划边界：内核在放别的类型时，本页只显示"空闲"，
 * 绝不去暂停/停止不属于自己的播放。
 *
 * 数据有三层：本机媒体库（MediaStore）、最近打开（公共访问表）、收藏。
 * 本机库需要读媒体权限，被拒时退回只看最近打开。
 */
class MediaPlayerComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val kind: MediaKind,
    @Assisted private val initialUri: Uri?,
    @Assisted private val initialPlaylist: List<String>,
    @Assisted private val initialAutoPlay: Boolean,
    @Assisted val onGoBack: () -> Unit,
    @ApplicationContext private val context: Context,
    private val libraryRepository: MediaLibraryRepository,
    private val deviceLibrary: MediaDeviceLibrarySource,
    private val permissionChecker: MediaPermissionChecker,
    private val favoritesRepository: MediaFavoritesRepository,
    private val metadataSource: MediaMetadataSource,
    private val entryResolver: MediaEntryResolver,
    private val playback: MediaPlaybackController,
    private val videoPipState: VideoPipState,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(
        MediaPlayerUiState(
            kind = kind,
            tab = if (initialUri == null) MediaTab.LIBRARY else MediaTab.PLAYER,
            hasMediaPermission = permissionChecker.hasAccess(kind),
        )
    )
    val uiState = _uiState.asStateFlow()

    /** 画中画窗口状态（AppActivity 的 PiP 回调写进 VideoPipState）。 */
    val inPictureInPicture = videoPipState.inPip

    /** 本机媒体是否已经加载过：加载后最近记录刷新不能把 library 覆盖回最近列表 */
    private var deviceEntriesLoaded = false
    private var recentsLoaded = false

    /** 因为切到库页而暂停的视频：切回播放页时自动续上 */
    private var pausedForLibraryTab = false
    private var notificationPermissionAsked = false

    init {
        observeRecents()
        observeFavorites()
        observePlayback()
        observePlaybackFailures()
        refreshDeviceLibrary()
        openInitialUri()

        componentContext.lifecycle.doOnDestroy {
            // 只停"本页自己在放的视频"；音频在后台放时打开过视频页，退出不能把歌停掉
            if (kind == MediaKind.VIDEO && _uiState.value.playback.kind == MediaKind.VIDEO) {
                playback.stop()
            }
            videoPipState.clear(this@MediaPlayerComponent)
        }
    }

    // ── 播放动作 ─────────────────────────────────────────────────────────

    /**
     * 点列表项。
     *
     * 已经在播放器队列里的条目按"切歌"处理（同一首不重头放，换一首直接跳过去），
     * 不在队列里才用当前库重建队列。
     */
    fun play(entry: MediaEntry) {
        val engineQueue = _uiState.value.playback.queue
        if (engineQueue.any { it.uri == entry.uri }) {
            playback.playFromQueue(entry)
        } else {
            val library = _uiState.value.library
            val index = library.indexOfFirst { it.uri == entry.uri }
            if (index >= 0) {
                playback.openQueue(library, index, kind)
            } else {
                playback.open(entry, kind)
            }
        }
        pausedForLibraryTab = false
        _uiState.value = _uiState.value.copy(
            tab = MediaTab.PLAYER,
            current = entry,
            showQueueSheet = false,
        )
        recordOpened(entry)
        loadMetadata(entry)
        askNotificationPermissionIfNeeded()
    }

    /** 系统选择器选完文件：拿到持久读权限再播，否则重进 App 就打不开了。 */
    fun onPickedUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        uris.forEach { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
        }
        componentScope.launch {
            val entries = uris.mapNotNull { entryResolver.resolve(it.toString()) }
            if (entries.isEmpty()) return@launch
            entries.forEach { libraryRepository.recordOpened(it) }
            playback.openQueue(entries, 0, kind)
            pausedForLibraryTab = false
            _uiState.value = _uiState.value.copy(
                tab = MediaTab.PLAYER,
                current = entries.first(),
                isLoading = false,
            )
            loadMetadata(entries.first())
            askNotificationPermissionIfNeeded()
        }
    }

    fun onPickedUri(uri: Uri) = onPickedUris(listOf(uri))

    /**
     * 外部入口（最近文件/Intent/深链）打开时的统一入口。
     *
     * [playlist] 是同目录的同类媒体；有它时"下一首"才有意义。
     * [autoPlay] = false 用于通知/锁屏回到播放页：只把页面接上，不替用户续播。
     */
    fun openUri(
        uriString: String,
        autoPlay: Boolean = true,
        playlist: List<String> = emptyList(),
    ) {
        componentScope.launch {
            val entry = entryResolver.resolve(uriString)
                ?: MediaEntry(
                    uri = uriString,
                    displayName = uriString.substringAfterLast('/').ifBlank { uriString },
                )
            libraryRepository.recordOpened(entry)
            _uiState.value = _uiState.value.copy(
                tab = MediaTab.PLAYER,
                current = entry,
                isLoading = false,
            )
            loadMetadata(entry)

            // 通知点回来：播放器里已经是这首时什么都不做，保持用户离开时的暂停状态
            val engineState = playback.state.value
            if (!autoPlay && engineState.kind == kind && engineState.mediaUri == entry.uri) {
                return@launch
            }

            val resolvedPlaylist = playlist
                .filter { it != uriString }
                .take(MAX_PLAYLIST_SIZE)
                .mapNotNull { uri ->
                    runCatching { entryResolver.resolve(uri) }.getOrNull()
                } + entry
            val queue = if (playlist.isNotEmpty()) {
                resolvedPlaylist.distinctBy { it.uri }
            } else {
                _uiState.value.library.takeIf { it.any { item -> item.uri == entry.uri } }
                    ?: listOf(entry)
            }
            val index = queue.indexOfFirst { it.uri == entry.uri }.coerceAtLeast(0)
            playback.openQueue(queue, index, kind, autoPlay)
            pausedForLibraryTab = false
            if (autoPlay) askNotificationPermissionIfNeeded()
        }
    }

    /** 本机库的"播放全部"：从第一首开始，队列就是当前筛选后的列表。 */
    fun playAll() {
        val queue = _uiState.value.visibleLibrary
        if (queue.isEmpty()) return
        playback.openQueue(queue, 0, kind, autoPlay = true)
        pausedForLibraryTab = false
        val first = queue.first()
        _uiState.value = _uiState.value.copy(tab = MediaTab.PLAYER, current = first)
        loadMetadata(first)
        recordOpened(first)
        askNotificationPermissionIfNeeded()
    }

    /** App 内真正停止播放（不只是暂停），同时收掉通知。 */
    fun stopPlayback() {
        playback.stop()
        pausedForLibraryTab = false
        videoPipState.clear(this)
        _uiState.value = _uiState.value.copy(current = null, metadata = MediaMetadata())
    }

    fun togglePlayPause() = playback.togglePlayPause()

    fun seekToFraction(fraction: Float) = playback.seekToFraction(fraction)

    fun next() = playback.next()

    fun previous() = playback.previous()

    fun setMode(mode: PlaybackMode) {
        playback.setMode(mode)
        _uiState.value = _uiState.value.copy(showModeSheet = false)
    }

    fun toggleFavorite() {
        _uiState.value.current?.let(::toggleFavorite)
    }

    /** 列表行的收藏：行的条目不一定是"当前播放"，所以单独收一个入参。 */
    fun toggleFavorite(entry: MediaEntry) {
        favoritesRepository.toggle(entry)
    }

    fun isFavorite(entry: MediaEntry): Boolean = entry.uri in _uiState.value.favorites

    // ── 页面状态 ─────────────────────────────────────────────────────────

    fun selectTab(tab: MediaTab) {
        if (_uiState.value.tab == tab) return
        if (kind == MediaKind.VIDEO) {
            // 视频切去库页等于离开画面，先暂停；切回来时把刚才暂停的续上，不静默留个暂停状态
            if (tab == MediaTab.LIBRARY && _uiState.value.playback.isPlaying) {
                playback.togglePlayPause()
                pausedForLibraryTab = true
            } else if (tab == MediaTab.PLAYER && pausedForLibraryTab) {
                pausedForLibraryTab = false
                playback.togglePlayPause()
            }
        }
        _uiState.value = _uiState.value.copy(tab = tab)
    }

    fun setFilter(filter: MediaFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
    }

    fun showQueue(show: Boolean) {
        _uiState.value = _uiState.value.copy(showQueueSheet = show)
    }

    fun showModeSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showModeSheet = show)
    }

    fun showInfo(show: Boolean) {
        _uiState.value = _uiState.value.copy(showInfoDialog = show)
    }

    fun setFullscreen(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isFullscreen = enabled)
    }

    fun showClearRecentDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showClearRecentDialog = show)
    }

    fun clearRecent() {
        _uiState.value = _uiState.value.copy(showClearRecentDialog = false)
        componentScope.launch { libraryRepository.clearRecent(kind) }
    }

    // ── 本机媒体权限 ─────────────────────────────────────────────────────

    /** 页面拉起系统权限框时需要的权限列表。 */
    fun requiredPermissions(): List<String> = permissionChecker.requiredPermissions(kind)

    /** Android 13+ 的播放通知权限；低版本返回 null。 */
    fun notificationPermission(): String? = permissionChecker.notificationPermission()

    /** 用户点了授权入口后置一个标记，由页面去拉起系统权限框。 */
    fun requestMediaPermission() {
        _uiState.value = _uiState.value.copy(isPermissionRequestPending = true)
    }

    fun onMediaPermissionResult(granted: Boolean) {
        _uiState.value = _uiState.value.copy(isPermissionRequestPending = false)
        if (granted) refreshDeviceLibrary()
        else _uiState.value = _uiState.value.copy(hasMediaPermission = false)
    }

    /**
     * 页面被别的页面盖住/离开组合时调用。
     *
     * 视频页还留在 Decompose 栈里但仍登记着画中画的话，用户按 Home 会进一个
     * 显示"别的页面"的小窗；离开画面就暂停并清掉登记，音频则继续后台播放。
     */
    fun onScreenLeft() {
        if (kind != MediaKind.VIDEO) return
        if (_uiState.value.playback.isPlaying) playback.togglePlayPause()
        // 这次暂停是"页面离开"造成的，不是切到库页；回来时不能被 selectTab 自动续播
        pausedForLibraryTab = false
        videoPipState.clear(this)
    }

    fun requestNotificationPermission() {
        _uiState.value = _uiState.value.copy(isNotificationPermissionPending = true)
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        _uiState.value = _uiState.value.copy(isNotificationPermissionPending = false)
        if (granted) notificationPermissionAsked = true
    }

    private fun askNotificationPermissionIfNeeded() {
        if (notificationPermissionAsked) return
        if (permissionChecker.notificationPermission() == null) return
        if (permissionChecker.hasNotificationPermission()) return
        notificationPermissionAsked = true
        requestNotificationPermission()
    }

    /** 页面重启（例如权限设置页返回）时重新检查。 */
    fun refreshDeviceLibrary() {
        if (!permissionChecker.hasAccess(kind)) {
            _uiState.value = _uiState.value.copy(
                hasMediaPermission = false,
                isLoading = false,
                library = _uiState.value.recentEntries,
            )
            return
        }
        componentScope.launch {
            val entries = deviceLibrary.load(kind)
            deviceEntriesLoaded = true
            if (_uiState.value.kind != kind) return@launch
            _uiState.value = _uiState.value.copy(
                library = entries,
                hasMediaPermission = true,
                isLoading = isLibraryLoading(),
            )
        }
    }

    // ── 分享 / 外开 ──────────────────────────────────────────────────────

    fun share() = share(_uiState.value.current)

    fun share(entry: MediaEntry?) {
        val uri = entry?.uri?.toUriOrNull() ?: return
        runCatching {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = context.contentResolver.getType(uri) ?: mimeTypeOf(entry)
                putExtra(Intent.EXTRA_STREAM, uri)
                // 没有 ClipData 时部分接收方拿不到临时读权限，附件会变成"无法读取"
                clipData = ClipData.newRawUri(entry.bestTitle, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(intent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    /** "用其他应用打开"：排除本 App，避免选回自己又叠一层播放页。 */
    fun openWithOtherApp() = openWithOtherApp(_uiState.value.current)

    fun openWithOtherApp(entry: MediaEntry?) {
        val uri = entry?.uri?.toUriOrNull() ?: return
        runCatching {
            val type = context.contentResolver.getType(uri) ?: mimeTypeOf(entry)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, type)
                clipData = ClipData.newRawUri(entry.bestTitle, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(
                Intent.createChooser(intent, null)
                    .putParcelableArrayListExtra(
                        Intent.EXTRA_EXCLUDE_COMPONENTS,
                        arrayListOf(ComponentName(context, SELF_ACTIVITY)),
                    )
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    /** 列表缩略图/封面：页面用 produceState 调它，解码与采样都在数据层。 */
    suspend fun artwork(entry: MediaEntry, maxSize: Int) =
        metadataSource.artwork(entry, maxSize)

    /** 列表项的时长徽标/作者：同样按需读，读过的会被数据层缓存。 */
    suspend fun metadata(entry: MediaEntry) = metadataSource.metadata(entry)

    /** 把视频画面接到 Compose 里的 TextureView；播放器本身仍归数据层持有。 */
    fun attachVideoTexture(view: android.view.TextureView) = playback.attachVideoTexture(view)

    fun detachVideoTexture(view: android.view.TextureView) = playback.detachVideoView(view)

    // ── 数据订阅 ─────────────────────────────────────────────────────────

    /**
     * 收藏里"既不在本机库、也不在最近列表"的条目。
     *
     * 只靠库过滤的话，文件被移出最近 200 条或清空记录后收藏就"消失"了；这里用收藏时
     * 记下的显示名补一条，至少还能点开（文件被删掉时播放会报错，符合预期）。
     */
    private fun buildFavoriteFallbacks(favorites: Set<String>): List<MediaEntry> {
        if (favorites.isEmpty()) return emptyList()
        val known = (_uiState.value.library + _uiState.value.recentEntries)
            .mapTo(HashSet()) { it.uri }
        return favorites.asSequence()
            .filter { it !in known }
            .mapNotNull { uri ->
                val name = favoritesRepository.favoriteNames.value[uri] ?: return@mapNotNull null
                if (!kind.matchesFileName(name)) return@mapNotNull null
                MediaEntry(uri = uri, displayName = name)
            }
            .toList()
    }

    private fun observeRecents() {
        libraryRepository.observeRecent(kind)
            .onEach { entries ->
                recentsLoaded = true
                _uiState.value = _uiState.value.copy(
                    recentEntries = entries,
                    // 本机库加载完成后，最近记录只供"最近"区块使用，不能覆盖"全部"
                    library = if (deviceEntriesLoaded) _uiState.value.library else entries,
                    // 权限已给但 MediaStore 还没回来时仍然算 loading，避免空态闪一下
                    isLoading = isLibraryLoading(),
                )
            }
            .launchIn(componentScope)
    }

    private fun isLibraryLoading(): Boolean {
        if (!_uiState.value.hasMediaPermission) return false
        return !deviceEntriesLoaded || !recentsLoaded
    }

    private fun observeFavorites() {
        favoritesRepository.favorites
            .onEach { favorites ->
                _uiState.value = _uiState.value.copy(
                    favorites = favorites,
                    favoriteFallbacks = buildFavoriteFallbacks(favorites),
                )
            }
            .launchIn(componentScope)
        favoritesRepository.favoriteNames
            .onEach { _ ->
                _uiState.value = _uiState.value.copy(
                    favoriteFallbacks = buildFavoriteFallbacks(_uiState.value.favorites),
                )
            }
            .launchIn(componentScope)
    }

    private fun observePlayback() {
        playback.state
            .onEach { state ->
                val isOwnSession = state.kind == kind
                val visibleState = if (isOwnSession) {
                    state
                } else {
                    // 内核在放另一类媒体：本页展示为空闲，避免把正在放的歌当成"当前视频"
                    state.copy(
                        mediaUri = null,
                        isPlaying = false,
                        isBuffering = false,
                        positionMs = 0L,
                        durationMs = 0L,
                        queue = emptyList(),
                        currentIndex = 0,
                        videoWidth = 0,
                        videoHeight = 0,
                        hasVideo = false,
                    )
                }
                val previous = _uiState.value
                _uiState.value = previous.copy(playback = visibleState)

                if (isOwnSession) {
                    syncCurrentEntry(previous, state)
                    syncPictureInPicture(state)
                } else if (kind == MediaKind.VIDEO) {
                    // 视频页不再登记画中画；只清自己的声明，不影响别的所有者
                    videoPipState.clear(this@MediaPlayerComponent)
                }
            }
            .launchIn(componentScope)
    }

    /**
     * 播放失败的处理。
     *
     * 一处失败可能被引擎"静默跳过"（队列里还有下一首），页面上就看不出任何提示；
     * 所以不管跳没跳都提示一次。已知不支持的格式顺手从"最近播放"里摘掉 ——
     * 留着只会让用户反复点进同一个放不了的文件。
     */
    private fun observePlaybackFailures() {
        playback.failures
            .onEach { failure ->
                if (failure.kind != kind) return@onEach
                val title = failure.entry?.bestTitle ?: failure.entry?.displayName
                when {
                    // 格式不支持：从"最近播放"里摘掉，别让用户反复点进同一个放不了的文件
                    failure.unsupported -> {
                        failure.entry?.let { entry ->
                            componentScope.launch { libraryRepository.forget(entry.uri) }
                        }
                        if (failure.skipped) {
                            AppToastHost.showToast(
                                message = if (title != null) {
                                    context.getString(R.string.media_unsupported_format, title)
                                } else {
                                    context.getString(R.string.media_unsupported_format_generic)
                                }
                            )
                        }
                    }
                    // 文件坏了/权限丢了：自动跳过的场合没人看得见，补一条提示；
                    // 没跳过时页面本身已经显示"播放不了"，不再叠一条 toast
                    failure.skipped -> AppToastHost.showToast(
                        message = context.getString(R.string.media_play_failed)
                    )
                }
            }
            .launchIn(componentScope)
    }

    private fun syncCurrentEntry(previous: MediaPlayerUiState, state: PlaybackState) {
        val uri = state.mediaUri ?: return
        if (previous.current?.uri == uri) return

        val known = state.queue.firstOrNull { it.uri == uri }
            ?: previous.library.firstOrNull { it.uri == uri }
            ?: previous.recentEntries.firstOrNull { it.uri == uri }
        if (known != null) {
            _uiState.value = _uiState.value.copy(current = known, metadata = MediaMetadata())
            loadMetadata(known)
            return
        }
        componentScope.launch {
            val resolved = entryResolver.resolve(uri) ?: return@launch
            if (_uiState.value.playback.mediaUri != uri) return@launch
            _uiState.value = _uiState.value.copy(current = resolved, metadata = MediaMetadata())
            loadMetadata(resolved)
        }
    }

    /**
     * 只有"本页的视频正在放 + 拿到画面尺寸"才登记画中画。
     *
     * 尺寸可能比打开页面晚几帧才到，所以这里跟随播放状态反复更新，
     * 暂停/停止时立刻清掉，AppActivity 那边只认这一份状态。
     */
    private fun syncPictureInPicture(state: PlaybackState) {
        if (kind != MediaKind.VIDEO || state.kind != MediaKind.VIDEO) return
        val width = state.videoWidth.takeIf { it > 0 } ?: _uiState.value.metadata.width
        val height = state.videoHeight.takeIf { it > 0 } ?: _uiState.value.metadata.height
        // 暂停时保留登记（isPlaying=false）：小窗里的播放/暂停按钮要还能用，
        // 但 AppActivity 只在 isPlaying=true 时才允许自动进入
        if (width > 0 && height > 0) {
            videoPipState.update(this, width, height, isPlaying = state.isPlaying)
        } else {
            videoPipState.clear(this)
        }
    }

    private fun loadMetadata(entry: MediaEntry) {
        componentScope.launch {
            val metadata = metadataSource.metadata(entry)
            if (_uiState.value.current?.uri != entry.uri) return@launch
            _uiState.value = _uiState.value.copy(metadata = metadata)
            syncPictureInPicture(_uiState.value.playback)
        }
    }

    private fun recordOpened(entry: MediaEntry) {
        componentScope.launch { libraryRepository.recordOpened(entry) }
    }

    private fun openInitialUri() {
        val uri = initialUri ?: return
        openUri(
            uriString = uri.toString(),
            autoPlay = initialAutoPlay,
            playlist = initialPlaylist,
        )
    }

    private fun String.toUriOrNull(): Uri? = runCatching { Uri.parse(this) }.getOrNull()

    private fun mimeTypeOf(entry: MediaEntry?): String = when {
        entry == null -> "*/*"
        kind == MediaKind.VIDEO -> "video/*"
        else -> "audio/*"
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            kind: MediaKind,
            uri: Uri?,
            playlist: List<String>,
            autoPlay: Boolean,
            onGoBack: () -> Unit,
        ): MediaPlayerComponent
    }

    private companion object {
        /** 分享/外开时要排除的宿主 Activity（debug 包同名类，两者都排）。 */
        const val SELF_ACTIVITY = "com.shifenmiao.app.AppActivity"

        /** 深链带来的播放列表上限，避免异常输入一次解析几百条 */
        const val MAX_PLAYLIST_SIZE = 200
    }
}
