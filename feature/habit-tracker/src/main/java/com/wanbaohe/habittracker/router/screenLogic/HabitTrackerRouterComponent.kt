/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.habittracker.router.screenLogic

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
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
