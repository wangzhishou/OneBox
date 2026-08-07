package com.shifenmiao.marktodo.screenLogic

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.marktodo.repo.MarkTodoRepository
import com.shifenmiao.marktodo.data.iconFromKey
import com.shifenmiao.marktodo.data.toModel
import com.shifenmiao.marktodo.model.AddTodoUiEvent
import com.shifenmiao.marktodo.model.AddTodoUiState
import com.shifenmiao.marktodo.service.MarkTodoServiceImpl
import com.shifenmiao.model.todo.TaskInput
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddTodoComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val initialCategoryId: String?,
    @Assisted val onNavigate: (Screen) -> Unit,
    @Assisted val onGoBack: () -> Unit,
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder,
    private val repository: MarkTodoRepository,
    private val service: MarkTodoServiceImpl,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(AddTodoUiState())
    val uiState: StateFlow<AddTodoUiState> = _uiState.asStateFlow()

    init {
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
                } else {
                    categories.firstOrNull()
                }

                _uiState.value = _uiState.value.copy(
                    categories = categories,
                    selectedCategory = newSelected
                )
            }
        }
    }

    fun handleEvent(event: AddTodoUiEvent): Boolean {
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
                _uiState.value = _uiState.value.copy(taskNote = event.note)
                true
            }
            is AddTodoUiEvent.UpdateTaskTags -> {
                _uiState.value = _uiState.value.copy(taskTagsText = event.tags)
                true
            }
            is AddTodoUiEvent.UpdateDueDate -> {
                _uiState.value = _uiState.value.copy(
                    dueDateMillis = event.dateMillis,
                    showDatePicker = false
                )
                true
            }
            is AddTodoUiEvent.ToggleDatePicker -> {
                _uiState.value = _uiState.value.copy(showDatePicker = event.show)
                true
            }
            is AddTodoUiEvent.SubmitTask -> {
                submitTask()
            }
            is AddTodoUiEvent.NavigateBack -> {
                onGoBack()
                true
            }
        }
    }

    private fun submitTask(): Boolean {
        val currentState = _uiState.value
        val title = currentState.taskTitle.trim()

        if (title.isBlank() || currentState.selectedCategory == null) {
            _uiState.value = currentState.copy(showValidationErrors = true)
            return false
        }

        componentScope.launch {
            service.createTask(
                input = TaskInput(
                    categoryId = currentState.selectedCategory.id,
                    title = title,
                    note = currentState.taskNote.takeIf { it.isNotBlank() },
                    dueDateMillis = currentState.dueDateMillis,
                    tags = currentState.parsedTags
                ),
                source = "UI:AddTodoScreen"
            )
            onGoBack()
        }

        return true
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
            initialCategoryId: String?,
            onNavigate: (Screen) -> Unit,
            onGoBack: () -> Unit
        ): AddTodoComponent
    }
}
