package com.wanbaohe.media.playback

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.TextureView
import androidx.annotation.MainThread
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.media.domain.MediaEntry
import com.wanbaohe.media.domain.MediaKind
import com.wanbaohe.media.domain.MediaPlaybackController
import com.wanbaohe.media.domain.PlaybackFailure
import com.wanbaohe.media.domain.PlaybackMode
import com.wanbaohe.media.domain.PlaybackState
import com.wanbaohe.media.domain.isKnownUnsupported
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 唯一的播放内核（音频与视频共用同一个 ExoPlayer）。
 *
 * 生命周期跟着进程走：[openQueue] 之后即使播放页销毁，音频仍在后台放（前台服务挂着
 * 通知栏控制），只有 [stop] 才真正释放媒体。视频画面由页面通过 [attachVideoTexture]
 * 挂到 TextureView 上，离开页面时调 [detachVideoView] 把画面摘掉。
 *
 * 状态里带 [PlaybackState.kind]：音频页和视频页共享这一个内核，页面必须靠 kind
 * 判断"现在播的是不是我这一类"，否则打开视频页会把正在听的歌当成当前视频。
 */
@Singleton
class ExoPlaybackEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder,
) : MediaPlaybackController {

    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    // extraBufferCapacity 让 tryEmit 在页面还没开始收集时也不会丢事件
    private val _failures = MutableSharedFlow<PlaybackFailure>(extraBufferCapacity = 8)
    override val failures: SharedFlow<PlaybackFailure> = _failures.asSharedFlow()

    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var playerKind: MediaKind = MediaKind.AUDIO
    private var activeTextureView: TextureView? = null
    private var queueEntries: List<MediaEntry> = emptyList()
    private var errorStreak = 0

    private val scope = CoroutineScope(SupervisorJob() + dispatchersHolder.uiDispatcher)

    init {
        // 进度条是"看着像在动"就够的信息，500ms 一次比每秒重排更稳
        scope.launch {
            while (isActive) {
                exoPlayer?.takeIf { it.isPlaying }?.let(::syncProgress)
                delay(POSITION_TICK_MS)
            }
        }
    }

    // ── 播放控制 ─────────────────────────────────────────────────────────

    @MainThread
    override fun openQueue(
        entries: List<MediaEntry>,
        startIndex: Int,
        kind: MediaKind,
        autoPlay: Boolean,
    ) {
        if (entries.isEmpty()) return
        val player = ensurePlayer()
        playerKind = kind
        queueEntries = entries
        errorStreak = 0

        val items = entries.map { entry ->
            MediaItem.Builder()
                .setUri(entry.uri)
                .setMediaId(entry.uri)
                // 锁屏、车机、蓝牙耳机读的是 MediaItem 上的元数据，不是我们页面上的标题
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(entry.bestTitle)
                        .setArtist(entry.subtitleHint)
                        .setAlbumTitle(entry.album)
                        .build()
                )
                .build()
        }
        val index = startIndex.coerceIn(0, items.lastIndex)
        player.setMediaItems(items, index, 0L)
        player.prepare()
        // 通知点回来时 autoPlay=false：保留当前暂停状态，不替用户续播
        player.playWhenReady = autoPlay
        // 先把状态落到 state，再拉服务：服务启动时会立刻按 state 画通知
        syncCurrent(player)
        startPlaybackService()
    }

    @MainThread
    override fun open(entry: MediaEntry, kind: MediaKind) {
        openQueue(entries = listOf(entry), startIndex = 0, kind = kind, autoPlay = true)
    }

    @MainThread
    override fun playFromQueue(entry: MediaEntry) {
        val player = exoPlayer ?: return
        val index = _state.value.queue.indexOfFirst { it.uri == entry.uri }
        if (index < 0) {
            open(entry, _state.value.kind ?: playerKind)
            return
        }
        val isCurrent = player.currentMediaItemIndex == index &&
            player.currentMediaItem?.mediaId == entry.uri
        if (isCurrent) {
            // 已经在放的这首再点一次不应从头开始；但播完/暂停停在末尾时要从头再来
            if (!player.isPlaying) {
                if (player.playbackState == Player.STATE_ENDED || player.playbackState == Player.STATE_IDLE) {
                    player.seekTo(player.currentMediaItemIndex, 0L)
                    if (player.playbackState == Player.STATE_IDLE) player.prepare()
                }
                player.play()
            }
            return
        }
        errorStreak = 0
        player.seekTo(index, 0L)
        if (player.playbackState == Player.STATE_IDLE) player.prepare()
        player.play()
        syncCurrent(player)
    }

    @MainThread
    override fun togglePlayPause() {
        val player = exoPlayer ?: return
        when {
            // 出错后 play() 不会自愈，必须重新 prepare 才有解码器可用
            player.playerError != null -> {
                player.prepare()
                player.play()
            }
            // 播到末尾后按播放键应该从头再来
            player.playbackState == Player.STATE_ENDED -> {
                player.seekTo(player.currentMediaItemIndex, 0L)
                player.play()
            }
            player.isPlaying -> player.pause()
            else -> player.play()
        }
    }

    @MainThread
    override fun seekTo(positionMs: Long) {
        val player = exoPlayer ?: return
        player.seekTo(positionMs.coerceAtLeast(0L))
        syncProgress(player)
    }

    @MainThread
    override fun seekToFraction(fraction: Float) {
        val player = exoPlayer ?: return
        val duration = player.duration.takeIf { it > 0L && it != C.TIME_UNSET } ?: return
        seekTo((duration * fraction.coerceIn(0f, 1f)).toLong())
    }

    @MainThread
    override fun next() {
        val player = exoPlayer ?: return
        if (player.hasNextMediaItem()) player.seekToNextMediaItem()
    }

    /**
     * 上一首：播过半首时先回到本首开头，否则切上一首。
     *
     * 和系统蓝牙按键、通知栏的行为保持一致——用户按"上一首"多数是想重听当前这首。
     */
    @MainThread
    override fun previous() {
        val player = exoPlayer ?: return
        val shouldRestart = player.currentPosition > RESTART_THRESHOLD_MS ||
            !player.hasPreviousMediaItem()
        if (shouldRestart) {
            player.seekTo(player.currentMediaItemIndex, 0L)
        } else {
            player.seekToPreviousMediaItem()
        }
    }

    @MainThread
    override fun setMode(mode: PlaybackMode) {
        val player = exoPlayer ?: return
        when (mode) {
            PlaybackMode.SEQUENTIAL -> {
                player.shuffleModeEnabled = false
                // 顺序播放 = 放完列表就停，不是无限循环
                player.repeatMode = Player.REPEAT_MODE_OFF
            }

            PlaybackMode.REPEAT_ONE -> {
                player.shuffleModeEnabled = false
                player.repeatMode = Player.REPEAT_MODE_ONE
            }

            PlaybackMode.SHUFFLE -> {
                player.repeatMode = Player.REPEAT_MODE_ALL
                player.shuffleModeEnabled = true
            }
        }
        _state.value = _state.value.copy(mode = currentMode(player))
    }

    @MainThread
    override fun stop() {
        exoPlayer?.let { player ->
            player.stop()
            player.clearMediaItems()
            player.clearVideoSurface()
        }
        queueEntries = emptyList()
        errorStreak = 0
        _state.value = PlaybackState()
        MediaPlaybackService.stop(context)
    }

    // ── 视频画面 ─────────────────────────────────────────────────────────

    @MainThread
    override fun attachVideoTexture(view: TextureView) {
        activeTextureView = view
        ensurePlayer().setVideoTextureView(view)
    }

    @MainThread
    override fun detachVideoView(view: TextureView) {
        if (activeTextureView !== view) return
        activeTextureView = null
        exoPlayer?.clearVideoSurface()
    }

    /**
     * 媒体会话（锁屏卡片、蓝牙耳机按键、快捷设置媒体控件）。
     *
     * 挂在与前台服务同一个播放器上，系统就能把"正在播放的是什么"和
     * "按键该投给谁"对上号。
     */
    @MainThread
    fun ensureMediaSession(): MediaSession {
        mediaSession?.let { return it }
        val session = MediaSession.Builder(context, ensurePlayer()).build()
        mediaSession = session
        return session
    }

    /** 当前播放的是视频还是音频，通知等用例据此取文案。 */
    fun currentKind(): MediaKind = playerKind

    /** 队列里当前条目；页面用来还原标题/后缀。 */
    fun entryOf(uri: String?): MediaEntry? = uri?.let { id -> queueEntries.firstOrNull { it.uri == id } }

    fun release() {
        scope.cancel()
        mediaSession?.release()
        mediaSession = null
        exoPlayer?.release()
        exoPlayer = null
        queueEntries = emptyList()
    }

    // ── 内部实现 ─────────────────────────────────────────────────────────

    @MainThread
    private fun ensurePlayer(): ExoPlayer {
        exoPlayer?.let { return it }
        // 三星等机器的硬件解码器偶发初始化失败；打开回退后坏了的硬解会自动降到软解，
        // 不至于"只有声音没有画面"（黑屏时不会走 onPlayerError，回退是更稳的兜底）
        val renderersFactory = DefaultRenderersFactory(context)
            .setEnableDecoderFallback(true)
        val player = ExoPlayer.Builder(context, renderersFactory)
            .build()
            .apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(),
                    true,
                )
                setHandleAudioBecomingNoisy(true)
                // 息屏后 CPU 会睡，前台服务保不住解码线程；本地唤醒锁让音频连着放
                setWakeMode(C.WAKE_MODE_LOCAL)
                repeatMode = Player.REPEAT_MODE_OFF
                addListener(listener)
            }
        exoPlayer = player
        ensureMediaSession()
        return player
    }

    private val listener = object : Player.Listener {

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            errorStreak = 0
            exoPlayer?.let(::syncCurrent)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) errorStreak = 0
            _state.value = _state.value.copy(isPlaying = isPlaying, hasError = false)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            exoPlayer?.let { player ->
                _state.value = _state.value.copy(
                    isBuffering = playbackState == Player.STATE_BUFFERING,
                    isPlaying = player.isPlaying,
                    isEnded = playbackState == Player.STATE_ENDED,
                )
                syncProgress(player)
            }
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            _state.value = _state.value.copy(
                videoWidth = videoSize.width,
                videoHeight = videoSize.height,
                hasVideo = videoSize.width > 0 && videoSize.height > 0,
            )
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            exoPlayer?.let { _state.value = _state.value.copy(mode = currentMode(it)) }
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            exoPlayer?.let { _state.value = _state.value.copy(mode = currentMode(it)) }
        }

        /**
         * 一个文件坏了不能把整个队列卡死：自动跳到下一项再试，
         * 连续多次都失败才把错误摆到界面上，避免在坏条目上无限循环。
         *
         * 不管跳不跳，都把这次失败发出去：静默跳过正是"点了没反应"的来源。
         */
        override fun onPlayerError(error: PlaybackException) {
            val player = exoPlayer ?: return
            val canSkip = errorStreak < MAX_CONSECUTIVE_ERRORS &&
                player.mediaItemCount > 1 &&
                player.hasNextMediaItem()
            publishFailure(error, skipped = canSkip)
            if (canSkip) {
                errorStreak++
                player.seekToNextMediaItem()
                player.prepare()
                player.play()
                return
            }
            _state.value = _state.value.copy(isPlaying = false, hasError = true)
        }
    }

    /**
     * 把失败归到"当前这条"上。
     *
     * 用播放器的 currentMediaItemIndex 取队列条目：onPlayerError 时索引还停在
     * 出错的那一项上（跳过发生在之后）。拿不到条目也照样发，页面至少能提示一次。
     */
    private fun publishFailure(error: PlaybackException, skipped: Boolean) {
        val mediaId = exoPlayer?.currentMediaItem?.mediaId
        val entry = queueEntries.firstOrNull { it.uri == mediaId }
            ?: mediaId?.let { MediaEntry(uri = it, displayName = it.substringAfterLast('/')) }
        val unsupported = entry?.let { playerKind.isKnownUnsupported(it.displayName) } == true ||
                error.errorCode == PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED ||
                error.errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED
        runCatching {
            _failures.tryEmit(
                PlaybackFailure(
                    entry = entry,
                    kind = playerKind,
                    unsupported = unsupported,
                    skipped = skipped,
                    message = error.errorCodeName,
                )
            )
        }
    }

    private fun syncProgress(player: ExoPlayer) {
        val duration = player.duration.takeIf { it > 0L && it != C.TIME_UNSET } ?: 0L
        _state.value = _state.value.copy(
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = if (duration > 0L) duration else _state.value.durationMs,
        )
    }

    /**
     * 切歌后重建状态。
     *
     * 画面尺寸归零，等新视频的 onVideoSizeChanged 再填：如果不归零，
     * 从"上一个视频"切到音频时旧宽高会留在 state 里，画中画就会按视频尺寸误触发。
     */
    private fun syncCurrent(player: ExoPlayer) {
        val mediaItem = player.currentMediaItem
        val uri = mediaItem?.mediaId ?: mediaItem?.localConfiguration?.uri?.toString()
        _state.value = PlaybackState(
            mediaUri = uri,
            kind = playerKind,
            queue = queueEntries,
            currentIndex = player.currentMediaItemIndex,
            isPlaying = player.isPlaying,
            isBuffering = player.playbackState == Player.STATE_BUFFERING,
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = player.duration.takeIf { it > 0L && it != C.TIME_UNSET } ?: 0L,
            mode = currentMode(player),
            queueSize = player.mediaItemCount,
            isEnded = player.playbackState == Player.STATE_ENDED,
        )
        updateSessionActivity(uri)
    }

    /**
     * 让系统媒体中心（锁屏/快捷设置）点进去能回到播放页，而不是打开 App 首页。
     *
     * 深链带 `play=0`：用户暂停后点系统卡片只是回到页面，不会自动续播。
     */
    private fun updateSessionActivity(uri: String?) {
        val session = mediaSession ?: return
        if (uri.isNullOrBlank()) return
        val page = if (playerKind == MediaKind.VIDEO) "VideoPlayer" else "AudioPlayer"
        val deepLink = "onebox://screen/$page?url=${Uri.encode(uri)}&play=0"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
            .setClassName(context.packageName, APP_ACTIVITY)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context,
            SESSION_ACTIVITY_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        runCatching { session.setSessionActivity(pendingIntent) }
    }

    private fun currentMode(player: Player): PlaybackMode = when {
        player.shuffleModeEnabled -> PlaybackMode.SHUFFLE
        player.repeatMode == Player.REPEAT_MODE_ONE -> PlaybackMode.REPEAT_ONE
        else -> PlaybackMode.SEQUENTIAL
    }

    private fun startPlaybackService() {
        MediaPlaybackService.start(context)
    }

    internal companion object {
        const val POSITION_TICK_MS = 500L

        /** 按"上一首"时超过这个位置就先重播当前曲目，和主流播放器一致 */
        const val RESTART_THRESHOLD_MS = 3_000L

        /** 连续跳过这么多个坏条目后不再自动跳，避免在坏队列里空转 */
        const val MAX_CONSECUTIVE_ERRORS = 5

        const val SESSION_ACTIVITY_REQUEST_CODE = 0x4D454449

        /** 宿主 Activity（debug/release 必须一致，包名分开） */
        const val APP_ACTIVITY = "com.shifenmiao.app.AppActivity"
    }
}
