package com.wanbaohe.diceroller.component

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.wanbaohe.diceroller.data.DiceHistoryStorage
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 摇晃检测的合力加速度阈值（m/s²），值越小越灵敏 */
private const val SHAKE_THRESHOLD = 15f

/** 摇晃防抖冷却时间（ms），避免一次摇晃触发多次 */
private const val SHAKE_COOLDOWN_MS = 900L

/** 骰子翻滚动效持续时间（ms），与 UI 层动画保持一致 */
private const val ROLL_ANIM_MS = 800L

/**
 * 投骰子业务逻辑组件
 *
 * 职责：
 * 1. 维护 [DiceRollerUiState] 并通过 [uiState] 暴露给 Compose
 * 2. 注册加速度传感器，检测摇晃并触发 [roll]
 * 3. 将投掷结果写入 MMKV 历史
 * 4. 提供 UI 交互方法（[roll]、[setDiceType]、[setDiceCount]、[clearHistory]）
 */
class DiceRollerComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(
        DiceRollerUiState(
            diceType = DiceHistoryStorage.loadDiceType(),
            diceCount = DiceHistoryStorage.loadDiceCount(),
            history = DiceHistoryStorage.loadHistory()
        )
    )
    val uiState = _uiState.asStateFlow()

    // ─── 传感器 ──────────────────────────────────────────────────────────────

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastShakeMs = 0L

    private val shakeListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            // 合力（去掉重力影响近似处理）
            val force = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val now = System.currentTimeMillis()
            if (force > SHAKE_THRESHOLD && now - lastShakeMs > SHAKE_COOLDOWN_MS) {
                lastShakeMs = now
                roll()
            }
        }
    }

    init {
        accelerometer?.let {
            sensorManager.registerListener(
                shakeListener, it, SensorManager.SENSOR_DELAY_GAME
            )
        }
        // 组件销毁时注销传感器，防止内存泄漏
        componentContext.lifecycle.doOnDestroy {
            sensorManager.unregisterListener(shakeListener)
        }
    }

    // ─── 公开交互接口 ─────────────────────────────────────────────────────────

    /** 投掷骰子：生成随机点数，触发动效，并写入历史 */
    fun roll() {
        if (_uiState.value.isRolling) return
        componentScope.launch {
            // 1. 标记动效开始
            _uiState.update { it.copy(isRolling = true) }
            // 2. 等待动效结束后再更新点数，让动效中保持旧值显示
            delay(ROLL_ANIM_MS)
            val type = _uiState.value.diceType
            val count = _uiState.value.diceCount
            val results = List(count) { DiceResult(type, type.roll()) }
            val record = RollRecord(dice = results)
            DiceHistoryStorage.appendRecord(record)
            _uiState.update { state ->
                state.copy(
                    isRolling = false,
                    currentRoll = results,
                    history = DiceHistoryStorage.loadHistory()
                )
            }
        }
    }

    /** 切换骰子类型，自动持久化 */
    fun setDiceType(type: DiceType) {
        DiceHistoryStorage.saveDiceType(type)
        _uiState.update { it.copy(diceType = type) }
    }

    /** 调整骰子数量（1-[DiceRollerUiState.MAX_DICE]），自动持久化 */
    fun setDiceCount(count: Int) {
        val clamped = count.coerceIn(1, DiceRollerUiState.MAX_DICE)
        DiceHistoryStorage.saveDiceCount(clamped)
        _uiState.update { it.copy(diceCount = clamped) }
    }

    /** 展开/收起历史面板 */
    fun toggleHistory() {
        _uiState.update { it.copy(showHistory = !it.showHistory) }
    }

    /** 清空历史记录 */
    fun clearHistory() {
        DiceHistoryStorage.clearHistory()
        _uiState.update { it.copy(history = emptyList()) }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit
        ): DiceRollerComponent
    }
}

