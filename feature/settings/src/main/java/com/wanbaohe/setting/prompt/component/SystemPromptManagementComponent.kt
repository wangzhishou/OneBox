package com.wanbaohe.setting.prompt.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.shifenmiao.model.Source
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SystemPromptManagementComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    appDatabase: AppDatabase,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    val systemPrompts: StateFlow<List<PromptEntity>> =
        appDatabase.chatPromptDao()
            .getPromptsBySourceFlow(Source.SYSTEM)
            .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): SystemPromptManagementComponent
    }
}
