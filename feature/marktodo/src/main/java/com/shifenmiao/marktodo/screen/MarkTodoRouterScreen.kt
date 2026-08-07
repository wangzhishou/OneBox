package com.shifenmiao.marktodo.screen

import androidx.compose.runtime.Composable
import com.shifenmiao.marktodo.screenLogic.MarkTodoRouterComponent

@Composable
fun MarkTodoRouterScreen(
    component: MarkTodoRouterComponent,
    onGoBack: () -> Unit
) {
    when (val child = component.child) {
        is MarkTodoRouterComponent.MarkTodoChild.Dashboard -> MarkTodoScreen(
            markTodoComponent = child.component,
            onGoBack = onGoBack
        )

        is MarkTodoRouterComponent.MarkTodoChild.CategoryDetail -> CategoryDetailScreen(
            component = child.component,
            onGoBack = onGoBack
        )

        is MarkTodoRouterComponent.MarkTodoChild.AddTodo -> AddTodoScreen(
            addTodoComponent = child.component,
            onGoBack = onGoBack
        )

        is MarkTodoRouterComponent.MarkTodoChild.AddCategory -> AddCategoryScreen(
            component = child.component,
            onGoBack = onGoBack
        )

        is MarkTodoRouterComponent.MarkTodoChild.TodoDetail -> TodoDetailScreen(
            component = child.component,
            onGoBack = onGoBack
        )
    }
}
