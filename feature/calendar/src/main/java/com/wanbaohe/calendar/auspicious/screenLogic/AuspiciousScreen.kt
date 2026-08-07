package com.wanbaohe.calendar.auspicious.screenLogic

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.wanbaohe.calendar.data.CalendarUiState
import com.wanbaohe.calendar.ui.AuspiciousDayTab

@Composable
fun AuspiciousScreen(component: AuspiciousComponent) {
    val state by component.uiState.collectAsState()

    val adaptedState = CalendarUiState(
        isAvoidMode = state.isAvoidMode,
        selectedAuspiciousItems = state.selectedItems,
        auspiciousDayResults = state.results,
        isAuspiciousLoading = state.isLoading,
    )

    AuspiciousDayTab(
        state = adaptedState,
        onToggleItem = component::toggleItem,
        onToggleMode = component::toggleAvoidMode,
        onDayClick = component::navigateToCalendar,
        modifier = Modifier.fillMaxSize()
    )
}