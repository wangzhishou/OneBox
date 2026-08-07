package com.t8rin.imagetoolbox.feature.root.presentation.components.navigation

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.demo.screenLogic.DemoComponent
import javax.inject.Inject

class DemoScreenProviderImpl @Inject constructor(
    private val demoComponentFactory: DemoComponent.Factory
) : DemoScreenProvider {
    override fun createChild(
        appComponent: AppComponent,
        componentContext: ComponentContext
    ): NavigationChild = Demo(
        appComponent = appComponent,
        demoComponent = demoComponentFactory(componentContext = componentContext)
    )
}
