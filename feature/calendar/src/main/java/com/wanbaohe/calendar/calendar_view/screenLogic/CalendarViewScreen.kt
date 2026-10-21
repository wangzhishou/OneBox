package com.wanbaohe.calendar.calendar_view.screenLogic

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.wanbaohe.calendar.data.CalendarUiState
import com.wanbaohe.calendar.ui.CalendarTab
import com.wanbaohe.calendar.ui.CalendarTabSkeleton
import com.wanbaohe.core.ui.utils.navigation.Screen

@Composable
fun CalendarViewScreen(component: CalendarViewComponent) {
    val state by component.uiState.collectAsState()

    val adaptedState = CalendarUiState(
        currentYear = state.currentYear,
        currentMonth = state.currentMonth,
        selectedDay = state.selectedDay,
        lunarDate = state.lunarDate,
        yiJi = state.yiJi,
        timeSlots = state.timeSlots,
        fotoData = state.fotoData,
        taoData = state.taoData,
        calendarDays = state.calendarDays,
        nextSolarTerm = state.nextSolarTerm,
        upcomingFestivalItems = state.upcomingFestivalItems,
        isDataReady = state.isDataReady,
    )

    if (!state.isDataReady) {
        CalendarTabSkeleton(modifier = Modifier.fillMaxSize())
    } else {
        CalendarTab(
            state = adaptedState,
            onSelectDate = component::selectDate,
            onPreviousMonth = component::previousMonth,
            onNextMonth = component::nextMonth,
            onReset = component::goToToday,
            onShare = {
                component.onNavigate(
                    Screen.CalendarShare(
                        year = state.currentYear,
                        month = state.currentMonth,
                        day = state.selectedDay,
                    )
                )
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
