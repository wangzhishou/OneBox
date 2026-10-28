package com.shifenmiao.marktodo.screenLogic

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.marktodo.repo.MarkTodoRepository
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.data.iconFromKey
import com.shifenmiao.marktodo.data.toModel
import com.shifenmiao.marktodo.model.DialogState
import com.shifenmiao.marktodo.model.HomeView
import com.shifenmiao.marktodo.model.MarkTodoUiEvent
import com.shifenmiao.marktodo.model.MarkTodoUiState
import com.shifenmiao.marktodo.model.TaskSortConfig
import com.shifenmiao.marktodo.model.TaskSortMode
import com.shifenmiao.marktodo.model.TodoCategory
import com.shifenmiao.marktodo.model.TodoTag
import com.shifenmiao.marktodo.model.TodoTask
import com.shifenmiao.marktodo.service.MarkTodoServiceImpl
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.tencent.mmkv.MMKV
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.navigation.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MarkTodoComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onNavigate: (Screen) -> Unit,
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder,
    private val repository: MarkTodoRepository,
    private val service: MarkTodoServiceImpl,
    quickAddTodoFactory: QuickAddTodoComponent.Factory,
) : BaseComponent(dispatchersHolder, componentContext) {

    val quickAddTodoComponent = quickAddTodoFactory(componentScope)

    private val prefs by lazy { MMKV.mmkvWithID("marktodo_home") }
    private val completionRemovalJobs = mutableMapOf<String, Job>()

    private val _uiState = MutableStateFlow(
        MarkTodoUiState(
            homeView = prefs.decodeString(KEY_HOME_VIEW, null)
                ?.let { runCatching { HomeView.valueOf(it) }.getOrNull() }
                ?: HomeView.THEMES,
            sort = TaskSortConfig(
                mode = prefs.decodeString(KEY_SORT_MODE, null)
                    ?.let { runCatching { TaskSortMode.valueOf(it) }.getOrNull() }
                    ?: TaskSortMode.MANUAL,
                ascending = prefs.decodeBool(KEY_SORT_ASCENDING, true),
            ),
            isGridView = prefs.decodeBool(KEY_GRID_VIEW, true),
        )
    )
    val uiState: StateFlow<MarkTodoUiState> = _uiState.asStateFlow()

    private val _categoriesState = MutableStateFlow<List<TodoCategory>>(emptyList())
    val categoriesState: StateFlow<List<TodoCategory>> = _categoriesState.asStateFlow()

    private val _tasksState = MutableStateFlow<List<TodoTask>>(emptyList())
    val tasksState: StateFlow<List<TodoTask>> = _tasksState.asStateFlow()

    private val _tagsState = MutableStateFlow<List<TodoTag>>(emptyList())
    val tagsState: StateFlow<List<TodoTag>> = _tagsState.asStateFlow()

    private val _dialogState = MutableStateFlow<DialogState>(DialogState.Dismissed)
    val dialogState: StateFlow<DialogState> = _dialogState.asStateFlow()

    init {
        componentScope.launch {
            service.ensurePresetTags(defaultPresetTagNames())
        }
        componentScope.launch {
            repository.observeTags().collect { tags ->
                _tagsState.value = tags.map { it.toModel() }
            }
        }
        componentScope.launch {
            repository.observeDashboard().collect { dashboardData ->
                _categoriesState.value = dashboardData.map { relation ->
                    val safeIconKey = relation.category.iconKey.ifBlank { "inbox" }
                    relation.category.toModel(
                        icon = iconFromKey(safeIconKey),
                        tasks = relation.tasks.map { it.toModel() }.sortedBy { it.sortOrder },
                    )
                }
            }
        }
        componentScope.launch {
            repository.observeAll().collect { tasks ->
                _tasksState.value = tasks.map { it.toModel() }
            }
        }
    }

    fun handleEvent(event: MarkTodoUiEvent): Boolean {
        when (event) {
            is MarkTodoUiEvent.CategoryClicked -> openCategory(event.category)
            MarkTodoUiEvent.AddCategoryClicked -> onNavigate(
                Screen.MarkTodoRouter(Screen.MarkTodoRouter.MarkTodoType.AddCategory())
            )
            is MarkTodoUiEvent.EditCategoryClicked -> onNavigate(
                Screen.MarkTodoRouter(
                    Screen.MarkTodoRouter.MarkTodoType.AddCategory(
                        editingCategoryId = event.category.id,
                    )
                )
            )
            is MarkTodoUiEvent.DeleteCategory -> {
                _dialogState.value = DialogState.DeleteCategoryConfirm(event.category)
            }
            is MarkTodoUiEvent.ConfirmDeleteCategory -> deleteCategory(event.category)
            is MarkTodoUiEvent.TaskClicked -> openTaskEditor(event.task)
            is MarkTodoUiEvent.AddTaskClicked -> {
                quickAddTodoComponent.open(initialCategoryId = event.category?.id)
            }
            is MarkTodoUiEvent.ToggleTaskComplete -> toggleTaskComplete(event.task)
            is MarkTodoUiEvent.ToggleTaskStar -> toggleTaskStar(event.task)
            is MarkTodoUiEvent.DeleteTask -> deleteTask(event.task)
            is MarkTodoUiEvent.SelectHomeView -> {
                _uiState.value = _uiState.value.copy(
                    homeView = event.view,
                    isReorderMode = if (event.view == HomeView.THEMES) {
                        _uiState.value.isReorderMode
                    } else {
                        false
                    },
                )
                prefs.encode(KEY_HOME_VIEW, event.view.name)
            }
            is MarkTodoUiEvent.SearchQueryChanged -> {
                _uiState.value = _uiState.value.copy(searchQuery = event.query)
            }
            is MarkTodoUiEvent.TagOnTaskClicked -> {
                val filter = _uiState.value.filter
                val selected = !filter.tagNames.contains(event.tagName)
                _uiState.value = _uiState.value.copy(
                    filter = filter.copy(
                        tagNames = if (selected) setOf(event.tagName) else filter.tagNames - event.tagName,
                    )
                )
            }
            is MarkTodoUiEvent.SetFilter -> {
                _uiState.value = _uiState.value.copy(filter = event.filter)
            }
            is MarkTodoUiEvent.SetSort -> updateSort(event.sort)
            MarkTodoUiEvent.ShowFilterSortSheet -> {
                _uiState.value = _uiState.value.copy(isFilterSortSheetVisible = true)
            }
            MarkTodoUiEvent.DismissFilterSortSheet -> {
                _uiState.value = _uiState.value.copy(isFilterSortSheetVisible = false)
            }
            is MarkTodoUiEvent.ApplyFilterSort -> {
                _uiState.value = _uiState.value.copy(
                    filter = event.filter,
                    isFilterSortSheetVisible = false,
                )
                updateSort(event.sort)
            }
            MarkTodoUiEvent.ResetQuery -> {
                _uiState.value = _uiState.value.copy(filter = com.shifenmiao.marktodo.model.TaskFilter())
                updateSort(TaskSortConfig())
            }
            is MarkTodoUiEvent.SetGridView -> {
                _uiState.value = _uiState.value.copy(isGridView = event.isGrid)
                prefs.encode(KEY_GRID_VIEW, event.isGrid)
            }
            is MarkTodoUiEvent.SetReorderMode -> {
                _uiState.value = _uiState.value.copy(isReorderMode = event.enabled)
            }
            is MarkTodoUiEvent.ReorderCategories -> reorderCategories(event)
            MarkTodoUiEvent.DismissDialog -> dismissDialog()
            MarkTodoUiEvent.NavigateBack -> Unit
        }
        return true
    }

    private fun openCategory(category: TodoCategory) {
        val safeTitle = category.title.ifBlank { context.getString(R.string.category_unnamed) }
        onNavigate(
            Screen.MarkTodoRouter(
                Screen.MarkTodoRouter.MarkTodoType.CategoryDetail(
                    categoryId = category.id,
                    categoryTitle = safeTitle,
                )
            )
        )
    }

    private fun openTaskEditor(task: TodoTask) {
        onNavigate(
            Screen.MarkTodoRouter(
                Screen.MarkTodoRouter.MarkTodoType.AddTodo(
                    initialCategoryId = task.categoryId,
                    editingTaskId = task.id,
                )
            )
        )
    }

    private fun deleteCategory(category: TodoCategory) {
        if (_uiState.value.isDeletingCategory) return
        _uiState.value = _uiState.value.copy(isDeletingCategory = true)
        componentScope.launch {
            service.deleteCategory(
                categoryId = category.id,
                categoryTitle = category.title,
                source = "UI:MarkTodoScreen",
            ).onSuccess {
                _dialogState.value = DialogState.Dismissed
                _uiState.value = _uiState.value.copy(isDeletingCategory = false)
            }.onFailure {
                _uiState.value = _uiState.value.copy(isDeletingCategory = false)
                AppToastHost.showFailureToast(R.string.error_delete_failed)
            }
        }
    }

    private fun reorderCategories(event: MarkTodoUiEvent.ReorderCategories) {
        val all = _categoriesState.value
        if (event.fromIndex !in event.visibleCategoryIds.indices ||
            event.toIndex !in event.visibleCategoryIds.indices
        ) return

        val reorderedVisible = event.visibleCategoryIds.toMutableList().apply {
            add(event.toIndex, removeAt(event.fromIndex))
        }
        val visibleIds = reorderedVisible.toSet()
        val queue = ArrayDeque(reorderedVisible)
        val mergedIds = all.map { category ->
            if (category.id in visibleIds) queue.removeFirst() else category.id
        }
        val previous = all
        val byId = all.associateBy { it.id }
        _categoriesState.value = mergedIds.mapNotNull(byId::get)

        componentScope.launch {
            service.reorderCategories(
                orderedIds = mergedIds,
                source = "UI:MarkTodoScreen",
            ).onFailure {
                _categoriesState.value = previous
                AppToastHost.showFailureToast(R.string.p1_error_update_failed)
            }
        }
    }

    private fun toggleTaskComplete(task: TodoTask) {
        val newValue = !task.isCompleted
        val wasPending = task.id in _uiState.value.pendingCompletionIds
        completionRemovalJobs.remove(task.id)?.cancel()
        setCompletionPending(task.id, newValue)
        if (newValue) scheduleCompletionRemoval(task.id)
        updateTask(task.id) {
            it.copy(
                isCompleted = newValue,
                completedAt = if (newValue) System.currentTimeMillis() else null,
            )
        }

        componentScope.launch {
            service.toggleTaskComplete(
                taskId = task.id,
                isCompleted = newValue,
                taskTitle = task.title,
                source = "UI:MarkTodoScreen",
            ).onSuccess {
                if (newValue && _tasksState.value.firstOrNull { it.id == task.id }?.isCompleted == true) {
                    AppToastHost.showActionToast(
                        message = context.getString(R.string.p1_task_completed_toast),
                        actionLabel = context.getString(R.string.action_undo),
                        onAction = { undoCompletion(task.id) },
                    )
                }
            }.onFailure {
                completionRemovalJobs.remove(task.id)?.cancel()
                setCompletionPending(task.id, false)
                if (!newValue && wasPending && task.isCompleted) {
                    setCompletionPending(task.id, true)
                    scheduleCompletionRemoval(task.id)
                }
                updateTask(task.id) { current ->
                    if (current.isCompleted == newValue) {
                        current.copy(isCompleted = task.isCompleted, completedAt = task.completedAt)
                    } else {
                        current
                    }
                }
                AppToastHost.showFailureToast(R.string.p1_error_update_failed)
            }
        }
    }

    private fun undoCompletion(taskId: String) {
        _tasksState.value.firstOrNull { it.id == taskId && it.isCompleted }?.let(::toggleTaskComplete)
    }

    private fun scheduleCompletionRemoval(taskId: String) {
        setCompletionPending(taskId, true)
        completionRemovalJobs[taskId] = componentScope.launch {
            delay(COMPLETION_UNDO_WINDOW_MILLIS)
            completionRemovalJobs.remove(taskId)
            setCompletionPending(taskId, false)
        }
    }

    private fun setCompletionPending(taskId: String, pending: Boolean) {
        val ids = _uiState.value.pendingCompletionIds
        _uiState.value = _uiState.value.copy(
            pendingCompletionIds = if (pending) ids + taskId else ids - taskId,
        )
    }

    private fun toggleTaskStar(task: TodoTask) {
        val newValue = !task.isStarred
        updateTask(task.id) { it.copy(isStarred = newValue) }
        componentScope.launch {
            service.toggleTaskStar(
                taskId = task.id,
                isStarred = newValue,
                taskTitle = task.title,
                source = "UI:MarkTodoScreen",
            ).onFailure {
                updateTask(task.id) { current ->
                    if (current.isStarred == newValue) current.copy(isStarred = task.isStarred) else current
                }
                AppToastHost.showFailureToast(R.string.p1_error_update_failed)
            }
        }
    }

    private fun deleteTask(task: TodoTask) {
        componentScope.launch {
            service.deleteTask(
                taskId = task.id,
                taskTitle = task.title,
                source = "UI:MarkTodoScreen",
            ).onFailure {
                AppToastHost.showFailureToast(R.string.error_delete_failed)
            }
        }
    }

    private fun updateTask(taskId: String, transform: (TodoTask) -> TodoTask) {
        _tasksState.value = _tasksState.value.map { task ->
            if (task.id == taskId) transform(task) else task
        }
    }

    private fun updateSort(sort: TaskSortConfig) {
        _uiState.value = _uiState.value.copy(sort = sort)
        prefs.encode(KEY_SORT_MODE, sort.mode.name)
        prefs.encode(KEY_SORT_ASCENDING, sort.ascending)
    }

    private fun dismissDialog() {
        if (!_uiState.value.isDeletingCategory) {
            _dialogState.value = DialogState.Dismissed
        }
    }

    private fun defaultPresetTagNames(): Map<String, String> = mapOf(
        "preset_work" to context.getString(R.string.preset_tag_work),
        "preset_study" to context.getString(R.string.preset_tag_study),
        "preset_life" to context.getString(R.string.preset_tag_life),
        "preset_health" to context.getString(R.string.preset_tag_health),
        "preset_travel" to context.getString(R.string.preset_tag_travel),
    )

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onNavigate: (Screen) -> Unit,
        ): MarkTodoComponent
    }

    private companion object {
        const val KEY_HOME_VIEW = "home_view"
        const val KEY_GRID_VIEW = "grid_view"
        const val KEY_SORT_MODE = "sort_mode"
        const val KEY_SORT_ASCENDING = "sort_ascending"
        const val COMPLETION_UNDO_WINDOW_MILLIS = 6500L
    }
}
