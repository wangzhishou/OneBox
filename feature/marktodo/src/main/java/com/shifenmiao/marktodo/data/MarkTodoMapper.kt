package com.shifenmiao.marktodo.data

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import com.shifenmiao.base.ui.icon.IconRegistry
import com.shifenmiao.database.marktodo.entity.MarkTodoCategoryEntity
import com.shifenmiao.database.marktodo.entity.MarkTodoTaskEntity
import com.shifenmiao.marktodo.model.TodoCategory
import com.shifenmiao.marktodo.model.TodoTask
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInbox

/**
 * Entity ↔ UI Model 转换工具。
 *
 * 将数据库实体转换为 UI 层可直接使用的模型，
 * 并处理 icon 解析等 UI 层关注的问题。
 */

// ── Category 转换 ──────────────────────────────────────

internal fun TodoCategory.toEntity(
    sortOrder: Int,
    iconKeyOverride: String,
): MarkTodoCategoryEntity {
    return MarkTodoCategoryEntity(
        id = id,
        title = title,
        iconKey = iconKeyOverride,
        sortOrder = sortOrder,
        updatedAt = System.currentTimeMillis(),
    )
}

internal fun MarkTodoCategoryEntity.toModel(
    icon: ImageVector,
    tasks: List<TodoTask>,
): TodoCategory {
    return TodoCategory(
        id = id,
        title = title.ifBlank { iconKey },
        icon = icon,
        tasks = tasks
    )
}

// ── Task 转换 ──────────────────────────────────────────

internal fun TodoTask.toEntity(categoryId: String): MarkTodoTaskEntity {
    return MarkTodoTaskEntity(
        id = id,
        categoryId = categoryId,
        title = title,
        note = note,
        startDate = startDate,
        dueDate = dueDate,
        tags = tags,
        isCompleted = isCompleted,
        isStarred = isStarred,
        sortOrder = sortOrder,
        updatedAt = System.currentTimeMillis(),
    )
}

internal fun MarkTodoTaskEntity.toModel(): TodoTask {
    return TodoTask(
        id = id,
        title = title,
        note = note,
        startDate = startDate,
        dueDate = dueDate,
        tags = tags,
        isCompleted = isCompleted,
        isStarred = isStarred,
        sortOrder = sortOrder
    )
}

// ── Icon 转换 ──────────────────────────────────────────

/**
 * 从 key 获取图标，优先从 IconRegistry 查找，找不到则返回默认的 Inbox 图标。
 */
fun iconFromKey(key: String): ImageVector {
    return IconRegistry.resolve(key) ?: com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInbox
}

/**
 * 从图标获取 key（反向查找），找不到则返回 "Category" 作为默认值。
 */
fun iconKeyFromIcon(icon: ImageVector): String {
    return IconRegistry.allKeys.firstOrNull { IconRegistry.resolve(it) == icon } ?: "Category"
}
