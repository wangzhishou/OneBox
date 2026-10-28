package com.shifenmiao.marktodo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.model.TaskWithCategory
import com.shifenmiao.marktodo.model.TodoTag
import com.shifenmiao.marktodo.model.TodoTask
import com.shifenmiao.marktodo.theme.priorityColor
import com.t8rin.imagetoolbox.core.resources.icons.CheckCircle
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.Star
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAccessTime
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCheckBoxBlank
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFlag
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStar
import com.wanbaohe.core.ui.widget.enhanced.EnhancedDropdownMenu
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.other.RevealDirection
import com.wanbaohe.core.ui.widget.other.RevealValue
import com.wanbaohe.core.ui.widget.other.SwipeToReveal
import com.wanbaohe.core.ui.widget.other.rememberRevealState
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

@Composable
fun TaskListContent(
    tasks: List<TaskWithCategory>,
    tagsByName: Map<String, TodoTag>,
    onTaskClick: (TodoTask) -> Unit,
    onToggleComplete: (TodoTask) -> Unit,
    onToggleStar: (TodoTask) -> Unit,
    onDelete: (TodoTask) -> Unit,
    modifier: Modifier = Modifier,
    onTagClick: ((String) -> Unit)? = null,
    showCategory: Boolean = true,
    isGrid: Boolean = false,
    enableSwipeDelete: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = OneBoxDesignSystem.screenPadding,
        vertical = OneBoxDesignSystem.compactSpacing,
    ),
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(if (isGrid) 2 else 1),
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
    ) {
        items(items = tasks, key = { it.task.id }, contentType = { "task_list_item" }) { item ->
            if (enableSwipeDelete) {
                SwipeableTaskListItem(
                    item = item,
                    tagsByName = tagsByName,
                    onTaskClick = onTaskClick,
                    onToggleComplete = onToggleComplete,
                    onToggleStar = onToggleStar,
                    onDelete = onDelete,
                    onTagClick = onTagClick,
                    showCategory = showCategory,
                    compact = isGrid,
                )
            } else {
                TaskListItem(
                    item = item,
                    tagsByName = tagsByName,
                    onClick = onTaskClick,
                    onToggleComplete = onToggleComplete,
                    onToggleStar = onToggleStar,
                    onDelete = onDelete,
                    onTagClick = onTagClick,
                    showCategory = showCategory,
                    compact = isGrid,
                )
            }
        }
    }
}

@Composable
fun TaskListItem(
    item: TaskWithCategory,
    tagsByName: Map<String, TodoTag>,
    onClick: (TodoTask) -> Unit,
    onToggleComplete: (TodoTask) -> Unit,
    onToggleStar: (TodoTask) -> Unit,
    onDelete: (TodoTask) -> Unit,
    modifier: Modifier = Modifier,
    onTagClick: ((String) -> Unit)? = null,
    showCategory: Boolean = true,
    compact: Boolean = false,
) {
    val task = item.task
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = { onClick(task) },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TaskCompletionButton(task = task, onToggle = onToggleComplete)
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    modifier = Modifier.weight(1f),
                )
                if (!compact) {
                    TaskStarButton(task = task, onToggle = onToggleStar)
                }
                TaskOverflowMenu(
                    task = task,
                    onEdit = onClick,
                    onToggleStar = onToggleStar,
                    onDelete = onDelete,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                TaskMetadata(
                    item = item,
                    tagsByName = tagsByName,
                    onTagClick = onTagClick,
                    showCategory = showCategory,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 48.dp, bottom = OneBoxDesignSystem.compactSpacing),
                )
                if (compact) {
                    TaskStarButton(task = task, onToggle = onToggleStar)
                }
            }
        }
    }
}

@Composable
private fun TaskMetadata(
    item: TaskWithCategory,
    tagsByName: Map<String, TodoTag>,
    onTagClick: ((String) -> Unit)?,
    showCategory: Boolean,
    modifier: Modifier = Modifier,
) {
    val task = item.task
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        if (showCategory) {
            Text(
                text = item.category?.title ?: stringResource(R.string.p1_inbox),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAccessTime,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = stringResource(R.string.p1_created_at, formatTaskDate(task.createdAt)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        task.dueDate?.let { dueDate ->
            Text(
                text = buildString {
                    append(formatTodoDate(dueDate))
                    if (task.hasDueTime) {
                        append(' ')
                        append(formatTaskTime(dueDate))
                    }
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (task.isOverdue) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
        if (!task.note.isNullOrBlank()) {
            Text(
                text = task.note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (task.tags.isNotEmpty() || task.priority != TodoTask.PRIORITY_MEDIUM) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
                verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
            ) {
                if (task.priority != TodoTask.PRIORITY_MEDIUM) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFlag,
                        contentDescription = stringResource(
                            if (task.priority == TodoTask.PRIORITY_HIGH) {
                                R.string.priority_high
                            } else {
                                R.string.priority_low
                            }
                        ),
                        tint = priorityColor(task.priority),
                        modifier = Modifier.size(14.dp),
                    )
                }
                task.tags.forEach { tagName ->
                    TodoTagChip(
                        tagName = tagName,
                        tag = tagsByName[tagName],
                        onClick = onTagClick?.let { callback -> { callback(tagName) } },
                    )
                }
            }
        }
    }
}

@Composable
fun TaskCompletionButton(
    task: TodoTask,
    onToggle: (TodoTask) -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = { onToggle(task) },
        modifier = modifier.size(48.dp),
    ) {
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
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun TaskStarButton(
    task: TodoTask,
    onToggle: (TodoTask) -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = { onToggle(task) },
        modifier = modifier.size(48.dp),
    ) {
        Icon(
            imageVector = if (task.isStarred) {
                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Star
            } else {
                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStar
            },
            contentDescription = stringResource(
                if (task.isStarred) R.string.p1_remove_focus else R.string.p1_add_focus
            ),
            tint = if (task.isStarred) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(19.dp),
        )
    }
}

@Composable
fun TaskOverflowMenu(
    task: TodoTask,
    onEdit: (TodoTask) -> Unit,
    onToggleStar: (TodoTask) -> Unit,
    onDelete: (TodoTask) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember(task.id) { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = stringResource(R.string.action_menu),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(19.dp),
            )
        }
        EnhancedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_edit)) },
                leadingIcon = {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                        contentDescription = null,
                    )
                },
                onClick = {
                    expanded = false
                    onEdit(task)
                },
            )
            DropdownMenuItem(
                text = {
                    Text(
                        stringResource(
                            if (task.isStarred) R.string.p1_remove_focus else R.string.p1_add_focus
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStar,
                        contentDescription = null,
                    )
                },
                onClick = {
                    expanded = false
                    onToggleStar(task)
                },
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                    )
                },
                onClick = {
                    expanded = false
                    onDelete(task)
                },
            )
        }
    }
}

@Composable
fun TaskListEmptyState(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SwipeableTaskListItem(
    item: TaskWithCategory,
    tagsByName: Map<String, TodoTag>,
    onTaskClick: (TodoTask) -> Unit,
    onToggleComplete: (TodoTask) -> Unit,
    onToggleStar: (TodoTask) -> Unit,
    onDelete: (TodoTask) -> Unit,
    onTagClick: ((String) -> Unit)?,
    showCategory: Boolean,
    compact: Boolean,
) {
    val scope = rememberCoroutineScope()
    val revealState = rememberRevealState()
    SwipeToReveal(
        directions = setOf(RevealDirection.EndToStart),
        maxRevealDp = 72.dp,
        state = revealState,
        alphaTransformEnabled = true,
        revealedContentEnd = {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(start = OneBoxDesignSystem.compactSpacing)
                    .width(72.dp)
                    .clip(RoundedCornerShape(OneBoxDesignSystem.mediumRadius))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .clickable {
                        scope.launch { revealState.animateTo(RevealValue.Default) }
                        onDelete(item.task)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
        swipeableContent = {
            TaskListItem(
                item = item,
                tagsByName = tagsByName,
                onClick = onTaskClick,
                onToggleComplete = onToggleComplete,
                onToggleStar = onToggleStar,
                onDelete = onDelete,
                onTagClick = onTagClick,
                showCategory = showCategory,
                compact = compact,
            )
        },
    )
}

private fun formatTaskDate(timestamp: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(timestamp))

private fun formatTaskTime(timestamp: Long): String =
    DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(timestamp))
