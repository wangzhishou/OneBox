package com.wanbaohe.measurement.component

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.wanbaohe.measurement.data.sensor.LevelSensorSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val LOW_PASS_ALPHA = 0.08f

@Immutable
data class MeasurementUiState(
    val selectedTab: MeasurementTab = MeasurementTab.LEVEL,
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val isSensorAvailable: Boolean = true,
    val isLocked: Boolean = false,
    val rulerUnit: RulerUnit = RulerUnit.CM,
    val ppi: Float = 0f,
)

enum class MeasurementTab {
    LEVEL, RULER
}

enum class RulerUnit {
    CM, INCH
}

class MeasurementComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    @ApplicationContext private val context: Context,
    private val sensorSource: LevelSensorSource
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(
        MeasurementUiState(
            isSensorAvailable = sensorSource.isAvailable,
            ppi = calculatePpi()
        )
    )
    val uiState = _uiState.asStateFlow()

    private var smoothedPitch: Float = 0f
    private var smoothedRoll: Float = 0f

    init {
        if (sensorSource.isAvailable) {
            sensorSource.levelFlow()
                .onEach { reading ->
                    if (!_uiState.value.isLocked) {
                        smoothedPitch = lowPassFilter(reading.pitch, smoothedPitch)
                        smoothedRoll = lowPassFilter(reading.roll, smoothedRoll)
                        _uiState.value = _uiState.value.copy(
                            pitch = smoothedPitch,
                            roll = smoothedRoll
                        )
                    }
                }
                .catch { }
                .launchIn(componentScope)
        }
    }

    fun selectTab(tab: MeasurementTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun toggleLock() {
        _uiState.value = _uiState.value.copy(isLocked = !_uiState.value.isLocked)
    }

    fun toggleRulerUnit() {
        _uiState.value = _uiState.value.copy(
            rulerUnit = if (_uiState.value.rulerUnit == RulerUnit.CM) RulerUnit.INCH else RulerUnit.CM
        )
    }

    private fun lowPassFilter(newVal: Float, prevVal: Float): Float {
        return prevVal + LOW_PASS_ALPHA * (newVal - prevVal)
    }

    private fun calculatePpi(): Float {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        val xdpi = metrics.xdpi
        val ydpi = metrics.ydpi
        return (xdpi + ydpi) / 2f
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit
        ): MeasurementComponent
    }
}
