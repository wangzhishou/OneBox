package com.shifenmiao.marktodo.screenLogic

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.marktodo.repo.MarkTodoRepository
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.data.iconFromKey
import com.shifenmiao.marktodo.data.toModel
import com.shifenmiao.marktodo.model.CategoryDetailUiEvent
import com.shifenmiao.marktodo.model.CategoryDetailUiState
import com.shifenmiao.marktodo.model.TaskFilterMode
import com.shifenmiao.marktodo.model.TodoTask
import com.shifenmiao.marktodo.service.MarkTodoServiceImpl
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.schedule.service.ScheduleService
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
 * - 筛选 + 自定义排序
 * - 乐观 UI 更新
 * - 任务删除时级联清理关联日程事件
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
    private val scheduleService: ScheduleService,
) : BaseComponent(dispatchersHolder, componentContext) {

    // 状态管理
    private val _uiState = MutableStateFlow(CategoryDetailUiState())
    val uiState: StateFlow<CategoryDetailUiState> = _uiState.asStateFlow()

    // 删除主题确认对话框
    private val _showDeleteCategoryConfirm = MutableStateFlow(false)
    val showDeleteCategoryConfirm: StateFlow<Boolean> = _showDeleteCategoryConfirm.asStateFlow()

    init {
        componentScope.launch {
            loadCategoryData()
        }
        // 标签池（任务标签配色）
        componentScope.launch {
            repository.observeTags().collect { tags ->
                _uiState.value = _uiState.value.copy(tags = tags.map { it.toModel() })
            }
        }
    }

    /**
     * 统一事件处理入口
     */
    fun handleEvent(event: CategoryDetailUiEvent): Boolean {
        return when (event) {
            // 任务操作
            is CategoryDetailUiEvent.TaskClicked -> {
                navigateToEditTodo(event.task)
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

            // 筛选
            is CategoryDetailUiEvent.ChangeFilter -> {
                changeFilter(event.filterMode)
                true
            }

            // 导航
            is CategoryDetailUiEvent.NavigateBack -> {
                onGoBack()
                true
            }

            // 主题操作（头部卡片的编辑/删除图标）
            is CategoryDetailUiEvent.EditCategoryClicked -> {
                onNavigate(
                    Screen.MarkTodoRouter(
                        Screen.MarkTodoRouter.MarkTodoType.AddCategory(editingCategoryId = categoryId)
                    )
                )
                true
            }
            is CategoryDetailUiEvent.DeleteCategoryClicked -> {
                _showDeleteCategoryConfirm.value = true
                true
            }
            is CategoryDetailUiEvent.DismissDeleteCategory -> {
                _showDeleteCategoryConfirm.value = false
                true
            }
            is CategoryDetailUiEvent.ConfirmDeleteCategory -> {
                _showDeleteCategoryConfirm.value = false
                componentScope.launch {
                    service.deleteCategory(
                        categoryId = categoryId,
                        categoryTitle = _uiState.value.category?.title ?: "",
                        source = "UI:CategoryDetailScreen"
                    )
                    onGoBack()
                }
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

    private fun navigateToEditTodo(task: TodoTask) {
        // 原型设计稿：点击任务直接进入编辑待办页
        onNavigate(
            Screen.MarkTodoRouter(
                Screen.MarkTodoRouter.MarkTodoType.AddTodo(
                    initialCategoryId = categoryId,
                    editingTaskId = task.id
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
            state.copy(filteredTasks = applyFilters(state.category?.tasks ?: emptyList()))
        }

        // 持久化到数据库（级联清理关联日程事件）
        componentScope.launch {
            runCatching { scheduleService.deleteEventsByLinkedTaskId(task.id) }
            service.deleteTask(
                taskId = task.id,
                taskTitle = task.title,
                source = "UI:CategoryDetailScreen"
            )
        }
    }

    // --- 筛选 ---

    private fun changeFilter(filterMode: TaskFilterMode) {
        _uiState.value = _uiState.value.copy(
            filterMode = filterMode,
            filteredTasks = applyFilters(
                _uiState.value.category?.tasks ?: emptyList(),
                filterMode = filterMode
            )
        )
    }

    /**
     * 应用筛选逻辑（任务按 sortOrder 自定义顺序展示）
     */
    private fun applyFilters(
        tasks: List<TodoTask>,
        filterMode: TaskFilterMode = _uiState.value.filterMode
    ): List<TodoTask> {
        val filtered = when (filterMode) {
            TaskFilterMode.ALL -> tasks
            TaskFilterMode.ACTIVE -> tasks.filter { !it.isCompleted }
            TaskFilterMode.COMPLETED -> tasks.filter { it.isCompleted }
            TaskFilterMode.STARRED -> tasks.filter { it.isStarred }
        }
        return filtered.sortedBy { it.sortOrder }
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
                    filteredTasks = applyFilters(category.tasks),
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
            state.copy(filteredTasks = applyFilters(state.category?.tasks ?: emptyList()))
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
