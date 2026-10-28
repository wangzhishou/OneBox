package com.shifenmiao.marktodo.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.model.QuickAddTodoUiEvent
import com.shifenmiao.marktodo.screenLogic.QuickAddTodoComponent
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineClear
import com.wanbaohe.base.ui.picker.ChineseDatePickerDialog
import com.wanbaohe.base.ui.picker.ChineseTimePickerDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedChip
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OneBoxOutlinedTextField
import com.wanbaohe.core.ui.widget.system.OnePrimaryButton
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
import java.util.Calendar

@Composable
fun QuickAddTodoSheet(
    component: QuickAddTodoComponent,
) {
    val uiState by component.uiState.collectAsState()
    val titleFocusRequester = remember { FocusRequester() }

    LaunchedEffect(uiState.isVisible, uiState.titleFocusRequestId) {
        if (uiState.isVisible) titleFocusRequester.requestFocus()
    }

    EnhancedModalBottomSheet(
        visible = uiState.isVisible,
        onDismiss = { component.close() },
        title = stringResource(R.string.quick_add_title),
        closeEnabled = !uiState.isSaving,
        cancelable = !uiState.isSaving,
        nestedScrollEnabled = true,
        confirmBarModifier = Modifier.imePadding(),
        confirmButton = {
            OneSecondaryButton(
                text = stringResource(R.string.quick_add_save_continue),
                onClick = { component.handleEvent(QuickAddTodoUiEvent.SubmitAndContinue) },
                enabled = !uiState.isSaving,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(OneBoxDesignSystem.compactSpacing))
            OnePrimaryButton(
                text = stringResource(
                    if (uiState.isSaving) R.string.quick_add_saving else R.string.quick_add_submit
                ),
                onClick = { component.handleEvent(QuickAddTodoUiEvent.Submit) },
                enabled = !uiState.isSaving,
                modifier = Modifier.weight(1f),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = OneBoxDesignSystem.screenPadding)
                .padding(bottom = OneBoxDesignSystem.blockSpacing),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
        ) {
            OneBoxOutlinedTextField(
                value = uiState.title,
                onValueChange = { component.handleEvent(QuickAddTodoUiEvent.UpdateTitle(it)) },
                singleLine = true,
                placeholder = { Text(stringResource(R.string.quick_add_title_hint)) },
                isError = uiState.hasTitleError,
                supportingText = if (uiState.hasTitleError) {
                    { Text(stringResource(R.string.validation_task_title_required)) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocusRequester),
            )

            RecognizedTokenChips(
                tokens = uiState.recognizedTokens,
                onDismiss = { component.handleEvent(QuickAddTodoUiEvent.DismissRecognizedToken(it)) },
            )

            if (!uiState.showDetails) {
                OneSecondaryButton(
                    text = stringResource(R.string.quick_add_show_details),
                    onClick = { component.handleEvent(QuickAddTodoUiEvent.ToggleDetails) },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                OneSecondaryButton(
                    text = stringResource(R.string.quick_add_hide_details),
                    onClick = { component.handleEvent(QuickAddTodoUiEvent.ToggleDetails) },
                    modifier = Modifier.fillMaxWidth(),
                )

                QuickAddSectionLabel(stringResource(R.string.field_due_time))
                TaskDateField(
                    dueDateMillis = uiState.dueDateMillis,
                    onClearDate = { component.handleEvent(QuickAddTodoUiEvent.UpdateDueDate(null)) },
                    onSelectDate = { component.handleEvent(QuickAddTodoUiEvent.ToggleDatePicker(true)) },
                    placeholder = { Text(stringResource(R.string.dialog_add_task_due_date)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (uiState.dueDateMillis != null) {
                    TaskTimeField(
                        dueDateMillis = uiState.dueDateMillis,
                        hasDueTime = uiState.hasDueTime,
                        onSelectTime = { component.handleEvent(QuickAddTodoUiEvent.ToggleTimePicker(true)) },
                        onClearTime = { component.handleEvent(QuickAddTodoUiEvent.ClearDueTime) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                QuickAddSectionLabel(stringResource(R.string.field_priority))
                PrioritySelector(
                    priority = uiState.priority,
                    onSelect = { component.handleEvent(QuickAddTodoUiEvent.SelectPriority(it)) },
                )

                QuickAddSectionLabel(stringResource(R.string.task_tags))
                TagSelector(
                    tags = uiState.tags,
                    selectedTagNames = uiState.selectedTagNames,
                    onToggle = { component.handleEvent(QuickAddTodoUiEvent.ToggleTag(it)) },
                )

                QuickAddSectionLabel(stringResource(R.string.task_category))
                CategorySelector(
                    categories = uiState.categories,
                    selectedCategory = uiState.selectedCategory,
                    onSelect = { component.handleEvent(QuickAddTodoUiEvent.SelectCategory(it)) },
                    onSelectInbox = { component.handleEvent(QuickAddTodoUiEvent.SelectCategory(null)) },
                )

                QuickAddSectionLabel(stringResource(R.string.task_note))
                OneBoxOutlinedTextField(
                    value = uiState.note,
                    onValueChange = { component.handleEvent(QuickAddTodoUiEvent.UpdateNote(it)) },
                    placeholder = { Text(stringResource(R.string.dialog_add_task_note_hint)) },
                    minLines = 2,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    if (uiState.showDatePicker) {
        ChineseDatePickerDialog(
            initialDateMillis = uiState.dueDateMillis ?: System.currentTimeMillis(),
            onDismiss = { component.handleEvent(QuickAddTodoUiEvent.ToggleDatePicker(false)) },
            onDateSelected = { component.handleEvent(QuickAddTodoUiEvent.UpdateDueDate(it)) },
            placeAboveAll = true,
        )
    }

    if (uiState.showTimePicker) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = uiState.dueDateMillis ?: System.currentTimeMillis()
        }
        ChineseTimePickerDialog(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE),
            onTimeSelected = { hour, minute ->
                component.handleEvent(QuickAddTodoUiEvent.UpdateDueTime(hour, minute))
            },
            onDismiss = { component.handleEvent(QuickAddTodoUiEvent.ToggleTimePicker(false)) },
            placeAboveAll = true,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecognizedTokenChips(
    tokens: List<com.shifenmiao.marktodo.model.QuickAddToken>,
    onDismiss: (com.shifenmiao.marktodo.model.QuickAddToken) -> Unit,
) {
    if (tokens.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing)) {
        Text(
            text = stringResource(R.string.quick_add_recognized),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
        ) {
            tokens.forEach { token ->
                EnhancedChip(
                    selected = true,
                    onClick = { onDismiss(token) },
                    selectedColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = token.raw, maxLines = 1)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Outlined.LineClear,
                                contentDescription = stringResource(R.string.quick_add_remove_token),
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun QuickAddSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}
