package com.wanbaohe.xiangqi.data.local

import android.content.Context
import android.util.Log
import com.shifenmiao.storage.RemoteConfigStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 端侧象棋引擎的 NNUE 权重安装器。
 *
 * 权重是**数据**不是代码,所以走普通下载即可(不涉及 Play 的动态代码政策),也不进 APK:
 * 首装体积只增加引擎本体(约 1.5MB),10.74MB 权重按需下载到 `filesDir/local_engines/`。
 *
 * 权重来源为本仓库自己的 R2(`images.oneboxable.com`),与游戏音效、LiteRT-LM 模型同一公开域名
 * 和同一 `models/` 前缀。文件名内嵌 sha256 前 12 位,将来换权重可并存,不会打断旧客户端。
 *
 * 下载完必须校验(长度 + sha256):R2 上传曾出现「无报错但对象不存在」,回读校验是唯一可靠手段;
 * 客户端一侧同理,校验失败宁可当作未安装,也不要拿半个文件去喂引擎。
 */
@Singleton
class XiangqiEngineWeights @Inject constructor(
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder,
) : DispatchersHolder by dispatchersHolder {

    private val directory: File
        get() = File(context.filesDir, DIRECTORY).apply { mkdirs() }

    fun installedFile(): File = File(directory, FILE_NAME)

    /**
     * 实际使用的下载地址:优先取 RemoteConfig 下发的值(见
     * [com.shifenmiao.model.remote.RemoteConfig.xiangqiEngineWeightsUrl]),
     * 未下发或为空时回退到内置常量。
     *
     * 每次调用都实时读,不缓存:远程配置是本地 MMKV 读取,开销可忽略,
     * 而缓存会让「刚在后台改了地址」在本次进程内不生效。
     */
    fun downloadUrl(): String =
        RemoteConfigStorage.getRemoteConfig().xiangqiEngineWeightsUrl
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_DOWNLOAD_URL

    /** 已安装权重的字节数;未安装返回 0 */
    fun installedBytes(): Long = installedFile().let { if (it.isFile) it.length() else 0L }

    /** 只做便宜的检查(存在 + 长度对);完整 sha256 校验在下载完成时做一次 */
    fun isInstalled(): Boolean = installedFile().let { it.isFile && it.length() == EXPECTED_BYTES }

    /** 安装状态,供设置页观察 */
    sealed interface InstallState {
        data object NotInstalled : InstallState
        data class Downloading(val downloaded: Long, val total: Long) : InstallState
        data class Installed(val bytes: Long) : InstallState
    }

    /**
     * 下载跑在**单例自己的作用域**里,而不是页面的 componentScope:
     * 10.74MB 的下载不该因为用户退出设置页而中断(退出即取消会白费流量)。
     */
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private var downloadJob: Job? = null

    private val _state = MutableStateFlow(refreshState())
    val state: StateFlow<InstallState> = _state.asStateFlow()

    /** 开始下载;已在下载中或已安装返回 false(调用方可据此不做重复动作) */
    fun startDownload(): Boolean {
        if (downloadJob?.isActive == true) return false
        if (isInstalled()) {
            _state.value = InstallState.Installed(installedBytes())
            return false
        }
        downloadJob = scope.launch {
            _state.value = InstallState.Downloading(0L, EXPECTED_BYTES)
            val file = ensureInstalled { downloaded, total ->
                _state.value = InstallState.Downloading(downloaded, total)
            }
            _state.value = refreshState()
            if (file == null) Log.w(TAG, "权重下载未完成, 端侧引擎暂不可用")
        }
        return true
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        _state.value = refreshState()
    }

    /**
     * 删除已装权重(释放约 10.74MB)。
     * 调用方应先回收引擎进程 —— 进程可能还 mmap 着这个权重文件。
     */
    fun deleteWeights() {
        downloadJob?.cancel()
        downloadJob = null
        installedFile().takeIf { it.exists() }?.delete()
        _state.value = refreshState()
    }

    private fun refreshState(): InstallState =
        if (isInstalled()) InstallState.Installed(installedBytes()) else InstallState.NotInstalled

    /**
     * 确保权重就绪:已装直接返回;未装则下载并校验。
     * [onProgress] 以 (已下载, 总字节数) 回调,总长未知时传 -1。
     * 失败(网络/校验)返回 null —— 调用方应当退回其它引擎,不要重试到卡住对局。
     */
    suspend fun ensureInstalled(
        onProgress: ((downloaded: Long, total: Long) -> Unit)? = null,
    ): File? = withContext(ioDispatcher) {
        val target = installedFile()
        if (isInstalled()) return@withContext target

        val temp = File(directory, "$FILE_NAME.download")
        try {
            val url = downloadUrl()
            Log.i(TAG, "开始下载象棋权重: $url")
            val downloaded = download(url, temp, onProgress)
            if (downloaded != EXPECTED_BYTES) {
                Log.w(TAG, "权重长度不符: 期望 $EXPECTED_BYTES, 实际 $downloaded")
                return@withContext null
            }
            val digest = sha256(temp)
            if (!digest.equals(SHA256, ignoreCase = true)) {
                Log.w(TAG, "权重 sha256 不符: 期望 $SHA256, 实际 $digest")
                return@withContext null
            }
            if (!temp.renameTo(target)) {
                // rename 失败(极少数情况)时退回拷贝
                temp.copyTo(target, overwrite = true)
                temp.delete()
            }
            Log.i(TAG, "象棋权重就绪: ${target.absolutePath} (${target.length()} 字节)")
            target
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (e: Exception) {
            Log.w(TAG, "象棋权重下载失败", e)
            null
        } finally {
            if (temp.exists()) temp.delete()
        }
    }

    private fun download(
        url: String,
        destination: File,
        onProgress: ((downloaded: Long, total: Long) -> Unit)?,
    ): Long {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            instanceFollowRedirects = true
        }
        try {
            val code = connection.responseCode
            if (code !in 200..299) throw IOException("HTTP $code")
            val total = connection.contentLengthLong
            var received = 0L
            var lastLoggedPercent = -1
            onProgress?.invoke(0L, total)
            connection.inputStream.use { input ->
                destination.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        received += read
                        onProgress?.invoke(received, total)
                        if (total > 0) {
                            val percent = (received * 100 / total).toInt()
                            if (percent / 25 > lastLoggedPercent / 25) {
                                lastLoggedPercent = percent
                                Log.i(TAG, "权重下载进度 $percent% ($received/$total)")
                            }
                        }
                    }
                }
            }
            return received
        } finally {
            connection.disconnect()
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val TAG = "XiangqiEngineWeights"
        private const val DIRECTORY = "local_engines"

        /** 权重文件名内嵌 sha256 前 12 位,换权重时换文件名即可并存 */
        const val FILE_NAME = "xiangqi-83f16c17fe26.nnue"

        /**
         * 内置兜底地址(RemoteConfig 未下发时使用)。国内渠道同用此默认值;
         * 若国内实测速度不理想,不必发版 —— 在 CMS 下发 `xiangqiEngineWeightsUrl` 即可切到镜像。
         */
        const val DEFAULT_DOWNLOAD_URL = "https://images.oneboxable.com/models/$FILE_NAME"

        const val EXPECTED_BYTES = 11_261_915L
        const val SHA256 = "83f16c17fe266f8d0904cb7cd8997777ee6a618a82b5d7fd32d52f570c760a25"

        private const val CONNECT_TIMEOUT_MS = 30_000
        private const val READ_TIMEOUT_MS = 120_000
    }
}
