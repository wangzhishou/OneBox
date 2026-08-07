package com.shifenmiao.marktodo.screenLogic

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.marktodo.repo.MarkTodoRepository
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.data.iconFromKey
import com.shifenmiao.marktodo.data.toModel
import com.shifenmiao.marktodo.model.DialogState
import com.shifenmiao.marktodo.model.MarkTodoUiEvent
import com.shifenmiao.marktodo.model.MarkTodoUiState
import com.shifenmiao.marktodo.model.TodoCategory
import com.shifenmiao.marktodo.model.TodoTask
import com.shifenmiao.marktodo.service.MarkTodoServiceImpl
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

/**
 * Refactored component for managing MarkTodo screen state and logic.
 *
 * Key improvements:
 * - Event-driven architecture with sealed interface for type-safe events
 * - StateFlow for reactive state management with minimal recompositions
 * - Separation of concerns: UI logic, business logic, and data layer
 * - Proper error handling and validation
 * - Immutable state for predictable updates
 *
 * @param componentContext Decompose component context for lifecycle management
 * @param context Android context for resource access
 * @param dispatchersHolder Coroutine dispatchers for threading
 * @param repository Data repository for persisting categories and tasks
 */
class MarkTodoComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onNavigate: (Screen) -> Unit,
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder,
    private val repository: MarkTodoRepository,
    private val service: MarkTodoServiceImpl,
) : BaseComponent(dispatchersHolder, componentContext) {

    // State management using StateFlow for fine-grained reactivity
    private val _uiState = MutableStateFlow(MarkTodoUiState())
    val uiState: StateFlow<MarkTodoUiState> = _uiState.asStateFlow()

    private val _categoriesState = MutableStateFlow<List<TodoCategory>>(emptyList())
    val categoriesState: StateFlow<List<TodoCategory>> = _categoriesState.asStateFlow()

    private val _dialogState = MutableStateFlow<DialogState>(DialogState.Dismissed)
    val dialogState: StateFlow<DialogState> = _dialogState.asStateFlow()


    init {
        // 使用 Flow 自动观察数据库变化，实现响应式更新
        // 任何对分类或任务的增删改操作都会自动触发 UI 更新
        componentScope.launch {
            repository.observeDashboard().collect { dashboardData ->
                _categoriesState.value = dashboardData.map { rel ->
                    // 防御性编程：确保iconKey不为空
                    val safeIconKey = rel.category.iconKey.ifBlank { "inbox" }

                    rel.category.toModel(
                        icon = iconFromKey(safeIconKey),
                        tasks = rel.tasks.map { it.toModel() }.sortedBy { it.sortOrder }
                    )
                }
            }
        }
    }

    /**
     * Central event handler - single entry point for all user interactions.
     * Provides type safety and clear flow of events.
     *
     * @return Boolean indicating if the operation was successful (useful for UI feedback)
     */
    fun handleEvent(event: MarkTodoUiEvent): Boolean {
        return when (event) {
            // Category events
            is MarkTodoUiEvent.CategoryClicked -> {
                handleCategoryClick(event.category)
                true
            }
            is MarkTodoUiEvent.AddCategoryClicked -> {
                openAddCategoryDialog()
                true
            }
            is MarkTodoUiEvent.EditCategoryClicked -> {
                openEditCategoryDialog(event.category)
                true
            }
            is MarkTodoUiEvent.DeleteCategory -> {
                deleteCategory(event.category)
                true
            }
            is MarkTodoUiEvent.ConfirmDeleteCategory -> {
                confirmDeleteCategory(event.category)
                true
            }

            // Task events
            is MarkTodoUiEvent.TaskClicked -> {
                handleTaskClick(event.task)
                true
            }
            is MarkTodoUiEvent.AddTaskClicked -> {
                onNavigate(Screen.MarkTodoRouter(Screen.MarkTodoRouter.MarkTodoType.AddTodo(initialCategoryId = event.category.id)))
                true
            }
            is MarkTodoUiEvent.ToggleTaskComplete -> {
                toggleTaskComplete(event.task)
                true
            }
            is MarkTodoUiEvent.ToggleTaskStar -> {
                toggleTaskStar(event.task)
                true
            }

            // Edit mode
            is MarkTodoUiEvent.ToggleEditMode -> {
                toggleEditMode()
                true
            }

            is MarkTodoUiEvent.ReorderCategories -> {
                reorderCategories(event.fromIndex, event.toIndex)
                true
            }

            // Dialog actions
            is MarkTodoUiEvent.DismissDialog -> {
                dismissDialog()
                true
            }

            is MarkTodoUiEvent.NavigateBack -> {
                // Handle navigation if needed
                true
            }
        }
    }

    // --- Category Operations ---

    private fun handleCategoryClick(category: TodoCategory) {
        val safeTitle = category.title.ifBlank { context.getString(R.string.category_unnamed) }
        onNavigate(Screen.MarkTodoRouter(
            Screen.MarkTodoRouter.MarkTodoType.CategoryDetail(
                categoryId = category.id,
                categoryTitle = safeTitle
            )
        ))
    }

    private fun openAddCategoryDialog() {
        onNavigate(
            Screen.MarkTodoRouter(
                Screen.MarkTodoRouter.MarkTodoType.AddCategory()
            )
        )
    }

    private fun openEditCategoryDialog(category: TodoCategory) {
        onNavigate(
            Screen.MarkTodoRouter(
                Screen.MarkTodoRouter.MarkTodoType.AddCategory(editingCategoryId = category.id)
            )
        )
    }

    /**
     * 切换编辑模式
     */
    private fun toggleEditMode() {
        _uiState.value = _uiState.value.copy(
            isEditMode = !_uiState.value.isEditMode
        )
    }

    /**
     * 显示删除分类确认对话框
     */
    private fun deleteCategory(category: TodoCategory) {
        _dialogState.value = DialogState.DeleteCategoryConfirm(category)
    }

    /**
     * 确认删除分类
     * 注意：由于数据库外键设置了 CASCADE，删除分类会自动删除该分类下的所有任务
     */
    private fun confirmDeleteCategory(category: TodoCategory) {
        componentScope.launch {
            try {
                dismissDialog()
                service.deleteCategory(
                    categoryId = category.id,
                    categoryTitle = category.title,
                    source = "UI:MarkTodoScreen"
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 重排序分类
     * @param fromIndex 起始位置
     * @param toIndex 目标位置
     */
    private fun reorderCategories(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return

        // 乐观 UI 更新
        val currentCategories = _categoriesState.value.toMutableList()
        val movedCategory = currentCategories.removeAt(fromIndex)
        currentCategories.add(toIndex, movedCategory)
        _categoriesState.value = currentCategories

        // 立即持久化到数据库
        componentScope.launch {
            service.reorderCategories(
                orderedIds = currentCategories.map { it.id },
                source = "UI:MarkTodoScreen"
            )
        }
    }

    // --- Task Operations ---

    private fun handleTaskClick(task: TodoTask) {
        onNavigate(
            Screen.Schedule(
                linkedTaskId = task.id,
                focusDateMillis = task.dueDate
            )
        )
    }



    private fun toggleTaskComplete(task: TodoTask) {
        val newValue = !task.isCompleted

        // Optimistic UI update
        _categoriesState.value = _categoriesState.value.map { category ->
            category.copy(
                tasks = category.tasks.map { t ->
                    if (t.id == task.id) t.copy(isCompleted = newValue) else t
                }
            )
        }

        // Persist to database
        componentScope.launch {
            service.toggleTaskComplete(
                taskId = task.id,
                isCompleted = newValue,
                taskTitle = task.title,
                source = "UI:MarkTodoScreen"
            )
        }
    }

    private fun toggleTaskStar(task: TodoTask) {
        val newValue = !task.isStarred

        // Optimistic UI update
        _categoriesState.value = _categoriesState.value.map { category ->
            category.copy(
                tasks = category.tasks.map { t ->
                    if (t.id == task.id) t.copy(isStarred = newValue) else t
                }
            )
        }

        // Persist to database
        componentScope.launch {
            service.toggleTaskStar(
                taskId = task.id,
                isStarred = newValue,
                taskTitle = task.title,
                source = "UI:MarkTodoScreen"
            )
        }
    }

    // --- Dialog Management ---

    private fun dismissDialog() {
        _dialogState.value = DialogState.Dismissed
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onNavigate: (Screen) -> Unit
        ): MarkTodoComponent
    }
}
