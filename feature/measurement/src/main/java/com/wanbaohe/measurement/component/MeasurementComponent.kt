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

package com.wanbaohe.measurement.component

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
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
    val isCameraEnabled: Boolean = false,
)

enum class MeasurementTab {
    LEVEL, RULER, ANGLE
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

    fun setCameraEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isCameraEnabled = enabled)
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
