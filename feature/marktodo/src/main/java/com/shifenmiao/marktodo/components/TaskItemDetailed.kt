package com.shifenmiao.marktodo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.model.TodoTask
import com.shifenmiao.marktodo.theme.categoryContentColor
import com.shifenmiao.marktodo.theme.categoryContainerColor
import com.shifenmiao.marktodo.theme.categoryHeaderColor
import com.shifenmiao.marktodo.theme.categoryThemeForIndex
import com.t8rin.imagetoolbox.core.resources.icons.CheckCircle
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCheckBoxBlank
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAccessTime
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalendar

/**
 * 详情页任务项组件 - 极致扁平化设计，使用系统主题色
 *
 * 与首页 CategoryCard 中的 TaskItemReadOnly 风格保持一致
 * 支持编辑模式（多选）和星标功能
 *
 * @param task 任务数据
 * @param themeIndex 主题色索引，用于使用系统主题色
 * @param isEditMode 是否处于编辑模式
 * @param isSelected 是否被选中（编辑模式下）
 * @param onClick 点击任务回调
 * @param onToggleComplete 切换完成状态回调
 * @param onToggleStar 切换星标回调
 * @param onDelete 删除任务回调（仅在编辑模式下显示）
 */
@Composable
fun TaskItemDetailed(
    task: TodoTask,
    themeIndex: Int,
    isEditMode: Boolean,
    isSelected: Boolean,
    onClick: (TodoTask) -> Unit,
    onToggleComplete: (TodoTask) -> Unit,
    onToggleStar: (TodoTask) -> Unit,
    onDelete: ((TodoTask) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val theme = categoryThemeForIndex(themeIndex)
    val headerColor = categoryHeaderColor(theme)
    val contentColor = categoryContentColor(theme)

    // 卡片背景色
    val cardColor = when {
        isSelected -> headerColor.copy(alpha = 0.68f)
        else -> categoryContainerColor(theme)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = cardColor,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .clickable { onClick(task) }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 勾选框 - 使用主题色
            Icon(
                imageVector = if (task.isCompleted) {
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.CheckCircle
                } else {
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCheckBoxBlank
                },
                contentDescription = if (task.isCompleted) stringResource(R.string.cd_task_completed) else stringResource(R.string.cd_task_pending),
                tint = if (task.isCompleted) contentColor.copy(alpha = 0.6f) else contentColor.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onToggleComplete(task) }
            )

            // 任务信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 标题
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (task.isCompleted) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 2
                )

                // 备注（如果有）
                task.note?.let { note ->
                    Text(
                        text = note,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        maxLines = 1
                    )
                }

                // 标签和时间
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 显示标签（如果有）
                    if (task.tags.isNotEmpty()) {
                        task.tags.take(2).forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = headerColor,
                                        shape = RoundedCornerShape(3.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.8f
                                    ),
                                    color = contentColor,
                                    maxLines = 1
                                )
                            }
                        }
                        if (task.tags.size > 2) {
                            Text(
                                text = "+${task.tags.size - 2}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.8f
                                ),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }

                    // 显示时间
                    val dateText = formatDateShort(task.startDate)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.85f
                            ),
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1
                        )
                    }

                    // 显示截止时间（如果有）
                    task.dueDate?.let { dueDate ->
                        val dueDateText = formatDateShort(dueDate)
                        val dueDateColor = when {
                            task.isOverdue -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            task.isDueSoon -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = dueDateColor
                            )
                            Text(
                                text = dueDateText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.85f
                                ),
                                color = dueDateColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // 右侧按钮区域
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 编辑模式：显示删除按钮
                if (isEditMode && onDelete != null) {
                    IconButton(
                        onClick = { onDelete(task) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.action_delete),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    // 正常模式：显示星标按钮
                    IconButton(
                        onClick = { onToggleStar(task) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (task.isStarred) {
                                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStar
                            } else {
                                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStar
                            },
                            contentDescription = if (task.isStarred) stringResource(R.string.cd_remove_star) else stringResource(R.string.cd_add_star),
                            tint = if (task.isStarred) {
                                contentColor.copy(alpha = 0.6f)
                            } else {
                                contentColor.copy(alpha = 0.6f)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 格式化日期为简短格式（复用 CategoryCard 中的逻辑）
 */
@Composable
private fun formatDateShort(timestamp: Long): String {
    val now = System.currentTimeMillis()

    // 使用日历来正确计算天数差异
    val todayCalendar = java.util.Calendar.getInstance().apply {
        timeInMillis = now
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }

    val targetCalendar = java.util.Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }

    val daysDiff = ((todayCalendar.timeInMillis - targetCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

    return when {
        daysDiff == 0 -> stringResource(R.string.date_today)
        daysDiff == 1 -> stringResource(R.string.date_yesterday)
        daysDiff == -1 -> stringResource(R.string.date_tomorrow)
        daysDiff < -1 -> stringResource(R.string.date_days_later, -daysDiff)
        daysDiff in 2..6 -> stringResource(R.string.date_days_ago, daysDiff)
        else -> {
            val date = java.text.SimpleDateFormat("MM/dd", java.util.Locale.getDefault())
                .format(java.util.Date(timestamp))
            date
        }
    }
}
