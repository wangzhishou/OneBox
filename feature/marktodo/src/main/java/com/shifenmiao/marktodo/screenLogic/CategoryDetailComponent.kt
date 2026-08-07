package com.shifenmiao.marktodo.screenLogic

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.marktodo.repo.MarkTodoRepository
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.data.iconFromKey
import com.shifenmiao.marktodo.data.toModel
import com.shifenmiao.marktodo.model.*
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
 * 分类详情页组件，管理单个分类的任务列表
 *
 * 特性：
 * - 细粒度状态管理，最小化重组
 * - 支持筛选和排序
 * - 乐观 UI 更新
 * - 高性能任务操作
 *
 * @param componentContext Decompose 组件上下文
 * @param categoryId 分类 ID
 * @param onGoBack 返回回调
 * @param onNavigate 导航回调
 * @param context Android 上下文
 * @param dispatchersHolder 协程调度器
 * @param repository 数据仓库
 */
class CategoryDetailComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val categoryId: String,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder,
    private val repository: MarkTodoRepository,
    private val service: MarkTodoServiceImpl,
) : BaseComponent(dispatchersHolder, componentContext) {

    // 状态管理
    private val _uiState = MutableStateFlow(CategoryDetailUiState())
    val uiState: StateFlow<CategoryDetailUiState> = _uiState.asStateFlow()

    init {
        componentScope.launch {
            loadCategoryData()
        }
    }

    /**
     * 统一事件处理入口
     */
    fun handleEvent(event: CategoryDetailUiEvent): Boolean {
        return when (event) {
            // 任务操作
            is CategoryDetailUiEvent.TaskClicked -> {
                if (_uiState.value.isEditMode) {
                    toggleTaskSelection(event.task.id)
                } else {
                    navigateToTodoDetail(event.task)
                }
                true
            }
            is CategoryDetailUiEvent.AddTaskClicked -> {
                navigateToAddTodo()
                true
            }
            is CategoryDetailUiEvent.ToggleTaskComplete -> {
                toggleTaskComplete(event.task)
                true
            }
            is CategoryDetailUiEvent.ToggleTaskStar -> {
                toggleTaskStar(event.task)
                true
            }
            is CategoryDetailUiEvent.DeleteTask -> {
                deleteTask(event.task)
                true
            }

            // 筛选和排序
            is CategoryDetailUiEvent.ChangeFilter -> {
                changeFilter(event.filterMode)
                true
            }
            is CategoryDetailUiEvent.ChangeSort -> {
                changeSort(event.sortMode)
                true
            }

            // 编辑模式
            is CategoryDetailUiEvent.ToggleEditMode -> {
                toggleEditMode()
                true
            }
            is CategoryDetailUiEvent.ToggleTaskSelection -> {
                toggleTaskSelection(event.taskId)
                true
            }
            is CategoryDetailUiEvent.SelectAll -> {
                selectAll()
                true
            }
            is CategoryDetailUiEvent.ClearSelection -> {
                clearSelection()
                true
            }
            is CategoryDetailUiEvent.DeleteSelectedTasks -> {
                deleteSelectedTasks()
                true
            }
            is CategoryDetailUiEvent.ReorderTasks -> {
                reorderTasks(event.fromIndex, event.toIndex)
                true
            }

            // 导航
            is CategoryDetailUiEvent.NavigateBack -> {
                onGoBack()
                true
            }
        }
    }

    // --- 导航 ---

    private fun navigateToAddTodo() {
        onNavigate(
            Screen.MarkTodoRouter(
                Screen.MarkTodoRouter.MarkTodoType.AddTodo(initialCategoryId = categoryId)
            )
        )
    }

    private fun navigateToTodoDetail(task: TodoTask) {
        onNavigate(
            Screen.MarkTodoRouter(
                Screen.MarkTodoRouter.MarkTodoType.TodoDetail(
                    taskId = task.id,
                    categoryId = categoryId
                )
            )
        )
    }

    private fun toggleTaskComplete(task: TodoTask) {
        val newValue = !task.isCompleted

        // 乐观 UI 更新
        updateTaskInState(task.id) { it.copy(isCompleted = newValue) }

        // 持久化到数据库
        componentScope.launch {
            service.toggleTaskComplete(
                taskId = task.id,
                isCompleted = newValue,
                taskTitle = task.title,
                source = "UI:CategoryDetailScreen"
            )
        }
    }

    private fun toggleTaskStar(task: TodoTask) {
        val newValue = !task.isStarred

        // 乐观 UI 更新
        updateTaskInState(task.id) { it.copy(isStarred = newValue) }

        // 持久化到数据库
        componentScope.launch {
            service.toggleTaskStar(
                taskId = task.id,
                isStarred = newValue,
                taskTitle = task.title,
                source = "UI:CategoryDetailScreen"
            )
        }
    }

    private fun deleteTask(task: TodoTask) {
        // 乐观 UI 更新 - 从列表中移除
        _uiState.value = _uiState.value.copy(
            category = _uiState.value.category?.copy(
                tasks = _uiState.value.category!!.tasks.filter { it.id != task.id }
            )
        ).let { state ->
            state.copy(filteredTasks = applyFiltersAndSort(state.category?.tasks ?: emptyList()))
        }

        // 持久化到数据库
        componentScope.launch {
            service.deleteTask(
                taskId = task.id,
                taskTitle = task.title,
                source = "UI:CategoryDetailScreen"
            )
        }
    }

    // --- 筛选和排序 ---

    private fun changeFilter(filterMode: TaskFilterMode) {
        _uiState.value = _uiState.value.copy(
            filterMode = filterMode,
            filteredTasks = applyFiltersAndSort(
                _uiState.value.category?.tasks ?: emptyList(),
                filterMode = filterMode,
                sortMode = _uiState.value.sortMode
            )
        )
    }

    private fun changeSort(sortMode: TaskSortMode) {
        _uiState.value = _uiState.value.copy(
            sortMode = sortMode,
            filteredTasks = applyFiltersAndSort(
                _uiState.value.category?.tasks ?: emptyList(),
                filterMode = _uiState.value.filterMode,
                sortMode = sortMode
            )
        )
    }

    // --- 编辑模式 ---

    private fun toggleEditMode() {
        _uiState.value = _uiState.value.copy(
            isEditMode = !_uiState.value.isEditMode,
            selectedTaskIds = emptySet() // 切换编辑模式时清空选择
        )
    }

    private fun toggleTaskSelection(taskId: String) {
        val currentSelection = _uiState.value.selectedTaskIds
        _uiState.value = _uiState.value.copy(
            selectedTaskIds = if (taskId in currentSelection) {
                currentSelection - taskId
            } else {
                currentSelection + taskId
            }
        )
    }

    private fun selectAll() {
        val allTaskIds = _uiState.value.filteredTasks.map { it.id }.toSet()
        _uiState.value = _uiState.value.copy(selectedTaskIds = allTaskIds)
    }

    private fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedTaskIds = emptySet())
    }

    private fun deleteSelectedTasks() {
        val selectedIds = _uiState.value.selectedTaskIds
        if (selectedIds.isEmpty()) return

        // 乐观 UI 更新 - 批量删除
        _uiState.value = _uiState.value.copy(
            category = _uiState.value.category?.copy(
                tasks = _uiState.value.category!!.tasks.filter { it.id !in selectedIds }
            ),
            selectedTaskIds = emptySet()
        ).let { state ->
            state.copy(filteredTasks = applyFiltersAndSort(state.category?.tasks ?: emptyList()))
        }

        // 持久化到数据库
        componentScope.launch {
            selectedIds.forEach { taskId ->
                val taskTitle = _uiState.value.category?.tasks?.find { it.id == taskId }?.title ?: ""
                service.deleteTask(
                    taskId = taskId,
                    taskTitle = taskTitle,
                    source = "UI:CategoryDetailScreen:batch"
                )
            }
        }
    }

    private fun reorderTasks(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return

        val currentTasks = _uiState.value.filteredTasks.toMutableList()
        val movedTask = currentTasks.removeAt(fromIndex)
        currentTasks.add(toIndex, movedTask)

        // 更新 filteredTasks 中的 sortOrder
        val updatedFilteredTasks = currentTasks.mapIndexed { index, task ->
            task.copy(sortOrder = index)
        }

        // 同时更新 category.tasks 中对应任务的 sortOrder
        val updatedCategoryTasks = _uiState.value.category?.tasks?.map { task ->
            updatedFilteredTasks.find { it.id == task.id } ?: task
        } ?: emptyList()

        // 更新UI - 同时更新 category 和 filteredTasks
        _uiState.value = _uiState.value.copy(
            category = _uiState.value.category?.copy(tasks = updatedCategoryTasks),
            filteredTasks = updatedFilteredTasks
        )

        // 持久化排序到数据库
        componentScope.launch {
            service.reorderTasks(
                categoryId = categoryId,
                orderedIds = updatedFilteredTasks.map { it.id },
                source = "UI:CategoryDetailScreen"
            )
        }
    }

    /**
     * 应用筛选和排序逻辑
     */
    private fun applyFiltersAndSort(
        tasks: List<TodoTask>,
        filterMode: TaskFilterMode = _uiState.value.filterMode,
        sortMode: TaskSortMode = _uiState.value.sortMode
    ): List<TodoTask> {
        // 筛选
        val filtered = when (filterMode) {
            TaskFilterMode.ALL -> tasks
            TaskFilterMode.ACTIVE -> tasks.filter { !it.isCompleted }
            TaskFilterMode.COMPLETED -> tasks.filter { it.isCompleted }
            TaskFilterMode.STARRED -> tasks.filter { it.isStarred }
        }

        // 排序 - 注意：确保所有默认值都是 Long 类型，避免 ClassCastException
        return when (sortMode) {
            TaskSortMode.CUSTOM -> filtered.sortedBy { it.sortOrder }
            TaskSortMode.DATE_DESC -> filtered.sortedByDescending { it.dueDate ?: 0L }
            TaskSortMode.DATE_ASC -> filtered.sortedBy { it.dueDate ?: Long.MAX_VALUE }
            TaskSortMode.TITLE_ASC -> filtered.sortedBy { it.title }
            TaskSortMode.TITLE_DESC -> filtered.sortedByDescending { it.title }
            TaskSortMode.STARRED_FIRST -> filtered.sortedWith(
                compareByDescending<TodoTask> { it.isStarred }
                    .thenByDescending { it.dueDate ?: 0L }
            )
        }
    }

    // --- 数据管理 ---

    private suspend fun loadCategoryData() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        try {
            val categoryWithTasks = repository.getCategoryWithTasks(categoryId)
            if (categoryWithTasks != null) {
                // 防御性编程：确保iconKey不为空
                val safeIconKey = categoryWithTasks.category.iconKey.ifBlank { "inbox" }

                val category = categoryWithTasks.category.toModel(
                    icon = iconFromKey(safeIconKey),
                    tasks = categoryWithTasks.tasks.map { it.toModel() }
                )

                _uiState.value = _uiState.value.copy(
                    category = category,
                    filteredTasks = applyFiltersAndSort(category.tasks),
                    isLoading = false,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = context.getString(R.string.error_category_not_found)
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: context.getString(R.string.error_load_failed)
            )
        }
    }

    /**
     * 辅助函数：更新状态中的特定任务
     */
    private fun updateTaskInState(taskId: String, update: (TodoTask) -> TodoTask) {
        _uiState.value = _uiState.value.copy(
            category = _uiState.value.category?.copy(
                tasks = _uiState.value.category!!.tasks.map { task ->
                    if (task.id == taskId) update(task) else task
                }
            )
        ).let { state ->
            state.copy(filteredTasks = applyFiltersAndSort(state.category?.tasks ?: emptyList()))
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            categoryId: String,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit
        ): CategoryDetailComponent
    }
}
