package com.t8rin.imagetoolbox.feature.root.presentation.components.navigation

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.common.logic.AppComponent

interface DemoScreenProvider {
    fun createChild(
        appComponent: AppComponent,
        componentContext: ComponentContext
    ): NavigationChild?
}
