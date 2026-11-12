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
data class CategoryDetailUiState(
    val category: TodoCategory? = null,
    val themeIndex: Int = 0,
    val tags: List<TodoTag> = emptyList(),
    val filterMode: TaskFilterMode = TaskFilterMode.ALL,
    val sort: TaskSortConfig = TaskSortConfig(),
    val pendingCompletionIds: Set<String> = emptySet(),
    val isSortSheetVisible: Boolean = false,
    val isDeletingCategory: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    /** 收集箱是合成分组：不能改名/删除，任务落库时 category_id 为 NULL */
    val isInbox: Boolean = false,
) {
    val totalCount: Int get() = category?.totalCount ?: 0
    val completedCount: Int get() = category?.completedCount ?: 0
}

enum class TaskFilterMode {
    ALL, ACTIVE, COMPLETED, STARRED
}

sealed interface CategoryDetailUiEvent {
    data class TaskClicked(val task: TodoTask) : CategoryDetailUiEvent
    data object AddTaskClicked : CategoryDetailUiEvent
    data class ToggleTaskComplete(val task: TodoTask) : CategoryDetailUiEvent
    data class ToggleTaskStar(val task: TodoTask) : CategoryDetailUiEvent
    data class DuplicateTask(val task: TodoTask) : CategoryDetailUiEvent
    data class DeleteTask(val task: TodoTask) : CategoryDetailUiEvent

    data class ChangeFilter(val filterMode: TaskFilterMode) : CategoryDetailUiEvent
    data object ShowSortSheet : CategoryDetailUiEvent
    data object DismissSortSheet : CategoryDetailUiEvent
    data class ChangeSort(val sort: TaskSortConfig) : CategoryDetailUiEvent

    data object AddCategoryClicked : CategoryDetailUiEvent
    data object EditCategoryClicked : CategoryDetailUiEvent
    data object DeleteCategoryClicked : CategoryDetailUiEvent
    data object DismissDeleteCategory : CategoryDetailUiEvent
    data object ConfirmDeleteCategory : CategoryDetailUiEvent

    data object NavigateBack : CategoryDetailUiEvent
}
