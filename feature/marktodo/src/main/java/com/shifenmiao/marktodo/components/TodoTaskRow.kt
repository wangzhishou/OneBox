package com.shifenmiao.marktodo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
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
    contentColor: Color,
    tagsByName: Map<String, TodoTag>,
    onToggleComplete: (TodoTask) -> Unit,
    onClick: (TodoTask) -> Unit,
    modifier: Modifier = Modifier,
    onTagClick: ((String) -> Unit)? = null,
    compact: Boolean = false,
    onToggleStar: ((TodoTask) -> Unit)? = null,
    onDuplicate: ((TodoTask) -> Unit)? = null,
    onDelete: ((TodoTask) -> Unit)? = null,
) {
    val contentAlpha = if (task.isCompleted) 0.45f else 1f
    var menuExpanded by remember(task.id) { mutableStateOf(false) }
    val hasOverflowActions = onDelete != null && onToggleStar != null

    // 勾选框和右侧「更多」都要跟标题第一行的**字形**居中对齐：
    // 标题行高是字号的两倍，字形在行盒里居中，而 CJK 墨迹又比字体盒中心高约 0.25em，
    // 所以按 0.75 倍行高（= 0.5 行高 - 0.25 字号）算出来的中心，才和肉眼看到的第一行一致。
    // 直接写死 2dp 在默认字号下刚好，一旦系统字号放大（1.4 倍时差 4~5dp）就明显偏上。
    val titleStyle = if (compact) {
        MaterialTheme.typography.bodyMedium
    } else {
        MaterialTheme.typography.bodyLarge
    }
    val density = LocalDensity.current
    val firstLineCenter = with(density) {
        titleStyle.lineHeight.toDp() / 2 - titleStyle.fontSize.toDp() * 0.25f
    }
    val metaStyle = metaLabelStyle()
    val metaInkCenter = with(density) {
        metaStyle.lineHeight.toDp() / 2 - metaStyle.fontSize.toDp() * 0.25f
    }
    // 勾选框和「更多」是辅助控件，比正文淡一档（系数见 taskCheckAlpha / taskMoreAlpha）
    val checkTint = contentColor.copy(alpha = taskCheckAlpha(task.isCompleted))
    val moreTint = contentColor.copy(alpha = taskMoreAlpha(task.isCompleted))

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
        verticalAlignment = Alignment.Top,
    ) {
        TaskCompletionButton(
            task = task,
            onToggle = onToggleComplete,
            tint = checkTint,
            modifier = Modifier.padding(
                top = (firstLineCenter - CompletionCheckSize / 2).coerceAtLeast(0.dp),
            ),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = task.title,
                    style = titleStyle,
                    color = contentColor.copy(alpha = contentAlpha),
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    modifier = Modifier.weight(1f),
                )
                // 完成时间贴在标题行右侧：不额外占一行，勾选/取消时行高不变，列表不会跳
                val completionMillis = task.completedAt.takeIf {
                    task.isCompleted && task.dueDate == null && !compact
                }
                if (completionMillis != null) {
                    Text(
                        text = formatTodoDate(completionMillis),
                        style = metaStyle,
                        color = contentColor.copy(alpha = 0.65f),
                        maxLines = 1,
                        // 小字和标题墨迹对齐，别贴着标题行的顶边飘在上面
                        modifier = Modifier.padding(
                            top = (firstLineCenter - metaInkCenter).coerceAtLeast(0.dp),
                        ),
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
                            color = contentColor.copy(alpha = 0.55f * contentAlpha),
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
                            contentColor.copy(alpha = 0.7f)
                        }
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAccessTime,
                            contentDescription = null,
                            tint = dateColor,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = formatTodoDate(timeMillis),
                            style = metaLabelStyle(),
                            color = dateColor,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
        if (onDelete != null && onToggleStar != null && onDuplicate != null) {
            TaskOverflowMenu(
                task = task,
                onEdit = onClick,
                onToggleStar = onToggleStar,
                onDuplicate = onDuplicate,
                onDelete = onDelete,
                expanded = menuExpanded,
                onExpandedChange = { menuExpanded = it },
                showButton = !compact,
                contentColor = moreTint,
                // 48dp 按钮整体上移，让 19dp 的图标中心落到标题第一行上；
                // 触控范围不变，偏移量正好被行内 8dp 的上下留白吃掉，不会压到相邻行。
                // 紧凑卡片不显示按钮（长按出菜单），别动它的菜单锚点
                modifier = if (compact) {
                    Modifier
                } else {
                    Modifier.offset(y = firstLineCenter - TaskOverflowButtonSize / 2)
                },
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
                shape = OneBoxDesignSystem.chipShape,
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 6.dp, vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "#$tagName",
            style = metaLabelStyle(),
            color = tagColor,
            maxLines = 1,
        )
    }
}

/**
 * 任务行元信息（标签、日期、完成时间）共用的小号字：比 labelSmall 再小一档，
 * 免得日期和标题抢视线；标签与时间必须走同一份，不然两种字号并排会一高一矮。
 */
@Composable
internal fun metaLabelStyle(): TextStyle =
    MaterialTheme.typography.labelSmall.copy(
        fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.85f,
    )

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
