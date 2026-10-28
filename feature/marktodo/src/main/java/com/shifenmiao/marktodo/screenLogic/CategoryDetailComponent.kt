package com.shifenmiao.marktodo.screenLogic

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.marktodo.repo.MarkTodoRepository
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.data.iconFromKey
import com.shifenmiao.marktodo.data.toModel
import com.shifenmiao.marktodo.model.CategoryDetailUiEvent
import com.shifenmiao.marktodo.model.CategoryDetailUiState
import com.shifenmiao.marktodo.model.TodoTask
import com.shifenmiao.marktodo.service.MarkTodoServiceImpl
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
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
import kotlinx.coroutines.flow.catch

class CategoryDetailComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val categoryId: String,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder,
    private val repository: MarkTodoRepository,
    private val service: MarkTodoServiceImpl,
    quickAddTodoFactory: QuickAddTodoComponent.Factory,
) : BaseComponent(dispatchersHolder, componentContext) {

    val quickAddTodoComponent = quickAddTodoFactory(componentScope)

    private val _uiState = MutableStateFlow(CategoryDetailUiState(isLoading = true))
    val uiState: StateFlow<CategoryDetailUiState> = _uiState.asStateFlow()

    private val _showDeleteCategoryConfirm = MutableStateFlow(false)
    val showDeleteCategoryConfirm: StateFlow<Boolean> = _showDeleteCategoryConfirm.asStateFlow()

    private val completionRemovalJobs = mutableMapOf<String, Job>()

    init {
        componentScope.launch {
            repository.observeDashboard()
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: context.getString(R.string.error_load_failed),
                    )
                }
                .collect { dashboard ->
                    val relation = dashboard.firstOrNull { it.category.id == categoryId }
                    _uiState.value = if (relation == null) {
                        _uiState.value.copy(
                            category = null,
                            isLoading = false,
                            error = context.getString(R.string.error_category_not_found),
                        )
                    } else {
                        val safeIconKey = relation.category.iconKey.ifBlank { "inbox" }
                        _uiState.value.copy(
                            category = relation.category.toModel(
                                icon = iconFromKey(safeIconKey),
                                tasks = relation.tasks.map { it.toModel() }.sortedBy { it.sortOrder },
                            ),
                            isLoading = false,
                            error = null,
                        )
                    }
                }
        }
        componentScope.launch {
            repository.observeTags().collect { tags ->
                _uiState.value = _uiState.value.copy(tags = tags.map { it.toModel() })
            }
        }
    }

    fun handleEvent(event: CategoryDetailUiEvent): Boolean {
        when (event) {
            is CategoryDetailUiEvent.TaskClicked -> navigateToEditTodo(event.task)
            CategoryDetailUiEvent.AddTaskClicked -> {
                quickAddTodoComponent.open(initialCategoryId = categoryId)
            }
            is CategoryDetailUiEvent.ToggleTaskComplete -> toggleTaskComplete(event.task)
            is CategoryDetailUiEvent.ToggleTaskStar -> toggleTaskStar(event.task)
            is CategoryDetailUiEvent.DeleteTask -> deleteTask(event.task)
            is CategoryDetailUiEvent.ChangeFilter -> {
                _uiState.value = _uiState.value.copy(filterMode = event.filterMode)
            }
            CategoryDetailUiEvent.ShowSortSheet -> {
                _uiState.value = _uiState.value.copy(isSortSheetVisible = true)
            }
            CategoryDetailUiEvent.DismissSortSheet -> {
                _uiState.value = _uiState.value.copy(isSortSheetVisible = false)
            }
            is CategoryDetailUiEvent.ChangeSort -> {
                _uiState.value = _uiState.value.copy(sort = event.sort, isSortSheetVisible = false)
            }
            CategoryDetailUiEvent.NavigateBack -> onGoBack()
            CategoryDetailUiEvent.EditCategoryClicked -> onNavigate(
                Screen.MarkTodoRouter(
                    Screen.MarkTodoRouter.MarkTodoType.AddCategory(editingCategoryId = categoryId)
                )
            )
            CategoryDetailUiEvent.DeleteCategoryClicked -> _showDeleteCategoryConfirm.value = true
            CategoryDetailUiEvent.DismissDeleteCategory -> {
                if (!_uiState.value.isDeletingCategory) _showDeleteCategoryConfirm.value = false
            }
            CategoryDetailUiEvent.ConfirmDeleteCategory -> deleteCategory()
        }
        return true
    }

    private fun navigateToEditTodo(task: TodoTask) {
        onNavigate(
            Screen.MarkTodoRouter(
                Screen.MarkTodoRouter.MarkTodoType.AddTodo(
                    initialCategoryId = categoryId,
                    editingTaskId = task.id,
                )
            )
        )
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
                source = "UI:CategoryDetailScreen",
            ).onSuccess {
                if (newValue && currentTask(task.id)?.isCompleted == true) {
                    AppToastHost.showActionToast(
                        message = context.getString(R.string.p1_task_completed_toast),
                        actionLabel = context.getString(R.string.action_undo),
                        onAction = {
                            currentTask(task.id)?.takeIf { it.isCompleted }?.let(::toggleTaskComplete)
                        },
                    )
                }
            }.onFailure {
                completionRemovalJobs.remove(task.id)?.cancel()
                setCompletionPending(task.id, false)
                if (!newValue && wasPending && task.isCompleted) {
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
                source = "UI:CategoryDetailScreen",
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
                source = "UI:CategoryDetailScreen",
            ).onFailure {
                AppToastHost.showFailureToast(R.string.error_delete_failed)
            }
        }
    }

    private fun deleteCategory() {
        if (_uiState.value.isDeletingCategory) return
        _uiState.value = _uiState.value.copy(isDeletingCategory = true)
        componentScope.launch {
            service.deleteCategory(
                categoryId = categoryId,
                categoryTitle = _uiState.value.category?.title.orEmpty(),
                source = "UI:CategoryDetailScreen",
            ).onSuccess {
                _uiState.value = _uiState.value.copy(isDeletingCategory = false)
                _showDeleteCategoryConfirm.value = false
                onGoBack()
            }.onFailure {
                _uiState.value = _uiState.value.copy(isDeletingCategory = false)
                AppToastHost.showFailureToast(R.string.error_delete_failed)
            }
        }
    }

    private fun currentTask(taskId: String): TodoTask? =
        _uiState.value.category?.tasks?.firstOrNull { it.id == taskId }

    private fun updateTask(taskId: String, transform: (TodoTask) -> TodoTask) {
        val category = _uiState.value.category ?: return
        _uiState.value = _uiState.value.copy(
            category = category.copy(
                tasks = category.tasks.map { task ->
                    if (task.id == taskId) transform(task) else task
                }
            )
        )
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            categoryId: String,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): CategoryDetailComponent
    }

    private companion object {
        const val COMPLETION_UNDO_WINDOW_MILLIS = 6500L
    }
}
