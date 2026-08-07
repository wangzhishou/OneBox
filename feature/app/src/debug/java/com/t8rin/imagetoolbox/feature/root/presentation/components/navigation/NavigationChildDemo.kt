package com.t8rin.imagetoolbox.feature.root.presentation.components.navigation

import androidx.compose.runtime.Composable
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.demo.screen.DemoScreen
import com.shifenmiao.demo.screenLogic.DemoComponent

class Demo(
    private val appComponent: AppComponent,
    private val demoComponent: DemoComponent
) : NavigationChild {
    @Composable
    override fun Content() = DemoScreen(
        onGoBack = appComponent.onGoBack,
        onNavigate = appComponent.onNavigate,
        demoComponent = demoComponent
    )
}
