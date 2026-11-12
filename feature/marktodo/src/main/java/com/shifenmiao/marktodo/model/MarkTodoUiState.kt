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

package com.shifenmiao.marktodo.model

import androidx.compose.runtime.Immutable

@Immutable
data class MarkTodoUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val statusTab: TaskCompletionFilter = TaskCompletionFilter.ALL,
    val searchQuery: String = "",
    val filter: TaskFilter = TaskFilter(),
    val sort: TaskSortConfig = TaskSortConfig(),
    val pendingCompletionIds: Set<String> = emptySet(),
    val isFilterSortSheetVisible: Boolean = false,
    val isDeletingCategory: Boolean = false,
    val isReorderMode: Boolean = false,
    val isGridView: Boolean = true,
)

sealed interface DialogState {
    data object Dismissed : DialogState

    data class DeleteCategoryConfirm(
        val category: TodoCategory,
    ) : DialogState
}

sealed interface MarkTodoUiEvent {
    data class CategoryClicked(val category: TodoCategory) : MarkTodoUiEvent
    data object AddCategoryClicked : MarkTodoUiEvent
    data class EditCategoryClicked(val category: TodoCategory) : MarkTodoUiEvent
    data class DeleteCategory(val category: TodoCategory) : MarkTodoUiEvent
    data class ConfirmDeleteCategory(val category: TodoCategory) : MarkTodoUiEvent

    data class TaskClicked(val task: TodoTask) : MarkTodoUiEvent
    data class AddTaskClicked(val category: TodoCategory? = null) : MarkTodoUiEvent
    data class ToggleTaskComplete(val task: TodoTask) : MarkTodoUiEvent
    data class ToggleTaskStar(val task: TodoTask) : MarkTodoUiEvent
    data class DuplicateTask(val task: TodoTask) : MarkTodoUiEvent
    data class DeleteTask(val task: TodoTask) : MarkTodoUiEvent

    data class SelectStatusTab(val tab: TaskCompletionFilter) : MarkTodoUiEvent
    data class SearchQueryChanged(val query: String) : MarkTodoUiEvent
    data class TagOnTaskClicked(val tagName: String) : MarkTodoUiEvent
    data class SetFilter(val filter: TaskFilter) : MarkTodoUiEvent
    data class SetSort(val sort: TaskSortConfig) : MarkTodoUiEvent
    data object ShowFilterSortSheet : MarkTodoUiEvent
    data object DismissFilterSortSheet : MarkTodoUiEvent
    data class ApplyFilterSort(val filter: TaskFilter, val sort: TaskSortConfig) : MarkTodoUiEvent
    data object ClearFilters : MarkTodoUiEvent
    data class SetGridView(val isGrid: Boolean) : MarkTodoUiEvent

    data class SetReorderMode(val enabled: Boolean) : MarkTodoUiEvent
    data class ReorderCategories(
        val fromIndex: Int,
        val toIndex: Int,
        val visibleCategoryIds: List<String>,
    ) : MarkTodoUiEvent

    data object DismissDialog : MarkTodoUiEvent
    data object NavigateBack : MarkTodoUiEvent
}
