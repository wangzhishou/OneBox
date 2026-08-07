package com.wanbaohe.bookkeeping.router

import androidx.compose.runtime.Composable
import com.wanbaohe.bookkeeping.router.screenLogic.BookkeepingRouterComponent
import com.wanbaohe.bookkeeping.screen.AddRecordScreen
import com.wanbaohe.bookkeeping.screen.BookkeepingScreen

@Composable
fun BookkeepingRouterScreen(component: BookkeepingRouterComponent) {
    when (val child = component.child) {
        is BookkeepingRouterComponent.BookkeepingChild.Main -> BookkeepingScreen(
            component = child.component,
            onGoBack = child.component.onGoBack,
        )
        is BookkeepingRouterComponent.BookkeepingChild.AddRecord -> AddRecordScreen(
            component = child.component,
        )
    }
}
