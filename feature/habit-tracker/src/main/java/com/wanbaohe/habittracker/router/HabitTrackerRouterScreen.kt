package com.wanbaohe.habittracker.router

import androidx.compose.runtime.Composable
import com.wanbaohe.habittracker.router.screenLogic.HabitTrackerRouterComponent
import com.wanbaohe.habittracker.screen.HabitEditScreen
import com.wanbaohe.habittracker.screen.HabitMainScreen

@Composable
fun HabitTrackerRouterScreen(component: HabitTrackerRouterComponent) {
    when (val child = component.child) {
        is HabitTrackerRouterComponent.HabitTrackerChild.Main -> HabitMainScreen(
            component = child.component,
        )

        is HabitTrackerRouterComponent.HabitTrackerChild.Edit -> HabitEditScreen(
            component = child.component,
        )
    }
}
