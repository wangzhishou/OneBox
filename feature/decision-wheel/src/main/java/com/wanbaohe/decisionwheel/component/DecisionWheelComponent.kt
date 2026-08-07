package com.wanbaohe.decisionwheel.component

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.wanbaohe.decisionwheel.data.DecisionWheelPresetsProvider
import com.wanbaohe.decisionwheel.data.WheelRepository
import com.shifenmiao.database.decision_wheel.entity.WheelHistoryEntity
import com.shifenmiao.theme.AppTheme
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID
import com.wanbaohe.com.color.ColorGenerator

/**
 * 转盘选项数据
 */
@Immutable
data class WheelOption(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: Color
)

/**
 * 转盘配置数据
 */
@Immutable
data class DecisionWheel(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val options: List<WheelOption>,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * UI状态
 */
@Immutable
data class DecisionWheelUiState(
    val currentWheel: DecisionWheel? = null,
    val isSpinning: Boolean = false,
    val selectedOption: WheelOption? = null,
    val showResult: Boolean = false,
    val savedWheels: List<DecisionWheel> = emptyList(),
    val showWheelList: Boolean = false,
    val showEditDialog: Boolean = false,
    val showHistory: Boolean = false,
    val historyList: List<WheelHistoryEntity> = emptyList()
)

class DecisionWheelComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    dispatchersHolder: DispatchersHolder,
    private val repository: WheelRepository,
    private val presetsProvider: DecisionWheelPresetsProvider
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(DecisionWheelUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // 初始化：从数据库加载或创建默认转盘
        initWheel()
        loadSavedWheels()
        loadHistory()
    }

    private fun initWheel() {
        componentScope.launch {
            val wheels = repository.getAllWheels().firstOrNull()
            val currentWheel = if (wheels.isNullOrEmpty()) {
                // 创建并保存默认转盘
                createPresets()
                repository.getAllWheels().firstOrNull()?.firstOrNull()
            } else {
                wheels.first()
            }

            _uiState.emit(
                _uiState.value.copy(currentWheel = currentWheel)
            )
        }
    }

    private fun loadSavedWheels() {
        componentScope.launch {
            repository.getAllWheels().collect { wheels ->
                _uiState.emit(
                    _uiState.value.copy(savedWheels = wheels)
                )
            }
        }
    }

    private fun loadHistory() {
        componentScope.launch {
            repository.getAllHistory().collect { history ->
                _uiState.emit(
                    _uiState.value.copy(historyList = history)
                )
            }
        }
    }

    /**
     * 创建预置转盘。
     *
     * 注意：
     * - 预置标题/选项名称全部来自 `strings.xml`（通过 [DecisionWheelPresetsProvider] 获取）。
     * - 预置颜色按“基色 -> 互补色 -> 混合色”规则生成，并直接落库到每个 [WheelOption.color]。
     *   其中基色由 `listColor` 随机挑选，随后根据选项数量生成对应数量的混合色。
     */
    private suspend fun createPresets() {
        val listColor = mutableListOf<Color>(
            Color(0xFFA3F6F6),
            Color(0xFFF5A3F6),
            Color(0xFFD8F6A3),
            Color(0xFFF6D0A3),
            Color(0xFFA3B7F6),
            Color(0xFFB3F2EA)
        )

        val presets = presetsProvider.presets().map { preset ->
            val baseColor = listColor.randomOrNull() ?: AppTheme.colorScheme.primaryContainer
            val backgrounds = ColorGenerator.generateSegmentBackgrounds(
                baseColor = baseColor,
                count = preset.options.size
            )

            DecisionWheel(
                title = preset.title,
                options = preset.options.mapIndexed { index, optionName ->
                    val bg = backgrounds.getOrNull(index) ?: baseColor
                    WheelOption(name = optionName, color = bg)
                }
            )
        }

        presets.forEach { repository.saveWheel(it) }
    }

    /**
     * 开始旋转
     */
    fun startSpinning() {
        if (_uiState.value.isSpinning) return

        componentScope.launch {
            _uiState.emit(
                _uiState.value.copy(
                    isSpinning = true,
                    showResult = false,
                    selectedOption = null
                )
            )
        }
    }

    /**
     * 旋转停止，显示结果
     */
    fun onSpinComplete(selectedOption: WheelOption) {
        componentScope.launch {
            val currentWheel = _uiState.value.currentWheel
            if (currentWheel != null) {
                // 保存历史记录
                repository.saveHistory(currentWheel.id, selectedOption)
                // 更新使用记录
                repository.updateWheelUsage(currentWheel.id)
            }

            _uiState.emit(
                _uiState.value.copy(
                    isSpinning = false,
                    selectedOption = selectedOption,
                    showResult = true
                )
            )
        }
    }

    /**
     * 重新旋转
     */
    fun resetWheel() {
        componentScope.launch {
            _uiState.emit(
                _uiState.value.copy(
                    showResult = false,
                    selectedOption = null
                )
            )
        }
    }

    /**
     * 切换转盘
     */
    fun switchWheel(wheel: DecisionWheel) {
        componentScope.launch {
            _uiState.emit(
                _uiState.value.copy(
                    currentWheel = wheel,
                    showWheelList = false,
                    showResult = false,
                    selectedOption = null
                )
            )
        }
    }

    /**
     * 显示/隐藏转盘列表
     */
    fun toggleWheelList() {
        componentScope.launch {
            _uiState.emit(
                _uiState.value.copy(showWheelList = !_uiState.value.showWheelList)
            )
        }
    }

    /**
     * 显示/隐藏编辑对话框
     */
    fun toggleEditDialog() {
        componentScope.launch {
            _uiState.emit(
                _uiState.value.copy(showEditDialog = !_uiState.value.showEditDialog)
            )
        }
    }

    fun toggleHistory() {
        componentScope.launch {
            _uiState.emit(
                _uiState.value.copy(showHistory = !_uiState.value.showHistory)
            )
        }
    }

    fun clearHistory() {
        componentScope.launch {
            repository.clearHistory()
        }
    }

    /**
     * 添加自定义选项
     */
    fun addOption(name: String, color: Color) {
        componentScope.launch {
            val currentWheel = _uiState.value.currentWheel ?: return@launch
            val newOption = WheelOption(name = name, color = color)
            val updatedWheel = currentWheel.copy(
                options = currentWheel.options + newOption
            )
            repository.updateWheel(updatedWheel)
            _uiState.emit(
                _uiState.value.copy(currentWheel = updatedWheel)
            )
        }
    }

    /**
     * 删除选项
     */
    fun removeOption(optionId: String) {
        componentScope.launch {
            val currentWheel = _uiState.value.currentWheel ?: return@launch
            if (currentWheel.options.size <= 2) return@launch // 至少保留2个选项

            val updatedWheel = currentWheel.copy(
                options = currentWheel.options.filter { it.id != optionId }
            )
            repository.updateWheel(updatedWheel)
            _uiState.emit(
                _uiState.value.copy(currentWheel = updatedWheel)
            )
        }
    }

    /**
     * 创建新转盘
     */
    fun createWheel(title: String, options: List<WheelOption>) {
        componentScope.launch {
            val currentWheel = _uiState.value.currentWheel
            if (currentWheel != null) {
                // 始终更新当前转盘（通过 id），避免因标题变化/一致性问题导致误创建新转盘
                val updatedWheel = currentWheel.copy(
                    title = title,
                    options = options
                )
                repository.updateWheel(updatedWheel)
                _uiState.emit(
                    _uiState.value.copy(
                        currentWheel = updatedWheel,
                        showEditDialog = false
                    )
                )
            } else {
                // 当前转盘为空时，创建新转盘
                val newWheel = DecisionWheel(
                    title = title,
                    options = options
                )
                repository.saveWheel(newWheel)
                _uiState.emit(
                    _uiState.value.copy(
                        currentWheel = newWheel,
                        showEditDialog = false
                    )
                )
            }
        }
    }

    fun createNewWheel() {
        componentScope.launch {
            val baseColor = Color(AppTheme.colorScheme.primaryContainer.toArgb())
            val optionNames = presetsProvider.defaultNewWheelOptionNames()

            // 使用 ColorGenerator 自动分配颜色
            val colors = ColorGenerator.generateSegmentBackgrounds(
                baseColor = baseColor,
                count = optionNames.size
            )

            val newWheel = DecisionWheel(
                title = presetsProvider.defaultNewWheelTitle(),
                options = optionNames.mapIndexed { index, name ->
                    WheelOption(
                        name = name,
                        color = colors.getOrNull(index) ?: baseColor
                    )
                }
            )
            repository.saveWheel(newWheel)
            _uiState.emit(
                _uiState.value.copy(
                    currentWheel = newWheel,
                    showEditDialog = true
                )
            )
        }
    }

    /**
     * 删除转盘
     */
    fun deleteWheel(wheelId: String) {
        componentScope.launch {
            repository.deleteWheel(wheelId)
            // 如果删除的是当前转盘，切换到第一个
            if (_uiState.value.currentWheel?.id == wheelId) {
                val wheels = repository.getAllWheels().firstOrNull()
                _uiState.emit(
                    _uiState.value.copy(
                        currentWheel = wheels?.firstOrNull()
                    )
                )
            }
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext
        ): DecisionWheelComponent
    }
}