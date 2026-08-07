package com.shifenmiao.marktodo.model

import androidx.compose.runtime.Immutable

/**
 * 分类详情页的 UI 状态
 * 采用不可变数据类确保状态更新的可预测性和性能
 */
@Immutable
data class CategoryDetailUiState(
    val category: TodoCategory? = null,
    val filteredTasks: List<TodoTask> = emptyList(),
    val filterMode: TaskFilterMode = TaskFilterMode.ALL,
    val sortMode: TaskSortMode = TaskSortMode.CUSTOM, // 默认自定义排序
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false, // 编辑模式
    val selectedTaskIds: Set<String> = emptySet() // 选中的任务ID
) {
    val hasTasks: Boolean get() = filteredTasks.isNotEmpty()
    val totalCount: Int get() = category?.totalCount ?: 0
    val completedCount: Int get() = category?.completedCount ?: 0
    val starredCount: Int get() = filteredTasks.count { it.isStarred }
    val hasSelection: Boolean get() = selectedTaskIds.isNotEmpty()
    val selectedCount: Int get() = selectedTaskIds.size
}

/**
 * 任务筛选模式
 */
enum class TaskFilterMode {
    ALL,        // 全部
    ACTIVE,     // 未完成
    COMPLETED,  // 已完成
    STARRED     // 已标星
}

/**
 * 任务排序模式
 */
enum class TaskSortMode {
    CUSTOM,         // 自定义排序（拖拽排序）
    DATE_DESC,      // 日期降序
    DATE_ASC,       // 日期升序
    TITLE_ASC,      // 标题升序
    TITLE_DESC,     // 标题降序
    STARRED_FIRST   // 标星优先
}

/**
 * 分类详情页的用户事件
 */
sealed interface CategoryDetailUiEvent {
    // 任务操作
    data class TaskClicked(val task: TodoTask) : CategoryDetailUiEvent
    data object AddTaskClicked : CategoryDetailUiEvent
    data class ToggleTaskComplete(val task: TodoTask) : CategoryDetailUiEvent
    data class ToggleTaskStar(val task: TodoTask) : CategoryDetailUiEvent
    data class DeleteTask(val task: TodoTask) : CategoryDetailUiEvent

    // 筛选和排序
    data class ChangeFilter(val filterMode: TaskFilterMode) : CategoryDetailUiEvent
    data class ChangeSort(val sortMode: TaskSortMode) : CategoryDetailUiEvent

    // 编辑模式
    data object ToggleEditMode : CategoryDetailUiEvent
    data class ToggleTaskSelection(val taskId: String) : CategoryDetailUiEvent
    data object SelectAll : CategoryDetailUiEvent
    data object ClearSelection : CategoryDetailUiEvent
    data object DeleteSelectedTasks : CategoryDetailUiEvent
    data class ReorderTasks(val fromIndex: Int, val toIndex: Int) : CategoryDetailUiEvent

    // 导航
    data object NavigateBack : CategoryDetailUiEvent
}
