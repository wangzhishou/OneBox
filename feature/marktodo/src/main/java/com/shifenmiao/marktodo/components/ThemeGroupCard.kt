package com.shifenmiao.marktodo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.model.TodoCategory
import com.shifenmiao.marktodo.model.TodoTag
import com.shifenmiao.marktodo.model.TodoTask
import com.shifenmiao.marktodo.theme.categoryAccentColor
import com.shifenmiao.marktodo.theme.categoryCardTintColor
import com.shifenmiao.marktodo.theme.categoryAccentContainerColor
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineReorder
import com.wanbaohe.core.ui.widget.enhanced.EnhancedDropdownMenu
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassLinearProgressIndicator
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem

/**
 * 主题分组卡片（列表视图）—— 原型设计稿「进行中/已完成」页：
 * 图标徽章 + 标题 + 进度（n/m · 百分比/已完成）+ 圆头玻璃进度条 + 任务行列表。
 *
 * 卡片按主题色系染色（背景浅染 + 徽章/进度条用强调色）。
 * 交互：点击进主题详情；右上角或长按打开「编辑/删除主题」菜单。
 *
 * @param visibleTasks 当前查询过滤后的可见任务（头部进度仍按全部任务统计）
 * @param showMenu 是否显示「…」菜单（拖拽排序时隐藏）
 */
@Composable
fun ThemeGroupCard(
    category: TodoCategory,
    visibleTasks: List<TodoTask>,
    tagsByName: Map<String, TodoTag>,
    showMenu: Boolean,
    onCategoryClick: (TodoCategory) -> Unit,
    onTaskClick: (TodoTask) -> Unit,
    onToggleComplete: (TodoTask) -> Unit,
    onEditCategory: (TodoCategory) -> Unit,
    onDeleteCategory: (TodoCategory) -> Unit,
    modifier: Modifier = Modifier,
    isReorderMode: Boolean = false,
    onEnterReorder: () -> Unit = {},
    onTagClick: ((String) -> Unit)? = null,
    onToggleStar: ((TodoTask) -> Unit)? = null,
    onDeleteTask: ((TodoTask) -> Unit)? = null,
) {
    val accentColor = categoryAccentColor(category)
    val accentContainerColor = categoryAccentContainerColor(category)
    var menuExpanded by remember { mutableStateOf(false) }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = categoryCardTintColor(category)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { if (!isReorderMode) onCategoryClick(category) },
                    // 排序模式下长按留给拖拽排序
                    onLongClick = { if (!isReorderMode) menuExpanded = true }
                )
                .padding(OneBoxDesignSystem.itemSpacing)
        ) {
            // 头部：图标徽章 + 标题 + 进度文本 + 菜单锚点
            ThemeGroupHeader(
                category = category,
                accentColor = accentColor,
                accentContainerColor = accentContainerColor,
                showMenu = showMenu,
                menuExpanded = menuExpanded,
                onMenuExpandedChange = { menuExpanded = it },
                onEditCategory = { onEditCategory(category) },
                onDeleteCategory = { onDeleteCategory(category) },
                onEnterReorder = onEnterReorder
            )

            // 圆头玻璃进度条（无断口）
            GlassLinearProgressIndicator(
                progress = { category.progressPercentage / 100f },
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = OneBoxDesignSystem.compactSpacing)
                    .height(6.dp)
            )

            // 任务行
            if (visibleTasks.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = OneBoxDesignSystem.microSpacing)) {
                    visibleTasks.forEach { task ->
                        TodoTaskRow(
                            task = task,
                            accentColor = accentColor,
                            tagsByName = tagsByName,
                            onToggleComplete = onToggleComplete,
                            onClick = onTaskClick,
                            onTagClick = onTagClick,
                            onToggleStar = onToggleStar,
                            onDelete = onDeleteTask
                        )
                    }
                }
            }
        }
    }
}

/**
 * 主题分组卡片（网格视图）—— 原型设计稿 Grid 视图：
 * 图标徽章 + 标题 + 进度 + 前 2 条任务（两行布局：标题 / 标签·备注·时间）。
 */
@Composable
fun ThemeGroupGridCard(
    category: TodoCategory,
    visibleTasks: List<TodoTask>,
    tagsByName: Map<String, TodoTag>,
    onCategoryClick: (TodoCategory) -> Unit,
    onTaskClick: (TodoTask) -> Unit,
    onToggleComplete: (TodoTask) -> Unit,
    onEditCategory: (TodoCategory) -> Unit,
    onDeleteCategory: (TodoCategory) -> Unit,
    modifier: Modifier = Modifier,
    isReorderMode: Boolean = false,
    onEnterReorder: () -> Unit = {},
    maxPreviewTasks: Int = 2,
    onTagClick: ((String) -> Unit)? = null,
    onToggleStar: ((TodoTask) -> Unit)? = null,
    onDeleteTask: ((TodoTask) -> Unit)? = null,
) {
    val accentColor = categoryAccentColor(category)
    val accentContainerColor = categoryAccentContainerColor(category)
    var menuExpanded by remember { mutableStateOf(false) }
    val previewTasks = remember(visibleTasks, maxPreviewTasks) {
        visibleTasks.take(maxPreviewTasks)
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = categoryCardTintColor(category)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { if (!isReorderMode) onCategoryClick(category) },
                    onLongClick = { if (!isReorderMode) menuExpanded = true }
                )
                .padding(OneBoxDesignSystem.itemSpacing)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
            ) {
                ThemeIconBadge(
                    category = category,
                    accentColor = accentColor,
                    accentContainerColor = accentContainerColor
                )
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                // 菜单锚点（长按弹出，锚定在卡片右上角）
                ThemeMenuAnchor(
                    showMenuButton = !isReorderMode,
                    expanded = menuExpanded,
                    onExpandedChange = { menuExpanded = it },
                    onEdit = { onEditCategory(category) },
                    onDelete = { onDeleteCategory(category) },
                    onEnterReorder = onEnterReorder
                )
            }

            // 进度文本 + 圆头玻璃进度条（无断口）
            Text(
                text = categoryProgressText(category),
                style = MaterialTheme.typography.labelSmall,
                color = accentColor.copy(alpha = 0.85f),
                modifier = Modifier.padding(top = OneBoxDesignSystem.compactSpacing)
            )
            GlassLinearProgressIndicator(
                progress = { category.progressPercentage / 100f },
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = OneBoxDesignSystem.microSpacing)
                    .height(6.dp)
            )

            // 任务预览
            if (previewTasks.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = OneBoxDesignSystem.compactSpacing)) {
                    previewTasks.forEach { task ->
                        TodoTaskRow(
                            task = task,
                            accentColor = accentColor,
                            tagsByName = tagsByName,
                            onToggleComplete = onToggleComplete,
                            onClick = onTaskClick,
                            onTagClick = onTagClick,
                            onToggleStar = onToggleStar,
                            onDelete = onDeleteTask,
                            compact = true
                        )
                    }
                }
            }
        }
    }
}

/**
 * 头部行：图标徽章 + 标题 + 进度文本 + 菜单锚点（⋯ 按钮 / 长按菜单都锚定在此处）
 */
@Composable
private fun ThemeGroupHeader(
    category: TodoCategory,
    accentColor: Color,
    accentContainerColor: Color,
    showMenu: Boolean,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onEditCategory: () -> Unit,
    onDeleteCategory: () -> Unit,
    onEnterReorder: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)
    ) {
        ThemeIconBadge(
            category = category,
            accentColor = accentColor,
            accentContainerColor = accentContainerColor
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = categoryProgressText(category),
                style = MaterialTheme.typography.labelSmall,
                color = accentColor.copy(alpha = 0.85f)
            )
        }

        ThemeMenuAnchor(
            showMenuButton = showMenu,
            expanded = menuExpanded,
            onExpandedChange = onMenuExpandedChange,
            onEdit = onEditCategory,
            onDelete = onDeleteCategory,
            onEnterReorder = onEnterReorder
        )
    }
}

/**
 * 主题图标徽章：圆角方块 + 主题色图标
 */
@Composable
private fun ThemeIconBadge(
    category: TodoCategory,
    accentColor: Color,
    accentContainerColor: Color
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(OneBoxDesignSystem.smallRadius))
            .background(accentContainerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * 主题操作菜单锚点：编辑主题 / 删除主题 / 排序（进入拖拽排序模式）。
 * 下拉菜单锚定在本组合位置（卡片头部右侧），⋯ 按钮可选显示；长按卡片也会弹出。
 */
@Composable
private fun ThemeMenuAnchor(
    showMenuButton: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onEnterReorder: () -> Unit
) {
    Box {
        if (showMenuButton) {
            IconButton(onClick = { onExpandedChange(true) }, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = stringResource(R.string.action_menu),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        EnhancedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_edit)) },
                onClick = {
                    onExpandedChange(false)
                    onEdit()
                },
                leadingIcon = {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_sort_categories)) },
                onClick = {
                    onExpandedChange(false)
                    onEnterReorder()
                },
                leadingIcon = {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineReorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                },
                onClick = {
                    onExpandedChange(false)
                    onDelete()
                },
                leadingIcon = {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

/**
 * 进度文本：「2 / 5 · 40%」，全部完成时「5 / 5 · 已完成」
 */
@Composable
private fun categoryProgressText(category: TodoCategory): String {
    val countText = stringResource(R.string.tasks_count, category.completedCount, category.totalCount)
    return if (category.isAllCompleted) {
        "$countText · ${stringResource(R.string.task_completed)}"
    } else {
        "$countText · ${stringResource(R.string.task_progress_percent, category.progressPercentage)}"
    }
}
