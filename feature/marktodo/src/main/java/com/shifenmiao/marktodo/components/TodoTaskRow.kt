package com.shifenmiao.marktodo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.model.TodoTag
import com.shifenmiao.marktodo.model.TodoTask
import com.shifenmiao.marktodo.theme.priorityColor
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAccessTime
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFlag
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TodoTaskRow(
    task: TodoTask,
    accentColor: Color,
    tagsByName: Map<String, TodoTag>,
    onToggleComplete: (TodoTask) -> Unit,
    onClick: (TodoTask) -> Unit,
    modifier: Modifier = Modifier,
    onTagClick: ((String) -> Unit)? = null,
    compact: Boolean = false,
    onToggleStar: ((TodoTask) -> Unit)? = null,
    onDelete: ((TodoTask) -> Unit)? = null,
) {
    val contentAlpha = if (task.isCompleted) 0.45f else 1f
    var menuExpanded by remember(task.id) { mutableStateOf(false) }
    val hasOverflowActions = onDelete != null && onToggleStar != null
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick(task) },
                onLongClick = if (compact && hasOverflowActions) {
                    { menuExpanded = true }
                } else {
                    null
                },
            )
            .padding(vertical = if (compact) 4.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TaskCompletionButton(task = task, onToggle = onToggleComplete)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = task.title,
                    style = if (compact) {
                        MaterialTheme.typography.bodyMedium
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    // 网格预览卡片窄，标题最多一行，别折成两三行
                    maxLines = if (compact) 1 else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                // 完成时间贴在标题行右侧：不额外占一行，勾选/取消时行高不变，列表不会跳
                val completionMillis = task.completedAt.takeIf {
                    task.isCompleted && task.dueDate == null && !compact
                }
                if (completionMillis != null) {
                    Text(
                        text = formatTodoDate(completionMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            // 第二行的日期只用截止时间：换成完成时间会让行高忽大忽小
            val timeMillis = task.dueDate
            val showPriority = task.priority != TodoTask.PRIORITY_MEDIUM
            if (showPriority || task.tags.isNotEmpty() || !task.note.isNullOrBlank() || timeMillis != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (showPriority) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFlag,
                            contentDescription = null,
                            tint = priorityColor(task.priority).copy(alpha = contentAlpha),
                            modifier = Modifier.size(12.dp),
                        )
                    }
                    task.tags.take(2).forEachIndexed { index, tagName ->
                        TodoTagChip(
                            tagName = tagName,
                            tag = tagsByName[tagName],
                            themeIndex = tagsByName.keys.indexOf(tagName).takeIf { it >= 0 } ?: index,
                            onClick = onTagClick?.let { callback -> { callback(tagName) } },
                        )
                    }
                    if (!task.note.isNullOrBlank()) {
                        Text(
                            text = task.note,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f * contentAlpha),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    if (timeMillis != null) {
                        val dateColor = if (task.isOverdue) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAccessTime,
                            contentDescription = null,
                            tint = dateColor,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = formatTodoDate(timeMillis),
                            style = MaterialTheme.typography.labelSmall,
                            color = dateColor,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
        if (onDelete != null && onToggleStar != null) {
            TaskOverflowMenu(
                task = task,
                onEdit = onClick,
                onToggleStar = onToggleStar,
                onDelete = onDelete,
                expanded = menuExpanded,
                onExpandedChange = { menuExpanded = it },
                showButton = !compact,
            )
        }
    }
}

@Composable
fun TodoTagChip(
    tagName: String,
    tag: TodoTag?,
    themeIndex: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val tagColor = com.shifenmiao.marktodo.theme.tagAccentColor(tag?.colorArgb, themeIndex)
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(4.dp),
            )
            .then(
                if (onClick != null) {
                    Modifier
                        .heightIn(min = 32.dp)
                        .clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "#$tagName",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.85f
            ),
            color = tagColor,
            maxLines = 1,
        )
    }
}

@Composable
fun formatTodoDate(timestamp: Long): String {
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val target = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val daysDiff = ((target.timeInMillis - today.timeInMillis) / (24 * 60 * 60 * 1000)).toInt()

    return when (daysDiff) {
        0 -> stringResource(R.string.date_today)
        1 -> stringResource(R.string.date_tomorrow)
        -1 -> stringResource(R.string.date_yesterday)
        else -> {
            val locale = Locale.getDefault()
            val pattern = if (locale.language == "zh") "M月d日" else "MMM d"
            SimpleDateFormat(pattern, locale).format(Date(timestamp))
        }
    }
}
