package com.shifenmiao.marktodo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.model.TodoCategory
import com.shifenmiao.marktodo.model.TodoTask
import com.shifenmiao.marktodo.theme.categoryContainerColor
import com.shifenmiao.marktodo.theme.categoryContentColor
import com.shifenmiao.marktodo.theme.categoryHeaderColor
import com.shifenmiao.marktodo.theme.categoryThemeForIndex
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.CheckCircle
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCheckBoxBlank
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAccessTime

/**
 * 极致扁平化的分类卡片组件
 *
 * 设计理念：
 * - 极简扁平设计：最小圆角、无阴影、简洁分隔线
 * - 任务可直接勾选完成，显示备注和开始时间
 * - 高性能：使用 remember 缓存计算值
 * - 支持编辑模式：显示删除按钮
 *
 * @param category 分类数据
 * @param themeIndex 主题色索引，用于循环使用系统主题色
 * @param onCategoryClick 点击分类卡片的回调
 * @param onAddTaskClick 点击添加任务按钮的回调
 * @param onTaskToggleComplete 切换任务完成状态的回调
 * @param modifier 可选的修饰符
 * @param maxPreviewTasks 预览显示的最大任务数（默认3）
 * @param isEditMode 是否处于编辑模式
 * @param onDeleteClick 删除分类的回调
 */
@Composable
fun CategoryCard(
    category: TodoCategory,
    themeIndex: Int,
    onCategoryClick: (TodoCategory) -> Unit,
    onAddTaskClick: (TodoCategory) -> Unit,
    onTaskToggleComplete: (TodoTask) -> Unit,
    modifier: Modifier = Modifier,
    maxPreviewTasks: Int = 3,
    isEditMode: Boolean = false,
    onDeleteClick: ((TodoCategory) -> Unit)? = null,
    onEditClick: ((TodoCategory) -> Unit)? = null
) {
    val theme = categoryThemeForIndex(themeIndex)
    val containerColor = categoryContainerColor(theme)
    val headerColor = categoryHeaderColor(theme)
    val contentColor = categoryContentColor(theme)

    // 预切片任务列表
    val previewTasks = remember(category.tasks, maxPreviewTasks) {
        category.tasks.take(maxPreviewTasks)
    }
    val hasMoreTasks = remember(category.tasks.size, maxPreviewTasks) {
        category.tasks.size > maxPreviewTasks
    }
    val remainingCount = remember(category.tasks.size, maxPreviewTasks) {
        category.tasks.size - maxPreviewTasks
    }

    // 极致扁平化：4dp 小圆角，无阴影
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppTheme.shapes.getMediumShape(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .clickable {
                    if (isEditMode) {
                        onEditClick?.invoke(category)
                    } else {
                        onCategoryClick(category)
                    }
                }
        ) {
            // Header - 扁平化设计
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
            ) {
                CategoryHeaderFlat(
                    category = category,
                    completedCount = category.completedCount,
                    totalCount = category.totalCount,
                    contentColor = contentColor,
                    isEditMode = isEditMode,
                    onDeleteClick = onDeleteClick
                )
            }

            // Tasks preview - 支持勾选完成、显示备注和开始时间
            if (category.tasks.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    previewTasks.forEach { task ->
                        TaskItemReadOnly(
                            task = task,
                            accentColor = contentColor,
                            onToggleComplete = onTaskToggleComplete,
                            tagColor = headerColor,
                            tagTextColor = contentColor
                        )
                    }

                    if (hasMoreTasks) {
                        Text(
                            text = stringResource(
                                R.string.more_tasks_indicator,
                                remainingCount
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            } else {
                CategoryEmptyState()
            }

            // 简洁分隔线
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )

            // 添加任务按钮 - 扁平设计
            AddTaskButtonFlat(
                accentColor = contentColor,
                onClick = { onAddTaskClick(category) }
            )
        }
    }
}

/**
 * 扁平化分类头部组件
 */
@Composable
private fun CategoryHeaderFlat(
    category: TodoCategory,
    completedCount: Int,
    totalCount: Int,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false,
    onDeleteClick: ((TodoCategory) -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：图标和标题
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer( modifier = Modifier.width(4.dp))
        Text(
            modifier = Modifier
                .basicMarquee()
                .weight(1f),
            text = category.title,
            style = MaterialTheme.typography.titleMedium,
            color = contentColor,
            maxLines = 1
        )
        Spacer( modifier = Modifier.width(4.dp))
        // 编辑模式：显示删除按钮
        if (isEditMode && onDeleteClick != null) {
            IconButton(
                onClick = {
                    onDeleteClick(category)
                },
                modifier = Modifier
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            // 右侧：任务统计
            Text(
                text = "$completedCount/$totalCount",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}

/**
 * 任务项 - 支持点击勾选完成、显示备注、开始时间和标签
 * 极致扁平化设计，标签使用主题色背景
 */
@Composable
private fun TaskItemReadOnly(
    task: TodoTask,
    accentColor: androidx.compose.ui.graphics.Color,
    onToggleComplete: (TodoTask) -> Unit,
    modifier: Modifier = Modifier,
    tagColor: androidx.compose.ui.graphics.Color,
    tagTextColor: androidx.compose.ui.graphics.Color
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.Top)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 完成状态勾选框 - 直接使用图标切换
            Icon(
                imageVector = if (task.isCompleted) {
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.CheckCircle
                } else {
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCheckBoxBlank
                },
                contentDescription = if (task.isCompleted) stringResource(R.string.cd_task_completed) else stringResource(R.string.cd_task_pending),
                tint = accentColor.copy(0.68f),
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onToggleComplete(task) }
            )

            // 任务标题
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (task.isCompleted) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f)
            )

            // 星标指示器
            if (task.isStarred) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStar,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // 备注和附加信息 - 更小的字号，极致扁平化
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, top = 2.dp), // 对齐到勾选框后，图标20dp，间距8dp
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 第一行：备注（如果有）
            task.note?.let { note ->
                Text(
                    text = note,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.85f
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                )
            }

            // 第二行：标签 + 时间
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 显示开始时间 - 时间在后
                val dateText = formatDateShort(task.startDate)
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
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    maxLines = 1
                )
                // 显示标签（如果有）- 标签在前，使用反向颜色
                if (task.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.size(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        task.tags.take(2).forEach { tag ->
                            // 标签色块 - 使用系统主题色
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = tagColor,
                                        shape = RoundedCornerShape(3.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.8f
                                    ),
                                    color = tagTextColor,
                                    maxLines = 1
                                )
                            }
                        }
                        // 如果标签超过2个，显示数量
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
                }
            }
        }
    }
}

/**
 * 格式化日期为简短格式
 */
@Composable
private fun formatDateShort(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val days = diff / (1000 * 60 * 60 * 24)

    return when {
        days == 0L -> stringResource(R.string.date_today)
        days == 1L -> stringResource(R.string.date_yesterday)
        days < 7 -> stringResource(R.string.date_days_ago, days.toInt())
        else -> {
            val date = java.text.SimpleDateFormat("MM/dd", java.util.Locale.getDefault())
                .format(java.util.Date(timestamp))
            date
        }
    }
}


/**
 * 空状态 - 更紧凑的设计
 */
@Composable
private fun CategoryEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.empty_tasks_title),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

/**
 * 扁平化添加任务按钮
 */
@Composable
private fun AddTaskButtonFlat(
    accentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = accentColor.copy(0.68f)),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Icon(
            painter = painterResource(com.shifenmiao.core.R.drawable.add_task),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.add_task_hint),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
