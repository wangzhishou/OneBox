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

package com.shifenmiao.marktodo.screenLogic

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class MarkTodoRouterComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val type: Screen.MarkTodoRouter.MarkTodoType?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val markTodoFactory: MarkTodoComponent.Factory,
    private val categoryDetailFactory: CategoryDetailComponent.Factory,
    private val addTodoFactory: AddTodoComponent.Factory,
    private val todoDetailFactory: TodoDetailComponent.Factory,
    private val addCategoryFactory: AddCategoryComponent.Factory,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    val child: MarkTodoChild = when (type) {
        null, is Screen.MarkTodoRouter.MarkTodoType.Dashboard -> MarkTodoChild.Dashboard(
            markTodoFactory(
                componentContext = componentContext.childContext("marktodo_dashboard"),
                onNavigate = onNavigate,
            )
        )

        is Screen.MarkTodoRouter.MarkTodoType.CategoryDetail -> MarkTodoChild.CategoryDetail(
            categoryDetailFactory(
                componentContext = componentContext.childContext("marktodo_category_detail"),
                categoryId = type.categoryId,
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )

        is Screen.MarkTodoRouter.MarkTodoType.AddTodo -> MarkTodoChild.AddTodo(
            addTodoFactory(
                componentContext = componentContext.childContext("marktodo_add_todo"),
                initialCategoryId = type.initialCategoryId,
                editingTaskId = type.editingTaskId,
                onNavigate = onNavigate,
                onGoBack = onGoBack,
            )
        )

        is Screen.MarkTodoRouter.MarkTodoType.AddCategory -> MarkTodoChild.AddCategory(
            addCategoryFactory(
                componentContext = componentContext.childContext("marktodo_add_category"),
                editingCategoryId = type.editingCategoryId,
                onGoBack = onGoBack,
            )
        )

        is Screen.MarkTodoRouter.MarkTodoType.TodoDetail -> MarkTodoChild.TodoDetail(
            todoDetailFactory(
                componentContext = componentContext.childContext("marktodo_todo_detail"),
                taskId = type.taskId,
                categoryId = type.categoryId,
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )
    }

    sealed interface MarkTodoChild {
        data class Dashboard(val component: MarkTodoComponent) : MarkTodoChild
        data class CategoryDetail(val component: CategoryDetailComponent) : MarkTodoChild
        data class AddTodo(val component: AddTodoComponent) : MarkTodoChild
        data class AddCategory(val component: AddCategoryComponent) : MarkTodoChild
        data class TodoDetail(val component: TodoDetailComponent) : MarkTodoChild
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            type: Screen.MarkTodoRouter.MarkTodoType?,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): MarkTodoRouterComponent
    }
}
