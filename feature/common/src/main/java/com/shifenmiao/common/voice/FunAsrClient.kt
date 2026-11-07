package com.shifenmiao.common.voice

import com.shifenmiao.model.jsonStringOf
import com.shifenmiao.model.ModelProvider.AppJson
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.toByteString
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * 自建语音识别客户端(sherpa-onnx 两段式, 经 Go 网关 /api/voice/asr/ws 反代)。
 *
 * 上行: 首帧参数(文本) -> 16k 单声道 PCM 二进制帧 -> {"is_speaking":false}
 * 下行三种消息(与网关约定, 见 strapi_go 的 voice_asr_ws.go):
 * - `mode=2pass-online`                    : 当前这一段的中间结果, 只用于面板展示;
 * - `mode=2pass-offline` + `segment_final`: 检测到停顿, 这一段已用 SenseVoice 定稿
 *   -> 立刻回填输入框, 面板继续收音(说多段时前面的话不会被冲掉);
 * - `mode=2pass-offline` + `is_final`     : 用户主动说完, 尾段定稿 -> 回填并关闭面板。
 *
 * 状态经 [publish] 输出; 由 VoiceRecognizer 绑定当前会话, 上一轮迟到的回调不会污染新一轮 UI。
 */
class FunAsrClient(
    private val publish: (AsrState) -> Unit,
) : AsrStreamClient {

    private val httpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val sendQueue = LinkedBlockingQueue<ByteArray>()
    private var senderThread: Thread? = null

    /** 当前段落的中间结果(每段独立, 定稿后即被回填消费, 不再累积) */
    @Volatile
    private var partialText = ""

    @Volatile
    private var endRequested = false

    @Volatile
    private var cancelled = false

    /** 已产出最终结果: 兜底线程不再重复收尾 */
    @Volatile
    private var finished = false

    private var segmentSeq = 0

    /** 连接网关并开始识别。token 由调用方从本地登录态取出(网关 JWT 鉴权)。 */
    fun connect(wsUrl: String, token: String?) {
        val builder = Request.Builder().url(wsUrl)
        if (!token.isNullOrBlank()) builder.header("Authorization", "Bearer $token")
        webSocket = httpClient.newWebSocket(builder.build(), object : WebSocketListener() {
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
                // 已取消/已出最终结果的连接不再上报错误, 避免面板关闭后再弹一次错误
                if (!cancelled && !finished) {
                    publish(AsrState.Error(t.message ?: "WebSocket failure"))
                }
            }
        })
    }

    override fun sendPcm(chunk: ByteArray) {
        if (!cancelled) sendQueue.offer(chunk)
    }

    override fun stop() {
        endRequested = true
    }

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
            var started = false
            try {
                while (!cancelled) {
                    val chunk = sendQueue.poll(200, TimeUnit.MILLISECONDS)
                    if (chunk != null) {
                        if (!started) {
                            sendText(buildStartFrame())
                            started = true
                        }
                        webSocket?.send(chunk.toByteString())
                        // 录音天然 ~40ms/块, 这里兜底节流, 防止网关/WSS 慢于录音时堆积
                        Thread.sleep(FRAME_INTERVAL_MS)
                    } else if (endRequested) {
                        if (!started) sendText(buildStartFrame())
                        sendText(END_FRAME)
                        scheduleFallbackFinish()
                        break
                    }
                }
            } catch (_: InterruptedException) {
            }
        }, "funasr-asr-sender").also { it.start() }
    }

    private fun sendText(frame: String) {
        runCatching { webSocket?.send(frame) }
    }

    /** 首帧参数: 与 FunASR 2pass 协议一致; chunk_size [5,10,5] = 600ms 出一次中间结果 */
    private fun buildStartFrame(): String = jsonStringOf(
        mapOf(
            "mode" to "2pass",
            "wav_name" to "onebox",
            "is_speaking" to true,
            "wav_format" to "pcm",
            "audio_fs" to PcmAudioRecorder.SAMPLE_RATE,
            "chunk_size" to listOf(5, 10, 5),
            "itn" to true,
            "svs_itn" to true
        )
    )

    /**
     * 兜底: 结束帧发出后服务端迟迟不回尾段结果时, 用当前中间结果收尾(文本可能为空),
     * 保证面板一定会关闭, 不会卡在"正在识别"。
     */
    private fun scheduleFallbackFinish() {
        Thread({
            try {
                Thread.sleep(FINISH_FALLBACK_MS)
            } catch (_: InterruptedException) {
                return@Thread
            }
            if (cancelled || finished) return@Thread
            finished = true
            publish(AsrState.Finished(partialText))
        }, "funasr-finish-fallback").start()
    }

    private fun handleMessage(text: String) {
        if (cancelled) return
        // 解析不了的帧直接忽略, 不 crash
        try {
            val root = AppJson.parseToJsonElement(text) as? JsonObject ?: return
            fun str(key: String): String? = (root[key] as? JsonPrimitive)?.contentOrNull
            fun bool(key: String): Boolean =
                (root[key] as? JsonPrimitive)?.contentOrNull.equals("true", ignoreCase = true)
            val mode = str("mode").orEmpty()
            val recognized = str("text").orEmpty()
            val isFinal = bool("is_final")
            val segmentFinal = bool("segment_final")

            if (!mode.contains("offline")) {
                partialText = recognized
                publish(AsrState.Listening(recognized))
                return
            }

            partialText = ""
            when {
                isFinal -> {
                    if (finished) return
                    finished = true
                    publish(AsrState.Finished(recognized))
                }

                segmentFinal && recognized.isNotBlank() -> {
                    publish(AsrState.SegmentFinal(recognized, ++segmentSeq))
                }
            }
        } catch (_: Exception) {
        }
    }

    private companion object {
        /** 与讯飞侧一致的 40ms/帧节流 */
        const val FRAME_INTERVAL_MS = 40L

        /** 结束帧后等待尾段结果的兜底时长 */
        const val FINISH_FALLBACK_MS = 4000L

        const val END_FRAME = "{\"is_speaking\":false}"
    }
}
