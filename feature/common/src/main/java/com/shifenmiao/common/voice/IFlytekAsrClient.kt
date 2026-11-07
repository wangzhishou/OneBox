package com.shifenmiao.common.voice

import android.util.Base64
import com.shifenmiao.model.jsonStringOf
import com.shifenmiao.model.ModelProvider.AppJson
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.Collections
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/** 语音识别状态流,RMS 不经 WebSocket(由录音器直接给 UI) */
sealed interface AsrState {
    data object Idle : AsrState

    /** 当前这一段的中间结果(边说边出字) */
    data class Listening(val partialText: String) : AsrState

    /**
     * 检测到停顿时该段的定稿文本(自建引擎: 该段已用 SenseVoice 重跑)。
     * UI 收到后应立刻回填输入框,**但面板继续收音**;[seq] 保证连续两段文本相同时也能各回填一次。
     */
    data class SegmentFinal(val text: String, val seq: Int) : AsrState

    /** 用户主动说完: 回填并在尾段处理后关闭面板 */
    data class Finished(val text: String) : AsrState

    data class Error(val message: String) : AsrState
}

/**
 * 讯飞「大模型识别」WebAPI 客户端。
 *
 * App 持网关签名的一次性 wss URL 直连讯飞,流式上行 PCM、下行 wpgs 动态修正结果。
 * 自建轻量 OkHttpClient(readTimeout=0 的 WS 长连接),不挂项目业务拦截器。
 * 上行经 BlockingQueue + 单发送线程按 ~40ms/帧节流,防止 WS 慢于录音时堆积。
 */
class IFlytekAsrClient(
    private val appId: String,
    private val state: MutableStateFlow<AsrState>,
) : AsrStreamClient {

    private val httpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val seq = AtomicInteger(0)
    private val sendQueue = LinkedBlockingQueue<ByteArray>()
    private var senderThread: Thread? = null

    /** 识别文本分片:wpgs 协议下按 sn 维护,"apd" 追加、"rpl" 按 rg 区间替换 */
    private val segments = Collections.synchronizedList(mutableListOf<String>())

    @Volatile
    private var endRequested = false

    @Volatile
    private var cancelled = false

    fun connect(wsUrl: String) {
        val request = Request.Builder().url(wsUrl).build()
        webSocket = httpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                startSender()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleMessage(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(code, reason)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                if (!cancelled && state.value !is AsrState.Finished) {
                    state.value = AsrState.Error(t.message ?: "WebSocket failure")
                }
            }
        })
    }

    /** 上行一块 PCM(内部排队,发送线程节流发出) */
    override fun sendPcm(chunk: ByteArray) {
        if (!cancelled) sendQueue.offer(chunk)
    }

    /** 说完:队列排空后发结束帧(status=2),等服务端回最终结果 */
    override fun stop() {
        endRequested = true
    }

    /** 放弃:立即断开,不再等待结果 */
    override fun cancel() {
        cancelled = true
        senderThread?.interrupt()
        sendQueue.clear()
        runCatching { webSocket?.cancel() }
        webSocket = null
        httpClient.dispatcher.executorService.shutdown()
    }

    /* ─────────── private ─────────── */

    private fun startSender() {
        senderThread = Thread({
            var first = true
            try {
                while (!cancelled) {
                    val chunk = sendQueue.poll(200, TimeUnit.MILLISECONDS)
                    if (chunk != null) {
                        sendFrame(chunk, if (first) STATUS_FIRST else STATUS_CONTINUE)
                        first = false
                        // 讯飞建议 40ms/1280B,录音天然 ~40ms/块,这里再兜底节流
                        Thread.sleep(FRAME_INTERVAL_MS)
                    } else if (endRequested) {
                        sendFrame(null, STATUS_END)
                        break
                    }
                }
            } catch (_: InterruptedException) {
            }
        }, "iflytek-asr-sender").also { it.start() }
    }

    private fun sendFrame(chunk: ByteArray?, status: Int) {
        val ws = webSocket ?: return
        runCatching { ws.send(buildFrameJson(chunk, status)) }
    }

    /** 首帧带 parameter,中间帧/结束帧只有 header+payload;结束帧 audio 为空串 */
    private fun buildFrameJson(chunk: ByteArray?, status: Int): String {
        val audio = mapOf(
            "encoding" to "raw",
            "sample_rate" to PcmAudioRecorder.SAMPLE_RATE,
            "channels" to 1,
            "bit_depth" to 16,
            "seq" to seq.getAndIncrement(),
            "status" to status,
            "audio" to (chunk?.let { Base64.encodeToString(it, Base64.NO_WRAP) } ?: "")
        )
        val frame = mutableMapOf<String, Any>(
            "header" to mapOf("app_id" to appId, "status" to status),
            "payload" to mapOf("audio" to audio)
        )
        if (status == STATUS_FIRST) {
            frame["parameter"] = mapOf(
                "iat" to mapOf(
                    "domain" to "slm",
                    "language" to "zh_cn",
                    "accent" to "mandarin",
                    "dwa" to "wpgs",
                    "eos" to 2000,
                    // 不传 format,保持服务端默认 JSON 结构,wpgs 的 pgs/rg 动态修正字段才会返回
                    "result" to mapOf(
                        "encoding" to "utf8",
                        "compress" to "raw"
                    )
                )
            )
        }
        return jsonStringOf(frame)
    }

    private fun handleMessage(text: String) {
        // 解析失败的帧直接忽略,不 crash
        try {
            val root = AppJson.parseToJsonElement(text) as? JsonObject ?: return
            val header = root["header"] as? JsonObject ?: return
            fun JsonObject.str(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull
            fun JsonObject.int(key: String): Int? = (this[key] as? JsonPrimitive)?.intOrNull
            val code = header.int("code") ?: -1
            if (code != 0) {
                val sid = header.str("sid").orEmpty()
                val message = header.str("message") ?: "asr error"
                // 联调定位:logcat 过滤 IFlytekAsr 可看到讯飞错误码与 sid(提工单用)
                android.util.Log.e("IFlytekAsr", "asr error code=$code message=$message sid=$sid")
                state.value = AsrState.Error("$code: $message")
                return
            }
            ((root["payload"] as? JsonObject)
                ?.get("result") as? JsonObject)
                ?.str("text")
                ?.takeIf { it.isNotEmpty() }
                ?.let { applyResult(String(Base64.decode(it, Base64.DEFAULT))) }
            if (header.int("status") == STATUS_END) {
                finishWithSegments()
            }
        } catch (_: Exception) {
        }
    }

    /** 解析 base64 解码后的 wpgs 结果:ws[].cw[].w 拼接本片文本,按 pgs 维护 segments */
    private fun applyResult(decoded: String) {
        if (!decoded.startsWith("{")) {
            // 兜底:服务端若按纯文本返回(未带 JSON 结构),整片按追加处理
            synchronized(segments) { segments.add(decoded) }
            val full = synchronized(segments) { segments.joinToString("") }
            if (state.value !is AsrState.Finished) state.value = AsrState.Listening(full)
            return
        }
        val obj = AppJson.parseToJsonElement(decoded) as? JsonObject ?: return
        fun JsonObject.str(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull
        fun JsonObject.int(key: String): Int? = (this[key] as? JsonPrimitive)?.intOrNull
        val sb = StringBuilder()
        (obj["ws"] as? JsonArray)?.forEach { wsEl ->
            ((wsEl as? JsonObject)?.get("cw") as? JsonArray)?.forEach { cwEl ->
                sb.append((cwEl as? JsonObject)?.str("w").orEmpty())
            }
        }
        val text = sb.toString()
        if (text.isNotEmpty()) {
            when (obj.str("pgs")) {
                "rpl" -> {
                    // rg = [起始 sn, 结束 sn](1 起始,闭区间),用本片文本替换该区间
                    val rg = obj["rg"] as? JsonArray ?: return
                    val from = (((rg[0] as? JsonPrimitive)?.intOrNull ?: return) - 1).coerceAtLeast(0)
                    val to = ((rg[1] as? JsonPrimitive)?.intOrNull ?: return) - 1
                    synchronized(segments) {
                        while (segments.size < from) segments.add("")
                        var idx = minOf(to, segments.size - 1)
                        while (idx >= from) {
                            segments.removeAt(idx)
                            idx--
                        }
                        segments.add(from.coerceAtMost(segments.size), text)
                    }
                }

                else -> synchronized(segments) { segments.add(text) } // "apd" 或无 pgs:追加
            }
        }
        val full = synchronized(segments) { segments.joinToString("") }
        if ((obj["ls"] as? JsonPrimitive)?.contentOrNull.equals("true", ignoreCase = true)) {
            state.value = AsrState.Finished(full)
        } else if (state.value !is AsrState.Finished) {
            state.value = AsrState.Listening(full)
        }
    }

    private fun finishWithSegments() {
        if (state.value !is AsrState.Finished) {
            state.value = AsrState.Finished(synchronized(segments) { segments.joinToString("") })
        }
        runCatching { webSocket?.close(1000, "done") }
    }

    companion object {
        private const val STATUS_FIRST = 0
        private const val STATUS_CONTINUE = 1
        private const val STATUS_END = 2
        private const val FRAME_INTERVAL_MS = 40L
    }
}
