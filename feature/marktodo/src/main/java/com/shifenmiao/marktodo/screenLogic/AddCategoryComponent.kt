package com.shifenmiao.marktodo.screenLogic

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.marktodo.repo.MarkTodoRepository
import com.shifenmiao.marktodo.service.MarkTodoServiceImpl
import com.shifenmiao.model.todo.CategoryInput
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 添加/编辑分类页面组件
 */
class AddCategoryComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val editingCategoryId: String?,
    @Assisted val onGoBack: () -> Unit,
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder,
    private val repository: MarkTodoRepository,
    private val service: MarkTodoServiceImpl,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(AddCategoryUiState())
    val uiState: StateFlow<AddCategoryUiState> = _uiState.asStateFlow()

    init {
        if (editingCategoryId != null) {
            componentScope.launch {
                loadEditingCategory(editingCategoryId)
            }
        }
    }

    private suspend fun loadEditingCategory(categoryId: String) {
        val categoryWithTasks = repository.getCategoryWithTasks(categoryId)
        val categoryEntity = categoryWithTasks?.category
        if (categoryEntity != null) {
            _uiState.value = AddCategoryUiState(
                title = categoryEntity.title,
                iconKey = categoryEntity.iconKey.ifBlank { "inbox" }
            )
        }
    }

    fun handleEvent(event: AddCategoryUiEvent): Boolean {
        return when (event) {
            is AddCategoryUiEvent.UpdateTitle -> {
                _uiState.value = _uiState.value.copy(
                    title = event.title,
                    showValidationErrors = _uiState.value.showValidationErrors && event.title.isBlank()
                )
                true
            }
            is AddCategoryUiEvent.UpdateIconKey -> {
                _uiState.value = _uiState.value.copy(iconKey = event.iconKey)
                true
            }
            is AddCategoryUiEvent.Submit -> {
                submit()
            }
        }
    }

    private fun submit(): Boolean {
        val state = _uiState.value
        val title = state.title.trim()

        if (title.isBlank()) {
            _uiState.value = state.copy(showValidationErrors = true)
            return false
        }

        componentScope.launch {
            if (editingCategoryId != null) {
                service.updateCategory(
                    categoryId = editingCategoryId,
                    input = CategoryInput(
                        title = title,
                        iconKey = state.iconKey,
                        sortOrder = 0
                    ),
                    source = "UI:AddCategoryScreen"
                )
            } else {
                service.createCategory(
                    input = CategoryInput(
                        title = title,
                        iconKey = state.iconKey,
                        sortOrder = 0
                    ),
                    source = "UI:AddCategoryScreen"
                )
            }
            onGoBack()
        }

        return true
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            editingCategoryId: String?,
            onGoBack: () -> Unit,
        ): AddCategoryComponent
    }
}

data class AddCategoryUiState(
    val title: String = "",
    val iconKey: String = "inbox",
    val showValidationErrors: Boolean = false
) {
    val isTitleValid: Boolean get() = title.isNotBlank()
    val hasTitleError: Boolean get() = showValidationErrors && !isTitleValid
}

sealed interface AddCategoryUiEvent {
    data class UpdateTitle(val title: String) : AddCategoryUiEvent
    data class UpdateIconKey(val iconKey: String) : AddCategoryUiEvent
    data object Submit : AddCategoryUiEvent
}
