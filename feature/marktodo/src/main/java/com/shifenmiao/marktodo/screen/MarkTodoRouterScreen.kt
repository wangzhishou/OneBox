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
