package com.wanbaohe.bookkeeping.router.screenLogic

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.bookkeeping.component.BookkeepingComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class BookkeepingRouterComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val type: Screen.Bookkeeping.Type?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val bookkeepingComponentFactory: BookkeepingComponent.Factory,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    val child: BookkeepingChild = when (type) {
        null -> BookkeepingChild.Main(
            bookkeepingComponentFactory(
                componentContext = componentContext.childContext("bookkeeping_main"),
                onGoBack = onGoBack,
                onNavigate = onNavigate,
                editingRecordId = null,
            )
        )

        is Screen.Bookkeeping.Type.AddRecord -> BookkeepingChild.AddRecord(
            bookkeepingComponentFactory(
                componentContext = componentContext.childContext("bookkeeping_add"),
                onGoBack = onGoBack,
                onNavigate = onNavigate,
                editingRecordId = type.editingRecordId,
            )
        )
    }

    sealed interface BookkeepingChild {
        class Main(val component: BookkeepingComponent) : BookkeepingChild
        class AddRecord(val component: BookkeepingComponent) : BookkeepingChild
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            type: Screen.Bookkeeping.Type?,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): BookkeepingRouterComponent
    }
}
