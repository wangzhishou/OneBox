package com.shifenmiao.marktodo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.t8rin.imagetoolbox.core.resources.icons.CheckCircle
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAccessTime
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCheckBoxBlank
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFlag
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 待办任务行 —— 两行布局：
 * 第一行：勾选框 + 标题（完整换行显示，不截断不折叠）
 * 第二行：#标签（可点击，点击=按该标签筛选）+ 备注 + 时间
 *
 * 已完成任务：标题删除线 + 整体降透明度，时间显示完成时间。
 *
 * @param accentColor 主题强调色（勾选框/图标着色）
 * @param tagsByName 标签名 → 标签（配色查找）
 * @param onTagClick 点击任务上的标签 chip（首页用来呼出并选中标签筛选）
 */
@Composable
fun TodoTaskRow(
    task: TodoTask,
    accentColor: Color,
    tagsByName: Map<String, TodoTag>,
    onToggleComplete: (TodoTask) -> Unit,
    onClick: (TodoTask) -> Unit,
    modifier: Modifier = Modifier,
    onTagClick: ((String) -> Unit)? = null,
    compact: Boolean = false
) {
    val contentAlpha = if (task.isCompleted) 0.45f else 1f
    val checkboxSize = if (compact) 18.dp else 20.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(task) }
            .padding(vertical = if (compact) 8.dp else 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 完成状态勾选框 - 图标切换（顶部对齐，多行标题时保持在第一行）
        Icon(
            imageVector = if (task.isCompleted) {
                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.CheckCircle
            } else {
                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCheckBoxBlank
            },
            contentDescription = if (task.isCompleted) {
                stringResource(R.string.cd_task_completed)
            } else {
                stringResource(R.string.cd_task_pending)
            },
            tint = if (task.isCompleted) accentColor else accentColor.copy(alpha = 0.68f),
            modifier = Modifier
                .padding(top = 3.dp)
                .size(checkboxSize)
                .clickable(
                    interactionSource = null,
                    indication = null
                ) { onToggleComplete(task) }
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // 第一行：标题（完整换行，不截断）
            Text(
                text = task.title,
                style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
            )

            // 第二行：优先级旗帜（高/低才显示）+ 标签 + 备注 + 时间
            val timeMillis = if (task.isCompleted) {
                task.completedAt ?: task.dueDate
            } else {
                task.dueDate
            }
            val showPriority = task.priority != TodoTask.PRIORITY_MEDIUM
            if (showPriority || task.tags.isNotEmpty() || !task.note.isNullOrBlank() || timeMillis != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 优先级小旗（与详情页一致：中优先级不显示，保持安静）
                    if (showPriority) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFlag,
                            contentDescription = null,
                            tint = priorityColor(task.priority).copy(alpha = contentAlpha),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    // 标签 chips（最多 2 个，按标签色着色，可点击）
                    task.tags.take(2).forEach { tagName ->
                        TodoTagChip(
                            tagName = tagName,
                            tag = tagsByName[tagName],
                            onClick = onTagClick?.let { { it(tagName) } }
                        )
                    }

                    // 备注（一行省略，占满标签与时间之间的剩余空间）
                    if (!task.note.isNullOrBlank()) {
                        Text(
                            text = task.note,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f * contentAlpha),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // 时间：进行中显示截止时间，已完成显示完成时间
                    if (timeMillis != null) {
                        val dateColor = when {
                            task.isOverdue -> MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        }
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAccessTime,
                            contentDescription = null,
                            tint = dateColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = formatTodoDate(timeMillis),
                            style = MaterialTheme.typography.labelSmall,
                            color = dateColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * 标签小色块：#标签名，按标签配色（无配色时用系统主题色）；可点击（筛选联动）
 */
@Composable
fun TodoTagChip(
    tagName: String,
    tag: TodoTag?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val tagColor = tag?.colorArgb?.let { Color(it) } ?: MaterialTheme.colorScheme.onPrimaryContainer
    Row(
        modifier = modifier
            .background(
                color = tagColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(4.dp)
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = null,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#$tagName",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.85f
            ),
            color = tagColor,
            maxLines = 1
        )
    }
}

/**
 * 待办日期格式化：今天/明天/昨天，否则按语言显示 M月d日 或 MMM d
 */
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
