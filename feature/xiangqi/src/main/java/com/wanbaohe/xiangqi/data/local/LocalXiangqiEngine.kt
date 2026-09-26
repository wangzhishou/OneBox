package com.wanbaohe.xiangqi.data.local

import android.content.Context
import android.util.Log
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 端侧象棋引擎(Fairy-Stockfish)的 UCI 进程封装 —— 离线棋力方案。
 *
 * 为什么用子进程而不是 JNI 内嵌:
 * - 引擎本身就是个 UCI 程序,它的内部 API(Position/Search/Thread)不是为嵌入设计的,
 *   内嵌要碰全局状态与线程模型,风险高;
 * - 进程隔离是**实打实的健壮性收益**:第三方 native 代码若崩溃,只会带走子进程,
 *   不会让整个 App 闪退;
 * - 打包更简单:产物是 `lib*.so` 形式的 PIE 可执行文件,放进 `jniLibs` 由 AGP 按 ABI 打包,
 *   安装时随 `useLegacyPackaging=true` 解压到 native 库目录,该目录可执行。
 *
 * 坐标系是这里最容易错的地方:引擎的象棋纵线是 **1-based(a1-i10,红方底线为 1)**,
 * 而本模块与标准 UCCI 是 **0-based(a0-i9,红方底线为 0)**,因此换算关系是
 * `我们的 UCCI 纵线 = 引擎纵线 - 1`(横线 a-i 一致,无镜像)。
 *
 * 当前定位:服务端引擎不可达时的**优先兜底**(排在自研浅层搜索之前)。
 * 尚未接入的:引擎选择项 UI、下载入口与进度展示、空闲进程回收。
 */
@Singleton
class LocalXiangqiEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val weights: XiangqiEngineWeights,
    dispatchersHolder: DispatchersHolder,
) : DispatchersHolder by dispatchersHolder {

    data class EngineMove(
        val ucci: String,
        val depth: Int?,
        val scoreCp: Int?,
    )

    private val mutex = Mutex()
    private var process: Process? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null

    /** 引擎二进制是否随包(离线构建若没放 jniLibs 就是 false,功能自动降级) */
    fun isPackaged(): Boolean = engineFile().isFile

    /** 引擎与权重都就绪,可用 */
    fun isReady(): Boolean = isPackaged() && weights.isInstalled()

    /**
     * 让引擎对 [fen] 出一手,返回**本模块 0-based UCCI** 着法。
     * 任何环节失败都返回 null,由调用方退回自研兜底 —— 绝不让引擎问题卡住对局。
     */
    suspend fun bestMove(
        fen: String,
        legalMoves: List<String>,
        moveTimeMs: Int = DEFAULT_MOVE_TIME_MS,
    ): EngineMove? = withContext(ioDispatcher) {
        if (legalMoves.isEmpty()) return@withContext null
        mutex.withLock {
            val weightsFile = weights.installedFile()
            if (!weightsFile.isFile) return@withLock null
            val running = ensureProcess(weightsFile) ?: return@withLock null
            val writer = writer ?: return@withLock null
            val reader = reader ?: return@withLock null

            try {
                send(writer, "position fen ${toEngineFen(fen)}")
                send(writer, "go movetime $moveTimeMs")
            } catch (e: IOException) {
                Log.w(TAG, "引擎写入失败, 重启进程", e)
                destroyProcess()
                return@withLock null
            }

            var depth: Int? = null
            var scoreCp: Int? = null
            var bestmove: String? = null
            // go movetime 保证引擎会自己收手;这里只加一点读超时余量兜底
            val deadline = System.currentTimeMillis() + moveTimeMs + READ_GRACE_MS
            while (System.currentTimeMillis() < deadline) {
                val line = try {
                    reader.readLine()
                } catch (e: IOException) {
                    Log.w(TAG, "引擎读取失败, 重启进程", e)
                    destroyProcess()
                    return@withLock null
                } ?: break // EOF: 进程已退出

                when {
                    line.startsWith("info") -> {
                        parseIntAfter(line, " depth ")?.let { depth = it }
                        parseScoreCp(line)?.let { scoreCp = it }
                    }
                    line.startsWith("bestmove") -> {
                        bestmove = line.split(" ").getOrNull(1)
                        break
                    }
                }
            }

            if (bestmove == null || bestmove == "(none)") {
                Log.w(TAG, "引擎未返回 bestmove, 重启进程")
                destroyProcess()
                return@withLock null
            }

            val ourUcci = engineToOurUcci(bestmove)
            if (ourUcci == null || ourUcci !in legalMoves) {
                Log.w(TAG, "引擎着法无法映射: engine=$bestmove our=$ourUcci")
                return@withLock null
            }
            if (!running.isAlive) destroyProcess()
            EngineMove(ucci = ourUcci, depth = depth, scoreCp = scoreCp)
        }
    }

    /** 主动回收引擎进程(内存压力/离开对局时可调) */
    fun release() {
        destroyProcess()
    }

    private fun engineFile(): File =
        File(context.applicationInfo.nativeLibraryDir, ENGINE_LIB_NAME)

    private fun ensureProcess(weightsFile: File): Process? {
        process?.let { if (it.isAlive) return it }
        destroyProcess()

        val engine = engineFile()
        if (!engine.isFile) {
            Log.w(TAG, "引擎二进制不存在: ${engine.absolutePath}")
            return null
        }

        return try {
            val started = ProcessBuilder(engine.absolutePath)
                .redirectErrorStream(true)
                .start()
            val out = started.outputStream.bufferedWriter()
            val input = started.inputStream.bufferedReader()

            send(out, "uci")
            if (!await(input, "uciok", HANDSHAKE_TIMEOUT_MS)) {
                Log.w(TAG, "uci 握手超时")
                started.destroy()
                return null
            }
            send(out, "setoption name UCI_Variant value xiangqi")
            send(out, "setoption name EvalFile value ${weightsFile.absolutePath}")
            send(out, "isready")
            // 权重 10.74MB,首次加载在手机上要一点时间
            if (!await(input, "readyok", NET_LOAD_TIMEOUT_MS)) {
                Log.w(TAG, "权重加载超时")
                started.destroy()
                return null
            }

            process = started
            writer = out
            reader = input
            Log.i(TAG, "端侧象棋引擎就绪 net=${weightsFile.name}")
            started
        } catch (e: Exception) {
            Log.w(TAG, "端侧象棋引擎启动失败", e)
            null
        }
    }

    private fun send(writer: BufferedWriter, command: String) {
        writer.write(command)
        writer.write("\n")
        writer.flush()
    }

    /** 读到 [token] 开头的行为止;超时或 EOF 返回 false */
    private fun await(reader: BufferedReader, token: String, timeoutMs: Long): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val line = try {
                reader.readLine()
            } catch (e: IOException) {
                return false
            } ?: return false
            if (line.startsWith(token)) return true
        }
        return false
    }

    private fun destroyProcess() {
        runCatching { writer?.close() }
        runCatching { process?.destroy() }
        writer = null
        reader = null
        process = null
    }

    /**
     * 本模块 FEN 只有四段(`board side halfmove fullmove`),引擎要六段;
     * 象棋无易位、无吃过路兵,中间两段固定 `- -`。
     */
    private fun toEngineFen(fen: String): String {
        val parts = fen.trim().split(Regex("\\s+"))
        if (parts.size >= 6) return parts.take(6).joinToString(" ")
        val board = parts.getOrNull(0) ?: return fen
        val side = parts.getOrNull(1) ?: "w"
        val half = parts.getOrNull(2) ?: "0"
        val full = parts.getOrNull(3) ?: "1"
        return "$board $side - - $half $full"
    }

    /** 引擎 1-based 纵线 → 本模块 0-based UCCI,形如 "h3h5" → "h2h4" */
    private fun engineToOurUcci(move: String): String? {
        var split = 1
        while (split < move.length && !move[split].isLetter()) split++
        if (split >= move.length) return null

        fun convert(point: String): String? {
            if (point.length < 2) return null
            val file = point[0].lowercaseChar() - 'a'
            val engineRank = point.substring(1).toIntOrNull() ?: return null
            val ourRank = engineRank - 1
            if (file !in 0 until FILE_COUNT || ourRank !in 0 until RANK_COUNT) return null
            return "${('a'.code + file).toChar()}$ourRank"
        }

        val from = convert(move.substring(0, split)) ?: return null
        val to = convert(move.substring(split)) ?: return null
        return from + to
    }

    private fun parseIntAfter(line: String, marker: String): Int? {
        val index = line.indexOf(marker)
        if (index < 0) return null
        val start = index + marker.length
        var end = start
        while (end < line.length && line[end].isDigit()) end++
        if (end == start) return null
        return line.substring(start, end).toIntOrNull()
    }

    private fun parseScoreCp(line: String): Int? {
        val index = line.indexOf(" score cp ")
        if (index < 0) return null
        val start = index + " score cp ".length
        var end = start
        if (end < line.length && (line[end] == '-' || line[end] == '+')) end++
        while (end < line.length && line[end].isDigit()) end++
        return line.substring(start, end).toIntOrNull()
    }

    companion object {
        private const val TAG = "LocalXiangqiEngine"
        private const val ENGINE_LIB_NAME = "libfairystockfish.so"
        private const val FILE_COUNT = 9
        private const val RANK_COUNT = 10

        /** 与服务端 400ms 同级;端侧不吃网络往返,同样时间能搜得更深 */
        private const val DEFAULT_MOVE_TIME_MS = 400
        private const val HANDSHAKE_TIMEOUT_MS = 10_000L
        private const val NET_LOAD_TIMEOUT_MS = 30_000L
        private const val READ_GRACE_MS = 5_000L
    }
}
