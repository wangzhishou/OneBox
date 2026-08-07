package com.t8rin.imagetoolbox.feature.root.presentation.components.navigation

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.common.logic.AppComponent
import javax.inject.Inject

class DemoScreenProviderImpl @Inject constructor() : DemoScreenProvider {
    override fun createChild(
        appComponent: AppComponent,
        componentContext: ComponentContext
    ): NavigationChild? = null
}
