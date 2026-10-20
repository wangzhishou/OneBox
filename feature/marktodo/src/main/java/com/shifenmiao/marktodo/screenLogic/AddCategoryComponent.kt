package com.shifenmiao.marktodo.screenLogic

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.marktodo.repo.MarkTodoRepository
import com.shifenmiao.marktodo.service.MarkTodoServiceImpl
import com.shifenmiao.marktodo.theme.CategoryColorPaletteArgb
import com.shifenmiao.model.todo.CategoryInput
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs

/**
 * 添加/编辑分类页面组件
 *
 * 新建时预生成分类 id，并据此分配默认色板色（每个新主题默认色不同且稳定）；
 * id 随 [CategoryInput.id] 传给 service 落库，保证「按 ID 分配」前后一致。
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

    private val newCategoryId: String? = if (editingCategoryId == null) UUID.randomUUID().toString() else null

    private val _uiState = MutableStateFlow(
        AddCategoryUiState(
            colorArgb = newCategoryId?.let {
                CategoryColorPaletteArgb[abs(it.hashCode()) % CategoryColorPaletteArgb.size]
            } ?: AddCategoryUiState().colorArgb
        )
    )
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
                description = categoryEntity.description.orEmpty(),
                iconKey = categoryEntity.iconKey.ifBlank { "inbox" },
                // 全透明（历史脏数据）/未设置 → 按分类 ID 分配色板色
                colorArgb = categoryEntity.colorArgb?.takeIf { it ushr 24 != 0 }
                    ?: CategoryColorPaletteArgb[abs(categoryId.hashCode()) % CategoryColorPaletteArgb.size],
                isPinned = categoryEntity.isPinned
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
            is AddCategoryUiEvent.UpdateDescription -> {
                _uiState.value = _uiState.value.copy(description = event.description)
                true
            }
            is AddCategoryUiEvent.UpdateIconKey -> {
                _uiState.value = _uiState.value.copy(iconKey = event.iconKey)
                true
            }
            is AddCategoryUiEvent.UpdateColor -> {
                _uiState.value = _uiState.value.copy(colorArgb = event.colorArgb)
                true
            }
            is AddCategoryUiEvent.UpdatePinned -> {
                _uiState.value = _uiState.value.copy(isPinned = event.isPinned)
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

        val input = CategoryInput(
            title = title,
            iconKey = state.iconKey,
            sortOrder = 0,
            description = state.description,
            colorArgb = state.colorArgb,
            isPinned = state.isPinned,
            id = newCategoryId
        )

        componentScope.launch {
            if (editingCategoryId != null) {
                service.updateCategory(
                    categoryId = editingCategoryId,
                    input = input,
                    source = "UI:AddCategoryScreen"
                )
            } else {
                service.createCategory(
                    input = input,
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
    val description: String = "",
    val iconKey: String = "inbox",
    // 占位默认色（新建时组件会按预生成 id 重新分配色板色；编辑时按原分类 id 派生兜底）
    val colorArgb: Int = 0xFF8B5CF6.toInt(),
    val isPinned: Boolean = false,
    val showValidationErrors: Boolean = false
) {
    val isTitleValid: Boolean get() = title.isNotBlank()
    val hasTitleError: Boolean get() = showValidationErrors && !isTitleValid
}

sealed interface AddCategoryUiEvent {
    data class UpdateTitle(val title: String) : AddCategoryUiEvent
    data class UpdateDescription(val description: String) : AddCategoryUiEvent
    data class UpdateIconKey(val iconKey: String) : AddCategoryUiEvent
    data class UpdateColor(val colorArgb: Int) : AddCategoryUiEvent
    data class UpdatePinned(val isPinned: Boolean) : AddCategoryUiEvent
    data object Submit : AddCategoryUiEvent
}
