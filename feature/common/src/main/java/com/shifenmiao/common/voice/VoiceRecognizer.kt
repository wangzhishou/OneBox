/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.common.voice

import com.shifenmiao.network.NetworkBuilder
import com.shifenmiao.network.api.ApiService
import com.shifenmiao.storage.RemoteConfigStorage
import com.shifenmiao.storage.TokenStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 语音输入识别器(讯飞大模型识别 / 自建 FunASR,按 RemoteConfig.voiceInput.provider 分流)。
 *
 * provider = [VOICE_PROVIDER_SELF] 时直接连自家网关的反代端点(网关再转发本机 FunASR 2pass 服务);
 * 其余情况走讯飞:[prepare] 获取签名 wss URL + appId(RemoteConfig 下发了凭据则本地签名,
 * 否则经 Go 网关代签,密钥不出服务端)→
 * [start] 建 WebSocket 客户端 + PCM 录音器开始流式识别 →
 * [finish] 用户主动说完(发结束帧,等最终结果)或 [cancel] 放弃。
 * 识别状态经 [state] 输出,录音音量经 [rms] 输出供 UI 脉冲动画。
 */
@Singleton
class VoiceRecognizer @Inject constructor(
    private val apiService: ApiService
) {

    private val _state = MutableStateFlow<AsrState>(AsrState.Idle)

    /** 识别状态:Idle / Listening(本段中间结果) / SegmentFinal(本段定稿) / Finished / Error */
    val state: StateFlow<AsrState> = _state.asStateFlow()

    /**
     * 会话序号: [start] 递增, [cancel] 也递增(作废当前会话)。
     * 客户端只能通过 [publish] 写状态, 上一轮被取消后迟到的回调会被丢弃 ——
     * 否则"面板已划走、最终结果才到"的残留状态会在下次打开面板时把旧文本又回填一遍。
     */
    private val sessionSeq = java.util.concurrent.atomic.AtomicInteger(0)

    private val _rms = MutableStateFlow(0f)

    /** 当前录音音量 0..1 */
    val rms: StateFlow<Float> = _rms.asStateFlow()

    private var client: AsrStreamClient? = null
    private var recorder: PcmAudioRecorder? = null
    private var timeoutJob: Job? = null

    /** [prepare] 时定下的引擎, [start] 复用: 避免两次读取之间远程配置热切换导致 URL 与客户端不匹配 */
    @Volatile
    private var pendingEngine: VoiceInputEngine = VoiceInputEngine.SYSTEM

    /**
     * 获取 {签名 wss URL, appId}。
     * 走哪个引擎由后台开关 voiceInput.engine 决定(见 [resolveVoiceInputEngine]):
     * - self: 直接连自家网关反代端点, 无需向讯飞换签名 URL(第二项 appId 留空);
     * - iflytek: RemoteConfig 下发了完整凭据(appId/apiKey/apiSecret)时本地签名直连,
     *   否则向 Go 网关代签(/api/voice/asr/ws-auth,密钥不出服务端)。失败返回 null。
     */
    suspend fun prepare(): Pair<String, String>? {
        val local = RemoteConfigStorage.getRemoteConfig().voiceInput
        val engine = resolveVoiceInputEngine(local)
        pendingEngine = engine
        if (engine == VoiceInputEngine.SELF) {
            android.util.Log.i(TAG, "语音输入引擎: self(自建 FunASR, 网关反代) engine=" + local?.engine)
            return buildSelfAsrUrl() to ""
        }
        android.util.Log.i(TAG, "语音输入引擎: iflytek engine=" + local?.engine)
        if (!local?.appId.isNullOrBlank() &&
            !local?.apiKey.isNullOrBlank() &&
            !local?.apiSecret.isNullOrBlank()
        ) {
            return IFlytekAuth.buildWsUrl(local!!.apiKey!!, local.apiSecret!!) to local.appId!!
        }
        return try {
            val response = apiService.getVoiceAsrWsAuth()
            response.body()
                ?.takeIf { response.isSuccessful && it.url.isNotBlank() }
                ?.let { it.url to it.appId }
        } catch (_: Exception) {
            null
        }
    }

    /** 开始识别。防重入:先 cancel 掉上一轮。60s 上限到点自动 finish。 */
    fun start(url: String, appId: String, scope: CoroutineScope) {
        cancel()
        val session = sessionSeq.incrementAndGet()
        val publish: (AsrState) -> Unit = { next -> if (sessionSeq.get() == session) _state.value = next }
        _state.value = AsrState.Listening("")
        val newClient: AsrStreamClient = if (pendingEngine == VoiceInputEngine.SELF) {
            FunAsrClient(publish = publish).also { funAsr ->
                funAsr.connect(wsUrl = url, token = resolveAuthToken())
            }
        } else {
            IFlytekAsrClient(appId = appId, state = _state).also { iflytek ->
                iflytek.connect(url)
            }
        }
        client = newClient
        val newRecorder = PcmAudioRecorder()
        recorder = newRecorder
        try {
            newRecorder.start(
                onChunk = { newClient.sendPcm(it) },
                onRms = { _rms.value = it.toFloat() }
            )
        } catch (e: Exception) {
            cancel()
            _state.value = AsrState.Error(e.message ?: "recorder start failed")
            return
        }
        timeoutJob = scope.launch {
            delay(MAX_DURATION_MS)
            finish()
        }
    }

    /** 用户主动说完:停录音、发结束帧,最终结果仍经 [state] 回调 */
    fun finish() {
        timeoutJob?.cancel()
        timeoutJob = null
        recorder?.stop()
        recorder = null
        client?.stop()
        client = null
    }

    /** 放弃:立即断开并复位状态(同时作废当前会话, 丢弃迟到的回调) */
    fun cancel() {
        sessionSeq.incrementAndGet()
        timeoutJob?.cancel()
        timeoutJob = null
        recorder?.stop()
        recorder = null
        client?.cancel()
        client = null
        _rms.value = 0f
        _state.value = AsrState.Idle
    }

    /** 自建识别地址: 由业务 baseUrl 推导 wss, 走网关反代(识别端口与内网都不出网关) */
    private fun buildSelfAsrUrl(): String = NetworkBuilder.getBaseUrl()
        .replace("https://", "wss://")
        .replace("http://", "ws://")
        .removeSuffix("/") + SELF_ASR_PATH

    /** 与 AuthInterceptor 同源: 登录 JWT 优先, 未登录回退远程配置里的游客 token */
    private fun resolveAuthToken(): String? =
        TokenStorage.getTokenFromLocalStorage()?.takeIf { it.isNotBlank() }
            ?: RemoteConfigStorage.getRemoteConfig().accessToken?.takeIf { it.isNotBlank() }

    companion object {
        /** 联调/线上排查: logcat 过滤 VoiceAsr 可看到本轮用的识别引擎 */
        private const val TAG = "VoiceAsr"

        /** 单次识别上限 60s */
        private const val MAX_DURATION_MS = 60_000L

        /** 自建语音识别反代端点(Go 网关) */
        private const val SELF_ASR_PATH = "/api/voice/asr/ws"
    }
}
