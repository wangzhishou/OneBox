/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.marktodo.model

import androidx.compose.runtime.Immutable

/**
 * 首页底部状态页签：全部 / 进行中 / 已完成。
 *
 * 页面本身按分组卡片展示，页签只决定每张卡片里显示哪些任务
 * （进行中 = 未完成的，已完成 = 已完成的）。
 */
@Immutable
enum class TaskCompletionFilter {
    ALL, ACTIVE, COMPLETED
}

@Immutable
enum class TaskDateScope {
    ALL, TODAY, OVERDUE, NEXT_7_DAYS, UPCOMING, NO_DATE, RANGE
}

@Immutable
data class TaskFilter(
    val completion: TaskCompletionFilter = TaskCompletionFilter.ACTIVE,
    val categoryIds: Set<String> = emptySet(),
    val includeInbox: Boolean = false,
    val tagNames: Set<String> = emptySet(),
    val priorities: Set<Int> = emptySet(),
    val dateScope: TaskDateScope = TaskDateScope.ALL,
    val dueStartMillis: Long? = null,
    val dueEndMillis: Long? = null,
    val starredOnly: Boolean = false,
) {
    val hasCategoryFilter: Boolean get() = categoryIds.isNotEmpty() || includeInbox
    val hasDueRange: Boolean get() = dueStartMillis != null || dueEndMillis != null
    val hasDateFilter: Boolean get() = hasSmartDateFilter || hasDueRange
    val hasSmartDateFilter: Boolean get() = dateScope != TaskDateScope.ALL && dateScope != TaskDateScope.RANGE
    /** 是否有「完成状态」以外的筛选条件（完成状态由首页底部页签单独控制） */
    val hasAdvancedFilters: Boolean
        get() = hasCategoryFilter ||
            tagNames.isNotEmpty() ||
            priorities.isNotEmpty() ||
            hasDueRange ||
            dateScope == TaskDateScope.UPCOMING ||
            dateScope == TaskDateScope.NO_DATE ||
            starredOnly
}

fun TaskFilter.withQuickDateExclusivity(): TaskFilter {
    val hasQuickDate = dateScope == TaskDateScope.TODAY ||
        dateScope == TaskDateScope.OVERDUE ||
        dateScope == TaskDateScope.NEXT_7_DAYS
    return if (hasAdvancedFilters && hasQuickDate) {
        copy(dateScope = TaskDateScope.ALL, dueStartMillis = null, dueEndMillis = null)
    } else {
        this
    }
}

@Immutable
enum class TaskSortMode {
    MANUAL, DUE_DATE, PRIORITY, CREATED_AT
}

@Immutable
data class TaskSortConfig(
    val mode: TaskSortMode = TaskSortMode.MANUAL,
    val ascending: Boolean = true,
)

@Immutable
enum class TodayGroup {
    OVERDUE, TODAY, UPCOMING, NO_DATE
}

@Immutable
data class TaskWithCategory(
    val task: TodoTask,
    val category: TodoCategory? = null,
    val categoryOrder: Int = 0,
)

@Immutable
data class TodayTaskGroup(
    val group: TodayGroup,
    val tasks: List<TaskWithCategory>,
)
