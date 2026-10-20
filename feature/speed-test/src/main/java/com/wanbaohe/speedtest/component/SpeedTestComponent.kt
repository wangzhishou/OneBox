package com.wanbaohe.speedtest.component

import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.review.ReviewPromptHost
import com.wanbaohe.core.ui.review.ReviewPromptTrigger
import com.wanbaohe.speedtest.data.NetworkHelper
import com.wanbaohe.speedtest.data.SpeedTestConfig
import com.wanbaohe.speedtest.data.SpeedTestConfigRepository
import com.wanbaohe.speedtest.data.SpeedTestRepository
import com.wanbaohe.speedtest.domain.SpeedTestPhase
import com.wanbaohe.speedtest.domain.SpeedTestRecord
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

class SpeedTestComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val repository: SpeedTestRepository,
    private val configRepository: SpeedTestConfigRepository,
    private val networkHelper: NetworkHelper
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(
        SpeedTestUiState(networkType = networkHelper.getCurrentNetworkType())
    )
    val uiState = _uiState.asStateFlow()

    private var testJob: Job? = null

    init {
        // 首次启动时写入预设配置
        componentScope.launch {
            configRepository.initDefaultsIfEmpty()
        }

        // 订阅配置列表 + 历史记录，合并更新 UI
        combine(
            configRepository.getAll(),
            repository.getHistory()
        ) { configs, history ->
            // 直接取 isActive=true 的配置，DB setActive 后 Flow 会立刻推送正确值
            val active = configs.firstOrNull { it.isActive }
                ?: configs.firstOrNull()
                ?: SpeedTestConfig()
            _uiState.value = _uiState.value.copy(
                configList = configs,
                config = active,
                history = history
            )
        }.launchIn(componentScope)
    }

    // ── 测速 ────────────────────────────────────────────────────────────────

    /** 开始下载测速 */
    fun startTest() {
        if (_uiState.value.status == SpeedTestStatus.MEASURING) return
        testJob?.cancel()
        val networkType = networkHelper.getCurrentNetworkType()
        val config = _uiState.value.config
        _uiState.value = _uiState.value.copy(
            status = SpeedTestStatus.MEASURING,
            liveMbps = 0f, progress = 0f, result = null,
            latencyMs = -1, measuringLatency = true, testConfig = config,
            networkType = networkType, errorMsg = null
        )
        testJob = componentScope.launch {
            repository.startTest(config, networkType).collect { phase ->
                currentCoroutineContext().ensureActive()
                when (phase) {
                    is SpeedTestPhase.MeasuringLatency -> {}
                    is SpeedTestPhase.Downloading ->
                        _uiState.value = _uiState.value.copy(
                            liveMbps = phase.liveMbps, progress = phase.progress,
                            latencyMs = phase.latencyMs, measuringLatency = false
                        )
                    is SpeedTestPhase.Done -> {
                        repository.saveRecord(phase.record)
                        currentCoroutineContext().ensureActive()
                        _uiState.value = _uiState.value.copy(
                            status = SpeedTestStatus.DONE,
                            result = phase.record,
                            latencyMs = phase.record.latencyMs, measuringLatency = false,
                            liveMbps = phase.record.downloadMbps, progress = 1f
                        )
                        // 测速跑完并落库 = 用户刚拿到价值,报给应用内评分弹层做门控判断
                        // (真正弹不弹由 InAppReviewPrompt 的次数门槛/冷却/Play 配额决定)
                        ReviewPromptHost.notifySuccess(ReviewPromptTrigger.SPEED_TEST_SUCCESS)
                    }
                    is SpeedTestPhase.Error ->
                        _uiState.value = _uiState.value.copy(
                            status = SpeedTestStatus.IDLE, errorMsg = phase.message,
                            liveMbps = 0f, progress = 0f, latencyMs = -1,
                            measuringLatency = false, testConfig = null
                        )
                }
            }
        }
    }

    /** 取消正在进行的测速，回到空闲状态 */
    fun cancelTest() {
        testJob?.cancel()
        _uiState.value = _uiState.value.copy(
            status = SpeedTestStatus.IDLE, liveMbps = 0f, progress = 0f, errorMsg = null,
            latencyMs = -1, measuringLatency = false, testConfig = null, result = null
        )
    }

    /** 重新测速（清除上次结果后立即开始） */
    fun restartTest() {
        startTest()
    }

    fun refreshNetwork() {
        if (_uiState.value.status == SpeedTestStatus.IDLE) {
            _uiState.value = _uiState.value.copy(networkType = networkHelper.getCurrentNetworkType())
        }
    }

    /** 打开 Wi-Fi 设置 */
    fun openWifiSettings() = networkHelper.openWifiSettings()

    /** 清除所有历史记录 */
    fun clearHistory() {
        componentScope.launch { repository.clearHistory() }
    }

    /** 查看历史结果，停止当前测速，但不重复写入记录或更改测速配置。 */
    fun selectHistoryRecord(record: SpeedTestRecord) {
        testJob?.cancel()
        testJob = null
        _uiState.value = _uiState.value.copy(
            status = SpeedTestStatus.DONE,
            result = record,
            networkType = record.networkType,
            latencyMs = record.latencyMs,
            liveMbps = record.downloadMbps,
            progress = 1f,
            measuringLatency = false,
            testConfig = null,
            errorMsg = null
        )
    }

    // ── 配置管理 ─────────────────────────────────────────────────────────────

    /** 切换激活配置（不影响进行中的测速） */
    fun selectConfig(id: Long) {
        componentScope.launch { configRepository.setActive(id) }
    }

    /** 新增自定义配置 */
    fun addConfig(name: String, testUrl: String, estimatedMb: Int, durationSeconds: Int) {
        componentScope.launch {
            val id = configRepository.insert(
                SpeedTestConfig(
                    name = name, testUrl = testUrl,
                    estimatedDataMb = estimatedMb, durationSeconds = durationSeconds
                )
            )
            // 新增后自动激活
            configRepository.setActive(id)
        }
    }

    /** 更新已有配置 */
    fun updateConfig(config: SpeedTestConfig) {
        componentScope.launch { configRepository.update(config) }
    }

    /** 删除配置（预设配置由 UI 层拦截，不会传到此处） */
    fun deleteConfig(id: Long) {
        componentScope.launch {
            val current = _uiState.value
            configRepository.deleteById(id)
            // 若删除的是激活配置，改为激活第一个
            if (current.config.id == id) {
                current.configList.firstOrNull { it.id != id }
                    ?.let { configRepository.setActive(it.id) }
            }
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit
        ): SpeedTestComponent
    }
}
