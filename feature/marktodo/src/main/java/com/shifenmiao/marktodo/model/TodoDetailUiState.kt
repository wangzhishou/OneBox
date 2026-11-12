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
import com.wanbaohe.schedule.model.LinkedScheduleSnapshot

/**
 * 待办详情页的 UI 状态
 */
@Immutable
data class TodoDetailUiState(
    val task: TodoTask? = null,
    val categoryTitle: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCreatingSchedule: Boolean = false,
    val linkedSchedule: LinkedScheduleSnapshot? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    // 编辑表单状态
    val editTitle: String = "",
    val editNote: String = "",
    val editTagsText: String = "",
    val editDueDateMillis: Long? = null,
    val editHasDueTime: Boolean = false,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val showValidationErrors: Boolean = false,
) {
    val isTitleValid: Boolean get() = editTitle.isNotBlank()
    val hasTitleError: Boolean get() = showValidationErrors && !isTitleValid
    val parsedTags: List<String> get() = editTagsText
        .split(",", "，")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
}

sealed interface TodoDetailNotice {
    data object MissingDueDate : TodoDetailNotice
    data object ScheduleCreated : TodoDetailNotice
    data object ScheduleCreateFailed : TodoDetailNotice
}

/**
 * 待办详情页的用户事件
 */
sealed interface TodoDetailUiEvent {
    // 任务操作
    data object ToggleComplete : TodoDetailUiEvent
    data object ToggleStar : TodoDetailUiEvent
    data object DeleteTask : TodoDetailUiEvent
    data object ConfirmDelete : TodoDetailUiEvent
    data object OpenScheduleHub : TodoDetailUiEvent
    data object CreateLinkedSchedule : TodoDetailUiEvent
    data object RemoveLinkedSchedule : TodoDetailUiEvent

    // 编辑模式
    data object EnterEditMode : TodoDetailUiEvent
    data object CancelEdit : TodoDetailUiEvent
    data object SaveEdit : TodoDetailUiEvent

    // 编辑表单更新
    data class UpdateEditTitle(val title: String) : TodoDetailUiEvent
    data class UpdateEditNote(val note: String) : TodoDetailUiEvent
    data class UpdateEditTags(val tags: String) : TodoDetailUiEvent
    data class UpdateEditDueDate(val dateMillis: Long?) : TodoDetailUiEvent
    data class UpdateEditDueTime(val hour: Int, val minute: Int) : TodoDetailUiEvent
    data object ClearEditDueTime : TodoDetailUiEvent
    data class ToggleDatePicker(val show: Boolean) : TodoDetailUiEvent
    data class ToggleTimePicker(val show: Boolean) : TodoDetailUiEvent

    // 导航
    data object NavigateBack : TodoDetailUiEvent
}
