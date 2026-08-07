package com.wanbaohe.setting.display.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.shifenmiao.common.logic.AppComponent
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.feature.settings.presentation.screenLogic.SettingsComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class DisplaySettingsComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    @Assisted val appComponent: AppComponent,
    private val settingsComponentFactory: SettingsComponent.Factory,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    val appComponentProxy: AppComponent get() = appComponent

    val settingsComponent: SettingsComponent = settingsComponentFactory(
        componentContext = componentContext.childContext("display_settings_inner"),
        onNavigate = onNavigate,
        isUpdateAvailable = MutableValue(false),
        onGoBack = null,
        initialSearchQuery = "",
    )

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
            appComponent: AppComponent,
        ): DisplaySettingsComponent
    }
}
