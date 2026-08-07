package com.wanbaohe.app.app_screen

import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.model.HomeTabKey
import com.shifenmiao.online.component.ItemListComponent
import com.shifenmiao.online.component.PlaygroundComponent
import com.shifenmiao.online.screen.HomeContent
import com.t8rin.imagetoolbox.core.domain.performance.StartupTrace
import com.wanbaohe.app.components.MainLayout

@Composable
fun NewAppScreen(
    appComponent: AppComponent,
    itemListComponent: ItemListComponent,
    playgroundComponent: PlaygroundComponent,
    initialTab: HomeTabKey? = null,
) {
    val topAppBarState = rememberTopAppBarState()
    var initializedCollapsed by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        StartupTrace.markOnce("new_app_screen_composed", "NewAppScreen.composed")
    }

    LaunchedEffect(topAppBarState.heightOffsetLimit) {
        if (!initializedCollapsed && topAppBarState.heightOffsetLimit < 0f) {
            topAppBarState.heightOffset = topAppBarState.heightOffsetLimit
            initializedCollapsed = true
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)
    MainLayout(
        appComponent = appComponent,
        scrollBehavior = scrollBehavior,
        topAppBarState = topAppBarState
    ) {
        HomeContent(
            itemListComponent = itemListComponent,
            playgroundComponent = playgroundComponent,
            onGoBack = appComponent.onGoBack,
            initialTab = initialTab,
        )
    }
}