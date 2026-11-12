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

package com.shifenmiao.marktodo.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.shifenmiao.marktodo.theme.categoryAccentContainerColor
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineReorder
import com.wanbaohe.core.ui.widget.enhanced.EnhancedDropdownMenu
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassLinearProgressIndicator
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem

/** 列表卡片最多平铺多少条任务，超出部分提示「还有 N 个任务」 */
private const val MAX_CARD_TASKS = 10

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
    themeIndex: Int,
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
    onDuplicateTask: ((TodoTask) -> Unit)? = null,
    onDeleteTask: ((TodoTask) -> Unit)? = null,
) {
    val accentColor = categoryAccentColor(category, themeIndex)
    val accentContainerColor = categoryAccentContainerColor(category, themeIndex)
    val contentColor = com.wanbaohe.core.ui.widget.glass.tintedGlassContentColor(accentContainerColor)
    var menuExpanded by remember { mutableStateOf(false) }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        colors = com.wanbaohe.core.ui.widget.glass.tintedGlassCardColors(
            accentContainerColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(OneBoxDesignSystem.itemSpacing)
        ) {
            // 头部：图标徽章 + 标题 + 进度文本 + 菜单锚点
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { if (!isReorderMode) onCategoryClick(category) },
                        // 排序模式下长按留给拖拽排序；收集箱等虚拟分组没有分组菜单
                        onLongClick = { if (!isReorderMode && showMenu) menuExpanded = true }
                    )
            ) {
                ThemeGroupHeader(
                    category = category,
                    accentColor = accentColor,
                    contentColor = contentColor,
                    showMenu = showMenu,
                    menuExpanded = menuExpanded,
                    onMenuExpandedChange = { menuExpanded = it },
                    onEditCategory = { onEditCategory(category) },
                    onDeleteCategory = { onDeleteCategory(category) },
                    onEnterReorder = onEnterReorder
                )
            }

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
                val rowTasks = visibleTasks.take(MAX_CARD_TASKS)
                val hiddenCount = visibleTasks.size - rowTasks.size
                Column(modifier = Modifier.padding(top = OneBoxDesignSystem.microSpacing)) {
                    rowTasks.forEach { task ->
                        TodoTaskRow(
                            task = task,
                            contentColor = contentColor,
                            tagsByName = tagsByName,
                            onToggleComplete = onToggleComplete,
                            onClick = onTaskClick,
                            onTagClick = onTagClick,
                            onToggleStar = onToggleStar,
                            onDuplicate = onDuplicateTask,
                            onDelete = onDeleteTask
                        )
                    }
                    if (hiddenCount > 0) {
                        // 卡片里最多 10 条，剩下的点进分组看
                        Text(
                            text = stringResource(R.string.more_tasks_indicator, hiddenCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.72f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCategoryClick(category) }
                                .padding(
                                    start = OneBoxDesignSystem.listItemHorizontalPadding,
                                    top = OneBoxDesignSystem.microSpacing,
                                    bottom = OneBoxDesignSystem.microSpacing,
                                ),
                        )
                    }
                }
            }
        }
    }
}

/**
 * 主题分组卡片（网格视图）：长按分组打开分组菜单，长按事项打开事项菜单。
 */
@Composable
fun ThemeGroupGridCard(
    category: TodoCategory,
    themeIndex: Int,
    visibleTasks: List<TodoTask>,
    tagsByName: Map<String, TodoTag>,
    onCategoryClick: (TodoCategory) -> Unit,
    onTaskClick: (TodoTask) -> Unit,
    onToggleComplete: (TodoTask) -> Unit,
    onEditCategory: (TodoCategory) -> Unit,
    onDeleteCategory: (TodoCategory) -> Unit,
    modifier: Modifier = Modifier,
    isReorderMode: Boolean = false,
    showMenu: Boolean = true,
    onEnterReorder: () -> Unit = {},
    maxPreviewTasks: Int = 5,
    onTagClick: ((String) -> Unit)? = null,
    onToggleStar: ((TodoTask) -> Unit)? = null,
    onDuplicateTask: ((TodoTask) -> Unit)? = null,
    onDeleteTask: ((TodoTask) -> Unit)? = null,
) {
    val accentColor = categoryAccentColor(category, themeIndex)
    val accentContainerColor = categoryAccentContainerColor(category, themeIndex)
    val contentColor = com.wanbaohe.core.ui.widget.glass.tintedGlassContentColor(accentContainerColor)
    var menuExpanded by remember { mutableStateOf(false) }
    val previewTasks = remember(visibleTasks, maxPreviewTasks) {
        visibleTasks.take(maxPreviewTasks)
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        colors = com.wanbaohe.core.ui.widget.glass.tintedGlassCardColors(accentContainerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(OneBoxDesignSystem.itemSpacing)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { if (!isReorderMode) onCategoryClick(category) },
                        onLongClick = { if (!isReorderMode && showMenu) menuExpanded = true }
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
            ) {
                ThemeIconBadge(
                    category = category,
                    accentColor = accentColor,
                )
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                // 菜单锚点（长按弹出，锚定在卡片右上角）
                ThemeMenuAnchor(
                    showMenuButton = false,
                    expanded = menuExpanded,
                    onExpandedChange = { menuExpanded = it },
                    onEdit = { onEditCategory(category) },
                    onDelete = { onDeleteCategory(category) },
                    onEnterReorder = onEnterReorder,
                    contentColor = contentColor,
                )
            }

            // 进度文本 + 圆头玻璃进度条（无断口）
            Text(
                text = categoryProgressText(category),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.72f),
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

            // 最多展示 5 条完整标题，剩余任务进入分组查看。
            if (previewTasks.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = OneBoxDesignSystem.compactSpacing)) {
                    previewTasks.forEach { task ->
                        TodoTaskRow(
                            task = task,
                            contentColor = contentColor,
                            tagsByName = tagsByName,
                            onToggleComplete = onToggleComplete,
                            onClick = onTaskClick,
                            onTagClick = onTagClick,
                            onToggleStar = onToggleStar,
                            onDuplicate = onDuplicateTask,
                            onDelete = onDeleteTask,
                            compact = true
                        )
                    }
                    val hiddenCount = visibleTasks.size - previewTasks.size
                    if (hiddenCount > 0) {
                        Text(
                            text = stringResource(R.string.more_tasks_indicator, hiddenCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.72f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCategoryClick(category) }
                                .padding(
                                    start = OneBoxDesignSystem.listItemHorizontalPadding,
                                    top = OneBoxDesignSystem.microSpacing,
                                ),
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
    contentColor: Color,
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
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = categoryProgressText(category),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.72f)
            )
        }

        ThemeMenuAnchor(
            showMenuButton = showMenu,
            expanded = menuExpanded,
            onExpandedChange = onMenuExpandedChange,
            onEdit = onEditCategory,
            onDelete = onDeleteCategory,
            onEnterReorder = onEnterReorder,
            contentColor = contentColor,
        )
    }
}

/** 分组图标直接使用主题色，不再叠加独立底色。 */
@Composable
private fun ThemeIconBadge(
    category: TodoCategory,
    accentColor: Color,
) {
    Icon(
        imageVector = category.icon,
        contentDescription = null,
        tint = accentColor,
        modifier = Modifier.size(24.dp)
    )
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
    onEnterReorder: () -> Unit,
    contentColor: Color,
) {
    Box {
        if (showMenuButton) {
            IconButton(onClick = { onExpandedChange(true) }, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = stringResource(R.string.action_menu),
                    tint = contentColor,
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
