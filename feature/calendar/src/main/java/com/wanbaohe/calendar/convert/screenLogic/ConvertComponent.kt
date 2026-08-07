package com.wanbaohe.calendar.convert.screenLogic

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.calendar.data.FotoData
import com.wanbaohe.calendar.data.LunarCalendarCalculator
import com.wanbaohe.calendar.data.TaoData
import com.wanbaohe.calendar.data.getChineseHourSlot
import com.wanbaohe.calendar.data.LunarDate
import com.wanbaohe.calendar.data.LunarJavaBridge
import com.wanbaohe.calendar.data.LunarTimeSlot
import com.wanbaohe.calendar.data.SolarDate
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * 历法转换子组件状态
 */
@Immutable
data class ConvertViewState(
    val isSolarToLunar: Boolean = true,
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val month: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val day: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
    val hour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
    val isLunarLeapMonth: Boolean = false,
    val convertResult: LunarDate? = null,
    val convertSolarResult: SolarDate? = null,
    val timeSlot: LunarTimeSlot? = null,
    val fotoData: FotoData? = null,
    val taoData: TaoData? = null,
)

/**
 * 历法转换子组件
 *
 * 职责：公历 ↔ 农历双向转换、时辰、闰月
 */
class ConvertComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val isSolarToLunar: Boolean,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val shareProvider: ImageShareProvider<Bitmap>,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(
        ConvertViewState(isSolarToLunar = isSolarToLunar)
    )
    val uiState = _uiState.asStateFlow()

    /** 更新输入日期 */
    fun updateDate(year: Int, month: Int, day: Int) {
        _uiState.value = _uiState.value.copy(
            year = year, month = month, day = day
        )
    }

    /** 更新输入时辰 */
    fun updateHour(hour: Int) {
        _uiState.value = _uiState.value.copy(hour = hour.coerceIn(0, 23))
    }

    /** 切换转换模式 */
    fun toggleMode() {
        _uiState.value = _uiState.value.copy(
            isSolarToLunar = !_uiState.value.isSolarToLunar,
            convertResult = null,
            convertSolarResult = null,
            timeSlot = null
        )
    }

    /** 切换闰月 */
    fun toggleLeapMonth() {
        _uiState.value = _uiState.value.copy(
            isLunarLeapMonth = !_uiState.value.isLunarLeapMonth,
            convertResult = null,
            convertSolarResult = null,
            timeSlot = null
        )
    }

    /** 执行转换 */
    fun performConversion() {
        componentScope.launch {
            val state = _uiState.value
            if (state.isSolarToLunar) {
                val (result, slot, foto, tao) = withContext(defaultDispatcher) {
                    val r = LunarCalendarCalculator.solarToLunar(
                        state.year, state.month, state.day
                    )
                    val timeSlots = LunarJavaBridge.getTimeSlots(
                        state.year, state.month, state.day
                    )
                    val s = timeSlots.getOrNull(
                        ((state.hour + 1) / 2) % timeSlots.size.coerceAtLeast(1)
                    )
                    val f = LunarJavaBridge.getFotoData(
                        state.year, state.month, state.day
                    )
                    val t = LunarJavaBridge.getTaoData(
                        state.year, state.month, state.day
                    )
                    ConvertResult4(r, s, f, t)
                }
                _uiState.value = _uiState.value.copy(
                    convertResult = result,
                    convertSolarResult = null,
                    timeSlot = slot,
                    fotoData = foto,
                    taoData = tao
                )
            } else {
                val (convertedLunar, solar, slot, foto, tao) = withContext(defaultDispatcher) {
                    val s = LunarJavaBridge.lunarToSolarDate(
                        state.year,
                        state.month,
                        state.day,
                        state.isLunarLeapMonth
                    )
                    if (s == null) {
                        ConvertResult5(null, null, null, null, null)
                    } else {
                        val l = LunarCalendarCalculator.solarToLunar(s.year, s.month, s.day)
                        val timeSlots = LunarJavaBridge.getTimeSlots(s.year, s.month, s.day)
                        val t = timeSlots.getOrNull(
                            ((state.hour + 1) / 2) % timeSlots.size.coerceAtLeast(1)
                        )
                        val f = LunarJavaBridge.getFotoData(s.year, s.month, s.day)
                        val ta = LunarJavaBridge.getTaoData(s.year, s.month, s.day)
                        ConvertResult5(l, s, t, f, ta)
                    }
                }
                _uiState.value = _uiState.value.copy(
                    convertResult = convertedLunar,
                    convertSolarResult = solar,
                    timeSlot = slot,
                    fotoData = foto,
                    taoData = tao
                )
            }
        }
    }

    /** 截图分享 */
    fun shareBitmap(bitmap: Bitmap, onComplete: () -> Unit = {}) {
        componentScope.launch {
            shareProvider.shareImage(
                imageInfo = ImageInfo(
                    width = bitmap.width,
                    height = bitmap.height,
                    imageFormat = ImageFormat.Png.Lossless
                ),
                image = bitmap,
                onComplete = onComplete
            )
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            isSolarToLunar: Boolean = true,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): ConvertComponent
    }
}

private data class ConvertResult4(
    val lunarDate: LunarDate?,
    val timeSlot: LunarTimeSlot?,
    val fotoData: FotoData?,
    val taoData: TaoData?
)

private data class ConvertResult5(
    val lunarDate: LunarDate?,
    val solarDate: SolarDate?,
    val timeSlot: LunarTimeSlot?,
    val fotoData: FotoData?,
    val taoData: TaoData?
)
