package com.shifenmiao.marktodo.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.shifenmiao.marktodo.model.TodoCategory
import com.shifenmiao.marktodo.screenLogic.AddTodoComponent
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalComponentActivity
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxOutlinedTextField
import kotlinx.coroutines.launch
import java.util.Calendar
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.Check

@Composable
fun AddTodoScreen(
    addTodoComponent: AddTodoComponent,
    onGoBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalComponentActivity.current

    val uiState by addTodoComponent.uiState.collectAsState()

    BaseScreen(
        title = {
            Text(
                text = stringResource(
                    R.string.dialog_add_task_title_with_category,
                    uiState.selectedCategory?.title ?: ""
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onGoBack) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            // 右上角保存按钮
            IconButton(
                onClick = {
                    val success = addTodoComponent.handleEvent(AddTodoUiEvent.SubmitTask)
                    if (!success && uiState.selectedCategory == null) {
                        val msg = context.getString(R.string.toast_select_category_first)
                        scope.launch {
                            AppToastHost.showToast(msg)
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                    contentDescription = stringResource(R.string.action_confirm)
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
                .padding(horizontal = OneBoxDesignSystem.screenPadding)
        ) {
            // Horizontal categories list
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = OneBoxDesignSystem.screenPadding),
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(uiState.categories, key = { it.id }, contentType = { "category_chip" }) { category ->
                    CategoryChip(
                        category = category,
                        isSelected = category.id == uiState.selectedCategory?.id,
                        onClick = { addTodoComponent.handleEvent(AddTodoUiEvent.SelectCategory(category)) }
                    )
                }

                item {
                    IconButton(
                        onClick = { addTodoComponent.handleEvent(AddTodoUiEvent.AddCategoryClicked) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                            contentDescription = stringResource(R.string.action_add_category),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Task Form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing)
            ) {
                // Title input
                OneBoxOutlinedTextField(
                    value = uiState.taskTitle,
                    onValueChange = { newTitle ->
                        addTodoComponent.handleEvent(AddTodoUiEvent.UpdateTaskTitle(newTitle))
                    },
                    singleLine = true,
                    placeholder = { Text(text = stringResource(R.string.dialog_add_task_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.hasTitleError,
                    supportingText = if (uiState.hasTitleError) {
                        { Text(text = stringResource(R.string.validation_task_title_required), style = MaterialTheme.typography.bodySmall) }
                    } else null,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Next)
                )

                // Note input
                OneBoxOutlinedTextField(
                    value = uiState.taskNote,
                    onValueChange = { addTodoComponent.handleEvent(AddTodoUiEvent.UpdateTaskNote(it)) },
                    placeholder = { Text(text = stringResource(R.string.dialog_add_task_note_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Next)
                )

                // Due date section
                TaskDateField(
                    dueDateMillis = uiState.dueDateMillis,
                    onClearDate = { addTodoComponent.handleEvent(AddTodoUiEvent.UpdateDueDate(null)) },
                    onSelectDate = { addTodoComponent.handleEvent(AddTodoUiEvent.ToggleDatePicker(true)) },
                    placeholder = { Text(text = stringResource(R.string.dialog_add_task_due_date)) },
                    modifier = Modifier.fillMaxWidth()
                )

                // Tags input
                OneBoxOutlinedTextField(
                    value = uiState.taskTagsText,
                    onValueChange = { addTodoComponent.handleEvent(AddTodoUiEvent.UpdateTaskTags(it)) },
                    placeholder = { Text(text = stringResource(R.string.dialog_add_task_tags_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(OneBoxDesignSystem.blockSpacing))
            }
        }
    }

    if (uiState.showDatePicker) {
        TaskDatePickerDialog(
            initialDateMillis = uiState.dueDateMillis,
            onDismiss = { addTodoComponent.handleEvent(AddTodoUiEvent.ToggleDatePicker(false)) },
            onDateSelected = { millis ->
                addTodoComponent.handleEvent(AddTodoUiEvent.UpdateDueDate(millis))
            }
        )
    }

    BackHandler {
        onGoBack()
    }
}

@Composable
private fun CategoryChip(
    category: TodoCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer

    Row(
        modifier = Modifier
            .height(40.dp)
            .glassBackground(
                style = if (isSelected) GlassStyle.Medium else GlassStyle.Regular,
                shape = CircleShape,
                color = backgroundColor,
                borderWidth = 0.dp
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = category.title,
            color = contentColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
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
