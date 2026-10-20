package com.wanbaohe.period.router.screenLogic

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.period.component.PeriodComponent
import com.wanbaohe.period.component.PeriodDetailComponent
import com.wanbaohe.period.component.PeriodEditorComponent
import com.wanbaohe.period.model.PeriodTab
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class PeriodRouterComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val type: Screen.Period.Type?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val periodComponentFactory: PeriodComponent.Factory,
    private val periodEditorComponentFactory: PeriodEditorComponent.Factory,
    private val periodDetailComponentFactory: PeriodDetailComponent.Factory,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    val child: PeriodChild = when (type) {
        null -> PeriodChild.Main(
            periodComponentFactory(
                componentContext = componentContext.childContext("period_main"),
                onGoBack = onGoBack,
                onNavigate = onNavigate,
                initialTab = null,
            )
        )

        Screen.Period.Type.Add -> PeriodChild.Editor(
            periodEditorComponentFactory(
                componentContext = componentContext.childContext("period_add"),
                onGoBack = onGoBack,
                editingRecordId = null,
            )
        )

        is Screen.Period.Type.Edit -> PeriodChild.Editor(
            periodEditorComponentFactory(
                componentContext = componentContext.childContext("period_edit"),
                onGoBack = onGoBack,
                editingRecordId = type.recordId,
            )
        )

        is Screen.Period.Type.Detail -> PeriodChild.Detail(
            periodDetailComponentFactory(
                componentContext = componentContext.childContext("period_detail"),
                onGoBack = onGoBack,
                onNavigate = onNavigate,
                recordId = type.recordId,
            )
        )

        Screen.Period.Type.Stats -> PeriodChild.Main(
            periodComponentFactory(
                componentContext = componentContext.childContext("period_stats"),
                onGoBack = onGoBack,
                onNavigate = onNavigate,
                initialTab = PeriodTab.STATS,
            )
        )
    }

    sealed interface PeriodChild {
        class Main(val component: PeriodComponent) : PeriodChild
        class Editor(val component: PeriodEditorComponent) : PeriodChild
        class Detail(val component: PeriodDetailComponent) : PeriodChild
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            type: Screen.Period.Type?,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): PeriodRouterComponent
    }
}
