package com.shifenmiao.ai.request

import android.app.ActivityManager
import android.content.Context
import androidx.core.content.getSystemService
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiRequestProtocol
import com.t8rin.logger.makeLog
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 端侧 LLM 会话管理器:在 [LocalLlmRuntime] 之上收口"加载状态 / 预热 / 延迟释放"三件事。
 *
 * - 加载状态:[loadingModelName] 非 null 表示模型正在加载(预热或首次推理前的 prepare),
 *   UI 据此展示"正在加载本地模型…"提示,把数秒的不可见等待变成可解释的等待。
 * - 预热:进入聊天页调 [maybeWarmUp],仅端侧引擎且总 RAM ≥ [WARM_UP_MIN_TOTAL_RAM_BYTES]
 *   才后台加载,低端机保持懒加载;幂等,已加载 / 加载中直接返回。
 * - 延迟释放:离开聊天页调 [scheduleRelease](默认 60s)后释放 native 权重,
 *   平衡"重回聊天秒回"与"后台长时间持有数 GB 权重被 LMK 杀";
 *   [prepare] 入口会取消待执行的释放,模型只在聊天场景存活。
 *
 * 内存压力保命路径(AppApplication.onTrimMemory)必须改走 [releaseAll],
 * 保证 [loadedModelId] 缓存不失真。
 */
@Singleton
class LocalLlmSessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val runtime: LocalLlmRuntime,
    private val modelRegistry: LocalLlmModelRegistry,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _loadingModelName = MutableStateFlow<String?>(null)
    val loadingModelName: StateFlow<String?> = _loadingModelName

    /** 已确认加载成功的 modelId;[releaseAll] 后清空。 */
    @Volatile
    private var loadedModelId: String? = null
    private var releaseJob: Job? = null

    /** prepare 唯一入口:维护加载状态与 loadedModelId 缓存,并取消待执行的延迟释放。 */
    suspend fun prepare(spec: LocalLlmModelSpec): LocalLlmPrepareResult {
        releaseJob?.cancel()
        releaseJob = null
        if (loadedModelId == spec.id) return LocalLlmPrepareResult.Success(spec.id)
        _loadingModelName.value = spec.displayName
        val result = runCatching { runtime.prepare(spec) }
            .getOrElse { LocalLlmPrepareResult.Failure(LocalLlmError.Unknown(it.message ?: "prepare failed")) }
        // 并发 prepare(预热 + 首次发送)时只清自己设的状态;仅成功才更新 loadedModelId,
        // 失败保持旧值(runtime 里可能仍持有旧模型)
        _loadingModelName.compareAndSet(spec.displayName, null)
        if (result is LocalLlmPrepareResult.Success) {
            loadedModelId = result.loadedModelId
        }
        return result
    }

    /** 进入聊天页预热:协议 / RAM / 幂等检查通过后后台加载,失败静默(发送时还有懒加载兜底)。 */
    fun maybeWarmUp(engine: AiEngine) {
        if (engine.requestProtocol != AiRequestProtocol.LOCAL_ON_DEVICE) return
        val modelName = engine.model.name.takeIf { it.isNotBlank() } ?: return
        if (loadedModelId == modelName || _loadingModelName.value != null) return
        if (totalRamBytes() < WARM_UP_MIN_TOTAL_RAM_BYTES) {
            makeLog { "LocalLlmSessionManager: skip warm-up, total RAM below threshold" }
            return
        }
        scope.launch {
            val spec = modelRegistry.resolve(modelName) ?: return@launch
            makeLog { "LocalLlmSessionManager: warm-up start model=$modelName" }
            prepare(spec)
        }
    }

    /** 离开聊天页延迟释放;[delayMillis] 内再次 prepare(重回聊天 / 发消息)会取消。 */
    fun scheduleRelease(delayMillis: Long = RELEASE_DELAY_MILLIS) {
        if (loadedModelId == null && _loadingModelName.value == null) return
        releaseJob?.cancel()
        releaseJob = scope.launch {
            delay(delayMillis)
            makeLog { "LocalLlmSessionManager: delayed release fired" }
            releaseAll()
        }
    }

    /** 立即释放(内存压力保命路径);幂等,任何时刻调用都安全。 */
    suspend fun releaseAll() {
        releaseJob?.cancel()
        releaseJob = null
        loadedModelId = null
        _loadingModelName.value = null
        runtime.releaseAll()
    }

    private fun totalRamBytes(): Long {
        val activityManager = context.getSystemService<ActivityManager>() ?: return 0L
        val info = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(info)
        return info.totalMem
    }

    private companion object {
        /** 预热门槛:总 RAM ≥ 7GB(8GB 机型实际报告约 7.3GB+,6GB 机型约 5.5GB 不预热)。 */
        const val WARM_UP_MIN_TOTAL_RAM_BYTES = 7L * 1024 * 1024 * 1024
        const val RELEASE_DELAY_MILLIS = 60_000L
    }
}
