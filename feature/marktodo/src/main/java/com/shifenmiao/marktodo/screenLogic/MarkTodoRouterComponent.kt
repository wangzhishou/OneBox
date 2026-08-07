package com.shifenmiao.marktodo.screenLogic

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
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
