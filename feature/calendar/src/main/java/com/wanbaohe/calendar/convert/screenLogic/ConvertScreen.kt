package com.wanbaohe.calendar.convert.screenLogic

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.t8rin.imagetoolbox.core.ui.utils.capturable.capturable
import com.t8rin.imagetoolbox.core.ui.utils.capturable.rememberCaptureController
import com.wanbaohe.calendar.data.CalendarUiState
import com.wanbaohe.calendar.ui.ConversionTab
import kotlinx.coroutines.launch

@Composable
fun ConvertScreen(component: ConvertComponent) {
    val state by component.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val captureController = rememberCaptureController()

    val adaptedState = CalendarUiState(
        isConvertSolarToLunar = state.isSolarToLunar,
        convertYear = state.year,
        convertMonth = state.month,
        convertDay = state.day,
        convertHour = state.hour,
        isConvertLunarLeapMonth = state.isLunarLeapMonth,
        convertResult = state.convertResult,
        convertSolarResult = state.convertSolarResult,
        convertTimeSlot = state.timeSlot,
        convertFotoData = state.fotoData,
        convertTaoData = state.taoData,
    )

    ConversionTab(
        state = adaptedState,
        onUpdateDate = component::updateDate,
        onUpdateHour = component::updateHour,
        onConvert = component::performConversion,
        onToggleMode = component::toggleMode,
        onToggleLeapMonth = component::toggleLeapMonth,
        onShare = {
            scope.launch {
                component.shareBitmap(captureController.bitmap())
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .capturable(captureController)
    )
}