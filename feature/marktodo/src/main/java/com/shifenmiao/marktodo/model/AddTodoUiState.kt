package com.shifenmiao.marktodo.model

import androidx.compose.runtime.Immutable
import com.wanbaohe.schedule.model.LinkedScheduleSnapshot

/**
 * 新增/编辑待办页状态。
 *
 * 编辑模式（[editingTaskId] 非空）进入时加载原任务，底部显示「删除 + 保存」，
 * 保存成功后，已加入的日程会跟着截止日期改时间；清空日期则会移除日程。
 */
@Immutable
data class AddTodoUiState(
    val categories: List<TodoCategory> = emptyList(),
    val selectedCategory: TodoCategory? = null,
    val tags: List<TodoTag> = emptyList(),
    val editingTaskId: String? = null,
    val editingTaskLoaded: Boolean = false,
    val taskTitle: String = "",
    val taskNote: String = "",
    val selectedTagNames: Set<String> = emptySet(),
    val priority: Int = TodoTask.PRIORITY_MEDIUM,
    val dueDateMillis: Long? = null,
    val hasDueTime: Boolean = false,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val showValidationErrors: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val isJoiningSchedule: Boolean = false,
    val linkedSchedule: LinkedScheduleSnapshot? = null,
    /** 新任务还没有 id，先记下，保存成功后再写入日程。 */
    val joinScheduleOnSave: Boolean = false,
    val isSaving: Boolean = false,
    val isDirty: Boolean = false,
) {
    val isEditMode: Boolean get() = editingTaskId != null
    val isTitleValid: Boolean get() = taskTitle.isNotBlank()
    val hasTitleError: Boolean get() = showValidationErrors && !isTitleValid
    val noteAtMaxLength: Boolean get() = taskNote.length >= NOTE_MAX_LENGTH

    companion object {
        const val NOTE_MAX_LENGTH = 200
    }
}

sealed interface AddTodoUiEvent {
    data class SelectCategory(val category: TodoCategory?) : AddTodoUiEvent
    data object AddCategoryClicked : AddTodoUiEvent

    data class UpdateTaskTitle(val title: String) : AddTodoUiEvent
    data class UpdateTaskNote(val note: String) : AddTodoUiEvent
    data class ToggleTag(val tagName: String) : AddTodoUiEvent
    data class AddCustomTag(val name: String, val colorArgb: Int?) : AddTodoUiEvent
    data class SelectPriority(val priority: Int) : AddTodoUiEvent
    data class UpdateDueDate(val dateMillis: Long?) : AddTodoUiEvent
    data class UpdateDueTime(val hour: Int, val minute: Int) : AddTodoUiEvent
    data object ClearDueTime : AddTodoUiEvent
    data class ToggleDatePicker(val show: Boolean) : AddTodoUiEvent
    data class ToggleTimePicker(val show: Boolean) : AddTodoUiEvent

    // 日程：加入后留在本页；查看再进入按天排列的日程
    data object JoinSchedule : AddTodoUiEvent
    data object RemoveLinkedSchedule : AddTodoUiEvent
    data object CancelPendingJoin : AddTodoUiEvent
    data object OpenSchedule : AddTodoUiEvent

    data object SubmitTask : AddTodoUiEvent
    data object DeleteTask : AddTodoUiEvent
    data object ConfirmDeleteTask : AddTodoUiEvent
    data object DismissDeleteConfirm : AddTodoUiEvent
    data object NavigateBack : AddTodoUiEvent
}
