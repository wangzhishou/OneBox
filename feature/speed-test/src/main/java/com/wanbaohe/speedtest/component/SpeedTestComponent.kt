package com.wanbaohe.speedtest.component

import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.wanbaohe.speedtest.data.NetworkHelper
import com.wanbaohe.speedtest.data.SpeedTestConfig
import com.wanbaohe.speedtest.data.SpeedTestConfigRepository
import com.wanbaohe.speedtest.data.SpeedTestRepository
import com.wanbaohe.speedtest.domain.SpeedTestPhase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
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
        _uiState.value = _uiState.value.copy(
            status = SpeedTestStatus.MEASURING,
            liveMbps = 0f, progress = 0f, result = null,
            networkType = networkType, errorMsg = null
        )
        testJob = componentScope.launch {
            repository.startTest(_uiState.value.config, networkType).collect { phase ->
                when (phase) {
                    is SpeedTestPhase.MeasuringLatency -> {}
                    is SpeedTestPhase.Downloading ->
                        _uiState.value = _uiState.value.copy(
                            liveMbps = phase.liveMbps, progress = phase.progress
                        )
                    is SpeedTestPhase.Done -> {
                        repository.saveRecord(phase.record)
                        _uiState.value = _uiState.value.copy(
                            status = SpeedTestStatus.DONE,
                            result = phase.record,
                            liveMbps = phase.record.downloadMbps, progress = 1f
                        )
                    }
                    is SpeedTestPhase.Error ->
                        _uiState.value = _uiState.value.copy(
                            status = SpeedTestStatus.IDLE, errorMsg = phase.message
                        )
                }
            }
        }
    }

    /** 取消正在进行的测速，回到空闲状态 */
    fun cancelTest() {
        testJob?.cancel()
        _uiState.value = _uiState.value.copy(
            status = SpeedTestStatus.IDLE, liveMbps = 0f, progress = 0f, errorMsg = null
        )
    }

    /** 重新测速（清除上次结果后立即开始） */
    fun restartTest() {
        _uiState.value = _uiState.value.copy(status = SpeedTestStatus.IDLE, result = null)
        startTest()
    }

    /** 打开 Wi-Fi 设置 */
    fun openWifiSettings() = networkHelper.openWifiSettings()

    /** 清除所有历史记录 */
    fun clearHistory() {
        componentScope.launch { repository.clearHistory() }
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
