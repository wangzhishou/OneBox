package com.shifenmiao.marktodo.screenLogic

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.marktodo.entity.MarkTodoTagEntity
import com.shifenmiao.database.marktodo.repo.MarkTodoRepository
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.data.iconFromKey
import com.shifenmiao.marktodo.data.toModel
import com.shifenmiao.marktodo.model.DialogState
import com.shifenmiao.marktodo.model.MarkTodoUiEvent
import com.shifenmiao.marktodo.model.MarkTodoUiState
import com.shifenmiao.marktodo.model.TodoCategory
import com.shifenmiao.marktodo.model.TodoStatusTab
import com.shifenmiao.marktodo.model.TodoTag
import com.shifenmiao.marktodo.model.TodoTask
import com.shifenmiao.marktodo.service.MarkTodoServiceImpl
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.tencent.mmkv.MMKV
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 待办首页（仪表板）组件：状态页签（全部/进行中/已完成）+ 标签筛选 + 列表/网格视图。
 *
 * - 数据经 [MarkTodoRepository.observeDashboard] / [MarkTodoRepository.observeTags] 响应式驱动
 * - 写入一律走 [MarkTodoServiceImpl]（含审计日志）
 * - 页签与标签筛选为纯内存计算，见 [filterCategories]
 */
class MarkTodoComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onNavigate: (Screen) -> Unit,
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder,
    private val repository: MarkTodoRepository,
    private val service: MarkTodoServiceImpl,
) : BaseComponent(dispatchersHolder, componentContext) {

    // 视图偏好持久化（网格/列表 + 状态页签，进程重建后保持）
    private val prefs by lazy { MMKV.mmkvWithID("marktodo_home") }

    // State management using StateFlow for fine-grained reactivity
    private val _uiState = MutableStateFlow(
        MarkTodoUiState(
            statusTab = prefs.decodeString(KEY_STATUS_TAB, null)
                ?.let { runCatching { TodoStatusTab.valueOf(it) }.getOrNull() }
                ?: TodoStatusTab.ACTIVE,
            isGridView = prefs.decodeBool(KEY_GRID_VIEW, true)
        )
    )
    val uiState: StateFlow<MarkTodoUiState> = _uiState.asStateFlow()

    private val _categoriesState = MutableStateFlow<List<TodoCategory>>(emptyList())
    val categoriesState: StateFlow<List<TodoCategory>> = _categoriesState.asStateFlow()

    private val _tagsState = MutableStateFlow<List<TodoTag>>(emptyList())
    val tagsState: StateFlow<List<TodoTag>> = _tagsState.asStateFlow()

    private val _dialogState = MutableStateFlow<DialogState>(DialogState.Dismissed)
    val dialogState: StateFlow<DialogState> = _dialogState.asStateFlow()


    init {
        // 预置标签兜底种子：新装由 FeatureDatabase onCreate 本地化写入，老库升级后表为空时按当前语言补齐
        componentScope.launch {
            service.ensurePresetTags(defaultPresetTagNames())
        }

        // 使用 Flow 自动观察数据库变化，实现响应式更新
        componentScope.launch {
            repository.observeTags().collect { tags ->
                _tagsState.value = tags.map { it.toModel() }
            }
        }

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
     * 预置标签 id → 本地化名称
     */
    private fun defaultPresetTagNames(): Map<String, String> = mapOf(
        "preset_work" to context.getString(R.string.preset_tag_work),
        "preset_study" to context.getString(R.string.preset_tag_study),
        "preset_life" to context.getString(R.string.preset_tag_life),
        "preset_health" to context.getString(R.string.preset_tag_health),
        "preset_travel" to context.getString(R.string.preset_tag_travel),
    )

    /**
     * Central event handler - single entry point for all user interactions.
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
                onNavigate(Screen.MarkTodoRouter(Screen.MarkTodoRouter.MarkTodoType.AddTodo(initialCategoryId = event.category?.id)))
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

            // 状态页签 / 标签筛选 / 视图切换（页签与视图偏好持久化到 MMKV）
            is MarkTodoUiEvent.SelectStatusTab -> {
                _uiState.value = _uiState.value.copy(statusTab = event.tab)
                prefs.encode(KEY_STATUS_TAB, event.tab.name)
                true
            }
            is MarkTodoUiEvent.SelectTag -> {
                _uiState.value = if (event.tagName == null) {
                    // 清除筛选并收起筛选行
                    _uiState.value.copy(selectedTagName = null, tagFilterVisible = false)
                } else {
                    _uiState.value.copy(selectedTagName = event.tagName, tagFilterVisible = true)
                }
                true
            }
            is MarkTodoUiEvent.TagOnTaskClicked -> {
                // 点击任务上的标签：呼出筛选行并选中对应标签；再点一次（已选中）则收起
                val current = _uiState.value
                _uiState.value = if (current.tagFilterVisible && current.selectedTagName == event.tagName) {
                    current.copy(selectedTagName = null, tagFilterVisible = false)
                } else {
                    current.copy(tagFilterVisible = true, selectedTagName = event.tagName)
                }
                true
            }
            is MarkTodoUiEvent.SetGridView -> {
                _uiState.value = _uiState.value.copy(isGridView = event.isGrid)
                prefs.encode(KEY_GRID_VIEW, event.isGrid)
                true
            }

            // 排序模式
            is MarkTodoUiEvent.SetReorderMode -> {
                _uiState.value = _uiState.value.copy(isReorderMode = event.enabled)
                true
            }
            is MarkTodoUiEvent.ReorderCategories -> {
                reorderVisibleCategories(event.fromIndex, event.toIndex)
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
     * 拖拽排序：交换可见列表中的 from/to 下标，合并回全量顺序后乐观更新 + 持久化。
     *
     * 注意：reorderable 的 onMove 在拖拽过程中会连续触发，每次都必须基于
     * 当前最新状态计算（不能依赖 UI 层捕获的旧列表，否则连续拖拽会写坏顺序）。
     */
    private fun reorderVisibleCategories(fromIndex: Int, toIndex: Int) {
        val state = _uiState.value
        val all = _categoriesState.value
        val visible = filterCategories(all, state.statusTab, state.selectedTagName)
        if (fromIndex !in visible.indices || toIndex !in visible.indices) return

        val reorderedVisible = visible.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        val visibleIds = reorderedVisible.map { it.id }.toSet()
        val queue = ArrayDeque(reorderedVisible.map { it.id })
        val mergedIds = all.map { category ->
            if (category.id in visibleIds) queue.removeFirst() else category.id
        }

        // 乐观 UI 更新
        val byId = all.associateBy { it.id }
        _categoriesState.value = mergedIds.mapNotNull { byId[it] }

        // 立即持久化到数据库
        componentScope.launch {
            service.reorderCategories(
                orderedIds = mergedIds,
                source = "UI:MarkTodoScreen"
            )
        }
    }

    // --- Task Operations ---

    /**
     * 点击任务 → 直接进入编辑页（原型设计稿：编辑待办页）
     */
    private fun handleTaskClick(task: TodoTask) {
        onNavigate(
            Screen.MarkTodoRouter(
                Screen.MarkTodoRouter.MarkTodoType.AddTodo(
                    initialCategoryId = task.categoryId.ifBlank { null },
                    editingTaskId = task.id
                )
            )
        )
    }

    private fun toggleTaskComplete(task: TodoTask) {
        val newValue = !task.isCompleted

        // Optimistic UI update
        _categoriesState.value = _categoriesState.value.map { category ->
            category.copy(
                tasks = category.tasks.map { t ->
                    if (t.id == task.id) {
                        t.copy(
                            isCompleted = newValue,
                            completedAt = if (newValue) System.currentTimeMillis() else null
                        )
                    } else t
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

    companion object {
        private const val KEY_STATUS_TAB = "status_tab"
        private const val KEY_GRID_VIEW = "grid_view"

        /**
         * 按状态页签 + 选中标签过滤分类与任务（纯函数，UI 层 remember 使用）。
         *
         * - 进行中：仅未完成任务；已完成：仅已完成任务（按完成时间倒序）
         * - 标签筛选：任务 tags 含选中标签名
         * - 无可见任务的分类整组隐藏
         */
        fun filterCategories(
            categories: List<TodoCategory>,
            tab: TodoStatusTab,
            selectedTagName: String?,
        ): List<TodoCategory> {
            return categories.mapNotNull { category ->
                val visibleTasks = category.tasks
                    .filter { task ->
                        when (tab) {
                            TodoStatusTab.ALL -> true
                            TodoStatusTab.ACTIVE -> !task.isCompleted
                            TodoStatusTab.COMPLETED -> task.isCompleted
                        }
                    }
                    .filter { task ->
                        selectedTagName == null || task.tags.contains(selectedTagName)
                    }
                    .let { tasks ->
                        if (tab == TodoStatusTab.COMPLETED) {
                            tasks.sortedByDescending { it.completedAt ?: it.dueDate ?: 0L }
                        } else {
                            tasks
                        }
                    }
                if (visibleTasks.isEmpty() && (tab != TodoStatusTab.ALL || selectedTagName != null)) {
                    null
                } else {
                    category.copy(tasks = visibleTasks)
                }
            }
        }
    }
}
