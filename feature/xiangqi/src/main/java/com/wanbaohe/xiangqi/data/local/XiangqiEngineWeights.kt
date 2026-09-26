package com.wanbaohe.xiangqi.data.local

import android.content.Context
import android.util.Log
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
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

    /** 只做便宜的检查(存在 + 长度对);完整 sha256 校验在下载完成时做一次 */
    fun isInstalled(): Boolean = installedFile().let { it.isFile && it.length() == EXPECTED_BYTES }

    /**
     * 确保权重就绪:已装直接返回;未装则下载并校验。
     * 失败(网络/校验)返回 null —— 调用方应当退回其它引擎,不要重试到卡住对局。
     */
    suspend fun ensureInstalled(): File? = withContext(ioDispatcher) {
        val target = installedFile()
        if (isInstalled()) return@withContext target

        val temp = File(directory, "$FILE_NAME.download")
        try {
            Log.i(TAG, "开始下载象棋权重: $DOWNLOAD_URL")
            val downloaded = download(DOWNLOAD_URL, temp)
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

    private fun download(url: String, destination: File): Long {
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
            connection.inputStream.use { input ->
                destination.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        received += read
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
         * 来源同 R2 上现有的 LiteRT-LM 模型对象(同一公开域名与 `models/` 前缀)。国内渠道也走这里
         * (本仓库音效/模型既有做法);
         * 若国内实测速度不理想,可在此按 flavor 切到阿里云 OSS 镜像 —— 刻意收在这一个常量里。
         */
        const val DOWNLOAD_URL = "https://images.oneboxable.com/models/$FILE_NAME"

        const val EXPECTED_BYTES = 11_261_915L
        const val SHA256 = "83f16c17fe266f8d0904cb7cd8997777ee6a618a82b5d7fd32d52f570c760a25"

        private const val CONNECT_TIMEOUT_MS = 30_000
        private const val READ_TIMEOUT_MS = 120_000
    }
}
