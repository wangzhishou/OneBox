package com.wanbaohe.calendar.bazi.screenLogic

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.shifenmiao.base.utils.ActionUtils
import com.t8rin.imagetoolbox.core.ui.utils.capturable.capturable
import com.t8rin.imagetoolbox.core.ui.utils.capturable.rememberCaptureController
import com.wanbaohe.calendar.data.CalendarUiState
import com.wanbaohe.calendar.ui.BaZiTab
import com.wanbaohe.calendar.ui.BaZiTabSkeleton
import kotlinx.coroutines.launch

@Composable
fun BaZiScreen(component: BaZiComponent) {
    val state by component.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val captureController = rememberCaptureController()

    val adaptedState = CalendarUiState(
        baZiYear = state.year,
        baZiMonth = state.month,
        baZiDay = state.day,
        baZiHour = state.hour,
        baZiData = state.baZiData,
        daYunList = state.daYunList,
        fortuneData = state.fortuneData,
        isDataReady = state.isDataReady,
    )

    if (!state.isDataReady) {
        BaZiTabSkeleton(modifier = Modifier.fillMaxSize())
    } else {
        BaZiTab(
            state = adaptedState,
            onSelectDate = component::updateDate,
            onSelectHour = component::updateHour,
            onShare = {
                scope.launch {
                    component.shareBitmap(captureController.bitmap())
                }
            },
            onAiBaZiClick = {
                ActionUtils.showLogin {
                    component.openBaZiAI()
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .capturable(captureController)
        )
    }
}