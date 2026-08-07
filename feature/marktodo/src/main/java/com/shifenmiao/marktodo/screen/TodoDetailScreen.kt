package com.shifenmiao.marktodo.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.picker.ChineseDatePickerDialog
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.components.TaskDateField
import com.shifenmiao.marktodo.model.TodoDetailNotice
import com.shifenmiao.marktodo.model.TodoDetailUiEvent
import com.shifenmiao.marktodo.model.TodoTask
import com.shifenmiao.marktodo.screenLogic.TodoDetailComponent
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDangerButton
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionHeader
import com.t8rin.imagetoolbox.core.ui.widget.system.OnePrimaryButton
import com.t8rin.imagetoolbox.core.ui.widget.system.OneSecondaryButton
import java.util.Calendar
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineEventAvailable

@Composable
fun TodoDetailScreen(
    component: TodoDetailComponent,
    onGoBack: () -> Unit
) {
    val uiState by component.uiState.collectAsState()
    val showDeleteConfirm by component.showDeleteConfirm.collectAsState()
    val scheduleActionNotice by component.scheduleActionNotice.collectAsState()

    val scheduleMissingDueDate = stringResource(R.string.schedule_missing_due_date)
    val scheduleCreated = stringResource(R.string.schedule_created_success)
    val scheduleCreateFailed = stringResource(R.string.schedule_created_failed)

    LaunchedEffect(scheduleActionNotice) {
        val message = when (scheduleActionNotice) {
            TodoDetailNotice.MissingDueDate -> scheduleMissingDueDate
            TodoDetailNotice.ScheduleCreated -> scheduleCreated
            TodoDetailNotice.ScheduleCreateFailed -> scheduleCreateFailed
            null -> null
        }
        if (message != null) {
            AppToastHost.showToast(message)
            component.clearScheduleActionNotice()
        }
    }

    BaseScreen(
        title = {
            Text(
                text = uiState.categoryTitle.ifEmpty { stringResource(R.string.marktodo) },
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = {
                if (uiState.isEditing) {
                    component.handleEvent(TodoDetailUiEvent.CancelEdit)
                } else {
                    onGoBack()
                }
            }) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            if (uiState.isEditing) {
                // 编辑模式下右上角显示保存按钮
                IconButton(onClick = { component.handleEvent(TodoDetailUiEvent.SaveEdit) }) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                        contentDescription = stringResource(R.string.action_confirm)
                    )
                }
            } else if (uiState.task != null) {
                // 查看模式下的操作按钮
                IconButton(onClick = { component.handleEvent(TodoDetailUiEvent.EnterEditMode) }) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.action_edit)
                    )
                }
                IconButton(onClick = { component.handleEvent(TodoDetailUiEvent.ToggleStar) }) {
                    Icon(
                        imageVector = if (uiState.task!!.isStarred) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStar else Icons.Outlined.StarBorder,
                        contentDescription = stringResource(R.string.action_star),
                        tint = if (uiState.task!!.isStarred) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { component.handleEvent(TodoDetailUiEvent.OpenScheduleHub) }) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineEventAvailable,
                        contentDescription = stringResource(R.string.action_open_schedule)
                    )
                }
                IconButton(onClick = { component.handleEvent(TodoDetailUiEvent.DeleteTask) }) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.action_delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        isShowDefaultActions = false,
        onGoBack = {
            if (uiState.isEditing) {
                component.handleEvent(TodoDetailUiEvent.CancelEdit)
            } else {
                onGoBack()
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.task != null -> {
                    if (uiState.isEditing) {
                        TodoEditContent(
                            title = uiState.editTitle,
                            note = uiState.editNote,
                            tagsText = uiState.editTagsText,
                            dueDateMillis = uiState.editDueDateMillis,
                            titleError = uiState.hasTitleError,
                            showValidationErrors = uiState.showValidationErrors,
                            onTitleChange = { component.handleEvent(TodoDetailUiEvent.UpdateEditTitle(it)) },
                            onNoteChange = { component.handleEvent(TodoDetailUiEvent.UpdateEditNote(it)) },
                            onTagsChange = { component.handleEvent(TodoDetailUiEvent.UpdateEditTags(it)) },
                            onDueDateChange = { component.handleEvent(TodoDetailUiEvent.UpdateEditDueDate(it)) },
                            onToggleDatePicker = { component.handleEvent(TodoDetailUiEvent.ToggleDatePicker(it)) },
                            showDatePicker = uiState.showDatePicker,
                            onSave = { component.handleEvent(TodoDetailUiEvent.SaveEdit) },
                            onCancel = { component.handleEvent(TodoDetailUiEvent.CancelEdit) }
                        )
                    } else {
                        TodoDetailContent(
                            task = uiState.task!!,
                            isCreatingSchedule = uiState.isCreatingSchedule,
                            onToggleComplete = { component.handleEvent(TodoDetailUiEvent.ToggleComplete) },
                            onOpenSchedule = { component.handleEvent(TodoDetailUiEvent.OpenScheduleHub) },
                            onCreateSchedule = { component.handleEvent(TodoDetailUiEvent.CreateLinkedSchedule) }
                        )
                    }
                }
            }
        }
    }

    // 删除确认对话框
    if (showDeleteConfirm && uiState.task != null) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { component.dismissDeleteConfirm() },
            title = { Text(stringResource(R.string.action_delete)) },
            text = { Text(stringResource(R.string.dialog_delete_task_message, uiState.task!!.title)) },
            confirmButton = {
                OneBoxDangerButton(
                    text = stringResource(R.string.action_delete),
                    onClick = { component.handleEvent(TodoDetailUiEvent.ConfirmDelete) }
                )
            },
            dismissButton = {
                OneSecondaryButton(
                    text = stringResource(R.string.action_cancel),
                    onClick = { component.dismissDeleteConfirm() }
                )
            }
        )
    }

    BackHandler {
        when {
            showDeleteConfirm -> component.dismissDeleteConfirm()
            uiState.isEditing -> component.handleEvent(TodoDetailUiEvent.CancelEdit)
            else -> onGoBack()
        }
    }
}

@Composable
private fun TodoDetailContent(
    task: TodoTask,
    isCreatingSchedule: Boolean,
    onToggleComplete: () -> Unit,
    onOpenSchedule: () -> Unit,
    onCreateSchedule: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(OneBoxDesignSystem.screenPadding),
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing)
    ) {
        // 标题行 + 完成状态
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)
        ) {
            androidx.compose.material3.Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleComplete() }
            )
            Text(
                text = task.title,
                style = MaterialTheme.typography.headlineSmall,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                modifier = Modifier.weight(1f)
            )
        }

        // 备注
        if (!task.note.isNullOrBlank()) {
            OneBoxSectionCard {
                OneBoxSectionHeader(title = stringResource(R.string.task_note))
                Text(
                    text = task.note,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 截止日期
        if (task.dueDate != null) {
            OneBoxSectionCard {
                OneBoxSectionHeader(title = stringResource(R.string.task_date))
                val dateStr = android.text.format.DateFormat.format("yyyy-MM-dd", task.dueDate).toString()
                val isOverdue = task.isOverdue
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodyLarge,
                    color = when {
                        isOverdue -> MaterialTheme.colorScheme.error
                        task.isDueSoon -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }

        // 标签
        if (task.tags.isNotEmpty()) {
            OneBoxSectionCard {
                OneBoxSectionHeader(title = stringResource(R.string.task_tags))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
                ) {
                    task.tags.forEach { tag ->
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .glassBackground(style = GlassStyle.Regular)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        OneBoxSectionCard {
            OneBoxSectionHeader(title = stringResource(R.string.schedule_section_title))
            Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)) {
                if (task.dueDate != null) {
                    OnePrimaryButton(
                        text = if (isCreatingSchedule) {
                            stringResource(R.string.schedule_creating)
                        } else {
                            stringResource(R.string.action_create_schedule)
                        },
                        onClick = onCreateSchedule,
                    )
                }
                OneSecondaryButton(
                    text = stringResource(R.string.action_open_schedule),
                    onClick = onOpenSchedule,
                )
            }
        }

        // 状态信息
        OneBoxSectionCard {
            OneBoxSectionHeader(title = stringResource(R.string.label_status))
            Row(
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing)
            ) {
                StatusChip(
                    label = if (task.isCompleted) stringResource(R.string.task_completed) else stringResource(R.string.task_pending),
                    isActive = task.isCompleted
                )
                if (task.isStarred) {
                    StatusChip(label = stringResource(R.string.action_star), isActive = true)
                }
                if (task.isOverdue) {
                    StatusChip(label = stringResource(R.string.status_overdue), isActive = true)
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .glassBackground(
                style = if (isActive) GlassStyle.Dense else GlassStyle.Regular
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
private fun TodoEditContent(
    title: String,
    note: String,
    tagsText: String,
    dueDateMillis: Long?,
    titleError: Boolean,
    showValidationErrors: Boolean,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onTagsChange: (String) -> Unit,
    onDueDateChange: (Long?) -> Unit,
    onToggleDatePicker: (Boolean) -> Unit,
    showDatePicker: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(OneBoxDesignSystem.screenPadding),
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing)
    ) {
        // 标题输入
        OneBoxOutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            singleLine = true,
            placeholder = { Text(text = stringResource(R.string.dialog_add_task_hint)) },
            modifier = Modifier.fillMaxWidth(),
            isError = titleError,
            supportingText = if (titleError) {
                { Text(text = stringResource(R.string.validation_task_title_required), style = MaterialTheme.typography.bodySmall) }
            } else null,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Next)
        )

        // 备注输入
        OneBoxOutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            placeholder = { Text(text = stringResource(R.string.dialog_add_task_note_hint)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 6,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Next)
        )

        // 截止日期
        TaskDateField(
            dueDateMillis = dueDateMillis,
            onClearDate = { onDueDateChange(null) },
            onSelectDate = { onToggleDatePicker(true) },
            placeholder = { Text(text = stringResource(R.string.dialog_add_task_due_date)) },
            modifier = Modifier.fillMaxWidth()
        )

        // 标签输入
        OneBoxOutlinedTextField(
            value = tagsText,
            onValueChange = onTagsChange,
            placeholder = { Text(text = stringResource(R.string.dialog_add_task_tags_hint)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(OneBoxDesignSystem.blockSpacing))
    }

    // Date picker
    if (showDatePicker) {
        ChineseDatePickerDialog(
            initialDateMillis = dueDateMillis ?: System.currentTimeMillis(),
            onDismiss = { onToggleDatePicker(false) },
            onDateSelected = { millis ->
                val normalizedMillis = millis.let {
                    val cal = Calendar.getInstance().apply { timeInMillis = it }
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    cal.timeInMillis
                }
                onDueDateChange(normalizedMillis)
            }
        )
    }
}
