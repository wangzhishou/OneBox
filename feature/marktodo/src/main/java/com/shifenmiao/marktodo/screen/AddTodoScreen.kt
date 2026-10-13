package com.shifenmiao.marktodo.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.picker.ChineseDatePickerDialog
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.components.TaskDateField
import com.shifenmiao.marktodo.model.AddTodoUiEvent
import com.shifenmiao.marktodo.model.AddTodoUiState
import com.shifenmiao.marktodo.model.TodoCategory
import com.shifenmiao.marktodo.model.TodoTag
import com.shifenmiao.marktodo.model.TodoTask
import com.shifenmiao.marktodo.screenLogic.AddTodoComponent
import com.shifenmiao.marktodo.theme.CategoryColorPalette
import com.shifenmiao.marktodo.theme.categoryPaletteWithThemeColors
import com.shifenmiao.marktodo.theme.categoryAccentColor
import com.shifenmiao.marktodo.theme.priorityColor
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAccessTime
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalendar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineEventAvailable
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFlag
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolder
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLabel
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSingleEdit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStickyNote
import com.t8rin.imagetoolbox.core.ui.widget.color_picker.ColorSelectionRow
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedChip
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassFilterChip
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxBottomActionBar
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDangerButton
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OnePrimaryButton
import com.t8rin.imagetoolbox.core.ui.widget.system.OneSecondaryButton
import java.util.Calendar

/**
 * 新增/编辑待办页 —— 原型设计稿：
 * 分组玻璃表单（标题 / 备注 / 截止时间 / 优先级 / 标签 / 所属主题）+ 底部保存（编辑模式含删除）。
 */
@Composable
fun AddTodoScreen(
    addTodoComponent: AddTodoComponent,
    onGoBack: () -> Unit
) {
    val uiState by addTodoComponent.uiState.collectAsState()
    var showAddTagDialog by remember { mutableStateOf(false) }

    BaseScreen(
        title = {
            Text(
                text = stringResource(
                    if (uiState.isEditMode) R.string.dialog_edit_task_title
                    else R.string.dialog_add_task_title
                )
            )
        },
        actions = {
            IconButton(
                onClick = { addTodoComponent.handleEvent(AddTodoUiEvent.SubmitTask) }
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                    contentDescription = stringResource(R.string.action_save)
                )
            }
        },
        foreground = {
            if (uiState.isEditMode) {
                OneBoxBottomActionBar(
                    primaryText = stringResource(R.string.action_save),
                    onPrimaryClick = { addTodoComponent.handleEvent(AddTodoUiEvent.SubmitTask) },
                    dangerText = stringResource(R.string.action_delete),
                    onDangerClick = { addTodoComponent.handleEvent(AddTodoUiEvent.DeleteTask) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                )
            }
        },
        onGoBack = onGoBack,
        isShowDefaultActions = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = OneBoxDesignSystem.screenPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing)
        ) {
            // 待办标题
            TodoFormSection(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSingleEdit,
                label = stringResource(R.string.task_title)
            ) {
                OneBoxOutlinedTextField(
                    value = uiState.taskTitle,
                    onValueChange = {
                        addTodoComponent.handleEvent(AddTodoUiEvent.UpdateTaskTitle(it))
                    },
                    singleLine = true,
                    placeholder = { Text(text = stringResource(R.string.dialog_add_task_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.hasTitleError,
                    supportingText = if (uiState.hasTitleError) {
                        {
                            Text(
                                text = stringResource(R.string.validation_task_title_required),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else null,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Next)
                )
            }

            // 备注（200 字上限 + 字数统计）
            TodoFormSection(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStickyNote,
                label = stringResource(R.string.task_note)
            ) {
                OneBoxOutlinedTextField(
                    value = uiState.taskNote,
                    onValueChange = {
                        addTodoComponent.handleEvent(AddTodoUiEvent.UpdateTaskNote(it))
                    },
                    placeholder = { Text(text = stringResource(R.string.dialog_add_task_note_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Next)
                )
                // 字数统计，达上限时标红提示
                val noteAtMax = uiState.noteAtMaxLength
                Text(
                    text = if (noteAtMax) {
                        stringResource(
                            R.string.note_char_count_at_limit,
                            uiState.taskNote.length,
                            AddTodoUiState.NOTE_MAX_LENGTH
                        )
                    } else {
                        stringResource(
                            R.string.note_char_count,
                            uiState.taskNote.length,
                            AddTodoUiState.NOTE_MAX_LENGTH
                        )
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (noteAtMax) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.align(Alignment.End)
                )
            }

            // 截止时间
            TodoFormSection(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAccessTime,
                label = stringResource(R.string.field_due_time)
            ) {
                TaskDateField(
                    dueDateMillis = uiState.dueDateMillis,
                    onClearDate = { addTodoComponent.handleEvent(AddTodoUiEvent.UpdateDueDate(null)) },
                    onSelectDate = { addTodoComponent.handleEvent(AddTodoUiEvent.ToggleDatePicker(true)) },
                    placeholder = { Text(text = stringResource(R.string.select_due_time_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 日程联动（仅编辑模式：承接原详情页的「生成日程 / 在日历查看」）
            if (uiState.isEditMode) {
                TodoFormSection(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar,
                    label = stringResource(R.string.schedule_section_title)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
                        EnhancedChip(
                            selected = false,
                            onClick = {
                                if (!uiState.isCreatingSchedule) {
                                    addTodoComponent.handleEvent(AddTodoUiEvent.CreateLinkedSchedule)
                                }
                            },
                            selectedColor = Color.Unspecified,
                            modifier = Modifier
                                .weight(1f)
                                .alpha(if (uiState.isCreatingSchedule) 0.5f else 1f),
                            label = {
                                ScheduleActionLabel(
                                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineEventAvailable,
                                    text = stringResource(
                                        if (uiState.isCreatingSchedule) R.string.schedule_creating
                                        else R.string.action_create_schedule
                                    )
                                )
                            }
                        )
                        EnhancedChip(
                            selected = false,
                            onClick = {
                                addTodoComponent.handleEvent(AddTodoUiEvent.OpenScheduleHub)
                            },
                            selectedColor = Color.Unspecified,
                            modifier = Modifier.weight(1f),
                            label = {
                                ScheduleActionLabel(
                                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar,
                                    text = stringResource(R.string.action_open_schedule)
                                )
                            }
                        )
                    }
                }
            }

            // 优先级（高/中/低 三色 chips）
            TodoFormSection(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFlag,
                label = stringResource(R.string.field_priority)
            ) {
                PrioritySelector(
                    priority = uiState.priority,
                    onSelect = {
                        addTodoComponent.handleEvent(AddTodoUiEvent.SelectPriority(it))
                    }
                )
            }

            // 标签（多选，标签表驱动 + 编辑时保留任务原标签 + 新建自定义标签）
            TodoFormSection(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLabel,
                label = stringResource(R.string.task_tags)
            ) {
                TagSelector(
                    tags = uiState.tags,
                    selectedTagNames = uiState.selectedTagNames,
                    onToggle = {
                        addTodoComponent.handleEvent(AddTodoUiEvent.ToggleTag(it))
                    },
                    onAddTag = { showAddTagDialog = true }
                )
            }

            // 所属主题（单选 + 新建主题入口）
            TodoFormSection(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFolder,
                label = stringResource(R.string.task_category)
            ) {
                CategorySelector(
                    categories = uiState.categories,
                    selectedCategory = uiState.selectedCategory,
                    onSelect = {
                        addTodoComponent.handleEvent(AddTodoUiEvent.SelectCategory(it))
                    },
                    onAddCategory = {
                        addTodoComponent.handleEvent(AddTodoUiEvent.AddCategoryClicked)
                    }
                )
            }

            // 新增模式：底部大保存按钮（编辑模式用 OneBoxBottomActionBar）
            if (!uiState.isEditMode) {
                OnePrimaryButton(
                    text = stringResource(R.string.action_save),
                    onClick = { addTodoComponent.handleEvent(AddTodoUiEvent.SubmitTask) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 底部留白（编辑模式给 bottom bar 让位）
            SpacerForBottomBar(uiState.isEditMode)
        }
    }

    // 日期选择
    if (uiState.showDatePicker) {
        TaskDatePickerDialog(
            initialDateMillis = uiState.dueDateMillis,
            onDismiss = { addTodoComponent.handleEvent(AddTodoUiEvent.ToggleDatePicker(false)) },
            onDateSelected = { millis ->
                addTodoComponent.handleEvent(AddTodoUiEvent.UpdateDueDate(millis))
            }
        )
    }

    // 删除确认
    if (uiState.showDeleteConfirm) {
        AlertDialog(
            containerColor = AppTheme.colors.getContainerSurfaceColor(),
            onDismissRequest = { addTodoComponent.handleEvent(AddTodoUiEvent.DismissDeleteConfirm) },
            title = { Text(stringResource(R.string.action_delete)) },
            text = { Text(stringResource(R.string.dialog_delete_task_message, uiState.taskTitle)) },
            confirmButton = {
                OneBoxDangerButton(
                    text = stringResource(R.string.action_delete),
                    onClick = { addTodoComponent.handleEvent(AddTodoUiEvent.ConfirmDeleteTask) }
                )
            },
            dismissButton = {
                OneSecondaryButton(
                    text = stringResource(R.string.action_cancel),
                    onClick = { addTodoComponent.handleEvent(AddTodoUiEvent.DismissDeleteConfirm) }
                )
            }
        )
    }

    // 新建自定义标签
    if (showAddTagDialog) {
        AddTagDialog(
            onDismiss = { showAddTagDialog = false },
            onConfirm = { name, colorArgb ->
                addTodoComponent.handleEvent(AddTodoUiEvent.AddCustomTag(name, colorArgb))
                showAddTagDialog = false
            }
        )
    }

    BackHandler {
        onGoBack()
    }
}

/**
 * 表单分组：浅色玻璃卡片 + 图标标题行（原型设计稿分区块样式）
 */
@Composable
private fun TodoFormSection(
    icon: ImageVector,
    label: String,
    content: @Composable ColumnScope.() -> Unit
) {
    OneBoxSectionCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        content()
    }
}

/**
 * 日程操作 chip 的内容：图标 + 文案居中
 */
@Composable
private fun ScheduleActionLabel(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 优先级选择：高（粉）/ 中（橙）/ 低（绿）三等分 chips
 */
@Composable
private fun PrioritySelector(
    priority: Int,
    onSelect: (Int) -> Unit
) {
    val options = listOf(
        TodoTask.PRIORITY_HIGH to R.string.priority_high,
        TodoTask.PRIORITY_MEDIUM to R.string.priority_medium,
        TodoTask.PRIORITY_LOW to R.string.priority_low,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)) {
        options.forEach { (value, labelRes) ->
            val color = priorityColor(value)
            val selected = priority == value
            EnhancedChip(
                selected = selected,
                onClick = { onSelect(value) },
                selectedColor = color.copy(alpha = 0.2f),
                selectedContentColor = color,
                modifier = Modifier.weight(1f),
                label = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFlag,
                            contentDescription = null,
                            tint = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(labelRes),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            )
        }
    }
}

/**
 * 标签多选 chips：标签表驱动；编辑时任务原有标签若不在池中则并入显示；末尾「+」新建自定义标签
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagSelector(
    tags: List<TodoTag>,
    selectedTagNames: Set<String>,
    onToggle: (String) -> Unit,
    onAddTag: () -> Unit
) {
    val poolNames = tags.map { it.name }.toSet()
    val extraNames = selectedTagNames.filter { it !in poolNames }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
    ) {
        tags.forEach { tag ->
            val selected = tag.name in selectedTagNames
            val tagColor = tag.colorArgb?.let { Color(it) }
            GlassFilterChip(
                selected = selected,
                onClick = { onToggle(tag.name) },
                label = {
                    Text(
                        text = tag.name,
                        color = when {
                            selected && tagColor != null -> tagColor
                            else -> Color.Unspecified
                        }
                    )
                }
            )
        }
        // 任务原有的自定义标签（不在标签池中的历史数据）
        extraNames.forEach { name ->
            GlassFilterChip(
                selected = true,
                onClick = { onToggle(name) },
                label = { Text(text = name) }
            )
        }
        // 新建自定义标签
        EnhancedChip(
            selected = false,
            onClick = onAddTag,
            selectedColor = Color.Unspecified,
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.action_add_tag),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.action_add_tag),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        )
    }
}

/**
 * 新建自定义标签对话框：名称 + 色板选色（与主题页统一走 ColorSelectionRow，含自定义取色）
 */
@Composable
private fun AddTagDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, colorArgb: Int?) -> Unit
) {
    var tagName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(CategoryColorPalette.first()) }

    AlertDialog(
        containerColor = AppTheme.colors.getContainerSurfaceColor(),
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_add_tag_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing)) {
                OneBoxOutlinedTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    singleLine = true,
                    placeholder = { Text(text = stringResource(R.string.tag_name_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
                ColorSelectionRow(
                    value = selectedColor,
                    onValueChange = { selectedColor = it },
                    defaultColors = categoryPaletteWithThemeColors(),
                    allowAlpha = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            OnePrimaryButton(
                text = stringResource(R.string.action_save),
                onClick = { onConfirm(tagName, selectedColor.toArgb()) },
                enabled = tagName.isNotBlank()
            )
        },
        dismissButton = {
            OneSecondaryButton(
                text = stringResource(R.string.action_cancel),
                onClick = onDismiss
            )
        }
    )
}

/**
 * 所属主题单选 chips + 新建主题入口
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySelector(
    categories: List<TodoCategory>,
    selectedCategory: TodoCategory?,
    onSelect: (TodoCategory) -> Unit,
    onAddCategory: () -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
    ) {
        categories.forEach { category ->
            val selected = category.id == selectedCategory?.id
            // 与首页卡片同一套配色：自定义色优先，否则按分类 ID 分配
            val accent = categoryAccentColor(category)
            EnhancedChip(
                selected = selected,
                onClick = { onSelect(category) },
                selectedColor = accent.copy(alpha = 0.2f),
                selectedContentColor = accent,
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = category.title,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            )
        }
        // 新建主题
        EnhancedChip(
            selected = false,
            onClick = onAddCategory,
            selectedColor = Color.Unspecified,
            label = {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.action_add_category),
                    modifier = Modifier.size(16.dp)
                )
            }
        )
    }
}

@Composable
private fun ColumnScope.SpacerForBottomBar(isEditMode: Boolean) {
    androidx.compose.foundation.layout.Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isEditMode) 72.dp else OneBoxDesignSystem.blockSpacing)
    )
}

@Composable
private fun TaskDatePickerDialog(
    initialDateMillis: Long?,
    onDismiss: () -> Unit,
    onDateSelected: (Long?) -> Unit
) {
    ChineseDatePickerDialog(
        initialDateMillis = initialDateMillis ?: System.currentTimeMillis(),
        onDismiss = onDismiss,
        onDateSelected = { millis ->
            val normalizedMillis = millis.let {
                val cal = Calendar.getInstance().apply { timeInMillis = it }
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            onDateSelected(normalizedMillis)
        }
    )
}
