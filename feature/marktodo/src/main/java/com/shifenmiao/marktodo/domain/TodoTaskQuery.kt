package com.shifenmiao.marktodo.domain

import com.shifenmiao.marktodo.model.TaskCompletionFilter
import com.shifenmiao.marktodo.model.TaskDateScope
import com.shifenmiao.marktodo.model.TaskFilter
import com.shifenmiao.marktodo.model.TaskSortConfig
import com.shifenmiao.marktodo.model.TaskSortMode
import com.shifenmiao.marktodo.model.TaskWithCategory
import com.shifenmiao.marktodo.model.TodayGroup
import com.shifenmiao.marktodo.model.TodayTaskGroup
import com.shifenmiao.marktodo.model.TodoCategory
import com.shifenmiao.marktodo.model.TodoTask
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object TodoTaskQuery {

    fun flatten(
        tasks: List<TodoTask>,
        categories: List<TodoCategory>,
    ): List<TaskWithCategory> {
        val categoriesById = categories.associateBy { it.id }
        val categoryOrders = categories.mapIndexed { index, category -> category.id to index }.toMap()
        return tasks.map { task ->
            TaskWithCategory(
                task = task,
                category = task.categoryId?.let(categoriesById::get),
                categoryOrder = task.categoryId?.let { categoryOrders[it] } ?: -1,
            )
        }
    }

    fun filterAndSort(
        tasks: List<TaskWithCategory>,
        searchQuery: String = "",
        filter: TaskFilter = TaskFilter(),
        sort: TaskSortConfig = TaskSortConfig(),
        retainedCompletedTaskIds: Set<String> = emptySet(),
        today: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): List<TaskWithCategory> {
        val query = searchQuery.trim()
        return tasks.asSequence()
            .filter { item -> item.task.matchesSearch(query) }
            .filter { item ->
                item.task.matchesFilter(filter, retainedCompletedTaskIds, today, zoneId)
            }
            .sortedWith(taskComparator(sort))
            .toList()
    }

    fun groupForToday(
        tasks: List<TaskWithCategory>,
        now: Long = System.currentTimeMillis(),
        today: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): List<TodayTaskGroup> {
        return tasks.groupBy { item ->
            val task = item.task
            when {
                task.dueDate == null -> TodayGroup.NO_DATE
                TodoTaskTimeRules.isOverdue(
                    dueDateMillis = task.dueDate,
                    hasDueTime = task.hasDueTime,
                    isCompleted = false,
                    now = now,
                    today = today,
                    zoneId = zoneId,
                ) -> TodayGroup.OVERDUE
                task.dueDate.toLocalDate(zoneId) == today -> TodayGroup.TODAY
                else -> TodayGroup.UPCOMING
            }
        }.toSortedMap(compareBy { it.ordinal })
            .map { (group, items) -> TodayTaskGroup(group = group, tasks = items) }
    }

    private fun TodoTask.matchesSearch(query: String): Boolean {
        if (query.isEmpty()) return true
        return title.contains(query, ignoreCase = true) ||
            note?.contains(query, ignoreCase = true) == true ||
            tags.any { it.equals(query, ignoreCase = true) }
    }

    private fun TodoTask.matchesFilter(
        filter: TaskFilter,
        retainedCompletedTaskIds: Set<String>,
        today: LocalDate,
        zoneId: ZoneId,
    ): Boolean {
        val completionMatches = when (filter.completion) {
            TaskCompletionFilter.ALL -> true
            TaskCompletionFilter.ACTIVE -> !isCompleted || id in retainedCompletedTaskIds
            TaskCompletionFilter.COMPLETED -> isCompleted
        }
        if (!completionMatches) return false
        if (filter.starredOnly && !isStarred) return false
        if (filter.hasCategoryFilter) {
            val categoryMatches = if (categoryId == null) {
                filter.includeInbox
            } else {
                categoryId in filter.categoryIds
            }
            if (!categoryMatches) return false
        }
        if (filter.tagNames.isNotEmpty() && tags.none(filter.tagNames::contains)) return false
        if (filter.priorities.isNotEmpty() && priority !in filter.priorities) return false
        val dateScopeMatches = when (filter.dateScope) {
            TaskDateScope.ALL, TaskDateScope.RANGE -> true
            TaskDateScope.TODAY -> dueDate?.toLocalDate(zoneId) == today
            TaskDateScope.OVERDUE -> TodoTaskTimeRules.isOverdue(
                dueDateMillis = dueDate,
                hasDueTime = hasDueTime,
                isCompleted = isCompleted,
                now = System.currentTimeMillis(),
                today = today,
                zoneId = zoneId,
            )
            TaskDateScope.UPCOMING -> dueDate?.toLocalDate(zoneId)?.let { it > today } == true
            TaskDateScope.NO_DATE -> dueDate == null
        }
        if (!dateScopeMatches) return false
        if (filter.hasDueRange) {
            val due = dueDate?.toLocalDate(zoneId) ?: return false
            val start = filter.dueStartMillis?.toLocalDate(zoneId)
            val end = filter.dueEndMillis?.toLocalDate(zoneId)
            if (start != null && due < start) return false
            if (end != null && due > end) return false
        }
        return true
    }

    private fun taskComparator(sort: TaskSortConfig): Comparator<TaskWithCategory> {
        val primary = Comparator<TaskWithCategory> { left, right ->
            when (sort.mode) {
                TaskSortMode.MANUAL -> {
                    compareValuesDirection(left.categoryOrder, right.categoryOrder, sort.ascending)
                        .takeIf { it != 0 }
                        ?: compareValuesDirection(left.task.sortOrder, right.task.sortOrder, sort.ascending)
                            .takeIf { it != 0 }
                            ?: compareValuesDirection(left.task.createdAt, right.task.createdAt, sort.ascending)
                }
                TaskSortMode.DUE_DATE -> compareNullableDueDates(left.task, right.task, sort.ascending)
                TaskSortMode.PRIORITY -> compareValuesDirection(
                    left.task.priority,
                    right.task.priority,
                    ascending = sort.ascending,
                )
                TaskSortMode.CREATED_AT -> compareValuesDirection(
                    left.task.createdAt,
                    right.task.createdAt,
                    ascending = sort.ascending,
                )
            }
        }
        return primary.thenBy { it.task.id }
    }

    private fun compareNullableDueDates(left: TodoTask, right: TodoTask, ascending: Boolean): Int {
        val leftDue = left.dueDate
        val rightDue = right.dueDate
        if (leftDue == null && rightDue == null) return 0
        if (leftDue == null) return 1
        if (rightDue == null) return -1
        return compareValuesDirection(leftDue, rightDue, ascending)
    }

    private fun <T : Comparable<T>> compareValuesDirection(left: T, right: T, ascending: Boolean): Int {
        val result = compareValues(left, right)
        return if (ascending) result else -result
    }

    private fun Long.toLocalDate(zoneId: ZoneId): LocalDate =
        Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()
}
