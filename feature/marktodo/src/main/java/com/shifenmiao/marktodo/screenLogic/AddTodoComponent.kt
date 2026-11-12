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

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.marktodo.repo.MarkTodoRepository
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.data.iconFromKey
import com.shifenmiao.marktodo.data.toModel
import com.shifenmiao.marktodo.model.AddTodoUiEvent
import com.shifenmiao.marktodo.model.AddTodoUiState
import com.shifenmiao.marktodo.service.MarkTodoServiceImpl
import com.shifenmiao.model.todo.TaskInput
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.schedule.service.ScheduleService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 新增/编辑待办页组件。
 *
 * @param editingTaskId 非空时为编辑模式：进入时加载原任务，保存走 updateTask
 */
class AddTodoComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted("initialCategoryId") val initialCategoryId: String?,
    @Assisted("editingTaskId") val editingTaskId: String?,
    @Assisted val onNavigate: (Screen) -> Unit,
    @Assisted val onGoBack: () -> Unit,
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder,
    private val repository: MarkTodoRepository,
    private val service: MarkTodoServiceImpl,
    private val scheduleService: ScheduleService,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(AddTodoUiState(editingTaskId = editingTaskId))
    val uiState: StateFlow<AddTodoUiState> = _uiState.asStateFlow()

    init {
        // 分类列表
        componentScope.launch {
            repository.observeDashboard().collect { dashboardData ->
                val categories = dashboardData.map { rel ->
                    val safeIconKey = rel.category.iconKey.ifBlank { "inbox" }
                    rel.category.toModel(
                        icon = iconFromKey(safeIconKey),
                        tasks = emptyList() // Not needed for AddTodoScreen, but required by model
                    )
                }

                val currentSelected = _uiState.value.selectedCategory
                val newSelected = if (currentSelected != null && categories.any { it.id == currentSelected.id }) {
                    categories.first { it.id == currentSelected.id }
                } else if (initialCategoryId != null && categories.any { it.id == initialCategoryId }) {
                    categories.first { it.id == initialCategoryId }
                } else if (editingTaskId != null) {
                    null
                } else {
                    categories.firstOrNull()
                }

                _uiState.value = _uiState.value.copy(
                    categories = categories,
                    selectedCategory = newSelected
                )
            }
        }

        // 标签池
        componentScope.launch {
            repository.observeTags().collect { tags ->
                _uiState.value = _uiState.value.copy(tags = tags.map { it.toModel() })
            }
        }

        // 编辑模式：加载原任务；失败（任务已删/异常）提示后返回，避免空表单保存写坏数据
        if (editingTaskId != null) {
            componentScope.launch {
                val task = runCatching { service.getTask(editingTaskId) }.getOrNull()
                if (task != null) {
                    _uiState.value = _uiState.value.copy(
                        editingTaskLoaded = true,
                        taskTitle = task.title,
                        taskNote = task.note.orEmpty(),
                        selectedTagNames = task.tags.toSet(),
                        priority = task.priority,
                        dueDateMillis = task.dueDate,
                        hasDueTime = task.hasDueTime,
                    )
                } else {
                    AppToastHost.showToast(context.getString(R.string.error_todo_not_found))
                    onGoBack()
                }
            }
            componentScope.launch {
                scheduleService.observeLinkedSchedule(editingTaskId).collect { linked ->
                    _uiState.value = _uiState.value.copy(linkedSchedule = linked)
                }
            }
        }
    }

    fun handleEvent(event: AddTodoUiEvent): Boolean {
        when (event) {
            is AddTodoUiEvent.SelectCategory,
            is AddTodoUiEvent.UpdateTaskTitle,
            is AddTodoUiEvent.UpdateTaskNote,
            is AddTodoUiEvent.ToggleTag,
            is AddTodoUiEvent.AddCustomTag,
            is AddTodoUiEvent.SelectPriority,
            is AddTodoUiEvent.UpdateDueDate,
            is AddTodoUiEvent.UpdateDueTime,
            AddTodoUiEvent.ClearDueTime -> {
                _uiState.value = _uiState.value.copy(isDirty = true)
            }
            else -> Unit
        }
        return when (event) {
            is AddTodoUiEvent.SelectCategory -> {
                _uiState.value = _uiState.value.copy(selectedCategory = event.category)
                true
            }
            is AddTodoUiEvent.AddCategoryClicked -> {
                navigateToAddCategory()
                true
            }
            is AddTodoUiEvent.UpdateTaskTitle -> {
                _uiState.value = _uiState.value.copy(
                    taskTitle = event.title,
                    showValidationErrors = _uiState.value.showValidationErrors && event.title.isBlank()
                )
                true
            }
            is AddTodoUiEvent.UpdateTaskNote -> {
                _uiState.value = _uiState.value.copy(
                    taskNote = event.note.take(AddTodoUiState.NOTE_MAX_LENGTH)
                )
                true
            }
            is AddTodoUiEvent.ToggleTag -> {
                val current = _uiState.value.selectedTagNames
                _uiState.value = _uiState.value.copy(
                    selectedTagNames = if (event.tagName in current) {
                        current - event.tagName
                    } else {
                        current + event.tagName
                    }
                )
                true
            }
            is AddTodoUiEvent.AddCustomTag -> {
                addCustomTag(event.name, event.colorArgb)
                true
            }
            is AddTodoUiEvent.SelectPriority -> {
                _uiState.value = _uiState.value.copy(priority = event.priority)
                true
            }
            is AddTodoUiEvent.UpdateDueDate -> {
                updateDueDate(event.dateMillis)
                true
            }
            is AddTodoUiEvent.UpdateDueTime -> {
                updateDueTime(event.hour, event.minute)
                true
            }
            AddTodoUiEvent.ClearDueTime -> {
                clearDueTime()
                true
            }
            is AddTodoUiEvent.ToggleDatePicker -> {
                _uiState.value = _uiState.value.copy(showDatePicker = event.show)
                true
            }
            is AddTodoUiEvent.ToggleTimePicker -> {
                _uiState.value = _uiState.value.copy(showTimePicker = event.show)
                true
            }
            AddTodoUiEvent.JoinSchedule -> {
                joinSchedule()
                true
            }
            AddTodoUiEvent.RemoveLinkedSchedule -> {
                removeLinkedSchedule()
                true
            }
            AddTodoUiEvent.CancelPendingJoin -> {
                _uiState.value = _uiState.value.copy(joinScheduleOnSave = false)
                true
            }
            AddTodoUiEvent.OpenSchedule -> {
                openSchedule()
                true
            }
            is AddTodoUiEvent.SubmitTask -> {
                submitTask()
            }
            is AddTodoUiEvent.DeleteTask -> {
                _uiState.value = _uiState.value.copy(showDeleteConfirm = true)
                true
            }
            is AddTodoUiEvent.ConfirmDeleteTask -> {
                confirmDelete()
                true
            }
            is AddTodoUiEvent.DismissDeleteConfirm -> {
                _uiState.value = _uiState.value.copy(showDeleteConfirm = false)
                true
            }
            is AddTodoUiEvent.NavigateBack -> {
                onGoBack()
                true
            }
        }
    }

    private fun updateDueDate(dateMillis: Long?) {
        val current = _uiState.value
        val updatedDate = dateMillis?.let { date ->
            java.util.Calendar.getInstance().apply {
                timeInMillis = date
                if (current.hasDueTime && current.dueDateMillis != null) {
                    val previousTime = java.util.Calendar.getInstance().apply {
                        timeInMillis = current.dueDateMillis
                    }
                    set(java.util.Calendar.HOUR_OF_DAY, previousTime.get(java.util.Calendar.HOUR_OF_DAY))
                    set(java.util.Calendar.MINUTE, previousTime.get(java.util.Calendar.MINUTE))
                } else {
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                }
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
        _uiState.value = current.copy(
            dueDateMillis = updatedDate,
            hasDueTime = updatedDate != null && current.hasDueTime,
            showDatePicker = false,
            joinScheduleOnSave = updatedDate != null && current.joinScheduleOnSave,
        )
    }

    private fun updateDueTime(hour: Int, minute: Int) {
        val current = _uiState.value
        val date = current.dueDateMillis ?: return
        val updatedDate = java.util.Calendar.getInstance().apply {
            timeInMillis = date
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        _uiState.value = current.copy(
            dueDateMillis = updatedDate,
            hasDueTime = true,
            showTimePicker = false,
        )
    }

    private fun clearDueTime() {
        val current = _uiState.value
        val updatedDate = current.dueDateMillis?.let { date ->
            java.util.Calendar.getInstance().apply {
                timeInMillis = date
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
        _uiState.value = current.copy(
            dueDateMillis = updatedDate,
            hasDueTime = false,
        )
    }

    private fun submitTask(): Boolean {
        val currentState = _uiState.value
        val title = currentState.taskTitle.trim()

        if (currentState.isSaving) return false
        // 编辑模式下原任务未加载成功时不允许保存（防止空表单 REPLACE 掉原数据）
        if (editingTaskId != null && !currentState.editingTaskLoaded) return false

        if (title.isBlank()) {
            _uiState.value = currentState.copy(showValidationErrors = true)
            return false
        }

        val input = TaskInput(
            categoryId = currentState.selectedCategory?.id,
            title = title,
            note = currentState.taskNote.takeIf { it.isNotBlank() },
            dueDateMillis = currentState.dueDateMillis,
            tags = currentState.selectedTagNames.toList(),
            priority = currentState.priority,
            hasDueTime = currentState.hasDueTime,
        )
        _uiState.value = currentState.copy(isSaving = true)

        componentScope.launch {
            if (editingTaskId != null) {
                service.updateTask(
                    taskId = editingTaskId,
                    input = input,
                    source = "UI:EditTodoScreen",
                ).onSuccess {
                    if (!syncLinkedSchedule(editingTaskId, input)) {
                        AppToastHost.showToast(context.getString(R.string.schedule_sync_failed))
                    }
                    onGoBack()
                }.onFailure {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    AppToastHost.showToast(context.getString(R.string.error_save_failed))
                }
            } else {
                service.createTask(
                    input = input,
                    source = "UI:AddTodoScreen",
                ).onSuccess { newTaskId ->
                    val dueAt = input.dueDateMillis
                    if (_uiState.value.joinScheduleOnSave && dueAt != null) {
                        val joined = scheduleService.createTaskDeadlineEvent(
                            linkedTaskId = newTaskId,
                            title = input.title,
                            description = input.note,
                            dueAtMillis = dueAt,
                            source = "UI:AddTodoScreen",
                            isAllDay = !input.hasDueTime,
                        ).isSuccess
                        if (!joined) {
                            AppToastHost.showToast(context.getString(R.string.schedule_join_failed))
                        }
                    }
                    onGoBack()
                }.onFailure {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    AppToastHost.showToast(context.getString(R.string.error_save_failed))
                }
            }
        }

        return true
    }

    private suspend fun syncLinkedSchedule(taskId: String, input: TaskInput): Boolean {
        return runCatching {
            input.dueDateMillis?.let { dueDate ->
                scheduleService.syncTaskDeadlineEventIfExists(
                    linkedTaskId = taskId,
                    title = input.title,
                    description = input.note,
                    dueAtMillis = dueDate,
                    source = "UI:EditTodoScreen:SyncSchedule",
                    isAllDay = !input.hasDueTime,
                ).isSuccess
            } ?: scheduleService.deleteEventsByLinkedTaskId(taskId).isSuccess
        }.getOrDefault(false)
    }

    private fun confirmDelete() {
        val taskId = editingTaskId ?: return
        _uiState.value = _uiState.value.copy(showDeleteConfirm = false)
        componentScope.launch {
            service.deleteTask(
                taskId = taskId,
                taskTitle = _uiState.value.taskTitle,
                source = "UI:EditTodoScreen"
            ).onSuccess {
                onGoBack()
            }.onFailure {
                AppToastHost.showToast(context.getString(R.string.error_delete_failed))
            }
        }
    }

    private fun addCustomTag(name: String, colorArgb: Int?) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        // 标签按名字关联，重名会让筛选/配色混乱：命中已有标签时直接选中它，不再新建
        val existing = _uiState.value.tags.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
        if (existing != null) {
            _uiState.value = _uiState.value.copy(
                selectedTagNames = _uiState.value.selectedTagNames + existing.name
            )
            AppToastHost.showToast(context.getString(R.string.tag_already_exists))
            return
        }
        componentScope.launch {
            service.createTag(
                name = trimmed,
                colorArgb = colorArgb,
                source = "UI:AddTodoScreen"
            ).onSuccess {
                // 新建后自动选中该标签
                _uiState.value = _uiState.value.copy(
                    selectedTagNames = _uiState.value.selectedTagNames + trimmed
                )
            }.onFailure {
                AppToastHost.showToast(context.getString(R.string.error_create_tag_failed))
            }
        }
    }

    // ── 日程：加入后留在编辑页，查看再打开按天排列的全部日程 ──

    private fun openSchedule() {
        val state = _uiState.value
        onNavigate(
            Screen.Schedule(
                linkedTaskId = editingTaskId,
                focusDateMillis = state.linkedSchedule?.startUtcMillis ?: state.dueDateMillis,
            )
        )
    }

    private fun joinSchedule() {
        val state = _uiState.value
        if (state.isJoiningSchedule) return
        if (state.taskTitle.isBlank()) {
            _uiState.value = state.copy(showValidationErrors = true)
            return
        }
        val dueDate = state.dueDateMillis ?: return
        val taskId = editingTaskId
        if (taskId == null) {
            _uiState.value = state.copy(joinScheduleOnSave = true, isDirty = true)
            return
        }
        _uiState.value = state.copy(isJoiningSchedule = true)
        componentScope.launch {
            val result = scheduleService.createTaskDeadlineEvent(
                linkedTaskId = taskId,
                title = state.taskTitle.trim(),
                description = state.taskNote.takeIf { it.isNotBlank() },
                dueAtMillis = dueDate,
                source = "UI:EditTodoScreen",
                isAllDay = !state.hasDueTime,
            )
            _uiState.value = _uiState.value.copy(isJoiningSchedule = false)
            result.onFailure {
                AppToastHost.showToast(context.getString(R.string.schedule_join_failed))
            }
        }
    }

    private fun removeLinkedSchedule() {
        if (editingTaskId == null) {
            _uiState.value = _uiState.value.copy(joinScheduleOnSave = false)
            return
        }
        val taskId = editingTaskId
        componentScope.launch {
            scheduleService.deleteEventsByLinkedTaskId(taskId)
                .onSuccess {
                    AppToastHost.showToast(context.getString(R.string.schedule_removed))
                }
                .onFailure {
                    AppToastHost.showToast(context.getString(R.string.schedule_join_failed))
                }
        }
    }

    private fun navigateToAddCategory() {
        onNavigate(
            Screen.MarkTodoRouter(
                Screen.MarkTodoRouter.MarkTodoType.AddCategory()
            )
        )
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            @Assisted("initialCategoryId") initialCategoryId: String?,
            @Assisted("editingTaskId") editingTaskId: String?,
            onNavigate: (Screen) -> Unit,
            onGoBack: () -> Unit
        ): AddTodoComponent
    }
}
