package com.shifenmiao.marktodo.screenLogic

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.model.TodoDetailUiEvent
import com.shifenmiao.marktodo.model.TodoDetailNotice
import com.shifenmiao.marktodo.model.TodoDetailUiState
import com.shifenmiao.marktodo.service.MarkTodoServiceImpl
import com.shifenmiao.model.todo.TaskInput
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.schedule.service.ScheduleService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TodoDetailComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted("taskId") val taskId: String,
    @Assisted("categoryId") val categoryId: String,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder,
    private val service: MarkTodoServiceImpl,
    private val scheduleService: ScheduleService,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(TodoDetailUiState())
    val uiState: StateFlow<TodoDetailUiState> = _uiState.asStateFlow()

    private val _showDeleteConfirm = MutableStateFlow(false)
    val showDeleteConfirm: StateFlow<Boolean> = _showDeleteConfirm.asStateFlow()

    private val _scheduleActionNotice = MutableStateFlow<TodoDetailNotice?>(null)
    val scheduleActionNotice: StateFlow<TodoDetailNotice?> = _scheduleActionNotice.asStateFlow()

    init {
        componentScope.launch {
            loadTask()
        }
    }

    fun handleEvent(event: TodoDetailUiEvent): Boolean {
        return when (event) {
            is TodoDetailUiEvent.ToggleComplete -> {
                toggleComplete()
                true
            }
            is TodoDetailUiEvent.ToggleStar -> {
                toggleStar()
                true
            }
            is TodoDetailUiEvent.DeleteTask -> {
                _showDeleteConfirm.value = true
                true
            }
            is TodoDetailUiEvent.ConfirmDelete -> {
                confirmDelete()
                true
            }
            is TodoDetailUiEvent.OpenScheduleHub -> {
                openScheduleHub()
                true
            }
            is TodoDetailUiEvent.CreateLinkedSchedule -> {
                createLinkedSchedule()
                true
            }
            is TodoDetailUiEvent.EnterEditMode -> {
                enterEditMode()
                true
            }
            is TodoDetailUiEvent.CancelEdit -> {
                cancelEdit()
                true
            }
            is TodoDetailUiEvent.SaveEdit -> {
                saveEdit()
            }
            is TodoDetailUiEvent.UpdateEditTitle -> {
                _uiState.value = _uiState.value.copy(
                    editTitle = event.title,
                    showValidationErrors = _uiState.value.showValidationErrors && event.title.isBlank()
                )
                true
            }
            is TodoDetailUiEvent.UpdateEditNote -> {
                _uiState.value = _uiState.value.copy(editNote = event.note)
                true
            }
            is TodoDetailUiEvent.UpdateEditTags -> {
                _uiState.value = _uiState.value.copy(editTagsText = event.tags)
                true
            }
            is TodoDetailUiEvent.UpdateEditDueDate -> {
                _uiState.value = _uiState.value.copy(
                    editDueDateMillis = event.dateMillis,
                    showDatePicker = false
                )
                true
            }
            is TodoDetailUiEvent.ToggleDatePicker -> {
                _uiState.value = _uiState.value.copy(showDatePicker = event.show)
                true
            }
            is TodoDetailUiEvent.NavigateBack -> {
                onGoBack()
                true
            }
        }
    }

    fun dismissDeleteConfirm() {
        _showDeleteConfirm.value = false
    }

    fun clearScheduleActionNotice() {
        _scheduleActionNotice.value = null
    }

    private suspend fun loadTask() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        try {
            val task = service.getTask(taskId)
            if (task != null) {
                val category = service.getCategoryWithTasks(categoryId)
                _uiState.value = _uiState.value.copy(
                    task = task,
                    categoryTitle = category?.title ?: "",
                    isLoading = false,
                    error = null,
                    editTitle = task.title,
                    editNote = task.note ?: "",
                    editTagsText = task.tags.joinToString(", "),
                    editDueDateMillis = task.dueDate
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = context.getString(R.string.error_todo_not_found)
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: context.getString(R.string.error_load_failed)
            )
        }
    }

    private fun toggleComplete() {
        val task = _uiState.value.task ?: return
        val newValue = !task.isCompleted
        _uiState.value = _uiState.value.copy(task = task.copy(isCompleted = newValue))

        componentScope.launch {
            service.toggleTaskComplete(
                taskId = task.id,
                isCompleted = newValue,
                taskTitle = task.title,
                source = "UI:TodoDetailScreen"
            )
        }
    }

    private fun toggleStar() {
        val task = _uiState.value.task ?: return
        val newValue = !task.isStarred
        _uiState.value = _uiState.value.copy(task = task.copy(isStarred = newValue))

        componentScope.launch {
            service.toggleTaskStar(
                taskId = task.id,
                isStarred = newValue,
                taskTitle = task.title,
                source = "UI:TodoDetailScreen"
            )
        }
    }

    private fun confirmDelete() {
        val task = _uiState.value.task ?: return
        _showDeleteConfirm.value = false

        componentScope.launch {
            scheduleService.deleteEventsByLinkedTaskId(task.id)
            service.deleteTask(
                taskId = task.id,
                taskTitle = task.title,
                source = "UI:TodoDetailScreen"
            )
            onGoBack()
        }
    }

    private fun openScheduleHub() {
        val task = _uiState.value.task ?: return
        onNavigate(
            Screen.Schedule(
                linkedTaskId = task.id,
                focusDateMillis = task.dueDate
            )
        )
    }

    private fun createLinkedSchedule() {
        val task = _uiState.value.task ?: return
        val dueDate = task.dueDate ?: run {
            _scheduleActionNotice.value = TodoDetailNotice.MissingDueDate
            return
        }

        _uiState.value = _uiState.value.copy(isCreatingSchedule = true)

        componentScope.launch {
            val result = scheduleService.createTaskDeadlineEvent(
                linkedTaskId = task.id,
                title = task.title,
                description = task.note,
                dueAtMillis = dueDate,
                source = "UI:TodoDetailScreen"
            )
            _uiState.value = _uiState.value.copy(isCreatingSchedule = false)

            result
                .onSuccess {
                    _scheduleActionNotice.value = TodoDetailNotice.ScheduleCreated
                    openScheduleHub()
                }
                .onFailure {
                    _scheduleActionNotice.value = TodoDetailNotice.ScheduleCreateFailed
                }
        }
    }

    private fun enterEditMode() {
        val task = _uiState.value.task ?: return
        _uiState.value = _uiState.value.copy(
            isEditing = true,
            editTitle = task.title,
            editNote = task.note ?: "",
            editTagsText = task.tags.joinToString(", "),
            editDueDateMillis = task.dueDate,
            showValidationErrors = false
        )
    }

    private fun cancelEdit() {
        val task = _uiState.value.task ?: return
        _uiState.value = _uiState.value.copy(
            isEditing = false,
            editTitle = task.title,
            editNote = task.note ?: "",
            editTagsText = task.tags.joinToString(", "),
            editDueDateMillis = task.dueDate,
            showValidationErrors = false
        )
    }

    private fun saveEdit(): Boolean {
        val state = _uiState.value
        val task = state.task ?: return false
        val title = state.editTitle.trim()

        if (title.isBlank()) {
            _uiState.value = state.copy(showValidationErrors = true)
            return false
        }

        val updatedTask = task.copy(
            title = title,
            note = state.editNote.trim().takeIf { it.isNotBlank() },
            dueDate = state.editDueDateMillis,
            tags = state.parsedTags
        )

        // Optimistic UI update
        _uiState.value = state.copy(
            task = updatedTask,
            isEditing = false
        )

        // Persist
        componentScope.launch {
            service.updateTask(
                taskId = task.id,
                input = TaskInput(
                    categoryId = categoryId,
                    title = title,
                    note = state.editNote.trim().takeIf { it.isNotBlank() },
                    dueDateMillis = state.editDueDateMillis,
                    tags = state.parsedTags
                ),
                source = "UI:TodoDetailScreen"
            )

            state.editDueDateMillis?.let { dueDate ->
                scheduleService.syncTaskDeadlineEventIfExists(
                    linkedTaskId = task.id,
                    title = title,
                    description = state.editNote.trim().takeIf { it.isNotBlank() },
                    dueAtMillis = dueDate,
                    source = "UI:TodoDetailScreen:SyncSchedule"
                )
            } ?: scheduleService.deleteEventsByLinkedTaskId(task.id)
        }

        return true
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            @Assisted("taskId") taskId: String,
            @Assisted("categoryId") categoryId: String,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): TodoDetailComponent
    }
}
