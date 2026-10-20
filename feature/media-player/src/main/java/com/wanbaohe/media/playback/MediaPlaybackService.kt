package com.wanbaohe.media.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.net.Uri
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.wanbaohe.media.R
import com.wanbaohe.media.domain.MediaKind
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * 后台播放的前台服务。
 *
 * 为什么不用 media3 的 MediaSessionService：它是"按播放状态自动进出前台"的，
 * 在页面之间快速切换（视频页销毁 stopService、紧接着音频页 startForegroundService）
 * 会出现服务已存在但没进前台的窗口期，系统 5 秒后直接给 App 抛
 * ForegroundServiceDidNotStartInTime 崩溃。这里改成自己控制：
 * onCreate 立刻 startForeground，播放状态变化只更新通知，停止时才 stopSelf —— 不存在窗口期。
 *
 * 媒体会话（锁屏/蓝牙耳机按键/快捷设置里的媒体卡片）由 [ExoPlaybackEngine] 里的
 * MediaSession 提供，和这个服务绑在同一个播放器上，所以通知只做本 App 的控制入口。
 */
@AndroidEntryPoint
class MediaPlaybackService : Service() {

    @Inject
    lateinit var engine: ExoPlaybackEngine

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var observeJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        // 必须在 5 秒内 startForeground，这里同步做完，不给系统超时的机会
        startForeground(NOTIFICATION_ID, buildNotification(snapshotOf()))
        observeJob = engine.state
            // 播放进度 500ms 跳一次，但通知内容一分钟都不变；只按真正影响通知的字段去重
            .map { snapshotOf() }
            .distinctUntilChanged()
            .onEach { snapshot ->
                if (snapshot.isEnded || snapshot.kind == null) {
                    // 队列自然播完 / 播放器空闲：收掉前台服务和通知，避免幽灵通知
                    stopForegroundNow()
                    stopSelf()
                    return@onEach
                }
                val manager = getSystemService(NotificationManager::class.java)
                manager?.notify(NOTIFICATION_ID, buildNotification(snapshot))
            }
            .launchIn(scope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TOGGLE -> engine.togglePlayPause()
            ACTION_NEXT -> engine.next()
            ACTION_PREVIOUS -> engine.previous()
            ACTION_STOP -> {
                engine.stop()
                stopForegroundNow()
                stopSelf()
                return START_NOT_STICKY
            }
        }
        // 不要 sticky：进程被杀后系统重启服务只会拿到空播放器，凭空弹一条幽灵通知
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        observeJob?.cancel()
        scope.cancel()
        stopForegroundNow()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── 通知 ─────────────────────────────────────────────────────────────

    private fun buildNotification(snapshot: NotificationSnapshot): Notification {
        val title = snapshot.title
        val isVideo = snapshot.kind == MediaKind.VIDEO

        // 点通知回到播放页本身，而不是停在首页：用深链带上当前媒体的 URI
        val mediaUri = snapshot.mediaUri
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            mediaDeepLink(mediaUri) ?: packageManager.getLaunchIntentForPackage(packageName)
            ?: Intent().setClassName(packageName, SELF_ACTIVITY),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_media_notification)
            .setContentTitle(title)
            .setContentText(snapshot.text)
            .setContentIntent(contentIntent)
            .setOngoing(snapshot.isPlaying)
            .setShowWhen(false)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .addAction(
                R.drawable.ic_media_notification_previous,
                getString(R.string.media_previous),
                servicePendingIntent(ACTION_PREVIOUS, 1),
            )
            .addAction(
                if (snapshot.isPlaying) {
                    R.drawable.ic_media_notification_pause
                } else {
                    R.drawable.ic_media_notification_play
                },
                getString(if (snapshot.isPlaying) R.string.media_pause else R.string.media_play),
                servicePendingIntent(ACTION_TOGGLE, 2),
            )
            .addAction(
                R.drawable.ic_media_notification_next,
                getString(R.string.media_next),
                servicePendingIntent(ACTION_NEXT, 3),
            )

        if (!snapshot.isPlaying) {
            builder.addAction(
                R.drawable.ic_media_notification_close,
                getString(R.string.media_close),
                servicePendingIntent(ACTION_STOP, 4),
            )
        }
        return builder.build()
    }

    /**
     * onebox://screen/VideoPlayer?url=<uri>&play=0：落回播放页并带回当前媒体。
     *
     * `play=0` 很关键：用户主动暂停后点通知只是想回 App，不能被强迫续播。
     */
    private fun mediaDeepLink(mediaUri: String?): Intent? {
        if (mediaUri.isNullOrBlank()) return null
        val page = if (engine.currentKind() == MediaKind.VIDEO) "VideoPlayer" else "AudioPlayer"
        val deepLink = "onebox://screen/$page?url=${Uri.encode(mediaUri)}&play=0"
        return Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
            .setClassName(packageName, SELF_ACTIVITY)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }

    private fun servicePendingIntent(action: String, requestCode: Int): PendingIntent =
        PendingIntent.getService(
            this,
            requestCode,
            Intent(this, MediaPlaybackService::class.java).setAction(action),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.media_playback_channel),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                setShowBadge(false)
                description = getString(R.string.media_playback_channel_description)
            }
        )
    }

    @Suppress("DEPRECATION")
    private fun stopForegroundNow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
    }

    /** 通知真正依赖的字段：进度变化不应该触发通知重建。 */
    private data class NotificationSnapshot(
        val title: String,
        val text: String,
        val isPlaying: Boolean,
        val isEnded: Boolean,
        val kind: MediaKind?,
        val mediaUri: String?,
    ) {
    }

    /** 从播放器状态里摘出"只影响通知"的部分（标题/副标题/播放态/是否播完）。 */
    private fun snapshotOf(): NotificationSnapshot {
        val state = engine.state.value
        val entry = engine.entryOf(state.mediaUri)
        val title = entry?.bestTitle?.takeIf { it.isNotBlank() }
            ?: state.mediaUri?.substringAfterLast('/')?.substringBeforeLast('.')
            ?: getString(R.string.media_now_playing_hint)
        val text = entry?.subtitleHint
            ?: getString(
                if (state.kind == MediaKind.VIDEO) R.string.media_video_type else R.string.media_audio_type
            )
        return NotificationSnapshot(
            title = title,
            text = text,
            isPlaying = state.isPlaying,
            isEnded = state.isEnded,
            kind = state.kind,
            mediaUri = state.mediaUri,
        )
    }

    companion object {
        private const val CHANNEL_ID = "media_playback"
        private const val NOTIFICATION_ID = 0x4D45

        const val ACTION_TOGGLE = "com.wanbaohe.media.ACTION_TOGGLE"
        private const val ACTION_NEXT = "com.wanbaohe.media.ACTION_NEXT"
        private const val ACTION_PREVIOUS = "com.wanbaohe.media.ACTION_PREVIOUS"
        private const val ACTION_STOP = "com.wanbaohe.media.ACTION_STOP"

        private const val SELF_ACTIVITY = "com.shifenmiao.app.AppActivity"

        /** 开播/继续时拉起前台服务；重复调用是幂等的。 */
        fun start(context: Context) {
            runCatching {
                val intent = Intent(context, MediaPlaybackService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }

        /** 停止播放并撤掉通知（停止不需要走 startService，避免后台启动限制）。 */
        fun stop(context: Context) {
            runCatching { context.stopService(Intent(context, MediaPlaybackService::class.java)) }
        }
    }
}
