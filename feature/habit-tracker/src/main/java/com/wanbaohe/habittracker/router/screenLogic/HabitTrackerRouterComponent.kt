package com.wanbaohe.habittracker.router.screenLogic

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.habittracker.component.HabitEditComponent
import com.wanbaohe.habittracker.component.HabitTrackerComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

/**
 * 习惯打卡 Router — 按 [Screen.HabitTracker.Type] 分发到主页 / 编辑页。
 */
class HabitTrackerRouterComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val type: Screen.HabitTracker.Type?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val habitTrackerComponentFactory: HabitTrackerComponent.Factory,
    private val habitEditComponentFactory: HabitEditComponent.Factory,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    val child: HabitTrackerChild = when (type) {
        null, is Screen.HabitTracker.Type.Main -> HabitTrackerChild.Main(
            habitTrackerComponentFactory(
                componentContext = componentContext.childContext("habit_main"),
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )

        is Screen.HabitTracker.Type.Edit -> HabitTrackerChild.Edit(
            habitEditComponentFactory(
                componentContext = componentContext.childContext("habit_edit"),
                onGoBack = onGoBack,
                habitId = type.habitId,
            )
        )
    }

    sealed interface HabitTrackerChild {
        class Main(val component: HabitTrackerComponent) : HabitTrackerChild
        class Edit(val component: HabitEditComponent) : HabitTrackerChild
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            type: Screen.HabitTracker.Type?,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): HabitTrackerRouterComponent
    }
}
