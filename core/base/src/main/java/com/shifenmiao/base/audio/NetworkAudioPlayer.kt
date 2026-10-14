package com.shifenmiao.base.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.t8rin.logger.makeLog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 基础网络声音播放器。
 *
 * - 首次播放时将网络媒体文件下载到应用缓存目录；
 * - 后续相同 URL 直接复用本地缓存；
 * - 支持短音效播放和背景音乐循环播放；
 * - URL 可以指向常见音频文件，也可以指向包含音轨的视频文件。
 */
@Singleton
class NetworkAudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val cacheDir: File by lazy {
        File(context.cacheDir, CACHE_DIR_NAME).also { it.mkdirs() }
    }
    private val cacheMutex = Mutex()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var effectPlayer: MediaPlayer? = null
    private var backgroundPlayer: MediaPlayer? = null
    /** 页面还想听背景音乐。退后台只暂停，这个标记保持；离开页面或关掉声音才清掉。 */
    private var backgroundDesired = false
    /** [stopBackground] 递增。下载还没完成的播放看到代号变了就放弃，避免停掉之后又响起来。 */
    private var playEpoch = 0

    suspend fun playEffect(url: String) {
        val file = getCachedFile(url) ?: return
        withContext(Dispatchers.Main) {
            runCatching {
                effectPlayer?.release()
                effectPlayer = MediaPlayer().apply {
                    setAudioAttributes(effectAttributes)
                    setDataSource(file.absolutePath)
                    setOnCompletionListener { player ->
                        player.release()
                        if (effectPlayer === player) effectPlayer = null
                    }
                    prepare()
                    start()
                }
            }.onFailure { it.makeLog("NetworkAudioPlayer") }
        }
    }

    suspend fun playBackground(url: String) {
        val epoch = playEpoch
        val file = getCachedFile(url) ?: return
        withContext(Dispatchers.Main) {
            if (epoch != playEpoch) return@withContext
            runCatching {
                backgroundDesired = true
                val current = backgroundPlayer
                if (backgroundPlayerSource == url && current != null) {
                    if (!current.isPlaying && requestFocus()) current.start()
                    return@runCatching
                }
                releaseBackgroundPlayer()
                backgroundPlayerSource = url
                backgroundPlayer = MediaPlayer().apply {
                    setAudioAttributes(musicAttributes)
                    setDataSource(file.absolutePath)
                    isLooping = true
                    prepare()
                }
                if (requestFocus()) backgroundPlayer?.start()
            }.onFailure { it.makeLog("NetworkAudioPlayer") }
        }
    }

    /**
     * 播放本地音频文件（用于 TTS 生成的本地缓存文件）。
     * [onComplete] 播放结束(或被打断释放)时回调。
     */
    suspend fun playLocalFile(file: File, onComplete: () -> Unit = {}) {
        if (!file.exists() || file.length() <= 0L) return
        withContext(Dispatchers.Main) {
            runCatching {
                effectPlayer?.release()
                effectPlayer = MediaPlayer().apply {
                    setAudioAttributes(effectAttributes)
                    setDataSource(file.absolutePath)
                    setOnCompletionListener { player ->
                        player.release()
                        if (effectPlayer === player) effectPlayer = null
                        onComplete()
                    }
                    prepare()
                    start()
                }
            }.onFailure { it.makeLog("NetworkAudioPlayer") }
        }
    }

    /** 停止并释放当前短音频/本地文件播放(背景音乐走 [stopBackground]) */
    fun stopEffect() {
        runCatching {
            effectPlayer?.release()
        }
        effectPlayer = null
    }

    /**
     * 调整背景音播放速度（连带音高，1f 为原速）。
     *
     * 转盘转动声要跟着盘面一起慢下来，所以需要在播放过程中连续变速。
     * 变速走 MediaPlayer 的 PlaybackParams：底层是对解码后的 PCM 重采样，
     * 等效于磁带减速 —— 棘轮咔哒是宽带瞬态，音高下降不明显，但"节奏变慢"非常直观。
     *
     * 部分机型/解码器不支持变速，失败就静默维持原速，不影响转盘本身。
     */
    fun setBackgroundSpeed(speed: Float) {
        val player = backgroundPlayer ?: return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val target = speed.coerceIn(0.1f, 2f)
        runCatching {
            val params = player.playbackParams
            // 差得少就不折腾，PlaybackParams 一写就要重采样，频繁设置有咔哒风险
            if (kotlin.math.abs(params.speed - target) > 0.01f) {
                params.speed = target
                player.playbackParams = params
            }
        }.onFailure { it.makeLog("NetworkAudioPlayer") }
    }

    /**
     * 淡出并停止背景音。
     *
     * 背景音是循环播放的(如转盘转动声),直接 [stopBackground] 会"啪"一下断掉,
     * 停在半句上很难听。这里用主线程的 Handler 分段压音量,淡出结束再释放。
     *
     * 淡出期间若已经换了别的背景音(引用变了),本次淡出立即让位给新音源。
     */
    fun fadeOutBackground(durationMs: Long = 400L) {
        val player = backgroundPlayer
        if (player == null || durationMs <= 0L) {
            stopBackground()
            return
        }
        val handler = Handler(Looper.getMainLooper())
        val steps = 10
        val stepMs = (durationMs / steps).coerceAtLeast(16L)
        var step = 0
        val ramp = object : Runnable {
            override fun run() {
                // 期间换了音源就别再压了,交给新音源自己处理
                if (backgroundPlayer !== player) return
                step++
                val volume = (1f - step.toFloat() / steps).coerceIn(0f, 1f)
                runCatching { player.setVolume(volume, volume) }
                if (step < steps) {
                    handler.postDelayed(this, stepMs)
                } else if (backgroundPlayer === player) {
                    // 淡出收尾才释放。期间要是已经转了下一轮(连点),新音源不能一起被掐掉
                    stopBackground()
                }
            }
        }
        handler.post(ramp)
    }

    /** 暂停背景音乐并记住进度。页面回到前台后再 [playBackground] 同一地址会接着播。 */
    fun pauseBackground() {
        onMain {
            backgroundDesired = false
            runCatching {
                val player = backgroundPlayer
                if (player != null && player.isPlaying) player.pause()
            }
            abandonFocus()
        }
    }

    fun stopBackground() {
        onMain {
            playEpoch++
            backgroundDesired = false
            releaseBackgroundPlayer()
            abandonFocus()
        }
    }

    private fun releaseBackgroundPlayer() {
        runCatching { backgroundPlayer?.release() }
        backgroundPlayer = null
        backgroundPlayerSource = null
    }

    private fun onMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else mainHandler.post(block)
    }

    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        mainHandler.post {
            when (change) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    if (!backgroundDesired) return@post
                    runCatching {
                        val player = backgroundPlayer
                        if (player != null && !player.isPlaying) player.start()
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK,
                -> runCatching {
                    val player = backgroundPlayer
                    if (player != null && player.isPlaying) player.pause()
                }
            }
        }
    }

    private val musicAttributes: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    private val effectAttributes: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val focusRequest: AudioFocusRequest? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(musicAttributes)
            .setOnAudioFocusChangeListener(focusListener, mainHandler)
            .build()
    } else {
        null
    }

    private fun requestFocus(): Boolean {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(focusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                focusListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN,
            )
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(focusListener)
        }
    }

    suspend fun warmUp(url: String): Boolean {
        return getCachedFile(url) != null
    }

    private suspend fun getCachedFile(url: String): File? {
        val normalizedUrl = url.trim()
        if (normalizedUrl.isBlank()) return null
        return cacheMutex.withLock {
            withContext(Dispatchers.IO) {
                runCatching {
                    cacheDir.mkdirs()
                    val target = File(cacheDir, normalizedUrl.cacheFileName())
                    if (target.exists() && target.length() > 0L) return@runCatching target
                    val temp = File(cacheDir, "${target.name}.tmp")
                    URL(normalizedUrl).openStream().use { input ->
                        temp.outputStream().use { output -> input.copyTo(output) }
                    }
                    if (temp.length() <= 0L) {
                        temp.delete()
                        return@runCatching null
                    }
                    if (target.exists()) target.delete()
                    temp.renameTo(target)
                    target
                }.onFailure { it.makeLog("NetworkAudioPlayer") }.getOrNull()
            }
        }
    }

    private fun String.cacheFileName(): String {
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(toByteArray())
            .joinToString(separator = "") { "%02x".format(it) }
        val extension = substringBefore('?')
            .substringAfterLast('/', missingDelimiterValue = "")
            .substringAfterLast('.', missingDelimiterValue = "")
            .takeIf { it.length in 2..6 }
            ?: DEFAULT_EXTENSION
        return "$hash.$extension"
    }

    private var backgroundPlayerSource: String? = null

    private companion object {
        const val CACHE_DIR_NAME = "network_audio"
        const val DEFAULT_EXTENSION = "media"
    }
}

